package org.processmining.analysis.causality;

import java.util.*;
import java.util.Map.Entry;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

import org.processmining.framework.log.*;
import org.processmining.framework.models.*;
import org.processmining.framework.models.causality.*;
import org.processmining.framework.plugin.*;
import org.processmining.framework.ui.*;
import org.processmining.framework.util.*;

import cern.colt.matrix.DoubleMatrix2D;

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
public class CausalFootprintSimilarityResult extends JPanel implements
		Provider, ActionListener {

	public final static LogEvent DONOTMAP = new LogEvent(" Do not map", "");
	private JPanel left = new JPanel(new BorderLayout());
	private JPanel right = new JPanel(new BorderLayout());
	private JPanel center = new JPanel(new BorderLayout());
	private CausalFootprint c1 = null;
	private CausalFootprint c2 = null;
	private JButton goButton = new JButton(
			"<html><B>Calculate Similarity</B></html>");
	private JLabel resultLabel = new JLabel("");
	private JComboBox[] logEventSelections;
	private HashMap<LogEventProvider, LogEventProvider> mapping = new HashMap();
	public final static LogEventProvider NOMAP = new LogEventProvider() {
		public LogEvent getLogEvent() {
			return DONOTMAP;
		}

		public void setLogEvent(LogEvent logEvent) {
		}

		public String toString() {
			return DONOTMAP.toString();
		}
	};;

	public CausalFootprintSimilarityResult(ProvidedObject input1,
			ProvidedObject input2) {
		super(new BorderLayout());
		goButton.addActionListener(this);

		JSplitPane sp1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left,
				new JScrollPane(center));
		sp1.setOneTouchExpandable(true);
		sp1.setResizeWeight(0.5);
		JSplitPane sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp1, right);
		sp2.setOneTouchExpandable(true);
		sp2.setResizeWeight(0.5);
		sp2.setResizeWeight(2. / 3.);
		this.add(sp2);

		// Build first footprint
		Object[] o = input1.getObjects();
		int i = 0;
		boolean b = false;
		while (!b && (i < o.length)) {
			b |= (o[i] instanceof CausalFootprint);
			b |= CausalityFootprintFactory.canConvert(o[i]);
			if (o[i] instanceof ModelGraph) {
				ModelGraphPanel panel = ((ModelGraph) o[i])
						.getGrappaVisualization();
				if (panel != null) {
					left.add(panel);
				}
			}
			i++;
		}
		Object o2 = o[i - 1];
		if (!(o2 instanceof CausalFootprint)) {
			c1 = CausalityFootprintFactory.make(o2);
		} else {
			c1 = (CausalFootprint) o2;
		}

		// Build second footprint
		o = input2.getObjects();
		i = 0;
		b = false;
		while (!b && (i < o.length)) {
			b |= (o[i] instanceof CausalFootprint);
			b |= CausalityFootprintFactory.canConvert(o[i]);
			if (o[i] instanceof ModelGraph) {
				ModelGraphPanel panel = ((ModelGraph) o[i])
						.getGrappaVisualization();
				if (panel != null) {
					right.add(panel);
				}
			}
			i++;
		}
		o2 = o[i - 1];
		if (!(o2 instanceof CausalFootprint)) {
			c2 = CausalityFootprintFactory.make(o2);
		} else {
			c2 = (CausalFootprint) o2;
		}

		TreeSet<LogEventProvider> c1Elements = getLogEventProviders(c1
				.getVerticeList(), c1);
		TreeSet<LogEventProvider> c2Elements = getLogEventProviders(c2
				.getVerticeList(), c2);
		c2Elements.add(NOMAP);
		LogEventProvider[] c2ElementsArray = c2Elements
				.toArray(new LogEventProvider[] {});

		center.setLayout(new GridBagLayout());
		center.add(new JLabel("<html><B>Element from left model</B></html>"),
				new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(1, 1, 1, 1), 0, 0));
		center.add(new JLabel("<html><B>Element from right model</B></html>"),
				new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(1, 1, 1, 1), 0, 0));

		logEventSelections = new JComboBox[c1Elements.size()];
		i = 0;
		for (LogEventProvider e : c1Elements) {
			center.add(new JLabel("<html>" + e.getLogEvent().toString()
					+ "</html>"), new GridBagConstraints(0, i + 1, 1, 1, 0.0,
					0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(1, 1, 1, 1), 0, 0));

			JComboBox comboBox = new LogEventProviderMappingComboBox(
					c2ElementsArray, e);
			comboBox.setRenderer(new LogEventComboBoxRenderer());
			comboBox.setPreferredSize(new Dimension(200, (int) comboBox
					.getPreferredSize().getHeight()));
			comboBox.setSize(new Dimension(200, (int) comboBox
					.getPreferredSize().getHeight()));
			comboBox.addActionListener(this);

			comboBox.setSelectedIndex(findIndexOfSimilarElement(e,
					c2ElementsArray));
			logEventSelections[i] = comboBox;
			center.add(logEventSelections[i], new GridBagConstraints(1, i + 1,
					1, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));

			i++;
		}
		center.add(goButton, new GridBagConstraints(0, i + 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 1, 1), 0, 0));

		center.add(resultLabel, new GridBagConstraints(1, i + 1, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(1, 1, 1, 1), 0, 0));

	}

	public CausalFootprintSimilarityResult(CausalFootprint c1,
			CausalFootprint c2,
			HashMap<LogEventProvider, LogEventProvider> mapping) {
		this.c1 = c1;
		this.c2 = c2;
		this.mapping = mapping;

	}

	public Similarity calculateSimilarity(CancelationComponent progress,
			boolean removeStartAndEndNodes) {
		Map<Entry<LogEventProvider, LogEventProvider>, Double> map = new HashMap<Entry<LogEventProvider, LogEventProvider>, Double>();
		for (Entry entry : mapping.entrySet()) {
			map.put(entry, 1.0);
		}
		return calculateSimilarity(progress, removeStartAndEndNodes, map);
	}

	public Similarity calculateSimilarity(CancelationComponent progress,
			boolean removeStartAndEndNodes,
			Map<Entry<LogEventProvider, LogEventProvider>, Double> similarityMap) {
		// Now we finally got the two footprints ;-)
		Similarity result = new Similarity();
		if (removeStartAndEndNodes) {
			c1.removeVertex(c1.getSource());
			c1.removeVertex(c1.getTarget());
			c2.removeVertex(c2.getSource());
			c2.removeVertex(c2.getTarget());
		}

		result.nameA = c1.getIdentifier();
		result.nameB = c2.getIdentifier();

		// Nodes represents the list of unique nodes
		Collection<Integer> nodes = new ArrayList();
		Map<Integer, Double> nodeWeights = new HashMap<Integer, Double>();
		// map back to c1
		HashMap<Integer, ModelGraphVertex> node2c1 = new HashMap();

		// map back to c2
		HashMap<Integer, ModelGraphVertex> node2c2 = new HashMap();
		HashMap<LogEventProvider, Integer> c2ToNode = new HashMap();

		int i = 0;
		// First add all LogEventProviders of c2
		for (ModelGraphVertex v : (Collection<ModelGraphVertex>) c2
				.getVerticeList()) {
			if (progress.isCanceled()) {
				return result;
			}
			if ((c2.getBaseVertex(v) instanceof LogEventProvider)) {
				nodes.add(i);
				nodeWeights.put(i, 1.0);
				node2c2.put(i, v);
				c2ToNode.put((LogEventProvider) c2.getBaseVertex(v),
						new Integer(i));
				i++;
			}
		}

		// Now add all elements of c1, except for those that are mapped
		// to something in c2
		for (ModelGraphVertex v : (Collection<ModelGraphVertex>) c1
				.getVerticeList()) {
			if (progress.isCanceled()) {
				return result;
			}
			if ((c1.getBaseVertex(v) instanceof LogEventProvider)) {
				LogEventProvider lep = mapping.get(c1.getBaseVertex(v));
				if (lep == NOMAP) {
					nodes.add(new Integer(i));
					nodeWeights.put(i, 1.0);
					node2c1.put(new Integer(i), v);
					i++;
				} else {
					// do not add a new node
					node2c1.put(c2ToNode.get(lep), v);
				}
			}
		}
		if (!removeStartAndEndNodes) {
			nodes.add(new Integer(i));
			nodeWeights.put(i, 1.0);
			node2c1.put(new Integer(i), c1.getSource());
			node2c2.put(new Integer(i), c2.getSource());
			i++;
			nodes.add(new Integer(i));
			nodeWeights.put(i, 1.0);
			node2c1.put(new Integer(i), c1.getTarget());
			node2c2.put(new Integer(i), c2.getTarget());
		}

		for (Entry<Entry<LogEventProvider, LogEventProvider>, Double> entry : similarityMap
				.entrySet()) {
			Integer index = c2ToNode.get(entry.getKey().getValue());
			nodeWeights.put(index, entry.getValue());
		}

		Summation sums = updateSums(nodes, c1, node2c1, c2, node2c2,
				nodeWeights);
		result.similarity = sums.pairwiseSum
				/ (Math.sqrt(sums.rightSum) * Math.sqrt(sums.leftSum));

		/*
		 * // Nodes contains all unique nodes //DoubleMatrix2D m1 =
		 * getMatrix(c1, nodes, node2c1); //DoubleMatrix2D m2 = getMatrix(c2,
		 * nodes, node2c2); double[][] m1 = getMatrix(c1, nodes, node2c1);
		 * double[][] m2 = getMatrix(c2, nodes, node2c2);
		 * 
		 * //cern.jet.math.Functions F = cern.jet.math.Functions.functions; //
		 * aggregate(m, F.plus,F.mult) results in the sum over the pairwise
		 * product of the matrix //Double sum = m1.aggregate(m2, F.plus,
		 * F.mult); double sum= pairwiseSum(m1,m2); //Double sqsum1 =
		 * Math.sqrt(m1.aggregate(m1, F.plus, F.mult)); result.sizeA=
		 * Math.sqrt(pairwiseSum(m1,m1)); //Double sqsum2 =
		 * Math.sqrt(m2.aggregate(m2, F.plus, F.mult)); result.sizeB=
		 * Math.sqrt(pairwiseSum(m2,m2)); // Message.add("Similarity = " + sum +
		 * "/ (" + sqsum1 + " * " + sqsum2 + ") = " + // sum / (sqsum1 *
		 * sqsum2), Message.DEBUG); result.similarity = sum / (result.sizeA *
		 * result.sizeB);
		 */
		if (result.similarity > 1.) {
			result.similarity = 1.0;
		}
		return result;
	}

	private class Summation {
		public double pairwiseSum = 0.0;
		public double leftSum = 0.0;
		public double rightSum = 0.0;
	}

	private double pairwiseSum(double[][] m1, double[][] m2) {
		double d = 0;
		for (int i = 0; i < m1.length; i++) {
			for (int j = 0; j < m1[i].length; j++) {
				d += m1[i][j] * m2[i][j];
			}
		}
		return d;
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] {
				new ProvidedObject("Left Causal Footprint", new Object[] { c1 }),
				new ProvidedObject("Right Causal Footprint",
						new Object[] { c2 }) };
	}

	private int findIndexOfSimilarElement(LogEventProvider element,
			LogEventProvider[] objects) {
		int match;
		int prevMatch = -1;
		int mostMatching = 0;
		int l = element.getLogEvent().toString().length()
				+ objects[0].getLogEvent().toString().length();
		// Set this one to be the best match, if it is a
		// better match then we saw before, and the distance is less then
		// half of the sum ot the number of characters.
		for (int j = 0; j < objects.length; j++) {
			match = StringSimilarity.similarity(element.getLogEvent()
					.toString(), objects[j].getLogEvent().toString());
			if ((prevMatch == -1 || match < prevMatch) && match < l / 2) {
				mostMatching = j;
				prevMatch = match;
			}
		}
		return mostMatching;
	}

	private TreeSet<LogEventProvider> getLogEventProviders(Collection c,
			CausalFootprint f) {
		TreeSet<LogEventProvider> result = new TreeSet<LogEventProvider>(
				new Comparator() {
					public int compare(Object o1, Object o2) {
						if (o1 == null) {
							return -1;
						}
						if (o2 == null) {
							return 1;
						}
						return ((LogEventProvider) o1).getLogEvent().toString()
								.compareTo(
										((LogEventProvider) o2).getLogEvent()
												.toString());
					}

					public boolean equals(Object obj) {
						return false;
					}
				});
		for (ModelGraphVertex v : (Collection<ModelGraphVertex>) f
				.getVerticeList()) {
			if (f.getBaseVertex(v) instanceof LogEventProvider) {
				result.add((LogEventProvider) f.getBaseVertex(v));
			}
		}
		return result;
	}

	private Summation updateSums(Collection<Integer> nodes,
			CausalFootprint leftFp, Map<Integer, ModelGraphVertex> node2leftFp,
			CausalFootprint rightFp,
			Map<Integer, ModelGraphVertex> node2rightFp,
			Map<Integer, Double> node2weight) {
		Summation sums = new Summation();

		Object[] stringNodes = nodes.toArray();
		int size = nodes.size();
		int setSize = (int) Math.pow(2, size);

		for (int i = 0; i < size; i++) {
			ModelGraphVertex rightNode = (ModelGraphVertex) node2rightFp
					.get(stringNodes[i]);
			ModelGraphVertex leftNode = (ModelGraphVertex) node2leftFp
					.get(stringNodes[i]);

			double val = node2weight.get(stringNodes[i]);
			if (rightNode != null && leftNode != null) {
				// this node is in both cfp's, so add to the pairwise sum
				sums.pairwiseSum += val * val;
			}
			if (rightNode != null) {
				// update the pairwise sum of the right elements
				sums.rightSum += val;
			}
			if (leftNode != null) {
				// update the pairwise sum of the left elements
				sums.leftSum += val;
			}

			HashSet subset;
			// We consider the links of node n1
			for (int j = 1; j < setSize; j++) {
				// We consider one of our subsets
				// Build subset j
				subset = new HashSet();
				int x = 1;
				for (int k = 0; k < size; k++) {
					// x=2^k
					if ((x & j) == x) {
						subset.add(stringNodes[k]);
					}
					x *= 2;
				}
				HashMap<ModelGraphVertex, Integer> rightFp2Node = new HashMap();
				for (Entry<Integer, ModelGraphVertex> entry : node2rightFp
						.entrySet()) {
					rightFp2Node.put(entry.getValue(), entry.getKey());
				}
				double[] rightVals = getValues(node2rightFp, rightFp2Node,
						rightFp, rightNode, subset, node2weight);

				HashMap<ModelGraphVertex, Integer> leftFp2Node = new HashMap();
				for (Entry<Integer, ModelGraphVertex> entry : node2leftFp
						.entrySet()) {
					leftFp2Node.put(entry.getValue(), entry.getKey());
				}
				double[] leftVals = getValues(node2leftFp, leftFp2Node, leftFp,
						leftNode, subset, node2weight);

				// Message.add("Forward: from "+leftNode+" to "+subset+" results in "+rightVals[0]+" and "+leftVals[0],
				// Message.DEBUG);
				// Message.add("Backward: from "+subset+" to "+leftNode+" results in "+rightVals[1]+" and "+leftVals[1],
				// Message.DEBUG);
				// System.out.println("Forward: from "+leftNode+" to "+subset+" results in "+rightVals[0]+" and "+leftVals[0]);
				// System.out.println("Backward: from "+subset+" to "+leftNode+" results in "+rightVals[1]+" and "+leftVals[1]);

				subset.clear();
				sums.rightSum += Math.pow(rightVals[0], 2.0);
				sums.rightSum += Math.pow(rightVals[1], 2.0);
				sums.leftSum += Math.pow(leftVals[0], 2.0);
				sums.leftSum += Math.pow(leftVals[1], 2.0);
				sums.pairwiseSum += rightVals[0] * leftVals[0];
				sums.pairwiseSum += rightVals[1] * leftVals[1];
			}
		}
		return sums;
	}

	private double[] getValues(Map<Integer, ModelGraphVertex> node2fp,
			Map<ModelGraphVertex, Integer> fp2node, CausalFootprint fp,
			ModelGraphVertex node, HashSet subset,
			Map<Integer, Double> node2weight) {
		double[] res = new double[] { 0.0, 0.0 };
		HashSet subsetNodes;
		if (node2fp.keySet().containsAll(subset) && node != null) {
			// Now, see if there is a relation, i.e.
			// a look-ahead (n1, { } )
			subsetNodes = new HashSet(subset.size());
			for (Object s : subset) {
				subsetNodes.add(node2fp.get(s));
			}
			int y;
			if (subsetNodes.contains(node)) {
				// Make sure that node is not counted.
				y = 1;
			} else {
				y = 0;
			}

			ForwardEdge smallerForward = fp.getSmallerEdge(node, subsetNodes);
			if (smallerForward != null) {
				double val = 0;
				for (Object s : smallerForward.destinations) {
					val = Math.max(val, node2weight.get(fp2node.get(s)));
				}
				val *= node2weight.get(fp2node.get(node));

				// m1.set(i, j, 1 / (Math.pow(2, subset.size() - y)));
				res[0] = val / (Math.pow(2, subset.size() - y));
			}

			BackwardEdge smallerBackward = fp.getSmallerEdge(subsetNodes, node);
			if (smallerBackward != null) {
				double val = 0;
				for (Object s : smallerBackward.sources) {
					val = Math.max(val, node2weight.get(fp2node.get(s)));
				}
				val *= node2weight.get(fp2node.get(node));
				// m1.set(size + i, j, 1 / (Math.pow(2, subset.size() - y)));
				res[1] = val / (Math.pow(2, subset.size() - y));
			}
			subsetNodes.clear();
		}
		return res;
	}

	private double[][] getMatrix(CausalFootprint fp, Collection nodes,
			HashMap node2fp) {
		Object[] stringNodes = nodes.toArray();
		int size = nodes.size();
		int setSize = (int) Math.pow(2, size);
		double[][] m1 = new double[2 * size][setSize];// DoubleFactory2D.sparse.make(2
		// * size, setSize, 0);
		// Message.add("Making matrix: ("+2*size+"x"+setSize+")");
		for (int i = 0; i < 2 * size; i++) {
			java.util.Arrays.fill(m1[i], 0.0);
		}
		for (int i = 0; i < size; i++) {
			ModelGraphVertex n1 = (ModelGraphVertex) node2fp
					.get(stringNodes[i]);
			if (n1 == null) {
				// There is nothing we can do for this node..
				continue;
			}
			// This node is in the footprint, hence:
			// m1.set(i, 0, 1.0);
			m1[i][0] = 1.0;
			// We consider the links of node n1
			for (int j = 1; j < setSize; j++) {
				// We consider one of our subsets
				// Build subset j
				HashSet subset = new HashSet();
				int x = 1;
				for (int k = 0; k < size; k++) {
					// x=2^k
					if ((x & j) == x) {
						subset.add(stringNodes[k]);
					}
					x *= 2;
				}
				// Check if all nodes are present in model 1
				if (node2fp.keySet().containsAll(subset)) {
					// Now, see if there is a relation, i.e.
					// a look-ahead (n1, { } )
					HashSet subsetNodes = new HashSet(subset.size());
					for (Object s : subset) {
						subsetNodes.add(node2fp.get(s));
					}
					int y = (subsetNodes.contains(n1) ? 1 : 0);
					if (fp.getSmallerEdge(n1, subsetNodes) != null) {
						// m1.set(i, j, 1 / (Math.pow(2, subset.size() - y)));
						m1[i][j] = 1 / (Math.pow(2, subset.size() - y));
					}
					if (fp.getSmallerEdge(subsetNodes, n1) != null) {
						// m1.set(size + i, j, 1 / (Math.pow(2, subset.size() -
						// y)));
						m1[size + i][j] = 1 / (Math.pow(2, subset.size() - y));
					}
				}
			}
		}
		return m1;

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == goButton) {
			if (isOkMapping()) {
				Similarity d = calculateSimilarity(new Progress(
						"Calculating Similarity", 0, 100), false);
				String s = String.format("%.2f", 100 * d.similarity);
				resultLabel.setText("<html><B>" + s + " %</B></html>");
			} else {
				resultLabel.setText("<html>No Correct Mapping Made</html>");
			}
			resultLabel.validate();
		}
		if (e.getSource() instanceof LogEventProviderMappingComboBox) {
			LogEventProviderMappingComboBox combo = (LogEventProviderMappingComboBox) e
					.getSource();
			LogEventProvider source = combo.getSource();
			mapping.put(source, (LogEventProvider) combo.getSelectedItem());
		}
	}

	private boolean isOkMapping() {
		boolean ok = true;
		LogEventProvider[] keys = mapping.keySet().toArray(
				new LogEventProvider[] {});
		for (int i = 0; ok && i < keys.length; i++) {
			LogEventProvider lep = mapping.get(keys[i]);
			if (lep == NOMAP) {
				continue;
			}
			for (int j = i + 1; ok && j < keys.length; j++) {
				ok &= (lep != mapping.get(keys[j]));
			}
		}

		return ok;
	}

	class LogEventProviderMappingComboBox extends ToolTipComboBox {
		private LogEventProvider source;

		public LogEventProviderMappingComboBox(Object[] items,
				LogEventProvider source) {
			super(items);
			this.source = source;
		}

		public LogEventProvider getSource() {
			return source;
		}
	}

	class LogEventComboBoxRenderer extends BasicComboBoxRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			String text = "";
			if (value instanceof LogEventProvider) {
				text = ((LogEventProvider) value).getLogEvent().toString();
			} else {
				text = value.toString();
			}
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				if (-1 < index) {
					list.setToolTipText(text);
				}
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setFont(list.getFont());
			setText((value == null) ? "" : text);
			return this;
		}
	}

}
