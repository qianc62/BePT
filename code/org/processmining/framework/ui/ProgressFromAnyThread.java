package org.processmining.framework.ui;

import javax.swing.SwingUtilities;

public class ProgressFromAnyThread extends Progress {
	public ProgressFromAnyThread(String s) {
		super(s);
	}

	protected void doUpdate(Runnable r) {
		if (progress != null) {
			SwingUtilities.invokeLater(r);
		}
	}
}
