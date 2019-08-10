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
import javax.xml.parsers.*;
import org.processmining.framework.models.*;
import org.w3c.dom.*;
import org.processmining.framework.models.pdm.*;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title: PDMModel
 * </p>
 * <p>
 * Description: Represents a Product Data Model (PDM)
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
public class PDMModel extends ModelGraph {

	private String name; // the name of the model
	private PDMDataElement root; // the root element of the model
	HashMap dataElements = new HashMap(); // the list of dataElements in the
	// model
	HashMap operations = new HashMap(); // the list of operations in the model
	HashMap resources = new HashMap(); // the list of resources from the model
	HashSet removedOps = new HashSet();
	int i = 0; // global counter for unique state identifiers

	/**
	 * Create a new PDM model with the given name
	 * 
	 * @param name
	 *            The name of the PDM model
	 */
	public PDMModel(String name) {
		super("PDM model");
		this.name = name;
	}

	/**
	 * Adds a Data Element to the set of Data Elements of the Product Data Model
	 * 
	 * @param element
	 *            PDMDataElement
	 */
	public void addDataElement(PDMDataElement element) {
		dataElements.put(element.getID(), element);
		addVertex(element);
	}

	/**
	 * Sets the root element of the model. This is one of the data elements
	 * added to the hashMap before.
	 * 
	 * @param dataElement
	 *            PDMDataElement
	 */
	public void setRootElement(PDMDataElement dataElement) {
		root = dataElement;
	}

	/**
	 * Adds a Resource to the set of Resources ot the Product Data Model NB:
	 * Later on this should be moved to the section of the Organizational Model
	 * 
	 * @param resource
	 *            PDMResource
	 */
	public void addResource(PDMResource resource) {
		resources.put(resource.getID(), resource);
	}

	/**
	 * Adds an Operation to the set of Operations of the Product Data Model
	 * 
	 * @param operation
	 *            PDMOperation
	 */
	public void addOperation(PDMOperation operation) {
		operations.put(operation.getID(), operation);
	}

	/**
	 * Removes an Operation to the set of Operations of the Product Data Model
	 * added by Johfra
	 * 
	 * @param operation
	 *            PDMOperation
	 */
	public void remOperation(PDMOperation operation) {
		operations.remove(operation.getID());
	}

	/**
	 * Returns the Data Element with identifier "id"
	 * 
	 * @param id
	 *            String
	 * @return PDMDataElement
	 */
	public PDMDataElement getDataElement(String id) {
		return (PDMDataElement) dataElements.get(id);
	}

	/**
	 * Returns a HashMap with all data elements in this PDM model.
	 * 
	 * @return HashMap
	 */
	public HashMap getDataElements() {
		return dataElements;
	}

	/**
	 * Returns the Resource with identifier "id"
	 * 
	 * @param id
	 *            String
	 * @return PDMResource
	 */
	public PDMResource getResource(String id) {
		return (PDMResource) resources.get(id);
	}

	/**
	 * Returns the Resource with identifier "id"
	 * 
	 * @param id
	 *            String
	 * @return PDMResource
	 */
	public PDMOperation getOperation(String id) {
		return (PDMOperation) operations.get(id);
	}

	/**
	 * Returns all operations in the PDM model.
	 * 
	 * @return HashMap
	 */
	public HashMap getOperations() {
		return operations;
	}

	/**
	 * Returns the root element op the Product Data Model
	 * 
	 * @return PDMDataElement
	 */
	public PDMDataElement getRootElement() {
		return root;
	}

