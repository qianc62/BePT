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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

import org.processmining.ProMSplash;
import org.processmining.framework.ui.Message;

/**
 * Defines a collection of plugins of a certain type.
 * <p>
 * This class is declared abstract, because subclasses need to specify which
 * type of plugins are accepted by the particular collection (i.e. they should
 * override the abstract <code>isValidPlugin</code> method).
 * <p>
 * Subclasses should also be implemented using the singleton pattern. This means
 * that there is only a single collection of algorithms during the execution of
 * the program.
 * <p>
 * In general, a subclass will look as follows:
 * 
 * <pre>
 * &lt;code&gt;
 * public class MyCollection extends PluginCollection {
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
 *     public boolean isValidPlugin(Plugin plugin) {
 *         return plugin instanceof MyPlugin;
 *     }
 * }
 * &lt;/code&gt;
 * </pre>
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

abstract public class PluginCollection {

	/**
	 * List of currently loaded plugins.
	 */
	private ArrayList<PluginListElement> plugins = new ArrayList<PluginListElement>();
	private Class<? extends Annotation> pluginAnnotationClass = null;

	protected PluginCollection() {
	}

	protected PluginCollection(Class<? extends Annotation> pluginAnnotationClass) {
		this.pluginAnnotationClass = pluginAnnotationClass;
	}

	/**
	 * Subclasses should implement this method to check whether this Plugin
	 * object is valid for this collection.
	 * 
	 * For instance, a collection of <code>MiningPlugin</code>'s might do
	 * something like <code>return plugin instanceof MiningPlugin;</code>
	 * Plugins are only loaded when they are valid according to this method
	 * (i.e. it returns true).
	 * 
	 * @param plugin
	 *            the plugin to check whether it is valid
	 * @return true if the plugin may be added to the collection, false
	 *         otherwise.
	 */
	public abstract boolean isValidPlugin(Plugin plugin);

	/**
	 * Load plugins from ini file. Each line in the ini file should be of the
	 * form Key=Value Plugins in the plugin collection are sorted based on the
	 * Key in the ini file. The Value should be the full class name of the
	 * plugin to load.
	 * 
	 * @param filename
	 *            ini file to load plugins from
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

				loadPlugin(ini.getProperty(key), key, splash);
			}
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		}
	}

	/**
	 * Load plugins from a semi-colon separated list of class names.
	 * 
	 * @param list
	 *            a semi-colon separated list of class names of plugins
	 * @param splash
	 *            the prom splashscreen to write messages to
	 * 
	 *            public void loadFromList(String list, ProMSplash splash) {
	 *            StringTokenizer st = new StringTokenizer(list, ";");
	 * 
	 *            while (st.hasMoreTokens()) { loadPlugin(st.nextToken(), null,
	 *            splash); } }
	 */

	/**
	 * Number of plugins in the collection.
	 * 
	 * @return number of plugins in the collection
	 */
	public int size() {
		return plugins.size();
	}

	/**
	 * Get a plugin by its index in the collection.
	 * 
	 * @param index
	 *            the index of the plugin in the list (in the range [0..size() -
	 *            1])
	 * @return the plugin
	 */
	public Plugin get(int index) {
		return ((PluginListElement) plugins.get(index)).getPlugin();
	}

	/**
	 * Return the whole plugin collection.
	 * 
	 * @return the plugins
	 */
	public ArrayList<Plugin> getPlugins() {
		ArrayList<Plugin> list = new ArrayList<Plugin>();
		for (int i = 0; i < plugins.size(); i++) {
			list.add(get(i));
		}
		return list;
	}

	/**
	 * Get a plugin by its name.
	 * 
	 * @param name
	 *            the name of the plugin to get
	 * @return the plugin if it's found in the collection, null otherwise
	 */
	public Plugin get(String name) {
		if (name == null) {
			return null;
		}

		for (int i = 0; i < size(); i++) {
			if (get(i) != null) {
				if (name.equals(get(i).getName())) {
					return get(i);
				}
			}
		}
		return null;
	}

	/**
	 * Get a plugin by its name.
	 * 
	 * @param name
	 *            the name of the plugin to get
	 * @return the plugin if it's found in the collection, null otherwise
	 */
	public Plugin getByKey(String key) {
		if (key == null) {
			return null;
		}

		for (int i = 0; i < size(); i++) {
			if (get(i) != null) {
				if (key.equals(plugins.get(i).getSortName())) {
					return get(i);
				}
			}
		}
		return null;
	}

	protected void addPlugin(Plugin plugin, String sortName, ProMSplash splash) {
		try {
			splash.changeText("adding: "
					+ (plugin == null ? "separator" : plugin.getClass()
							.getName()), Message.DEBUG);
			plugins.add(new PluginListElement(plugin, sortName));
			Collections.sort(plugins);
		} catch (IncompatibleClassChangeError e) {
			System.out.println("Error while adding plugin with sortName "
					+ sortName + " to collection " + this.getClass().getName());
		}
	}

	/**
	 * Loads a single plugin. Loading errors are printed in the standard error
	 * stream.
	 * 
	 * @param name
	 *            the class name of the plugin to load
	 * @param sortName
	 *            the sort key
	 * @param splash
	 *            the splashscreen to write messages to
	 */
	protected void loadPlugin(String name, String sortName, ProMSplash splash) {
		if (name == null) {
			return;
		}
		name = name.trim();
		if (name.equals("") || get(name) != null) {
			return;
		}
		if (name.equals("separator")) {
			addPlugin(null, sortName, splash);
			return;
		}

		try {
			Class pluginClass = Class.forName(name, true, Thread
					.currentThread().getContextClassLoader());

			boolean foundAny = findAnnotatedPluginMethods(pluginClass,
					sortName, splash);

			if (Plugin.class.isAssignableFrom(pluginClass)) {
				Plugin plugin = (Plugin) pluginClass.newInstance();

				if (isValidPlugin(plugin)) {
					addPlugin(plugin, sortName, splash);
					foundAny = true;
				}
			}
			if (!foundAny) {
				splash.changeText("The class '" + name + "'"
						+ (sortName == null ? "" : " (" + sortName + ")")
						+ " is not a valid implementation", Message.ERROR);
			}
		} catch (ClassCastException ex) {
			splash.changeText("The class '" + name + "'"
					+ (sortName == null ? "" : " (" + sortName + ")")
					+ " is not a valid implementation", Message.ERROR);
		} catch (NoClassDefFoundError ex) {
			String s = ex.toString();
			s = s.substring(31);
			splash.changeText("The class '" + s + "'"
					+ (sortName == null ? "" : " ( requested by " + name + ")")
					+ " could not be found", Message.ERROR);
		} catch (Throwable t) {
			splash.changeText("Could not load plugin '"
					+ name
					+ "'"
					+ (sortName == null ? "" : " (" + sortName + "): "
							+ t.getMessage()), Message.ERROR);
		}
	}

	private boolean findAnnotatedPluginMethods(Class<?> pluginClass,
			String sortName, ProMSplash splash) {
		boolean foundAny = false;

		if (pluginAnnotationClass != null) {
			try {
				for (Method method : pluginClass.getMethods()) {
					if (method.isAnnotationPresent(pluginAnnotationClass)) {
						AnnotatedPlugin plugin = createFromAnnotation(
								pluginClass, method, method
										.getAnnotation(pluginAnnotationClass),
								splash);

						if (plugin != null) {
							try {
								plugin.validate();
								addPlugin(
										plugin,
										(plugin.getSortName() == null || plugin
												.getSortName().length() == 0) ? sortName
												: plugin.getSortName(), splash);
								foundAny = true;
							} catch (Exception e) {
								splash.changeText("Error loading plugin: "
										+ e.getMessage(), Message.ERROR);
							}
						}
					}
				}
			} catch (SecurityException se) {
				splash.changeText("Could not check for annotated plugins in '"
						+ pluginClass.getName()
						+ "'"
						+ (sortName == null ? "" : " (" + sortName + "): "
								+ se.getMessage()), Message.ERROR);
			}
		}
		return foundAny;
	}

	protected AnnotatedPlugin createFromAnnotation(Class<?> pluginClass,
			Method pluginMethod, Annotation annotation, ProMSplash splash) {
		return null;
	}
}

class PluginListElement implements Comparable<PluginListElement> {

	private Plugin plugin;
	private String pluginName;
	private String sortName;
	private static int sepindex = 0;

	public PluginListElement(Plugin plugin, String sortName) {
		this.plugin = plugin;
		if (plugin != null) {
			this.pluginName = plugin.getName();
		} else {
			this.pluginName = "separator " + sepindex++;
		}
		this.sortName = (sortName == null ? "" : sortName.toLowerCase());
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public boolean equals(Object o) {
		return (o instanceof PluginListElement) && o != null ? pluginName
				.equals(((PluginListElement) o).pluginName) : false;
	}

	public String toString() {
		return sortName.equals("") ? "__nosortname = " + pluginName : sortName
				+ " = " + pluginName;
	}

	public int compareTo(PluginListElement ale) {
		// first sort on sortName, then on plugin.getName()
		return sortName.equals(ale.sortName) ? plugin.getName().compareTo(
				ale.plugin.getName()) : sortName.compareTo(ale.sortName);
	}

	/**
	 * getSortName
	 * 
	 * @return Object
	 */
	public String getSortName() {
		return sortName;
	}
}
