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
package ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ApplicationMeta implements Comparable<ApplicationMeta> {
    private Class<?> clazz;
    private File srcFile;

    public ApplicationMeta(Class<?> clazz, File srcFile) {
        super();
        this.clazz = clazz;
        this.srcFile = srcFile;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public File getSrcFile() {
        return srcFile;
    }

    public void setSrcFile(File srcFile) {
        this.srcFile = srcFile;
    }

    public int compareTo(ApplicationMeta o) {
        if (clazz == null) {
            return -1;
        }
        return clazz.getSimpleName().compareTo(o.getClazz().getSimpleName());
    }

    public String toString() {
        return this.getClazz().getSimpleName() + "\t" + this.getSrcFile().getAbsolutePath();
    }
}

public class ApplicationConfigureFactory {
    private static List<ApplicationMeta> appMetas = new ArrayList<ApplicationMeta>();

    static List<ApplicationMeta> getAppMetas() {
        return appMetas;
    }

    public static void regist(ApplicationMeta app) {
        boolean registered = false;
        if (appMetas != null) {
            for (ApplicationMeta appMeta : appMetas) {
                if (appMeta.getSrcFile() != null && app.getSrcFile() != null) {
                    if (appMeta.getSrcFile().getAbsolutePath().equals(app.getSrcFile().getAbsolutePath())) {
                        registered = true;
                        break;
                    }
                }
                if (app.getClazz() == appMeta.getClazz()) {
                    registered = true;
                    break;
                }
            }
        }
        if (registered == false) {
            appMetas.add(app);
            Collections.sort(appMetas);
        }
    }

    public static void unregist(Class<?> clazz) {
        if (appMetas != null) {
            int appMetasSize = appMetas.size();
            for (int i = 0; i < appMetasSize; i++) {
                ApplicationMeta appMeta = appMetas.get(i);
                if (appMeta.getClazz() == clazz) {// found
                    appMetas.remove(i);
                    break;
                }
            }
            Collections.sort(appMetas);
        }
    }

}
