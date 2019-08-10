package org.processmining.framework.util;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.TreeSet;

import lpsolve.AbortListener;
import lpsolve.LogListener;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import org.processmining.framework.ui.Message;

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
public class ProMLpSolve {

	public final static int EQ = LpSolve.EQ;
	public final static int LE = LpSolve.LE;
	public final static int GE = LpSolve.GE;

	public final static int PRESOLVE_NONE = LpSolve.PRESOLVE_NONE;
	public final static int PRESOLVE_ROWS = LpSolve.PRESOLVE_ROWS;
	public final static int PRESOLVE_COLS = LpSolve.PRESOLVE_COLS;
	public final static int PRESOLVE_LINDEP = LpSolve.PRESOLVE_LINDEP;
	public final static int PRESOLVE_SOS = LpSolve.PRESOLVE_SOS;
	public final static int PRESOLVE_REDUCEMIP = LpSolve.PRESOLVE_REDUCEMIP;
	public final static int PRESOLVE_KNAPSACK = LpSolve.PRESOLVE_KNAPSACK;
	public final static int PRESOLVE_ELIMEQ2 = LpSolve.PRESOLVE_ELIMEQ2;
	public final static int PRESOLVE_IMPLIEDFREE = LpSolve.PRESOLVE_IMPLIEDFREE;
	public final static int PRESOLVE_REDUCEGCD = LpSolve.PRESOLVE_REDUCEGCD;
	public final static int PRESOLVE_PROBEFIX = LpSolve.PRESOLVE_PROBEFIX;
	public final static int PRESOLVE_PROBEREDUCE = LpSolve.PRESOLVE_PROBEREDUCE;
	public final static int PRESOLVE_ROWDOMINATE = LpSolve.PRESOLVE_ROWDOMINATE;
	public final static int PRESOLVE_COLDOMINATE = LpSolve.PRESOLVE_COLDOMINATE;
	public final static int PRESOLVE_MERGEROWS = LpSolve.PRESOLVE_MERGEROWS;
	public final static int PRESOLVE_IMPLIEDSLK = LpSolve.PRESOLVE_IMPLIEDSLK;
	public final static int PRESOLVE_COLFIXDUAL = LpSolve.PRESOLVE_COLFIXDUAL;
	public final static int PRESOLVE_BOUNDS = LpSolve.PRESOLVE_BOUNDS;
	public final static int PRESOLVE_DUALS = LpSolve.PRESOLVE_DUALS;
	public final static int PRESOLVE_SENSDUALS = LpSolve.PRESOLVE_SENSDUALS;

	public final static int NEUTRAL = LpSolve.NEUTRAL;
	public final static int CRITICAL = LpSolve.CRITICAL;
	public final static int SEVERE = LpSolve.SEVERE;
	public final static int IMPORTANT = LpSolve.IMPORTANT;
	public final static int NORMAL = LpSolve.NORMAL;
	public final static int DETAILED = LpSolve.DETAILED;
	public final static int FULL = LpSolve.FULL;

	private final LpSolve problem;
	private int orgColumns;
	private static boolean loaded = false;

	private void loadLibrary() {
		String path;
		if (System.getProperty("os.name", "").toLowerCase().startsWith(
				"windows")) {
			// Run on a Windows machine
			path = System.getProperty("user.dir")
					+ System.getProperty("file.separator") + "lib"
					+ System.getProperty("file.separator") + "external"
					+ System.getProperty("file.separator");

			// Load libraries required by LpSolve.
			System.load(path + "lpsolve55.dll");

		} else if (System.getProperty("os.name", "").toLowerCase().startsWith(
				"linux")) {
			// Run on a Linux machine.
			path = System.getProperty("user.dir")
					+ System.getProperty("file.separator") + "lib"
					+ System.getProperty("file.separator") + "external"
					+ System.getProperty("file.separator");

			// Load libraries required by LpSolve.
			System.load(path + "liblpsolve55.so");
			System.load(path + "liblpsolve55j.so");
		}
		Message.add("Using LpSolve version "
				+ LpSolve.lpSolveVersion().getMajorversion() + "."
				+ LpSolve.lpSolveVersion().getMinorversion() + "."
				+ LpSolve.lpSolveVersion().getRelease() + "."
				+ LpSolve.lpSolveVersion().getBuild(), Message.DEBUG);
		loaded = true;
	}

