/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 *
 * LICENSE:
 *
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * EXEMPTION:
 *
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 *
 */
package org.processmining.analysis.benchmark.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickTabbedPane;
import org.processmining.analysis.benchmark.BenchmarkItem;
import org.processmining.analysis.benchmark.metric.BehavioralAppropriatenessMetric;
import org.processmining.analysis.benchmark.metric.BehavioralPrecisionMetric;
import org.processmining.analysis.benchmark.metric.BehavioralRecallMetric;
import org.processmining.analysis.benchmark.metric.BenchmarkMetric;
import org.processmining.analysis.benchmark.metric.CausalFootprintMetric;
import org.processmining.analysis.benchmark.metric.DuplicatesPrecisionMetric;
import org.processmining.analysis.benchmark.metric.DuplicatesRecallMetric;
import org.processmining.analysis.benchmark.metric.ImprovedContinuousSemanticsFitnessMetric;
import org.processmining.analysis.benchmark.metric.ProperCompletionFitnessMetric;
import org.processmining.analysis.benchmark.metric.StructuralAppropriatenessMetric;
import org.processmining.analysis.benchmark.metric.StructuralPrecisionMetric;
import org.processmining.analysis.benchmark.metric.StructuralRecallMetric;
import org.processmining.analysis.benchmark.metric.TokenFitnessMetric;
import org.processmining.analysis.logreaderconnection.PetriNetLogReaderConnectionPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * UI for the benchmark analysis plugin.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 */
public class BenchmarkUI extends JPanel {

	private static final long serialVersionUID = -1108724808375264721L;
	protected static final String NO_SELECTION = "- no item selected -";

	protected static Color COLOR_BG = new Color(140, 140, 140);
	protected static Color COLOR_FG = new Color(30, 30, 30);
	protected static Color COLOR_OUTER_BG = new Color(80, 80, 80);
	protected static Color COLOR_TEXT = new Color(50, 50, 50);

	protected Map<String, PetriNet> petriNets;
	protected Map<String, LogReader> logReaders;

	protected BenchmarkItemList itemList;
	protected BenchmarkMetricList metricsList;
	protected JComponent view;
	protected JPanel configurationPanel;
	protected JComboBox referenceLogBox;
	protected JComboBox referenceModelBox;
	protected JEditorPane helpPane;
	protected JLabel helpHeader;

	protected JButton startButton;

	public BenchmarkUI() {
		view = null;
		configurationPanel = null;
		petriNets = null;
		logReaders = null;
		setLayout(new BorderLayout());
		setBackground(COLOR_OUTER_BG);
		HeaderBar header = new HeaderBar("Control Flow Benchmark");
		header.setHeight(40);
		add(header, BorderLayout.NORTH);
		showConfigurationPanel();
	}

	public synchronized Map<String, PetriNet> getPetriNets() {
		return petriNets;
	}

	public synchronized Map<String, LogReader> getLogReaders() {
		return logReaders;
	}

	public synchronized void updateFrameworkResources() {
		petriNets = new HashMap<String, PetriNet>();
		logReaders = new HashMap<String, LogReader>();
		JInternalFrame[] frames = MainUI.getInstance().getDesktop()
				.getAllFrames();
		for (JInternalFrame frame : frames) {
			if (frame instanceof Provider) {
				ProvidedObject[] providedObjects = ((Provider) frame)
						.getProvidedObjects();
				for (ProvidedObject providedObject : providedObjects) {
					for (Object object : providedObject.getObjects()) {
						if (object instanceof PetriNet) {
							petriNets.put(frame.getTitle() + " - "
									+ providedObject.getName(),
									(PetriNet) object);
						} else if (object instanceof LogReader) {
							logReaders.put(frame.getTitle() + " - "
									+ providedObject.getName(),
									(LogReader) object);
						}
					}
				}
			}
		}
	}

	protected synchronized void setView(JComponent component) {
		if (view != null) {
			remove(view);
		}
		add(component, BorderLayout.CENTER);
		view = component;
		revalidate();
		repaint();
	}

