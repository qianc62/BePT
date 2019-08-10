/**
 * Project: ProM HPLR
 * File: Process.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Apr 26, 2006, 7:18:52 PM
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
 * This abstract class provides high-level access and information for a process,
 * including access to the contained process instances.
 * 
 * @author Christian W. Guenther(christian at deckfour dot org)
 */
public abstract class Process extends LogEntity {

	/**
	 * Returns the number of instances for this process contained in the log.
	 * 
	 * @return
	 */
	public abstract int size();

	/**
	 * Retrieves an instance of this process by its relative index, i.e. order
	 * of appearance in the log.
	 * 
	 * @param index
	 *            Index of the requested process instance, must be within the
	 *            range <code>[0, size()]</code>.
	 * @return
	 */
	public abstract ProcessInstance getInstance(int index);

	/**
	 * Returns an array containing the names, or IDs, of all instances of this
	 * process.
	 * 
	 * @return
	 */
	public abstract String[] getInstanceNames();

	/**
	 * Retrieves an instance of this process identified by its name, or ID,
	 * string.
	 * 
	 * @param name
	 * @return
	 */
	public abstract ProcessInstance getInstance(String name);

	/**
	 * Returns an iterator over all instances of this process in the log.
	 * 
	 * @return
	 */
	public abstract Iterator iterator();

	/**
	 * Adds the given process instance to this process. The process defined in
	 * the given instance must correspond to this process. If an instance with
	 * the same name is already contained in this process, it will be replaced
	 * by the given one.
	 * 
	 * @param instance
	 */
	public abstract void addProcessInstance(ProcessInstance instance);

	/**
	 * Removes the process instance with the given name, or ID, from this
	 * process.
	 * 
	 * @param name
	 *            Name of the process instance to remove.
	 * @return The instance, if it was contained and removed; <code>null</code>
	 *         otherwise.
	 */
	public abstract ProcessInstance removeProcessInstance(String name);

	/**
	 * Removes the process instance with the given index, i.e. order in the log,
	 * from this process.
	 * 
	 * @param index
	 *            Index of the process instance to remove.
	 * @return The instance, if it was contained and removed; <code>null</code>
	 *         otherwise.
	 */
	public abstract ProcessInstance removeProcessInstance(int index);

	/**
	 * Removes all contained process instances from this process.
	 * 
	 * @return The number of removed process instances.
	 */
	public abstract int clear();

	/**
	 * Returns the set of model elements contained in all instances of this
	 * process.
	 * 
	 * @return
	 */
	public abstract ModelElements getModelElements();

}
