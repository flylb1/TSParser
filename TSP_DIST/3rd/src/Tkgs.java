import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import ui.IApplication;
import ui.UIUtil;
import core.BitStream;
import core.CommonParser;
import core.NodeValue;
import core.SyntaxBuildFactory;
import core.TSSection;
import core.TSUtil;

public class Tkgs implements IApplication {
    private Tree tkgsTree;
    private BitStream bitStream;
    private SashForm sashFormH;
    private CTabFolder tabFolder;
    private Composite versionComp;
    private Set<Integer> versionSet = new HashSet<Integer>();

    public void render(TabItem applicationTabItem, BitStream bitStream) {
        this.bitStream = bitStream;
        Composite parentContainer = applicationTabItem.getParent();
        Composite parentComp = new Composite(parentContainer, SWT.FILL);
        applicationTabItem.setControl(parentComp);
        parentComp.setLayout(new FillLayout());
        parentComp.setBackground(new Color(null, 255, 0, 0));
        parentComp.layout();

        sashFormH = new SashForm(parentComp, SWT.HORIZONTAL | SWT.FILL);
        versionComp = new Composite(sashFormH, SWT.FILL | SWT.BORDER);

        tabFolder = new CTabFolder(sashFormH, SWT.TOP);
        tabFolder.setBorderVisible(false);

        // Tree view table
        CTabItem tkgsTreeTabItem = new CTabItem(tabFolder, SWT.FLAT);
        tkgsTreeTabItem.setText("Tree");
        Composite tkgsTreeViewComposite = new Composite(tabFolder, SWT.FILL);
        tkgsTreeTabItem.setControl(tkgsTreeViewComposite);

        tkgsTreeViewComposite.setLayout(new FillLayout());
        tkgsTree = new Tree(tkgsTreeViewComposite, SWT.NONE);

        tabFolder.setSelection(0);

        renderTkgsVersion();
        versionComp.setLayout(new RowLayout());

        sashFormH.setWeights(new int[] { 10, 90 });
        sashFormH.layout();
        parentContainer.layout();
    }

    private void renderTkgsVersion() {
        versionSet.clear();
        List<TSSection> tkgsSections = TSUtil.getSectionsByTableid(bitStream, 0xA7);
        for (int i = 0; i < tkgsSections.size(); i++) {
            TSSection tsSection = (TSSection) tkgsSections.get(i);
            int versionNumber = tsSection.getVersion_number();
            versionSet.add(versionNumber);
        }

        Iterator<Integer> iter = versionSet.iterator();
        while (iter.hasNext()) {
            Integer ver = (Integer) iter.next();
            Button button = new Button(versionComp, SWT.NONE);
            button.setText("Ver:" + ver);
            button.setData(ver);
            button.addSelectionListener(selectionListener);
        }
    }

    SelectionListener selectionListener = new SelectionListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void widgetSelected(SelectionEvent e) {
            Button button = (Button) e.widget;
            if (button == null) {
                return;
            }
            Integer ver = (Integer) button.getData();
            renderTkgsBody(ver);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }
    };

    private void renderTkgsBody(Integer ver) {
        tkgsTree.removeAll();
        tkgsTree.clearAll(true);

        byte[] byteBuffer = new byte[4096 * 32];
        List<TSSection> tkgsSections = TSUtil.getSectionsByTableid(bitStream, 0xA7);
        TSSection firstSection = (TSSection) tkgsSections.get(0);
        int lastSectionNumber = firstSection.getLast_section_number();

        if (tkgsSections.size() != lastSectionNumber + 1) {
            System.out.println("TKGS section not enough");
            return;
        }

        Collections.sort(tkgsSections, //
                new Comparator<Object>() {
                    public int compare(Object o1, Object o2) {
                        Object root1 = ((TSSection) o1).getRoot();
                        Object root2 = ((TSSection) o2).getRoot();
                        int number1 = (Integer) TSUtil.getObjectByName(root1, "section_number");
                        int number2 = (Integer) TSUtil.getObjectByName(root2, "section_number");
                        return (number1 < number2) ? -1 : 1;
                    }
                });

        System.out.println("Total TKGS section number :" + tkgsSections.size());
        int destPos = 0;
        for (TSSection section : tkgsSections) {
            Object root = section.getRoot();
            byte[] bytes = (byte[]) TSUtil.getObjectByName(root, "tkgs_data_byte");
            System.arraycopy(bytes, 0, byteBuffer, destPos, bytes.length);
            destPos += bytes.length;
        }

        Class<?> clazz = null;
        try {
            String clazzName = "S91_TKGS.section.tkgs_data_section";
            clazz = SyntaxBuildFactory.getClazz(clazzName);
            if (clazz == null) {
                return;
            }
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        CommonParser commonParser = null;
        Stack<List<NodeValue>> valueStack = new Stack<List<NodeValue>>();
        List<NodeValue> node = new ArrayList<NodeValue>();
        valueStack.push(node);
        try {
            commonParser = (CommonParser) SyntaxBuildFactory.getInstanceByClass(clazz);// using pool
            commonParser.setValueStack(valueStack);
            commonParser.reset();
            commonParser.parse(byteBuffer, destPos);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TreeItem treeRoot = new TreeItem(tkgsTree, 0);
        treeRoot.setText("TKGS");
        treeRoot.setImage(UIUtil.binImage);
        UIUtil.renderNode(node, treeRoot, true);

        StringBuffer sb = new StringBuffer();
        int step = 0;
        TSUtil.dumpNode(sb, (List<NodeValue>) valueStack.firstElement(), step, 80);

        Browser browser = null;
        if (browser == null) {
            browser = new Browser(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.NONE);
            browser.setVisible(false);
        }

        ServiceManager manager = RWT.getServiceManager();
        StringBuilder url = new StringBuilder();
        HttpServletRequest req = RWT.getRequest();
        req.getSession().setAttribute("buffer", sb.toString());

        url.append(req.getRequestURL());
        url.append(manager.getServiceHandlerUrl("stringDownloadServiceHandler").replaceAll("/TSP", ""));
        url.append("&filename=buffer.txt");
        browser.setUrl(url.toString());
    }

    public String getAppName() {
        return "Tkgs";
    }
}
