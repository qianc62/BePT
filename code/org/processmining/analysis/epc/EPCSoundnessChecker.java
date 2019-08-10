package org.processmining.analysis.epc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.processmining.analysis.Analyzer;
import org.processmining.converting.epc2transitionsystem.EPCMarking;
import org.processmining.converting.epc2transitionsystem.EpcToTransitionSystem;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertexSet;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.models.transitionsystem.TransitionSystemEdge;
import org.processmining.framework.models.epcpack.EPCEdge;
import java.util.List;
import org.processmining.framework.models.epcpack.EPCEvent;

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
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class EPCSoundnessChecker {

	@Analyzer(name = "EPC Soundness Analysis", names = { "EPC" })
	public JComponent analyze(ConfigurableEPC epc) {

		EpcToTransitionSystem converter = new EpcToTransitionSystem();
		return analyze(epc, converter.convert(epc));
	}

	@Analyzer(name = "EPC Soundness Analysis (with existing TS)", names = {
			"EPC", "Transition System" }, connected = false)
	public JComponent analyze(ConfigurableEPC epc, TransitionSystem ts) {
		TransitionSystem transitionSystem = ts;
		String message = checkSoundness(transitionSystem, epc);

		// remove nodes from transition system
		for (int i = ts.getVerticeList().size() - 1; i >= 0; i--) {
			ModelGraphVertex node = ts.getVerticeList().get(i);
			if (node.getAttribute(EPCMarking.NOTE) == null
					|| !node.getAttributeValue(EPCMarking.NOTE).equals(
							EPCMarking.END)) {
				ts.removeVertex(node);
			}
		}
		HashMap<String, EPCEvent> inEdge2inEvent = new HashMap();
		for (EPCEdge edge : (List<EPCEdge>) epc.getEdges()) {
			if (edge.getSource() instanceof EPCEvent) {
				EPCEvent evt = (EPCEvent) edge.getSource();
				if (evt.inDegree() == 0) {
					inEdge2inEvent.put("" + edge.getId(), evt);
				}
			}
		}
		TransitionSystemVertexSet init = new TransitionSystemVertexSet("init",
				ts);
		ts.addVertexQuick(init);
		ts.setStartState(init);
		for (ModelGraphVertex v : ts.getVerticeList()) {
			if (v != init && v.inDegree() == 0) {
				TransitionSystemVertexSet vertex = (TransitionSystemVertexSet) v;
				String label = "Events: {";
				for (String doc : vertex.getDocs()) {
					if (doc.startsWith("+")) {
						String toAdd = inEdge2inEvent.get(doc.substring(1))
								.getIdentifier();
						if (toAdd.endsWith("\\n")) {
							toAdd = toAdd.substring(0, toAdd.length() - 2);
						}
						label += toAdd + ",";
					}
				}
				label += "}";
				ts.addEdge(new TransitionSystemEdge(label, init,
						(TransitionSystemVertexSet) v));
			}
		}
		return new EPCSoundnessUI(epc, transitionSystem, message);
	}

	public String checkSoundness(TransitionSystem transitionSystem,
			ConfigurableEPC epc) {
		boolean isSound = true;
		String result = "";
		Iterator it = transitionSystem.getVerticeList().iterator();
		ArrayList<TransitionSystemVertexSet> goodstarts = new ArrayList<TransitionSystemVertexSet>();
		ArrayList<TransitionSystemVertexSet> ends = new ArrayList<TransitionSystemVertexSet>();
		ArrayList<TransitionSystemVertexSet> goodends = new ArrayList<TransitionSystemVertexSet>();
		Stack<TransitionSystemVertexSet> todolist = new Stack<TransitionSystemVertexSet>();
		Stack<TransitionSystemVertexSet> toendlist = new Stack<TransitionSystemVertexSet>();
		while (it.hasNext()) {
			TransitionSystemVertexSet next = (TransitionSystemVertexSet) it
					.next();
			if (next.getInEdges() == null && next.getOutEdges() == null) {
				continue;
			} else if (transitionSystem.getStartStates().contains(next) /*
																		 * next.getInEdges
																		 * () ==
																		 * null
																		 */) {
				if (next.getAttribute(EPCMarking.HIGHLIGHT) == null) {
					goodstarts.add(next);
					next.setAttribute(EPCMarking.HIGHLIGHT, EPCMarking.END);
				}
			} else if (next.getOutEdges() == null) {
				ends.add(next);
				if (next.getAttribute(EPCMarking.HIGHLIGHT) != null
						&& next.getAttributeValue(EPCMarking.HIGHLIGHT).equals(
								EPCMarking.DEADLOCK)) {
					todolist.add(next);
				} else {
					goodends.add(next);
				}
			}
		}
		int noBadEnds = todolist.size();
		if (noBadEnds == 0) {
			result = "<html>The EPC is sound for all initial markings.</html>";
			return result;
		}
		while (todolist.size() > 0) {
			TransitionSystemVertexSet next = (TransitionSystemVertexSet) todolist
					.pop();
			if (next.getInEdges() != null) {
				it = next.getInEdgesIterator();
				while (it.hasNext()) {
					ModelGraphEdge edge = (ModelGraphEdge) it.next();
					/**
					 * HV 2008-05-27: The start state has an incoming edge from
					 * a (dummy) node that is not a TransitionSytsemVertexSet!
					 */
					if (edge.getSource() instanceof TransitionSystemVertexSet) {
						TransitionSystemVertexSet vex = (TransitionSystemVertexSet) edge
								.getSource();
						if (vex.getAttribute(EPCMarking.NOTE) == null) {
							vex.setAttribute(EPCMarking.NOTE,
									EPCMarking.DEADLOCK);
							vex.setDotAttribute("color", "red");
							todolist.add(vex);
						}
					}
				}
			} else {
				if (next.getAttribute(EPCMarking.NOTE) != null
						&& next.getAttributeValue(EPCMarking.NOTE).equals(
								EPCMarking.DEADLOCK)) {
					next
							.setAttribute(EPCMarking.HIGHLIGHT,
									EPCMarking.DEADLOCK);
					next.setDotAttribute("color", "red");

					goodstarts.remove(next);
				} else if (next.getAttribute(EPCMarking.HIGHLIGHT) == null) {
					next.setAttribute(EPCMarking.NOTE, EPCMarking.END);
					next.setAttribute(EPCMarking.HIGHLIGHT, EPCMarking.END);

				}
			}
		}
		toendlist.addAll(goodstarts);
		while (toendlist.size() > 0) {
			TransitionSystemVertexSet next = (TransitionSystemVertexSet) toendlist
					.pop();
			next.setAttribute(EPCMarking.NOTE, EPCMarking.END);

			if (next.getOutEdges() != null) {
				it = next.getOutEdgesIterator();
				while (it.hasNext()) {
					TransitionSystemVertexSet target = (TransitionSystemVertexSet) ((ModelGraphEdge) it
							.next()).getDest();
					if (target.getAttribute(EPCMarking.NOTE) == null) {
						target.setAttribute(EPCMarking.NOTE, EPCMarking.END);

						toendlist.add(target);
					}
				}
			}
		}
		it = ends.iterator();
		Stack<ModelGraphVertex> goodEndNodes = new Stack<ModelGraphVertex>();
		while (it.hasNext()) {
			ModelGraphVertex endV = (ModelGraphVertex) it.next();
			if (endV.getAttribute(EPCMarking.NOTE) != null
					&& endV.getAttributeValue(EPCMarking.NOTE).equals(
							EPCMarking.END)) {
				endV.setAttribute(EPCMarking.HIGHLIGHT, EPCMarking.END);

				goodEndNodes.add(endV);
			} else {
				endV.setAttribute(EPCMarking.HIGHLIGHT, EPCMarking.DEADLOCK);
				endV.setDotAttribute("color", "red");

				goodends.remove(endV);
			}
		}
		// goodends and goodstarts are available
		ArrayList edges = epc.getEdges();
		Stack<String> badEndArcs = new Stack<String>();
		Stack<String> badStartArcs = new Stack<String>();
		HashMap<String, ModelGraphEdge> IdEdge = new HashMap<String, ModelGraphEdge>();
		it = edges.iterator();
		ModelGraphEdge current;
		while (it.hasNext()) {
			current = (ModelGraphEdge) it.next();
			IdEdge.put("" + current.getId(), current);
			if (current.getSource().getInEdges() == null) {
				badStartArcs.push("" + current.getId());
			}
			if (current.getDest().getOutEdges() == null) {
				badEndArcs.push("" + current.getId());
			}
		}
		it = goodends.iterator();
		TransitionSystemVertexSet v;
		while (badEndArcs.size() > 0 && it.hasNext()) {
			v = (TransitionSystemVertexSet) it.next();
			badEndArcs.removeAll(getPositiveArcs(v.getLabel()));
		}
		if (badEndArcs.size() > 0) {
			isSound = false;
		}
		it = goodstarts.iterator();
		while (badStartArcs.size() > 0 && it.hasNext()) {
			v = (TransitionSystemVertexSet) it.next();
			badStartArcs.removeAll(getPositiveArcs(v.getLabel()));
		}
		if (badStartArcs.size() > 0) {
			isSound = false;
		}
		if (isSound == true) {
			if (goodstarts.size() > 1) {
				result = "The EPC is sound.<br>"
						+ "There are "
						+ goodstarts.size()
						+ " initial markings which do not run into a deadlock:<br>";
			} else if (goodstarts.size() == 1) {
				result = "The EPC is sound.<br>"
						+ "There is "
						+ goodstarts.size()
						+ " initial marking which does not run into a deadlock:<br>";
			} else {
				result = "The EPC is sound.<br>"
						+ "There is no initial marking which does not run into a deadlock.<br>";
			}
			it = goodstarts.iterator();
			while (it.hasNext()) {
				result = result
						+ ((ModelGraphVertex) it.next()).getIdentifier()
						+ "<br>";
			}
			if (goodends.size() > 1) {
				result = result
						+ "There are "
						+ goodends.size()
						+ " final markings which can be reached from the initial markings:<br>";
			} else if (goodends.size() == 1) {
				result = result
						+ "There is "
						+ goodends.size()
						+ " final marking which can be reached from the initial markings:<br>";
			} else {
				result = result
						+ "There is no final marking which can be reached from the initial markings.<br>";
			}
			it = goodends.iterator();
			while (it.hasNext()) {
				result = result
						+ ((ModelGraphVertex) it.next()).getIdentifier()
						+ "<br>";
			}
		} else {
			if (goodstarts.size() > 1) {
				result = "The EPC is <i>NOT</i> sound.<br>"
						+ "There are "
						+ goodstarts.size()
						+ " initial markings which do not run into a deadlock:<br>";
			} else if (goodstarts.size() == 1) {
				result = "The EPC is <i>NOT</i> sound.<br>"
						+ "There is "
						+ goodstarts.size()
						+ " initial marking which does not run into a deadlock:<br>";
			} else {
				result = "The EPC is <i>NOT</i> sound.<br>"
						+ "There is no initial marking which does not run into a deadlock.<br>";
			}
			it = goodstarts.iterator();
			while (it.hasNext()) {
				result = result
						+ ((ModelGraphVertex) it.next()).getIdentifier()
						+ "<br>";
			}
			String id;
			if (badStartArcs.size() > 0) {
				result = result + "The initial markings do not include "
						+ badStartArcs.size() + " start arcs:<br>";
				it = badStartArcs.iterator();
				while (it.hasNext()) {
					id = (String) it.next();
					String startLabel = ((ModelGraphEdge) IdEdge.get(id))
							.getSource().getIdentifier();
					result = result + id + " after node " + startLabel + "<br>";
				}
			} else {
				result = result
						+ "The initial markings cover all start arcs.<br>";
			}
			if (goodends.size() > 1) {
				result = result
						+ "There are "
						+ goodends.size()
						+ " final markings which can be reached from the initial markings:<br>";
			} else if (goodends.size() == 1) {
				result = result
						+ "There is "
						+ goodends.size()
						+ " final marking which can be reached from the initial markings:<br>";
			} else {
				result = result
						+ "There is no final marking which can be reached from the initial markings.<br>";
			}
			it = goodends.iterator();
			while (it.hasNext()) {
				result = result
						+ ((ModelGraphVertex) it.next()).getIdentifier()
						+ "<br>";
			}
			if (badEndArcs.size() > 0) {
				result = result + "The final markings do not include "
						+ badEndArcs.size() + " end arcs:<br>";
				it = badEndArcs.iterator();
				while (it.hasNext()) {
					id = (String) it.next();
					String endLabel = ((ModelGraphEdge) IdEdge.get(id))
							.getDest().getIdentifier();
					result = result + id + " before node " + endLabel + "<br>";
				}
			} else {
				result = result + "The final markings cover all end arcs.<br>";
			}
		}
		result = "<html>" + result + "</html>";
		return result;
	}

	public Vector getPositiveArcs(String markingLabel) {
		Vector<String> list = new Vector<String>();
		// markingLabel = markingLabel.substring(1);
		StringTokenizer st = new StringTokenizer(markingLabel, "_");
		String trunc = "";
		String sign = "";
		while (st.hasMoreTokens()) {
			trunc = st.nextToken();
			if (trunc.equals("s")) {
				continue;
			}
			sign = trunc.substring(0, 1);
			trunc = trunc.substring(1);
			if (sign.equals("+")) {
				list.add(trunc);
			}
		}
		return list;
	}

	private void showWarningDialog(String message) {
		final JDialog dialog = new JDialog(MainUI.getInstance(),
				"Warning about EPC:", true);

		JLabel argLabel = new JLabel(message);

		JButton okButton = new JButton("    Ok    ");

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		dialog.getContentPane().setLayout(new GridBagLayout());

		dialog.getContentPane().add(
				argLabel,
				new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));
		dialog.getContentPane().add(
				okButton,
				new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(15, 5, 5, 5), 0, 0));

		dialog.pack();
		CenterOnScreen.center(dialog);
		dialog.setVisible(true);

	}

}
