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

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;
import org.processmining.framework.log.rfb.AuditTrailEntryListImpl;
import org.processmining.framework.log.rfb.ProcessInstanceImpl;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

/**
 * <p>
 * Title: Filter per Workflow Model Element and Event
 * </p>
 * <p>
 * Description: This class stores a process instance. Its equals method is based
 * on a projection from the "workflow model element" and "event type" fields.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class FilterPerWorkflowModelElementAndEvent implements MyProcessInstance {

	public static final String IDENTIFIERS = "GroupedIdentifiers";

	private ProcessInstance processInstance = null;

	/**
	 * Constructor method.
	 * 
	 * @param pi
	 *            process instance to be stored.
	 */
	public FilterPerWorkflowModelElementAndEvent(ProcessInstance pi,
			int numberSimilarPIs) {

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

			if (pi.getAttributes().containsKey(IDENTIFIERS)) {
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

			setNumberSimilarPIs(Integer.toString(numberSimilarPIs));

		} else {
			throw new NullPointerException("pi equals null!");
		}

	}

	/**
	 * @return The number of similar process instance.
	 */
	public int getNumberSimilarPIs() {
		return Integer.parseInt((String) processInstance.getAttributes().get(
				MethodsForWorkflowLogDataStructures.NUM_SIMILAR_INSTANCES));
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
	 * Checks of a process instance is equal to the process instance that is
	 * stored at the current object. Note that a process instance is EQUAL to
	 * another one whenever all the workflow model elements, and their
	 * correponding event types, appear in the same order at both process
	 * instances.
	 * 
	 * @param obj
	 *            object that contains a process instance to be compared to the
	 *            process instance in the current object.
	 * @return true if the process instances are the same, false otherwise.
	 */
	public boolean equals(Object obj) {

		FilterPerWorkflowModelElementAndEvent toCompare = (FilterPerWorkflowModelElementAndEvent) obj;

		if (toCompare == null) {
			return false;
		}

		if (this.processInstance.getAuditTrailEntryList().size() != toCompare
				.getPI().getAuditTrailEntryList().size()) {
			return false;
		}

		for (int i = 0; i < processInstance.getAuditTrailEntryList().size(); i++) {
			try {
				if (!processInstance.getAuditTrailEntryList().get(i)
						.getElement().equals(
								toCompare.getPI().getAuditTrailEntryList().get(
										i).getElement())
						|| !processInstance.getAuditTrailEntryList().get(i)
								.getType().equals(
										toCompare.getPI()
												.getAuditTrailEntryList()
												.get(i).getType())) {
					return false;
				}
			} catch (Exception e) {
				// could not compare...
				return false;
			}
		}

		return true;
	}

	/**
	 * The hashcode is the sum of the workflow model elements' hascode and the
	 * event types' hashcodes.
	 * 
	 * @return the hashcode for the projected process instance.
	 */

	public int hashCode() {

		int hashCode = 0;

		for (int i = 0; i < processInstance.getAuditTrailEntryList().size(); i++) {
			try {
				hashCode += processInstance.getAuditTrailEntryList().get(i)
						.getElement().hashCode()
						+ processInstance.getAuditTrailEntryList().get(i)
								.getType().hashCode();
			} catch (Exception e) {
				// Do nothing because boundaries are respected in the FOR loop.
			}
		}

		return hashCode;

	}

	/**
	 * @return The process instance stored at this object.
	 */
	public ProcessInstance getPI() {
		return processInstance;
	}

	public void addGroupedPiIdentifier(String idToAppend) {
		if (processInstance.getAttributes().containsKey(IDENTIFIERS)) {
			String identifiers = (String) processInstance.getAttributes().get(
					IDENTIFIERS);
			identifiers += ", " + idToAppend;
			processInstance.getAttributes().put(IDENTIFIERS, identifiers);
		} else {
			processInstance.getAttributes().put(IDENTIFIERS, idToAppend);
		}
	}

}
