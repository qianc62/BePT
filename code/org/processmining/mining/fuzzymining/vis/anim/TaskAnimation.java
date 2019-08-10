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

import org.processmining.mining.fuzzymining.graph.Node;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class TaskAnimation {

	public enum Type {
		PRIMITIVE, CLUSTER;
	}

	protected final Node node;
	protected final String id;
	protected final Type type;
	protected final String eventName;
	protected final String eventType;
	protected final float positionX;
	protected final float positionY;
	protected final float width;
	protected final float height;

	protected List<TaskAnimationKeyframe> keyframeList;

	protected long[] keyframeTime;
	protected TaskAnimationKeyframe[] keyframes;

	// buffers to speed up repeated lookups
	protected long keyframeBufferTime;
	protected TaskAnimationKeyframe keyframeBuffer;

	public TaskAnimation(Node node, String id, Type type, float positionX,
			float positionY, float width, float height) {
		this.node = node;
		this.id = id;
		this.type = type;
		this.eventName = node.getElementName();
		this.eventType = node.getEventType();
		this.positionX = positionX;
		this.positionY = positionY;
		this.width = width;
		this.height = height;
		keyframeList = new ArrayList<TaskAnimationKeyframe>();
	}

	public Node getNode() {
		return node;
	}

	public String id() {
		return id;
	}

	public Type getType() {
		return type;
	}

	public String getEventName() {
		return eventName;
	}

	public String getEventType() {
		return eventType;
	}

	public float getPositionX() {
		return positionX;
	}

	public float getPositionY() {
		return positionY;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public void addKeyframe(TaskAnimationKeyframe keyframe) {
		for (int i = keyframeList.size(); i > 0; i--) {
			if (keyframe.getTime() >= keyframeList.get(i - 1).getTime()) {
				keyframeList.add(i, keyframe);
				return;
			}
		}
		// insert at zero position
		keyframeList.add(0, keyframe);
	}

	public void finalizeData() {
		// add first keyframe
		TaskAnimationKeyframe firstFrame = new TaskAnimationKeyframe("invalid",
				0, false, false);
		firstFrame.setEmergeCount(0);
		firstFrame.setSwallowCount(0);
		firstFrame.setEventCount(0);
		keyframeList.add(0, firstFrame);
		keyframeTime = new long[keyframeList.size()];
		keyframes = new TaskAnimationKeyframe[keyframeList.size()];
		int emergeCount = 0;
		int swallowCount = 0;
		for (int i = 0; i < keyframeList.size(); i++) {
			keyframes[i] = keyframeList.get(i);
			keyframeTime[i] = keyframes[i].getTime();
			// update keyframe information
			keyframes[i].setEventCount(i);
			emergeCount += keyframes[i].getEmergeCount();
			keyframes[i].setEmergeCount(emergeCount);
			swallowCount += keyframes[i].getSwallowCount();
			keyframes[i].setSwallowCount(swallowCount);
		}
		keyframeList = null;
		keyframeBufferTime = -1;
		keyframeBuffer = null;
	}

	public synchronized TaskAnimationKeyframe getKeyframe(long time) {
		// check buffer first
		if (time == keyframeBufferTime) {
			return keyframeBuffer;
		}
		// look up
		if (time < keyframeTime[0]) {
			// no keyframe yet
			keyframeBufferTime = time;
			keyframeBuffer = null;
			return null;
		} else if (time >= keyframeTime[keyframeTime.length - 1]) {
			// last keyframe
			keyframeBufferTime = time;
			keyframeBuffer = keyframes[keyframes.length - 1];
			return keyframes[keyframes.length - 1];
		} else {
			for (int i = 1; i < keyframes.length; i++) {
				if (keyframeTime[i] > time) {
					keyframeBufferTime = time;
					keyframeBuffer = keyframes[i - 1];
					return keyframes[i - 1];
				}
			}
			// code should not be reached!
			keyframeBufferTime = time;
			keyframeBuffer = null;
			return null;
		}
	}

	public int getActivityBetween(long start, long end) {
		int activity = 0;
		for (int i = 0; i < keyframeTime.length; i++) {
			if (keyframeTime[i] >= start) {
				if (keyframeTime[i] < end) {
					activity++;
				} else {
					break;
				}
			}
		}
		return activity;
	}

}
