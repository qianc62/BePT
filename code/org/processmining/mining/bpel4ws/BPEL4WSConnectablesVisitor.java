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

package org.processmining.mining.bpel4ws;

import java.util.ArrayList;

import org.processmining.framework.models.bpel4ws.type.BPEL4WS;
import org.processmining.framework.models.bpel4ws.type.BPEL4WSProcess;
import org.processmining.framework.models.bpel4ws.type.BPEL4WSVisitor;
import org.processmining.framework.models.bpel4ws.type.activity.Activity;
import org.processmining.framework.models.bpel4ws.type.activity.Assign;
import org.processmining.framework.models.bpel4ws.type.activity.Empty;
import org.processmining.framework.models.bpel4ws.type.activity.Flow;
import org.processmining.framework.models.bpel4ws.type.activity.Invoke;
import org.processmining.framework.models.bpel4ws.type.activity.Pick;
import org.processmining.framework.models.bpel4ws.type.activity.Receive;
import org.processmining.framework.models.bpel4ws.type.activity.Reply;
import org.processmining.framework.models.bpel4ws.type.activity.Sequence;
import org.processmining.framework.models.bpel4ws.type.activity.Switch;
import org.processmining.framework.models.bpel4ws.type.activity.Wait;
import org.processmining.framework.models.bpel4ws.type.activity.While;

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
public class BPEL4WSConnectablesVisitor extends BPEL4WSVisitor {
	/**
	 * The instance of this BPEL4WSHierarchyVisitor
	 */
	private static final BPEL4WSConnectablesVisitor instance = new BPEL4WSConnectablesVisitor();

	private static ArrayList list;

	public BPEL4WSConnectablesVisitor() {
	}

	public static synchronized void Build(BPEL4WS model, ArrayList list) {
		instance.list = list;
		model.process.acceptVisitor(instance);
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitProcess(org.processmining.exporting.bpel4ws.type.Process)
	 */
	public void visitProcess(BPEL4WSProcess process) {
		process.activity.acceptVisitor(this);
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitSequence(org.processmining.exporting.bpel4ws.type.activity.Sequence)
	 */
	public void visitSequence(Sequence sequence) {
		for (Activity activity : sequence.activities) {
			activity.acceptVisitor(this);
		}
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitReceive(org.processmining.exporting.bpel4ws.type.activity.Receive)
	 */
	public void visitReceive(Receive receive) {
		list.add(receive);
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitEmpty(org.processmining.exporting.bpel4ws.type.activity.Empty)
	 */
	public void visitEmpty(Empty empty) {
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitReply(org.processmining.exporting.bpel4ws.type.activity.Reply)
	 */
	public void visitReply(Reply reply) {
		list.add(reply);
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitWhile(org.processmining.exporting.bpel4ws.type.activity.While)
	 */
	public void visitWhile(While whileActivity) {
		whileActivity.activity.acceptVisitor(this);
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitSwitch(org.processmining.exporting.bpel4ws.type.activity.Switch)
	 */
	public void visitSwitch(Switch switch1) {
		for (Activity activity : switch1.cases.keySet()) {
			activity.acceptVisitor(this);
		}
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitFlow(org.processmining.exporting.bpel4ws.type.activity.Flow)
	 */
	public void visitFlow(Flow flow) {
		for (Activity activity : flow.activities) {
			activity.acceptVisitor(this);
		}
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitInvoke(org.processmining.exporting.bpel4ws.type.activity.Invoke)
	 */
	public void visitInvoke(Invoke invoke) {
		list.add(invoke);
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitPick(org.processmining.exporting.bpel4ws.type.activity.Pick)
	 */
	public void visitPick(Pick pick) {
		for (Activity activity : pick.messages.keySet()) {
			activity.acceptVisitor(this);
		}
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitAssign(org.processmining.exporting.bpel4ws.type.activity.Assign)
	 */
	public void visitAssign(Assign assign) {
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitWait(org.processmining.exporting.bpel4ws.type.activity.Wait)
	 */
	public void visitWait(Wait wait) {
	}
}
