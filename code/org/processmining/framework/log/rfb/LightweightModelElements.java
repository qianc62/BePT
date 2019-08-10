/**
 * Project: ProM HPLR
 * File: LightweightModelElements.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Apr 28, 2006, 12:01:47 AM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the 
 *      names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written 
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.log.rfb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.ModelElement;
import org.processmining.framework.log.ModelElements;
import org.processmining.framework.log.ProcessInstance;

/**
 * This class implements a set of model elements, as found in a log file or
 * process instance, as a dynamic structure of lightweight proxies for elements
 * and iterators.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class LightweightModelElements implements ModelElements {

	/**
	 * Contains the actual model elements as strings.
	 */
	protected ArrayList<String> elements = null;
	/**
	 * The process instance(s) this set of model elements refers to
	 */
	protected HashSet<ProcessInstance> processInstances = null;
	/**
	 * Legacy iterator implementation; holds built-in iterator's position.
	 * 
	 * @deprecated subject to removal (iterator interface deprecated)
	 */
	protected int iteratorPosition = 0;

	/**
	 * Creates a new set of model elements.
	 * 
	 * @param elementNames
	 *            List of model elements as strings.
	 * @param instances
	 *            The set of process instances this set refers to
	 */
	public LightweightModelElements(ArrayList<String> elementNames,
			HashSet<ProcessInstance> instances) {
		elements = new ArrayList<String>(elementNames);
		processInstances = instances;
	}

	/**
	 * Creates a new empty set of model elements.
	 * 
	 * @param instances
	 *            The set of process instances this set refers to
	 */
	public LightweightModelElements(HashSet<ProcessInstance> instances) {
		elements = new ArrayList<String>();
		processInstances = instances;
	}

	/**
	 * Merges this set of model elements with another one, thus rendering the
	 * instance on which this method is invoked the union of its prior set and
	 * the set of elements to be merged with.
	 * 
	 * @param mergeElements
	 *            Set of elements to be merged with.
	 */
	public void merge(LightweightModelElements mergeElements) {
		processInstances.addAll(mergeElements.processInstances);
		ArrayList elementStrings = mergeElements.getModelElementStrings();
		for (Iterator it = elementStrings.iterator(); it.hasNext();) {
			elements.add((String) it.next());
		}
	}

	/**
	 * Retrieves the set of model elements as a list of strings.
	 * 
	 * @return
	 */
	public ArrayList<String> getModelElementStrings() {
		return elements;
	}

	/**
	 * Adds a model element with the given name to the set.
	 * 
	 * @param element
	 *            Name of the model element to be added.
	 */
	public void addElement(String element) {
		if (elements.contains(element) == false) {
			elements.add(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ModelElements#find(java.lang.String)
	 */
	public ModelElement find(String element) {
		// checks if requested model element is contained,
		// if so return a lightweight proxy to it.
		int findIndex = elements.indexOf(element);
		if (findIndex >= 0) {
			return new LightweightModelElement(this, findIndex);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ModelElements#first()
	 */
	public ModelElement first() {
		if (elements.isEmpty() == false) {
			return new LightweightModelElement(this, 0);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ModelElements#last()
	 */
	public ModelElement last() {
		if (elements.isEmpty() == false) {
			return new LightweightModelElement(this, elements.size() - 1);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ModelElements#get(int)
	 */
	public ModelElement get(int index) {
		if (elements.isEmpty() == false) {
			return new LightweightModelElement(this, index);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ModelElements#iterator()
	 */
	public Iterator iterator() {
		return new LightweightModelElementIterator(this, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ModelElements#hasNext()
	 */
	public boolean hasNext() {
		return (iteratorPosition < elements.size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ModelElements#next()
	 */
	public ModelElement next() {
		ModelElement result = new LightweightModelElement(this,
				iteratorPosition);
		iteratorPosition++;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ModelElements#reset()
	 */
	public void reset() {
		iteratorPosition = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ModelElements#size()
	 */
	public int size() {
		return elements.size();
	}

	/**
	 * Retrieves the set of audit trail entries which are instances of the
	 * indexed model element contained in this set.
	 * 
	 * @param index
	 * @return
	 */
	protected Set<AuditTrailEntry> getInstancesForModelElement(int index) {
		String modelElementName = elements.get(index);
		HashSet<AuditTrailEntry> resultSet = new HashSet<AuditTrailEntry>();
		AuditTrailEntryList ateList = null;
		AuditTrailEntry ate = null;
		for (ProcessInstance pi : processInstances) {
			ateList = pi.getAuditTrailEntryList();
			for (int i = 0; i < ateList.size(); i++) {
				try {
					ate = ateList.get(i);
					if (ate.getElement().equals(modelElementName)) {
						resultSet.add(ate);
					}
				} catch (IOException e) {
					System.err
							.println("Failed to compile instances for model element "
									+ index);
					e.printStackTrace();
					return null;
				}
			}
		}
		return resultSet;
	}

	/**
	 * Lightweight proxy class implementing a model element in the lighweight
	 * set. The trick is to avoid copying strings; instead only the index of the
	 * model element in the parent's set is stored. This dramatically decreases
	 * this class's footprint in operation and speeds up operation, by limiting
	 * the instantiation of new strings.
	 * 
	 * @author Christian W. Guenther (christian at deckfour dot org)
	 */
	protected class LightweightModelElement implements ModelElement {

		/**
		 * Index of the proxied model element's name in parent's list.
		 */
		protected int index = 0;
		/**
		 * Reference to the fat parent.
		 */
		protected LightweightModelElements parent = null;

		/**
		 * Creates a new model element proxy
		 * 
		 * @param parent
		 *            fat parent on which to delegate transparently
		 * @param index
		 *            index of the proxied model element in parent.
		 */
		protected LightweightModelElement(LightweightModelElements parent,
				int index) {
			this.index = index;
			this.parent = parent;
		}

		/**
		 * Retrieves the name of the proxied model element.
		 */
		public String getName() {
			return (String) parent.elements.get(index);
		}

		/**
		 * Retrieves the set of audit trail entries which are instances of this
		 * model element
		 */
		public Set<AuditTrailEntry> getInstances() {
			return parent.getInstancesForModelElement(index);
		}

	}

	/**
	 * Typical lightweight iterator over a set of model elements. This iterator
	 * stores only a reference to its parent and his internal state as index in
	 * the parent's list of model elements.
	 * <p>
	 * Notice that the iterator will transparently instantiate the returned
	 * model elements as lightweight proxies on the parent.
	 * 
	 * @author Christian W. Guenther (christian at deckfour dot org)
	 */
	protected class LightweightModelElementIterator implements Iterator {

		/**
		 * Current position of the iterator on the parent's set of model
		 * elements.
		 */
		protected int position = 0;
		/**
		 * Reference to fat parent.
		 */
		protected LightweightModelElements parent = null;

		/**
		 * Creates a new lightweight iterator on a set of model elements with
		 * its initial position in that set.
		 * 
		 * @param parent
		 *            reference to fat parent
		 * @param position
		 *            initial position in the parent set
		 */
		protected LightweightModelElementIterator(
				LightweightModelElements parent, int position) {
			this.parent = parent;
			this.position = position;
		}

		/**
		 * Probes whether further elements are available
		 */
		public boolean hasNext() {
			return (position < parent.elements.size());
		}

		/**
		 * Retrieves the next model element from the parent set.
		 */
		public Object next() {
			Object result = new LightweightModelElement(parent, position);
			position++;
			return result;
		}

		/**
		 * Not implemented for this iterator!
		 */
		public void remove() {
			// Not implemented
		}

	}

}
