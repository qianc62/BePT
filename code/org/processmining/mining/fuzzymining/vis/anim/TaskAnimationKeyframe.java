/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.mining.fuzzymining.vis.anim;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class TaskAnimationKeyframe implements Comparable<TaskAnimationKeyframe> {

	protected String caseId;
	protected long time;
	protected int eventCount;
	protected int emergeCount;
	protected int swallowCount;

	public TaskAnimationKeyframe(String caseId, long time, boolean emerged,
			boolean swallowed) {
		this.caseId = caseId;
		this.time = time;
		this.eventCount = 0;
		this.emergeCount = 0;
		this.swallowCount = 0;
		if (emerged) {
			this.emergeCount++;
		}
		if (swallowed) {
			this.swallowCount++;
		}
	}

	public void setEventCount(int eventCount) {
		this.eventCount = eventCount;
	}

	public String getCaseId() {
		return caseId;
	}

	public long getTime() {
		return time;
	}

	public int getEventCount() {
		return eventCount;
	}

	public int getEmergeCount() {
		return emergeCount;
	}

	public int getSwallowCount() {
		return swallowCount;
	}

	public void setEmergeCount(int emergeCount) {
		this.emergeCount = emergeCount;
	}

	public void setSwallowCount(int swallowCount) {
		this.swallowCount = swallowCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TaskAnimationKeyframe o) {
		if (time > o.time) {
			return 1;
		} else if (time < o.time) {
			return -1;
		} else {
			return 0;
		}
	}

}
