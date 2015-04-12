/*******************************************************************************
 * Copyright 2015 Bin Liu (flylb1@gmail.com)
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *******************************************************************************/
package core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;

public class BitStream {
    public static final int PACKAGE_LEN = 188;
    private static Logger log = Logger.getLogger(BitStream.class);
    private static final int HEAD_HEX = 0x47;
    private static final int BUFFER_SIZE = 100 * 188;
    private static int packetNumber = 0;

    private double tsRate = 0;
    private int totalTime = 0;
    private int validTime = 0;
    private long parseLimitSize = 0;

    private File file;
    private SectionManager sectionManager;
    private IParseNotify notify;
    private TableParser parser;
    private Filter filter = new Filter();
    private List<Integer> PCR_PIDList = new ArrayList<Integer>();
    private MyStreamFilter streamFilter;

    private volatile boolean stop = false;

    public BitStream() {
        super();
        sectionManager = new SectionManager(this);
    }

    public SectionManager getSectionManager() {
        return sectionManager;
    }

    public void setSectionManager(SectionManager sectionManager) {
        this.sectionManager = sectionManager;
    }

    public Filter getFilter() {
        return filter;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public MyStreamFilter getStreamFilter() {
        return streamFilter;
    }

    public void setStreamFilter(MyStreamFilter streamFilter) {
        this.streamFilter = streamFilter;
    }

    public TableParser getParser() {
        return parser;
    }

    public void setParser(TableParser specConfig) {
        this.parser = specConfig;
        for (PidMeta pid : specConfig.getPidMetaList()) {
            if (pid.isEnable() == true) {
                filter.addPidFilter(pid.getPid().shortValue(), pid.getName());
            }
        }
    }

    private int validTSPackageHeader(byte[] header) {
        if (header == null) {
            return -1;
        }
        int pid = 0;
        int head = StringUtil.byteArrayToInt(header, 0);
        pid = (int) ((((head >> 8) << 11 >> 11)) & 0x1fff);
        if (filter.inPidFilter(pid)) {
            return pid;
        }

        return -1;// invalid pid
    }

    private int parseTSpackage(byte[] data, int count, long totalCount) {
        int retToken = 0;
        if (data == null) {
            return -1;
        }
        int pid = 0;
        int token = 0;
        int dataLen = count;
        byte[] header = new byte[4];
        long positionOffset = totalCount - dataLen;
        // log.info("positionOffset=" + positionOffset + "\ttotalCount=" +
        // totalCount + "\tdataLen=" + dataLen);//
        TSpacket tsPackage = null;
        byte[] rawData = null;
        while (token < dataLen) {
            if (data[token] == (byte) HEAD_HEX) {
                // System.out.println(token + "--" + dataLen);
                if (token + 4 >= dataLen) {
                    break;
                }
                System.arraycopy(data, token, header, 0, 4);

                pid = this.validTSPackageHeader(header);
                if (pid >= 0) {
                    tsPackage = new TSpacket(this);
                    tsPackage.setPacketNumber(packetNumber);
                    rawData = tsPackage.getRawData();
                    Arrays.fill(rawData, (byte) 0x0);
                    if (dataLen - token >= PACKAGE_LEN) {
                        System.arraycopy(data, token, rawData, 0, PACKAGE_LEN);
                        tsPackage.parseHeader();
                        // positionOffset += token;
                        tsPackage.setPositionOffset(positionOffset + token);
                        // log.info("positionOffset=" + (positionOffset + token)
                        // + "\t token=" + token);
                        token += PACKAGE_LEN;
                        if (tsPackage.processSection() != true) {
                            tsPackage = null;
                        }
                    } else {
                        // log.info("packet not complete!");
                        retToken = token;
                        token += dataLen - token;
                        break;
                    }
                } else {
                    token += PACKAGE_LEN;
                }

                packetNumber++;

            } else {
                token++;
            }
        }
        return retToken;
    }

    private synchronized void parser(File file, long maxCount) throws Exception {
        this.setFile(file);
        parseLimitSize = maxCount;
        long start = System.currentTimeMillis();
        byte[] data = new byte[BUFFER_SIZE];
        byte[] unprocessedData = new byte[BitStream.PACKAGE_LEN];
        BufferedInputStream bis = null;
        int unprocessedToken = 0;

        bis = new BufferedInputStream(new FileInputStream(file));
        int count = 0;
        long totalCount = 0;
        if (maxCount == 0) {
            maxCount = file.length();
            log.info("maxCount:" + maxCount / 1024 / 1024 + " M " + maxCount + " Byte");
        } else {
            log.info("maxCount:" + maxCount / 1024 / 1024 + " M " + maxCount + " Byte");
        }

        int lastProgress = 0;
        int currentProgress = 0;
        int readOffset = 0;
        while (!stop && (count = bis.read(data, readOffset, BUFFER_SIZE - readOffset)) > 0) {
            totalCount += count;
            parseLimitSize = totalCount;
            if (totalCount >= maxCount) {
                break;
            }

            if (stop == true) {
                log.info("stoped!");
                break;
            }

            long elaps = System.currentTimeMillis();
            // Parse 2s and did not got PCR packet,Re parse //TODO
            boolean reparsePCR = false;
            if (reparsePCR) {
                if (elaps - start > (2 * 1000) && this.getTsRate() == 0 && PCR_PIDList.size() < 2) {
                    bis.close();
                    log.info("Re Parser file:" + file.getAbsolutePath());
                    bis = new BufferedInputStream(new FileInputStream(file));
                    getFilter().pids.clear();
                    getSectionManager().reset();
                    setParser(new TableParser(this));
                    count = 0;
                    totalCount = 0;
                    start = System.currentTimeMillis();// reset start time
                    continue;
                }
            }

            unprocessedToken = parseTSpackage(data, count, totalCount);
            if (unprocessedToken > 0) {// copy unprocessed bytes to
                                       // unprocessedData
                Arrays.fill(unprocessedData, (byte) 0x0);
                System.arraycopy(data, unprocessedToken, unprocessedData, 0, count - unprocessedToken);
            }
            // log.info("count=" + count + "\ttotalCount=" + totalCount);
            // data = new byte[BUFFER_SIZE];
            Arrays.fill(data, (byte) 0x0);

            if (unprocessedToken > 0) {// copy unprocessed bytes from unprocessedData to data
                System.arraycopy(unprocessedData, 0, data, 0, count - unprocessedToken);
                readOffset = unprocessedToken;
            } else {
                readOffset = 0;
            }

            currentProgress = (int) (((float) totalCount / maxCount) * 100);
            if (currentProgress != lastProgress) {
                // log.info(currentProgress);
                if (notify != null) {
                    notify.notifyProgreess(currentProgress, "");
                }
                lastProgress = currentProgress;
            }

        }
        if ((int) tsRate <= 0) {
            validTime = 0;
        } else {
            validTime = (int) ((parseLimitSize * 8.0) / tsRate);
        }

        long end = System.currentTimeMillis();
        log.info("Time used=" + (end - start));
        log.info("Time used=" + (end - start) / 1000);
        if (notify != null) {
            notify.notifyProgreess(100, "Time used=" + (end - start) / 1000);
        }

        // Regist to bitstream parse factory
        // StreamHistory.addFile(file, this);// TODO remove it when close window
    }

    // public void saveToFile(OutputStream out) {
    // TSpacket tsPackage = null;
    // int packageListSize = packageList.size();
    // for (int i = 0; i < packageListSize; i++) {
    // tsPackage = packageList.get(i);
    // if (tsPackage == null) {
    // continue;
    // }
    // tsPackage.saveToFile(out);
    // }
    // }

    private String dumpSectionBreifInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append(sectionManager.sectionBreifInfo());
        return sb.toString();
    }

