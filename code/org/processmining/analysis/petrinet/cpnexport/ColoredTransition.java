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
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.analysis.petrinet.cpnexport.hltocpn.CpnAttributeManager;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.petrinet.Transition;

import att.grappa.Grappa;

/**
 * A transition being part of a high-level Petri net simulation model.
 * 
 * @see ColoredPetriNet
 * @see ColoredPlace
 * @see ColoredEdge
 * 
 * @author Anne Rozinat
 * @author Ronny Mans
 */
public class ColoredTransition extends Transition {

	/**
	 * Defines an enumeration type for the event types.
	 */
	public enum EventType {
		schedule, start, complete;
	}

	/**
	 * the subpageMapping for this transition.
	 */
	private SubpageMapping mySubpageMapping;

	/**
	 * Defines a link to sub Simulated Petri Net for this transition.
	 */
	private ColoredPetriNet mySubpage;

	/** The x coordinate of the center point for this place */
	private int centerX = 0;
	/** The y coordinate of the center point for this place */
	private int centerY = 0;
	/** The width of this place */
	private int width = 0;
	/** The height of this place */
	private int height = 0;
	/**
	 * The ID of this transition for the CPN file
	 */
	private String cpnID = "";

	/**
	 * The guard for this transition
	 */
	private String guard = "";

	/**
	 * The time delay for this transition
	 */
	private String timeDelay = "";

	/**
	 * The code inscription for this transition
	 */
	private String codeInscription = "";

	/**
	 * The event type for this transition (needed for the monitors)
	 */
	private EventType eventType = null;

	/**
	 * the object containing the high-level information referring to this
	 * process.
	 */
	private HLActivity highLevelTransition;

	/** configuration information about probability dependency */
	private boolean myProbabilityDependencyExists;

	/** configuration information about data dependency */
	private boolean myDataDependencyExists;

	/**
	 * Constructor to create a ColoredTransition without having a template
	 * transition.
	 * 
	 * @param name
	 *            the identifier is to be passed to super class
	 * @param net
	 *            the Petri net it belongs to (to be passed to super class)
	 */
	public ColoredTransition(String name, ColoredPetriNet net) {
		super(new LogEvent(name, "complete"), net);
		// this.setValue(template.getValue());
		this.setIdentifier(name);
	}

	/**
	 * Constructor to create a ColoredTransition without having a template
	 * transition.
	 * 
	 * @param name
	 *            the identifier is to be passed to super class
	 * @param net
	 *            the Petri net it belongs to (to be passed to super class)
	 */
	public ColoredTransition(String name, String nameEvent, ColoredPetriNet net) {
		super(new LogEvent(name, nameEvent), net);
		// this.setValue(template.getValue());
		this.setIdentifier(name);
	}

	/**
	 * Constructor to create a ColoredTransition without having a template
	 * transition.
	 * 
	 * @param name
	 *            the identifier is to be passed to super class
	 * @param net
	 *            the Petri net it belongs to (to be passed to super class)
	 * @param x
	 *            The x coordinate of the center point for this transition
	 * @param y
	 *            The y coordinate of the center point for this transition
	 * @param w
	 *            The width of this transition
	 * @param h
	 *            The height of this transition
	 */
	public ColoredTransition(String name, ColoredPetriNet net, int x, int y,
			int w, int h) {
		super(new LogEvent("name", "complete"), net);
		// this.setValue(template.getValue());
		this.setIdentifier(name);
		this.centerX = x;
		this.centerY = y;
		this.width = w;
		this.height = h;
	}

