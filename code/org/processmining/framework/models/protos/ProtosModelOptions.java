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

import org.processmining.framework.models.*;
import org.processmining.framework.models.protos.*;

import org.w3c.dom.*;

/**
 * <p>
 * Title: Protos model options
 * </p>
 * 
 * <p>
 * Description: Holds the Protos model options
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
public class ProtosModelOptions {
	private int handleWidth;
	private int handleHeight;
	private int roleRouteColumnSep;
	private int distributionRouteColumnSep;
	private int dataRouteColumnSep;
	private int applicationRouteColumnSep;
	private int roleRouteRowSep;
	private int distributionRouteRowSep;
	private int dataRouteRowSep;
	private int applicationRouteRowSep;
	private int roleRouteBlockSize;
	private int distributionRouteBlockSize;
	private int dataRouteBlockSize;
	private int applicationRouteBlockSize;
	private int grid;
	private int snapping;
	private int gridVisible;
	private HashSet fonts; // contains ProtosFonts
	private int batchSize;
	private int numberOfBatches;
	private int numberOfRuns;
	private int runLength;
	private ProtosStatisticalFunction simulationArrival;
	private String description;
	private String workinstruction;
	private boolean exportTitlePage;
	private boolean exportHeader;
	private boolean exportFooter;
	private boolean exportTableOfContents;
	private boolean exportDescription;
	private boolean exportWorkflowWorkinstruction;
	private boolean exportOverview;
	private boolean exportOverviewFigure;
	private boolean exportOverviewTable;
	private boolean exportRoles;
	private boolean exportRolesFigure;
	private boolean exportRolesTable;
	private boolean exportDistributions;
	private boolean exportGroups;
	private boolean exportAnalysis;
	private boolean exportAnalysisRoleRoute;
	private boolean exportAnalysisRoleTable;
	private boolean exportAnalysisDistributionRoute;
	private boolean exportAnalysisDistributionTable;
	private boolean exportAnalysisDataRoute;
	private boolean exportAnalysisApplicationRoute;
	private boolean exportProcess;
	private int exportProcessDescriptionInstruction;
	private int exportProcessDescriptionInstruction2;
	private boolean exportProcessFigure;
	private boolean exportProcessContents;
	private boolean exportProcessActivities;
	private boolean exportProcessTriggers;
	private boolean exportProcessStatus;
	private boolean exportProcessBuffers;
	private boolean exportProcessArcs;
	private boolean exportProcessApplications;
	private boolean exportProcessData;
	private boolean exportMeta;
	private boolean exportSuppressEmpty;
	private boolean exportActivityWorkinstruction;
	private boolean exportActivityDescription;
	private boolean exportActivityRole;
	private boolean exportActivityResponsible;
	private boolean exportActivityDistribution;
	private boolean exportActivityBatch;
	private boolean exportActivityData;
	private int exportActivityArc;
	private boolean exportActivityApplication;
	private boolean exportRoleDescription;
	private boolean exportRoleWorkinstruction;
	private boolean exportDistributionDescription;
	private boolean exportDistributionWorkinstruction;
	private boolean exportGroupDescription;
	private boolean exportGroupWorkinstruction;
	private boolean exportTriggerDescription;
	private boolean exportTriggerWorkinstruction;
	private boolean exportStatusDescription;
	private boolean exportStatusWorkinstruction;
	private boolean exportBufferDescription;
	private boolean exportBufferWorkinstruction;
	private boolean exportArcDescription;
	private boolean exportArcWorkinstruction;
	private boolean exportApplicationDescription;
	private boolean exportApplicationWorkinstruction;
	private boolean exportDataDescription;
	private boolean exportDataWorkinstruction;
	private boolean exportKeepRows;
	private boolean exportSimulation;
	private boolean exportActivityPart;
	private boolean exportTriggerPart;
	private boolean exportApplicationPart;
	private boolean exportDataPart;
	private boolean exportColorPrinter;
	private int exportRoleTableContents;
	private int exportDistributionTableContents;
	private double exportTableSeparation;
	private int exportWordProcessor;
	private int exportLayout;
	private boolean htmlMail;
	private String htmlMailAddress;
	private String htmlBaseDirectory;
	private boolean htmlEnvironment;
	private String htmlHomePage;
	private boolean htmlInstructionTop;
	private boolean htmlHyperlinks;
	private boolean htmlStartBrowser;
	private boolean htmlOrientationLeft;
	private boolean htmlMaximize;
	private boolean htmlMaximizeCurrentWindow;
	private boolean htmlActivity;
	private boolean htmlActivityApplications;
	private boolean htmlActivityData;
	private boolean htmlActivityDescription;
	private boolean htmlActivityMeta;
	private boolean htmlActivityInstruction;
	private boolean htmlActivityInvolved;
	private boolean htmlActivityResponsible;
	private boolean htmlActivityRole;
	private boolean htmlActivitySimulation;
	private boolean htmlActivityDistribution;
	private boolean htmlApplication;
	private boolean htmlApplicationData;
	private boolean htmlApplicationDescription;
	private boolean htmlApplicationMeta;
	private boolean htmlApplicationInstruction;
	private boolean htmlApplicationInvolved;
	private boolean htmlBuffer;
	private boolean htmlBufferDescription;
	private boolean htmlBufferMeta;
	private boolean htmlBufferInstruction;
	private boolean htmlConnection;
	private boolean htmlConnectionDescription;
	private boolean htmlConnectionMeta;
	private boolean htmlConnectionInstruction;
	private boolean htmlConnectionData;
	private boolean htmlData;
	private boolean htmlDataData;
	private boolean htmlDataDescription;
	private boolean htmlDataMeta;
	private boolean htmlDataInstruction;
	private boolean htmlDataInvolved;
	private boolean htmlGroup;
	private boolean htmlGroupData;
	private boolean htmlGroupDescription;
	private boolean htmlGroupMeta;
	private boolean htmlGroupInstruction;
	private boolean htmlProcessModel;
	private boolean htmlProcessModelDescription;
	private boolean htmlProcessModelMeta;
	private boolean htmlProcessModelInstruction;
	private boolean htmlRole;
	private boolean htmlRoleDescription;
	private boolean htmlRoleMeta;
	private boolean htmlRoleInstruction;
	private boolean htmlStatus;
	private boolean htmlStatusDescription;
	private boolean htmlStatusMeta;
	private boolean htmlStatusInstruction;
	private boolean htmlSubprocess;
	private boolean htmlSubprocessApplications;
	private boolean htmlSubprocessData;
	private boolean htmlSubprocessDescription;
	private boolean htmlSubprocessMeta;
	private boolean htmlSubprocessInstruction;
	private boolean htmlSubprocessInvolved;
	private boolean htmlSubprocessResponsible;
	private boolean htmlDistribution;
	private boolean htmlDistributionData;
	private boolean htmlDistributionDescription;
	private boolean htmlDistributionMeta;
	private boolean htmlDistributionInstruction;
	private boolean htmlTrigger;
	private boolean htmlTriggerApplications;
	private boolean htmlTriggerData;
	private boolean htmlTriggerDescription;
	private boolean htmlTriggerMeta;
	private boolean htmlTriggerInstruction;
	private boolean htmlTriggerInvolved;
	private boolean htmlTriggerSimulation;
	private boolean exportWorkflowHistory;
	private boolean htmlWeb1;
	private String htmlWeb1String;
	private boolean htmlWeb2;
	private String htmlWeb2String;
	private boolean exportStartWordProcessor;
	private boolean exportLinkModel;
	private boolean exportRoleTable;
	private boolean exportDistributionTable;
	private boolean exportDataRoute;
	private boolean exportApplicationRoute;
	private int responsibleRouteColumnSep;
	private int responsibleRouteRowSep;
	private int responsibleRouteBlockSize;
	private boolean exportResponsibleRoute;
	private boolean exportResponsibleTable;
	private boolean exportProcessResponsibleTable;
	private int responsibleTableContents;
	private boolean webSearch;
	private String webSearchString;
	private boolean webRegistration;
	private String webRegistrationString;
	private boolean exportActivityAssignment;
	private boolean showDrawing;
	private boolean exportRtfDrawing;
	private boolean exportHtmlDrawing;
	private boolean exportAnalysisRACITable;
	private int exportRACITableContents;
	private boolean exportHtmlSuppressEmpty;
	private int exportHtmlExecutorAnalysis;
	private int exportHtmlResponsibleAnalysis;
	private int exportHtmlDistributionAnalysis;
	private int exportHtmlDataAnalysis;
	private int exportHtmlApplicationAnalysis;
	private int exportHtmlRACIAnalysis;
	private int exportHtmlHistory;
	private HashSet caseNameObjects; // Contains IDs
	private HashSet caseOwners; // Contains IDs
	private String protosVersion;

	public ProtosModelOptions() {
		simulationArrival = new ProtosStatisticalFunction();
	}

	// added by Mariska Netjes
	public ProtosStatisticalFunction getArrival() {
		return simulationArrival;
	}

	/**
	 * Constructs a (for the time being: empty) Model Options object out of a
	 * Node.
	 * 
	 * @param optionsNode
	 *            Node The node that contains the Model Options.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node optionsNode) {
		String msg = "";
		NodeList nodes = optionsNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.SimulationArrival)) {
				msg += simulationArrival.readXMLExport(node);
			}
		}
		return msg;
	}

	/**
	 * Returns the (for the time being: empty) Model Options object in Protos
	 * XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Model Options object.
	 * @return String The Model Options object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag + ">";
		{
			xml += simulationArrival
					.writeXMLExport(ProtosString.SimulationArrival);
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
