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

package org.processmining.framework.log.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import org.processmining.ProMSplash;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.ui.Message;
import org.processmining.importing.ProMInputStream;
import org.processmining.importing.log.LogFilterImportPlugin;

/**
 * Defines a collection of logFilters of a certain type.
 * <p>
 * This class is declared abstract, because subclasses need to specify which
 * type of logFilters are accepted by the particular collection (i.e. they
 * should override the abstract <code>isValidlogFilter</code> method).
 * <p>
 * Subclasses should also be implemented using the singleton pattern. This means
 * that there is only a single collection of algorithms during the execution of
 * the program.
 * <p>
 * In general, a subclass will look as follows:
 * 
 * <pre>
 * &lt;code&gt;
 * public class MyCollection extends logFilterCollection {
 *     private static MyCollection instance = null;
 * 
 *     protected MyCollection() {}
 * 
 *     public static MyCollection getInstance() {
 *         if (instance == null) {
 *            instance = new MyCollection();
 *         }
 *         return instance;
 *     }
 * 
 *     public boolean isValidlogFilter(logFilter logFilter) {
 *         return logFilter instanceof MylogFilter;
 *     }
 * }
 * &lt;/code&gt;
 * </pre>
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public class LogFilterCollection {

	/**
	 * List of currently loaded logFilters.
	 */
	private ArrayList logFilters = new ArrayList();

	/**
	 * Current instance of LogFilterCollection
	 */
	private static LogFilterCollection instance;

	protected LogFilterCollection() {
	}

	/**
	 * Returns an instance of an <code>MiningPluginCollection</code>.
	 * 
	 * @return an instance of an <code>MiningPluginCollection</code>
	 */
	public static LogFilterCollection getInstance() {
		if (instance == null) {
			instance = new LogFilterCollection();
		}
		return instance;
	}

	/**
	 * Load logFilters from ini file. Each line in the ini file should be of the
	 * form Key=Value logFilters in the logFilter collection are sorted based on
	 * the Key in the ini file. The Value should be the full class name of the
	 * logFilter to load.
	 * 
	 * @param filename
	 *            ini file to load logFilters from
	 * @param splash
	 *            splashscreen to write messages to
	 */
	public void loadFromIni(String filename, ProMSplash splash) {
		try {
			Properties ini = new Properties();
			FileInputStream is = new FileInputStream(filename);
			Enumeration i;

			ini.load(is);
			is.close();

			i = ini.propertyNames();
			while (i.hasMoreElements()) {
				String key = (String) i.nextElement();

				loadlogFilter(ini.getProperty(key), key, splash);
			}
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		}
	}

	/**
	 * Load logFilters from a semi-colon separated list of class names.
	 * 
	 * @param list
	 *            a semi-colon separated list of class names of logFilters
	 * @param splash
	 *            the prom splashscreen to write messages to
	 */
	public void loadFromList(String list, ProMSplash splash) {
		StringTokenizer st = new StringTokenizer(list, ";");

		while (st.hasMoreTokens()) {
			loadlogFilter(st.nextToken(), null, splash);
		}
	}

	/**
	 * Number of logFilters in the collection.
	 * 
	 * @return number of logFilters in the collection
	 */
	public int size() {
		return logFilters.size();
	}

	/**
	 * Get a logFilter by its index in the collection.
	 * 
	 * @param index
	 *            the index of the logFilter in the list (in the range
	 *            [0..size() - 1])
	 * @return the logFilter
	 */
	public LogFilter get(int index) {
		return ((LogFilterListElement) logFilters.get(index)).getLogFilter();
	}

	/**
	 * Get a logFilter by its name.
	 * 
	 * @param name
	 *            the name of the logFilter to get
	 * @return the logFilter if it's found in the collection, null otherwise
	 */
	public LogFilter get(String name) {
		if (name == null) {
			return null;
		}

		for (int i = 0; i < size(); i++) {
			if (name.equals(get(i).getName())
					|| name.equals(get(i).getClass().getName())) {
				return get(i);
			}
		}
		return null;
	}

	/**
	 * Loads a single logFilter. Loading errors are printed in the standard
	 * error stream.
	 * 
	 * @param name
	 *            the class name of the logFilter to load
	 * @param sortName
	 *            the sort key
	 * @param splash
	 *            the splashscreen to write messages to
	 */
	private void loadlogFilter(String name, String sortName, ProMSplash splash) {
		if (name == null) {
			return;
		}

		name = name.trim();
		if (name.equals("") || get(name) != null) {
			return;
		}
		try {
			splash.changeText("adding: " + name, Message.DEBUG);
			LogFilter logFilter = null;
			if (name.startsWith("file://")) {
				// We have to import from file'
				name = name.substring("file://".length());
				name = (new File(name)).getAbsolutePath();
				FileInputStream input = new ProMInputStream(name);
				LogFilterImportPlugin li = new LogFilterImportPlugin();
				li.importFile(input);
				input.close();
			} else {
				Class logFilterClass = Class.forName(name, true, Thread
						.currentThread().getContextClassLoader());
				logFilter = (LogFilter) logFilterClass.newInstance();
				add(logFilter, sortName);
			}
		} catch (ClassCastException ex) {
			splash.changeText("The class '" + name + "'"
					+ (sortName == null ? "" : " (" + sortName + ")")
					+ " is not a valid implementation", Message.ERROR);
		} catch (Exception ex1) {
			splash.changeText("Could not load logFilter '" + name + "'"
					+ (sortName == null ? "" : " (" + sortName + ") :")
					+ ex1.toString(), Message.ERROR);
		}
	}

	public void add(LogFilter logFilter, String sortName) {
		logFilters.add(new LogFilterListElement(logFilter, sortName));
		Collections.sort(logFilters);
	}
}

class LogFilterListElement implements Comparable {

	private LogFilter filter;
	private String sortName;

	public LogFilterListElement(LogFilter filter, String sortName) {
		this.filter = filter;
		this.sortName = (sortName == null ? "" : sortName.toLowerCase());
	}

	public LogFilter getLogFilter() {
		return filter;
	}

	public boolean equals(Object o) {
		return (o instanceof LogFilterListElement) && o != null ? filter
				.getName().equals(((LogFilterListElement) o).filter.getName())
				: false;
	}

	public String toString() {
		return sortName.equals("") ? "__nosortname = " + filter.getName()
				: sortName + " = " + filter.getName();
	}

	public int compareTo(Object o) {
		LogFilterListElement ale = (LogFilterListElement) o;

		// first sort on sortName, then on logFilter.getName()
		return sortName.equals(ale.sortName) ? filter.getName().compareTo(
				ale.filter.getName()) : sortName.compareTo(ale.sortName);
	}
}