	/**
	 * The constructor creates a ColoredTransition from an ordinary transition.
	 * 
	 * @param template
	 *            the template transition for creating the object (to be passed
	 *            to super class)
	 * @param net
	 *            the Petri net it belongs to (to be passed to super class)
	 */
	public ColoredTransition(Transition template, ColoredPetriNet net) {
		super(template.getLogEvent(), net);
		this.setValue(template.getValue());
		this.setIdentifier(template.getIdentifier());

		centerX = (int) template.getCenterPoint().getX()
				* ManagerLayout.getInstance().getScaleFactor();
		centerY = -(int) template.getCenterPoint().getY()
				* ManagerLayout.getInstance().getScaleFactor(); // invert the y
		// axis
		// (everything
		// would be
		// upside down
		// otherwise)
		width = (int) (((Double) template.getAttributeValue(Grappa.WIDTH_ATTR))
				.doubleValue() * ManagerLayout.getInstance().getStretchFactor());
		height = (int) (((Double) template
				.getAttributeValue(Grappa.HEIGHT_ATTR)).doubleValue() * ManagerLayout
				.getInstance().getStretchFactor());
	}

	/**
	 * The constructor creates a ColoredTransition from an ordinary transition,
	 * but the position, width and height can be provided
	 * 
	 * @param template
	 *            Transition The template transition for creating the object (to
	 *            be passed to super class)
	 * @param net
	 *            PetriNet The Petri Net it belongs to (to be passed to the
	 *            super class)
	 * @param x
	 *            int The x coordinate of the center point for this transition
	 * @param y
	 *            int The y coordinate of the center point for this transition
	 * @param w
	 *            int the width of this transition
	 * @param h
	 *            int the height of this transition
	 */
	public ColoredTransition(Transition template, ColoredPetriNet net, int x,
			int y, int w, int h) {
		super(template.getLogEvent(), net);
		this.setValue(template.getValue());
		this.setIdentifier(template.getIdentifier());

		this.centerX = x;
		this.centerY = y;
		this.width = w;
		this.height = h;
	}

	/**
	 * Returns the object containing the high-level information referring to
	 * this transition.
	 * 
	 * @return the high-level transition information for this transition
	 */
	public HLActivity getHighLevelTransition() {
		return highLevelTransition;
	}

	/**
	 * Sets the high level activity for this transition
	 * 
	 * @param hlTransition
	 *            HLTransition the high level activity
	 */
	public void setHighLevelTransition(HLActivity hlTransition) {
		highLevelTransition = hlTransition;
	}

	/**
	 * Indicate whether a possibility dependency exists for this transition
	 * 
	 * @param dep
	 *            boolean <code>true</code> if yes, <code>false</code> otherwise
	 */
	public void existsProbabilityDependency(boolean dep) {
		myProbabilityDependencyExists = dep;
	}

	/**
	 * Retrieves whether a probability dependency exists for this transition
	 * 
	 * @return boolean <code>true</code> if a probability dependency exists,
	 *         <code>false</code> otherwise
	 */
	public boolean hasProbabilityDependency() {
		return myProbabilityDependencyExists;
	}

	/**
	 * Indicate whether a data dependency exists for this transition
	 * 
	 * @param dep
	 *            boolean <code>true</code> if yes, <code>false</code> otherwise
	 */
	public void existsDataDependency(boolean dep) {
		myDataDependencyExists = dep;
	}

	/**
	 * Retrieves whether a data dependency exists for this transition
	 * 
	 * @return boolean <code>true</code> if a data dependency exists,
	 *         <code>false</code> otherwise
	 */
	public boolean hasDataDependency() {
		return myDataDependencyExists;
	}

	/**
	 * Writes this transition to the cpn-file.
	 * 
	 * @param bw
	 *            BufferedWriter used to stream the data to the file.
	 * @throws java.io.IOException
	 */
	public void write(BufferedWriter bw) throws IOException {
		boolean isInvisible = this.isInvisibleTask();

		if (this.getSubpage() == null) {
			ManagerXml.writeTransitionTag(bw, this.getCpnID(), this
					.getXCoordinate(), this.getYCoordinate(), this
					.getIdentifier(), this.getWidth(), this.getHeight(), this
					.getGuard(), this.getTimeDelay(),
					this.getCodeInscription(), null, null, null, isInvisible);
		} else { // this transition has a subpage
			String subPageID = this.getSubpageMapping().getSubPageID();
			String subPageTagName = this.getSubpage().getIdentifier();
			ManagerXml.writeTransitionTag(bw, this.getCpnID(), this
					.getXCoordinate(), this.getYCoordinate(), this
					.getIdentifier(), this.getWidth(), this.getHeight(), this
					.getGuard(), this.getTimeDelay(),
					this.getCodeInscription(), subPageID, subPageTagName, this
							.getSubpageMapping().getMappings(), isInvisible);
		}
	}

