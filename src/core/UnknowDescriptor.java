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

import java.util.List;

@SuppressWarnings("unchecked")
public class UnknowDescriptor extends CommonParser { //
    public void parse(byte[] descriptBuffer, int len) throws Exception {
        super.parse(descriptBuffer, len);
        parseData("descriptor_tag 8 uimsbf ");
        parseData("descriptor_length 8 uimsbf ");
        byte[] buffer = parseData(contextGet("descriptor_length") * 8, false, false);
        NodeValue newNode = new NodeValue("Unknow", buffer);
        List<NodeValue> currentList = (List<NodeValue>) getCurrentList();
        currentList.add(newNode);
    }

    public String getSyntax() {
        return "UnknowDescriptor";
    }

    public String getIdInfo() {
        return "tag=unknow";
    }

    public String getName() {
        return "Unknow";
    }
}
