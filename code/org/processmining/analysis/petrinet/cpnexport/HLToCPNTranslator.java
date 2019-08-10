/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/
package org.processmining.analysis.petrinet.cpnexport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.processmining.analysis.petrinet.cpnexport.hltocpn.ColorSetTranslator;
import org.processmining.analysis.petrinet.cpnexport.hltocpn.CpnAttributeManager;
import org.processmining.analysis.petrinet.cpnexport.hltocpn.VariableTranslator;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.HLTypes.ChoiceEnum;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;

import att.grappa.Grappa;
import att.grappa.Node;

/**
 * Translates the high-level information to the CPN-specific Petri net that can
 * be immediately written to a .cpn file.
 * 
 * @author arozinat
 * @author rmans
 */
public class HLToCPNTranslator {

	public final static String cpnVarNameForCaseId = "c";
	public final static String cpnColorSetNameForCaseId = "CASE_ID";
	public final static String cpnVarNameForStartCase = "t";
	public final static String cpnColorSetNameForStartCase = "START_TIME";
	public final static String cpnVarNameForDataAttributes = "data";
	public final static String cpnVarNameForModifiedDataAttributes = "modifiedData";
	public final static String cpnColorSetNameForDataAttributes = "DATA";
	public final static String cpnVarNameForGroupAllResources = "Anybody";
	public final static String cpnColorSetNameForGroupAllResources = "ANYBODY";
	public final static String cpnVarNameForProbDep = "prob";
	public final static String cpnColorSetNameForProbDep = "INT";
	public final static String namePlaceExecuting = "E";
	public final static String namePlaceWaiting = "W";

	private boolean isRedesign = false;

	/**
	 * the object containing the high-level information referring to this
	 * process (i.e., the source of the translation process).
	 */
	private HLPetriNet highLevelPN;
	/**
	 * the CPN-like object that needs to be filled with the translated
	 * information (i.e., the target of the translation process).
	 */
	private ColoredPetriNet simulatedPN;
	/**
	 * Saves information about the fusion places
	 */
	private HashMap<String, HashSet<ColoredPlace>> fusionPlaces = new HashMap<String, HashSet<ColoredPlace>>();
	/**
	 * the simulation environment for the process
	 */
	private ColoredPetriNet simulationEnvironment;
	/**
	 * Saves the products of the cpnID of a transition and the corresponding
	 * pageinstanceIdRefs. With this information, a data collector monitor can
	 * be generated that keeps tracks of the resources in the resources place
	 */
	private HashMap<String, ArrayList> monitoringResources = new HashMap<String, ArrayList>();
	/**
	 * the cpn id of the function to which the monitor is attached that
	 * calculates the throughput time for each case
	 */
	private String cpnIdMonitorThroughputTime = "";
	/**
	 * the page instanceidref of the page on which the transition is located
	 * which has the monitor that calculates the throughput time for each case.
	 */
	private ArrayList<String> pageInstancesMonitorThroughputTime = new ArrayList<String>();

	protected ColorSetTranslator colorsetTranslator;
	protected VariableTranslator variableTranslator;

	/**
	 * Default constructor
	 * 
	 * @param hlPN
	 *            the PetriNet-based simulation model
	 * @param simPN
	 *            the ordinary colored petri net
	 */
	public HLToCPNTranslator(HLPetriNet hlPN, ColoredPetriNet simPN) {
		highLevelPN = hlPN;
		simulatedPN = simPN;
		colorsetTranslator = new ColorSetTranslator(highLevelPN, simulatedPN);
		variableTranslator = new VariableTranslator(highLevelPN);
	}

	public void isRedesign(boolean isRedesign) {
		this.isRedesign = isRedesign;
	}

	/**
	 * Resets all data structure that are used to write the CPN to file. This is
	 * needed to enable the multiple export of the same ColoredPetriNet
	 * structure (e.g., after changing some simulation parameters).
	 */
	public void reset() {
		fusionPlaces = new HashMap<String, HashSet<ColoredPlace>>();
		monitoringResources = new HashMap<String, ArrayList>();
		pageInstancesMonitorThroughputTime = new ArrayList<String>();
		colorsetTranslator.reset();
	}

	/**
	 * Returns the fusion that are needed when generating the subpage
	 * 
	 * @return HashMap the fusionplaces
	 */
	public HashMap<String, HashSet<ColoredPlace>> getFusionPlaces() {
		return fusionPlaces;
	}

	/**
	 * Retrieves the cpn id of the transition to which the monitor is attached
	 * that calculates the throughput time for each case
	 * 
	 * @return String
	 */
	public String getCpnIdMonitorTroughputTime() {
		return cpnIdMonitorThroughputTime;
	}

	/**
	 * Retrieves the pageinstance idrefs of the page on which the transition is
	 * located which monitor calculates the throughput time for each case
	 * 
	 * @return ArrayList the pageinstanceidrefs
	 */
	public ArrayList<String> getPageinstancesMonitorThroughputTime() {
		return pageInstancesMonitorThroughputTime;
	}

	/**
	 * Returns the declaration of a function that can be used in cpn for
	 * generating random values (between some range) for the given data
	 * attribute.
	 * 
	 * @param dataAttr
	 *            HLAttribute the data attribute for which we want to obtain a
	 *            function that generates random values between some range
	 * @return CpnFunction a declaration of a function that generates random
	 *         values
	 */
	public CpnFunction generateRandomFunctionForDataAttribute(
			HLAttribute dataAttr) {
		return new CpnFunction(dataAttr);
	}

