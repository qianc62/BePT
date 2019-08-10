package org.processmining.framework.models.epcpack;

import java.util.*;
import org.processmining.framework.models.epcpack.algorithms.ConnectorStructureExtractor;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description: This class builds a fragment of an EPC, based on a visible
 * function f. The EPC contains the spanning tree of the original epc starting
 * in f upto the preceeding events. The EPC contains the spanning tree of the
 * original epc starting in f downto the succeeding events.
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
 * @author not attributable
 * @version 1.0
 */
public class EPCFragment extends ConfigurableEPC {

	private EPCFunction function;
	private ConfigurableEPC epc;
	private HashMap org2frag = new HashMap();
	private HashMap frag2org = new HashMap();
	private ArrayList initialEvents = new ArrayList();
	private ArrayList finalEvents = new ArrayList();

	public EPCFragment(EPCFunction function) {
		this.function = function;
		epc = function.getEPC();

		EPCFunction f = addFunction(new EPCFunction(function.getLogEvent(),
				function.isConfigurable(), this));
		f.setIdentifier(function.getIdentifier());
		org2frag.put(function.getIdKey(), f);
		frag2org.put(f.getIdKey(), function);
		constructBack(function, false);
		constructForward(function, false);
	}

	public ArrayList getInitialEvents() {
		return initialEvents;
	}

	public ArrayList getFinalEvents() {
		return finalEvents;
	}

	public ConfigurableEPC getOriginalEPC() {
		return epc;
	}

	public EPCObject fragToOrg(EPCObject fragObject) {
		return (EPCObject) frag2org.get(fragObject.getIdKey());
	}

	public EPCObject orgToFrag(EPCObject object) {
		return (EPCObject) org2frag.get(object.getIdKey());
	}

	private void constructBack(EPCObject o, boolean invis) {
		if (o instanceof EPCEvent) {
			// event
			EPCEvent e = (EPCEvent) o;
			EPCEvent newEvt = addEvent(new EPCEvent(o.getIdentifier(), this));
			org2frag.put(e.getIdKey(), newEvt);
			frag2org.put(newEvt.getIdKey(), e);
			if (!invis) {
				initialEvents.add(newEvt);
				return;
			} else {
				Iterator it = e.getPredecessors().iterator();
				while (it.hasNext()) {
					EPCObject p = (EPCObject) it.next();
					constructBack(p, false);
					addEdge((EPCObject) org2frag.get(p.getIdKey()), newEvt);
				}
			}
		} else if (o == function) {
			Iterator it = o.getPredecessors().iterator();
			while (it.hasNext()) {
				EPCObject p = (EPCObject) it.next();
				constructBack(p, false);
				addEdge((EPCObject) org2frag.get(p.getIdKey()),
						(EPCObject) org2frag.get(o.getIdKey()));
			}
			return;

		} else if (o instanceof EPCConnector) {
			EPCConnector f = (EPCConnector) o;
			// connector
			EPCConnector c = addConnector(new EPCConnector(f.getType(), this));
			org2frag.put(f.getIdKey(), c);
			frag2org.put(c.getIdKey(), f);
			Iterator it = f.getPredecessors().iterator();
			while (it.hasNext()) {
				EPCObject p = (EPCObject) it.next();
				constructBack(p, false);
				addEdge((EPCObject) org2frag.get(p.getIdKey()),
						(EPCObject) org2frag.get(f.getIdKey()));
			}
			return;
		}
	}

	private void constructForward(EPCObject o, boolean invis) {
		if (o instanceof EPCEvent) {
			// event
			EPCEvent e = (EPCEvent) o;
			EPCEvent newEvt = addEvent(new EPCEvent(o.getIdentifier(), this));
			org2frag.put(e.getIdKey(), newEvt);
			frag2org.put(newEvt.getIdKey(), e);
			if (!invis) {
				finalEvents.add(newEvt);
				return;
			} else {
				Iterator it = e.getSuccessors().iterator();
				while (it.hasNext()) {
					EPCObject p = (EPCObject) it.next();
					constructForward(p, false);
					addEdge(newEvt, (EPCObject) org2frag.get(p.getIdKey()));
				}
			}
		} else if (o == function) {
			Iterator it = o.getSuccessors().iterator();
			while (it.hasNext()) {
				EPCObject p = (EPCObject) it.next();
				constructForward(p, false);
				addEdge((EPCObject) org2frag.get(o.getIdKey()),
						(EPCObject) org2frag.get(p.getIdKey()));
			}
			return;
		} else if (o instanceof EPCConnector) {
			EPCConnector f = (EPCConnector) o;
			// connector
			EPCConnector c = addConnector(new EPCConnector(f.getType(), this));
			org2frag.put(f.getIdKey(), c);
			frag2org.put(c.getIdKey(), f);
			Iterator it = f.getSuccessors().iterator();
			while (it.hasNext()) {
				EPCObject p = (EPCObject) it.next();
				constructForward(p, false);
				addEdge((EPCObject) org2frag.get(f.getIdKey()),
						(EPCObject) org2frag.get(p.getIdKey()));
			}
			return;
		}
	}

}