	/**
	 * Returns the leaf elements of the Product Data Model
	 * 
	 * @return HashSet
	 */
	/*
	 * public HashMap getLeafElements() { HashMap map = new HashMap(); Object[]
	 * elts = dataElements.values().toArray(); for (int j=0; j<elts.length; j++)
	 * { PDMDataElement data = (PDMDataElement) elts[j];
	 * map.put(data.getID(),data); } Object[] ops =
	 * operations.values().toArray(); for (int i=0; i<ops.length; i++){
	 * PDMOperation op = (PDMOperation) ops[i]; if
	 * (!(op.getInputElements().isEmpty())){ HashMap outs =
	 * op.getOutputElements(); Object[] outArray = outs.values().toArray(); for
	 * (int j=0; j<outArray.length; j++) { PDMDataElement d = (PDMDataElement)
	 * outArray[j]; map.remove(d.getID()); } } } return map; }
	 */
	public HashMap getLeafElements() {
		HashMap result = new HashMap();
		HashSet leafOps = getLeafOperations();
		if (!(leafOps.isEmpty())) {
			Iterator it = leafOps.iterator();
			while (it.hasNext()) {
				PDMOperation op = (PDMOperation) it.next();
				PDMDataElement data = op.getOutputElement();
				result.put(data.getID(), data);
			}
		} else {
			Object[] elts = dataElements.values().toArray();
			for (int j = 0; j < elts.length; j++) {
				PDMDataElement data = (PDMDataElement) elts[j];
				result.put(data.getID(), data);
			}
			Object[] ops = operations.values().toArray();
			for (int i = 0; i < ops.length; i++) {
				PDMOperation op = (PDMOperation) ops[i];
				HashMap outs = op.getOutputElements();
				Object[] outArray = outs.values().toArray();
				for (int j = 0; j < outArray.length; j++) {
					PDMDataElement d = (PDMDataElement) outArray[j];
					result.remove(d.getID());
				}
			}
		}
		return result;
	}

	public HashSet getLeafOperations() {
		HashSet result = new HashSet();
		Object[] ops = operations.values().toArray();
		for (int i = 0; i < ops.length; i++) {
			PDMOperation op = (PDMOperation) ops[i];
			if ((op.getInputElements().isEmpty())) {
				result.add(op);
			}
		}
		return result;
	}

	/**
	 * Returns a HashMap with the preceeding data elements of data element
	 * 'data'
	 * 
	 * @param data
	 *            PDMDataElement
	 * @return HashMap
	 */
	public HashMap getPrecedingElements(PDMDataElement data) {
		HashMap precs = new HashMap();
		Object[] ops = operations.values().toArray();
		for (int i = 0; i < ops.length; i++) {
			PDMOperation op = (PDMOperation) ops[i];
			if (op.getOutputElements().containsValue(data)) {
				HashMap ins = op.getInputElements();
				Object[] inputs = ins.values().toArray();
				for (int j = 0; j < inputs.length; j++) {
					PDMDataElement el = (PDMDataElement) inputs[j];
					precs.put(el.getID(), el);
				}
			}
		}
		return precs;
	}

	/**
	 * Returns a HashSet with the operations that have data element 'data' as
	 * output element.
	 * 
	 * @param data
	 *            PDMDataElement
	 * @return HashSet
	 */
	public HashSet getOperationsWithOutputElement(PDMDataElement data) {
		HashSet opso = new HashSet();
		Object[] ops = operations.values().toArray();
		for (int i = 0; i < ops.length; i++) {
			PDMOperation op = (PDMOperation) ops[i];
			HashMap outputs = op.getOutputElements();
			if (outputs.containsValue(data)) {
				opso.add(op);
			}
		}
		return opso;
	}

	public HashSet calculateExecutableOperations(HashSet dataElts,
			HashSet executed, HashSet failed, boolean root) {
		HashSet result = new HashSet();
		HashSet enabledOperations = new HashSet();

		if (root) {
			// Calculate the enabled operations (i.e. those operation of which
			// all input elements are in the set of available elements)
			Object[] ops = operations.values().toArray();
			for (int i = 0; i < ops.length; i++) {
				PDMOperation op = (PDMOperation) ops[i];
				HashMap inputs = op.getInputElements();
				Object[] ins = inputs.values().toArray();
				boolean enabled = true;
				int k = 0;
				while (enabled && k < ins.length) {
					PDMDataElement d = (PDMDataElement) ins[k];
					if (!(dataElts.contains(d))) {
						enabled = false;
					}
					k++;
				}
				if (enabled) {
					enabledOperations.add(op);
					// System.out.println("Enabled operation: "+ op.getID());
				}
			}
		} else if (!(dataElts.contains(this.getRootElement()))) {
			// Calculate the enabled operations (i.e. those operation of which
			// all input elements are in the set of available elements)
			Object[] ops = operations.values().toArray();
			for (int i = 0; i < ops.length; i++) {
				PDMOperation op = (PDMOperation) ops[i];
				HashMap inputs = op.getInputElements();
				Object[] ins = inputs.values().toArray();
				boolean enabled = true;
				int k = 0;
				while (enabled && k < ins.length) {
					PDMDataElement d = (PDMDataElement) ins[k];
					if (!(dataElts.contains(d))) {
						enabled = false;
					}
					k++;
				}
				if (enabled) {
					enabledOperations.add(op);
				}
			}
		}
		// remove already executed operations
		Iterator exIt = executed.iterator();
		while (exIt.hasNext()) {
			PDMOperation op = (PDMOperation) exIt.next();
			enabledOperations.remove(op);
		}
		// remove already failed operations
		Iterator fIt = failed.iterator();
		while (fIt.hasNext()) {
			PDMOperation op = (PDMOperation) fIt.next();
			enabledOperations.remove(op);
		}
		result = enabledOperations;
		return result;
	}