	/**
	 * Creates a simulation environment for the given net.
	 * 
	 * @param net
	 *            ColoredPetriNet the net for which a simulation environment
	 *            needs to be created
	 * @return ColoredPetriNet the (hierarchical) simulation environment
	 */
	private ColoredPetriNet createSimulationEnvironment(ColoredPetriNet net) {
		// If the logging is enabled the types of some places needs to be
		// changed and the
		// inscription of some arcs needs to be changed
		CpnColorSet colorSetPlace = null;
		String arcInscriptionEdge = "";
		if (ManagerConfiguration.getInstance().isThroughputTimeMonitorEnabled()) {
			ArrayList<CpnColorSet> productColorSets = new ArrayList<CpnColorSet>();
			productColorSets.add(colorsetTranslator.getColorSetCaseID());
			productColorSets.add(colorsetTranslator.getColorSetStartCase());
			colorSetPlace = colorsetTranslator
					.productCpnColorSet(productColorSets);
			// arc inscription
			arcInscriptionEdge = "("
					+ variableTranslator.getCpnVarForCaseId().getVarName()
					+ ","
					+ variableTranslator.getCpnVarForStartCase().getVarName()
					+ ")";
		} else {
			colorSetPlace = colorsetTranslator.getColorSetCaseID();
			arcInscriptionEdge = variableTranslator.getCpnVarForCaseId()
					.getVarName();
		}

		// global resources place, needed to generate the monitor that keeps
		// track of the
		// resources in the global resources place
		ColoredPlace globalResourcesPlace = null;
		ColoredPetriNet simulationOverview = new ColoredPetriNet();
		simulationOverview.setIdentifier("Overview");

		// create overview page
		ColoredPlace startPlace = new ColoredPlace("Start", simulationOverview,
				-289, -10, 48, 48);
		startPlace.setPlaceType(colorSetPlace.getNameColorSet());
		if (ManagerConfiguration.getInstance().isCurrentStateSelected()) {
			// inject initial marking to enable loading current state
			startPlace.setInitMark("getInitialTokens(\"Overview`Start\")");
		}
		simulationOverview.addPlace(startPlace);
		ColoredPlace endPlace = new ColoredPlace("End", simulationOverview, 65,
				-10, 48, 48);
		endPlace.setPlaceType(colorSetPlace.getNameColorSet());
		if (ManagerConfiguration.getInstance().isCurrentStateSelected()) {
			// inject initial marking to enable loading current state
			endPlace.setInitMark("getInitialTokens(\"Overview`End\")");
		}
		simulationOverview.addPlace(endPlace);
		ColoredTransition envTrans = new ColoredTransition("Environment",
				simulationOverview, -116, 98, 176, 38);
		simulationOverview.addTransition(envTrans);
		ColoredTransition procTrans = new ColoredTransition("Process",
				simulationOverview, -116, -10, 176, 94);
		simulationOverview.addTransition(procTrans);
		ColoredEdge StartToProc = new ColoredEdge(startPlace, procTrans);
		simulationOverview.addEdge(StartToProc);
		ColoredEdge ProcToEnd = new ColoredEdge(procTrans, endPlace);
		simulationOverview.addEdge(ProcToEnd);
		ColoredEdge EndToEnv = new ColoredEdge(endPlace, envTrans);
		ArrayList<String> bendpoint1 = new ArrayList<String>();
		bendpoint1.add("65");
		bendpoint1.add("98");
		EndToEnv.addBendPoint(bendpoint1);
		simulationOverview.addEdge(EndToEnv);
		ColoredEdge EnvToStart = new ColoredEdge(envTrans, startPlace);
		ArrayList<String> bendpoint2 = new ArrayList<String>();
		bendpoint2.add("-289");
		bendpoint2.add("98");
		EnvToStart.addBendPoint(bendpoint2);
		simulationOverview.addEdge(EnvToStart);

		// create environment page level
		ColoredPetriNet environmentPage = new ColoredPetriNet();
		environmentPage.setIdentifier("Environment");
		ColoredPlace startPlaceEnv = new ColoredPlace("Start", environmentPage,
				-292, -11, 48, 48);
		startPlaceEnv.setPlaceType(colorSetPlace.getNameColorSet());
		environmentPage.addPlace(startPlaceEnv);
		ColoredPlace endPlaceEnv = new ColoredPlace("End", environmentPage,
				259, -14, 48, 48);
		endPlaceEnv.setPlaceType(colorSetPlace.getNameColorSet());
		environmentPage.addPlace(endPlaceEnv);
		ColoredPlace nextCaseID = new ColoredPlace("next\ncase ID",
				environmentPage, -292, 263, 120, 58);
		// Set the initial marking of this place
		if (ManagerConfiguration.getInstance().isCurrentStateSelected()) {
			nextCaseID.setInitMark("getNextCaseID()");
		} else {
			nextCaseID.setInitMark("1");
		}
		environmentPage.addPlace(nextCaseID);
		if (isRedesign) {
			// add for redesign
			ColoredPlace startFile = new ColoredPlace("startFile",
					environmentPage, -292, 500, 120, 58);
			environmentPage.addPlace(startFile);
			ColoredTransition initFile = new ColoredTransition("InitFile",
					environmentPage, -292, 400, 92, 56);
			environmentPage.addTransition(initFile);
			// arcs
			ColoredEdge startFileToInitFile = new ColoredEdge(startFile,
					initFile);
			startFileToInitFile.setArcInscription("c");
			environmentPage.addEdge(startFileToInitFile);
			ColoredEdge initFileToNCID = new ColoredEdge(initFile, nextCaseID);
			initFileToNCID.setArcInscription("c");
			environmentPage.addEdge(initFileToNCID);
			nextCaseID.setInitMark("");
			startFile.setInitMark("1");
			startFile.setPlaceType("CASE_ID");
			initFile
					.setCodeInscription("input (c);output ();action(createFile());");
		}
		// end for redesign
		ColoredTransition init = new ColoredTransition("Init", environmentPage,
				-292, 140, 92, 56);
		if (isRedesign) {
			init.setGuard("c<=readNumberOfCases()");
		}
		environmentPage.addTransition(init);
		ColoredTransition cleanUp = new ColoredTransition("Clean-up",
				environmentPage, 259, 140, 92, 56);
		environmentPage.addTransition(cleanUp);
		if (isRedesign) {
			cleanUp
					.setCodeInscription("input (c,t); output (); action(addToFile(c,getTPT(t)));");
		}
		ColoredEdge initToStart = new ColoredEdge(init, startPlaceEnv);
		if (ManagerConfiguration.getInstance().isThroughputTimeMonitorEnabled()) {
			String arcInscription = "("
					+ variableTranslator.getCpnVarForCaseId().getVarName()
					+ ", ModelTime.toString(time()))";
			initToStart.setArcInscription(arcInscription);
		} else {
			initToStart.setArcInscription(variableTranslator
					.getCpnVarForCaseId().getVarName());
		}
		environmentPage.addEdge(initToStart);
		ColoredEdge initToNextCaseID = new ColoredEdge(init, nextCaseID);
		// case generation scheme
		if (ManagerConfiguration.getInstance().isTimePerspectiveEnabled()) {
			String timeDelayArc = "";
			if (isRedesign) {
				timeDelayArc = "@+readArrivalRate()";
			} else {
				timeDelayArc = "@+"
						+ CpnUtils.getCpnDistributionFunction(highLevelPN
								.getHLProcess().getGlobalInfo()
								.getCaseGenerationScheme());
			}
			String arcInscription = variableTranslator.getCpnVarForCaseId()
					.getVarName()
					+ "+1 " + timeDelayArc;
			initToNextCaseID.setArcInscription(arcInscription);
		} else {
			initToNextCaseID.setArcInscription(variableTranslator
					.getCpnVarForCaseId().getVarName()
					+ "+1");
		}
		environmentPage.addEdge(initToNextCaseID);
		ColoredEdge nextCaseIdToInit = new ColoredEdge(nextCaseID, init);
		nextCaseIdToInit.setArcInscription(variableTranslator
				.getCpnVarForCaseId().getVarName());
		environmentPage.addEdge(nextCaseIdToInit);
		ColoredEdge endToCleanUp = new ColoredEdge(endPlaceEnv, cleanUp);
		endToCleanUp.setArcInscription(arcInscriptionEdge);
		environmentPage.addEdge(endToCleanUp);

		if (ManagerConfiguration.getInstance().isDataPerspectiveEnabled()
				&& highLevelPN.getHLProcess().getAttributes() != null
				&& highLevelPN.getHLProcess().getAttributes().size() > 0) {
			// the place for the casedata
			ColoredPlace caseDat = new ColoredPlace("Case data",
					environmentPage, -18, 140, 120, 58);
			// inject initial marking for current state
			if (ManagerConfiguration.getInstance().isCurrentStateSelected()) {
				caseDat.setInitMark("getInitialCaseData()");
			}
			// Set the type of this place
			ArrayList<CpnColorSet> productColorSet = new ArrayList<CpnColorSet>();
			productColorSet.add(colorsetTranslator.getColorSetCaseID());
			productColorSet.add(colorsetTranslator.getColorSetDataAttributes());
			CpnColorSet colorSet = colorsetTranslator
					.productCpnColorSet(productColorSet);
			caseDat.setPlaceType(colorSet.getNameColorSet());
			environmentPage.addPlace(caseDat);

			// the arc connecting the casedata place with the init transition
			ColoredEdge initToCaseDat = new ColoredEdge(init, caseDat);
			// Set the initialisation arc inscription
			String initString = "";
			for (HLAttribute dataAttribute : highLevelPN.getHLProcess()
					.getAttributes()) {
				initString = CpnAttributeManager.appendInitialValueString(
						dataAttribute, initString);
			}
			// remove the last ,
			int length = initString.length();
			if (length > 0) {
				initString = initString.substring(0, length - 2);
			}
			initToCaseDat.setArcInscription("("
					+ variableTranslator.getCpnVarForCaseId().getVarName()
					+ ", {" + initString + "})");
			environmentPage.addEdge(initToCaseDat);

			// the arc connecting the casedata
			ColoredEdge caseDatToCleanUp = new ColoredEdge(caseDat, cleanUp);
			caseDatToCleanUp.setArcInscription("("
					+ variableTranslator.getCpnVarForCaseId().getVarName()
					+ ","
					+ variableTranslator.getCpnVarForDataAttributes()
							.getVarName() + ")");
			environmentPage.addEdge(caseDatToCleanUp);

			// the case data place needs to be a fusion place
			if (!fusionPlaces.containsKey("Case data")) {
				fusionPlaces.put("Case data", new HashSet<ColoredPlace>());
			}
			fusionPlaces.get("Case data").add(caseDat);
			caseDat.setNameFusionPlace("Case data");
		}

		if ((ManagerConfiguration.getInstance().isResourcePerspectiveEnabled())
				&& highLevelPN.getHLProcess().getResources() != null) {
			// create the resources place
			ColoredPlace resource = new ColoredPlace("Resources",
					environmentPage, -18, 0, 120, 58);
			String test = colorsetTranslator.getColorSetForResourcesPlace()
					.getNameColorSet();
			String test2 = CpnUtils.getCpnValidName(test);
			resource.setPlaceType(CpnUtils.getCpnValidName(colorsetTranslator
					.getColorSetForResourcesPlace().getNameColorSet()));
			environmentPage.addPlace(resource);
			// initialize this one
			if (ManagerConfiguration.getInstance().isCurrentStateSelected()) {
				resource.setInitMark("FREE.all()");
			} else {
				SubSetColorSet cset = colorsetTranslator
						.getColorSetForResourcesPlace();
				resource.setInitMark(cset.getInitMarking());
			}
			// the resource place needs to be a fusion place
			if (!fusionPlaces.containsKey("Resources")) {
				fusionPlaces.put("Resources", new HashSet<ColoredPlace>());
			}
			fusionPlaces.get("Resources").add(resource);
			resource.setNameFusionPlace("Resources");
			globalResourcesPlace = resource;
		}

		// ensure that environmentPage, overviewpage and process has a cpnID.
		// This
		// is needed for writing the cpnfile
		simulationOverview.setCpnID(ManagerID.getNewID());
		environmentPage.setCpnID(ManagerID.getNewID());
		net.setCpnID(ManagerID.getNewID());

		// First, make sure that every transition and place in the
		// environmentPage,
		// OverviewPage and Process has a cpnID. This is needed for writing the
		// cpnfile
		simulationOverview.generateCpnIDs();
		environmentPage.generateCpnIDs();
		net.generateCpnIDs();

		// establish hierarchy from Environment to Overview
		SubpageMapping mappingEnvToOv = new SubpageMapping();
		mappingEnvToOv.addMapping(endPlaceEnv, endPlace);
		mappingEnvToOv.addMapping(startPlaceEnv, startPlace);
		mappingEnvToOv.setSubPageID(environmentPage.getCpnID());
		// add the mapping to the environment transition
		envTrans.setSubpageMapping(mappingEnvToOv);
		envTrans.setSubpage(environmentPage);

		// establish hierarchy from Process to Overview
		SubpageMapping mappingProcToOv = new SubpageMapping();
		// Map the input place of this net to the top input place of Overview
		// Map the output place of this net to the top output place of Overview
		// Assuming that this net has precisely one input and one output place
		Iterator places = net.getPlaces().iterator();
		while (places.hasNext()) {
			ColoredPlace place = (ColoredPlace) places.next();
			if (place.inDegree() == 0) {
				// This is an input place
				mappingProcToOv.addMapping(place, startPlace);
			}
			if (place.outDegree() == 0) {
				// This is an output place
				mappingProcToOv.addMapping(place, endPlace);
			}
			// inject initial marking for current state for all places
			// on process page but the start and the end place
			if (ManagerConfiguration.getInstance().isCurrentStateSelected()
					&& place.inDegree() != 0 && place.outDegree() != 0) {
				place.setInitMark("getInitialTokens(\"Process`"
						+ CpnUtils.getCpnValidName(place.getIdentifier())
						+ "\")");
			}
		}
		mappingProcToOv.setSubPageID(net.getCpnID());
		procTrans.setSubpageMapping(mappingProcToOv);
		procTrans.setSubpage(net);
		// process page is always called "Process" (name of Petri net ignored)
		// unless it is used for redesign
		if (!isRedesign) {
			net.setIdentifier("Process");
		}

		if (ManagerConfiguration.getInstance().isResourcePerspectiveEnabled()
				&& ManagerConfiguration.getInstance()
						.isResourcesMonitorEnabled()
				&& highLevelPN.getHLProcess().getResources() != null
				&& highLevelPN.getHLProcess().getResources().size() > 0) {
			// save the cpnid of the resources place and the pageinstanceidref
			// of the page on which
			// the resources places is located. Needed for the monitor that
			// keeps track of resources in
			// the resources place.
			monitoringResources.put(globalResourcesPlace.getCpnID(),
					environmentPage.getPageInstanceIDs());
		}
		// save the cpn id of cleanUp. Needed for the monitor that calculates
		// the throughput time for each case
		this.cpnIdMonitorThroughputTime = cleanUp.getCpnID();
		// the same for the pageinstanceidrefs of the page on which cleanUp is
		// located
		this.pageInstancesMonitorThroughputTime = environmentPage
				.getPageInstanceIDs();

		// return the hierarchical simulation model
		return simulationOverview;
	}

