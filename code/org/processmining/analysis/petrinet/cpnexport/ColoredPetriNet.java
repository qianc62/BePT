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

package org.processmining.analysis.petrinet.cpnexport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.analysis.redesign.ui.RedesignAnalysisUI;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.HLTypes.ChoiceEnum;
import org.processmining.framework.models.hlprocess.expr.HLDataExpression;
import org.processmining.framework.models.hlprocess.expr.HLExpressionElement;
import org.processmining.framework.models.hlprocess.expr.HLExpressionManager;
import org.processmining.framework.models.hlprocess.expr.operator.HLAndOperator;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.TransitionCluster;
import org.processmining.framework.ui.Message;

import att.grappa.Element;
import att.grappa.Grappa;
import att.grappa.Node;
import att.grappa.Subgraph;

/**
 * A hierarchical Petri net close to a Coloured Petri net that can be exported
 * to CPN Tools.
 * 
 * It holds a reference to a simulation model {@link HLPetriNet} and a
 * translator object {@link HLToCPNTranslator} that is able to translate the
 * given simulation information to this Coloured Petri net representation of the
 * simulation model.
 * 
 * @author Anne Rozinat
 * @author Ronny Mans
 */
public class ColoredPetriNet extends PetriNet {

	/** The cpnID for this model. */
	private String CpnID = "";
	/**
	 * the instanceID of the page in CPN on which the Simulated PetriNet is
	 * place.
	 */
	private ArrayList<String> pageInstanceIDs = new ArrayList<String>();
	/**
	 * the object containing the high-level information referring to this
	 * process.
	 */
	private HLPetriNet highLevelPN;
	/** the actual high level process information */
	private HLProcess process;
	/**
	 * the translator (high level information to Cpn specific information) for
	 * this process.
	 */
	private HLToCPNTranslator translator;
	/**
	 * Indicator for determining whether the petri net model has been written to
	 * the cpn-file.
	 */
	private boolean pnWrittenToCPN = false;
	private boolean isRedesign = false;

	/**
	 * Constructor to create a ColoredPetriNet from scratch.
	 */
	public ColoredPetriNet() {
		highLevelPN = new HLPetriNet(this);
		translator = new HLToCPNTranslator(highLevelPN, this);
	}

	/**
	 * Constructs a ColoredPetriNet out from a given HLPetri net (i.e., a
	 * PetriNet-based simulation model).
	 * 
	 * @param simModel
	 *            the PetriNet-based simulation model
	 */
	public ColoredPetriNet(HLPetriNet simModel) {
		highLevelPN = simModel;
		process = highLevelPN.getHLProcess();
		translator = new HLToCPNTranslator(highLevelPN, this);
		establishPetriNetStructure((PetriNet) simModel.getProcessModel());
		updateHighLevelInfo();
		evaluateOrderRanks();
	}

	/**
	 * Constructs a ColoredPetriNet out of an ordinary one. <br>
	 * Note that a default simulation model is created in that case.
	 * 
	 * @param net
	 *            the Petri net that is re-established in the simulated net
	 */
	public ColoredPetriNet(PetriNet net) {
		this(new HLPetriNet(net));
	}

	public void isRedesign(boolean isRedesign) {
		this.isRedesign = isRedesign;
	}

