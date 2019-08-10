package org.processmining.framework.util;

/**
 * This interface needs to implemented by any class that wants to be notified by
 * a GUI property when its state has changed and also wants to receive some
 * object that is involved in this state change. <br>
 * It forces the implementation of the {@link #stateHasChanged()} method in
 * order to react on the changed GUI property. <br>
 * Note that if a class acts as a notification target for multiple properties at
 * the same time, there might be unwanted side effects. In that case, it may be
 * advisable to compose the notification target class of smaller sub classes
 * that each act as a notification target for one of the properties.
 * 
 * @author arozinat
 */
public interface GuiNotificationTargetStateful {

	/**
	 * This method will be called as soon as the associated GUI property has
	 * changed. <br>
	 * Using this callback technique the reaction to changes of the GUI property
	 * becomes possible in a decoupled way. However, one can still react
	 * differently depending on the object that has been involved in this state
	 * change.
	 * 
	 * @param involvedObj
	 *            the object that is involved in the current state change
	 */
	public void stateHasChanged(Object involvedObj);
}
