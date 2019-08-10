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

import org.processmining.framework.models.*;
import org.processmining.framework.models.epcpack.*;

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

public class ConnectorStructureExtractor {

	public synchronized static ConfigurableEPC extract(ConfigurableEPC baseEPC) {
		return extract(baseEPC, true, true, true, true, true, true, true);
	}

	public synchronized static ConfigurableEPC extract(ConfigurableEPC baseEPC,
			boolean singleInSingleOut, boolean sameSplitAndJoin,
			boolean splitWithOrJoin, boolean sameSplitAndSplit,
			boolean sameJoinAndJoin, boolean xorLoop, boolean orLoop) {
		return extract(baseEPC, singleInSingleOut, sameSplitAndJoin,
				splitWithOrJoin, sameSplitAndSplit, sameJoinAndJoin, xorLoop,
				orLoop, true, true, true);
	}

	// Sometimes, we only need to extract single-in single-out connectors.
	// For this reason, I've extended the extract method with three flags:
	// connectors, functions, and events.
	// - Eric.
	public synchronized static ConfigurableEPC extract(ConfigurableEPC baseEPC,
			boolean singleInSingleOut, boolean sameSplitAndJoin,
			boolean splitWithOrJoin, boolean sameSplitAndSplit,
			boolean sameJoinAndJoin, boolean xorLoop, boolean orLoop,
			boolean connectors, boolean functions, boolean events) {
		return extract(baseEPC, singleInSingleOut, sameSplitAndJoin,
				splitWithOrJoin, sameSplitAndSplit, sameJoinAndJoin, xorLoop,
				orLoop, connectors, functions, events, new HashMap());
	}

	public synchronized static ConfigurableEPC extract(ConfigurableEPC baseEPC,
			boolean singleInSingleOut, boolean sameSplitAndJoin,
			boolean splitWithOrJoin, boolean sameSplitAndSplit,
			boolean sameJoinAndJoin, boolean xorLoop, boolean orLoop,
			boolean connectors, boolean functions, boolean events,
			HashMap orgToDup) {

		HashMap dupToOrg = new HashMap();

		ConfigurableEPC cs = EPCCopier.copy(baseEPC, orgToDup, dupToOrg);

		// In all object2 fields of the EPCObjects of cs, we
		// store an HashSet of the original objects from baseEPC
		Iterator it = cs.getVerticeList().iterator();
		while (it.hasNext()) {
			ModelGraphVertex v = (ModelGraphVertex) it.next();
			v.object2 = new HashSet();
			((HashSet) v.object2).add(dupToOrg.get(v.getIdKey()));
		}
		it = cs.getEdges().iterator();
		while (it.hasNext()) {
			EPCEdge v = (EPCEdge) it.next();
			v.object2 = new HashSet();
			((HashSet) v.object2).add(dupToOrg.get(v.getIdKey()));
		}

		boolean b = false;
		do {
			b = false;
			if (singleInSingleOut) {
				b = b
						|| removeSingleInputSingleOutput(cs, connectors,
								functions, events);
			}

			if (sameSplitAndSplit) {
				b = b || combineSimilarSplitConnectors(cs);
			}

			if (sameJoinAndJoin) {
				b = b || combineSimilarJoinConnectors(cs);
			}

			if (sameSplitAndJoin) {
				b = b || reduceSameSplitJoin(cs);
			}

			if (splitWithOrJoin) {
				b = b || reduceSplitAndOrJoin(cs);
			}

			if (xorLoop) {
				b = b || removeXorJoinXorSplitLoops(cs);
			}

			if (orLoop) {
				b = b || removeOrJoinAnySplitLoops(cs);
			}
		} while (b);

		return cs;

	}

