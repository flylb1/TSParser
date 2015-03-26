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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;

@SuppressWarnings({ "unused", "unchecked" })
public class TSSection implements Comparable<TSSection> {
	private static boolean identSectionUsingBuffer = false;
	private BitStream bitStream;
	private int pid;

	private/* byte */int table_id;// table_id 8 uimsbf
	private/* byte */int section_syntax_indicator; // section_syntax_indicator 1 bslbf
	private/* byte */int reserved_future_use; // reserved_future_use 1 bslbf
	private/* byte */int reserved1;// reserved 2 bslbf
	private/* short */int section_length;// section_length 12 uimsbf
	private/* short */int table_id_extension;// table_id_extension 16 uimsbf
	private/* byte */int reserved2;// reserved 2 bslbf
	private/* byte */int version_number;// version_number 5 uimsbf
	private/* byte */int current_next_indicator;// current_next_indicator 1 bslbf
	private/* byte */int section_number;// section_number 8 uimsbf
	private/* byte */int last_section_number;// last_section_number 8 uimsbf

	private static Logger log = Logger.getLogger(TSSection.class);

	private static int section_len = 1024 * (4 + 1);// +1

	private List<TSpacket> packetList = new ArrayList<TSpacket>();

	private ByteBuffer sectionData = ByteBuffer.allocate(section_len);// 4K

	private long positionOffset;

	private int got_private_section_length;

	private List<Long> transmitTimeList = new ArrayList<Long>();
	private CommonParser commonParser;
	private String tableName;
	private Stack<List<NodeValue>> valueStack = new Stack<List<NodeValue>>();

	private int id;
	private boolean hasParse = false;
	private boolean eitIgnoreByFilter = false;

