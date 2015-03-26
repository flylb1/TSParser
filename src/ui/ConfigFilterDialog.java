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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.rap.rwt.RWT;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import core.BitStream;
import core.MyStreamFilter;

class ConfigFilterDialog extends Dialog {
    private static final long serialVersionUID = 1L;
    private Control parentShell;
    private BitStream bitStream;

    ConfigFilterDialog(Shell shell, BitStream bitStream) {
        super(shell);
        this.parentShell = shell;
        this.bitStream = bitStream;
    }

    private void center() {
        Rectangle p = parentShell.getBounds();
        int width = 400;// p.width / 2;
        int height = 200;// p.height / 2;
        Rectangle rec = new Rectangle(0, 0, width, height);
        rec.x = p.x + (p.width - width) / 2;
        rec.y = p.y + (p.height - height) / 2;
        this.getShell().setBounds(rec);
    }

    protected Control createContents(Composite parent) {
        this.getShell().setText(Messages.ConfigFilterDialog_0);
        // this.getShell().setSize(600, 400);
        center();
        parent.setLayout(new GridLayout(1, true));
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comp.setLayout(new GridLayout(2, false));

        Label labelOnid = new Label(comp, SWT.NONE);
        labelOnid.setText(Messages.ConfigFilterDialog_1);
        labelOnid.setLayoutData(new GridData(SWT.LEFT, GridData.FILL, false, false));

        final Text textOnid = new Text(comp, SWT.BORDER);
        textOnid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        textOnid.setText(Messages.ConfigFilterDialog_2);

        Label labelTsid = new Label(comp, SWT.NONE);
        labelTsid.setText(Messages.ConfigFilterDialog_3);
        labelTsid.setLayoutData(new GridData(SWT.LEFT, GridData.FILL, false, false));

        final Text textTsid = new Text(comp, SWT.BORDER);
        textTsid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        textTsid.setText(Messages.ConfigFilterDialog_4);

        Label labelSvcid = new Label(comp, SWT.NONE);
        labelSvcid.setText(Messages.ConfigFilterDialog_5);
        labelSvcid.setLayoutData(new GridData(SWT.LEFT, GridData.FILL, false, false));

        final Text textSvcid = new Text(comp, SWT.BORDER);
        textSvcid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        textSvcid.setText(Messages.ConfigFilterDialog_6);

        final Label labelError = new Label(comp, SWT.FILL);
        // labelError.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
        labelError.setText(Messages.ConfigFilterDialog_7);
        GridData gd_labelError = new GridData(SWT.FILL, GridData.FILL, true, true);
        gd_labelError.horizontalSpan = 2;
        labelError.setLayoutData(gd_labelError);
        RWT.getUISession(this.getShell().getDisplay()).exec(new Runnable() {
            public void run() {
                MyStreamFilter streamFilter = (MyStreamFilter) RWT.getUISession().getAttribute(Messages.ConfigFilterDialog_8);
                if (streamFilter == null) {
                    streamFilter = new MyStreamFilter();
                    RWT.getUISession().setAttribute(Messages.ConfigFilterDialog_9, streamFilter);
                }

                int onid = streamFilter.onid;
                int tsid = streamFilter.tsid;
                int svcid = streamFilter.svcid;

                System.out.println(bitStream.getFile().getAbsolutePath() + Messages.ConfigFilterDialog_10 + onid
                        + Messages.ConfigFilterDialog_11 + tsid + Messages.ConfigFilterDialog_12 + svcid);
                textOnid.setText(String.valueOf(onid));
                textTsid.setText(String.valueOf(tsid));
                textSvcid.setText(String.valueOf(svcid));
            }
        });

        Button okButton = new Button(comp, SWT.BORDER);
        okButton.setText(Messages.ConfigFilterDialog_13);
        GridData gd_okButton = new GridData(SWT.RIGHT, GridData.FILL, true, true);
        gd_okButton.horizontalSpan = 2;
        okButton.setLayoutData(gd_okButton);
        okButton.addSelectionListener(new SelectionListener() {
            private static final long serialVersionUID = 1L;

            public void widgetSelected(SelectionEvent e) {
                String onidString = textOnid.getText();
                String tsidString = textTsid.getText();
                String svcidString = textSvcid.getText();
                try {
                    MyStreamFilter streamFilter = (MyStreamFilter) RWT.getUISession().getAttribute(Messages.ConfigFilterDialog_14);
                    streamFilter.path = bitStream.getFile().getAbsolutePath();
                    streamFilter.onid = Integer.parseInt(onidString);
                    streamFilter.tsid = Integer.parseInt(tsidString);
                    streamFilter.svcid = Integer.parseInt(svcidString);
                    ConfigFilterDialog.this.close();
                } catch (Exception e1) {
                    System.out.println(e1.getMessage());
                    labelError.setText(Messages.ConfigFilterDialog_15 + e1.getMessage());
                    labelError.redraw();
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        textSvcid.setFocus();

        parent.layout();
        return null;
    }

    protected void configureShell(final Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.ConfigFilterDialog_16);
        shell.addControlListener(new ControlAdapter() {
            private static final long serialVersionUID = 1L;

            public void controlResized(ControlEvent e) {
                initializeBounds();
            }
        });
    }

    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, Messages.ConfigFilterDialog_17, false);
    }

    protected void buttonPressed(final int buttonId) {
        if (buttonId == IDialogConstants.CANCEL_ID) {
            close();
        }
        super.buttonPressed(buttonId);
    }

}