	/**
	 * Calculates which data elements are the input elements of the PDM. In
	 * order to do so it checks for each operation if the input elements are
	 * also output elements to another operation. If so, the input element of
	 * the operation is not een input element of the PDM model. If not, then the
	 * input element of the operation is an input element to the PDM model.
	 * 
	 * @return HashSet
	 */
	/*
	 * public HashSet getPDMInputElements() { HashSet result = new HashSet();
	 * HashMap ops = getOperations(); Object[] opsAr = ops.values().toArray();
	 * // walk through all operations of the PDM for (int i=0; i<opsAr.length;
	 * i++){ PDMOperation op = (PDMOperation) opsAr[i]; HashMap ins =
	 * op.getInputElements(); Object[] insAr = ins.values().toArray(); // check
	 * all input data elements of operation 'op' for (int j=0; j<insAr.length;
	 * j++){ PDMDataElement data = (PDMDataElement) insAr[j]; Boolean isInputElt
	 * = true; // check for on of the input data elements of 'op' whether there
	 * // is an operation that produces this data element for (int k=0;
	 * k<opsAr.length; k++){ PDMOperation op2 = (PDMOperation) opsAr[k]; if
	 * (op2.hasOutputDataElement(data)){ isInputElt = false; } } if
	 * (isInputElt){ result.add(data); //
	 * System.out.println("PDM input element: "+ data.getID()); } } } return
	 * result; }
	 */

	public PDMStateSpace calculateSimpleStateSpace(boolean root,
			boolean failure, boolean input, boolean colored, int numStates,
			int breadth) {
		PDMStateSpace result = new PDMStateSpace(this, colored);
		HashSet states = new HashSet();
		int j = (operations.size() + 1);

		if (!input) {
			HashSet empty = new HashSet();
			PDMState st = new PDMState(result, "state" + i, empty, empty, empty);
			result.addState(st);
			states.add(st);
			i++;
		} else {
			// Start with the complete set of input data elements available
			HashSet empty = new HashSet();
			String name = new String("state" + i);
			HashSet ins = new HashSet(); // this hashSet contains the input
			// elements to the process (input
			// elements of PDM)
			HashSet execOps = new HashSet();
			// Fill the hashSet with the leaf elements
			HashMap leafs = getLeafElements();
			Object[] leafElts = leafs.values().toArray();
			for (int i = 0; i < leafElts.length; i++) {
				PDMDataElement d = (PDMDataElement) leafElts[i];
				ins.add(d);
			}
			HashSet leafOps = getLeafOperations();
			Iterator it = leafOps.iterator();
			while (it.hasNext()) {
				PDMOperation op = (PDMOperation) it.next();
				execOps.add(op);
			}

			PDMState start = new PDMState(result, name, ins, execOps, empty); // start
			// state
			// of
			// the
			// statespace
			result.addState(start);
			i++;
			states.add(start);
		}
		while (!states.isEmpty()) {
			HashSet states2 = (HashSet) states.clone();
			Iterator it = states2.iterator();
			while (it.hasNext()) {
				PDMState state = (PDMState) it.next();
				HashSet nextStates = calculateNextStates(state, result, root,
						failure, numStates, breadth);
				Iterator it2 = nextStates.iterator();
				// Add the new states to iterator
				while (it2.hasNext()) {
					PDMState st = (PDMState) it2.next();
					states.add(st);
				}
				states.remove(state);
			}
		}
		i = 0;
		j = 0;
		Message.add("<PDMMDPStateSpace>", Message.TEST);
		Message.add("<NumberOfStates = " + result.getNumberOfStates() + " >",
				Message.TEST);
		Message.add("</PDMMDPStateSpace>", Message.TEST);
		return result;
	}

