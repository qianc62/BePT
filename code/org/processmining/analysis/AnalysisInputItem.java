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

import org.processmining.framework.plugin.ProvidedObject;

/**
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public class AnalysisInputItem {

	private String caption;
	private int min;
	private int max;
	private ProvidedObject[] objects;

	public AnalysisInputItem(String caption) {
		this(caption, 1, 1);
	}

	public AnalysisInputItem(String caption, int min, int max) {
		this.caption = caption;
		this.min = min;
		this.max = max;
	}

	public boolean accepts(ProvidedObject object) {
		return false;
	}

	public int getMinimum() {
		return min;
	}

	public int getMaximum() {
		return max;
	}

	public String getCaption() {
		return caption;
	}

	public void setProvidedObjects(ProvidedObject[] objects) {
		this.objects = objects;
	}

	public ProvidedObject[] getProvidedObjects() {
		return objects;
	}
}
