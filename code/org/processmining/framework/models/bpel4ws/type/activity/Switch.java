/*
 * Created on 15-02-2005
 *
 */
package org.processmining.framework.models.bpel4ws.type.activity;

import java.util.LinkedHashMap;
import java.util.Map;

import org.processmining.framework.models.bpel4ws.type.BPEL4WSVisitor;
import org.processmining.framework.models.bpel4ws.type.BPEL4WS;

/**
 * @author Kristian Bisgaard Lassen
 */
public class Switch extends Composed {

	/***/
	public final Map<Activity, String> cases;

	/**
	 * @param name
	 * @param cases
	 */
	public Switch(String name, Map<Activity, String> cases) {
		super(name);
		this.cases = cases;
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitable#acceptVisitor(type.bpel4ws.BPEL4WSVisitor)
	 */
	public void acceptVisitor(BPEL4WSVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @see type.bpel4ws.activity.Activity#cloneActivity()
	 */
	@Override
	public Activity cloneActivity() {
		Map<Activity, String> clonedCases = new LinkedHashMap<Activity, String>();
		for (Activity activity : cases.keySet())
			clonedCases.put(activity.cloneActivity(), cases.get(activity));
		return new Switch(name, cases);
	}

	public String writeToDot(BPEL4WS model) {
		String s = "";
		for (Activity activity : cases.keySet()) {
			s += "    n" + this + " [label=\"" + name + "];\n";
		}
		return s;
	}
}
