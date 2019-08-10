/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.ui;

import java.lang.reflect.InvocationTargetException;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

/**
 * Realizes a progress bar (showing the status of some operation).
 * 
 * @author Peter van den Brand
 * @version 1.0
 */
public class Progress implements CancelationComponent {

	protected ProgressMonitor progress = null;
	private int val;

	/**
	 * Creates a progress bar with the given caption and a default range from 0
	 * to 100.
	 * 
	 * @param caption
	 *            the text to be displayed above the progress visualization
	 */
	public Progress(String caption) {
		this(caption, 0, 100);
	}

	/**
	 * Creates a progress bar with the given minimum and maximum range and an
	 * empty default caption.
	 * 
	 * @param minimum
	 *            the minimum range value (from)
	 * @param maximum
	 *            the maximum range value (t0)
	 */
	public Progress(int minimum, int maximum) {
		this("", minimum, maximum);
	}

	/**
	 * Creates a progress bar with given caption, minimum and maximum range.
	 * 
	 * @param caption
	 *            the text to be displayed above the progress visualization
	 * @param minimum
	 *            the minimum range value (from)
	 * @param maximum
	 *            the maximum range value (to)
	 */
	public Progress(String caption, int minimum, int maximum) {
		progress = new ProgressMonitor(MainUI.getInstance(), caption, "",
				minimum, maximum);
		progress.setMillisToDecideToPopup(0);
		progress.setMillisToPopup(0);
		val = 0;
	}

	/**
	 * Creates an empty progress bar (non-initialized).
	 */
	protected Progress() {
		progress = null;
	}

	/**
	 * Closes this progress bar.
	 */
	public void close() {
		doUpdate(new Runnable() {
			public void run() {
				if (progress != null) {
					progress.close();
				}
				progress = null;
			}
		});
	}

	/**
	 * Determines whether the user has clicked the "cancel" button of this
	 * progress dialog.
	 * 
	 * @return <code>true</code> if user has clicked "cancel",
	 *         <code>false</code> otherwise
	 */
	public boolean isCanceled() {
		return progress == null ? false : progress.isCanceled();
	}

	/**
	 * Sets the maximum range value of the progress bar ('to' value).
	 * 
	 * @param m
	 *            the number towards the progress will move
	 */
	public void setMaximum(int m) {
		final int v = m;

		doUpdate(new Runnable() {
			public void run() {
				progress.setMaximum(v);
			}
		});
	}

	/**
	 * Retrieves the current maximum range value of this progress bar.
	 * 
	 * @return the current maximum value
	 */
	public int getMaximum() {
		return progress == null ? 0 : progress.getMaximum();
	}

	/**
	 * Sets the minimum range value of the progress bar ('from' value)
	 * 
	 * @param m
	 *            the number from which the progress will move
	 */
	public void setMinimum(int m) {
		final int v = m;

		doUpdate(new Runnable() {
			public void run() {
				progress.setMinimum(v);
			}
		});
	}

	/**
	 * Sets both the minimum and maximum range value of the progress bar ('from'
	 * and 'to' values).
	 * 
	 * @param min
	 *            the number from which the progress will move
	 * @param max
	 *            the number towards the progress will move
	 */
	public void setMinMax(int min, int max) {
		final int v1 = min, v2 = max;

		doUpdate(new Runnable() {
			public void run() {
				progress.setMinimum(v1);
				progress.setMaximum(v2);
			}
		});
	}

	/**
	 * Sets the text to be displayed in the progress dialog. Can be used to give
	 * more detailed feedback to the user than the progress visualization (e.g.,
	 * what exactly is currently happening).
	 * 
	 * @param note
	 *            the updated text to be displayed in progress dialog
	 */
	public void setNote(String note) {
		final String s = note;

		doUpdate(new Runnable() {
			public void run() {
				progress.setNote(s);
			}
		});
	}

	public String getNote() {
		return progress == null ? "" : progress.getNote();
	}

	/**
	 * Sets the progress status to the given value. This will move the progress
	 * bar to the new status position (relative to the minimum and maximum range
	 * values).
	 * 
	 * @param nv
	 *            the new progress status
	 */
	public void setProgress(int nv) {
		final int v = nv;
		doUpdate(new Runnable() {
			public void run() {
				progress.setProgress(v);
				val = v;
			}
		});
	}

	/**
	 * Increments the progress status by exactly 1.
	 * 
	 * @see Progress#setProgress(int)
	 */
	public void inc() {
		setProgress(val + 1);
	}

	/**
	 * TODO: comment what this method exactly does (and when it should be used)
	 * 
	 * @param r
	 */
	protected void doUpdate(Runnable r) {
		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InvocationTargetException e1) {
				System.err.println(e1);
			} catch (InterruptedException e2) {
				System.err.println(e2);
			}
		}
	}
}
