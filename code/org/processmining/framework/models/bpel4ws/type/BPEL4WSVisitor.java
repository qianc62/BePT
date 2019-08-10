package org.processmining.framework.models.bpel4ws.type;

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
 * @author Kristian Bisgaard Lassen
 * 
 */
public abstract class BPEL4WSVisitor {

	/**
	 * @param o
	 */
	public void visit(Object o) {
		if (o instanceof BPEL4WSProcess)
			visitProcess((BPEL4WSProcess) o);
		else if (o instanceof Invoke)
			visitInvoke((Invoke) o);
		else if (o instanceof Sequence)
			visitSequence((Sequence) o);
		else if (o instanceof Receive)
			visitReceive((Receive) o);
		else if (o instanceof Empty)
			visitEmpty((Empty) o);
		else if (o instanceof Reply)
			visitReply((Reply) o);
		else if (o instanceof While)
			visitWhile((While) o);
		else if (o instanceof Switch)
			visitSwitch((Switch) o);
		else if (o instanceof Flow)
			visitFlow((Flow) o);
		else if (o instanceof Pick)
			visitPick((Pick) o);
		else if (o instanceof Assign)
			visitAssign((Assign) o);
		else if (o instanceof Wait)
			visitWait((Wait) o);
	}

	/**
	 * @param process
	 */
	public abstract void visitProcess(BPEL4WSProcess process);

	/**
	 * @param invoke
	 */
	public abstract void visitInvoke(Invoke invoke);

	/**
	 * @param sequence
	 */
	public abstract void visitSequence(Sequence sequence);

	/**
	 * @param receive
	 */
	public abstract void visitReceive(Receive receive);

	/**
	 * @param empty
	 */
	public abstract void visitEmpty(Empty empty);

	/**
	 * @param reply
	 */
	public abstract void visitReply(Reply reply);

	/**
	 * @param while1
	 */
	public abstract void visitWhile(While while1);

	/**
	 * @param switch1
	 */
	public abstract void visitSwitch(Switch switch1);

	/**
	 * @param flow
	 */
	public abstract void visitFlow(Flow flow);

	/**
	 * @param pick
	 */
	public abstract void visitPick(Pick pick);

	/**
	 * @param assign
	 */
	public abstract void visitAssign(Assign assign);

	/**
	 * @param wait
	 */
	public abstract void visitWait(Wait wait);
}
