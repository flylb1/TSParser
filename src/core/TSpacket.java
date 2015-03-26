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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class TSpacket {
    private BitStream bitStream;
    private static Logger log = Logger.getLogger(TSpacket.class);
    private static byte[] HEADER_BIT_DESCRIPT = { 8, 1, 1, 1, 13, 2, 2, 4 };
    private static String sync_byte = "sync byte                   "; //
    private static String transport_error_indicator = "transport error indicator   "; //
    private static String payload_unit_start_indicator = "payload unit start indicator"; //
    private static String transport_priority = "transport priority          "; //
    private static String packet_identifier = "packet identifier           "; //
    private static String transport_scrambling_control = "transport scrambling control"; //
    private static String adaptation_field_control = "adaptation field control    "; //
    private static String continuity_counter = "continuity counter          "; //
    private static String[] HEADER_NAME = {
            //
            sync_byte,//
            transport_error_indicator,//
            payload_unit_start_indicator,//
            transport_priority,//
            packet_identifier,//
            transport_scrambling_control,//
            adaptation_field_control,//
            continuity_counter //
    };

    private int packetNumber;
    private long positionOffset;
    private byte[] rawData = new byte[BitStream.PACKAGE_LEN];
    private byte[] headerBytes = new byte[4];

    // for first packet
    private int payload_unit_start_indicator_pos = 0;
    private short point_of_field = 0;
    private int adaptation_field_control_byte = 0;
    private int adaptation_field_length = 0;
    private int tableId = 0;

    private static long position = 0; //
    private static long PCRi = 0; //

    private Map<String, Integer> header = new HashMap<String, Integer>();

    public TSpacket(BitStream ts) { //
        this.bitStream = ts;
    }

    public int getPacketNumber() {
        return packetNumber;
    }

    public void setPacketNumber(int packetNumber) {
        this.packetNumber = packetNumber;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public int getPayload_unit_start_indicator() {
        return payload_unit_start_indicator_pos;
    }

    public short getPoint_of_field() {
        return point_of_field;
    }

    public int getTableId() {
        return tableId;
    }

    public long getPositionOffset() {
        return positionOffset;
    }

    public void setPositionOffset(long positionOffset) {
        this.positionOffset = positionOffset;
    }

    public int getAdaptation_field_length() {
        return adaptation_field_length;
    }

    public int getAdaptation_field_control_byte() {
        return adaptation_field_control_byte;
    }

    public void parseHeader() { //
        System.arraycopy(rawData, 0, headerBytes, 0, 4);

        SectionParser sectionParser = new SectionParser();
        sectionParser.setBuffer(headerBytes);

        int len = HEADER_BIT_DESCRIPT.length;
        try {
            for (int i = 0; i < len; i++) {
                Integer data = (Integer) sectionParser.parseData(HEADER_BIT_DESCRIPT[i]);
                header.put(HEADER_NAME[i], data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String dump2String() { //
        StringBuffer sb = new StringBuffer();

        // log.info("packetNumber=" + this.packetNumber);
        sb.append("Packet Header\n");
        sb.append("-----------------------------------------\n");
        for (int i = 0; i < HEADER_NAME.length; i++) {
            sb.append(HEADER_NAME[i] + //
                    "(" + StringUtil.formatString(HEADER_BIT_DESCRIPT[i] + "b", 4) + "):" + //
                    StringUtil.object2hex(header.get(HEADER_NAME[i])) + "\n"//
            );
        }
        sb.append("-----------------------------------------\n");
        sb.append("Raw Data\n" + StringUtil.getHexString(getRawData(), RuntimeConfig.byteLineNumber));
        sb.append("\n\n");
        return sb.toString();
    }

    public void saveToFile(OutputStream out) { //
        FileUtil.printHexToOut(rawData, out);
    }

    public boolean processSection() { //
        payload_unit_start_indicator_pos = header.get(payload_unit_start_indicator);

        int token = 4;
        // get point of field
        byte[] ab = getRawData();
        int pid = header.get(packet_identifier);
        adaptation_field_control_byte = header.get(adaptation_field_control);

        if (RuntimeConfig.monitor_pid.contains(pid)) {
            log.info("pid=" + pid);
        }

        String PID_DESC = null;
        if (bitStream != null) {
            PID_DESC = bitStream.getFilter().pids.get(pid);
        }
        if (PID_DESC != null && PID_DESC.equals("PCR_PID")) {
            if (adaptation_field_control_byte == 2/* bin 10 adaptation_field only, no payload */
                    || adaptation_field_control_byte == 3/* bin 11 adaptation_field followed by payload */) {
                int adaptation_field_len = NumberUtil.unsignedByteToInt(ab[token]);
                // log.info("adaptation_field_len=" + adaptation_field_len);
                if (adaptation_field_len >= 7) {
                    byte[] buffer = new byte[adaptation_field_len];
                    // log.info("token=" + token + " rawlen=" + ab.length + " " + (adaptation_field_len));
                    // log.info("Raw" + this.dump2String());
                    System.arraycopy(ab, token + 1, buffer, 0, adaptation_field_len);
                    SectionParser sectionParser = new SectionParser();
                    sectionParser.setBuffer(buffer);
                    try {
                        sectionParser.parseData("skip 3 bslbf");
                        Integer PCR_flag = (Integer) sectionParser.parseData("PCR_flag 1 bslbf");
                        sectionParser.parseData("skip 4 bslbf");
                        if (PCR_flag == 1) {
                            // log.info("PCR_flag=" + PCR_flag);
                            Long program_clock_reference_base1 = (Long) sectionParser.parseData("program_clock_reference_base 32 uimsbf");
                            Integer program_clock_reference_base2 = (Integer) sectionParser
                                    .parseData("program_clock_reference_base2 1 uimsbf");
                            long program_clock_reference_base = (long) ((program_clock_reference_base1 << 1) + program_clock_reference_base2);
                            sectionParser.parseData("skip 6 bslbf");
                            Integer program_clock_reference_extension = (Integer) sectionParser
                                    .parseData("program_clock_reference_extension 9 uimsbf");
                            long _PCRi = (program_clock_reference_base * 300) + program_clock_reference_extension;
                            if (position == 0 || PCRi == 0) {
                                position = this.positionOffset;
                                PCRi = _PCRi;
                            } else {
                                double tsRate = ((this.positionOffset - position) * 8 * (27 * 1000 * 1000)) / (_PCRi - PCRi);
                                // double tsRate = ((this.positionOffset - position) * 8 * (27 * 1024 * 1024)) / (_PCRi - PCRi);
                                log.info(//
                                "PCR_base =" + program_clock_reference_base//
                                        + "  PCR_extension=" + program_clock_reference_extension//
                                        + "  position=" + this.positionOffset//
                                        + "  PCRi=" + _PCRi//
                                        + "  tsRate=" + tsRate + " [" + tsRate / 1000 / 1000 + "M]"//
                                );
                                if (bitStream != null) {
                                    bitStream.setTsRate(tsRate);
                                }

                                position = this.positionOffset;
                                PCRi = _PCRi;

                                // Remove PCR_PID pid filter
                                if (bitStream != null) {
                                    synchronized (bitStream) {
                                        Map<Integer, String> pids = bitStream.getFilter().pids;
                                        if (pids.containsValue("PCR_PID")) {
                                            for (Integer _pid : pids.keySet()) {
                                                if (pids.get(_pid).equals("PCR_PID")) {
                                                    pids.remove(_pid);
                                                    if (bitStream.getTsRate() > 0) {// Have got tsrate{
                                                        bitStream.getFilter().addPidFilter(-1, "PCR_PID");
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                position = 0;
                                PCRi = 0;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sectionParser = null;
                }
            }
            return false;
        }

        try {
            if (adaptation_field_control_byte == 2/* bin 10 adaptation_field only, no payload */
                    || adaptation_field_control_byte == 3/* bin 11 adaptation_field followed by payload */) {
                // should be skip
                adaptation_field_length = (short) NumberUtil.unsignedByteToInt(ab[token]);
                token++;
                token += adaptation_field_length;
            }

            // new table start here
            if (payload_unit_start_indicator_pos == 1) {
                // Byte point_of_field = Byte.valueOf(ab[4]);
                point_of_field = (short) NumberUtil.unsignedByteToInt(ab[token]);
                if (point_of_field > 0) {
                    // log.info("payload_unit_start_indicator_pos point_of_field=" + point_of_field);// TODO
                    bitStream.getSectionManager().sectionAppendPacket(pid, this, true);
                }

                token++;// Skip point of field
                token += point_of_field;
                if ((long) token >= 188) {
                    if (RuntimeConfig.debugMode == true) {
                        log.error("Error point_of_field pid=" + pid);
                    }
                    return false;
                }
                tableId = (int) ab[token] & 0x000000ff;

                if (RuntimeConfig.monitor_tid.contains(tableId)) {
                    log.info("monitor_tid:" + pid//
                            + "\ttableId:" + tableId + " [0x" + NumberUtil.Object2Hex(tableId & 0x000000ff) + "]");
                }

                TableMeta tableRange = bitStream.getParser().getTableConfigByTableId(tableId);
                if (tableRange != null) {
                    bitStream.getSectionManager().sectionStartPacket(pid, this);
                }
            } else {
                // If the Transport Stream packet does not carry the first byte of a PSI
                // section, the payload_unit_start_indicator value shall be '0', indicating that there is no pointer_field in the
                // payload
                bitStream.getSectionManager().sectionAppendPacket(pid, this, false);
            }
        } catch (Exception e) {
            log.info(e);
        }
        return true;
    }
}
