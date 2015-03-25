import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ui.IApplication;
import ui.UIUtil;
import core.BitStream;
import core.Event;
import core.Service;
import core.TSSection;
import core.TSUtil;

public class AppEPG implements IApplication {
	private static Logger log = Logger.getLogger(AppEPG.class);
	private static final int CHANNEL_NUMBER_DISP = 10;
	private static int HOURS_TIME_BAR = 2;
	private Composite parentContainer;
	private Color bg = new Color(null, 10, 10, 10);
	private Color fg = new Color(null, 255, 255, 255);
	private Color channelBg = new Color(null, 192, 192, 0);
	private Color eventBg = new Color(null, 1, 194, 191);
	private Color pfbgColor = new Color(null, 255, 192, 255);
	private BitStream bitStream;

	private List<Service> services = new ArrayList<Service>();
	private Composite[] serviceComp = new Composite[CHANNEL_NUMBER_DISP];
	private Composite[] eventComp = new Composite[CHANNEL_NUMBER_DISP];
	private int currentChannelIndex = 0;

	private Calendar brdcstCalendar;
	private Text brdcstText;// show brdcst time

	private Calendar dateCalendar;
	private Text dateText;// show current time

	private Calendar activeStartCal;
	private Calendar activeEndCal;

	private Composite[] timeBarComp = new Composite[HOURS_TIME_BAR];

	private Text eventDetail;

	public String getAppName() {
		return "EPG";
	}

	private SelectionListener eventSelect = new SelectionListener() {
		private static final long serialVersionUID = 1L;

		public void widgetSelected(SelectionEvent e) {
			Button button = (Button) e.widget;
			if (button == null) {
				return;
			}
			Event event = (Event) button.getData("event");
			if (event == null) {
				return;
			}
			eventDetail.setText(TSUtil.getEventDetail(event));
		}

		public void widgetDefaultSelected(SelectionEvent e) {

		}
	};

