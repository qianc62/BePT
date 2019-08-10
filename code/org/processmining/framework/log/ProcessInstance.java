/**
 * Project: ProM
 * File: ProcessInstance.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 22, 2006, 12:29:14 AM
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
package org.processmining.framework.log;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This abstract class provides acces to a process instance as read from a log
 * file
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public abstract class ProcessInstance extends LogEntity {

	/**
	 * This constant can be used to search the data container of the process
	 * instance to see whether or not it describes a partial order. If it does
	 * descibe a partial order, this attribute should be mapped to "true" and
	 * each audit trail entry should have an id according to
	 * <code>ATT_ATE_ID</code>. It is a preliminary solution for storing process
	 * instances as partial orders.
	 */
	public final static String ATT_PI_PO = "isPartialOrder";

	/**
	 * This constant can be used to search the data container of each
	 * audit-trail entry for the id of that entry. The result is a string. It is
	 * a preliminary solution for storing process instances as partial orders.
	 */
	public final static String ATT_ATE_ID = "ATE_id";

	/**
	 * This constant can be used to search the data container of each
	 * audit-trail entry for a comma separated list of successor-IDs of the ate.
	 * It is a preliminary solution for storing process instances as partial
	 * orders.
	 */
	public final static String ATT_ATE_POST = "ATE_post";

	/**
	 * This constant can be used to search the data container of each
	 * audit-trail entry for a comma separated list of predecessor-IDs of the
	 * ate. It is a preliminary solution for storing process instances as
	 * partial orders.
	 */
	public final static String ATT_ATE_PRE = "ATE_pre";

	/**
	 * Returns the identifier / name of the process of this object is an
	 * instance.
	 * 
	 * @return
	 */
	public abstract String getProcess();

	/**
	 * Returns true, if this process instance contains no audit trail entries to
	 * read.
	 * 
	 * @return
	 */
	public abstract boolean isEmpty();

	/**
	 * Returns the sequential set of audit trail entries, as found in this
	 * process instance.
	 * 
	 * @return
	 */
	public abstract AuditTrailEntryList getAuditTrailEntryList();

	/**
	 * Returns the sequential list of audit trail entries, as found in this
	 * process instance.
	 * 
	 * @return
	 */
	public List<AuditTrailEntry> getListOfATEs() {
		List<AuditTrailEntry> list = new ArrayList<AuditTrailEntry>();
		Iterator it2 = getAuditTrailEntryList().iterator();
		while (it2.hasNext()) {
			list.add((AuditTrailEntry) it2.next());
		}
		return list;
	}

	/**
	 * Returns the set of model elements contained in this process instance.
	 * 
	 * @return
	 */
	public abstract ModelElements getModelElements();

	/**
	 * Returns a deep copy of this process instance. Modifications are defined
	 * to be not synchronized between original and returned clone.
	 * 
	 * @return A deep copy of this process instance.
	 */
	public abstract ProcessInstance cloneInstance();

	/*
	 * --------------------------------------------------------------------------
	 * ----- Deprecated methods - do not use anymore! (subject to removal)
	 */

	/**
	 * <b>This method is deprecated!</b> Use <code>getAttributes()</code>
	 * instead!
	 * 
	 * @deprecated Use <code>getAttributes()</code> instead!
	 * @return
	 */
	public Map getData() {
		return getAttributes();
	}

	/**
	 * Returns the sequential set of audit trail entries, as found in this
	 * process instance.
	 * 
	 * @return
	 * @deprecated Use the <code>AuditTrailEntryList</code> interface for
	 *             accessing audit trail entries in a sequential manner instead.
	 *             This interface also provides support for persistently
	 *             modifying sets of audit trail entries.
	 */
	public abstract AuditTrailEntries getAuditTrailEntries();

}
