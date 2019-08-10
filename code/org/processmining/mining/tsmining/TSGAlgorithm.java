package org.processmining.mining.tsmining;

import java.util.Collection;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.models.transitionsystem.TSConstants;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public abstract class TSGAlgorithm {
	/*
	 * public final static int MODEL_ELEMENTS = 1; public final static int
	 * EVENT_TYPES = 2; public final static int EXPLICIT_END = 4; public final
	 * static int KILL_LOOPS = 8;
	 * 
	 * public final static int SETS = 1; public final static int BAGS = 2;
	 * public final static int BASIC = 4; public final static int EXTENDED = 8;
	 */

	protected TransitionSystem ts;
	protected LogReader log;
	protected DocsVertexCreator factory;

	int genFlags;
	int visFlags;
	int typeOfTS;
	int setsOrBags;

	// a method for generating the basic TS
	public abstract TransitionSystem generateTS();

	// a method for extending the basic TS
	// public abstract void extendTS();

	public TSGAlgorithm(LogReader log, int typeOfTS, int genFlags, int visFlags) {
		this.log = log;
		this.typeOfTS = typeOfTS;
		this.genFlags = genFlags;
		this.visFlags = visFlags;
		factory = new DocsVertexCreator();
		if ((typeOfTS & TSConstants.SETS) == TSConstants.SETS)
			setsOrBags = TSConstants.SETS;
		else
			setsOrBags = TSConstants.BAGS;

		ts = generateTS();
		/*
		 * if( (typeOfTS & EXTENDED) == EXTENDED) { extendTS(); }
		 */
	}

	public TransitionSystem getTransitionSystem() {
		return ts;
	}

	boolean isIntersection(Collection a, Collection b) {
		Iterator it = b.iterator();
		while (it.hasNext())
			if (a.contains(it.next()))
				return true;
		return false;
	}
}
