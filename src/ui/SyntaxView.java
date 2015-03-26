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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import core.FileUtil;
import core.Generator;
import core.SyntaxBuildFactory;

class SyntaxTreeMenuListener implements MenuListener {
    private static final long serialVersionUID = 1L;
    private Tree tree;

    public SyntaxTreeMenuListener(Tree tree) {
        super();
        this.tree = tree;
    }

    @Override
    public void menuHidden(MenuEvent e) {

    }

    @Override
    public void menuShown(MenuEvent e) {
        Menu menu = (Menu) e.getSource();
        if (menu == null) {
            return;
        }
        MenuItem[] menuItems = menu.getItems();
        int menuItemLength = menuItems.length;
        MenuItem menuItem = null;

        TreeItem[] items = tree.getSelection();
        if (items.length <= 0) {
            for (int i = 0; i < menuItemLength; i++) {
                menuItem = menuItems[i];
                menuItem.setEnabled(false);
            }
            return;
        }
        TreeItem item = items[0];
        int treeLevel = (Integer) item.getData("level");
        // System.out.println(item + "\t" + treeLevel);

        for (int i = 0; i < menuItemLength; i++) {
            menuItem = menuItems[i];
            int level = (Integer) menuItem.getData("level");
            if (level == treeLevel) {
                menuItem.setEnabled(true);
            } else {
                menuItem.setEnabled(false);
            }
            menuItem.setData("TreeItem", item);
        }
    }
}

public class SyntaxView extends MyViewPart { //
    private static Logger log = Logger.getLogger(SyntaxView.class);
    static final String ID = "SyntaxView";
    private File root;
    private Tree syntaxTree;
    private Menu menu;// syntaxTree context menu
    private CTabFolder tabFolder;
    private Action applyAction;
    private Action findAction;
    private Map<File, TreeItem> treeItemMap = new HashMap<File, TreeItem>();

    private Image folderImage = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/folder.gif").createImage();
    private Image fileImage = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/section.png").createImage();
    private ImageDescriptor applyImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/apply.gif");
    private ImageDescriptor findImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/find.gif");

    private Listener doubleClickExpandListener = new Listener() {
        private static final long serialVersionUID = 1L;

        public void handleEvent(Event event) {
            Widget parent = event.widget;
            if (parent instanceof Tree) {
                Tree tree = (Tree) parent;
                Point point = new Point(event.x, event.y);
                try {
                    TreeItem item = tree.getItem(point);
                    if (item.getItems().length > 0) {
                        item.setExpanded(!item.getExpanded());
                    }
                } catch (Exception e) {
                    log.info("Invalid position");
                }
            }
        }
    };

    private Listener selectSyntaxListener = new Listener() {
        private static final long serialVersionUID = 1L;

        public void handleEvent(Event event) {
            Point point = new Point(event.x, event.y);
            try {
                TreeItem item = syntaxTree.getItem(point);
                File file = (File) item.getData("File");
                if (file != null && file.isFile()) {
                    renderFile(file);
                }

            } catch (Exception e) {
                // log.info("Invalid position");
            }
        }
    };

    private CTabItem findTabByFile(File file) {
        CTabItem[] tabs = tabFolder.getItems();
        for (CTabItem c : tabs) {
            File _file = (File) c.getData("File");
            if (file.getName().equals(_file.getName())) {// already open
                return c;
            }
        }
        return null;
    }

    private void renderFile(File file) {
        CTabItem item = findTabByFile(file);
        if (item != null) {
            tabFolder.setSelection(item);
            return;
        }

        StringBuffer sb = FileUtil.readFileToStringBuffer(file.getAbsolutePath());
        CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE/* SWT.FLAT */);// new
                                                                            // tabItem
        tabItem.setData("File", file);
        tabItem.setText(file.getName());
        tabItem.setToolTipText(file.getAbsolutePath());
        Composite comp = new Composite(tabFolder, SWT.FILL);
        comp.setLayout(new FillLayout());
        tabItem.setControl(comp);
        Text syntaxText = new Text(comp, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        syntaxText.setText(sb.toString());
        tabItem.setData("Text", syntaxText);
        tabFolder.setSelection(tabItem);
        tabFolder.layout();
    }

    private void createSyntaxText(Composite comp) {
        tabFolder = new CTabFolder(comp, /* SWT.FLAT | */SWT.TOP);
        tabFolder.setBorderVisible(false);
    }

