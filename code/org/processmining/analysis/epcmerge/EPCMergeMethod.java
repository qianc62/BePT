package org.processmining.analysis.epcmerge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.processmining.converting.AggregationGraphToEPC;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;
import org.processmining.framework.log.rfb.AuditTrailEntryListImpl;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEdge;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.writing.LogSetSequential;
import org.processmining.lib.mxml.writing.ProcessInstanceType;
import org.processmining.lib.mxml.writing.impl.LogSetSequentialImpl;
import org.processmining.lib.mxml.writing.persistency.LogPersistency;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyDir;
import org.processmining.mining.epcmining.EPCResult;
import org.processmining.mining.logabstraction.FSMLogRelationBuilder;
import org.processmining.mining.logabstraction.LogAbstraction;
import org.processmining.mining.logabstraction.LogAbstractionImpl;
import org.processmining.mining.logabstraction.LogRelations;
import org.processmining.mining.logabstraction.MinValueLogRelationBuilder;
import org.processmining.mining.logabstraction.TimeIntervalLogRelationBuilder;
import org.processmining.mining.partialordermining.AggregationGraphResult;
import org.processmining.mining.partialordermining.PartialOrderAggregationPlugin;
import org.processmining.mining.partialordermining.PartialOrderGeneratorPlugin;
import org.processmining.mining.partialordermining.PartialOrderMiningResult;

import att.grappa.Edge;

public class EPCMergeMethod {

	private static final long serialVersionUID = 8186397348144508216L;
	public final static int NONE = 3, OR = 4, AND = 5, XOR = 6;
	public final static boolean PRECEDING = false, SUCCEEDING = true;

	private final static String EPCMERGEADD = "EPCMERGEADD";

	private ConfigurableEPC resultingEPC;
	// private ArrayList<ProcessImpl> processes;
	// private ProcessImpl p;
	// private ArrayList<ProcessInstanceImpl> instances;
	private LogReader log;
	private int instanceID = 0;
	private Hashtable<String, List<String>> combinedEventNames;
	private Hashtable<String, Integer> combinedJoinValues;
	private Hashtable<String, Integer> combinedSplitValues;
	private LogSetSequential outputLog;
	private Hashtable<String, Integer> addedFinalFunctions = new Hashtable<String, Integer>();
	private Hashtable<String, Integer> addedInitialFunctions = new Hashtable<String, Integer>();
	private EPCStructure epc1, epc2;

	protected class EPCStructure {
		private ConfigurableEPC epc;
		private LogSetSequential outputLog;
		private List<String> visited;
		private int noAddedFinalFunctions = 0;
		private int noAddedInitialFunctions = 0;
		private Hashtable<String, Integer> addedFinalFunctions = new Hashtable<String, Integer>();
		private Hashtable<String, Integer> addedInitialFunctions = new Hashtable<String, Integer>();
		private ArrayList<EPCObject> addedObjects = new ArrayList<EPCObject>();

		private Hashtable<String, Integer> splitRelation = new Hashtable<String, Integer>();
		private Hashtable<String, Integer> joinRelation = new Hashtable<String, Integer>();
		private Hashtable<EPCObject, Hashtable<EPCObject, Integer>> visitedEdges = new Hashtable<EPCObject, Hashtable<EPCObject, Integer>>();

		private void calculateVisitedEdges() {
			Iterator<EPCFunction> itFunction = epc.getFunctions().iterator();
			visitedEdges = new Hashtable<EPCObject, Hashtable<EPCObject, Integer>>();
			while (itFunction.hasNext()) {
				EPCFunction source = itFunction.next();
				Iterator<EPCEvent> itEvent = epc.getSucceedingEvents(source)
						.iterator();
				while (itEvent.hasNext()) {
					if (visitedEdges.containsKey(source)) {
						Hashtable<EPCObject, Integer> target = visitedEdges
								.get(source);
						target.put(itEvent.next(), 0);
					} else {
						Hashtable<EPCObject, Integer> target = new Hashtable<EPCObject, Integer>();
						target.put(itEvent.next(), 0);
						visitedEdges.put(source, target);
					}

				}
			}
			Iterator<EPCEvent> itEvent = epc.getEvents().iterator();
			while (itEvent.hasNext()) {
				EPCEvent source = itEvent.next();
				itFunction = epc.getSucceedingFunctions(source).iterator();
				while (itFunction.hasNext()) {
					if (visitedEdges.containsKey(source)) {
						Hashtable<EPCObject, Integer> target = visitedEdges
								.get(source);
						target.put(itFunction.next(), 0);
					} else {
						Hashtable<EPCObject, Integer> target = new Hashtable<EPCObject, Integer>();
						target.put(itFunction.next(), 0);
						visitedEdges.put(source, target);
					}

				}
			}

		}

		protected EPCStructure(ConfigurableEPC net) {
			this.epc = net;
			calculateVisitedEdges();

		}

		protected ConfigurableEPC getEPC() {
			return epc;
		}

		private Integer getEventRelation(List<String> events,
				EPCObject startElement, EPCEvent startEvent, Integer relation,
				boolean succeedingCon) {
			Iterator<EPCObject> oIt = (succeedingCon ? epc
					.getSucceedingElements(startElement).iterator() : epc
					.getPreceedingElements(startElement).iterator());
			while (oIt.hasNext()) {
				EPCObject o = oIt.next();
				if (EPCConnector.class.isAssignableFrom(o.getClass())) {
					List<EPCEvent> sucEvent = (succeedingCon ? epc
							.getPreceedingEvents((EPCConnector) o) : epc
							.getSucceedingEvents((EPCConnector) o));
					Iterator<String> eventIt = events.iterator();
					while (eventIt.hasNext()) {
						EPCEvent eventToCheck = epc.getEvent(eventIt.next());
						if (eventToCheck != startEvent
								&& sucEvent.contains(eventToCheck)) {
							if (relation.intValue() == XOR
									&& !(o.getType() == AND || o.getType() == OR)) {
								relation = new Integer(XOR);
							} else if (relation.intValue() == AND
									&& !(o.getType() == XOR || o.getType() == OR)) {
								relation = new Integer(AND);
							} else if (relation.intValue() == NONE) {
								relation = new Integer(o.getType());
							} else {
								relation = new Integer(OR);
							}

						}
					}
					getEventRelation(events, o, startEvent, relation,
							succeedingCon);
				}
			}
			return relation;
		}

		private Integer getEventRelation(List<String> events,
				boolean succeedingCon) {
			Iterator<String> it = events.iterator();
			Integer relation = null;
			while (it.hasNext()) {
				EPCEvent e = epc.getEvent(it.next());
				if (e != null) {
					if (relation == null) {
						relation = new Integer(NONE);
					}
					relation = new Integer(getEventRelation(events, e, e,
							relation, succeedingCon));
				}
			}
			return relation;
		}

		private List<String> getAlternativeEvents(List<String> events,
				EPCObject startElement, EPCEvent startEvent,
				List<String> listOfEvents, boolean succeedingCon) {
			Iterator<EPCObject> oIt = (succeedingCon ? epc
					.getSucceedingElements(startElement).iterator() : epc
					.getPreceedingElements(startElement).iterator());
			while (oIt.hasNext()) {
				EPCObject o = oIt.next();
				if (EPCConnector.class.isAssignableFrom(o.getClass())) {
					Iterator<EPCEvent> e = (succeedingCon ? epc
							.getPreceedingEvents((EPCConnector) o).iterator()
							: epc.getSucceedingEvents((EPCConnector) o)
									.iterator());
					while (e.hasNext()) {
						listOfEvents.add(e.next().getIdentifier());
					}
					getAlternativeEvents(events, o, startEvent, listOfEvents,
							succeedingCon);
				}
			}
			return listOfEvents;
		}

		private List<String> getAlternativeEvents(List<String> events,
				boolean succeedingCon) {
			Iterator<String> it = events.iterator();
			ArrayList<String> listOfEvents = new ArrayList<String>();
			while (it.hasNext()) {
				EPCEvent e = epc.getEvent(it.next());
				if (e != null) {
					if (!listOfEvents.contains(e)) {
						listOfEvents.add(e.getIdentifier());
					}
					getAlternativeEvents(events, e, e, listOfEvents,
							succeedingCon);
				}
			}
			return listOfEvents;
		}

