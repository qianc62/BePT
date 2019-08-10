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
public class AnimationTimer {

	protected long modelTimeStart;
	protected long modelTimeEnd;
	protected long realTimeStart;
	protected long realTimeEnd;
	protected double speedFactor;
	protected double relativePosition;
	protected long updateInterval;
	protected UpdateThread updateThread;

	protected List<AnimationListener> listeners;

	public AnimationTimer(long modelTimeStart, long modelTimeEnd,
			double speedFactor, long updateInterval) {
		this.modelTimeStart = modelTimeStart;
		this.modelTimeEnd = modelTimeEnd;
		this.speedFactor = speedFactor;
		this.relativePosition = 0.0;
		this.realTimeStart = -1;
		this.realTimeEnd = -1;
		this.updateInterval = updateInterval;
		this.listeners = new ArrayList<AnimationListener>();
		this.updateThread = null;
	}

	public double getSpeedFactor() {
		return speedFactor;
	}

	public long translateModelTimeToRealTime(long modelTime) {
		double percentage = (double) (modelTime - this.modelTimeStart)
				/ (double) (this.modelTimeEnd - this.modelTimeStart);
		return realTimeStart
				+ (long) (percentage * (double) (realTimeEnd - realTimeStart));
	}

	public long translateModelDurationToReal(long modelDuration) {
		return (long) ((double) modelDuration / speedFactor);
	}

	protected void updateData() {
		relativePosition = (double) (System.currentTimeMillis() - realTimeStart)
				/ (double) (realTimeEnd - realTimeStart);
		if (relativePosition > 1.0) {
			relativePosition = 1.0;
		}
	}

	protected void updateRealTimes() {
		long modelDuration = modelTimeEnd - modelTimeStart;
		long realDuration = translateModelDurationToReal(modelDuration);
		long currentTime = System.currentTimeMillis();
		this.realTimeStart = currentTime
				- (long) ((double) realDuration * this.relativePosition);
		this.realTimeEnd = this.realTimeStart + realDuration;
	}

	public void setSpeedFactor(double speedFactor) {
		this.speedFactor = speedFactor;
		updateRealTimes();
	}

	public synchronized void setRelativePosition(double relativePosition) {
		this.relativePosition = relativePosition;
		updateRealTimes();
		if (updateThread == null) {
			long modelTime = modelTimeStart
					+ (long) ((double) (modelTimeEnd - modelTimeStart) * relativePosition);
			for (AnimationListener listener : listeners) {
				listener.updateModelTime(modelTime);
			}
		}
	}

	public void addListener(AnimationListener listener) {
		listeners.add(listener);
	}

	public void removeAllListeners() {
		listeners.clear();
	}

	public void setUpdateInterval(long updateInterval) {
		this.updateInterval = updateInterval;
	}

	public void setUpdateFramesPerSecond(int fps) {
		this.updateInterval = 1000 / fps;
	}

	public synchronized boolean isRunning() {
		return (updateThread != null);
	}

	public synchronized void start() {
		if (updateThread == null) {
			updateRealTimes();
			updateThread = new UpdateThread();
			updateThread.start();
			for (AnimationListener listener : listeners) {
				listener.started();
			}
		}
	}

	public synchronized void stop() {
		if (updateThread != null) {
			updateThread.stopRunning();
		}
		updateThread = null;
	}

	protected class UpdateThread extends Thread {

		protected boolean isRunning = false;
		protected long lastInvoked = -1;

		public synchronized void start() {
			if (isRunning == false) {
				isRunning = true;
				super.start();
			}
		}

		public synchronized void stopRunning() {
			isRunning = false;
			lastInvoked = -1;
			for (AnimationListener listener : listeners) {
				listener.stopped();
			}
			updateThread = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			while (isRunning) {
				updateData();
				long modelTime = modelTimeStart
						+ (long) ((double) (modelTimeEnd - modelTimeStart) * relativePosition);
				for (AnimationListener listener : listeners) {
					listener.updateModelTime(modelTime);
				}
				if (relativePosition >= 1.0) {
					stopRunning();
					return; // finished animation
				}
				// calculate sleep time
				long currentTime = System.currentTimeMillis();
				long sleepTime = updateInterval;
				if (lastInvoked > 0) {
					long cycleTime = currentTime - lastInvoked;
					if (cycleTime > updateInterval) {
						// make up for lost drawing overtime
						sleepTime -= (cycleTime - updateInterval);
						// safety check
						if (sleepTime < 0) {
							sleepTime = 0;
						}
					}
				}
				lastInvoked = currentTime;
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// should be no big deal
					e.printStackTrace();
				}
			}
		}

	}

}
