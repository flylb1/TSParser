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

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import core.RuntimeConfig;

class OpenFileDialog extends Dialog {
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(OpenFileDialog.class);
    private Text pathText;
    private List fileList;
    private Control parentShell;
    private ArrayList<File> files = new ArrayList<File>();

    OpenFileDialog(Shell shell) {
        super(shell);
        this.parentShell = shell;
    }

    private void center() {
        Rectangle p = parentShell.getBounds();
        int width = p.width / 2;
        int height = p.height / 2;
        Rectangle rec = new Rectangle(0, 0, width, height);
        rec.x = p.x + (p.width - width) / 2;
        rec.y = p.y + (p.height - height) / 2;
        this.getShell().setBounds(rec);
    }

    private void showMessage(String title, String mesg, int style) {
        MessageBox mb = new MessageBox(this.getShell(), style);
        mb.setText(title);
        mb.setMessage(mesg);
        mb.open();
    }

    private void openInWorkBench(String fileName) {
        try {
            String second = fileName.hashCode() + "";
            IViewReference view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(TSView.ID, second);
            if (view == null) {
                OpenFileDialog.this.close();
                TSView tsView = (TSView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(//
                        TSView.ID, second, IWorkbenchPage.VIEW_ACTIVATE);
                tsView.parseFile(fileName, RuntimeConfig.TSP_Config.getParseSize());
            } else {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(view.getPart(true));
            }
        } catch (PartInitException e1) {
            e1.printStackTrace();
        }
    }

    private static void listDirectoryFiles(File sourceLocation, ArrayList<File> fileLists) {
        String[] children = sourceLocation.list();
        for (int i = 0; i < children.length; i++) {
            fileLists.add(new File(sourceLocation, children[i]));
        }
    }

    private void listDir(Text text) {
        String fileName = text.getText();
        File file = new File(fileName);
        if (!file.exists()) {
            text.setText("Input full file name here ...");
            return;
        }

        files.clear();
        fileList.removeAll();
        if (file.isFile()) {
            listDirectoryFiles(file.getParentFile(), files);
        } else {
            listDirectoryFiles(file, files);
        }

        for (File _file : files) {
            fileList.add(_file.getAbsolutePath());
        }
        fileList.redraw();
    }

    protected Control createContents(Composite parent) {
        this.getShell().setText("Open file");
        center();
        parent.setLayout(new GridLayout(1, true));
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comp.setLayout(new GridLayout(3, false));

        Label label = new Label(comp, SWT.NONE);
        label.setText("File:");
        label.setLayoutData(new GridData(SWT.LEFT, GridData.FILL, false, false));

        pathText = new Text(comp, SWT.BORDER);
        pathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pathText.setText("Input full file name here ...");
        pathText.setText("./");
        pathText.setToolTipText("Double click can list directory ");
        pathText.addMouseListener(new MouseListener() {
            private static final long serialVersionUID = 1L;

            public void mouseDoubleClick(MouseEvent e) {
                listDir(pathText);
            }

            public void mouseDown(MouseEvent e) {

            }

            public void mouseUp(MouseEvent e) {

            }
        });
        
        

        pathText.addKeyListener(new KeyListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.character == '\r' || e.character == '\n') {
                    listDir(pathText);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        Button okButton = new Button(comp, SWT.BORDER);
        okButton.setText("Ok");
        okButton.addSelectionListener(new SelectionListener() {
            private static final long serialVersionUID = 1L;

            public void widgetSelected(SelectionEvent e) {
                String fileName = pathText.getText().trim();// include full path
                File file = new File(fileName);
                if (file.isDirectory()) {
                    OpenFileDialog.this.listDir(pathText);
                    return;
                }

                if (!file.exists()) {
                    showMessage("Error", "File not exist:" + fileName, SWT.ICON_ERROR);
                    return;
                }
                log.info("File:" + fileName);
                openInWorkBench(fileName);
            }

            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        fileList = new List(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FILL);
        fileList.setLayoutData(new GridData(GridData.FILL_BOTH));
        fileList.addMouseListener(new MouseListener() {
            private static final long serialVersionUID = 1L;

            public void mouseDoubleClick(MouseEvent e) {
                if (fileList.getSelectionIndex() == -1)
                    return;

                File file = files.get(fileList.getSelectionIndex());
                // log.info("Select file:" + file.getAbsolutePath());
                if (file.isFile()) {
                    OpenFileDialog.this.close();
                    openInWorkBench(file.getAbsolutePath());
                } else {
                    files.clear();
                    fileList.removeAll();
                    listDirectoryFiles(file, files);

                    for (File _file : files) {
                        fileList.add(_file.getAbsolutePath());
                    }
                    fileList.redraw();
                }
            }

            public void mouseDown(MouseEvent e) {
                File file = files.get(fileList.getSelectionIndex());
                pathText.setText(file.getAbsolutePath());
            }

            public void mouseUp(MouseEvent e) {

            }
        });
        pathText.setFocus();
        
        listDir(pathText);

        parent.layout();
        return null;
    }

    protected void configureShell(final Shell shell) {
        super.configureShell(shell);
        shell.setText("Open file:");
        shell.addControlListener(new ControlAdapter() {
            private static final long serialVersionUID = 1L;

            public void controlResized(ControlEvent e) {
                initializeBounds();
            }
        });
    }

    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
    }

    protected void buttonPressed(final int buttonId) {
        if (buttonId == IDialogConstants.CANCEL_ID) {
            close();
        }
        super.buttonPressed(buttonId);
    }

}