	public Object[] getLogReaderBoxKeys() {
		Object[] readerKeys = logReaders.keySet().toArray();
		Object[] allKeys = new Object[readerKeys.length + 1];
		allKeys[0] = BenchmarkUI.NO_SELECTION;
		for (int i = 0; i < readerKeys.length; i++) {
			allKeys[i + 1] = readerKeys[i];
		}
		return allKeys;
	}

	public Object[] getPetriNetBoxKeys() {
		Object[] modelKeys = petriNets.keySet().toArray();
		Object[] allKeys = new Object[modelKeys.length + 1];
		allKeys[0] = BenchmarkUI.NO_SELECTION;
		for (int i = 0; i < modelKeys.length; i++) {
			allKeys[i + 1] = modelKeys[i];
		}
		return allKeys;
	}

	public LogReader getReferenceLogReader() {
		Object key = referenceLogBox.getSelectedItem();
		if (key == BenchmarkUI.NO_SELECTION) {
			return null;
		} else {
			return logReaders.get(key);
		}
	}

	public PetriNet getReferencePetriNet() {
		Object key = referenceModelBox.getSelectedItem();
		if (key == BenchmarkUI.NO_SELECTION) {
			return null;
		} else {
			return petriNets.get(key);
		}
	}

	protected void showConfigurationPanel() {
		if (configurationPanel == null) {
			// setup configuration panel
			updateFrameworkResources();
			configurationPanel = new JPanel();
			configurationPanel.setLayout(new BorderLayout());
			configurationPanel.setBackground(COLOR_OUTER_BG);
			// setup reference model / log configuration panel
			RoundedPanel refPanel = new RoundedPanel(10, 5, 5);
			refPanel.setLayout(new BoxLayout(refPanel, BoxLayout.Y_AXIS));
			refPanel.setBackground(COLOR_BG);
			referenceLogBox = new JComboBox(getLogReaderBoxKeys());
			referenceLogBox.setMinimumSize(new Dimension(100, 30));
			referenceLogBox.setMaximumSize(new Dimension(1000, 30));
			referenceLogBox.setPreferredSize(new Dimension(300, 30));
			referenceLogBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					LogReader log = getReferenceLogReader();
					metricsList.setReferenceLogAvailable(log != null);
					checkStartEnabled();
				}
			});
			referenceModelBox = new JComboBox(getPetriNetBoxKeys());
			referenceModelBox.setMinimumSize(new Dimension(100, 30));
			referenceModelBox.setMaximumSize(new Dimension(1000, 30));
			referenceModelBox.setPreferredSize(new Dimension(300, 30));
			referenceModelBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					PetriNet model = getReferencePetriNet();
					metricsList.setReferenceModelAvailable(model != null);
					checkStartEnabled();
				}
			});
			if (RuntimeUtils.isRunningMacOsX() == true) {
				referenceModelBox.setOpaque(false);
				referenceLogBox.setOpaque(false);
			}
			JLabel refLogLabel = new JLabel("Reference log");
			refLogLabel.setOpaque(false);
			refLogLabel.setForeground(COLOR_FG);
			JLabel refModelLabel = new JLabel("Reference model");
			refModelLabel.setOpaque(false);
			refModelLabel.setForeground(COLOR_FG);
			refPanel
					.add(packHorizontallyLeftAligned(new Component[] {
							refLogLabel, Box.createHorizontalStrut(20),
							referenceLogBox }, 0));
			refPanel.add(packHorizontallyLeftAligned(new Component[] {
					refModelLabel, Box.createHorizontalStrut(20),
					referenceModelBox }, 0));
			// setup right panel
			metricsList = new BenchmarkMetricList(this);
			// fitness metrics
			metricsList.addBenchmarkMetric(new ProperCompletionFitnessMetric());
			metricsList.addBenchmarkMetric(new TokenFitnessMetric());
			metricsList
					.addBenchmarkMetric(new ImprovedContinuousSemanticsFitnessMetric());
			// precision metrics
			metricsList
					.addBenchmarkMetric(new BehavioralAppropriatenessMetric());
			metricsList.addBenchmarkMetric(new BehavioralPrecisionMetric());
			// generalization metrics
			metricsList.addBenchmarkMetric(new BehavioralRecallMetric());
			// precision and generalization metrics (combined)
			metricsList.addBenchmarkMetric(new CausalFootprintMetric());
			// structure metrics
			metricsList
					.addBenchmarkMetric(new StructuralAppropriatenessMetric());
			metricsList.addBenchmarkMetric(new StructuralPrecisionMetric());
			metricsList.addBenchmarkMetric(new StructuralRecallMetric());
			metricsList.addBenchmarkMetric(new DuplicatesPrecisionMetric());
			metricsList.addBenchmarkMetric(new DuplicatesRecallMetric());
			metricsList
					.setReferenceLogAvailable(this.getReferenceLogReader() != null);
			metricsList
					.setReferenceModelAvailable(this.getReferencePetriNet() != null);
			JPanel startPanel = new JPanel();
			startPanel.setOpaque(false);
			startPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.X_AXIS));
			startButton = new AutoFocusButton("start benchmark");
			if (RuntimeUtils.isRunningMacOsX() == true) {
				startButton.setOpaque(false);
			}
			startButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					startBenchmark();
				}
			});
			startButton.setEnabled(false);
			startPanel.add(Box.createHorizontalGlue());
			startPanel.add(startButton);
			JPanel rightPanel = new JPanel();
			rightPanel.setOpaque(false);
			rightPanel.setBorder(BorderFactory.createEmptyBorder());
			rightPanel.setLayout(new BorderLayout());

			// metric panel with metrics and help window
			JPanel metricPanel = new JPanel();
			metricPanel.setOpaque(false);
			metricPanel.setBorder(BorderFactory.createEmptyBorder());
			metricPanel.setLayout(new BorderLayout());
			rightPanel.add(refPanel, BorderLayout.NORTH);
			metricPanel.add(metricsList, BorderLayout.CENTER);
			// add benchmark item list to west
			itemList = new BenchmarkItemList(this);

			// compose metric help panel
			RoundedPanel helpPanel = new RoundedPanel(10, 5, 2);
			helpPanel.setMinimumSize(new Dimension(200, 90));
			helpPanel.setMaximumSize(new Dimension(2000, 90));
			helpPanel.setPreferredSize(new Dimension(300, 90));
			helpPanel.setLayout(new BorderLayout());
			// helpPanel.setBackground(COLOR_BG);
			helpPanel.setBackground(new Color(110, 110, 110));
			helpPane = new JEditorPane();
			helpPane.setEditable(false);
			helpPane.setContentType("text/html");
			// helpPane.setBackground(COLOR_BG);
			helpPane.setBackground(new Color(110, 110, 110));
			helpPane.setForeground(COLOR_TEXT);
			helpPane.setFont(helpPane.getFont().deriveFont(10f));
			JScrollPane helpScrollPane = new JScrollPane(helpPane);
			helpScrollPane
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			helpScrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			helpScrollPane.setBorder(BorderFactory.createEmptyBorder());
			helpHeader = new JLabel("help:");
			helpHeader.setForeground(new Color(60, 60, 60));
			helpHeader.setOpaque(false);
			helpHeader.setFont(helpHeader.getFont().deriveFont(13f));
			JPanel helpHeaderPanel = new JPanel();
			helpHeaderPanel.setOpaque(false);
			helpHeaderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0,
					15));
			helpHeaderPanel.setLayout(new BoxLayout(helpHeaderPanel,
					BoxLayout.Y_AXIS));
			helpHeaderPanel.add(helpHeader);
			helpHeaderPanel.add(Box.createVerticalGlue());
			helpPanel.add(helpHeaderPanel, BorderLayout.WEST);
			helpPanel.add(helpScrollPane, BorderLayout.CENTER);
			// initial text
			showHelp("click metric to view help");

			metricPanel.add(helpPanel, BorderLayout.SOUTH);
			rightPanel.add(metricPanel, BorderLayout.CENTER);
			rightPanel.add(startPanel, BorderLayout.SOUTH);
			configurationPanel.add(itemList, BorderLayout.WEST);
			configurationPanel.add(rightPanel, BorderLayout.CENTER);
		}
		// switch to configuration view
		setView(configurationPanel);
	}

	public void checkStartEnabled() {
		if (itemList != null && metricsList != null) {
			boolean enabled = (itemList.getBenchmarkItems().size() > 0 && metricsList
					.getBenchmarkMetrics().size() > 0);
			startButton.setEnabled(enabled);
		}
	}

	public void startBenchmark() {
		LogReader referenceLog = getReferenceLogReader();
		PetriNet referenceModel = getReferencePetriNet();
		final List<BenchmarkItem> benchmarkItems = itemList.getBenchmarkItems();
		final List<BenchmarkMetric> benchmarkMetrics = metricsList
				.getBenchmarkMetrics();
		if (referenceLog != null) {
			// map reference model and all item's models to reference log
			if (referenceModel != null) {
				// TODO: check whether log projection must also be stored for
				// the reference model (not clear how this could be
				// considered for the metrics, which only take (on mined model
				// projected) log, mined model, and reference model)
				PetriNetResult result = connectModelWithLog(referenceModel,
						referenceLog);
				referenceModel = result.getPetriNet();
			}
			for (BenchmarkItem item : benchmarkItems) {
				try {
					// also store log reader as the mapping may change the log
					// for this particular combination
					PetriNetResult result = connectModelWithLog(
							item.getModel(), referenceLog);
					item.setModel(result.getPetriNet());
					item.setLog(result.getLogReader());
				} catch (Exception e) {
					Message
							.add("Copying the log reader for connection with benchmark item failed: "
									+ item.getName());
				}
			}
		}
		// heavy lifting goes here
		final BenchmarkRunPanel runPanel = new BenchmarkRunPanel();
		ActionListener finishListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (runPanel.isAborted() == true) {
					showConfigurationPanel();
				} else {
					showResults(benchmarkItems, benchmarkMetrics);
				}
			}
		};
		setView(runPanel.getPanel());
		runPanel.runBenchmark(referenceLog, referenceModel, benchmarkItems,
				benchmarkMetrics, finishListener);
	}

	public void showResults(List<BenchmarkItem> items,
			List<BenchmarkMetric> metrics) {
		BenchmarkResultTableModel tableModel = new BenchmarkResultTableModel(
				items, metrics);
		JTable resultTable = new JTable(tableModel);
		for (int x = 1; x < resultTable.getColumnCount(); x++) {
			resultTable.getColumnModel().getColumn(x).setCellRenderer(
					new BenchmarkResultTableCellRenderer());
		}
		JScrollPane resultScrollPane = new JScrollPane(resultTable);
		// JTabbedPane tabbedPane = new JTabbedPane();
		SlickTabbedPane tabbedPane = new SlickTabbedPane();
		tabbedPane.addTab("Bar profile view", new BenchmarkResultBarView(
				metrics, items));
		tabbedPane.addTab("Table view", resultScrollPane);
		tabbedPane.addTab("Export CSV",
				new BenchmarkExportPanel(items, metrics));
		setView(tabbedPane);
	}

	protected PetriNetResult connectModelWithLog(PetriNet model, LogReader log) {
		PetriNetResult result = new PetriNetResult(model);
		PetriNetLogReaderConnectionPlugin conn = new PetriNetLogReaderConnectionPlugin();
		MainUI.getInstance().connectResultWithLog(result, log, conn, false,
				true);
		return result;
	}

	protected static JPanel packHorizontallyLeftAligned(Component[] components,
			int leftOffset) {
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		if (leftOffset > 0) {
			packed.add(Box.createHorizontalStrut(leftOffset));
		}
		int minW = 0, minH = 0;
		for (Component comp : components) {
			packed.add(comp);
			Dimension dim = comp.getMinimumSize();
			minW += dim.getWidth();
			minH = Math.max(minH, (int) dim.getHeight());
		}
		packed.add(Box.createHorizontalGlue());
		packed.setMinimumSize(new Dimension(minW, minH));
		packed.setMaximumSize(new Dimension(4000, minH));
		packed.setPreferredSize(new Dimension(4000, minH));
		return packed;
	}

	/**
	 * Displays the help text about the clicked metric.
	 * 
	 * @param description
	 */
	protected void showHelp(String description) {
		String helpText = "<html><font face=\"helvetica, arial, sans-serif\" color=#303030 size=\"-1\">";
		helpText += description;
		helpText += "</font></html>";
		helpPane.setText(helpText);
		helpPane.setCaretPosition(0);
	}

}
