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

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>
 * Title: WFNet
 * </p>
 * 
 * <p>
 * Description: Class for workflow nets (WF nets)
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class WFNet extends PetriNet {

	// A F net is a Petri net with one source place and one sink place.

	Place sourcePlace, sinkPlace;

	public WFNet() {
		sourcePlace = null;
		sinkPlace = null;
	}

	public void setSourcePlace(Place place) {
		sourcePlace = place;
	}

	public void setSourcePlace(String name) {
		sourcePlace = findPlace(name);
	}

	public Place getSourcePlace() {
		return sourcePlace;
	}

	public void setSinkPlace(Place place) {
		sinkPlace = place;
	}

	public void setSinkPlace(String name) {
		sinkPlace = findPlace(name);
	}

	public Place getSinkPlace() {
		return sinkPlace;
	}
}
