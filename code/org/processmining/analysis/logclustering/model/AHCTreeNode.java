package org.processmining.analysis.logclustering.model;

import java.util.Iterator;

import org.processmining.analysis.logclustering.LogCluster;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class AHCTreeNode extends LogCluster {
	private LogCluster left = null;
	private LogCluster right = null;
	private double distance = 0.0;
	private String name = "";

	public AHCTreeNode(LogCluster left, LogCluster right, double distance) {
		this.left = left;
		this.right = right;
		this.distance = distance;

		// generate trace list with left node
		if (left != null && left.getTraces() != null) {
			Iterator itr = left.getTraces().iterator();
			while (itr.hasNext()) {
				this.addTraceStat((Integer) itr.next());
			}
		}

		// generate trace list with right node
		if (right != null && right.getTraces() != null) {
			Iterator itr = right.getTraces().iterator();
			while (itr.hasNext()) {
				this.addTraceStat((Integer) itr.next());
			}
		}
	}

	public void setName(String str) {
		name = str;
	}

	public String getName() {
		return name;
	}

	public LogCluster getLeft() {
		return left;
	}

	public LogCluster getRight() {
		return right;
	}

	public double getDistance() {
		return distance;
	}

}
