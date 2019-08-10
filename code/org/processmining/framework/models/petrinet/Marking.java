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

package org.processmining.framework.models.petrinet;

import java.util.*;

/**
 * <p>
 * Title: Marking class
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */

public class Marking implements Comparable {
	public final static int OMEGA = -1;
	private TreeSet<Place> places;
	private Hashtable<Long, Integer> tokens;
	private int tokenCount = 0;

	/**
	 * Create a new marking.
	 */
	public Marking() {
		places = new TreeSet<Place>();
		tokens = new Hashtable<Long, Integer>();
	}

	/**
	 * 
	 * @return The number of tokens in the extended marking. OMEGA if infinite.
	 */
	public int getTokenCount() {
		return tokenCount;
	}

	/**
	 * Adds a given number of tokens to a given place.
	 * 
	 * @param place
	 *            The place to add the tokes to.
	 * @param count
	 *            The number of tokens to add.
	 */
	public void addPlace(Place place, int count) {
		Integer i = tokens.get(place.getIdKey());
		if (i == null) {
			// No tokens yet in this place. Add place and set count.
			places.add(place);
			tokens.put(place.getIdKey(), new Integer(count));
			if (tokenCount == OMEGA) {
				// Nothing to do
			} else if (count == OMEGA) {
				tokenCount = OMEGA;
			} else {
				tokenCount += count;
			}
		} else if (i.intValue() == OMEGA) {
			// OMEGA tokens in this place.
		} else if (count == OMEGA) {
			// OMEGA tokens will be in this place.
			tokens.put(place.getIdKey(), new Integer(OMEGA));
			tokenCount = OMEGA;
		} else {
			tokens.put(place.getIdKey(), new Integer(i.intValue() + count));
			tokenCount += count;
		}
	}

	/**
	 * Removes a given number of tokens from a given place.
	 * 
	 * @param place
	 *            The place to remove the tokens from.
	 * @param count
	 *            The number of tokens to remove.
	 */
	public void delPlace(Place place, int count) {
		Integer i = tokens.get(place.getIdKey());
		if (i == null) {
			// No tokens to remove in this place. Leave it like that.
		} else if (i.intValue() == OMEGA) {
			// OMEGA tokens in this place. Leave it like that,
		} else if (count == OMEGA || i.intValue() - count <= 0) {
			// All tokens will be removed. Remove place from marking.
			places.remove(place);
			tokens.remove(place.getIdKey());
			tokenCount -= i.intValue();
		} else {
			// Tokens will remain. Don't remove the place.
			tokens.put(place.getIdKey(), new Integer(i.intValue() - count));
			tokenCount -= count;
		}
	}

	/**
	 * Checks whether two markings are identical.
	 * 
	 * @param marking
	 *            The marking to check this marking against.
	 * @return True if identical, false otherwise.
	 */
	public boolean equals(Object marking) {
		if (!(marking instanceof Marking)) {
			return false;
		}

		// boolean b = places.equals(((Marking) marking).places);
		// boolean b = places.containsAll(((Marking) marking).places)
		// && ((Marking) marking).places.containsAll(places);
		// if (!b) {
		// return false;
		// }
		//
		// Iterator it = iterator();
		// while (it.hasNext() && b) {
		// Place place = (Place) it.next();
		// b = tokens.get(place.getIdKey()).equals(
		// ((Marking) marking).tokens.get(place.getIdKey()));
		// }
		// return b;
		if (this.compareTo(marking) == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @return An iterator over the marked places.
	 */
	public Iterator iterator() {
		return places.iterator();
	}

	/**
	 * Checks whether this marking is smaller than a given marking.
	 * 
	 * @param marking
	 *            The given marking.
	 * @return True if smaller, false otherwise.
	 */
	public boolean isLessOrEqual(Marking marking) {
		Iterator it = iterator();
		boolean r = true;
		while (it.hasNext() && r) {
			Place place = (Place) it.next();
			int n1 = getTokens(place);
			int n2 = marking.getTokens(place);
			r = (n2 == OMEGA || (n1 != OMEGA && n1 <= n2));
		}
		return r;
	}

	/**
	 * 
	 * @return The number of places marked.
	 */
	public int getSize() {
		return places.size();
	}

	/**
	 * Compare the marking with a given marking, using some total order.
	 * 
	 * @param object
	 *            The marking given for comparison. object *must be* a marking..
	 * @return 0 if identical, <0 if this marking considered smaller then given
	 *         marking, >0 if considered greater.
	 */
	public int compareTo(Object object) {
		Marking marking = (Marking) object;
		if (getSize() != marking.getSize()) {
			// A marking with less places marked is considered smaller.
			return getSize() - marking.getSize();
		}

		// Same amount of places

		if (getSize() == 0) {
			// both are empty markings
			return 0;
		}

		Iterator<Place> it = places.iterator();
		Iterator<Place> it2 = marking.places.iterator();
		Place pThis = it.next();
		Place pThat = it2.next();

		do {
			Place minPlace;
			if (pThat == null || pThis.compareTo(pThat) < 0) {
				minPlace = pThis;
				pThis = (it.hasNext() ? it.next() : null);
			} else {
				minPlace = pThat;
				pThat = (it2.hasNext() ? it2.next() : null);
			}
			if (getTokens(minPlace) < marking.getTokens(minPlace)) {
				return -1;
			} else if (getTokens(minPlace) > marking.getTokens(minPlace)) {
				return 1;
			}

		} while (pThis != null || pThat != null);

		return 0;
	}

	/**
	 * Returns the number of tokens for a given place. Could be OMEGA.
	 * 
	 * @param place
	 *            The given place.
	 * @return The number of tokens.
	 */
	public int getTokens(Place place) {
		if (tokens.containsKey(place.getIdKey())) {
			return (tokens.get(place.getIdKey())).intValue();
		} else {
			return 0;
		}
	}

	/**
	 * Add a given marking
	 * 
	 * @param marking
	 *            The marking to add
	 */
	public void add(Marking marking) {
		Place place;
		Iterator it = marking.iterator();
		while (it.hasNext()) {
			place = (Place) it.next();
			addPlace(place, marking.getTokens(place));
		}
	}

	/**
	 * Subtract a given marking
	 * 
	 * @param marking
	 *            The marking to subtract
	 */
	public void sub(Marking marking) {
		Place place;
		Iterator it = marking.iterator();
		while (it.hasNext()) {
			place = (Place) it.next();
			delPlace(place, marking.getTokens(place));
		}
	}

	/**
	 * Clear the marking.
	 */
	public void clear() {
		Iterator it = places.iterator();
		Place place;
		while (it.hasNext()) {
			place = (Place) it.next();
			tokens.remove(place.getIdKey());
			it.remove();
		}
		tokenCount = 0;
	}

	/**
	 * 
	 * @return String representation for the marking.
	 */
	public String toString() {
		String s = "[";
		Iterator it = places.iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			String i = (getTokens(place) != OMEGA ? "" + getTokens(place)
					: "++");
			s += "(" + String.valueOf(place);
			s += "," + i + ")";
			if (it.hasNext()) {
				s += ", ";
			}
		}
		s += "]";
		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int ret = 17;
		for (Place p : places) {
			ret = 37 * ret + p.hashCode() + tokens.get(p.getIdKey());
		}
		return ret;
	}

}
