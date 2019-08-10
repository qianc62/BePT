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

import java.io.*;
import java.util.*;

import org.processmining.framework.models.*;

/**
 * <p>
 * Title: PDM operation
 * </p>
 * <p>
 * Description: Represents an operation in a PDM model
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Irene Vanderfeesten
 * @version 1.0
 */
public class PDMOperation {

	private String opID; // the id of the operation
	private Integer opNR; // the number of the operation, needed for Declare.
	// The number is the hashCode of opID.
	private int time; // the time the execution of the operation takes
	private int cost; // the costs of execution the operation
	private double prob; // failure probability (0<prob<=1)
	HashSet conditions = new HashSet(); // the set of conditions
	// private String kindOf; // the kind of operation (specialisation,
	// generalisation, has, etc.)
	private int rootDistance; // the distance to the root element counted in the
	// minimum number of operations to be executed
	// after this operation
	private int remainingProcessingTime; // the remaining processing time
	// including the execution of this
	// operation.
	HashMap resource = new HashMap(1); // the resource that is allowed to
	// execute the operation
	HashMap output = new HashMap(); // the output element(s) of the operation
	HashMap input = new HashMap(); // the input elements of the operation
	ModelGraphVertex between; // this object represents the 'knot' of an

	// operation that takes together the arcs from
	// all input elements

	/**
	 * Creates the operation with opID 'id'. At the same time also the 'knot' is
	 * created, as a new ModelGraphVertex, that takes the arcs from the input
	 * elements together.
	 * 
	 * @param model
	 *            PDMModel
	 * @param id
	 *            String
	 */
	public PDMOperation(PDMModel model, String id) {
		this.opID = id;
		between = new ModelGraphVertex(model);
		model.addVertex(between);
		setOperationNR(opID.hashCode());
		this.rootDistance = 1000000000;
		this.remainingProcessingTime = 1000000000;
		this.prob = 0.0;
	}

	/**
	 * Creates the operation with opID 'id'. At the same time also the 'knot' is
	 * created, as a new ModelGraphVertex, that takes the arcs from the input
	 * elements together.
	 * 
	 * @param model
	 *            PDMModel
	 * @param id
	 *            String
	 */
	public PDMOperation(PDMModel model, String id, Integer cost, Integer time,
			double probability, HashSet conditions) {
		this.opID = id;
		this.cost = cost;
		this.time = time;
		between = new ModelGraphVertex(model);
		model.addVertex(between);
		setOperationNR(opID.hashCode());
		this.rootDistance = 1000000000;
		this.remainingProcessingTime = 1000000000;
		this.conditions = conditions;
		this.prob = probability;
	}

	/**
	 * Creates the operation with opID 'id'. At the same time also the 'knot' is
	 * created, as a new ModelGraphVertex, that takes the arcs from the input
	 * elements together.
	 * 
	 * @param model
	 *            PDMModel
	 * @param id
	 *            String
	 */
	/*
	 * public PDMOperation(PDMModel model, String id, Integer cost, Integer
	 * time, HashSet conditions) { this.opID = id; this.cost = cost; this.time =
	 * time; between = new ModelGraphVertex(model); model.addVertex(between);
	 * setOperationNR(opID.hashCode()); this.rootDistance = 1000000000;
	 * this.remainingProcessingTime = 1000000000; this.conditions = conditions;
	 * this.prob = 0.0; }
	 */

	/**
	 * Sets the integer number for the operation based on the ID. To be able to
	 * generate a Declare model from the PDM, we need a numrical reference to
	 * each operation instead of the string identifier. This numerical reference
	 * is generated from the string id by using the hashCode() of the
	 * identifier.
	 * 
	 * @param in
	 *            Integer
	 */
	public void setOperationNR(Integer in) {
		this.opNR = in;
	}

	/**
	 * Returns the numerical identifier of an operation. This numerical
	 * reference is generated from the string identifier bij using the
	 * hashCode() of the string identifier. A numerical reference is needed to
	 * be able to generate a Declare model from the PDM model.
	 * 
	 * @return Integer
	 */
	public Integer getOperationNR() {
		return opNR;
	}

	/**
	 * Returns the identifier of the operation.
	 * 
	 * @return String
	 */
	public String getID() {
		return this.opID;
	}

	/**
	 * Adds a data element with dataElementID to the list of input elements.
	 * 
	 * @param dataElementID
	 *            String
	 * @param model
	 *            PDMModel
	 */
	public void addInputElement(String dataElementID, PDMModel model) {
		input.put(dataElementID, model.getDataElement(dataElementID));
		ModelGraphEdge edge = new ModelGraphEdge(model
				.getDataElement(dataElementID), between);
		model.addEdge(edge);
	}

