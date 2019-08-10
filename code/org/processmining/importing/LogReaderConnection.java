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

package org.processmining.importing;

import java.util.ArrayList;
import java.util.HashMap;

import org.processmining.framework.log.LogReader;

/**
 * Allows a <code>LogReader</code> to be connected to other objects, like a
 * particular <code>MiningResult</code>.
 * <p>
 * This is used by import plugins to associate a LogReader with an imported
 * file.
 * 
 * @author Peter van den Brand, Boudewijn van Dongen
 * @version 1.0
 */

public interface LogReaderConnection {

	/**
	 * Returns all connectable objects of the underlying model.
	 * 
	 * @return all connectable objects of the underlying model
	 */
	public ArrayList getConnectableObjects();

	/**
	 * Connects a <code>LogReader</code> to the object. The
	 * <code>eventsMapping</code> variable is a <code>HashMap</code> that has a
	 * key for every <code>Object</code> returned by the
	 * <code>getConnectableObjects</code> method. Each key is an
	 * <code>Object</code> and it is associated with an <code>Object[2]</code>
	 * object. This array contains two objects. The first object is a
	 * <code>LogEvent</code> object, to which the original should be mapped. The
	 * second is a <code>String</code> object, representing the label that
	 * should be used for the identifier of the underlying graphical object. The
	 * <code>eventsMapping</code> parameter may be <code>null</code>.
	 * 
	 * @param newLog
	 *            the log reader to connect
	 * @param eventsMapping
	 *            the events to map
	 */
	public void connectWith(LogReader newLog, HashMap eventsMapping);
}
