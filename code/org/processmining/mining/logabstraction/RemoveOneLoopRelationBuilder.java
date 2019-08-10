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

package org.processmining.mining.logabstraction;

import cern.colt.matrix.DoubleFactory1D;

/**
 * Removes all one length loop entries in the relations tables.
 * 
 * @author not attributable
 * @version 1.0
 */

public class RemoveOneLoopRelationBuilder implements LogRelationBuilder {

	private LogRelations relations;

	public RemoveOneLoopRelationBuilder(LogRelations relations) {
		this.relations = new LogRelationsImpl(relations
				.getCausalFollowerMatrix(), relations.getParallelMatrix(),
				relations.getEndInfo(), relations.getStartInfo(),
				DoubleFactory1D.sparse.make(relations.getOneLengthLoopsInfo()
						.size(), 0), relations.getLogEvents());
	}

	public LogRelations getLogRelations() {
		return relations;
	}
}
