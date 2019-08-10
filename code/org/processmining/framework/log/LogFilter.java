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

package org.processmining.framework.log;

import java.io.BufferedWriter;
import java.io.IOException;

import org.processmining.framework.log.filter.LogFilterCollection;
import org.processmining.framework.log.filter.LogFilterParameterDialog;
import org.w3c.dom.Node;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

/**
 * A generic abstract class for a workflow log filter.
 * <p>
 * When reading a log, the process instances are read one by one. The
 * <code>filter</code> method of the selected log filter is called for each
 * process instance. This <code>filter</code> method can then modify the
 * contents of the given process instance. For example, certain audit trail
 * entries can be deleted. The process instance is completely ignored
 * (discarded) if the <code>filter</code> returns false.
 * 
 * The LogFilter objects can be stacked, which means that they are capable of
 * calling eachothers <code>filter</code> method. Note that each LogFilter
 * should call the <code>filter</code> method of the lower level filter first
 * for efficiency reasons.
 * 
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */
public abstract class LogFilter {

	/**
	 * FAST should be used if the computation time of the <code>filter</code>
	 * method does not depend on the size of the process instance (i.e. it is
	 * constant).
	 */
	public final static int FAST = 0;

	/**
	 * AVERAGE should be used if the computation time of the <code>filter</code>
	 * method depends polynomially on the size of the process instance .
	 */
	public final static int MODERATE = 1;

	/**
	 * SLOW should be used if the computation time of the <code>filter</code>
	 * method depends exponentially on the size of the process instance .
	 */
	public final static int SLOW = 2;

	/**
	 * A integer that can have three values: FAST, AVERAGE, SLOW. This should be
	 * set in the constructor (default is SLOW) to tell how computationally
	 * intensive this filter is. FAST should be used if the computation time of
	 * the <code>filter</code> method does not depend on the size of the process
	 * instance (i.e. it is constant). AVERAGE if it is linear in the size of
	 * the instance and SLOW otherwise.
	 */
	protected int load;

	/**
	 * Variable to store the name of the plugin.
	 */
	private String name;

	/**
	 * A LogFilter with a pointer to the lower level filter. When filtering, the
	 * <code>filter(ProcessInstance pi)</code> method should first call
	 * <code>filter.filter(pi)</code>. If that returns false, no further
	 * filtering is necessary.
	 * 
	 * Note that this is handled in the Filter method of LogFilter and should
	 * NOT be repeated in the doFiltering implementations.
	 */
	private LogFilter filter = null;

	/**
	 * Provides access to the lower level log filter used by this instance.
	 * 
	 * @return
	 */
	public LogFilter getFilter() {
		return filter;
	}

	/**
	 * Instantiates a LogFilter with a pointer to the lower level filter. When
	 * filtering, the <code>filter(ProcessInstance pi)</code> method should
	 * first call <code>filter.filter(pi)</code>. If that returns false, no
	 * further filtering is necessary.
	 * 
	 * @param lowLevelFilter
	 *            the filter that should be checked first, to see if the
	 *            instance can be discared anyway. Note that this filter can be
	 *            null.
	 * @param load
	 *            the load of this LogFilter (i.e. SLOW, AVERAGE or FAST)
	 * @param name
	 *            The name of the LogFilter
	 */
	public LogFilter(int load, String name) {
		super();
		this.load = load;
		this.name = name;
	}

	/**
	 * Filters a single process instance. The process instance can be modified.
	 * Note that the implementing classes can assume that the instance is not
	 * empty, i.e. instance.isEmpty() == false.
	 * 
	 * @param instance
	 *            the process instance to filter
	 * @return true if the whole process instance passes the filter, false if
	 *         the process instance should be discarded.
	 */
	protected abstract boolean doFiltering(ProcessInstance instance);

	/**
	 * Filters a single process instance. The process instance can be modified.
	 * This method should start with the following code:
	 * 
	 * <code>if ((filter!=null) && !filter.filter(instance)  || instance.isEmpty()) {return false;}</code>
	 * 
	 * @param instance
	 *            the process instance to filter
	 * @return true if the whole process instance passes the filter, false if
	 *         the process instance should be discarded.
	 */
	public final boolean filter(ProcessInstance instance) {
		if (instance.isEmpty()) {
			return false;
		} else if (filter != null) {
			boolean ok = filter.filter(instance);
			if (!ok || instance.isEmpty()) {
				return false;
			}
		}
		return doFiltering(instance);
	}

	/**
	 * Returns the name of this LogFilter
	 * 
	 * @return String
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Sets the name of this LogFilter
	 * 
	 * @param name
	 *            new name of this LogFilter
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of this LogFilter
	 * 
	 * @return String
	 */
	public String toString() {
		return getName();
	}

	/**
	 * Method to tell whether this LogFilter changes the log or not. This method
	 * is used in the <code>compare</code> method.
	 * 
	 * @return boolean True if this LogFilter changes the process instance in
	 *         the <code>filter()</code> method. False otherwise.
	 */
	protected abstract boolean thisFilterChangesLog();

