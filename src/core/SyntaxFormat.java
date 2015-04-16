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
import java.util.List;

/**
 * @author liubin
 *
 *         This class only for pretty format all syntax
 */
public class SyntaxFormat {
    private static void processFile(String fileName) {
        System.out.println("Process file:" + fileName);
        int maxLen = 0;
        String reg = "^(\\s+)(\\w+)(\\s+)(\\d+)(\\s+)(\\w+).*";
        List<String> list = FileUtil.readFileToList(fileName);
        for (String item : list) {
            item = item.replaceAll("\\t", "    ");
            boolean b = item.matches(reg);
            if (b) {
                String key = ReplaceUtil.replace(item, reg, "$1$2");
                maxLen = (key.length() > maxLen) ? key.length() : maxLen;
            }
        }

        maxLen += 4;

        StringBuffer sb = new StringBuffer();
        for (String item : list) {
            item = item.replaceAll("\\t", "    ");
            boolean b = item.matches(reg);
            if (b) {
                String key = ReplaceUtil.replace(item, reg, "$1$2");
                key = StringUtil.formatString(key, maxLen);

                String len = ReplaceUtil.replace(item, reg, "$4");
                String type = ReplaceUtil.replace(item, reg, "$6");

                String newSyntax = key + "    " //
                        + StringUtil.formatString(len, 4)//
                        + "    "//
                        + StringUtil.formatString(type, 8)//
                ;
                sb.append(newSyntax + "\n");
            } else {
                sb.append(item + "\n");
            }
        }
        FileUtil.writeStringToFile(fileName, sb.toString());
    }

    public static void main(String[] args) {
        List<String> fileList = new ArrayList<String>();
        String syntaxRootDir = "." /* + File.separator + "TSP_DIST" */+ File.separator + "syntax" + File.separator + "input";
        FileUtil.ListDirectory(new File(syntaxRootDir), fileList);
        try {
            for (String fileName : fileList) {
                processFile(fileName);
            }
        } catch (Exception e) {
        }
    }
}
