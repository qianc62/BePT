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

package org.processmining.analysis;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.ProMSplash;
import org.processmining.framework.plugin.AnnotatedPlugin;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.PluginCollection;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;

/**
 * Collection of analysis plugins.
 * 
 * @see AnalysisPlugin
 * @author Peter van den Brand
 * @version 1.0
 */

public class AnalysisPluginCollection extends PluginCollection {

	private static AnalysisPluginCollection instance = null;

	protected AnalysisPluginCollection() {
		super(Analyzer.class);
	}

	/**
	 * Returns an instance of an <code>AnalysisPluginCollection</code>.
	 * 
	 * @return an instance of an <code>AnalysisPluginCollection</code>
	 */
	public static AnalysisPluginCollection getInstance() {
		if (instance == null) {
			instance = new AnalysisPluginCollection();
		}
		return instance;
	}

	/**
	 * Returns <code>true</code> if the given plugin is an
	 * <code>AnalysisPlugin</code>.
	 * 
	 * @param plugin
	 *            the plugin to check
	 * @return <code>true</code> if the given plugin is an
	 *         <code>AnalysisPlugin</code>, <code>false</code> otherwise.
	 */
	public boolean isValidPlugin(Plugin plugin) {
		return plugin instanceof AnalysisPlugin;
	}

	@Override
	protected AnnotatedPlugin createFromAnnotation(Class<?> pluginClass,
			Method pluginMethod, Annotation annotation, ProMSplash splash) {
		return new AnnotatedAnalysisPlugin(pluginClass, pluginMethod,
				annotation);
	}
}

class AnnotatedAnalysisPlugin extends AnnotatedPlugin implements AnalysisPlugin {

	private String[] names;
	private boolean connected;

	public AnnotatedAnalysisPlugin(Class<?> pluginClass, Method pluginMethod,
			Annotation annotation) {
		super(pluginClass, pluginMethod, ((Analyzer) annotation).name(),
				((Analyzer) annotation).help(), ((Analyzer) annotation)
						.sortName());
		this.names = ((Analyzer) annotation).names();
		this.connected = ((Analyzer) annotation).connected();
	}

	@Override
	public void validate() throws Exception {
		if (getPluginMethod().getParameterTypes().length == 0) {
			throw new Exception(
					"Analysis plugin methods need to have at least one parameter (an object to analyze)");
		}
		if (!JComponent.class.isAssignableFrom(getPluginMethod()
				.getReturnType())) {
			throw new Exception(
					"Analysis plugin methods need to return an object which is derived from JComponent");
		}
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		return connected ? analyseConnected(inputs)
				: analyseDisconnected(inputs);
	}

	public AnalysisInputItem[] getInputItems() {
		return connected ? getConnectedInputItems()
				: getDisconnectedInputItems();
	}

	private JComponent analyseConnected(AnalysisInputItem[] inputs) {
		Class[] parameters = getPluginMethod().getParameterTypes();
		Object[] arguments = new Object[parameters.length];
		List<Object> provided = new LinkedList<Object>();

		for (Object obj : (inputs[0].getProvidedObjects())[0].getObjects()) {
			provided.add(obj);
		}
		for (int i = 0; i < parameters.length; i++) {
			Class<?> param = parameters[i];
			Iterator<Object> iter = provided.iterator();
			boolean found = false;

			while (iter.hasNext()) {
				Object obj = iter.next();
				if (param.isAssignableFrom(obj.getClass())) {
					iter.remove();
					arguments[i] = obj;
					found = true;
					break;
				}
			}
			if (!found) {
				return new JLabel(
						"Internal Error: provided objects do not match the requested objects. Please notify the developers of this problem.");
			}
		}
		try {
			return (JComponent) getPluginMethod().invoke(
					getNewPluginInstance(), arguments);
		} catch (Throwable t) {
			t.printStackTrace();
			return new JLabel("Error: " + t.getMessage());
		}
	}

	private JComponent analyseDisconnected(AnalysisInputItem[] inputs) {
		Class[] parameters = getPluginMethod().getParameterTypes();
		Object[] arguments = new Object[parameters.length];

		assert (inputs.length == parameters.length);

		for (int i = 0; i < parameters.length; i++) {
			arguments[i] = (inputs[i].getProvidedObjects())[0].getObjects()[0];
		}
		try {
			return (JComponent) getPluginMethod().invoke(
					getNewPluginInstance(), arguments);
		} catch (Throwable t) {
			t.printStackTrace();
			return new JLabel("Error: " + t.getMessage());
		}
	}

	private AnalysisInputItem[] getConnectedInputItems() {
		final Class[] parameters = getPluginMethod().getParameterTypes();
		String name = "";

		if (names.length > 0) {
			name = names[0];
		} else {
			for (Class param : parameters) {
				name += ", " + param.getName();
			}
			name = name.substring(2);
		}

		return new AnalysisInputItem[] { new AnalysisInputItem(name) {
			public boolean accepts(ProvidedObject object) {
				if (object == null) {
					return false;
				}

				List<Object> o = new LinkedList<Object>();
				for (Object obj : object.getObjects()) {
					if (obj != null) {
						o.add(obj);
					}
				}

				for (Class<?> param : parameters) {
					boolean found = false;

					for (int i = 0; !found && i < o.size(); i++) {
						if (param.isAssignableFrom(o.get(i).getClass())) {
							o.remove(i);
							found = true;
						}
					}
					if (!found) {
						return false;
					}
				}
				return true;
			}
		} };
	}

	private AnalysisInputItem[] getDisconnectedInputItems() {
		Class[] parameters = getPluginMethod().getParameterTypes();
		AnalysisInputItem[] items = new AnalysisInputItem[parameters.length];

		for (int i = 0; i < parameters.length; i++) {
			final Class<?> parameterType = parameters[i];
			String name = i < names.length ? names[i] : parameterType.getName();

			items[i] = new AnalysisInputItem(name) {
				public boolean accepts(ProvidedObject object) {
					if (object != null) {
						Object[] o = object.getObjects();

						if (o != null) {
							for (int i = 0; i < o.length; i++) {
								if (o[i] != null
										&& parameterType.isAssignableFrom(o[i]
												.getClass())) {
									return true;
								}
							}
						}
					}
					return false;
				}
			};
		}
		return items;
	}
}
