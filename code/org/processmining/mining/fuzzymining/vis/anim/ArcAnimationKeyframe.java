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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ArcAnimationKeyframe {

	protected long time;
	protected long lastCompleted;
	protected int traverseCount;

	protected List<TokenAnimation> tokenAnimations;

	public ArcAnimationKeyframe(long time) {
		this.time = time;
		this.lastCompleted = -1;
		this.traverseCount = -1;
		this.tokenAnimations = new ArrayList<TokenAnimation>();
	}

	/**
	 * Adds the provided token animation at the logical position within the list
	 * already contained. The animations are sorted in order of their finishing,
	 * i.e. the one finishing first is also the first element in the list.
	 * 
	 * @param anim
	 *            Token animation to be added
	 */
	public void addTokenAnimation(TokenAnimation anim) {
		long animEnd = anim.getEnd();
		for (int i = tokenAnimations.size(); i > 0; i--) {
			if (tokenAnimations.get(i - 1).getEnd() < animEnd) {
				tokenAnimations.add(i, anim);
				return;
			}
		}
		tokenAnimations.add(0, anim);
	}

	public void setLastCompleted(long lastCompleted) {
		this.lastCompleted = lastCompleted;
	}

	public void setTraverseCount(int traverseCount) {
		this.traverseCount = traverseCount;
	}

	public long getTime() {
		return time;
	}

	public long getLastCompleted() {
		return lastCompleted;
	}

	public int getTraverseCount() {
		return traverseCount;
	}

	public List<TokenAnimation> getTokenAnimations() {
		return tokenAnimations;
	}

}
