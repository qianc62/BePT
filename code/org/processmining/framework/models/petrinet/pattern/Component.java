package org.processmining.framework.models.petrinet.pattern;

import org.processmining.framework.models.petrinet.PetriNet;

/**
 * <p>
 * Title: Component
 * </p>
 * 
 * <p>
 * Description: Implements a component. The fields set in the component
 * correspond to the information that is present when the component was found.
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
public abstract class Component {

	/**
	 * The actual Petri net of this component
	 */
	private final PetriNet wfnet;

	/**
	 * This is the component of the library component that this Component match.
	 * If the type of this Component is not LIBARARY then this is null
	 */

	/**
	 * @param type
	 * @param wfnet
	 */
	public Component(PetriNet wfnet) {
		this.wfnet = (PetriNet) wfnet.clone();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();

	public abstract Component cloneComponent();

	/**
	 * @return the wfnet
	 */
	public PetriNet getWfnet() {
		return wfnet;
	}

}
