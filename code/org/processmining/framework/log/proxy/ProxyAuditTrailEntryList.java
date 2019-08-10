/**
 * Project: ProM
 * File: ProxyAuditTrailEntryList.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Sep 28, 2006, 5:19:31 PM
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
package org.processmining.framework.log.proxy;

import java.io.IOException;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.rfb.AuditTrailEntryListIterator;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ProxyAuditTrailEntryList implements AuditTrailEntryList {

	protected AuditTrailEntryList parent = null;

	public ProxyAuditTrailEntryList(AuditTrailEntryList aParent) {
		parent = aParent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#append(org.processmining
	 * .framework.log.AuditTrailEntry)
	 */
	public int append(AuditTrailEntry ate) throws IOException {
		return parent.append(ate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#cleanup()
	 */
	public void cleanup() throws IOException {
		parent.cleanup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#cloneInstance()
	 */
	public AuditTrailEntryList cloneInstance() throws IOException {
		return parent.cloneInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#consolidate()
	 */
	public boolean consolidate() throws IOException {
		return parent.consolidate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#get(int)
	 */
	public AuditTrailEntry get(int index) throws IndexOutOfBoundsException,
			IOException {
		return new ProxyAuditTrailEntry(parent, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#insert(org.processmining
	 * .framework.log.AuditTrailEntry, int)
	 */
	public void insert(AuditTrailEntry ate, int index)
			throws IndexOutOfBoundsException, IOException {
		// inserting is not allowed in proxy classes, only if the index
		// is equal to appending!
		if (index == parent.size()) {
			parent.append(ate);
		} else {
			throw new IOException("Illegal modification of proxy!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#insertOrdered(org
	 * .processmining.framework.log.AuditTrailEntry)
	 */
	public int insertOrdered(AuditTrailEntry ate) throws IOException {
		// modification of proxy not allowed (order distortion)!
		throw new IOException("Illegal modification of proxy!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#isTainted()
	 */
	public boolean isTainted() {
		return parent.isTainted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#iterator()
	 */
	public Iterator iterator() {
		return new AuditTrailEntryListIterator(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#remove(int)
	 */
	public AuditTrailEntry remove(int index) throws IndexOutOfBoundsException,
			IOException {
		// modification of proxy not allowed (order distortion)!
		throw new IOException("Illegal modification of proxy!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#replace(org.processmining
	 * .framework.log.AuditTrailEntry, int)
	 */
	public AuditTrailEntry replace(AuditTrailEntry ate, int index)
			throws IndexOutOfBoundsException, IOException {
		return parent.replace(ate, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#size()
	 */
	public int size() {
		return parent.size();
	}

}
