/**
 * Project: ProM
 * File: ProxyLogReader.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Sep 28, 2006, 4:58:57 PM
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

import java.util.Iterator;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ProxyLogReader extends LogReader {

	protected LogReader parent = null;

	public ProxyLogReader(LogReader reader) {
		parent = reader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#clone(int[])
	 */
	public LogReader clone(int[] pitk) {
		return new ProxyLogReader(parent.clone(pitk));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#getFile()
	 */
	public LogFile getFile() {
		return parent.getFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#getInstance(int)
	 */
	public ProcessInstance getInstance(int index) {
		return new ProxyProcessInstance(parent.getInstance(index));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#getLogFilter()
	 */
	public LogFilter getLogFilter() {
		return parent.getLogFilter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#getLogSummary()
	 */
	public LogSummary getLogSummary() {
		return parent.getLogSummary();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#getProcess(int)
	 */
	public Process getProcess(int index) {
		return new ProxyProcess(parent.getProcess(index));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#hasNext()
	 */
	public boolean hasNext() {
		return parent.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#instanceIterator()
	 */
	public Iterator instanceIterator() {
		return new ProxyProcessInstanceIterator(parent.instanceIterator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#isSelection()
	 */
	public boolean isSelection() {
		return parent.isSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#next()
	 */
	public ProcessInstance next() {
		return new ProxyProcessInstance(parent.next());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#numberOfInstances()
	 */
	public int numberOfInstances() {
		return parent.numberOfInstances();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#numberOfProcesses()
	 */
	public int numberOfProcesses() {
		return parent.numberOfProcesses();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#processInstancesToKeep()
	 */
	public int[] processInstancesToKeep() {
		return parent.processInstancesToKeep();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#processIterator()
	 */
	public Iterator processIterator() {
		return new ProxyProcessIterator(parent.processIterator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#reset()
	 */
	public void reset() {
		parent.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#toString()
	 */
	public String toString() {
		return parent.toString();
	}

	protected class ProxyProcessIterator implements Iterator {

		protected Iterator parent = null;

		public ProxyProcessIterator(Iterator aParent) {
			parent = aParent;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return parent.hasNext();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			return new ProxyProcess((Process) parent.next());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			System.err.println("Modification not allowed on proxies!");
		}

	}

	protected class ProxyProcessInstanceIterator implements Iterator {

		protected Iterator parent = null;

		public ProxyProcessInstanceIterator(Iterator aParent) {
			parent = aParent;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return parent.hasNext();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			return new ProxyProcessInstance((ProcessInstance) parent.next());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			System.err.println("Modification not allowed on proxies!");
		}

	}

}
