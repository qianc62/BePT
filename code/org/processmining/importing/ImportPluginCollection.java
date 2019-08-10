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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;

import org.processmining.ProMSplash;
import org.processmining.framework.plugin.AnnotatedPlugin;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.PluginCollection;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.mining.MiningResult;
import org.processmining.mining.MiningResultWithLogConnectionImpl;

/**
 * Collection of import plugins.
 * 
 * @see ImportPlugin
 * @author Peter van den Brand
 * @version 1.0
 */

public class ImportPluginCollection extends PluginCollection {

	private static ImportPluginCollection instance = null;

	protected ImportPluginCollection() {
		super(Importer.class);
	}

	/**
	 * Returns an instance of an <code>ImportPluginCollection</code>.
	 * 
	 * @return an instance of an <code>ImportPluginCollection</code>
	 */
	public static ImportPluginCollection getInstance() {
		if (instance == null) {
			instance = new ImportPluginCollection();
		}
		return instance;
	}

	/**
	 * Returns <code>true</code> if the given plugin is an
	 * <code>ImportPlugin</code>.
	 * 
	 * @param plugin
	 *            the plugin to check
	 * @return <code>true</code> if the given plugin is an
	 *         <code>ImportPlugin</code>, <code>false</code> otherwise.
	 */
	public boolean isValidPlugin(Plugin plugin) {
		return plugin instanceof ImportPlugin;
	}

	@Override
	protected AnnotatedPlugin createFromAnnotation(Class<?> pluginClass,
			Method pluginMethod, Annotation annotation, ProMSplash splash) {
		if (((Importer) annotation).connectToLog()) {
			return new AnnotatedImportPluginWithLogConnection(pluginClass,
					pluginMethod, annotation);
		} else {
			return new AnnotatedImportPlugin(pluginClass, pluginMethod,
					annotation);
		}
	}
}

class AnnotatedImportPlugin extends AnnotatedPlugin implements ImportPlugin {

	private String extension;

	public AnnotatedImportPlugin(Class<?> pluginClass, Method pluginMethod,
			Annotation annotation) {
		super(pluginClass, pluginMethod, ((Importer) annotation).name(),
				((Importer) annotation).help(), ((Importer) annotation)
						.sortName());
		this.extension = ((Importer) annotation).extension();
	}

	@Override
	protected void validate() throws Exception {
		if (getPluginMethod().getParameterTypes().length != 1
				|| !getPluginMethod().getParameterTypes()[0]
						.equals(InputStream.class)) {
			throw new Exception(
					"Import plugin methods need to have exactly one parameter: the InputStream to import from");
		}
		if (!JComponent.class.isAssignableFrom(getPluginMethod()
				.getReturnType())
				&& !MiningResult.class.isAssignableFrom(getPluginMethod()
						.getReturnType())) {
			throw new Exception(
					"Import plugin methods need to return an object which is derived from JComponent or MiningResult");
		}
	}

	public FileFilter getFileFilter() {
		return new GenericFileFilter(extension);
	}

	public MiningResult importFile(InputStream input) throws IOException {
		try {
			Object result = getPluginMethod().invoke(getNewPluginInstance(),
					input);

			if (result == null) {
				return null;
			} else if (MiningResult.class.isAssignableFrom(result.getClass())) {
				return (MiningResult) result;
			} else {
				return new MiningResultWithLogConnectionImpl(null,
						(JComponent) result, (LogReaderConnection) result);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException(t.getMessage());
		}
	}
}

class AnnotatedImportPluginWithLogConnection extends AnnotatedImportPlugin
		implements LogReaderConnectionImportPlugin {

	private boolean useFuzzyMatching;

	public AnnotatedImportPluginWithLogConnection(Class<?> pluginClass,
			Method pluginMethod, Annotation annotation) {
		super(pluginClass, pluginMethod, annotation);
		this.useFuzzyMatching = ((Importer) annotation).useFuzzyMatching();
	}

	@Override
	protected void validate() throws Exception {
		super.validate();
		if (!LogReaderConnection.class.isAssignableFrom(getPluginMethod()
				.getReturnType())) {
			throw new Exception(
					"Import plugin methods using connectToLog need to return an object which implements the LogReaderConnection interface");
		}
	}

	public boolean shouldFindFuzzyMatch() {
		return useFuzzyMatching;
	}
}
