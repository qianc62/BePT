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

package org.processmining.analysis.performance.fsmevaluator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

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
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

public class FSMEvaluationAnalysisUI extends JPanel implements
		GuiNotificationTarget, Provider {

	private static final long serialVersionUID = -4272000689166538512L;

	public static Color colorBg = new Color(120, 120, 120);
	public static Color colorInnerBg = new Color(140, 140, 140);
	public static Color colorFg = new Color(30, 30, 30);
	public static Color colorTextAreaBg = new Color(160, 160, 160);

	public static String INTER = "inter";
	public static String OVER = "overall";
	private static final String H1 = "h1";
	private static final String TR = "tr";
	private static final String TH = "th";
	private static final String TD = "td";

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
	protected double pathMax = 0.0;

	protected FSMEvaluationStatistics fsmStatistics;
	protected HashMap<String, DescriptiveStatistics> suMSEMap;
	protected HashMap<String, DescriptiveStatistics> suMAEMap;
	protected HashMap<String, DescriptiveStatistics> suMAPEMap;
	protected HashSet<FSMState> inbtwnState;
	protected HashSet<ModelGraphEdge> inbtwnEdge;
	protected GUIPropertyListEnumeration colorBySort;
	protected StringBuffer sb = new StringBuffer("<html><body><table><th>");

	public FSMEvaluationAnalysisUI(AcceptFSM fsm,
			FSMEvaluationStatistics fsmStat) {
		acceptFSM = fsm;
		fsmStatistics = fsmStat;
		suMSEMap = fsmStatistics.getMSEMap();
		suMAEMap = fsmStatistics.getMAEMap();
		suMAPEMap = fsmStatistics.getMAPEMap();

		sb.append(tag("Table view ", H1));
		sb.append("<table border=\"1\">");

		// write duration statistics table header
		StringBuffer tableHeader = new StringBuffer();
		tableHeader.append(tag("state", TH));
		tableHeader.append(tag("MAE", TH));
		tableHeader.append(tag("RMSE", TH));
		tableHeader.append(tag("MAPE", TH));
		tableHeader.append(tag("Freq", TH));
		sb.append(tag(tableHeader.toString(), TR));

		this.setLayout(new BorderLayout());
		this.setBackground(colorBg);
		this.removeAll();
		initGraphMenu();
		buildMainMenuGui();
	}

	public void initGraphMenu() {
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerSize(0);

		initColorBySort();
		menuPanel.add(colorBySort.getPropertyPanel());
		menuPanel.add(min.getPropertyPanel());
		menuPanel.add(max.getPropertyPanel());

		JButton updateButton = new AutoFocusButton("Update");
		updateButton.setOpaque(false);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateGUI();
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
			if (overallMax < ds.getSum())
				overallMax = ds.getSum();
		}
		adjustTimeScale();
		chartPanel.add(acceptFSM.getGrappaVisualization(), BorderLayout.CENTER);

		splitPane.setLeftComponent(menuPanel);
		splitPane.setRightComponent(chartPanel);
	}

	public void buildMainMenuGui() {
		// create configuration panel
		configurationPanel = new JPanel();
		configurationPanel.setBackground(colorTextAreaBg);
		configurationPanel.setForeground(colorFg);
		configurationPanel.setLayout(new BoxLayout(configurationPanel,
				BoxLayout.Y_AXIS));
		configurationPanel.setBorder(BorderFactory.createEmptyBorder());

		// add header
		configurationPanel = new JPanel();
		configurationPanel.setBorder(BorderFactory.createEmptyBorder());
		configurationPanel.setLayout(new BorderLayout());
		configurationPanel.add(splitPane, BorderLayout.CENTER);
		// set configuration panel as displayed
		configurationPanel.revalidate();
		this.removeAll();
		this.add(configurationPanel, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}

	public JScrollPane getStringBuffer() {
		JTextPane myTextPane = new JTextPane();
		myTextPane.setContentType("text/html");
		myTextPane.setText(sb.toString());
		myTextPane.setEditable(false);
		myTextPane.setCaretPosition(0);

		JScrollPane scrollPane = new JScrollPane(myTextPane);
		scrollPane.setBackground(colorBg);
		return scrollPane;
	}

	// helper method from the extended log summary - functionality will be moved
	// to the log summary at a later point in time
	private String tag(String s, String tag) {
		return "<" + tag + ">" + s + "</" + tag + ">";
	}

	public void updateGUI() {
		overallMax = 0.0;
		for (DescriptiveStatistics ds : getTimeMap().values()) {
			if (overallMax < ds.getSum())
				overallMax = ds.getSum();
		}
		adjustTimeScale();
		splitPane.remove(chartPanel);
		chartPanel = null;
		chartPanel = new JPanel();
		chartPanel.setLayout(new BorderLayout());
		chartPanel.add(acceptFSM.getGrappaVisualization(), BorderLayout.CENTER);
		chartPanel.setBackground(colorBg);
		splitPane.setRightComponent(chartPanel);
	}

	protected void initColorBySort() {
		ArrayList<String> colorByList = new ArrayList<String>();
		colorByList.add("MSE");
		colorBySort = new GUIPropertyListEnumeration("Color By:", "",
				colorByList, this, 150);
	}

	private HashMap<String, DescriptiveStatistics> getTimeMap() {
		if (colorBySort.getValue().equals("MSE")) {
			return suMSEMap;
		}
		return null;
	}

	private void adjustTimeScale() {
		for (ModelGraphVertex as : acceptFSM.getVerticeList()) {
			String key = getActualLabel(((FSMState) as).getLabel());

			if (getTimeMap().get(key) != null) {
				String value = String.valueOf("\\n MSE = "
						+ getSubString(String.valueOf(suMSEMap.get(key)
								.getMean())));
				value += String.valueOf("\\n Frequency = "
						+ String.valueOf(suMSEMap.get(key).getN()));
				StringBuffer tempBuffer = new StringBuffer();
				tempBuffer.append(tag(key, TD));
				tempBuffer.append(tag(getSubString(String.valueOf(suMAEMap.get(
						key).getMean())), TD));
				tempBuffer.append(tag(getSubString(String.valueOf(Math
						.sqrt(suMSEMap.get(key).getMean()))), TD));
				tempBuffer.append(tag(getSubString(String.valueOf(suMAPEMap
						.get(key).getMean())), TD));
				tempBuffer.append(tag(String.valueOf(suMAEMap.get(key).getN()),
						TD));
				sb.append(tag(tempBuffer.toString(), TR));

				as.setDotAttribute("URL", value);
				as.setDotAttribute("tooltip", value);
				((FSMState) as).setLabel(key + "\\n " + value);
				if (getTimeMap().get(key).getSum() >= 0.0) {
					double tempValue = getTimeMap().get(key).getSum();
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
		StringBuffer tempBuffer = new StringBuffer();
		tempBuffer.append(tag("overall(mean)", TD));
		tempBuffer.append(tag(getSubString(String.valueOf(fsmStatistics
				.getOverallMAE().getMean())), TD));
		tempBuffer.append(tag(getSubString(String.valueOf(fsmStatistics
				.getOverallRMSE().getMean())), TD));
		tempBuffer.append(tag(getSubString(String.valueOf(fsmStatistics
				.getOverallMAPE().getMean())), TD));
		tempBuffer.append(tag(String.valueOf(fsmStatistics.getOverallMAPE()
				.getN()), TD));
		sb.append(tag(tempBuffer.toString(), TR));
		tempBuffer = new StringBuffer();
		tempBuffer.append(tag("overall(aggregated mean)", TD));
		tempBuffer.append(tag(getSubString(String.valueOf(fsmStatistics
				.getOverallMAEAggre())), TD));
		tempBuffer.append(tag(getSubString(String.valueOf(fsmStatistics
				.getOverallRMSEAggre())), TD));
		tempBuffer.append(tag(getSubString(String.valueOf(fsmStatistics
				.getOverallMAPEAggre())), TD));
		tempBuffer.append(tag(String.valueOf(fsmStatistics.getNumber()), TD));
		sb.append(tag(tempBuffer.toString(), TR));
		sb.append("</table></body></html>");
		Message.add("Overall MAE value = "
				+ fsmStatistics.getOverallMAE().getSum(), Message.NORMAL);
		Message.add("Overall RMSE value = "
				+ fsmStatistics.getOverallRMSE().getSum(), Message.NORMAL);
		Message.add("Overall MAPE value = "
				+ fsmStatistics.getOverallMAPE().getSum(), Message.NORMAL);
	}

	private String getActualLabel(String str) {
		return str.substring(0, str.lastIndexOf("]") + 1);
	}

	// private String getSubString(String str)
	// {
	// return str.substring(0, Math.min(str.length(), 6));
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

	public HashSet<ModelGraphEdge> getAllEdgesTo(ModelGraphVertex v2) {
		HashSet<ModelGraphEdge> s = new HashSet<ModelGraphEdge>();
		for (Object obj : acceptFSM.getEdges()) {
			ModelGraphEdge edge = (ModelGraphEdge) obj;
			if (isInPath(edge, v2))
				s.add(edge);
		}
		return s;
	}

	public boolean isInPath(ModelGraphVertex v1, ModelGraphVertex v2) {
		Iterator<ModelGraphEdge> it = v1.getOutEdgesIterator();
		while (it.hasNext()) {
			ModelGraphEdge e = it.next();
			if (e.getDest() == v1)
				continue;
			if (e.getDest() == v2) {
				return true;
			} else {
				if (e.getDest().getOutEdges() != null
						&& e.getDest().getOutEdges().size() > 0)
					if (isInPath(e.getDest(), v2))
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
					&& e1.getDest().getOutEdges().size() > 0)
				return isInPath(e1.getDest(), v2);
		}
		return false;
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("Error Statistics",
				suMSEMap), };
	}
}