	/**
	 * Generates the basic structure of a subpage of a transition. Only 'empty'
	 * transitions, places and arcs are put on the subpage. So, no arc
	 * inscriptions, no code inscriptions and so on, are provided
	 * 
	 * @param transition
	 *            ColoredTransition the transition for which a subpage needs to
	 *            be created.
	 * @exception java.lang.Exception
	 */
	private void generateStructureSubPageForTransition(
			ColoredTransition transition) throws Exception {
		// input and output places of the transition, needed for the mapping
		// between
		// sub to top
		ArrayList<ColoredPlace> inputPlacesSub = new ArrayList<ColoredPlace>();

		SubpageMapping mapping = new SubpageMapping();

		// you always have a complete transition and create a simulatedPetriNet
		// for the subpage
		ColoredPetriNet subPage = new ColoredPetriNet();
		subPage.setHighLevelProcess(highLevelPN);
		subPage.setTranslatorToCpn(this);

		ColoredTransition completeTransition = new ColoredTransition(transition
				.getIdentifier()
				+ "_complete", subPage);
		// in the case that a subpage is generated for an invisible transition,
		// then each transition on the generated
		// subpage does not have a logevent so that these transitions are also
		// invisible and that as a consequence
		// no monitor is generated for these transitions and they are not logged
		if (transition.isInvisibleTask()) {
			completeTransition.setLogEvent(null);
		}
		completeTransition.setEventType(ColoredTransition.EventType.complete);
		// set a reference to the high level activity
		completeTransition.setHighLevelTransition(transition
				.getHighLevelTransition());
		subPage.addTransition(completeTransition);
		//
		// define the type of the places in the net
		CpnColorSet colorSet = colorsetTranslator.getColorSetCaseID();
		String arcInscription = variableTranslator.getCpnVarForCaseId()
				.getVarName();
		if (ManagerConfiguration.getInstance().isThroughputTimeMonitorEnabled()) {
			ArrayList<CpnColorSet> productColorSet = new ArrayList<CpnColorSet>();
			productColorSet.add(colorsetTranslator.getColorSetCaseID());
			productColorSet.add(colorsetTranslator.getColorSetStartCase());
			colorSet = colorsetTranslator.productCpnColorSet(productColorSet);
			// the inscription for the arc needs to be adapted
			arcInscription = "(" + arcInscription + ","
					+ variableTranslator.getCpnVarForStartCase().getVarName()
					+ ")";
		}

		// create port places for the input of the subpage
		Iterator onlyInputTop = transition.getVerticesOnlyPredecessor()
				.iterator();
		while (onlyInputTop.hasNext()) {
			ColoredPlace topPlace = (ColoredPlace) onlyInputTop.next();
			ColoredPlace subPlace = new ColoredPlace(topPlace.getIdentifier(),
					subPage);
			subPlace.setPlaceType(colorSet.getNameColorSet());
			inputPlacesSub.add(subPlace);
			subPage.addPlace(subPlace);
			// create a mapping from sub to top
			mapping.addMapping(subPlace, topPlace);
		}

		// create port places for the output of the subpage
		// connect the output places already with the complete transition
		Iterator onlyOutputTop = transition.getVerticesOnlySuccessor()
				.iterator();
		while (onlyOutputTop.hasNext()) {
			ColoredPlace topPlace = (ColoredPlace) onlyOutputTop.next();
			ColoredPlace subPlace = new ColoredPlace(topPlace.getIdentifier(),
					subPage);
			subPlace.setPlaceType(colorSet.getNameColorSet());
			subPage.addPlace(subPlace);
			// connect with the complete transition
			ColoredEdge edge = new ColoredEdge(completeTransition, subPlace);
			// set the arc inscription for the edge
			edge.setArcInscription(arcInscription);
			subPage.addEdge(edge);
			// create a mapping from sub to top
			mapping.addMapping(subPlace, topPlace);
		}

		// create port places for the places that are are both predecessor and
		// successor of the transition
		Iterator predAndSucc = transition.getVerticesPredecessorAndSuccessor()
				.iterator();
		while (predAndSucc.hasNext()) {
			ColoredPlace topPlace = (ColoredPlace) predAndSucc.next();
			ColoredPlace subPlace = new ColoredPlace(topPlace.getIdentifier(),
					subPage);
			subPlace.setPlaceType(colorSet.getNameColorSet());
			inputPlacesSub.add(subPlace);
			subPage.addPlace(subPlace);
			// connect with the complete transition
			ColoredEdge edge = new ColoredEdge(completeTransition, subPlace);
			// set the arc inscription for the edge
			edge.setArcInscription(arcInscription);
			subPage.addEdge(edge);
			// create a mapping from sub to top
			mapping.addMapping(subPlace, topPlace);
		}

		ColoredTransition firstTransition = null;
		// check for the settings of the configuration manager
		if (!ManagerConfiguration.getInstance().isTimePerspectiveEnabled()
				&& !ManagerConfiguration.getInstance()
						.isOnlyExecutionTimeEnabled()
				&& !ManagerConfiguration.getInstance()
						.isOnlyWaitingAndExecutionTimeEnabled()
				&& !ManagerConfiguration.getInstance()
						.isOnlySojournTimeEnabled()) {
			// only complete
			firstTransition = completeTransition;
		} else if (ManagerConfiguration.getInstance()
				.isOnlyExecutionTimeEnabled()) {
			// start and complete
			ColoredTransition start = new ColoredTransition(transition
					.getIdentifier()
					+ "_start", subPage);
			start.setEventType(ColoredTransition.EventType.start);
			// in the case that a subpage is generated for an invisible
			// transition, then each transition on the generated
			// subpage does not have a logevent so that these transitions are
			// also invisible and that as a consequence
			// no monitor is generated for these transitions and they are not
			// logged
			if (transition.isInvisibleTask()) {
				start.setLogEvent(null);
			}
			// set a reference to the high level activity
			start.setHighLevelTransition(transition.getHighLevelTransition());
			subPage.addTransition(start);
			ColoredPlace between = new ColoredPlace(namePlaceExecuting, subPage);
			subPage.addPlace(between);
			ColoredEdge startToBetween = new ColoredEdge(start, between);
			String timeDelay = CpnUtils.getCpnDistributionFunction(transition
					.getHighLevelTransition().getExecutionTime());
			startToBetween.setArcInscription("@+" + timeDelay);
			subPage.addEdge(startToBetween);
			ColoredEdge betweenToComplete = new ColoredEdge(between,
					completeTransition);
			subPage.addEdge(betweenToComplete);
			// inject initial marking for current state. invisible transitions
			// cannot take any time, and they
			// cannot be observed in the engine - pure routing. therefore no
			// current state marking needed.
			if (ManagerConfiguration.getInstance().isCurrentStateSelected()
					&& transition.isInvisibleTask() == false) {
				between.setInitMark("getInitialTokensExePlace(\""
						+ transition.getIdentifier() + "`E\")@+(" + timeDelay
						+ " div 2)");
			}
			firstTransition = start;
		} else if (ManagerConfiguration.getInstance()
				.isOnlyWaitingAndExecutionTimeEnabled()) {
			// schedule, start and complete
			ColoredTransition schedule = new ColoredTransition(transition
					.getIdentifier()
					+ "_schedule", subPage);
			schedule.setEventType(ColoredTransition.EventType.schedule);
			// in the case that a subpage is generated for an invisible
			// transition, then each transition on the generated
			// subpage does not have a logevent so that these transitions are
			// also invisible and that as a consequence
			// no monitor is generated for these transitions and they are not
			// logged
			if (transition.isInvisibleTask()) {
				schedule.setLogEvent(null);
			}
			// set a reference to the high level activity
			schedule
					.setHighLevelTransition(transition.getHighLevelTransition());
			subPage.addTransition(schedule);
			ColoredTransition start = new ColoredTransition(transition
					.getIdentifier()
					+ "_start", subPage);
			start.setEventType(ColoredTransition.EventType.start);
			// in the case that a subpage is generated for an invisible
			// transition, then each transition on the generated
			// subpage does not have a logevent so that these transitions are
			// also invisible and that as a consequence
			// no monitor is generated for these transitions and they are not
			// logged
			if (transition.isInvisibleTask()) {
				start.setLogEvent(null);
			}
			// set a reference to the high level activity
			start.setHighLevelTransition(transition.getHighLevelTransition());
			subPage.addTransition(start);

			ColoredPlace betweenScheduleStart = new ColoredPlace(
					namePlaceWaiting, subPage);
			subPage.addPlace(betweenScheduleStart);

			ColoredPlace betweenStartComplete = new ColoredPlace(
					namePlaceExecuting, subPage);
			subPage.addPlace(betweenStartComplete);
			// create the edge between the nodes
			ColoredEdge scheduleToBetween1 = new ColoredEdge(schedule,
					betweenScheduleStart);
			String timeDelay = CpnUtils.getCpnDistributionFunction(transition
					.getHighLevelTransition().getWaitingTime());
			// if resource are also used, applying waiting ratio to waiting time
			if (ManagerConfiguration.getInstance()
					.isResourcePerspectiveEnabled()) {
				timeDelay = timeDelay.substring(0, timeDelay.length() - 1)
						+ "*"
						+ (ManagerConfiguration.getInstance().getWaitingRatio() / 100.0)
						+ ")";
			}
			scheduleToBetween1.setArcInscription("@+" + timeDelay);
			subPage.addEdge(scheduleToBetween1);
			ColoredEdge between1ToStart = new ColoredEdge(betweenScheduleStart,
					start);
			subPage.addEdge(between1ToStart);
			ColoredEdge startToBetween2 = new ColoredEdge(start,
					betweenStartComplete);
			subPage.addEdge(startToBetween2);
			String timeDelay2 = CpnUtils.getCpnDistributionFunction(transition
					.getHighLevelTransition().getExecutionTime());
			startToBetween2.setArcInscription("@+" + timeDelay2);
			ColoredEdge between2ToComplete = new ColoredEdge(
					betweenStartComplete, completeTransition);
			subPage.addEdge(between2ToComplete);
			// inject initial marking for current state. invisible transitions
			// cannot take any time, and they
			// cannot be observed in the engine - pure routing. therefore no
			// current state marking needed.
			if (ManagerConfiguration.getInstance().isCurrentStateSelected()
					&& transition.isInvisibleTask() == false) {
				betweenStartComplete.setInitMark("getInitialTokensExePlace(\""
						+ transition.getIdentifier() + "`E\")@+(" + timeDelay2
						+ " div 2)");
			}
			firstTransition = schedule;
		} else if (ManagerConfiguration.getInstance()
				.isOnlySojournTimeEnabled()) {
			// schedule and complete
			ColoredTransition schedule = new ColoredTransition(transition
					.getIdentifier()
					+ "_schedule", subPage);
			schedule.setEventType(ColoredTransition.EventType.schedule);
			// in the case that a subpage is generated for an invisible
			// transition, then each transition on the generated
			// subpage does not have a logevent so that these transitions are
			// also invisible and that as a consequence
			// no monitor is generated for these transitions and they are not
			// logged
			if (transition.isInvisibleTask()) {
				schedule.setLogEvent(null);
			}
			// set a reference to the high level activity
			schedule
					.setHighLevelTransition(transition.getHighLevelTransition());
			subPage.addTransition(schedule);

			ColoredPlace between = new ColoredPlace("S", subPage);
			subPage.addPlace(between);
			ColoredEdge scheduleToBetween = new ColoredEdge(schedule, between);
			subPage.addEdge(scheduleToBetween);
			String timeDelay = CpnUtils.getCpnDistributionFunction(transition
					.getHighLevelTransition().getSojournTime());
			scheduleToBetween.setArcInscription("@+" + timeDelay);
			ColoredEdge betweenToComplete = new ColoredEdge(between,
					completeTransition);
			subPage.addEdge(betweenToComplete);

			firstTransition = schedule;
		} else {
			throw new java.lang.Exception("Conflict when generating subpage!");
		}

		// connect the first transition with its input port places
		Iterator inputSub = inputPlacesSub.iterator();
		while (inputSub.hasNext()) {
			ColoredPlace sub = (ColoredPlace) inputSub.next();
			// connect place sub with firstTransition
			ColoredEdge edge = new ColoredEdge(sub, firstTransition);
			// set the arc inscription
			edge.setArcInscription(arcInscription);
			subPage.addEdge(edge);
		}

		// create name for the subpage and ensure that the subpage itself and
		// the transitions
		// on it have an unique CPN-id
		subPage.setIdentifier(transition.getIdentifier());
		subPage.setCpnID(ManagerID.getNewID());
		subPage.generateCpnIDs();
		mapping.setSubPageID(subPage.getCpnID());
		transition.setSubpageMapping(mapping);
		transition.setSubpage(subPage);
	}