	/**
	 * Writes the monitor for this transition, which is needed to enable the
	 * logging functionality in cpn-tools. So, when a transition is executed for
	 * some case-id that into the corresponding log of that case-id an audit
	 * trail entry is written.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file
	 * @param usedNamesMonitors
	 *            the list of names that have already been used for a monitor
	 * @throws IOException
	 */
	public void writeLoggingMonitor(BufferedWriter bw,
			ArrayList<String> usedNamesMonitors) throws IOException {
		// first check whether this transition is invisible
		// if invisible then no monitor may be generated
		if (!this.isInvisibleTask()) {
			ColoredPetriNet pn = ((ColoredPetriNet) this.getGraph());
			ArrayList<String> pageInstanceIDs = ((ColoredPetriNet) this
					.getGraph()).getPageInstanceIDs();
			String nameTransition = this.getIdentifier();
			String nameMonitor = "";
			// check whether the name of this transition has already been used
			// for another monitor
			if (usedNamesMonitors.contains(this.getIdentifier())) {
				// generate a new name for the monitor
				int counter = 1;
				while (usedNamesMonitors.contains(this.getIdentifier()
						+ counter)) {
					counter++;
				}
				nameMonitor = this.getIdentifier() + counter;
			} else {
				nameMonitor = this.getIdentifier();
			}
			usedNamesMonitors.add(nameMonitor);
			// the name of the monitor needs to be CPN compliant
			nameMonitor = CpnUtils.getCpnValidName(nameMonitor);
			String namePage = ((ColoredPetriNet) this.getGraph())
					.getIdentifier();
			// start with the bounded vars of this transition
			ArrayList<String> boundedVars = new ArrayList<String>();
			ArrayList<String> valuesTrue = new ArrayList<String>();
			ArrayList<String> valuesFalse = new ArrayList<String>();
			// in case of case-id (always present)
			CpnVarAndType cpnVarCaseId = pn.getTranslatorToCpn().variableTranslator
					.getCpnVarForCaseId();
			boundedVars.add(cpnVarCaseId.getVarName());
			valuesTrue.add(cpnVarCaseId.getVarName());
			valuesFalse.add(String.valueOf(0));
			// in case of resources
			String cpnTypeOfOriginator = "";
			if (ManagerConfiguration.getInstance()
					.isResourcePerspectiveEnabled()
					&& !getHighLevelTransition().isAutomatic()) {
				if (pn.getHighLevelProcess().getHLProcess().getResources()
						.size() > 0
						&& !((this.eventType == EventType.schedule) && ManagerConfiguration
								.getInstance().isPullEnabled())) {
					if (this.getHighLevelTransition().getGroup() != null
							&& this.getHighLevelTransition().getGroup()
									.getResources().size() > 0) {
						HLGroup group = this.getHighLevelTransition()
								.getGroup();
						CpnVarAndType cpnVar = pn.getTranslatorToCpn().variableTranslator
								.getCpnVarForGroup(group);
						boundedVars.add(cpnVar.getVarName());
						valuesTrue.add(cpnVar.getVarName());
						cpnTypeOfOriginator = cpnVar.getTypeName();
					} else {
						CpnVarAndType cpnVar = pn.getTranslatorToCpn().variableTranslator
								.getCpnVarForGroupAllResources();
						boundedVars.add(cpnVar.getVarName());
						valuesTrue.add(cpnVar.getVarName());
						cpnTypeOfOriginator = cpnVar.getTypeName();
					}
					valuesFalse.add("\"\"");
				}
			}
			// in case of data attributes
			String cpnTypeOfDataVar = "";
			HashMap<String, String> dataAttrMap = new HashMap<String, String>();
			if (ManagerConfiguration.getInstance().isDataPerspectiveEnabled()) {
				// modified data attributes var
				if (this.getHighLevelTransition().getOutputDataAttributes()
						.size() > 0
						&& this.getEventType().equals(
								ColoredTransition.EventType.complete)) {
					// bounded vars, values true, values false
					boundedVars.add(pn.getTranslatorToCpn().variableTranslator
							.getCpnVarForDataAttributes().getVarName());
					boundedVars.add(pn.getTranslatorToCpn().variableTranslator
							.getCpnVarForModifiedDataAttributes().getVarName());
					valuesTrue.add(pn.getTranslatorToCpn().variableTranslator
							.getCpnVarForModifiedDataAttributes().getVarName());
					// get the false string
					String valuesFalseDataAttr = "";
					for (HLAttribute dataAttr : pn.getHighLevelProcess()
							.getHLProcess().getAttributes()) {
						valuesFalseDataAttr = CpnAttributeManager
								.getFalseStringLoggingMonitors(dataAttr,
										valuesFalseDataAttr);
					}
					// remove the last ,
					if ((valuesFalseDataAttr.length() - 2 >= 0)) {
						valuesFalseDataAttr = valuesFalseDataAttr.substring(0,
								valuesFalseDataAttr.length() - 2);
					}
					valuesFalseDataAttr = "{ " + valuesFalseDataAttr + " }";
					valuesFalse.add(valuesFalseDataAttr);
					cpnTypeOfDataVar = pn.getTranslatorToCpn().variableTranslator
							.getCpnVarForDataAttributes().getTypeName();
					// now, obtain the name and the type for the data attributes
					// of the high level transition of this transition
					Iterator<HLAttribute> dataAttrs = getHighLevelTransition()
							.getOutputDataAttributes().iterator();
					while (dataAttrs.hasNext()) {
						HLAttribute dataAttr = dataAttrs.next();
						CpnVarAndType cpnVar = pn.getTranslatorToCpn().variableTranslator
								.getCpnVarForDataAttribute(dataAttr);
						dataAttrMap.put(cpnVar.getVarName(), cpnVar
								.getTypeName());
					}
				}
				// only data var
				// only do the if for the first transition
				if (pn.getHighLevelProcess().getHLProcess().getAttributes()
						.size() > 0
						&& this.hasDataDependency()) {
					// not needed if only one transition on page
					if (!(((ColoredPetriNet) this.getGraph()).getTransitions()
							.size() == 1 && getHighLevelTransition()
							.getOutputDataAttributes().size() > 0)) {
						boundedVars
								.add(pn.getTranslatorToCpn().variableTranslator
										.getCpnVarForDataAttributes()
										.getVarName());
					}
				}
			}
			// end data attributes

			// check whether the high level transition is involved in some
			// probability dependency
			if (this.hasProbabilityDependency()) {
				if ((ManagerConfiguration.getInstance()
						.isOnlyExecutionTimeEnabled() && this.getEventType()
						.equals(EventType.start))
						|| (ManagerConfiguration.getInstance()
								.isOnlyWaitingAndExecutionTimeEnabled() && this
								.getEventType().equals(EventType.schedule))
						|| (ManagerConfiguration.getInstance()
								.isOnlySojournTimeEnabled() && this
								.getEventType().equals(EventType.schedule))) {
					// get the vars that are involved in the probability
					// this is more or less a hack.
					Iterator<ColoredEdge> incomingArcs = this
							.getInEdgesIterator();
					while (incomingArcs.hasNext()) {
						ColoredEdge edge = incomingArcs.next();
						if (edge.getArcInscription().startsWith(
								pn.getTranslatorToCpn().cpnVarNameForProbDep)) {
							boundedVars.add(edge.getArcInscription());
						}
					}

					// boundedVars.add(pn.getTranslatorToCpn().getCpnVarForProbDep().getVarName());
				}
				if (!ManagerConfiguration.getInstance()
						.isTimePerspectiveEnabled()) {
					Iterator<ColoredEdge> incomingArcs = this
							.getInEdgesIterator();
					while (incomingArcs.hasNext()) {
						ColoredEdge edge = incomingArcs.next();
						if (edge.getArcInscription().startsWith(
								pn.getTranslatorToCpn().cpnVarNameForProbDep)) {
							boundedVars.add(edge.getArcInscription());
						}
					}
					// boundedVars.add(pn.getTranslatorToCpn().getCpnVarForProbDep().getVarName());
				}
			}

			// add the variable that needs to hold the start time stamp for the
			// case
			if (ManagerConfiguration.getInstance()
					.isThroughputTimeMonitorEnabled()) {
				boundedVars.add(pn.getTranslatorToCpn().variableTranslator
						.getCpnVarForStartCase().getVarName());
			}
			nameTransition = CpnUtils.getCpnValidName(nameTransition);
			namePage = CpnUtils.getCpnValidName(namePage);

			ManagerXml.writeBeginMonitorForFunction(bw, nameMonitor, this
					.getCpnID(), pageInstanceIDs);
			ManagerXml.writeMonitorInitFunction(bw);
			ManagerXml.writeMonitorPredicateFunction(bw, namePage,
					nameTransition, boundedVars, pageInstanceIDs.size());
			ManagerXml.writeMonitorObserverFunction(bw, namePage,
					nameTransition, boundedVars, valuesTrue, valuesFalse,
					pageInstanceIDs.size());
			ManagerXml.writeMonitorActionFunctionNormal(bw, CpnUtils
					.getCpnValidName(this.getIdentifier()), this.getEventType()
					.toString(), cpnVarCaseId.getTypeName(),
					cpnTypeOfOriginator, cpnTypeOfDataVar, dataAttrMap);
			ManagerXml.writeMonitorStopFunctionAndEndMonitor(bw);
		}
	}

