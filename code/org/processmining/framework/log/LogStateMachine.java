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

package org.processmining.framework.log;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Captures the state machine on log events and provides some helper methods.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public class LogStateMachine {

	public final static String SCHEDULE = "schedule";
	public final static String ASSIGN = "assign";
	public final static String REASSIGN = "reassign";
	public final static String START = "start";
	public final static String AUTOSKIP = "autoskip";
	public final static String MANUALSKIP = "manualskip";
	public final static String PI_ABORT = "pi_abort";
	public final static String ATE_ABORT = "ate_abort";
	public final static String WITHDRAW = "withdraw";
	public final static String COMPLETE = "complete";
	public final static String SUSPEND = "suspend";
	public final static String RESUME = "resume";
	public final static String UNKNOWN = " ";

	public final static int ORD_SCHEDULE = 0;
	public final static int ORD_ASSIGN = 1;
	public final static int ORD_REASSIGN = 2;
	public final static int ORD_START = 3;
	public final static int ORD_AUTOSKIP = 4;
	public final static int ORD_MANUALSKIP = 5;
	public final static int ORD_PI_ABORT = 6;
	public final static int ORD_ATE_ABORT = 7;
	public final static int ORD_WITHDRAW = 8;
	public final static int ORD_COMPLETE = 9;
	public final static int ORD_SUSPEND = 10;
	public final static int ORD_RESUME = 11;

	public final static String[] EVENT_TYPES = { SCHEDULE, ASSIGN, REASSIGN,
			START, AUTOSKIP, MANUALSKIP, PI_ABORT, ATE_ABORT, WITHDRAW,
			COMPLETE, SUSPEND, RESUME };

	public final static String INITIALIZED = "initialized";
	public final static String SCHEDULED = "scheduled";
	public final static String ASSIGNED = "assigned";
	public final static String STARTED = "started";
	public final static String COMPLETED = "completed";
	public final static String SUSPENDED = "suspended";
	public final static String ABORTED = "aborted";

	public final static int ORD_INITIALIZED = 0;
	public final static int ORD_SCHEDULED = 1;
	public final static int ORD_ASSIGNED = 2;
	public final static int ORD_STARTED = 3;
	public final static int ORD_COMPLETED = 4;
	public final static int ORD_SUSPENDED = 5;
	public final static int ORD_ABORTED = 6;

	public final static String[] STATE_TYPES = { INITIALIZED, SCHEDULED,
			ASSIGNED, STARTED, COMPLETED, SUSPENDED, ABORTED };

	private TreeSet eventTypes = new TreeSet();

	/**
	 * Return true if the given string equals any of the constants SCHEDULE,
	 * ASSIGN, REASSIGN, START, AUTOSKIP, MANUALSKIP, PI_ABORT, ATE_ABORT,
	 * WITHDRAW, COMPLETE, SUSPEND, RESUME
	 * 
	 * @param evt
	 *            String possible event name to check
	 * @return boolean true if the event name is in the list of constants
	 */
	public boolean isPossibleEvent(String evt) {
		return eventTypes.contains(evt);
	}

	public static boolean inPreset(int s, int e, boolean tc) {
		if (tc) {
			if (s == ORD_SCHEDULED) {
				return e == ORD_SCHEDULE;
			} else if (s == ORD_ASSIGNED) {
				return e == ORD_SCHEDULE || e == ORD_ASSIGN;
			} else if (s == ORD_STARTED || s == ORD_SUSPENDED) {
				return e == ORD_SCHEDULE || e == ORD_ASSIGN
						|| e == ORD_REASSIGN || e == ORD_START;
			} else if (s == ORD_COMPLETED) {
				return e != ORD_PI_ABORT && e != ORD_ATE_ABORT
						&& e != ORD_WITHDRAW;
			} else if (s == ORD_ABORTED) {
				return e != ORD_AUTOSKIP && e != ORD_MANUALSKIP
						&& e != ORD_COMPLETE;
			} else {
				return false;
			}
		} else {
			if (s == ORD_SCHEDULED) {
				return e == ORD_SCHEDULE;
			} else if (s == ORD_ASSIGNED) {
				return e == ORD_ASSIGN;
			} else if (s == ORD_STARTED) {
				return e == ORD_START;
			} else if (s == ORD_SUSPENDED) {
				return e == ORD_SUSPEND;
			} else if (s == ORD_COMPLETED) {
				return e == ORD_COMPLETED || e == ORD_AUTOSKIP
						|| e == ORD_MANUALSKIP;
			} else if (s == ORD_ABORTED) {
				return e == ORD_PI_ABORT || e == ORD_ATE_ABORT
						|| e == ORD_WITHDRAW;
			} else {
				return false;
			}
		}
	}

	public static boolean inPostset(int s, int e, boolean tc) {
		if (tc) {
			if (s == ORD_INITIALIZED) {
				return true;
			} else if (s == ORD_SCHEDULED) {
				return e != ORD_SCHEDULE && e != ORD_AUTOSKIP;
			} else if (s == ORD_ASSIGNED) {
				return e != ORD_SCHEDULE && e != ORD_ASSIGN
						&& e != ORD_REASSIGN && e != ORD_AUTOSKIP;
			} else if (s == ORD_STARTED || s == ORD_SUSPENDED) {
				return e == ORD_COMPLETE || e == ORD_PI_ABORT
						|| e == ORD_ATE_ABORT;
			} else {
				return false;
			}
		} else {
			if (s == ORD_INITIALIZED) {
				return e == ORD_SCHEDULE || e == ORD_AUTOSKIP
						|| e == ORD_PI_ABORT;
			} else if (s == ORD_SCHEDULED) {
				return e != ORD_ASSIGN || e == ORD_MANUALSKIP
						|| e == ORD_PI_ABORT || e == ORD_WITHDRAW;
			} else if (s == ORD_ASSIGNED) {
				return e == ORD_START || e == ORD_MANUALSKIP
						|| e == ORD_PI_ABORT || e == ORD_WITHDRAW;
			} else if (s == ORD_STARTED) {
				return e == ORD_COMPLETE || e == ORD_PI_ABORT
						|| e == ORD_ATE_ABORT;
			} else if (s == ORD_SUSPENDED) {
				return e == ORD_RESUME || e == ORD_PI_ABORT
						|| e == ORD_ATE_ABORT;
			} else {
				return false;
			}
		}
	}

	private HashMap<String, String[]> occursAfter;

	private static LogStateMachine instance = new LogStateMachine();

	private LogStateMachine() {
		occursAfter = new HashMap<String, String[]>();
		occursAfter
				.put(SCHEDULE, new String[] { ASSIGN, REASSIGN, MANUALSKIP,
						WITHDRAW, PI_ABORT, START, RESUME, SUSPEND, COMPLETE,
						ATE_ABORT });
		occursAfter.put(ASSIGN, new String[] { REASSIGN, MANUALSKIP, WITHDRAW,
				PI_ABORT, START, RESUME, SUSPEND, COMPLETE, ATE_ABORT });
		occursAfter.put(REASSIGN,
				new String[] { REASSIGN, MANUALSKIP, WITHDRAW, PI_ABORT, START,
						RESUME, SUSPEND, COMPLETE, ATE_ABORT });
		occursAfter.put(START, new String[] { PI_ABORT, RESUME, SUSPEND,
				COMPLETE, ATE_ABORT });
		occursAfter.put(SUSPEND, new String[] { PI_ABORT, RESUME, SUSPEND,
				COMPLETE, ATE_ABORT });
		occursAfter.put(RESUME, new String[] { PI_ABORT, RESUME, SUSPEND,
				COMPLETE, ATE_ABORT });
		occursAfter.put(AUTOSKIP, new String[] {});
		occursAfter.put(PI_ABORT, new String[] {});
		occursAfter.put(ATE_ABORT, new String[] {});
		occursAfter.put(WITHDRAW, new String[] {});
		occursAfter.put(MANUALSKIP, new String[] {});
		occursAfter.put(COMPLETE, new String[] {});

		eventTypes.add(SCHEDULE);
		eventTypes.add(ASSIGN);
		eventTypes.add(REASSIGN);
		eventTypes.add(START);
		eventTypes.add(AUTOSKIP);
		eventTypes.add(MANUALSKIP);
		eventTypes.add(PI_ABORT);
		eventTypes.add(WITHDRAW);
		eventTypes.add(COMPLETE);
		eventTypes.add(ATE_ABORT);
		eventTypes.add(SUSPEND);
		eventTypes.add(RESUME);

		/*
		 * public final static String SCHEDULE = "schedule"; public final static
		 * String ASSIGN = "assign"; public final static String REASSIGN =
		 * "reassign"; public final static String START = "start"; public final
		 * static String AUTOSKIP = "autoskip"; public final static String
		 * MANUALSKIP = "manualskip"; public final static String PI_ABORT =
		 * "pi_abort"; public final static String ATE_ABORT = "ate_abort";
		 * public final static String WITHDRAW = "withdraw"; public final static
		 * String COMPLETE = "complete"; public final static String SUSPEND =
		 * "suspend"; public final static String RESUME = "resume";
		 * 
		 * SMNode schedule = new SMNode(), assign = new SMNode(), start = new
		 * SMNode(); SMNode suspend = new SMNode(), autoskip = new SMNode(),
		 * pi_abort = new SMNode(); root = new SMNode(); root.put(SCHEDULE,
		 * schedule); root.put(AUTOSKIP, autoskip); root.put(PI_ABORT,
		 * pi_abort); schedule.put(ASSIGN, assign); schedule.put(WITHDRAW,
		 * pi_abort); schedule.put(PI_ABORT, pi_abort); schedule.put(MANUALSKIP,
		 * autoskip); assign.put(REASSIGN, assign); assign.put(START, start);
		 * assign.put(PI_ABORT, pi_abort); assign.put(WITHDRAW, pi_abort);
		 * assign.put(MANUALSKIP, autoskip); start.put(SUSPEND, suspend);
		 * start.put(PI_ABORT, pi_abort); start.put(ATE_ABORT, pi_abort);
		 * start.put(COMPLETE, autoskip); suspend.put(RESUME, start);
		 * suspend.put(ATE_ABORT, pi_abort); suspend.put(PI_ABORT, pi_abort);
		 */
	}

	public static LogStateMachine getInstance() {
		return instance;
	}

	public String[] possibleEventsAfter(String event) {
		return (String[]) occursAfter.get(event);
	}

	public boolean mayEventuallyOccurAfter(String eventBefore, String eventAfter) {
		String[] after = possibleEventsAfter(eventBefore);

		if (after == null) {
			return false;
		}

		for (int i = 0; i < after.length; i++) {
			if (after[i].equals(eventAfter)) {
				return true;
			}
		}
		return false;
	}
}
