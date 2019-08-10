package org.processmining.analysis.petrinet.structuredness;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.processmining.framework.models.bpel.util.Pair;
import org.processmining.framework.models.bpel.util.Triple;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.pattern.Component;
import org.processmining.framework.models.petrinet.pattern.PatternMatcher;
import org.processmining.framework.models.petrinet.pattern.log.Log;
import org.processmining.framework.models.petrinet.pattern.log.Reduction;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.importing.tpn.TpnImport;
import org.processmining.mining.petrinetmining.PetriNetResult;

import weka.gui.arffviewer.FileChooser;

public class BatchLog extends JTabbedPane implements ActionListener, Provider {

	private static final long serialVersionUID = 1614970092770615133L;

	private final List<StructurednessResult> results = new ArrayList<StructurednessResult>();

	private final Map<JButton, String> netNames = new LinkedHashMap<JButton, String>();

	private final Map<JButton, PetriNet> button2Component = new LinkedHashMap<JButton, PetriNet>();

	private final JPanel netPanel = new JPanel(new GridLayout(1, 1));

	private PetriNet selectedPetrinet;

	private JButton struct;

	private JButton cardoso;

	private JButton cyclomatic;

	private JScrollPane netScorePane;

	private List<StructurednessResult> netScoreResultList;

	public BatchLog() {
	}

	public void logResult(StructurednessResult result) {
		results.add(result);
	}

	public void prepare() {
		prepareSummary();
		prepareTotalScore();
		// prepareCorrelations();
		prepareStructurednessSummary();
	}