	/**
	 * Re-establishes the given Petri net structure for this colored Petri net.
	 * <p>
	 * Replaces ordinary transition, places, and edges by simulated transitions,
	 * places, and edges.
	 * 
	 * @param net
	 *            the given Petri net
	 */
	private void establishPetriNetStructure(PetriNet net) {
		// establish the same petri net structure like in the given model
		HashMap mapping = new HashMap();
		// arraylist for all nodes in the petri net
		ArrayList nodeList = new ArrayList();
		Subgraph graph = net.getGrappaVisualization().getSubgraph().getGraph();
		// get all the nodes that are in a cluster
		Enumeration subGraphElts = graph.subgraphElements();
		while (subGraphElts.hasMoreElements()) {
			Element e1 = (Element) subGraphElts.nextElement();
			if (e1 instanceof Subgraph) {
				Subgraph subgraph = (Subgraph) e1;
				Enumeration enumerationNodes = subgraph.nodeElements();
				// put all the nodeElements in nodeList
				while (enumerationNodes.hasMoreElements()) {
					Element enumNode = (Element) enumerationNodes.nextElement();
					if (enumNode != null
							&& enumNode.object instanceof ModelGraphVertex) {
						nodeList.add(enumNode);
					}
				}
			}
		}
		// add the nodes that are not in a cluster to the list of all nodes
		Enumeration nodeElts = graph.nodeElements();
		while (nodeElts.hasMoreElements()) {
			Element e1 = (Element) nodeElts.nextElement();
			if (e1.object != null && e1.object instanceof ModelGraphVertex) {
				nodeList.add(e1);
			}
		}
		// convert the ordinary transitions to simulated transitions
		Iterator transitions = nodeList.iterator();
		while (transitions.hasNext()) {
			Element e1 = (Element) transitions.next();
			if (e1.object != null && e1.object instanceof Transition) {
				Node n = (Node) e1;
				int x = (int) n.getCenterPoint().getX()
						* ManagerLayout.getInstance().getScaleFactor();
				int y = -(int) n.getCenterPoint().getY()
						* ManagerLayout.getInstance().getScaleFactor();
				int width = (int) (((Double) n
						.getAttributeValue(Grappa.WIDTH_ATTR)).doubleValue() * ManagerLayout
						.getInstance().getStretchFactor());
				int height = (int) (((Double) n
						.getAttributeValue(Grappa.HEIGHT_ATTR)).doubleValue() * ManagerLayout
						.getInstance().getStretchFactor());
				ColoredTransition simTransition = new ColoredTransition(
						(Transition) e1.object, this, x, y, width, height);
				this.addTransition(simTransition);

				// keep the mapping until the edges have been established
				mapping.put((Transition) e1.object, simTransition);
			}
		}
		// convert the ordinary places to simulated places
		Iterator places = nodeList.iterator();
		while (places.hasNext()) {
			Element e1 = (Element) places.next();
			if (e1.object != null && e1.object instanceof Place) {
				Node n = (Node) e1;
				int x = (int) n.getCenterPoint().getX()
						* ManagerLayout.getInstance().getScaleFactor();
				int y = -(int) n.getCenterPoint().getY()
						* ManagerLayout.getInstance().getScaleFactor();
				int width = (int) (((Double) n
						.getAttributeValue(Grappa.WIDTH_ATTR)).doubleValue() * ManagerLayout
						.getInstance().getStretchFactor());
				int height = (int) (((Double) n
						.getAttributeValue(Grappa.HEIGHT_ATTR)).doubleValue() * ManagerLayout
						.getInstance().getStretchFactor());
				ColoredPlace simPlace = new ColoredPlace((Place) e1.object,
						this, x, y, width, height);
				this.addPlace(simPlace);
			}
		}
		// convert the ordinary edges to simulated edges
		Iterator edges = net.getEdges().iterator();
		while (edges.hasNext()) {
			PNEdge edge = (PNEdge) edges.next();
			ColoredEdge simEdge;
			// if place is source
			if (edge.isPT()) {
				Place p = (Place) edge.getSource();
				// find respective place in this net (place names are assumed to
				// be unique)
				Place myPlace = this.findPlace(p.getIdentifier());
				Transition t = (Transition) edge.getDest();
				// find respective transition in this net
				Transition myTransition = (Transition) mapping.get(t);
				// reproduce edge
				simEdge = new ColoredEdge(myPlace, myTransition);
				this.addEdge(simEdge);
			} else {
				// if transition is source
				Place p = (Place) edge.getDest();
				// find respective place in this net (place names are assumed to
				// be unique)
				Place myPlace = (Place) this.findPlace(p.getIdentifier());
				Transition t = (Transition) edge.getSource();
				// find respective transition in this net
				Transition myTransition = (Transition) mapping.get(t);
				// reproduce edge
				simEdge = new ColoredEdge(myTransition, myPlace);
				this.addEdge(simEdge);
			}
		}
		Iterator clusters = net.getClusters().iterator();
		while (clusters.hasNext()) {
			TransitionCluster currentCluster = (TransitionCluster) clusters
					.next();
			this.addCluster(new TransitionCluster(currentCluster));
		}
	}

