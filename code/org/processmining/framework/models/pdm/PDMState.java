package org.processmining.framework.models.pdm;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

import java.io.*;
import java.util.*;

import javax.swing.*;

import org.processmining.framework.log.*;
import org.processmining.framework.models.pdm.*;
import org.processmining.framework.models.recommendation.*;
import org.processmining.framework.ui.*;
import org.processmining.framework.models.ModelGraphVertex;

public class PDMState extends ModelGraphVertex {

	HashSet dataElements = new HashSet();
	HashSet executedOperations = new HashSet();
	HashSet failedOperations = new HashSet();
	HashSet decisionSpace = new HashSet();
	int status; // the status of the state in the statespace (0:green; 1:orange;

	// 2:red)

	public PDMState(PDMStateSpace statespace, String id) {
		super(statespace);
		setIdentifier(id);
		// String label = "{"+ getExecutedOperations() +"}{"+
		// getFailedOperations() +"}{"+ getDataElements()+"}";
		// setDotAttribute("label", label);
		// setDotAttribute("shape", "box");
	}

	public PDMState(PDMStateSpace statespace, String id, HashSet data,
			HashSet exec, HashSet failed) {
		super(statespace);
		setIdentifier(id);
		this.dataElements = data;
		this.executedOperations = exec;
		this.failedOperations = failed;
		this.status = 0;
		// String label = "{"+ getExecutedOperations() +"}{"+
		// getFailedOperations() +"}{"+ getDataElements()+"}";
		// setDotAttribute("label", label);
		// setDotAttribute("shape", "box");
	}

	/*
	 * public PDMState(PDMStateSpace statespace, String id, HashSet data,
	 * HashSet exec, HashSet failed, int status) { super(statespace);
	 * setIdentifier(id); this.dataElements = data; this.executedOperations =
	 * exec; this.failedOperations = failed; this.status = status; // String
	 * label = "{"+ getExecutedOperations() +"}{"+ getFailedOperations() +"}{"+
	 * getDataElements()+"}"; // setDotAttribute("label", label); //
	 * setDotAttribute("shape", "box"); }
	 */

	public void addDataElement(PDMDataElement data) {
		dataElements.add(data);
	}

	public void addExecutedOperation(PDMOperation op) {
		executedOperations.add(op);
	}

	public void addFailedOperation(PDMOperation op) {
		failedOperations.add(op);
	}

	public String getExecutedOperations() {
		String result = "";
		Iterator it2 = executedOperations.iterator();
		TreeSet<String> sorted = new TreeSet();
		while (it2.hasNext()) {
			PDMOperation op = (PDMOperation) it2.next();
			sorted.add(op.getID());
		}
		Iterator it = sorted.iterator();
		while (it.hasNext()) {
			String str = (String) it.next();
			result = result + str + ",";
		}
		if (result.endsWith(",")) {
			result = result + "-";
			String[] r;
			r = result.split(",-");
			result = r[0];
		}
		return result;
	}

	public HashSet getExecutedOperationSet() {
		return executedOperations;
	}

	public HashSet getFailedOperationSet() {
		return failedOperations;
	}

	public String getFailedOperations() {
		String result = "";
		Iterator it2 = failedOperations.iterator();
		TreeSet<String> sorted = new TreeSet();
		while (it2.hasNext()) {
			PDMOperation op = (PDMOperation) it2.next();
			sorted.add(op.getID());
		}
		Iterator it = sorted.iterator();
		while (it.hasNext()) {
			String str = (String) it.next();
			result = result + str + ",";
		}
		if (result.endsWith(",")) {
			result = result + "-";
			String[] r;
			r = result.split(",-");
			result = r[0];
		}
		return result;
	}

	public HashSet getDataElementSet() {
		return dataElements;
	}

	public String getDataElements() {
		String result = "";
		Iterator it2 = dataElements.iterator();
		TreeSet<String> sorted = new TreeSet();
		while (it2.hasNext()) {
			PDMDataElement d = (PDMDataElement) it2.next();
			sorted.add(d.getID());
		}
		Iterator it = sorted.iterator();
		while (it.hasNext()) {
			String str = (String) it.next();
			result = result + str + ",";
		}
		if (result.endsWith(",")) {
			result = result + "-";
			String[] r;
			r = result.split(",-");
			result = r[0];
		}
		return result;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getID() {
		String str = identifier.substring(5);
		Integer i = new Integer(str);
		return i.intValue();
	}

	/**
	 * Writes the state to dot.
	 * 
	 * @param bw
	 *            Writer
	 * @param model
	 *            PDMModel
	 * @throws IOException
	 */
	public void writeToDot(Writer bw) throws IOException {
		String str;
		if (status == 0) {
			bw.write(this.getIdentifier() + " [shape=box, label=\"{"
					+ getExecutedOperations() + "} {" + getFailedOperations()
					+ "} {" + getDataElements() + "}\"];\n");

		} else {
			if (status == 1) {
				str = "green";
			} else if (status == 2) {
				str = "yellow";
			} else if (status == 3) {
				str = "red";
			} else {
				str = "";
			}
			// write state
			bw.write(this.getIdentifier() + " [shape=box, style=filled, color="
					+ str + ", label=\"{" + getExecutedOperations() + "} {"
					+ getFailedOperations() + "} {" + getDataElements()
					+ "}\"];\n");
		}
	}
}
