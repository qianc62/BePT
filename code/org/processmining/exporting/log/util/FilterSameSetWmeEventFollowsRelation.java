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

package org.processmining.exporting.log.util;

import java.io.IOException;
import java.util.TreeSet;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;
import org.processmining.framework.log.rfb.AuditTrailEntryListImpl;
import org.processmining.framework.log.rfb.ProcessInstanceImpl;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

/**
 * <p>
 * Title: Filter per Same Set of Workflow Model Elements and Events with Follows
 * Relation
 * </p>
 * <p>
 * Description: This class stores a process instance. Any other process instance
 * X is equal to the stored process instance Y whenever X and Y have the same
 * set of follows relation with respect to workflow model elements and event
 * types.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class FilterSameSetWmeEventFollowsRelation implements MyProcessInstance {

	private ProcessInstance processInstance = null;
	private TreeSet setFollowsRelations = null;

	/**
	 * Constructor method.
	 * 
	 * @param pi
	 *            process instance to be stored.
	 */
	public FilterSameSetWmeEventFollowsRelation(ProcessInstance pi,
			int numberSimilarPIS) {
		if (pi != null) {
			AuditTrailEntryListImpl ates = null;

			try {
				ates = new AuditTrailEntryListImpl();

				for (int i = 0; i < pi.getAuditTrailEntryList().size(); i++) {
					AuditTrailEntry ate = new AuditTrailEntryImpl();
					ate.setElement(pi.getAuditTrailEntryList().get(i)
							.getElement());
					ate.setType(pi.getAuditTrailEntryList().get(i).getType());
					ates.append(ate);
				}
			} catch (IOException ioe) {
				System.err
						.println("Could not create an AuditTrailEntryListImpl in constructor of the class org.processmining.exporting.log.util.FilterPerWorkflowModelElementAndEvent!");
			}

			if (pi.getAttributes().containsKey(
					FilterPerWorkflowModelElementAndEvent.IDENTIFIERS)) {
				processInstance = new ProcessInstanceImpl(pi.getProcess(),
						ates, pi.getModelReferences());
				processInstance.setDataAttributes(pi.getDataAttributes());
				processInstance.setName(pi.getName());
				processInstance.setDescription(pi.getDescription());

			} else {

				processInstance = new ProcessInstanceImpl(pi.getProcess(),
						ates, pi.getModelReferences());
				processInstance.setDataAttributes(new DataSection());
				processInstance.setName(pi.getName());
				processInstance.setDescription(pi.getDescription());
				addGroupedPiIdentifier(pi.getName());
			}

			setNumberSimilarPIs(Integer.toString(numberSimilarPIS));
			this.setFollowsRelations = buildFollowsRelations(processInstance);

		} else {
			throw new NullPointerException("pi equals null!");
		}

	}

	public static TreeSet buildFollowsRelations(ProcessInstance pi) {
		TreeSet followsRelations = new TreeSet();
		AuditTrailEntryList ates = pi.getAuditTrailEntryList();

		if (ates.size() > 1) {
			String relation = null;
			for (int i = 1; i < ates.size(); i++) {
				try {
					relation = ates.get(i - 1).getElement().trim()
							+ ates.get(i - 1).getType().trim() + " > "
							+ ates.get(i).getElement().trim()
							+ ates.get(i).getType().trim();

					followsRelations.add(relation);
				} catch (Exception e) {
					// do nothing because the "for" loop makes sure that we do
					// not go beyond the boundaries of the list!
				}
			}
		}
		return followsRelations;
	}

	/**
	 * @return The number of similar process instance.
	 */
	public int getNumberSimilarPIs() {
		return Integer.parseInt((String) processInstance.getAttributes().get(
				MethodsForWorkflowLogDataStructures.NUM_SIMILAR_INSTANCES));
	}

	/**
	 * 
	 * @return The set of follows relations for this process instance.
	 */

	public TreeSet getSetFollowsRelations() {
		return setFollowsRelations;
	}

	private void setNumberSimilarPIs(String number) {
		processInstance.getAttributes().put(
				MethodsForWorkflowLogDataStructures.NUM_SIMILAR_INSTANCES,
				number);

	}

	/**
	 * Increases the number of similar process instance.
	 */
	public void increaseNumberSimilarPIs(int increase) {

		int numberSimilarPIs = getNumberSimilarPIs();
		numberSimilarPIs += increase;
		setNumberSimilarPIs(Integer.toString(numberSimilarPIs));
	}

	/**
	 * Checks if a process instance Y is equal to the process instance X that is
	 * stored at the current object. Note that Y is EQUAL to X whenever the set
	 * of follows relations for all the workflow model elements, and their
	 * correponding event types, are the same at both process instances X and Y.
	 * 
	 * @param obj
	 *            object that contains a process instance to be compared to the
	 *            process instance in the current object.
	 * @return true if the process instances are the same, false otherwise.
	 */
	public boolean equals(Object obj) {

		FilterSameSetWmeEventFollowsRelation toCompare = (FilterSameSetWmeEventFollowsRelation) obj;

		if (toCompare == null) {
			return false;
		}

		return this.setFollowsRelations.equals(toCompare
				.getSetFollowsRelations());

	}

	/**
	 * The stored process instance is also kept in a TreeSet whose elements are
	 * "'workflow model element'  'event type'".
	 * 
	 * @return the hashcode returned by the hashcode method as defined in
	 *         TreeSet.
	 */

	public int hashCode() {
		return setFollowsRelations.hashCode();
	}

	/**
	 * @return The process instance stored at this object.
	 */
	public ProcessInstance getPI() {
		return processInstance;
	}

	/**
	 * Sets the process instance stored at this object.
	 * 
	 * @return boolean True if the pi was successfully set. False otherwise. A
	 *         pi is successfully set if it contains the same set of follows
	 *         relations as the current pi of this object.
	 */
	public boolean setPI(ProcessInstance pi) {
		TreeSet setRelationNewPi = buildFollowsRelations(pi);

		if (!this.setFollowsRelations.equals(setRelationNewPi)) {
			return false;
		}
		this.processInstance = pi;

		return true;
	}

	public void addGroupedPiIdentifier(String idToAppend) {
		if (processInstance.getAttributes().containsKey(
				FilterPerWorkflowModelElementAndEvent.IDENTIFIERS)) {
			String identifiers = (String) processInstance.getAttributes().get(
					FilterPerWorkflowModelElementAndEvent.IDENTIFIERS);
			identifiers += ", " + idToAppend;
			processInstance.getAttributes().put(
					FilterPerWorkflowModelElementAndEvent.IDENTIFIERS,
					identifiers);
		} else {
			processInstance.getAttributes().put(
					FilterPerWorkflowModelElementAndEvent.IDENTIFIERS,
					idToAppend);
		}
	}
}