	/**
	 * Updates the highLevelPN, so that all instances of Transition are replaced
	 * by the corresponding ColoredTransition object and that all instances of
	 * Place are replaced by the corresponding ColoredPlace object
	 * 
	 * @param highLevelPN
	 *            HighLevelProcess
	 */
	private void updateHighLevelInfo() {
		// update activities mapping at high level petri net
		for (HLActivity activity : process.getActivities()) {
			// update the choice node itself
			Transition transNode = (Transition) highLevelPN
					.findModelGraphVertexForActivity(activity.getID());
			ColoredTransition transNsim = (ColoredTransition) this
					.findTransition(transNode);
			highLevelPN
					.replaceModelGraphVertexForActivity(transNode, transNsim);
		}
		// update choices mapping at high level petri net
		for (HLChoice choice : process.getChoices()) {
			// update the choice node itself
			Place choiceNode = (Place) highLevelPN
					.findModelGraphVertexForChoice(choice.getID());
			ColoredPlace choiceNsim = (ColoredPlace) this.findPlace(choiceNode
					.getIdentifier());
			highLevelPN
					.replaceModelGraphVertexForChoice(choiceNode, choiceNsim);
		}
		// update the hlActivity links at the ColoredTransition
		Iterator it = this.getTransitions().iterator();
		while (it.hasNext()) {
			ColoredTransition simTrans = (ColoredTransition) it.next();
			HLActivity hlTrans = highLevelPN.findActivity(simTrans);
			simTrans.setHighLevelTransition(hlTrans);
		}

		// Add the group for all resources to each transition and to the process
		// the group for all resources may only be attached to a transition in
		// the case
		// that no group has been defined for that transition
		ArrayList<HLID> allResourceIDs = new ArrayList<HLID>();
		Iterator<HLResource> resourceIt = process.getResources().iterator();
		while (resourceIt.hasNext()) {
			allResourceIDs.add(resourceIt.next().getID());
		}
	}

