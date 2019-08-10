/*
 * Created on July. 02, 2007
 *
 * Author: Minseok Song
 * (c) 2006 Technische Universiteit Eindhoven, Minseok Song
 * all rights reserved
 *
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */

package org.processmining.analysis.performance.fsmanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.deckfour.slickerbox.components.AutoFocusButton;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.fsm.AcceptFSM;
import org.processmining.framework.models.fsm.FSMState;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

public class FSMPerformanceAnalysisUI extends JPanel implements
		GuiNotificationTarget {

	private static final long serialVersionUID = -3036511492888928964L;

	public static Color colorBg = new Color(120, 120, 120);
	public static Color colorInnerBg = new Color(140, 140, 140);
	public static Color colorFg = new Color(30, 30, 30);
	public static Color colorTextAreaBg = new Color(160, 160, 160);

	public static String INTER = "inter";
	public static String OVER = "overall";

	// Performance objects
	protected AcceptFSM acceptFSM;
	private JPanel mainPanel = new JPanel();
	private JSplitPane splitPane = new JSplitPane();
	private JPanel menuPanel = new JPanel();
	private JPanel chartPanel = new JPanel();
	protected JPanel configurationPanel;
	protected ProgressPanel progressPanel;
	protected GUIPropertyInteger min = new GUIPropertyInteger("min", 60, 0, 100);
	protected GUIPropertyInteger max = new GUIPropertyInteger("max", 80, 0, 100);
	protected double overallMax = 0.0;
	protected double overallEdgeMax = 0.0;
	protected double pathMax = 0.0;

	protected FSMStatistics fsmStatistics;
	protected HashMap<String, DescriptiveStatistics> suSojournMap;
	protected HashMap<String, DescriptiveStatistics> suRemainingMap;
	protected HashMap<String, DescriptiveStatistics> edgeInterMap;
	protected HashMap<String, DescriptiveStatistics> suElapsedMap;
	protected HashSet<FSMState> criticalState;
	protected HashSet<ModelGraphEdge> criticalEdge;
	protected HashSet<FSMState> inbtwnState;
	protected HashSet<ModelGraphEdge> inbtwnEdge;
	protected GUIPropertyListEnumeration timeUnitSort;
	protected GUIPropertyListEnumeration measureSort;
	protected GUIPropertyListEnumeration colorBySort;

	public FSMPerformanceAnalysisUI(AcceptFSM fsm, FSMStatistics fsmStat) {
		acceptFSM = fsm;
		fsmStatistics = fsmStat;
		suSojournMap = fsmStatistics.getSojournMap();
		suRemainingMap = fsmStatistics.getRemainingMap();
		suElapsedMap = fsmStatistics.getElapsedMap();
		edgeInterMap = fsmStatistics.getEdgeMap();

		this.setLayout(new BorderLayout());
		this.setBackground(colorBg);
		this.removeAll();
		initGraphMenu();
		buildMainMenuGui();
	}

	public void initGraphMenu() {
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerSize(0);

		initTimeSort();
		menuPanel.add(timeUnitSort.getPropertyPanel());
		initMeasureSort();
		menuPanel.add(measureSort.getPropertyPanel());
		initColorBySort();
		menuPanel.add(colorBySort.getPropertyPanel());
		menuPanel.add(min.getPropertyPanel());
		menuPanel.add(max.getPropertyPanel());

		JButton updateButton = new AutoFocusButton("Update");
		updateButton.setOpaque(false);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateGUI();
				restoreAcceptFSM();
			}
		});
		menuPanel.add(updateButton);

		mainPanel.setBackground(colorBg);
		menuPanel.setBackground(colorBg);
		chartPanel.setBackground(colorBg);
		splitPane.setBackground(colorBg);

		chartPanel.setLayout(new BorderLayout());

		overallMax = 0.0;
		for (DescriptiveStatistics ds : getTimeMap().values()) {
			if (overallMax < getData(ds))
				overallMax = getData(ds);
		}

		overallEdgeMax = 0.0;
		for (DescriptiveStatistics ds : edgeInterMap.values()) {
			if (overallEdgeMax < getData(ds))
				overallEdgeMax = getData(ds);
		}
		findCriticalPath();
		adjustTimeScale();
		chartPanel.add(acceptFSM.getGrappaVisualization(), BorderLayout.CENTER);
		restoreAcceptFSM();
		splitPane.setLeftComponent(menuPanel);
		splitPane.setRightComponent(chartPanel);
	}

	public void buildMainMenuGui() {
		this.removeAll();
		this.add(splitPane, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}

	public void updateGUI() {
		overallMax = 0.0;
		for (DescriptiveStatistics ds : getTimeMap().values()) {
			if (overallMax < getData(ds))
				overallMax = getData(ds);
		}
		overallEdgeMax = 0.0;
		for (DescriptiveStatistics ds : edgeInterMap.values()) {
			if (overallEdgeMax < getData(ds))
				overallEdgeMax = getData(ds);
		}
		findCriticalPath();
		adjustTimeScale();
		splitPane.remove(chartPanel);
		chartPanel = null;
		chartPanel = new JPanel();
		chartPanel.setLayout(new BorderLayout());
		chartPanel.add(acceptFSM.getGrappaVisualization(), BorderLayout.CENTER);
		chartPanel.setBackground(colorBg);
		splitPane.setRightComponent(chartPanel);
	}

	protected void initMeasureSort() {
		ArrayList<String> measureList = new ArrayList<String>();
		measureList.add("Average");
		measureList.add("Sum");
		measureList.add("Minimum");
		measureList.add("Median");
		measureList.add("Maximum");
		measureList.add("StandDev");
		measureList.add("Variance");
		measureList.add("Frequency");
		measureSort = new GUIPropertyListEnumeration("Measure:", "",
				measureList, this, 150);
	}

	protected void initColorBySort() {
		ArrayList<String> colorByList = new ArrayList<String>();
		colorByList.add("Sojourn");
		colorByList.add("Remaining");
		colorByList.add("Elapsed");
		colorBySort = new GUIPropertyListEnumeration("Color By:", "",
				colorByList, this, 150);
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
				timeList, this, 150);
		timeUnitSort.setValue("hours");
	}

	protected long getTimeUnit() {
		if (((String) measureSort.getValue()).equals("Frequency")) {
			return 1;
		}
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

	protected double getData(DescriptiveStatistics ds) {
		String sort = (String) measureSort.getValue();
		if (sort.equals("Minimum")) {
			return ds.getMin();
		} else if (sort.equals("Average")) {
			return (ds.getMean());
		} else if (sort.equals("Median")) {
			return (ds.getPercentile(50));
		} else if (sort.equals("Maximum")) {
			return (ds.getMax());
		} else if (sort.equals("Sum")) {
			return (ds.getSum());
		} else if (sort.equals("StandDev")) {
			return (ds.getStandardDeviation());
		} else if (sort.equals("Variance")) {
			return (ds.getStandardDeviation() * ds.getStandardDeviation());
		} else if (sort.equals("Frequency")) {
			return (ds.getN());
		}
		return 0.0;
	}

	private HashMap<String, DescriptiveStatistics> getTimeMap() {
		if (colorBySort.getValue().equals("Sojourn")) {
			return suSojournMap;
		} else if (colorBySort.getValue().equals("Elapsed")) {
			return suElapsedMap;
		} else if (colorBySort.getValue().equals("Remaining")) {
			return suRemainingMap;
		}
		return null;
	}

	private void restoreAcceptFSM() {
		for (ModelGraphVertex as : acceptFSM.getVerticeList()) {
			((FSMState) as)
					.setLabel(getActualLabel(((FSMState) as).getLabel()));
			as.setDotAttribute("color", "black");
		}

		for (Object obj : acceptFSM.getEdges()) {
			ModelGraphEdge edg = (ModelGraphEdge) obj;
			edg.setDotAttribute("color", "black");
		}
	}

	private void adjustTimeScale() {
		for (ModelGraphVertex as : acceptFSM.getVerticeList()) {
			String key = getActualLabel(((FSMState) as).getLabel());

			if (getTimeMap().get(key) != null) {
				String value = String
						.valueOf("sojourn = "
								+ getSubString(String
										.valueOf(getData((DescriptiveStatistics) suSojournMap
												.get(key))
												/ (double) getTimeUnit())));
				value += String.valueOf("\\n elapsed = "
						+ getSubString(String.valueOf(getData(suElapsedMap
								.get(key))
								/ (double) getTimeUnit())));
				value += String.valueOf("\\n remaining = "
						+ getSubString(String.valueOf(getData(suRemainingMap
								.get(key))
								/ (double) getTimeUnit())));

				as.setDotAttribute("URL", value);
				as.setDotAttribute("tooltip", value);
				((FSMState) as).setLabel(key + "\\n " + value);
				if (getData(getTimeMap().get(key)) >= 0.0) {
					double tempValue = getData(getTimeMap().get(key));
					if (tempValue < overallMax
							* (((double) min.getValue()) / 100.0)) {
						as.setDotAttribute("color", "blue");
					} else if (tempValue < overallMax
							* (((double) max.getValue()) / 100.0)) {
						as.setDotAttribute("color", "yellow");
					} else {
						as.setDotAttribute("color", "red");
					}
				}
			}
		}

		for (Object obj : acceptFSM.getEdges()) {
			ModelGraphEdge edg = (ModelGraphEdge) obj;
			String start, end;
			start = getActualLabel(((FSMState) edg.getSource()).getLabel());
			end = getActualLabel(((FSMState) edg.getDest()).getLabel());
			String key = start + ":" + end;
			if (true) {
				if (criticalEdge.contains(edg)) {
					edg.setDotAttribute("color", "red");
				} else if (inbtwnEdge.contains(edg)) {
					edg.setDotAttribute("color", "yellow");
				} else {
					edg.setDotAttribute("color", "blue");
				}
			} else {
				if (edgeInterMap.get(key) != null) {
					double tempValue = getData(edgeInterMap.get(key));
					edg.setDotAttribute("tooltip", "interval = " + tempValue);
					if (tempValue < overallEdgeMax
							* (((double) min.getValue()) / 100.0)) {
						edg.setDotAttribute("color", "blue");
					} else if (tempValue < overallEdgeMax
							* (((double) max.getValue()) / 100.0)) {
						edg.setDotAttribute("color", "yellow");
					} else {
						edg.setDotAttribute("color", "red");
					}
				}
			}
		}
	}

	private String getActualLabel(String str) {
		return str.substring(0, str.lastIndexOf("]") + 1);
	}

	// private String getSubString(String str)
	// {
	// if(str.contains("E"));
	// return str;
	// // return str.substring(0, Math.min(str.length(), 10));
	// }

	private String getSubString(String str) {
		double d = Double.valueOf(str);
		if (d >= 0.000001 && d <= 100000 || d == 0)
			return str.substring(0, Math.min(str.length(), 7));
		;
		NumberFormat formatter = new DecimalFormat("0.#####E0");
		String s = formatter.format(d);
		return s;
	}

	private void findCriticalPath() {

		HashSet<FSMState> fsms = acceptFSM.getAcceptStates();

		pathMax = 0.0;
		for (FSMState st : fsms) {
			String key = getActualLabel((st).getLabel());
			if (suElapsedMap.get(key) != null) {
				pathMax = Math.max(pathMax, getData(suElapsedMap.get(key)));
			}
		}

		criticalState = new HashSet();
		criticalEdge = new HashSet<ModelGraphEdge>();
		inbtwnState = new HashSet();
		inbtwnEdge = new HashSet<ModelGraphEdge>();
		// for critical states
		for (FSMState st : fsms) {
			String key = getActualLabel((st).getLabel());
			if (suElapsedMap.get(key) != null) {
				if (getData(suElapsedMap.get(key)) >= pathMax
						* (((double) max.getValue()) / 100.0)) {
					criticalState.add(st);
					criticalEdge.addAll(getAllEdgesTo(st));
				}
			}
		}
		// for in-between states
		for (FSMState st : fsms) {
			String key = getActualLabel((st).getLabel());
			if (suElapsedMap.get(key) != null) {
				if (getData(suElapsedMap.get(key)) >= pathMax
						* (((double) min.getValue()) / 100.0)
						&& getData(suElapsedMap.get(key)) < pathMax
								* (((double) max.getValue()) / 100.0)) {
					inbtwnState.add(st);
					for (ModelGraphEdge edge : getAllEdgesTo(st)) {
						if (!criticalEdge.contains(edge))
							inbtwnEdge.add(edge);
					}
				}
			}
		}
	}

	public HashSet<ModelGraphEdge> getAllEdgesTo(ModelGraphVertex v2) {
		HashSet<ModelGraphEdge> s = new HashSet<ModelGraphEdge>();
		for (Object obj : acceptFSM.getEdges()) {
			ModelGraphEdge edge = (ModelGraphEdge) obj;
			if (isInPath(edge, v2))
				s.add(edge);
		}
		return s;
	}

	public boolean isInPath(ModelGraphVertex v1, ModelGraphVertex v2,
			HashSet<ModelGraphVertex> vs) {
		Iterator<ModelGraphEdge> it = v1.getOutEdgesIterator();
		while (it.hasNext()) {
			ModelGraphEdge e = it.next();
			if (e.getDest() == v1)
				continue;
			if (vs.contains(e.getDest()))
				return false;
			if (e.getDest() == v2) {
				return true;
			} else {
				vs.add(v1);
				if (e.getDest().getOutEdges() != null
						&& e.getDest().getOutEdges().size() > 0)
					if (isInPath(e.getDest(), v2, vs))
						return true;
			}
		}
		return false;
	}

	public boolean isInPath(ModelGraphEdge e1, ModelGraphVertex v2) {
		if (e1.getDest() == v2) {
			return true;
		} else {
			if (e1.getSource() != e1.getDest()
					&& e1.getDest().getOutEdges() != null
					&& e1.getDest().getOutEdges().size() > 0) {
				HashSet<ModelGraphVertex> vs = new HashSet<ModelGraphVertex>();
				return isInPath(e1.getDest(), v2, vs);
			}
		}
		return false;
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] {
				new ProvidedObject("FSM Statistics", fsmStatistics),
				new ProvidedObject("Sojourn Statistics", suSojournMap),
				new ProvidedObject("Remaining Statistics", suRemainingMap),
				new ProvidedObject("Elapsed Statistics", suElapsedMap),
				new ProvidedObject("Edge Statistics", edgeInterMap) };
	}
}