	public HashSet calculateNextStates(PDMState state,
			PDMStateSpace statespace, boolean root, boolean failure,
			int numStates, int breadth) {
		HashSet result = new HashSet();
		HashSet data = state.dataElements;
		HashSet exec1 = state.executedOperations;
		HashSet failed = state.failedOperations;

		HashSet execOps = calculateExecutableOperations(data, exec1, failed,
				root);
		Iterator it = execOps.iterator();
		int b = 0;
		int bLimit = 0;
		if (!failure) {
			bLimit = breadth;
		} else {
			bLimit = breadth / 2;
		}
		// for each executable operation a new state is created
		while (it.hasNext() && i < numStates && b < bLimit) {
			if (!failure) {
				PDMOperation op = (PDMOperation) it.next();
				PDMDataElement d = op.getOutputElement();
				// First, add the state with the operation 'op' succesfully
				// executed
				HashSet ins2 = (HashSet) data.clone(); // NB: is it necessary to
				// clear this clone
				// again?
				ins2.add(d);
				HashSet exec2 = (HashSet) exec1.clone();
				exec2.add(op);
				PDMState s = checkIfStateExists(statespace, ins2, exec2, failed);
				// Check whether the new state already exists
				// If so, then another link to this state is made
				if (!(s == null)) {
					PDMState st = s;
					PDMStateEdge edge = new PDMStateEdge(state, st, op.getID(),
							1.0);
					statespace.addEdge(edge);
				}
				// If not, a new state is created and linked to the current
				// state
				else {
					String name = "state" + i;
					// int num = checkStatusOfState(statespace, )
					PDMState st = new PDMState(statespace, name, ins2, exec2,
							failed);
					statespace.addState(st);
					result.add(st);
					PDMStateEdge edge = new PDMStateEdge(state, st, op.getID(),
							1.0);
					statespace.addEdge(edge);
					i++;
					b++;
				}
			}

			// Then, if failure of operations is considered, add the state with
			// the failed operations 'op'.
			if (failure) {
				PDMOperation op = (PDMOperation) it.next();
				PDMDataElement d = op.getOutputElement();
				// First, add the state with the operation 'op' succesfully
				// executed
				HashSet ins2 = (HashSet) data.clone(); // NB: is it necessary to
				// clear this clone
				// again?
				ins2.add(d);
				HashSet exec2 = (HashSet) exec1.clone();
				exec2.add(op);
				PDMState s = checkIfStateExists(statespace, ins2, exec2, failed);
				// Check whether the new state already exists
				// If so, then another link to this state is made
				if (!(s == null)) {
					PDMState st = s;
					double prob = 1.0 - (op.getFailureProbability());
					PDMStateEdge edge = new PDMStateEdge(state, st, op.getID(),
							prob);
					statespace.addEdge(edge);
				}
				// If not, a new state is created and linked to the current
				// state
				else {
					String name = "state" + i;
					// int num = checkStatusOfState(statespace, )
					PDMState st = new PDMState(statespace, name, ins2, exec2,
							failed);
					statespace.addState(st);
					result.add(st);
					double prob = 1.0 - (op.getFailureProbability());
					PDMStateEdge edge = new PDMStateEdge(state, st, op.getID(),
							prob);
					statespace.addEdge(edge);
					i++;
					b++;
				}
				HashSet failed2 = (HashSet) failed.clone();
				failed2.add(op);
				PDMState s2 = checkIfStateExists(statespace, data, exec1,
						failed2);
				if (!(s2 == null)) {
					PDMState st = s2;
					PDMStateEdge edge = new PDMStateEdge(state, st, op.getID(),
							op.getFailureProbability());
					statespace.addEdge(edge);
				}
				// If not, a new state is created and linked to the current
				// state
				else {
					String name = "state" + i;
					PDMState st = new PDMState(statespace, name, data, exec1,
							failed2);
					statespace.addState(st);
					result.add(st);
					PDMStateEdge edge = new PDMStateEdge(state, st, op.getID(),
							op.getFailureProbability());
					statespace.addEdge(edge);
					i++;
					b++;
				}
				// failed2.clear();
			}
		}
		return result;
	}

