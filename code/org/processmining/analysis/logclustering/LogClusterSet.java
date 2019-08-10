package org.processmining.analysis.logclustering;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * @author Minseok Song
 * 
 */
public class LogClusterSet implements Comparator {

	protected ArrayList<LogCluster> clusters;

	public LogClusterSet() {
		clusters = new ArrayList<LogCluster>();
	}

	public void clear() {
		clusters.clear();
	}

	public void addLogCluster(LogCluster logCluster) {
		clusters.add(logCluster);
	}

	public int size() {
		return clusters.size();
	}

	public List<LogCluster> getClusters() {
		return clusters;
	}

	public LogCluster getCluster(int i) {
		return clusters.get(i);
	}

	public Object clone() {
		LogClusterSet obj = new LogClusterSet();
		Iterator itr = clusters.iterator();
		while (itr.hasNext()) {
			obj.addLogCluster((LogCluster) ((LogCluster) itr.next()).clone());
		}
		return obj;
	}

	public int compare(Object o1, Object o2) {
		return 0;
	}

	public boolean equals(Object obj) {
		boolean bResult = true;
		for (int i = 0; i < clusters.size(); i++) {
			LogCluster aCluster = clusters.get(i);
			LogCluster bCluster = ((LogClusterSet) obj).getCluster(i);
			if (aCluster.size() != bCluster.size()) {
				bResult = false;
				break;
			}
			HashSet<Integer> traces = aCluster.getTraces();
			Iterator itr = traces.iterator();
			while (itr.hasNext()) {
				Integer temp = (Integer) itr.next();
				if (!bCluster.containTrace(temp)) {
					bResult = false;
					return bResult;
				}
			}
		}
		return bResult;
	}
}
