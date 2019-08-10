/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

/*
 * Copyright (c) 2006 Eindhoven University of Technology
 * All rights reserved.
 */

package org.processmining.analysis.performance;

import java.awt.Color;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedPetriNet;
import org.processmining.framework.ui.Message;

/**
 * This class is used to enhance the Petri net model with performance results.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class ExtendedPetriNet extends ReplayedPetriNet {

	/**
	 * The currentlySelectedInstances attribute is necessary because the
	 * writeToDot() method has a fixed interface, though the visualization
	 * should be able to take them into account.
	 */
	public ArrayList currentlySelectedInstances = new ArrayList();

	// Required fields for setting up bottleneck colouring
	private long timeDivider;
	private int[] advancedSettings;
	private ArrayList bounds;
	private ArrayList levelColors;
	private HashSet failedInstances = new HashSet();

	/**
	 * Constructs an ExtendedPetriNet out of an ordinary one.
	 * 
	 * @param net
	 *            the Petri net that is re-established in the replayed net
	 * @param caseIDs
	 *            a list of strings containing the IDs of those instances that
	 *            want to store diagnostic information
	 */
	public ExtendedPetriNet(PetriNet net, ArrayList caseIDs) {
		super(net, caseIDs);
		HashMap activities = new HashMap();
		ListIterator lit = this.getTransitions().listIterator();
		while (lit.hasNext()) {
			// walk through transitions
			ExtendedTransition trans = (ExtendedTransition) lit.next();
			// TODO: Peter, please check whether this is in fact what you want
			// to do?
			// asking avoids the exception below (Anne)
			if (trans.isInvisibleTask() == false) {
				// try {
				String activityName = trans.getLogEvent().getModelElementName();
				ExtendedActivity activity;
				if (activities.get(activityName) == null) {
					activity = new ExtendedActivity(activityName);
					activities.put(activityName, activity);
				} else {
					activity = (ExtendedActivity) activities.get(activityName);
				}
				// connect transition to activity
				trans.setAssociatedActivity(activity);
			} // catch (NullPointerException ex) {
			// //NullPointerException can occur when invisible transitions exist
			// //within the Petri net
			// ex.printStackTrace();
			// }
		}
	}

	/**
	 * Copy Constructor mimics the clone method and will be called by sub
	 * classes when the visualization state has changed in
	 * PerformanceAnalysisResult. Note that a deep copy with respect to the
	 * Petri net elements is created, i.e., the places, transitions and edges
	 * will be different objects afterwards. Nevertheless, the list of selected
	 * instances and the associated extended log are reassigned and do not get
	 * cloned.
	 * 
	 * @param copyTemplate
	 *            The Petri net containing all the diagnostic information that
	 *            should be preserved.
	 */
	public ExtendedPetriNet(ExtendedPetriNet copyTemplate) {
		super(copyTemplate);
		// keep also the selection status
		currentlySelectedInstances = copyTemplate.currentlySelectedInstances;
	}

	/**
	 * {@inheritDoc} Produces an {@link ExtendedTransition ExtendedTransition}.
	 */
	protected Transition makeTransition(Transition template,
			ReplayedPetriNet targetNet, ArrayList caseIDs) {

		return new ExtendedTransition(template, targetNet, caseIDs);
	}

	/**
	 * {@inheritDoc} Produces an {@link ExtendedPlace ExtendedPlace}.
	 */
	protected Place makePlace(Place template, ReplayedPetriNet targetNet,
			ArrayList caseIDs) {

		return new ExtendedPlace(template, targetNet, caseIDs);
	}

	/**
	 * {@inheritDoc} Produces an {@link ExtendedPNEdge ExtendedPNEdge}.
	 */
	protected PNEdge makeEdge(PNEdge template, Place sourceNode,
			Transition targetNode, ReplayedPetriNet targetNet, ArrayList caseIDs) {

		return new ExtendedPNEdge((ExtendedPlace) sourceNode,
				(ExtendedTransition) targetNode, caseIDs);
	}

	/**
	 * {@inheritDoc} Produces a {@link ExtendedPNEdge ExtendedPNEdge}.
	 */
	protected PNEdge makeEdge(PNEdge template, Transition sourceNode,
			Place targetNode, ReplayedPetriNet targetNet, ArrayList caseIDs) {

		return new ExtendedPNEdge((ExtendedTransition) sourceNode,
				(ExtendedPlace) targetNode, caseIDs);
	}

	// /////// VISUALIZATION SECTION //////////

	/**
	 * This is the transition writing part of the writeToDot procedure (refer to
	 * it for further information).
	 * 
	 * @param bw
	 *            The writer used by the framework to create the temporary dot
	 *            file.
	 * @throws IOException
	 *             If writing to the writer fails.
	 */
	protected void writeTransitionsToDot(Writer bw) throws IOException {
		// No performance information drawn in transitions, so write transitions
		// to dot normally
		Iterator it = this.getTransitions().iterator();
		while (it.hasNext()) {
			ExtendedTransition myExtendedTransition = (ExtendedTransition) (it
					.next());
			String label = myExtendedTransition.getIdentifier();
			try {
				bw.write("t"
						+ myExtendedTransition.getNumber()
						+ " [shape=\"box\",label=\""
						+ label
						+ "\""
						+ (myExtendedTransition.getLogEvent() != null ? ""
								: "style=\"filled\"") + "];\n");
				// connect Petri net nodes to later grappa components
				nodeMapping.put(new String("t"
						+ myExtendedTransition.getNumber()),
						myExtendedTransition);
			} catch (Exception ex) {
				Message.add("Failure while updating the visualization.\n"
						+ ex.toString(), 2);
				ex.printStackTrace();
			}
		}

	}

	/**
	 * This is the place writing part of the writeToDot procedure (refer to it
	 * for further information), which gets affected if the tokenCounterOption
	 * evaluates to true.
	 * 
	 * @param bw
	 *            The writer used by the framework to create the temporary dot
	 *            file.
	 * @throws IOException
	 *             If writing to the writer fails.
	 */
	protected void writePlacesToDot(Writer bw) throws IOException {
		// Draw places according to their waiting time level
		try {
			Iterator it = this.getPlaces().iterator();
			while (it.hasNext()) {
				ExtendedPlace p = (ExtendedPlace) (it.next());
				bw
						.write("p"
								+ p.getNumber()
								+ " [shape=\"circle\",color=\"black\","
								+ determinePlaceColor(p)
								+ ",style=\"filled\",label=\"\",fontcolor=\"black\"];\n");

				nodeMapping.put(new String("p" + p.getNumber()), p);
			}
		} catch (Exception ex) {
			Message.add("Failure while updating the visualization.\n"
					+ ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * This is the edge writing part of the {@link #writeToDot writeToDot}
	 * procedure. This part of the visualization gets affected by the
	 * probabilities that are displayed at XOR-splits.
	 * 
	 * @param bw
	 *            the writer used by the framework to create the temporary dot
	 *            file
	 * @throws IOException
	 *             if writing to the writer fails
	 * 
	 * @todo Peter: figure out how to place the probabilities at the tail of the
	 *       edge
	 */
	protected void writeEdgesToDot(Writer bw) throws IOException {
		try {
			Iterator it = this.getEdges().iterator();
			while (it.hasNext()) {
				ExtendedPNEdge e = (ExtendedPNEdge) (it.next());
				if (e.isPT()) {
					ExtendedPlace p = (ExtendedPlace) e.getSource();
					Transition t = (Transition) e.getDest();
					if (p.getOutEdges().size() > 1) {
						// for all edges coming from places with more than one
						// outgoing edge, display probability in 2 decimal
						// digits
						double prob = e.getProbability(
								currentlySelectedInstances, p
										.getTotalOutEdgeFrequency(
												currentlySelectedInstances,
												advancedSettings[1],
												failedInstances),
								advancedSettings[1], failedInstances);
						DecimalFormat digits = new DecimalFormat();
						digits.setMaximumFractionDigits(2);
						digits.setMinimumFractionDigits(2);
						String chance = digits.format(prob);
						// replace commas with dots, since dot forces use of
						// US-Locale
						chance = chance.replace(",", ".");
						bw.write("p" + p.getNumber() + " -> t" + t.getNumber()
								+ " [label=" + chance + "];\n");
					} else {
						// write a normal edge
						bw.write("p" + p.getNumber() + " -> t" + t.getNumber()
								+ ";\n");
					}
				} else {
					// write a normal edge
					Place p = (Place) e.getDest();
					Transition t = (Transition) e.getSource();
					bw.write("t" + t.getNumber() + " -> p" + p.getNumber()
							+ ";\n");
				}
			}
		} catch (Exception ex) {
			Message.add("Failure while updating the visualization.\n"
					+ ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * Method which determines the color that the place p, should get depending
	 * on its average waiting time. This color is returned as String in dot-form
	 * 
	 * @param p
	 *            ExtendedPlace: the place to be colored
	 * @return String
	 */
	private String determinePlaceColor(ExtendedPlace p) {
		String temp = "fillcolor=";
		// paint places according to their waiting time level:
		double bnd0 = 0, bnd1 = 0, wait = 0;
		Color col0, col1, col2;
		if (p.hasSettings()) {
			bnd0 = ((Double) p.getBounds().get(0)).doubleValue() * timeDivider;
			bnd1 = ((Double) p.getBounds().get(1)).doubleValue() * timeDivider;
			col0 = (Color) p.getColors().get(0);
			col1 = (Color) p.getColors().get(1);
			col2 = (Color) p.getColors().get(2);
		} else {
			bnd0 = ((Double) bounds.get(0)).doubleValue() * timeDivider;
			bnd1 = ((Double) bounds.get(1)).doubleValue() * timeDivider;
			col0 = (Color) levelColors.get(0);
			col1 = (Color) levelColors.get(1);
			col2 = (Color) levelColors.get(2);
		}
		try {
			p.calculateMetrics(currentlySelectedInstances, advancedSettings[1],
					failedInstances);
			wait = (p.getMeanWaitingTime());
		} catch (Exception ex) {
		}
		// array to store HSB values in
		float[] hsb = new float[3];
		if (wait <= bnd0) {
			Color.RGBtoHSB(col0.getRed(), col0.getGreen(), col0.getBlue(), hsb);
			temp += "\"" + hsb[0] + "," + hsb[1] + "," + hsb[2] + "\"";
		} else if (wait <= bnd1) {
			Color.RGBtoHSB(col1.getRed(), col1.getGreen(), col1.getBlue(), hsb);
			temp += "\"" + hsb[0] + "," + hsb[1] + "," + hsb[2] + "\"";
		} else if (wait > bnd1) {
			// final one
			Color.RGBtoHSB(col2.getRed(), col2.getGreen(), col2.getBlue(), hsb);
			temp += "\"" + hsb[0] + "," + hsb[1] + "," + hsb[2] + "\"";
		} else {
			Color.RGBtoHSB(col0.getRed(), col0.getGreen(), col0.getBlue(), hsb);
			temp += "\"" + hsb[0] + "," + hsb[1] + "," + hsb[2] + "\"";
		}
		return temp;
	}

	// ///////////////////////SET METHODS//////////////////////////

	/**
	 * Sets the timeSort to tim
	 * 
	 * @param tim
	 *            String
	 */
	public void setTimeDivider(long tim) {
		timeDivider = tim;
	}

	/**
	 * Sets bounds to bnds
	 * 
	 * @param bnds
	 *            ArrayList
	 */
	public void setBounds(ArrayList bnds) {
		bounds = bnds;
	}

	/**
	 * Sets levelColors to cols
	 * 
	 * @param cols
	 *            ArrayList
	 */
	public void setLevelColors(ArrayList cols) {
		levelColors = cols;
	}

	/**
	 * Sets advanced settings to settings
	 * 
	 * @param settings
	 *            int[]
	 */
	public void setAdvancedSettings(int[] settings) {
		advancedSettings = settings.clone();
	}

	/**
	 * Sets failed instances
	 * 
	 * @param failedInstances
	 *            HashSet
	 */
	public void setFailedInstances(HashSet failedInstances) {
		this.failedInstances = failedInstances;
	}
}
