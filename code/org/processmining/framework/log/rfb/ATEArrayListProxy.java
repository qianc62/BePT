/**
 * Project: ProM
 * File: ATEArrayListProxy.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 24, 2006, 3:18:59 AM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
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
 ***********************************************************
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;

/**
 * Provides the <code>java.util.ArrayList</code> interface to a wrapped
 * AuditTrailEntryList.
 * <p>
 * Used to retain backward compatibility to previous classic log reading and
 * access implementation.
 * <p>
 * This class implements a lightweight proxy redirecting all access
 * transparently and immediately to the wrapped audit trail entry list instance,
 * with all performance implications of this.
 * <p>
 * It should not be used deliberately, its purpose is to retain the
 * functionality of e.g. LogFilter implementations which use the direct access
 * to a ProcessInstance's audit trail entries via the <code>toArrayList()</code>
 * method of AuditTrailEntries for modification.
 * <p>
 * Owners of code using the <code>toArrayList()</code> method for write access
 * should adjust their code accordingly. Implementations in this proxy class may
 * miss, fail, or be implemented in a bad-performing or erroneous manner. Check
 * your results!
 * 
 * @see org.processmining.framework.log.AuditTrailEntryList
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class ATEArrayListProxy extends ArrayList {

	/**
	 * The wrapped instance of <code>AuditTrailEntryList</code> to which all
	 * access is transparently delegated.
	 */
	protected AuditTrailEntryList list = null;

	/**
	 * serial version id for serialization TODO: bogus implementation to
	 * suppress warnings; implement properly if used.
	 */
	private static final long serialVersionUID = -6779915681044279917L;

	/**
	 * Wraps the provided list of audit trail entries into an interface derived
	 * from java.util.ArrayList
	 * 
	 * @param aList
	 */
	public ATEArrayListProxy(AuditTrailEntryList aList) {
		list = aList;
	}

	/**
	 * <b>Not in use:</b> not applicable in this context!
	 * <p>
	 * Overwritten and declared <code>protected</code> to prevent from being
	 * used.
	 * 
	 * @param initialCapacity
	 */
	protected ATEArrayListProxy(int initialCapacity) {
		// Not in use; not applicable in this context!
	}

	/**
	 * <b>Not in use:</b> not applicable in this context!
	 * <p>
	 * Overwritten and declared <code>protected</code> to prevent from being
	 * used.
	 */
	protected ATEArrayListProxy() {
		// Not in use; not applicable in this context!
	}

	/**
	 * <b>Not in use:</b> not applicable in this context!
	 * <p>
	 * Overwritten and declared <code>protected</code> to prevent from being
	 * used.
	 * 
	 * @param arg0
	 */
	protected ATEArrayListProxy(Collection arg0) {
		// Not in use; not applicable in this context!
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#add(int, java.lang.Object)
	 */
	public void add(int arg0, Object arg1) {
		try {
			list.insert((AuditTrailEntry) arg1, arg0);
		} catch (Exception e) {
			// Exceptions are printed to STDOUT;
			// This is only a legacy bridge implementation.
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean add(Object arg0) {
		try {
			list.append((AuditTrailEntry) arg0);
			return true;
		} catch (Exception e) {
			// Exceptions are printed to STDOUT;
			// This is only a legacy bridge implementation.
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection arg0) {
		for (Iterator it = arg0.iterator(); it.hasNext();) {
			try {
				list.append((AuditTrailEntry) it.next());
			} catch (IOException e) {
				// Exceptions are printed to STDOUT;
				// This is only a legacy bridge implementation.
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int arg0, Collection arg1) {
		int index = 0;
		AuditTrailEntry ate = null;
		for (Iterator it = arg1.iterator(); it.hasNext();) {
			ate = (AuditTrailEntry) it.next();
			if (index >= arg0) {
				add(ate);
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#clear()
	 */
	public void clear() {
		for (int i = list.size() - 1; i >= 0; i--) {
			try {
				list.remove(i);
			} catch (IOException e) {
				// Exceptions are printed to STDOUT;
				// This is only a legacy bridge implementation.
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#clone()
	 */
	public Object clone() {
		return new ATEArrayListProxy(list);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#contains(java.lang.Object)
	 */
	public boolean contains(Object elem) {
		if (elem instanceof AuditTrailEntry) {
			return false;
		}
		AuditTrailEntry ate = null;
		for (Iterator it = list.iterator(); it.hasNext();) {
			ate = (AuditTrailEntry) it.next();
			if (ate.equals(elem)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#ensureCapacity(int)
	 */
	public void ensureCapacity(int minCapacity) {
		// no implementation needed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#get(int)
	 */
	public Object get(int index) {
		Object result = null;
		try {
			result = list.get(index);
		} catch (IOException e) {
			// Exceptions are printed to STDOUT;
			// This is only a legacy bridge implementation.
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#indexOf(java.lang.Object)
	 */
	public int indexOf(Object elem) {
		AuditTrailEntry ate = null;
		for (int i = 0; i < list.size(); i++) {
			try {
				ate = (AuditTrailEntry) list.get(i);
			} catch (IOException e) {
				// Exceptions are printed to STDOUT;
				// This is only a legacy bridge implementation.
				e.printStackTrace();
			}
			if (ate.equals(elem)) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#isEmpty()
	 */
	public boolean isEmpty() {
		return (list.size() == 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object elem) {
		return indexOf(elem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#remove(int)
	 */
	public Object remove(int index) {
		Object result = null;
		try {
			result = list.remove(index);
		} catch (IOException e) {
			// Exceptions are printed to STDOUT;
			// This is only a legacy bridge implementation.
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		int index = indexOf(o);
		try {
			list.remove(index);
		} catch (IOException e) {
			// Exceptions are printed to STDOUT;
			// This is only a legacy bridge implementation.
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#removeRange(int, int)
	 */
	protected void removeRange(int fromIndex, int toIndex) {
		for (int i = toIndex; i >= fromIndex; i--) {
			try {
				list.remove(i);
			} catch (IOException e) {
				// Exceptions are printed to STDOUT;
				// This is only a legacy bridge implementation.
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#set(int, java.lang.Object)
	 */
	public Object set(int arg0, Object arg1) {
		Object result = null;
		try {
			result = list.replace((AuditTrailEntry) arg1, arg0);
		} catch (IOException e) {
			// Exceptions are printed to STDOUT;
			// This is only a legacy bridge implementation.
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#size()
	 */
	public int size() {
		return list.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#toArray()
	 */
	public Object[] toArray() {
		AuditTrailEntry result[] = new AuditTrailEntry[list.size()];
		for (int i = 0; i < list.size(); i++) {
			try {
				result[i] = list.get(i);
			} catch (IOException e) {
				// Exceptions are printed to STDOUT;
				// This is only a legacy bridge implementation.
				e.printStackTrace();
				return null;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#toArray(java.lang.Object[])
	 */
	// @SuppressWarnings("unchecked")
	public Object[] toArray(Object[] arg0) {
		// TODO: not implemented
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#trimToSize()
	 */
	public void trimToSize() {
		// not implemented
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return (o == this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#hashCode()
	 */
	public int hashCode() {
		return 1; // not implemented
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#iterator()
	 */
	public Iterator iterator() {
		return list.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#listIterator()
	 */
	public ListIterator listIterator() {
		// TODO not implemented
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#listIterator(int)
	 */
	public ListIterator listIterator(int index) {
		// TODO not implemented
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#subList(int, int)
	 */
	public List subList(int fromIndex, int toIndex) {
		// TODO not implemented
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection arg0) {
		for (Iterator it = arg0.iterator(); it.hasNext();) {
			if (contains(it.next()) == false) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection arg0) {
		for (Iterator it = arg0.iterator(); it.hasNext();) {
			remove(it.next());
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection arg0) {
		// TODO not implemented
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		// TODO not implementedd
		return "toString() not implemented for ATEArrayListProxy";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		// not implemented
	}

}
