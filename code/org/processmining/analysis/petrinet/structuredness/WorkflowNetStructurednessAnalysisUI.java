package org.processmining.analysis.petrinet.structuredness;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;

import org.jdom.Namespace;
import org.processmining.converting.wfnet2bpel.pattern.BPELPatternMatcher;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.bpel.util.Pair;
import org.processmining.framework.models.bpel.util.Quadruple;
import org.processmining.framework.models.bpel.util.Triple;
import org.processmining.framework.models.petrinet.Choice;
import org.processmining.framework.models.petrinet.Marking;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.PetriNetNavigation;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.State;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.CoverabilityGraphBuilder;
import org.processmining.framework.models.petrinet.algorithms.InitialPlaceMarker;
import org.processmining.framework.models.petrinet.algorithms.PnmlReader;
import org.processmining.framework.models.petrinet.pattern.ChoiceComponent;
import org.processmining.framework.models.petrinet.pattern.Component;
import org.processmining.framework.models.petrinet.pattern.ComponentDescription;
import org.processmining.framework.models.petrinet.pattern.ExplicitChoiceComponent;
import org.processmining.framework.models.petrinet.pattern.ImplicitChoiceComponent;
import org.processmining.framework.models.petrinet.pattern.LibraryComponent;
import org.processmining.framework.models.petrinet.pattern.MarkedGraphComponent;
import org.processmining.framework.models.petrinet.pattern.MatchingOrder;
import org.processmining.framework.models.petrinet.pattern.NodeHash;
import org.processmining.framework.models.petrinet.pattern.PatternMatcher;
import org.processmining.framework.models.petrinet.pattern.SequenceComponent;
import org.processmining.framework.models.petrinet.pattern.StateMachineComponent;
import org.processmining.framework.models.petrinet.pattern.WellStructuredGraphComponent;
import org.processmining.framework.models.petrinet.pattern.WhileComponent;
import org.processmining.framework.models.petrinet.pattern.log.Log;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.importing.tpn.TpnImport;

import weka.gui.arffviewer.FileChooser;
import att.grappa.Edge;
import att.grappa.Node;

