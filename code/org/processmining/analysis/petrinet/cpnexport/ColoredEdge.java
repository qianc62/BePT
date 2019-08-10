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

package org.processmining.analysis.petrinet.cpnexport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;

import att.grappa.Node;

/**
 * An edge being part of a high-level Petri net simulation model.
 * 
 * @see ColoredPetriNet
 * @see ColoredTransition
 * @see ColoredPlace
 * 
 * @author Anne Rozinat
 * @author Ronny Mans
 */
public class ColoredEdge extends PNEdge {

	/** The arc inscription for this edge */
	private String arcInscription = "";

	private ArrayList<ArrayList> bendPoints = new ArrayList<ArrayList>();

	/**
	 * ONLY OF USE FOR CPN FILE GENERATION
	 */
	private boolean doubleHeaded = false;

	/**
	 * The constructor creates a ColoredEdge from a ColoredTransition to a
	 * ColoredPlace.
	 * 
	 * @param source
	 *            the transition to be connected to this arc as a source node
	 * @param target
	 *            the place to be connected to this arc as a target node
	 */
	public ColoredEdge(Transition source, Place target) {
		super(source, target);
	}

	/**
	 * The constructor creates a ColoredEdge from a ColoredPlace to a
	 * ColoredTransition.
	 * 
	 * @param source
	 *            the place to be connected to this arc as a source node
	 * @param target
	 *            the transition to be connected to this arc as a target node
	 */
	public ColoredEdge(Place source, Transition target) {
		super(source, target);
	}

	/**
	 * Writes this edge to the cpn-file.
	 * 
	 * @param bw
	 *            BufferedWriter used to stream the data to the file
	 * @throws IOException
	 */
	public void write(BufferedWriter bw) throws java.io.IOException {
		Node head = this.getHead();
		Node tail = this.getTail();
		ColoredPlace place;
		ColoredTransition trans;
		String transID = "";
		String placeID = "";
		String orientation = "";
		// in case of a double headed arc the orientation should be BOTHDIR
		if (this.isDoubleHeaded()) {
			orientation = "BOTHDIR";
			place = (ColoredPlace) tail;
			trans = (ColoredTransition) head;
			placeID = place.getCpnID();
			transID = trans.getCpnID();
		} else {
			if (this.isPT()) {
				// the arc is directed from a place to a transition
				orientation = "PtoT";
				place = (ColoredPlace) tail;
				trans = (ColoredTransition) head;
				placeID = place.getCpnID();
				transID = trans.getCpnID();
				ManagerXml.getXCoordinateArcInscription(place.getXCoordinate(),
						trans.getXCoordinate());
			} else {
				// the arc is directed from a transition to a place
				orientation = "TtoP";
				trans = (ColoredTransition) tail;
				place = (ColoredPlace) head;
				transID = trans.getCpnID();
				placeID = place.getCpnID();
			}
		}
		// write edge to file
		// check whether there are some bendpoints
		if (this.getBendPoints().size() > 0) {
			ManagerXml.writeArcTag(bw, orientation, transID, placeID,
					ManagerXml.getXCoordinateArcInscription(place
							.getXCoordinate(), trans.getXCoordinate()),
					ManagerXml.getYCoordinateArcInscription(place
							.getYCoordinate(), trans.getYCoordinate()), this
							.getArcInscription(), this.getBendPoints());
		} else {
			ManagerXml.writeArcTag(bw, orientation, transID, placeID,
					ManagerXml.getXCoordinateArcInscription(place
							.getXCoordinate(), trans.getXCoordinate()),
					ManagerXml.getYCoordinateArcInscription(place
							.getYCoordinate(), trans.getYCoordinate()), this
							.getArcInscription(), null);
		}

	}

	/**
	 * Returns the arc inscription for this arc (according to the CPN tools
	 * syntax).
	 * 
	 * @return the arc inscription.
	 */
	public String getArcInscription() {
		return arcInscription;
	}

	/**
	 * Sets the arc inscription for this arc (The inscription has to be
	 * according to the CPN tools syntax).
	 * 
	 * @param inscript
	 *            the arc inscription for this arc.
	 */
	public void setArcInscription(String inscript) {
		arcInscription = inscript;
	}

	/**
	 * Add a bend point to this arc
	 * 
	 * @param bendPoint
	 *            ArrayList the first element in the array needs to be value for
	 *            the x-axis and the second value in the array needs to be the
	 *            value on the y-axis
	 */
	public void addBendPoint(ArrayList<String> bendPoint) {
		bendPoints.add(bendPoint);
	}

	/**
	 * Retrieve all the bendpoints for this arc
	 * 
	 * @return ArrayList
	 */
	public ArrayList<ArrayList> getBendPoints() {
		return bendPoints;
	}

	/**
	 * indicate whether the arc is doubleheaded USE ONLY FOR CPN FILE GENERATION
	 * 
	 * @param doubleHeaded
	 *            boolean
	 */
	public void setDoubleHeaded(boolean doubleHeaded) {
		this.doubleHeaded = doubleHeaded;
	}

	/**
	 * Retrieves whether the arc is doubleheaded. USE ONLY FOR CPN FILE
	 * GENERATION
	 * 
	 * @return boolean
	 */
	public boolean isDoubleHeaded() {
		return doubleHeaded;
	}
}
