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

package org.processmining.framework.log.classic;

import java.util.HashSet;
import java.util.Set;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.ModelElement;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class ModelElementClassic implements ModelElement {

	private String name;
	private ModelElementInstancesClassic instances;

	public ModelElementClassic(String name) {
		this.name = name;
		instances = new ModelElementInstancesClassic();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ModelElement#getName()
	 */
	public String getName() {
		return name;
	}

	public ModelElementInstancesClassic getModelElementInstances() {
		return instances;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ModelElement#toString()
	 */
	public String toString() {
		return name + " (ModelElement)\n" + instances.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ModelElement#getInstances()
	 */
	public Set<AuditTrailEntry> getInstances() {
		HashSet<AuditTrailEntry> instanceSet = new HashSet<AuditTrailEntry>();
		instances.reset();
		while (instances.hasNext()) {
			instanceSet.addAll(instances.next().toArrayList());
		}
		return instanceSet;
	}
}
