package org.processmining.analysis.sequenceclustering;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SmoothPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.analysis.clustering.model.LogSequence;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.slicker.logdialog.InspectorUI;
import org.processmining.framework.ui.slicker.logdialog.SlickerOpenLogSettings;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.plugin.Provider;

/**
 * @author Gabriel Veiga, IST - Technical University of Lisbon
 * @author Supervisor: Prof. Diogo Ferreira
 */
public class PreProcessingUI extends JPanel implements Provider {

	protected Color colorEnclosureBg = new Color(40, 40, 40);
	protected Color colorNonFocus = new Color(70, 70, 70);
	protected Color colorInstanceList = new Color(60, 60, 60);
	protected Color colorListBg = new Color(60, 60, 60);
	protected Color colorListBgLower = new Color(45, 45, 45);
	protected Color colorListFg = new Color(180, 180, 180);
	protected Color colorListSelectionBg = new Color(80, 0, 0);
	protected Color colorListSelectionBgLower = new Color(30, 10, 10);
	protected Color colorListSelectionFg = new Color(240, 240, 240);
	protected Color labelColor = new Color(30, 30, 30);
	protected Color backGroundColor = new Color(20, 20, 20);
	protected Color bgColor = new Color(160, 160, 160);
	protected Color fgColor = new Color(50, 50, 50);
	protected static Color panelBackgroundColor = new Color(140, 140, 140, 140);

	protected JList instancesList, eventsList;
	protected JLabel instanceNameLabel, instanceSizeLabel, eventLabel,
			instancesListLabel;

	protected InspectorUI inspectorUI;
	protected SlickerOpenLogSettings parent;
	protected LogReader log = null;
	protected LogReader currentlog = null;
	protected LogReader originalLog = null;
	protected LogSummary logSummary;
	protected LogSequence logS = null;

	protected GUIPropertyDoubleSC minimumEventSupportBox = new GUIPropertyDoubleSC(
			"Min event occurrence (percentage) = ", 0.0, 0.0, 100.0, 0.001);
	protected GUIPropertyDoubleSC maximumEventSupportBox = new GUIPropertyDoubleSC(
			"Max event occurrence (percentage) = ", 100.000, 0.000, 100.000,
			0.001);
	protected GUIPropertyInteger minimumSequenceSizeBox = new GUIPropertyInteger(
			"Min number of events in a sequence = ", 1, 1, 100);
	protected GUIPropertyInteger maximumSequenceSizeBox;
	protected GUIPropertyInteger minimumSequenceSupportBox;
	protected GUIPropertyInteger maximumSequenceSupportBox;
	protected GUIPropertyInteger clusterBox = new GUIPropertyInteger(
			"Number of clusters = ", 3, 1, 100);
	protected JButton startPreProcessingButton;
	protected JButton resetButton;
	protected JButton clusterButton;

	protected int minSequenceSize, maxSequenceSize, minSequenceSupport,
			maxSequenceSupport;
	protected double minEventSupport, maxEventSupport;
	protected boolean preprocessed = false;

	protected SCLogFilter filter = null;
	protected ArrayList<LogEvent> events = new ArrayList<LogEvent>();
	protected ArrayList<String> instancesToRemove = new ArrayList<String>();
	protected List<ProcessInstance> instanceList;

	protected ArrayList<String> sequences;
	protected ArrayList<Integer> sequenceOccurences;

