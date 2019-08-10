package org.processmining.mining.flowermodel;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;

/**
 * Simple mining plugin that constructs a so-called "Flower model" from the
 * given event log. Can be useful as a reference model, and as a helper model if
 * no real model is available but a model is needed for some analysis technique
 * (e.g., the Decision Miner).
 * 
 * @author Anne Rozinat
 */
public class FlowerMiningPlugin implements MiningPlugin {

	public JPanel getOptionsPanel(LogSummary summary) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#mine(org.processmining.framework
	 * .log.LogReader)
	 */
	public MiningResult mine(LogReader log) {
		// build base model
		PetriNet result = new PetriNet();
		Place start = new Place("Start", result);
		Place end = new Place("End", result);
		Place middle = new Place("Middle", result);
		result.addPlace(start);
		result.addPlace(end);
		result.addPlace(middle);
		Transition first = new Transition("first", result);
		Transition last = new Transition("last", result);
		result.addTransition(first);
		result.addTransition(last);
		PNEdge startToFirst = new PNEdge(start, first);
		PNEdge firstToMiddle = new PNEdge(first, middle);
		PNEdge middleToLast = new PNEdge(middle, last);
		PNEdge lastToEnd = new PNEdge(last, end);
		result.addEdge(startToFirst);
		result.addEdge(firstToMiddle);
		result.addEdge(middleToLast);
		result.addEdge(lastToEnd);

		Iterator<LogEvent> allEvents = log.getLogSummary().getLogEvents()
				.iterator();
		LogEvent current;
		while (allEvents.hasNext()) {
			current = allEvents.next();
			Transition flowerTrans = new Transition(current, result);
			result.addTransition(flowerTrans);
			PNEdge from = new PNEdge(middle, flowerTrans);
			PNEdge to = new PNEdge(flowerTrans, middle);
			result.addEdge(from);
			result.addEdge(to);
		}
		return new FlowerResult(result, log);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "Builds a so-called Flower model for the given event log. This model is the most general model one can think of as "
				+ "it allows for any sequence of the observed activities. This can be useful as a reference model, and as a helper model if "
				+ "no real model is available but a model is needed for some analysis technique (e.g., the Decision Miner).";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Flower Model Miner";
	}

	public class FlowerResult implements MiningResult, Provider {

		protected LogReader logReader;
		protected PetriNet model;

		/**
		 * Creates a Flower model mining result based on the given model and
		 * log.
		 * 
		 * @param net
		 *            the Petri net that represents the Flower model
		 * @param log
		 *            the log that was used to construct the Flower model
		 */
		public FlowerResult(PetriNet net, LogReader log) {
			logReader = log;
			model = net;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.mining.MiningResult#getLogReader()
		 */
		public LogReader getLogReader() {
			return logReader;
		}

		/**
		 * Returns the mined flower Petri net.
		 * 
		 * @return the net
		 */
		public PetriNet getPetriNet() {
			return model;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.mining.MiningResult#getVisualization()
		 */
		public JComponent getVisualization() {
			JComponent result = new JPanel(new BorderLayout());
			result.add(model.getGrappaVisualization(), BorderLayout.CENTER);
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
		 */
		public ProvidedObject[] getProvidedObjects() {
			return new ProvidedObject[] { new ProvidedObject("Flower Model",
					new Object[] { model, logReader }) };
		}
	}

}
