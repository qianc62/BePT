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

package org.processmining.framework.models.pdm;

import java.util.*;
import java.lang.*;

/**
 * <p>
 * Title: PDMActivity *
 * <p>
 * Description: Represents an activity from a PDM Design
 * </p>
 * *
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * *
 * <p>
 * Company:
 * </p>
 * *
 * 
 * @author Irene Vanderfeesten
 * @version 1.0
 */
public class PDMActivity {

	private String activityID; // the ID of the activity
	private Integer activityNR; // the number of the activity, needed for
	// Declare
	HashMap operations = new HashMap(); // the operations of the activity

	/**
	 * Creates the activity with identifier 'id'.
	 * 
	 * @param id
	 *            String
	 */
	public PDMActivity(String id) {
		this.activityID = id;
		setActivityNR(activityID.hashCode());
	}

	public void setActivityNR(Integer in) {
		this.activityNR = in;
	}

	public Integer getActivityNR() {
		return this.activityNR;
	}

	/**
	 * Adds an operation to the list of operations of this activity
	 * 
	 * @param operation
	 *            PDMOperation
	 */
	public void addOperation(PDMOperation operation) {
		operations.put(operation.getID(), operation);
	}

	/**
	 * Returns the ID of this activity (activityID).
	 * 
	 * @return String
	 */
	public String getID() {
		return this.activityID;
	}

	/**
	 * Returns true if the list of operations contains the operation 'op'.
	 * Returns false otherwise.
	 * 
	 * @param op
	 *            PDMOperation
	 * @return Boolean
	 */
	public Boolean hasOperation(PDMOperation op) {
		Boolean result;
		result = false;
		if (!(operations.get(op.getID()) == null))
			result = true;
		return result;
	}

	/**
	 * Returns ture is there is an operation in the list of operations that
	 * contains the data element 'el' as input or as output element. Returns
	 * false otherwise.
	 * 
	 * @param el
	 *            PDMDataElement
	 * @return Boolean
	 */
	public Boolean hasDataElement(PDMDataElement el) {
		Boolean result = false;
		Object[] ops = new Object[1];
		ops = operations.values().toArray();
		// the 'ops' array now contains al operations from the hashmap
		// 'operations'
		// this is necessary because there is no construct to walk through the
		// hashmap step-by-step
		// therefore the hashmap is transferred to an array.
		int i = 0;
		while ((result == false) & (i < ops.length)) {
			PDMOperation op = (PDMOperation) ops[i];
			if (op.hasDataElement(el)) {
				result = true;
			} else
				result = false;
			i++;
		}
		return result;
	}

	/**
	 * Returns the number of operations in the list of operations.
	 * 
	 * @return Integer
	 */
	public Integer size() {
		return operations.size();
	}

	/**
	 * Returns true when this activity is connected with activity act by an
	 * overlapping data element. This is determined by walking through all data
	 * elements of this activity and checking whether activity act also has the
	 * data element (act.hasDataElement).
	 * 
	 * @param act
	 *            PDMActivity
	 * @return Boolean
	 */
	public Boolean isConnectedWith(PDMActivity act) {
		Boolean result = false;
		HashSet dataElts = new HashSet();
		dataElts = this.getDataElements();
		if (!this.equals(act)) {
			Iterator it = dataElts.iterator();
			while ((result == false) & (it.hasNext())) {
				PDMDataElement data = (PDMDataElement) it.next();
				Boolean bool = act.hasDataElement(data);
				result = bool;
			}
		} else
			result = false;
		return result;
	}

	/**
	 * Returns (one of) the data element that makes the overlap between this
	 * activity and activity act.
	 * 
	 * @param act
	 *            PDMActivity
	 * @return PDMData Element
	 */
	public PDMDataElement getConnectingDataElement(PDMActivity act) {
		PDMDataElement result = null;
		HashSet dataElts = new HashSet();
		dataElts = this.getDataElements();
		if (!this.equals(act)) {
			Iterator it = dataElts.iterator();
			while ((result == null) & (it.hasNext())) {
				PDMDataElement data = (PDMDataElement) it.next();
				if (act.hasDataElement(data)) {
					result = data;
				}
			}
		}
		return result;
	}

	/**
	 * Calculates the total activity cohesion, based on the activity informaiton
	 * cohesion and the activity relation cohesion.
	 * 
	 * @return Double
	 */
	public Double calculateActivityCohesion() {
		Double cohesion = new Double(0.0);
		cohesion = (calculateActivityInformationCohesion() * calculateActivityRelationCohesion());
		return cohesion;
	}

	/**
	 * Calculates the activity relation cohesion.
	 * 
	 * @return Double
	 */
	public Double calculateActivityRelationCohesion() {
		Double relcoh = new Double(0.0);
		Integer intersecs = 0;
		Object[] ops = new Object[1];
		ops = operations.values().toArray();

		if (ops.length <= 1) {
			relcoh = 0.0;
		} else {
			for (int i = 0; i < ops.length; i++) {
				for (int j = 0; j < ops.length; j++) {
					PDMOperation op1 = (PDMOperation) ops[i];
					PDMOperation op2 = (PDMOperation) ops[j];
					if ((!op1.equals(op2))
							& (!op1.getOutputElements().equals(
									op2.getOutputElements()))) {
						if (op1.intersectsWith(op2)) {
							intersecs = intersecs + 1;
						}
					}
				}
			}

			Integer s = size();
			Double ss = s.doubleValue();
			relcoh = (intersecs.doubleValue())
					/ ((s.doubleValue()) * (s.doubleValue() - 1));
		}
		return relcoh;

	}