	private void prepareStructurednessSummary() {
		structureSummaryPane = new JTabbedPane();
		Pair<Map<String, Integer>, Integer> pair = getComponentOccurrences();
		Map<String, Integer> usedComponentCount = pair.first;
		int reductionsSize = pair.second;
		List<Pair<Reduction, Integer>> unstructuredUsedCount = getUnstructuredUsedCount();
		Pair<SortedMap<String, SortedMap<Integer, Integer>>, SortedMap<String, Double>> pair2 = getMatchDepts(usedComponentCount);

		final SortedMap<String, SortedMap<Integer, Integer>> matchDepths = pair2.first;
		final SortedMap<String, Double> fractionUsed = pair2.second;

		StringBuilder str = new StringBuilder();
		str.append("<html><body>");
		str.append("Number of different types of components matched: ");
		str.append(usedComponentCount.size());
		str
				.append("<h3>Stats</h3><table border=\"1\"><tr><th>Component</th><th>Times used</th><th>Percentage used</th><th>Average match depth</th><th>Depth used (0 top - 1 bottom)</th></tr>");
		for (String key : usedComponentCount.keySet()) {
			str.append("<tr><td>");
			str.append(key);
			str.append("</td><td align=\"center\">");
			str.append(usedComponentCount.get(key));
			str.append("</td><td align=\"center\">");
			String percent = round(100 * usedComponentCount.get(key)
					/ ((double) reductionsSize));
			str.append(percent);
			str.append("%</td><td align=\"center\">");
			str.append(averageDepth(matchDepths.get(key)));
			str.append("</td><td align=\"center\">");
			str.append(round(fractionUsed.get(key)));
			str.append("</td></tr>");
		}
		str.append("</table>");

		str.append("Unstructured components: " + unstructuredUsedCount.size());
		str
				.append("<table border=\"1\"><tr><th>Order</th><th>(|P|, |T|, |F|)</th><th>Times used</th><th>Percentage</th></tr>");
		int i = 1;
		for (Pair<Reduction, Integer> reduction : unstructuredUsedCount) {
			str.append("<tr><td align=\"center\">" + (i++) + "</td>");
			str.append("<td align=\"center\">("
					+ reduction.first.petriNet.numberOfPlaces() + ", "
					+ reduction.first.petriNet.numberOfTransitions() + ", "
					+ reduction.first.petriNet.getEdges().size() + ")</td>");
			str.append("<td align=\"center\">" + reduction.second + "</td>");
			str
					.append("<td align=\"center\">"
							+ round(100 * reduction.second
									/ (double) unstructuredUsedCount.size())
							+ "%</td>");
			str.append("</tr>");
		}
		str.append("</table>");
		str.append("</body></html>");

		JSplitPane unstructuredComponents = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT);
		unstructuredPanel = new JPanel(new GridLayout(1, 1));
		unstructuredComponents.add(unstructuredPanel, JSplitPane.RIGHT);
		JPanel componentPanel = new JPanel();
		unstructuredComponents.add(new JScrollPane(componentPanel),
				JSplitPane.LEFT);
		componentPanel
				.setLayout(new BoxLayout(componentPanel, BoxLayout.Y_AXIS));
		button2Component.clear();
		for (Pair<Reduction, Integer> component : unstructuredUsedCount) {
			JPanel entry = new JPanel(new FlowLayout());
			JButton button = new JButton("Show");
			button.addActionListener(this);
			entry.add(button);
			entry.add(new JLabel("("
					+ component.first.component.getWfnet().numberOfPlaces()
					+ ", "
					+ component.first.component.getWfnet()
							.numberOfTransitions() + ", "
					+ component.first.component.getWfnet().getEdges().size()
					+ "), occurrences " + component.second + ", score ("
					+ component.first.cardosoMetric + ","
					+ component.first.cyclomaticMetric + ","
					+ component.first.unstructuredSmResult + ")"));
			button2Component.put(button, component.first.component.getWfnet());
			componentPanel.add(entry);
		}

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(
				new JScrollPane(new JEditorPane("text/html", str.toString())),
				BorderLayout.CENTER);
		JPanel subpanel = new JPanel();
		panel.add(new JScrollPane(subpanel), BorderLayout.SOUTH);
		JButton button = new JButton("Copy all component scores");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final String componentScores = getAllComponentScores();
				Clipboard clipboard = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				clipboard.setContents(new StringSelection(componentScores),
						null);
			}
		});
		subpanel.add(button);

		button = new JButton("Copy component matches");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String str = getMatchesString(matchDepths);
				Clipboard clipboard = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				clipboard.setContents(new StringSelection(str), null);
			}

		});
		subpanel.add(button);
		// for (String name : usedComponentCount.keySet()) {
		// JButton button = new JButton("Copy " + name + "-component scores");
		// final String componentScores = getComponentScores(name);
		// button.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent arg0) {
		// Clipboard clipboard = Toolkit.getDefaultToolkit()
		// .getSystemClipboard();
		// clipboard.setContents(new StringSelection(componentScores),
		// null);
		// }
		// });
		// subpanel.add(button);
		// }

		structureSummaryPane.add("Summary", panel);
		structureSummaryPane.add("Matches", Log
				.createMatchesPanel(usedComponentCount));
		structureSummaryPane.add("Unstructured components",
				unstructuredComponents);

		add("Structuredness Reduction Summary", structureSummaryPane);
	}

	private String averageDepth(SortedMap<Integer, Integer> sortedMap) {
		double count = 0;
		for (Integer value : sortedMap.values()) {
			count += value;
		}
		double average = 0;
		for (Integer key : sortedMap.keySet()) {
			average += key * sortedMap.get(key) / count;
		}
		return round(average);
	}

	private String getMatchesString(
			SortedMap<String, SortedMap<Integer, Integer>> matchDepths) {
		StringBuilder str = new StringBuilder();
		str.append("Level\t");
		for (String key : matchDepths.keySet()) {
			str.append(key + "\t");
		}
		for (String key : matchDepths.keySet()) {
			str.append(key + "\t");
		}
		str.append("\n");
		Map<String, Integer> maxDepth = new LinkedHashMap<String, Integer>();
		for (String key : matchDepths.keySet()) {
			int max = 0;
			for (Integer i : matchDepths.get(key).keySet()) {
				Integer value = matchDepths.get(key).get(i);
				max += value;
			}
			maxDepth.put(key, max);
		}
		boolean more = true;
		for (int i = 0; more; i++) {
			more = false;
			str.append(i + "\t");
			for (String key : matchDepths.keySet()) {
				Integer value = matchDepths.get(key).get(i);
				if (value == null) {
					value = 0;
				}
				str.append(value + "\t");
				more = more || i < matchDepths.get(key).lastKey();
			}
			for (String key : matchDepths.keySet()) {
				Integer value = matchDepths.get(key).get(i);
				if (value == null) {
					value = 0;
				}
				double d = ((double) value) / maxDepth.get(key);
				str.append(d + "\t");
				more = more || i < matchDepths.get(key).lastKey();
			}
			str.append("\n");
		}
		return str.toString();
	}

	private Pair<SortedMap<String, SortedMap<Integer, Integer>>, SortedMap<String, Double>> getMatchDepts(
			Map<String, Integer> usedComponentCount) {
		SortedMap<String, SortedMap<Integer, Integer>> usedCount = new TreeMap<String, SortedMap<Integer, Integer>>();
		SortedMap<String, Double> depthUsedFractions = new TreeMap<String, Double>();
		for (StructurednessResult sr : results) {
			Map<String, Component> transition2Component = sr
					.getTransition2Component();
			Component component = transition2Component.get(sr.getWfnet()
					.getTransitions().get(0).getName());
			int maxDepth = recurseComponents(component, transition2Component,
					usedCount, 0);
			getDepthUsedFractions(component, transition2Component,
					depthUsedFractions, maxDepth, 0);
		}
		for (String key : usedComponentCount.keySet()) {
			double fraction = depthUsedFractions.get(key);
			int used = usedComponentCount.get(key);
			depthUsedFractions.put(key, fraction / used);
		}

		return Pair.create(usedCount, depthUsedFractions);
	}

	private void getDepthUsedFractions(Component component,
			Map<String, Component> transition2Component,
			SortedMap<String, Double> depthUsedFractions, int maxDepth,
			int depth) {
		if (component == null || maxDepth == 0)
			return;
		Double fraction = depthUsedFractions.get(component.toString());
		if (fraction == null) {
			fraction = 0.0;
		}
		fraction += ((double) depth) / maxDepth;
		depthUsedFractions.put(component.toString(), fraction);
		for (Transition transition : component.getWfnet().getTransitions()) {
			getDepthUsedFractions(transition2Component
					.get(transition.getName()), transition2Component,
					depthUsedFractions, maxDepth, depth + 1);
		}
	}

	private int recurseComponents(Component component,
			Map<String, Component> transition2Component,
			SortedMap<String, SortedMap<Integer, Integer>> result, int depth) {
		if (component == null)
			return depth - 1;
		SortedMap<Integer, Integer> series = result.get(component.toString());
		if (series == null) {
			series = new TreeMap<Integer, Integer>();
			result.put(component.toString(), series);
		}
		Integer value = series.get(depth);
		if (value == null) {
			series.put(depth, 1);
		} else {
			series.put(depth, value + 1);
		}
		int maxDepth = 0;
		for (Transition transition : component.getWfnet().getTransitions()) {
			int newDepth = recurseComponents(transition2Component
					.get(transition.getName()), transition2Component, result,
					depth + 1);
			if (newDepth > maxDepth)
				maxDepth = newDepth;
		}
		return maxDepth;
	}

	private String getAllComponentScores() {
		SortedMap<String, List<Triple<Integer, Integer, Double>>> lists = new TreeMap<String, List<Triple<Integer, Integer, Double>>>();
		for (StructurednessResult result : results) {
			for (Reduction reduction : result.getLog().getReductions()) {
				List<Triple<Integer, Integer, Double>> scores = lists
						.get(reduction.component.toString());
				if (scores == null) {
					scores = new ArrayList<Triple<Integer, Integer, Double>>();
					lists.put(reduction.component.toString(), scores);
				}
				scores.add(Triple.create(reduction.cardosoMetric,
						reduction.cyclomaticMetric,
						reduction.unstructuredSmResult));
			}
		}
		Map<String, Integer> sizes = new LinkedHashMap<String, Integer>();
		StringBuilder str = new StringBuilder();
		for (String key : lists.keySet()) {
			sizes.put(key, lists.get(key).size());
			str.append(key + "\t\t\tCorrelations\t\t\t");
		}
		str.append("\n");
		for (String key : lists.keySet()) {
			str.append("ECaM\tECyM\tSM\t\t\t\t");
		}
		str.append("\n");
		boolean more = false;
		for (int i = 0; more || i < 3; i++) {
			int j = 0;
			more = false;
			for (String key : lists.keySet()) {
				List<Triple<Integer, Integer, Double>> scores = lists.get(key);
				if (scores.isEmpty()) {
					str.append("\t\t\t");
				} else {
					Triple<Integer, Integer, Double> triple = scores.remove(0);
					str.append(triple.first + "\t" + triple.second + "\t"
							+ triple.third + "\t");
					more = true;
				}
				String a = "$" + getColumn(j);
				String b = "$" + getColumn(j + 1);
				String c = "$" + getColumn(j + 2);
				if (i == 0) {
					str.append("ECaM/ECyM\t=CORREL(OFFSET(" + a
							+ "$3,0,0,COUNT(" + a + ":" + a + "),1),OFFSET("
							+ b + "$3,0,0,COUNT(" + b + ":" + b + "),1))\t\t");
				} else if (i == 1) {
					str.append("ECaM/SM\t=CORREL(OFFSET(" + a + "$3,0,0,COUNT("
							+ a + ":" + a + "),1),OFFSET(" + c
							+ "$3,0,0,COUNT(" + c + ":" + c + "),1))\t\t");
				} else if (i == 2) {
					str.append("ECyM/SM\t=CORREL(OFFSET(" + b + "$3,0,0,COUNT("
							+ b + ":" + b + "),1),OFFSET(" + c
							+ "$3,0,0,COUNT(" + c + ":" + c + "),1))\t\t");
				} else {
					str.append("\t\t\t");
				}
				j += 6;
			}
			str.append("\n");
		}
		return str.toString();
	}

	private String getColumn(int i) {
		if (i <= 25) {
			return String.valueOf((char) (65 + i));
		}
		int rem = i / 25 - 1;
		return String.valueOf((char) (65 + rem)) + getColumn(i % 25 - 1);
	}

	private String getComponentScores(String name) {
		StringBuilder str = new StringBuilder();
		str.append(name + "\nECaM\tECyM\tSM\n");
		for (StructurednessResult result : results) {
			for (Reduction reduction : result.getLog().getReductions()) {
				if (reduction.component.toString().equals(name))
					str.append(reduction.cardosoMetric + "\t"
							+ reduction.cyclomaticMetric + "\t"
							+ reduction.unstructuredSmResult + "\n");
			}
		}
		return str.toString();
	}

	private static String round(double d) {
		return Double.toString(Math.round(100 * d) / 100.0);
	}

	private List<Pair<Reduction, Integer>> getUnstructuredUsedCount() {
		List<Pair<Reduction, Integer>> result = new ArrayList<Pair<Reduction, Integer>>();
		List<Reduction> components = new ArrayList<Reduction>();
		for (StructurednessResult sr : results) {
			for (Reduction reduction : sr.getLog().getReductions()) {
				if (reduction.component instanceof UnstructuredComponent)
					components.add(reduction);
			}
		}
		while (!components.isEmpty()) {
			Reduction component = components.remove(0);
			int usedCount = 1;
			for (Reduction anotherComponent : new ArrayList<Reduction>(
					components)) {
				if (PatternMatcher.getIsomorph(component.component.getWfnet(),
						anotherComponent.component.getWfnet()) != null) {
					usedCount++;
					components.remove(anotherComponent);
				}
			}
			result.add(Pair.create(component, usedCount));
		}

		Collections.sort(result, new Comparator<Pair<Reduction, Integer>>() {
			public int compare(Pair<Reduction, Integer> arg0,
					Pair<Reduction, Integer> arg1) {
				int i = arg1.second.compareTo(arg0.second);
				if (i != 0)
					return i;
				return new Integer(arg0.first.component.getWfnet()
						.numberOfNodes()
						+ arg0.first.component.getWfnet().getEdges().size())
						.compareTo(new Integer(arg1.first.component.getWfnet()
								.numberOfNodes()
								+ arg1.first.component.getWfnet().getEdges()
										.size()));
			}
		});

		return result;
	}

	private Pair<Map<String, Integer>, Integer> getComponentOccurrences() {
		Map<String, Integer> count = new TreeMap<String, Integer>(
				new Comparator<String>() {
					public int compare(String arg0, String arg1) {
						return arg0.compareTo(arg1);
					}
				});
		int reductions = 0;
		for (StructurednessResult sr : results) {
			Log log = sr.getLog();
			Map<String, Integer> usedComponentCount = log
					.getUsedComponentCount();
			for (String key : usedComponentCount.keySet()) {
				Integer i = count.get(key);
				if (i == null) {
					count.put(key, usedComponentCount.get(key));
				} else {
					count.put(key, i + usedComponentCount.get(key));
				}
			}
			reductions += log.getReductions().size();
		}
		return Pair.create(count, reductions);
	}

	private final Comparator<Double> leq = new Comparator<Double>() {
		public int compare(Double arg0, Double arg1) {
			return arg0.compareTo(arg1);
		}
	};

	private JPanel unstructuredPanel;

	private JTabbedPane structureSummaryPane;

	private JTabbedPane scorePane;

	private void prepareSummary() {
		int spread = 5;
		JTabbedPane subpane = new JTabbedPane();
		List<Double> places = new ArrayList<Double>(), transitions = new ArrayList<Double>(), edges = new ArrayList<Double>();
		List<Double> placesStat = new ArrayList<Double>(), transitionsStat = new ArrayList<Double>(), edgesStat = new ArrayList<Double>();
		for (StructurednessResult r : results) {
			places.add((double) r.getOriginalNet().numberOfPlaces());
			transitions.add((double) r.getOriginalNet().numberOfTransitions());
			edges.add((double) r.getOriginalNet().getEdges().size());
			placesStat.add((double) r.getOriginalNet().numberOfPlaces());
			transitionsStat.add((double) r.getOriginalNet()
					.numberOfTransitions());
			edgesStat.add((double) r.getOriginalNet().getEdges().size());
		}
		Collections.sort(places, leq);
		Collections.sort(transitions, leq);
		Collections.sort(edges, leq);

		List<Pair<String, List<Double>>> input = new ArrayList<Pair<String, List<Double>>>();
		input.add(Pair.create("Places", placesStat));
		input.add(Pair.create("Transitions", transitionsStat));
		input.add(Pair.create("Edges", edgesStat));
		java.awt.Component pane = generateStatistics("Processed nets: <b>"
				+ results.size() + "</b><br>" + "Statistics on the nets:<br>",
				input);

		subpane.add("Statistics", pane);
		subpane.add("Places", new SpreadPanel("Places", spread, places));
		subpane.add("Transitions", new SpreadPanel("Transitions", spread,
				transitions));
		subpane.add("Edges", new SpreadPanel("Edges", spread, edges));

		add("Nets", subpane);
	}

	private static java.awt.Component generateStatistics(String header,
			final List<Pair<String, List<Double>>> list) {
		int N = list.get(0).second.size();
		List<Double> totals = new ArrayList<Double>();
		for (Pair<?, List<Double>> pair : list)
			totals.add(sum(pair.second));
		List<Double> means = new ArrayList<Double>();
		for (Double total : totals)
			means.add(total / N);
		List<Double> variances = new ArrayList<Double>();
		int index = 0;
		for (Pair<?, List<Double>> pair : list)
			variances.add(calculateVariance(pair.second, means.get(index++)));
		List<Double> standardDeviations = new ArrayList<Double>();
		for (Double variance : variances)
			standardDeviations.add(Math.sqrt(variance));
		List<String> fns = new ArrayList<String>();
		for (Pair<?, List<Double>> pair : list)
			fns.add(calculateFiveNumberSummary(pair.second));
		List<Double> standardErrorOfTheMean = new ArrayList<Double>();
		for (Double sd : standardDeviations)
			standardErrorOfTheMean.add(sd / Math.sqrt(N));
		List<String> confidenceIntervals = new ArrayList<String>();
		index = 0;
		for (Double mean : means) {
			double tmp = 2 * standardErrorOfTheMean.get(index++);
			confidenceIntervals.add("[" + round(mean - tmp) + "; "
					+ round(mean + tmp) + "]");
		}
		Map<Pair<String, String>, String> correlationCoefficients = new LinkedHashMap<Pair<String, String>, String>();
		int index1 = 0;
		for (Pair<String, List<Double>> pair1 : list) {
			int index2 = 0;
			for (Pair<String, List<Double>> pair2 : list) {
				if (index1 != index2) {
					double covariance = calculateCovariance(pair1.second,
							pair2.second, means.get(index1), means.get(index2));
					double cc = covariance
							/ (standardDeviations.get(index1) * standardDeviations
									.get(index2));
					correlationCoefficients.put(Pair.create(pair1.first,
							pair2.first), round(cc));
				}
				index2++;
			}
			index1++;
		}
		List<String> correlationPairs = new ArrayList<String>();
		index1 = 0;
		for (Pair<String, List<Double>> pair1 : list) {
			int index2 = 0;
			StringBuilder str = new StringBuilder("<ul>");
			for (Pair<String, List<Double>> pair2 : list) {
				if (index1 != index2) {
					str.append("<li>"
							+ pair1.first
							+ ", "
							+ pair2.first
							+ ": "
							+ correlationCoefficients.get(Pair.create(
									pair1.first, pair2.first)) + "</li>");
				}
				index2++;
			}
			index1++;
			str.append("</ul>");
			correlationPairs.add(str.toString());
		}

		StringBuilder summary = new StringBuilder("<html><head></head><body>");
		if (header != null)
			summary.append(header);
		summary.append("<table width=\"100%\" border=\"1\"><tr><th></th>");
		for (Pair<String, ?> pair : list)
			summary.append("<th>" + pair.first + "</th>");
		summary.append("</tr>");
		printRow(summary, "Sum", totals);
		printRow(summary, "Mean", means);
		printRow(summary, "Variance", variances);
		printRow(summary, "Standard deviation", standardDeviations);
		// printRow(summary, "Five-number summary", fns);
		printRow(summary, "Standard error of the mean", standardErrorOfTheMean);
		printRow(summary, "Confidence interval (at a 95% level)",
				confidenceIntervals);
		printRow(summary, "Correlation coefficents", correlationPairs);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(new JEditorPane("text/html", summary
				.toString())), BorderLayout.CENTER);
		JPanel bottom = new JPanel();
		panel.add(bottom, BorderLayout.SOUTH);
		JButton button = new JButton("Export raw data");
		bottom.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FileChooser fc = new FileChooser();
				fc.setFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".csv");
					}

					@Override
					public String getDescription() {
						return "Comma-separated values (csv)";
					}
				});
				int returnVal = fc.showSaveDialog(MainUI.getInstance());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (!file.getName().endsWith(".csv"))
						file = new File(file.getAbsolutePath() + ".csv");
					try {
						BufferedWriter out = new BufferedWriter(new FileWriter(
								file));
						out.write(exportRawData(list, ","));
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		button = new JButton("Copy raw data");
		bottom.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Clipboard clipboard = Toolkit.getDefaultToolkit()
						.getSystemClipboard();
				clipboard.setContents(new StringSelection(exportRawData(list,
						"\t")), null);
			}
		});
		return panel;
	}

	private static String exportRawData(List<Pair<String, List<Double>>> list,
			String separator) {
		StringBuilder out = new StringBuilder();
		int index = 0;
		for (Pair<String, ?> pair : list) {
			out.append(pair.first);
			if (index < list.size() - 1)
				out.append(separator);
			index++;
		}
		out.append("\n");
		for (int i = 0; i < list.get(0).second.size(); i++) {
			index = 0;
			for (Pair<?, List<Double>> pair : list) {
				out.append(pair.second.get(i).toString());
				if (index < list.size() - 1)
					out.append(separator);
			}
			out.append("\n");
			index++;
		}
		return out.toString();
	}

	private static double calculateCovariance(List<Double> list1,
			List<Double> list2, Double mean1, Double mean2) {
		double result = 0;
		for (int i = 0; i < list1.size(); i++) {
			result += (list1.get(i) - mean1) * (list2.get(i) - mean2);
		}
		result = result / list1.size();
		return result;
	}

	private static void printRow(StringBuilder summary, String label,
			List<? extends Object> values) {
		summary.append("<tr><td align=\"center\">" + label + "</td>");
		for (Object value : values) {
			String s;
			if (value instanceof Double)
				s = round((Double) value);
			else
				s = value.toString();
			summary.append("<td align=\"center\">" + s + "</td>");
		}
		summary.append("</tr>");
	}

	private static String calculateFiveNumberSummary(List<Double> values) {
		if (values.size() == 0)
			return "No points";
		if (values.size() <= 5) {
			String result = "" + values.get(0);
			for (int i = 1; i < values.size(); i++)
				result += ", " + round(values.get(i));
			return result;
		}
		double q1, q2, q3, q4, q5;
		q1 = values.get(0);
		q5 = values.get(values.size() - 1);
		if (values.size() % 2 == 0) {
			q2 = (values.get((values.size() / 2 + 1) / 2) + values.get((values
					.size() / 2 + 1) / 2 + 1)) / 2;
			q3 = (values.get(values.size() / 2 - 1) + values
					.get(values.size() / 2)) / 2;
			q4 = (values.get(values.size() / 2 + (values.size() / 2 + 1) / 2) + values
					.get(values.size() / 2 + (values.size() / 2 + 1) / 2 + 1)) / 2;
		} else {
			q2 = (values.get((values.size() / 2) / 2) + values.get((values
					.size() / 2) / 2 + 1)) / 2;
			q3 = values.get(values.size() / 2);
			q4 = (values.get(values.size() / 2 + (values.size() / 2) / 2) + values
					.get(values.size() / 2 + (values.size() / 2) / 2 + 1)) / 2;
		}
		return round(q1) + ", " + round(q2) + ", " + round(q3) + ", "
				+ round(q4) + ", " + round(q5);
	}

	private static double sum(List<Double> doubles) {
		double result = 0;
		for (double d : doubles)
			result += d;
		return result;
	}

	private static double calculateVariance(List<Double> doubles, double mean) {
		double result = 0;
		for (Double d : doubles)
			result += Math.pow(d - mean, 2);
		result = result / doubles.size();
		return result;
	}

	private java.awt.Component prepareNetScoreList() {
		JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		netScorePane = new JScrollPane();
		JPanel topLeftlist = new JPanel(new BorderLayout());
		netScoreResultList = new ArrayList<StructurednessResult>(results);
		updateNetScoreList();
		topLeftlist.add(netScorePane, BorderLayout.CENTER);
		panel.add(topLeftlist, JSplitPane.LEFT);
		panel.add(netPanel, JSplitPane.RIGHT);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(new JLabel("Sort by: "));
		struct = new JButton("Structureness");
		cardoso = new JButton("Cardoso");
		cyclomatic = new JButton("Cyclomatic");
		buttonPanel.add(struct);
		buttonPanel.add(cardoso);
		buttonPanel.add(cyclomatic);
		topLeftlist.add(buttonPanel, BorderLayout.SOUTH);
		struct.addActionListener(this);
		cardoso.addActionListener(this);
		cyclomatic.addActionListener(this);

		return panel;
	}

	private void updateNetScoreList() {
		netNames.clear();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		for (StructurednessResult r : netScoreResultList) {
			JPanel net = new JPanel(new FlowLayout());
			JButton button = new JButton("Show");
			button.addActionListener(this);
			netNames.put(button, r.getPath());
			net.add(button);
			net.add(new JLabel(r.getNetName() + ": <" + r.getMyMetric() + ","
					+ r.getCardosoMetric() + "," + r.getCyclomaticMetric()
					+ ">"));
			panel.add(net);
		}
		netScorePane.setViewportView(panel);
	}

	private Pair<java.awt.Component, java.awt.Component> prepareSpreads() {
		int spread = 5;
		List<Double> structuredness = new ArrayList<Double>(), cardoso = new ArrayList<Double>(), cyclomatic = new ArrayList<Double>();
		List<Double> structurednessStat = new ArrayList<Double>(), cardosoStat = new ArrayList<Double>(), cyclomaticStat = new ArrayList<Double>();
		for (StructurednessResult sr : results) {
			structuredness.add((double) sr.getMyMetric());
			cardoso.add((double) sr.getCardosoMetric());
			cyclomatic.add((double) sr.getCyclomaticMetric());
			structurednessStat.add((double) sr.getMyMetric());
			cardosoStat.add((double) sr.getCardosoMetric());
			cyclomaticStat.add((double) sr.getCyclomaticMetric());
		}
		Collections.sort(structuredness, leq);
		Collections.sort(cardoso, leq);
		Collections.sort(cyclomatic, leq);

		JTabbedPane subpane = new JTabbedPane();
		subpane.add("Cardoso", new SpreadPanel("Cardoso", spread, cardoso));
		subpane.add("Cyclomatic", new SpreadPanel("Cyclomatic", spread,
				cyclomatic));
		subpane.add("Structureness", new SpreadPanel("Structureness", spread,
				structuredness));

		List<Pair<String, List<Double>>> list = new ArrayList<Pair<String, List<Double>>>();
		list.add(Pair.create("Cardoso", cardosoStat));
		list.add(Pair.create("Cyclomatic", cyclomaticStat));
		list.add(Pair.create("Structuredness", structurednessStat));

		return Pair.create((java.awt.Component) subpane,
				(java.awt.Component) generateStatistics(null, list));
	}

	private void prepareCorrelations() {
		int spread = 5;
		List<Double> cardosoVSstruct = new ArrayList<Double>(), cardosoVScyclo = new ArrayList<Double>(), cycloVSstruct = new ArrayList<Double>();
		List<Double> cardosoVSstructStat = new ArrayList<Double>(), cardosoVScycloStat = new ArrayList<Double>(), cycloVSstructStat = new ArrayList<Double>();
		for (StructurednessResult sr : results) {
			cardosoVSstruct.add(sr.getCardosoMetric()
					/ (double) sr.getMyMetric());
			cardosoVScyclo.add(sr.getCardosoMetric()
					/ (double) sr.getCyclomaticMetric());
			cycloVSstruct.add(sr.getCyclomaticMetric()
					/ (double) sr.getMyMetric());
			cardosoVSstructStat.add(sr.getCardosoMetric()
					/ (double) sr.getMyMetric());
			cardosoVScycloStat.add(sr.getCardosoMetric()
					/ (double) sr.getCyclomaticMetric());
			cycloVSstructStat.add(sr.getCyclomaticMetric()
					/ (double) sr.getMyMetric());
		}
		Collections.sort(cardosoVSstruct, leq);
		Collections.sort(cardosoVScyclo, leq);
		Collections.sort(cycloVSstruct, leq);

		JTabbedPane subpane = new JTabbedPane();

		List<Pair<String, List<Double>>> list = new ArrayList<Pair<String, List<Double>>>();

		list.add(Pair.create("Cardoso / Cyclomatic", cardosoVScycloStat));
		list.add(Pair.create("Cardoso / Structuredness", cardosoVSstructStat));
		list.add(Pair.create("Cyclomatic / Structuredness", cycloVSstructStat));
		subpane.add("Statistics", generateStatistics(null, list));
		subpane.add("Cardoso / Cyclomatic", new SpreadPanel(
				"Cardoso / Cyclomatic", spread, cardosoVScyclo));
		subpane.add("Cardoso / Structureness", new SpreadPanel(
				"Cardoso / Structureness", spread, cardosoVSstruct));
		subpane.add("Cyclomatic / Structureness", new SpreadPanel(
				"Cyclomatic / Structureness", spread, cycloVSstruct));

		add("Correlations", subpane);
	}

	private void prepareTotalScore() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (StructurednessResult result : results) {
			dataset.addValue(result.getCardosoMetric(), "Cardoso metric",
					result.getNetName());
			dataset.addValue(result.getCyclomaticMetric(), "Cyclomatic metric",
					result.getNetName());
			dataset.addValue(result.getMyMetric(), "Structuredness metric",
					result.getNetName());
		}
		JFreeChart matches = ChartFactory.createBarChart("", // chart title
				"Nets", // domain axis label
				"Score", // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);
		matches.getCategoryPlot().getRenderer().setItemLabelGenerator(
				new CategoryItemLabelGenerator() {

					public String generateColumnLabel(CategoryDataset dataset,
							int column) {
						return null;
					}

					public String generateLabel(CategoryDataset dataset,
							int row, int column) {
						switch (row) {
						case 2:
							return "" + results.get(column).getMyMetric();
						case 0:
							return "" + results.get(column).getCardosoMetric();
						case 1:
							return ""
									+ results.get(column).getCyclomaticMetric();
						}
						return null;
					}

					public String generateRowLabel(CategoryDataset dataset,
							int row) {
						return null;
					}
				});
		matches.getCategoryPlot().getRenderer().setItemLabelsVisible(true);
		// setCategoryChartProperties(matches);
		ChartPanel chartPanel = new ChartPanel(matches, false);

		scorePane = new JTabbedPane();
		Pair<java.awt.Component, java.awt.Component> pair = prepareSpreads();
		scorePane.add("Statistics", pair.second);
		scorePane.add("List", prepareNetScoreList());
		scorePane.add("Graph", new JScrollPane(chartPanel));
		scorePane.add("Spreads", pair.first);

		add("Scores", scorePane);
	}

	public void actionPerformed(ActionEvent arg0) {
		String name = netNames.get(arg0.getSource());
		PetriNet net = button2Component.get(arg0.getSource());
		if (name != null) {
			if (name.endsWith(".tpn")) {
				try {
					PetriNetResult result = new TpnImport()
							.importFile(new FileInputStream(name));
					selectedPetrinet = result.getPetriNet();
					netPanel.removeAll();
					netPanel.add(result.getVisualization());
					validate();
					repaint();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (net != null) {
			unstructuredPanel.removeAll();
			unstructuredPanel.add(new PetriNetResult(net).getVisualization());
			selectedPetrinet = net;
			validate();
			repaint();
		} else if (arg0.getSource() == struct) {
			Collections.sort(netScoreResultList,
					new Comparator<StructurednessResult>() {
						public int compare(StructurednessResult arg0,
								StructurednessResult arg1) {
							return (int) (arg0.getMyMetric() - arg1
									.getMyMetric());
						}
					});
			updateNetScoreList();
		} else if (arg0.getSource() == cardoso) {
			Collections.sort(netScoreResultList,
					new Comparator<StructurednessResult>() {
						public int compare(StructurednessResult arg0,
								StructurednessResult arg1) {
							return arg0.getCardosoMetric()
									- arg1.getCardosoMetric();
						}
					});
			updateNetScoreList();
		} else if (arg0.getSource() == cyclomatic) {
			Collections.sort(netScoreResultList,
					new Comparator<StructurednessResult>() {
						public int compare(StructurednessResult arg0,
								StructurednessResult arg1) {
							return arg0.getCyclomaticMetric()
									- arg1.getCyclomaticMetric();
						}
					});
			updateNetScoreList();
		}
	}

	public ProvidedObject[] getProvidedObjects() {
		int index = getSelectedIndex();
		if (((index == 1 && scorePane.getSelectedIndex() == 1) || (index == 3 && structureSummaryPane
				.getSelectedIndex() == 2))
				&& selectedPetrinet != null) {
			return new ProvidedObject[] { new ProvidedObject(
					"Selected Petri net", new Object[] { selectedPetrinet }) };
		}
		return new ProvidedObject[] {};
	}

}
