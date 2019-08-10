/*
 * Copyright (c) 2008 Minseok Song
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

package org.processmining.analysis.performance.fsmevaluator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
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

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickTabbedPane;

import org.processmining.analysis.performance.fsmanalysis.FSMPerformanceAnalysisUI;
import org.processmining.analysis.performance.fsmanalysis.FSMStatistics;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.fsm.AcceptFSM;
import org.processmining.framework.models.fsm.FSMState;
import org.processmining.framework.models.fsm.FSMTransition;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.mining.fsm.FsmHorizonSettings;
import org.processmining.mining.fsm.FsmMinerPayload;
import org.processmining.mining.fsm.FsmSettings;

import java.util.Iterator;

/**
 * UI for the benchmark analysis plug-in.
 * 
 * @author Minseok Song
 */
public class FSMEvaluationMenuUI extends JPanel implements Provider {

	protected static final String NO_SELECTION = "- no item selected -";

	protected static Color COLOR_BG = new Color(140, 140, 140);
	protected static Color COLOR_FG = new Color(30, 30, 30);
	protected static Color COLOR_OUTER_BG = new Color(80, 80, 80);
	protected static Color COLOR_TEXT = new Color(50, 50, 50);

	public static String MEAN = "MEAN";
	public static String MEDIAN = "MEDIAN";
	public static String MIN = "MIN";
	public static String HEUST1 = "HEURISTIC1";
	public static String HEUST2 = "HEURISTIC2";
	protected SlickTabbedPane tabbedPane = null;

	protected Map<String, LogReader> logReaders;
	protected JComponent view;
	protected JPanel configurationPanel;
	protected JPanel rightPanel;
	protected JPanel metricsPanel;
	protected GUIPropertyListEnumeration logsEnumeration;
	protected GUIPropertyListEnumeration timeUnitSort;
	protected GUIPropertyListEnumeration estimatorSort;

	protected FSMEvaluationStatistics fsmEvaluationStatistics;
	protected FSMStatistics fsmStatistics;
	protected AcceptFSM acceptFSM;

	protected JButton startButton;

	public FSMEvaluationMenuUI(FSMStatistics fsmStat) {

		fsmStatistics = fsmStat;
		acceptFSM = fsmStatistics.getFSM();
		fsmEvaluationStatistics = new FSMEvaluationStatistics();

		view = null;
		configurationPanel = null;
		logReaders = null;
		setLayout(new BorderLayout());
		setBackground(COLOR_OUTER_BG);
		HeaderBar header = new HeaderBar("FSM Evaluator");
		header.setHeight(40);
		add(header, BorderLayout.NORTH);
		showConfigurationPanel();
	}

	public synchronized Map<String, LogReader> getLogReaders() {
		if (logReaders == null)
			updateFrameworkResources();
		return logReaders;
	}

