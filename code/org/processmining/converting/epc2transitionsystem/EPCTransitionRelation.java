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

package org.processmining.converting.epc2transitionsystem;

import java.util.HashSet;

import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.models.transitionsystem.TransitionSystemEdge;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertexSet;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Jan Mendling
 * @version 1.0
 */

public class EPCTransitionRelation {
	private HashSet<String> previousMarking;
	private HashSet<String> followingMarking;
	private String firingNode;

	public EPCTransitionRelation(HashSet<String> prev, HashSet<String> follow,
			String firing) {
		previousMarking = prev;
		followingMarking = follow;
		firingNode = firing;
	}

	public boolean addToTransitionSystem(TransitionSystem TS) {
		TransitionSystemVertexSet from = new TransitionSystemVertexSet(
				previousMarking, "", TS);
		TransitionSystemVertexSet to2 = new TransitionSystemVertexSet(
				followingMarking, "", TS);
		from = (TransitionSystemVertexSet) TS.addVertex(from);
		TransitionSystemVertexSet to = (TransitionSystemVertexSet) TS
				.addVertex(to2);
		TS.addEdge(new TransitionSystemEdge(firingNode, from, to));
		return (to == to2);
	}
}
