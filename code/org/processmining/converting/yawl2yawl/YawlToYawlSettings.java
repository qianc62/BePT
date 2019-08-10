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

package org.processmining.converting.yawl2yawl;

public class YawlToYawlSettings {
	public static final int ALL_CONDITIONS_EXPLICIT = 0;
	public static final int FLATTEN_MODEL = 1;
	public static final int USE_HIERARCHY = 2;
	public static final int MAX = 3;

	private boolean[] settings;

	public YawlToYawlSettings() {
		settings = new boolean[MAX];
		for (int i = 0; i < MAX; i++) {
			settings[i] = false;
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
}
