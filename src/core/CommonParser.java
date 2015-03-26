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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

@SuppressWarnings("unchecked")
public abstract class CommonParser {
    private static Logger log = Logger.getLogger(CommonParser.class);
    protected SectionParser sp = new SectionParser(); //
    private Stack<List<NodeValue>> valueStack;
    private TSSection section;

    public abstract String getSyntax();

    public abstract String getIdInfo();

    public abstract String getName();

    protected void setParseFlag(boolean flag) { //
        if (section != null) {
            section.setHasParse(flag);
        }
    }

    public void setSection(TSSection section) {
        this.section = section;
    }

    public Stack<List<NodeValue>> getValueStack() {
        return valueStack;
    }

    public void setValueStack(Stack<List<NodeValue>> valueStack) {
        this.valueStack = valueStack;
    }

    public void reset() { //
        sp.reset();
    }

    public CommonParser() {
        super();
    }

    public List<NodeValue> getCurrentParentList() {
        List<NodeValue> currentList = valueStack.lastElement();
        int pos = valueStack.indexOf(currentList);
        if (pos >= 1) {
            return valueStack.elementAt(pos - 1);
        }
        return null;
    }

    public Object getCurrentList() {
        List<NodeValue> currentList = valueStack.lastElement();
        return currentList;
    }

    protected int contextGet(String key) {
        int size = valueStack.size();
        boolean found = false;
        for (int i = size - 1; i >= 0; i--) {
            List<NodeValue> currentList = valueStack.elementAt(i);
            if (currentList != null) {
                for (NodeValue node : currentList) {
                    if (node.getName().equals(key)) {
                        found = true;
                        return NumberUtil.getIntValue(node.getValue());
                    }
                }
            }

            if (found == true) {
                break;
            }
        }
        if (found == false) {
            return -1;
        }
        return -1;
    }

    public void parse(byte[] descriptBuffer, int len) throws Exception {
        sp.setBuffer(descriptBuffer);
        sp.setBufferLen(len);
    }

    protected void parseData(String syntax) throws Exception {
        List<String> list = StringUtil.string2List(syntax, " ");
        // 0.varname
        // 1.bitused
        // 2.xx
        Object obj = null;
        int bitused = Integer.parseInt(list.get(1));// 2.bitused
        // if (list.get(2).equalsIgnoreCase("bslbf")) {// decode bytes
        // this.parseBytes(syntax);
        // return;
        // }

        // decode integer
        obj = sp.parseData(bitused, true, false);
        NodeValue value = new NodeValue(list.get(0), obj);
        List<NodeValue> currentList = (List<NodeValue>) getCurrentList();
        currentList.add(value);
    }

    protected void parseBytes(String syntax) throws Exception { //
        List<String> list = StringUtil.string2List(syntax, " ");
        // 1.varname
        // 2.bitused
        // 3.xx
        int bitused = Integer.parseInt(list.get(1));// 2.bitused
        NodeValue value = new NodeValue(list.get(0), null);
        List<NodeValue> currentList = (List<NodeValue>) getCurrentList();
        currentList.add(value);
        byte[] bytes = (byte[]) sp.parseData(bitused, false, false);
        value.setValue(bytes);

    }

    protected byte[] parseData(int bitused, boolean parseBuffer, boolean skip) throws Exception {
        if (bitused <= 0) {
            return null;
        }
        return (byte[]) sp.parseData(bitused, parseBuffer, skip);
    }

    protected int getToken() { //
        return sp.getToken();
    }

    protected int getBufferLen() { //
        return sp.getBufferLen();
    }

    protected void parseDescriptors(String name, byte[] buffer) throws Exception { //
        if (buffer == null) {
            return;
        }
        List<NodeValue> currentList = (List<NodeValue>) getCurrentList();
        /* List<NodeValue> descriptorsNodes = */parseDescriptorsBuffer(currentList, name, buffer);
    }

    protected void pushBag(String bagName) { //
        List<NodeValue> valueList = new ArrayList<NodeValue>();
        NodeValue newNode = new NodeValue(bagName, valueList);
        List<NodeValue> currentList = (List<NodeValue>) getCurrentList();
        currentList.add(newNode);
        valueStack.push(valueList);
    }

    protected void popBag(String bagName) { //
        /* List<NodeValue> list = */valueStack.pop();
    }

    protected void pushElement() { //
        List<NodeValue> currentParentList = getCurrentParentList();
        List<NodeValue> currentList = (List<NodeValue>) getCurrentList();
        String bagName = null;
        for (NodeValue node : currentParentList) {
            if (node.getValue() == currentList) {
                bagName = node.getName();
                break;
            }
        }

        if (bagName != null && bagName.endsWith("s")) {
            bagName = bagName.substring(0, bagName.length() - 1);
        } else {
            if (bagName != null) {
                bagName = bagName.replaceAll("s.*", "");
            }
        }
        List<NodeValue> valueList = new ArrayList<NodeValue>();
        NodeValue newNode = new NodeValue(bagName, valueList);
        currentList.add(newNode);
        valueStack.push(valueList);
    }

    protected void popElement() { //
        valueStack.pop();
    }

