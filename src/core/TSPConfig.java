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
import java.util.List;

import com.thoughtworks.xstream.XStream;

public class TSPConfig {
    private List<PidMeta> PIDS = new ArrayList<PidMeta>();
    private List<TableMeta> tableIdList = new ArrayList<TableMeta>();
    private int parseSize = 50 * 1024 * 1024;
    private boolean serviceListAppSuppourt = true;
    private boolean epgAppSuppourt = true;
    private boolean identPMTUseBuffer = true;

    private static XStream xstream = new XStream();
    static {
        xstream.alias("Config", TSPConfig.class);
        xstream.alias("Table", TableMeta.class);
        xstream.alias("Pid", PidMeta.class);
    }

    private void defaultValue() {
        PIDS.add(new PidMeta(0x0000, "PAT"));
        PIDS.add(new PidMeta(0x0001, "CAT"));
        PIDS.add(new PidMeta(0x0002, "TSDT"));
        PIDS.add(new PidMeta(0x0010, "NIT, ST"));
        PIDS.add(new PidMeta(0x0011, "SDT, BAT, ST"));
        PIDS.add(new PidMeta(0x0012, "EIT, ST CIT"));
        PIDS.add(new PidMeta(0x0013, "RST, ST"));
        PIDS.add(new PidMeta(0x0014, "TDT, TOT, ST"));
        PIDS.add(new PidMeta(0x0015, "network synchronization"));
        PIDS.add(new PidMeta(0x0016, "RNT"));// (TS 102 323 [13])
        PIDS.add(new PidMeta(0x001C, "inband signalling"));
        PIDS.add(new PidMeta(0x001D, "measurement"));
        PIDS.add(new PidMeta(0x001E, "DIT"));
        PIDS.add(new PidMeta(0x001F, "SIT"));

        PIDS.add(new PidMeta(900, "CanalDigitaal SD"));
        PIDS.add(new PidMeta(901, "CanalDigitaal HD"));
        PIDS.add(new PidMeta(910, "TV VLAANDEREN SD"));
        PIDS.add(new PidMeta(911, "TV VLAANDEREN HD"));
        PIDS.add(new PidMeta(920, "TeleSAT Belgium"));
        PIDS.add(new PidMeta(921, "TeleSAT Luxemburg"));
        PIDS.add(new PidMeta(950, "AustriaSat"));

        tableIdList.add(new TableMeta(0x00, 0x00, "PAT", "program_association_section", "S10_13818.section.program_association_section"));
        tableIdList.add(new TableMeta(0x01, 0x00, "CAT", "conditional_access_section", "S10_13818.section.CA_section"));
        tableIdList.add(new TableMeta(0x02, 0x00, "PMT", "program_map_section", "S10_13818.section.TS_program_map_section"));
        // tableIdList.add(new TableMeta(0x03, 0x00, "TSDT", "transport_stream_description_section", null));//
        // tableIdList.add(new TableMeta(0x04, 0x3F, "reserved", "reserved", null));//
        tableIdList.add(new TableMeta(0x40, 0x00, "NIT_actual", "network_information_section - actual_network",
                "S20_300468.section.network_information_section"));
        tableIdList.add(new TableMeta(0x41, 0x00, "NIT_other", "network_information_section - other_network",
                "S20_300468.section.network_information_section"));

        tableIdList.add(new TableMeta(0x42, 0x00, "SDT_actual", "service_description_section - actual_transport_stream",
                "S20_300468.section.service_description_section"));
        // tableIdList.add(new TableMeta(0x43, 0x45, "reserved for future", "reserved for future use", null));//
        tableIdList.add(new TableMeta(0x46, 0x00, "SDT_other", "service_description_section - other_transport_stream",
                "S20_300468.section.service_description_section"));
        // tableIdList.add(new TableMeta(0x47, 0x49, "reserved for future", "reserved for future use", null));//
        tableIdList.add(new TableMeta(0x4A, 0x00, "BAT", "bouquet_association_section", "S20_300468.section.bouquet_association_section"));
        // tableIdList.add(new TableMeta(0x4B, 0x4D, "reserved for future", "reserved for future use", null));//

        tableIdList.add(new TableMeta(0x4E, 0x00, "EIT_actual_pf", "event_information_section - actual_transport_stream,present/following",
                "S20_300468.section.event_information_section"));
        tableIdList.add(new TableMeta(0x4F, 0x00, "EIT_other_pf", "event_information_section - other_transport_stream,present/following",
                "S20_300468.section.event_information_section"));
        tableIdList.add(new TableMeta(0x50, 0x5F, "EIT_actual_schedule", "event_information_section - actual_transport_stream,schedule",
                "S20_300468.section.event_information_section"));
        tableIdList.add(new TableMeta(0x60, 0x6F, "EIT_other_schedule", "event_information_section - other_transport_stream,schedule",
                "S20_300468.section.event_information_section"));
        tableIdList.add(new TableMeta(0x70, 0x00, "TDT", "time_date_section", "S20_300468.section.time_date_section"));
        tableIdList.add(new TableMeta(0x71, 0x00, "RST", "running_status_section", "S20_300468.section.running_status_section"));
        tableIdList.add(new TableMeta(0x72, 0x00, "stuffing_section", "stuffing_section", "S20_300468.section.stuffing_section"));
        tableIdList.add(new TableMeta(0x73, 0x00, "TOT", "time_offset_section", "S20_300468.section.time_offset_section"));
        tableIdList.add(new TableMeta(0x74, 0x00, "AIT", "application information section (TS 102 812 [15])",
                "S40_102812.section.application_information_section"));
        // tableIdList.add(new TableMeta(0x75, 0x00, "Container section", "container section (TS 102 323 [13])", null));//
        // tableIdList.add(new TableMeta(0x76, 0x00, "Related content section", "related content section (TS 102 323 [13])", null));//
        // tableIdList.add(new TableMeta(0x77, 0x00, "Content identifier section", "content identifier section (TS 102 323 [13])", null));//
        // tableIdList.add(new TableMeta(0x78, 0x00, "MPE-FEC section", "MPE-FEC section (EN 301 192 [4])", null));//
        // tableIdList.add(new TableMeta(0x79, 0x00, "Resolution notification section", "resolution notification section (TS 102 323 [13])",
        // null));
        tableIdList.add(new TableMeta(0x91, 0x00, "SGT", "Service Guide Table ASTRA_LCN_v2_4", "S60_other.section.service_guide_section"));
        tableIdList.add(new TableMeta(0xBD, 0x00, "FST", "Fastscan Services Table (FST)", "S60_other.section.FST_section"));
        tableIdList.add(new TableMeta(0xBC, 0x00, "FNT", "Fastscan Network Table (FNT)", "S60_other.section.FNT_section"));
    }