	/**
	 * Expands the data expressions according to the order ranks in XOR
	 * semantics (like in YAWL). First generates exclusive expressions in
	 * ascending rank order, and then makes the default expresson to be a
	 * complement of all the other expressions.
	 * <p>
	 * If this should become not always desirable (as e.g., also OR splits -
	 * which have different order rank evaluation semantics - are supported),
	 * then include an option in CPN Export configuration, or move to the
	 * conversion.
	 */
	private void evaluateOrderRanks() {
		// First ensure that the order rank is respected if was provided
		for (HLChoice choice : process.getChoices()) {
			ArrayList<HLCondition> rankedConditions = new ArrayList<HLCondition>();
			// check order rank for each condition
			for (HLCondition cond : choice.getConditions()) {
				if (cond.getExpression().getOrderRank() != -1) {
					rankedConditions.add(cond);
				}
			}
			if (rankedConditions.size() > 1) {
				HashMap<HLDataExpression, HLCondition> exprCondMapping = new HashMap<HLDataExpression, HLCondition>();
				for (HLCondition mappedCond : rankedConditions) {
					exprCondMapping.put(mappedCond.getExpression(), mappedCond);
				}
				ArrayList<HLDataExpression> exprList = new ArrayList<HLDataExpression>(
						exprCondMapping.keySet());
				Object[] sortedRanks = exprList.toArray();
				Arrays.sort(sortedRanks);
				for (int i = 0; i < sortedRanks.length; i++) {
					ArrayList<HLDataExpression> lowerRankExpressions = new ArrayList<HLDataExpression>();
					for (int j = 0; j < sortedRanks.length; j++) {
						if (j < i) {
							lowerRankExpressions
									.add((HLDataExpression) sortedRanks[j]);
						}
					}
					if (lowerRankExpressions.size() > 0) {
						// connect lower rank conditions with OR and negate
						HLExpressionElement orConnected = HLExpressionManager
								.connectExpressionsWithOr(lowerRankExpressions);
						HLExpressionElement negatedOr = HLExpressionManager
								.negateExpression(new HLDataExpression(
										orConnected));
						// add own expression in front and connect with AND
						HLAndOperator andOp = new HLAndOperator();
						andOp
								.addSubExpression(((HLDataExpression) sortedRanks[i])
										.getRootExpressionElement()
										.getExpressionNode());
						andOp.addSubExpression(negatedOr.getExpressionNode());
						// assign the expanded expression to the original
						// condition
						HLCondition condition = exprCondMapping
								.get(sortedRanks[i]);
						condition.setExpression(new HLDataExpression(andOp,
								((HLDataExpression) sortedRanks[i])
										.getOrderRank()));
					}
				}
			}
		}
		// Now, generate data expressions for "default" (i.e., "else") branch
		HLCondition defaultCond = null;
		for (HLChoice choice : process.getChoices()) {
			ArrayList<HLCondition> nonDefaultConditions = new ArrayList<HLCondition>(
					choice.getConditions());
			for (HLCondition cond : choice.getConditions()) {
				if (cond.getExpression().getRootExpressionElement() == null
						&& cond.getExpression().getExpressionString().equals(
								"default")) {
					nonDefaultConditions.remove(cond);
					defaultCond = cond;
				}
			}
			if (defaultCond != null) {
				HLExpressionElement orConnected = HLExpressionManager
						.connectConditionsWithOr(nonDefaultConditions);
				HLExpressionElement negatedOr = HLExpressionManager
						.negateExpression(new HLDataExpression(orConnected));
				defaultCond.setExpression(new HLDataExpression(negatedOr));
			}
		}
	}

	/**
	 * Returns the object containing the high-level information referring to
	 * this process.
	 * 
	 * @return the high-level process information for this Petri net
	 */
	public HLPetriNet getHighLevelProcess() {
		return highLevelPN;
	}

	/**
	 * set the link to the high level process
	 * 
	 * @param hlpn
	 *            HLPetriNet the high level process
	 */
	public void setHighLevelProcess(HLPetriNet hlpn) {
		highLevelPN = hlpn;
	}

	/**
	 * Returns the cpnID for this model.
	 * 
	 * @return String the cpnID for this model. "" is returned if no such cpnID
	 *         exists.
	 */
	public String getCpnID() {
		return this.CpnID;
	}

	/**
	 * Sets the cpnID for this model.
	 * 
	 * @param cpnID
	 *            String the cpnID to be assigned to this model.
	 */
	public void setCpnID(String cpnID) {
		this.CpnID = cpnID;
	}

	/**
	 * Returns the pageInstanceID for the page in CPN on which the Petri Net is
	 * located.
	 * 
	 * @return String the pageInstanceID
	 */
	public ArrayList<String> getPageInstanceIDs() {
		return this.pageInstanceIDs;
	}

	/**
	 * Adds a pageInstanceID for one or more pages in CPN on which the Petri Net
	 * will be located.
	 * 
	 * @param pageInstanceID
	 *            String the pageInstanceID.
	 */
	public void addPageInstanceID(String pageInstanceID) {
		this.pageInstanceIDs.add(pageInstanceID);
	}

	/**
	 * Generates an unique cpnID for all places and all transitions in this
	 * model. No cpnIDs are generated for any places and transitions in a model
	 * that is a submodel of any transition in this model.
	 */
	public void generateCpnIDs() {
		// for all places, generate a cpnID
		Iterator places = this.getPlaces().iterator();
		while (places.hasNext()) {
			ColoredPlace place = (ColoredPlace) places.next();
			place.setCpnID(ManagerID.getNewID());
		}
		// for all transitions, generate a cpnID
		Iterator transitions = this.getTransitions().iterator();
		while (transitions.hasNext()) {
			ColoredTransition transition = (ColoredTransition) transitions
					.next();
			transition.setCpnID(ManagerID.getNewID());
		}
	}