    protected List<NodeValue> parseDescriptorsBuffer(List<NodeValue> currentList, String name, byte[] buffer) throws Exception {
        if (buffer == null || buffer.length < 2) {
            log.debug("buffer is null");
            return null;
        }
        List<Class<?>> descriptorClasses;
        List<NodeValue> descriptors = new ArrayList<NodeValue>();
        NodeValue newNode = new NodeValue(name, descriptors);
        currentList.add(newNode);
        SectionParser sectionParser = new SectionParser();
        sectionParser.setBuffer(buffer);
        sectionParser.setBufferLen(buffer.length);

        int descriptor_tag = 0;
        int descriptor_length = 0;
        CommonParser descriptorInstance = null;

        while (!sectionParser.isEnd()) {
            descriptor_tag = 0;
            descriptor_length = 0;
            descriptorInstance = null;

            descriptor_tag = buffer[sectionParser.getToken()] & 0xff;
            descriptor_length = buffer[sectionParser.getToken() + 1] & 0xff;
            descriptorClasses = SyntaxBuildFactory.getClassByTag(descriptor_tag);

            // Get single descriptors buffer data
            byte[] descriptBuffer = null;
            boolean parseDescHasException = false;
            Exception parseDescException = null;
            try {

                if (sectionParser.getToken() + (descriptor_length + 2) > buffer.length) {
                    log.error("Error:\t" + section.shortName() + "\tLen:" + //
                            StringUtil.formatString(section.getSection_length(), 5) + "\t" + //
                            "Descriptor:" + descriptorClasses + "\tTotol_len=" + buffer.length//
                            + "\tToken=" + sectionParser.getToken()//
                            + "\tNeed_buffer=" + (descriptor_length /* + 2 */));

                    // descriptor_length = buffer.length - sectionParser.getToken() - 2;// -2 mean tag length two bytes
                    // parseDescHasException = true;
                }
                descriptBuffer = (byte[]) sectionParser.parseData(//
                        ((descriptor_length + 2)/* +2 mean add length for tag and length */) * 8, false, false//
                        );
            } catch (Exception e1) {
                if (RuntimeConfig.debugMode) {
                    e1.printStackTrace();
                }
                parseDescHasException = true;
                parseDescException = e1;
                // here do not throw
            }

            // Try again using descriptor buffer as needed
            if (parseDescHasException == true) {
                try {
                    descriptor_length = buffer.length - sectionParser.getToken() - 2;// -2 mean tag length two bytes
                    parseDescHasException = true;
                    descriptBuffer = (byte[]) sectionParser.parseData(//
                            ((descriptor_length + 2)/* +2 mean add length for tag and length */) * 8, false, false//
                            );
                } catch (Exception e1) {
                    if (RuntimeConfig.debugMode) {
                        e1.printStackTrace();
                    }
                    throw e1;
                }
            }

            descriptorClasses.add(null);// add null for UnknowDescriptor
            Class<?> descriptorClass = null;
            int descriptorClassesSize = descriptorClasses.size();
            for (int i = 0; i < descriptorClassesSize; i++) {
                descriptorClass = descriptorClasses.get(i);
                String nodeName = null;
                if (descriptorClass == null) {
                    descriptorInstance = new UnknowDescriptor();
                    nodeName = "UnknowDescriptor";
                } else {
                    descriptorInstance = (CommonParser) SyntaxBuildFactory.getInstanceByClass(descriptorClass);// using instance pool
                    // descriptorInstance = (CommonParser) descriptorClass.newInstance();// new
                    nodeName = descriptorInstance.getName();
                }

                // descriptor_length: This 8-bit field specifies the total number of bytes of the data portion of the descriptor
                // following the byte defining the value of this field.

                try {
                    descriptorInstance.reset();
                    Stack<List<NodeValue>> valueStack = new Stack<List<NodeValue>>();
                    List<NodeValue> valueList = new ArrayList<NodeValue>();
                    valueStack.push(valueList);
                    descriptorInstance.setValueStack(valueStack);
                    log.debug("Parsing descriptor:" + StringUtil.formatString(descriptorInstance.getName(), 30) //
                            + "\t" + StringUtil.formatString(Integer.toHexString(descriptor_tag), 4)//
                            + "\tlength=" + (descriptor_length + 2));
                    descriptorInstance.parse(descriptBuffer, descriptor_length + 2/* +2 mean add length for tag and length */);
                    List<NodeValue> values = (List<NodeValue>) descriptorInstance.getCurrentList();// Get parser result list
                    NodeValue descriptNode = null;
                    descriptNode = new NodeValue(nodeName, values);// Create new node for single descriptor
                    descriptors.add(descriptNode);// Add to descriptors list
                    break;
                } catch (Exception e) {
                    log.info("\t\t\tParse Error " + descriptorInstance.getName() + "\t" + descriptor_tag);
                    if (descriptor_tag == 0) {/* For wrong descriptor ,avoid dead loop ,just break it */
                        parseDescHasException = true;
                        break;
                    }
                    if (i == 0) {
                        List<NodeValue> values = (List<NodeValue>) descriptorInstance.getCurrentList();// Get parser result list
                        NodeValue descriptNode = null;
                        descriptNode = new NodeValue(nodeName, values);// Create new node for single descriptor
                        descriptors.add(descriptNode);// Add to descriptors list
                    }
                    if (i != descriptorClasses.size() - 1) {
                        continue;
                    }

                    if (RuntimeConfig.debugMode) {
                        e.printStackTrace();
                    } else {
                        log.info("descriptors parse error," + descriptorInstance);
                    }
                }
            }

            // last parse has exception
            if (parseDescHasException == true) {
                log.info(parseDescException.toString());
                throw parseDescException;
            }
        }
        return descriptors;
    }
}
