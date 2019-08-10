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

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.log.ModelElement;
import org.processmining.framework.log.ModelElements;
import org.processmining.framework.log.ProcessInstance;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class ModelElementsClassic implements ModelElements, Cloneable {

	private ArrayList elements;
	private Iterator iterator;
	private int currentPosition = 0; // position stored in order to provide a

	// clone() method

	public ModelElementsClassic(ProcessInstance pi) {
		AuditTrailEntries ates = pi.getAuditTrailEntries();
		LogStateMachine sm = LogStateMachine.getInstance();

		elements = new ArrayList();
		while (ates.hasNext()) {
			AuditTrailEntry ate = ates.next();
			ModelElement elt = find(ate.getElement());
			ModelElementInstancesClassic instances;
			AuditTrailEntries instance;
			AuditTrailEntry lastEntry;

			if (elt == null) {
				elt = new ModelElementClassic(ate.getElement());
				elements.add(elt);
			}

			instances = ((ModelElementClassic) elt).getModelElementInstances();
			instance = instances.last();
			lastEntry = (instance == null ? null : instance.last());

			if (lastEntry == null
					|| !sm.mayEventuallyOccurAfter(lastEntry.getType(), ate
							.getType())) {
				// a new instance is started
				instance = new AuditTrailEntriesClassic();
				instances.add(instance);
			}
			instance.add(ate);
		}
		reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.classic.ModelElements#find(java.lang.
	 * String)
	 */
	public ModelElement find(String element) {
		Iterator i = elements.iterator();

		while (i.hasNext()) {
			ModelElement me = (ModelElement) i.next();

			if (element.equals(me.getName())) {
				return me;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ModelElements#hasNext()
	 */
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ModelElements#next()
	 */
	public ModelElement next() {
		// keep position for clone method
		currentPosition++;
		return (ModelElement) iterator.next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ModelElements#reset()
	 */
	public void reset() {
		iterator = elements.iterator();
		// keep position for clone method
		currentPosition = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ModelElements#first()
	 */
	public ModelElement first() {
		return elements.size() > 0 ? (ModelElement) elements.get(0) : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ModelElements#last()
	 */
	public ModelElement last() {
		return elements.size() > 0 ? (ModelElement) elements.get(elements
				.size() - 1) : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ModelElements#size()
	 */
	public int size() {
		return elements.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ModelElements#get(int)
	 */
	public ModelElement get(int index) {
		return (ModelElement) elements.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ModelElements#toString()
	 */
	public String toString() {
		String s = "ModelElements:\n";

		for (int i = 0; i < size(); i++) {
			s += "  " + get(i).toString() + "\n";
		}
		return s;
	}

	public Iterator iterator() {
		return elements.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		ModelElementsClassic o = null;
		try {
			o = (ModelElementsClassic) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// clone non-primitive types to obtain deep copy
		if (elements != null) {
			o.elements = (ArrayList) elements.clone();
			Iterator it = elements.iterator();
			while (it.hasNext()) {
				ModelElement el = (ModelElement) it.next();
				o.elements.add(el);
			}
		}
		// make the iterator belong to the cloned list
		o.iterator = o.elements.iterator();
		// re-establish position of the iterator
		while (o.currentPosition < currentPosition) {
			o.next();
		}
		return o;
	}
}