    private void addContextmenu() {
        if (syntaxTree == null) {
            return;
        }

        menu = new Menu(syntaxTree);
        menu.addMenuListener(new SyntaxTreeMenuListener(syntaxTree));
        {
            MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
            menuItem.setText("Add folder");
            menuItem.setData("level", 0);
            menuItemAddListener(menuItem, 0);
        }
        {
            MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
            menuItem.setText("Remove folder");
            menuItem.setData("level", 1);
            menuItemAddListener(menuItem, 1);
        }
        {
            MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
            menuItem.setText("Add syntax");
            menuItem.setData("level", 2);
            menuItemAddListener(menuItem, 2);
        }
        {
            MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
            menuItem.setText("Remove syntax");
            menuItem.setData("level", 3);
            menuItemAddListener(menuItem, 3);
        }
        syntaxTree.setMenu(menu);
    }

    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        createActions();
        initializeToolBar();
        body.setLayout(new FillLayout());
        SashForm sashFormH = new SashForm(body, SWT.HORIZONTAL | SWT.FILL);
        Composite leftComp = new Composite(sashFormH, SWT.BORDER);
        leftComp.setLayout(new FillLayout());
        createSyntaxTree(leftComp);

        Composite rightComp = new Composite(sashFormH, SWT.BORDER);
        rightComp.setLayout(new FillLayout());
        createSyntaxText(rightComp);
        renderSyntaxTree();
        addContextmenu();
        sashFormH.setWeights(new int[] { 1, 2 });

