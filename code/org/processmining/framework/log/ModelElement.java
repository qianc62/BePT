/**
 * Project: ProM
 * File: ModelElement.java
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

import java.util.Set;

/**
 * Wraps a model element as having been observed in the log.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public interface ModelElement {

	/**
	 * Retrieves the name of this model element.
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Returns a string representation of this model element.
	 * 
	 * @return
	 */
	public abstract String toString();

	/**
	 * Returns the set of audit trail entries which are instances of this model
	 * element
	 * 
	 * @return
	 */
	public abstract Set<AuditTrailEntry> getInstances();

}