	/**
	 * Method to tell whether this LogFilter changes the log or not. This method
	 * is used in the <code>compare</code> method.
	 * 
	 * @return boolean True if this LogFilter changes the process instance in
	 *         the <code>filter()</code> method, or the lower level filter does.
	 *         False otherwise.
	 */
	public final boolean changesLog() {
		return thisFilterChangesLog()
				|| (filter == null ? false : filter.changesLog());
	}

	/**
	 * Returns a Panel for the setting of parameters. When a LogFilter can be
	 * added to a list in the framework. This panel is shown, and parameters can
	 * be set. When the dialog is closed, a new instance of a LogFilter is
	 * created by the framework by calling the <code>getNewLogFilter</code>
	 * method of the dialog.
	 * 
	 * @param summary
	 *            A LogSummary to be used for setting parameters.
	 * @return JPanel
	 */
	public abstract LogFilterParameterDialog getParameterDialog(
			LogSummary summary);

	/**
	 * Sets the lower level filter for this log. Every implementation to
	 * this.filter() shold call filter.filter() first!
	 * 
	 * @param filter
	 *            LogFilter
	 */
	public final void setLowLevelFilter(LogFilter filter) {
		this.filter = filter;
	}

	/**
	 * Returns either SLOW, MODERATE or FAST
	 * 
	 * @return int
	 */
	protected int getThisLogFilterComplexity() {
		return load;
	}

	/**
	 * Returns either SLOW, MODERATE or FAST
	 * 
	 * @return int
	 */
	public final int getComplexity() {
		return (filter == null ? load : Math.max(load, filter.getComplexity()));
	}

	/**
	 * Returns either "Slow", "Moderate" or "Fast"
	 * 
	 * @return int
	 */
	public final String getComplexityAsString() {
		int i = getComplexity();
		if (i == FAST) {
			return "Fast";
		} else if (i == MODERATE) {
			return "Moderate";
		} else {
			return "Slow";
		}
	}

	/**
	 * Returns the help for this LogFilter as HTML text without the <html> and
	 * </html> tags!
	 * 
	 * @return the help as string
	 */
	public final String getHelp() {
		return (filter == null ? "" : "" + filter.getHelp() + "<p>") + "<i>"
				+ getName() + ":</i><br>" + getHelpForThisLogFilter();
	}

	/**
	 * Returns the help for this LogFilter as HTML text, but without the <html>
	 * and </html> tags!
	 * 
	 * @return the help as string
	 */
	protected abstract String getHelpForThisLogFilter();

	/**
	 * Write the inside of the <FilterSpecific> tag in the XML export file to
	 * the OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	protected abstract void writeSpecificXML(BufferedWriter output)
			throws IOException;

	/**
	 * Read the inside of the <FilterSpecific> tag in the XML export file from
	 * the InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	protected abstract void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException;

	/**
	 * Write the inside of the <FilterSpecific> tag in the XML export file to
	 * the OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	public void writeXML(BufferedWriter output) throws IOException {
		output.write("<LogFilter load=\"" + load + "\" name=\"" + name
				+ "\" class=\"" + getClass().getName() + "\">\n");
		output.write("<FilterSpecific>\n");
		writeSpecificXML(output);
		output.write("</FilterSpecific>\n");
		if (filter != null) {
			filter.writeXML(output);
		}
		output.write("</LogFilter>\n");
	}

	/**
	 * Read the inside of the <LogFilter> tag in the XML export file from the
	 * InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	public static LogFilter readXML(Node logFilterNode) throws IOException,
			ClassNotFoundException, IllegalAccessException,
			InstantiationException {
		// Assume logFilterNode is the Node containing LogFilter;
		String className = logFilterNode.getAttributes().getNamedItem("class")
				.getNodeValue();
		LogFilter result = LogFilterCollection.getInstance().get(className)
				.getClass().newInstance();
		// LogFilter result = (LogFilter) Class.forName(className, true,
		// Thread.currentThread().getContextClassLoader()).newInstance();

		result.load = Integer.parseInt(logFilterNode.getAttributes()
				.getNamedItem("load").getNodeValue());
		result.name = logFilterNode.getAttributes().getNamedItem("name")
				.getNodeValue();

		for (int i = 0; i < logFilterNode.getChildNodes().getLength(); i++) {
			if (logFilterNode.getChildNodes().item(i).getNodeName().equals(
					"FilterSpecific")) {
				// Read the nodeSpecific part of this LogFilter
				result.readSpecificXML(logFilterNode.getChildNodes().item(i));
			} else if (logFilterNode.getChildNodes().item(i).getNodeName()
					.equals("LogFilter")) {
				result.setLowLevelFilter(LogFilter.readXML(logFilterNode
						.getChildNodes().item(i)));
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LogFilter) {
			LogFilter other = (LogFilter) obj;
			if (obj != this) {
				return false;
			} else {
				if (this.filter != null && other.filter == null) {
					return false;
				} else if (this.filter == null && other.filter != null) {
					return false;
				} else if (this.filter == null && other.filter == null) {
					return true;
				} else {
					return this.filter.equals(other.filter);
				}
			}
		} else {
			return false;
		}
	}

}
