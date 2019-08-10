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
package org.processmining.mining.organizationmining.model;

import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.organizationmining.profile.AggregateProfile;

/**
 * @author Minseok
 * 
 */
public class ClusterSet implements Provider {

	protected LogReader log;
	protected List<Cluster> clusters;
	protected AggregateProfile agProfiles;

	public ClusterSet(LogReader aLog) {
		log = aLog;
		clusters = new ArrayList<Cluster>();
	}

	public ClusterSet(LogReader aLog, AggregateProfile aAGProfiles) {
		log = aLog;
		agProfiles = aAGProfiles;
		clusters = new ArrayList<Cluster>();
	}

	public void initializeClusterSet() {
		clusters.clear();
		for (int i = 0; i < log.getLogSummary().getOriginators().length; i++) {
			Cluster cluster = new Cluster(log, "OrgUnit " + i);
			cluster.addOriginator(log.getLogSummary().getOriginators()[i]);
			clusters.add(cluster);
		}
	}

	public int size() {
		return clusters.size();
	}

	public void clear() {
		clusters.clear();
	}

	public LogReader getLog() {
		return log;
	}

	public AggregateProfile getAGProfiles() {
		return agProfiles;
	}

	public List<Cluster> getClusters() {
		return clusters;
	}

	public void addCluster(Cluster clusterA) {
		clusters.add(clusterA);
	}

	public void removeCluster(Cluster clusterA) {
		clusters.remove(clusterA);
	}

	public Cluster mergeClusters(Cluster clusterA, Cluster clusterB) {
		clusterA.mergeWith(clusterB);
		clusters.remove(clusterB);
		return clusterA;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[clusters.size() + 2];
		int index = 0;
		// add all currently displayed clusters
		/*
		 * for(Cluster cluster : clusters) { try { objects[index] =
		 * cluster.getProvidedObject(); } catch(Exception e) {
		 * e.printStackTrace(); return null; } index++; }
		 */
		// add complete log
		objects[index] = new ProvidedObject("Complete log (all clusters)",
				new Object[] { log });
		objects[++index] = new ProvidedObject("Cluster Set",
				new Object[] { this });

		return objects;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ClusterSet clone = new ClusterSet(log);
		for (Cluster cluster : clusters) {
			clone.addCluster((Cluster) (cluster.clone()));
		}
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClusterSet) {
			ClusterSet other = (ClusterSet) obj;
			return (clusters.containsAll(other.clusters) && other.clusters
					.containsAll(clusters));
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return clusters.size();
	}

	@Override
	public String toString() {
		return "Cluster with size " + clusters.size();
	}

}
