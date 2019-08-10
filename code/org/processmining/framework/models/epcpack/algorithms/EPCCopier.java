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

package org.processmining.framework.models.epcpack.algorithms;

import java.util.*;

import org.processmining.framework.models.epcpack.*;
import att.grappa.Element;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class EPCCopier {
	public static ConfigurableEPC copy(ConfigurableEPC org, HashMap orgToDup,
			HashMap dupToOrg) {
		ConfigurableEPC copy = new ConfigurableEPC();

		orgToDup.clear();
		dupToOrg.clear();
		Iterator it = org.getVerticeList().iterator();
		while (it.hasNext()) {
			EPCObject o = (EPCObject) it.next();
			if (o instanceof EPCSubstFunction) {
				EPCFunction f = copy.addFunction(new EPCSubstFunction(
						((EPCSubstFunction) o).getLogEvent(), ((EPCFunction) o)
								.isConfigurable(), copy, ((EPCSubstFunction) o)
								.getSubstitutedEPC()));
				orgToDup.put(o.getIdKey(), f);
				dupToOrg.put(f.getIdKey(), o);
				duplicateFunctionObjects((EPCFunction) o, f);
			} else if (o instanceof EPCFunction) {
				EPCFunction f = copy.addFunction(new EPCFunction(
						((EPCFunction) o).getLogEvent(), ((EPCFunction) o)
								.isConfigurable(), copy));
				orgToDup.put(o.getIdKey(), f);
				dupToOrg.put(f.getIdKey(), o);
				duplicateFunctionObjects((EPCFunction) o, f);
			} else if (o instanceof EPCEvent) {
				EPCObject f = copy.addEvent(new EPCEvent(((EPCEvent) o)
						.getIdentifier(), copy));
				orgToDup.put(o.getIdKey(), f);
				dupToOrg.put(f.getIdKey(), o);
			} else if (o instanceof EPCConnector) {
				EPCObject f = copy.addConnector(new EPCConnector(
						((EPCConnector) o).getType(), copy));
				orgToDup.put(o.getIdKey(), f);
				dupToOrg.put(f.getIdKey(), o);
			}
			if (orgToDup.get(o.getIdKey()) != null) {
				((EPCObject) orgToDup.get(o.getIdKey())).setIdentifier(o
						.getIdentifier());
			}
		}

		it = org.getEdges().iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (!(o instanceof EPCEdge)) {
				continue;
			}
			EPCEdge e = (EPCEdge) o;
			EPCEdge e2 = (EPCEdge) copy.addEdge(new EPCEdge(
					(EPCObject) orgToDup.get(e.getSource().getIdKey()),
					(EPCObject) orgToDup.get(e.getDest().getIdKey())));
			orgToDup.put(e.getIdKey(), e2);
			dupToOrg.put(e2.getIdKey(), e);
		}

		return copy;
	}

	private static void duplicateFunctionObjects(EPCFunction source,
			EPCFunction dest) {
		for (int i = 0; i < source.getNumDataObjects(); i++) {
			dest.addDataObject(new EPCDataObject(source.getDataObject(i)
					.getLabel(), dest));
		}
		for (int i = 0; i < source.getNumOrgObjects(); i++) {
			dest.addOrgObject(new EPCOrgObject(source.getOrgObject(i)
					.getLabel(), dest));
		}
		for (int i = 0; i < source.getNumInfSysObjects(); i++) {
			dest.addInfSysObject(new EPCInfSysObject(source.getInfSysObject(i)
					.getLabel(), dest));
		}

	}
}
