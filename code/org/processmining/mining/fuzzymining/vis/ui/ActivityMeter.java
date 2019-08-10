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
package org.processmining.mining.fuzzymining.vis.ui;

import java.text.DecimalFormat;

import org.processmining.mining.fuzzymining.vis.anim.Animation;
import org.processmining.mining.fuzzymining.vis.anim.ArcAnimation;
import org.processmining.mining.fuzzymining.vis.anim.TaskAnimation;

/**
 * @author christian
 * 
 */
public class ActivityMeter extends AnimationMeter {

	protected DecimalFormat format = new DecimalFormat("##0 %");

	/**
	 * @param anim
	 * @param name
	 */
	public ActivityMeter(Animation anim) {
		super(anim, "Activity");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.vis.ui.AnimationMeter#update(long)
	 */
	@Override
	protected void update(long modelTime) {
		float maxActivity = 0;
		float activity = 0;
		long maxTaskDelay = modelTime - (getAnimation().getFadeTime() * 5);
		for (TaskAnimation anim : getAnimation().getPrimitiveTaskAnimations()) {
			maxActivity++;
			if (anim.getKeyframe(modelTime).getTime() > maxTaskDelay) {
				activity += 4;
			}
		}
		for (TaskAnimation anim : getAnimation().getClusterTaskAnimations()) {
			maxActivity++;
			if (anim.getKeyframe(modelTime).getTime() > maxTaskDelay) {
				activity += 2;
			}
		}
		for (ArcAnimation anim : getAnimation().getArcAnimations()) {
			maxActivity++;
			activity += anim.getKeyframe(modelTime).getTokenAnimations().size();
		}
		// amplify activity
		activity *= 2;
		activity = activity / maxActivity;
		if (activity > 1f) {
			activity = 1f;
		}
		this.setState(activity, format.format(activity));
	}

}
