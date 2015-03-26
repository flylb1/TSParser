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
import java.util.HashMap;
import java.util.Map;

import core.BitStream;

public class StreamHistory {
    private static Map<String, BitStream> tsHistory = new HashMap<String, BitStream>();

    public static void clear() {
        tsHistory.clear();
    }

    public static void addFile(File file, BitStream ts) {
        if (file != null && ts != null) {
            tsHistory.put(file.getAbsolutePath(), ts);
        }
    }

    public static BitStream getBitStream(String fileName) {
        return tsHistory.get(fileName);
    }

}
