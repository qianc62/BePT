package org.processmining.analysis.pdm.recommendation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JComponent;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.pdm.PDMDataElement;
import org.processmining.framework.models.pdm.PDMModel;
import org.processmining.framework.models.pdm.PDMOperation;
import org.processmining.framework.models.recommendation.Recommendation;
import org.processmining.framework.models.recommendation.RecommendationProvider;
import org.processmining.framework.models.recommendation.RecommendationQuery;
import org.processmining.framework.models.recommendation.RecommendationResult;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;

/**
 * 
 * @author Irene Vanderfeesten
 */
public class PDMRecommendation extends PDMAcceptor implements
		RecommendationProvider {

	private String pdm; // the name of the product data model
	private PDMRecommendationUI ui; // the user interface linking to this class
	private PDMModel model; // the product data model on which the

	// recommendations are based

	public PDMRecommendation() {
	}

	protected JComponent analyse(PDMModel model) {
		this.pdm = model.getName();
		this.model = model;
		ui = new PDMRecommendationUI(model, this);
		return ui;
	}

	/**
	 * Returns a description or link to a description webpage of this plugin.
	 * 
	 * @return String
	 */
	public String getHtmlDescription() {
		return "http://is.tm.tue.nl/staff/ivanderfeesten/ProM/documentation/PDMrecommendations.htm";
	}

	/**
	 * Returns the name of this plugin.
	 * 
	 * @return String
	 */
	public String getName() {
		return "PDM recommendations";
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
		RecommendationResult result = new RecommendationResult(query.getId(),
				0.0);
		// First, write the query to the user interface
		ui.writeQuery(query);
		Message.add("<PDMRecommendationQuery>", Message.TEST);
		// Secondly, get the available data elements
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
		// and get the already executed operations from the history of the case
		List caseAuditTrail = query.getAuditTrail(); // List of audit trail
		// entries
		HashSet executedOperations = new HashSet();
		for (int i = 0; i < caseAuditTrail.size(); i++) {
			AuditTrailEntry ate = (AuditTrailEntry) caseAuditTrail.get(i);
			if (ate.getType().equals("complete")) {
				String str = ate.getElement();
				if (!(str.matches("Initial"))) {
					PDMOperation op = model.getOperation(str);
					executedOperations.remove(op);
				}
				if (str.matches("Initial")) {
					PDMOperation op = new PDMOperation(model, "Initial");
					executedOperations.add(op);
				}
			}
		}
		// Then, calculate the recommendations.
		// If there are no tasks that have been executed the recommendation
		// should be the initial task
		if (executedOperations.isEmpty()) {
			Recommendation rec2 = new Recommendation();
			rec2.setTask("Initial");
			rec2.setDoExpectedValue(1.0);
			rec2.setDontExpectedValue(0.0);
			rec2.setRationale("Initial values leaf elements");
			result.add(rec2);
		}
		// If there is a history, then calculate the enabled operations
		// When the root element is not yet determined there are still
		// operations to be recommended
		else if (!(avElts.contains(model.getRootElement()))) {
			// Calculate the enabled operations (i.e. those operation of which
			// all input elements are in the set of available elements)
			HashSet enabledOperations = new HashSet();
			HashMap allOps = model.getOperations();
			Object[] ops = allOps.values().toArray();
			for (int i = 0; i < ops.length; i++) {
				PDMOperation op = (PDMOperation) ops[i];
				HashMap inputs = op.getInputElements();
				Object[] ins = inputs.values().toArray();
				// check whether the input element is available
				boolean enabled = true;
				int k = 0;
				while (enabled && k < ins.length) {
					PDMDataElement d = (PDMDataElement) ins[k];
					if (!(avElts.contains(d))) {
						enabled = false;
					}
					k++;
				}
				if (enabled) {
					enabledOperations.add(op);
				}
			}
			// PDMOperation operation = new PDMOperation(model,"Initial");
			// enabledOperations.add(operation);

			// remove already executed operations
			Object[] hist = caseAuditTrail.toArray();
			for (int i = 0; i < hist.length; i++) {
				AuditTrailEntry ate = (AuditTrailEntry) hist[i];
				String str = ate.getElement();
				PDMOperation op = model.getOperation(str);
				enabledOperations.remove(op);
			}
			// remove operation with input constraints that evaluate to false
			HashSet enOps = (HashSet) enabledOperations.clone();
			Iterator opIt = enOps.iterator();
			while (opIt.hasNext()) {
				PDMOperation op = (PDMOperation) opIt.next();
				// System.out.println(op.getID());
				if (!(op.evaluateConditions(caseData))) {
					enabledOperations.remove(op);
				}
			}
			// remove input operations of which the output element is already
			// determined
			HashSet leafOps = model.getLeafOperations();
			Iterator leafIt = leafOps.iterator();
			while (leafIt.hasNext()) {
				PDMOperation op = (PDMOperation) leafIt.next();
				PDMDataElement data = op.getOutputElement();
				if (avElts.contains(data)) {
					enabledOperations.remove(op);
				}
			}

			// select a recommendation function (user selection, random, low
			// cost, low duration, ... or more advanced (to be implemented))
			String selectedStrategy = ui.getSelectedStrategy();
			// Then, calculate the result of the query; select the best
			// candidate from the list of enabled operations based on the
			// indicated strategy
			if (!(enabledOperations.isEmpty())) {
				if (selectedStrategy == "User selection") {
					Iterator setIt2 = enabledOperations.iterator();
					while (setIt2.hasNext()) {
						PDMOperation op = (PDMOperation) setIt2.next();
						Recommendation rec = new Recommendation();
						rec.setTask(op.getID());
						rec.setDoExpectedValue(1.0);
						rec.setDontExpectedValue(0.0);
						rec.setRationale("");
						result.add(rec);
					}
				} else if (selectedStrategy == "Random selection (Random)") {
					result = calculateRandomSelection(enabledOperations, query);
				} else if (selectedStrategy == "Lowest cost") {
					result = calculateLowestCostSelection(enabledOperations,
							query);
				} else if (selectedStrategy == "Shortest processing time (SPT)") {
					result = calculateShortestDurationSelection(
							enabledOperations, query);
				} else if (selectedStrategy == "Shortest remaining processing time (SR)") {
					result = calculateShortestRemainingTime(enabledOperations,
							query);
				} else if (selectedStrategy == "Distance to root element (FOPNR)") {
					result = calculateDistanceToRootElement(enabledOperations,
							query);
				} else if (selectedStrategy == "Smallest failure probability") {
					result = calculateSmallestFailureProbability(
							enabledOperations, query);
				} else
					System.out
							.println("This strategy does not have an implementation!");

			}

			/*
			 * // or if there are no enabled operations and there is no history
			 * for the case, then select the initial task that gives values to
			 * all leaf elements. else if (hist.length == 0) { Recommendation
			 * rec2 = new Recommendation(); rec2.setTask("Initial");
			 * rec2.setDoExpectedValue(1.0); rec2.setDontExpectedValue(0.0);
			 * rec2.setRationale("Initial values leaf elements");
			 * result.add(rec2); }
			 */
			Message.add("received query with id " + query.getId());
			Message.add("query " + query.getId() + " answered:");
			// Finally, write the result of the query to the user interface
			ui.writeResult(result);
		}
		executedOperations.clear();
		Message.add("<NumberOfRecommendations = " + result.size() + " >",
				Message.TEST);
		Message.add("</PDMRecommendationQuery>", Message.TEST);
		return result;
	}

	/**
	 * calculateSmallesFailureProbability
	 * 
	 * @param enabledOperations
	 *            HashSet
	 * @param query
	 *            RecommendationQuery
	 * @return RecommendationResult
	 */
	private RecommendationResult calculateSmallestFailureProbability(
			HashSet enabledOperations, RecommendationQuery query) {
		RecommendationResult result = new RecommendationResult(query.getId(),
				0.0);
		double minDur = calculateSmallestFailureProbability(enabledOperations);
		Iterator it = enabledOperations.iterator();
		while (it.hasNext()) {
			PDMOperation op1 = (PDMOperation) it.next();
			double opFProb = op1.getFailureProbability();
			if (opFProb == minDur) {
				Recommendation rec = new Recommendation();
				rec.setTask(op1.getID());
				rec.setDoExpectedValue(1.0);
				rec.setDontExpectedValue(0.0);
				rec.setRationale("Failure probability = "
						+ op1.getFailureProbability());
				result.add(rec);
				// System.out.println(op1.getID() + " | 1.0 "+
				// "| Processing time = "+ op1.getDuration());
			} else {
				Recommendation rec1 = new Recommendation();
				rec1.setTask(op1.getID());
				rec1.setDoExpectedValue(0.0);
				rec1.setDontExpectedValue(0.0);
				rec1.setRationale("Processing time = " + op1.getDuration());
				result.add(rec1);
				// System.out.println(op1.getID() + " | 0.0 "+
				// "| Processing time = "+ op1.getDuration());
			}
		}
		return result;
	}

	/**
	 * calculateSmallestFailureProbability
	 * 
	 * @param enabledOperations
	 *            HashSet
	 * @return double
	 */
	private double calculateSmallestFailureProbability(HashSet enabledOperations) {
		double minFProb = 1.1;
		Iterator itOp = enabledOperations.iterator();
		while (itOp.hasNext()) {
			PDMOperation op2 = (PDMOperation) itOp.next();
			// System.out.println(op2.getID() + op2.getCost());
			double c = op2.getFailureProbability();
			// System.out.println(c);
			if (c < minFProb) {
				minFProb = c;
			}
		}
		return minFProb;

	}

	public void signalPickedResult(RecommendationResult recommendationResult,
			int _int) {
	}

	public void signalPickedResult(RecommendationResult result,
			Recommendation picked) {
		signalPickedResult(result, (picked == null ? -1 : result
				.indexOf(picked)));
	}

	/**
	 * Calculates which operation should be recommended based on a random
	 * selection
	 * 
	 * @param enabledOperations
	 *            HashSet
	 * @param query
	 *            RecommendationQuery
	 * @return RecommendationResult
	 */
	public RecommendationResult calculateRandomSelection(
			HashSet enabledOperations, RecommendationQuery query) {
		Object[] enOp = enabledOperations.toArray();
		RecommendationResult result = new RecommendationResult(query.getId(),
				0.0);
		Random r = new Random();
		int i = enOp.length;
		if (i > 0) {
			int nr = r.nextInt(i);
			PDMOperation op = (PDMOperation) enOp[nr];
			Recommendation rec = new Recommendation();
			rec.setTask(op.getID());
			rec.setRationale("Random selection - selected");
			rec.setDoExpectedValue(1.0);
			rec.setDontExpectedValue(0.0);
			result.add(rec);
			enabledOperations.remove(op);
			Iterator it = enabledOperations.iterator();
			while (it.hasNext()) {
				PDMOperation op1 = (PDMOperation) it.next();
				Recommendation rec1 = new Recommendation();
				rec1.setTask(op.getID());
				rec1.setDoExpectedValue(0.0);
				rec1.setDontExpectedValue(0.0);
				rec1.setRationale("Random selection - not selected");
				result.add(rec1);
			}
		}
		return result;
	}

	/**
	 * Calculates which operation(s) should be recommended based on selection of
	 * the lowest cost operation.
	 * 
	 * @param enabledOperations
	 *            HashSet
	 * @param query
	 *            RecommendationQuery
	 * @return RecommendationResult
	 */
	public RecommendationResult calculateLowestCostSelection(
			HashSet enabledOperations, RecommendationQuery query) {
		RecommendationResult result = new RecommendationResult(query.getId(),
				1.0);
		int minCost = calculateLowestCost(enabledOperations);
		Iterator it = enabledOperations.iterator();
		while (it.hasNext()) {
			PDMOperation op1 = (PDMOperation) it.next();
			int opCost = op1.getCost();
			if (opCost == minCost) {
				Recommendation rec = new Recommendation();
				rec.setTask(op1.getID());
				rec.setDoExpectedValue(1.0);
				rec.setDontExpectedValue(0.0);
				rec.setRationale("Cost = " + op1.getCost());
				result.add(rec);
				Message.add("<LowestCost = " + op1.getCost() + ">",
						Message.TEST);
			} else {
				Recommendation rec1 = new Recommendation();
				rec1.setTask(op1.getID());
				rec1.setDoExpectedValue(0.0);
				rec1.setDontExpectedValue(0.0);
				rec1.setRationale("Cost = " + op1.getCost());
				result.add(rec1);
			}
		}
		return result;
	}

	/**
	 * Returns the cost of the operation with the lowest cost
	 * 
	 * @param enabledOperation
	 *            HashSet
	 * @return int
	 */
	public int calculateLowestCost(HashSet enabledOperation) {
		int minCost = 1000000000;
		Iterator itOp = enabledOperation.iterator();
		while (itOp.hasNext()) {
			PDMOperation op2 = (PDMOperation) itOp.next();
			// System.out.println(op2.getID() + op2.getCost());
			int c = op2.getCost();
			// System.out.println(c);
			if (c < minCost) {
				minCost = c;
			}
		}
		return minCost;
	}

	/**
	 * Calculates which operation(s) should be recommended based on selection of
	 * the operation with the shortest processing time (SPT).
	 * 
	 * @param enabledOperations
	 *            HashSet
	 * @param query
	 *            RecommendationQuery
	 * @return RecommendationResult
	 */
	public RecommendationResult calculateShortestDurationSelection(
			HashSet enabledOperations, RecommendationQuery query) {
		RecommendationResult result = new RecommendationResult(query.getId(),
				0.0);
		int minDur = calculateShortestDuration(enabledOperations);
		Iterator it = enabledOperations.iterator();
		while (it.hasNext()) {
			PDMOperation op1 = (PDMOperation) it.next();
			int opDur = op1.getDuration();
			if (opDur == minDur) {
				Recommendation rec = new Recommendation();
				rec.setTask(op1.getID());
				rec.setDoExpectedValue(1.0);
				rec.setDontExpectedValue(0.0);
				rec.setRationale("Processing time = " + op1.getDuration());
				result.add(rec);
				// System.out.println(op1.getID() + " | 1.0 "+
				// "| Processing time = "+ op1.getDuration());
			} else {
				Recommendation rec1 = new Recommendation();
				rec1.setTask(op1.getID());
				rec1.setDoExpectedValue(0.0);
				rec1.setDontExpectedValue(0.0);
				rec1.setRationale("Processing time = " + op1.getDuration());
				result.add(rec1);
				// System.out.println(op1.getID() + " | 0.0 "+
				// "| Processing time = "+ op1.getDuration());
			}
		}
		return result;
	}

	/**
	 * Returns the processing time of the operation with the shortest processing
	 * time.
	 * 
	 * @param enabledOperation
	 *            HashSet
	 * @return int
	 */
	public int calculateShortestDuration(HashSet enabledOperation) {
		int minDur = 1000000000;
		Iterator it = enabledOperation.iterator();
		while (it.hasNext()) {
			PDMOperation op = (PDMOperation) it.next();
			int c = op.getDuration();
			if (c < minDur) {
				minDur = c;
			}
		}
		return minDur;
	}

	/**
	 * Calculates which operation(s) should be recommended based on selection of
	 * the operation with the shortest distance to the root element counted in
	 * number of operations.
	 * 
	 * @param enabledOperations
	 *            HashSet
	 * @param query
	 *            RecommendationQuery
	 * @return RecommendationResult
	 */
	private RecommendationResult calculateDistanceToRootElement(
			HashSet enabledOperations, RecommendationQuery query) {
		RecommendationResult result = new RecommendationResult(query.getId(),
				0.0);
		int minDist = 1000000000;
		Iterator it2 = enabledOperations.iterator();
		while (it2.hasNext()) {
			PDMOperation op1 = (PDMOperation) it2.next();
			int c = op1.getDistance(model);
			if (c < minDist) {
				minDist = c;
			}
		}
		Iterator it = enabledOperations.iterator();
		while (it.hasNext()) {
			PDMOperation op1 = (PDMOperation) it.next();
			int opDist = op1.getDistance(model);
			if (opDist == minDist) {
				Recommendation rec = new Recommendation();
				rec.setTask(op1.getID());
				rec.setDoExpectedValue(1.0);
				rec.setDontExpectedValue(0.0);
				rec
						.setRationale("Distance to root = "
								+ op1.getDistance(model));
				result.add(rec);
				// System.out.println(op1.getID() + " | 1.0 "+
				// "| Distance to root = "+ op1.getDistance(model));
			} else {
				Recommendation rec1 = new Recommendation();
				rec1.setTask(op1.getID());
				rec1.setDoExpectedValue(0.0);
				rec1.setDontExpectedValue(0.0);
				rec1.setRationale("Distance to root = "
						+ op1.getDistance(model));
				result.add(rec1);
				// System.out.println(op1.getID() + " | 0.0 "+
				// "| Distance to root = "+ op1.getDistance(model));
			}
		}
		return result;
	}

	/**
	 * Calculates which operation(s) should be recommended based on selection of
	 * the operation with the shortest remaining processing time, including its
	 * own processing time (= distance to root element counted in processing
	 * time).
	 * 
	 * @param enabledOperations
	 *            HashSet
	 * @param query
	 *            RecommendationQuery
	 * @return RecommendationResult
	 */
	private RecommendationResult calculateShortestRemainingTime(
			HashSet enabledOperations, RecommendationQuery query) {
		RecommendationResult result = new RecommendationResult(query.getId(),
				0.0);
		int minRemTime = 1000000000;
		Iterator it2 = enabledOperations.iterator();
		while (it2.hasNext()) {
			PDMOperation op1 = (PDMOperation) it2.next();
			int c = op1.getRemainingProcessingTime(model);
			// System.out.println(op1.getID() + " : " +
			// op1.getRemainingProcessingTime(model));
			if (c < minRemTime) {
				minRemTime = c;
			}
		}
		Iterator it = enabledOperations.iterator();
		while (it.hasNext()) {
			PDMOperation op1 = (PDMOperation) it.next();
			int opDist = op1.getRemainingProcessingTime(model);
			if (opDist == minRemTime) {
				Recommendation rec = new Recommendation();
				rec.setTask(op1.getID());
				rec.setDoExpectedValue(1.0);
				rec.setDontExpectedValue(0.0);
				rec.setRationale("Remaining processing time = "
						+ op1.getRemainingProcessingTime(model));

				result.add(rec);
				// System.out.println(op1.getID() + " | 1.0 "+
				// "| Remaining processing time = "+
				// op1.getRemainingProcessingTime(model));
			} else {
				Recommendation rec1 = new Recommendation();
				rec1.setTask(op1.getID());
				rec1.setDoExpectedValue(0.0);
				rec1.setDontExpectedValue(0.0);
				rec1.setRationale("Remaining processing time = "
						+ op1.getRemainingProcessingTime(model));

				result.add(rec1);
				// System.out.println(op1.getID() + " | 0.0 "+
				// "| Remaining processing time = "+
				// op1.getRemainingProcessingTime(model));
			}
		}
		return result;
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
