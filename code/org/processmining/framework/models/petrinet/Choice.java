package org.processmining.framework.models.petrinet;

/**
 * <p>
 * Title: Choice
 * </p>
 * 
 * <p>
 * Description: Describes the possibles types of choice a place can have
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: University of Aarhus
 * </p>
 * 
 * @author Kristian Bisgaard Lassen (<a
 *         href="mailto:K.B.Lassen@daimi.au.dk">mailto
 *         :K.B.Lassen@daimi.au.dk</a>)
 * @version 1.0
 */
public enum Choice {
	/**
	 * The choice is an explicit choice. This means that the incoming
	 * transitions intersection with outgoing transitions are empty, and all
	 * outgoing transitions are silent.
	 */
	EXPLICIT,
	/**
	 * The choice is an implicit choice. This means that the incoming
	 * transitions intersection with outgoing transitions are empty, and all
	 * outgoing transitions are NOT silent.
	 */
	IMPLICIT,
	/**
	 * The choice is not defined. This means that the incoming transitions
	 * intersection with outgoing transitions are empty, but the outgoing
	 * transitions are a mix of silent and non-silent transitions.
	 */
	NOT_DEFINED,
	/**
	 * The choice is not a choice. This means that the incoming transitions
	 * intersection with outgoing transitions are NOT empty.
	 */
	NOT_CHOICE
}