    public List<PidMeta> getPIDS() {
        return PIDS;
    }

    public void setPIDS(List<PidMeta> pIDS) {
        PIDS = pIDS;
    }

    public List<TableMeta> getTableIdList() {
        return tableIdList;
    }

    public void setTableIdList(List<TableMeta> tableIdList) {
        this.tableIdList = tableIdList;
    }

    public int getParseSize() {
        return parseSize;
    }

    public void setParseSize(int parseSize) {
        this.parseSize = parseSize;
    }

    public boolean isServiceListAppSuppourt() {
        return serviceListAppSuppourt;
    }

    public void setServiceListAppSuppourt(boolean serviceListAppSuppourt) {
        this.serviceListAppSuppourt = serviceListAppSuppourt;
    }

    public boolean isEpgAppSuppourt() {
        return epgAppSuppourt;
    }

    public void setEpgAppSuppourt(boolean epgAppSuppourt) {
        this.epgAppSuppourt = epgAppSuppourt;
    }

    public boolean isIdentPMTUseBuffer() {
        return identPMTUseBuffer;
    }

    public void setIdentPMTUseBuffer(boolean identPMTUseBuffer) {
        this.identPMTUseBuffer = identPMTUseBuffer;
    }

    private static TSPConfig fromXML(String xml) {
        TSPConfig config = (TSPConfig) xstream.fromXML(xml);
        return config;
    }

    public static String toXML(TSPConfig config) {
        String xml = xstream.toXML(config);
        return xml;
    }

    public static TSPConfig checkConfig(File configFile) {
        if (!configFile.exists()) {
            TSPConfig tspConfig = new TSPConfig();
            tspConfig.defaultValue();
            String str = TSPConfig.toXML(tspConfig);
            FileUtil.writeStringToFile(configFile.getAbsolutePath(), str);
            return tspConfig;
        } else {
            StringBuffer sb = FileUtil.readFileToStringBuffer(configFile.getAbsolutePath());
            TSPConfig newTspConfig = TSPConfig.fromXML(sb.toString());
            return newTspConfig;
        }
    }
}
