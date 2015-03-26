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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class SectionManager { //
    private static Logger log = Logger.getLogger(SectionManager.class);
    private List<TSSection> sectionList = new ArrayList<TSSection>();
    private Map<Integer, TSSection> pidSection = new HashMap<Integer, TSSection>();
    private Map<Integer, List<TSSection>> tableidSection = new HashMap<Integer, List<TSSection>>();
    private Map<Service, List<Event>> serviceEvents = new HashMap<Service, List<Event>>();
    private BitStream bitStream;

    public SectionManager(BitStream ts) {
        this.bitStream = ts;
    }

    public List<TSSection> getSectionList() {
        return sectionList;
    }

    public BitStream getBitStream() {
        return bitStream;
    }

    public Map<Integer, List<TSSection>> getTableidSection() {
        return tableidSection;
    }

    public Map<Service, List<Event>> getServiceEvents() {
        return serviceEvents;
    }

    public void setServiceEvents(Map<Service, List<Event>> serviceEvents) {
        this.serviceEvents = serviceEvents;
    }

    void sectionStartPacket(int pid, TSpacket tsPacket) {
        TSSection section = new TSSection(bitStream, pid);
        try {
            section.addTsPacket(tsPacket, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int pos = sectionList.indexOf(section);
        if (pos >= 0) {// section already in list
            TSSection findSection = sectionList.get(pos);
            findSection.getTransmitTimeList().add(section.getPositionOffset());
            if (findSection.isHasParse() == false) {// not parsed ,maybe section data not enough,maybe parse error
                sectionList.remove(pos);
                List<TSSection> tableIdSections = tableidSection.get(section.getTable_id());
                if (tableIdSections != null) {
                    tableIdSections.remove(section);
                }
            } else {
                section = null;
                return;
            }
        }

        section.getTransmitTimeList().add(section.getPositionOffset());
        // One Section should be transfer over
        // before an other section start
        pidSection.put(pid, section);
        section.setPid(pid);

        // Section transfer over,parse section here
        // log.info(section.getGot_private_section_length() + "\t" + section.getSection_length() + "\t" + section);

        if (section.getGot_private_section_length() >= section.getSection_length() //
                && section.getSection_length() > 0) {
            log.debug(section.getGot_private_section_length() + "\t" + section.getSection_length() + "\t" + section);
            this.parseSection(section);// Parse section data here

            if (section.isIgnoreByFilter() == false) {// for parse ok section or error section put into sectionList
                sectionList.add(section);
            }
            if (pidSection.containsKey(pid)) {
                pidSection.remove(pid);
            }
        }
    }

    void sectionAppendPacket(int pid, TSpacket tsPacket, boolean lastPacketWithPointOfField) {
        TSSection section = pidSection.get(pid);
        if (section != null) {
            try {
                section.addTsPacket(tsPacket, lastPacketWithPointOfField);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Section tranfer over,parse section here
            if (section.getGot_private_section_length() >= section.getSection_length() //
                    && section.getSection_length() > 0) {
                // log.info(section.getGot_private_section_length() + "\t" + section.getSection_length() + "\t" + section);
                int pos = sectionList.indexOf(section);
                if (pos >= 0) {// section already in list
                    TSSection findSection = sectionList.get(pos);
                    findSection.getTransmitTimeList().add(section.getPositionOffset());
                    if (findSection.isHasParse() == false) {// not parsed ,maybe section data not enough,maybe parse error
                        sectionList.remove(pos);
                        List<TSSection> tableIdSections = tableidSection.get(section.getTable_id());
                        if (tableIdSections != null) {
                            tableIdSections.remove(section);
                        }
                    } else {
                        section = null;
                        return;
                    }
                }

                this.parseSection(section);
                if (!section.isIgnoreByFilter()) {// for success parse section or error section put into sectionList
                    sectionList.add(section);
                }
                if (pidSection.containsKey(pid)) {
                    pidSection.remove(pid);
                }
                // log.info("End  section");
            }
        }
    }

    /**
     * Parse section
     * 
     * @param section
     */
    private void parseSection(TSSection section) {
        TableParser tableParser = bitStream.getParser();

        int tableId = section.getTable_id();
        List<TSSection> sections = tableidSection.get(tableId);
        if (sections == null) {
            sections = new ArrayList<TSSection>();
            tableidSection.put(tableId, sections);
        }
        try {
            log.debug("parseSection tableid=" + tableId);
            if (RuntimeConfig.monitor_tid.contains(tableId)) {
                System.out.println("Got tableid=" + tableId);
            }
            tableParser.parse(section);
        } catch (Exception e) {
            log.info(section.dumpTextResult());
            log.info(section.dumpSectionData());
            e.printStackTrace();
        }

        if (!section.isIgnoreByFilter()) {// for success parse section or error section put into sectionList
            sections.add(section);
        }
    }

    public String sectionBreifInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("Total section number(No duplicator):" + sectionList.size() + "\t");
        List<Long> timeList = null;
        int totalSectionNumber = 0;
        int sectionListSize = sectionList.size();
        for (int i = 0; i < sectionListSize; i++) {
            timeList = sectionList.get(i).getTransmitTimeList();
            totalSectionNumber += timeList.size();
        }
        sb.append("Total section number:" + totalSectionNumber /* + "\n" */);
        return sb.toString();
    }

    public List<TSSection> getSectionsBytableId(Integer tableId) {
        return tableidSection.get(tableId);
    }

    void dumpSections(boolean withDetail) {
        boolean save2file = false;
        int sectionListSize = sectionList.size();
        for (int i = 0; i < sectionListSize; i++) {
            TSSection section = sectionList.get(i);
            dumpSection(section, withDetail);

            if (save2file) {
                StringBuffer sb = new StringBuffer();
                sb.append(
                        "d:" + File.separator + "temp" + File.separator + "syntax" + File.separator + "" + bitStream.getFile().getName()
                                + "_")//
                        .append(section.getTableName())//
                        .append("_")//
                        .append(section.getTable_id() + "_")//
                        .append(section.getTable_id_extension() + "_)")//
                        .append(section.getSection_number())//
                        .append(".dump");

                FileUtil.writeStringToFile(sb.toString(), section.dumpTextResult());
            }
        }
    }

    void dumpSection(TSSection section, boolean withDetail) {
        if (section == null) {
            return;
        }
        log.info(section.briefTimeInfo());

        if (section.getTableName() == null) {
            log.error("Not transfer over\t" + section);
            return;
        }

        if (withDetail) {
            StringBuffer sb = new StringBuffer();
            if (section != null) {
                sb.append(section.dumpSectionData());
                sb.append(section.dumpSectionRawPacket());
            }
            log.info(sb.toString());
        }

    }

    void addEvents(Service service, Event event) {
        List<Event> events = serviceEvents.get(service);
        if (events == null) {
            events = new ArrayList<Event>();
            serviceEvents.put(service, events);
        } else {
            events.add(event);
        }
    }

    void sortEvents() {
        if (serviceEvents == null) {
            return;
        }
        for (Service service : serviceEvents.keySet()) {
            List<Event> events = serviceEvents.get(service);
            if (events != null) {
                Collections.sort(events);
            }
        }
    }

    void reset() {
        sectionList.clear();
        pidSection.clear();
        tableidSection.clear();
        serviceEvents.clear();
    }
}