		private LogSetSequential addPI(LogSetSequential startLog) {
			this.outputLog = startLog;
			visited = new ArrayList<String>();
			Iterator<EPCEvent> initialEvents = getInitialEvents().iterator();
			// System.out.println(getInitialEvents(net).toString().concat("****")
			// );
			while (initialEvents.hasNext()) {
				try {
					AuditTrailEntryList NewAteList = new AuditTrailEntryListImpl();
					List<String> newVisitedThisInstance = new ArrayList<String>();
					// getNextObjects(NewAteList,newVisitedThisInstance,true,initialEvents.next());
					getNextObjects(NewAteList, true, initialEvents.next());
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
			return outputLog;

		}

		private List<EPCEvent> getInitialEvents() {
			List<EPCEvent> initialEvents = new Vector<EPCEvent>();
			Iterator<EPCEvent> it = epc.getEvents().iterator();
			while (it.hasNext()) {
				EPCEvent f = (EPCEvent) it.next();
				Iterator edges = f.getInEdgesIterator();
				if (!edges.hasNext()) {
					initialEvents.add(f);
				}
			}
			return initialEvents;
		}

		protected void setInitialFunctions(Hashtable<String, Integer> functions) {
			addedInitialFunctions = functions;
			noAddedInitialFunctions = functions.size();
		}

		protected Hashtable<String, Integer> addInitialFunctions() {
			// initial events
			List<EPCEvent> initialEvents = getInitialEvents();
			List<EPCEvent> newInitialEvents = new Vector<EPCEvent>();
			Iterator<EPCEvent> it = initialEvents.iterator();
			while (it.hasNext()) {
				EPCEvent event = (EPCEvent) it.next();
				int id = noAddedInitialFunctions;
				if (addedInitialFunctions.containsKey(event.getIdentifier())) {
					id = addedInitialFunctions.get(event.getIdentifier());
				} else {
					addedInitialFunctions.put(event.getIdentifier(), id);
					noAddedInitialFunctions++;
				}
				LogEvent logEvent = new LogEvent(
						(EPCMERGEADD + "INITIAL" + id), "start");
				EPCFunction f = new EPCFunction(logEvent, epc);
				EPCEvent addedEvent = new EPCEvent(
						(EPCMERGEADD + "INITIAL" + id), epc);
				epc.addFunction(f);
				addedObjects.add(f);
				epc.addEdge(f, event);
				epc.addEvent(addedEvent);
				addedObjects.add(addedEvent);
				newInitialEvents.add(addedEvent);
				epc.addEdge(addedEvent, f);
			}
			calculateVisitedEdges();
			return addedInitialFunctions;
		}

		protected void removeAddedObjects() {
			Iterator<EPCObject> addedObjectIt = addedObjects.iterator();
			while (addedObjectIt.hasNext()) {
				EPCObject o = addedObjectIt.next();
				if (EPCFunction.class.isAssignableFrom(o.getClass())) {
					epc.delFunction((EPCFunction) o);
				}
				if (EPCEvent.class.isAssignableFrom(o.getClass())) {
					epc.delEvent((EPCEvent) o);
				}
			}
			addedObjects.clear();
		}

		protected void setFinalFunctions(Hashtable<String, Integer> functions) {
			addedFinalFunctions = functions;
			noAddedFinalFunctions = functions.size();
		}

		protected Hashtable<String, Integer> addFinalFunctions() {
			// final events:
			List<EPCEvent> finalEvents = new Vector<EPCEvent>();
			Iterator<EPCEvent> it = epc.getEvents().iterator();
			while (it.hasNext()) {
				EPCEvent f = (EPCEvent) it.next();
				Iterator edges = f.getOutEdgesIterator();
				if (!edges.hasNext()) {
					finalEvents.add(f);
				}
			}
			List<EPCEvent> newFinalEvents = new Vector<EPCEvent>();
			it = finalEvents.iterator();
			while (it.hasNext()) {
				EPCEvent event = (EPCEvent) it.next();
				int id = noAddedFinalFunctions;
				if (addedFinalFunctions.containsKey(event.getIdentifier())) {
					id = addedFinalFunctions.get(event.getIdentifier());
				} else {
					addedFinalFunctions.put(event.getIdentifier(), id);
					noAddedFinalFunctions++;
				}
				LogEvent logEvent = new LogEvent((EPCMERGEADD + "FINAL" + id),
						"start");
				EPCFunction f = new EPCFunction(logEvent, epc);
				EPCEvent addedEvent = new EPCEvent(
						(EPCMERGEADD + "FINAL" + id), epc);
				epc.addFunction(f);
				addedObjects.add(f);
				epc.addEdge(event, f);
				epc.addEvent(addedEvent);
				addedObjects.add(addedEvent);
				newFinalEvents.add(addedEvent);
				epc.addEdge(f, addedEvent);
			}
			calculateVisitedEdges();
			return addedFinalFunctions;
		}

		private EPCObject getLeastVisitedTarget(EPCObject source) {
			EPCObject leastVisitedTarget = null;
			int novisits = -1;
			if (visitedEdges.containsKey(source)) {
				Iterator<EPCObject> itTargets = visitedEdges.get(source)
						.keySet().iterator();
				while (itTargets.hasNext()) {
					EPCObject target = (EPCObject) itTargets.next();
					if (visitedEdges.get(source).get(target) < novisits
							|| novisits < 0) {
						novisits = visitedEdges.get(source).get(target);
						leastVisitedTarget = target;
						if (novisits == 0) {
							break;
						}
					}
				}
			}
			return leastVisitedTarget;
		}

		private EPCObject getNextElement(EPCObject o, Boolean foundNew) {
			List<EPCObject> sucEvents = new ArrayList<EPCObject>();
			// List<EPCObject> sucAlternativeEvents = new
			// ArrayList<EPCObject>();
			if (EPCFunction.class.isAssignableFrom(o.getClass())) {
				sucEvents.addAll(epc.getSucceedingEvents((EPCFunction) o));
			}
			if (EPCEvent.class.isAssignableFrom(o.getClass())) {
				sucEvents.addAll(epc.getSucceedingFunctions((EPCEvent) o));
			}
			Iterator<EPCObject> itSucEvents = sucEvents.iterator();
			while (itSucEvents.hasNext()) {
				EPCObject no = itSucEvents.next();
				if (visitedEdges.get(o).get(no) == 0) {
					return no;
				}
			}
			if (foundNew) {
				return getLeastVisitedTarget(o);
			}
			return null;
		}

		private boolean hasNextElement(EPCObject o) {
			List<EPCObject> sucEvents = new ArrayList<EPCObject>();
			// List<EPCObject> sucAlternativeEvents = new
			// ArrayList<EPCObject>();
			if (EPCFunction.class.isAssignableFrom(o.getClass())) {
				sucEvents.addAll(epc.getSucceedingEvents((EPCFunction) o));
			}
			if (EPCEvent.class.isAssignableFrom(o.getClass())) {
				sucEvents.addAll(epc.getSucceedingFunctions((EPCEvent) o));
			}
			if (sucEvents.size() == 0) {
				return false;
			} else {
				return true;
			}

		}

		private AuditTrailEntryList getNextObjects(AuditTrailEntryList ateList,
				Boolean foundNew, EPCObject o) {
			if (EPCFunction.class.isAssignableFrom(o.getClass())) {
				AuditTrailEntry ate = new AuditTrailEntryImpl();
				ate.setElement(o.getIdentifier());
				// System.out.println("New ATE: "+o.getIdentifier());
				ate.setType("start");
				try {
					ateList.append(ate);
				} catch (IOException e) {
					e.printStackTrace();
				}
				setConnectorValues(o, o, true, true, (Integer) NONE,
						(Integer) NONE);
			}

			if (!hasNextElement(o)) {
				try {
					outputLog.startProcessInstance(
							ProcessInstanceType.CHANGE_LOG, Integer
									.toString(instanceID), "", null);
					Iterator itATE = ateList.iterator();
					while (itATE.hasNext()) {
						org.processmining.lib.mxml.AuditTrailEntry ate = promAte2mxmlibAte((AuditTrailEntry) itATE
								.next());
						outputLog.addAuditTrailEntry(ate);
					}
					outputLog.endProcessInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
				// System.out.println("No. Instances in process: "+instances.size());
				instanceID++;
			} else {

				EPCObject no = getNextElement(o, foundNew);

				while (no != null) {
					String edge = o.getIdentifier() + ";" + no.getIdentifier();
					// System.out.println(visited.toString());
					try {
						if (EPCFunction.class.isAssignableFrom(o.getClass())) {
							// System.out.println(edge);
						}
						if (visitedEdges.containsKey(o)
								&& visitedEdges.get(o).containsKey(no)) {
							if (visitedEdges.get(o).get(no) == 0) {
								foundNew = true;
							}
							visitedEdges.get(o).put(no,
									(visitedEdges.get(o)).get(no) + 1);
						}
						AuditTrailEntryList NewAteList = ateList
								.cloneInstance();
						getNextObjects(NewAteList, foundNew, no);
					} catch (Exception e) {
						e.printStackTrace();
					}
					foundNew = false;
					no = getNextElement(o, foundNew);
					if (no != null) {
						System.out.println("back");
					}

				}

			}

			return ateList;

		}

		private AuditTrailEntryList getNextObjects(AuditTrailEntryList ateList,
				List<String> visitedThisInstance, Boolean foundNew, EPCObject o) {

			List<EPCObject> sucAlternativeEvents = new ArrayList<EPCObject>();

			if (EPCFunction.class.isAssignableFrom(o.getClass())) {
				AuditTrailEntry ate = new AuditTrailEntryImpl();
				ate.setElement(o.getIdentifier());
				// System.out.println("New ATE: "+o.getIdentifier());
				ate.setType("start");
				try {
					ateList.append(ate);
				} catch (IOException e) {
					e.printStackTrace();
				}
				setConnectorValues(o, o, true, true, (Integer) NONE,
						(Integer) NONE);
				sucAlternativeEvents.addAll(epc
						.getSucceedingEvents((EPCFunction) o));
			}

			// saes = succeeding alternative events
			// List<EPCObject> saes = new ArrayList<EPCObject>();
			if (EPCEvent.class.isAssignableFrom(o.getClass())) {
				sucAlternativeEvents.addAll(epc
						.getSucceedingFunctions((EPCEvent) o));
			}
			// saes= epc.getOrderedSucceedingObjects(o);

			Boolean foundNewAlready = false;
			// Iterator<EPCObject> nextObject= saes.iterator();
			Iterator<EPCObject> nextObject = sucAlternativeEvents.iterator();
			// boolean second=false;
			if (nextObject.hasNext()) {
				while (nextObject.hasNext()) {
					EPCObject no = nextObject.next();
					String edge = o.getIdentifier() + ";" + no.getIdentifier();
					// System.out.println(visited.toString());
					if (!visited.contains(edge)) {
						try {
							System.out.println("\n From: " + o.getIdentifier());
							System.out.println("\n Not visited yet: "
									+ no.getIdentifier());
							System.out.println(edge);
							visited.add(edge);
							if (visitedEdges.containsKey(o)
									&& visitedEdges.get(o).containsKey(no)) {
								visitedEdges.get(o).put(no,
										(visitedEdges.get(o)).get(no) + 1);
							}
							// System.out.println("EDGE NOT PASSED YET!!!");
							// System.out.println(visited.toString());
							visitedThisInstance.add(edge);
							foundNewAlready = true;
							AuditTrailEntryList NewAteList = ateList
									.cloneInstance();
							List<String> newVisitedThisInstance = new ArrayList<String>();
							newVisitedThisInstance.addAll(visitedThisInstance);
							getNextObjects(NewAteList, newVisitedThisInstance,
									true, no);
							// second=true;
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (visited.contains(edge)
							&& !visitedThisInstance.contains(edge)) {
						// System.out.println("EDGE PASSED, BUT NOT IN THIS INSTANCE!!!");
						// System.out.println("\n Not visited in this instance: "+no.getIdentifier());
						if (no == getLeastVisitedTarget(o)) {
							try {
								// foundNew=true;
								AuditTrailEntryList NewAteList = ateList
										.cloneInstance();
								visitedThisInstance.add(edge);
								if (visitedEdges.containsKey(o)
										&& visitedEdges.get(o).containsKey(no)) {
									visitedEdges.get(o).put(no,
											(visitedEdges.get(o)).get(no) + 1);
								}
								List<String> newVisitedThisInstance = new ArrayList<String>();
								newVisitedThisInstance
										.addAll(visitedThisInstance);
								getNextObjects(NewAteList,
										newVisitedThisInstance, foundNew, no);
								foundNew = false;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (visited.contains(edge)
							&& visitedThisInstance.contains(edge) && foundNew) {
						// System.out.println("VISITED THIS INSTANCE BUT FOUND NEW!!!");
						// System.out.println("\n visited, but new node: "+o.getIdentifier());
						if (no == getLeastVisitedTarget(o)) {
							// System.out.println("NO ALTERNATIVE!!!");
							// System.out.println("\n No alternative "+o.getIdentifier());
							try {
								if (visitedEdges.containsKey(o)
										&& visitedEdges.get(o).containsKey(no)) {
									visitedEdges.get(o).put(no,
											(visitedEdges.get(o)).get(no) + 1);
								}
								// foundNew=false;
								visitedThisInstance.add(edge);
								AuditTrailEntryList NewAteList = ateList
										.cloneInstance();
								List<String> newVisitedThisInstance = new ArrayList<String>();
								newVisitedThisInstance
										.addAll(visitedThisInstance);
								getNextObjects(NewAteList,
										newVisitedThisInstance, foundNew, no);
								foundNew = false;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}

				}

			} else {
				// System.out.println("\n ADDING PROCESS INSTANCE");
				// System.out.println(ateList.toString());

				// ProcessInstanceImpl pi =new ProcessInstanceImpl("Test",
				// ateList, new ArrayList<String>());
				// pi.setName(Integer.toString(instanceID));

				try {
					outputLog.startProcessInstance(
							ProcessInstanceType.CHANGE_LOG, Integer
									.toString(instanceID), "", null);
					Iterator itATE = ateList.iterator();
					while (itATE.hasNext()) {
						org.processmining.lib.mxml.AuditTrailEntry ate = promAte2mxmlibAte((AuditTrailEntry) itATE
								.next());
						outputLog.addAuditTrailEntry(ate);
					}
					outputLog.endProcessInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
				// System.out.println("No. Instances in process: "+instances.size());
				instanceID++;

			}

			return ateList;

		}

		protected org.processmining.lib.mxml.AuditTrailEntry promAte2mxmlibAte(
				AuditTrailEntry promAte) {
			org.processmining.lib.mxml.AuditTrailEntry mxmlibAte = new org.processmining.lib.mxml.AuditTrailEntry();
			mxmlibAte.setWorkflowModelElement(promAte.getElement());
			mxmlibAte.setEventType(EventType.getType(promAte.getType()));
			mxmlibAte.setOriginator(promAte.getOriginator());
			if (promAte.getTimestamp() != null) {
				mxmlibAte.setTimestamp(promAte.getTimestamp());
			}
			mxmlibAte.setAttributes(promAte.getAttributes());
			return mxmlibAte;
		}

		private int combineConnectorValues(int con1, int con2) {
			int relationJoinNew = NONE;
			if (con1 == XOR && !(con2 == AND || con2 == OR)) {
				relationJoinNew = XOR;
			} else if (con1 == AND && !(con2 == XOR || con2 == OR)) {
				relationJoinNew = AND;
			} else if (con1 == NONE) {
				relationJoinNew = con2;
			} else {
				relationJoinNew = OR;
			}
			return relationJoinNew;
		}

		protected Hashtable<String, Integer> getSplitConnectorValues() {
			return splitRelation;
		}

		protected Hashtable<String, Integer> getJoinConnectorValues() {
			return joinRelation;
		}

		private void setConnectorValues(EPCObject oo, EPCObject o,
				boolean init, boolean forward, Integer relationJoin,
				Integer relationSplit) {
			int relationJoinNew = relationJoin.intValue();
			int relationSplitNew = relationSplit.intValue();
			// If not Event or function search for event or function
			if (init || !EPCFunction.class.isAssignableFrom(o.getClass())) {
				// System.out.println("Not Event/Function");
				if (EPCConnector.class.isAssignableFrom(o.getClass())) {
					int connectortype = o.getType();
					if (o.getInEdges().size() > 1) {
						relationJoinNew = combineConnectorValues(connectortype,
								relationJoinNew);
					}
					if (o.getOutEdges().size() > 1) {
						relationSplitNew = combineConnectorValues(
								connectortype, relationSplitNew);
					}
				}

				Iterator it = (forward ? o.getOutEdgesIterator() : o
						.getInEdgesIterator());
				while (it.hasNext()) {
					EPCEdge e = ((EPCEdge) it.next());
					EPCObject node = (EPCObject) (forward ? e.getDest() : e
							.getSource());
					setConnectorValues(oo, node, false, forward,
							(Integer) relationJoinNew,
							(Integer) relationSplitNew);

				}
				// if we found a Event or Function, a new list is started that
				// can be used when going "up" again
			} else {
				String key = oo.getIdentifier() + ";" + o.getIdentifier();
				if (relationSplit == NONE) {
					splitRelation.put(key, XOR);
				} else {
					splitRelation.put(key, relationSplit);
				}
				if (relationJoin == NONE) {
					joinRelation.put(key, XOR);
				} else {
					joinRelation.put(key, relationJoin);
				}
				// System.out.println("Split: "+key+" "+relationSplit);
				// System.out.println("Join: "+key+" "+relationJoin);
			}

		}

		protected Hashtable<String, ArrayList<String>> getEventNames() {
			Hashtable<String, ArrayList<String>> eventNames = new Hashtable<String, ArrayList<String>>();

			Iterator<EPCFunction> it = epc.getFunctions().iterator();
			while (it.hasNext()) {
				EPCFunction f = it.next();
				Iterator<EPCEvent> itEvents = ((List<EPCEvent>) epc
						.getPreceedingEvents(f)).iterator();
				while (itEvents.hasNext()) {
					EPCEvent event = itEvents.next();
					Iterator<EPCFunction> targetFunctions = epc
							.getPreceedingFunctions(event).iterator();
					if (targetFunctions.hasNext()) {
						while (targetFunctions.hasNext()) {
							String key = targetFunctions.next().getIdentifier()
									+ ";" + f.getIdentifier();
							if (eventNames.containsKey(key)) {
								ArrayList<String> values = eventNames.get(key);
								Iterator<String> newEventNames = Arrays.asList(
										event.getIdentifier().split(" / "))
										.iterator();
								while (newEventNames.hasNext()) {
									String newEventName = newEventNames.next();
									if (!values.contains(newEventName)) {
										// System.out.println("***"+newEventName+"***");
										values.add(newEventName);
									}
								}
							} else {
								ArrayList<String> values = new ArrayList<String>();
								Iterator<String> newEventNames = Arrays.asList(
										event.getIdentifier().split(" / "))
										.iterator();
								while (newEventNames.hasNext()) {
									String newEventName = newEventNames.next();
									// System.out.println("***"+newEventName+"***");
									values.add(newEventName);
								}
								eventNames.put(key, values);
							}

						}
					} else {
						String key = "NULL;" + f.getIdentifier();
						if (eventNames.containsKey(key)) {
							ArrayList<String> values = eventNames.get(key);
							Iterator<String> newEventNames = Arrays.asList(
									event.getIdentifier().split(" / "))
									.iterator();
							while (newEventNames.hasNext()) {
								String newEventName = newEventNames.next();
								if (!values.contains(newEventName)) {
									// System.out.println("***"+newEventName+"***");
									values.add(newEventName);
								}
							}
						} else {
							ArrayList<String> values = new ArrayList<String>();
							Iterator<String> newEventNames = Arrays.asList(
									event.getIdentifier().split(" / "))
									.iterator();
							while (newEventNames.hasNext()) {
								String newEventName = newEventNames.next();
								if (!values.contains(newEventName)) {
									// System.out.println("***"+newEventName+"***");
									values.add(newEventName);
								}
							}
							eventNames.put(key, values);
						}

					}

				}
				itEvents = ((List<EPCEvent>) epc.getSucceedingEvents(f))
						.iterator();
				while (itEvents.hasNext()) {
					EPCEvent event = itEvents.next();
					Iterator<EPCFunction> targetFunctions = epc
							.getSucceedingFunctions(event).iterator();
					if (targetFunctions.hasNext()) {
						while (targetFunctions.hasNext()) {
							String key = f.getIdentifier() + ";"
									+ targetFunctions.next().getIdentifier();
							if (eventNames.containsKey(key)) {
								ArrayList<String> values = eventNames.get(key);
								Iterator<String> newEventNames = Arrays.asList(
										event.getIdentifier().split(" / "))
										.iterator();
								while (newEventNames.hasNext()) {
									String newEventName = newEventNames.next();
									if (!values.contains(newEventName)) {
										// System.out.println("***"+newEventName+"***");
										values.add(newEventName);
									}
								}
							} else {
								ArrayList<String> values = new ArrayList<String>();
								Iterator<String> newEventNames = Arrays.asList(
										event.getIdentifier().split(" / "))
										.iterator();
								while (newEventNames.hasNext()) {
									String newEventName = newEventNames.next();
									// System.out.println("***"+newEventName+"***");
									values.add(newEventName);
								}
								eventNames.put(key, values);
							}

						}
					} else {
						String key = f.getIdentifier() + ";NULL";
						if (eventNames.containsKey(key)) {
							ArrayList<String> values = eventNames.get(key);
							Iterator<String> newEventNames = Arrays.asList(
									event.getIdentifier().split(" / "))
									.iterator();
							while (newEventNames.hasNext()) {
								String newEventName = newEventNames.next();
								if (!values.contains(newEventName)) {
									// System.out.println("***"+newEventName+"***");
									values.add(newEventName);
								}
							}
						} else {
							ArrayList<String> values = new ArrayList<String>();
							Iterator<String> newEventNames = Arrays.asList(
									event.getIdentifier().split(" / "))
									.iterator();
							while (newEventNames.hasNext()) {
								String newEventName = newEventNames.next();
								// System.out.println("***"+newEventName+"***");
								values.add(newEventName);
							}
							eventNames.put(key, values);
						}

					}
				}

			}
			return eventNames;
		}

	}

	private void combineEventNames(EPCStructure net1, EPCStructure net2) {
		combinedEventNames = new Hashtable<String, List<String>>();
		Hashtable<String, ArrayList<String>> eventNames = net1.getEventNames();
		Enumeration<String> events = eventNames.keys();
		while (events.hasMoreElements()) {
			String key = (String) events.nextElement();
			combinedEventNames.put(key, eventNames.get(key));
		}
		eventNames = net2.getEventNames();
		events = eventNames.keys();
		while (events.hasMoreElements()) {
			String key = (String) events.nextElement();
			if (combinedEventNames.containsKey(key)) {
				List<String> values = combinedEventNames.get(key);
				Iterator<String> itEventNames = eventNames.get(key).iterator();
				while (itEventNames.hasNext()) {
					String eventName = itEventNames.next();
					if (!values.contains(eventName)) {
						values.add(eventName);
					}
				}
			} else {
				ArrayList<String> values = new ArrayList<String>();
				Iterator<String> itEventNames = eventNames.get(key).iterator();
				while (itEventNames.hasNext()) {
					values.add(itEventNames.next());
				}
				combinedEventNames.put(key, values);
			}
		}

	}

	private void combineConnectorValues(EPCStructure net1, EPCStructure net2) {
		combinedJoinValues = new Hashtable<String, Integer>();
		Enumeration<String> connectors = net1.getJoinConnectorValues().keys();
		while (connectors.hasMoreElements()) {
			String key = (String) connectors.nextElement();
			combinedJoinValues.put(key, net1.getJoinConnectorValues().get(key));
		}
		connectors = net2.getJoinConnectorValues().keys();
		while (connectors.hasMoreElements()) {
			String key = (String) connectors.nextElement();
			if (combinedJoinValues.containsKey(key)) {
				int connectorType = combinedJoinValues.get(key);
				int relationNew = net2.getJoinConnectorValues().get(key);
				if (connectorType == XOR
						&& !(relationNew == AND || relationNew == OR)) {
					combinedJoinValues.put(key, XOR);
				} else if (connectorType == AND
						&& !(relationNew == XOR || relationNew == OR)) {
					String name = key.substring(key.indexOf(";") + 1);
					ArrayList<String> preceedingFunctions1 = new ArrayList<String>();
					Iterator<EPCFunction> fIt = net1.getEPC().getAllFunctions(
							name).iterator();
					while (fIt.hasNext()) {
						Iterator<EPCFunction> preFunIt = net1.getEPC()
								.getPreceedingFunctions(fIt.next()).iterator();
						while (preFunIt.hasNext()) {
							preceedingFunctions1.add(preFunIt.next()
									.getIdentifier());
						}
					}
					ArrayList<String> preceedingFunctions2 = new ArrayList<String>();
					fIt = net2.getEPC().getAllFunctions(name).iterator();
					while (fIt.hasNext()) {
						Iterator<EPCFunction> preFunIt = net2.getEPC()
								.getPreceedingFunctions(fIt.next()).iterator();
						while (preFunIt.hasNext()) {
							preceedingFunctions2.add(preFunIt.next()
									.getIdentifier());
						}
					}
					if (preceedingFunctions1.containsAll(preceedingFunctions2)
							&& preceedingFunctions2
									.containsAll(preceedingFunctions1)) {
						combinedJoinValues.put(key, AND);
					} else {
						combinedJoinValues.put(key, OR);
					}
				} else if (connectorType == NONE) {
					combinedJoinValues.put(key, relationNew);
				} else {
					combinedJoinValues.put(key, OR);
				}

			} else {
				combinedJoinValues.put(key, net2.getJoinConnectorValues().get(
						key));
			}
		}

		// SPLIT:
		combinedSplitValues = new Hashtable<String, Integer>();
		connectors = net1.getSplitConnectorValues().keys();
		while (connectors.hasMoreElements()) {
			String key = (String) connectors.nextElement();
			combinedSplitValues.put(key, net1.getSplitConnectorValues()
					.get(key));
		}
		connectors = net2.getSplitConnectorValues().keys();
		while (connectors.hasMoreElements()) {
			String key = (String) connectors.nextElement();
			if (combinedSplitValues.containsKey(key)) {
				int connectorType = combinedSplitValues.get(key);
				int relationNew = net2.getSplitConnectorValues().get(key);
				// System.out.println("Split Connector: "+connectorType);
				if (connectorType == XOR
						&& !(relationNew == AND || relationNew == OR)) {
					combinedSplitValues.put(key, XOR);
				} else if (connectorType == AND
						&& !(relationNew == XOR || relationNew == OR)) {
					String name = key.substring(0, key.indexOf(";"));
					ArrayList<String> succeedingFunctions1 = new ArrayList<String>();

					Iterator<EPCFunction> itF = net1.getEPC().getAllFunctions(
							name).iterator();
					while (itF.hasNext()) {
						Iterator<EPCFunction> preFunIt = net1.getEPC()
								.getSucceedingFunctions(itF.next()).iterator();
						while (preFunIt.hasNext()) {
							succeedingFunctions1.add(preFunIt.next()
									.getIdentifier());
						}
					}
					ArrayList<String> succeedingFunctions2 = new ArrayList<String>();
					itF = net2.getEPC().getAllFunctions(name).iterator();
					while (itF.hasNext()) {
						Iterator<EPCFunction> preFunIt = net2.getEPC()
								.getSucceedingFunctions(itF.next()).iterator();
						while (preFunIt.hasNext()) {
							succeedingFunctions2.add(preFunIt.next()
									.getIdentifier());
						}
					}
					if (succeedingFunctions1.containsAll(succeedingFunctions2)
							&& succeedingFunctions2
									.containsAll(succeedingFunctions1)) {
						combinedSplitValues.put(key, AND);
					} else {
						combinedSplitValues.put(key, OR);
					}
				} else if (connectorType == NONE) {
					combinedSplitValues.put(key, relationNew);
				} else {
					combinedSplitValues.put(key, OR);
				}

			} else {
				combinedSplitValues.put(key, net2.getSplitConnectorValues()
						.get(key));
			}
		}

	}

	public EPCMergeMethod(ConfigurableEPC net1, ConfigurableEPC net2) {
		ConfigurableEPC epc = new ConfigurableEPC();
		HashMap<Long, EPCFunction> org2new = new HashMap<Long, EPCFunction>();
		epc.copyAllFrom(net1, org2new);
		epc1 = new EPCStructure(epc);
		epc = new ConfigurableEPC();
		org2new = new HashMap<Long, EPCFunction>();
		epc.copyAllFrom(net2, org2new);
		epc2 = new EPCStructure(net2);
	}

	public ConfigurableEPC analyse() {
		ArrayList<EPCObject> elementsModel1 = new ArrayList<EPCObject>();
		Iterator<EPCFunction> functions = epc1.getEPC().getFunctions()
				.iterator();
		while (functions.hasNext()) {
			elementsModel1.add(functions.next());
		}
		ArrayList<EPCObject> elementsModel2 = new ArrayList<EPCObject>();
		functions = epc2.getEPC().getFunctions().iterator();
		while (functions.hasNext()) {
			elementsModel2.add(functions.next());
		}

		String tempdir = System.getProperty("java.io.tmpdir", "");
		String filename = "MergeProcess";
		try {
			File outputFile = new File(tempdir);
			LogPersistency outputFilter = new LogPersistencyDir(outputFile);
			outputLog = new LogSetSequentialImpl(outputFilter, "EPC Merge",
					null);
			outputLog.startProcess(filename, "process paths", null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// processes= new ArrayList<ProcessImpl>();
		// instances= new ArrayList<ProcessInstanceImpl>();
		// p = new ProcessImpl("Test", "Test", new DataSection(), new
		// Vector<String>() );

		// deriving process instances

		addedInitialFunctions = epc1.addInitialFunctions();
		addedFinalFunctions = epc1.addFinalFunctions();
		outputLog = epc1.addPI(outputLog);

		epc2.setInitialFunctions(addedInitialFunctions);
		epc2.setFinalFunctions(addedFinalFunctions);
		addedInitialFunctions = epc2.addInitialFunctions();
		addedFinalFunctions = epc2.addFinalFunctions();
		outputLog = epc2.addPI(outputLog);

		try {
			outputLog.endProcess();
			outputLog.finish();
			File outputFile = new File(tempdir, filename + ".mxml");
			LogFile logFile = LogFile.getInstance(outputFile.getAbsolutePath());
			log = BufferedLogReader.createInstance(new DefaultLogFilter(
					DefaultLogFilter.INCLUDE), logFile);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Multi phase Miner:
		Progress progress = new Progress("Merging EPCs");
		LogRelations logRelations = getLogRelations(log, progress);
		progress.setMaximum(progress.getMaximum() + log.numberOfInstances() * 2
				+ 1);
		PartialOrderGeneratorPlugin poGen = new PartialOrderGeneratorPlugin();
		PartialOrderMiningResult poGenResult = (PartialOrderMiningResult) poGen
				.mineWithProgressSet(log, logRelations, progress);
		PartialOrderAggregationPlugin poAgg = new PartialOrderAggregationPlugin();
		AggregationGraphResult aggResult = (AggregationGraphResult) poAgg.mine(
				poGenResult.getLogReader(), progress, false);

		progress.setNote("Converting to EPC");
		AggregationGraphToEPC ag2epc = new AggregationGraphToEPC();
		EPCResult result = (EPCResult) ag2epc.convert(aggResult
				.getProvidedObjects()[0]);
		resultingEPC = result.getEPC();
		resultingEPC.setValidating(false);

		// Replace connector values:

		combineConnectorValues(epc1, epc2);
		List<EPCConnector> connectors = resultingEPC.getConnectors();
		Iterator<EPCConnector> connectorsIt = connectors.iterator();
		while (connectorsIt.hasNext()) {
			EPCConnector connector = (EPCConnector) connectorsIt.next();
			int splitConnectorType = NONE;
			int joinConnectorType = NONE;
			List<EPCFunction> sfs = resultingEPC
					.getSucceedingFunctions(connector);
			List<EPCFunction> pfs = resultingEPC
					.getPreceedingFunctions(connector);
			Iterator<EPCFunction> sfIt = sfs.iterator();
			while (sfIt.hasNext()) {
				EPCFunction sf = (EPCFunction) sfIt.next();
				Iterator<EPCFunction> pfIt = pfs.iterator();
				while (pfIt.hasNext()) {
					EPCFunction pf = (EPCFunction) pfIt.next();
					if (pf.getIdentifier().length() > 6
							&& sf.getIdentifier().length() > 6) {
						String key = pf.getIdentifier().substring(0,
								pf.getIdentifier().length() - 7)
								+ ";"
								+ sf.getIdentifier().substring(0,
										sf.getIdentifier().length() - 7);
						if (combinedSplitValues.containsKey(key)
								&& connector.getOutEdges().size() > 1) {
							if (splitConnectorType == NONE
									&& combinedSplitValues.get(key) == AND) {
								splitConnectorType = AND;
							} else if (splitConnectorType == NONE
									&& combinedSplitValues.get(key) == XOR) {
								splitConnectorType = XOR;
							} else if (splitConnectorType == XOR
									&& combinedSplitValues.get(key) == AND) {
								splitConnectorType = OR;
							} else if (splitConnectorType == AND
									&& combinedSplitValues.get(key) == XOR) {
								splitConnectorType = OR;
							} else if (combinedSplitValues.get(key) == OR) {
								splitConnectorType = OR;
							}
						}
						if (connector.getInEdges().size() > 1
								&& combinedJoinValues.containsKey(key)) {
							if (joinConnectorType == NONE
									&& combinedJoinValues.get(key) == AND) {
								joinConnectorType = AND;
							} else if (joinConnectorType == NONE
									&& combinedJoinValues.get(key) == XOR) {
								joinConnectorType = XOR;
							} else if (joinConnectorType == XOR
									&& combinedJoinValues.get(key) == AND) {
								joinConnectorType = OR;
							} else if (joinConnectorType == AND
									&& combinedJoinValues.get(key) == XOR) {
								joinConnectorType = OR;
							} else if (combinedJoinValues.get(key) == OR) {
								joinConnectorType = OR;
							}
						}

					}

				}

			}
			if (connector.getOutEdges().size() > 1
					&& splitConnectorType != NONE) {
				connector.setType(splitConnectorType);
			}
			if (connector.getInEdges().size() > 1 && joinConnectorType != NONE) {
				connector.setType(joinConnectorType);
			}

		}

		// MOVE Events before connector
		List<EPCConnector> cons = resultingEPC.getConnectors();
		Iterator<EPCConnector> conIt = cons.iterator();
		while (conIt.hasNext()) {
			EPCConnector con = conIt.next();
			if (con.getInEdges().size() > 1) {
				Iterator<EPCEdge> it = (Iterator<EPCEdge>) con
						.getOutEdgesIterator();
				while (it.hasNext()) {
					EPCEdge edge = it.next();
					EPCEvent event = (EPCEvent) edge.getDest();
					Iterator<EPCEdge> itOutEdges = (Iterator<EPCEdge>) event
							.getOutEdgesIterator();
					while (itOutEdges.hasNext()) {
						EPCEdge outEdge = itOutEdges.next();
						EPCObject object = (EPCObject) outEdge.getDest();
						event.setIdentifier(event.getIdentifier()
								+ object.getIdentifier());
						resultingEPC.addEdge(con, object);
						// EPCEdge edgeAdded = resultingEPC.addEdge(con,
						// object);

						// resultingEPC.delEdge(event, object);
					}
					Iterator<EPCEdge> itInEdges = (Iterator<EPCEdge>) con
							.getInEdgesIterator();
					while (itInEdges.hasNext()) {
						EPCEdge inEdge = itInEdges.next();
						EPCObject object = (EPCObject) inEdge.getSource();
						EPCEvent event2 = new EPCEvent(event.getIdentifier(),
								resultingEPC);
						resultingEPC.addEvent(event2);
						resultingEPC.addEdge(event2, con);
						resultingEPC.addEdge(object, event2);
						resultingEPC.delEdge(object, con);
					}
					resultingEPC.delEdge(con, event);
					resultingEPC.delEvent(event);

				}

			}

		}

		// Remove beginning:
		EPCEvent startEvent = resultingEPC.getEvent("status change to\\nf0");
		Iterator<EPCFunction> fIt = resultingEPC.getAllFunctions("f0")
				.iterator();
		while (fIt.hasNext()) {
			EPCFunction startFunction = fIt.next();
			if (startEvent != null && startFunction != null) {
				resultingEPC.delEdge(startEvent, startFunction);
				resultingEPC.delEvent(startEvent);
				List<EPCObject> startObjects = resultingEPC
						.getSucceedingElements(startFunction);
				Iterator<EPCObject> it = startObjects.iterator();
				while (it.hasNext()) {
					EPCObject nextObject = it.next();
					if (EPCEvent.class.isAssignableFrom(nextObject.getClass())) {
						resultingEPC.delEdge(startFunction, nextObject);
					}
					if (EPCConnector.class.isAssignableFrom(nextObject
							.getClass())) {
						if (nextObject.getInEdges().size() > 1) {
							resultingEPC.delEdge(startFunction, nextObject);
						} else {
							Iterator itOutEdges = nextObject
									.getOutEdgesIterator();
							while (itOutEdges.hasNext()) {
								EPCEdge e = ((EPCEdge) itOutEdges.next());
								EPCObject node = (EPCObject) e.getDest();
								resultingEPC.delEdge(nextObject, node);
							}
							resultingEPC
									.delConnector((EPCConnector) nextObject);
						}
					}

				}
				resultingEPC.delFunction(startFunction);

			}
		}

		// Remove end:
		EPCEvent endEvent = resultingEPC.getEvent("f1\\n finished");
		fIt = resultingEPC.getAllFunctions("f1").iterator();
		while (fIt.hasNext()) {
			EPCFunction endFunction = fIt.next();
			if (endEvent != null && endFunction != null) {
				resultingEPC.delEdge(endFunction, endEvent);
				resultingEPC.delEvent(endEvent);
				List<EPCObject> endObjects = resultingEPC
						.getPreceedingElements(endFunction);
				Iterator<EPCObject> it = endObjects.iterator();
				while (it.hasNext()) {
					EPCObject nextObject = it.next();
					if (EPCEvent.class.isAssignableFrom(nextObject.getClass())) {
						resultingEPC.delEdge(nextObject, endFunction);
					}
					if (EPCConnector.class.isAssignableFrom(nextObject
							.getClass())) {
						if (nextObject.getOutEdges().size() > 1) {
							resultingEPC.delEdge(nextObject, endFunction);
						} else {
							Iterator<EPCEdge> itInEdges = nextObject
									.getInEdgesIterator();
							while (itInEdges.hasNext()) {
								EPCEdge e = ((EPCEdge) itInEdges.next());
								EPCObject node = (EPCObject) e.getSource();
								resultingEPC.delEdge(node, nextObject);
							}
							resultingEPC
									.delConnector((EPCConnector) nextObject);
						}
					}

				}
				resultingEPC.delFunction(endFunction);

			}
		}

		// Remove "start" from function names
		Iterator<EPCFunction> it = resultingEPC.getFunctions().iterator();
		while (it.hasNext()) {
			EPCFunction f = (EPCFunction) it.next();
			if (f.getIdentifier().length() > 7) {
				String identifier = f.getIdentifier().substring(0,
						f.getIdentifier().length() - 7);
				f.setIdentifier(identifier);
			}

		}

		// Change event names to original values:
		combineEventNames(epc1, epc2);
		List<EPCEvent> eventList = new ArrayList<EPCEvent>(resultingEPC
				.getEvents());
		Iterator<EPCEvent> itEvents = eventList.iterator();
		while (itEvents.hasNext()) {
			List<String> values = new ArrayList<String>();
			EPCEvent event = (EPCEvent) itEvents.next();
			Iterator<EPCFunction> preFunctions = resultingEPC
					.getPreceedingFunctions(event).iterator();
			if (preFunctions.hasNext()) {
				while (preFunctions.hasNext()) {
					EPCFunction preFunction = preFunctions.next();
					Iterator<EPCFunction> postFunctions = resultingEPC
							.getSucceedingFunctions(event).iterator();
					if (postFunctions.hasNext()) {
						while (postFunctions.hasNext()) {
							EPCFunction postFunction = postFunctions.next();
							String key = preFunction.getIdentifier() + ";"
									+ postFunction.getIdentifier();
							if (combinedEventNames.containsKey(key)) {
								Iterator<String> itEventNames = combinedEventNames
										.get(key).iterator();
								while (itEventNames.hasNext()) {
									String eventName = itEventNames.next();
									if (!values.contains(eventName)) {
										values.add(eventName);
									}
								}

							}

						}
					} else {
						String key = preFunction.getIdentifier() + ";NULL";
						if (combinedEventNames.containsKey(key)) {
							Iterator<String> itEventNames = combinedEventNames
									.get(key).iterator();
							while (itEventNames.hasNext()) {
								String eventName = itEventNames.next();
								if (!values.contains(eventName)) {
									values.add(eventName);
								}
							}

						}

					}

				}
			} else {
				Iterator<EPCFunction> postFunctions = resultingEPC
						.getSucceedingFunctions(event).iterator();
				if (postFunctions.hasNext()) {
					while (postFunctions.hasNext()) {
						EPCFunction postFunction = postFunctions.next();
						String key = "NULL;" + postFunction.getIdentifier();
						if (combinedEventNames.containsKey(key)) {
							Iterator<String> itEventNames = combinedEventNames
									.get(key).iterator();
							while (itEventNames.hasNext()) {
								String eventName = itEventNames.next();
								if (!values.contains(eventName)) {
									values.add(eventName);
								}
							}

						}

					}
				}
			}
			if (values.size() > 0) {
				boolean add = false;
				boolean first = true;
				String eventName = "";
				Iterator<String> itValues = values.iterator();
				ArrayList<EPCConnector> preConnectors = new ArrayList<EPCConnector>();
				ArrayList<EPCConnector> sucConnectors = new ArrayList<EPCConnector>();
				ArrayList<String> events = new ArrayList<String>();
				EPCConnector preConnector = null;
				EPCConnector postConnector = null;
				while (itValues.hasNext()) {
					if (add) {
						if (first) {
							// EPCConnector sucConnector = new
							// EPCConnector(NONE, resultingEPC);
							Iterator<EPCEdge> inEdges = event
									.getInEdgesIterator();
							while (inEdges.hasNext()) {
								EPCEdge inEdge = inEdges.next();
								preConnector = new EPCConnector(NONE,
										resultingEPC);
								preConnectors.add(preConnector);
								resultingEPC.addConnector(preConnector);
								EPCObject preObject = (EPCObject) inEdge
										.getSource();
								resultingEPC.addEdge(preObject, preConnector);
								resultingEPC.addEdge(preConnector, event);
								resultingEPC.delEdge(preObject, event);
							}
							Iterator<EPCEdge> outEdges = event
									.getOutEdgesIterator();
							while (outEdges.hasNext()) {
								EPCEdge outEdge = outEdges.next();
								postConnector = new EPCConnector(NONE,
										resultingEPC);
								sucConnectors.add(postConnector);
								resultingEPC.addConnector(postConnector);
								EPCObject postObject = (EPCObject) outEdge
										.getDest();
								resultingEPC.addEdge(postConnector, postObject);
								resultingEPC.addEdge(event, postConnector);
								resultingEPC.delEdge(event, postObject);
							}
							first = false;

						}
						EPCEvent newEvent = new EPCEvent(itValues.next(),
								resultingEPC);
						resultingEPC.addEvent(newEvent);
						events.add(newEvent.getIdentifier());
						Iterator<EPCConnector> itInConnector = preConnectors
								.iterator();
						while (itInConnector.hasNext()) {
							resultingEPC
									.addEdge(itInConnector.next(), newEvent);
						}
						Iterator<EPCConnector> itOutConnector = sucConnectors
								.iterator();
						while (itOutConnector.hasNext()) {
							resultingEPC.addEdge(newEvent, itOutConnector
									.next());
						}
					} else {
						eventName += itValues.next();
						event.setIdentifier(eventName);
						events.add(event.getIdentifier());
						add = true;
					}
				}
				if (values.size() > 1) {
					// Preceding connector value
					if (preConnector != null) {
						preConnector.setType(getConnectorValuesForEvents(
								events, PRECEDING));
					}

					// Succeeding connector value
					if (postConnector != null) {
						postConnector.setType(getConnectorValuesForEvents(
								events, SUCCEEDING));
					}
				}

			}

		}

		// Move events after connector if all preceeding events are the same.
		Iterator<EPCConnector> itConnectors = resultingEPC.getConnectors()
				.iterator();
		while (itConnectors.hasNext()) {
			EPCConnector c = itConnectors.next();
			if (c.getInEdges().size() > 1 && c.getOutEdges().size() == 1) {
				boolean identical = true;
				Iterator<EPCObject> itObj = resultingEPC.getPreceedingElements(
						c).iterator();
				if (itObj.hasNext()) {
					EPCObject obj = itObj.next();
					if (EPCEvent.class.isAssignableFrom(obj.getClass())) {
						String id = obj.getIdentifier();
						while (itObj.hasNext()) {
							EPCObject obj2 = itObj.next();
							if ((!obj2.getIdentifier().equals(id))
									|| (!EPCEvent.class.isAssignableFrom(obj2
											.getClass()))) {
								identical = false;
								break;
							}
						}
					} else {
						identical = false;
					}
				}
				if (identical) {
					Iterator<EPCEdge> inEdgesCon = (Iterator<EPCEdge>) c
							.getInEdgesIterator();
					boolean moved = false;
					while (inEdgesCon.hasNext()) {
						EPCEdge e = inEdgesCon.next();
						EPCObject preObject = (EPCObject) e.getSource();
						Iterator<EPCEdge> inEdgesEvent = (Iterator<EPCEdge>) preObject
								.getInEdgesIterator();
						while (inEdgesEvent.hasNext()) {
							EPCEdge inEdgeEvent = inEdgesEvent.next();
							resultingEPC.addEdge((EPCObject) inEdgeEvent
									.getSource(), c);
							resultingEPC.delEdge((EPCObject) inEdgeEvent
									.getSource(), preObject);
							resultingEPC.delEdge(preObject, c);
						}
						if (!moved) {
							Iterator<EPCEdge> outEdgesCon = (Iterator<EPCEdge>) c
									.getOutEdgesIterator();
							while (outEdgesCon.hasNext()) {
								EPCEdge outEdgeCon = outEdgesCon.next();
								resultingEPC.addEdge(preObject,
										(EPCObject) outEdgeCon.getDest());
								resultingEPC.addEdge(c, preObject);
								resultingEPC.delEdge(c, (EPCObject) outEdgeCon
										.getDest());
							}
							moved = true;
						} else {
							if (EPCEvent.class.isAssignableFrom(preObject
									.getClass())) {
								resultingEPC.delEvent((EPCEvent) preObject);
							} else if (EPCConnector.class
									.isAssignableFrom(preObject.getClass())) {
								resultingEPC
										.delConnector((EPCConnector) preObject);
							}

						}

					}

				}
			} else if (c.getInEdges().size() == 1 && c.getOutEdges().size() > 1) {
				boolean identical = true;
				Iterator<EPCObject> itObj = resultingEPC.getSucceedingElements(
						c).iterator();
				if (itObj.hasNext()) {
					EPCObject obj = itObj.next();
					if (EPCEvent.class.isAssignableFrom(obj.getClass())) {
						String id = obj.getIdentifier();
						while (itObj.hasNext()) {
							EPCObject obj2 = itObj.next();
							if ((!obj2.getIdentifier().equals(id))
									|| (!EPCEvent.class.isAssignableFrom(obj2
											.getClass()))) {
								identical = false;
								break;
							}
						}
					} else {
						identical = false;
					}
				}
				if (identical) {
					Iterator<EPCEdge> outEdgesCon = (Iterator<EPCEdge>) c
							.getOutEdgesIterator();
					boolean moved = false;
					while (outEdgesCon.hasNext()) {
						EPCEdge e = outEdgesCon.next();
						EPCObject postObject = (EPCObject) e.getDest();
						Iterator<EPCEdge> outEdgesEvent = (Iterator<EPCEdge>) postObject
								.getOutEdgesIterator();
						while (outEdgesEvent.hasNext()) {
							EPCEdge outEdgeEvent = outEdgesEvent.next();
							resultingEPC.addEdge(c, (EPCObject) outEdgeEvent
									.getDest());
							resultingEPC.delEdge(postObject,
									(EPCObject) outEdgeEvent.getDest());
							resultingEPC.delEdge(c, postObject);
						}
						if (!moved) {
							Iterator<EPCEdge> inEdgesCon = (Iterator<EPCEdge>) c
									.getInEdgesIterator();
							while (inEdgesCon.hasNext()) {
								EPCEdge inEdgeCon = inEdgesCon.next();
								resultingEPC.addEdge((EPCObject) inEdgeCon
										.getSource(), postObject);
								resultingEPC.addEdge(postObject, c);
								resultingEPC.delEdge((EPCObject) inEdgeCon
										.getSource(), c);
							}
							moved = true;
						} else {
							if (EPCEvent.class.isAssignableFrom(postObject
									.getClass())) {
								resultingEPC.delEvent((EPCEvent) postObject);
							} else if (EPCConnector.class
									.isAssignableFrom(postObject.getClass())) {
								resultingEPC
										.delConnector((EPCConnector) postObject);
							}

						}

					}

				}
			}
		}

		removeAddedFunctions(resultingEPC);
		epc1.removeAddedObjects();
		epc2.removeAddedObjects();

		return resultingEPC;

	}

	private LogRelations getLogRelations(LogReader log, Progress progress) {
		boolean useFsm = false;
		boolean usePO = false;
		LogAbstraction logAbstraction;
		LogRelations relations;
		String[][] intervals;

		Message.add("Starting log filtering...", Message.DEBUG);
		if (progress != null) {
			progress.setMinMax(0, useFsm ? 5 : 4);
		}

		// First layer: abstract from log, make direct succession and model
		// elements
		// message("Starting log abstraction: building >                             ",
		// 1, progress);
		logAbstraction = new LogAbstractionImpl(log, usePO);

		if (progress != null && progress.isCanceled()) {
			return null;
		}

		// Second layer: Abstract from succession and build causal and parallel
		// relations
		// message("Starting log abstraction: building -> and ||", 2, progress);
		relations = (new MinValueLogRelationBuilder(logAbstraction, 0, log
				.getLogSummary().getLogEvents())).getLogRelations();

		if (progress != null && progress.isCanceled()) {
			return null;
		}

		// Third layer: Use Finite State Machine to insert causality
		if (useFsm) {
			// message("Starting log abstraction: building -> based on FSM", 3,
			// progress);
			relations = (new FSMLogRelationBuilder(relations))
					.getLogRelations();
		}

		if (progress != null && progress.isCanceled()) {
			return null;
		}

		// Fourth layer: Use log ordering to determine paralellism and causality
		// message("Starting log abstraction: building || based on overlap", 4,
		// progress);
		intervals = (new String[0][0]);
		for (int i = 0; i < intervals.length; i++) {
			relations = (new TimeIntervalLogRelationBuilder(relations, log,
					intervals[i][0], intervals[i][1])).getLogRelations();

			if (progress != null && progress.isCanceled()) {
				return null;
			}
		}

		if (progress.isCanceled()) {
			progress.close();
			return null;
		}
		progress.close();
		return relations;
	}

	private int getConnectorValuesForEvents(List<String> events,
			boolean succeedingCon) {
		int relation = NONE;
		Integer relation1 = epc1.getEventRelation(events, succeedingCon);
		Integer relation2 = epc2.getEventRelation(events, succeedingCon);
		if (relation1 == null && relation2 != null) {
			relation = relation2.intValue();
		} else if (relation2 == null && relation1 != null) {
			relation = relation1.intValue();
		} else if ((relation1.intValue() == XOR || relation1.intValue() == NONE)
				&& !(relation2.intValue() == AND || relation2.intValue() == OR)) {
			relation = XOR;
		} else if (relation1.intValue() == AND
				&& !(relation2.intValue() == XOR || relation2.intValue() == OR)
				&& epc1.getAlternativeEvents(events, succeedingCon)
						.containsAll(
								epc2
										.getAlternativeEvents(events,
												succeedingCon))
				&& epc2.getAlternativeEvents(events, succeedingCon)
						.containsAll(
								epc1
										.getAlternativeEvents(events,
												succeedingCon))) {
			relation = AND;
		} else {
			relation = OR;
		}
		return relation;

	}

	private void removeAddedFunctions(ConfigurableEPC epc) {
		for (int i = 0; i < addedInitialFunctions.size(); i++) {
			Iterator<EPCFunction> fIt = epc.getAllFunctions(
					EPCMERGEADD + "INITIAL" + i + "\\nstart").iterator();
			while (fIt.hasNext()) {
				EPCFunction f = fIt.next();
				Iterator<EPCObject> connectors = (Iterator<EPCObject>) f
						.getPredecessors().iterator();
				while (connectors.hasNext()) {
					EPCObject connector = connectors.next();
					if (EPCConnector.class.isAssignableFrom(connector
							.getClass())) {
						Iterator<EPCEvent> initialEvent = (Iterator<EPCEvent>) connector
								.getPredecessors().iterator();
						while (initialEvent.hasNext()) {
							epc.delEvent(initialEvent.next());
						}
						epc.delConnector((EPCConnector) connector);
					} else if (EPCEvent.class.isAssignableFrom(connector
							.getClass())) {
						epc.delEvent((EPCEvent) connector);
					}

				}
				epc.delFunction(f);
			}
		}
		for (int i = 0; i < addedFinalFunctions.size(); i++) {
			Iterator<EPCFunction> fIt = epc.getAllFunctions(
					EPCMERGEADD + "FINAL" + i + "\\nstart").iterator();
			while (fIt.hasNext()) {
				EPCFunction f = fIt.next();
				Iterator<EPCObject> connectors = (Iterator<EPCObject>) f
						.getSuccessors().iterator();
				while (connectors.hasNext()) {
					EPCObject connector = connectors.next();
					if (EPCConnector.class.isAssignableFrom(connector
							.getClass())) {
						Iterator<EPCEvent> finalEvent = (Iterator<EPCEvent>) connector
								.getSuccessors().iterator();
						while (finalEvent.hasNext()) {
							epc.delEvent(finalEvent.next());
						}
						epc.delConnector((EPCConnector) connector);
					} else if (EPCEvent.class.isAssignableFrom(connector
							.getClass())) {
						epc.delEvent((EPCEvent) connector);
					}

				}
				epc.delFunction(f);
			}
		}

	}

}