	public TSSection(BitStream bitStream, int pid) { // 
		super();
		this.bitStream = bitStream;
		this.pid = pid;
		List<NodeValue> valueList = new ArrayList<NodeValue>();
		valueStack.push(valueList);
		Arrays.fill(sectionData.array(), (byte) (0x0));
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSection_length() {
		return section_length;
	}

	public int getTable_id_extension() {
		return table_id_extension;
	}

	public long getPositionOffset() {
		return positionOffset;
	}

	public int getGot_private_section_length() {
		return got_private_section_length;
	}

	public ByteBuffer getSectionData() {
		return sectionData;
	}

	public int getTable_id() {
		return table_id;
	}

	public int getSection_syntax_indicator() {
		return section_syntax_indicator;
	}

	public int getReserved_future_use() {
		return reserved_future_use;
	}

	public int getVersion_number() {
		return version_number;
	}

	public int getCurrent_next_indicator() {
		return current_next_indicator;
	}

	public int getSection_number() {
		return section_number;
	}

	public int getLast_section_number() {
		return last_section_number;
	}

	public List<Long> getTransmitTimeList() {
		return transmitTimeList;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public CommonParser getCommonParser() {
		return commonParser;
	}

	public void setCommonParser(CommonParser commonParser) {
		this.commonParser = commonParser;
	}

	public BitStream getBitStream() {
		return bitStream;
	}

	public void setBitStream(BitStream bitStream) {
		this.bitStream = bitStream;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public boolean isHasParse() {
		return hasParse;
	}

	public void setHasParse(boolean hasParse) {
		this.hasParse = hasParse;
	}

	public Stack<List<NodeValue>> getValueStack() {
		return valueStack;
	}

	public List<NodeValue> getRoot() {
		List<NodeValue> root = valueStack.firstElement();
		return root;
	}

	public List<TSpacket> getPacketList() {
		return packetList;
	}

	public void setPacketList(List<TSpacket> packetList) {
		this.packetList = packetList;
	}

	public boolean isIgnoreByFilter() {
		return eitIgnoreByFilter;
	}

	public void setEITIgnoreByFilter(boolean ignoreByFilter) {
		this.eitIgnoreByFilter = ignoreByFilter;
	}

	private boolean parseSectionHeader() throws Exception {
		byte[] rawSection = sectionData.array();
		if (rawSection == null)
			return false;

		SectionParser sectionParser = new SectionParser();
		sectionParser.setBuffer(rawSection);

		table_id = (Integer) (sectionParser.parseData(8));//
		section_syntax_indicator = (Integer) (sectionParser.parseData(1));
		reserved_future_use = (Integer) (sectionParser.parseData(1));// reserved_future_use 1 bslbf
		reserved1 = (Integer) (sectionParser.parseData(2));// reserved 2 bslbf
		section_length = (Integer) (sectionParser.parseData(12));// section_length 12 uimsbf
		// section_length -- This is a 12 bit field, the first two bits of which shall be '00'. It specifies the number of bytes
		// of the section starting immediately following the section_length field, and including the CRC.
		section_length += 3;// there are 3 bytes before field "section_length"
		table_id_extension = (Integer) (sectionParser.parseData(16));// service_id 16 uimsbf
		reserved2 = (Integer) (sectionParser.parseData(2));// reserved 2 bslbf
		version_number = (Integer) (sectionParser.parseData(5));// version_number 5 uimsbf
		current_next_indicator = (Integer) (sectionParser.parseData(1));// current_next_indicator 1 bslbf
		section_number = (Integer) (sectionParser.parseData(8));// section_number 8 uimsbf
		last_section_number = (Integer) (sectionParser.parseData(8));// last_section_number 8 uimsbf
		return true;
	}

	/* 13818 Annex C. Table 2-30 -- Private section */
	void addTsPacket(TSpacket tsPacket, boolean lastPacketWithPointOfField) throws Exception {
		this.positionOffset = tsPacket.getPositionOffset();
		this.packetList.add(tsPacket);
		int payload_unit_start_indicator = tsPacket.getPayload_unit_start_indicator();
		short point_of_field = tsPacket.getPoint_of_field();
		int tableId = tsPacket.getTableId();
		byte[] tsPacketRawData = tsPacket.getRawData();
		int startPos = 0;

		if (point_of_field > 188) {
			log.error("Wrong point_of_field value=" + point_of_field);
			return;
		}

		try {
			if (lastPacketWithPointOfField == true) {
				startPos = 4 + 1;
				sectionData.put(tsPacketRawData, startPos, point_of_field);
				got_private_section_length += point_of_field;
			} else {
				startPos = 4;
				// skip Adaptation_field_length if have
				if (tsPacket.getAdaptation_field_control_byte() == 2/* bin 10 adaptation_field only, no payload */
						|| tsPacket.getAdaptation_field_control_byte() == 3/* bin 11 adaptation_field followed by payload */) {
					startPos += 1;
					startPos += tsPacket.getAdaptation_field_length();
				}

				if (payload_unit_start_indicator == 0x1) {
					startPos += 1;// skip point_field
					startPos += (int) point_of_field;
					sectionData.put(tsPacketRawData, startPos, BitStream.PACKAGE_LEN - startPos);
					got_private_section_length += BitStream.PACKAGE_LEN - startPos;
					this.parseSectionHeader();
				} else {
					if (sectionData.position() + (BitStream.PACKAGE_LEN - startPos) <= section_len) {
						sectionData.put(tsPacketRawData, startPos, BitStream.PACKAGE_LEN - startPos);
						got_private_section_length += BitStream.PACKAGE_LEN - startPos;
					}
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	public String dumpSectionData() {
		if (sectionData == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		byte[] ab = sectionData.array();
		List<TSpacket> tspackets = this.packetList;
		if (tspackets.size() > 0) {
			long packetNumber = tspackets.get(0).getPacketNumber();
			long time = (long) ((positionOffset * 8) / (bitStream.getTsRate()));
			sb.append("Section Infomation\n");
			sb.append("------------------------------------------------\n");
			sb.append(StringUtil.getHexString(ab, 0, section_length //
					/* + 3 *//* ahead 3 byte before section length */, //
					RuntimeConfig.byteLineNumber));
			sb.append("\n");
			sb.append("------------------------------------------------\n");

			return sb.toString();
		}
		return null;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Section [tid=");
		builder.append(table_id);
		builder.append('[' + StringUtil.toHex(table_id) + ']');
		builder.append(", ext=");
		builder.append(table_id_extension);
		builder.append('[' + StringUtil.toHex(table_id_extension) + ']');

		builder.append(", ver=");
		builder.append(version_number);

		builder.append(", sec_no=");
		builder.append(section_number);
		builder.append("/" + last_section_number);

		builder.append(", section_syntax_indicator=");
		builder.append(section_syntax_indicator);

		builder.append(", section_length=");
		builder.append(section_length);
		builder.append('[' + StringUtil.toHex(section_length) + ']');

		builder.append(", current_next_indicator=");
		builder.append(current_next_indicator);

		builder.append("] ");
		builder.append(this.getTransmitTimeList().size());
		builder.append(this.getTransmitTimeList());

		return builder.toString();
	}

	public String briefTimeInfo() {
		StringBuilder builder = new StringBuilder();
		builder.append("Section [table_id=");
		builder.append(table_id);
		builder.append('[' + StringUtil.toHex(table_id) + ']');
		builder.append(", table_id_extension=");
		builder.append(table_id_extension);
		builder.append(", section_number=");
		builder.append(section_number);
		builder.append(", version=");
		builder.append(version_number);
		builder.append("] \r\n");
		builder.append(/* this.getTransmitTimeList().size() + */"[\r\n");
		List<Long> list = this.getTransmitTimeList();
		if (bitStream.getTsRate() > 0) {
			int time = 0;
			int listSize = list.size();
			for (int i = 0; i < listSize; i++) {
				time++;
				builder.append("" + (long) ((list.get(i) * 8) / (bitStream.getTsRate())) + "=>");
				if (i > 0 && time % 10 == 0) {
					builder.append("\r\n");
				}
			}
		}
		builder.append("\r\n]");

		return builder.toString();
	}

	/** Dump section raw packet data */
	public String dumpSectionRawPacket() {
		if (packetList == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("Packet Infomation\n");
		int packetListSize = packetList.size();
		for (int i = 0; i < packetListSize; i++) {
			TSpacket packet = packetList.get(i);
			sb.append(packet.dump2String());
		}
		return sb.toString();
	}

	private void dumpNode(StringBuffer sb, List<NodeValue> values, int step) {
		int formatLen = 50;
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
				dumpNode(sb, (List<NodeValue>) node.getValue(), step);
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

	void clearRawData() {
		sectionData.clear();
		sectionData = null;

		packetList.clear();
		packetList = null;
	}

	public String dumpTextResult() {
		if (commonParser == null) {
			return "";
		}
		List<NodeValue> values = (List<NodeValue>) getRoot();
		StringBuffer sb = new StringBuffer();
		int step = 0;
		dumpNode(sb, values, step);
		return sb.toString();
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + current_next_indicator;
		result = prime * result + last_section_number;
		result = prime * result + reserved1;
		result = prime * result + reserved2;
		result = prime * result + reserved_future_use;
		result = prime * result + section_length;
		result = prime * result + section_number;
		result = prime * result + section_syntax_indicator;
		result = prime * result + table_id;
		result = prime * result + table_id_extension;
		result = prime * result + version_number;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TSSection))
			return false;
		TSSection other = (TSSection) obj;

		if (this.getTable_id() == 0x80 || this.getTable_id() == 0x81) {// is CA Message section ECM
			if (Arrays.equals(this.getSectionData().array(), other.getSectionData().array())) {
				return true;
			}
			return false;
		}

		if (identSectionUsingBuffer == true) {
			if (this.getTable_id() >= 0x3A && this.getTable_id() <= 0x3E && this.getTable_id() == 0x3C) {
				if (Arrays.equals(this.getSectionData().array(), other.getSectionData().array())) {
					return true;
				} else {
					return false;
				}
			}
		}

		if (current_next_indicator != other.current_next_indicator)
			return false;
		if (last_section_number != other.last_section_number)
			return false;
		if (section_number != other.section_number)
			return false;
		if (section_syntax_indicator != other.section_syntax_indicator)
			return false;
		if (table_id != other.table_id)
			return false;
		if (table_id_extension != other.table_id_extension)
			return false;
		if (version_number != other.version_number)
			return false;
		return true;
	}

	public String shortName() {
		StringBuilder builder = new StringBuilder();
		builder.append("TID = " + table_id + "[0x" + Integer.toHexString(table_id).toUpperCase() + "] ");
		builder.append("Ext = " + table_id_extension + "[0x" + Integer.toHexString(table_id_extension).toUpperCase() + "] ");
		builder.append("Ver = " + StringUtil.formatString(version_number, 3) + " ");
		builder.append("Sec_no = " + StringUtil.formatString(section_number, 3) + "/" + StringUtil.formatString(last_section_number, 3));

		return builder.toString();
	}

	public int compareTo(TSSection o) {
		if (this.table_id < o.table_id) {
			return -1;
		} else if (this.table_id == o.table_id) {
			if (this.table_id_extension < o.table_id_extension) {
				return -1;
			} else if (this.table_id_extension == o.table_id_extension) {
				if (this.version_number < o.version_number) {
					return -1;
				} else if (this.version_number == o.version_number) {
					if (this.section_number < o.section_number) {
						return -1;
					} else if (this.section_number == o.section_number) {
						return 0;
					} else {
						return 1;
					}
				} else {
					return 1;
				}

			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}

}
