package org.processmining.framework.models.fsm;

import java.util.Comparator;

/**
 * <p>
 * Title: FSMPayload
 * </p>
 * 
 * <p>
 * Description: Payload for an FSMState
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
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
public abstract class FSMPayload implements Comparable {
	public abstract FSMPayload merge(FSMPayload payload);
}
