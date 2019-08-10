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

package org.processmining.converting;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.swing.JComponent;

import org.processmining.ProMSplash;
import org.processmining.framework.plugin.AnnotatedPlugin;
import org.processmining.framework.plugin.AnnotatedPluginAcceptingObject;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.PluginCollection;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.MiningResultImpl;

/**
 * Collection of mining algorithms.
 * 
 * @see MiningPlugin
 * @author Peter van den Brand
 * @version 1.0
 */

public class ConvertingPluginCollection extends PluginCollection {

	private static ConvertingPluginCollection instance = null;

	protected ConvertingPluginCollection() {
		super(Converter.class);
	}

	/**
	 * Returns an instance of an <code>ConvertingPluginCollection</code>.
	 * 
	 * @return an instance of an <code>ConvertingPluginCollection</code>
	 */
	public static ConvertingPluginCollection getInstance() {
		if (instance == null) {
			instance = new ConvertingPluginCollection();
		}
		return instance;
	}

	/**
	 * Returns <code>true</code> if the given plugin is an
	 * <code>ConvertingPlugin</code>.
	 * 
	 * @param plugin
	 *            the plugin to check
	 * @return <code>true</code> if the given plugin is an
	 *         <code>ConvertingPlugin</code>, <code>false</code> otherwise.
	 */
	public boolean isValidPlugin(Plugin plugin) {
		return plugin instanceof ConvertingPlugin;
	}

	@Override
	protected AnnotatedPlugin createFromAnnotation(Class<?> pluginClass,
			Method pluginMethod, Annotation annotation, ProMSplash splash) {
		return new AnnotatedConverterPlugin(pluginClass, pluginMethod,
				annotation);
	}
}

class AnnotatedConverterPlugin extends AnnotatedPluginAcceptingObject implements
		ConvertingPlugin {

	public AnnotatedConverterPlugin(Class<?> pluginClass, Method pluginMethod,
			Annotation annotation) {
		super(pluginClass, pluginMethod, ((Converter) annotation).name(),
				((Converter) annotation).help(), ((Converter) annotation)
						.sortName(), pluginMethod.getParameterTypes()[0]);
	}

	@Override
	protected void validate() throws Exception {
		if (getPluginMethod().getParameterTypes().length != 1) {
			throw new Exception(
					"Conversion plugin methods need to have exactly one parameter: the object to convert");
		}
		if (!JComponent.class.isAssignableFrom(getPluginMethod()
				.getReturnType())
				&& !MiningResult.class.isAssignableFrom(getPluginMethod()
						.getReturnType())) {
			throw new Exception(
					"Conversion plugin methods need to return an object which is derived from JComponent or MiningResult");
		}
	}

	public MiningResult convert(ProvidedObject object) {
		try {
			Object result = getPluginMethod().invoke(getNewPluginInstance(),
					findObject(object));

			if (result == null) {
				return null;
			} else if (MiningResult.class.isAssignableFrom(getPluginMethod()
					.getReturnType())) {
				return (MiningResult) result;
			} else {
				return new MiningResultImpl(null, (JComponent) result);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			Message.add("Error executing conversion: " + t.getMessage());
			return null;
		}
	}
}
