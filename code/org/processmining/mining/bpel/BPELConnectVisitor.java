package org.processmining.mining.bpel;

import java.util.HashMap;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
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
public class BPELConnectVisitor extends BPELVisitor {
	/**
	 * The instance of this BPEL4WSHierarchyVisitor
	 */
	private static final BPELConnectVisitor instance = new BPELConnectVisitor();

	private static LogReader log;
	private static HashMap map;

	public BPELConnectVisitor() {
	}

	public static synchronized void Build(BPEL model, LogReader log, HashMap map) {
		instance.log = log;
		instance.map = map;
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
		Object[] objects = (Object[]) map.get(receiveActivity);
		receiveActivity.setLogEvent((LogEvent) objects[0]);
		receiveActivity.getVertex().setIdentifier((String) objects[1]);
	}

	public void visitEmpty(BPELEmpty emptyActivity) {
	}

	public void visitReply(BPELReply replyActivity) {
		Object[] objects = (Object[]) map.get(replyActivity);
		replyActivity.setLogEvent((LogEvent) objects[0]);
		replyActivity.getVertex().setIdentifier((String) objects[1]);
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
		Object[] objects = (Object[]) map.get(invokeActivity);
		invokeActivity.setLogEvent((LogEvent) objects[0]);
		invokeActivity.getVertex().setIdentifier((String) objects[1]);
	}

	public void visitPick(BPELPick pickActivity) {
		for (BPELActivity activity : pickActivity.getMessages().keySet()) {
			activity.acceptVisitor(this);
		}
	}

	public void visitAssign(BPELAssign assignActivity) {
	}

	public void visitWait(BPELWait waitActivity) {
	}
}
