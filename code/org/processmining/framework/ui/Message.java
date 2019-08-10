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

package org.processmining.framework.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.EventListenerList;

//import cn.edu.thss.iise.beehivez.util.ResourcesManager;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class Message {

	public final static int NORMAL = 0, WARNING = 1, ERROR = 2, DEBUG = 3,
			TEST = 4;
	public final static int NUM_TYPES = TEST + 1;
	public final static int CLEAR_MESSAGES = -1;
	//static ResourcesManager resourcesManager = new ResourcesManager();
	
	//public final static String TYPE_NAMES[] = { resourcesManager.getString("status.Normal"), resourcesManager.getString("status.Warning"), resourcesManager.getString("status.Error"),
		//resourcesManager.getString("status.Debug"), resourcesManager.getString("status.Test") };

	private static EventListenerList listeners = new EventListenerList();
	public static String[] TYPE_NAMES;

	private Message() {
	}

	public static synchronized void addActionListener(ActionListener l) {
		synchronized (listeners) {
			listeners.add(ActionListener.class, l);
		}
	}

	public static synchronized void removeActionListener(ActionListener l) {
		synchronized (listeners) {
			listeners.remove(ActionListener.class, l);
		}
	}

	public static void clearMessages() {
		synchronized (listeners) {
			ActionListener[] list = (ActionListener[]) listeners
					.getListeners(ActionListener.class);
			ActionEvent action = new ActionEvent("", 0, "", 0, CLEAR_MESSAGES);

			for (int i = 0; i < list.length; i++) {
				list[i].actionPerformed(action);
			}
		}
	}

	public static void add(String message) {
		add(message, NORMAL);
	}

	public static void add(String message, int status) {
		synchronized (listeners) {
			ActionListener[] list = (ActionListener[]) listeners
					.getListeners(ActionListener.class);
			ActionEvent action = new ActionEvent("", 0, message, 0, status);

			for (int i = 0; i < list.length; i++) {
				list[i].actionPerformed(action);
			}
		}
	}
}