	public PDMState checkIfStateExists(PDMStateSpace statespace, HashSet data,
			HashSet exec, HashSet failed) {
		PDMState result = null;
		boolean bool = false;
		HashSet states = statespace.getStates();
		Iterator it = states.iterator();
		while (it.hasNext() && !bool) {
			PDMState state2 = (PDMState) it.next();
			boolean one = false;
			boolean two = false;
			boolean three = false;
			HashSet data2 = state2.dataElements;
			HashSet exec2 = state2.executedOperations;
			HashSet failed2 = state2.failedOperations;
			one = hashSetContainsSameDataElements(data, data2);
			two = hashSetContainsSameOperations(exec, exec2);
			three = hashSetContainsSameOperations(failed, failed2);

			if (one && two && three) {
				bool = true;
				result = state2;
			}
		}
		return result;
	}

	public boolean hashSetContainsSameDataElements(HashSet set1, HashSet set2) {
		boolean result = false;
		HashSet s1 = (HashSet) set1.clone();
		HashSet s2 = (HashSet) set2.clone();
		// first part, are all elements of s1 also in s2?
		boolean one = false;
		Iterator it = set1.iterator();
		while (it.hasNext()) {
			PDMDataElement d = (PDMDataElement) it.next();
			if (s2.contains(d)) {
				s1.remove(d);
			}
		}
		if (s1.isEmpty()) {
			one = true;
		}
		// second part, are all elements of s2 also in s1?
		boolean two = false;
		HashSet s3 = (HashSet) set1.clone();
		HashSet s4 = (HashSet) set2.clone();
		Iterator it2 = set2.iterator();
		while (it2.hasNext()) {
			PDMDataElement d = (PDMDataElement) it2.next();
			if (s3.contains(d)) {
				s4.remove(d);
			}
		}
		if (s4.isEmpty()) {
			two = true;
		}
		// administrative stuff
		s1.clear();
		s2.clear();
		s3.clear();
		s4.clear();
		result = one && two;
		return result;
	}

	public boolean hashSetContainsSameOperations(HashSet set1, HashSet set2) {
		boolean result = false;
		HashSet s1 = (HashSet) set1.clone();
		HashSet s2 = (HashSet) set2.clone();
		// first part, are all elements of s1 also in s2?
		boolean one = false;
		Iterator it = set1.iterator();
		while (it.hasNext()) {
			PDMOperation d = (PDMOperation) it.next();
			if (s2.contains(d)) {
				s1.remove(d);
			}
		}
		if (s1.isEmpty()) {
			one = true;
		}
		// second part, are all elements of s21 also in s1?
		boolean two = false;
		HashSet s3 = (HashSet) set1.clone();
		HashSet s4 = (HashSet) set2.clone();
		Iterator it2 = set2.iterator();
		while (it2.hasNext()) {
			PDMOperation d = (PDMOperation) it2.next();
			if (s3.contains(d)) {
				s4.remove(d);
			}
		}
		if (s4.isEmpty()) {
			two = true;
		}
		// administrative stuff
		s1.clear();
		s2.clear();
		s3.clear();
		s4.clear();
		result = one && two;
		return result;
	}

