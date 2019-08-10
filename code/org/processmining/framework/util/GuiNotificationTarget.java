package org.processmining.framework.util;

/**
 * This interface needs to implemented by any class that wants to be notified by
 * a GUI property when its state has changed. <br>
 * It forces the implementation of the {@link #updateGUI()} method in order to
 * react on the changed GUI property. <br>
 * Note that if a class acts as a notification target for multiple properties at
 * the same time, there might be unwanted side effects. In that case, it may be
 * advisable to compose the notification target class of smaller sub classes
 * that each act as a notification target for one of the properties.
 * 
 * @see GUIPropertyBoolean
 * @see GUIPropertyDouble
 * @see GUIPropertyFloat
 * @see GUIPropertyInteger
 * @see GUIPropertyListEnumeration
 * @see GUIPropertyLong
 * @see GUIPropertySetEnumeration
 * @see GUIPropertyString
 * 
 * @author arozinat
 */
public interface GuiNotificationTarget {

	/**
	 * This method will be called as soon as the associated GUI property has
	 * changed. <br>
	 * Using this callback technique the reaction to changes of the GUI property
	 * becomes possible in a decoupled way.
	 */
	public void updateGUI();
}
