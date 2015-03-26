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
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

import core.BitStream;
import core.CommonParser;
import core.IParseNotify;
import core.MyStreamFilter;
import core.NodeValue;
import core.TSSection;
import core.TSUtil;
import core.TableMeta;

public class TSView extends MyViewPart { //
    private static Logger log = Logger.getLogger(TSView.class);
    static final String ID = "TSView";

    private BitStream bitStream;
    private Tree tableIdSectiontree;
    private Text sectionText;
    private Tree sectionTree;
    private Text sectionRawText;
    private Text syntaxText;
    private ProgressMonitorDialog progressDialog;
    private IProgressMonitor progressMonitor;
    private Scale tsTimeScale;
    private Text tsTimeText;
    private TabFolder tsTabFolder;
    private SashForm sashFormV;

    private Action tsInfo;
    private Action reParse;
    private Action exportAction;
    private Action filterAction;

    private Listener selectTableIdListener = new Listener() {
        private static final long serialVersionUID = 1L;

        public void handleEvent(Event event) {
            Point point = new Point(event.x, event.y);
            try {
                TreeItem item = tableIdSectiontree.getItem(point);
                if (item == null) {
                    TreeItem[] items = tableIdSectiontree.getSelection();
                    if (items.length > 0) {
                        item = items[0];
                    }
                }
                if (item == null) {
                    return;
                }

                TSSection tsSection = (TSSection) (item.getData("TSSection"));
                if (tsSection == null) {
                    return;
                }
                renderTsSection(tsSection);

            } catch (Exception e) {
                // log.info("Invalid position");
            }
        }
    };