	public PreProcessingUI(SlickerOpenLogSettings parent) {

		// Determine the size of the largest instance
		int maxInstanceLength = 0;
		int size;
		for (int i = 0; i < parent.getLog().getInstances().size(); i++) {
			size = parent.getLog().getInstances().get(i)
					.getAuditTrailEntryList().size();
			if (maxInstanceLength < size) {
				maxInstanceLength = size;
			}
		}

		// Assign values to the preprocessing option boxes
		maximumSequenceSizeBox = new GUIPropertyInteger(
				"Max number of events in a sequence = ", maxInstanceLength, 1,
				maxInstanceLength);
		minimumSequenceSupportBox = new GUIPropertyInteger(
				"Min sequence occurrence = ", 1, 1, parent.getLog()
						.getInstances().size());
		maximumSequenceSupportBox = new GUIPropertyInteger(
				"Max sequence occurrence = ", parent.getLog().getInstances()
						.size(), 1, parent.getLog().getInstances().size());

		this.parent = parent;
		this.setOpaque(false);
		this.setBackground(backGroundColor);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {
				updateView();
			}

			public void ancestorMoved(AncestorEvent arg0) { /* ignore */
			}

			public void ancestorRemoved(AncestorEvent arg0) { /* ignore */
			}
		});
		// create instances list
		instancesList = new JList();
		instancesList.setBackground(colorInstanceList);
		instancesList.setForeground(colorListFg);
		instancesList.setSelectionBackground(colorListSelectionBg);
		instancesList.setSelectionForeground(colorListSelectionFg);
		instancesList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		instancesList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				instancesSelectionChanged();
			}
		});
		JScrollPane instancesScrollPane = new JScrollPane(instancesList);
		instancesScrollPane.setOpaque(false);
		instancesScrollPane.setBorder(BorderFactory.createEmptyBorder());
		instancesScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		instancesScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollBar vBar = instancesScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), colorNonFocus, 4, 12));
		vBar.setOpaque(false);

		// assemble instances list
		instancesListLabel = new JLabel(parent.getLog().getInstances().size()
				+ " Instances");
		instancesListLabel.setOpaque(false);
		instancesListLabel.setForeground(colorListSelectionFg);
		instancesListLabel
				.setFont(instancesListLabel.getFont().deriveFont(13f));
		instancesListLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		instancesListLabel.setHorizontalAlignment(JLabel.CENTER);
		instancesListLabel.setHorizontalTextPosition(JLabel.CENTER);
		RoundedPanel instancesPanel = new RoundedPanel(10, 5, 0);
		instancesPanel.setBackground(colorEnclosureBg);
		instancesPanel
				.setLayout(new BoxLayout(instancesPanel, BoxLayout.Y_AXIS));
		instancesPanel.setMaximumSize(new Dimension(180, 1000));
		instancesPanel.setPreferredSize(new Dimension(180, 1000));
		instancesPanel.add(instancesListLabel);
		instancesPanel.add(Box.createVerticalStrut(8));
		instancesPanel.add(instancesScrollPane);
		// create event list header
		instanceNameLabel = new JLabel("(no instance selected)");
		instanceNameLabel.setOpaque(false);
		instanceNameLabel.setForeground(colorNonFocus);
		instanceNameLabel.setFont(instanceNameLabel.getFont().deriveFont(13f));
		instanceNameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		instanceNameLabel.setHorizontalAlignment(JLabel.CENTER);
		instanceNameLabel.setHorizontalTextPosition(JLabel.CENTER);
		instanceSizeLabel = new JLabel("select single instance to browse");
		instanceSizeLabel.setOpaque(false);
		instanceSizeLabel.setForeground(colorNonFocus);
		instanceSizeLabel.setFont(instanceSizeLabel.getFont().deriveFont(11f));
		instanceSizeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		instanceSizeLabel.setHorizontalAlignment(JLabel.CENTER);
		instanceSizeLabel.setHorizontalTextPosition(JLabel.CENTER);
		// create events list
		eventsList = new JList();
		eventsList.setBackground(colorListBg);
		eventsList.setCellRenderer(new EventCellRenderer());
		eventsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		eventsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				eventsSelectionChanged();
			}
		});

		JScrollPane eventsScrollPane = new JScrollPane(eventsList);
		eventsScrollPane.setOpaque(false);
		eventsScrollPane.setBorder(BorderFactory.createEmptyBorder());
		eventsScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		eventsScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		vBar = eventsScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), colorNonFocus, 4, 12));
		vBar.setOpaque(false);
		// assemble events list
		RoundedPanel eventsPanel = new RoundedPanel(10, 5, 0);
		eventsPanel.setBackground(colorEnclosureBg);
		eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
		eventsPanel.add(instanceNameLabel);
		eventsPanel.add(instanceSizeLabel);
		eventsPanel.add(Box.createVerticalStrut(8));
		eventsPanel.add(eventsScrollPane);
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(200, 200, 200), colorNonFocus, 4, 12));
		vBar.setOpaque(false);
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(200, 200, 200), colorNonFocus, 4, 12));
		vBar.setOpaque(false);

		eventLabel = new JLabel("(no event selected)");
		eventLabel.setOpaque(false);
		eventLabel.setForeground(colorNonFocus);
		eventLabel.setFont(eventLabel.getFont().deriveFont(13f));
		eventLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		eventLabel.setHorizontalAlignment(JLabel.CENTER);
		eventLabel.setHorizontalTextPosition(JLabel.CENTER);

		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rightPanel.setOpaque(false);
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setMinimumSize(new Dimension(375, 100));
		rightPanel.setMaximumSize(new Dimension(375, 1000));
		rightPanel.setPreferredSize(new Dimension(375, 500));

		// Preprocessing Panel
		SmoothPanel PreprocessingPanel = new SmoothPanel();
		PreprocessingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
				10, 10));
		PreprocessingPanel.setLayout(new BoxLayout(PreprocessingPanel,
				BoxLayout.Y_AXIS));

		startPreProcessingButton = new JButton("Apply Preprocessing changes");
		startPreProcessingButton.setOpaque(false);
		startPreProcessingButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		startPreProcessingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				filterLog();
			}
		});

		JLabel preprocessingLabel = new JLabel("Preprocessing");
		Font preprocessingLabelFont = preprocessingLabel.getFont().deriveFont(
				16f);
		preprocessingLabel.setFont(preprocessingLabelFont);
		preprocessingLabel.setOpaque(false);
		preprocessingLabel.setForeground(labelColor);
		preprocessingLabel.setAlignmentX(LEFT_ALIGNMENT);

		resetButton = new JButton("Reset changes");
		resetButton.setOpaque(false);
		resetButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetChanges();
			}
		});

		PreprocessingPanel.add(packLeftAligned(preprocessingLabel));
		PreprocessingPanel.add(minimumEventSupportBox.getPropertyPanel());
		PreprocessingPanel.add(maximumEventSupportBox.getPropertyPanel());
		PreprocessingPanel.add(minimumSequenceSizeBox.getPropertyPanel());
		PreprocessingPanel.add(maximumSequenceSizeBox.getPropertyPanel());
		PreprocessingPanel.add(minimumSequenceSupportBox.getPropertyPanel());
		PreprocessingPanel.add(maximumSequenceSupportBox.getPropertyPanel());
		PreprocessingPanel.add(startPreProcessingButton);
		PreprocessingPanel.add(resetButton);

		resetButton.setEnabled(false);

		// Clustering Panel
		SmoothPanel clusteringPanel = new SmoothPanel();
		clusteringPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
				10));
		clusteringPanel.setLayout(new BoxLayout(clusteringPanel,
				BoxLayout.Y_AXIS));

		clusterButton = new JButton("Cluster");
		clusterButton.setOpaque(false);
		clusterButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		clusterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SCUI SC = new SCUI(log, originalLog, minEventSupport,
						maxEventSupport, clusterBox.getValue(), preprocessed);
				SC.setSize(600, 400);
				SC.setVisible(true);
				SC.setBackground(backGroundColor);
				SC.validate();
				SC.repaint();
				MainUI.getInstance().createFrame(
						"Result - Sequence Clustering", SC);
			}
		});

		JLabel clusteringLabel = new JLabel("Sequence Clustering");
		Font clusteringLabelFont = preprocessingLabel.getFont().deriveFont(16f);
		clusteringLabel.setFont(clusteringLabelFont);
		clusteringLabel.setOpaque(false);
		clusteringLabel.setForeground(labelColor);
		clusteringLabel.setAlignmentX(LEFT_ALIGNMENT);

		clusteringPanel.add(packLeftAligned(clusteringLabel));
		clusteringPanel.add(clusterBox.getPropertyPanel());
		clusteringPanel.add(clusterButton);

		rightPanel.add(PreprocessingPanel);
		rightPanel.add(Box.createVerticalStrut(8));
		rightPanel.add(clusteringPanel);
		rightPanel.add(Box.createVerticalStrut(8));

		// assemble GUI
		this.add(instancesPanel);
		this.add(eventsPanel);
		this.add(rightPanel);
	}

	protected JComponent packLeftAligned(JComponent comp) {
		Box enclosure = Box.createHorizontalBox();
		enclosure.setOpaque(false);
		enclosure.add(comp);
		enclosure.add(Box.createHorizontalGlue());
		return enclosure;
	}

	protected void instancesSelectionChanged() {
		int[] selectedIndices = instancesList.getSelectedIndices();
		if (selectedIndices.length == 0 || selectedIndices.length > 1) {
			eventsList.setListData(new Object[] {});
			eventsList.clearSelection();
			instanceNameLabel.setForeground(colorNonFocus);
			instanceNameLabel.setText("(no instance selected)");
			instanceSizeLabel.setForeground(colorNonFocus);
			instanceSizeLabel.setText("select single instance to browse");
		} else {
			ProcessInstance instance = log.getInstance(selectedIndices[0]);
			AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
			instanceNameLabel.setForeground(colorListSelectionFg);
			instanceNameLabel.setText(instance.getName());
			instanceSizeLabel.setForeground(colorListSelectionFg);
			instanceSizeLabel.setText(ateList.size() + " events");
			eventsList.setModel(new AuditTrailEntryListModel(ateList));
			eventsList.ensureIndexIsVisible(0);
		}
		showSelectedInstanceData();
	}

	public void eventsSelectionChanged() {
		AuditTrailEntry ate = (AuditTrailEntry) eventsList.getSelectedValue();

		if (ate != null) {
			eventLabel.setForeground(colorListSelectionFg);
		} else {
			showSelectedInstanceData();
		}
	}

	protected void showSelectedInstanceData() {
		int[] selectedIndices = instancesList.getSelectedIndices();

		if (selectedIndices.length == 1) {
			ProcessInstance instance = log.getInstance(selectedIndices[0]);

			eventLabel.setForeground(colorListSelectionFg);
		} else {
			eventLabel.setForeground(colorNonFocus);
			eventLabel.setText("(no case or event selected)");
		}
	}

	public ActionListener getActivationListener() {
		ActionListener activationListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateView();
			}
		};
		return activationListener;
	}

	protected void updateView() {
		LogReader uLog = parent.getLog();
		if (log == null || uLog.equals(log) == false) {
			originalLog = uLog;
			log = uLog;
			logSummary = log.getLogSummary();
			// repopulate instance names list
			String[] instanceNames = new String[log.numberOfInstances()];
			for (int i = 0; i < instanceNames.length; i++) {
				instanceNames[i] = log.getInstance(i).getName();
			}
			instancesList.setListData(instanceNames);
			// reset events list
			instancesList.clearSelection();
			eventsList.clearSelection();
			revalidate();
			repaint();
		}
	}

	protected void updateView2() {
		try {
			originalLog = log;
			currentlog = LogReaderFactory.createInstance(filter, log);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log = currentlog;
		logSummary = log.getLogSummary();

		// repopulate instance names list
		String[] instanceNames = new String[log.numberOfInstances()];
		for (int i = 0; i < instanceNames.length; i++) {
			instanceNames[i] = log.getInstance(i).getName();
		}
		instancesList.setListData(instanceNames);
		// reset events list
		instancesList.clearSelection();
		eventsList.clearSelection();
		revalidate();
		repaint();
	}

	public LogReader getResultReader() {
		if (log == null || instancesList.getSelectedIndices().length == 0) {
			return null;
		} else {
			try {
				return LogReaderFactory.createInstance(log, instancesList
						.getSelectedIndices());
			} catch (Exception e) {
				// oops...
				e.printStackTrace();
				return null;
			}
		}
	}

	protected class AuditTrailEntryListModel extends AbstractListModel {

		protected AuditTrailEntryList ateList;

		protected AuditTrailEntryListModel(AuditTrailEntryList list) {
			ateList = list;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		public Object getElementAt(int index) {
			try {
				return ateList.get(index);
			} catch (IndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.ListModel#getSize()
		 */
		public int getSize() {
			return ateList.size();
		}
	}

	protected class EventCellRenderer extends GradientPanel implements
			ListCellRenderer {

		protected int height = 60;
		protected DateFormat dateFormat = new SimpleDateFormat(
				"dd.MM.yyyy HH:mm:ss.SSS");

		protected JLabel nameLabel;
		protected JLabel numberLabel;
		protected JLabel originatorLabel;
		protected JLabel typeLabel;
		protected JLabel timestampLabel;

		public EventCellRenderer() {
			super(colorListBg, colorListBgLower);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			setMinimumSize(new Dimension(180, height));
			setMaximumSize(new Dimension(500, height));
			setPreferredSize(new Dimension(250, height));
			nameLabel = new JLabel("name");
			nameLabel.setOpaque(false);
			nameLabel.setForeground(colorListFg);
			nameLabel.setFont(nameLabel.getFont().deriveFont(13f));
			nameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			nameLabel.setHorizontalAlignment(JLabel.CENTER);
			nameLabel.setHorizontalTextPosition(JLabel.CENTER);
			originatorLabel = new JLabel("originator");
			originatorLabel.setOpaque(false);
			originatorLabel.setForeground(colorListFg);
			originatorLabel.setFont(originatorLabel.getFont().deriveFont(9f));
			numberLabel = new JLabel("#");
			numberLabel.setOpaque(false);
			numberLabel.setForeground(colorListFg);
			numberLabel.setFont(numberLabel.getFont().deriveFont(9f));
			typeLabel = new JLabel("type");
			typeLabel.setOpaque(false);
			typeLabel.setForeground(colorListFg);
			typeLabel.setFont(typeLabel.getFont().deriveFont(11f));
			timestampLabel = new JLabel("timestamp");
			timestampLabel.setOpaque(false);
			timestampLabel.setForeground(colorListFg);
			timestampLabel.setFont(timestampLabel.getFont().deriveFont(12f));
			timestampLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			timestampLabel.setHorizontalAlignment(JLabel.CENTER);
			timestampLabel.setHorizontalTextPosition(JLabel.CENTER);
			JPanel middlePanel = new JPanel();
			middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
			middlePanel.setOpaque(false);
			middlePanel.setBorder(BorderFactory.createEmptyBorder());
			middlePanel.add(Box.createHorizontalGlue());
			middlePanel.add(numberLabel);
			middlePanel.add(Box.createHorizontalStrut(10));
			middlePanel.add(typeLabel);
			middlePanel.add(Box.createHorizontalStrut(10));
			middlePanel.add(originatorLabel);
			middlePanel.add(Box.createHorizontalGlue());
			this.add(Box.createVerticalGlue());
			this.add(nameLabel);
			this.add(Box.createVerticalGlue());
			this.add(middlePanel);
			this.add(Box.createVerticalGlue());
			this.add(timestampLabel);
			this.add(Box.createVerticalGlue());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
		 * .JList, java.lang.Object, int, boolean, boolean)
		 */
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			// configure yourself
			if (isSelected) {
				this.setColors(colorListSelectionBg, colorListSelectionBgLower);
			} else {
				this.setColors(colorListBg, colorListBgLower);
			}
			AuditTrailEntry ate = (AuditTrailEntry) value;
			nameLabel.setText(ate.getElement());
			typeLabel.setText(ate.getType());
			numberLabel.setText("#" + (index + 1));
			if (ate.getOriginator() != null) {
				originatorLabel.setText("@" + ate.getOriginator());
			} else {
				originatorLabel.setText("");
			}
			if (ate.getTimestamp() != null) {
				timestampLabel.setText(dateFormat.format(ate.getTimestamp()));
			} else {
				timestampLabel.setText("- no timestamp -");
			}
			return this;
		}

	}

	protected double round(double number, int decimals) {

		double factor = Math.pow(10, decimals);

		return Math.round(number * factor) / factor;

	}

	// Discover the items to be removed by the filter

	protected void buildItemsToRemove() {
		List<LogEvent> eventList = new ArrayList<LogEvent>();
		eventList = logSummary.getLogEvents();
		LogEvent eventToRemove;
		instanceList = new ArrayList<ProcessInstance>();
		instanceList = log.getInstances();
		double eventOccurence;

		int maxInstanceSize = 0;
		int size;

		for (int i = 0; i < instanceList.size(); i++) {
			size = instanceList.get(i).getAuditTrailEntryList().size();
			if (maxInstanceSize < size) {
				maxInstanceSize = size;
			}
		}

		int[][] changedInstances = new int[instanceList.size()][maxInstanceSize];

		for (int i = 0; i < instanceList.size(); i++) {
			for (int j = 0; j < maxInstanceSize; j++) {
				changedInstances[i][j] = 0;
			}
		}
		// Remove events with low occurrence

		for (int i = 0; i < eventList.size(); i++) {
			eventOccurence = round(((((double) (eventList.get(i)
					.getOccurrenceCount())) / (logSummary
					.getNumberOfAuditTrailEntries())) * 100), 3);

			if (eventOccurence < minEventSupport
					|| eventOccurence > maxEventSupport)
				events.add(eventList.get(i));
		}

		for (int i = 0; i < events.size(); i++) {
			eventToRemove = events.get(i);

			for (int j = 0; j < instanceList.size(); j++) {

				for (int k = 0; k < instanceList.get(j)
						.getAuditTrailEntryList().size(); k++) {
					try {
						if (instanceList.get(j).getAuditTrailEntryList().get(k)
								.getElement().equals(
										eventToRemove.getModelElementName())
								&& instanceList.get(j).getAuditTrailEntryList()
										.get(k).getType().equals(
												eventToRemove.getEventType())) {

							changedInstances[j][k]++;

						}

					} catch (IndexOutOfBoundsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		// remove duplicate events (like in the sequence AABCD we remove the
		// second A)

		for (int i = 0; i < instanceList.size(); i++) {

			for (int j = 0; j < instanceList.get(i).getAuditTrailEntryList()
					.size() - 1; j++) {
				try {
					int nextEvent = nextEvent(i, j, instanceList.get(i)
							.getAuditTrailEntryList().size(), changedInstances);
					if (nextEvent != -1) {
						if (instanceList
								.get(i)
								.getAuditTrailEntryList()
								.get(j)
								.getElement()
								.equals(
										instanceList.get(i)
												.getAuditTrailEntryList().get(
														nextEvent).getElement())
								&& instanceList
										.get(i)
										.getAuditTrailEntryList()
										.get(j)
										.getType()
										.equals(
												instanceList
														.get(i)
														.getAuditTrailEntryList()
														.get(nextEvent)
														.getType())) {

							changedInstances[i][nextEvent]++;
						}
					}
				} catch (IndexOutOfBoundsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// remove sequences with low occurrence
		sequenceOccurences = new ArrayList<Integer>();
		sequences = new ArrayList<String>();

		for (int i = 0; i < instanceList.size(); i++) {
			sequenceOccurences.add(i, 1);
			sequences.add(i, "");

		}

		for (int i = 0; i < instanceList.size(); i++) {
			for (int j = 0; j < instanceList.get(i).getAuditTrailEntryList()
					.size(); j++) {
				try {
					if (changedInstances[i][j] == 0)
						sequences.set(i, sequences.get(i).concat(
								instanceList.get(i).getAuditTrailEntryList()
										.get(j).getElement()));
				} catch (IndexOutOfBoundsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		for (int i = 0; i < instanceList.size() - 1; i++) {
			for (int j = i + 1; j < instanceList.size(); j++) {
				if (sequences.get(i).equals(sequences.get(j))) {
					sequenceOccurences.set(i, sequenceOccurences.get(i) + 1);
					sequenceOccurences.set(j, sequenceOccurences.get(j) + 1);
				}
			}
		}

		for (int i = 0, j = 0; i < instanceList.size(); i++, j++) {

			if (sequenceOccurences.get(j) < minSequenceSupport
					|| sequenceOccurences.get(j) > maxSequenceSupport) {
				instancesToRemove.add(instanceList.get(i).getName());
			}
		}
	}

	public int nextEvent(int pi, int index, int size, int[][] changedInstances) {
		for (int i = index + 1; i < size; i++) {
			if (changedInstances[pi][i] == 0) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[2];
		int index = 0;

		// add complete log
		objects[index] = new ProvidedObject("Original log",
				new Object[] { originalLog });
		objects[++index] = new ProvidedObject(
				"Preprocessed for Sequence Clustering", new Object[] { log });
		return objects;
	}

	public JComponent filterLog() {

		this.minSequenceSize = minimumSequenceSizeBox.getValue();
		this.maxSequenceSize = maximumSequenceSizeBox.getValue();
		this.minEventSupport = minimumEventSupportBox.getValue();
		this.maxEventSupport = maximumEventSupportBox.getValue();
		this.minSequenceSupport = minimumSequenceSupportBox.getValue();
		this.maxSequenceSupport = maximumSequenceSupportBox.getValue();

		minimumSequenceSizeBox.disable();
		maximumSequenceSizeBox.disable();
		minimumEventSupportBox.disable();
		maximumEventSupportBox.disable();
		minimumSequenceSupportBox.disable();
		maximumSequenceSupportBox.disable();
		startPreProcessingButton.setEnabled(false);
		resetButton.setEnabled(true);

		buildItemsToRemove();

		filter = new SCLogFilter();
		filter.setFlag(0);
		filter.setFilterEvents(events.toArray(new LogEvent[0]));
		filter.setFilterSequences(instancesToRemove.toArray(new String[0]));
		filter.setMinSequenceSize(minSequenceSize);
		filter.setMaxSequenceSize(maxSequenceSize);
		preprocessed = true;

		updateView2();
		instancesListLabel.setText(log.getInstances().size() + " Instances");
		instancesListLabel.setForeground(new Color(150, 50, 50));
		return this;
	}

	public JComponent resetChanges() {

		minimumSequenceSizeBox.enable();
		maximumSequenceSizeBox.enable();
		minimumEventSupportBox.enable();
		maximumEventSupportBox.enable();
		minimumSequenceSupportBox.enable();
		maximumSequenceSupportBox.enable();
		startPreProcessingButton.setEnabled(true);
		resetButton.setEnabled(false);
		instancesList.setBackground(new Color(60, 60, 60));

		instancesToRemove = new ArrayList<String>();
		events = new ArrayList<LogEvent>();
		preprocessed = false;

		updateView();
		instancesListLabel.setText(log.getInstances().size() + " Instances");
		instancesListLabel.setForeground(colorListSelectionFg);
		return this;
	}

	public JComponent getInstances() {
		updateView();
		return this;
	}

}
