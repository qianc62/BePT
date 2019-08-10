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

package org.processmining.framework.models.epcpack;

import java.io.*;
import java.util.*;

import org.processmining.framework.log.*;
import org.processmining.framework.models.*;
import org.processmining.framework.ui.*;
import java.text.SimpleDateFormat;
import org.processmining.framework.util.StringNormalizer;

/**
 * @author not attributable
 * @version 1.0
 */

public class ConfigurableEPC extends ModelGraph {

	protected ArrayList<EPCFunction> functions = new ArrayList<EPCFunction>();
	protected ArrayList<EPCEvent> events = new ArrayList<EPCEvent>();
	protected ArrayList<EPCConnector> connectors = new ArrayList<EPCConnector>();

	private ArrayList<EPCConfigurableObject> configurableObjects = new ArrayList<EPCConfigurableObject>();

	protected boolean enforceValidity;

	boolean showOrgObjects = true;
	boolean showDataObjects = true;
	boolean showInfSysObjects = true;

	private ArrayList configurations = new ArrayList();

	public ConfigurableEPC() {
		this(true);
	}

	public ConfigurableEPC(boolean enforceValidity) {
		super("Configurable EPC");
		this.enforceValidity = enforceValidity;
	}

	/**
	 * setValidating
	 * 
	 * @param b
	 *            boolean
	 */
	public void setValidating(boolean b) {
		this.enforceValidity = b;
	}

	public EPCFunction addFunction(EPCFunction f) {
		addVertex(f);
		functions.add(f);
		if (f.isConfigurable()) {
			configurableObjects.add(f);
		}
		return f;
	}

	public EPCConnector addConnector(EPCConnector c) {
		addVertex(c);
		connectors.add(c);
		if (c.isConfigurable()) {
			configurableObjects.add(c);
		}
		return c;
	}

	public EPCEvent getEvent(String identifier) {
		Iterator i = events.iterator();
		EPCEvent event = null;
		boolean b = true;
		while (i.hasNext() && b) {
			event = (EPCEvent) i.next();
			b = !identifier.equals(event.getIdentifier());
		}
		return (b ? null : event);
	}

	public EPCEvent addEvent(EPCEvent e) {
		addVertex(e);
		events.add(e);
		return e;
	}

	public void delFunction(EPCFunction f) {
		removeVertex(f);
		functions.remove(f);
		configurableObjects.remove(f);
	}

	public void delConnector(EPCConnector c) {
		removeVertex(c);
		connectors.remove(c);
		configurableObjects.remove(c);
	}

	public void delEvent(EPCEvent e) {
		removeVertex(e);
		events.remove(e);
	}

	public String isValidEPC() {
		String message = "";
		Iterator it = getEvents().iterator();
		while (it.hasNext()) {
			EPCEvent e = (EPCEvent) it.next();
			if (e.inDegree() > 1 || e.outDegree() > 1) {
				message += "Event <I>" + e.getIdentifier()
						+ "</I> has too many input or outputs.<br>";
			}
			ArrayList preEvents = new ArrayList();
			Iterator<EPCEdge> it2 = e.getInEdgesIterator();
			while (it2.hasNext()) {
				addPreObjectsOverConnectors((EPCObject) it2.next().getSource(),
						preEvents, EPCEvent.class);
			}
			if (!preEvents.isEmpty()) {
				message += "Event <I>" + e.getIdentifier()
						+ "</I> has an event as direct predecessor.<br>";
			}
			preEvents.clear();
			it2 = e.getOutEdgesIterator();
			while (it2.hasNext()) {
				addSucObjectsOverConnectors((EPCObject) it2.next().getDest(),
						preEvents, EPCEvent.class);
			}
			if (!preEvents.isEmpty()) {
				message += "Event <I>" + e.getIdentifier()
						+ "</I> has an event as direct successor.<br>";
			}
		}
		it = getFunctions().iterator();
		while (it.hasNext()) {
			EPCFunction f = (EPCFunction) it.next();
			if (f.inDegree() != 1 || f.outDegree() != 1) {
				message += "Function <I>"
						+ f.getIdentifier()
						+ "</I> does not have exactly one input and one output.<br>";
			}
			ArrayList preFunctions = new ArrayList();
			Iterator<EPCEdge> it2 = f.getInEdgesIterator();
			while (it2.hasNext()) {
				addPreObjectsOverConnectors((EPCObject) it2.next().getSource(),
						preFunctions, EPCFunction.class);
			}
			if (!preFunctions.isEmpty()) {
				message += "Function <I>" + f.getIdentifier()
						+ "</I> has a function as direct predecessor.<br>";
			}
			preFunctions.clear();
			it2 = f.getOutEdgesIterator();
			while (it2.hasNext()) {
				addSucObjectsOverConnectors((EPCObject) it2.next().getDest(),
						preFunctions, EPCFunction.class);
			}
			if (!preFunctions.isEmpty()) {
				message += "Function <I>" + f.getIdentifier()
						+ "</I> has a function as direct successor.<br>";
			}
		}
		it = getConnectors().iterator();
		while (it.hasNext()) {
			EPCConnector c = (EPCConnector) it.next();
			if (c.inDegree() == 0 || c.outDegree() == 0) {
				message += "Connector <I>" + c.getIdentifier()
						+ "</I> does not have enough inputs or outputs.<br>";
			}
			if (c.inDegree() > 1 && c.outDegree() > 1) {
				message += "Connector <I>" + c.getIdentifier()
						+ "</I> has multiple inputs and outputs.<br>";
			}
		}
		if (message.length() > 0) {
			message = "<html>Problems found in EPC: "
					+ getIdentifier()
					+ ": <br>"
					+ message
					+ "<br>As a result of these problems,<br> some plugins may not work.</html>";
		}
		return message;
	}