	/**
	 * Export to PDM file.
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 *             If writing fails
	 */
	public void writeToPDM(Writer bw) throws IOException {
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bw.write("<PDM\n");
		bw.write("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		bw
				.write("\txsi:noNamespaceSchemaLocation=\"C:/Documents and Settings/ivdfeest/My Documents/Implementatie/PDM.xsd\"\n");
		bw.write(">\n");
		Iterator it = dataElements.values().iterator();
		while (it.hasNext()) {
			PDMDataElement dataElement = (PDMDataElement) it.next();
			dataElement.writeToPDM(bw);
		}
		Iterator it2 = resources.values().iterator();
		while (it2.hasNext()) {
			PDMResource resource = (PDMResource) it.next();
			resource.writeToPDM(bw);
		}
		Iterator it3 = operations.values().iterator();
		while (it3.hasNext()) {
			PDMOperation operation = (PDMOperation) it.next();
			operation.writeToPDM(bw);
		}
		bw.write("</PDM>\n");
	}

	/**
	 * Export PDM model to Declare process model.
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 *             If writing fails
	 */
	public void writePDMToDeclare(Writer bw) throws IOException {
		// write the preamble of the XML file
		bw
				.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		bw.write("<model>\n");
		bw.write("<assignment language=\"ConDec\" name=\"" + name + "\">\n");

		// write the activity definitions, i.e. each operation in the PDM is an
		// activity definition in Declare
		bw.write("<activitydefinitions>\n");
		// start with an initial activity that puts the values for the leaf
		// elements of the PDM
		bw.write("<activity id=\"Initial\" name=\"Initial\">\n");
		bw.write("<authorization/>\n");
		bw.write("<datamodel>\n");
		// all leaf elements
		HashMap leafs = getLeafElements();
		Object[] leafElts = leafs.values().toArray();
		for (int i = 0; i < leafElts.length; i++) {
			PDMDataElement data = (PDMDataElement) leafElts[i];
			data.writePDMToDeclare(bw, "output");
		}
		bw.write("</datamodel>\n");
		bw.write("<attributes/>\n");
		bw.write("</activity>\n");

		// first remove input operations from the set of operations and then
		// write all real operations
		HashMap realOps = (HashMap) operations.clone();
		HashSet inputOps = getLeafOperations();
		Iterator it7 = inputOps.iterator();
		while (it7.hasNext()) {
			PDMOperation op = (PDMOperation) it7.next();
			realOps.remove(op.getID());
		}
		Iterator it4 = realOps.values().iterator();
		while (it4.hasNext()) {
			PDMOperation operation = (PDMOperation) it4.next();
			operation.writePDMToDeclare(bw);
		}
		bw.write("\n");
		// write all input operations (i.e. producing input data elements)
		Iterator it8 = inputOps.iterator();
		while (it8.hasNext()) {
			PDMOperation op = (PDMOperation) it8.next();
			op.writePDMToDeclare(bw);
		}

		bw.write("</activitydefinitions>\n");

		// write the constraint definition, for now we do not have any
		// constraints in the PDM that are translated to Declare
		bw.write("<constraintdefinitions>\n");
		bw.write("</constraintdefinitions>\n");

		// write all dataelements
		bw.write("<data>\n");
		Iterator it5 = dataElements.values().iterator();
		while (it5.hasNext()) {
			PDMDataElement dataElement = (PDMDataElement) it5.next();
			dataElement.writePDMToDeclare(bw);
		}
		bw.write("</data>\n");

		// write the organizational information
		bw.write("<team/>\n");

		// TODO: improve graphical positioning of activities. Now they are
		// presented in one long line.
		// write the graphical positioning information of the Declare model,
		// first the initial operation, then the real operations and then the
		// input operations.
		bw.write("<graphical>\n");
		bw.write("<cells>\n");
		Iterator it6 = realOps.values().iterator();
		Double pos = 10.0;
		while (it6.hasNext()) {
			PDMOperation operation = (PDMOperation) it6.next();
			bw.write("<cell activitydefinition=\"" + operation.getOperationNR()
					+ "\" height=\"40.0\" width=\"80.0\" x=\"" + pos
					+ "\" y=\"90.0\" />\n");
			pos = pos + 85.0;
		}
		Iterator it9 = inputOps.iterator();
		pos = 10.0;
		while (it9.hasNext()) {
			PDMOperation operation = (PDMOperation) it9.next();
			bw.write("<cell activitydefinition=\"" + operation.getOperationNR()
					+ "\" height=\"40.0\" width=\"80.0\" x=\"" + pos
					+ "\" y=\"180.0\" />\n");
			pos = pos + 85.0;
		}

		bw.write("</cells>\n");

		// write the connectors
		bw.write("<connectors/>\n");
		// close the XML file in the right way
		bw.write("</graphical>\n");
		bw.write("</assignment>\n");
		bw.write("</model>\n");
	}

	/**
	 * Writes the model to DOT.
	 * 
	 * @param bw
	 *            The writer
	 * @throws IOException
	 *             If writing fails
	 */
	public void writeToDot(Writer bw) throws IOException {
		// super.writeToDot(bw);

		// Preamble of dot file
		bw
				.write("digraph G {ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; rankdir=TB; ");
		bw.write("fontname=\"Arial\"; \n");
		bw.write("edge [arrowsize=\"0.5\"];\n");
		bw.write("node [fontname=\"Arial\",fontsize=\"8\"];\n");

		// Add the Data Element nodes
		Iterator it = getVerticeList().iterator();
		while (it.hasNext()) {
			Object object = it.next();
			if (object instanceof PDMDataElement) {
				((PDMDataElement) object).writeToDot(bw, this);
			}
		}

		// Add all edges
		it = operations.values().iterator();
		while (it.hasNext()) {
			Object object = it.next();
			if (object instanceof PDMOperation) {
				((PDMOperation) object).writeToDot(bw, this);
			}
		}

		bw.write("\n}\n");

	}

}
