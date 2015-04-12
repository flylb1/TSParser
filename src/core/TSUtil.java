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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

@SuppressWarnings("unchecked")
public class TSUtil {
    private static Logger log = Logger.getLogger(TSUtil.class);

    // private static Map<String, String> _tempLangMap = new HashMap<String, String>();

    private static Calendar getDefaultCal() {
        Calendar calendar = new GregorianCalendar();
        return calendar;
    }

    public static List<TSSection> getSectionsByTableid(BitStream bitStream, int tableId) { //
        SectionManager sectionManager = bitStream.getSectionManager();
        List<TSSection> sections = sectionManager.getSectionsBytableId(tableId);
        if (sections == null) {
            return null;
        }
        List<TSSection> dest = new ArrayList<TSSection>(sections);
        Collections.sort(dest);
        return dest;
    }

    public static List<Service> getServiceList(TSSection section) {
        int onId = 0;
        int tsId = 0;
        int svcId = 0;
        String serviceName = null;
        List<Service> services = new ArrayList<Service>();
        Object root = null;
        try {
            root = section.getRoot();
            if (root == null) {
                return services;
            }

            onId = (Integer) getObjectByName(root, "original_network_id");
            tsId = (Integer) getObjectByName(root, "transport_stream_id");

            int serviceNumber = getObjectLenByName(root, "services");

            for (int i = 0; i < serviceNumber; i++) {
                Object serviceObj = getObjectByNameIdx(root, "services", i);
                svcId = (Integer) getObjectByName(serviceObj, "service_id");
                int desNumber = getObjectLenByName(serviceObj, "descriptors");
                for (int j = 0; j < desNumber; j++) {
                    Object descript = getObjectByNameIdx(serviceObj, "descriptors", j);
                    if (descript == null) {
                        continue;
                    }
                    int descriptTag = (Integer) getObjectByName(descript, "descriptor_tag");
                    if (descriptTag != 0x48) {// not service_descriptor
                        continue;
                    }
                    Object serviceNameBytes = getObjectByName(descript, "service_name");
                    if (serviceNameBytes != null && serviceNameBytes.getClass() == byte[].class) {
                        serviceName = TextUtil.getTextFromByte((byte[]) serviceNameBytes, (List<NodeValue>) descript);
                        // log.info(service_id + "\t" + TextUtil.getTextFromByte((byte[]) serviceName));
                    }
                    Service servcie = new Service(onId, tsId, svcId);
                    servcie.setServiceName(serviceName);
                    services.add(servcie);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return services;
    }

    public static List<TSSection> getPatListByExtension(BitStream bitStream) {
        List<TSSection> sections = getSectionsByTableid(bitStream, 0x0);
        if (sections == null) {
            return null;
        }
        List<TSSection> patSections = new ArrayList<TSSection>();
        for (TSSection section : sections) {
            patSections.add(section);
        }
        return patSections;
    }

    public static List<TSSection> getPmtListByExtension(BitStream bitStream, int extensionId) {
        List<TSSection> sections = getSectionsByTableid(bitStream, 0x2);
        if (sections == null) {
            return null;
        }
        List<TSSection> pmtSections = new ArrayList<TSSection>();
        for (TSSection section : sections) {
            if (section.getTable_id_extension() != extensionId && extensionId != -1) {
                continue;
            }
            pmtSections.add(section);
        }
        return pmtSections;
    }

    private static Calendar parseMJD2cal(int MJD) {
        int Y_ = (int) ((MJD - 15078.2) / 365.25);
        int M_ = (int) ((MJD - 14956.1 - (Y_ * 365.25)) / 30.6001);
        int D_ = (int) (MJD - 14956 - (int) (Y_ * 365.25) - (int) (M_ * 30.6001));

        int K = 0;
        if (M_ == 14 || M_ == 15) {
            K = 1;
        } else {
            K = 0;
        }
        int Y = Y_ + K + 1900;
        int M = M_ - 1 - K * 12;
        Calendar cal = TSUtil.getDefaultCal();
        // month the value used to set the MONTH calendar field. Month value is 0-based. e.g., 0 for January
        cal.set(Y, M - 1, D_);
        return cal;
    }

    public static Calendar bufferToCal(byte[] dt) {
        Calendar cal = getDefaultCal();
        try {
            if (dt.length == 5) {
                int MJD = (dt[0] & 0x000000FF) << 8;
                MJD += (dt[1] & 0x000000FF);
                cal = parseMJD2cal(MJD);
            }

            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(Integer.toHexString(dt[2] & 0x000000FF)));
            cal.set(Calendar.MINUTE, Integer.parseInt(Integer.toHexString(dt[3] & 0x000000FF)));
            cal.set(Calendar.SECOND, Integer.parseInt(Integer.toHexString(dt[4] & 0x000000FF)));
        } catch (NumberFormatException e) {
            log.info("NumberFormatException");
        }
        return cal;
    }

    public static String calendarToString(Calendar cal, String format) {
        StringBuffer sb = new StringBuffer();
        if (cal == null) {
            return "";
        }
        if (format.indexOf("yyyy/") != -1) {
            sb.append(cal.get(Calendar.YEAR));
            sb.append("/");
        }
        if (format.indexOf("MM/") != -1) {
            sb.append(cal.get(Calendar.MONTH) + 1);
            sb.append("/");
        }
        if (format.indexOf("dd") != -1) {
            sb.append(cal.get(Calendar.DATE));
            sb.append(" ");
        }
        if (format.indexOf("hh") != -1) {
            if (format.indexOf("hh:") != -1) {
                sb.append(cal.get(Calendar.HOUR_OF_DAY));
                sb.append(":");
            } else {
                sb.append(cal.get(Calendar.HOUR_OF_DAY));
            }
        }
        if (format.indexOf("mm") != -1) {
            if (format.indexOf("mm:") != -1) {
                sb.append(cal.get(Calendar.MINUTE));
                sb.append(":");
            } else {
                sb.append(cal.get(Calendar.MINUTE));
            }
        }

        if (format.indexOf("ss") != -1) {
            sb.append(cal.get(Calendar.SECOND));
        }

        return sb.toString();
    }

    public static Calendar getBrdcstTimeByTOTTDT(BitStream bitStream) {
        Calendar cal = null;
        TSSection timeSection = null;
        CommonParser commonParser = null;
        Object root = null;
        List<TSSection> sections = null;
        sections = bitStream.getSectionManager().getSectionsBytableId(0x73);// TOT
        if (sections == null) {
            sections = bitStream.getSectionManager().getSectionsBytableId(0x70);// TDT
        }
        if (sections == null || sections.size() == 0) {
            return null;
        }
        timeSection = sections.get(0);
        commonParser = timeSection.getCommonParser();
        if (commonParser == null) {
            return null;
        }
        root = timeSection.getRoot();
        if (root == null) {
            return null;
        }
        Object dtObject = getObjectByName(root, "UTC_time");
        if (dtObject.getClass() == byte[].class) {
            byte[] dt = (byte[]) dtObject;
            cal = TSUtil.bufferToCal(dt);
            log.debug(TSUtil.calendarToString(cal, "yyyy/MM/dd hh:mm:ss"));
        }
        return cal;
    }

    public static String getEventTitle(Event event) {
        int ISO_639_language_code_raw = 0;
        StringBuffer event_name_char = new StringBuffer();
        TSSection eitSection = null;
        CommonParser commonParser = null;

        eitSection = event.getSection();
        if (eitSection == null) {
            return "";
        }
        commonParser = eitSection.getCommonParser();
        if (commonParser == null) {
            return "";
        }
        Object rootObj = event.getValue();
        if (rootObj == null) {
            return "";
        }
        int descriptorsLen = getObjectLenByName(rootObj, "descriptors");
        byte[] ISO_639_language_code_bytes = new byte[3];
        String ISO_639_language_code = null;
        try {
            for (int j = 0; j < descriptorsLen; j++) {
                Object descript = getObjectByNameIdx(rootObj, "descriptors", j);
                int descriptTag = (Integer) getObjectByName(descript, "descriptor_tag");
                if (descriptTag != 0x4d) {// not service_descriptor
                    continue;
                }
                ISO_639_language_code_raw = ((Long) getObjectByName(descript, "ISO_639_language_code")).intValue();
                ISO_639_language_code_bytes[0] = (byte) (ISO_639_language_code_raw >> 16);
                ISO_639_language_code_bytes[1] = (byte) (ISO_639_language_code_raw >> 8);
                ISO_639_language_code_bytes[2] = (byte) (ISO_639_language_code_raw >> 0);
                ISO_639_language_code = new String(ISO_639_language_code_bytes);
                byte[] event_name_char_bytes = (byte[]) getObjectByName(descript, "event_name_char");
                event_name_char.append(TextUtil.getTextFromByte(event_name_char_bytes, (List<NodeValue>) descript));
                event_name_char.append("[" + ISO_639_language_code + "]");
            }
        } catch (Exception e) {
            // e.printStackTrace();
            log.error("parse data error");
        }

        return event_name_char.toString();
    }

    public static String getEventDetail(Event event) {
        StringBuffer sb = new StringBuffer();
        sb.append("Start:" + calendarToString(event.getStartTime(), "yyyy/MM/dd hh:mm:ss") + "    ");
        sb.append("End:" + calendarToString(event.getEndTime(), "yyyy/MM/dd hh:mm:ss") + "\r\n");
        // sb.append("Title:" + getEventTitle(event) + "\r\n");
        // sb.append("Detail:");

        int ISO_639_language_code_raw = 0;
        TSSection eitSection = null;
        CommonParser commonParser = null;

        eitSection = event.getSection();
        if (eitSection == null) {
            return "";
        }
        commonParser = eitSection.getCommonParser();
        if (commonParser == null) {
            return "";
        }
        Object rootObj = event.getValue();
        if (rootObj == null) {
            return "";
        }
        int descriptorsLen = getObjectLenByName(rootObj, "descriptors");
        byte[] ISO_639_language_code_bytes = new byte[3];
        String ISO_639_language_code = null;
        try {
            for (int j = 0; j < descriptorsLen; j++) {
                Object descript = getObjectByNameIdx(rootObj, "descriptors", j);
                int descriptTag = (Integer) getObjectByName(descript, "descriptor_tag");
                if (descriptTag != 0x4d) {// not service_descriptor
                    continue;
                }
                ISO_639_language_code_raw = ((Long) getObjectByName(descript, "ISO_639_language_code")).intValue();
                ISO_639_language_code_bytes[0] = (byte) (ISO_639_language_code_raw >> 16);
                ISO_639_language_code_bytes[1] = (byte) (ISO_639_language_code_raw >> 8);
                ISO_639_language_code_bytes[2] = (byte) (ISO_639_language_code_raw >> 0);
                ISO_639_language_code = new String(ISO_639_language_code_bytes);
                byte[] event_name_char_bytes = (byte[]) getObjectByName(descript, "event_name_char");
                sb.append("[event_name]");
                sb.append(TextUtil.getTextFromByte(event_name_char_bytes, (List<NodeValue>) descript));
                sb.append("[" + ISO_639_language_code + "]");

                byte[] text_char_bytes = (byte[]) getObjectByName(descript, "text_char");
                String text_char = TextUtil.getTextFromByte(text_char_bytes, (List<NodeValue>) descript);
                sb.append("[event_text]");
                sb.append(text_char);
                if (text_char != null) {
                    sb.append("[" + ISO_639_language_code + "]");
                }
                sb.append("\r\n");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.append("\r\n");

        return sb.toString();
    }

    public static int getObjectLenByName(Object objectList, String name) {
        if (objectList == null || name == null) {
            return 0;
        }
        if (objectList.getClass() != ArrayList.class) {
            return 0;
        }

        List<NodeValue> list = (List<NodeValue>) objectList;

        for (NodeValue node : list) {
            if (node.getName().equals(name)) {
                Object obj = node.getValue();
                if (obj.getClass() == ArrayList.class) {
                    return ((List<NodeValue>) obj).size();
                }
            }
        }
        return 0;
    }

    public static Object getObjectByNameIdx(Object objectList, String name, int index) {
        if (objectList.getClass() == ArrayList.class) {
            return getObjectByName((List<NodeValue>) objectList, name, index);
        }
        return null;
    }

    private static Object getObjectByName(Object objectList, String name, int index) {
        if (objectList.getClass() == ArrayList.class) {
            Object object = getObjectByName((List<NodeValue>) objectList, name);
            if (object.getClass() == ArrayList.class) {
                List<NodeValue> list = (List<NodeValue>) object;
                return list.get(index).getValue();
            }
        }
        return null;
    }

    public static Object getObjectByName(Object objectList, String name) {
        if (objectList == null) {
            return null;
        }
        if (objectList.getClass() == ArrayList.class) {
            return getObjectByName((List<NodeValue>) objectList, name);
        }
        return null;
    }

    private static Object getObjectByName(List<NodeValue> nodeList, String name) {
        if (nodeList == null || name == null) {
            return null;
        }
        Object value = null;
        for (NodeValue node : nodeList) {
            if (node.getName().equals(name)) {
                value = node.getValue();
                break;
            }
        }

        return value;
    }

    public static boolean sectionInTime(TSSection tsSection, int time, BitStream bitStream) {
        boolean valid = false;
        if (tsSection == null || bitStream == null) {
            return valid;
        }
        List<Long> list = tsSection.getTransmitTimeList();
        long sectionMaxOffset = 0;
        if (list.size() > 0) {
            sectionMaxOffset = list.get(0);// The first section transfer offset
        }
        if (tsSection.isHasParse() == true || RuntimeConfig.showErrorSection == true) {
            if (//
            (sectionMaxOffset * 8) < ((time) * bitStream.getTsRate())//
                    || (((int) (bitStream.getTsRate())) <= 0)) {// valid position
                valid = true;
            }
        }
        return valid;
    }

    public static void dumpNode(StringBuffer sb, List<NodeValue> values, int step, int formatLen) {
        // int formatLen = 100;
        if (values == null) {
            return;
        }
        for (NodeValue node : values) {
            if (node.getValue() == null) {
                continue;
            }
            if (node.getValue().getClass() == ArrayList.class) {
                String name = StringUtil.formatString(StringUtil.getString(step, ' ') + node.getName(), formatLen);
                sb.append(name + "\n");
                step += 2;
                dumpNode(sb, (List<NodeValue>) node.getValue(), step, formatLen);
                step -= 2;
            } else if (node.getValue().getClass() == byte[].class) {
                String name = StringUtil.formatString(StringUtil.getString(step, ' ') + node.getName() + "    ", formatLen);
                String hexPreFix = StringUtil.getString(name.length(), ' ');
                sb.append(name + StringUtil.getHexString((byte[]) node.getValue(), 16, hexPreFix) + "\n");
                // if (name.equalsIgnoreCase("Unknow")) {
                // System.out.println(node.getValue());
                // }
            } else {
                String name = StringUtil.formatString(StringUtil.getString(step, ' ') + node.getName() + "    ", formatLen);
                String value = StringUtil.formatString(node.getValue().toString(), 10);
                String hexValue = StringUtil.formatString(NumberUtil.Object2Hex(node.getValue()), 10);
                sb.append(name //
                        + value //
                        + " [0x" + hexValue + "]"//
                        + "\n");
            }
        }
    }

}
