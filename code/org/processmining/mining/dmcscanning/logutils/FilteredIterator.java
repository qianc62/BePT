/*
 * Created on Jun 3, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
 * all rights reserved
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning.logutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.mining.dmcscanning.logutils.filter.EventFilter;

/**
 * FilteredIterator This class implements a read-only iterator over a
 * ProcessInstance, or rather the AuditTrailEntries contained within it. Notice
 * that this implies that you cannot alter the iterated set in any way, i.e. no
 * removal or insertion methods are implemented. Instances of this class can be
 * equipped with an EventFilter instance. This is used to filter the set of
 * audit trail entries from the wrapped ProcessInstance following its specified
 * constraints. Notice: Although a FilteredIterator can only be equipped with
 * one EventFilter instance at a time, you can use e.g. the BooleanFilter to
 * compose more sophisticated filter criteria (e.g. multiple filters of which
 * all (AND), one (OR), etc. have to match).
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class FilteredIterator implements Iterator {

	protected ProcessInstance instance = null;
	protected ArrayList entries = null;
	protected int position = 0;
	protected int cachedNextPosition = 0;
	protected EventFilter filter = null;

	/**
	 * constructor
	 */
	public FilteredIterator(ProcessInstance anInstance) {
		super();
		instance = anInstance;
		entries = instance.getAuditTrailEntries().toArrayList();
		position = 0;
		filter = null;
	}

	public FilteredIterator(ProcessInstance anInstance, EventFilter aFilter) {
		super();
		instance = anInstance;
		entries = instance.getAuditTrailEntries().toArrayList();
		position = 0;
		filter = aFilter;
	}

	/**
	 * copy constructor
	 * 
	 * @param toClone
	 */
	public FilteredIterator(FilteredIterator toClone) {
		super();
		instance = toClone.instance;
		entries = toClone.entries;
		position = toClone.position;
		filter = toClone.filter;
	}

	/**
	 * clone method
	 */
	public Object clone() {
		return new FilteredIterator(this);
	}

	/**
	 * Sets the event filter to be used in filtering the result set
	 * 
	 * @param aFilter
	 */
	public void setFilter(EventFilter aFilter) {
		filter = aFilter;
	}

	/**
	 * @return the currently set event filter
	 */
	public EventFilter getFilter() {
		return filter;
	}

	/**
	 * Sets the process instance to be iterated over
	 * 
	 * @param anInstance
	 */
	public void setProcessInstance(ProcessInstance anInstance) {
		instance = anInstance;
		entries = anInstance.getAuditTrailEntries().toArrayList();
		position = 0;
	}

	/**
	 * @return the process instance currently iterated over
	 */
	public ProcessInstance getProcessInstance() {
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if (cachedNextPosition >= 0) {
			return true; // previously scanned
		}
		for (int index = (position + 1); index < entries.size(); index++) {
			if (match(index)) {
				cachedNextPosition = index; // cache for speedup
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the next matched AuditTrailEntry instance (casted to Object for
	 *         compliance)
	 */
	public Object next() {
		if (cachedNextPosition < 0) {
			if (hasNext() == false) { // scan to next matching event
				return null; // invalid access
			}
		}
		position = cachedNextPosition;
		cachedNextPosition = -1; // invalidate cache
		return entries.get(position);
	}

	/**
	 * @return the next matched AuditTrailEntry instance
	 */
	public AuditTrailEntry nextAuditTrailEntry() {
		return (AuditTrailEntry) next();
	}

	/**
	 * This class implements a read-only iterator, i.e. neither insert nor
	 * remove operations are supported. Thus, this method (from Iterator) has
	 * been overwritten with an empty (NOP) implementation.
	 */
	public void remove() {
		// not implemented! (read-only iterator)
	}

	/**
	 * @return bookmark containing the current iterator state (i.e. position)
	 *         for later reset
	 */
	public Bookmark bookmark() {
		return new Bookmark(position);
	}

	/**
	 * Resets the iterator's state (i.e. position) to a previously created
	 * bookmark
	 * 
	 * @param bookmark
	 * @return
	 */
	public boolean reset(Bookmark bookmark) {
		if (match(bookmark.getPosition())) {
			position = bookmark.getPosition();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Resets the iterator to its initial state, i.e. to the start position
	 */
	public void reset() {
		position = 0;
	}

	/**
	 * Convenience method.
	 * 
	 * @return the subset of the wrapped audit trail entries that match the
	 *         filter criteria, in their original order
	 */
	public Collection filteredList() {
		ArrayList resultSet = new ArrayList();
		for (int i = 0; i < entries.size(); i++) {
			if (match(i)) {
				resultSet.add(entries.get(i));
			}
		}
		return resultSet;
	}

	/**
	 * Tests, whether all active EventFilters match the given audit trail entry
	 * 
	 * @param index
	 * @return
	 */
	protected boolean match(int index) {
		if (filter == null) {
			return true; // not filtered
		} else {
			return filter.matches((AuditTrailEntry) entries.get(index));
		}
	}

}
