/**
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.processmining.analysis.petrinet.cpnexport.CpnUtils;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.hlprocess.att.HLAttributeManager;
import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;

/**
 * Extends a given event log by information related to the decision point
 * analysis. <br>
 * Note that the current implementation keeps the whole log in memory. In order
 * to support also the analysis of very large logs (and the availability of
 * learning instances is crucial for machine learning schemes) one should
 * consider writing this information back to another log file.
 * 
 * @author arozinat (a.rozinat@tm.tue.nl)
 */
public class DecisionMiningLogReader {

	/** The orginial log reader. */
	private LogReader myEncapsulatedLogReader;

	/** private iterator for the enhanced log reader */
	private Iterator myLogIterator;

	/**
	 * The list of {@link DecisionMingingLogTrace DecisionMiningLogTraces}.
	 * 
	 * @see #classifyLog
	 * */
	private ArrayList<DecisionMiningLogTrace> myDiagnosticLogTraces;

	/**
	 * Holds for a list of log traces for each decision category (i.e., those
	 * that have taken that branch). [key = ID string of DecisionCategory, value
	 * = ArrayList of DecisionMiningLogTraces]
	 */
	private HashMap<String, ArrayList<DecisionMiningLogTrace>> myBranchTraceMapping = new HashMap<String, ArrayList<DecisionMiningLogTrace>>();

	/**
	 * Stores the ATE data attributes that have been found in the log. [key =
	 * string of attribute name, value = DecisionAttribute object]
	 */
	private HashMap<String, DecisionAttribute> myDecisionAttributes = new HashMap<String, DecisionAttribute>();

	/**
	 * Stores the case data attributes that have been found in the log. [key =
	 * string of attribute name, value = DecisionAttribute object]
	 */
	private HashMap<String, DecisionAttribute> myGlobalAttributes = new HashMap<String, DecisionAttribute>();

	/**
	 * Stores the reference to the CPN simulation model (needs to be updated if,
	 * e.g., attribute-related information changes).
	 */
	private HLPetriNet mySimulationModel;

	/**
	 * Creates a wrapped LogReader with additional functionality.
	 * 
	 * @param logReader
	 *            the original log reader
	 */
	public DecisionMiningLogReader(LogReader logReader, HLPetriNet highLevelPN) {
		myEncapsulatedLogReader = logReader;
		mySimulationModel = highLevelPN;
	}

	// //////////////////// Public methods ////////////////////////////////////

