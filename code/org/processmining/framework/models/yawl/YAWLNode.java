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

package org.processmining.framework.models.yawl;

import org.processmining.framework.models.*;

/**
 * <p>
 * Title: YAWL Node
 * </p>
 * 
 * <p>
 * Description: Super class for YAWLTask and YAWLCondition
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
abstract public class YAWLNode extends ModelGraphVertex {

	protected String id;
	protected String name;

	public YAWLNode(ModelGraph g, String anID) {
		super(g);
		setID(anID);
	}

	public String getID() {
		return id;
	}

	public void setID(String anID) {
		id = anID;
		int tab = id.indexOf('\t');
		name = tab < 0 ? id : id.substring(0, tab);
		setIdentifier(name);
	}
}
