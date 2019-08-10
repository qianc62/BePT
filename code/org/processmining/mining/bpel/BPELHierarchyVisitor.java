package org.processmining.mining.bpel;

import java.util.Stack;

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
public class BPELHierarchyVisitor extends BPELVisitor {
	private static final BPELHierarchyVisitor instance = new BPELHierarchyVisitor();

	private static Stack<Object> stack = new Stack<Object>();

	private static BPELHierarchy hierarchy;

	private BPELHierarchyVisitor() {
	}

	public static synchronized void Build(BPEL model, BPELHierarchy hierarchy) {
		instance.hierarchy = hierarchy;
		instance.hierarchy.addHierarchyObject(model, null, "model "
				+ model.getName());
		stack.clear();
		stack.push(model);
		model.getProcess().acceptVisitor(instance);
		stack.pop();
	}

	public void visitProcess(BPELProcess process) {
		hierarchy.addHierarchyObject(process, stack.peek(), "process "
				+ process.getName());
		stack.push(process);
		process.getActivity().acceptVisitor(this);
		stack.pop();
	}

	public void visitScope(BPELScope scopeActivity) {
		hierarchy.addHierarchyObject(scopeActivity, stack.peek(), "scope "
				+ scopeActivity.getName(false));
		stack.push(scopeActivity);
		scopeActivity.getActivity().acceptVisitor(this);
		stack.pop();
	}

	public void visitSequence(BPELSequence sequenceActivity) {
		hierarchy.addHierarchyObject(sequenceActivity, stack.peek(),
				"sequence " + sequenceActivity.getName(false));
		stack.push(sequenceActivity);
		for (BPELActivity activity : sequenceActivity.getActivities()) {
			activity.acceptVisitor(this);
		}
		stack.pop();
	}

	public void visitReceive(BPELReceive receiveActivity) {
	}

	public void visitEmpty(BPELEmpty emptyActivity) {
	}

	public void visitReply(BPELReply replyActivity) {
	}

	public void visitWhile(BPELWhile whileActivity) {
		hierarchy.addHierarchyObject(whileActivity, stack.peek(), "while "
				+ whileActivity.getName(false));
		stack.push(whileActivity);
		whileActivity.getActivity().acceptVisitor(this);
		stack.pop();
	}

	public void visitSwitch(BPELSwitch switchActivity) {
		hierarchy.addHierarchyObject(switchActivity, stack.peek(), "switch "
				+ switchActivity.getName(false));
		stack.push(switchActivity);
		for (BPELActivity activity : switchActivity.getCases().keySet()) {
			activity.acceptVisitor(this);
		}
		stack.pop();
	}

	public void visitFlow(BPELFlow flowActivity) {
		hierarchy.addHierarchyObject(flowActivity, stack.peek(), "flow "
				+ flowActivity.getName(false));
		stack.push(flowActivity);
		for (BPELActivity activity : flowActivity.getActivities()) {
			activity.acceptVisitor(this);
		}
		stack.pop();
	}

	public void visitInvoke(BPELInvoke invokeActivity) {
	}

	public void visitPick(BPELPick pickActivity) {
		hierarchy.addHierarchyObject(pickActivity, stack.peek(), "pick "
				+ pickActivity.getName(false));
		stack.push(pickActivity);
		for (BPELActivity activity : pickActivity.getMessages().keySet()) {
			activity.acceptVisitor(this);
		}
		for (BPELActivity activity : pickActivity.getAlarms().keySet()) {
			activity.acceptVisitor(this);
		}
		stack.pop();
	}

	public void visitAssign(BPELAssign assignActivity) {
	}

	public void visitWait(BPELWait waitActivity) {
	}
}
