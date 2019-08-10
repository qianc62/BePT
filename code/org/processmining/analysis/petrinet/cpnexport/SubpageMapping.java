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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A mapping for the subpage of a transition, that is needed for writing the
 * cpn-file, is defined.
 * 
 * @author arozinat (a.rozinat@tm.tue.nl)
 * @author Ronny Mans
 */
public class SubpageMapping implements Cloneable {

	/** The cpnID of the subpage for this transition */
	private String subPageID = "";

	/** Arraylist containing the mapping from the subplaces to the topplaces */
	private ArrayList<Mapping> mappings = new ArrayList<Mapping>();

	/**
	 * Gives the cpnID of the subpage for a transition
	 * 
	 * @return String the cpnID for the subpage of this transition.
	 */
	public String getSubPageID() {
		return subPageID;
	}

	/**
	 * Sets the cpnID of the subpage for a transition
	 * 
	 * @param id
	 *            the cpnID for the subpage of a transition.
	 */
	public void setSubPageID(String id) {
		subPageID = id;
	}

	/**
	 * Add a mapping from a subplace to a topplace
	 * 
	 * @param sub
	 *            ColoredPlace the subplace
	 * @param top
	 *            ColoredPlace the topplace
	 */
	public void addMapping(ColoredPlace sub, ColoredPlace top) {
		Mapping mapping = new Mapping(sub, top);
		this.mappings.add(mapping);
	}

	/**
	 * Retrieves the list with all mappings from sub to top
	 * 
	 * @return ArrayList (Mapping) a list with mappings, one mapping defines a
	 *         mapping from a sub place to a top place
	 */
	public ArrayList<Mapping> getMappings() {
		return this.mappings;
	}

	/**
	 * Retrieves the mapping that belongs to the sub place
	 * 
	 * @param sub
	 *            ColoredPlace the place on the sub page
	 * @return Mapping the mapping that belongs to this sub place
	 *         <code>null</code> if no mapping exists
	 */
	public Mapping getMappingForSubPlace(ColoredPlace sub) {
		Mapping returnMapping = null;
		Iterator<Mapping> mappings = getMappings().iterator();
		while (mappings.hasNext()) {
			Mapping mapping = mappings.next();
			if (mapping.first().equals(sub)) {
				returnMapping = mapping;
				break;
			}
		}
		return returnMapping;
	}

	/**
	 * Retrieves the mapping that belongs to the top place
	 * 
	 * @param top
	 *            ColoredPlace the place on the top page
	 * @return Mapping the mapping that belongs to this top place
	 *         <code>null</code> if no mapping exists
	 */
	public Mapping getMappingForTopPlace(ColoredPlace top) {
		Mapping returnMapping = null;
		Iterator<Mapping> mappings = getMappings().iterator();
		while (mappings.hasNext()) {
			Mapping mapping = mappings.next();
			if (mapping.second().equals(top)) {
				returnMapping = mapping;
				break;
			}
		}
		return returnMapping;
	}

	/**
	 * Make a deep copy of the object. Note, that because a cloned mapping from
	 * a place on the subpage to a place on the top page has to be made, that
	 * the cloned places on the subpage and top page level has to be provided.
	 * 
	 * @param topPlacesNew
	 *            ArrayList the cloned places on the top page level
	 * @param subPlacesNew
	 *            ArrayList the cloned places on the sub page level
	 * @return Object the cloned subpage mapping
	 * @throws CloneNotSupportedException
	 */
	public Object clone(ArrayList topPlacesNew, ArrayList subPlacesNew)
			throws CloneNotSupportedException {
		SubpageMapping o = null;
		o = (SubpageMapping) super.clone();
		o.mappings = new ArrayList();
		// clone each mapping in the mappings list
		Iterator allMappings = this.mappings.iterator();
		while (allMappings.hasNext()) {
			Mapping currentMapping = (Mapping) allMappings.next();
			ColoredPlace oldSubPlace = currentMapping.subPlace;
			ColoredPlace oldTopPlace = currentMapping.topPlace;
			ColoredPlace subPlaceNew = null;
			ColoredPlace topPlaceNew = null;
			// Find the corresponding cloned places for oldSubplace and
			// oldTopPlace
			for (int i = 0; i < subPlacesNew.size(); i++) {
				ColoredPlace place = (ColoredPlace) subPlacesNew.get(i);
				if (oldSubPlace.getIdentifier().equals(place.getIdentifier())) {
					subPlaceNew = place;
					break;
				}
			}
			for (int i = 0; i < topPlacesNew.size(); i++) {
				ColoredPlace place = (ColoredPlace) topPlacesNew.get(i);
				if (oldTopPlace.getIdentifier().equals(place.getIdentifier())) {
					topPlaceNew = place;
					break;
				}
			}
			o.mappings.add(new Mapping(subPlaceNew, topPlaceNew));
		}
		return o;
	}

	/**
	 * A mapping from a place on a subpage (subplace) to a place on the upper
	 * page (topplace) is defined.
	 * 
	 * @author arozinat
	 * @author Ronny Mans
	 */
	class Mapping {

		/**
		 * the place on the subpage.
		 */
		private ColoredPlace subPlace;

		/**
		 * the place on the toppage.
		 */
		private ColoredPlace topPlace;

		/**
		 * Constructor for defining a mapping from a subplace to a topplace.
		 * 
		 * @param sub
		 *            ColoredPlace the subplace.
		 * @param top
		 *            ColoredPlace the topplace.
		 */
		public Mapping(ColoredPlace sub, ColoredPlace top) {
			this.subPlace = sub;
			this.topPlace = top;
		}

		/**
		 * Returns the subplace for this mapping.
		 * 
		 * @return ColoredPlace the subplace.
		 */
		public ColoredPlace first() {
			return this.subPlace;
		}

		/**
		 * Returns the topplace for this mapping.
		 * 
		 * @return ColoredPlace the topplace.
		 */
		public ColoredPlace second() {
			return this.topPlace;
		}

		/**
		 * Returns the string representation with cpn-ids for this mapping that
		 * can be used for writing to the cpn-file.
		 * 
		 * @return String the string representation for this mapping that can be
		 *         used for writing to the cpn-file.
		 */
		public String toString() {
			return new String("(" + this.subPlace.getCpnID() + ","
					+ this.topPlace.getCpnID() + ")");
		}

		/**
		 * returns the string representation of the cpn-id of the subplace for
		 * this mapping.
		 * 
		 * @return String
		 */
		public String toStringFirst() {
			return new String(this.subPlace.getCpnID());
		}

		/**
		 * Returns the string representation of the cpn-id of the topplace for
		 * this mapping.
		 * 
		 * @return String
		 */
		public String toStringSecond() {
			return new String(this.topPlace.getCpnID());
		}
	}
}
