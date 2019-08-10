package org.processmining.analysis.pdm.recommendation;

import java.io.*;
import java.util.*;

import javax.swing.*;

import org.processmining.framework.log.*;
import org.processmining.framework.models.pdm.*;
import org.processmining.framework.models.recommendation.*;
import org.processmining.framework.ui.*;
import org.processmining.analysis.pdm.PDMAnalysisUI;
import org.processmining.analysis.AnalysisInputItem;

/**
 * 
 * @author Irene Vanderfeesten
 */
public class PDMMDPRecommendation extends PDMMDPAcceptor implements
		RecommendationProvider {

	private String name; // the name of the product data model
	private PDMMDPRecommendationUI ui; // the user interface linking to this
	// class
	private PDMModel model; // the product data model on which the
	// recommendations are based
	private PDMStateSpace statespace; // the statespace based on the product

	// data model

	public PDMMDPRecommendation() {
	}

	protected JComponent analyse(PDMStateSpace statespace) {
		this.statespace = statespace;
		this.model = statespace.getPDMModel();
		ui = new PDMMDPRecommendationUI(statespace, this);
		return ui;
	}

	/**
	 * Returns a description or link to a description webpage of this plugin.
	 * 
	 * @return String
	 */
	public String getHtmlDescription() {
		return "http://is.tm.tue.nl/staff/ivanderfeesten/ProM/documentation/PDMMDPrecommendations.htm";
	}

	/**
	 * Returns the name of this plugin.
	 * 
	 * @return String
	 */
	public String getName() {
		return "PDM MDP Recommendations";
	}

	public void requestRestart(String contributer, String scale) {
	}

	public void handleCompletedExecution(ProcessInstance instance) {
	}

	/**
	 * Calculates the list of recommendation for the query (send by Declare)
	 * 
	 * @param query
	 *            RecommendationQuery
	 * @return RecommendationResult
	 * @throws IOException
	 * @throws Exception
	 */
	public RecommendationResult getRecommendation(RecommendationQuery query)
			throws IOException, Exception {
		Vector strategy = statespace.getStrategy();
		RecommendationResult result = new RecommendationResult(query.getId(),
				0.0);
		// First, write the query to the user interface
		ui.writeQuery(query);
		Message.add("<PDMRecommendationQuery>", Message.TEST);

		int numStates = statespace.getNumberOfStates();

		// Secondly, determine the corresponding state in the state space and
		// derive the recommendation from the strategy Vector.
		// Get the data elements filled in by the user in Declare
		Map<String, String> caseData = query.getProcessInstanceData();
		HashSet avElts = new HashSet();
		HashMap allElts = model.getDataElements();
		Object[] dataElts = allElts.values().toArray();
		for (int i = 0; i < dataElts.length; i++) {
			PDMDataElement data = (PDMDataElement) dataElts[i];
			String mapElt = caseData.get(data.getID());
			if (!(mapElt.matches(""))) {
				avElts.add(data);
			}
		}
		// if there are no available data elements recommend to execute the
		// "initial" activity
		if (avElts.isEmpty()) {
			Recommendation rec = new Recommendation();
			rec.setTask("Initial");
			rec.setDoExpectedValue(1.0);
			rec.setDontExpectedValue(0.0);
			result.add(rec);
		}
		// else, determine the state of the case by looking at the operations
		// that have
		// been executed (either successful or unsuccessful). These operations
		// are put in
		// one hashset since Declare can not distinguish between successful or
		// failed
		// operations.
		else {
			List caseAuditTrail = query.getAuditTrail(); // List of audit trail
			// entries
			HashSet execOps = new HashSet();

			// first add the operations that have produced input data elements
			HashMap leafs = model.getLeafElements();
			Object[] leafElts = leafs.values().toArray();
			for (int i = 0; i < leafElts.length; i++) {
				PDMDataElement d = (PDMDataElement) leafElts[i];
				if (avElts.contains(d)) {
					HashSet opsOut = model.getOperationsWithOutputElement(d);
					Iterator it = opsOut.iterator();
					PDMOperation op = (PDMOperation) it.next();
					execOps.add(op);
				}
			}
			// then, find all completed operations and add them to the set of
			// executed operations.
			// Note that both successful and failed operations are added to this
			// set.
			for (int i = 0; i < caseAuditTrail.size(); i++) {
				AuditTrailEntry ate = (AuditTrailEntry) caseAuditTrail.get(i);

				if (ate.getType().equals("complete")) {
					String str = ate.getElement();
					if (!(str.matches("Initial"))) {
						PDMOperation op = model.getOperation(str);
						execOps.add(op);
					}
				}
			}
			// Find the corresponding state in the statespace by comparing the
			// operations in the sets of executed and failed operations and by
			// comparing the available data elements
			HashSet states = statespace.getStates();
			Iterator its = states.iterator();
			while (its.hasNext()) {
				PDMState state = (PDMState) its.next();
				HashSet stateOps = new HashSet();
				HashSet exec = state.getExecutedOperationSet();
				HashSet failed = state.getFailedOperationSet();
				Iterator it2 = exec.iterator();
				while (it2.hasNext()) {
					PDMOperation op = (PDMOperation) it2.next();
					stateOps.add(op);
				}
				Iterator it3 = failed.iterator();
				while (it3.hasNext()) {
					PDMOperation op = (PDMOperation) it3.next();
					stateOps.add(op);
				}
				// when the right state in the state space is found read the
				// best
				// decision from the strategy and make a new recommendation
				if ((execOps.equals(stateOps))
						&& (avElts.equals(state.getDataElementSet()))) {
					if (state.getID() < numStates) {
						TreeSet<Double> sortedSet = new TreeSet();
						Vector vec = (Vector) strategy.get(state.getID());
						int size = vec.size();

						// Evaluate the conditions of all elements in the
						// strategy. Remove if the condition is not satisfied
						Vector vec2 = (Vector) vec.clone();
						/*
						 * for (int i=0; i<size; i++){ Vector dec = (Vector)
						 * vec.get(i); String str = (String) dec.get(0);
						 * PDMOperation op = model.getOperation(str); boolean
						 * executable = op.evaluateConditions(caseData);
						 * if(!executable){ vec2.remove(dec); } }
						 */int size2 = vec2.size();
						for (int i = 0; i < size2; i++) {
							Vector dec = (Vector) vec2.get(i);
							Double c = (Double) dec.get(1);
							sortedSet.add(c);
						}

						// Get the first element of the sorted set; this will be
						// the recommended task with value 1.0
						if (!(sortedSet.size() == 0)) {
							Double dd = sortedSet.first();
							for (int i = 0; i < size2; i++) {
								Vector dec = (Vector) vec2.get(i);
								Double c = (Double) dec.get(1);
								if (c.equals(dd)) {
									String str = (String) dec.get(0);
									Recommendation rec = new Recommendation();
									rec.setTask(str);
									rec.setDoExpectedValue(1.0);
									rec.setDontExpectedValue(0.0);
									rec.setRationale(c.toString());
									result.add(rec);
									Message.add("<OptimalValue = " + c + " >",
											Message.TEST);
								}
							}
							sortedSet.remove(dd);
							// Get the other tasks that are enabled. They will
							// get a recommendation with value 0.0
							Iterator it = sortedSet.iterator();
							int ii = 1;
							while (it.hasNext()) {
								Double d2 = (Double) it.next();
								for (int i = 0; i < size2; i++) {
									Vector dec = (Vector) vec2.get(i);
									Double c = (Double) dec.get(1);
									if (c.equals(d2)) {
										String str = (String) dec.get(0);
										Double weight = 1.0 - ((ii) * (1.0 / size));
										Recommendation rec = new Recommendation();
										rec.setTask(str);
										rec.setDoExpectedValue(weight);
										rec.setDontExpectedValue(0.0);
										rec.setRationale(c.toString());
										result.add(rec);
										ii++;
									}
								}
							}
							vec2.clear();
						}
					}
				}
			}
		}
		Message.add("received query with id " + query.getId());
		Message.add("query " + query.getId() + " answered:");
		// Finally, write the result of the query to the user interface
		ui.writeResult(result);
		Message.add("<NumberOfRecommendations = " + result.size() + " >",
				Message.TEST);
		Message.add("</PDMRecommendationQuery>", Message.TEST);

		return result;
	}

	public void signalPickedResult(RecommendationResult recommendationResult,
			int _int) {
	}

	public void signalPickedResult(RecommendationResult result,
			Recommendation picked) {
		signalPickedResult(result, (picked == null ? -1 : result
				.indexOf(picked)));
	}

	public void requestClose() throws Exception {
		// Added due to interface change. This method can be called by a
		// recommendation requestor and the result should
		// be that the application closes down.
		// write a message to both the message bar and to the system prompt
		Message
				.add("Closed the application as requested by the recommendation service.");
		System.out
				.println("Closed the application as requested by the recommendation service.");
		// Ask ProM to quit
		MainUI.getInstance().quit();
	}

}
