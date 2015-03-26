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

public class TableMeta {
    private int start;
    private int end;
    private String shortName;
    private String descript;
    private String fullClassName;
    private boolean enable;

    public TableMeta(int start, int end, String shortName, String descript, String fullClassName) { //
        super();
        this.start = start;
        this.end = end;
        this.shortName = shortName;
        this.descript = descript;
        this.fullClassName = fullClassName;
        this.enable = true;
    }

    public TableMeta() {
        super();
    }

    public TableMeta(int start, int end, String shortName, String descript, String fullClassName, boolean enable) {
        super();
        this.start = start;
        this.end = end;
        this.shortName = shortName;
        this.descript = descript;
        this.fullClassName = fullClassName;
        this.enable = enable;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescript() {
        return descript;
    }

    public void setDescript(String descript) {
        this.descript = descript;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String toString() {
        return "[0x" + NumberUtil.Object2Hex(start) + "---0x" + NumberUtil.Object2Hex((byte) end) + "]\t" + descript;
    }

}
