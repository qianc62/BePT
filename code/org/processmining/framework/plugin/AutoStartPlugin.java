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

package org.processmining.framework.plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the interface of a plugin that can be started from the command line.
 * <p>
 * Implementing classes needs to be able to send all parameters required to
 * start this plugin. Furthermore, it needs to be able to parse all required
 * parameters from a combination of a String representing the name of the
 * parameter and a String representing the Value of the parameter. The
 * InvalidParameterException can be thrown if the parameter cannot be set for
 * this plugin and the NotAllParametersSetException can be thrown if not all
 * parameters are set when the plugin is started and not all parameters are set.
 * <p>
 * Furthermore, documentation of a plugin can be returned by the
 * <code>getHtmlDescription</code> method. This documentation is displayed in
 * the help system.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public interface AutoStartPlugin extends Plugin {

	/**
	 * This method returns a map from the names of parameters to the class of
	 * that parameter. The ProM framework will take care of setting all these
	 * parameters using the setParameter method.
	 * 
	 * @return Map
	 */
	public Map<String, Class> getParameters();

	/**
	 * this method is called by the framework to set the parameters of the
	 * implementing class. If the string provided as name is not in the keyset
	 * of the map returned by the getParameters() method, an
	 * InvalidParameterException should be thrown. The String value passed to
	 * this method should contain a string representation of an object of type
	 * getParameters().get(name); It is up to the implementing class to
	 * "unmarshal" this String into an object of the correct type.
	 * 
	 * @param name
	 *            String
	 * @param value
	 *            String
	 * @throws InvalidParameterException
	 */
	public void setParameter(String name, String value)
			throws InvalidParameterException;

}
