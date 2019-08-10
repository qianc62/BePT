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

package org.processmining.framework.models.petrinet;

import org.processmining.framework.models.*;
import qc.common.Common;

/**
 * <p>
 * Title: PN Node
 * </p>
 * 
 * <p>
 * Description: Superclass of Transition and Place
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
 * @author Eric Verbeek
 * @version 1.0
 */
abstract public class PNNode extends ModelGraphVertex {
//	//By QC
//	public String qc_shapeType = Common.NormalNode;
//	public String qc_dyedType = Common.NormalNode;
//	public int qc_id = -1;
//	public String qc_label = "";

	public PNNode(PetriNet net) {
		super(net);
	}
}
