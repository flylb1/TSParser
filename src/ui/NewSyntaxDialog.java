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

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import core.FileUtil;
import core.Generator;
import core.RuntimeConfig;
import core.SyntaxBuildFactory;

class NewSyntaxDialog extends Dialog {
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(NewSyntaxDialog.class);
    private Control parentShell;
    private MenuItem menuItem;
    private Text syntaxNametext;
    private Text syntaxContentText;

    NewSyntaxDialog(Shell shell, MenuItem menuItem) {
        super(shell);
        this.parentShell = shell;
        this.menuItem = menuItem;
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

    private void addNewSyntax() {
        String syntaxName = this.syntaxNametext.getText();
        String syntaxContent = this.syntaxContentText.getText();
        TreeItem item = (TreeItem) menuItem.getData("TreeItem");
        if (item == null) {
            return;
        }
        File file = (File) item.getData("File");
        if (file == null) {
            return;
        }
        log.info(item + "\t" + file);

        try {
            String newFileName = file.getAbsolutePath() + File.separator + syntaxName;
            log.info(newFileName);
            FileUtil.writeStringToFile(newFileName, syntaxContent);
            Generator.parseFile(new File(newFileName), false);
            SyntaxBuildFactory.refreshClassLoader();
            showMessage("Info",//
                    "Successfully Write to file\r\n" + file.getName(),//
                    SWT.ICON_WORKING);
            this.close();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Exception",//
                    e.getMessage(),//
                    SWT.ICON_WORKING);
        }
    }

    protected Control createContents(Composite parent) {
        this.getShell().setText("Create new Syntax");
        this.getShell().setSize(600, 400);
        center();
        parent.setLayout(new GridLayout(1, true));
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comp.setLayout(new GridLayout(3, false));

        Label label = new Label(comp, SWT.NONE);
        label.setText("Syntax Name:");
        label.setLayoutData(new GridData(SWT.LEFT, GridData.FILL, false, false));

        syntaxNametext = new Text(comp, SWT.BORDER);
        syntaxNametext.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        syntaxNametext.setToolTipText("Input new syntax here");

        Button okButton = new Button(comp, SWT.BORDER);
        okButton.setText("Ok");
        okButton.addSelectionListener(new SelectionListener() {
            private static final long serialVersionUID = 1L;

            public void widgetSelected(SelectionEvent e) {
                addNewSyntax();
            }

            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        syntaxContentText = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        syntaxContentText.setFont(RuntimeConfig.fontRegistry.get("code"));
        syntaxContentText.setEditable(true);
        syntaxContentText.setLayoutData(new GridData(GridData.FILL_BOTH));

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
