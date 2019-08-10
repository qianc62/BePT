/**
 * Project: ProM
 * File: ModelElements.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Apr 27, 2006, 2:25:08 AM
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

package org.processmining.framework.log;

import java.util.Iterator;

/**
 * Provides abstract access to the set of model elements having been observed in
 * a log.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public interface ModelElements {

	/**
	 * Retrieves a model element from this set.
	 * 
	 * @param element
	 *            Name of the model element.
	 * @return The requested model element, if found; <code>null</code>
	 *         otherwise.
	 */
	public abstract ModelElement find(String element);

	/**
	 * Retrieves the first model element in this set.
	 * 
	 * @return The first model element in this set.
	 */
	public abstract ModelElement first();

	/**
	 * Retrieves the last model element in this set.
	 * 
	 * @return The last model element in this set.
	 */
	public abstract ModelElement last();

	/**
	 * Retrieves the number of model elements contained in this set.
	 * 
	 * @return The number of model elements contained in this set.
	 */
	public abstract int size();

	/**
	 * Retrieves a model element contained in this set at the specific index.
	 * 
	 * @param index
	 *            Index of the requested model element in this set.
	 * @return The requested model element.
	 */
	public abstract ModelElement get(int index);

	/**
	 * Retrieves an iterator over the <code>ModelElement</code> instances
	 * contained in this set.
	 * 
	 * @return An iterator over the model elements contained.
	 */
	public abstract Iterator iterator();

	/**
	 * Returns a string representation of this set.
	 * 
	 * @return A string representation.
	 */
	public abstract String toString();

	/*
	 * --------------------------------------------------------------------------
	 * ----- Deprecated methods - do not use anymore! (subject to removal)
	 */

	/**
	 * Iterator method. <b>deprecated!</b>
	 * 
	 * @deprecated For iterator access please use the <code>iterator()</code>
	 *             method of this class.
	 */
	public abstract boolean hasNext();

	/**
	 * Iterator method. <b>deprecated!</b>
	 * 
	 * @deprecated For iterator access please use the <code>iterator()</code>
	 *             method of this class.
	 */
	public abstract ModelElement next();

	/**
	 * Iterator method. <b>deprecated!</b>
	 * 
	 * @deprecated For iterator access please use the <code>iterator()</code>
	 *             method of this class.
	 */
	public abstract void reset();

}