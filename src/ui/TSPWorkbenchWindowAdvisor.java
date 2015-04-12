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
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.rap.rwt.service.ServiceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import core.FileUtil;
import core.Generator;
import core.RuntimeConfig;
import core.StreamHistory;
import core.SyntaxBuildFactory;
import core.TSPConfig;

/**
 * Configures the initial size and appearance of a workbench window.
 */
public class TSPWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
    private static Logger log = Logger.getLogger(TSPWorkbenchWindowAdvisor.class);

    public TSPWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new TSPActionBarAdvisor(configurer);
    }

    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setShowMenuBar(false);
        configurer.setShowPerspectiveBar(false);
        configurer.setShowProgressIndicator(false);
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(false);
        configurer.setTitle("TSP");
        configurer.setShellStyle(SWT.NONE | SWT.NO_TRIM);

        PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, false);
        PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION, SWT.TOP);
    }

    public void postWindowOpen() {
        super.postWindowOpen();
        String ROOT_DIR = TSPActivator.ROOT;
        RuntimeConfig.TSP_Config = TSPConfig.checkConfig(new File(TSPActivator.CONFIGURE_FILE));
        Generator.generatorSytax(ROOT_DIR, false);// in memory compile
        this.getWindowConfigurer().getWindow().getShell().setMaximized(true);
        Display display = this.getWindowConfigurer().getWindow().getShell().getDisplay();
        RuntimeConfig.fontRegistry = new FontRegistry(display);
        RuntimeConfig.fontRegistry.put("code", new FontData[] { new FontData("Courier New", 12, SWT.NORMAL) });

        ApplicationConfigureFactory.getAppMetas().clear();

        String _3RDAPP_ROOT = ROOT_DIR + File.separator + "3rd" + File.separator + "src";
        List<File> fileLists = new ArrayList<File>();
        FileUtil.ListDirectoryFiles(new File(_3RDAPP_ROOT), fileLists);
        String resourceName = null;
        if (fileLists != null && fileLists.size() > 0) {
            for (File file : fileLists) {
                if (file.getAbsolutePath().indexOf(".java") == -1)
                    continue;

                resourceName = file.getName().replaceAll(".java", "");
                Class<?> clazz = null;
                try {
                    SyntaxBuildFactory.createFromjavaCode(-1, resourceName, //
                            FileUtil.readFileToStringBuffer(file.getAbsolutePath()).toString());
                    log.info("Regist 3rd app:" + resourceName + "\t");
                    clazz = SyntaxBuildFactory.getClazz(resourceName);

                } catch (Exception e) {
                    // e.printStackTrace();
                    log.error("Regist 3rd app:" + resourceName + "FAIL\t");
                }

                ApplicationConfigureFactory.regist(new ApplicationMeta(clazz, file));
            }
        }
        SyntaxBuildFactory.refreshClassLoader();

        ServiceManager manager = RWT.getServiceManager();
        ServiceHandler downloadServiceHandler = new DownloadServiceHandler();
        manager.unregisterServiceHandler("downloadServiceHandler");
        manager.registerServiceHandler("downloadServiceHandler", downloadServiceHandler);
        ServiceHandler tsExporthandler = new TsExportHandler();
        manager.unregisterServiceHandler("tsExporthandler");
        manager.registerServiceHandler("tsExporthandler", tsExporthandler);

        ServiceHandler stringDownloadServiceHandler = new StringDownloadServiceHandler();
        manager.unregisterServiceHandler("stringDownloadServiceHandler");
        manager.registerServiceHandler("stringDownloadServiceHandler", stringDownloadServiceHandler);

        // clear all history
        StreamHistory.clear();
        // System.gc();
    }
}