	private static boolean combineSimilarSplitConnectors(ConfigurableEPC cs) {
		Iterator it = cs.getConnectors().iterator();
		boolean result = false;
		while (it.hasNext()) {
			EPCConnector c = (EPCConnector) it.next();

			Iterator it2 = c.getSuccessors().iterator();
			while (it2.hasNext()) {
				EPCObject o = (EPCObject) it2.next();
				if (!(o instanceof EPCConnector)) {
					continue;
				}
				EPCConnector c2 = (EPCConnector) o;
				if ((c.outDegree() > 1) && (c2.outDegree() > 1)
						&& c.getType() == c2.getType()) {
					// Two splits of same type
					// Make new edges from c to successors of c2
					EPCEdge inEdge = (EPCEdge) c2.getInEdges().iterator()
							.next();
					Iterator it3 = c2.getOutEdges().iterator();
					while (it3.hasNext()) {
						EPCEdge e = (EPCEdge) it3.next();
						EPCEdge e2 = (EPCEdge) cs.addEdge(new EPCEdge(c,
								(EPCObject) e.getDest()));
						// map the object2 element of e to e2
						e2.object2 = e.object2;
						((HashSet) e2.object2).addAll((HashSet) inEdge.object2);
					}
					// Remove c2
					// But first, add all object2 elements to c
					((HashSet) c.object2).addAll((HashSet) c2.object2);
					cs.delConnector(c2);
					it2 = c.getSuccessors().iterator();
					it = cs.getConnectors().iterator();
					result = true;
				}
			}
		}
		return result;
	}

	private static boolean combineSimilarJoinConnectors(ConfigurableEPC cs) {
		Iterator it = cs.getConnectors().iterator();
		boolean result = false;
		while (it.hasNext()) {
			EPCConnector c = (EPCConnector) it.next();

			Iterator it2 = c.getPredecessors().iterator();
			while (it2.hasNext()) {
				EPCObject o = (EPCObject) it2.next();
				if (!(o instanceof EPCConnector)) {
					continue;
				}
				EPCConnector c2 = (EPCConnector) o;
				if ((c.inDegree() > 1) && (c2.inDegree() > 1)
						&& c.getType() == c2.getType()) {
					// Two joins of same type
					// Make new edges from all predecessors of c2 to c
					EPCEdge outEdge = (EPCEdge) c2.getOutEdges().iterator()
							.next();
					Iterator it3 = c2.getInEdges().iterator();
					while (it3.hasNext()) {
						EPCEdge e = (EPCEdge) it3.next();
						EPCEdge e2 = (EPCEdge) cs.addEdge(new EPCEdge(
								(EPCObject) e.getSource(), c));
						e2.object2 = e.object2;
						((HashSet) e2.object2)
								.addAll((HashSet) outEdge.object2);
					}
					// Remove c2
					((HashSet) c.object2).addAll((HashSet) c2.object2);
					cs.delConnector(c2);
					it2 = c.getPredecessors().iterator();
					it = cs.getConnectors().iterator();
					result = true;
				}
			}
		}
		return result;
	}

	private static boolean reduceSameSplitJoin(ConfigurableEPC cs) {
		Iterator it = cs.getEdges().iterator();
		boolean result = false;
		while (it.hasNext()) {
			EPCEdge e = (EPCEdge) it.next();
			if (!(e.getSource() instanceof EPCConnector)
					|| !(e.getDest() instanceof EPCConnector)) {
				continue;
			}
			EPCConnector c1 = (EPCConnector) e.getSource();
			EPCConnector c2 = (EPCConnector) e.getDest();
			// c1 is split, c2 is join
			if (c1.getType() == c2.getType()) {

				// Check for other edges
				Iterator it2 = c1.getOutEdges().iterator();
				while (it2.hasNext()) {
					EPCEdge e2 = (EPCEdge) it2.next();
					if (e2 == e) {
						continue;
					}
					if (e2.getDest() != c2) {
						continue;
					}
					// add all object2 elements of e2 to e
					((HashSet) e.object2).addAll((HashSet) e2.object2);
					cs.removeEdge(e2);
					it2 = c1.getOutEdges().iterator();
					it = cs.getEdges().iterator();
					result = true;
				}
			}
		}
		return result;
	}

	private static boolean reduceSplitAndOrJoin(ConfigurableEPC cs) {
		Iterator it = cs.getEdges().iterator();
		boolean result = false;
		while (it.hasNext()) {
			EPCEdge e = (EPCEdge) it.next();
			if (!(e.getSource() instanceof EPCConnector)
					|| !(e.getDest() instanceof EPCConnector)) {
				continue;
			}
			EPCConnector c1 = (EPCConnector) e.getSource();
			EPCConnector c2 = (EPCConnector) e.getDest();
			// c1 is split, c2 is join
			if (c2.getType() == EPCConnector.OR) {

				// Check for other edges
				Iterator it2 = c1.getOutEdges().iterator();
				while (it2.hasNext()) {
					EPCEdge e2 = (EPCEdge) it2.next();
					if (e2 == e) {
						continue;
					}
					if (e2.getDest() != c2) {
						continue;
					}
					// add all object2 elements of e2 to e
					((HashSet) e.object2).addAll((HashSet) e2.object2);
					cs.removeEdge(e2);
					it2 = c1.getOutEdges().iterator();
					it = cs.getEdges().iterator();
					result = true;
				}
			}
		}
		return result;
	}

