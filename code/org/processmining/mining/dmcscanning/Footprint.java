/*
 * Created on May 19, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
 * all rights reserved
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning;

import java.util.HashSet;
import java.util.Iterator;

import org.processmining.mining.dmcscanning.equivalence.ObjectEquivalence;

/**
 * This class wraps a footprint, i.e. an unordered set of data objects that are
 * typically modified in a clustered way.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class Footprint {

	protected HashSet<String> dataObjects = null;

	/**
	 * constructor
	 */
	public Footprint() {
		dataObjects = new HashSet<String>();
	}

	/**
	 * copy constructor
	 * 
	 * @param other
	 */
	public Footprint(Footprint other) {
		dataObjects = new HashSet<String>(other.dataObjects);
	}

	/**
	 * returns a deep copy of this footprint instance
	 */
	public Object clone() {
		return new Footprint(this);
	}

	/**
	 * add a new data object to this footprint, keeps the set minimal (no double
	 * entries)
	 * 
	 * @param data
	 */
	public void add(String data) {
		dataObjects.add(data.trim());
	}

	/**
	 * removes a data object from this footprint
	 * 
	 * @param data
	 */
	public void remove(String data) {
		dataObjects.remove(data);
	}

	/**
	 * @return the number of data objects contained
	 */
	public int size() {
		return dataObjects.size();
	}

	/**
	 * checks, whether a data object is already contained
	 * 
	 * @param data
	 * @return
	 */
	public boolean contains(String data) {
		return dataObjects.contains(data);
	}

	/**
	 * @return Iterator on the contained data object identifier strings
	 */
	public Iterator iterator() {
		return dataObjects.iterator();
	}

	/**
	 * @return the contained data objects as strings
	 */
	public HashSet<String> getData() {
		return new HashSet<String>(dataObjects);
	}

	/**
	 * @return contained data objects as array of strings
	 */
	public Object[] toArray() {
		return dataObjects.toArray();
	}

	/**
	 * tests for deep equality with another footprint instance
	 * 
	 * @param obj
	 * @return
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Footprint) {
			Footprint other = (Footprint) obj;
			if (dataObjects.size() == other.dataObjects.size()) {
				for (Iterator it = dataObjects.iterator(); it.hasNext();) {
					if (other.dataObjects.contains((String) it.next()) == false) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Set-wise intersection. Both this and other footprint remain unchanged.
	 * 
	 * @param other
	 *            further footprint
	 * @return the intersection of this footprint with another one
	 */
	public Footprint intersection(Footprint other) {
		Footprint intersect = new Footprint();
		String data = null;
		for (Iterator it = dataObjects.iterator(); it.hasNext();) {
			data = (String) it.next();
			if (other.dataObjects.contains(data)) {
				intersect.add(data);
			}
		}
		return intersect;
	}

	/**
	 * Set-wise union. Both this and other footprint remain unchanged.
	 * 
	 * @param other
	 *            further footprint
	 * @return the union of this footprint with another one
	 */
	public Footprint union(Footprint other) {
		Footprint union = new Footprint(this);
		for (Iterator it = other.dataObjects.iterator(); it.hasNext();) {
			union.add((String) it.next());
		}
		return union;
	}

	/**
	 * Computes the relative overlap of this footprint with another one, i.e. if
	 * the intersection of this and another footprint contains exactly half of
	 * each footprint's elements, the overlap will be 0.5. Identical footprints
	 * will yield an overlap of 1.0, those whose intersection is empty will
	 * yield an overlap of 0.0.
	 * 
	 * @param other
	 *            further footprint
	 * @return relative overlap, percentage within [0,1]
	 */
	public double overlap(Footprint other) {
		double completeSize = this.size() + other.size();
		double intersectionSize = intersection(other).size();
		return ((2 * intersectionSize) / completeSize);
	}

	/**
	 * Checks whether this footprint is a subset of another footprint, i.e. all
	 * its data objects are also contained within the other footprint
	 * 
	 * @param other
	 * @return
	 */
	public boolean isSubsetOf(Footprint other) {
		if (this.size() > other.size()) {
			return false;
		}
		for (Iterator it = dataObjects.iterator(); it.hasNext();) {
			if (other.dataObjects.contains((String) it.next()) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Converts all contained data object identifiers to canonical
	 * representations of the data objects, following the rules implemented in
	 * the given equivalence relation implementation.
	 * 
	 * @param equiv
	 *            equivalence relation implementation used for conversion
	 */
	public void convertToCanonical(ObjectEquivalence equiv) {
		HashSet<String> converted = new HashSet<String>();
		for (Iterator it = dataObjects.iterator(); it.hasNext();) {
			converted.add(equiv.getCanonicalName((String) it.next()));
		}
		dataObjects = converted;
	}

	/**
	 * String representation
	 */
	public String toString() {
		String result = "";
		for (Iterator it = dataObjects.iterator(); it.hasNext();) {
			result += it.next() + "\n";
		}
		return result;
	}

}
