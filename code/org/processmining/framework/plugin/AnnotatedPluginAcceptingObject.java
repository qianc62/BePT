package org.processmining.framework.plugin;

import java.lang.reflect.Method;

public abstract class AnnotatedPluginAcceptingObject extends AnnotatedPlugin {

	private Class<? extends Object> type;

	public AnnotatedPluginAcceptingObject(Class<?> pluginClass,
			Method pluginMethod, String name, String help, String sortName,
			Class<? extends Object> type) {
		super(pluginClass, pluginMethod, name, help, sortName);
		this.type = type;
	}

	public boolean accepts(ProvidedObject object) {
		return findObject(object) != null;
	}

	protected Object findObject(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] != null && type.isAssignableFrom(o[i].getClass())) {
				return o[i];
			}
		}
		return null;
	}
}
