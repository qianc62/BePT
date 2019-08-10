/**
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *    Copyright (c) 2003-2006 TU/e Eindhoven
 *    by Eindhoven University of Technology
 *    Department of Information Systems
 *    http://is.tm.tue.nl
 *
 ************************************************************************/

package org.processmining.analysis.decisionmining;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.processmining.analysis.decisionmining.DecisionPointContext.AttributeSelectionScope;
import org.processmining.analysis.petrinet.cpnexport.CpnUtils;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Records additional information related to the decision points of the
 * connected process model for one Process instance (i.e., records which
 * alternative paths have been taken in this trace).
 * 
 * @author arozinat
 */
public class DecisionMiningLogTrace {

	/**
	 * The process instance that is represented by this log trace.
	 */
	private ProcessInstance myInstance;

	/**
	 * Contains the amount of times each branch of each decision point has been
	 * taken (can be more than one in the presence of loops).
	 */
	private HashMap myDecisions;

	/**
	 * Create a diagnostic log trace by copying the ordinary process instance.
	 * 
	 * @param pi
	 *            the template process instance used to create the diagnostic
	 *            log trace
	 */
	public DecisionMiningLogTrace(ProcessInstance pi) {
		myInstance = pi;
	}

	/**
	 * Retrieves the process instance represented by this log trace.
	 * 
	 * @return the belonging process instance from the log
	 */
	public ProcessInstance getProcessInstance() {
		return myInstance;
	}

	/**
	 * Initializes the diagnostic entry for each branch of each decision point
	 * with <code>0</code>.
	 * 
	 * @param decisionPoints
	 *            the list of decision points for which the taken branches will
	 *            be recorded
	 */
	public void initDiagnosticDataStructures(List<DecisionPoint> decisionPoints) {
		myDecisions = new HashMap();
		Iterator allDecisionPoints = decisionPoints.iterator();
		while (allDecisionPoints.hasNext()) {
			DecisionPoint current = (DecisionPoint) allDecisionPoints.next();
			Iterator currentBranches = current.getTargetConcept().iterator();
			while (currentBranches.hasNext()) {
				DecisionCategory branch = (DecisionCategory) currentBranches
						.next();
				// put only global ID string
				myDecisions.put(branch.getID(), new Integer(0));
			}
		}
	}

	/**
	 * Increment the corresponding entry by the specified value.
	 * 
	 * @param branchName
	 *            the name of the decision branch to be updated
	 * @param value
	 *            the value to be added to the current value
	 * @throws Exception
	 *             in the case that the specified entry is missing
	 */
	public void addDecisionOccurrences(String branchName, Integer value)
			throws Exception {
		// check existence of entry
		if (myDecisions.containsKey(branchName)) {
			// retrieve the corrensponding Item from token counter
			int currentAmount = ((Integer) myDecisions.get(branchName))
					.intValue();
			int incrementValue = value.intValue();
			// create updated item
			Integer newAmount = new Integer(currentAmount + incrementValue);
			// modify the corresponding entry
			myDecisions.put(branchName, newAmount);
		} else {
			throw new Exception(
					"addDecisionOccurrences could not find an entry\nat process instance: "
							+ myInstance.getName() + "\nfor decision branch: "
							+ branchName + ".");
		}
	}

	/**
	 * The increment of the corresponding missingTokens entry is assumed to be
	 * 1.
	 * 
	 * @param branchName
	 *            the name of the decision branch to be updated
	 */
	public void incrementDecisionOccurrences(String branchName)
			throws Exception {
		addDecisionOccurrences(branchName, new Integer(1));
	}

	/**
	 * Returns the name of this process instance.
	 */
	public String toString() {
		return myInstance.getName();
	}

	/**
	 * Creates a learning instance that can be used by a weka data mining
	 * algorithm. Note that loop semantics are not yet taken into account,
	 * therefore each trace is exactly represented by one learning instance.
	 * 
	 * @param dataset
	 *            the enclosing data set
	 * @param attInfoSize
	 *            the number of attributes (including the class attribute)
	 *            specified by the instance
	 * @return the learning instance representing this log trace
	 */
	public Instance makeInstance(Instances dataset, int attInfoSize,
			DecisionCategory branch, DecisionMiningLogReader log,
			DecisionPointContext context) {
		Instance instance = new Instance(attInfoSize);

		// provide global attribute values
		Map<String, String> piData = myInstance.getAttributes();
		evaluateDataForInstance(piData, instance, dataset);

		// provide ATE attribute values
		Iterator<AuditTrailEntry> ates = myInstance.getAuditTrailEntryList()
				.iterator();
		while (ates.hasNext()) {
			AuditTrailEntry ate = ates.next();
			Map<String, String> ateData = ate.getAttributes();
			evaluateDataForInstance(ateData, instance, dataset);

			// Which logEvent happened?
			LogEvent le = log.findLogEvent(ate);
			// check whether trace should be further traversed
			// (or whether learning instance should be build from current state)
			if (branch.contains(le)
					&& ((context.getAttributeSelectionScope() == AttributeSelectionScope.ALL_BEFORE) || (context
							.getAttributeSelectionScope() == AttributeSelectionScope.JUST_BEFORE))) {
				// stop with data attributes up to current state of trace
				break;
			}
		}
		// make enclosing dataset known to this instance
		instance.setDataset(dataset);
		return instance;
	}

	/**
	 * Helper method for reading the mapp of data attributes (key,value) pairs,
	 * and adding them to the current learning instance.
	 * 
	 * @param data
	 *            the map of data attribues
	 * @param instance
	 *            the current learning instance
	 * @param dataset
	 *            the current learning problem (contains attribute info)
	 */
	private void evaluateDataForInstance(Map<String, String> data,
			Instance instance, Instances dataset) {
		Iterator<Entry<String, String>> dataIterator = data.entrySet()
				.iterator();
		while (dataIterator.hasNext()) {
			Entry<String, String> currentDataEntry = dataIterator.next();
			String attName = currentDataEntry.getKey();
			String attValue = currentDataEntry.getValue();

			// replace white spaces and special characters by underscores both
			// in name and value
			attName = CpnUtils.replaceSpecialCharacters(attName);
			attValue = CpnUtils.replaceSpecialCharacters(attValue);

			// check whether current attribute is contained in relevant
			// attributes
			Attribute wekaAtt = dataset.attribute(attName);
			if (wekaAtt != null) {
				if (wekaAtt.isNominal() == true) {
					// corresponding string can be directly provided
					instance.setValue(wekaAtt, attValue);
				} else if (wekaAtt.isNumeric() == true) {
					// value must be converted into double first
					try {
						double doubleAttValue = (new Double(attValue))
								.doubleValue();
						instance.setValue(wekaAtt, doubleAttValue);
					} catch (NumberFormatException ex) {
						// the attribute did not contain a parsable number
						Message.add("attribute " + attName
								+ " appears not to be a numeric attribute!", 2);
					}
				}
			}
		}
	}
}
