package org.processmining.analysis.causality;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.causality.BackwardEdge;
import org.processmining.framework.models.causality.CausalFootprint;
import org.processmining.framework.models.causality.ForwardEdge;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.ToolTipComboBox;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class CausalFootprintAnalysisResult extends JPanel implements Provider {

	private CausalFootprint cs;
	private ModelGraphPanel graph;
	private ProvidedObject original;

	public CausalFootprintAnalysisResult(final CausalFootprint cs,
			final ModelGraph graphVisualization, ProvidedObject original) {
		this.cs = cs;

		setLayout(new BorderLayout());

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setOneTouchExpandable(true);
		split.setResizeWeight(0.5);

		final ContextSelectionPanel csVisualization = new ContextSelectionPanel(
				cs);

		graph = graphVisualization.getGrappaVisualization();
		this.original = original;
		if (graphVisualization != null) {
			split.add(graph, JSplitPane.LEFT);
			graph.addGrappaListener(new GrappaAdapter() {

				/**
				 * The method is called when a mouse press occurs on a displayed
				 * subgraph. The returned menu is added to the end of the
				 * default right-click menu
				 * 
				 * @param subg
				 *            displayed subgraph where action occurred
				 * @param elem
				 *            subgraph element in which action occurred
				 * @param pt
				 *            the point where the action occurred (graph
				 *            coordinates)
				 * @param modifiers
				 *            mouse modifiers in effect
				 * @param panel
				 *            specific panel where the action occurred
				 */
				protected JMenuItem getCustomMenu(Subgraph subg,
						final Element elem, GrappaPoint pt, int modifiers,
						GrappaPanel panel) {
					if (elem != null
							&& elem.object != null
							&& cs
									.getCausalVertex((ModelGraphVertex) elem.object) != null) {
						JMenuItem menu = new JMenuItem("Show Causal Context");
						menu.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								csVisualization
										.setSelectedObject(cs
												.getCausalVertex((ModelGraphVertex) elem.object));
							}
						});
						return menu;
					}

					return null;
				}
			});
		} else {
			split
					.add(
							new JLabel(
									"<html>No model available or <br> visualization was aborted.</html>"),
							JSplitPane.LEFT);
		}
		/*
		 * if (csVisualization != null) { split.add(csVisualization,
		 * JSplitPane.RIGHT); } else { split.add(new JLabel(
		 * "<html>No footprint available or <br> visualization was aborted.</html>"
		 * ), JSplitPane.RIGHT); }
		 */
		split.add(csVisualization);

		add(split, BorderLayout.CENTER);

		final ToolTipComboBox combo = new ToolTipComboBox();
		combo.addItem("Singular Trap Pattern");
		combo.addItem("Generalized Trap Pattern");
		combo.addItem("Possible Deadlock Pattern");
		combo.addItem("Possible Muti-termination Pattern");

		final ToolTipComboBox results = new ToolTipComboBox();
		results.setEnabled(false);
		results.addItem("First select a Pattern");
		results.setPreferredSize(new Dimension(300,
				combo.getPreferredSize().height));

		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.add(combo);

		JButton ok = new JButton("Find Pattern");
		bottomPanel.add(ok);
		bottomPanel.add(new JLabel("==>"));
		bottomPanel.add(results);

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList pat = new ArrayList(0);
				if (combo.getSelectedIndex() == 0) {
					// find Singular Trap Pattern
					pat = findSingularTrapPattern();
					Message.add("<SingularTrapPatternsFound " + pat.size()
							+ ">", Message.TEST);
				}
				if (combo.getSelectedIndex() == 1) {
					// find generalized Trap Pattern
					pat = findGeneralizedTrapPattern();
					Message.add("<GeneralizedTrapPatternsFound " + pat.size()
							+ ">", Message.TEST);
				}
				if (combo.getSelectedIndex() == 2) {
					// find deadlock Trap Pattern
					pat = findDeadlockPatterns();
					Message.add("<DeadlockTrapPatternsFound " + pat.size()
							+ ">", Message.TEST);
				}
				if (combo.getSelectedIndex() == 3) {
					// find multi term Trap Pattern
					pat = findMultiTermPatterns();
					Message.add("<MultiTermTrapPatternsFound " + pat.size()
							+ ">", Message.TEST);
				}
				results.removeAllItems();
				Iterator it = pat.iterator();
				while (it.hasNext()) {
					results.addItem(it.next());
				}
				results.setEnabled(true);
				if (results.getItemCount() == 0) {
					Message.add("<NoPatternFound>", Message.TEST);
					results.addItem("No pattern found.");
					results.setEnabled(false);
					results.setSelectedIndex(0);
				}
			}
		});

		JButton show = new JButton("Show Pattern");
		bottomPanel.add(show);
		show.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (results.getSelectedItem() instanceof HashSet) {
					HashSet selected = (HashSet) results.getSelectedItem();
					if (graph != null) {
						graph.unSelectAll();
						graph.selectElements(cs.getBaseVertices(selected));
					}
					csVisualization.unSelectAll();
					csVisualization.selectElements(selected);
				}
			}
		});

		add(bottomPanel, BorderLayout.SOUTH);

	}

	public ProvidedObject[] getProvidedObjects() {
		if (original != null) {
			return new ProvidedObject[] { original,
					new ProvidedObject("Causal Footprint", new Object[] { cs }) };
		} else {
			return new ProvidedObject[] { new ProvidedObject(
					"Causal Footprint", new Object[] { cs }) };
		}
	}

	private ArrayList findSingularTrapPattern() {
		ArrayList result = new ArrayList();
		Iterator it = cs.getVerticeList().iterator();
		while (it.hasNext()) {
			ModelGraphVertex v = (ModelGraphVertex) it.next();
			HashSet V = new HashSet(1);
			V.add(v);
			if (cs.containsEdge(v, V) || cs.containsEdge(V, v)) {
				result.add(V);
			}
		}
		return result;
	}

	private ArrayList findGeneralizedTrapPattern() {
		ArrayList result = new ArrayList();
		findGeneralizedTrapPattern(result, new HashSet(), 0, true);
		findGeneralizedTrapPattern(result, new HashSet(), 0, false);
		return result;
	}

	private void findGeneralizedTrapPattern(ArrayList result, HashSet setSoFar,
			int index, boolean forward) {

		if (isTrapPattern(setSoFar, forward) && !setSoFar.isEmpty()) {
			result.add(new HashSet(setSoFar));
		}
		for (int i = index; i < cs.getVerticeList().size(); i++) {
			ModelGraphVertex v = (ModelGraphVertex) cs.getVerticeList().get(i);
			setSoFar.add(v);
			findGeneralizedTrapPattern(result, setSoFar, i + 1, forward);
			setSoFar.remove(v);
		}

	}

	private boolean isTrapPattern(HashSet set, boolean forward) {
		boolean result = true;
		Iterator it = set.iterator();
		while (result && it.hasNext()) {
			ModelGraphVertex v = (ModelGraphVertex) it.next();
			if (forward) {
				result &= (cs.getSmallerEdge(v, set) != null);
			} else {
				result &= (cs.getSmallerEdge(set, v) != null);
			}
		}
		return result;
	}

	private ArrayList findDeadlockPatterns() {
		ArrayList pat = new ArrayList();
		Iterator it = cs.getLookAheadEdges().iterator();
		while (it.hasNext()) {
			ForwardEdge edge = (ForwardEdge) it.next();
			if (edge.destinations.size() <= 1) {
				continue;
			}
			// a = edge.source
			// B = edge.destinations
			HashSet outgoing = cs.getOutgoingLookAheadEdges(edge.source);
			Iterator it2 = outgoing.iterator();
			while (it2.hasNext()) {
				ForwardEdge e = (ForwardEdge) it2.next();
				if ((e.destinations.size() == 1)
						&& !edge.destinations.contains(e.destinations
								.iterator().next())) {
					// {d} = e.destinations
					boolean allFromB = true;
					Iterator it3 = edge.destinations.iterator();
					while (allFromB && it3.hasNext()) {
						HashSet B = new HashSet(1);
						B.add(it3.next());
						allFromB &= cs.containsEdge(B,
								(ModelGraphVertex) e.destinations.iterator()
										.next());
					}
					if (allFromB) {
						// Deadlock found
						HashSet result = new HashSet();
						result.add(edge.source);
						result.addAll(edge.destinations);
						result.addAll(e.destinations);
						pat.add(result);
					}
				}
			}
		}
		return pat;
	}

	private ArrayList findMultiTermPatterns() {
		ArrayList pat = new ArrayList();
		Iterator it = cs.getLookBackEdges().iterator();
		while (it.hasNext()) {
			BackwardEdge edge = (BackwardEdge) it.next();
			if (edge.sources.size() <= 1) {
				continue;
			}
			// B = edge.sources
			// d = edge.destination
			HashSet ingoing = cs.getIncomingLookBackEdges(edge.destination);
			Iterator it2 = ingoing.iterator();
			while (it2.hasNext()) {
				BackwardEdge e = (BackwardEdge) it2.next();
				if ((e.sources.size() == 1)
						&& !edge.sources.contains(e.sources.iterator().next())) {
					// {a} = e.sources
					boolean allToB = true;
					Iterator it3 = edge.sources.iterator();
					while (allToB && it3.hasNext()) {
						HashSet B = new HashSet(1);
						B.add(it3.next());
						allToB &= cs.containsEdge((ModelGraphVertex) e.sources
								.iterator().next(), B);
					}
					if (allToB) {
						// Deadlock found
						HashSet result = new HashSet();
						result.add(edge.destination);
						result.addAll(edge.sources);
						result.addAll(e.sources);
						pat.add(result);
					}
				}
			}
		}
		return pat;
	}
}

