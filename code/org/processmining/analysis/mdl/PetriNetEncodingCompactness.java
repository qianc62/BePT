package org.processmining.analysis.mdl;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;

/**
 * Metric measuring model complexity based on encoding cost of the Petri net.
 * (Does not abstract from potential Start and End tasks in the model yet).
 * 
 * @author Anne Rozinat, Christian Guenther
 */
public class PetriNetEncodingCompactness extends MDLCompactnessMetric {

	protected PetriNetEncodingCompactness() {
		super("Petri-net encoding",
				"Metric measuring model complexity based on encoding cost of the Petri net.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.mdl.MDLBaseMetric#getEncodingCost(org.
	 * processmining.framework.ui.slicker.ProgressPanel)
	 */
	public int getEncodingCost(ProgressPanel progress, PetriNet aNet,
			LogReader aLog) {
		net = aNet;
		encodingCost = 0;
		int noOfEdges = net.getNumberOfEdges();
		int noOfTransitions = net.getTransitions().size();
		int noOfPlaces = net.getPlaces().size();

		// factor out the added b and e places and transitions plus the arcs
		// connecting them
		noOfEdges = noOfEdges - 2;
		noOfTransitions = noOfTransitions - 2;
		noOfPlaces = noOfPlaces - 2;
		// factor out the added (virtual) place and the edges
		noOfPlaces = noOfPlaces - 1;
		noOfEdges = noOfEdges - 2 * noOfTransitions; // transitions now reflect
		// the actual
		// transitions
		noOfEdges = noOfEdges - 2; // arc from start to virtual place, and from
		// virtual place to end

		encodingCost += 2 * getUpperBound(Math.log(noOfTransitions + 1)
				/ Math.log(2)); // = 2 * log2(noOfTransitions + 1)
		Message
				.add(
						"\n + 2 * getUpperBound(log2("
								+ (noOfTransitions + 1)
								+ "))       //  2 * getUpperBound(log2(noOfTransitions + 1))       // "
								+ (2 * getUpperBound(Math
										.log(noOfTransitions + 1)
										/ Math.log(2))), 3);
		// Message.add("\n -> 2 * (log2(" + (noOfPlaces + 1) + ") + log2(" +
		// (noOfTransitions + 1) +
		// "))      //  2 * (log2(noOfPlaces + 1) + log2(noOfTransitions + 1))        // "
		// + 2 * ((Math.log(noOfPlaces + 1) / Math.log(2)) +
		// (Math.log(noOfTransitions + 1) / Math.log(2))), 3);
		// Message.add("\n -> log2(" + (noOfPlaces + 1) +
		// ")      //  log2(noOfPlaces + 1)        // " + (Math.log(noOfPlaces +
		// 1) / Math.log(2)), 3);

		encodingCost += 2 + (noOfTransitions * (Math.log(noOfTransitions) / Math
				.log(2))); // = 2 + (noOfTransitions * log2(noOfTransitions))
		Message
				.add(
						"\n + 2 + "
								+ noOfTransitions
								+ " * log2("
								+ (noOfTransitions)
								+ ")       //  2 + (noOfTransitions * log2(noOfTransitions))       // "
								+ (2 + (noOfTransitions * (Math
										.log(noOfTransitions) / Math.log(2)))),
						3);

		encodingCost += 2 * noOfPlaces
				* (Math.log(noOfTransitions + 1) / Math.log(2)); // = 2 *
		// noOfPlaces
		// *
		// log2(noOfTransitions
		// + 1)
		Message
				.add(
						"\n + 2 * "
								+ noOfPlaces
								+ " * log2("
								+ (noOfTransitions + 1)
								+ ")       //  2 * noOfPlaces * log2(noOfTransitions + 1)       // "
								+ (2 * noOfPlaces * (Math
										.log(noOfTransitions + 1) / Math.log(2))),
						3);

		encodingCost += noOfEdges
				* (Math.log(noOfTransitions + 1) / Math.log(2)); // = noOfEdges
		// *
		// log2(noOfTransitions
		// + 1)
		Message.add("\n + " + noOfEdges + " * log2(" + (noOfTransitions + 1)
				+ ")       //  noOfEdges * log2(noOfTransitions + 1)       // "
				+ (noOfEdges * (Math.log(noOfTransitions + 1) / Math.log(2))),
				3);

		return getUpperBound(encodingCost);
	}
}