	/**
	 * Calculates the activity information cohesion.
	 * 
	 * @return Double
	 */
	public Double calculateActivityInformationCohesion() {
		Double infcoh = 0.00;
		HashSet overlapEltSet = new HashSet();
		Object[] ops = new Object[1];
		ops = operations.values().toArray();

		if (ops.length == 0) {
			infcoh = 0.0;
		} else {
			for (int i = 0; i < ops.length; i++) {
				for (int j = 0; j < ops.length; j++) {
					PDMOperation op1 = (PDMOperation) ops[i];
					PDMOperation op2 = (PDMOperation) ops[j];
					if ((!op1.equals(op2))
							& (!op1.getOutputElements().equals(
									op2.getOutputElements()))) {
						HashSet set = op1.getIntersectionSet(op2);
						Object[] array = new Object[1];
						array = set.toArray();
						for (int k = 0; k < array.length; k++) {
							PDMDataElement d = (PDMDataElement) array[k];
							overlapEltSet.add(d);
						}
					}
				}
			}
			Integer s = getNumberOfDataElements();
			infcoh = (overlapEltSet.size() / (s.doubleValue()));
		}
		return infcoh;
	}

	/**
	 * Returns a Hash Set containing all the data elements of the activity (i.e.
	 * for every operation it returns all the input and output elements).
	 * 
	 * @return HashSet
	 */
	public HashSet getDataElements() {
		HashSet result = new HashSet();

		// Walk through all operations in this activity.
		Object[] ops = new Object[1];
		ops = operations.values().toArray();
		for (int i = 0; i < ops.length; i++) {

			// First, add the input data elements of operation i to the
			// resulting HashSet result.
			HashMap in = new HashMap();
			Object[] inArray = new Object[1];
			PDMOperation operation = (PDMOperation) ops[i];
			in = operation.getInputElements();
			inArray = in.values().toArray();
			for (int k = 0; k < inArray.length; k++) {
				PDMDataElement d1 = (PDMDataElement) inArray[k];
				result.add(d1);
				// System.out.println(d1.getID());
			}

			// Secondly, add the output data elements of operation i to the
			// resulting HashSet result.
			HashMap out = new HashMap();
			Object[] outArray = new Object[1];
			out = operation.getOutputElements();
			outArray = out.values().toArray();
			for (int m = 0; m < outArray.length; m++) {
				PDMDataElement d2 = (PDMDataElement) outArray[m];
				result.add(d2);
				// System.out.println(d2.getID());
			}
		}
		// System.out.println(result.size());
		return result;
	}

	/**
	 * Returns the number of data elements in this activity.
	 * 
	 * @return Integer
	 */
	public Integer getNumberOfDataElements() {
		HashSet set = new HashSet();
		set = getDataElements();
		int result = set.size();
		return result;
	}

	/**
	 * Returns the operations of this activity as a HashMap.
	 * 
	 * @return HashMap
	 */
	public HashMap getOperations() {
		return operations;
	}

	//
	public HashSet getInputDataElements() {
		HashSet result = new HashSet();
		HashSet inputs = new HashSet();
		Object[] ops = new Object[1];
		ops = operations.values().toArray();
		for (int i = 0; i < ops.length; i++) {
			PDMOperation op = (PDMOperation) ops[i];
			Object[] inp = op.getInputElements().values().toArray();
			for (int j = 0; j < inp.length; j++) {
				PDMDataElement elt = (PDMDataElement) inp[j];
				inputs.add(elt);
			}
		}
		result = inputs;
		/*
		 * Iterator it2 = inputs.iterator(); while (it2.hasNext()) {
		 * PDMDataElement d = (PDMDataElement) it2.next();
		 * System.out.println(d.getID());
		 * 
		 * } System.out.println();
		 */return result;
	}

	//
	public HashSet getOutputDataElements() {
		HashSet result = new HashSet();
		HashSet outputs = new HashSet();
		Object[] ops = new Object[1];
		ops = operations.values().toArray();
		for (int i = 0; i < ops.length; i++) {
			PDMOperation op = (PDMOperation) ops[i];
			Object[] outp = op.getOutputElements().values().toArray();
			for (int j = 0; j < outp.length; j++) {
				PDMDataElement elt = (PDMDataElement) outp[j];
				outputs.add(elt);
			}
		}
		HashSet inputs = getInputDataElements();
		Iterator it = inputs.iterator();
		while (it.hasNext()) {
			PDMDataElement d = (PDMDataElement) it.next();
			outputs.remove(d);
		}
		result = outputs;
		/*
		 * Iterator it2 = outputs.iterator(); while (it2.hasNext()) {
		 * PDMDataElement d = (PDMDataElement) it2.next();
		 * System.out.println(d.getID());
		 * 
		 * } System.out.println();
		 */return result;
	}

}