class ContextSelectionPanel extends JPanel {

	private final CausalFootprint fp;

	private ModelGraphPanel fpVis;

	public ContextSelectionPanel(CausalFootprint footprint) {
		super();
		fp = footprint;
		initialize();
	}

	public ContextSelectionPanel(boolean isDoubleBuffered,
			CausalFootprint footprint) {
		super(isDoubleBuffered);
		fp = footprint;
		initialize();
	}

	public ContextSelectionPanel(LayoutManager layout, CausalFootprint footprint) {
		super();
		fp = footprint;
		initialize();
	}

	public ContextSelectionPanel(LayoutManager layout,
			boolean isDoubleBuffered, CausalFootprint footprint) {
		super(isDoubleBuffered);
		fp = footprint;
		initialize();
	}

	private JComboBox selectionBox = new ToolTipComboBox(
			new DefaultComboBoxModel());
	private JButton selectButton = new JButton("Show");

	private void initialize() {
		this.setLayout(new BorderLayout());

		selectionBox.addItem("Whole Causal Footprint");
		Iterator it = fp.getVerticeList().iterator();
		while (it.hasNext()) {
			selectionBox.addItem(it.next());
		}
		selectionBox.setSelectedIndex(0);

		JPanel southPanel = new JPanel(new FlowLayout());
		southPanel.add(new JLabel("Select node to show context"));
		southPanel.add(selectionBox);
		southPanel.add(selectButton);
		this.add(southPanel, BorderLayout.SOUTH);

		selectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Object o = selectionBox.getSelectedItem();
				setSelectedObject(o);
			}
		});
		showWhole();
	}

	void setSelectedObject(Object o) {
		if (o instanceof ModelGraphVertex) {
			showContext((ModelGraphVertex) o);
		} else {
			showWhole();
		}
	}

	private void showWhole() {
		if (fpVis != null) {
			this.remove(fpVis);
		}
		fp.setWriteSelection(null);
		fpVis = fp.getGrappaVisualization();
		this.add(fpVis, BorderLayout.CENTER);
		this.invalidate();
		this.repaint();
		this.validate();
	}

	private void showContext(ModelGraphVertex selected) {
		if (fpVis != null) {
			this.remove(fpVis);
		}
		fp.setWriteSelection(selected);
		fpVis = fp.getGrappaVisualization();
		this.add(fpVis, BorderLayout.CENTER);
		this.invalidate();
		this.repaint();
		this.validate();
	}

	void selectElements(HashSet selected) {
		if (fpVis != null) {
			fpVis.selectElements(selected);
		}
	}

	/**
	 * unSelectAll
	 */
	void unSelectAll() {
		if (fpVis != null) {
			fpVis.unSelectAll();
		}
	}
}
