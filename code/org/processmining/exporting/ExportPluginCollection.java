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

package org.processmining.exporting;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.processmining.ProMSplash;
import org.processmining.framework.plugin.AnnotatedPlugin;
import org.processmining.framework.plugin.AnnotatedPluginAcceptingObject;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.PluginCollection;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class ExportPluginCollection extends PluginCollection {

	private static ExportPluginCollection instance = null;

	protected ExportPluginCollection() {
		super(Exporter.class);
	}

	/**
	 * Returns an instance of an <code>ExportPluginCollection</code>.
	 * 
	 * @return an instance of an <code>ExportPluginCollection</code>
	 */
	public static ExportPluginCollection getInstance() {
		if (instance == null) {
			instance = new ExportPluginCollection();
		}
		return instance;
	}

	/**
	 * Returns <code>true</code> if the given plugin is an
	 * <code>ExportPlugin</code>.
	 * 
	 * @param plugin
	 *            the plugin to check
	 * @return <code>true</code> if the given plugin is an
	 *         <code>ExportPlugin</code>, <code>false</code> otherwise.
	 */
	public boolean isValidPlugin(Plugin plugin) {
		return plugin instanceof ExportPlugin;
	}

	@Override
	protected AnnotatedPlugin createFromAnnotation(Class<?> pluginClass,
			Method pluginMethod, Annotation annotation, ProMSplash splash) {
		return new AnnotatedExportPlugin(pluginClass, pluginMethod, annotation);
	}
}

class AnnotatedExportPlugin extends AnnotatedPluginAcceptingObject implements
		ExportPlugin {

	private String extension;

	public AnnotatedExportPlugin(Class<?> pluginClass, Method pluginMethod,
			Annotation annotation) {
		super(pluginClass, pluginMethod, ((Exporter) annotation).name(),
				((Exporter) annotation).help(), ((Exporter) annotation)
						.sortName(), pluginMethod.getParameterTypes()[0]);
		this.extension = ((Exporter) annotation).extension();
	}

	public String getFileExtension() {
		return extension;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		try {
			getPluginMethod().invoke(getNewPluginInstance(),
					findObject(object), output);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException(t.getMessage());
		}
	}

	@Override
	public void validate() throws Exception {
		if (getPluginMethod().getParameterTypes().length != 2
				|| !getPluginMethod().getParameterTypes()[1]
						.equals(OutputStream.class)) {
			throw new Exception(
					"Export plugin methods need to have exactly 2 parameters: the object to export and an OutputStream");
		}
	}
}