	/**
	 * Writes the monitor for a initialisation transition. This is needed to
	 * enable the logging functionality in cpn-tools. This monitor that monitors
	 * the initialisation transition ensures that for each case-id that enters
	 * the net the corresponding mxml file is created
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file
	 * @throws IOException
	 */
	public void writeInitMonitor(BufferedWriter bw) throws IOException {
		ColoredPetriNet pn = ((ColoredPetriNet) this.getGraph());
		ArrayList<String> pageInstanceIDs = ((ColoredPetriNet) this.getGraph())
				.getPageInstanceIDs();
		String nameTransition = this.getIdentifier();
		String namePage = ((ColoredPetriNet) this.getGraph()).getIdentifier();
		ArrayList<String> boundedVars = new ArrayList<String>();
		boundedVars.add(pn.getTranslatorToCpn().variableTranslator
				.getCpnVarForCaseId().getVarName());
		ArrayList<String> valuesTrue = new ArrayList<String>();
		valuesTrue.add(pn.getTranslatorToCpn().variableTranslator
				.getCpnVarForCaseId().getVarName());
		ArrayList<String> valuesFalse = new ArrayList<String>();
		valuesFalse.add("0");
		ManagerXml.writeBeginMonitorForFunction(bw, nameTransition, this
				.getCpnID(), pageInstanceIDs);
		ManagerXml.writeMonitorInitFunctionForFolderGeneration(bw);
		ManagerXml.writeMonitorPredicateFunction(bw, namePage, nameTransition,
				boundedVars, 1);
		ManagerXml.writeMonitorObserverFunction(bw, namePage, nameTransition,
				boundedVars, valuesTrue, valuesFalse, 1);
		ManagerXml.writeMonitorActionFunctionForInit(bw);
		ManagerXml.writeMonitorStopFunctionAndEndMonitor(bw);
	}

