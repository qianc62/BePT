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
import org.processmining.framework.models.*;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ModelGraphEdge;

public class PDMStateEdge extends ModelGraphEdge {

	PDMState in;
	PDMState out;
	String label;
	double probability;

	public PDMStateEdge(PDMState in, PDMState out) {
		super(in, out);
		this.in = in;
		this.out = out;
	}

	public PDMStateEdge(PDMState in, PDMState out, String label, double prob) {
		super(in, out);
		this.in = in;
		this.out = out;
		this.label = label;
		this.probability = prob;
	}

	public double getProbability() {
		return probability;
	}

	public PDMState getSource() {
		return in;
	}

	public PDMState getDestination() {
		return out;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void writeToDot(Writer bw) throws IOException {
		bw.write(in.getIdentifier() + " -> " + out.getIdentifier() + "[label="
				+ label + "];\n");
		// , arrowhead=normal, arrowtail=none
	}

}
