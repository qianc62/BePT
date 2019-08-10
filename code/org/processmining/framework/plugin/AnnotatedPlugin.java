package org.processmining.framework.plugin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.processmining.framework.util.PluginDocumentationLoader;

public abstract class AnnotatedPlugin implements Plugin, DoNotCreateNewInstance {

	private Class<?> pluginClass;
	private Method pluginMethod;
	private String name;
	private String help;
	private String sortName;

	public AnnotatedPlugin(Class<?> pluginClass, Method pluginMethod,
			String name, String help, String sortName) {
		this.pluginClass = pluginClass;
		this.pluginMethod = pluginMethod;
		this.name = name;
		this.help = help;
		this.sortName = sortName;
	}

	public String getHtmlDescription() {
		if (help == null || help.length() == 0) {
			return PluginDocumentationLoader.load(pluginClass.getName());
		}
		return help;
	}

	public String getName() {
		return name;
	}

	public String getSortName() {
		return sortName;
	}

	protected Object getNewPluginInstance() throws InstantiationException,
			IllegalAccessException {
		if (Modifier.isStatic(pluginMethod.getModifiers())) {
			return null;
		} else {
			return pluginClass.newInstance();
		}
	}

	protected Method getPluginMethod() {
		return pluginMethod;
	}

	// should throw an exception if the plugin method does not satisfy the
	// interface (which interface this is depends on the plugin type)
	protected abstract void validate() throws Exception;

}
