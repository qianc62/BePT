package org.processmining.framework.models.fsm;

import org.processmining.framework.models.ModelGraphVertex;

/**
 * <p>
 * Title: FSM state
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 * 
 *          Code rating: Red
 * 
 *          Review rating: Red
 */
public class FSMState extends ModelGraphVertex {
	private String label;
	private FSMPayload payload;

	public FSMState(FSM fsm, String label, FSMPayload payload) {
		super(fsm);
		this.label = label;
		this.payload = payload;
		setDotAttribute("label", label);
	}

	public FSMState(FSM fsm, String label) {
		super(fsm);
		this.label = label;
		this.payload = null;
		setDotAttribute("label", label);
	}

	public void setLabel(String label) {
		this.label = label;
		setDotAttribute("label", label);
	}

	public String getLabel() {
		return label;
	}

	public void setPayload(FSMPayload payload) {
		this.payload = payload;
	}

	public FSMPayload getPayload() {
		return payload;
	}
}
