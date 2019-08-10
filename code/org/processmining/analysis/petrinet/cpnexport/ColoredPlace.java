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
import java.util.Iterator;

import org.processmining.analysis.petrinet.cpnexport.SubpageMapping.Mapping;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;

import att.grappa.Grappa;

/**
 * A place being part of a high-level Petri net simulation model.
 * 
 * @see ColoredPetriNet
 * @see ColoredTransition
 * @see ColoredEdge
 * 
 * @author Anne Rozinat (a.rozinat@tm.tue.nl)
 * @author Ronny Mans
 */
public class ColoredPlace extends Place {

	private String placeType = "CASE_ID";
	/** The type of the place */
	private String initMark = "";
	/** The initial marking of the place */

	private int centerX = 0;
	/** The x coordinate of the center point for this place */
	private int centerY = 0;
	/** The y coordinate of the center point for this place */
	private int width = 0;
	/** The width this place */
	private int height = 0;
	/** The height this place */

	/**
	 * The name of the fusion place, in the case that this place belongs to a
	 * fusion place
	 */
	private String nameFusionPlace = "";

	/**
	 * The ID of this place for the CPN file
	 */
	private String cpnID = "";

	/**
	 * Constructor to create a ColoredPlace without having a template place.
	 * 
	 * @param name
	 *            the identifier is to be passed to super class
	 * @param net
	 *            the Petri net it belongs to (to be passed to super class)
	 */
	public ColoredPlace(String name, ColoredPetriNet net) {
		super(name, net);
	}

	/**
	 * Constructor to create a ColoredPlace without having a template place.
	 * 
	 * @param name
	 *            the identifier is to be passed to super class
	 * @param net
	 *            the Petri net it belongs to (to be passed to super class)
	 * @param x
	 *            The x coordinate of the center point for this place
	 * @param y
	 *            The y coordinate of the center point for this place
	 * @param w
	 *            The width of this place
	 * @param h
	 *            The height of this place
	 */
	public ColoredPlace(String name, PetriNet net, int x, int y, int w, int h) {
		super(name, net);
		this.centerX = x;
		this.centerY = y;
		this.width = w;
		this.height = h;
	}

	/**
	 * The constructor creates a ColoredPlace from an ordinary place.
	 * 
	 * @param template
	 *            place (whose identifier is to be passed to super class)
	 * @param net
	 *            the Petri net it belongs to (to be passed to super class)
	 */
	public ColoredPlace(Place template, ColoredPetriNet net) {
		super(template.getIdentifier(), net);
		centerX = (int) template.getCenterPoint().getX()
				* ManagerLayout.getInstance().getScaleFactor();
		centerY = -(int) template.getCenterPoint().getY()
				* ManagerLayout.getInstance().getScaleFactor(); // invert the y
		// axis
		// (everything
		// would be
		// upside down
		// otherwise)
		width = (int) (((Double) template.getAttributeValue(Grappa.WIDTH_ATTR))
				.doubleValue() * ManagerLayout.getInstance().getStretchFactor());
		height = (int) (((Double) template
				.getAttributeValue(Grappa.HEIGHT_ATTR)).doubleValue() * ManagerLayout
				.getInstance().getStretchFactor());
	}

	/**
	 * The constructor creates a ColoredPlace from an ordinary place, but the
	 * position, width and heigth have to be provided
	 * 
	 * @param template
	 *            Place Place (whose identifier is to be passed to super class)
	 * @param net
	 *            ColoredPetriNet The Petri net it belongs to (to be passed to
	 *            the super class)
	 * @param x
	 *            int The x coordinate of the center point for this place
	 * @param y
	 *            int The y coordinate of the center point for this place
	 * @param w
	 *            int The width of this place
	 * @param h
	 *            int The height of this place
	 */
	public ColoredPlace(Place template, ColoredPetriNet net, int x, int y,
			int w, int h) {
		super(template.getIdentifier(), net);
		this.centerX = x;
		this.centerY = y;
		this.width = w;
		this.height = h;
	}

