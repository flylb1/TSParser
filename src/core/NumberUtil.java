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

public class NumberUtil {
    public static String Object2Hex(Object object) {
        if (object.getClass() == Long.class) {
            return Long.toHexString((Long) object).toUpperCase();
        } else if (object.getClass() == Integer.class) {
            return Integer.toHexString((Integer) object).toUpperCase();
        } else if (object.getClass() == Short.class) {
            return Integer.toHexString((Short) object).toUpperCase();
        } else if (object.getClass() == Byte.class) {
            return Integer.toHexString((Byte) object).toUpperCase();
        } else if (object.getClass() == byte[].class) {
            byte[] bytes = (byte[]) object;
            if (bytes.length <= 4) {
                return StringUtil.getHexString(bytes);
            }
        }
        return "";
    }

    public static int getIntValue(Object object) {
        if (object == null) {
            return -1;
        }
        if (object.getClass() == Long.class) {
            return ((Long) object).intValue();
        } else if (object.getClass() == Integer.class) {
            return ((Integer) object).intValue();
        } else if (object.getClass() == Short.class) {
            return ((Short) object).intValue();
        } else if (object.getClass() == Byte.class) {
            return ((Byte) object).intValue();
        }
        return -1;
    }

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }
}
