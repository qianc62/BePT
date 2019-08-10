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

package org.processmining.converting.yawl2pn;

public class YawlToPetriNetSettings {
	public static final int CREATE_TASK_TRANSITION = 0;
	public static final int CREATE_CLUSTERS = 1;
	public static final int CREATE_LOG_EVENTS = 2;
	public static final int REDUCE_PN = 3;
	public static final int MAX = 4;

	private boolean[] settings;
	private String decomposition;

	public YawlToPetriNetSettings() {
		settings = new boolean[MAX];
		for (int i = 0; i < MAX; i++) {
			settings[i] = true;
		}
	}

	public boolean set(int index, boolean value) {
		assert index >= 0 && index < MAX;
		boolean old = settings[index];
		settings[index] = value;
		return old;
	}

	public boolean get(int index) {
		assert index >= 0 && index < MAX;
		return settings[index];
	}

	public String setDecomposition(String decomposition) {
		String old = this.decomposition;
		this.decomposition = decomposition;
		return old;
	}

	public String getDecomposition() {
		return decomposition;
	}
}