	/**
	 * Classifies the process instances according to the alternative branches
	 * that have been taken and records the results.
	 * 
	 * @param decisionPoints
	 *            the decision points that have been found in the model
	 */
	public boolean classifyLog(List<DecisionPoint> decisionPoints) {
		myDiagnosticLogTraces = new ArrayList();
		Iterator piIterator = myEncapsulatedLogReader.instanceIterator();
		// whalk through the whole event log
		while (piIterator.hasNext()) {
			ProcessInstance pi = (ProcessInstance) piIterator.next();
			if (classifyInstance(pi, decisionPoints) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Adds the data attributes found in this log to the given simulation model.
	 * 
	 * @param the
	 *            high level process containing the high level activities where
	 *            the data attributes should be added
	 */
	public void addSimulationModelAttributes(HLPetriNet highLevelPN) {
		// walk through detected attributes and add to simulation model
		Iterator attIt = myDecisionAttributes.keySet().iterator();
		while (attIt.hasNext()) {
			DecisionAttribute att = (DecisionAttribute) myDecisionAttributes
					.get(attIt.next());
			if (HLAttributeManager.isNumeric((HLNominalAttribute) att
					.getSimulationAttribute())) {
				att.setDecisionAttributeType(DecisionAttributeType.NUMERIC);
			}
			att.createSimulationAttribute();
		}
	}

	/**
	 * Retrieves the data attributes found in the log for audit trail entries
	 * belonging to the given log event.
	 * 
	 * @param le
	 *            the log event for which the data attributes are requested
	 * @return a set of {@link DecisionAttribute} objects. In case there is no
	 *         data attribute associated to this log event the returned set will
	 *         be empty
	 */
	public Set getAttributesForLogEvent(LogEvent le) {
		Set resultSet = new HashSet();
		Iterator allAttrNames = myDecisionAttributes.keySet().iterator();
		while (allAttrNames.hasNext()) {
			String attName = (String) allAttrNames.next();
			DecisionAttribute attribute = (DecisionAttribute) myDecisionAttributes
					.get(attName);
			// only include if relevant for the given log event
			if (attribute.hasBeenObservedBy(le) == true) {
				resultSet.add(attribute);
			}
		}
		return resultSet;
	}

	/**
	 * Retrieves the global data attributes found in the log.
	 * 
	 * @return a set of {@link DecisionAttribute} objects. In case there is no
	 *         data attribute associated to this log event the returned set will
	 *         be empty
	 */
	public Set getGlobalAttributes() {
		HashSet resultList = new HashSet();
		resultList.addAll(myGlobalAttributes.values());
		return resultList;
	}

	/**
	 * Retrieves all the data attributes found in the whole log. This includes
	 * both the global case attributes and attributes provided by individual
	 * audit trail entries.
	 * 
	 * @return a set of {@link DecisionAttribute} objects. In case there is no
	 *         data attribute associated the returned set will be empty
	 */
	public Set getAttributesForWholeLog() {
		HashSet resultList = new HashSet();
		resultList.addAll(myDecisionAttributes.values());
		resultList.addAll(myGlobalAttributes.values());
		return resultList;
	}

	/**
	 * Delivers all process instances that have taken the specified branch.
	 * 
	 * @param branch
	 *            the decision category, i.e., the branch in question
	 * @return a list of log traces that have taken the given branch, the list
	 *         will be empty if none of them did
	 */
	public ArrayList getTracesInCategory(DecisionCategory branch) {
		ArrayList recordedTraces = (ArrayList) myBranchTraceMapping.get(branch
				.getID());
		// make empty list if no result for this branch
		if (recordedTraces == null) {
			recordedTraces = new ArrayList();
		}
		return recordedTraces;
	}

	/**
	 * Resets the diagnostic log reader to its start position (i.e., the
	 * iterator is placed in front of the first element).
	 */
	public void reset() {
		myLogIterator = myDiagnosticLogTraces.iterator();
	}

	/**
	 * Determines whether there is a log trace left to be read by the diagnostic
	 * log reader (i.e., the iterator is not yet located after the last
	 * element).
	 * 
	 * @return <code>true</code> if there is a trace to be read,
	 *         <code>false</code> otherwise
	 */
	public boolean hasNext() {
		return myLogIterator.hasNext();
	}

	/**
	 * Reads the current diagnostic log trace and move to the next one (i.e.,
	 * fetch the next element and place the iterator in behind it). Note that
	 * the existance of an element in that position should be checked before
	 * using logicalHasNext().
	 * 
	 * @return the current diagnostic log trace
	 */
	public DecisionMiningLogTrace next() {
		return (DecisionMiningLogTrace) myLogIterator.next();
	}

	/**
	 * Returns the LogEvent for the given audit trail entry.
	 * 
	 * @param currentAte
	 *            the audit trail entry for which the LogEvent is requested
	 * @return the LogEvent for the given audit traill entry
	 */
	public LogEvent findLogEvent(AuditTrailEntry currentAte) {
		// Which logEvent happened?
		LogEvent le = myEncapsulatedLogReader.getLogSummary().getLogEvents()
				.findLogEvent(currentAte.getElement(), currentAte.getType());
		return le;
	}

	// ////////////////////// Private methods
	// ///////////////////////////////////////

	/**
	 * Classifies the given instance according to its decisions made. The
	 * occurrence of a log event characterizing an alternative branch will be
	 * interpreted as such a decision, and recorded correspondingly.
	 * 
	 * @see DecisionPoint
	 * @see DecisionCategory
	 * 
	 * @param pi
	 *            the process instance to classify
	 * @param decisionPoints
	 *            the decision points that have been found in the model
	 * @return <code>true</code> if the decision-related information could be
	 *         successfully recorded in the diagnostic data structure of the
	 *         corresponding {@link DecisionMiningLogTrace}, <code>false</code>
	 *         otherwise
	 */
	private boolean classifyInstance(ProcessInstance pi,
			List<DecisionPoint> decisionPoints) {
		DecisionMiningLogTrace currentTrace = new DecisionMiningLogTrace(pi);
		myDiagnosticLogTraces.add(currentTrace);
		currentTrace.initDiagnosticDataStructures(decisionPoints);

		// retrieve case data
		evaluateCaseData(pi);

		// walk through current trace and check each log event
		Iterator ates = pi.getAuditTrailEntryList().iterator();
		while (ates.hasNext()) {
			AuditTrailEntry currentAte = (AuditTrailEntry) ates.next();
			// Which logEvent happened?
			LogEvent le = myEncapsulatedLogReader.getLogSummary()
					.getLogEvents().findLogEvent(currentAte.getElement(),
							currentAte.getType());

			// retrieve associated data attributes
			evaluateAsscociatedData(currentAte, le);

			Iterator allChoices = decisionPoints.iterator();
			while (allChoices.hasNext()) {
				DecisionPoint currentDP = (DecisionPoint) allChoices.next();
				Iterator branches = currentDP.getTargetConcept().iterator();
				while (branches.hasNext()) {
					DecisionCategory currentBranch = (DecisionCategory) branches
							.next();
					// if found log event characterizes a decision, record
					// this
					// in trace
					if (currentBranch.contains(le)) {
						try {
							currentTrace
									.incrementDecisionOccurrences(currentBranch
											.getID());
							ArrayList<DecisionMiningLogTrace> recordedTraces = myBranchTraceMapping
									.get(currentBranch.getID());
							// if is first recorded trace for that branch
							// -->
							// start a new list
							if (recordedTraces == null) {
								recordedTraces = new ArrayList();
							}
							if (recordedTraces.contains(currentTrace) == false) {
								recordedTraces.add(currentTrace);
								myBranchTraceMapping.put(currentBranch.getID(),
										recordedTraces);
							}
						} catch (Exception ex) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Parses the data elements attached to the given audit trail entry and
	 * attaches them to the given log event. The values of each case attribute
	 * are recorded in case it corresponds to an enumeration type.
	 * 
	 * @param ate
	 *            the audit trail entry for which the attached data is examined
	 * @param le
	 *            the log event belonging to the audit trail entry
	 */
	private void evaluateAsscociatedData(AuditTrailEntry ate, LogEvent le) {
		Map ateData = ate.getAttributes();
		Iterator allData = ateData.entrySet().iterator();
		while (allData.hasNext()) {
			Entry dataEntry = (Entry) allData.next();
			String attributeName = (String) dataEntry.getKey();
			String attributeValue = (String) dataEntry.getValue();

			// replace white spaces and special characters by underscores both
			// in name and value
			attributeName = CpnUtils.replaceSpecialCharacters(attributeName);
			attributeValue = CpnUtils.replaceSpecialCharacters(attributeValue);

			// check whether current attribute has been observed already
			DecisionAttribute attribute = (DecisionAttribute) myDecisionAttributes
					.get(attributeName);
			if (attribute == null) {
				attribute = new DecisionAttribute(attributeName,
						mySimulationModel);
			}

			// record the attribute value observed for this ATE
			attribute.addValue(attributeValue);
			// record which type of ATE has observed this attribute
			attribute.addLogEvent(le);
			// update attribute entry
			myDecisionAttributes.put(attributeName, attribute);
		}
	}

	/**
	 * Parses the data elements attached to the given process instance and
	 * remembers the values of each case attribute (in case it corresponds to an
	 * enumeration type).
	 * 
	 * @param pi
	 *            the process instance for which the attached data is examined
	 */
	private void evaluateCaseData(ProcessInstance pi) {
		Map ateData = pi.getAttributes();
		Iterator allData = ateData.entrySet().iterator();
		while (allData.hasNext()) {
			Entry dataEntry = (Entry) allData.next();
			String attributeName = (String) dataEntry.getKey();
			String attributeValue = (String) dataEntry.getValue();

			// replace white spaces and special characters by underscores both
			// in name and value
			attributeName = CpnUtils.replaceSpecialCharacters(attributeName);
			attributeValue = CpnUtils.replaceSpecialCharacters(attributeValue);

			// check whether current attribute has been observed already
			DecisionAttribute attribute = (DecisionAttribute) myGlobalAttributes
					.get(attributeName);
			if (attribute == null) {
				attribute = new DecisionAttribute(attributeName,
						mySimulationModel);
			}

			// record the attribute value observed for this ATE
			attribute.addValue(attributeValue);
			// record that this is not an ATE-wide but a global attribute (null
			// = global)
			attribute.addLogEvent(null);
			// update attribute entry
			myGlobalAttributes.put(attributeName, attribute);
		}
	}
}
