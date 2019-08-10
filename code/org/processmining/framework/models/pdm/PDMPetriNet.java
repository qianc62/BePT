package org.processmining.framework.models.pdm;

import org.processmining.framework.models.petrinet.*;
import org.processmining.framework.log.*;
import org.processmining.framework.models.*;
import cern.colt.list.*;
import cern.colt.matrix.*;
import java.lang.*;
import java.io.*;
import java.util.*;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class PDMPetriNet extends PetriNet {

	// Super class PetriNet contains:
	// - ArrayList of Transitions
	// - ArrayList of Places
	// - ArrayList of TransitionsCluster (???)
	//
	// Super super class ModelGraph contains:
	// - ArrayList of verices
	// - ArrayList of edges
	//

	public HashMap Resources = new HashMap();
	public HashMap dataElements = new HashMap();
	public Double throughputTime;

	public PDMPetriNet() {
	}

	/**
	 * This is the transition writing part of the {@link #writeToDot writeToDot}
	 * procedure.
	 * 
	 * @param bw
	 *            the writer used by the framework to create the temporary dot
	 *            file
	 * @throws IOException
	 *             if writing to the writer fails
	 */
	protected void writeTransitionsToDot(Writer bw) throws IOException {
		Iterator it = getTransitions().iterator();
		while (it.hasNext()) {
			PDMTransition t = (PDMTransition) (it.next());
			// write to dot
			bw.write("subgraph cluster_" + t.getID() + "{" + "\n");
			t.writeContentToDot(bw);
			bw.write(";");
			bw.write("label =" + "\"" + t.getID() + "\"");
			bw.write(";");
			bw.write("}" + "\n");
			// connect Petri net nodes to later grappa components
			// nodeMapping.put(new String(t.getID()), t);
		}

	}

	/**
	 * This is the place writing part of the {@link #writeToDot writeToDot}
	 * procedure.
	 * 
	 * @param bw
	 *            the writer used by the framework to create the temporary dot
	 *            file
	 * @throws IOException
	 *             if writing to the writer fails
	 */
	protected void writePlacesToDot(Writer bw) throws IOException {
		Iterator it = this.getPlaces().iterator();
		while (it.hasNext()) {
			Place p = (Place) (it.next());
			bw.write(p.getIdentifier() + " [shape=\"circle\",label=\""
					+ "\"];\n");

			// connect Petri net nodes to later grappa components
			// nodeMapping.put(new String(p.getId()));
		}
	}

	/**
	 * This is the edge writing part of the {@link #writeToDot writeToDot}
	 * procedure.
	 * 
	 * @param bw
	 *            the writer used by the framework to create the temporary dot
	 *            file
	 * @throws IOException
	 *             if writing to the writer fails
	 */
	protected void writeEdgesToDot(Writer bw) throws IOException {
		Iterator it = this.getEdges().iterator();
		while (it.hasNext()) {
			PNEdge e = (PNEdge) (it.next());
			if (e.isPT()) {
				Place p = (Place) e.getSource();
				Transition t = (Transition) e.getDest();
				bw
						.write(p.getIdentifier() + " -> " + t.getIdentifier()
								+ ";\n");
			} else {
				Place p = (Place) e.getDest();
				Transition t = (Transition) e.getSource();
				bw
						.write(t.getIdentifier() + " -> " + p.getIdentifier()
								+ ";\n");
			}
		}
	}

	/*
	 * protected void writeEdgesToDot(Writer bw) throws IOException { Iterator
	 * it = this.getEdges().iterator(); while (it.hasNext()) { PNEdge e =
	 * (PNEdge) (it.next()); System.out.println("Edge : "+ e.toString()); if
	 * (e.isPT()) { Place p = (Place) e.getSource(); PDMTransition t =
	 * (PDMTransition) e.getDest(); PDMOperation op = t.getAOperation(); //
	 * bw.write(e.toString() + // " ["+ "lhead=cluster_" + t.getID() +"]"+ //
	 * ";\n"); } else if (e.isTP()) { Place p = (Place) e.getDest();
	 * PDMTransition t = (PDMTransition) e.getSource(); PDMOperation op =
	 * t.getAOperation(); // bw.write(t.getIdentifier()+ op.getID() + " -> " +
	 * p.getIdentifier() + // " ["+ "ltail=cluster_" + t.getID() +"]" + //
	 * ";\n"); } else if(!e.isPT() && ! e.isTP()) {
	 * System.out.println("writeEdgesToDot : this is no edge"); } } }
	 */
	public void addDataElement(PDMDataElement element) {
		dataElements.put(element.getID(), element);
	}

}
