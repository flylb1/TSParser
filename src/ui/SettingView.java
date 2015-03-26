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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import core.FileUtil;
import core.NumberUtil;
import core.PidMeta;
import core.RuntimeConfig;
import core.TSPConfig;
import core.TableMeta;

public class SettingView extends MyViewPart { //
    private static Logger log = Logger.getLogger(SettingView.class);
    private static final int PID = 0;
    private static final int PID_DESC = 1;

    private static final int TId_FROM = 0;
    private static final int TId_TO = 1;
    private static final int TABLE_SHORT_NAME = 2;
    private static final int TABLE_DESC = 3;
    private static final int TABLE_FULL_CLASS_NAME = 4;

    private static final String PID_TEXT = "PID";
    private static final String TABLE_ID_TEXT = "TABLE ID";
    private static final String Other = "Other";

    static final String ID = "SettingView";
    private TSPConfig tspConfig;
    private TabFolder tabFolder;
    private Table pidTable;
    private Table tableIdTable;

    private Button serviceSupport;
    private Button epgSupport;

    private Action applyAction;
    private ImageDescriptor applyImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/apply.gif");

    private void createTableColumn(Table table, String text, int style, int width) {
        TableColumn tableColumn = new TableColumn(table, style);
        if (width > 0) {
            tableColumn.setWidth(width);
        }
        tableColumn.setText(text);
    }

    private void renderPidTable() {
        List<PidMeta> pids = tspConfig.getPIDS();

        for (PidMeta pid : pids) {
            TableItem item = new TableItem(pidTable, SWT.MULTI);
            item.setText(PID, "0x" + NumberUtil.Object2Hex(pid.getPid()).toUpperCase());
            item.setText(PID_DESC, pid.getName());
            item.setChecked(pid.isEnable());
        }
    }

    private void renderTableIdTable() {
        List<TableMeta> list = tspConfig.getTableIdList();
        for (TableMeta meta : list) {
            TableItem item = new TableItem(tableIdTable, SWT.MULTI);
            item.setText(TId_FROM, "0x" + NumberUtil.Object2Hex(meta.getStart()).toUpperCase());
            item.setText(TId_TO, "0x" + NumberUtil.Object2Hex(meta.getEnd()).toUpperCase());
            item.setText(TABLE_SHORT_NAME, meta.getShortName());
            item.setText(TABLE_DESC, meta.getDescript());
            if (meta.getFullClassName() != null) {
                item.setText(TABLE_FULL_CLASS_NAME, meta.getFullClassName());
            }
            item.setChecked(meta.isEnable());
        }
    }

    @SuppressWarnings("unused")
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        createActions();
        initializeToolBar();
        tspConfig = RuntimeConfig.TSP_Config;
        // log.info(tspConfig.PIDS);
        body.setLayout(new FillLayout());
        tabFolder = new TabFolder(body, SWT.FLAT | SWT.FILL | SWT.BOTTOM);

