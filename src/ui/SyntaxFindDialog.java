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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import core.FileUtil;

class SyntaxFindDialog extends Dialog {
    private static final long serialVersionUID = 1L;
    private String[] syntaxNames;
    private List syntaxList;
    private Control parentShell;
    private ArrayList<File> fileLists = new ArrayList<File>();
    private File root;
    private SyntaxView syntaxView;

    SyntaxFindDialog(final SyntaxView syntaxView, File root) {
        super(syntaxView.getViewSite().getShell());
        Shell shell = syntaxView.getViewSite().getShell();
        this.root = root;
        this.parentShell = shell;
        this.syntaxView = syntaxView;
    }

    private void center() {
        Rectangle p = parentShell.getBounds();
        Rectangle c = this.getShell().getBounds();

        Rectangle rec = new Rectangle(0, 0, c.width, c.height);
        rec.x = p.x + (p.width - c.width) / 2;
        rec.y = p.y + (p.height - c.height) / 2;
        this.getShell().setBounds(rec);
    }

    protected Control createContents(Composite parent) {
        this.getShell().setText("Search Syntax");
        this.getShell().setSize(450, 500);
        center();
        parent.setLayout(new GridLayout(1, true));
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comp.setLayout(new GridLayout(2, false));

        Label label = new Label(comp, SWT.NONE);
        label.setText("Find Element:");
        label.setLayoutData(new GridData(SWT.LEFT, GridData.FILL, false, false));

        final Text text = new Text(comp, SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        text.addKeyListener(new KeyListener() {
            private static final long serialVersionUID = 4116719901720017682L;

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        String key = text.getText();
                        for (int i = 0; i < syntaxNames.length; i++) {
                            String name = syntaxNames[i];
                            int pos = name.indexOf(key);
                            if (pos != -1) {// found
                                syntaxList.select(i);
                                break;
                            }
                        }
                    }
                });
            }
        });

        syntaxList = new List(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FILL);
        syntaxList.setLayoutData(new GridData(GridData.FILL_BOTH));
        syntaxList.addMouseListener(new MouseListener() {
            private static final long serialVersionUID = 1L;

            public void mouseDoubleClick(MouseEvent e) {
                int pos = syntaxList.getSelectionIndex();
                File file = fileLists.get(pos);
                syntaxView.selectTreeItemByFile(file);
            }

            public void mouseDown(MouseEvent e) {

            }

            public void mouseUp(MouseEvent e) {

            }
        });

        fileLists.clear();
        FileUtil.ListDirectoryFiles(root, fileLists);
        for (File file : fileLists) {
            syntaxList.add(file.getName());
        }
        syntaxNames = syntaxList.getItems();
        syntaxList.redraw();
        text.setFocus();
        parent.layout();
        return null;
    }

    protected void configureShell(final Shell shell) {
        super.configureShell(shell);
        shell.setText("Find Syntax ");
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