public class WorkflowNetStructurednessAnalysisUI extends JComponent implements
		ActionListener, Provider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1967664913733346543L;

	private final PetriNet net;

	private final JPanel settingsPanel;

	private final JButton back, proceed, batch;

	private static String matchingOrderXML = "lib/plugins/Workflow Net Structuredness Analysis/matching-order.xml";

	private static String matchingOrderXSD = "lib/plugins/Workflow Net Structuredness Analysis/matching-order.xsd";

	private static String matchingOrderFolder = "lib/plugins/Workflow Net Structuredness Analysis";

	private final MatchingOrder matchingOrder;

	private JPanel buttonPanel;

	private static final int INFINITE_SS = -1, UNFINISHED_CG = -2;

	private static final String[] template = new String[] { "Sequence",
			"Explicit choice", "Implicit choice", "While", "State machine",
			"Marked graph", "Well-structured" };

	private static final Namespace namespace = Namespace
			.getNamespace("http://www.processmining.org/wfnet-structuredness/matching-order");

	private static String batchDir = null;

	private BatchLog currentBatchLog;

	private Log currentLog;

	// private JCheckBox removeDummyPlaces;

	private JCheckBox pumpTTComponents;

	public WorkflowNetStructurednessAnalysisUI(PetriNet net) {
		this.net = net;

		matchingOrder = new MatchingOrder(true, true, true, matchingOrderXML,
				matchingOrderXSD, template, namespace, new String[] { "pnml" });

		// START: Initialize view

		// START: Main component - Buttons

		back = new JButton("Back");
		back.setEnabled(false);
		back.addActionListener(this);
		proceed = new JButton("Proceed");
		proceed.setEnabled(matchingOrder.exists());
		proceed.addActionListener(this);
		batch = new JButton("Batch analysis");
		batch.setEnabled(true);
		batch.addActionListener(this);
		buttonPanel = new JPanel();
		buttonPanel.add(back);
		buttonPanel.add(proceed);
		buttonPanel.add(batch);

		// END: Main component - Buttons

		// START: Settings

		// START: Library components

		// END: Library components

		settingsPanel = new JPanel();
		settingsPanel.setLayout(new BoxLayout(settingsPanel,
				BoxLayout.PAGE_AXIS));
		settingsPanel.add(matchingOrder);

		JPanel rewriteRules = new JPanel();
		rewriteRules.setLayout(new BoxLayout(rewriteRules, BoxLayout.Y_AXIS));
		// removeDummyPlaces = new JCheckBox("Remove dummy places", true);
		pumpTTComponents = new JCheckBox("Pump TT-components", false);
		// rewriteRules.add(removeDummyPlaces);
		rewriteRules.add(pumpTTComponents);
		// settingsPanel.add(rewriteRules);

		// END: Welcome

		// START: Main component - Layout

		setLayout(new BorderLayout());
		JPanel overall = new JPanel(new BorderLayout());
		overall.add(settingsPanel, BorderLayout.CENTER);
		overall.add(buttonPanel, BorderLayout.SOUTH);
		add(overall, BorderLayout.CENTER);
		overall.validate();
		overall.repaint();

		// END: Main component - Layout

		setVisible(true);

		// END: Initialize view
	}

	protected void doProceed() {
		final ProgressMonitor progress = new ProgressMonitor(MainUI
				.getInstance(), null, "Working...", 0, 2);
		progress.setMillisToDecideToPopup(1);
		progress.setMillisToPopup(1);
		Thread workerThread = new Thread() {
			public void run() {
				progress.setProgress(1);
				proceed.setEnabled(false);
				batch.setEnabled(false);
				try {
					StructurednessResult result = checkStructuredness(progress,
							net.getIdentifier(), net, null);
					progress.setProgress(2);
					showMetric(result, result.getLog(), result
							.getTransition2Component());
					back.setEnabled(true);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		workerThread.start();
	}

	public synchronized void actionPerformed(ActionEvent e) {
		if (e.getSource() == proceed) {
			doProceed();
		} else if (e.getSource() == back) {
			back.setEnabled(false);
			removeAll();
			add(settingsPanel, BorderLayout.CENTER);
			add(buttonPanel, BorderLayout.SOUTH);
			proceed.setEnabled(true);
			batch.setEnabled(true);
			validate();
			repaint();
			currentLog = null;
			currentBatchLog = null;
		} else if (e.getSource() == batch) {
			FileChooser fc = new FileChooser(batchDir);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(MainUI.getInstance());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File dir = fc.getSelectedFile();
				batchDir = dir.getParent();
				Queue<File> dirs = new LinkedList<File>();
				List<File> files = new ArrayList<File>();
				dirs.add(dir);
				while (!dirs.isEmpty()) {
					dir = dirs.remove();
					for (File file : dir.listFiles()) {
						if (file.isDirectory())
							dirs.add(file);
						else if (file.getName().endsWith(".pnml")
								|| file.getName().endsWith(".tpn"))
							files.add(file);
					}
				}
				proceed.setEnabled(false);
				batch.setEnabled(false);
				batchStructureness(batchDir, files);
			}
		}
	}

	private void showBatchLog(BatchLog batchLog) {
		removeAll();
		batchLog.prepare();
		add(batchLog, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		validate();
		repaint();
	}

	protected void batchStructureness(final String path, final List<File> files) {
		final ProgressMonitor progress = new ProgressMonitor(MainUI
				.getInstance(), "Analyzing " + files.size() + " files.",
				"Completed: 0%", 0, files.size());
		Thread workerThread = new Thread() {
			public void run() {
				BatchLog batchLog = new BatchLog();
				currentBatchLog = batchLog;
				int count = 1;
				for (File file : files) {
					System.err.println("Importing: " + file.getAbsolutePath());
					PetriNet net = null;
					if (file.getName().endsWith(".pnml")) {
						try {
							net = new PnmlReader().read(new FileInputStream(
									file));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (file.getName().endsWith(".tpn")) {
						try {
							net = new TpnImport().importFile(
									new FileInputStream(file)).getPetriNet();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (net != null) {
						try {
							StructurednessResult result = checkStructuredness(
									progress, file.getAbsolutePath().substring(
											path.length() + 1), net, file
											.getAbsolutePath());
							batchLog.logResult(result);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (progress.isCanceled()) {
						break;
					}
					progress.setProgress(count);
					progress
							.setNote("Completed: "
									+ (Math.round((count / (double) files
											.size()) * 1000) / 10) + "%");
					count++;
					MainUI.getInstance().repaint();
				}
				currentLog = null;
				if (progress.isCanceled()) {
					proceed.setEnabled(true);
					batch.setEnabled(true);
				} else {
					progress.close();
					showBatchLog(batchLog);
					back.setEnabled(true);
				}
			}
		};
		workerThread.start();
	}

	protected StructurednessResult checkStructuredness(
			ProgressMonitor progress, String netName, PetriNet net, String path)
			throws FileNotFoundException, Exception {
		Pair<Integer, List<Pair<Place, Set<Set<Place>>>>> tmpResult1 = calculateCardosoMetric(net);
		int cardosoMetric = tmpResult1.first;
		List<Pair<Place, Set<Set<Place>>>> cardosoCalculation = tmpResult1.second;
		Pair<Integer, Quadruple<StateSpace, Integer, Integer, List<List<ModelGraphVertex>>>> tmpResult2 = calculateCyclomaticMetric(net);
		int cyclomaticMetric = tmpResult2.first;
		Quadruple<StateSpace, Integer, Integer, List<List<ModelGraphVertex>>> cyclomaticCalculation = tmpResult2.second;
		Component component = null;
		PetriNet wfnet = (PetriNet) net.clone();
		Map<String, Choice> choices = new LinkedHashMap<String, Choice>();
		for (Place place : wfnet.getPlaces())
			choices.put(place.getName(), NodeHash.getChoice(place));
		Log log = new Log(true);
		currentLog = log;
		Map<String, Double> annotations = new LinkedHashMap<String, Double>();
		for (Transition transition : wfnet.getTransitions())
			annotations.put(transition.getName(), 1.0);
		Map<String, Component> transition2Component = new LinkedHashMap<String, Component>();
		// Map<String, Double> dummyAnnotations = new LinkedHashMap<String,
		// Double>(
		// annotations);
		while (!isTrivial(wfnet)
				&& (component = matchComponent(wfnet, choices)) != null) {
			if (!(component instanceof UnstructuredComponent
					&& pumpTTComponents.isSelected()
					&& isTTSplitComponent(component) && pumpTTComponent(wfnet,
					component))) {
				double smResult = calculateStructurednessMetric(component,
						annotations);
				double unstructuredSmResult = calculateStructurednessMetric(
						component, new AllKeysAreOne());
				log.storeLogMatch(wfnet, component, smResult,
						unstructuredSmResult, calculateCardosoMetric(component
								.getWfnet()).first,
						calculateCyclomaticMetric(component.getWfnet()).first);
				Pair<Transition, Set<Node>> pair = PatternMatcher.reduce(wfnet,
						component.getWfnet());
				annotations.put(pair.first.getName(), smResult);
				transition2Component.put(pair.first.getName(), component);
				// dummyAnnotations.put(pair.first.getName(), 1.0);
			}
			if (progress.isCanceled())
				break;
		}
		return new StructurednessResult(netName, path, net, wfnet, annotations
				.get(wfnet.getTransitions().get(0).getName()), cardosoMetric,
				cardosoCalculation, cyclomaticMetric, cyclomaticCalculation,
				log, transition2Component);
	}

	private class AllKeysAreOne implements Map<String, Double> {

		public void clear() {
		}

		public boolean containsKey(Object key) {
			return false;
		}

		public boolean containsValue(Object value) {
			return false;
		}

		public Set<java.util.Map.Entry<String, Double>> entrySet() {
			return null;
		}

		public Double get(Object key) {
			return 1.0;
		}

		public boolean isEmpty() {
			return false;
		}

		public Set<String> keySet() {
			return null;
		}

		public Double put(String key, Double value) {
			return null;
		}

		public void putAll(Map<? extends String, ? extends Double> t) {
		}

		public Double remove(Object key) {
			return null;
		}

		public int size() {
			return 0;
		}

		public Collection<Double> values() {
			return null;
		}
	};

	private boolean pumpTTComponent(PetriNet wfnet, Component component) {
		Transition source = null, sink = null;
		String componentSource = component.getWfnet().getSource().getName();
		String componentSink = component.getWfnet().getSink().getName();
		for (Transition transition : wfnet.getTransitions()) {
			if (transition.getName().equals(componentSource))
				source = transition;
			else if (transition.getName().equals(componentSink))
				sink = transition;
			if (source != null && sink != null)
				break;
		}
		List<Triple<List<Place>, Set<Node>, Set<Place>>> branches = new ArrayList<Triple<List<Place>, Set<Node>, Set<Place>>>();
		for (Place place : PetriNetNavigation.getOutgoingPlaces(source)) {
			Set<Node> branch = new LinkedHashSet<Node>();
			Set<Place> endPoints = new LinkedHashSet<Place>();
			getBranch(place, sink, branch, endPoints);
			boolean found = false;
			search: for (Triple<List<Place>, Set<Node>, Set<Place>> triple : branches) {
				for (Node node : branch) {
					if (triple.second.contains(node)) {
						triple.first.add(place);
						triple.second.addAll(branch);
						triple.third.addAll(endPoints);
						found = true;
						break search;
					}
				}
			}
			if (!found) {
				List<Place> list = new ArrayList<Place>();
				list.add(place);
				branches.add(Triple.create(list, branch, endPoints));
			}
		}
		if (branches.size() > 1) {
			for (Triple<List<Place>, Set<Node>, Set<Place>> triple : branches) {
				String idStart = UUID.randomUUID().toString();
				Place dummyPlaceStart = new Place("dummy_place_start_"
						+ idStart, wfnet);
				wfnet.addPlace(dummyPlaceStart);
				Transition dummyTransitionStart = new Transition(
						"dummy_transition_start_" + idStart, wfnet);
				wfnet.addTransition(dummyTransitionStart);
				wfnet.addAndLinkEdge(new PNEdge(source, dummyPlaceStart),
						source, dummyPlaceStart);
				wfnet.addAndLinkEdge(new PNEdge(dummyPlaceStart,
						dummyTransitionStart), dummyPlaceStart,
						dummyTransitionStart);

				for (Place place : triple.first) {
					for (Edge edge : place.getInEdges())
						if (edge.getTail() == source) {
							wfnet.removeEdge((PNEdge) edge);
							edge.delete();
						}
					wfnet.addAndLinkEdge(
							new PNEdge(dummyTransitionStart, place),
							dummyTransitionStart, place);
				}

				String idEnd = UUID.randomUUID().toString();
				Place dummyPlaceEnd = new Place("dummy_place_end_" + idEnd,
						wfnet);
				wfnet.addPlace(dummyPlaceEnd);
				Transition dummyTransitionEnd = new Transition(
						"dummy_transition_end_" + idEnd, wfnet);
				wfnet.addTransition(dummyTransitionEnd);
				wfnet.addAndLinkEdge(new PNEdge(dummyTransitionEnd,
						dummyPlaceEnd), dummyTransitionEnd, dummyPlaceEnd);
				wfnet.addAndLinkEdge(new PNEdge(dummyPlaceEnd, sink),
						dummyPlaceEnd, sink);

				for (Place place : triple.third) {
					for (Edge edge : place.getOutEdges())
						if (edge.getHead() == sink) {
							wfnet.removeEdge((PNEdge) edge);
							edge.delete();
						}
					wfnet.addAndLinkEdge(new PNEdge(place, dummyTransitionEnd),
							place, dummyTransitionEnd);
				}
			}
			return true;
		}
		return false;
	}

	private void getBranch(Node node, Transition sink, Set<Node> branch,
			Set<Place> endPoints) {
		if (node != sink && branch.add(node)) {
			for (Node outNode : PetriNetNavigation.getOutgoingNodes(node)) {
				if (outNode == sink)
					endPoints.add((Place) node);
				getBranch(outNode, sink, branch, endPoints);
			}
		}
	}

	private boolean isTTSplitComponent(Component component) {
		return component.getWfnet().getSource().outDegree() > 1
				&& component.getWfnet().getSource() instanceof Transition
				&& component.getWfnet().getSink() instanceof Transition;
	}

	private Pair<Integer, Quadruple<StateSpace, Integer, Integer, List<List<ModelGraphVertex>>>> calculateCyclomaticMetric(
			PetriNet net) {
		Node source = net.getSource();
		Node sink = net.getSink();
		if (source instanceof Transition || sink instanceof Transition) {
			PetriNet clone = (PetriNet) net.clone();
			if (source instanceof Transition) {
				source = clone.getSource();
				Place dummy = new Place("dummySource", clone);
				clone.addPlace(dummy);
				clone.addAndLinkEdge(new PNEdge(dummy, (Transition) source),
						dummy, (Transition) source);
			}
			if (sink instanceof Transition) {
				sink = clone.getSink();
				Place dummy = new Place("dummySink", clone);
				clone.addPlace(dummy);
				clone.addAndLinkEdge(new PNEdge((Transition) sink, dummy),
						(Transition) sink, dummy);
			}
			net = clone;
		}
		InitialPlaceMarker.mark(net, 1);
		StateSpace ss = null;
		try {
			ss = CoverabilityGraphBuilder.build(net);
			for (ModelGraphVertex v : ss.getVerticeList()) {
				State s = (State) v;
				Marking m = s.getMarking();
				Iterator i = m.iterator();
				while (i.hasNext()) {
					Place p = (Place) i.next();
					if (m.getTokens(p) == Marking.OMEGA)
						return Pair
								.create(
										INFINITE_SS,
										(Quadruple<StateSpace, Integer, Integer, List<List<ModelGraphVertex>>>) null);
				}
			}
		} catch (OutOfMemoryError ome) {
			return Pair
					.create(
							UNFINISHED_CG,
							(Quadruple<StateSpace, Integer, Integer, List<List<ModelGraphVertex>>>) null);
		}
		List<List<ModelGraphVertex>> stronglyConnectedComponents = ss
				.getStronglyConnectedComponents();
		return Pair.create(ss.getEdges().size() - ss.getVerticeList().size()
				+ stronglyConnectedComponents.size(), Quadruple.create(ss, ss
				.getEdges().size(), ss.getVerticeList().size(),
				stronglyConnectedComponents));
	}

	private Pair<Integer, List<Pair<Place, Set<Set<Place>>>>> calculateCardosoMetric(
			PetriNet net) {
		int result = 0;
		List<Pair<Place, Set<Set<Place>>>> calculation = new ArrayList<Pair<Place, Set<Set<Place>>>>();
		for (Place place : net.getPlaces()) {
			TreeSet<Set<Place>> nextStates = new TreeSet<Set<Place>>(
					new Comparator<Set<Place>>() {
						public int compare(Set<Place> arg0, Set<Place> arg1) {
							if (arg0.size() > arg1.size())
								return -1;
							else if (arg0.size() < arg1.size())
								return 1;
							else if (arg0.containsAll(arg1))
								return 0;
							else
								return 1;
						}
					});
			for (Transition transition : PetriNetNavigation
					.getOutgoingTransitions(place)) {
				Vector<Place> outgoingPlaces = PetriNetNavigation
						.getOutgoingPlaces(transition);
				nextStates.add(new LinkedHashSet<Place>(outgoingPlaces));
			}
			result += nextStates.size();
			calculation.add(Pair.create(place, (Set<Set<Place>>) nextStates));
		}
		return Pair.create(result, calculation);
	}

	protected void showMetric(StructurednessResult result, Log log,
			Map<String, Component> transition2Component) {
		removeAll();
		String msg = "Extended Cardoso metric: <b>" + result.getCardosoMetric()
				+ "</b><br>Extended Cyclomatic metric: <b>";
		switch (result.getCyclomaticMetric()) {
		case INFINITE_SS:
			msg += "&#8734;"; // the HTML entity for infinite
			break;
		case UNFINISHED_CG:
			msg += "Could not calculate; java.lang.OutOfMemoryError";
			break;
		default:
			msg += result.getCyclomaticMetric();
			break;
		}
		msg += "</b><br>Structuredness metric: <b>" + result.getMyMetric();

		log.prepareToShowLog(result.getWfnet(), msg, false, result
				.getCardosoMetric(), result.getCardosoCalculation(), result
				.getCyclomaticMetric(), result.getCyclomaticCalculation(),
				transition2Component);
		add(log, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		validate();
		repaint();
	}

	private double calculateStructurednessMetric(Component component,
			Map<String, Double> annotations) {
		double penalty = 0;
		if (component instanceof SequenceComponent) {
			for (Transition transition : component.getWfnet().getTransitions())
				penalty += annotations.get(transition.getName());
		} else if (component instanceof ChoiceComponent) {
			for (Transition transition : component.getWfnet().getTransitions())
				penalty += 1.5 * annotations.get(transition.getName());
		} else if (component instanceof WhileComponent) {
			for (Transition transition : component.getWfnet().getTransitions())
				if (transition.inDegree() > 0 && transition.outDegree() > 0)
					penalty += 2 * annotations.get(transition.getName());
				else
					penalty += annotations.get(transition.getName());
		} else if (component instanceof MarkedGraphComponent) {
			int splits = 0, joins = 0;
			for (Transition transition : component.getWfnet().getTransitions()) {
				if (transition.inDegree() > 1)
					joins++;
				if (transition.outDegree() > 1)
					splits++;
				penalty += annotations.get(transition.getName()) * 2;
			}
			penalty = penalty * Math.abs(splits - joins + 1);
		} else if (component instanceof StateMachineComponent) {
			int splits = 0, joins = 0;
			for (Place place : component.getWfnet().getPlaces()) {
				if (place.inDegree() > 1)
					joins++;
				if (place.outDegree() > 1) {
					splits++;
				}
			}
			for (Transition transition : component.getWfnet().getTransitions())
				penalty += annotations.get(transition.getName());
			penalty *= Math.abs(splits - joins) + 1;
		} else if (component instanceof WellStructuredGraphComponent) {
			int splits = 0, joins = 0;
			for (Transition transition : component.getWfnet().getTransitions()) {
				if (transition.inDegree() > 1)
					joins++;
				if (transition.outDegree() > 1) {
					splits++;
				}
				penalty += annotations.get(transition.getName()) * 2;
			}
			penalty *= Math.abs(splits - joins) + 1;

			splits = 0;
			joins = 0;
			for (Place place : component.getWfnet().getPlaces()) {
				if (place.inDegree() > 1)
					joins++;
				if (place.outDegree() > 1) {
					splits++;
				}
			}
			penalty *= Math.abs(splits - joins) + 1;
		} else if (component instanceof UnstructuredComponent) {
			for (Transition transition : component.getWfnet().getTransitions())
				penalty += annotations.get(transition.getName());
			penalty *= 5 * (Math.abs(component.getWfnet().getNumberOfEdges()
					- component.getWfnet().getNumberOfEdges()) + 1);
			int splits = 0, joins = 0;
			for (Transition transition : component.getWfnet().getTransitions()) {
				if (transition.inDegree() > 1)
					joins++;
				if (transition.outDegree() > 1) {
					splits++;
				}
				penalty += annotations.get(transition.getName()) * 2;
			}
			penalty *= Math.abs(splits - joins) + 1;

			splits = 0;
			joins = 0;
			for (Place place : component.getWfnet().getPlaces()) {
				if (place.inDegree() > 1)
					joins++;
				if (place.outDegree() > 1) {
					splits++;
				}
			}
			penalty *= Math.abs(splits - joins) + 1;
		} else if (component instanceof LibraryComponent)
			penalty = matchingOrder.getPenalty(component);
		else
			System.err.println("!!!!!!!!!!!!! UNKNOWN COMPONENT !!!!!!!!!!!!!");
		return penalty;
	}

	private boolean isTrivial(PetriNet wfnet) {
		return wfnet.getTransitions().size() == 1;
	}

	private Component matchComponent(PetriNet wfnet, Map<String, Choice> choices)
			throws FileNotFoundException, Exception {
		Component match = null;
		TreeSet<PetriNet> components = null;
		for (ComponentDescription componentDescription : matchingOrder
				.getOrder()) {
			if (componentDescription.isPredefined()) {
				if (componentDescription.getName().equals("Maximal sequence"))
					match = PatternMatcher.getMaximalSequence(wfnet);
				else if (componentDescription.getName().equals(
						"Maximal state machine")) {
					if (components == null)
						components = BPELPatternMatcher.getComponents(wfnet);
					List<StateMachineComponent> stateMachineComponents = PatternMatcher
							.getStateMachineComponents(wfnet, components);
					if (!stateMachineComponents.isEmpty())
						match = stateMachineComponents.get(0);
				} else if (componentDescription.getName().equals(
						"Maximal marked graph")) {
					if (components == null)
						components = BPELPatternMatcher.getComponents(wfnet);
					List<MarkedGraphComponent> markedGraphComponents = PatternMatcher
							.getMarkedGraphComponents(wfnet, components);
					if (!markedGraphComponents.isEmpty())
						match = markedGraphComponents.get(0);
				} else if (componentDescription.getName().equals(
						"Explicit choice")) {
					List<ExplicitChoiceComponent> choiceComponents = PatternMatcher
							.getExplicitChoiceComponents(wfnet, choices);
					if (!choiceComponents.isEmpty())
						match = choiceComponents.get(0);
				} else if (componentDescription.getName().equals(
						"Implicit choice")) {
					List<ImplicitChoiceComponent> choiceComponents = PatternMatcher
							.getImplicitChoiceComponents(wfnet, choices);
					if (!choiceComponents.isEmpty())
						match = choiceComponents.get(0);
				} else if (componentDescription.getName().equals("While"))
					match = PatternMatcher.getWhile(wfnet);
				else if (componentDescription.getName().equals(
						"Maximal well-structured")) {
					if (components == null)
						components = BPELPatternMatcher.getComponents(wfnet);
					List<WellStructuredGraphComponent> wellStructuredGraphComponents = PatternMatcher
							.getWellStructuredGraphComponents(wfnet, components);
					if (!wellStructuredGraphComponents.isEmpty())
						match = wellStructuredGraphComponents.get(0);
				}
			} else if (!componentDescription.isPredefined()) {
				if (components == null)
					components = PatternMatcher.getComponents(wfnet);
				match = PatternMatcher.getComponentFromLibrary(wfnet,
						componentDescription.getName(), matchingOrderFolder,
						components);
			}
			if (match != null)
				break;
		}

		if (match == null) {
			if (components == null)
				components = PatternMatcher.getComponents(wfnet);
			if (!components.isEmpty())
				match = new UnstructuredComponent(components.first());
		}

		return match;
	}

	public ProvidedObject[] getProvidedObjects() {
		if (currentLog != null) {
			return currentLog.getProvidedObjects();
		}
		if (currentBatchLog != null) {
			return currentBatchLog.getProvidedObjects();
		}
		return new ProvidedObject[] {};
	}

}
