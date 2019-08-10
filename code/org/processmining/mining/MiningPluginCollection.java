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

package org.processmining.mining;

import java.awt.BorderLayout;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.ProMSplash;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.plugin.AnnotatedPlugin;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.PluginCollection;
import org.processmining.framework.ui.Message;

/**
 * Collection of mining algorithms.
 * 
 * @see MiningPlugin
 * @author Peter van den Brand
 * @version 1.0
 */

public class MiningPluginCollection extends PluginCollection {

	private static MiningPluginCollection instance = null;

	protected MiningPluginCollection() {
		super(Miner.class);
	}

	/**
	 * Returns an instance of an <code>MiningPluginCollection</code>.
	 * 
	 * @return an instance of an <code>MiningPluginCollection</code>
	 */
	public static MiningPluginCollection getInstance() {
		if (instance == null) {
			instance = new MiningPluginCollection();
		}
		return instance;
	}

	/**
	 * Returns <code>true</code> if the given plugin is an
	 * <code>MiningPlugin</code>.
	 * 
	 * @param plugin
	 *            the plugin to check
	 * @return <code>true</code> if the given plugin is an
	 *         <code>MiningPlugin</code>, <code>false</code> otherwise.
	 */
	public boolean isValidPlugin(Plugin plugin) {
		return plugin instanceof MiningPlugin;
	}

	@Override
	protected AnnotatedPlugin createFromAnnotation(Class<?> pluginClass,
			Method pluginMethod, Annotation annotation, ProMSplash splash) {
		return new AnnotatedMiningPlugin(pluginClass, pluginMethod, annotation);
	}
}

class AnnotatedMiningPlugin extends AnnotatedPlugin implements MiningPlugin,
		NewStyleMiningPlugin {

	private final static String GET_SETTINGS_METHOD = "getSettings";

	private Class<?> settingsGuiClass;
	private Class<?> settingsClass;

	public AnnotatedMiningPlugin(Class<?> pluginClass, Method pluginMethod,
			Annotation annotation) {
		super(pluginClass, pluginMethod, ((Miner) annotation).name(),
				((Miner) annotation).help(), ((Miner) annotation).sortName());
		settingsGuiClass = ((Miner) annotation).settings();
		settingsClass = usesSettingsParameter() ? pluginMethod
				.getParameterTypes()[1] : null;
	}

	public void validate() throws Exception {
		if (getPluginMethod().getParameterTypes().length < 1
				|| getPluginMethod().getParameterTypes().length > 2
				|| !getPluginMethod().getParameterTypes()[0]
						.equals(LogReader.class)) {
			throw new Exception(
					"Mining plugin methods need to have either 1 or 2 parameters: a LogReader and an optional settings object");
		}
		if (!JComponent.class.isAssignableFrom(getPluginMethod()
				.getReturnType())) {
			throw new Exception(
					"Mining plugin methods need to return an object which is derived from JComponent");
		}
		if (usesSettingsParameter()) {
			if (!Modifier.isPublic(settingsClass.getModifiers())) {
				throw new Exception("Settings class ("
						+ settingsClass.getName() + ") must be public");
			}

			try {
				settingsClass.getConstructor(LogSummary.class);
			} catch (Throwable e) {
				try {
					settingsClass.newInstance();
				} catch (Throwable e2) {
					throw new Exception(
							"The settings class passed to the mining function ("
									+ settingsClass.getName()
									+ ") "
									+ "must have a constructor without parameters or a constructor with a LogSummary parameter, "
									+ "to set the default values of all parameters.");
				}
			}

			if (usesSettingsGui()) {
				if (!Modifier.isPublic(settingsGuiClass.getModifiers())) {
					throw new Exception("Settings GUI class ("
							+ settingsGuiClass.getName() + ") must be public");
				}
				try {
					settingsGuiClass.getConstructor(settingsClass,
							LogSummary.class);
				} catch (Throwable e) {
					throw new Exception("The settings GUI class ("
							+ settingsGuiClass.getName()
							+ ") needs to have a constructor which takes a "
							+ settingsClass.getName()
							+ " object and a LogSummary");
				}
				try {
					settingsGuiClass.getMethod(GET_SETTINGS_METHOD);
				} catch (Throwable e) {
					throw new Exception("The settings GUI class ("
							+ settingsGuiClass.getName() + ") needs to have a "
							+ GET_SETTINGS_METHOD + "() method");
				}
				if (!settingsClass.isAssignableFrom(settingsGuiClass.getMethod(
						GET_SETTINGS_METHOD).getReturnType())) {
					throw new Exception(
							"The "
									+ GET_SETTINGS_METHOD
									+ "() method of the settings GUI class must return a "
									+ settingsClass.getName()
									+ " (or derived class)");
				}
				if (!JComponent.class.isAssignableFrom(settingsGuiClass)) {
					throw new Exception("Settings GUI class ("
							+ settingsGuiClass.getName()
							+ ") must be derived from JComponent");
				}
			}
		}
	}

	public JPanel getOptionsPanel(LogSummary summary) {
		JPanel guiPanel = null;

		if (usesSettingsParameter() && usesSettingsGui()) {
			try {
				JComponent settingsGui = (JComponent) settingsGuiClass
						.getConstructor(settingsClass, LogSummary.class)
						.newInstance(createDefaultSettings(summary), summary);
				guiPanel = new JPanel(new BorderLayout());
				guiPanel.add(settingsGui, BorderLayout.CENTER);
			} catch (Throwable e) {
				e.printStackTrace();
				Message.add("Could not instantiate plugin: " + e.getMessage());
			}
		}
		return guiPanel;
	}

	private Object createDefaultSettings(LogSummary summary)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		try {
			Constructor ctorWithSummary = settingsClass
					.getConstructor(LogSummary.class);
			return ctorWithSummary.newInstance(summary);
		} catch (NoSuchMethodException e) {
			return settingsClass.newInstance();
		}
	}

	private boolean usesSettingsParameter() {
		return getPluginMethod().getParameterTypes().length > 1;
	}

	private boolean usesSettingsGui() {
		return settingsGuiClass != null
				&& !settingsGuiClass.equals(NoMiningSettings.class);
	}

	public MiningResult mine(LogReader log) {
		if (usesSettingsParameter() && usesSettingsGui()) {
			Message
					.add(
							"Cannot execute a new style mining plugin which requires a settings GUI using the old mine(LogReader) method,"
									+ "please use mine(LogReader, JComponent)",
							Message.ERROR);
			return null;
		}
		return mine(log, null);
	}

	public MiningResult mine(LogReader log, JPanel optionsPanel) {
		try {
			JComponent result;

			if (usesSettingsParameter()) {
				Object settingsToUse = usesSettingsGui()
						&& optionsPanel != null
						&& optionsPanel.getComponentCount() == 1 ? optionsPanel
						.getComponent(0).getClass().getMethod(
								GET_SETTINGS_METHOD).invoke(
								optionsPanel.getComponent(0))
						: createDefaultSettings(log.getLogSummary());
				result = (JComponent) getPluginMethod().invoke(
						getNewPluginInstance(), log, settingsToUse);
			} else {
				result = (JComponent) getPluginMethod().invoke(
						getNewPluginInstance(), log);
			}
			return result == null ? null : new MiningResultImpl(log, result);

		} catch (Throwable e) {
			e.printStackTrace();
			Message.add("Error executing plugin: " + e.getMessage(),
					Message.ERROR);
		}
		return null;
	}
}
