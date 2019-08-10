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

import org.deckfour.gantzgraf.layout.GGCurve;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ArcAnimation {

	protected final String id;
	protected final String sourceId;
	protected final String targetId;
	protected final GGCurve curve;

	protected List<ArcAnimationKeyframe> keyframeList;

	protected long[] keyframeTimes;
	protected ArcAnimationKeyframe[] keyframes;

	// buffer to speed up repeated lookup
	protected long bufferKeyframeTime;
	protected ArcAnimationKeyframe bufferKeyframe;

	public ArcAnimation(String id, String sourceId, String targetId,
			GGCurve curve) {
		this.id = id;
		this.sourceId = sourceId;
		this.targetId = targetId;
		this.curve = curve;
		keyframeList = new ArrayList<ArcAnimationKeyframe>();
	}

	public String id() {
		return id;
	}

	public String getSourceId() {
		return sourceId;
	}

	public String getTargetId() {
		return targetId;
	}

	public GGCurve getCurve() {
		return curve;
	}

	public synchronized ArcAnimationKeyframe getKeyframe(long time) {
		// look at buffer first
		if (time == bufferKeyframeTime) {
			return bufferKeyframe;
		}
		// look up
		if (time < keyframeTimes[0]) {
			bufferKeyframeTime = time;
			bufferKeyframe = null;
			return null;
		} else if (time >= keyframeTimes[keyframeTimes.length - 1]) {
			bufferKeyframeTime = time;
			bufferKeyframe = keyframes[keyframes.length - 1];
			return keyframes[keyframes.length - 1];
		} else {
			for (int i = 1; i < keyframeTimes.length; i++) {
				if (keyframeTimes[i] > time) {
					bufferKeyframeTime = time;
					bufferKeyframe = keyframes[i - 1];
					return keyframes[i - 1];
				}
			}
			// code should not be reached!
			bufferKeyframeTime = time;
			bufferKeyframe = null;
			return null;
		}
	}

	public int getTraverseCount() {
		return keyframes[keyframes.length - 1].getTraverseCount();
	}

	public void finalizeData() {
		// add traverse count and last-traversed attributes
		// to all keyframes in list
		long lastCompleted = 0;
		int traverseCount = 0;
		ArcAnimationKeyframe currentFrame;
		List<TokenAnimation> currentAnims, lastAnims;
		lastAnims = new ArrayList<TokenAnimation>();
		for (int i = 1; i < keyframeList.size(); i++) {
			currentFrame = keyframeList.get(i);
			currentAnims = currentFrame.getTokenAnimations();
			for (TokenAnimation anim : lastAnims) {
				if (anim.getEnd() == currentFrame.getTime()) {
					// token animation has completed at this step
					traverseCount++;
					lastCompleted = currentFrame.getTime();
				}
			}
			currentFrame.setTraverseCount(traverseCount);
			currentFrame.setLastCompleted(lastCompleted);
			lastAnims = currentAnims;
		}
		// add first empty keyframe
		ArcAnimationKeyframe firstFrame = new ArcAnimationKeyframe(0);
		firstFrame.setLastCompleted(0);
		firstFrame.setTraverseCount(0);
		keyframeList.add(0, firstFrame);
		// create efficient data structures for lookup
		keyframeTimes = new long[keyframeList.size()];
		keyframes = new ArcAnimationKeyframe[keyframeList.size()];
		for (int i = 0; i < keyframeList.size(); i++) {
			keyframes[i] = keyframeList.get(i);
			keyframeTimes[i] = keyframes[i].getTime();
		}
		// remove list (triggers null pointer exceptions on errors)
		keyframeList = null;
		// initialize buffer
		bufferKeyframeTime = -1;
		bufferKeyframe = null;
	}

	public void addTokenAnimation(TokenAnimation anim) {
		int startIndex = getKeyframeIndexAtTimeInserting(anim.getStart());
		int endIndex = getKeyframeIndexAtTimeInserting(anim.getEnd());
		for (int i = startIndex; i < endIndex; i++) {
			keyframeList.get(i).addTokenAnimation(anim);
		}
	}

	protected int getKeyframeIndexAtTimeInserting(long time) {
		int listSize = keyframeList.size();
		// first keyframe is special case
		if (listSize == 0) {
			insertKeyframeAtPosition(0, time);
			return 0;
		}
		long lastTime = keyframeList.get(listSize - 1).getTime();
		long firstTime = keyframeList.get(0).getTime();
		if (time < firstTime) {
			// insert at first position
			insertKeyframeAtPosition(0, time);
			return 0;
		} else if (time > lastTime) {
			// insert at last position
			insertKeyframeAtPosition(listSize, time);
			return listSize;
		} else {
			// determine from which end to start searching
			long diffToLast = lastTime - time;
			long diffToFirst = time - firstTime;
			long currentTime;
			if (diffToFirst < diffToLast) {
				// search from start of list
				for (int i = 0; i < listSize; i++) {
					currentTime = keyframeList.get(i).getTime();
					if (currentTime == time) {
						return i;
					} else if (currentTime > time) {
						insertKeyframeAtPosition(i, time);
						return i;
					}
				}
			} else {
				// search from end of list
				for (int i = listSize - 1; i >= 0; i--) {
					currentTime = keyframeList.get(i).getTime();
					if (currentTime == time) {
						return i;
					} else if (currentTime < time) {
						insertKeyframeAtPosition(i + 1, time);
						return i + 1;
					}
				}
			}
		}
		throw new Error("unable to address correctly!");
	}

	protected void insertKeyframeAtPosition(int position, long time) {
		ArcAnimationKeyframe keyFrame = new ArcAnimationKeyframe(time);
		keyframeList.add(position, keyFrame);
		// copy relevant token animations from last frame
		if (position > 0) {
			List<TokenAnimation> tokenAnimations = keyframeList.get(
					position - 1).getTokenAnimations();
			for (TokenAnimation anim : tokenAnimations) {
				if (anim.getEnd() > time) {
					keyFrame.addTokenAnimation(anim);
				}
			}
		}
	}

}