	// ///////////////// GET + SET /////////////////////////////

	/**
	 * Returns the cpnID for this transition.
	 * 
	 * @return String the cpnID for this transition or "" if no such cpnID
	 *         exists.
	 */
	public String getCpnID() {
		return this.cpnID;
	}

	/**
	 * Sets the cpnID for this transition.
	 * 
	 * @param id
	 *            String the cpnID.
	 */
	public void setCpnID(String id) {
		this.cpnID = id;
	}

	/**
	 * Returns the sub Simulated PetriNet for this transition.
	 * 
	 * @return ColoredPetriNet the sub ColoredPetriNet. Null if no such submodel
	 *         exists.
	 */
	public ColoredPetriNet getSubpage() {
		return mySubpage;
	}

	/**
	 * Sets the subpage for this transition.
	 * 
	 * @param subpage
	 *            the subpage that belongs to this transition.
	 */
	public void setSubpage(ColoredPetriNet subpage) {
		mySubpage = subpage;
	}

	/**
	 * Returns the subpage mapping for this transition.
	 * 
	 * @return SubpageMapping the subpage mapping for this transition. Null is
	 *         returned if no such mapping exists.
	 */
	public SubpageMapping getSubpageMapping() {
		return mySubpageMapping;
	}

	/**
	 * Retrieves the x coordinate of the center point.
	 * 
	 * @return the x coordinate.
	 */
	public int getXCoordinate() {
		return centerX;
	}