	/**
	 * Adds a data element with dataElementID to the list of output elements.
	 * 
	 * @param dataElementID
	 *            String
	 * @param model
	 *            PDMModel
	 */
	public void addOutputElement(String dataElementID, PDMModel model) {
		output.put(dataElementID, model.getDataElement(dataElementID));
		ModelGraphEdge edge = new ModelGraphEdge(between, model
				.getDataElement(dataElementID));
		model.addEdge(edge);
	}

	/**
	 * Reutns the input data elements of the operation.
	 * 
	 * @return HashMap
	 */
	public HashMap getInputElements() {
		return input;
	}

	/**
	 * Returns the output data elements of the operation.
	 * 
	 * @return HashMap
	 */
	public HashMap getOutputElements() {
		return output;
	}

	/**
	 * Returns the output data element of the operation.
	 * 
	 * @return HashMap
	 */
	public PDMDataElement getOutputElement() {
		PDMDataElement result;
		Object[] outputs = output.values().toArray();
		result = (PDMDataElement) outputs[0];
		return result;
	}

	/**
	 * Adds a resource, with resourceID, to the list of resource.
	 * 
	 * @param resourceID
	 *            String
	 * @param model
	 *            PDMModel
	 */
	public void addResource(String resourceID, PDMModel model) {
		resource.put(resourceID, model.getResource(resourceID));
	}

	/**
	 * Sets the costs of executing this operation.
	 * 
	 * @param resourceID
	 *            String
	 * @param model
	 *            PDMModel
	 */
	public void setCost(int cost) {
		this.cost = cost;
	}

	/**
	 * Sets the value for duration of executing this operation.
	 * 
	 * @param resourceID
	 *            String
	 * @param model
	 *            PDMModel
	 */
	public void setDuration(int dur) {
		this.time = dur;
	}

	/**
	 * Returns the probability of failure of execution of this operation
	 * 
	 * @return double
	 */
	public double getFailureProbability() {
		return prob;
	}

	/**
	 * Sets the probability of failure of execution of this operation
	 * 
	 * @return double
	 */
	public void setFailureProbability(double probability) {
		this.prob = probability;
	}

	/**
	 * Checks whether the specified data element (object) is an element of the
	 * input or output elements of the operation.
	 * 
	 * @param el
	 *            PDMDataElement
	 * @return Boolean
	 */
	public Boolean hasDataElement(PDMDataElement el) {
		Boolean result;
		result = false;
		if (!(input.get(el.getID()) == null))
			result = true;
		else if (!(output.get(el.getID()) == null))
			result = true;
		return result;
	}

	/**
	 * Checks whether the specified data element (object) is an input element of
	 * the the operation.
	 * 
	 * @param el
	 *            PDMDataElement
	 * @return Boolean
	 */
	public Boolean hasInputDataElement(PDMDataElement el) {
		Boolean result;
		result = false;
		if (!(input.get(el.getID()) == null))
			result = true;
		return result;
	}

	/**
	 * Checks whether the specified data element (object) is an output elements
	 * of the operation.
	 * 
	 * @param el
	 *            PDMDataElement
	 * @return Boolean
	 */
	public Boolean hasOutputDataElement(PDMDataElement el) {
		Boolean result;
		result = false;
		if (!(output.get(el.getID()) == null))
			result = true;
		return result;
	}

	/**
	 * checks whether the specified data element with identifier 'el' is an
	 * element of the input or output elements of the operation.
	 * 
	 * @param el
	 *            String
	 * @return Boolean
	 */
	public Boolean hasDataElement(String el) {
		Boolean result;
		result = false;
		if (!(input.get(el) == null))
			result = true;
		else if (!(output.get(el) == null))
			result = true;
		return result;
	}

	/**
	 * Returns true when the operation intersects with this operation. Returns
	 * false otherwise
	 * 
	 * @param operation
	 *            PDMOperation
	 * @return Boolean
	 */
	public Boolean intersectsWith(PDMOperation operation) {
		Boolean result = false;

		// check whether input or output elements of the two operations overlap
		Object[] dataElts1 = new Object[1];
		dataElts1 = output.values().toArray();
		Object[] dataElts2 = new Object[1];
		dataElts2 = input.values().toArray();
		int i = 0;
		int j = 0;
		while ((result == false) & (i < dataElts1.length)) {
			PDMDataElement de = (PDMDataElement) dataElts1[i];
			result = de.intersectsWith(operation);
			i++;
		}
		while (result == false & j < dataElts2.length) {
			PDMDataElement de = (PDMDataElement) dataElts2[j];
			result = de.intersectsWith(operation);
			j++;
		}
		return result;
	}

