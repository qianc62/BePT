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

package org.processmining.framework.models.protos;

import java.io.*;
import java.util.*;

import org.processmining.framework.models.*; //import org.processmining.framework.models.protos.ProtosStatisticalFunction;
import org.processmining.framework.models.protos.*;

import org.w3c.dom.*;
import org.processmining.framework.log.*;
import org.processmining.framework.models.yawl.YAWLTask;

/**
 * <p>
 * Title: Protos flow element
 * </p>
 * 
 * <p>
 * Description: Holds a Protos flow element
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
public class ProtosFlowElement extends ModelGraphVertex {
	static int FlowElementBasicActivity = 0;
	static int FlowElementLogisticActivity = 1;
	static int FlowElementAuthorisationActivity = 2;
	static int FlowElementCommunicationActivity = 3;
	static int FlowElementControlActivity = 4;
	static int FlowElementSubProcessActivity = 5;
	static int FlowElementLinkModelActivity = 6;
	static int FlowElementTrigger = 7;
	static int FlowElementBuffer = 8;
	static int FlowElementStatus = 9;

	private int type; // One of the values above.

	private String ID;
	private String name;
	private String description;
	private String workinstruction;
	private String subprocessID; // ProtosSubProcessActivity
	private int linkId; // ProtosLinkModelActivity
	private HashMap roleAssignments; // ProtosLinkModelActivity (from formalRole
	// (String) to actualRole (ID))
	private HashMap dataAssignments; // ProtosLinkModelActivity (from formalData
	// (String) to actualData (ID))
	private boolean startProcess; // Protos*Activity
	private boolean endProcess; // Protos*Activity
	private boolean startModel; // Protos*Activity
	private boolean endModel; // Protos*Activity
	private boolean userInitiative; // Protos*Activity
	private String iteration; // Protos*Activity
	private boolean batchSpecified; // Protos*Activity
	private int batchSize; // Protos*Activity
	private int batchElapse; // Protos*Activity
	private boolean batchSelective; // Protos*Activity
	private String executorID; // Protos*Activity, equal to the ProtosRole
	// variable ID
	private String sameExecutorOperator; // Protos*Activity
	private String sameExecutorActivityID; // Protos*Activity
	private String activityDistributionID; // Protos*Activity
	private String sameActivityDistributionOperator; // Protos*Activity
	private String sameActivityDistributionActivityID; // Protos*Activity
	private String responsibleID; // Protos*Activity
	private String sameResponsibleOperator; // Protos*Activity
	private String sameResponsibleActivityID; // Protos*Activity
	private int simulationAllocate; // Protos*Activity
	private double simulationCost; // Protos*Activity
	private double simulationFrequency; // Protos*Activity, ProtosTrigger
	private ProtosStatisticalFunction duration; // Protos*Activity
	private ProtosStatisticalFunction priority; // Protos*Activity
	private String analysisIn; // Protos*Activity, ProtosTrigger
	private String analysisOut; // Protos*Activity, ProtosTrigger
	private HashSet datas; // Protos*Activity (contains ProtosActivityDatas),
	// ProtosTrigger (contains IDs)
	private HashSet applications; // Protos*Activity (contains IDs)
	private HashSet parties; // Protos*Activity (contains
	// ProtosActivityParties), ProtosTrigger
	// (contains IDs)
	private HashSet metaValues; // Protos*Activity, ProtosTrigger, ProtosBuffer,
	// ProtosStatus (contains ProtosMetaValues)
	private String triggerType; // ProtosTrigger
	private int elapse; // ProtosTrigger
	private boolean cyclic; // ProtosTrigger
	private String deadlineObjectID; // ProtosTrigger
	private ProtosStatisticalFunction simulationWaiting; // ProtosTrigger

	private LogEvent event = null;
	private ProtosSubprocess subprocess = null;

	public ProtosFlowElement(ProtosSubprocess subprocess) {
		super(subprocess);
		this.subprocess = subprocess;
		subprocess.addVertex(this);

		duration = new ProtosStatisticalFunction();
		priority = new ProtosStatisticalFunction();
		roleAssignments = new HashMap();
		dataAssignments = new HashMap();
		datas = new HashSet();
		applications = new HashSet();
		parties = new HashSet();
		metaValues = new HashSet();
		simulationWaiting = new ProtosStatisticalFunction();
	}

	public ProtosFlowElement(ProtosSubprocess subprocess, String ID,
			String name, int type) {
		super(subprocess);
		this.subprocess = subprocess;
		subprocess.addVertex(this /* vertex */);

		this.type = type;
		this.ID = new String(ID);
		this.name = new String(name);
		description = "";
		workinstruction = "";

		setDotAttribute("label", name + "\\n[" + analysisIn + "," + analysisOut
				+ "]");
		if (type == FlowElementTrigger) {
			setDotAttribute("shape", "polygon");
			setDotAttribute("sides", "4");
			setDotAttribute("skew", ".4");
		} else if (type == FlowElementBuffer) {
			setDotAttribute("shape", "invtriangle");
		} else {
			setDotAttribute("shape", "box");
		}

		if (isActivity()) {
			startProcess = false;
			endProcess = false;
			startModel = false;
			endModel = false;
			userInitiative = false;
			iteration = "single";
			batchSpecified = false;
			batchSize = 0;
			batchElapse = 0;
			batchSelective = false;
			executorID = "";
			sameExecutorOperator = "same";
			sameExecutorActivityID = "";
			activityDistributionID = "";
			sameActivityDistributionOperator = "same";
			sameActivityDistributionActivityID = "";
			responsibleID = "";
			sameResponsibleOperator = "same";
			sameResponsibleActivityID = "";
			simulationAllocate = 1;
			simulationCost = 0.0;
			simulationFrequency = 1.0;
			analysisIn = "And";
			analysisOut = "And";
			elapse = 0;
		} else if (isStatus()) {
			setDotAttribute("shape", "ellipse");
			setDotAttribute("label", name);
		}

		duration = new ProtosStatisticalFunction();
		priority = new ProtosStatisticalFunction();
		roleAssignments = new HashMap();
		dataAssignments = new HashMap();
		datas = new HashSet();
		applications = new HashSet();
		parties = new HashSet();
		metaValues = new HashSet();
		simulationWaiting = new ProtosStatisticalFunction();
	}

	public void setStartEndActivity(boolean isStart) {
		if (isStart) {
			startProcess = true;
		} else {
			endProcess = true;
		}
	}

	public ProtosSubprocess getSubprocess() {
		return subprocess;
	}

	/*
	 * Kept in sake of backwards compatibility. Initially, the ProtosFlowElement
	 * and the underlying ModelGraphVertex were two separate objects, and this
	 * methods returned the vertex for the element. Now, both have been unified,
	 * and the method doesn't do much any more. However, it is being used now by
	 * several other plug-ins.
	 */
	public ModelGraphVertex getVertex() {
		return this;
	}

	public void setLogEvent(LogEvent event) {
		this.event = event;
	}

	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String getName() {
		return name == null ? super.getName() : name;
	}

	// executorID is equal to the role ID
	public String getRole() {
		return executorID;
	}

	public HashSet<ProtosActivityData> getDatas() {
		return datas;
	}

	public HashSet<ProtosActivityData> getOutputDatas() {
		HashSet<ProtosActivityData> outputDatas = new HashSet<ProtosActivityData>();
		for (ProtosActivityData data : getDatas()) {
			// output data is created within the activity.
			if (data.isCreated()) {
				outputDatas.add(data);
			}
		}
		return outputDatas;
	}

	public HashSet<ProtosActivityData> getInputDatas() {
		HashSet<ProtosActivityData> inputDatas = new HashSet<ProtosActivityData>();
		for (ProtosActivityData data : getDatas()) {
			// input data is mandatory for the execution of the activity and it
			// is not created by the activity.
			if (data.isMandatory() && !data.isCreated()) {
				inputDatas.add(data);
			}
		}
		return inputDatas;
	}

	public ProtosStatisticalFunction getDuration() {
		return duration;
	}

	public int getWaitingTime() {
		return elapse;
	}

	public ProtosStatisticalFunction getPriority() {
		return priority;
	}

	public double getFrequency() {
		return simulationFrequency;
	}

	public boolean isActivity() {
		return type == FlowElementBasicActivity
				|| type == FlowElementLogisticActivity
				|| type == FlowElementAuthorisationActivity
				|| type == FlowElementCommunicationActivity
				|| type == FlowElementControlActivity
				|| type == FlowElementSubProcessActivity
				|| type == FlowElementLinkModelActivity;
	}

	public boolean isStatus() {
		return type == FlowElementStatus;
	}

	public boolean isTrigger() {
		return type == FlowElementTrigger;
	}

	/**
	 * Usign YAWLTask here ... As a result, this plug-in depends on the YAWL
	 * model.
	 * 
	 * @return int
	 */
	public int getJoinType() {
		if (analysisIn.toLowerCase().equals("and")) {
			return YAWLTask.AND;
		} else if (analysisIn.toLowerCase().equals("xor")) {
			return YAWLTask.XOR;
		} else if (analysisIn.toLowerCase().equals("or")) {
			return YAWLTask.OR;
		}
		return YAWLTask.NONE;
	}

	public int getSplitType() {
		if (analysisOut.toLowerCase().equals("and")) {
			return YAWLTask.AND;
		} else if (analysisOut.toLowerCase().equals("xor")) {
			return YAWLTask.XOR;
		} else if (analysisOut.toLowerCase().equals("or")) {
			return YAWLTask.OR;
		}
		return YAWLTask.NONE;
	}

	public int setJoinType(int type) {
		int oldType = getJoinType();
		if (type == YAWLTask.AND) {
			analysisIn = "and";
		} else if (type == YAWLTask.XOR) {
			analysisIn = "xor";
		} else if (type == YAWLTask.OR) {
			analysisIn = "or";
		} else {
			analysisIn = "none";
		}
		setDotAttribute("label", name + "\\n[" + analysisIn + "," + analysisOut
				+ "]");
		return oldType;
	}

	public int setSplitType(int type) {
		int oldType = getSplitType();
		if (type == YAWLTask.AND) {
			analysisOut = "and";
		} else if (type == YAWLTask.XOR) {
			analysisOut = "xor";
		} else if (type == YAWLTask.OR) {
			analysisOut = "or";
		} else {
			analysisOut = "none";
		}
		setDotAttribute("label", name + "\\n[" + analysisIn + "," + analysisOut
				+ "]");
		return oldType;
	}

	/**
	 * Constructs the Link Model Activity of a Flow Element object out of a
	 * "LinkModelActivity" Node.
	 * 
	 * @param flowElementNode
	 *            Node The "LinkModelActivity" node that contains the Link Model
	 *            Activity.
	 * @return String Any error message.
	 */
	private String readXMLExportLinkModelActivity(Node linkModelActivityNode) {
		String msg = "";
		NodeList nodes = linkModelActivityNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Id)) {
				linkId = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.RoleAssignment)) {
				NodeList subNodes = node.getChildNodes();
				String formal = "";
				String actualID = "";
				for (int j = 0; j < subNodes.getLength(); j++) {
					Node subNode = subNodes.item(j);
					if (subNode.getNodeName().equals(ProtosString.FormalRole)) {
						formal = ProtosUtil.readString(subNode);
					} else if (subNode.getNodeName().equals(
							ProtosString.ActualRole)) {
						actualID = ProtosUtil.readString(subNode);
					}
				}
				if (formal.length() > 0 && actualID.length() > 0) {
					roleAssignments.put(formal, actualID);
				}
			} else if (node.getNodeName().equals(ProtosString.DataAssignment)) {
				NodeList subNodes = node.getChildNodes();
				String formal = "";
				String actualID = "";
				for (int j = 0; j < subNodes.getLength(); j++) {
					Node subNode = subNodes.item(j);
					if (subNode.getNodeName().equals(ProtosString.FormalData)) {
						formal = ProtosUtil.readString(subNode);
					} else if (subNode.getNodeName().equals(
							ProtosString.ActualData)) {
						actualID = ProtosUtil.readString(subNode);
					}
				}
				if (formal.length() > 0 && actualID.length() > 0) {
					dataAssignments.put(formal, actualID);
				}
			}
		}
		return msg;
	}

	/**
	 * Constructs the type of an "activity" Flow Element object out of a "type"
	 * Node.
	 * 
	 * @param typeNode
	 *            Node The "type" node that contains the type.
	 * @return String Any error message.
	 */
	private String readXMLExportType(Node typeNode) {
		String msg = "";
		NodeList nodes = typeNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.BasicActivity)) {
				type = FlowElementBasicActivity;
			} else if (node.getNodeName().equals(ProtosString.LogisticActivity)) {
				type = FlowElementLogisticActivity;
			} else if (node.getNodeName().equals(
					ProtosString.AuthorisationActivity)) {
				type = FlowElementAuthorisationActivity;
			} else if (node.getNodeName().equals(
					ProtosString.CommunicationActivity)) {
				type = FlowElementCommunicationActivity;
			} else if (node.getNodeName().equals(ProtosString.ControlActivity)) {
				type = FlowElementControlActivity;
			} else if (node.getNodeName().equals(
					ProtosString.SubProcessActivity)) {
				type = FlowElementSubProcessActivity;
				subprocessID = ProtosUtil.readString(node);
			} else if (node.getNodeName()
					.equals(ProtosString.LinkModelActivity)) {
				type = FlowElementLinkModelActivity;
				msg += readXMLExportLinkModelActivity(node);
			}
		}
		return msg;
	}

	/**
	 * Constructs a Flow Element object (except for its type if not activity)
	 * out of an "activity", "trigger", "buffer", or "status" Node.
	 * 
	 * @param anyNode
	 *            Node The "activity", "trigger", "buffer", or "status" node
	 *            that contains the Flow Element.
	 * @return String Any error message.
	 */
	private String readXMLExportAny(Node anyNode) {
		String msg = "";
		ID = anyNode.getAttributes().getNamedItem(ProtosString.Id)
				.getNodeValue();
		NodeList nodes = anyNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Name)) {
				name = ProtosUtil.readString(node);
				setIdentifier(name);
			} else if (node.getNodeName().equals(ProtosString.Description)) {
				description = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Workinstruction)) {
				workinstruction = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Type)) {
				if (type == FlowElementTrigger) {
					triggerType = ProtosUtil.readString(node);
				} else {
					msg += readXMLExportType(node);
				}
			} else if (node.getNodeName().equals(ProtosString.Elapse)) {
				elapse = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Cyclic)) {
				cyclic = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.StartProcess)) {
				startProcess = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.StartModel)) {
				startModel = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.EndProcess)) {
				endProcess = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.EndModel)) {
				endModel = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.UserInitiative)) {
				userInitiative = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.Iteration)) {
				iteration = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.BatchSpecified)) {
				batchSpecified = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.BatchSize)) {
				batchSize = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.BatchElapse)) {
				batchElapse = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.BatchSelective)) {
				batchSelective = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.Executor)) {
				executorID = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(
					ProtosString.SameExecutorOperator)) {
				sameExecutorOperator = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(
					ProtosString.SameExecutorActivity)) {
				sameExecutorActivityID = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(
					ProtosString.ActivityDistribution)) {
				activityDistributionID = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(
					ProtosString.SameActivityDistributionOperator)) {
				sameActivityDistributionOperator = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(
					ProtosString.SameActivityDistributionActivity)) {
				sameActivityDistributionActivityID = ProtosUtil
						.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Responsible)) {
				responsibleID = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(
					ProtosString.SameResponsibleOperator)) {
				sameResponsibleOperator = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(
					ProtosString.SameResponsibleActivity)) {
				sameResponsibleActivityID = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(
					ProtosString.SimulationAllocate)) {
				simulationAllocate = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.SimulationCost)) {
				simulationCost = ProtosUtil.readDouble(node);
			} else if (node.getNodeName().equals(
					ProtosString.SimulationFrequency)) {
				simulationFrequency = ProtosUtil.readDouble(node);
			} else if (node.getNodeName().equals(ProtosString.Duration)) {
				msg += duration.readXMLExport(node);
			} else if (node.getNodeName().equals(ProtosString.Priority)) {
				msg += priority.readXMLExport(node);
			} else if (node.getNodeName().equals(ProtosString.AnalysisIn)) {
				analysisIn = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.AnalysisOut)) {
				analysisOut = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.ActivityData)) {
				ProtosActivityData data = new ProtosActivityData();
				msg += data.readXMLExport(node);
				datas.add(data);
			} else if (node.getNodeName().equals(
					ProtosString.ActivityApplication)) {
				String applicationID = ProtosUtil.readString(node);
				applications.add(applicationID);
			} else if (node.getNodeName().equals(ProtosString.ActivityParty)) {
				String partyID = ProtosUtil.readString(node);
				parties.add(partyID);
			} else if (node.getNodeName().equals(ProtosString.Metavalue)) {
				ProtosMetavalue metaValue = new ProtosMetavalue();
				msg += metaValue.readXMLExport(node);
				metaValues.add(metaValue);
			}
		}
		return msg;
	}

	/**
	 * Constructs a Flow Element object out of a Node.
	 * 
	 * @param flowElementNode
	 *            Node The node that contains the Flow Element.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node flowElementNode) {
		String msg = "";
		NodeList nodes = flowElementNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Activity)) {
				// type is deferred
				msg += readXMLExportAny(node);
				setDotAttribute("shape", "box");
			} else if (node.getNodeName().equals(ProtosString.Trigger)) {
				type = FlowElementTrigger;
				msg += readXMLExportAny(node);
				setDotAttribute("shape", "polygon");
				setDotAttribute("sides", "4");
				setDotAttribute("skew", ".4");
			} else if (node.getNodeName().equals(ProtosString.Buffer)) {
				type = FlowElementBuffer;
				msg += readXMLExportAny(node);
				setDotAttribute("shape", "invtriangle");
			} else if (node.getNodeName().equals(ProtosString.Status)) {
				type = FlowElementStatus;
				msg += readXMLExportAny(node);
				setDotAttribute("shape", "ellipse");
			}
			if (type == FlowElementStatus) {
				setDotAttribute("label", name);
			} else {
				setDotAttribute("label", name + "\\n[" + analysisIn + ","
						+ analysisOut + "]");
			}
		}
		return msg;
	}

	/**
	 * Returns the Activity object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Activity object.
	 * @return String The Activity object in Protos XML Export format.
	 */
	private String writeXMLExportActivity(String tag) {
		String xml = "";
		xml += "<" + tag;
		xml += " " + ProtosString.Id + "=\"" + ID;
		xml += "\">";
		{
			xml += ProtosUtil.writeString(ProtosString.Name, name);
			xml += "<" + ProtosString.Type + ">";
			{
				if (type == FlowElementBasicActivity) {
					xml += "<" + ProtosString.BasicActivity + "/>";
				} else if (type == FlowElementLogisticActivity) {
					xml += "<" + ProtosString.LogisticActivity + "/>";
				} else if (type == FlowElementAuthorisationActivity) {
					xml += "<" + ProtosString.AuthorisationActivity + "/>";
				} else if (type == FlowElementCommunicationActivity) {
					xml += "<" + ProtosString.CommunicationActivity + "/>";
				} else if (type == FlowElementControlActivity) {
					xml += "<" + ProtosString.ControlActivity + "/>";
				} else if (type == FlowElementSubProcessActivity) {
					xml += ProtosUtil.writeString(
							ProtosString.SubProcessActivity, subprocessID);
				} else if (type == FlowElementLinkModelActivity) {
					xml += "<" + ProtosString.LinkModelActivity + ">";
					{
						xml += ProtosUtil.writeInt(ProtosString.Id, linkId);
						for (Iterator it = roleAssignments.keySet().iterator(); it
								.hasNext();) {
							xml += "<" + ProtosString.RoleAssignment + ">";
							{
								String formal = (String) it.next();
								String actual = (String) roleAssignments
										.get(formal);
								xml += ProtosUtil.writeString(
										ProtosString.FormalRole, formal);
								xml += ProtosUtil.writeString(
										ProtosString.ActualRole, actual);
							}
							xml += "</" + ProtosString.RoleAssignment + ">";
						}
						for (Iterator it = dataAssignments.keySet().iterator(); it
								.hasNext();) {
							xml += "<" + ProtosString.DataAssignment + ">";
							{
								String formal = (String) it.next();
								String actual = (String) dataAssignments
										.get(formal);
								xml += ProtosUtil.writeString(
										ProtosString.FormalData, formal);
								xml += ProtosUtil.writeString(
										ProtosString.ActualData, actual);
							}
							xml += "</" + ProtosString.DataAssignment + ">";
						}
					}
					xml += "</" + ProtosString.LinkModelActivity + ">";
				}
			}
			xml += "</" + ProtosString.Type + ">";
			xml += ProtosUtil
					.writeBool(ProtosString.StartProcess, startProcess);
			xml += ProtosUtil.writeBool(ProtosString.StartModel, startModel);
			xml += ProtosUtil.writeBool(ProtosString.EndProcess, endProcess);
			xml += ProtosUtil.writeBool(ProtosString.EndModel, endModel);
			xml += ProtosUtil.writeBool(ProtosString.UserInitiative,
					userInitiative);
			xml += ProtosUtil.writeString(ProtosString.Iteration, iteration);
			xml += ProtosUtil.writeBool(ProtosString.BatchSpecified,
					batchSpecified);
			xml += ProtosUtil.writeInt(ProtosString.BatchSize, batchSize);
			xml += ProtosUtil.writeInt(ProtosString.BatchElapse, batchElapse);
			xml += ProtosUtil.writeBool(ProtosString.BatchSelective,
					batchSelective);
			xml += ProtosUtil.writeStringIfNonEmpty(ProtosString.Executor,
					executorID);
			xml += ProtosUtil.writeStringIfNonEmpty(
					ProtosString.SameExecutorOperator, sameExecutorOperator);
			xml += ProtosUtil.writeStringIfNonEmpty(
					ProtosString.SameExecutorActivity, sameExecutorActivityID);
			xml += ProtosUtil.writeStringIfNonEmpty(
					ProtosString.ActivityDistribution, activityDistributionID);
			xml += ProtosUtil.writeStringIfNonEmpty(
					ProtosString.SameActivityDistributionOperator,
					sameActivityDistributionOperator);
			xml += ProtosUtil.writeStringIfNonEmpty(
					ProtosString.SameActivityDistributionActivity,
					sameActivityDistributionActivityID);
			xml += ProtosUtil.writeStringIfNonEmpty(ProtosString.Responsible,
					responsibleID);
			xml += ProtosUtil.writeStringIfNonEmpty(
					ProtosString.SameResponsibleOperator,
					sameResponsibleOperator);
			xml += ProtosUtil.writeStringIfNonEmpty(
					ProtosString.SameResponsibleActivity,
					sameResponsibleActivityID);
			xml += ProtosUtil.writeInt(ProtosString.SimulationAllocate,
					simulationAllocate);
			xml += ProtosUtil.writeDouble(ProtosString.SimulationCost,
					simulationCost);
			xml += ProtosUtil.writeDouble(ProtosString.SimulationFrequency,
					simulationFrequency);
			xml += duration.writeXMLExport(ProtosString.Duration);
			xml += priority.writeXMLExport(ProtosString.Priority);
			xml += ProtosUtil.writeString(ProtosString.AnalysisIn, analysisIn);
			xml += ProtosUtil
					.writeString(ProtosString.AnalysisOut, analysisOut);
			xml += ProtosUtil
					.writeString(ProtosString.Description, description);
			xml += ProtosUtil.writeString(ProtosString.Workinstruction,
					workinstruction);
			for (Iterator it = datas.iterator(); it.hasNext();) {
				ProtosActivityData data = (ProtosActivityData) it.next();
				xml += data.writeXMLExport(ProtosString.ActivityData);
			}
			for (Iterator it = applications.iterator(); it.hasNext();) {
				String application = (String) it.next();
				xml += ProtosUtil.writeString(ProtosString.ActivityApplication,
						application);
				;
			}
			for (Iterator it = parties.iterator(); it.hasNext();) {
				ProtosActivityParty party = (ProtosActivityParty) it.next();
				xml += party.writeXMLExport(ProtosString.ActivityParty);
			}
			for (Iterator it = metaValues.iterator(); it.hasNext();) {
				ProtosMetavalue metavalue = (ProtosMetavalue) it.next();
				xml += metavalue.writeXMLExport(ProtosString.Metavalue);
			}
		}
		xml += "</" + tag + ">";
		return xml;
	}

	/**
	 * Returns the Trigger object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Trigger object.
	 * @return String The Trigger object in Protos XML Export format.
	 */
	private String writeXMLExportTrigger(String tag) {
		String xml = "";
		xml += "<" + tag;
		xml += " " + ProtosString.Id + "=\"" + ID;
		xml += "\">";
		{
			xml += ProtosUtil.writeString(ProtosString.Name, name);
			xml += ProtosUtil.writeString(ProtosString.Type, triggerType);
			xml += ProtosUtil.writeInt(ProtosString.Elapse, elapse);
			xml += ProtosUtil.writeBool(ProtosString.Cyclic, cyclic);
			xml += ProtosUtil.writeDouble(ProtosString.SimulationFrequency,
					simulationFrequency);
			xml += simulationWaiting
					.writeXMLExport(ProtosString.SimulationWaiting);
			xml += ProtosUtil.writeString(ProtosString.AnalysisIn, analysisIn);
			xml += ProtosUtil
					.writeString(ProtosString.AnalysisOut, analysisOut);
			xml += ProtosUtil
					.writeString(ProtosString.Description, description);
			xml += ProtosUtil.writeString(ProtosString.Workinstruction,
					workinstruction);
			xml += ProtosUtil.writeStringIfNonEmpty(
					ProtosString.DeadlineObject, deadlineObjectID);
			for (Iterator it = datas.iterator(); it.hasNext();) {
				String data = (String) it.next();
				xml += ProtosUtil.writeString(ProtosString.TriggerData, data);
			}
			for (Iterator it = applications.iterator(); it.hasNext();) {
				String application = (String) it.next();
				xml += ProtosUtil.writeString(ProtosString.TriggerApplication,
						application);
				;
			}
			for (Iterator it = parties.iterator(); it.hasNext();) {
				String party = (String) it.next();
				xml += ProtosUtil.writeString(ProtosString.TriggerParty, party);
				;
			}
			for (Iterator it = metaValues.iterator(); it.hasNext();) {
				ProtosMetavalue metavalue = (ProtosMetavalue) it.next();
				xml += metavalue.writeXMLExport(ProtosString.ActivityParty);
			}
		}
		xml += "</" + tag + ">";
		return xml;
	}

	/**
	 * Returns the Buffer or Status object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Buffer or Status object.
	 * @return String The Buffer or Status object in Protos XML Export format.
	 */
	private String writeXMLExportBufferOrStatus(String tag) {
		String xml = "";
		xml += "<" + tag;
		xml += " " + ProtosString.Id + "=\"" + ID;
		xml += "\">";
		{
			xml += ProtosUtil.writeString(ProtosString.Name, name);
			xml += ProtosUtil
					.writeString(ProtosString.Description, description);
			xml += ProtosUtil.writeString(ProtosString.Workinstruction,
					workinstruction);
			for (Iterator it = metaValues.iterator(); it.hasNext();) {
				ProtosMetavalue metavalue = (ProtosMetavalue) it.next();
				xml += metavalue.writeXMLExport(ProtosString.ActivityParty);
			}
		}
		xml += "</" + tag + ">";
		return xml;
	}

	/**
	 * Returns the Flow Element object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Flow Element object.
	 * @return String The Flow Element object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag + ">";
		{
			if (type == FlowElementBasicActivity
					|| type == FlowElementLogisticActivity
					|| type == FlowElementAuthorisationActivity
					|| type == FlowElementCommunicationActivity
					|| type == FlowElementControlActivity
					|| type == FlowElementSubProcessActivity
					|| type == FlowElementLinkModelActivity) {
				xml += writeXMLExportActivity(ProtosString.Activity);
			} else if (type == FlowElementTrigger) {
				xml += writeXMLExportTrigger(ProtosString.Trigger);
			} else if (type == FlowElementBuffer) {
				xml += writeXMLExportBufferOrStatus(ProtosString.Buffer);
			} else if (type == FlowElementStatus) {
				xml += writeXMLExportBufferOrStatus(ProtosString.Status);
			}
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