        sashFormH.layout();
        body.layout();
    }

    private void initializeToolBar() {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        toolBarManager.add(applyAction);
        toolBarManager.add(findAction);
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

                // log.info(file);
                // log.info(syntaxText.getText());
                try {
                    FileUtil.writeStringToFile(file.getAbsolutePath(), syntaxText.getText());
                    Generator.parseFile(file, false);
                    SyntaxBuildFactory.refreshClassLoader();
                    showMessage("Info",//
                            "Apply new Syntax:" + file.getName(),//
                            SWT.ICON_WORKING);
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox mb = new MessageBox(syntaxTree.getShell(), SWT.ICON_WORKING);
                    mb.setText("Error");
                    mb.setMessage(e.getMessage());
                    mb.open();
                }
            }
        };
        applyAction.setText("Apply");
        applyAction.setToolTipText("Apply this syntax");
        applyAction.setImageDescriptor(applyImageDescriptor);

        findAction = new Action() {
            private static final long serialVersionUID = 1L;

            public void run() {
                showFindDialog();
            }
        };
        findAction.setText("Find");
        findAction.setToolTipText("Find syntax");
        findAction.setImageDescriptor(findImageDescriptor);
    }

    private void showFindDialog() {
        final SyntaxFindDialog findDialog = //
        new SyntaxFindDialog(this, root);
        /* int returnCode = */findDialog.open();
        // if (returnCode == Window.OK) {
        // }
    }

    private void listDirectory(File dir, TreeItem parent) {
        int level = (Integer) parent.getData("level");
        if (dir.isDirectory()) {
            TreeItem thisTreeItem = new TreeItem(parent, 0);
            thisTreeItem.setText(dir.getName());
            thisTreeItem.setImage(folderImage);
            thisTreeItem.setData("level", (level + 1));
            thisTreeItem.setData("File", dir.getAbsoluteFile());
            String[] children = dir.list();
            Arrays.sort(children);
            for (int i = 0; i < children.length; i++) {
                listDirectory(new File(dir, children[i]), thisTreeItem);
            }
            // thisTreeItem.setExpanded(true);
        } else {
            TreeItem thisTreeItem = new TreeItem(parent, 0);
            thisTreeItem.setImage(fileImage);
            thisTreeItem.setText(dir.getName());
            thisTreeItem.setData("File", dir.getAbsoluteFile());
            thisTreeItem.setData("level", (level + 1));
            treeItemMap.put(dir, thisTreeItem);
        }
    }

    private void createSyntaxTree(Composite comp) {
        syntaxTree = new Tree(comp, SWT.FILL);
        syntaxTree.setLinesVisible(true);
        syntaxTree.addListener(SWT.MouseDown, selectSyntaxListener);
        syntaxTree.addListener(SWT.MouseDoubleClick, doubleClickExpandListener);
    }

    void selectTreeItemByFile(File file) {
        TreeItem treeItem = treeItemMap.get(file);
        syntaxTree.select(treeItem);
        syntaxTree.setTopItem(treeItem);
        renderFile(file);
    }

    private void addFolderDialog() {
        final IInputValidator val = new IInputValidator() {
            private static final long serialVersionUID = 1L;

            public String isValid(final String newText) {
                String result = null;
                if (newText == null || newText.length() == 0) {
                    return "";
                }

                char firstletter = newText.charAt(0);
                if (Character.isDigit(firstletter)) {
                    result = "The first character must not be digital";
                }
                return result;
            }
        };
        String title = "Add new Folder";
        String msg = "Enter new folder name ";
        String def = null;

        final InputDialog dlg;
        dlg = new InputDialog(this.getSite().getShell(), title, msg, def, val);
        int returnCode = dlg.open();

        String newFolderName = null;
        if (returnCode == Window.OK) {
            newFolderName = dlg.getValue();
            // log.info(newFolderName);
            // Create new Folder at root folder
            String newFolder = root.getAbsolutePath() + File.separator + newFolderName;
            String newFolderDescriptor = newFolder + File.separatorChar + "descriptor";
            String newFoldersection = newFolder + File.separatorChar + "section";
            FileUtil.createDirectory(newFolder);
            FileUtil.createDirectory(newFolderDescriptor);
            FileUtil.createDirectory(newFoldersection);
            renderSyntaxTree();
        }
    }

    private void deleteFolderDialog() {
        TreeItem[] items = syntaxTree.getSelection();
        if (items.length <= 0) {
            return;
        }
        TreeItem item = items[0];
        // int treeLevel = (Integer) item.getData("level");
        // log.info(item + "\t" + treeLevel + "\t" + file);
        File file = (File) item.getData("File");
        if (file == null) {
            return;
        }
        String message = "Delete folder " + file.getName() + "\r\nCan not be restored";
        boolean ques = MessageDialog.openQuestion(syntaxTree.getShell(), "Information", message);

        if (ques == false) {
            return;
        }

        if (file.isDirectory()) {
            boolean flag = FileUtil.deleteDirectory(file.getAbsoluteFile());
            if (!flag) {
                System.out.println(flag);
            }

            SyntaxBuildFactory.resetAll();
            String ROOT_DIR = TSPActivator.ROOT;
            Generator.generatorSytax(ROOT_DIR, false);// in memory compile
            renderSyntaxTree();
        }
    }

    private void addSyntaxDialog(MenuItem menuItem) {
        final NewSyntaxDialog newSyntaxDialog = //
        new NewSyntaxDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), menuItem);
        int returnCode = newSyntaxDialog.open();
        if (returnCode == Window.OK) {
            this.renderSyntaxTree();
        }
    }

    private void deleteSyntax(MenuItem menuItem) {
        TreeItem item = (TreeItem) menuItem.getData("TreeItem");
        if (item == null) {
            return;
        }
        File file = (File) item.getData("File");
        if (file == null) {
            return;
        }
        // log.info(item + "\t" + file);
        String message = "Delete syntax " + file.getName() + //
                "\r\nCan not be restored";
        boolean ques = MessageDialog.openQuestion(syntaxTree.getShell(), "Information", message);
        if (ques == false) {
            return;
        }

        boolean flag = file.delete();
        if (flag == false) {
            MessageDialog.openConfirm(syntaxTree.getShell(), "", "Delete fail");
            return;
        }

        CTabItem cTabItem = findTabByFile(file);
        if (cTabItem != null) {
            cTabItem.dispose();
        }

        SyntaxBuildFactory.resetAll();
        String ROOT_DIR = TSPActivator.ROOT;
        Generator.generatorSytax(ROOT_DIR, false);// in memory compile

        renderSyntaxTree();
    }

    private void menuItemAddListener(final MenuItem menuItem, final int level) {
        menuItem.addSelectionListener(new SelectionAdapter() {
            private static final long serialVersionUID = 1L;

            public void widgetSelected(final SelectionEvent event) {
                switch (level) {
                case 0:// Root
                    addFolderDialog();
                    break;
                case 1:// Folder
                    deleteFolderDialog();
                    break;
                case 2:// descriptor or section folder
                    addSyntaxDialog(menuItem);
                    break;
                case 3:// Root
                    deleteSyntax(menuItem);
                    break;
                default:
                    break;
                }
            }

        });
    }

    private void renderSyntaxTree() {
        syntaxTree.removeAll();
        root = new File(TSPActivator.ROOT + File.separator + "syntax" + File.separator + "input");
        if (!root.exists()) {
            log.info("File not exist:" + root.getAbsolutePath());
            return;
        }
        log.info(root.getAbsoluteFile());
        treeItemMap.clear();
        TreeItem rootTree = new TreeItem(syntaxTree, 0);
        rootTree.setImage(folderImage);
        rootTree.setText(root.getAbsoluteFile().toString());
        rootTree.setData("level", 0);

        File[] files = root.listFiles();
        Arrays.sort(files);
        for (File file : files) {
            listDirectory(file, rootTree);
        }
        rootTree.setExpanded(true);
        syntaxTree.redraw();
    }

    public void setFocus() {

    }

    public void dispose() {
        super.dispose();
        treeItemMap.clear();
        treeItemMap = null;
    }

    public String getHelp() {
        return null;
    }
}
