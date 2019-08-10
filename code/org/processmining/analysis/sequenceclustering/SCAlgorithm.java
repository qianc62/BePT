package org.processmining.analysis.sequenceclustering;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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

import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.analysis.clustering.model.LogSequence;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.ui.slicker.logdialog.InspectorUI;
import org.processmining.framework.ui.slicker.logdialog.SlickerOpenLogSettings;

/**
 * @author Gabriel Veiga, IST - Technical University of Lisbon
 * @author Supervisor: Prof. Diogo Ferreira
 */
public class SCAlgorithm extends JPanel {

	protected Color colorEnclosureBg = new Color(40, 40, 40);
	protected Color colorNonFocus = new Color(70, 70, 70);
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

	protected JList clustersList;
	protected JList instancesList;
	protected JList eventsList;

	protected JLabel clusterNameLabel;
	protected JLabel clusterSizeLabel;
	protected JLabel instanceLabel;
	protected JLabel instanceSizeLabel;

	protected FlatTabbedPane tabPane;

	protected InspectorUI inspectorUI;
	protected SlickerOpenLogSettings parent;
	protected LogReader log = null;
	protected LogReader originalLog = null;
	protected LogSummary logSummary;
	protected LogSequence logS = null;

	protected int numClusters, numInstances, numLogEvents;
	protected List<ProcessInstance> instanceList;
	protected Random rand = new Random();
	protected double[][] probabilities;

	protected int iteration;
	protected double minEventSupport, maxEventSupport;
	protected boolean preprocessed;

	protected SCLogFilter filter = null;
	protected ArrayList<LogEvent> events = new ArrayList<LogEvent>();
	protected ArrayList<String> instancesToRemove = new ArrayList<String>();
	protected ArrayList<Cluster> clusterList;
	protected ArrayList<String> sequences;
	protected ArrayList<Integer> sequenceOccurences;
	protected Cluster currentCluster;