	public synchronized void updateFrameworkResources() {
		logReaders = new HashMap<String, LogReader>();
		JInternalFrame[] frames = MainUI.getInstance().getDesktop()
				.getAllFrames();
		for (JInternalFrame frame : frames) {
			if (frame instanceof Provider) {
				ProvidedObject[] providedObjects = ((Provider) frame)
						.getProvidedObjects();
				for (ProvidedObject providedObject : providedObjects) {
					for (Object object : providedObject.getObjects()) {
						if (object instanceof LogReader) {
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
		allKeys[0] = FSMEvaluationMenuUI.NO_SELECTION;
		for (int i = 0; i < readerKeys.length; i++) {
			allKeys[i + 1] = readerKeys[i];
		}
		return allKeys;
	}

	public void checkStartEnabled() {
		startButton.setEnabled(true);
	}

	protected void showConfigurationPanel() {
		if (configurationPanel == null) {
			// setup configuration panel
			updateFrameworkResources();
			configurationPanel = new JPanel();
			configurationPanel.setLayout(new BorderLayout());
			configurationPanel.setBackground(COLOR_OUTER_BG);
			// setup logs panel

			ArrayList<String> values = new ArrayList<String>();
			Iterator<String> itr = logReaders.keySet().iterator();
			while (itr.hasNext()) {
				values.add(itr.next());
			}
			logsEnumeration = new GUIPropertyListEnumeration("Event Log :",
					null, values, null, 180);
			// initializing Logs
			RoundedPanel content = new RoundedPanel(10, 5, 5);
			content.setBackground(COLOR_BG);
			content.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			content.setLayout(new BoxLayout(content, BoxLayout.LINE_AXIS));
			content.add(logsEnumeration.getPropertyPanel());
			// initializing Time Sort
			initTimeSort();
			RoundedPanel content2 = new RoundedPanel(10, 5, 5);
			content2.setBackground(COLOR_BG);
			content2.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			content2.setLayout(new BoxLayout(content2, BoxLayout.LINE_AXIS));
			content2.add(timeUnitSort.getPropertyPanel());
			// initializing Measure
			ArrayList<String> values2 = new ArrayList<String>();
			values2.add(MEAN);
			values2.add(MEDIAN);
			values2.add(MIN);
			values2.add(HEUST1);
			values2.add(HEUST2);
			estimatorSort = new GUIPropertyListEnumeration("Estimator:", null,
					values2, null, 180);
			RoundedPanel content3 = new RoundedPanel(10, 5, 5);
			content3.setBackground(COLOR_BG);
			content3.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			content3.setLayout(new BoxLayout(content3, BoxLayout.LINE_AXIS));
			content3.add(estimatorSort.getPropertyPanel());

			// setup reference model / log configuration panel
			JPanel startPanel = new JPanel();
			startPanel.setOpaque(false);
			startPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.X_AXIS));
			startButton = new AutoFocusButton("start calculation");
			if (RuntimeUtils.isRunningMacOsX() == true) {
				startButton.setOpaque(true);
			}
			startButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					startCalculation();
				}
			});
			startButton.setEnabled(true);
			startPanel.add(Box.createHorizontalGlue());
			startPanel.add(startButton);
			rightPanel = new JPanel();
			rightPanel.setOpaque(false);
			rightPanel.setBorder(BorderFactory.createEmptyBorder());
			rightPanel.setLayout(new BorderLayout());
			// blank panel
			JPanel blankPanel = new JPanel();
			blankPanel.setOpaque(false);
			blankPanel.setBorder(BorderFactory.createEmptyBorder());
			blankPanel.setLayout(new BorderLayout());

			JPanel leftPanel = new JPanel();
			leftPanel.setOpaque(false);
			leftPanel.setBorder(BorderFactory.createEmptyBorder());
			leftPanel.setLayout(new BorderLayout());
			leftPanel.add(content, BorderLayout.CENTER);
			leftPanel.add(content2, BorderLayout.SOUTH);
			leftPanel.add(content3, BorderLayout.NORTH);
			// add benchmark item list to west
			rightPanel.add(blankPanel, BorderLayout.CENTER);
			rightPanel.add(startPanel, BorderLayout.SOUTH);
			configurationPanel.add(leftPanel, BorderLayout.WEST);
			configurationPanel.add(rightPanel, BorderLayout.CENTER);
		}
		// switch to configuration view
		setView(configurationPanel);
	}

	public void startCalculation() {
		rightPanel.removeAll();
		tabbedPane = new SlickTabbedPane();
		fsmEvaluationStatistics.buildDS(acceptFSM, getTimeUnit());
		fsmEvaluationStatistics.analysis(logReaders.get(logsEnumeration
				.getValue()), acceptFSM, fsmStatistics, (String) estimatorSort
				.getValue());
		FSMEvaluationAnalysisUI ui = new FSMEvaluationAnalysisUI(acceptFSM,
				fsmEvaluationStatistics);
		tabbedPane.addTab("graph view", ui);
		tabbedPane.addTab("text view", ui.getStringBuffer());
		rightPanel.add(tabbedPane, BorderLayout.CENTER);
		rightPanel.revalidate();
		rightPanel.repaint();
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

	public String getSelectedLogs() {
		return logsEnumeration.getValue().toString();
	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[0];

		// objects = new ProvidedObject[] {
		// new ProvidedObject("Similarity Model", new Object[] {simModel})
		// };

		return objects;
	}

	protected void initTimeSort() {
		ArrayList<String> timeList = new ArrayList<String>();
		timeList.add("seconds");
		timeList.add("minutes");
		timeList.add("hours");
		timeList.add("days");
		timeList.add("weeks");
		timeList.add("months");
		timeList.add("years");
		timeUnitSort = new GUIPropertyListEnumeration("Time Unit:", "",
				timeList, null, 150);
		timeUnitSort.setValue("hours");
	}

	protected long getTimeUnit() {
		if (timeUnitSort.getValue().equals("seconds")) {
			return 1000;
		} else if (timeUnitSort.getValue().equals("minutes")) {
			return 60000;
		} else if (timeUnitSort.getValue().equals("hours")) {
			return 3600000L;
		} else if (timeUnitSort.getValue().equals("days")) {
			return 86400000L;
		} else if (timeUnitSort.getValue().equals("weeks")) {
			return 604800000L;
		} else if (timeUnitSort.getValue().equals("months")) {
			return 2592000000L;
		} else {
			return 31536000000L;
		}
	}
}
