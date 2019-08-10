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

package org.processmining.exporting.log.util;

import org.processmining.framework.log.ProcessInstance;

/**
 * <p>
 * Title: My Process Intance (Inteface)
 * </p>
 * <p>
 * Description: Interface that contains operations to manipulate the process
 * instances at a workflow log.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public interface MyProcessInstance {

	public int getNumberSimilarPIs();

	public void increaseNumberSimilarPIs(int increase);

	public ProcessInstance getPI();

	public void addGroupedPiIdentifier(String identifier);
}