	public EPCEdge addEdge(EPCObject source, EPCObject destination) {
		if (enforceValidity) {
			// The number of outgoing arcs at each event should be at most 1
			if ((source instanceof EPCEvent) && (source.outDegree() != 0)) {
				return null;
			}
			// The number of outgoing arcs at each function should be at most 1
			/*
			 * if ((source instanceof EPCFunction) && ((destination instanceof
			 * EPCEvent) ||(destination instanceof EPCConnector)) &&
			 * (source.outDegree() != 0)) { return null; }
			 */
			// The number of incoming arcs at each event should be at most 1
			if ((destination instanceof EPCEvent)
					&& (destination.inDegree() != 0)) {
				return null;
			}
			// The number of incoming arcs at each function should be at most 1
			if ((destination instanceof EPCFunction)
					&& ((source instanceof EPCEvent) || (source instanceof EPCConnector))
					&& (destination.inDegree() != 0)) {
				return null;
			}
			// The number of incoming or outgoing arcs at each connector should
			// be at most 1
			if ((source instanceof EPCConnector) && (source.inDegree() > 1)
					&& (source.outDegree() > 0)) {
				return null;
			}
			// The number of incoming or outgoing arcs at each connector should
			// be at most 1
			if ((destination instanceof EPCConnector)
					&& (destination.outDegree() > 1)
					&& (destination.inDegree() > 0)) {
				return null;
			}

		}
		if (getFirstEdge(source, destination) == null) {
			EPCEdge e = new EPCEdge(source, destination);
			addEdge(e);
			return e;
		}
		return null;
	}

