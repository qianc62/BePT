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
package org.processmining.framework.models.pdm;

import java.util.*;
import org.processmining.framework.models.pdm.*;

/**
 * <p>
 * Title: PDM Process Model
 * </p>
 * <p>
 * Description: Represents a process model based on a pdm.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Irene Vanderfeesten
 * @version 1.0
 */
public class PDMProcessModel {

	HashMap designs = new HashMap(); // the list of designs on the product data

	// model

	/**
	 * Creates the PDM process model.
	 */
	public PDMProcessModel() {
	}

	/**
	 * Adds a design to the list of designs.
	 * 
	 * @param design
	 *            PDMDesign
	 */
	public void addDesign(PDMDesign design) {
		designs.put(design.getID(), design);
	}

	public PDMDesign getFirstDesign() {
		Object[] ds = new Object[1];
		ds = designs.values().toArray();
		PDMDesign firstDesign = (PDMDesign) ds[1];
		return firstDesign;
	}

}