	public SCAlgorithm(LogReader log, LogReader originalLog,
			double minEventSupport, double maxEventSupport, int numberClusters,
			ProgressPanel progressPanel, boolean preprocessed) {
		this.log = log;
		this.originalLog = originalLog;
		this.numClusters = numberClusters;
		this.numInstances = log.numberOfInstances();
		this.numLogEvents = log.getLogSummary().getLogEvents().size();
		this.instanceList = log.getInstances();
		this.iteration = 0;
		this.minEventSupport = minEventSupport;
		this.maxEventSupport = maxEventSupport;
		this.preprocessed = preprocessed;

		sequenceClustering(progressPanel);

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

		tabPane = new FlatTabbedPane("Sequence Clustering", new Color(240, 240,
				240, 230), new Color(180, 180, 180, 120), new Color(220, 220,
				220, 150));
		// create clusters list
		clustersList = new JList();
		clustersList.setBackground(colorListBg);
		clustersList.setForeground(colorListFg);
		clustersList.setSelectionBackground(colorListSelectionBg);
		clustersList.setSelectionForeground(colorListSelectionFg);
		clustersList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		clustersList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				clustersSelectionChanged();
			}
		});
		JScrollPane clustersScrollPane = new JScrollPane(clustersList);
		clustersScrollPane.setOpaque(false);
		clustersScrollPane.setBorder(BorderFactory.createEmptyBorder());
		clustersScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		clustersScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollBar vBar = clustersScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), colorNonFocus, 4, 12));
		vBar.setOpaque(false);
		vBar = clustersScrollPane.getHorizontalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(200, 200, 200), colorNonFocus, 4, 12));
		vBar.setOpaque(false);

		// assemble clusters list
		JLabel clustersListLabel = new JLabel("Clusters");
		clustersListLabel.setOpaque(false);
		clustersListLabel.setForeground(colorListSelectionFg);
		clustersListLabel.setFont(clustersListLabel.getFont().deriveFont(13f));
		clustersListLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		clustersListLabel.setHorizontalAlignment(JLabel.CENTER);
		clustersListLabel.setHorizontalTextPosition(JLabel.CENTER);
		RoundedPanel clustersPanel = new RoundedPanel(10, 5, 0);
		clustersPanel.setBackground(colorEnclosureBg);
		clustersPanel.setLayout(new BoxLayout(clustersPanel, BoxLayout.Y_AXIS));
		clustersPanel.add(clustersListLabel);
		clustersPanel.add(Box.createVerticalStrut(8));
		clustersPanel.add(clustersScrollPane);

		clustersPanel.setMinimumSize(new Dimension(180, 100));
		clustersPanel.setMaximumSize(new Dimension(300, 1000));
		clustersPanel.setPreferredSize(new Dimension(200, 500));

		// create instance list header
		clusterNameLabel = new JLabel("(no cluster selected)");
		clusterNameLabel.setOpaque(false);
		clusterNameLabel.setForeground(colorNonFocus);
		clusterNameLabel.setFont(clusterNameLabel.getFont().deriveFont(13f));
		clusterNameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		clusterNameLabel.setHorizontalAlignment(JLabel.CENTER);
		clusterNameLabel.setHorizontalTextPosition(JLabel.CENTER);
		clusterSizeLabel = new JLabel("select single cluster to browse");
		clusterSizeLabel.setOpaque(false);
		clusterSizeLabel.setForeground(colorNonFocus);
		clusterSizeLabel.setFont(clusterSizeLabel.getFont().deriveFont(11f));
		clusterSizeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		clusterSizeLabel.setHorizontalAlignment(JLabel.CENTER);
		clusterSizeLabel.setHorizontalTextPosition(JLabel.CENTER);

		// create instances list
		instancesList = new JList();
		instancesList.setBackground(colorListBg);
		instancesList.setForeground(colorListFg);
		instancesList.setSelectionBackground(colorListSelectionBg);
		instancesList.setSelectionForeground(colorListSelectionFg);
		instancesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		vBar = instancesScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), colorNonFocus, 4, 12));
		vBar.setOpaque(false);
		vBar = instancesScrollPane.getHorizontalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(200, 200, 200), colorNonFocus, 4, 12));
		vBar.setOpaque(false);
		// assemble instances list
		RoundedPanel instancesPanel = new RoundedPanel(10, 5, 0);
		instancesPanel.setBackground(colorEnclosureBg);
		instancesPanel
				.setLayout(new BoxLayout(instancesPanel, BoxLayout.Y_AXIS));
		instancesPanel.add(clusterNameLabel);
		instancesPanel.add(clusterSizeLabel);
		instancesPanel.add(Box.createVerticalStrut(8));
		instancesPanel.add(instancesScrollPane);

		// create events list
		eventsList = new JList();
		eventsList.setBackground(colorListBg);
		eventsList.setForeground(colorListFg);
		eventsList.setCellRenderer(new EventCellRenderer());

		eventsList.setSelectionBackground(colorListBg);
		eventsList.setSelectionForeground(colorListFg);

		JScrollPane eventsScrollPane = new JScrollPane(eventsList);
		eventsScrollPane.setOpaque(false);
		eventsScrollPane.setBorder(BorderFactory.createEmptyBorder());
		eventsScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		eventsScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		vBar = eventsScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(200, 200, 200), colorNonFocus, 4, 12));
		vBar.setOpaque(false);
		vBar = eventsScrollPane.getHorizontalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(200, 200, 200), colorNonFocus, 4, 12));
		vBar.setOpaque(false);

		// assemble events panel
		instanceLabel = new JLabel("(no instance selected)");
		instanceLabel.setOpaque(false);
		instanceLabel.setForeground(colorNonFocus);
		instanceLabel.setFont(instanceLabel.getFont().deriveFont(13f));
		instanceLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		instanceLabel.setHorizontalAlignment(JLabel.CENTER);
		instanceLabel.setHorizontalTextPosition(JLabel.CENTER);
		instanceSizeLabel = new JLabel("select single instance to browse");
		instanceSizeLabel.setOpaque(false);
		instanceSizeLabel.setForeground(colorNonFocus);
		instanceSizeLabel.setFont(instanceSizeLabel.getFont().deriveFont(13f));
		instanceSizeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		instanceSizeLabel.setHorizontalAlignment(JLabel.CENTER);
		instanceSizeLabel.setHorizontalTextPosition(JLabel.CENTER);
		RoundedPanel eventsPanel = new RoundedPanel(10, 5, 0);
		eventsPanel.setBackground(colorEnclosureBg);
		eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
		eventsPanel.add(instanceLabel);
		eventsPanel.add(instanceSizeLabel);
		eventsPanel.add(Box.createVerticalStrut(8));
		eventsPanel.add(eventsScrollPane);

		eventsPanel.setMinimumSize(new Dimension(220, 100));
		eventsPanel.setMaximumSize(new Dimension(380, 1000));
		eventsPanel.setPreferredSize(new Dimension(250, 500));

		// assemble GUI

		this.add(clustersPanel);
		this.add(instancesPanel);
		this.add(eventsPanel);
	}

	protected JComponent packLeftAligned(JComponent comp) {
		Box enclosure = Box.createHorizontalBox();
		enclosure.setOpaque(false);
		enclosure.add(comp);
		enclosure.add(Box.createHorizontalGlue());
		return enclosure;
	}

	protected void clustersSelectionChanged() {
		int[] selectedIndices = clustersList.getSelectedIndices();
		if (selectedIndices.length == 0 || selectedIndices.length > 1) {
			instancesList.setListData(new Object[] {});
			instancesList.clearSelection();
			clusterNameLabel.setForeground(colorNonFocus);
			clusterNameLabel.setText("(no cluster selected)");
			clusterSizeLabel.setForeground(colorNonFocus);
			clusterSizeLabel.setText("select single cluster to browse");
		} else {
			Cluster cluster = clusterList.get(selectedIndices[0]);
			currentCluster = cluster;
			// List<ProcessInstance> piList = cluster.getLog().getInstances();
			List<String> piList = cluster.instancesTypesName;
			clusterNameLabel.setForeground(colorListSelectionFg);
			clusterNameLabel.setText(cluster.getName());
			clusterSizeLabel.setForeground(colorListSelectionFg);

			if (piList.size() > 1)
				clusterSizeLabel.setText(piList.size() + " Instances Types");
			else
				clusterSizeLabel.setText(piList.size() + " Instance Type");

			instancesList.setModel(new InstanceTypeListModel(piList));
			instancesList.ensureIndexIsVisible(0);
		}
		showSelectedclusterData();
	}

	protected void instancesSelectionChanged() {
		int[] selectedIndices = instancesList.getSelectedIndices();
		if (selectedIndices.length == 0 || selectedIndices.length > 1) {
			eventsList.setListData(new Object[] {});
			eventsList.clearSelection();
			instanceLabel.setForeground(colorNonFocus);
			instanceLabel.setText("(no instance selected)");
			instanceSizeLabel.setForeground(colorNonFocus);
			instanceSizeLabel.setText("select single instance to browse");
		} else {
			ProcessInstance instance = currentCluster.getLog().getInstance(
					currentCluster.instancesTypesRepresentative
							.get(selectedIndices[0]));
			AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
			instanceLabel.setForeground(colorListSelectionFg);
			instanceLabel.setText("Type " + (selectedIndices[0] + 1));
			instanceSizeLabel.setForeground(colorListSelectionFg);
			instanceSizeLabel.setText(ateList.size() + " events");
			eventsList.setModel(new AuditTrailEntryListModel(ateList));
			eventsList.ensureIndexIsVisible(0);
		}
		showSelectedinstanceData();
	}

	public void eventsSelectionChanged() {
		AuditTrailEntry ate = (AuditTrailEntry) eventsList.getSelectedValue();

		if (ate != null) {
			instanceLabel.setForeground(colorListSelectionFg);
		} else {
			showSelectedclusterData();
		}
	}

	protected void showSelectedclusterData() {
		int[] selectedIndices = clustersList.getSelectedIndices();

		if (selectedIndices.length == 1) {
			Cluster cluster = clusterList.get(selectedIndices[0]);
		} else {
			instanceLabel.setForeground(colorNonFocus);
			instanceLabel.setText("(no instance selected)");
		}
	}

	protected void showSelectedinstanceData() {
		int[] selectedIndices = clustersList.getSelectedIndices();

		if (selectedIndices.length == 1) {
			ProcessInstance instance = log.getInstance(selectedIndices[0]);
		} else {
			instanceLabel.setForeground(colorNonFocus);
			instanceLabel.setText("(no instance selected)");
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

		logSummary = log.getLogSummary();
		// repopulate instance names list
		String[] clusterNames = new String[numClusters];
		for (int i = 0; i < clusterNames.length; i++) {
			clusterNames[i] = clusterList.get(i).getName() + "  ("
					+ clusterList.get(i).instancesToKeep.size() + " Instances)";

		}
		clustersList.setListData(clusterNames);
		// reset events list
		clustersList.clearSelection();
		revalidate();
		repaint();
	}

	public LogReader getResultReader() {
		if (log == null || clustersList.getSelectedIndices().length == 0) {
			return null;
		} else {
			try {
				return LogReaderFactory.createInstance(log, clustersList
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

	protected class InstanceTypeListModel extends AbstractListModel {

		protected List<String> piList;

		protected InstanceTypeListModel(List<String> list) {
			piList = list;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		public Object getElementAt(int index) {
			try {
				return piList.get(index);
			} catch (IndexOutOfBoundsException e) {
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
			return piList.size();
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

		/**
		 * @param colorTop
		 * @param colorBottom
		 */
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

	public void initializeProbabilities() {
		for (int i = 0; i < log.getInstances().size(); i++) {
			for (int j = 0; j < numClusters; j++) {
				probabilities[i][j] = 0.0;
			}

		}
	}

	/**
	 * Calculates the probability of each cluster producing each sequence
	 * 
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 */
	public void calculateProbabilities() throws IndexOutOfBoundsException,
			IOException {

		for (int k = 0; k < numClusters; k++) {

			for (int i = 0; i < log.getInstances().size(); i++) {
				AuditTrailEntry event;
				double prob = 0.0;
				int size;
				int index = -1;
				int previousIndex = -1;

				for (int j = 0; j < log.getInstances().get(i)
						.getAuditTrailEntryList().size(); j++) {
					size = log.getInstances().get(i).getAuditTrailEntryList()
							.size();
					event = log.getInstances().get(i).getAuditTrailEntryList()
							.get(j);
					previousIndex = index;
					index = index(event);

					if (j == size - 1) {
						prob += Math
								.log(clusterList.get(k).markovChain[index][numLogEvents + 1]);
					} else if (previousIndex == -1) {
						prob += Math
								.log(clusterList.get(k).markovChain[0][index]);
					} else
						prob += Math
								.log(clusterList.get(k).markovChain[previousIndex][index]);
				}
				index = -1;
				probabilities[i][k] = prob;
			}
		}
	}

	/**
	 * Assigns sequences to clusters
	 */
	public void assignSequences() {
		int iter, maxCluster;
		double max;

		initializeProbabilities();

		try {
			calculateProbabilities();
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < log.getInstances().size(); i++) {
			iter = 0;
			max = 0.0;
			maxCluster = 0;

			for (int k = 0; k < numClusters; k++) {
				if (iter == 0 || max < probabilities[i][k]) {
					max = probabilities[i][k];
					maxCluster = k;
					iter++;
				}
			}

			clusterList.get(maxCluster).instancesToKeep.add(log.getInstances()
					.get(i).getName());
			clusterList.get(maxCluster).instancesToKeepId.add(i);
		}

	}

	public void calculateMarkovChain() {
		try {
			calculateAbsoluteOccurences();
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		calculateMarkovMatrix();
	}

	/**
	 * Calculates the absolute occurrence of transitions in the cluster
	 * 
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 */
	public void calculateAbsoluteOccurences() throws IndexOutOfBoundsException,
			IOException {
		AuditTrailEntry event;

		int instanceId, size;
		int index = -1;
		int previousIndex = -1;

		for (int k = 0; k < numClusters; k++) {
			for (int i = 0; i < clusterList.get(k).instancesToKeep.size(); i++) {
				instanceId = clusterList.get(k).instancesToKeepId.get(i);

				for (int j = 0; j < log.getInstances().get(instanceId)
						.getAuditTrailEntryList().size(); j++) {
					size = log.getInstances().get(instanceId)
							.getAuditTrailEntryList().size();
					event = log.getInstances().get(instanceId)
							.getAuditTrailEntryList().get(j);

					if (size == 1) {
						index = index(event);
						clusterList.get(k).absoluteOccurences[0][index]++;
						clusterList.get(k).absoluteOccurences[index][numLogEvents + 1]++;
					} else {

						if (j == size - 1) {
							previousIndex = index;
							index = index(event);
							clusterList.get(k).absoluteOccurences[previousIndex][index]++;
							clusterList.get(k).absoluteOccurences[index][numLogEvents + 1]++;
						} else {
							previousIndex = index;
							index = index(event);

							if (previousIndex == -1)
								clusterList.get(k).absoluteOccurences[0][index]++;
							else
								clusterList.get(k).absoluteOccurences[previousIndex][index]++;
						}
					}
				}
				index = -1;
			}
		}
	}

	/**
	 * Calculates the relative occurrence of transitions in the cluster to fill
	 * the probabilities of the Markov chain representing that cluster
	 */
	public void calculateMarkovMatrix() {
		double sum;

		for (int k = 0; k < numClusters; k++) {
			for (int i = 0; i < numLogEvents + 2; i++) {
				sum = 0;
				for (int j = 0; j < numLogEvents + 2; j++) {
					sum = sum + clusterList.get(k).absoluteOccurences[i][j];
				}
				for (int j = 0; j < numLogEvents + 2; j++) {
					if (sum != 0)
						clusterList.get(k).markovChain[i][j] = clusterList
								.get(k).absoluteOccurences[i][j]
								/ sum;
				}
			}
		}

		normalizeMarkovChain();

	}

	/**
	 * Normalizes the Markov chain, so that the sum of each line of the matrix
	 * equals 1.0
	 */
	public void normalizeMarkovChain() {
		double sum;

		for (int k = 0; k < numClusters; k++) {
			for (int i = 0; i < numLogEvents + 2; i++) {
				sum = 0;
				for (int j = 0; j < numLogEvents + 2; j++) {
					sum = sum + clusterList.get(k).markovChain[i][j];
				}
				for (int j = 0; j < numLogEvents + 2; j++) {
					if (sum != 0)
						clusterList.get(k).markovChain[i][j] = clusterList
								.get(k).markovChain[i][j]
								/ sum;
				}
			}
		}
	}

	/**
	 * Rounds the values of the probabilities
	 */
	public void roundMarkovChain() {
		double sum;

		for (int k = 0; k < numClusters; k++) {
			for (int i = 0; i < numLogEvents + 2; i++) {
				sum = 0;
				for (int j = 0; j < numLogEvents + 2; j++) {
					sum = sum + clusterList.get(k).markovChain[i][j];
				}
				for (int j = 0; j < numLogEvents + 2; j++) {
					if (sum != 0)
						clusterList.get(k).markovChain[i][j] = round(
								clusterList.get(k).markovChain[i][j] / sum, 3);
				}
			}
		}
	}

	public double round(double number, int decimals) {

		double factor = Math.pow(10, decimals);

		return Math.round(number * factor) / factor;

	}

	public int index(AuditTrailEntry event) {

		for (int i = 0; i < numLogEvents; i++) {
			if (event.getElement().equals(
					log.getLogSummary().getLogEvents().get(i)
							.getModelElementName())) {
				return i + 1;
			}
		}

		return -1;
	}

	public void printProbabilities() {

		for (int i = 0; i < numLogEvents + 2; i++) {
			System.out.print("\n");
			if (i >= numLogEvents)
				System.out.print("o");
			else
				System.out.print(log.getLogSummary().getLogEvents().get(i)
						.getModelElementName());
			for (int j = 0; j < numLogEvents + 2; j++) {
				System.out.print(" "
						+ clusterList.get(0).absoluteOccurences[i][j]);
			}

		}
	}

	public boolean changed() {
		boolean changedClusters = false;

		for (int i = 0; i < numClusters; i++) {
			if (clusterList.get(i).compareInstances())
				clusterList.get(i).changed = false;
			else
				clusterList.get(i).changed = true;
		}

		for (int i = 0; i < numClusters; i++) {
			changedClusters = changedClusters || clusterList.get(i).changed;
		}

		return changedClusters;
	}

	/**
	 * Performs the sequence clustering algorithm
	 * 
	 * @param progressPanel
	 */
	public void sequenceClustering(ProgressPanel progressPanel) {
		clusterList = new ArrayList<Cluster>();

		// create clusters
		for (int i = 0; i < numClusters; i++) {
			clusterList.add(new Cluster(i, numLogEvents, log, originalLog,
					this, minEventSupport, maxEventSupport, preprocessed));
		}

		// create the probabilities matrix
		probabilities = new double[numInstances][numClusters];

		initializeProbabilities();

		// randomly assign sequences to clusters
		for (int i = 0; i < numInstances; i++) {
			Integer r = new Integer(rand.nextInt(numClusters));
			clusterList.get(r).instancesToKeep.add(log.getInstances().get(i)
					.getName());
			clusterList.get(r).instancesToKeepId.add(i);
		}

		calculateMarkovChain();

		// printProbabilities();

		while (changed() || iteration == 0) {
			iteration++;
			progressPanel.inc();

			if (iteration == 1)
				progressPanel.setNote(iteration + "st iteration");
			else if (iteration == 2)
				progressPanel.setNote(iteration + "nd iteration");
			else if (iteration == 3)
				progressPanel.setNote(iteration + "rd iteration");
			else
				progressPanel.setNote(iteration + "th iteration");

			for (int i = 0; i < numClusters; i++) {
				clusterList.get(i).initializeInstances();
			}
			assignSequences();

			for (int i = 0; i < numClusters; i++) {
				clusterList.get(i).initializeMarkovChain();
			}
			calculateMarkovChain();

		}
		roundMarkovChain();

		progressPanel.setProgress(progressPanel.getMaximum());
		progressPanel.setNote("Finalizing");

		// build logs(clusters)
		for (int i = 0; i < numClusters; i++) {
			clusterList.get(i).buildCluster();
		}

		for (int i = 0; i < numClusters; i++) {
			clusterList.get(i).buildInstancesTypes();
		}

	}

	public JComponent getclusters() {
		updateView();
		return this;
	}

}