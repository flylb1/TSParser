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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Creates, adds and disposes actions for the menus and action bars of each workbench window.
 */
class TSPActionBarAdvisor extends ActionBarAdvisor {
    private Action aboutAction;
    private Action openFileAction;
    private Action settingAction;
    private Action syntaxAction;
    private Action thirdAppAction;
    private Action homeAction;

    private ImageDescriptor syntaxImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/syntax.gif");
    private ImageDescriptor openImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/open.gif");
    private ImageDescriptor aboutImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/license.gif");
    private ImageDescriptor homeImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/home.gif");
    private ImageDescriptor settingImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID,
            "/icons/setting.gif");
    private ImageDescriptor thirdAppImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID,
            "/icons/thirdApp.gif");

    TSPActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    private void showOpenFileDialog() {
        final OpenFileDialog openFileDialog = new OpenFileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        /* int returnCode = */openFileDialog.open();
        // if (returnCode == Window.OK) {
        // }
    }

    protected void makeActions(IWorkbenchWindow window) {
        // For help action start
        aboutAction = new Action("About") {
            private static final long serialVersionUID = 1L;

            public void run() {
                super.run();
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(AboutView.ID);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        };
        aboutAction.setImageDescriptor(aboutImageDescriptor);
        
        
        homeAction= new Action("Home") {
            private static final long serialVersionUID = 1L;

            public void run() {
                super.run();
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HomeView.ID);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        };
        homeAction.setImageDescriptor(homeImageDescriptor);
        // For help action end

        // For openFileAction action start
        openFileAction = new Action("Open") {
            private static final long serialVersionUID = 1L;

            public void run() {
                super.run();
                showOpenFileDialog();
            }
        };
        openFileAction.setImageDescriptor(openImageDescriptor);

        syntaxAction = new Action("Syntax") {
            private static final long serialVersionUID = 1L;

            public void run() {
                super.run();
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(SyntaxView.ID);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        };
        syntaxAction.setImageDescriptor(syntaxImageDescriptor);

        settingAction = new Action("Setting") {
            private static final long serialVersionUID = 1L;

            public void run() {
                super.run();
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(SettingView.ID);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        };
        settingAction.setImageDescriptor(settingImageDescriptor);

        thirdAppAction = new Action("Third Apps") {
            private static final long serialVersionUID = 1L;

            public void run() {
                super.run();
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ThirdAppView.ID);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        };
        thirdAppAction.setImageDescriptor(thirdAppImageDescriptor);

    }

    protected void fillMenuBar(IMenuManager menuBar) {
    }

    protected void fillCoolBar(ICoolBarManager coolBar) {
        super.fillCoolBar(coolBar);

        IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        coolBar.add(new ToolBarContributionItem(toolbar, "main"));

        try {
            toolbar.add(openFileAction);
            toolbar.add(syntaxAction);
            toolbar.add(settingAction);
            toolbar.add(thirdAppAction);
            toolbar.add(aboutAction);
            toolbar.add(homeAction);
            
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

}