	public void writeToDot(Writer bw) throws IOException {
		nodeMapping.clear();
		bw
				.write("digraph G {ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
		bw.write("fontname=\"Arial\";rankdir=\"TB\";compound=\"true\" \n");
		bw.write("edge [arrowsize=\"0.5\"];\n");
		bw
				.write("node [height=\".2\",width=\"1\",fontname=\"Arial\",fontsize=\"8\"];\n");

		int j = 0;
		for (int i = 0; i < functions.size(); i++) {
			EPCFunction function = (EPCFunction) functions.get(i);
			bw.write("subgraph cluster_function_" + function.getId() + " {"
					+ " label=\"\";color=\"white\"");
			function.writeDOTCode(bw, nodeMapping);
			bw.write("}");
			j++;
		}

		for (int i = 0; i < events.size(); i++) {
			EPCEvent event = (EPCEvent) events.get(i);

			bw
					.write("node"
							+ event.getId()
							+ " [shape=\"hexagon\",style=\"filled\",fillcolor=\"orchid1\",label=\""
							+ event.getID() + "\",fontsize=6];\n");
			nodeMapping.put(new String("node" + event.getId()), event);
			j++;
		}

		for (int i = 0; i < connectors.size(); i++) {
			EPCConnector connector = (EPCConnector) connectors.get(i);

			bw
					.write("node"
							+ connector.getId()
							+ " [shape =\"circle\",fixedsize=\"true\",height=\"0.25\",width=\"0.25\","
							+ "style=\"filled"
							+ (connector.isConfigurable() ? ",bold" : "")
							+ "\",fillcolor=\"azure2\",label=\"");
			if (connector.getType() == EPCConnector.AND) {
				bw.write("^" + "\",fontsize=\"16\"");
			} else if (connector.getType() == EPCConnector.XOR) {
				bw.write("X" + "\",fontsize=\"10\"");
			} else if (connector.getType() == EPCConnector.OR) {
				bw.write("V" + "\",fontsize=\"10\"");
			} else {
				Message.add("problem with connectortype");
				bw.write("\"");
			}
			bw.write("];\n");
			nodeMapping.put(new String("node" + connector.getId()), connector);
			j++;
		}

		Iterator it = getEdges().iterator();
		while (it.hasNext()) {
			ModelGraphEdge e = (ModelGraphEdge) it.next();
			EPCObject source = (EPCObject) e.getSource();
			EPCObject destination = (EPCObject) e.getDest();
			// if (!(source instanceof EPCFunction) ||
			// (destination instanceof EPCConnector) ||
			// (destination instanceof EPCEvent)) {

			bw.write("node"
					+ source.getId()
					+ " -> node"
					+ destination.getId()
					+ " [label=\""
					+ (e.getValue() > 0 ? new Double(e.getValue()).toString()
							: "") + "\"]; \n");
			// } else {
			// edge has been written by the function already
			// }
		}

		bw.write("}\n");
	}

	public ArrayList<EPCFunction> getAllFunctions(LogEvent lme) {
		ArrayList a = new ArrayList();
		Iterator it = functions.iterator();
		while (it.hasNext()) {
			EPCFunction f = (EPCFunction) it.next();
			if ((f.getLogEvent() != null) && (f.getLogEvent().equals(lme))) {
				a.add(f);
			}
		}
		return a;
	}

	public ArrayList<EPCFunction> getAllFunctions(String identifier) {
		ArrayList a = new ArrayList();
		Iterator it = functions.iterator();
		while (it.hasNext()) {
			EPCFunction f = (EPCFunction) it.next();
			if ((f.getIdentifier().equals(identifier))) {
				a.add(f);
			}
		}
		return a;
	}

	public ArrayList<EPCFunction> getFunctions() {
		return functions;
	}

	public ArrayList<EPCEvent> getEvents() {
		return events;
	}

	public ArrayList<EPCConnector> getConnectors() {
		return connectors;
	}

	public ArrayList<EPCConfigurableObject> getConfigurableObjects() {
		return configurableObjects;
	}

	public void setShowObjects(boolean org, boolean data, boolean infSys) {
		showOrgObjects = org;
		showDataObjects = data;
		showInfSysObjects = infSys;
	}

	public ConfigurableEPCConfiguration addConfiguration(
			ConfigurableEPCConfiguration config) {
		configurations.add(config);
		return config;
	}

	public void delConfiguration(ConfigurableEPCConfiguration config) {
		configurations.remove(config);
	}

	/**
	 * applyConfiguration applies the configuration of this configurable epc,
	 * thus yielding a new EPC, note that config.isComplete() should be true. If
	 * not, null is returned.
	 * 
	 * @param config
	 *            ConfigurableEPCConfiguration
	 * @return EPC the newly configured EPC.
	 */
	public EPC applyConfiguration(ConfigurableEPCConfiguration config) {
		return null;
	}

	/**
	 * applyConfiguration applies the current configuration of this configurable
	 * epc, thus yielding a new EPC. The current configuration is the one last
	 * added by the addConfiguration method. If there is no such configuration,
	 * null is returned. Note that the configuration should be complete. If not,
	 * null is returned.
	 * 
	 * @param config
	 *            ConfigurableEPCConfiguration
	 * @return EPC the newly configured EPC.
	 */
	public EPC applyCurrentConfiguration() {
		if (configurations.size() > 0) {
			return applyConfiguration((ConfigurableEPCConfiguration) configurations
					.get(configurations.size() - 1));
		}
		return null;
	}

	/**
	 * applyConfiguration applies the current configuration of this configurable
	 * epc, thus yielding a new EPC. The current configuration is the one last
	 * added by the addConfiguration method. If there is no such configuration,
	 * null is returned. Note that the configuration should be complete. If not,
	 * null is returned.
	 * 
	 * @param config
	 *            ConfigurableEPCConfiguration
	 * @return EPC the newly configured EPC.
	 */
	public EPC applyFirstCompleteConfiguration() {
		for (int i = 0; i < configurations.size(); i++) {
			if (((ConfigurableEPCConfiguration) configurations.get(i))
					.isComplete()) {
				return applyConfiguration((ConfigurableEPCConfiguration) configurations
						.get(i));
			}
			;
		}
		return null;
	}

	/**
	 * Check if there is at least one configuration that is complete
	 * 
	 * @param config
	 *            ConfigurableEPCConfiguration
	 * @return EPC the newly configured EPC.
	 */
	public boolean isFullyConfigurable() {
		boolean conf = false;
		for (int i = 0; !conf && i < configurations.size(); i++) {
			conf |= ((ConfigurableEPCConfiguration) configurations.get(i))
					.isComplete();
		}
		return conf;
	}

	/**
	 * Get a list of events preceding a function of an EPC, either directly or
	 * via connectors.
	 * 
	 * @param EPCFunction
	 *            A function of the EPC
	 * @return ArrayList<EPCEvent> A list of all EPC Events that preceed the
	 *         function either directly or via connectors.
	 */
	public ArrayList<EPCEvent> getPreceedingEvents(EPCFunction f) {
		ArrayList list = new ArrayList();
		addSurroundingObjects(f, list, EPCEvent.class, EPCFunction.class, true,
				false);
		return list;
	}

	/**
	 * Get a list of events preceding a connector of an EPC, either directly or
	 * via other connectors and functions.
	 * 
	 * @param EPCConnector
	 *            A connector of the EPC
	 * @return ArrayList<EPCEvent> A list of all EPC Events that preceed the
	 *         connector either directly or via other connectors and functions.
	 */
	public ArrayList<EPCEvent> getPreceedingEvents(EPCConnector c) {
		ArrayList list = new ArrayList();
		addSurroundingObjects(c, list, EPCEvent.class, EPCEvent.class, true,
				false);
		return list;
	}

	/**
	 * Get a list of events preceding an event of an EPC via connectors and
	 * functions.
	 * 
	 * @param EPCEvent
	 *            An event of the EPC
	 * @return ArrayList<EPCEvent> A list of all EPC Events that preceed the
	 *         event via connectors and functions.
	 */
	public ArrayList<EPCEvent> getPreceedingEvents(EPCEvent e) {
		ArrayList list = new ArrayList();
		addSurroundingObjects(e, list, EPCEvent.class, EPCEvent.class, true,
				false);
		return list;
	}

	/**
	 * Get a list of events succeeding a function of an EPC, either directly or
	 * via connectors.
	 * 
	 * @param EPCFunction
	 *            A function of the EPC
	 * @return ArrayList<EPCEvent> A list of all EPC Events that succeed the
	 *         function either directly or via connectors.
	 */
	public ArrayList<EPCEvent> getSucceedingEvents(EPCFunction f) {
		ArrayList list = new ArrayList();
		addSurroundingObjects(f, list, EPCEvent.class, EPCFunction.class, true,
				true);
		return list;
	}

	/**
	 * Get a list of events succeeding a connector of an EPC, either directly or
	 * via other connectors and functions.
	 * 
	 * @param EPCConnector
	 *            A connector of the EPC
	 * @return ArrayList<EPCEvent> A list of all EPC Events that succeed the
	 *         connector either directly or via other connectors and functions.
	 */
	public ArrayList<EPCEvent> getSucceedingEvents(EPCConnector c) {
		ArrayList list = new ArrayList();
		addSurroundingObjects(c, list, EPCEvent.class, EPCEvent.class, true,
				true);
		return list;
	}

	/**
	 * Get a list of events succeeding an event of an EPC via connectors and
	 * functions.
	 * 
	 * @param EPCEvent
	 *            An event of the EPC
	 * @return ArrayList<EPCEvent> A list of all EPC Events that succeed the
	 *         event via connectors and functions.
	 */
	public ArrayList<EPCEvent> getSucceedingEvents(EPCEvent e) {
		ArrayList list = new ArrayList();
		addSurroundingObjects(e, list, EPCEvent.class, EPCEvent.class, true,
				true);
		return list;
	}

	/**
	 * Provides a list of objects that directly succeed the given object of the
	 * EPC in the EPC.
	 * 
	 * @param EPCObject
	 *            An object of the EPC
	 * @return ArrayList<EPCFunction> A list of all EPC objects succeeding the
	 *         given EPC object.
	 */
	public ArrayList<EPCObject> getSucceedingElements(EPCObject o) {
		ArrayList<EPCObject> list = new ArrayList<EPCObject>();
		Iterator it = o.getOutEdgesIterator();
		while (it.hasNext()) {
			EPCEdge e = ((EPCEdge) it.next());
			EPCObject node = (EPCObject) e.getDest();
			list.add(node);
		}
		return list;
	}

	/**
	 * Provides a list of objects that directly preceed the given object of the
	 * EPC in the EPC.
	 * 
	 * @param EPCObject
	 *            An object of the EPC
	 * @return ArrayList<EPCFunction> A list of all EPC objects preceeding the
	 *         given EPC object.
	 */
	public ArrayList<EPCObject> getPreceedingElements(EPCObject o) {
		ArrayList<EPCObject> list = new ArrayList<EPCObject>();
		Iterator it = o.getInEdgesIterator();
		while (it.hasNext()) {
			EPCEdge e = ((EPCEdge) it.next());
			EPCObject node = (EPCObject) e.getSource();
			list.add(node);
		}
		return list;
	}

	/**
	 * Get a list of functions preceeding an event of an EPC either directly or
	 * via connectors.
	 * 
	 * @param EPCEvent
	 *            An event of the EPC
	 * @return ArrayList<EPCFunction> A list of all EPC functions that preceed
	 *         the function either directly or via connectors.
	 */
	public ArrayList<EPCFunction> getPreceedingFunctions(EPCEvent e) {
		ArrayList<EPCFunction> list = new ArrayList<EPCFunction>();
		addSurroundingObjects(e, list, EPCFunction.class, EPCEvent.class, true,
				false);
		return list;
	}

	/**
	 * Get a list of functions preceeding a connector of an EPC either directly
	 * or via other connectors and events.
	 * 
	 * @param EPCConnector
	 *            A connector of the EPC
	 * @return ArrayList<EPCFunction> A list of all EPC functions that preceed
	 *         the function either directly or via other connectors and events.
	 */
	public ArrayList<EPCFunction> getPreceedingFunctions(EPCConnector c) {
		ArrayList list = new ArrayList();
		addSurroundingObjects(c, list, EPCFunction.class, EPCFunction.class,
				true, false);
		return list;
	}

	/**
	 * Get a list of functions preceeding a function of an EPC via connectors
	 * and events.
	 * 
	 * @param EPCFunction
	 *            A function of the EPC
	 * @return ArrayList<EPCFunction> A list of all EPC functions that preceed
	 *         the function via connectors and events.
	 */
	public ArrayList<EPCFunction> getPreceedingFunctions(EPCFunction f) {
		ArrayList list = new ArrayList();
		addSurroundingObjects(f, list, EPCFunction.class, EPCFunction.class,
				true, false);
		return list;
	}

	/**
	 * Get a list of functions succeeding an event of an EPC either directly or
	 * via connectors.
	 * 
	 * @param EPCEvent
	 *            An event of the EPC
	 * @return ArrayList<EPCFunction> A list of all EPC functions that succeed
	 *         the function either directly or via connectors.
	 */
	public ArrayList<EPCFunction> getSucceedingFunctions(EPCEvent e) {
		ArrayList list = new ArrayList();
		addSurroundingObjects(e, list, EPCFunction.class, EPCEvent.class, true,
				true);
		return list;
	}

	/**
	 * Get a list of functions succeeding a connector of an EPC either directly
	 * or via other connectors and events.
	 * 
	 * @param EPCConnector
	 *            A connector of the EPC
	 * @return ArrayList<EPCFunction> A list of all EPC functions that succeed
	 *         the function either directly or via other connectors and events.
	 */
	public ArrayList<EPCFunction> getSucceedingFunctions(EPCConnector c) {
		ArrayList list = new ArrayList();
		addSurroundingObjects(c, list, EPCFunction.class, EPCFunction.class,
				true, true);
		return list;
	}

	/**
	 * Get a list of functions succeeding a function of an EPC via connectors
	 * and events.
	 * 
	 * @param EPCFunction
	 *            A function of the EPC
	 * @return ArrayList<EPCFunction> A list of all EPC functions that succeed
	 *         the function via connectors and events.
	 */
	public ArrayList<EPCFunction> getSucceedingFunctions(EPCFunction f) {
		ArrayList list = new ArrayList();
		addSurroundingObjects(f, list, EPCFunction.class, EPCFunction.class,
				true, true);
		return list;
	}

	private void addSurroundingObjects(EPCObject o, ArrayList list,
			Class searchFor, Class doNotCross, boolean init, boolean forward) {
		if (!init && searchFor.isAssignableFrom(o.getClass())) {
			list.add(o);
		} else if (init || !doNotCross.isAssignableFrom(o.getClass())) {
			Iterator it = (forward ? o.getOutEdgesIterator() : o
					.getInEdgesIterator());
			while (it.hasNext()) {
				EPCEdge e = ((EPCEdge) it.next());
				EPCObject node = (EPCObject) (forward ? e.getDest() : e
						.getSource());
				addSurroundingObjects(node, list, searchFor, doNotCross, false,
						forward);
			}
		} // else, don't continue (i.e. a doNotCross class.
	}

	private void addPreObjectsOverConnectors(EPCObject o, ArrayList list,
			Class name) {
		if (name.isAssignableFrom(o.getClass())) {
			list.add(o);
		} else if (o instanceof EPCConnector) {
			Iterator it = o.getInEdgesIterator();
			while (it.hasNext()) {
				EPCEdge e = ((EPCEdge) it.next());
				addPreObjectsOverConnectors((EPCObject) e.getSource(), list,
						name);
			}
		}
	}

	private void addSucObjectsOverConnectors(EPCObject o, ArrayList list,
			Class name) {
		if (name.isAssignableFrom(o.getClass())) {
			list.add(o);
		} else if (o instanceof EPCConnector) {
			Iterator it = o.getOutEdgesIterator();
			while (it.hasNext()) {
				EPCEdge e = ((EPCEdge) it.next());
				addSucObjectsOverConnectors((EPCObject) e.getDest(), list, name);
			}
		}
	}

	public void copyAllFrom(ConfigurableEPC epc, HashMap org2new) {

		Iterator it = epc.getFunctions().iterator();
		while (it.hasNext()) {
			EPCFunction f = (EPCFunction) it.next();
			EPCFunction newF;
			if (f instanceof EPCFunction) {
				newF = addFunction(new EPCFunction(f.getLogEvent(), f
						.isConfigurable(), this));
			} else {
				newF = addFunction(new EPCSubstFunction(f.getLogEvent(), f
						.isConfigurable(), this, ((EPCSubstFunction) f)
						.getSubstitutedEPC()));
			}
			newF.setIdentifier(f.getIdentifier());
			org2new.put(f.getIdKey(), newF);
		}
		it = epc.getEvents().iterator();
		while (it.hasNext()) {
			EPCEvent f = (EPCEvent) it.next();
			org2new.put(f.getIdKey(), addEvent(new EPCEvent(f.getIdentifier(),
					this)));
		}
		it = epc.getConnectors().iterator();
		while (it.hasNext()) {
			EPCConnector f = (EPCConnector) it.next();
			org2new.put(f.getIdKey(), addConnector(new EPCConnector(
					f.getType(), this)));
		}
		it = epc.getEdges().iterator();
		while (it.hasNext()) {
			EPCEdge e = (EPCEdge) it.next();
			addEdge((EPCObject) org2new.get(e.getSource().getIdKey()),
					(EPCObject) org2new.get(e.getDest().getIdKey()));
		}
	}

	public void writePPMImport(OutputStream out) throws IOException {
		// Assume that the header of the graph part is written,

		int i = 0;
		Iterator it = getFunctions().iterator();
		while (it.hasNext()) {
			EPCObject o = (EPCObject) it.next();
			o.setNumber(i);
			write("      <node id=\"object_" + i + "\" type=\"OT_FUNC\">\n",
					out);
			write("         <attribute type=\"AT_OBJNAME_INTERN\">"
					+ StringNormalizer.escapeXMLCharacters(editName(o
							.getIdentifier())) + "</attribute>\n", out);
			write("         <attribute type=\"AT_OBJNAME\">"
					+ StringNormalizer.escapeXMLCharacters(editName(o
							.getIdentifier())) + "</attribute>\n", out);
			if (o.object2 instanceof AuditTrailEntry) {
				AuditTrailEntry ate = (AuditTrailEntry) o.object2;

				if (ate.getTimestamp() != null
						&& !ate.getTimestamp().toString().equals("")) {
					SimpleDateFormat dateParser = new SimpleDateFormat(
							"dd.MM.yyyy' 'HH:mm:ss");

					write("         <attribute type=\"AT_TIMESTAMP\">"
							+ dateParser.format(ate.getTimestamp())
							+ "</attribute>\n", out);
				}
				Map data = ate.getData();
				Iterator dataKeys = data.keySet().iterator();
				while (dataKeys.hasNext()) {
					Object key = dataKeys.next();
					String s = new String((String) key);
					s.replaceAll(" ", "_");
					write(
							"         <attribute type=\"AT_"
									+ s
									+ "\">"
									+ StringNormalizer
											.escapeXMLCharacters((String) data
													.get(key))
									+ "</attribute>\n", out);
				}
			}
			write("      </node>\n", out);

			if (o.object2 instanceof AuditTrailEntry) {
				AuditTrailEntry ate = (AuditTrailEntry) o.object2;
				if (ate.getOriginator() != null
						&& !ate.getOriginator().equals("")) {
					write("      <node id=\"originator_ate_" + i
							+ "\" type=\"OT_ORG\">\n", out);
					write("         <attribute type=\"AT_OBJNAME\">"
							+ StringNormalizer.escapeXMLCharacters(ate
									.getOriginator()) + "</attribute>\n", out);
					write("         <attribute type=\"AT_OBJNAME_INTERN\">"
							+ ate.getOriginator() + "</attribute>\n", out);
					write("      </node>\n", out);

				}
			}

			i++;
		}

		it = getEvents().iterator();
		while (it.hasNext()) {
			EPCObject o = (EPCObject) it.next();
			o.setNumber(i);
			write("      <node id=\"object_" + i + "\" type=\"OT_EVT\">\n", out);
			write("         <attribute type=\"AT_OBJNAME_INTERN\">"
					+ editName(o.getIdentifier()) + "</attribute>\n", out);
			write("         <attribute type=\"AT_OBJNAME\">"
					+ editName(o.getIdentifier()) + "</attribute>\n", out);
			write("      </node>\n", out);
			i++;
		}

		it = getConnectors().iterator();
		while (it.hasNext()) {
			EPCConnector c = (EPCConnector) it.next();
			c.setNumber(i);
			write("      <node id=\"object_" + i + "\" type=\"OT_RULE", out);
			if (c.getType() == EPCConnector.AND) {
				write("AND", out);
			}
			if (c.getType() == EPCConnector.XOR) {
				write("XOR", out);
			}
			if (c.getType() == EPCConnector.OR) {
				write("OR", out);
			}
			write("\"/>\n", out);
			i++;
		}

		it = getEdges().iterator();
		while (it.hasNext()) {
			EPCEdge e = (EPCEdge) it.next();
			EPCObject o1 = (EPCObject) e.getSource();
			EPCObject o2 = (EPCObject) e.getDest();
			write("      <edge type=\"CXN_FOLLOWS\" source=\"object_"
					+ o1.getNumber() + "\" target=\"object_" + o2.getNumber()
					+ "\"/>\n", out);
		}

		it = getFunctions().iterator();
		i = 0;
		while (it.hasNext()) {
			EPCObject o = (EPCObject) it.next();
			if (o.object2 instanceof AuditTrailEntry) {
				AuditTrailEntry ate = (AuditTrailEntry) o.object2;
				if (ate.getOriginator() != null
						&& !ate.getOriginator().equals("")) {
					write(
							"      <edge type=\"CXN_UNDIRECTED\" source=\"object_"
									+ i + "\" target=\"originator_ate_" + i
									+ "\"/>\n", out);
				}
			}
			i++;
		}

	}

	protected void write(String s, OutputStream out) {
		try {
			out.write(s.getBytes());
		} catch (IOException ex) {
		}
	}

	public static String editName(String name) {
		return name.replaceAll("\\\\n", " ");
	}

	/**
	 * Print key indicators of the EPC to the Test tab.
	 * 
	 * @param tag
	 *            String The tag to use for the indicators.
	 */
	public void Test(String tag) {
		Message.add("<" + tag + " nofFunctions=\"" + getFunctions().size()
				+ "\" nofEvents=\"" + getEvents().size()
				+ "\" nofConnectors=\"" + getConnectors().size()
				+ "\" nofEdges=\"" + getEdges().size() + "\"/>", Message.TEST);
	}
}