    public void dumpSections(boolean withDetail) { //
        if (sectionManager != null) {
            sectionManager.dumpSections(withDetail);
        }
    }

    public void save2History() {
        StreamHistory.addFile(file, this);
    }

    public static BitStream parseSingleFile(BitStream ts, File file, long limitSize, IParseNotify notify) {
        try {
            if (limitSize == 0) {
                limitSize = file.length();
            }
            ts.setNotify(notify);
            ts.setParser(new TableParser(ts));

            log.info("Parser file:" + file.getAbsolutePath());
            // if (file.getName().endsWith(".ts")) {
            log.info("File size=" + file.length() / 1024 / 1024 + " M " + file.length() + " Byte");
            log.info("Limit Size " + limitSize / 1024 / 1024 + " M " + limitSize + " Byte");
            ts.parser(file, limitSize);//
            log.info(ts.dumpSectionBreifInfo());
            // log.info(ts.parseResult()); }
            log.info("Parser file:" + file.getAbsolutePath() + " Finish\n");
            return ts;
        } catch (FileNotFoundException e) {
            log.error("FileNotFound:" + file.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setTsRate(double tsRate) {
        this.tsRate = tsRate;
        totalTime = (int) ((file.length() * 8) / tsRate);
        validTime = (int) ((parseLimitSize * 8) / tsRate);
    }

    public double getTsRate() {
        return tsRate;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getValidTime() {
        return validTime;
    }

    public List<Integer> getPCR_PIDList() {
        return PCR_PIDList;
    }

    public void setStop(boolean stop) {
        log.info("Set stop " + stop);
        this.stop = stop;
    }

    public String parseResult() {
        StringBuffer sb = new StringBuffer();
        sb.append("\r\n");
        sb.append("File       = " + file.getAbsolutePath() + "\r\n");
        sb.append("File  size = " + file.length() / 1024 / 1024 + " M [" + file.length() + " Byte]" + "\r\n");
        sb.append("Parse Size = " + parseLimitSize / 1024 / 1024 + " M [" + parseLimitSize + " Byte]" + "\r\n");
        sb.append(dumpSectionBreifInfo() + "\r\n");
        sb.append("TS Rate    = " + tsRate / 1000 / 1000 + " M bps" + "\r\n");
        sb.append("Total Time = " + totalTime + " s\r\n");
        return sb.toString();
    }

    public IParseNotify getNotify() {
        return notify;
    }

    public void setNotify(IParseNotify notify) {
        this.notify = notify;
    }

    public static void main(String[] args) {
        System.out.println(new File(".").getAbsoluteFile());
        String ROOT_DIR = new File(".").getAbsolutePath();

        // true :generator java files
        // false :do not generator java files
        Generator.generatorSytax(ROOT_DIR, false);
        try {
            BitStream ts = new BitStream();
            String fileName = "./42-11593V-25000.ts";
            log.info("Parse file:" + fileName);
            parseSingleFile(ts, new File(fileName), 50 * 1024 * 1024, null);

            // ts.dumpSections(false);//
            // showPatResult(ts);
            showPmtResult(ts);// Print pmt sections
            // showSdtResult(ts);// print SDT
            // showTimeResult(ts);
            // showBatResult(ts);// print SDT
            // showEventResult(ts);
            // log.info(fileName);
            showTkgs(ts);
            ts = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    private static void showTkgs(BitStream ts) {
        byte[] byteBuffer = new byte[4096 * 32];
        List<TSSection> tkgsSections = TSUtil.getSectionsByTableid(ts, 0xA7);

        Collections.sort(tkgsSections, //
                new Comparator<TSSection>() {
                    @Override
                    public int compare(TSSection o1, TSSection o2) {
                        Object root1 = o1.getRoot();
                        Object root2 = o2.getRoot();
                        int number1 = (Integer) TSUtil.getObjectByName(root1, "section_number");
                        int number2 = (Integer) TSUtil.getObjectByName(root2, "section_number");
                        return (number1 < number2) ? -1 : 1;
                    }
                });

        System.out.println("Total TKGS section number :" + tkgsSections.size());
        int destPos = 0;
        for (TSSection section : tkgsSections) {
            Object root = section.getRoot();
            int number = (Integer) TSUtil.getObjectByName(root, "section_number");
            byte[] bytes = (byte[]) TSUtil.getObjectByName(root, "tkgs_data_byte");
            log.info(number + ":" + bytes.length);
            System.arraycopy(bytes, 0, byteBuffer, destPos, bytes.length);
            destPos += bytes.length;
        }
        log.info(":" + destPos);

        // log.info("\n" + StringUtil.getHexString((byte[]) byteBuffer, 0, 2 * 1024/* destPos */, 16));

        Class<?> clazz = null;
        try {
            String clazzName = "S91_TKGS.section.tkgs_data_section";
            clazz = SyntaxBuildFactory.getClazz(clazzName);
            if (clazz == null) {
                return;
            }
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        CommonParser commonParser = null;
        Stack<List<NodeValue>> valueStack = new Stack<List<NodeValue>>();
        valueStack.push(new ArrayList<NodeValue>());
        try {
            commonParser = (CommonParser) SyntaxBuildFactory.getInstanceByClass(clazz);// using pool
            commonParser.setValueStack(valueStack);
            commonParser.reset();
            commonParser.parse(byteBuffer, destPos);
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuffer sb = new StringBuffer();
        int step = 0;
        TSUtil.dumpNode(sb, valueStack.firstElement(), step, 80);

        log.info("\n" + sb);

    }

    // private static void dumpNode(StringBuffer sb, List<NodeValue> values, int step) {
    // int formatLen = 100;
    // if (values == null) {
    // return;
    // }
    // for (NodeValue node : values) {
    // if (node.getValue() == null) {
    // continue;
    // }
    // if (node.getValue().getClass() == ArrayList.class) {
    // String name = StringUtil.formatString(StringUtil.getString(step, ' ') + node.getName(), formatLen);
    // sb.append(name + "\n");
    // step += 2;
    // dumpNode(sb, (List<NodeValue>) node.getValue(), step);
    // step -= 2;
    // } else if (node.getValue().getClass() == byte[].class) {
    // String name = StringUtil.formatString(StringUtil.getString(step, ' ') + node.getName() + "    ", formatLen);
    // String hexPreFix = StringUtil.getString(name.length(), ' ');
    // sb.append(name + StringUtil.getHexString((byte[]) node.getValue(), 16, hexPreFix) + "\n");
    // // if (name.equalsIgnoreCase("Unknow")) {
    // // System.out.println(node.getValue());
    // // }
    // } else {
    // String name = StringUtil.formatString(StringUtil.getString(step, ' ') + node.getName() + "    ", formatLen);
    // String value = StringUtil.formatString(node.getValue().toString(), 10);
    // String hexValue = StringUtil.formatString(NumberUtil.Object2Hex(node.getValue()), 10);
    // sb.append(name //
    // + value //
    // + " [0x" + hexValue + "]"//
    // + "\n");
    // }
    // }
    // }

    @SuppressWarnings("unused")
    private static void showBatResult(BitStream ts) {
        List<TSSection> batSections = TSUtil.getSectionsByTableid(ts, 74);
        if (batSections != null) {
            for (TSSection section : batSections) {
                Object root = null;
                root = section.getRoot();
                // int table_id = (Integer) TSUtil.getObjectByName(root, "table_id");
                // log.info(table_id + "" + section);
                int tsNumber = TSUtil.getObjectLenByName(root, "transport_streams");
                log.info("\n" + section);
                Integer service_id = null;
                Integer dp_ts_id = null;
                Integer dp_service_id = null;

                for (int i = 0; i < tsNumber; i++) {
                    Object tsObj = TSUtil.getObjectByNameIdx(root, "transport_streams", i);
                    int on_id = (Integer) TSUtil.getObjectByName(tsObj, "original_network_id");
                    int ts_id = (Integer) TSUtil.getObjectByName(tsObj, "transport_stream_id");
                    // log.info("on_id:" + on_id + "\t" + "ts_id:" + ts_id);
                    boolean found = false;
                    int desNumber = TSUtil.getObjectLenByName(tsObj, "descriptors");
                    service_id = null;
                    dp_ts_id = null;
                    dp_service_id = null;
                    for (int j = 0; j < desNumber; j++) {
                        Object descript = TSUtil.getObjectByNameIdx(tsObj, "descriptors", j);
                        int descriptTag = (Integer) TSUtil.getObjectByName(descript, "descriptor_tag");
                        if (descriptTag != 0x83) {// not service_descriptor
                            continue;
                        }
                        service_id = (Integer) TSUtil.getObjectByName(descript, "service_id");
                        dp_ts_id = (Integer) TSUtil.getObjectByName(descript, "dp_ts_id");
                        dp_service_id = (Integer) TSUtil.getObjectByName(descript, "dp_service_id");
                        found = true;
                    }
                    log.info(StringUtil.formatString("on_id:" + on_id, 20) //
                            + StringUtil.formatString("ts_id:" + ts_id, 20) //
                            + StringUtil.formatString("service_id:" + service_id, 20) //
                            + StringUtil.formatString("dp_ts_id:" + dp_ts_id, 20) //
                            + StringUtil.formatString("dp_service_id:" + dp_service_id, 20));

                }
            }
        }
    }

    private static void showSdtResult(BitStream ts) {
        log.info("\n\n");
        List<TSSection> sdtSections = TSUtil.getSectionsByTableid(ts, 0x42);
        if (sdtSections != null) {
            for (TSSection section : sdtSections) {
                log.info("Dump SDT");
                ts.sectionManager.dumpSection(section, true);
                List<Service> services = TSUtil.getServiceList(section);
                log.info("Dump Service information");
                for (Service service : services) {
                    log.info(service.getOnId() + " " + service.getTsId() + " " + service.getSvcId() + " " + service.getServiceName());
                }
            }
        }
    }

    private static void showPatResult(BitStream ts) {
        log.info("\n\n");
        log.info("PAT");
        List<TSSection> pmtSections = TSUtil.getPatListByExtension(ts);
        if (pmtSections != null) {
            for (TSSection patSection : pmtSections) {
                log.info(patSection);
                ts.sectionManager.dumpSection(patSection, true);
            }
        }
    }

    private static void showPmtResult(BitStream ts) {
        log.info("\n\n");
        log.info("PMT");
        List<TSSection> pmtSections = TSUtil.getPmtListByExtension(ts, -1);
        if (pmtSections != null) {
            for (TSSection pmtSection : pmtSections) {
                log.info(pmtSection);
            }
        }
    }

    @SuppressWarnings("unused")
    private static void showTimeResult(BitStream ts) {
        Calendar cal = TSUtil.getBrdcstTimeByTOTTDT(ts);
        log.info(TSUtil.calendarToString(cal, "yyyy/MM/dd hh:mm:ss E"));
    }

    public static void showEventResult(BitStream ts) { //
        SectionManager sectionManager = ts.getSectionManager();
        sectionManager.sortEvents();
        Map<Service, List<Event>> serviceEvents = sectionManager.getServiceEvents();
        log.info("On" + "\t" + "ts" + "\t" + "Svc");
        boolean printEvent = false;
        for (Service service : serviceEvents.keySet()) {
            log.info(service.getOnId() + "\t" + service.getTsId() + "\t" + service.getSvcId());
            if (printEvent) {
                List<Event> events = serviceEvents.get(service);
                for (Event event : events) {
                    Calendar endtime = (Calendar) event.getStartTime().clone();
                    endtime.add(Calendar.SECOND, event.getDuration());

                    String eventTitle = TSUtil.getEventTitle(event);
                    log.info("\t" + event.getEventId() + "\t" //
                            + TSUtil.calendarToString(event.getStartTime(), "yyyy/MM/dd hh:mm:ss") + "\t" //
                            + TSUtil.calendarToString(endtime, "yyyy/MM/dd hh:mm:ss") + "\t" //
                            + event.isPf() + "\tTitle:" + eventTitle);
                }
            }
        }
    }

}
