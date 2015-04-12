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

public class DescriptorMeta { //
    private short tag;
    private String name;
    private Class<?> clazz;

    public short getTag() {
        return tag;
    }

    public void setTag(short tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public DescriptorMeta(String name, short tag, Class<?> clazz) {
        super();
        this.tag = tag;
        this.name = name;
        this.clazz = clazz;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DescriptorMeta [tag=");
        builder.append(tag);
        builder.append(", name=");
        builder.append(name);
        builder.append(", clazz=");
        builder.append(clazz);
        builder.append("]");
        return builder.toString();
    }
}