	/**
	 * Translate the simulation model to a cpn model
	 * 
	 * @return a petri net that is close to cpn and can be immediately exported
	 *         to cpn.
	 */
	public ColoredPetriNet translate() {
		// scaling and stretching of the simulatedPN
		scalingAndStretchingForPN(simulatedPN);
		// create the simulation environment
		this.simulationEnvironment = createSimulationEnvironment(simulatedPN);
		// If the logging is enabled the types of places on the process page
		// needs to be changed
		if (ManagerConfiguration.getInstance().isThroughputTimeMonitorEnabled()) {
			ArrayList<CpnColorSet> productColorSets = new ArrayList<CpnColorSet>();
			productColorSets.add(colorsetTranslator.getColorSetCaseID());
			productColorSets.add(colorsetTranslator.getColorSetStartCase());
			CpnColorSet colorSet = colorsetTranslator
					.productCpnColorSet(productColorSets);
			Iterator places = simulatedPN.getPlaces().iterator();
			while (places.hasNext()) {
				ColoredPlace place = (ColoredPlace) places.next();
				place.setPlaceType(colorSet.getNameColorSet());
			}
		}

		// generate a sub-page for each transition "highLevelTrans" on the
		// Process page
		Iterator<Transition> transitions = simulatedPN.getTransitions()
				.iterator();
		while (transitions.hasNext()) {
			ColoredTransition transition = (ColoredTransition) transitions
					.next();
			// if (!transition.isInvisibleTask()) {
			if (true) {
				try {
					generateStructureSubPageForTransition(transition);
					generateLayoutHierarchicalPN(transition.getSubpage(), false);
					generateInscriptionsInnerPart(transition);
					// find high level activity and related choices for
					// transition
					HLActivity act = highLevelPN.findActivity(transition);
					int noOfChoicesInvolved = highLevelPN.getHLProcess()
							.getChoicesForTargetActivity(act.getID()).size();
					// generate based on configuration
					boolean test = ((ManagerConfiguration.getInstance()
							.isDataPerspectiveEnabled() && highLevelPN
							.getHLProcess().getAttributes().size() > 0) && ((choiceAllowed(
							ChoiceEnum.DATA, transition) && noOfChoicesInvolved > 0) || transition
							.getHighLevelTransition().getOutputDataAttributes()
							.size() > 0));

					if (test == true) {
						generateDataStructureOnSubpage(transition,
								choiceAllowed(ChoiceEnum.DATA, transition));
					}
					if ((ManagerConfiguration.getInstance()
							.isResourcePerspectiveEnabled())
							&& !transition.isInvisibleTask()
							&& !act.isAutomatic()) {
						// not needed if the transition is an automatic or
						// invisible task
						generateResourceStructureOnSubpage(transition);
					}
					// if
					// (transition.getHighLevelTransition().getFrequencyDependency()
					// > 1 &&
					if (noOfChoicesInvolved > 0
							&& choiceAllowed(ChoiceEnum.FREQ, transition)) {
						generateFrequencyDependencySubpage(transition);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				// transition is a hidden task, so the arcs on the process page
				// should
				// have the correct arc inscriptions
				String arcInscription = "";
				if (ManagerConfiguration.getInstance()
						.isThroughputTimeMonitorEnabled()) {
					arcInscription = "(" + this.cpnVarNameForCaseId + ","
							+ this.cpnVarNameForStartCase + ")";
				} else {
					arcInscription = this.cpnVarNameForCaseId;
				}
				// all the incoming and outgoing arcs of the hidden task need to
				// have the arc inscription
				Iterator<ColoredEdge> inEdges = transition.getInEdgesIterator();
				while (inEdges.hasNext()) {
					ColoredEdge edge = inEdges.next();
					edge.setArcInscription(arcInscription);
				}
				Iterator<ColoredEdge> outEdges = transition
						.getOutEdgesIterator();
				while (outEdges.hasNext()) {
					ColoredEdge edge = outEdges.next();
					edge.setArcInscription(arcInscription);
				}
			}
		}
		// probability dependency.
		checkForPossibilityDependencies(simulatedPN.getPlaces());
		return simulationEnvironment;
	}

	/**
	 * Checks whether some choice is allowed for a decision point, so that it
	 * may be put in the cpn model. In the GUI of the cpn export you can choose
	 * between a choice based on data attributes, frequencies, probabilities or
	 * unguided for a given decision point. Before translating to cpn, it needs
	 * be checked which option is chosen.
	 * 
	 * @param choiceType
	 *            ChoiceEnum the kind of choice for which we want to check.
	 * @param transition
	 *            ColoredTransition the transition for which we want to check,
	 *            whether the kind of choice is allowed or not.
	 * @return boolean <code>true</code>, if the choice is allowed for the
	 *         transition. <code>false</code> otherwise.
	 */
	private boolean choiceAllowed(ChoiceEnum choiceType,
			ColoredTransition transition) {
		HLActivity act = highLevelPN.findActivity(transition);
		if (act != null) {
			Iterator<HLChoice> choiceIt = highLevelPN.getHLProcess()
					.getChoicesForTargetActivity(act.getID()).iterator();
			while (choiceIt.hasNext()) {
				HLChoice choice = choiceIt.next();
				if (choice.getChoiceConfiguration() == choiceType) {
					// return for first choice found with this type
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check whether there are possibility dependencies defined for some places.
	 * In the case that there are more than one possibility dependencies defined
	 * for one place, then generate the correct distribution function for each
	 * of these possibility dependencies
	 * 
	 * @param places
	 *            List the list of places in the net.
	 */
	private void checkForPossibilityDependencies(List<Place> places) {
		Iterator<Place> placesIt = places.iterator();
		while (placesIt.hasNext()) {
			ColoredPlace place = (ColoredPlace) placesIt.next();
			HLChoice choice = highLevelPN.findChoice(place);
			// if choice exists for this place and is configured to be based on
			// probability
			if (choice != null
					&& choice.getChoiceConfiguration() == ChoiceEnum.PROB) {
				// first normalize if needed
				double sum = 0.0;
				Iterator<HLCondition> probDepsNorm = choice.getConditions()
						.iterator();
				while (probDepsNorm.hasNext()) {
					HLCondition probDepNorm = probDepsNorm.next();
					sum += probDepNorm.getProbability();
				}
				// check whether sum is 0, in that case use default values.
				if (sum == 0) {
					// use default values
					Iterator<HLCondition> dependencies = choice.getConditions()
							.iterator();
					while (dependencies.hasNext()) {
						HLCondition dependency = dependencies.next();
						double prob = ((double) 1 / (double) choice
								.getConditions().size());
						dependency.setProbability(prob);
					}
				} else {
					// use normalized values
					Iterator<HLCondition> dependencies = choice.getConditions()
							.iterator();
					while (dependencies.hasNext()) {
						HLCondition dependency = dependencies.next();
						double prob = ((double) dependency.getProbability() / (double) sum);
						dependency.setProbability(prob);
					}
				}
				// first find the multiplier for a round value
				// maxInt value in cpn is 1073741823
				// so the multiplier may be at most 100000000
				double multiplier = 1.0;
				Iterator<HLCondition> probDeps = choice.getConditions()
						.iterator();
				while (probDeps.hasNext()) {
					HLCondition probDep = probDeps.next();
					double doubleVal = probDep.getProbability();
					while (Math.rint(doubleVal * multiplier) != (doubleVal * multiplier)
							&& multiplier <= 100000000) {
						multiplier = multiplier * 10;
					}
				}
				// calculate the max value of all the probabilities
				int maxVal = 0;
				probDeps = choice.getConditions().iterator();
				while (probDeps.hasNext()) {
					HLCondition probDep = probDeps.next();
					maxVal += Math.round(probDep.getProbability() * multiplier);
				}
				// String distFunc = "round(uniform(0.0," + maxVal + ".0))";
				String distFunc = "discrete(0," + (maxVal - 1) + ")";
				// actually generate
				double cumulative = 0;
				Iterator<HLCondition> conditions = choice.getConditions()
						.iterator();
				int i = 0;
				while (conditions.hasNext()) {
					HLCondition cond = conditions.next();
					if (i == 0) {
						// the first one
						String cpnVarNameProb = variableTranslator
								.getCpnVarForProbDep(cond).getVarName();
						String guard = cpnVarNameProb
								+ " < "
								+ Math
										.round(cond.getProbability()
												* multiplier);
						generateProbabilityDependencySubpage(cond, distFunc,
								guard, cpnVarNameProb);
						cumulative = cumulative
								+ Math
										.round(cond.getProbability()
												* multiplier);
					} else if (i == choice.getConditions().size() - 1) {
						// the last one
						String cpnVarNameProb = variableTranslator
								.getCpnVarForProbDep(cond).getVarName();
						String guard = cpnVarNameProb + " >= "
								+ ((int) cumulative);
						generateProbabilityDependencySubpage(cond, distFunc,
								guard, cpnVarNameProb);
					} else {
						// somewhere in the middle
						String cpnVarNameProb = variableTranslator
								.getCpnVarForProbDep(cond).getVarName();
						String guard = cpnVarNameProb
								+ " >= "
								+ ((int) cumulative)
								+ " andalso "
								+ cpnVarNameProb
								+ " < "
								+ (Math.round(cumulative
										+ (cond.getProbability() * multiplier)));
						generateProbabilityDependencySubpage(cond, distFunc,
								guard, cpnVarNameProb);
						cumulative = cumulative
								+ Math
										.round(cond.getProbability()
												* multiplier);
					}
					// remember configuration option in colored pn structure
					// (needed for writing)
					HLActivity act = cond.getTarget();
					ModelGraphVertex targetNode = highLevelPN
							.findModelGraphVertexForActivity(act.getID());
					if (targetNode instanceof ColoredTransition) {
						// ((ColoredTransition) targetNode)
						// .existsProbabilityDependency(true);
						ColoredTransition firstTransition = getFirstTransitionOnSubpage(((ColoredTransition) targetNode)
								.getSubpage());
						if (firstTransition != null) {
							firstTransition.existsProbabilityDependency(true);
						}
					}
					i++;
				}
			}
		}
	}

	/**
	 * generates the structure on the subpage that is needed to model the data
	 * attributes and the data dependencies for a transition
	 * 
	 * @param transition
	 *            ColoredTransition the transition for which the data structure
	 *            on the subpage needs to be generated.
	 * @param choiceAllowed
	 *            indicate whether for this transition a choice based on data is
	 *            allowed.
	 */
	private void generateDataStructureOnSubpage(ColoredTransition transition,
			boolean choiceAllowed) {
		ColoredPlace caseDat = new ColoredPlace("Case data", transition
				.getSubpage());
		caseDat.setCpnID(ManagerID.getNewID());
		// inject initial marking for current state
		if (ManagerConfiguration.getInstance().isCurrentStateSelected()) {
			caseDat.setInitMark("getInitialCaseData()");
		}
		// generate product color set for data attributes and case-id
		ArrayList<CpnColorSet> productColorSets = new ArrayList<CpnColorSet>();
		productColorSets.add(colorsetTranslator.getColorSetCaseID());
		productColorSets.add(colorsetTranslator.getColorSetDataAttributes());
		CpnColorSet prodColSet = colorsetTranslator
				.productCpnColorSet(productColorSets);
		caseDat.setPlaceType(prodColSet.getNameColorSet());
		transition.getSubpage().addPlace(caseDat);

		ColoredTransition firstTransition = getFirstTransitionOnSubpage(transition
				.getSubpage());
		ColoredTransition lastTransition = getLastTransitionOnSubpage(transition
				.getSubpage());
		//
		// add layout information manually
		// also take into account the scaling and the stretching factor
		caseDat.setWidth(120 * (int) Math.round((double) ManagerLayout
				.getInstance().getStretchFactor()
				/ (double) ManagerLayout.DEFAULT_STRETCH_FACTOR));
		caseDat.setHeight(58 * (int) Math.round((double) ManagerLayout
				.getInstance().getStretchFactor()
				/ (double) ManagerLayout.DEFAULT_STRETCH_FACTOR));
		caseDat.setYCoordinate((firstTransition.getYCoordinate() + 200)
				* (int) Math.round((double) ManagerLayout.getInstance()
						.getScaleFactor()
						/ (double) ManagerLayout.DEFAULT_SCALE_FACTOR));
		int middle = 0;
		if (firstTransition.getXCoordinate() < lastTransition.getXCoordinate()) {
			middle = Math.round(firstTransition.getXCoordinate()
					+ ((lastTransition.getXCoordinate() - firstTransition
							.getXCoordinate()) / 2));
		} else { // lastTransition.getXCoordinate() <=
			// firstTransition.getXCoordinate()
			middle = Math.round(lastTransition.getXCoordinate()
					+ ((firstTransition.getXCoordinate() - lastTransition
							.getXCoordinate()) / 2));
		}
		caseDat.setXCoordinate(middle);

		// Generate the code inscription for the case that there exists data
		// attributes for
		// a transition and that the logging is enabled
		String codeInscription = "";
		String actionPart = "";
		if (transition.getHighLevelTransition().getOutputDataAttributes()
				.size() > 0) {
			codeInscription = codeInscription
					+ "input ("
					+ variableTranslator.getCpnVarForDataAttributes()
							.getVarName()
					+ "); \n output ("
					+ variableTranslator.getCpnVarForModifiedDataAttributes()
							.getVarName() + "); \n action \n";
			ListIterator<HLAttribute> dataAttributes = transition
					.getHighLevelTransition().getOutputDataAttributes()
					.listIterator();
			boolean first = true;
			while (dataAttributes.hasNext()) {
				HLAttribute dataAttribute = (HLAttribute) dataAttributes.next();
				if (first) {
					// the first element in the list
					if (transition.getHighLevelTransition()
							.getTransformationType(dataAttribute.getID())
							.equals(HLTypes.TransformationType.Resample)) {
						// the data attribute needs to be resampled
						actionPart = actionPart
								+ "("
								+ colorsetTranslator
										.getColorSetDataAttributes()
										.getNameColorSet()
								+ ".set_"
								+ CpnUtils.getCpnValidName(variableTranslator
										.getCpnVarForDataAttribute(
												dataAttribute).getVarName())
								+ " "
								+ variableTranslator
										.getCpnVarForDataAttributes()
										.getVarName()
								+ " ("
								+ generateRandomFunctionForDataAttribute(
										dataAttribute).getFunctionName() + "))";
					} else { // the data attribute needs to be reused
						actionPart = actionPart
								+ "("
								+ colorsetTranslator
										.getColorSetDataAttributes()
										.getNameColorSet()
								+ ".set_"
								+ CpnUtils.getCpnValidName(variableTranslator
										.getCpnVarForDataAttribute(
												dataAttribute).getVarName())
								+ " "
								+ variableTranslator
										.getCpnVarForDataAttributes()
										.getVarName()
								+ " (#"
								+ variableTranslator.getCpnVarForDataAttribute(
										dataAttribute).getVarName()
								+ " "
								+ variableTranslator
										.getCpnVarForDataAttributes()
										.getVarName() + "))";
					}
					first = false;
				} else {
					if (transition.getHighLevelTransition()
							.getTransformationType(dataAttribute.getID())
							.equals(HLTypes.TransformationType.Resample)) {
						// the data attribute needs to be resampled
						actionPart = "("
								+ colorsetTranslator
										.getColorSetDataAttributes()
										.getNameColorSet()
								+ ".set_"
								+ CpnUtils.getCpnValidName(variableTranslator
										.getCpnVarForDataAttribute(
												dataAttribute).getVarName())
								+ " "
								+ actionPart
								+ " ("
								+ generateRandomFunctionForDataAttribute(
										dataAttribute).getFunctionName() + "))";
					} else { // the data attribute needs to be reused
						actionPart = "("
								+ colorsetTranslator
										.getColorSetDataAttributes()
										.getNameColorSet()
								+ ".set_"
								+ CpnUtils.getCpnValidName(variableTranslator
										.getCpnVarForDataAttribute(
												dataAttribute).getVarName())
								+ " "
								+ actionPart
								+ " (#"
								+ variableTranslator.getCpnVarForDataAttribute(
										dataAttribute).getVarName()
								+ " "
								+ variableTranslator
										.getCpnVarForDataAttributes()
										.getVarName() + "))";

					}
				}
			}

			codeInscription = codeInscription + actionPart + ";";
		}

		// Get the data dependencies for which this transition is the target
		// node, if there
		// exists more than one data dependency
		// then the epression of these data dependencies need to be the same,
		// but is assumed
		// that this is always the case

		// ArrayList<HLDataDependency> dataDependencies =
		// getDataDependenciesForTargetNode(transition);
		HLActivity act = highLevelPN.findActivity(transition);
		ArrayList<HLChoice> dataDependencies = highLevelPN.getHLProcess()
				.getChoicesForTargetActivity(act.getID());
		if (dataDependencies.size() > 0 && choiceAllowed) {
			transition.existsDataDependency(true);
		}
		// if (transition.getHighLevelTransition().getDataAttributes().size() >
		// 0 &&
		// dataDependencies.size() == 0) {
		if (transition.getHighLevelTransition().getOutputDataAttributes()
				.size() > 0
				&& choiceAllowed == false) {
			// only data attributes, connect place caseDat with last transition
			ColoredEdge caseDatToLastTransition = new ColoredEdge(caseDat,
					lastTransition);
			transition.getSubpage().addEdge(caseDatToLastTransition);
			ColoredEdge lastTransitionToCaseDat = new ColoredEdge(
					lastTransition, caseDat);
			transition.getSubpage().addEdge(lastTransitionToCaseDat);
			// set the correct arc inscriptions
			caseDatToLastTransition.setArcInscription("("
					+ variableTranslator.getCpnVarForCaseId().getVarName()
					+ ","
					+ variableTranslator.getCpnVarForDataAttributes()
							.getVarName() + ")");
			lastTransitionToCaseDat.setArcInscription("("
					+ variableTranslator.getCpnVarForCaseId().getVarName()
					+ ","
					+ variableTranslator.getCpnVarForModifiedDataAttributes()
							.getVarName() + ")");
			// set the correct code inscription
			lastTransition.setCodeInscription(codeInscription);
			// bendpoint
			ArrayList bendpoint = new ArrayList();
			bendpoint.add(caseDat.getXCoordinate() + 10);
			bendpoint.add(lastTransition.getYCoordinate() + 100);
			caseDatToLastTransition.addBendPoint(bendpoint);
		} else if (transition.getHighLevelTransition()
				.getOutputDataAttributes().size() == 0
				&& dataDependencies.size() > 0 && choiceAllowed) {
			// only data dependencies, connect place caseDat with first
			// transition
			ColoredEdge caseDatFirstTransition = new ColoredEdge(caseDat,
					firstTransition);
			transition.getSubpage().addEdge(caseDatFirstTransition);
			// ColoredEdge firstTransitionToCaseDat = new
			// ColoredEdge(firstTransition, caseDat);
			// transition.getSubpage().addEdge(firstTransitionToCaseDat);
			// set the correct arc inscriptions
			caseDatFirstTransition.setArcInscription("("
					+ variableTranslator.getCpnVarForCaseId().getVarName()
					+ ","
					+ variableTranslator.getCpnVarForDataAttributes()
							.getVarName() + ")");
			caseDatFirstTransition.setDoubleHeaded(true);
			// firstTransitionToCaseDat.setArcInscription("(" +
			// getCpnVarForCaseId().getVarName() +
			// "," + getCpnVarForDataAttributes().getVarName() + ")");
			// How do I know in the expression the correct cpn variable for the
			// data attributes???
			HLChoice choice = dataDependencies.get(0);
			HLCondition cond = choice.getCondition(act.getID());
			firstTransition.setGuard(CpnUtils.getCpnExpression(cond
					.getExpression()));
			firstTransition.existsDataDependency(true);
			// bendpoint
			ArrayList bendpoint = new ArrayList();
			bendpoint.add(caseDat.getXCoordinate() - 10);
			bendpoint.add(firstTransition.getYCoordinate() + 100);
			caseDatFirstTransition.addBendPoint(bendpoint);
		} else if (transition.getHighLevelTransition()
				.getOutputDataAttributes().size() > 0
				&& dataDependencies.size() > 0 && choiceAllowed) {
			transition.existsDataDependency(true);
			// data attributes and data dependencies
			if (firstTransition.equals(lastTransition)) {
				// connect caseDat with last Transition
				ColoredEdge caseDatToFirstTransition = new ColoredEdge(caseDat,
						firstTransition);
				transition.getSubpage().addEdge(caseDatToFirstTransition);
				ColoredEdge firstTransitionToCaseDat = new ColoredEdge(
						firstTransition, caseDat);
				transition.getSubpage().addEdge(firstTransitionToCaseDat);
				// set the correct arc inscriptions
				caseDatToFirstTransition.setArcInscription("("
						+ variableTranslator.getCpnVarForCaseId().getVarName()
						+ ","
						+ variableTranslator.getCpnVarForDataAttributes()
								.getVarName() + ")");
				firstTransitionToCaseDat.setArcInscription("("
						+ variableTranslator.getCpnVarForCaseId().getVarName()
						+ ","
						+ variableTranslator
								.getCpnVarForModifiedDataAttributes()
								.getVarName() + ")");
				// set the correct code inscription and the correct guard
				lastTransition.setCodeInscription(codeInscription);
				HLChoice choice = dataDependencies.get(0);
				HLCondition cond = choice.getCondition(act.getID());
				firstTransition.setGuard(CpnUtils.getCpnExpression(cond
						.getExpression()));
				// bendpoint
				ArrayList bendpoint = new ArrayList();
				bendpoint.add(firstTransition.getXCoordinate() - 10);
				bendpoint.add(firstTransition.getYCoordinate() + 100);
				caseDatToFirstTransition.addBendPoint(bendpoint);
				firstTransition.existsDataDependency(true);
			} else {
				// connect caseDat with first and last transition
				ColoredEdge caseDatFirstTransition = new ColoredEdge(caseDat,
						firstTransition);
				transition.getSubpage().addEdge(caseDatFirstTransition);
				// ColoredEdge firstTransitionToCaseDat = new
				// ColoredEdge(firstTransition, caseDat);
				// transition.getSubpage().addEdge(firstTransitionToCaseDat);
				ColoredEdge caseDatToLastTransition = new ColoredEdge(caseDat,
						lastTransition);
				transition.getSubpage().addEdge(caseDatToLastTransition);
				ColoredEdge lastTransitionToCaseDat = new ColoredEdge(
						lastTransition, caseDat);
				transition.getSubpage().addEdge(lastTransitionToCaseDat);
				// set the correct arc inscriptions
				caseDatFirstTransition.setArcInscription("("
						+ variableTranslator.getCpnVarForCaseId().getVarName()
						+ ","
						+ variableTranslator.getCpnVarForDataAttributes()
								.getVarName() + ")");
				caseDatFirstTransition.setDoubleHeaded(true);
				// firstTransitionToCaseDat.setArcInscription("(" +
				// getCpnVarForCaseId().getVarName() + ","
				// + this.getCpnVarForDataAttributes().getVarName() + ")");
				caseDatToLastTransition.setArcInscription("("
						+ variableTranslator.getCpnVarForCaseId().getVarName()
						+ ","
						+ variableTranslator.getCpnVarForDataAttributes()
								.getVarName() + ")");
				lastTransitionToCaseDat.setArcInscription("("
						+ variableTranslator.getCpnVarForCaseId().getVarName()
						+ ","
						+ variableTranslator
								.getCpnVarForModifiedDataAttributes()
								.getVarName() + ")");
				// set the correct code inscription and the correct guard
				lastTransition.setCodeInscription(codeInscription);
				HLChoice choice = dataDependencies.get(0);
				HLCondition cond = choice.getCondition(act.getID());
				firstTransition.setGuard(CpnUtils.getCpnExpression(cond
						.getExpression()));
				// add bendpoints
				ArrayList bendpoint1 = new ArrayList();
				bendpoint1.add(caseDat.getXCoordinate() + 10);
				bendpoint1.add(lastTransition.getYCoordinate() + 100);
				caseDatToLastTransition.addBendPoint(bendpoint1);
				firstTransition.existsDataDependency(true);
				// firstTransitionToCaseDat.addBendPoint(bendpoint1);
				// bendpoint not needed anymore
				// ArrayList bendpoint2 = new ArrayList();
				// bendpoint2.add(caseDat.getXCoordinate()-10);
				// bendpoint2.add(firstTransition.getYCoordinate()+100);
				// caseDatFirstTransition.addBendPoint(bendpoint2);
			}
		} else {
			// this should never happen
		}

		// the case data place needs to be a fusion place
		if (!fusionPlaces.containsKey("Case data")) {
			fusionPlaces.put("Case data", new HashSet<ColoredPlace>());
		}
		fusionPlaces.get("Case data").add(caseDat);
		caseDat.setNameFusionPlace("Case data");
	}

	/**
	 * Generates the structure on the subpage that is needed to model the group
	 * for a transition
	 * 
	 * @param transition
	 *            ColoredTransition the transition for which the resource
	 *            structure on the subpage needs to be generated
	 * @exception java.lang.Exception
	 */
	private void generateResourceStructureOnSubpage(ColoredTransition transition)
			throws Exception {
		// First, generate the Resources place
		// not needed if the task is an automatic task
		ColoredPlace resource = new ColoredPlace("Resources", transition
				.getSubpage());
		resource.setCpnID(ManagerID.getNewID());
		resource.setPlaceType(CpnUtils.getCpnValidName(colorsetTranslator
				.getColorSetForResourcesPlace().getNameColorSet()));
		// inject initial marking for current state if requested
		if (ManagerConfiguration.getInstance().isCurrentStateSelected()) {
			resource.setInitMark("FREE.all()");
		} else {
			SubSetColorSet cset = colorsetTranslator
					.getColorSetForResourcesPlace();
			resource.setInitMark(cset.getInitMarking());
		}
		transition.getSubpage().addPlace(resource);

		ColoredTransition firstTransition = getFirstTransitionOnSubpage_Resource(transition
				.getSubpage()); // modified
		ColoredTransition lastTransition = getLastTransitionOnSubpage(transition
				.getSubpage());
		// get the place that is in between first and last transition, the place
		// with identifier E.
		ColoredPlace placeInBetween = null;
		Iterator successors = firstTransition.getSuccessors().iterator();
		while (successors.hasNext()) {
			ColoredPlace successor = (ColoredPlace) successors.next();
			if (successor.getIdentifier().equals(namePlaceExecuting)) {
				placeInBetween = successor;
				break;
			}
		}
		// add layout information manually
		// also take into account the scaling and stretch factor
		resource
				.setWidth(120 * (int) Math
						.round(((double) ManagerLayout.getInstance()
								.getStretchFactor() / (double) ManagerLayout.DEFAULT_STRETCH_FACTOR)));
		resource.setHeight(58 * (int) Math.round((double) ManagerLayout
				.getInstance().getStretchFactor()
				/ (double) ManagerLayout.DEFAULT_STRETCH_FACTOR));
		if (placeInBetween != null) {
			resource.setYCoordinate((placeInBetween.getYCoordinate() - 200)
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getScaleFactor()
							/ (double) ManagerLayout.DEFAULT_SCALE_FACTOR));
			int middle = 0;
			if (firstTransition.getXCoordinate() < lastTransition
					.getXCoordinate()) {
				middle = Math.round(firstTransition.getXCoordinate()
						+ ((lastTransition.getXCoordinate() - firstTransition
								.getXCoordinate()) / 2));
			} else { // lastTransition.getXCoordinate() <=
				// firstTransition.getXCoordinate()
				middle = Math.round(lastTransition.getXCoordinate()
						+ ((firstTransition.getXCoordinate() - lastTransition
								.getXCoordinate()) / 2));
			}
			resource.setXCoordinate(middle);
		} else {
			resource.setYCoordinate((firstTransition.getYCoordinate() - 200)
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getScaleFactor()
							/ (double) ManagerLayout.DEFAULT_SCALE_FACTOR));
			resource.setXCoordinate(firstTransition.getXCoordinate());
		}
		// already determine which variable should be used for the arc
		// expressions
		CpnVarAndType cpnVarForGroup = null;
		if (transition.getHighLevelTransition().getGroup() != null
				&& transition.getHighLevelTransition().getGroup()
						.getResources().size() > 0) {
			cpnVarForGroup = variableTranslator
					.getCpnVarForGroup((HLGroup) transition
							.getHighLevelTransition().getGroup());
		} else {
			throw new Exception("The group attached to transition "
					+ transition.getIdentifier()
					+ " does not contain any resources");
		}

		if (firstTransition.equals(lastTransition)) {
			// connect the resource place with the complete transition
			ColoredEdge resourceToFirstTransition = new ColoredEdge(resource,
					firstTransition);
			resourceToFirstTransition.setArcInscription(cpnVarForGroup
					.getVarName());
			transition.getSubpage().addEdge(resourceToFirstTransition);
			ColoredEdge firstTransitionToResource = new ColoredEdge(
					firstTransition, resource);
			firstTransitionToResource.setArcInscription(cpnVarForGroup
					.getVarName());
			transition.getSubpage().addEdge(firstTransitionToResource);
		} else {
			// check whether firstTransition has a start event Type
			if (firstTransition.getEventType().equals(
					ColoredTransition.EventType.start)) {
				// connect the resource place with the complete and the start
				// transition
				ColoredEdge resourceToFirstTransition = new ColoredEdge(
						resource, firstTransition);
				resourceToFirstTransition.setArcInscription(cpnVarForGroup
						.getVarName());
				transition.getSubpage().addEdge(resourceToFirstTransition);
				ColoredEdge lastTransitionToResource = new ColoredEdge(
						lastTransition, resource);
				lastTransitionToResource.setArcInscription(cpnVarForGroup
						.getVarName());
				transition.getSubpage().addEdge(lastTransitionToResource);
				// add bendpoints
				ArrayList bendpoint1 = new ArrayList();
				bendpoint1
						.add(String.valueOf(firstTransition.getXCoordinate()));
				bendpoint1.add(String.valueOf(resource.getYCoordinate()));
				resourceToFirstTransition.addBendPoint(bendpoint1);
				ArrayList bendpoint2 = new ArrayList();
				bendpoint2.add(String.valueOf(lastTransition.getXCoordinate()));
				bendpoint2.add(String.valueOf(resource.getYCoordinate()));
				lastTransitionToResource.addBendPoint(bendpoint2);
			} else {
				// generate exception
				throw new Exception(
						"Resources on subpage: The first transition after the incoming "
								+ "place does not have the start event type");
			}
		}

		// the resource place needs to be a fusion place
		if (!fusionPlaces.containsKey("Resources")) {
			fusionPlaces.put("Resources", new HashSet<ColoredPlace>());
		}
		fusionPlaces.get("Resources").add(resource);
		//
		resource.setNameFusionPlace("Resources");
		//
		// Save the cpnid for all the transitions that are connected to the
		// resources and
		// the pageinstanceidref of the page on which the transition is located.
		// Actually, the only transition that can be connected to the resources
		// place
		// are the first and the last transition on the page
		monitoringResources.put(firstTransition.getCpnID(), transition
				.getSubpage().getPageInstanceIDs());
		monitoringResources.put(lastTransition.getCpnID(), transition
				.getSubpage().getPageInstanceIDs());
	}

	private void generateFrequencyDependencySubpage(ColoredTransition transition) {
		// in the case that the incoming place of the transition is a decision
		// place,
		// calculate the gcd of the frequency dependencies that are attached to
		// the
		// outgoing transitions of that decision place

		HLActivity act = highLevelPN.findActivity(transition);
		ArrayList<HLChoice> choices = highLevelPN.getHLProcess()
				.getChoicesForTargetActivity(act.getID());
		// TODO: now only take first choice (and assume that the others would be
		// consistent)
		// However, it might be that different frequencies are given for this
		// activity for different choices
		// The cpn export currently does not deal with this - needs global
		// normalization if required
		HLChoice choice = choices.get(0);
		// calculate the gcd for the frequencydependencies in transitionsFreqDep
		int gcd = 1;
		boolean start = true;
		Iterator<HLCondition> conditions = choice.getConditions().iterator();
		while (conditions.hasNext()) {
			HLCondition cond1 = conditions.next();
			if (start && conditions.hasNext()) {
				HLCondition cond2 = conditions.next();
				int freq1 = cond1.getFrequency();
				int freq2 = cond2.getFrequency();
				gcd = org.apache.commons.math.util.MathUtils.gcd(freq1, freq2);
				start = false;
			} else {
				gcd = org.apache.commons.math.util.MathUtils.gcd(gcd, cond1
						.getFrequency());
			}
		}
		SubpageMapping oldMapping = transition.getSubpageMapping();
		ColoredPetriNet oldSubpage = transition.getSubpage();
		// Generate subpage between the already existing subpage for this
		// transition
		ColoredPetriNet subpage = new ColoredPetriNet();
		subpage.setIdentifier("Frequency_dependency_"
				+ transition.getIdentifier());
		subpage.setCpnID(ManagerID.getNewID());
		// Generate an input and an output place for each input and output place
		// of transition
		SubpageMapping mappingFromSubpageToTop = new SubpageMapping();
		mappingFromSubpageToTop.setSubPageID(subpage.getCpnID());

		HashSet inputPlacesSubpage = new HashSet<ColoredPlace>();
		HashSet outputPlacesSubpage = new HashSet<ColoredPlace>();

		Iterator inputPlaces = transition.getVerticesOnlyPredecessor()
				.iterator();
		while (inputPlaces.hasNext()) {
			ColoredPlace top = (ColoredPlace) inputPlaces.next();
			ColoredPlace sub = new ColoredPlace(top.getIdentifier(), subpage);
			// the type of the top and sub place needs to be the same
			sub.setPlaceType(top.getPlaceType());
			subpage.addPlace(sub);
			inputPlacesSubpage.add(sub);
			mappingFromSubpageToTop.addMapping(sub, top);
		}
		// the same for the output places
		Iterator outputPlaces = transition.getVerticesOnlySuccessor()
				.iterator();
		while (outputPlaces.hasNext()) {
			ColoredPlace top = (ColoredPlace) outputPlaces.next();
			ColoredPlace sub = new ColoredPlace(top.getIdentifier(), subpage);
			// the type of the top and sub place needs to be the same
			sub.setPlaceType(top.getPlaceType());
			subpage.addPlace(sub);
			outputPlacesSubpage.add(sub);
			mappingFromSubpageToTop.addMapping(sub, top);
		}
		// the same for the places that are both output and input
		Iterator inputOutputPlaces = transition
				.getVerticesPredecessorAndSuccessor().iterator();
		while (inputOutputPlaces.hasNext()) {
			ColoredPlace top = (ColoredPlace) inputOutputPlaces.next();
			ColoredPlace sub = new ColoredPlace(top.getIdentifier(), subpage);
			// the type of the top and sub place needs to be the same
			sub.setPlaceType(top.getPlaceType());
			subpage.addPlace(sub);
			inputPlacesSubpage.add(sub);
			outputPlacesSubpage.add(sub);
			mappingFromSubpageToTop.addMapping(sub, top);
		}

		transition.setSubpageMapping(mappingFromSubpageToTop);
		transition.setSubpage(subpage);

		// The number of the generated transitions on the subpage equals the
		// frequency dependency
		// that has been set for the transition. Furthermore, ensure that each
		// generated transition
		// is connected correctly with the input and output places
		HLCondition cond = choice.getCondition(act.getID());
		for (int i = 0; i < ((cond.getFrequency()) / gcd); i++) {
			ColoredTransition generatedTransition = new ColoredTransition(
					transition.getIdentifier() + (i + 1), subpage);
			subpage.addTransition(generatedTransition);
			Iterator<ColoredPlace> subPageInputPlaces = inputPlacesSubpage
					.iterator();
			while (subPageInputPlaces.hasNext()) {
				ColoredPlace inputSubpage = subPageInputPlaces.next();
				// connect with generatedTransition
				ColoredEdge edge = new ColoredEdge(inputSubpage,
						generatedTransition);
				subpage.addEdge(edge);
			}
			Iterator<ColoredPlace> subPageOutputPlaces = outputPlacesSubpage
					.iterator();
			while (subPageOutputPlaces.hasNext()) {
				ColoredPlace outputSubpage = subPageOutputPlaces.next();
				// connect with generatedTransition
				ColoredEdge edge = new ColoredEdge(generatedTransition,
						outputSubpage);
				subpage.addEdge(edge);
			}

			// ensure that the generatedTransition is pointing to the correct
			// subpage and that
			// the mapping is correct
			generatedTransition.setSubpage(oldSubpage);
			SubpageMapping newMapping = new SubpageMapping();
			generatedTransition.setSubpageMapping(newMapping);
			newMapping.setSubPageID(oldMapping.getSubPageID());
			// fix the mappings
			Iterator<Place> placesSub = oldSubpage.getPlaces().iterator();
			while (placesSub.hasNext()) {
				ColoredPlace placeSub = (ColoredPlace) placesSub.next();
				// Get the mapping that belongs to this place, if existing
				SubpageMapping.Mapping oldMappingForSubPlace = oldMapping
						.getMappingForSubPlace(placeSub);
				if (oldMappingForSubPlace != null) {
					ColoredPlace topPlace = oldMappingForSubPlace.second();
					SubpageMapping.Mapping mappingTopToSubpageForPlace = mappingFromSubpageToTop
							.getMappingForTopPlace(topPlace);
					newMapping.addMapping(placeSub, mappingTopToSubpageForPlace
							.first());
				}
			}
		}
		subpage.generateCpnIDs();
		// generate the layout of the subpage
		generateLayoutHierarchicalPN(subpage, false);
	}

	private void generateProbabilityDependencySubpage(HLCondition probDep,
			String distFunction, String guard, String cpnVarNameProb) {
		HLActivity act = probDep.getTarget();
		ModelGraphVertex targetNode = highLevelPN
				.findModelGraphVertexForActivity(act.getID());
		HLChoice choice = probDep.getChoice();
		ModelGraphVertex choiceNode = highLevelPN
				.findModelGraphVertexForChoice(choice.getID());
		if (targetNode instanceof ColoredTransition) {
			ColoredTransition transition = (ColoredTransition) targetNode;
			// First, generate the fusion place for this probability dependency
			ColoredPlace posDep = new ColoredPlace(choiceNode.getIdentifier()
					+ "_Probability", transition.getSubpage());
			posDep.setCpnID(ManagerID.getNewID());
			posDep.setPlaceType(this.cpnColorSetNameForProbDep);
			posDep.setInitMark(distFunction);
			transition.getSubpage().addPlace(posDep);

			// connect posDep with the first transition on the subpage
			ColoredTransition firstTransition = getFirstTransitionOnSubpage(transition
					.getSubpage());
			// temporary
			String firstGuard = firstTransition.getGuard();
			if (firstGuard.equals("")) {
				firstTransition.setGuard(guard);
			} else {
				firstTransition.setGuard(firstGuard + " andalso " + guard);
			}
			ColoredEdge posDepToFirst = new ColoredEdge(posDep, firstTransition);
			transition.getSubpage().addEdge(posDepToFirst);
			posDepToFirst.setArcInscription(cpnVarNameProb);
			ColoredEdge firstToPosDep = new ColoredEdge(firstTransition, posDep);
			transition.getSubpage().addEdge(firstToPosDep);
			firstToPosDep.setArcInscription(distFunction);

			// add layout information manually
			// Also take into account the scaling and stretch factor
			posDep.setWidth(120 * (int) Math.round((double) ManagerLayout
					.getInstance().getStretchFactor()
					/ (double) ManagerLayout.DEFAULT_STRETCH_FACTOR));
			posDep.setHeight(58 * (int) Math.round((double) ManagerLayout
					.getInstance().getStretchFactor()
					/ (double) ManagerLayout.DEFAULT_STRETCH_FACTOR));
			posDep.setYCoordinate((firstTransition.getYCoordinate() - 200)
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getScaleFactor()
							/ (double) ManagerLayout.DEFAULT_SCALE_FACTOR));
			posDep.setXCoordinate((firstTransition.getXCoordinate() - 100)
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getScaleFactor()
							/ (double) ManagerLayout.DEFAULT_SCALE_FACTOR));
			// add bendpoint
			ArrayList bendpoint = new ArrayList();
			bendpoint.add((posDep.getXCoordinate() - 10)
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getScaleFactor()
							/ (double) ManagerLayout.DEFAULT_SCALE_FACTOR));
			bendpoint.add(firstTransition.getYCoordinate() - 100);
			firstToPosDep.addBendPoint(bendpoint);

			// add the probability place to the fusion places
			String nameFusion = choiceNode.getIdentifier() + "_Probability";
			posDep.setNameFusionPlace(nameFusion);
			if (!fusionPlaces.containsKey(nameFusion)) {
				fusionPlaces.put(nameFusion, new HashSet<ColoredPlace>());
			}
			fusionPlaces.get(nameFusion).add(posDep);
		}
	}

	/**
	 * Generates the inscriptions for the inner part of the model. So, this
	 * means that the arcs and places in between the first and last transition
	 * get the correct inscription, respectively the correct type.
	 * 
	 * @param transition
	 *            ColoredTransition
	 */
	private void generateInscriptionsInnerPart(ColoredTransition transition) {
		// needed for push/pull
		ArrayList<CpnColorSet> colorSetWithoutGroup = new ArrayList<CpnColorSet>();

		HLGroup group = transition.getHighLevelTransition().getGroup();
		ArrayList<CpnColorSet> colorSets = new ArrayList<CpnColorSet>();
		ArrayList<CpnColorSet> colorSetsNoGroup = new ArrayList<CpnColorSet>();
		CpnColorSet colorSetsCPN = null;
		CpnColorSet colorSetsNoGroupCPN = null;
		colorSets.add(colorsetTranslator.getColorSetCaseID());
		if (ManagerConfiguration.getInstance().isThroughputTimeMonitorEnabled()) {
			colorSets.add(colorsetTranslator.getColorSetStartCase());
		}
		if (!transition.isInvisibleTask()
				&& (ManagerConfiguration.getInstance()
						.isResourcePerspectiveEnabled())
				&& !transition.getHighLevelTransition().isAutomatic()) {
			if (ManagerConfiguration.getInstance().isPullEnabled()) {
				// need to have a group with and without group
				colorSetsNoGroup = (ArrayList<CpnColorSet>) colorSets.clone();
				colorSetsNoGroupCPN = colorsetTranslator
						.productCpnColorSet(colorSetsNoGroup);
			}
			colorSets.add(colorsetTranslator.getColorSetGroup(group));
		}
		colorSetsCPN = colorsetTranslator.productCpnColorSet(colorSets);

		SubpageMapping mapping = transition.getSubpageMapping();
		Iterator<Place> places = transition.getSubpage().getPlaces().iterator();
		while (places.hasNext()) {
			ColoredPlace p = (ColoredPlace) places.next();
			if (mapping.getMappingForSubPlace(p) == null) {
				String arcInscription = "";
				// if pull is enabled and we are considering place with name
				// W and the resource perspective is enabled and there are
				// resources
				// then the product color set needs to be changed (so without
				// resources)
				if (p.getIdentifier().equals(namePlaceWaiting)
						&& ManagerConfiguration.getInstance().isPullEnabled()) {
					if (!transition.getHighLevelTransition().isAutomatic()) {
						// do not need a colorset with a group
						p.setPlaceType(CpnUtils
								.getCpnValidName(colorSetsNoGroupCPN
										.getNameColorSet()));
					} else {
						p.setPlaceType(CpnUtils.getCpnValidName(colorSetsCPN
								.getNameColorSet()));
					}
				} else {
					p.setPlaceType(CpnUtils.getCpnValidName(colorSetsCPN
							.getNameColorSet()));
				}
				// ensure that the ingoing arcs and outgoing arcs have the
				// correct
				// inscription (tuple of a resource and a case-id)
				arcInscription = "("
						+ variableTranslator.getCpnVarForCaseId().getVarName();
				if (ManagerConfiguration.getInstance()
						.isThroughputTimeMonitorEnabled()) {
					arcInscription = arcInscription
							+ ","
							+ variableTranslator.getCpnVarForStartCase()
									.getVarName();
				}
				if ((ManagerConfiguration.getInstance()
						.isResourcePerspectiveEnabled())
						&& !transition.getHighLevelTransition().isAutomatic()
						&& !transition.isInvisibleTask()) {
					String withRes = ","
							+ variableTranslator.getCpnVarForGroup(group)
									.getVarName();
					if (p.getIdentifier().equals(namePlaceWaiting)) {
						if (ManagerConfiguration.getInstance().isPushEnabled()) {
							arcInscription = arcInscription + withRes;
						} else {
							// nothing
						}
					} else {
						arcInscription = arcInscription + withRes;
					}
				}
				arcInscription = arcInscription + ")";

				Iterator<ColoredEdge> edgesIn = p.getInEdgesIterator();
				while (edgesIn.hasNext()) {
					ColoredEdge edge = edgesIn.next();
					edge.setArcInscription(arcInscription
							+ edge.getArcInscription());
				}
				Iterator<ColoredEdge> edgesOut = p.getOutEdgesIterator();
				while (edgesOut.hasNext()) {
					ColoredEdge edge = edgesOut.next();
					edge.setArcInscription(arcInscription);
				}
			}
		}
	}

	/**
	 * Returns the first transition on the subpage. The first transitions does
	 * either have the schedule, start or complete eventtype
	 * 
	 * @param subpage
	 *            ColoredPetriNet
	 * @return ColoredTransition
	 */
	protected ColoredTransition getFirstTransitionOnSubpage(
			ColoredPetriNet subpage) {
		ColoredTransition returnTransition = null;
		Iterator transitionsSchedule = subpage.getTransitions().iterator();
		while (transitionsSchedule.hasNext()) {
			ColoredTransition transition = (ColoredTransition) transitionsSchedule
					.next();
			if (transition.getEventType().equals(
					ColoredTransition.EventType.schedule)) {
				returnTransition = transition;
				break;
			}
		}
		if (returnTransition == null) {
			Iterator transitionsStart = subpage.getTransitions().iterator();
			while (transitionsStart.hasNext()) {
				ColoredTransition transition = (ColoredTransition) transitionsStart
						.next();
				if (transition.getEventType().equals(
						ColoredTransition.EventType.start)) {
					returnTransition = transition;
					break;
				}
			}
		}
		if (returnTransition == null) {
			Iterator transitionsComplete = subpage.getTransitions().iterator();
			while (transitionsComplete.hasNext()) {
				ColoredTransition transition = (ColoredTransition) transitionsComplete
						.next();
				if (transition.getEventType().equals(
						ColoredTransition.EventType.complete)) {
					returnTransition = transition;
					break;
				}
			}
		}

		return returnTransition;
	}

	/**
	 * Returns the first start transition on the subpage. Since the resource can
	 * not be assigen to schedule event, the first transitions does either have
	 * the start or complete eventtype
	 * 
	 * @param subpage
	 *            ColoredPetriNet
	 * @return ColoredTransition
	 */
	protected ColoredTransition getFirstTransitionOnSubpage_Resource(
			ColoredPetriNet subpage) {
		ColoredTransition returnTransition = null;
		Iterator transitionsStart = subpage.getTransitions().iterator();
		while (transitionsStart.hasNext()) {
			ColoredTransition transition = (ColoredTransition) transitionsStart
					.next();
			if (transition.getEventType().equals(
					ColoredTransition.EventType.start)) {
				returnTransition = transition;
				break;
			}
		}
		if (returnTransition == null) {
			Iterator transitionsComplete = subpage.getTransitions().iterator();
			while (transitionsComplete.hasNext()) {
				ColoredTransition transition = (ColoredTransition) transitionsComplete
						.next();
				if (transition.getEventType().equals(
						ColoredTransition.EventType.complete)) {
					returnTransition = transition;
					break;
				}
			}
		}

		return returnTransition;
	}

	/**
	 * Returns the last transition on the subpage. The last transition on the
	 * subpage does always have the complete eventtype which always exists
	 * 
	 * @param subpage
	 *            ColoredPetriNet
	 * @return ColoredTransition
	 */
	protected ColoredTransition getLastTransitionOnSubpage(
			ColoredPetriNet subpage) {
		ColoredTransition returnTransition = null;
		Iterator transitions = subpage.getTransitions().iterator();
		while (transitions.hasNext()) {
			ColoredTransition transition = (ColoredTransition) transitions
					.next();
			if (transition.getEventType().equals(
					ColoredTransition.EventType.complete)) {
				returnTransition = transition;
				break;
			}
		}

		return returnTransition;

	}

	/**
	 * Generates the layout for this petri net and all its underlying petri nets
	 * 
	 * @param pn
	 *            ColoredPetriNet the petri net that is the top of the structure
	 *            of hierarchical petri nets
	 * @param hierarchical
	 *            <code>true</code> when also for the subprocesses of the
	 *            transitions the layout needs to be generated.
	 *            <code>false</code> when only for the coloredpetriNet provided
	 *            by pn, the layout needs to be generated.
	 */
	private void generateLayoutHierarchicalPN(ColoredPetriNet pn,
			boolean hierarchical) {
		pn.getGrappaVisualization();
		// start with the grappa stuff
		Iterator transitions = pn.getTransitions().iterator();
		while (transitions.hasNext()) {
			ColoredTransition t = (ColoredTransition) transitions.next();
			Node n = t.visualObject;
			int x = (int) n.getCenterPoint().getX()
					* ManagerLayout.getInstance().getScaleFactor();
			int y = -(int) n.getCenterPoint().getY()
					* ManagerLayout.getInstance().getScaleFactor();
			int width = (int) (((Double) n.getAttributeValue(Grappa.WIDTH_ATTR))
					.doubleValue() * ManagerLayout.getInstance()
					.getStretchFactor());
			int height = (int) (((Double) n
					.getAttributeValue(Grappa.HEIGHT_ATTR)).doubleValue() * ManagerLayout
					.getInstance().getStretchFactor());
			t.setXCoordinate(x);
			t.setYCoordinate(y);
			t.setWidth(width);
			t.setHeight(height);
			// check for subpages
			// and check whether it is allowed to also generate the layout for
			// the subpages
			if (t.getSubpage() != null && hierarchical) {
				generateLayoutHierarchicalPN(t.getSubpage(), true);
			}
		}
		Iterator places = pn.getPlaces().iterator();
		while (places.hasNext()) {
			ColoredPlace p = (ColoredPlace) places.next();
			Node n = p.visualObject;
			int x = (int) n.getCenterPoint().getX()
					* ManagerLayout.getInstance().getScaleFactor();
			int y = -(int) n.getCenterPoint().getY()
					* ManagerLayout.getInstance().getScaleFactor();
			int width = (int) (((Double) n.getAttributeValue(Grappa.WIDTH_ATTR))
					.doubleValue() * ManagerLayout.getInstance()
					.getStretchFactor());
			int height = (int) (((Double) n
					.getAttributeValue(Grappa.HEIGHT_ATTR)).doubleValue() * ManagerLayout
					.getInstance().getStretchFactor());
			p.setXCoordinate(x);
			p.setYCoordinate(y);
			p.setWidth(width);
			p.setHeight(height);
		}
	}

	/**
	 * Stretches and scales the transitions and places in the given petri net
	 * 
	 * @param pn
	 *            ColoredPetriNet the petri net of which the transitions and
	 *            places needs to be stretched and scaled.
	 */
	private void scalingAndStretchingForPN(ColoredPetriNet pn) {
		Iterator transitions = pn.getTransitions().iterator();
		while (transitions.hasNext()) {
			ColoredTransition transition = (ColoredTransition) transitions
					.next();
			transition.setXCoordinate(transition.getXCoordinate()
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getScaleFactor()
							/ (double) ManagerLayout.DEFAULT_SCALE_FACTOR));
			transition.setYCoordinate(transition.getYCoordinate()
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getScaleFactor()
							/ (double) ManagerLayout.DEFAULT_SCALE_FACTOR));
			transition.setWidth(transition.getWidth()
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getStretchFactor()
							/ (double) ManagerLayout.DEFAULT_STRETCH_FACTOR));
			transition.setHeight(transition.getHeight()
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getStretchFactor()
							/ (double) ManagerLayout.DEFAULT_STRETCH_FACTOR));
		}
		Iterator places = pn.getPlaces().iterator();
		while (places.hasNext()) {
			ColoredPlace place = (ColoredPlace) places.next();
			place.setXCoordinate(place.getXCoordinate()
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getScaleFactor()
							/ (double) ManagerLayout.DEFAULT_SCALE_FACTOR));
			place.setYCoordinate(place.getYCoordinate()
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getScaleFactor()
							/ (double) ManagerLayout.DEFAULT_SCALE_FACTOR));
			place.setWidth(place.getWidth()
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getStretchFactor()
							/ (double) ManagerLayout.DEFAULT_STRETCH_FACTOR));
			place.setHeight(place.getHeight()
					* (int) Math.round((double) ManagerLayout.getInstance()
							.getStretchFactor()
							/ (double) ManagerLayout.DEFAULT_STRETCH_FACTOR));
		}
	}

	/**
	 * Returns the information that is needed for generating the monitor that
	 * keeps track of the resources that are available in the resources place
	 * 
	 * @return HashMap
	 */
	public HashMap<String, ArrayList> getInfoForMonitoringResources() {
		return monitoringResources;
	}

	/**
	 * Gets for the given transitions all the other transitions that are
	 * involved in the same choice based on frequencies.
	 * 
	 * @param transitionsFreqDep
	 *            HashSet. A hashset in which we only have one transition and
	 *            for which we want to obtain all the other transitions that are
	 *            involved in the same choice based on frequencies.
	 */
	private void getTransitionsForChoiceOnFrequencies(
			HashSet<ColoredTransition> transitionsFreqDep) {
		int oldSizeTransitions = transitionsFreqDep.size();
		HashSet<ColoredPlace> places = new HashSet<ColoredPlace>();
		Iterator<ColoredTransition> transitionsFreqDepIt = transitionsFreqDep
				.iterator();
		while (transitionsFreqDepIt.hasNext()) {
			ColoredTransition transition = transitionsFreqDepIt.next();
			places.addAll(transition.getPredecessors());
		}
		// get the outgoing transitions for each place in places
		Iterator<ColoredPlace> placesIt = places.iterator();
		while (placesIt.hasNext()) {
			ColoredPlace place = placesIt.next();
			transitionsFreqDep.addAll(place.getSuccessors());
		}

		if (transitionsFreqDep.size() > oldSizeTransitions) {
			getTransitionsForChoiceOnFrequencies(transitionsFreqDep);
		}
	}
}
