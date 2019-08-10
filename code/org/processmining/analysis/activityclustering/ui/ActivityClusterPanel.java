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
package org.processmining.analysis.activityclustering.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.analysis.activityclustering.model.Cluster;
import org.processmining.analysis.activityclustering.model.ClusterType;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;

/**
 * @author christian
 * 
 */
public class ActivityClusterPanel extends JComponent {

	protected LogReader log;
	protected List<ClusterType> types;
	protected List<Cluster>[] instanceClusters;
	protected List<Integer>[] instanceClustersTypes;
	protected Date[] instancesStart;
	protected Date[] instancesEnd;
	protected Date start;
	protected Date end;
	long lStart;
	long lEnd;
	long duration;
	protected Color clusterColor = new Color(255, 0, 0, 100);
	protected int instanceHeight = 70;
	protected int maxOffset = 10;
	protected double zoom = 1.0;
	protected int width;
	protected int height;

	public ActivityClusterPanel(LogReader log, List<ClusterType> clusterTypes) {
		setOpaque(true);
		types = clusterTypes;
		this.log = log;
		updateTypes();
	}

	public void setClusterTypes(List<ClusterType> clusterTypes) {
		types = clusterTypes;
		updateTypes();
	}

	protected void updateTypes() {
		instanceClusters = (List<Cluster>[]) new List[log.numberOfInstances()];
		instanceClustersTypes = (List<Integer>[]) new List[log
				.numberOfInstances()];
		instancesStart = new Date[log.numberOfInstances()];
		instancesEnd = new Date[log.numberOfInstances()];
		for (int i = 0; i < log.numberOfInstances(); i++) {
			instanceClusters[i] = new ArrayList<Cluster>();
			instanceClustersTypes[i] = new ArrayList<Integer>();
			try {
				AuditTrailEntryList iList = log.getInstance(i)
						.getAuditTrailEntryList();
				Date iStart = iList.get(0).getTimestamp();
				Date iStop = iList.get(iList.size() - 1).getTimestamp();
				// instancesStart[i] = iStart;
				// instancesEnd[i] = iStop;
				if (start == null || iStart.before(start)) {
					start = iStart;
				}
				if (end == null || iStop.after(end)) {
					end = iStop;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// update date / time
		lStart = start.getTime();
		lEnd = end.getTime();
		duration = lEnd - lStart;
		// sort clusters into respective instance lists
		for (int i = types.size() - 1; i >= 0; i--) {
			for (Cluster cluster : types.get(i).getInstances()) {
				int instanceIndex = cluster.getInstanceIndex();
				instanceClusters[instanceIndex].add(cluster);
				instanceClustersTypes[instanceIndex].add(i);
				try {
					Date cStart = cluster.getStart();
					if (instancesStart[instanceIndex] == null
							|| instancesStart[instanceIndex].after(cStart)) {
						instancesStart[instanceIndex] = cStart;
					}
					Date cEnd = cluster.getEnd();
					if (instancesEnd[instanceIndex] == null
							|| instancesEnd[instanceIndex].before(cEnd)) {
						instancesEnd[instanceIndex] = cEnd;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// sort all instance lists
		for (List<Cluster> instanceList : instanceClusters) {
			Collections.sort(instanceList);
		}
		int height = log.numberOfInstances() * this.instanceHeight;
		this.setMinimumSize(new Dimension(3000, height));
		this.setMaximumSize(new Dimension(3000, height));
		this.setPreferredSize(new Dimension(3000, height));
	}

	protected void paintComponent(Graphics g) {
		width = getWidth();
		height = getHeight();
		Rectangle clip = new Rectangle(0, 0, width, height);// this.getVisibleRect();
		// calculate viewport in date terms
		double pctLeft = (double) clip.x / (double) width;
		double pctRight = (double) (clip.x + clip.width) / (double) width;
		/*
		 * long lLeft = lStart + (long)((double)duration * pctLeft); long lRight
		 * = lStart + (long)((double)duration * pctRight); Date dLeft = new
		 * Date(lLeft); Date dRight = new Date(lRight);
		 */
		// System.out.println(dLeft + " -> " + dRight);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// draw background
		g2d.setColor(Color.BLACK);
		g2d.fillRect(clip.x, clip.y, clip.width, clip.height);
		int clipLow = clip.x + clip.height;
		int y = 30, yLow = 65;
		for (int i = 0; i < log.numberOfInstances(); i++) {
			g2d.setColor(new Color(0, 0, 50));
			g2d.fillRect(clip.x, y, clip.width, 35);
			// check whether instance is (vertically spoken) visible
			if (y <= clipLow && yLow >= clip.y) {
				// visible
				g2d.setColor(this.clusterColor);
				// calculate boundaries for instance
				long liStart = instancesStart[i].getTime();
				long liEnd = instancesEnd[i].getTime();
				long duration = liEnd - liStart;
				if (duration == 0) {
					System.out.println("duration is zero for instance " + i);
					duration = 1000;
				}
				long liLeft = liStart + (long) ((double) duration * pctLeft);
				long liRight = liStart + (long) ((double) duration * pctRight);
				if (instanceClusters[i].size() == 0) {
					System.out.println("no clusters for instance " + i + "!");
				}
				// track clusters for instance
				for (int j = 0; j < instanceClusters[i].size(); j++) {
					try {
						Cluster cluster = instanceClusters[i].get(j);
						Date cStart = cluster.getStart();
						Date cEnd = cluster.getEnd();
						if (true) {// cEnd.getTime() >= liLeft &&
							// cStart.getTime() <= liRight) {
							// cluster intersects viewport, draw!
							int xStart = translateToXCoordinateInstance(i,
									cStart.getTime());
							int xEnd = translateToXCoordinateInstance(i, cEnd
									.getTime());
							if (xStart == xEnd) {
								if (cStart.equals(instancesStart[i])
										&& cEnd.equals(instancesEnd[i])) {
									xStart = 0;
									xEnd = width - 1;
								} else {
									xEnd = xStart + 2;
								}
							}
							// System.out.println(xStart + " -> " + xEnd +
							// " -- " + cStart + " -> " +cEnd);
							int yStart = y + 3
									+ instanceClustersTypes[i].get(j)
									% this.maxOffset;
							g2d.fillRect(xStart, yStart, xEnd - xStart + 1, 10);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			y += this.instanceHeight;
			yLow += this.instanceHeight;
		}

	}

	protected int translateToXCoordinateInstance(int instance, long timestamp) {
		long normalizedPosition = timestamp
				- instancesStart[instance].getTime();
		long duration = instancesEnd[instance].getTime()
				- instancesStart[instance].getTime();
		// System.out.println(normalizedPosition + " - " + timestamp);
		if (normalizedPosition == 0 || duration == 0) {
			return 0;
		}
		double pct = (double) normalizedPosition / (double) duration;
		return (int) ((double) width * pct);
	}

	protected int translateToXCoordinate(long timestamp) {
		long normalizedPosition = timestamp - lStart;
		// System.out.println(normalizedPosition + " - " + timestamp);
		if (normalizedPosition == 0) {
			return 0;
		}
		double pct = (double) normalizedPosition / (double) duration;
		return (int) ((double) width * pct);
	}

}