	/**
	 * Returns the overlapping data elements of this operation with the
	 * parameter operation as a HashSet.
	 * 
	 * @param operation
	 *            PDMOperation
	 * @param overlapEltsSet
	 *            HashSet
	 * @return Boolean
	 */
	public HashSet getIntersectionSet(PDMOperation operation) {
		HashSet result = new HashSet();
		Object[] dataElts1 = new Object[1];
		dataElts1 = output.values().toArray();
		Object[] dataElts2 = new Object[1];
		dataElts2 = input.values().toArray();
		for (int i = 0; i < dataElts1.length; i++) {
			PDMDataElement data = (PDMDataElement) dataElts1[i];
			if (operation.hasDataElement(data)) {
				result.add(data);
			}
		}
		for (int j = 0; j < dataElts2.length; j++) {
			PDMDataElement data = (PDMDataElement) dataElts2[j];
			if (operation.hasDataElement(data)) {
				result.add(data);
			}
		}
		return result;
	}

	/**
	 * Writes the operation to a PDM file.
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 */
	public void writeToPDM(Writer bw) throws IOException {
		bw.write("\t<Operation\n");
		bw.write("\t\t\tOperationID=\"" + opID + "\"\n");
		bw.write("\t\t>\n");
		bw.write("\t\t\t<Input>\n");
		Iterator it = input.values().iterator();
		while (it.hasNext()) {
			PDMDataElement dataElement = (PDMDataElement) it.next();
			bw.write("\t\t\t\t<DataElementRef>" + dataElement.getID()
					+ "\"</DataElementRef>\n");
		}
		bw.write("\t\t\t</Input>\n");
		bw.write("\t\t\t<Output>\n");
		Iterator it2 = output.values().iterator();
		while (it2.hasNext()) {
			PDMDataElement dataElement = (PDMDataElement) it.next();
			bw.write("\t\t\t\t<DataElementRef>" + dataElement.getID()
					+ "\"</DataElementRef>\n");
		}
		bw.write("\t\t\t</Output>\n");
		Iterator it3 = resource.values().iterator();
		while (it3.hasNext()) {
			PDMResource resource = (PDMResource) it.next();
			bw.write("\t\t\t\t<ResourceRef>" + resource.getID()
					+ "\"</ResourceRef>\n");
		}
		bw.write("\t</Operation\n");
	}

	/**
	 * Writes the PDM model to an XML file that can be imported by Declare, such
	 * that the model can be used by the recommendation service and worklist of
	 * Declare. The Declare model contains all operations as activities.
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 */
	public void writePDMToDeclare(Writer bw) throws IOException {
		bw.write("<activity id=\"" + opNR + "\" name=\"" + opID + "\">\n");
		bw.write("<authorization/>\n");
		bw.write("<datamodel>\n");
		// input data elementen
		Object[] data1 = new Object[1];
		data1 = input.values().toArray();
		for (int i1 = 0; i1 < data1.length; i1++) {
			PDMDataElement data = (PDMDataElement) data1[i1];
			data.writePDMToDeclare(bw, "input");
		}
		// output data elementen
		Object[] data2 = new Object[1];
		data2 = output.values().toArray();
		for (int i2 = 0; i2 < data2.length; i2++) {
			PDMDataElement data = (PDMDataElement) data2[i2];
			data.writePDMToDeclare(bw, "output");
		}
		bw.write("</datamodel>\n");
		bw.write("<attributes/>\n");
		bw.write("</activity>\n");
	}

	/**
	 * Writes the operation to dot. The operation is represented by arcs from
	 * the input to the output elements. In between there is a 'knot' to connect
	 * the arcs together that belong together in one operation.
	 * 
	 * @param bw
	 *            Writer
	 * @param model
	 *            PDMModel
	 * @throws IOException
	 */
	public void writeToDot(Writer bw, PDMModel model) throws IOException {
		// write the point in between
		bw
				.write(opID
						+ " [shape=circle, fixedsize=true, height=\".1\", style=filled, label=\"\"];\n");

		// write the arcs from the input elements to the point in between
		Iterator it = input.values().iterator();
		while (it.hasNext()) {
			PDMDataElement dataElement = (PDMDataElement) it.next();
			bw.write(opID + " -> " + dataElement.getID()
					+ "[arrowhead=none];\n");
		}

		// write the arcs from the point in between to the output elements.
		Iterator it2 = output.values().iterator();
		while (it2.hasNext()) {
			PDMDataElement dataElement = (PDMDataElement) it2.next();
			bw.write(dataElement.getID() + " -> " + opID
					+ "[arrowhead=none, arrowtail=normal];\n");
		}

	}