        // pid table
        {
            TabItem pidTabItem = new TabItem(tabFolder, SWT.FLAT);// new tabItem
            pidTabItem.setText(PID_TEXT);
            Composite composite = new Composite(tabFolder, SWT.FILL);
            pidTabItem.setControl(composite);
            composite.setLayout(new FillLayout());

            TableViewer tv = new TableViewer(composite, //
                    SWT.SINGLE | SWT.HIDE_SELECTION | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
            pidTable = tv.getTable();
            createTableColumn(pidTable, "PID", SWT.MULTI, 100);
            createTableColumn(pidTable, "DESCRIPT", SWT.MULTI, 200);
            pidTable.setHeaderVisible(true);
            pidTable.setLinesVisible(true);
            renderPidTable();
            supportEdit(pidTable);
        }

        // table id table
        {
            TabItem tableIdTabItem = new TabItem(tabFolder, SWT.FLAT);// new
                                                                      // tabItem
            tableIdTabItem.setText(TABLE_ID_TEXT);
            Composite composite = new Composite(tabFolder, SWT.FILL);
            tableIdTabItem.setControl(composite);
            composite.setLayout(new FillLayout());
            TableViewer tv = new TableViewer(composite, //
                    SWT.SINGLE | SWT.HIDE_SELECTION | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
            tableIdTable = tv.getTable();

            createTableColumn(tableIdTable, "ID FROM", SWT.MULTI | SWT.CHECK, 70);
            createTableColumn(tableIdTable, "ID TO", SWT.MULTI, 50);
            createTableColumn(tableIdTable, "SHORT NAME", SWT.MULTI, 150);
            createTableColumn(tableIdTable, "DESCRIPT", SWT.MULTI, 250);
            createTableColumn(tableIdTable, "FULL CLASS NAME", SWT.MULTI, 400);
            tableIdTable.setHeaderVisible(true);
            tableIdTable.setLinesVisible(true);
            renderTableIdTable();
            supportEdit(tableIdTable);
        }

        // Other setting
        {
            TabItem tableIdTabItem = new TabItem(tabFolder, SWT.FLAT);// new
                                                                      // tabItem
            tableIdTabItem.setText(Other);
            Composite composite = new Composite(tabFolder, SWT.FILL);
            tableIdTabItem.setControl(composite);
            composite.setLayout(new GridLayout(4, true));
            if (false) {
                serviceSupport = new Button(composite, SWT.CHECK);
                serviceSupport.setText("Service List support");
                serviceSupport.setSelection(tspConfig.isServiceListAppSuppourt());
            }

            epgSupport = new Button(composite, SWT.CHECK);
            epgSupport.setText("EPG support");
            epgSupport.setSelection(tspConfig.isEpgAppSuppourt());

        }
        body.layout();
    }

    private void initializeToolBar() {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        toolBarManager.add(applyAction);
    }

    private void applyCurrentSetting() {
        try {
            {
                List<PidMeta> pids = tspConfig.getPIDS();
                pids.clear();
                TableItem[] tableItems = pidTable.getItems();
                TableItem tableItem = null;
                String pidStrHex = null;
                String pidDescStr = null;
                for (int i = 0; i < tableItems.length; i++) {
                    tableItem = tableItems[i];
                    pidStrHex = tableItem.getText(PID);
                    pidStrHex = pidStrHex.replaceAll("0x", "").trim();
                    pidDescStr = tableItem.getText(PID_DESC).trim();
                    // log.info(pidStrHex + "\t" + pidDescStr);
                    pids.add(new PidMeta(Integer.valueOf(pidStrHex, 16), pidDescStr, tableItem.getChecked()));
                }
            }

            {
                List<TableMeta> metaList = tspConfig.getTableIdList();
                metaList.clear();
                TableItem[] tableItems = tableIdTable.getItems();
                TableItem tableItem = null;
                String tidFromStr = null;
                String tidToStr = null;
                String shortName = null;
                String desc = null;
                String fullClassName = null;
                for (int i = 0; i < tableItems.length; i++) {
                    tableItem = tableItems[i];
                    tidFromStr = tableItem.getText(TId_FROM);
                    tidFromStr = tidFromStr.replaceAll("0x", "").trim();
                    tidToStr = tableItem.getText(TId_TO);
                    tidToStr = tidToStr.replaceAll("0x", "").trim();

                    shortName = tableItem.getText(TABLE_SHORT_NAME).trim();
                    desc = tableItem.getText(TABLE_DESC).trim();
                    fullClassName = tableItem.getText(TABLE_FULL_CLASS_NAME).trim();
                    metaList.add(new TableMeta(//
                            Integer.valueOf(tidFromStr, 16),//
                            Integer.valueOf(tidToStr, 16),//
                            shortName,//
                            desc,//
                            fullClassName,//
                            tableItem.getChecked()));
                }
            }

            tspConfig.setServiceListAppSuppourt(serviceSupport.getSelection());
            tspConfig.setEpgAppSuppourt(epgSupport.getSelection());

            String xml = TSPConfig.toXML(tspConfig);
            FileUtil.writeStringToFile(TSPActivator.CONFIGURE_FILE, xml);
            showMessage("Info", "Successfully Apply new setting\r\n", SWT.ICON_WORKING);
        } catch (NumberFormatException e) {
            showMessage("Error", "Fail error=" + e, SWT.ICON_ERROR);
            e.printStackTrace();
        }
    }

    private void createActions() {
        applyAction = new Action() {
            private static final long serialVersionUID = 1L;

            public void run() {
                applyCurrentSetting();
            }
        };
        applyAction.setText("Apply");
        applyAction.setToolTipText("Apply current setting");
        applyAction.setImageDescriptor(applyImageDescriptor);

    }

    private Text inputTextEdit = null;

    private void supportEdit(final Table table) {
        final TableEditor editor = new TableEditor(table);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;

        table.addMouseListener(new MouseAdapter() {
            private static final long serialVersionUID = -219343428342202243L;

            public void mouseDown(MouseEvent event) {
                Control old = editor.getEditor();
                if (old != null)
                    old.dispose();

                Point pt = new Point(event.x, event.y);
                int row = table.getSelectionIndex();
                if (row == -1)
                    return;

                final TableItem item = table.getItem(row);
                int column = -1;

                if (item != null) {
                    for (int i = 0, n = table.getColumnCount(); i < n; i++) {
                        Rectangle rect = item.getBounds(i);
                        if (rect.contains(pt)) {
                            column = i;
                            break;
                        }
                    }
                }

                if (column == -1)
                    return;

                log.debug("(" + row + "," + column + ")");

                if (inputTextEdit != null) {
                    inputTextEdit.dispose();
                }

                inputTextEdit = new Text(table, SWT.NONE);
                inputTextEdit.setText(item.getText(column));
                inputTextEdit.setForeground(item.getForeground());
                inputTextEdit.selectAll();
                inputTextEdit.setFocus();
                inputTextEdit.setToolTipText(item.getText(column));

                editor.minimumWidth = inputTextEdit.getBounds().width;
                // editor.minimumHeight = inputTextEdit.getBounds().height;
                editor.setEditor(inputTextEdit, item, column);

                final int col = column;
                inputTextEdit.addModifyListener(new ModifyListener() {
                    private static final long serialVersionUID = -4334501058374462920L;

                    public void modifyText(ModifyEvent event) {
                        item.setText(col, inputTextEdit.getText());
                    }
                });
            }
        });
    }
}
