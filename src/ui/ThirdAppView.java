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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import core.FileUtil;
import core.RuntimeConfig;
import core.SyntaxBuildFactory;

public class ThirdAppView extends MyViewPart { //
    private static Logger log = Logger.getLogger(ThirdAppView.class);
    static final String ID = "ThirdAppView";
    private ImageDescriptor applyImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/apply.gif");
    private CTabFolder tabFolder;
    private Action applyAction;

    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        createActions();
        initializeToolBar();

        body.setLayout(new FillLayout());
        tabFolder = new CTabFolder(body, /* SWT.FLAT | */SWT.TOP);
        tabFolder.setBorderVisible(false);
        List<ApplicationMeta> appMetas = ApplicationConfigureFactory.getAppMetas();
        if (appMetas != null) {
            for (ApplicationMeta appMeta : appMetas) {
                File file = appMeta.getSrcFile();
                if (file != null) {// render
                    StringBuffer sb = FileUtil.readFileToStringBuffer(file.getAbsolutePath());
                    CTabItem tabItem = new CTabItem(tabFolder, /* SWT.CLOSE */SWT.FLAT);// new tabItem
                    tabItem.setData("File", file);
                    tabItem.setText(file.getName());
                    tabItem.setToolTipText(file.getAbsolutePath());
                    Composite comp = new Composite(tabFolder, SWT.FILL);
                    comp.setLayout(new FillLayout());
                    tabItem.setControl(comp);
                    Text syntaxText = new Text(comp, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
                    syntaxText.setFont(RuntimeConfig.fontRegistry.get("code"));
                    syntaxText.setText(sb.toString());
                    tabItem.setData("Text", syntaxText);
                    tabFolder.setSelection(tabItem);
                }
            }
        }
        parent.layout();
    }

    private void initializeToolBar() {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        toolBarManager.add(applyAction);
    }

    private void createActions() {
        applyAction = new Action() {
            private static final long serialVersionUID = 1L;

            public void run() {
                CTabItem tabItem = tabFolder.getSelection();
                if (tabItem == null) {
                    showMessage("Error",//
                            "No active syntax!", SWT.ERROR);
                    return;
                }

                File file = (File) tabItem.getData("File");
                Text syntaxText = (Text) tabItem.getData("Text");
                if (file == null || syntaxText == null) {
                    showMessage("Error",//
                            "Internal Error", SWT.ERROR);
                    return;
                }

                try {
                    FileUtil.writeStringToFile(file.getAbsolutePath(), syntaxText.getText());
                    reload3rdApp();
                    showMessage("Info", "Successfully Write to file\r\n" + file.getName(),//
                            SWT.ICON_WORKING);
                } catch (Exception e) {
                    showMessage("Error", "" + e.getCause(), SWT.ICON_WORKING);
                    // e.printStackTrace();
                }
            }
        };
        applyAction.setText("Apply");
        applyAction.setToolTipText("Apply this syntax");
        applyAction.setImageDescriptor(applyImageDescriptor);
    }

    private void reload3rdApp() throws Exception {
        String ROOT_DIR = TSPActivator.ROOT;
        ApplicationConfigureFactory.getAppMetas().clear();

        String _3RDAPP_ROOT = ROOT_DIR + File.separator + "3rd" + File.separator + "src";
        List<File> fileLists = new ArrayList<File>();
        FileUtil.ListDirectoryFiles(new File(_3RDAPP_ROOT), fileLists);
        String resourceName = null;
        SyntaxBuildFactory.refreshClassLoader();
        if (fileLists != null && fileLists.size() > 0) {
            for (File file : fileLists) {
                if (file.getAbsolutePath().indexOf(".java") == -1)
                    continue;

                resourceName = file.getName().replaceAll(".java", "");
                SyntaxBuildFactory.createFromjavaCode(-1, resourceName, //
                        FileUtil.readFileToStringBuffer(file.getAbsolutePath()).toString());

                try {
                    Class<?> clazz = SyntaxBuildFactory.getClazz(resourceName);
                    log.info("Regist 3rd app:" + resourceName + "\t" + clazz);
                    ApplicationConfigureFactory.regist(new ApplicationMeta(clazz, file));
                } catch (Exception e) {
                    throw e;
                }
            }
        }
    }

    public void setFocus() {

    }

    public String getHelp() {
        return null;
    }
}
