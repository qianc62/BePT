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

/**
 * <p>
 * Title: Protos string
 * </p>
 * 
 * <p>
 * Description: Holds all Protos string constants
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
public class ProtosString {
	static public String Activity = "activity";
	static public String ActivityApplication = "activityapplication";
	static public String ActivityData = "activitydata";
	static public String ActivityDistribution = "activitydistribution";
	static public String ActivityParty = "activityparty";
	static public String ActualData = "actualdata";
	static public String ActualRole = "actualrole";
	static public String Alignment = "alignment";
	static public String AnalysisIn = "analysis_in";
	static public String AnalysisOut = "analysis_out";
	static public String Application = "application";
	static public String ApplicationData = "applicationdata";
	static public String ApplicationParty = "applicationparty";
	static public String Arc = "arc";
	static public String ArcData = "arcdata";
	static public String Archive = "archive";
	static public String Argument = "argument";
	static public String AuthorisationActivity = "AuthorisationActivity";
	static public String Automatic = "automatic";
	static public String Available = "available";
	static public String BasicActivity = "BasicActivity";
	static public String BatchElapse = "batch_elapse";
	static public String BatchSelective = "batch_selective";
	static public String BatchSize = "batch_size";
	static public String BatchSpecified = "batch_specified";
	static public String Beta = "Beta";
	static public String Bold = "bold";
	static public String BooleanConstant = "booleanConstant";
	static public String Buffer = "buffer";
	static public String ChoiceExpression = "choiceExpression";
	static public String Color = "color";
	static public String CommunicationActivity = "CommunicationActivity";
	static public String ComScript = "comscript";
	static public String Condition = "condition";
	static public String ConditionExpression = "conditionExpression";
	static public String Constant = "Constant";
	static public String Consulted = "consulted";
	static public String ControlActivity = "ControlActivity";
	static public String Cost = "cost";
	static public String Created = "created";
	static public String Cyclic = "cyclic";
	static public String Data = "data";
	static public String DataAssignment = "dataassignment";
	static public String DataElement = "dataelement";
	static public String DataExpression = "dataExpression";
	static public String DataObject = "dataobject";
	static public String Deactivated = "deactivated";
	static public String DeadlineObject = "deadlineobject";
	static public String Deleted = "deleted";
	static public String Description = "description";
	static public String Distribution = "distribution";
	static public String DistributionData = "distributiondata";
	static public String Document = "document";
	static public String DocumentData = "documentdata";
	static public String DocumentParty = "documentparty";
	static public String Drawing = "drawing";
	static public String Duration = "duration";
	static public String DyadicExpression = "dyadicExpression";
	static public String Elapse = "elapse";
	static public String Ellipse = "ellipse";
	static public String ElseExpression = "elseExpression";
	static public String EnclosedExpression = "enclosedExpression";
	static public String EndModel = "endmodel";
	static public String EndProcess = "endprocess";
	static public String Enum = "enum";
	static public String Erlang = "Erlang";
	static public String Executor = "executor";
	static public String Export = "export";
	static public String ExternalName = "externalName";
	static public String False = "false";
	static public String Fillcolor = "fillcolor";
	static public String FloatConstant = "floatConstant";
	static public String FlowElement = "flowelement";
	static public String Folder = "folder";
	static public String FolderData = "folderdata";
	static public String FolderParty = "folderparty";
	static public String Font = "font";
	static public String FormalData = "formaldata";
	static public String FormalRole = "formalrole";
	static public String Format = "format";
	static public String From = "from";
	static public String FunctionExpression = "functionExpression";
	static public String Gamma = "Gamma";
	static public String Group = "group";
	static public String GroupRole = "grouprole";
	static public String Id = "id";
	static public String Informed = "informed";
	static public String IntegerConstant = "intergerConstant"; // typo?
	static public String InternalName = "internalName";
	static public String IOSpec = "iospec";
	static public String Italic = "italic";
	static public String Iteration = "iteration";
	static public String Label = "label";
	static public String LeftExpression = "leftExpression";
	static public String Length = "length";
	static public String Line = "line";
	static public String Linearrow = "linearrow";
	static public String Linecolor = "linecolor";
	static public String Linestyle = "linestyle";
	static public String Linewidth = "linewidth";
	static public String LinkModelActivity = "LinkModelActivity";
	static public String LogisticActivity = "LogisticActivity";
	static public String Mandatory = "mandatory";
	static public String ManualLayout = "manuallayout";
	static public String Maximum = "maximum";
	static public String Mean = "mean";
	static public String MetaNames = "metanames";
	static public String MetaType = "metatype";
	static public String Metavalue = "metavalue";
	static public String MetavalueEmail = "metavalueEmail";
	static public String MetavalueEnum = "metavalueEnum";
	static public String MetavalueFloat = "metavalueFloat";
	static public String MetavalueInteger = "metavalueInteger";
	static public String MetavalueObject = "metavalueObject";
	static public String MetavalueString = "metavalueString";
	static public String MetavalueUrl = "metavalueUrl";
	static public String Minimum = "minimum";
	static public String MonadicExpression = "monadicExpression";
	static public String Name = "name";
	static public String Nexp = "nexp";
	static public String NoLabel = "nolabel";
	static public String Normal = "Normal";
	static public String NrVarArgs = "nrvarargs";
	static public String Number = "number";
	static public String Object = "object";
	static public String Operator = "operator";
	static public String Organisation = "organisation";
	static public String Priority = "priority";
	static public String ProcessArc = "processarc";
	static public String ProcessFlow = "processflow";
	static public String ProcessModel = "processmodel";
	static public String Protos = "protos";
	static public String ProtosModel = "protosmodel";
	static public String ProtosModelOptions = "protosmodeloptions";
	static public String Rectangle = "rectangle";
	static public String Responsible = "responsible";
	static public String RestExpression = "restExpression";
	static public String RightExpression = "rightExpression";
	static public String Role = "role";
	static public String Rolearc = "rolearc";
	static public String RoleAssignment = "roleassignment";
	static public String RoleGraph = "rolegraph";
	static public String Rolegraph = "rolegraph";
	static public String RoleGroupObject = "rolegroupobject";
	static public String SameActivityDistributionActivity = "sameactivitydistributionactivity";
	static public String SameActivityDistributionOperator = "sameactivitydistributionoperator";
	static public String SameExecutorActivity = "sameexecutoractivity";
	static public String SameExecutorOperator = "sameexecutoroperator";
	static public String SameResponsibleActivity = "sameresponsibleactivity";
	static public String SameResponsibleOperator = "sameresponsibleoperator";
	static public String Scalar = "scalar";
	static public String ScalarParty = "scalarparty";
	static public String SimFrequency = "sim_frequency";
	static public String Simulation = "simulation";
	static public String SimulationAllocate = "simulation_allocate";
	static public String SimulationArrival = "SimulationArrival";
	static public String SimulationCost = "simulation_cost";
	static public String SimulationFrequency = "simulation_frequency";
	static public String SimulationWaiting = "simulation_waiting";
	static public String Size = "size";
	static public String StandardLetter = "standardletter";
	static public String StartModel = "startmodel";
	static public String StartProcess = "startprocess";
	static public String Status = "status";
	static public String StringConstant = "stringConstant";
	static public String Struct = "struct";
	static public String StructData = "structdata";
	static public String StructParty = "structparty";
	static public String SubProcess = "subprocess";
	static public String SubProcessActivity = "SubProcessActivity";
	static public String Suffix = "suffix";
	static public String SwitchValues = "switchvalues";
	static public String Text = "text";
	static public String ThenExpression = "thenExpression";
	static public String TimeConstant = "timeConstant";
	static public String To = "to";
	static public String Transparent = "transparent";
	static public String Triangle = "triangle";
	static public String Trigger = "trigger";
	static public String TriggerApplication = "triggerapplication";
	static public String TriggerData = "triggerdata";
	static public String TriggerParty = "triggerparty";
	static public String True = "true";
	static public String Type = "type";
	static public String Underline = "underline";
	static public String Uniform = "Uniform";
	static public String UrlBase = "urlBase";
	static public String UserInitiative = "userinitiative";
	static public String Variance = "variance";
	static public String WindowPlacement = "windowPlacement";
	static public String Workinstruction = "workinstruction";
	static public String X0 = "x0";
	static public String X1 = "x1";
	static public String X2 = "x2";
	static public String X3 = "x3";
	static public String Y0 = "y0";
	static public String Y1 = "y1";
	static public String Y2 = "y2";
	static public String Y3 = "y3";
	static public String YesLabel = "yeslabel";

	public ProtosString() {
	}
}
