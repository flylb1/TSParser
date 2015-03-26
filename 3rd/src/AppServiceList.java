import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import ui.IApplication;
import ui.UIUtil;
import core.BitStream;
import core.CommonParser;
import core.Service;
import core.TSSection;
import core.TSUtil;
import core.TableMeta;
import core.TableParser;

public class AppServiceList implements IApplication {
	private Composite parentContainer;
	private Composite channelListComp;
	private Composite serviceComp;
	private CTabFolder tabFolder;
	private Tree serviceTree;
	private Tree pmtTree;

	private SelectionListener serviceLinklis = new SelectionListener() {
		private static final long serialVersionUID = 1L;

		public void widgetSelected(SelectionEvent e) {
			Widget w = e.widget;
			if (w.getClass() == Link.class) {
				Link link = (Link) w;
				TSSection section = (TSSection) link.getData("section");
				Integer serviceId = (Integer) link.getData("service_id");
				String rootText = (String) link.getData("rootText");
				if (section == null || serviceId == null)
					return;

				renderService(section, rootText, serviceId);
				renderPmt(section, rootText, serviceId);
			}
		}

		public void widgetDefaultSelected(SelectionEvent e) {

		}
	};

	private void renderPmt(TSSection tsSection, String rootText, Integer serviceId) {
		pmtTree.removeAll();
		pmtTree.clearAll(true);

		TreeItem treeRoot = new TreeItem(pmtTree, 0);
		treeRoot.setText(rootText);

		Object root = null;

		List<TSSection> pmtSections = null;
		pmtSections = TSUtil.getPmtListByExtension(tsSection.getBitStream(), serviceId.intValue());
		if (pmtSections == null) {
			return;
		}

		TSSection pmtSection = null;
		int pmtSectionSize = pmtSections.size();
		for (int i = 0; i < pmtSectionSize; i++) {
			pmtSection = (TSSection) pmtSections.get(i);
			root = pmtSection.getRoot();
			Object streamNode = TSUtil.getObjectByName(root, "streams");
			UIUtil.renderNode(streamNode, treeRoot, true);
		}
		treeRoot.setImage(UIUtil.sectionRootImage);
	}

	private void renderService(TSSection tsSection, String rootText, Integer serviceId) {
		serviceTree.removeAll();
		serviceTree.clearAll(true);

		TreeItem treeRoot = new TreeItem(serviceTree, 0);
		treeRoot.setText(rootText);

		CommonParser commonParser = null;
		Object root = null;
		commonParser = tsSection.getCommonParser();
		if (commonParser == null) {
			return;
		}
		root = tsSection.getRoot();
		if (root == null) {
			return;
		}
		int serviceNumber = TSUtil.getObjectLenByName(root, "services");
		for (int i = 0; i < serviceNumber; i++) {
			Object service = TSUtil.getObjectByNameIdx(root, "services", i);
			Object service_id = TSUtil.getObjectByName(service, "service_id");
			if (((Integer) service_id).intValue() != serviceId.intValue()) {
				continue;
			}
			UIUtil.renderNode(service, treeRoot, true);
			break;
		}
		treeRoot.setImage(UIUtil.sectionRootImage);
		// treeRoot.setExpanded(true);
	}

	public void render(TabItem applicationTabItem, BitStream bitStream) {
		parentContainer = applicationTabItem.getParent();
		Composite thisComposite = UIUtil.createFormComposite(parentContainer, 0, 0, 100, 100);
		applicationTabItem.setControl(thisComposite);
		thisComposite.setLayout(new FillLayout());

		SashForm sashFormH = new SashForm(thisComposite, SWT.HORIZONTAL | SWT.FILL);
		channelListComp = new Composite(sashFormH, SWT.NONE);
		serviceComp = new Composite(sashFormH, SWT.NONE);

		channelListComp.setLayout(new FillLayout());
		ExpandBar sdtExpandBar = new ExpandBar(channelListComp, SWT.VERTICAL);
		sdtExpandBar.setSpacing(1);

		TableParser tableParser = bitStream.getParser();

		GridLayout gridLayout = new GridLayout();
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		int[] sdtTableIds = new int[] { 0x42, 0x46 };// SDT actual and other
		TSSection section = null;
		int sdtTableIdsLength = sdtTableIds.length;
		for (int i = 0; i < sdtTableIdsLength; i++) {
			List<TSSection> sections = bitStream.getSectionManager().getSectionsBytableId(new Integer(sdtTableIds[i]));
			if (sections == null) {
				continue;
			}
			Collections.sort(sections);
			int sectionsSize = sections.size();
			for (int j = 0; j < sectionsSize; j++) {
				section = (TSSection) sections.get(j);
				ExpandItem item = new ExpandItem(sdtExpandBar, SWT.WRAP | SWT.MULTI);
				Composite composite = new Composite(sdtExpandBar, SWT.FILL);
				composite.setLayout(gridLayout);
				List<Service> serviceNames = TSUtil.getServiceList(section);
				if (serviceNames != null) {
					Service service = null;
					int serviceNamesSize = serviceNames.size();
					for (int k = 0; k < serviceNamesSize; k++) {
						service = (Service) serviceNames.get(k);
						Link text = new Link(composite, SWT.FILL);
						text.setText("<a>" + service.getSvcId() + "\t" + service.getServiceName() + "</a>");
						text.setLayoutData(gridData);
						text.setData("section", section);
						text.setData("service_id", new Integer(service.getSvcId()));
						text.setData("rootText", service.toString());
						text.addSelectionListener(serviceLinklis);
					}
				}

				TableMeta meta = tableParser.getTableConfigByTableId(sdtTableIds[i]);
				if (meta != null) {
					item.setText(meta.getShortName() + " " + section.shortName());
				}
				item.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				item.setControl(composite);
				item.setImage(UIUtil.sectionImage);
			}
		}

		createServiceComp(serviceComp);
		sashFormH.setWeights(new int[] { 1, 2 });
		sashFormH.layout();
	}

	private void createServiceComp(Composite serviceComp) {
		serviceComp.setLayout(new FillLayout());
		tabFolder = new CTabFolder(serviceComp, SWT.TOP);
		tabFolder.setBorderVisible(false);

		// Service Tree view table
		CTabItem sectionTreeView = new CTabItem(tabFolder, SWT.FLAT);
		sectionTreeView.setText("Service");
		Composite serviceTreeViewComposite = new Composite(tabFolder, SWT.FILL);
		sectionTreeView.setControl(serviceTreeViewComposite);

		serviceTreeViewComposite.setLayout(new FillLayout());
		serviceTree = new Tree(serviceTreeViewComposite, SWT.NONE);

		// PMT Tree view table
		CTabItem pmtTreeView = new CTabItem(tabFolder, SWT.FLAT);
		pmtTreeView.setText("PMT");
		Composite pmtTreeViewComposite = new Composite(tabFolder, SWT.FILL);
		pmtTreeView.setControl(pmtTreeViewComposite);
		pmtTreeViewComposite.setLayout(new FillLayout());
		pmtTree = new Tree(pmtTreeViewComposite, SWT.NONE);

		tabFolder.setSelection(0);
	}

	public String getAppName() {
		return "ServiceList";
	}
}
