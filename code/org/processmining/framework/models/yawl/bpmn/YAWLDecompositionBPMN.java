/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
package org.processmining.framework.models.yawl.bpmn;

import org.processmining.framework.models.yawl.YAWLDecomposition;
import java.util.ArrayList;

public class YAWLDecompositionBPMN extends YAWLDecomposition {
	// pools and lanes when YAWLModel converted to and from BPMN model
	private ArrayList<String> pools = new ArrayList<String>();
	private ArrayList<String> lanes = new ArrayList<String>();

	public YAWLDecompositionBPMN(String id, String isRootNet, String xsiType) {
		super(id, isRootNet, xsiType);
	}

	public String[] getPools() {
		return pools.toArray(new String[pools.size()]);
	}

	public String[] getLanes() {
		return lanes.toArray(new String[lanes.size()]);
	}

	public void addPoolLane(String poolLane, boolean isPool) {
		if (isPool) {
			if (!pools.contains(poolLane)) {
				pools.add(poolLane);
			}
		} else if (!lanes.contains(poolLane)) {
			lanes.add(poolLane);
		}
	}
}
