package org.processmining.converting.fsm;

/**
 * <p>
 * Title: FsmModificationSettings
 * </p>
 * 
 * <p>
 * Description: Settings for the FSM modification plug-in.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class FsmModificationSettings {

	public static final int KILLSELFLOOPSTRATEGY = 0;
	public static final int EXTENDSTRATEGY = 1;
	public static final int MERGYBYOUTPUTSTRATEGY = 2;
	public static final int MERGYBYINPUTSTRATEGY = 3;
	public static final int LAST = 4;

	private boolean[] use = new boolean[LAST];

	public FsmModificationSettings() {
		for (int strategy = 0; strategy < LAST; strategy++) {
			use[strategy] = false;
		}
	}

	public boolean getUse(int strategy) {
		return use[strategy];
	}

	public boolean setUse(int strategy, boolean newValue) {
		boolean oldValue = use[strategy];
		use[strategy] = newValue;
		return oldValue;
	}
}