    private KeyListener sectionTreeKeyListener = new KeyListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    };

    private Listener doubleClickExpandListener = new Listener() {
        private static final long serialVersionUID = 1L;

        public void handleEvent(Event event) {
            Widget parent = event.widget;
            TSSection tsSection = null;
            if (parent instanceof Tree) {
                Tree tree = (Tree) parent;
                Point point = new Point(event.x, event.y);
                try {
                    TreeItem item = tree.getItem(point);
                    if (item == null) {
                        return;
                    }
                    // log.info(item.getText() + "\t" + item.getItems().length +
                    // "\t" + item.getExpanded());
                    if (item.getItems().length > 0) {
                        item.setExpanded(!item.getExpanded());
                        tree.redraw();
                    }
                    String level = (String) item.getData("level");
                    if (level == null) {
                        return;
                    }
                    if (level.equals("2")) {
                        if (item.getItems().length == 0) {
                            tsSection = (TSSection) item.getData("TSSection");
                            if (tsSection != null) {
                                UIUtil.renderNode((List<NodeValue>) tsSection.getRoot(), item);
                                item.setExpanded(true);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.info("Invalid position");
                }
            }
        }
    };

    private Listener tableTreeListener = new Listener() {
        private static final long serialVersionUID = 1L;

        public void handleEvent(Event event) {
            Point point = new Point(event.x, event.y);
            try {
                TreeItem item = sectionTree.getItem(point);
                if (item == null) {
                    return;
                }
            } catch (Exception e) {
                // log.info("Invalid position");
            }
        }
    };

    private IParseNotify notify = new IParseNotify() {
        public void notifyProgreess(final int progress, String info) {
            if (progressDialog != null) {
                progressDialog.getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        if (progressMonitor != null) {
                            progressMonitor.beginTask("Parsing", 100);
                            progressMonitor.worked(progress);
                        }
                        if (progressMonitor != null && progressMonitor.isCanceled()) {
                            log.info("progressMonitor canceled");
                            progressMonitor.beginTask("Canceling", 100);
                            TSView.this.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
                                public void run() {
                                    TSView.this.bitStream.setStop(true);
                                    TSView.this.bitStream.save2History();
                                }
                            });
                        }
                    }
                });
            }
            if (progress == 100) {
                progressDialog.getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        progressMonitor.beginTask("Parsing", 100);
                        progressMonitor.worked(progress);
                        progressMonitor.beginTask("Done ...", 100);
                        progressMonitor.done();
                        progressDialog.close();
                        TSView.this.uiStart(bitStream.getValidTime());
                        TSView.this.bitStream.save2History();
                    }
                });
            }
        }
    };

    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        createActions();
        initializeToolBar();

        body.setLayout(new FillLayout());

        sashFormV = new SashForm(body, SWT.VERTICAL | SWT.FILL);

        tsTabFolder = new TabFolder(sashFormV, SWT.BOTTOM);

        TabItem sectionTabItem = new TabItem(tsTabFolder, SWT.NONE);// new
                                                                    // tabItem
        sectionTabItem.setText("Section");

        tsTabFolder.addSelectionListener(new SelectionListener() {
            private static final long serialVersionUID = 1L;

            public void widgetSelected(SelectionEvent e) {
                int selectIdx = tsTabFolder.getSelectionIndex();
                if (selectIdx == 0) {
                    double tsRate = TSView.this.bitStream.getTsRate();
                    if (tsRate <= 0.0) {
                        sashFormV.setWeights(new int[] { 30, 0 });
                    } else {
                        sashFormV.setWeights(new int[] { 30, 2 });
                    }
                } else {
                    sashFormV.setWeights(new int[] { 30, 0 });
                }
                sashFormV.redraw();
            }

            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        Composite sectionComp_1 = new Composite(tsTabFolder, SWT.FILL);
        sectionComp_1.setLayout(new FillLayout());
        // tsTabFolder.addMouseListener(listener)

        SashForm sashFormH = new SashForm(sectionComp_1, SWT.HORIZONTAL);
        Composite navigatorComp = new Composite(sashFormH, SWT.NONE);
        Composite sectionComp = new Composite(sashFormH, SWT.NONE);

        sectionTabItem.setControl(sectionComp_1);

        createTableTreeNavigator(navigatorComp);

        createSectionInformationTab(sectionComp);

        // scale bar
        createScale(sashFormV);

        navigatorComp.layout();
        sectionComp.layout();
        sashFormH.setWeights(new int[] { 1, 2 });
        sashFormH.layout();
        sashFormV.setWeights(new int[] { 30, 2 });
        sashFormV.layout();
        body.layout();
    }

    private void uiStart(final int validTime) {
        this.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                renderTableNavigator(validTime);
            }
        });
    }

    private void createScale(SashForm sashFormV) {
        sashFormV.setLayout(new FillLayout());
        SashForm sashFormH = new SashForm(sashFormV, SWT.HORIZONTAL);

        tsTimeScale = new Scale(sashFormH, SWT.HORIZONTAL);
        tsTimeScale.setIncrement(1);
        tsTimeScale.setPageIncrement(1);

        Composite comp = new Composite(sashFormH, SWT.FILL);
        comp.setLayout(new GridLayout(2, true));
        tsTimeText = new Text(comp, SWT.NONE);
        tsTimeText.setText("0");
        Button goButton = new Button(comp, SWT.NONE);
        goButton.setText("Go");
        comp.layout();

        goButton.addSelectionListener(new SelectionListener() {
            private static final long serialVersionUID = 1L;

            public void widgetSelected(SelectionEvent e) {
                renderTableNavigator(tsTimeScale.getSelection());
                render3rdApp(tsTimeScale.getSelection());
            }

            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        sashFormH.setWeights(new int[] { 100, 10 });

        tsTimeScale.addSelectionListener(new SelectionListener() {
            private static final long serialVersionUID = 1L;

            public void widgetSelected(final SelectionEvent event) {
                tsTimeText.setText("" + tsTimeScale.getSelection() + "/" + bitStream.getValidTime());
            }

            public void widgetDefaultSelected(final SelectionEvent event) {
                tsTimeText.setText("" + tsTimeScale.getSelection() + "/" + bitStream.getValidTime());
            }

        });
    }

    private void createSectionInformationTab(Composite sectionComp) {
        sectionComp.setLayout(new FillLayout());
        TabFolder tabFolder = new TabFolder(sectionComp, SWT.FLAT);

        // Tree view table
        {
            TabItem sectionTreeView = new TabItem(tabFolder, SWT.FLAT);// new
                                                                       // tabItem
            sectionTreeView.setText("Tree");
            Composite sectionTreeViewComposite = new Composite(tabFolder, SWT.FILL);
            sectionTreeView.setControl(sectionTreeViewComposite);
            sectionTreeViewComposite.setLayout(new FillLayout());
            sectionTree = new Tree(sectionTreeViewComposite, SWT.NONE);
            sectionTree.setFont(TSPWorkbenchWindowAdvisor.fontRegistry.get("code"));
            sectionTree.addListener(SWT.MouseDown, tableTreeListener);
            sectionTree.addKeyListener(sectionTreeKeyListener);
        }

        // Section text view
        {
            TabItem sectionView = new TabItem(tabFolder, SWT.FLAT);
            sectionView.setText("Section");
            Composite body = new Composite(tabFolder, SWT.FILL);
            sectionView.setControl(body);
            body.setLayout(new FillLayout());

            sectionText = new Text(body, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
            sectionText.setFont(TSPWorkbenchWindowAdvisor.fontRegistry.get("code"));
            sectionText.setEditable(false);
        }

        // section Raw Text view
        {
            TabItem sectionRawView = new TabItem(tabFolder, SWT.FLAT);
            sectionRawView.setText("Section Raw");
            Composite body = new Composite(tabFolder, SWT.FILL);
            sectionRawView.setControl(body);
            body.setLayout(new FillLayout());

            sectionRawText = new Text(body, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
            sectionRawText.setFont(TSPWorkbenchWindowAdvisor.fontRegistry.get("code"));
            sectionRawText.setEditable(false);
        }

        // Sectin or descriptor syntax view
        {
            TabItem sectionRawView = new TabItem(tabFolder, SWT.FLAT);
            sectionRawView.setText("Syntax");
            Composite body = new Composite(tabFolder, SWT.FILL);
            sectionRawView.setControl(body);
            body.setLayout(new FillLayout());

            syntaxText = new Text(body, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);//
            syntaxText.setFont(TSPWorkbenchWindowAdvisor.fontRegistry.get("code"));
            syntaxText.setEditable(false);
        }
    }

    private void createTableTreeNavigator(final Composite navigatorComp) {
        navigatorComp.setLayout(new FillLayout());
        final TabFolder tabFolder = new TabFolder(navigatorComp, SWT.FLAT);
        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);// new tabItem
        tabItem.setText("Tables");

        Composite view = new Composite(tabFolder, SWT.FILL);
        tabItem.setControl(view);
        view.setLayout(new FillLayout());

        Composite tableComposite = new Composite(view, SWT.FILL);
        tableComposite.setLayout(new FillLayout());

        tableIdSectiontree = new Tree(tableComposite, SWT.FILL);
        tableIdSectiontree.addListener(SWT.MouseDown, selectTableIdListener);
        tableIdSectiontree.addListener(SWT.MouseDoubleClick, doubleClickExpandListener);
        tableIdSectiontree.addKeyListener(new KeyListener() {
            private static final long serialVersionUID = 4116719901720017682L;
            private StringBuffer sb = new StringBuffer();

            public void keyPressed(KeyEvent e) {
                TreeItem[] items = tableIdSectiontree.getSelection();
                if (items == null || items.length <= 0) {
                    return;
                }
                TreeItem item = items[0];
                TSSection tsSection = (TSSection) (item.getData("TSSection"));
                if (tsSection == null) {
                    return;
                }
                renderTsSection(tsSection);
            }

            public void keyReleased(final KeyEvent e) {
                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        sb.append(e.character);
                        String key = "author";
                        if (sb.toString().indexOf("author") >= 0) {
                            Shell parentShell = navigatorComp.getShell();
                            Shell shell;
                            shell = new Shell(parentShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
                            shell.setBounds(UIUtil.great.getBounds());
                            shell.open();

                            Rectangle p = parentShell.getBounds();
                            Rectangle c = shell.getBounds();

                            Rectangle rec = new Rectangle(0, 0, c.width, c.height);
                            rec.x = p.x + (p.width - c.width) / 2;
                            rec.y = p.y + (p.height - c.height) / 2;
                            shell.setBounds(rec);
                            shell.setText("");
                            shell.setLayout(new FillLayout());
                            shell.setBackgroundImage(UIUtil.great);
                            shell.setToolTipText("mail:flylb1@gmail.com");
                            shell.setAlpha(128);

                            shell.layout();
                            if (sb.length() >= key.length()) {
                                sb = null;
                                sb = new StringBuffer();
                            }
                        }
                    }
                });
            }
        });
    }

    private void renderTsSection(TSSection tsSection) {
        if (tsSection == null) {
            sectionTree.removeAll();
            sectionTree.clearAll(true);
            sectionTree.redraw();
            sectionText.setText("");
            sectionRawText.setText("");
            syntaxText.setText("");
            return;
        }

        // Show section parse result in text
        if (sectionText != null) {
            sectionText.setText(tsSection.dumpTextResult());
        }

        // Show section parse result in tree
        sectionTree.removeAll();
        sectionTree.clearAll(true);
        TreeItem root = new TreeItem(sectionTree, 0);
        root.setText(tsSection.shortName());
        if (tsSection.getCommonParser() != null) {
            UIUtil.renderNode((List<NodeValue>) tsSection.getRoot(), root);
        }
        root.setImage(UIUtil.sectionRootImage);
        sectionTree.removeListener(SWT.MouseDoubleClick, doubleClickExpandListener);
        sectionTree.addListener(SWT.MouseDoubleClick, doubleClickExpandListener);
        sectionTree.redraw();
        root.setExpanded(true);

        // Show Section raw data in HEX
        StringBuffer sb = new StringBuffer();
        String sectionData = tsSection.dumpSectionData();
        if (sectionData != null) {
            sb.append(sectionData);
        }
        String rawPacketData = tsSection.dumpSectionRawPacket();
        if (rawPacketData != null) {
            sb.append(rawPacketData);
        }

        sb.append("\r\n" + tsSection.briefTimeInfo());
        sectionRawText.setText(sb.toString());
        sectionRawText.redraw();

        // Show section syntax
        CommonParser commonParser = tsSection.getCommonParser();
        if (commonParser != null) {
            String syntax = commonParser.getSyntax();
            if (syntax != null) {
                syntaxText.setText(syntax);
                syntaxText.redraw();
            }
        }
    }

    void parseFile(final String fileName, final long size) {
        this.setTitleToolTip(fileName);
        this.setPartName("Bitstream:" + new File(fileName).getName());
        bitStream = new BitStream();
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        progressDialog = new ProgressMonitorDialog(shell);
        try {
            progressDialog.run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) {
                    monitor.beginTask("Parsing ...", 100);
                    parseTsFile(fileName, monitor, size);
                    monitor.beginTask("Done ...", 100);
                    monitor.done();
                }

            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public BitStream getBitStream() {
        return bitStream;
    }

    private void renderTableNavigator(final int time) {
        if (bitStream == null || tableIdSectiontree == null) {
            return;
        }

        // clear tree
        tableIdSectiontree.removeAll();

        Map<Integer, List<TSSection>> sectionMap = bitStream.getSectionManager().getTableidSection();
        Object[] keys = sectionMap.keySet().toArray();
        Arrays.sort(keys);
        for (Object tableId : keys) {
            TreeItem treeItem = null;
            TableMeta tableConfig = bitStream.getParser().getTableConfigByTableId((Integer) tableId);
            if (tableConfig != null) {
                String tableShortName = tableConfig.getShortName();
                if (tableShortName.indexOf("reserved") != -1) {
                    continue;
                }
                treeItem = new TreeItem(tableIdSectiontree, SWT.NONE);
                treeItem.setText(tableShortName + " [0x" + Integer.toHexString((Integer) tableId).toUpperCase() + "]");
                treeItem.setData("level", "1");
                treeItem.setImage(UIUtil.tableImage);

                List<TSSection> sections = sectionMap.get(tableId);
                if (sections == null) {
                    continue;
                }
                Collections.sort(sections);
                if (sections != null) {
                    treeItem.setText(tableShortName + " [0x" + Integer.toHexString((Integer) tableId).toUpperCase() + "] number:"
                            + sections.size());
                }

                int sectionCount = 0;
                for (TSSection tsSection : sections) {
                    if (TSUtil.sectionInTime(tsSection, time, bitStream)) {
                        TreeItem sectiontreeItem = new TreeItem(treeItem, SWT.NONE);
                        if (tsSection.isHasParse() == true) {
                            sectiontreeItem.setText(tsSection.shortName());
                        } else {
                            sectiontreeItem.setText("[***]" + tsSection.shortName());
                            sectiontreeItem.setForeground(new Color(null, 255, 0, 0));
                        }

                        sectiontreeItem.setData("TSSection", tsSection);

                        sectiontreeItem.setImage(UIUtil.sectionImage);
                        sectionCount++;
                        sectiontreeItem.setData("level", "2");
                    }
                }
                if (sectionCount == 0) {
                    treeItem.dispose();
                }
            }
        }

        tableIdSectiontree.setRedraw(true);
        renderTsSection(null);
        render3rdApp(tsTimeScale.getSelection());

        tsTabFolder.setSelection(0);// default select section table
        if (bitStream.getTsRate() <= 0.0) {
            sashFormV.setWeights(new int[] { 30, 0 });
        } else {
            sashFormV.setWeights(new int[] { 30, 2 });
        }
        sashFormV.redraw();

        this.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                int totalTime = bitStream.getTotalTime();
                if (bitStream.getValidTime() <= 0) {
                    return;
                }
                tsTimeScale.setMaximum(bitStream.getValidTime());
                tsTimeScale.setMinimum(0);
                tsTimeScale.setSelection(time);
                tsTimeScale.setToolTipText("Total TS time=" + totalTime + " current time=" + time);
                tsTimeText.setText("" + time + "/" + bitStream.getValidTime());
            }
        });
    }

    private void render3rdApp(final int time) {
        List<ApplicationMeta> appMetas = ApplicationConfigureFactory.getAppMetas();
        try {
            Class<?> clazz = null;
            File srcFile = null;
            int appMetasSize = appMetas.size();

            // close all table item dynamic created
            TabItem[] items = tsTabFolder.getItems();
            int length = items.length;
            for (int i = 0; i < length; i++) {
                if (items[i] != null && items[i].getData("app") != null) {
                    items[i].dispose();
                }
            }

            for (int i = 0; i < appMetasSize; i++) {
                clazz = appMetas.get(i).getClazz();
                srcFile = appMetas.get(i).getSrcFile();
                items = tsTabFolder.getItems();

                final IApplication app = (IApplication) clazz.newInstance();

                for (TabItem item : items) {
                    if (item.getText().equalsIgnoreCase(app.getAppName())) {// already opened,close it
                        IApplication oldApp = (IApplication) item.getData("app");
                        if (oldApp != null) {
                            oldApp = null;
                            item.setData("app", null);
                        }
                        item.dispose();
                    }
                }
                final TabItem applicationTabItem = new TabItem(tsTabFolder, SWT.FLAT);// new tabItem

                applicationTabItem.setText(app.getAppName());
                applicationTabItem.setData("File", srcFile);
                if (srcFile != null) {
                    applicationTabItem.setToolTipText(srcFile.getAbsolutePath());
                    applicationTabItem.setImage(UIUtil.sectionImage);
                } else {
                    applicationTabItem.setImage(UIUtil.binImage);
                }

                applicationTabItem.setData("app", app);
                app.render(applicationTabItem, TSView.this.getBitStream());
                Control control = applicationTabItem.getControl();
                if (control != null) {
                    control.redraw();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseTsFile(String fileName, IProgressMonitor monitor, long size) {
        MyStreamFilter streamFilter = (MyStreamFilter) RWT.getUISession().getAttribute("StreamFilter");
        bitStream.setStreamFilter(streamFilter);
        log.info(streamFilter);
        progressMonitor = monitor;
        File file = null;
        try {
            file = new File(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        BitStream.parseSingleFile(bitStream, file, size, notify);
    }

    private void createActions() {
        reParse = new Action() {
            private static final long serialVersionUID = 1L;

            public void run() {
                showReParseDialog();
            }
        };
        reParse.setText("Reparse file");
        reParse.setToolTipText("Reparse file");
        reParse.setImageDescriptor(UIUtil.reparseImageDescriptor);

        filterAction = new Action() {
            private static final long serialVersionUID = 1L;

            public void run() {
                final ConfigFilterDialog configFilterDialog = //
                new ConfigFilterDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), bitStream);
                /* int returnCode = */configFilterDialog.open();
            }
        };
        filterAction.setText("Filter section");
        filterAction.setToolTipText("Filter section");
        filterAction.setImageDescriptor(UIUtil.filterImageDescriptor);

        tsInfo = new Action() {
            private static final long serialVersionUID = 1L;

            public void run() {
                Shell parentShell = getSite().getShell();
                Shell shell;
                shell = new Shell(parentShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
                shell.open();

                Rectangle p = parentShell.getBounds();
                Rectangle c = shell.getBounds();

                Rectangle rec = new Rectangle(0, 0, c.width, c.height);
                rec.x = p.x + (p.width - c.width) / 2;
                rec.y = p.y + (p.height - c.height) / 2;
                shell.setBounds(rec);
                shell.setText("Bitstream Information");

                shell.setLayout(new FillLayout());
                Text text = new Text(shell, SWT.MULTI | SWT.WRAP);
                text.setEditable(false);
                text.setText((bitStream == null) ? "" : (bitStream.parseResult()));
                shell.layout();
            }
        };
        tsInfo.setText("TS Information");
        tsInfo.setToolTipText("TS Information");
        tsInfo.setImageDescriptor(UIUtil.infoImageDescriptor);

        exportAction = new Action() {
            private static final long serialVersionUID = 1L;

            public void run() {
                Browser browser = null;
                if (browser == null) {
                    browser = new Browser(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.NONE);
                    browser.setVisible(false);
                }

                ServiceManager manager = RWT.getServiceManager();
                StringBuilder url = new StringBuilder();
                HttpServletRequest req = RWT.getRequest();

                // log.info(manager.getServiceHandlerUrl("tsExporthandler"));
                url.append(req.getRequestURL());
                url.append(manager.getServiceHandlerUrl("tsExporthandler").replaceAll("/TSP", ""));
                url.append("&filename=");
                url.append(bitStream.getFile().getAbsolutePath());
                // log.info(url.toString());
                browser.setUrl(url.toString());
            }
        };
        exportAction.setText("Export PSI/SI data");
        exportAction.setToolTipText("Export PSI/SI data");
        exportAction.setImageDescriptor(UIUtil.exportImageDescriptor);

    }

    private void initializeToolBar() {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

        toolBarManager.add(filterAction);
        toolBarManager.add(reParse);
        toolBarManager.add(tsInfo);
        toolBarManager.add(exportAction);
    }

    public String getHelp() {
        return null;
    }

    private void showReParseDialog() {
        final IInputValidator val = new IInputValidator() {
            private static final long serialVersionUID = 1L;

            public String isValid(final String newText) {
                String result = null;
                long size = 0;
                try {
                    size = Integer.parseInt(newText);
                } catch (NumberFormatException e) {
                    result = "Please input number";
                }
                if (size < 0) {
                    result = "Please input number";
                }
                return result;
            }
        };
        String title = "Reparse File:" + this.bitStream.getFile().getAbsolutePath();
        String mesg = "Enter size[M] for parse ";
        String def = null;

        def = new String("5");
        final InputDialog dlg;
        dlg = new InputDialog(this.getSite().getShell(), title, mesg, def, val);
        dlg.create();
        int returnCode = dlg.open();

        if (returnCode == Window.OK) {
            try {
                long size = Integer.parseInt(dlg.getValue()) * 1024 * 1024;
                parseFile(bitStream.getFile().getAbsolutePath(), size);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public void dispose() {
        super.dispose();
        // log.info(this + " dispose");
        this.bitStream = null;
    }
}