	public ProMLpSolve(int rows, int columns) throws ProMLpSolveException {
		try {
			if (!loaded) {
				loadLibrary();
			}
			problem = LpSolve.makeLp(rows, columns);
			problem.setVerbose(0);
			// Write to Message
			problem.putLogfunc(new LogListener() {
				public void logfunc(LpSolve lpSolve, Object object,
						String string) throws LpSolveException {
					Message.add(string, Message.DEBUG);
				}

			}, null);
			// Don't write to System.out
			problem.setOutputfile("");
			orgColumns = columns;

			// Initialize the array of column labels
			colNames = new String[columns + 1];
			Arrays.fill(colNames, "");
			// initialize the org2new array
			org2new = new int[columns + 1];
			for (int i = 0; i < columns + 1; i++) {
				org2new[i] = i;
			}
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void setMaximizing() {
		problem.setMaxim();
	}

	AbortListener abortListener = null;
	Object abortListenerObject = null;

	public void putAbortListener(AbortListener listener, Object object)
			throws ProMLpSolveException {
		try {
			abortListener = listener;
			abortListenerObject = object;
			problem.putAbortfunc(listener, object);
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void setVerbose(int v) {
		problem.setVerbose(v);
	}

	public void setBounds(int i, double low, double up)
			throws ProMLpSolveException {
		try {
			if (presolved) {
				i = org2new[i];
			}
			if (i > 0) {
				problem.setBounds(i, low, up);
			}
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	private String[] colNames;
	private int[] org2new;
	private boolean presolved = false;

	public void setColName(int i, String name) throws ProMLpSolveException {
		try {
			if (presolved) {
				i = org2new[i];
			}
			if (i > 0) {
				problem.setColName(i, name);
				colNames[i] = name;
			}
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void setBinary(int i, boolean set) throws ProMLpSolveException {
		try {
			if (presolved) {
				i = org2new[i];
			}
			if (i > 0) {
				problem.setBinary(i, set);
			}
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void setInt(int i, boolean set) throws ProMLpSolveException {
		try {
			if (presolved) {
				i = org2new[i];
			}
			if (i > 0) {
				problem.setInt(i, set);
			}
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void setLowbo(int i, double low) throws ProMLpSolveException {
		try {
			if (presolved) {
				i = org2new[i];
			}
			if (i > 0) {
				problem.setLowbo(i, low);
			}
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void setUpbo(int i, double up) throws ProMLpSolveException {
		try {
			if (presolved) {
				i = org2new[i];
			}
			if (i > 0) {
				problem.setUpbo(i, up);
			}
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public int getNcolumns() {
		return problem.getNcolumns();
	}

	public int getNrows() {
		return problem.getNrows();
	}

	public void setAddRowmode(boolean addMode) {
		problem.setAddRowmode(addMode);
	}

	public void strSetObjFn(String constr) throws ProMLpSolveException {
		try {
			problem.strSetObjFn(constr);
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}

	}

	public void strAddConstraint(String constraint, int type, double val)
			throws ProMLpSolveException {
		try {
			if (presolved) {
				StringTokenizer st = new StringTokenizer(constraint, " ");
				double[] newConstraint = new double[problem.getNcolumns() + 1];
				int orgColNr = 1;
				while (st.hasMoreTokens()) {
					double currentVal = Double.parseDouble(st.nextToken());
					int newIndex = org2new[orgColNr++];
					if (newIndex > 0) {
						newConstraint[newIndex] = currentVal;
					}
				}
				String cs = "";
				for (int i = 1; i < newConstraint.length; i++) {
					cs += " " + newConstraint[i];
				}
				constraint = cs;
			}
			problem.strAddConstraint(constraint, type, val);
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void setPresolve(int presolveOptions, int presolveLoops) {
		problem.setPresolve(presolveOptions, presolveLoops);
	}

	public int getPresolveloops() {
		return problem.getPresolveloops();
	}

	public int solve() throws ProMLpSolveException {
		try {
			int s = problem.solve();
			// See if there is a change in columns, and address that in
			// the mapping
			if (!presolved
					&& (problem.getNcolumns() != problem.getNorigColumns() || problem
							.getNrows() != problem.getNorigRows())) {
				Arrays.fill(org2new, 0);
				for (int i = 1; i < problem.getNcolumns() + 1; i++) {
					// retrieve the original column number
					int col = problem.getOrigIndex(i + problem.getNrows());
					org2new[col] = i;
					if (i != col) {
						// the mapping exists
						presolved = true;
					}
				}
			}
			// mapping;
			return s;
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public double[] getColValuesSolution() throws ProMLpSolveException {
		try {
			double[] sol = new double[orgColumns];

			double[] variables = problem.getPtrVariables();

			for (int i = 0; i < orgColumns; i++) {
				if (org2new[i + 1] > 0) {
					sol[i] = (int) Math.round(variables[org2new[i + 1] - 1]);
				} else {
					sol[i] = 0;
				}
			}
			// Message.add("solution: " + Arrays.toString(sol), Message.DEBUG);
			return sol;
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void deleteLp() {
		try {
			putAbortListener(null, null);
		} catch (ProMLpSolveException ex) {
			// Not a problem. Listener not registered anymore
			abortListener = null;
			abortListenerObject = null;
		}
		problem.deleteLp();
	}

	public double getUpbo(int i) throws ProMLpSolveException {
		try {
			return problem.getUpbo(i);
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void delConstraint(int i) throws ProMLpSolveException {
		try {
			problem.delConstraint(i);
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void updateBounds(TreeSet<int[]> bounds) throws ProMLpSolveException {
		try {
			for (int[] bound : bounds) {
				problem.setBounds(bound[0], bound[1], bound[2]);
			}
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void addConstraint(int[] constraint, int type, double val)
			throws ProMLpSolveException {
		try {
			String s = "";
			for (int i = 0; i < constraint.length; i++) {
				s += constraint[i] + " ";
			}
			problem.strAddConstraint(s, type, val);
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void addTarget(int[] target) throws ProMLpSolveException {
		try {
			String s = "";
			for (int i = 0; i < target.length; i++) {
				s += target[i] + " ";
			}
			problem.strSetObjFn(s);
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void addTarget(double[] target) throws ProMLpSolveException {
		try {
			String s = "";
			for (int i = 0; i < target.length; i++) {
				s += target[i] + " ";
			}
			problem.strSetObjFn(s);
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public boolean isErrorSolveCode(int solve) {
		switch (solve) {
		case -2: {
			Message.add("Out of Memory while solving", Message.ERROR);
			break;
		}
		case 0: {
			Message.add("Optimal solution found while solving", Message.DEBUG);
			return false;
		}
		case 1: {
			Message.add("Suboptimal solution found while solving",
					Message.ERROR);
			break;
		}
		case 2: {
			Message.add("The model is infeasible", Message.ERROR);
			break;
		}
		case 3: {
			Message.add("The model is unbounded", Message.ERROR);
			break;
		}
		case 4: {
			Message.add("The basic model is degenerative", Message.ERROR);
			break;
		}
		case 5: {
			Message.add("A numerical failure occurred", Message.ERROR);
			break;
		}
		case 6: {
			Message.add("Solver aborted", Message.ERROR);
			break;
		}
		case 7: {
			Message.add("Solver timed-out", Message.ERROR);
			break;
		}
		case 9: {
			Message.add("Solution found during preSolve", Message.DEBUG);
			return false;
		}
		case 10: {
			Message.add("Branch and Bound routine failed", Message.ERROR);
			break;
		}
		case 11: {
			Message.add("Branch and Bound routine stopped", Message.ERROR);
			break;
		}
		case 12: {
			Message.add("Found feasible B&B solution", Message.DEBUG);
			return false;
		}
		case 13: {
			Message.add("No feasible B&B solution found", Message.ERROR);
			break;
		}
		}
		// problem.printLp();
		return true;
	}

	/**
	 * resizeLp
	 */
	public void resizeLp(int rows, int cols) throws ProMLpSolveException {
		try {
			problem.resizeLp(rows, cols);
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	public void setRowName(int row, String name) throws ProMLpSolveException {
		try {
			problem.setRowName(row, name);
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

	/**
	 * isFeasibleSolution
	 * 
	 * @param vector
	 *            int[]
	 * @return boolean
	 */
	public boolean isFeasible(double[] solution) throws ProMLpSolveException {
		try {
			if (solution.length != problem.getNcolumns()) {
				return false;
			}
			int[] indices = new int[solution.length];
			for (int i = 0; i < indices.length; i++) {
				indices[i] = i + 1;
			}

			boolean valid = true;
			double val = 0;
			double rh = 0;
			int type = 0;
			int row = 0;
			double[] constr = new double[problem.getNcolumns() + 1];
			for (int i = 0; valid && (i < problem.getNrows()); i++) {
				row = i + 1;
				val = problem.getConstrValue(row, indices.length, solution,
						indices);
				type = problem.getConstrType(row);
				rh = problem.getRh(row);
				problem.getRow(row, constr);
				if (type == problem.GE) {
					// val should be >= rh
					valid = (val >= rh);
				} else if (type == problem.LE) {
					// val should be <= rh
					valid = (val <= rh);
				} else { // type == problem.EQ
					valid = (val == rh);
				}
				// check for possible cancellation (return false in that case)
				valid = valid
						&& !abortListener.abortfunc(this.problem,
								abortListenerObject);
			}
			return valid;
		} catch (LpSolveException e) {
			throw new ProMLpSolveException(e);
		}
	}

}