	/**
	 * Writes the operation to dot. The operation is represented by arcs from
	 * the input to the output elements. In between there is a 'knot' to connect
	 * the arcs together that belong together in one operation.
	 * 
	 * @param bw
	 *            Writer
	 * @param model
	 *            PDMModel
	 * @throws IOException
	 */
	public void writeToDot(Writer bw) throws IOException {
		// write the point in between
		bw
				.write(opID
						+ " [shape=circle, fixedsize=true, height=\".1\", style=filled, label=\"\"];\n");

		// write the arcs from the input elements to the point in between
		Iterator it = input.values().iterator();
		while (it.hasNext()) {
			PDMDataElement dataElement = (PDMDataElement) it.next();
			bw.write(opID + " -> " + dataElement.getID()
					+ "[arrowhead=none];\n");
		}

		// write the arcs from the point in between to the output elements.
		Iterator it2 = output.values().iterator();
		while (it2.hasNext()) {
			PDMDataElement dataElement = (PDMDataElement) it2.next();
			bw.write(dataElement.getID() + " -> " + opID
					+ "[arrowhead=none, arrowtail=normal];\n");
		}

	}

	public int getCost() {
		return cost;
	}

	public int getDuration() {
		return time;
	}

	public int getDistance(PDMModel model) {
		if (rootDistance == 1000000000) {
			rootDistance = calculateRootDistance(model);
		}
		return rootDistance;
	}

	public int getRemainingProcessingTime(PDMModel model) {
		if (remainingProcessingTime == 1000000000) {
			remainingProcessingTime = calculateRemainingProcessingTime(model);
		}
		return remainingProcessingTime;
	}

	/**
	 * calculateRemainingProcessingTime
	 * 
	 * @return int
	 */
	public int calculateRemainingProcessingTime(PDMModel model) {
		int result = 1000000000;
		PDMDataElement root = model.getRootElement();
		// System.out.println(this.getID());
		if (output.containsValue(root)) {
			result = time;
		} else {
			HashMap operations = model.getOperations();
			Object[] ops = operations.values().toArray();
			for (int i = 0; i < ops.length; i++) {
				PDMOperation op = (PDMOperation) ops[i];
				HashMap outThis = output;
				HashMap inOp = op.getInputElements();
				HashSet intersection = getIntersection(outThis, inOp);
				if (!(intersection.isEmpty())) {
					int remTime = time
							+ op.calculateRemainingProcessingTime(model);
					if (remTime < result) {
						result = remTime;
					}
				}
			}
		}
		// System.out.println(this.getID() + " : " + result);
		return result;
	}

	/**
	 * getIntersection
	 * 
	 * @param outThis
	 *            HashMap
	 * @param inOp
	 *            HashMap
	 * @return HashSet
	 */
	private HashSet getIntersection(HashMap een, HashMap twee) {
		HashSet result = new HashSet();
		Object[] array = een.values().toArray();
		for (int i = 0; i < array.length; i++) {
			PDMDataElement data = (PDMDataElement) array[i];
			if (twee.containsValue(data)) {
				result.add(data);
				// System.out.println(data.getID());
				// System.out.println(result);
			}
		}
		return result;
	}

	public int calculateRootDistance(PDMModel model) {
		int result = 1000000000;
		PDMDataElement root = model.getRootElement();
		if (output.containsValue(root)) {
			result = 0;
		} else {
			HashMap operations = model.getOperations();
			Object[] ops = operations.values().toArray();
			for (int i = 0; i < ops.length; i++) {
				PDMOperation op = (PDMOperation) ops[i];
				HashMap outThis = output;
				HashMap inOp = op.getInputElements();
				HashSet intersection = getIntersection(outThis, inOp);
				if (!(intersection.isEmpty())) {
					int dist = 1 + op.calculateRootDistance(model);
					if (dist < result) {
						result = dist;
					}
				}
			}
		}
		return result;
	}

	public boolean evaluateConditions(Map<String, String> availableDataElements) {
		boolean result = true;
		Iterator cIt = conditions.iterator();
		while (cIt.hasNext() && (result == true)) {
			PDMCondition con = (PDMCondition) cIt.next();
			// System.out.println(con.writeCondition());
			PDMDataElement dataElt = con.getDataElement();
			String key = dataElt.getID();
			String value = availableDataElements.get(key);
			// System.out.println(key + " : " + value);
			if (!(value.equals(con.getValue()))) {
				result = false;
			}
			// System.out.println(result);
		}
		return result;
	}

	public void addCondition(PDMCondition con) {
		conditions.add(con);
	}

}