	/**
	 * Writes this place to the cpn-file.
	 * 
	 * @param bw
	 *            BufferedWriter used to stream the data to the file.
	 * @param topTransition
	 *            the transition that is the top of the subpage in which this
	 *            place is located. Null if no such top transition exists.
	 * @throws java.io.IOException
	 */
	public void write(BufferedWriter bw, ColoredTransition topTransition)
			throws java.io.IOException {
		// Find out whether the place should have an input, output or
		// input/output tag
		// so, check whether the cpnID of this place corresponds with a cpnID in
		// the
		// subpagemapping of topTransition
		boolean isInput = false;
		boolean isOutput = false;

		if (topTransition != null) {
			Iterator mappings = topTransition.getSubpageMapping().getMappings()
					.iterator();
			while (mappings.hasNext()) {
				Mapping mapping = (Mapping) mappings.next();
				if (mapping.first() == this) {
					// this place should have an input, output or input/output
					// tag, because
					// there exists a mapping
					ColoredPlace topMapping = mapping.second();
					// check for this place whether it is a predecessor,
					// successor
					// of topTransition
					if (topTransition.getPredecessors().contains(topMapping)) {
						isInput = true;
					}
					if (topTransition.getSuccessors().contains(topMapping)) {
						isOutput = true;
					}
					break;
				}
			}
		}
		if (isInput == true && isOutput == false) {
			ManagerXml.writePlaceTag(bw, this.getCpnID(),
					this.getXCoordinate(), this.getYCoordinate(), this
							.getIdentifier(), this.getWidth(),
					this.getHeight(), this.getPlaceType(), this.getInitMark(),
					"IN", this.getNameFusionPlace());
		} else if (isInput == false && isOutput == true) {
			ManagerXml.writePlaceTag(bw, this.getCpnID(),
					this.getXCoordinate(), this.getYCoordinate(), this
							.getIdentifier(), this.getWidth(),
					this.getHeight(), this.getPlaceType(), this.getInitMark(),
					"OUT", this.getNameFusionPlace());
		} else if (isInput == true && isOutput == true) {
			ManagerXml.writePlaceTag(bw, this.getCpnID(),
					this.getXCoordinate(), this.getYCoordinate(), this
							.getIdentifier(), this.getWidth(),
					this.getHeight(), this.getPlaceType(), this.getInitMark(),
					"I/O", this.getNameFusionPlace());
		} else { // isInput == false && isOutput == false
			ManagerXml.writePlaceTag(bw, this.getCpnID(),
					this.getXCoordinate(), this.getYCoordinate(), this
							.getIdentifier(), this.getWidth(),
					this.getHeight(), this.getPlaceType(), this.getInitMark(),
					"", this.getNameFusionPlace());
		}
	}

	// ///////////////// GET + SET /////////////////////////////

	/**
	 * Returns the cpnID for this place.
	 * 
	 * @return String the cpnID for this place or "" if no such cpnID exists.
	 */
	public String getCpnID() {
		return this.cpnID;
	}

	/**
	 * Sets the cpnID for this place.
	 * 
	 * @param id
	 *            String the cpnID.
	 */
	public void setCpnID(String id) {
		this.cpnID = id;
	}

	/**
	 * Sets the name of the fusion place (in cpn)
	 * 
	 * @param name
	 *            String the name of the fusion place or <code>""</code> if this
	 *            place does not need to be a fusion place
	 */
	public void setNameFusionPlace(String name) {
		nameFusionPlace = name;
	}

	/**
	 * Retrieves the place type of this place. Note that it is not checked
	 * whether a corresponding colorset has been defined in the declaration
	 * section of the CPN model.
	 * 
	 * @return the place type
	 */
	public String getPlaceType() {
		return placeType;
	}

	/**
	 * Retrieves the initial marking of this place. Note that it is not checked
	 * whether this complies with the type of this place.
	 * 
	 * @return the initial marking
	 */
	public String getInitMark() {
		return initMark;
	}

	/**
	 * Retrieves the x coordinate of the center point.
	 * 
	 * @return the x coordinate.
	 */
	public int getXCoordinate() {
		return centerX;
	}

	/**
	 * Retrieves the y coordinate of the center point.
	 * 
	 * @return the y coordinate
	 */
	public int getYCoordinate() {
		return centerY;
	}

	/**
	 * Retrieves the width of this node.
	 * 
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Retrieves the height of this node.
	 * 
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Retrieves the name of the fusion place.
	 * 
	 * @return String the name of the fusion place or <code>""</code> if this
	 *         place does not need to be a fusion place.
	 */
	public String getNameFusionPlace() {
		return nameFusionPlace;
	}

	/**
	 * Sets the place type of this place. Note that it is not checked whether a
	 * corresponding colorset has been defined in the declaration section of the
	 * CPN model.
	 * 
	 * @param type
	 *            the place type
	 */
	public void setPlaceType(String type) {
		placeType = type;
	}

	/**
	 * Sets the initial marking of this place. Note that it is not checked
	 * whether this complies with the type of this place.
	 * 
	 * @param init
	 *            the initial marking
	 */
	public void setInitMark(String init) {
		initMark = init;
	}

	/**
	 * Sets the x coordinate of the center point.
	 * 
	 * @param value
	 *            the new x coordinate to be assigned
	 */
	public void setXCoordinate(int value) {
		centerX = value;
	}

	/**
	 * Sets the y coordinate of the center point.
	 * 
	 * @param value
	 *            the new y coordinate to be assigned
	 */
	public void setYCoordinate(int value) {
		centerY = value;
	}

	/**
	 * Sets the width of this node.
	 * 
	 * @param value
	 *            the new width to be assigned
	 */
	public void setWidth(int value) {
		width = value;
	}

	/**
	 * Sets the height of this node.
	 * 
	 * @param value
	 *            the new height to be assigned
	 */
	public void setHeight(int value) {
		height = value;
	}

	/**
	 * Make a deep copy of the object. Note that this method needs to be
	 * extended as soon as there are attributes added to the class which are not
	 * primitive or immutable. <br>
	 * Note further that the belonging Simulated Petri net is not cloned (so the
	 * cloned object will point to the same one as this object). Only the
	 * {@link ColoredPetriNet#clone ColoredPetriNet.clone()} method will update
	 * the refernce correspondingly.
	 * 
	 * @return Object the cloned object
	 */
	/*
	 * public Object clone() { ColoredPlace o = null; o =
	 * (ColoredPlace)super.clone(); return o; }
	 */

	public int hashCode() {
		int returnInt = 0;
		returnInt = super.hashCode() + cpnID.hashCode();
		return returnInt;
	}

}
