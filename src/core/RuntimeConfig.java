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

import org.eclipse.jface.resource.FontRegistry;

public class RuntimeConfig { //
    public static TSPConfig TSP_Config;
    public static int byteLineNumber = 16;
    public static boolean debugMode = true;
    public static boolean showErrorSection = true;

    public static List<Integer> monitor_pid = new ArrayList<Integer>();
    public static List<Integer> monitor_tid = new ArrayList<Integer>(); //
    public static boolean storeRawData = true; //
    public static FontRegistry fontRegistry = null;

    static {
        // monitor_pid.add(900);
        // monitor_pid.add(901);
        // monitor_pid.add(910);
        // monitor_pid.add(911);
        // monitor_pid.add(920);
        // monitor_pid.add(921);
        // monitor_pid.add(950);

        // monitor_pid.add(0x11);
        // monitor_tid.add(0x42);
        // monitor_tid.add(0xBC);
        // monitor_pid.add(0x80);
        // monitor_tid.add(0x80);
    }

}