	private MouseListener eventMouseListener = new MouseListener() {
		private static final long serialVersionUID = 1L;

		public void mouseDoubleClick(MouseEvent e) {
			Button button = (Button) e.widget;
			if (button == null) {
				return;
			}
			Event event = (Event) button.getData("event");
			if (event == null) {
				return;
			}
			Service service = (Service) button.getData("service");
			if (service == null) {
				return;
			}

			Shell parentShell = parentContainer.getShell();
			Shell shell;
			shell = new Shell(parentShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
			shell.open();

			Rectangle p = parentShell.getBounds();
			Rectangle c = shell.getBounds();

			Rectangle rec = new Rectangle(0, 0, c.width, c.height);
			rec.x = p.x + (p.width - c.width) / 2;
			rec.y = p.y + (p.height - c.height) / 2;
			shell.setBounds(rec);

			shell.setLayout(new FillLayout());
			Tree tree = new Tree(shell, SWT.NONE);
			tree.removeAll();
			tree.clearAll(true);

			TreeItem treeRoot = new TreeItem(tree, 0);
			treeRoot.setText("Event Id="
					+ event.getEventId() //
					+ " {" + service.getServiceName() + " OnId=" + service.getOnId() + " [0x"
					+ Integer.toHexString((Integer) service.getOnId())
					+ "]"//
					+ " tsId=" + service.getTsId() + " [0x" + Integer.toHexString((Integer) service.getTsId()).toUpperCase()
					+ "]"//
					+ " svcId=" + service.getSvcId() + " [0x" + Integer.toHexString((Integer) service.getSvcId()).toUpperCase()
					+ "]"//
					+ " eventId=" + event.getEventId() + " [0x" + Integer.toHexString((Integer) event.getEventId()).toUpperCase() + "]"
					+ "}"//
			);
			UIUtil.renderNode(event.getValue(), treeRoot, true);
			treeRoot.setImage(UIUtil.sectionRootImage);
			shell.layout();
		}

		public void mouseDown(MouseEvent e) {

		}

		public void mouseUp(MouseEvent e) {

		}
	};

	public void render(TabItem applicationTabItem, BitStream bitStream) {
		this.bitStream = bitStream;
		parentContainer = applicationTabItem.getParent();

		Composite thisComposite = UIUtil.createFormComposite(parentContainer, 0, 0, 100, 100);
		applicationTabItem.setControl(thisComposite);
		thisComposite.setBackground(bg);
		thisComposite.setLayout(new FormLayout());

		Composite top = UIUtil.createFormComposite(thisComposite, 0, 0, 100, 5);
		top.setBackground(bg);
		createTop(top);

		Composite timeComp = UIUtil.createFormComposite(thisComposite, 5, 0, 100, 10);
		timeComp.setBackground(bg);
		timeComp.setLayout(new FormLayout());
		createTimeBar(timeComp);

		Composite body = UIUtil.createFormComposite(thisComposite, 10, 0, 100, 90);
		body.setBackground(bg);
		body.setLayout(new FormLayout());

		Composite channelEventComp = UIUtil.createFormComposite(body, 0, 0, 100, 70);
		channelEventComp.setBackground(bg);
		channelEventComp.setLayout(new FormLayout());

		Composite channelComp = UIUtil.createFormComposite(channelEventComp, 0, 0, 20, 100);
		channelComp.setBackground(bg);
		channelComp.setLayout(new FormLayout());
		createServices(channelComp);

		Composite eventComp = UIUtil.createFormComposite(channelEventComp, 0, 20, 100, 100);
		eventComp.setLayout(new FormLayout());
		eventComp.setBackground(bg);
		createEvent(eventComp);

		Composite detailComp = UIUtil.createFormComposite(body, 70, 0, 100, 100, SWT.BORDER);
		detailComp.setBackground(bg);
		createEventDetail(detailComp);

		Composite bottom = UIUtil.createFormComposite(thisComposite, 90, 0, 100, 100);
		bottom.setBackground(bg);
		bottom.setLayout(new FormLayout());
		createBottom(bottom);
		buildServiceList();

		// Render UI
		renderBrdcstTime();
		refreshUI();
	}

	private void createEventDetail(Composite detailComp) {
		detailComp.setLayout(new FillLayout());
		eventDetail = new Text(detailComp, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		eventDetail.setEditable(false);
		eventDetail.setText("");
		eventDetail.setBackground(bg);
		eventDetail.setForeground(fg);
	}

	private void createTop(Composite top) {
		top.setLayout(new FillLayout());
		brdcstText = new Text(top, SWT.CENTER);
		brdcstText.setBackground(bg);
		brdcstText.setForeground(fg);
	}

	private void createBottom(Composite bottom) {
		Composite channelUpDown = UIUtil.createFormComposite(bottom, 0, 0, 10, 100, SWT.NONE);
		channelUpDown.setBackground(bg);
		channelUpDown.setLayout(new GridLayout());
		Button up = new Button(channelUpDown, SWT.NONE);
		up.setImage(UIUtil.up);
		up.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			public void widgetSelected(SelectionEvent e) {
				if (currentChannelIndex == 0) {
					return;
				}

				currentChannelIndex--;
				refreshUI();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		up.setToolTipText("Channel +");

		Button down = new Button(channelUpDown, SWT.NONE);
		down.setImage(UIUtil.down);
		down.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			public void widgetSelected(SelectionEvent e) {
				if (currentChannelIndex == services.size() - 1) {
					return;
				}

				currentChannelIndex++;
				refreshUI();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		down.setToolTipText("Channel -");

		Composite epgleftRight = UIUtil.createFormComposite(bottom, 0, 10, 20, 100, SWT.NONE);
		epgleftRight.setBackground(bg);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.marginWidth = 3;
		gridLayout.marginHeight = 3;
		epgleftRight.setLayout(gridLayout);

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.CENTER;
		gridData.verticalSpan = 1;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;

		Button epgLeftDay = new Button(epgleftRight, SWT.CENTER);
		epgLeftDay.setImage(UIUtil.leftDay);
		epgLeftDay.setLayoutData(gridData);

		epgLeftDay.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			public void widgetSelected(SelectionEvent e) {
				if (dateCalendar == null) {
					return;
				}
				dateCalendar.add(Calendar.DATE, -1);
				refreshUI();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		epgLeftDay.setToolTipText("Prev day");

		Button epgLeft = new Button(epgleftRight, SWT.CENTER);
		epgLeft.setImage(UIUtil.left);
		epgLeft.setLayoutData(gridData);

		epgLeft.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			public void widgetSelected(SelectionEvent e) {
				if (dateCalendar == null) {
					return;
				}
				dateCalendar.add(Calendar.HOUR, -1);
				refreshUI();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		epgLeft.setToolTipText("Prev hour");

		Button epgRight = new Button(epgleftRight, SWT.NONE);
		epgRight.setImage(UIUtil.right);
		epgRight.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			public void widgetSelected(SelectionEvent e) {
				if (dateCalendar == null) {
					return;
				}
				dateCalendar.add(Calendar.HOUR, 1);
				refreshUI();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		epgRight.setLayoutData(gridData);
		epgRight.setToolTipText("Next hour");

		Button epgRightDay = new Button(epgleftRight, SWT.NONE);
		epgRightDay.setImage(UIUtil.rightDay);
		epgRightDay.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			public void widgetSelected(SelectionEvent e) {
				if (dateCalendar == null) {
					return;
				}
				dateCalendar.add(Calendar.DATE, 1);
				refreshUI();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		epgRightDay.setLayoutData(gridData);
		epgRightDay.setToolTipText("Next Day");

	}

	private void buildServiceList() {
		int[] sdtTableIds = new int[] { 0x42, 0x46 };// SDT actual and other
		int sdtTableIdslength = sdtTableIds.length;
		for (int i = 0; i < sdtTableIdslength; i++) {
			List<TSSection> sections = bitStream.getSectionManager().getSectionsBytableId(new Integer(sdtTableIds[i]));
			if (sections == null) {
				continue;
			}
			Collections.sort(sections);
			TSSection section = null;
			int sectionsSize = sections.size();
			for (int j = 0; j < sectionsSize; j++) {
				section = (TSSection) sections.get(j);
				List<Service> _services = TSUtil.getServiceList(section);
				services.addAll(_services);
			}
		}
	}

	private void createTimeBar(Composite timeComp) {
		Composite brdcstTimeComp = UIUtil.createFormComposite(timeComp, 0, 0, 20, 100, SWT.BORDER);
		brdcstTimeComp.setLayout(new FillLayout());
		brdcstTimeComp.setBackground(bg);
		dateText = new Text(brdcstTimeComp, SWT.NONE);
		dateText.setBackground(bg);
		dateText.setForeground(fg);

		Composite timeBar = UIUtil.createFormComposite(timeComp, 0, 20, 100, 100, SWT.NONE);
		timeBar.setBackground(bg);
		timeBar.setForeground(fg);
		timeBar.setLayout(new FormLayout());

		// Create TimeBar
		{
			int hourWidth = 100 / HOURS_TIME_BAR;
			for (int i = 0; i < HOURS_TIME_BAR; i++) {
				timeBarComp[i] = UIUtil.createFormComposite(timeBar, 0, i * hourWidth, (i + 1) * hourWidth, 100, SWT.BORDER);
				timeBarComp[i].setBackground(bg);
				timeBarComp[i].setLayout(new FillLayout());

				Text hourText = null;
				hourText = new Text(timeBarComp[i], SWT.LEFT | SWT.TRAIL);
				hourText.setBackground(bg);
				hourText.setForeground(fg);
				timeBarComp[i].setData("Text", hourText);
			}

		}

	}

	private void createServices(Composite channelComp) {
		channelComp.setBackground(bg);
		int step = 100 / CHANNEL_NUMBER_DISP;
		for (int i = 0; i < CHANNEL_NUMBER_DISP; i++) {
			serviceComp[i] = UIUtil.createFormComposite(channelComp, (step * i), 0, 100, (step * (i + 1)), SWT.NONE);
			serviceComp[i].setLayout(new FillLayout());
			Button button = null;
			button = new Button(serviceComp[i], SWT.LEFT | SWT.TRAIL);
			button.setBackground(channelBg);
			button.setForeground(fg);
			serviceComp[i].setData("Button", button);
			serviceComp[i].setBackground(bg);
		}
	}

	private void createEvent(Composite eventCompParent) {
		int step = 100 / CHANNEL_NUMBER_DISP;
		for (int i = 0; i < CHANNEL_NUMBER_DISP; i++) {
			eventComp[i] = UIUtil.createFormComposite(eventCompParent, (step * i), 0, 100, (step * (i + 1)), SWT.NONE);
			eventComp[i].setBackground(bg/* new Color(null, 20 + i, 20, 20) */);
			eventComp[i].setLayout(new FormLayout());
		}
	}

	private void renderServices() {
		// Load service list
		Service service = null;
		for (int j = 0; j < CHANNEL_NUMBER_DISP; j++) {
			Button button = (Button) serviceComp[j].getData("Button");
			if (button == null) {
				continue;
			}
			if (currentChannelIndex + j < services.size()) {
				service = (Service) services.get(currentChannelIndex + j);
				button.setText(service.getSvcId() + "\t" + service.getServiceName());
				serviceComp[j].setVisible(true);
				serviceComp[j].setData("Service", service);

			} else {
				button.setText("");
				serviceComp[j].setVisible(false);
				serviceComp[j].setData("Service", null);
			}
		}
	}

	private void renderEvents() {
		Service service = null;
		Map<Service, List<Event>> serviceEvents = this.bitStream.getSectionManager().getServiceEvents();
		for (int i = 0; i < CHANNEL_NUMBER_DISP; i++) {
			eventComp[i].setVisible(false);
			if (serviceComp[i].getVisible() == false)
				continue;

			service = (Service) serviceComp[i].getData("Service");
			if (service == null)
				continue;

			boolean fakeEvent = false;
			List<Event> events = (List<Event>) serviceEvents.get(service);
			if (fakeEvent == false) {
				if (events == null) {
					continue;
				}
			} else {
				if (events == null || events.size() == 0) {
					events = new ArrayList<Event>();
					Event event = new Event();
					if (activeStartCal != null) {
						Calendar start = (Calendar) activeStartCal.clone();
						start.add(Calendar.SECOND, 1);
						event.setStartTime(start);
					}

					if (activeEndCal != null) {
						Calendar end = (Calendar) activeEndCal.clone();
						end.add(Calendar.SECOND, -1);
						event.setEndTime(end);
					}
					event.setSection(null);
					event.setPf(false);
					events.add(event);
				}
			}

			eventComp[i].setVisible(true);
			// First dispose it
			Control[] controls = eventComp[i].getChildren();
			if (controls != null) {
				int controlsLength = controls.length;
				for (int j = 0; j < controlsLength; j++) {
					controls[j].dispose();
				}
			}
			eventComp[i].setLayout(new FormLayout());
			List<Event> visibleEvents = new ArrayList<Event>();
			boolean flag = false;
			Event event = null;
			int eventsSize = events.size();
			for (int j = 0; j < eventsSize; j++) {
				event = (Event) events.get(j);
				flag = false;
				if (event.getStartTime() == null || event.getEndTime() == null) {
					break;
				}
				if (event.getStartTime().after(this.activeStartCal) && event.getStartTime().before(this.activeEndCal)) {
					flag = true;
				}
				if (event.getEndTime().after(this.activeStartCal) && event.getEndTime().before(this.activeEndCal)) {
					flag = true;
				}
				if (flag) {
					visibleEvents.add(event);
				}

				if (event.getStartTime().after(this.activeEndCal)) {
					break;
				}
			}

			// Render Events
			// int top, int left, int right, int bottom
			long left = 0;
			long right = 0;
			int visibleEventsSize = visibleEvents.size();
			for (int j = 0; j < visibleEventsSize; j++) {
				event = (Event) visibleEvents.get(j);
				if (event.getStartTime().before(this.activeStartCal)) {
					left = 0;
				} else {
					left = (long) (event.getStartTime().getTimeInMillis() / 1000) - (long) (this.activeStartCal.getTimeInMillis() / 1000);// second
					left = (left * 100) / (HOURS_TIME_BAR * 60 * 60);
				}

				if (event.getEndTime().after(this.activeEndCal)) {
					right = 100;
				} else {
					right = (long) (event.getEndTime().getTimeInMillis() / 1000) - (long) (this.activeStartCal.getTimeInMillis() / 1000);// second
					right = (right * 100) / (HOURS_TIME_BAR * 60 * 60);
				}
				Composite singleEventComp = UIUtil.createFormComposite(eventComp[i], 0, (int) left, (int) right, 100, SWT.NONE);
				singleEventComp.setBackground(bg);
				singleEventComp.setLayout(new FillLayout());

				Button button = null;
				button = new Button(singleEventComp, SWT.LEFT | SWT.TRAIL | SWT.WRAP);
				button.setBackground(eventBg);
				button.setForeground(fg);
				String eventTitle = TSUtil.getEventTitle(event);
				button.setText(eventTitle);
				button.setToolTipText(eventTitle);
				button.setData("event", event);
				button.setData("service", service);
				if (event.isPf()) {
					button.setBackground(pfbgColor);
				}
				button.addSelectionListener(eventSelect);
				button.addMouseListener(eventMouseListener);
			}
			eventComp[i].layout();
		}
	}

	private void renderBrdcstTime() {
		this.brdcstCalendar = TSUtil.getBrdcstTimeByTOTTDT(bitStream);
		if (this.brdcstCalendar == null) {
			this.brdcstCalendar = Calendar.getInstance();
			return;
		}
		this.dateCalendar = (Calendar) this.brdcstCalendar.clone();
		if (this.brdcstCalendar != null) {
			brdcstText.setText(TSUtil.calendarToString(this.brdcstCalendar, "yyyy/MM/dd hh:mm:ss"));
		}
	}

	private void renderTimeBar() {
		if (this.dateCalendar == null) {
			return;
		}
		Calendar startCalendar = (Calendar) this.dateCalendar.clone();
		startCalendar.set(Calendar.MINUTE, 0);
		startCalendar.set(Calendar.SECOND, 0);

		activeStartCal = (Calendar) startCalendar.clone();
		activeEndCal = (Calendar) activeStartCal.clone();
		activeEndCal.add(Calendar.HOUR_OF_DAY, HOURS_TIME_BAR);
		activeEndCal.add(Calendar.SECOND, -1);// decrease 1 second for bandary events
		log.debug("Active start:" + TSUtil.calendarToString(activeStartCal, "yyyy/MM/dd hh:mm"));
		log.debug("Active End  :" + TSUtil.calendarToString(activeEndCal, "yyyy/MM/dd hh:mm"));

		this.dateText.setText(TSUtil.calendarToString(startCalendar, "yyyy/MM/dd"));

		for (int i = 0; i < HOURS_TIME_BAR; i++) {
			Calendar cal = (Calendar) startCalendar.clone();
			cal.add(Calendar.HOUR_OF_DAY, i);
			Text text = (Text) timeBarComp[i].getData("Text");
			text.setText(TSUtil.calendarToString(cal, "yyyy/MM/dd hh:mm"));
		}
	}

	private void refreshUI() {

		renderTimeBar();
		renderServices();
		renderEvents();

	}
}
