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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

public class TableParser {
    private static Logger log = Logger.getLogger(TableParser.class);
    private List<PidMeta> pidMetaList;
    private List<TableMeta> tableIdList = new ArrayList<TableMeta>();
    private BitStream bitStream;

    public List<PidMeta> getPidMetaList() {
        return pidMetaList;
    }

    public void setPidMetaList(List<PidMeta> pidMetaList) {
        this.pidMetaList = pidMetaList;
    }

    // public List<PidMeta> getPIDS() {
    // return pidMetaList;
    // }
    //
    // public void setPIDS(List<PidMeta> pIDS) {
    // pidMetaList = pIDS;
    // }

    public List<TableMeta> getTableIdList() {
        return tableIdList;
    }

    public void setTableIdList(List<TableMeta> tableIdList) {
        this.tableIdList = tableIdList;
    }

    public BitStream getBitStream() {
        return bitStream;
    }

    public String dumpResource() { //
        StringBuffer sb = new StringBuffer();
        int tableIdListSize = tableIdList.size();
        for (int i = 0; i < tableIdListSize; i++) {
            sb.append(tableIdList.get(i));
            sb.append("\n");
        }

        return sb.toString();
    }

    public TableMeta getTableConfigByTableId(int tableId) {
        TableMeta tableConfig = null;
        int tableIdListSize = tableIdList.size();
        for (int i = 0; i < tableIdListSize; i++) {
            tableConfig = tableIdList.get(i);
            // log.debug(tableConfig.toString());
            if (tableConfig.isEnable() == true && //
                    (//
                    tableConfig.getStart() == tableId || (tableConfig.getStart() <= tableId && tableId <= tableConfig.getEnd())//
                    )//
            ) {
                return tableConfig;
            }
        }
        return null;
    }

    public TableParser(BitStream bitStream) { //
        this.bitStream = bitStream;
        String currentDir = new File(".").getAbsolutePath();
        File configFile = new File(currentDir + "" + File.separator + "tspConfig.xml");
        TSPConfig tspConfig = TSPConfig.checkConfig(configFile);

        setPidMetaList(tspConfig.getPIDS());
        setTableIdList(tspConfig.getTableIdList());
    }