	/**
	 * Writes this high-level Petri net simulation model to the given file
	 * handle. <br>
	 * Note that this is the starting point to invoke the export procedure as
	 * it, e.g., writes the header information of the file. For actually writing
	 * the page containing the transitions and places of this Petri net the
	 * {@link #write write} procedure is invoked.
	 * 
	 * @param bw
	 *            the output stream handle to actually write into the file
	 */
	public void writeToFile(BufferedWriter bw,
			BufferedWriter currentStateWriter, String smlName) {
		try {
			translator.reset(); // needed to enable multiple exports from the
			// same data structure
			ManagerID.reset(); // not needed but nicer to make the generated
			// files more comparable

			// creates simulation overview in a hierarchical way
			// (and links this Petri net as the process page)
			if (this.isRedesign) {
				translator.isRedesign(true);
			}
			ColoredPetriNet environment = translator.translate();

			boolean isTimed = ManagerConfiguration.getInstance()
					.isTimePerspectiveEnabled();
			// write to the file which perspectives are enabled
			Message.add("TimePerspectiveEnabled: " + isTimed, Message.TEST);
			Message.add("DataPerspectiveEnabled: "
					+ ManagerConfiguration.getInstance()
							.isDataPerspectiveEnabled(), Message.TEST);
			Message.add("ResourcePerspectiveEnabled: "
					+ ManagerConfiguration.getInstance()
							.isResourcePerspectiveEnabled(), Message.TEST);
			Message.add("ActivityLoggingEnabled: "
					+ ManagerConfiguration.getInstance()
							.isActivityLoggingEnabled(), Message.TEST);
			Message.add("ThroughputTimeMonitorEnabled: "
					+ ManagerConfiguration.getInstance()
							.isThroughputTimeMonitorEnabled(), Message.TEST);
			Message.add("ResourceMonitorEnabled: "
					+ ManagerConfiguration.getInstance()
							.isResourcesMonitorEnabled(), Message.TEST);

			// write header information
			ManagerXml.writeHeader(bw);

			// write declaration section
			ManagerXml.writeStartGlobbox(bw);
			// write standard declarations (fixed)
			ManagerXml.writeStandDecl(bw);
			// write Control flow declarations (fixed)
			ManagerXml.writeControlFlowDec(bw, translator.variableTranslator
					.getCpnVarForCaseId(), translator.colorsetTranslator
					.getColorSetCaseID(), isTimed);
			// write Redesign declarations (fixed)
			if (isRedesign) {
				// ManagerXml.writeRedesignDec(bw,
				// "C:/RedesignAnalysis/currentSimSettings");
				ManagerXml
						.writeRedesignDec(
								bw,
								RedesignAnalysisUI.locationForCurrentSimSettingsInCPNExport);
			}
			// write Resource and group declarations (do this always in redesign
			// mode)
			if ((ManagerConfiguration.getInstance()
					.isResourcePerspectiveEnabled())
					&& process.getGroups().size() > 0) {
				ArrayList<SubSetColorSet> colorSetGroups = new ArrayList<SubSetColorSet>();
				colorSetGroups.addAll(translator.colorsetTranslator
						.getColorSetsGroups());
				colorSetGroups.add(translator.colorsetTranslator
						.getColorSetForResourcesPlace());
				ArrayList<CpnVarAndType> cpnVarsGroups = new ArrayList<CpnVarAndType>();
				cpnVarsGroups.addAll(translator.variableTranslator
						.getCpnVarsForGroups());
				cpnVarsGroups.add(translator.variableTranslator
						.getCpnVarForGroupAllResources());
				ManagerXml.writeResourceDec(bw, cpnVarsGroups, colorSetGroups);
				// write to the test file
				Message.add("number of cpn vars written for the groups ["
						+ cpnVarsGroups.size() + "]", Message.TEST);
				Message.add("number of color sets written for the groups ["
						+ colorSetGroups.size() + "]", Message.TEST);
			}
			// write Data declarations (optional)
			if (ManagerConfiguration.getInstance().isDataPerspectiveEnabled()
					&& process.getAttributes().size() > 0) {
				ArrayList<CpnVarAndType> cpnVarsDataAttr = new ArrayList<CpnVarAndType>();
				cpnVarsDataAttr.add(translator.variableTranslator
						.getCpnVarForDataAttributes());
				cpnVarsDataAttr.add(translator.variableTranslator
						.getCpnVarForModifiedDataAttributes());
				ManagerXml.writeDataDec(bw, cpnVarsDataAttr,
						translator.colorsetTranslator
								.getColorSetDataAttributes(),
						translator.colorsetTranslator
								.getColorSetsSeparateDataAttributes());
				// write Random value functions
				if (process.getAttributes().size() > 0) {
					ArrayList<CpnFunction> dataAttrCpnFunc = new ArrayList<CpnFunction>();
					Iterator<HLAttribute> dataAttrs = process.getAttributes()
							.iterator();
					while (dataAttrs.hasNext()) {
						HLAttribute dataAttr = dataAttrs.next();
						CpnFunction cpnFunc = translator
								.generateRandomFunctionForDataAttribute(dataAttr);
						dataAttrCpnFunc.add(cpnFunc);
					}
					if (dataAttrCpnFunc.size() > 0) {
						ManagerXml.writeRandomValuesFunctions(bw,
								dataAttrCpnFunc);
					}
				}
				// write to the test file
				Message.add(
						"number of separate color sets written for the data attributes ["
								+ translator.colorsetTranslator
										.getColorSetsSeparateDataAttributes()
										.size() + "]", Message.TEST);
			}
			// write information for the start timestamp of the case (do this
			// always in redesign mode)
			if (ManagerConfiguration.getInstance()
					.isThroughputTimeMonitorEnabled()
					|| isRedesign) {
				ManagerXml.writeStartCase(bw, translator.variableTranslator
						.getCpnVarForStartCase(), translator.colorsetTranslator
						.getColorSetStartCase());
			}
			// write combined declarations for control flow, data declarations,
			// resources
			// when needed or when in redesign mode
			if (isRedesign) {
				ManagerXml.writeProductColorSetsForPlaces(bw,
						translator.colorsetTranslator
								.getProductColorSetsForPlaces(), true);
			} else {
				ManagerXml.writeProductColorSetsForPlaces(bw,
						translator.colorsetTranslator
								.getProductColorSetsForPlaces(), isTimed);
			}
			// write product color set
			Message.add("number of special product color sets for places ["
					+ translator.colorsetTranslator
							.getProductColorSetsForPlaces().size() + "]",
					Message.TEST);

			// write the probabilityvariable, and maybe some other variables
			// that might be needed
			// check whether one of these probability dependencies is allowed
			Iterator choices = process.getChoices().iterator();
			while (choices.hasNext()) {
				HLChoice choice = (HLChoice) choices.next();
				if (choice.getChoiceConfiguration().equals(ChoiceEnum.PROB)) {
					ManagerXml.writePossibilityDependencies(bw,
							translator.variableTranslator
									.getCpnVarsForProbDep());
					break;
				}
			}

			// write current state functions (optional)
			if (ManagerConfiguration.getInstance().isCurrentStateSelected()) {
				ManagerXml.writeCurrentStateDeclarations(bw, smlName);
			}
			// write Logging functions (optional)
			if (ManagerConfiguration.getInstance().isActivityLoggingEnabled()) {
				ManagerXml.writeLogDeclarations(bw, ManagerConfiguration
						.getInstance().getYearOffset(), process.getGlobalInfo()
						.getTimeUnit());
			}
			ManagerXml.writeEndGlobbox(bw);
			// write all the pages starting from the top of the hierarchy
			// There has to be a link from Overview to Environment and a link
			// from Overview to Process
			environment.write(bw, null);
			// write information about fusion places
			ManagerXml.writeFusionPlaces(bw, translator.getFusionPlaces());
			// write fusion places to test log
			Message.add("number of fusion places ["
					+ translator.getFusionPlaces().size() + "]", Message.TEST);
			// write the instances
			ManagerXml.writeInstances(bw, environment);

			// write options and binders
			ManagerXml.writeOptionsBinders(bw);
			// write monitorblock.
			environment.resetWrittenToCPN();
			ManagerXml.writeStartTagMonitorBlock(bw);
			// generate a monitor for the lowest level functions of the process
			// page
			if (ManagerConfiguration.getInstance().isActivityLoggingEnabled()) {
				// use a list for keeping the names that already have been used
				// for a monitor
				ArrayList<String> usedNamesMonitors = new ArrayList<String>();
				environment.writeMonitors(bw, usedNamesMonitors);
				Message
						.add("Activity monitors have been written",
								Message.TEST);
			}
			// write resources monitor (if needed)
			if (ManagerConfiguration.getInstance()
					.isResourcePerspectiveEnabled()
					&& process.getResources().size() > 0
					&& ManagerConfiguration.getInstance()
							.isResourcesMonitorEnabled()) {
				// write the monitor that keeps track of the number of resources
				// in the resource place
				ManagerXml.writeMarkingSizeMonitorResources(bw, this.translator
						.getInfoForMonitoringResources());
				Message.add("Resource monitor has been written", Message.TEST);
			}

			// write the monitor that calculates the throughput time for a case
			// check whether variable d needs to be bounded
			if (ManagerConfiguration.getInstance()
					.isThroughputTimeMonitorEnabled()
					&& !isRedesign) {
				String dataVar = "";
				if (process.getAttributes().size() > 0
						&& ManagerConfiguration.getInstance()
								.isDataPerspectiveEnabled()) {
					dataVar = translator.variableTranslator
							.getCpnVarForDataAttributes().getVarName();
				}
				ManagerXml.writeThroughputTimeMonitor(bw, translator
						.getCpnIdMonitorTroughputTime(), translator
						.getPageinstancesMonitorThroughputTime(), dataVar, this
						.getHighLevelProcess().getHLProcess().getGlobalInfo()
						.getTimeUnit());
				Message.add("Throughput time monitor has been written",
						Message.TEST);
			}
			// finished writing the monitors
			ManagerXml.writeEndTagMonitorBlock(bw);
			// write index node of the cpn-file
			ManagerXml.writeEnd(bw);

			// close the file
			environment.resetWrittenToCPN();
			bw.close();

			// now generate empty current state file if option was selected
			if (currentStateWriter != null) {
				ManagerXml.writeCurrentStateFile(currentStateWriter);
			}
		} catch (IOException ex) {
			Message.add("Problem encountered while writing to file:\n"
					+ ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * Writes the page containing the transitions and places of this Petri net.
	 * 
	 * @param bw
	 *            the output stream handle to write into the file
	 * @param topTransition
	 *            the top transition of the subPage. Null if no such top
	 *            transition exists
	 */
	public void write(BufferedWriter bw, ColoredTransition topTransition) {
		try {
			this.Test(this.getIdentifier());
			// write this Petri net level as one page and invoke the writing of
			// potential subpages
			// write the top of the page
			writeTopOfPage(bw);
			// write places
			// first write the input and output places
			Iterator places = this.getPlaces().iterator();
			while (places.hasNext()) {
				ColoredPlace place = (ColoredPlace) places.next();
				place.write(bw, topTransition);
			}
			// write transitions
			Iterator transitions = this.getTransitions().iterator();
			while (transitions.hasNext()) {
				ColoredTransition transition = (ColoredTransition) transitions
						.next();
				// if (topTransition!=null) {
				// transition.existsDataDependency(topTransition.hasDataDependency());
				// transition.existsProbabilityDependency(topTransition.hasProbabilityDependency());
				// }
				transition.write(bw);
			}

			// write edges
			Iterator edges = this.getEdges().iterator();
			while (edges.hasNext()) {
				ColoredEdge edge = (ColoredEdge) edges.next();
				edge.write(bw);
			}

			// write the end of the page
			ManagerXml.writeEndOfPage(bw);

			pnWrittenToCPN = true;

			// writing subpages, you should not start with writing another page
			// before
			// the writing of the actual page is finished.
			Iterator transitionsSub = this.getTransitions().iterator();
			while (transitionsSub.hasNext()) {
				ColoredTransition transition = (ColoredTransition) transitionsSub
						.next();
				if (transition.getSubpage() != null
						&& !transition.getSubpage().isAlreadyWrittenToCPN()) {
					// create the subpage
					transition.getSubpage().write(bw, transition);
				}
			}
		} catch (IOException ex) {
			Message.add("Problem encountered while writing to file:\n"
					+ ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * Returns whether this colored petri net already has been written to the
	 * cpn file.
	 * 
	 * @return boolean <code>true</code> when this colored petri net is already
	 *         written to the cpn file. <code>false</code> otherwise.
	 */
	public boolean isAlreadyWrittenToCPN() {
		return this.pnWrittenToCPN;
	}

	/**
	 * Retrieves the translator that translator that translates the simulation
	 * information together with the ordinary petri net to a colored petri net
	 * which on its turn can be exported to cpn.
	 * 
	 * @return HLToCPNTranslator the translator
	 */
	protected HLToCPNTranslator getTranslatorToCpn() {
		return translator;
	}

	/**
	 * Sets the translator that can translate the simulation information
	 * together with the ordinary petri net to a colored petri net which on its
	 * turn can be exported to cpn.
	 * 
	 * @param translator
	 *            HLToCPNTranslator the translator.
	 */
	protected void setTranslatorToCpn(HLToCPNTranslator translator) {
		this.translator = translator;
	}

	/**
	 * The monitors for the logging functionality in cpn-tools are written. It
	 * is assumemed that there does not exist a transition with identifier
	 * "Init" on the process page
	 * 
	 * @param bw
	 *            BufferedWriter the output stream handle to write into the file
	 * @param usedNamesMonitors
	 *            the list of names that have already been used for a monitor
	 * @throws IOException
	 */
	private void writeMonitors(BufferedWriter bw,
			ArrayList<String> usedNamesMonitors) throws IOException {
		if (!isAlreadyWrittenToCPN()) {
			Iterator transitions = this.getTransitions().iterator();
			while (transitions.hasNext()) {
				ColoredTransition transition = (ColoredTransition) transitions
						.next();
				if (transition.getSubpage() == null) {
					if (this.getIdentifier().equals("Environment")
							&& transition.getIdentifier().equals("Clean-up")) {
						// don't write a monitor for this transition
					} else if (transition.getIdentifier().equals("Init")) {
						// write the initialisation monitor for this transition
						transition.writeInitMonitor(bw);
						usedNamesMonitors.add("Init");
					} else {
						// write the monitor for this transition
						transition.writeLoggingMonitor(bw, usedNamesMonitors);
					}
				} else { // there exists a subpage
					transition.getSubpage()
							.writeMonitors(bw, usedNamesMonitors);
				}
			}
			this.pnWrittenToCPN = true;
		}
	}

	/**
	 * Writes the start tag for the page element of the cpn-file. Furthermore,
	 * the page id and the pageattr name element are written to the cpn-file.
	 * 
	 * @param bw
	 *            BufferedWriter used to stream the data to the file
	 * @throws IOException
	 */
	private void writeTopOfPage(BufferedWriter bw) throws IOException {
		bw.write("\t\t<page id=\"" + this.getCpnID() + "\">\n");
		bw.write("\t\t\t<pageattr name=\""
				+ CpnUtils.getCpnValidName(this.getIdentifier()) + "\"/>\n");
	}

	/**
	 * Reset whether something is written to the cpn file.
	 */
	private void resetWrittenToCPN() {
		this.pnWrittenToCPN = false;
		Iterator transitions = this.getTransitions().iterator();
		while (transitions.hasNext()) {
			ColoredTransition transition = (ColoredTransition) transitions
					.next();
			if (transition.getSubpage() != null) {
				transition.getSubpage().resetWrittenToCPN();
			}
		}
	}
}
