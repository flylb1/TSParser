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

import java.util.HashMap;
import java.util.Map;

public class Filter { //
    /** pid */
    // private static Logger log = Logger.getLogger(Filter.class);
    Map<Integer, String> pids = new HashMap<Integer, String>();

    void addPidFilter(int pid, String name) {
        if (pids.containsValue(name) && name.equals("PCR_PID")) {
            return;
        }
        if (pids.containsKey(pid)) {
            return;
        }
        pids.put(pid, name);
        // log.debug("pid=" + pid + " name=" + name);
    }

    boolean inPidFilter(int pid) {
        return pids.containsKey(pid);
    }

    public Map<Integer, String> getPids() {
        return pids;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Filter \r\n");
        for (Integer pid : pids.keySet()) {
            builder.append("pid:" + pid + " name:" + pids.get(pid) + "\r\n");
        }
        return builder.toString();
    }

}