    @SuppressWarnings({ "unchecked" })
    public void parse(TSSection section) { //
        section.setHasParse(false);

        TableMeta tableConfig = getTableConfigByTableId(section.getTable_id());
        if (tableConfig == null || tableConfig.isEnable() == false) {
            return;
        }

        String fullClassName = tableConfig.getFullClassName();
        if (fullClassName == null) {
            return;
        }

        Class<?> clazz = null;
        try {
            clazz = SyntaxBuildFactory.getClazz(fullClassName);
            if (clazz == null) {
                if (RuntimeConfig.debugMode) {
                    log.info("Table Id:" + section.getTable_id() + "" + tableConfig + " not implement!!");
                }
                return;
            }
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        CommonParser commonParser = null;
        try {
            commonParser = (CommonParser) SyntaxBuildFactory.getInstanceByClass(clazz);// using pool
            commonParser.setValueStack(section.getValueStack());
            commonParser.reset();
            commonParser.setSection(section);
            // commonParser.parse(section.getSectionData().array(), section.getSection_length());//Move bellow
            List<NodeValue> root = (List<NodeValue>) commonParser.getCurrentList();
            section.setTableName(commonParser.getClass().toString());
            section.setCommonParser(commonParser);
            commonParser.parse(section.getSectionData().array(), section.getSection_length());

            // [tid=78[0x4E], ext=1101[0x44D], ver=18, sec_no=0/1

            // Map<Integer, String> pids = bitStream.getFilter().pids;
            // Get PMT NIT PID from PAT
            if (commonParser.getClass().getSimpleName().equals("program_association_section")) {
                // Add NIT PMT Filter
                List<NodeValue> programs = (List<NodeValue>) TSUtil.getObjectByName(root, "programs");
                for (NodeValue program : programs) {
                    List<NodeValue> programList = (List<NodeValue>) program.getValue();
                    int program_number = NumberUtil.getIntValue(TSUtil.getObjectByName(programList, "program_number"));
                    // log.info(program_number);
                    if (program_number == 0) {
                        int network_PID = NumberUtil.getIntValue(TSUtil.getObjectByName(programList, "network_PID"));
                        this.getBitStream().getFilter().addPidFilter(network_PID, "NIT");
                    } else {
                        int program_map_PID = NumberUtil.getIntValue(TSUtil.getObjectByName(programList, "program_map_PID"));
                        this.getBitStream().getFilter().addPidFilter(program_map_PID, "PMT pid=>" + program_map_PID);
                    }
                }
            }
            // Decode PCR_PID from PMT
            else if (commonParser.getClass().getSimpleName().equals("TS_program_map_section")) {
                if (this.getBitStream().getTsRate() <= 0) {
                    List<Integer> pcr_pidFilter = this.getBitStream().getPCR_PIDList();
                    int PCR_PID = NumberUtil.getIntValue(TSUtil.getObjectByName(root, "PCR_PID"));
                    if (!getBitStream().getFilter().getPids().containsValue("PCR_PID")) {
                        if (!pcr_pidFilter.contains(PCR_PID)) {
                            pcr_pidFilter.add(PCR_PID);// Add PCR_PID to bitstream.PCRlist
                            getBitStream().getFilter().addPidFilter(PCR_PID, "PCR_PID");
                            log.info("PCR_PID=" + PCR_PID + " [0x" + NumberUtil.Object2Hex(PCR_PID) + "]");
                        }
                    }
                }
                // Add SGT pid filter
                List<NodeValue> streams = (List<NodeValue>) TSUtil.getObjectByName(section.getRoot(), "streams");
                if (streams != null) {
                    for (NodeValue pmtNode : streams) {
                        int stream_type = (Integer) TSUtil.getObjectByName(pmtNode.getValue(), "stream_type");
                        int elementary_PID = (Integer) TSUtil.getObjectByName(pmtNode.getValue(), "elementary_PID");
                        if (stream_type == 0x5) {// Found SGT pid
                            if (RuntimeConfig.debugMode) {
                                log.info("PMT pid=" + section.getPid() + " stream_type=" + stream_type + "\t" + "SGT PID=" + elementary_PID //
                                        + " [0x" + NumberUtil.Object2Hex(elementary_PID) + "]");
                            }
                            this.getBitStream().getFilter().addPidFilter(elementary_PID, "SGT pid");
                        }
                        if (stream_type == 0xB) {// Found SGT pid
                            if (RuntimeConfig.debugMode) {
                                log.info("PMT pid=" + section.getPid() + " stream_type=" + stream_type + "\t" + "DSMCC PID="
                                        + elementary_PID //
                                        + " [0x" + NumberUtil.Object2Hex(elementary_PID) + "]");
                            }
                            this.getBitStream().getFilter().addPidFilter(elementary_PID, "DSMCC pid");
                        }
                    }
                }

                // Add CA_PID filter
                List<NodeValue> descriptors = (List<NodeValue>) TSUtil.getObjectByName(root, "descriptors");
                if (descriptors != null) {
                    for (NodeValue node : descriptors) {
                        if (node.getName().equalsIgnoreCase("CA_descriptor")) {
                            int ca_pid = (Integer) TSUtil.getObjectByName(node.getValue(), "CA_PID");
                            log.debug(node.getName() + "\t CA_PID=" + ca_pid);
                            this.getBitStream().getFilter().addPidFilter(ca_pid, "CA_PID");
                        }
                    }
                }

            } else if (//
            (//
                    (RuntimeConfig.TSP_Config != null && RuntimeConfig.TSP_Config.isEpgAppSuppourt())//
                    || (RuntimeConfig.TSP_Config == null)//
                    )//
                    && (commonParser.getClass().getSimpleName().equals("event_information_section"))//
            ) //
            {// IS EIT
                SectionManager sm = bitStream.getSectionManager();
                Service service = null;
                commonParser = section.getCommonParser();
                int onId = (Integer) TSUtil.getObjectByName(root, "original_network_id");
                int tsId = (Integer) TSUtil.getObjectByName(root, "transport_stream_id");
                int svcId = (Integer) TSUtil.getObjectByName(root, "service_id");

                MyStreamFilter myStreamFilter = bitStream.getStreamFilter();
                if (myStreamFilter != null) {// using filter ignore unused EIT
                    boolean match = true;
                    if (myStreamFilter.onid != -1 && onId != myStreamFilter.onid) {// Not match
                        match = false;
                    }
                    if (myStreamFilter.tsid != -1 && tsId != myStreamFilter.tsid) {// Not match
                        match = false;
                    }
                    if (myStreamFilter.svcid != -1 && svcId != myStreamFilter.svcid) {// Not match
                        match = false;
                    }
                    if (match == false) {
                        log.info("Ignore EIT by filter\t" + section.shortName());
                        section.setEITIgnoreByFilter(true);
                        return;
                    }
                }

                int eventId = 0;
                boolean isPf = (section.getTable_id() == 0x4e || section.getTable_id() == 0x4f);
                service = new Service(onId, tsId, svcId);
                int eventNumber = TSUtil.getObjectLenByName(root, "events");
                Event event = null;
                int hour = 0;
                int minute = 0;
                int second = 0;
                for (int i = 0; i < eventNumber; i++) {
                    Object eventObj = TSUtil.getObjectByNameIdx(root, "events", i);
                    eventId = (Integer) TSUtil.getObjectByName(eventObj, "event_id");
                    Object start_timeObj = TSUtil.getObjectByName(eventObj, "start_time");
                    Calendar startTime = TSUtil.bufferToCal((byte[]) start_timeObj);
                    int duration = ((Long) TSUtil.getObjectByName(eventObj, "duration")).intValue();
                    hour = Integer.parseInt(Integer.toString((duration & 0x00FF0000) >> 16, 16));// Get hour from BCD
                    minute = Integer.parseInt(Integer.toString((duration & 0x0000FF00) >> 8, 16));
                    second = Integer.parseInt(Integer.toString((duration & 0x000000FF) >> 0, 16));
                    duration = hour * 3600 + minute * 60 + second;// To second
                    // log.info(onId + "\t" + tsId + "\t" + svcId + "\t" + eventId + "\t" //
                    // + TSUtil.calendarToString(startTime, "yyyy/MM/dd hh:mm:ss") + "\t" + isPf);
                    event = new Event(eventId, startTime, (int) duration);
                    Calendar endtime = (Calendar) startTime.clone();
                    endtime.add(Calendar.SECOND, duration);
                    event.setEndTime(endtime);
                    event.setPf(isPf);
                    event.setValue(eventObj);
                    event.setSection(section);
                    sm.addEvents(service, event);
                }
            }

            // Parse SGT pid from NIT linkage_descriptor
            boolean sgtFromNit = false;
            if (sgtFromNit) {
                int linkage_type = 0;
                int service_id = 0;
                List<TSSection> pmtSections = null;
                List<TSSection> patSections = null;

                SectionManager sm = this.getBitStream().getSectionManager();

                List<NodeValue> patRoot = null;

                List<NodeValue> pmtRoot = null;

                int program_map_PID = -1;

                if (section.getPid() == 0x0010/* commonParser.getName().equals("network_information_section") */) {// IS NIT
                    List<NodeValue> descriptors = (List<NodeValue>) TSUtil.getObjectByName(root, "descriptors");
                    for (NodeValue node : descriptors) {
                        if (!node.getName().equals("linkage_descriptor")) {
                            continue;
                        }
                        linkage_type = (Integer) TSUtil.getObjectByName(node.getValue(), "linkage_type");
                        if (linkage_type < 0x90 || linkage_type > 0x93) {// Has linkage_descriptor type 0x90--0x93 SGT
                            continue;
                        }

                        service_id = (Integer) TSUtil.getObjectByName(node.getValue(), "service_id");
                        log.debug("linkage_type in 0x90--0x93 service_id=" + service_id);

                        // Find PAT
                        patSections = sm.getSectionsBytableId(0x0);
                        if (patSections == null) {
                            continue;
                        }
                        for (TSSection patSection : patSections) {
                            patRoot = patSection.getRoot();
                            List<NodeValue> programs = (List<NodeValue>) TSUtil.getObjectByName(patRoot, "programs");
                            for (NodeValue program : programs) {
                                int program_number = (Integer) TSUtil.getObjectByName(program.getValue(), "program_number");
                                if (program_number == service_id) {
                                    program_map_PID = (Integer) TSUtil.getObjectByName(program.getValue(), "program_map_PID");
                                    this.getBitStream().getFilter().addPidFilter(program_map_PID, "PMT pid");
                                    break;
                                }
                            }

                        }

                        // Find PID in PMT
                        pmtSections = sm.getSectionsBytableId(0x2);
                        if (pmtSections == null) {
                            continue;
                        }
                        for (TSSection pmtSection : pmtSections) {
                            // log.info("pmtSection.getPid()=" + pmtSection.getPid());
                            if (pmtSection.getPid() == program_map_PID) {
                                pmtRoot = pmtSection.getRoot();
                                List<NodeValue> streams = (List<NodeValue>) TSUtil.getObjectByName(pmtRoot, "streams");
                                for (NodeValue pmtNode : streams) {
                                    int stream_type = (Integer) TSUtil.getObjectByName(pmtNode.getValue(), "stream_type");
                                    int elementary_PID = (Integer) TSUtil.getObjectByName(pmtNode.getValue(), "elementary_PID");
                                    if (stream_type == 0x5) {// Found SGT pid
                                        log.debug("stream_type=" + stream_type + "\t" + "SGT PID=" + elementary_PID //
                                                + " [0x" + NumberUtil.Object2Hex(elementary_PID) + "]");
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }// end if (sgtFromNit)

            section.setHasParse(true);

        } catch (InstantiationException e) {
            e.printStackTrace();
            section.setHasParse(false);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            section.setHasParse(false);
        } catch (Exception e) {
            // section.setTableName(commonParser.getClass().toString());
            section.setHasParse(false);
            if (RuntimeConfig.debugMode) {
                e.printStackTrace();
                log.info(section);
                log.info(section.dumpTextResult());
                log.info(section.dumpSectionData());
                // log.info(section.dumpSectionRawPacket());
            } else {
                log.error("Parse error:" + section.shortName());
            }
        } finally {
            if (RuntimeConfig.storeRawData == false) {
                section.clearRawData();
            }
            if (commonParser.getClass().getSimpleName().equals("event_information_section")) {
                commonParser = section.getCommonParser();
                List<NodeValue> root = (List<NodeValue>) commonParser.getValueStack().get(0);
                int onId = 0;
                int tsId = 0;
                int svcId = 0;
                try {
                    onId = (Integer) TSUtil.getObjectByName(root, "original_network_id");
                    tsId = (Integer) TSUtil.getObjectByName(root, "transport_stream_id");
                    svcId = (Integer) TSUtil.getObjectByName(root, "service_id");
                } catch (Exception e) {
                    log.equals(e.getMessage());
                    return;
                }

                MyStreamFilter myStreamFilter = bitStream.getStreamFilter();
                if (myStreamFilter != null) {// using filter ignore unused EIT
                    boolean match = true;
                    if (myStreamFilter.onid != -1 && onId != myStreamFilter.onid) {// Not match
                        match = false;
                    }
                    if (myStreamFilter.tsid != -1 && tsId != myStreamFilter.tsid) {// Not match
                        match = false;
                    }
                    if (myStreamFilter.svcid != -1 && svcId != myStreamFilter.svcid) {// Not match
                        match = false;
                    }
                    if (match == false) {
                        log.info("Ignore EIT by filter\t" + section.shortName());
                        section.setEITIgnoreByFilter(true);
                        // return;
                    }
                }
            }
        }
    }

    public void dumpInfo() { //
        log.info("Table\t=>Pid");
        for (PidMeta pid : pidMetaList) {
            log.info(StringUtil.formatString(pid.getName(), 20) + "\t=>" + //
                    StringUtil.formatString("" + pid.getPid().shortValue(), 3) + //
                    "(0x" + Integer.toHexString(pid.getPid().shortValue()).toUpperCase() + ")");
        }
    }
}