	/**
	 * Retrieves the y coordinate of the center point.
	 * 
	 * @return the y coordinate.
	 */
	public int getYCoordinate() {
		return centerY;
	}

	/**
	 * Retrieves the width of this node.
	 * 
	 * @return the width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Retrieves the height of this node.
	 * 
	 * @return the height.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Retrieves the guard of this transition.
	 * 
	 * @return String the guard (cpn syntax).
	 */
	public String getGuard() {
		return this.guard;
	}

	/**
	 * Retrieves the time delay for this transition
	 * 
	 * @return String the time delay (cpn syntax).
	 */
	public String getTimeDelay() {
		return this.timeDelay;
	}

	/**
	 * Retrieves the code inscription of this transition.
	 * 
	 * @return String the code inscription (cpn syntax).
	 */
	public String getCodeInscription() {
		return this.codeInscription;
	}

	/**
	 * Retrieves the event type of this transition.
	 * 
	 * @return EventType the event type
	 */
	public EventType getEventType() {
		return this.eventType;
	}

	/**
	 * Set the subpageMapping for this node.
	 * 
	 * @param mapping
	 *            SubpageMapping.
	 */
	public void setSubpageMapping(SubpageMapping mapping) {
		mySubpageMapping = mapping;
	}

	/**
	 * Sets the x coordinate of the center point.
	 * 
	 * @param value
	 *            the new x coordinate to be assigned.
	 */
	public void setXCoordinate(int value) {
		centerX = value;
	}

	/**
	 * Sets the y coordinate of the center point.
	 * 
	 * @param value
	 *            the new y coordinate to be assigned.
	 */
	public void setYCoordinate(int value) {
		centerY = value;
	}

	/**
	 * Sets the width of this node.
	 * 
	 * @param value
	 *            the new width to be assigned.
	 */
	public void setWidth(int value) {
		width = value;
	}

	/**
	 * Sets the height of this node.
	 * 
	 * @param value
	 *            the new height to be assigned.
	 */
	public void setHeight(int value) {
		height = value;
	}

	/**
	 * Sets the guard of this node.
	 * 
	 * @param guard
	 *            String the guard to be assigned (cpn syntax).
	 */
	public void setGuard(String guard) {
		this.guard = guard;
	}

	/**
	 * Sets the time delay for this transition.
	 * 
	 * @param timeDelay
	 *            String the time delay to be assigned (cpn syntax).
	 */
	public void setTimeDelay(String timeDelay) {
		this.timeDelay = timeDelay;
	}

	/**
	 * Sets the code inscription of this transition.
	 * 
	 * @param inscription
	 *            String the code inscription to be assigned (cpn syntax).
	 */
	public void setCodeInscription(String inscription) {
		this.codeInscription = inscription;
	}

	/**
	 * Sets the event type of this transition
	 * 
	 * @param eventType
	 *            EventType the event type to be assigned (needed for the
	 *            monitors)
	 */
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

}
