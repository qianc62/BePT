package org.processmining.mining.bpel;

import java.util.ArrayList;

import org.processmining.framework.models.bpel.BPEL;
import org.processmining.framework.models.bpel.BPELActivity;
import org.processmining.framework.models.bpel.BPELAssign;
import org.processmining.framework.models.bpel.BPELEmpty;
import org.processmining.framework.models.bpel.BPELFlow;
import org.processmining.framework.models.bpel.BPELInvoke;
import org.processmining.framework.models.bpel.BPELPick;
import org.processmining.framework.models.bpel.BPELProcess;
import org.processmining.framework.models.bpel.BPELReceive;
import org.processmining.framework.models.bpel.BPELReply;
import org.processmining.framework.models.bpel.BPELScope;
import org.processmining.framework.models.bpel.BPELSequence;
import org.processmining.framework.models.bpel.BPELSwitch;
import org.processmining.framework.models.bpel.BPELWait;
import org.processmining.framework.models.bpel.BPELWhile;
import org.processmining.framework.models.bpel.visit.BPELVisitor;

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
public class BPELConnectablesVisitor extends BPELVisitor {
	/**
	 * The instance of this BPEL4WSHierarchyVisitor
	 */
	private static final BPELConnectablesVisitor instance = new BPELConnectablesVisitor();

	private static ArrayList list;

	public BPELConnectablesVisitor() {
	}

	public static synchronized void Build(BPEL model, ArrayList list) {
		instance.list = list;
		model.getProcess().acceptVisitor(instance);
	}

	public void visitProcess(BPELProcess process) {
		process.getActivity().acceptVisitor(this);
	}

	public void visitScope(BPELScope scopeActivity) {
		scopeActivity.getActivity().acceptVisitor(this);
	}

	public void visitSequence(BPELSequence sequenceActivity) {
		for (BPELActivity activity : sequenceActivity.getActivities()) {
			activity.acceptVisitor(this);
		}
	}

	public void visitReceive(BPELReceive receiveActivity) {
		list.add(receiveActivity);
	}

	public void visitEmpty(BPELEmpty emptyActivity) {
	}

	public void visitReply(BPELReply replyActivity) {
		list.add(replyActivity);
	}

	public void visitWhile(BPELWhile whileActivity) {
		whileActivity.getActivity().acceptVisitor(this);
	}

	public void visitSwitch(BPELSwitch switchActivity) {
		for (BPELActivity activity : switchActivity.getCases().keySet()) {
			activity.acceptVisitor(this);
		}
	}

	public void visitFlow(BPELFlow flowActivity) {
		for (BPELActivity activity : flowActivity.getActivities()) {
			activity.acceptVisitor(this);
		}
	}

	public void visitInvoke(BPELInvoke invokeActivity) {
		list.add(invokeActivity);
	}

	public void visitPick(BPELPick pickActivity) {
		for (BPELActivity activity : pickActivity.messages.keySet()) {
			activity.acceptVisitor(this);
		}
	}

	public void visitAssign(BPELAssign assignActivity) {
	}

	public void visitWait(BPELWait waitActivity) {
	}
}
