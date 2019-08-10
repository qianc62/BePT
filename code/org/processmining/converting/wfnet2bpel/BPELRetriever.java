package org.processmining.converting.wfnet2bpel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
import org.processmining.framework.models.bpel.BPELStructured;
import org.processmining.framework.models.bpel.BPELSwitch;
import org.processmining.framework.models.bpel.BPELWait;
import org.processmining.framework.models.bpel.BPELWhile;
import org.processmining.framework.models.bpel.visit.BPELVisitor;

/**
 * <p>
 * Title: BPELRetriever
 * </p>
 * 
 * <p>
 * Description: Implements the visitor pattern on a BPEL process. Visitor runs
 * through the BPEL structure and returns the BPEL activity with the given
 * argument node search name. Once found the BPEL activity can be replaced by
 * another.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: University of Aarhus
 * </p>
 * 
 * @author Kristian Bisgaard Lassen (<a
 *         href="mailto:K.B.Lassen@daimi.au.dk">mailto
 *         :K.B.Lassen@daimi.au.dk</a>)
 * @version 1.0
 */
public class BPELRetriever extends BPELVisitor {

	/**
	 * An instance of this BPEL4WSRetriever
	 */
	public final static BPELRetriever instance = new BPELRetriever();

	/**
	 * Can not instansiate the BPEL4WSRetriever from outside
	 */
	private BPELRetriever() {
		//
	}

	private final Stack<BPELActivity> stack = new Stack<BPELActivity>();

	/**
	 * The name that is being searched for
	 */
	private String searchName;

	/**
	 * The activity to replace the search for activity
	 */
	private BPELActivity replacementActivity;

	/**
	 * @see type.bpel4ws.BPEL4WSVisitor#visitInvoke(type.bpel4ws.activity.Invoke)
	 */
	@Override
	public void visitInvoke(BPELInvoke invoke) {
		visitNode(invoke);
	}

	/**
	 * Visits an activity in a BPEL4WS process and tests whether or not the
	 * activity has the name that is searched for
	 * 
	 * @param An
	 *            activity in a BPEL4WS process
	 */
	private void visitNode(BPELActivity activity) {
		if (activity.getName(false).equals(searchName)) {
			stack.pop();
			stack.push(replacementActivity.cloneActivity());
		} else if (activity instanceof BPELStructured) {
			BPELStructured structured = (BPELStructured) activity;
			List<BPELActivity> newChildren = new ArrayList<BPELActivity>();
			for (BPELActivity childActivity : structured.getActivities()) {
				stack.push(childActivity);
				childActivity.acceptVisitor(this);
				newChildren.add(stack.pop());
			}
			stack.pop();
			structured.removeAllChildActivities();
			structured = structured.cloneActivity();
			stack.push(structured);
			for (BPELActivity child : newChildren) {
				structured.appendChildActivity(child.cloneActivity());
			}
			structured.hookupActivities();
		}
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitor#visitSequence(type.bpel4ws.activity.Sequence)
	 */
	@Override
	public void visitSequence(BPELSequence sequence) {
		visitNode(sequence);
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitor#visitReceive(type.bpel4ws.activity.Receive)
	 */
	@Override
	public void visitReceive(BPELReceive receive) {
		visitNode(receive);
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitor#visitEmpty(type.bpel4ws.activity.Empty)
	 */
	@Override
	public void visitEmpty(BPELEmpty empty) {
		visitNode(empty);
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitor#visitReply(type.bpel4ws.activity.Reply)
	 */
	@Override
	public void visitReply(BPELReply reply) {
		visitNode(reply);
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitor#visitWhile(type.bpel4ws.activity.While)
	 */
	@Override
	public void visitWhile(BPELWhile while1) {
		visitNode(while1);
	}

	public void visitScope(BPELScope scope) {
		visitNode(scope);
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitor#visitSwitch(type.bpel4ws.activity.Switch)
	 */
	@Override
	public void visitSwitch(BPELSwitch switch1) {
		visitNode(switch1);
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitor#visitFlow(type.bpel4ws.activity.Flow)
	 */
	@Override
	public void visitFlow(BPELFlow flow) {
		visitNode(flow);
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitor#visitPick(type.bpel4ws.activity.Pick)
	 */
	@Override
	public void visitPick(BPELPick pick) {
		visitNode(pick);
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitor#visitAssign(type.bpel4ws.activity.Assign)
	 */
	@Override
	public void visitAssign(BPELAssign assign) {
		visitNode(assign);
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitor#visitWait(type.bpel4ws.activity.Wait)
	 */
	@Override
	public void visitWait(BPELWait wait) {
		visitNode(wait);
	}

	/**
	 * Finds activities in another activity and each activity that has the same
	 * name as the argument to this method.
	 * 
	 * @param name
	 * @param activity
	 * @param replacement
	 * @return An activity where each activity contained in the input activity
	 *         replaced by the replacement
	 */
	public synchronized BPELActivity replaceActivity(String name,
			BPELActivity activity, BPELActivity replacement) {
		searchName = name;
		replacementActivity = replacement;
		stack.clear();
		stack.push(activity);
		activity.acceptVisitor(instance);
		return stack.pop();
	}

	/**
	 * @see org.processmining.exporting.bpel4ws.type.BPEL4WSVisitor#visitProcess(org.processmining.exporting.bpel4ws.type.Process)
	 */
	@Override
	public void visitProcess(BPELProcess process) {
		// do nothing
	}

}
