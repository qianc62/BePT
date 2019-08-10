/*
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.analysis.streamscope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.Progress;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class EventClassTable {

	public class EventClass {

		protected String name;
		protected int frequency;
		protected int index;

		public EventClass(String name, int index) {
			this.index = index;
			this.name = name;
			this.frequency = 0;
		}

		public int getIndex() {
			return index;
		}

		public String name() {
			return name;
		}

		public int frequency() {
			return frequency;
		}

		public void incrementFrequency() {
			frequency++;
		}

		public boolean equals(Object o) {
			if (o instanceof EventClass) {
				return name.equals(((EventClass) o).name);
			} else {
				return false;
			}
		}
	}

	protected Map<String, EventClass> classMap;
	protected List<EventClass> classIndices;

	public EventClassTable(LogReader log, Progress progress) {
		try {
			initialize(log, progress);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initialize(LogReader log, Progress progress) throws IOException {
		classMap = new HashMap<String, EventClass>();
		classIndices = new ArrayList<EventClass>();
		if (log != null) {
			/*
			 * for(int i=0; i<log.numberOfInstances(); i++) {
			 * AuditTrailEntryList ateList =
			 * log.getInstance(i).getAuditTrailEntryList(); for(int k=0;
			 * k<ateList.size(); k++) { AuditTrailEntry ate = ateList.get(k);
			 * register(ate.getElement()); } progress.inc(); }
			 */
			for (LogEvent logEvent : log.getLogSummary().getLogEvents()) {
				register(logEvent.getModelElementName());
				// progress.inc();
			}
		}
	}

	public void register(String name) {
		EventClass ec = classMap.get(name);
		if (ec == null) {
			ec = new EventClass(name, classIndices.size());
			classMap.put(ec.name(), ec);
			classIndices.add(ec);
		}
		ec.incrementFrequency();
	}

	public List<EventClass> getEventClasses() {
		return classIndices;
	}

	public int getIndex(EventClass ec) {
		return classIndices.indexOf(ec);
	}

	public int getIndex(String element) {
		return getIndex(classMap.get(element));
	}

	public EventClass getClassByIndex(int index) {
		return classIndices.get(index);
	}

	public int size() {
		return classIndices.size();
	}

}
