import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import ui.IApplication;
import ui.UIUtil;
import core.BitStream;
import core.Service;
import core.TSSection;
import core.TSUtil;
import core.TableMeta;
import core.TableParser;

public class AppDemo implements IApplication {
	private SashForm sashFormH;
	private Composite resultComp;
	private BitStream bitStream;

	private static String tsFile = "TS Name";
	private static String tsBreifInfo = "TS breifInfo";
	private static String tsFilterInfo = "TS Filter Info";
	private static String tsServiceInfo = "TS Service List";
	private static List<String> functions = new ArrayList<String>();

	static {
		functions.add(tsFile);
		functions.add(tsBreifInfo);
		functions.add(tsFilterInfo);
		functions.add(tsServiceInfo);
	}

	SelectionListener selectionListener = new SelectionListener() {
		private static final long serialVersionUID = 1L;

		public void widgetSelected(SelectionEvent e) {
			Button button = (Button) e.widget;
			if (button == null) {
				return;
			}

			clean();
			if (button.getText().equals(tsFile)) {
				Text text = new Text(resultComp, SWT.MULTI | SWT.VERTICAL);
				text.setText(bitStream.getFile().getAbsolutePath().toString());
			} else if (button.getText().equals(tsBreifInfo)) {
				Text text = new Text(resultComp, SWT.MULTI | SWT.VERTICAL);
				text.setText(bitStream.getSectionManager().sectionBreifInfo());
			} else if (button.getText().equals(tsFilterInfo)) {
				Text text = new Text(resultComp, SWT.MULTI | SWT.VERTICAL);
				text.setText(bitStream.getFilter().toString());
			} else {
				ExpandBar sdtExpandBar = new ExpandBar(resultComp, SWT.VERTICAL);
				sdtExpandBar.setSpacing(1);

				TableParser tableParser = bitStream.getParser();
				GridLayout gridLayout = new GridLayout();
				int[] sdtTableIds = new int[] { 0x42, 0x46 };// SDT actual and other
				TSSection section = null;
				for (int i = 0; i < sdtTableIds.length; i++) {
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
								text.setText(service.getSvcId() + "\t" + service.getServiceName());
							}
						}

						TableMeta meta = tableParser.getTableConfigByTableId(sdtTableIds[i]);
						if (meta != null) {
							item.setText(meta.getShortName() + " " + section.shortName());
						}
						item.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
						item.setControl(composite);
						item.setImage(UIUtil.sectionImage);
						item.setExpanded(true);
					}
				}
			}
			resultComp.layout();
		}

		public void widgetDefaultSelected(SelectionEvent e) {

		}
	};

	public void render(TabItem applicationTabItem, BitStream bitStream) {
		this.bitStream = bitStream;
		Composite parentContainer = applicationTabItem.getParent();
		Composite parentComp = new Composite(parentContainer, SWT.FILL);
		applicationTabItem.setControl(parentComp);
		parentComp.setLayout(new FillLayout());
		parentComp.setBackground(new Color(null, 255, 0, 0));
		parentComp.layout();
		sashFormH = new SashForm(parentComp, SWT.HORIZONTAL | SWT.FILL);
		Composite functionsComp = new Composite(sashFormH, SWT.FILL | SWT.BORDER);
		resultComp = new Composite(sashFormH, SWT.FILL | SWT.BORDER);
		resultComp.setLayout(new FillLayout());

		functionsComp.setLayout(new RowLayout());
		int functionsSize = functions.size();
		for (int i = 0; i < functionsSize; i++) {
			String text = (String) functions.get(i);
			Button button = new Button(functionsComp, SWT.NONE);
			button.setText(text);
			button.addSelectionListener(selectionListener);
		}

		sashFormH.setWeights(new int[] { 30, 70 });
		sashFormH.layout();
		parentContainer.layout();
	}

	private void clean() {
		Control[] controls = resultComp.getChildren();
		if (controls != null) {
			for (int i = 0; i < controls.length; i++) {
				controls[i].dispose();
			}
		}
		resultComp.setLayout(new FillLayout());
	}

	public String getAppName() {
		return "Demo";
	}
}
