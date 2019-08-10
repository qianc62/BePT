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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.processmining.framework.models.ModelGraphVertex;
import qc.common.Common;

/**
 * A place is node in a Petri net. It is the passive component in the Petri net
 * structure, and may hold zero to an arbitrary number of tokens. The marking of
 * all the places in the net relates to the state of the Petri net.
 * 
 * @see Token
 * @see Transition
 * @see PetriNet
 * 
 * @author not attributable
 */

public class Place extends PNNode implements Comparable, Cloneable {

	private ArrayList<Token> tokens; /* holds the tokens for this place */
	private int number; /* can be used for numbering transitions in a unique way */

	/**
	 * Constructor.
	 * 
	 * @param identifier
	 *            the name of the this place
	 * @param net
	 *            the PetriNet this place belongs to
	 */
	public Place(String identifier, PetriNet net) {
		super(net);
		this.identifier = identifier;
		tokens = new ArrayList<Token>();
	}

	/**
	 * Adds the specified token to this place. Note that if the given token is
	 * already held by this place, it is not added again.
	 * 
	 * @param to
	 *            the token that should be added
	 * @return the added token
	 */
	public Token addToken(Token to) {
		if (!tokens.contains(to)) {
			tokens.add(to);
		}
		return to;
	}

	/**
	 * Removes the specified token from this place.
	 * 
	 * @param to
	 *            the token that should be removed
	 * @return the token that has been removed if it was found,
	 *         <code>null</code> otherwise
	 */
	public Token removeToken(Token to) {
		if (!tokens.contains(to)) {
			return null;
		}
		tokens.remove(to);
		return to;
	}

	/**
	 * Removes all tokens held by this place.
	 */
	public void removeAllTokens() {
		tokens.clear();
	}

	/**
	 * Removes one of the tokens held by this place.
	 * 
	 * @return the token that has been chosen to be removed if there is at least
	 *         one available, <code>null</code> otherwise
	 */
	public Token removeToken() {
		if (tokens.size() == 0) {
			return null;
		}
		return removeToken((Token) tokens.toArray()[0]);
	}

	/**
	 * Gets the number of tokens held by this place. If a timestamp should be
	 * considered, use {@link getNumberOfTokens(Date timestamp)
	 * getNumberOfTokens(Date timestamp)} instead.
	 * 
	 * @return the amount of tokens in this place
	 */
	public int getNumberOfTokens() {
		return tokens.size();
	}

	/**
	 * Gets the number of tokens that are available at the given timestamp at
	 * that place. If a timestamp is not important, use {@link
	 * getNumberOfTokens() getNumberOfTokens()} instead.
	 * 
	 * @param timestamp
	 *            the time at which the tokens must be available
	 * @return the amount of tokens available at the given time
	 */
	public int getNumberOfTokens(Date timestamp) {
		Iterator it = tokens.iterator();
		int i = 0;
		while (it.hasNext()) {
			Token t2 = (Token) it.next();
			if (!t2.isTimed()
					|| (t2.getTimestamp() == null ? true : !(t2.getTimestamp()
							.after(timestamp)))) {
				i++;
			}
		}
		return i;
	}

	/**
	 * Gets one of the tokens from this place that is available at the given
	 * timestamp. If a timestamp is not important, use {@link
	 * getRandomAvailableToken() getRandomAvailableToken()} instead.
	 * 
	 * @param timestamp
	 *            the time at which the token must be available
	 * @return one of the tokens from this place if there is any available,
	 *         <code>null</code> otherwise
	 */
	public Token getRandomAvailableToken(Date timestamp) {
		Token t = null;
		Iterator it = tokens.iterator();
		while (it.hasNext() && (t == null)) {
			Token t2 = (Token) it.next();
			if (!t2.isTimed()
					|| (t2.getTimestamp() == null ? true : !(t2.getTimestamp()
							.after(timestamp)))) {
				t = t2;
			}
		}
		return t;
	}

	/**
	 * Gets one of the tokens from this place. If a timestamp should be
	 * considered, use {@link getRandomAvailableToken(Date timestamp)
	 * getRandomAvailableToken(Date timestamp)} instead.
	 * 
	 * @return one token from this place if the place is marked,
	 *         <code>null</code> otherwise
	 */
	public Token getRandomAvailableToken() {
		if (tokens.size() == 0) {
			return null;
		}
		return (Token) tokens.toArray()[0];

	}

	/**
	 * Determines whether this place has the given identifier associated.
	 * 
	 * @param id
	 *            the name to compare with
	 * @return <code>true</code> if this place has the same name as the given
	 *         string, <code>false</code> otherwise
	 */
	public boolean hasIdentifier(String id) {
		return identifier.equals(id);
	}

	/**
	 * Assigns a number to this place.
	 * <p>
	 * Since the identifier of a place is not necessarily unique (i.e., two
	 * places can have the same name) this method can be used to assign unique
	 * numbers to the places of a Petri net, if needed. {@link getNumber
	 * getNumber()} can be used to retrieve the number.
	 * 
	 * @param n
	 *            the number to be assigned
	 */
	public void setNumber(int n) {
		number = n;
	}

	/**
	 * Returns the number which is associated to this place.
	 * <p>
	 * Since the identifier of a place is not necessarily unique (i.e., two
	 * places can have the same name) this method can be used to retrieve unique
	 * numbers from the places of a Petri net, if needed. However, they are not
	 * associated automatically but must have been set using the
	 * {@link setNumber setNumber(int i)} method before.
	 * 
	 * @return the number associated
	 */
	public int getNumber() {
		return number;
	}

	// //////////////// GENERAL PURPOSE METHODS
	// /////////////////////////////////

	/**
	 * @todo: provide documentation
	 * @param object
	 *            Object
	 * @return int
	 */
	public int compareTo(Object object) {
		Place place = (Place) object;
		// return identifier.compareTo(place.identifier);
		return getId() - place.getId();
	}

	/**
	 * Overridden to specify when two Petri net places are considered to be
	 * equal.
	 * 
	 * @param o
	 *            the <code>Place</code> to be compared with
	 * @return <code>true</code> if the identifiers are the same,
	 *         <code>false</code> otherwise.
	 */
	public boolean equals(Object o) {
		// check object identity first
		if (this == o) {
			return true;
		}
		// check type (which includes check for null)
		return (o instanceof Place) && identifier == (((Place) o).identifier);
	}

	/**
	 * Overridden to produce the same hash code for equal objects.
	 * 
	 * @return int The hash code calculated.
	 */
	public int hashCode() {
		// simple recipe for generating hashCode given by
		// Effective Java (Addison-Wesley, 2001)
		int result = 17;
		result = 37 * result + identifier.hashCode();
		return result;
	}

	/**
	 * Make a deep copy of the object. Note that this method needs to be
	 * extended as soon as there are attributes added to the class which are not
	 * primitive or immutable. <br>
	 * Note further that the belonging Petri net is not cloned (so the cloned
	 * object will point to the same one as this object). Only the
	 * {@link PetriNet#clone PetriNet.clone()} method will update the refernce
	 * correspondingly.
	 * 
	 * @return Object the cloned object
	 */
	public Object clone() {
		Place o = null;
		o = (Place) super.clone();
		// clone referenced objects to realize deep copy
		o.tokens = new ArrayList<Token>();
		Iterator allTokens = this.tokens.iterator();
		while (allTokens.hasNext()) {
			Token currentToken = (Token) allTokens.next();
			Token clonedToken = (Token) currentToken.clone();
			o.addToken(clonedToken);
		}
		return o;
	}
}