	private static boolean removeXorJoinXorSplitLoops(ConfigurableEPC cs) {
		Iterator it = cs.getEdges().iterator();
		boolean result = false;
		while (it.hasNext()) {
			EPCEdge e = (EPCEdge) it.next();
			ModelGraphEdge et = cs.getFirstEdge(e.getDest(), e.getSource());
			if (et == null) {
				continue;
			}
			EPCEdge e2 = (EPCEdge) et;

			if ((e.getValue() > 1) || (e2.getValue() > 1)
					|| (e.getSource().inDegree() <= 1)) {
				continue;
			}
			EPCConnector c1 = (EPCConnector) e.getSource();
			EPCConnector c2 = (EPCConnector) e.getDest();

			if ((c1.getType() == c2.getType())
					&& (c1.getType() == EPCConnector.XOR)) {
				result = true;

				// add all object2 elements of e2 to e
				((HashSet) e.object2).addAll((HashSet) e2.object2);
				cs.removeEdge(e2);
				it = cs.getEdges().iterator();
			}
		}
		return result;
	}

	private static boolean removeOrJoinAnySplitLoops(ConfigurableEPC cs) {
		Iterator it = cs.getEdges().iterator();
		boolean result = false;
		while (it.hasNext()) {
			EPCEdge e = (EPCEdge) it.next();
			ModelGraphEdge et = cs.getFirstEdge(e.getDest(), e.getSource());
			if (et == null) {
				continue;
			}
			EPCEdge e2 = (EPCEdge) et;

			if ((e.getValue() > 1) || (e2.getValue() > 1)
					|| (e.getSource().inDegree() <= 1)) {
				continue;
			}
			EPCConnector c1 = (EPCConnector) e.getSource();
			EPCConnector c2 = (EPCConnector) e.getDest();

			if ((c1.getType() == EPCConnector.OR)
					&& (c1.getType() != EPCConnector.AND)) {
				result = true;

				// add all object2 elements of e2 to e
				((HashSet) e.object2).addAll((HashSet) e2.object2);
				cs.removeEdge(e2);
				it = cs.getEdges().iterator();
			}
		}
		return result;
	}

	private static boolean removeSingleInputSingleOutput(ConfigurableEPC cs,
			boolean connectors, boolean functions, boolean events) {
		Iterator it = cs.getVerticeList().iterator();
		boolean result = false;
		while (it.hasNext()) {
			EPCObject c = (EPCObject) it.next();
			if ((c instanceof EPCConnector && connectors)
					|| (c instanceof EPCFunction && functions)
					|| (c instanceof EPCEvent && events)) {
				if ((c.inDegree() == 0) && (c.outDegree() == 0)) {
					if (c instanceof EPCConnector) {
						cs.delConnector((EPCConnector) c);
					} else if (c instanceof EPCFunction) {
						cs.delFunction((EPCFunction) c);
					} else if (c instanceof EPCEvent) {
						cs.delEvent((EPCEvent) c);
					}
					it = cs.getVerticeList().iterator();
					result = true;
				}
				if ((c.inDegree() == 1) && (c.outDegree() == 1)) {
					EPCObject from = (EPCObject) c.getPredecessors().toArray()[0];
					EPCObject to = (EPCObject) c.getSuccessors().toArray()[0];

					EPCEdge newEdge = new EPCEdge(from, to);
					HashSet a = new HashSet();
					newEdge.object2 = a;
					a.addAll((HashSet) c.object2);
					a.addAll((HashSet) ((EPCEdge) c.getInEdges().iterator()
							.next()).object2);
					a.addAll((HashSet) ((EPCEdge) c.getOutEdges().iterator()
							.next()).object2);

					if (c instanceof EPCConnector) {
						cs.delConnector((EPCConnector) c);
					} else if (c instanceof EPCFunction) {
						cs.delFunction((EPCFunction) c);
					} else if (c instanceof EPCEvent) {
						cs.delEvent((EPCEvent) c);
					}

					cs.addEdge(newEdge);

					it = cs.getVerticeList().iterator();
					result = true;
				}
			}
		}
		return result;

	}

}
