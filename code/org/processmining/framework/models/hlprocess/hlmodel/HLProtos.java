package org.processmining.framework.models.hlprocess.hlmodel;

/*
 * Author: Mariska Netjes
 * (c) 2008 Technische Universiteit Eindhoven and STW
 */

import java.util.ArrayList;
import java.util.List;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.models.hlprocess.distribution.*;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution.DistributionEnum;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLTypes.Perspective;
import org.processmining.framework.models.yawl.YAWLTask;
import org.processmining.framework.models.protos.ProtosModel;
import org.processmining.framework.models.protos.ProtosModelOptions;
import org.processmining.framework.models.protos.ProtosSubprocess;
import org.processmining.framework.models.protos.ProtosFlowElement;
import org.processmining.framework.models.protos.ProtosRole;
import org.processmining.framework.models.protos.ProtosStatisticalFunction;
import org.processmining.framework.models.protos.ProtosDataElement;
import org.processmining.framework.models.protos.ProtosActivityData;
import org.processmining.framework.models.protos.ProtosProcessArc;
import org.processmining.framework.models.protos.ProtosExpression;
import att.grappa.Edge;
import org.processmining.framework.models.hlprocess.expr.*;
import org.processmining.framework.models.hlprocess.expr.operand.*;
import org.processmining.framework.models.hlprocess.expr.operator.*;
import org.processmining.framework.models.hlprocess.att.*;
import org.processmining.framework.ui.Message;

/**
 * High-level process model implementation for Protos models. <br>
 * Maps ProtosFlowElements.isActivity onto HLActivities and both
 * ProtosFlowElements.isStatus with more than one outgoing arc as well as
 * ProtosFlowElements.isActivity with XOR split semantics to HLChoices. Further
 * maps ProtosDataElement onto HLAttribute, ProtosRole onto HLGroup, variable
 * available (int) in ProtosRole onto HLResource, ... onto HLCondition and
 * variable simulationArrival (ProtosStatisticalFunction) in ProtosModelOptions
 * onto variable caseGenerationScheme (HLDistribution) in HLGlobal and variable
 * duration (ProtosStatisticalFunction) in ProtosFlowElement onto variable
 * executionTime (HLDistribution) in HLActivity.
 */
public class HLProtos extends HLModel {

	/**
	 * Creates a high-level Protos model with default high-level information.
	 * 
	 * @param aProtosModel
	 *            the underlying (low-level) Protos model
	 */
	public HLProtos(ProtosModel aProtosModel) {
		super(aProtosModel);
		initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.hlmodel.HLModel#initialize()
	 */
	protected void initialize() {
		super.initialize();
		ProtosModel protosModel = (ProtosModel) model;

		// add perspectives
		hlProcess.getGlobalInfo().addPerspective(
				HLTypes.Perspective.ORGANIZATIONAL_MODEL);
		hlProcess.getGlobalInfo().addPerspective(
				HLTypes.Perspective.ROLES_AT_TASKS);
		hlProcess.getGlobalInfo().addPerspective(
				HLTypes.Perspective.CASE_GEN_SCHEME);
		hlProcess.getGlobalInfo().addPerspective(
				HLTypes.Perspective.DATA_INITIAL_VAL);
		hlProcess.getGlobalInfo().addPerspective(
				HLTypes.Perspective.DATA_AT_TASKS);
		hlProcess.getGlobalInfo().addPerspective(
				HLTypes.Perspective.TIMING_EXECTIME);
		hlProcess.getGlobalInfo().addPerspective(
				HLTypes.Perspective.TIMING_WAITTIME);
		hlProcess.getGlobalInfo().addPerspective(
				HLTypes.Perspective.CHOICE_CONF);
		hlProcess.getGlobalInfo().addPerspective(
				HLTypes.Perspective.CHOICE_FREQ);

		// Maps the Protos simulationArrival, i.e. the arrival rate and its
		// distribution onto the CaseGenerationSchema.
		ProtosStatisticalFunction function = protosModel.getModelOptions()
				.getArrival();
		HLGeneralDistribution caseGenerationScheme = new HLGeneralDistribution();
		if (function.getType() == 0) { // See ProtosStatisticalFunction
			// this is not a proper translation from Nexp, mean = 0 and
			// intensity = infinity
			caseGenerationScheme.setIntensity(1 / function.getMean());
			caseGenerationScheme.getExponentialDistribution();
			caseGenerationScheme
					.setBestDistributionType(DistributionEnum.EXPONENTIAL_DISTRIBUTION);
			hlProcess.getGlobalInfo().setCaseGenerationScheme(
					caseGenerationScheme);
		} else if (function.getType() == 1) { // Beta implemented as
			// UniformDistribution
			caseGenerationScheme.getUniformDistribution();
			caseGenerationScheme.setMin(function.getMinimum());
			caseGenerationScheme.setMax(function.getMaximum());
			caseGenerationScheme
					.setBestDistributionType(DistributionEnum.UNIFORM_DISTRIBUTION);
			hlProcess.getGlobalInfo().setCaseGenerationScheme(
					caseGenerationScheme);
		} else if (function.getType() == 2) {
			caseGenerationScheme
					.getDistribution(DistributionEnum.NORMAL_DISTRIBUTION);
			caseGenerationScheme.setMean(function.getMean());
			caseGenerationScheme.setVariance(function.getVariance());
			caseGenerationScheme
					.setBestDistributionType(DistributionEnum.NORMAL_DISTRIBUTION);
			hlProcess.getGlobalInfo().setCaseGenerationScheme(
					caseGenerationScheme);
		} else if (function.getType() == 3) {
			caseGenerationScheme
					.getDistribution(DistributionEnum.ERLANG_DISTRIBUTION);
			caseGenerationScheme.setIntensity(1 / function.getMean());
			caseGenerationScheme.setEmergenceOfEvents((int) function
					.getNumber());
			caseGenerationScheme
					.setBestDistributionType(DistributionEnum.ERLANG_DISTRIBUTION);
			hlProcess.getGlobalInfo().setCaseGenerationScheme(
					caseGenerationScheme);
		} else if (function.getType() == 4) { // Gamma implemented as
			// NormalDistribution
			caseGenerationScheme
					.getDistribution(DistributionEnum.NORMAL_DISTRIBUTION);
			caseGenerationScheme.setMean(function.getMean());
			caseGenerationScheme.setVariance(function.getVariance());
			caseGenerationScheme
					.setBestDistributionType(DistributionEnum.NORMAL_DISTRIBUTION);
			hlProcess.getGlobalInfo().setCaseGenerationScheme(
					caseGenerationScheme);
		} else if (function.getType() == 5) {
			caseGenerationScheme.getUniformDistribution();
			caseGenerationScheme.setMin(function.getMinimum());
			caseGenerationScheme.setMax(function.getMaximum());
			caseGenerationScheme
					.setBestDistributionType(DistributionEnum.UNIFORM_DISTRIBUTION);
			hlProcess.getGlobalInfo().setCaseGenerationScheme(
					caseGenerationScheme);
		} else if (function.getType() == 6) {
			caseGenerationScheme
					.getDistribution(DistributionEnum.CONSTANT_DISTRIBUTION);
			caseGenerationScheme.setConstant(function.getMean());
			caseGenerationScheme
					.setBestDistributionType(DistributionEnum.CONSTANT_DISTRIBUTION);
			hlProcess.getGlobalInfo().setCaseGenerationScheme(
					caseGenerationScheme);
		}

		// Maps the Protos model structure, i.e. its activities and statusses
		// onto
		// the HLModel structure, i.e. HLActivities and HLChoices.
		for (ProtosSubprocess process : protosModel.getSubprocesses()) {
			if (process.isRoot() == true) {
				// TODO Anne (HLYAWL) and Mariska: check how to handle
				// hierarchical models. For now only handle the nodes from the
				// the root net
				hlProcess.getGlobalInfo().setName(process.getName());
				initActivities(process);
				initChoices(process);

				// Creates the HLRoles on the global level
				for (ProtosRole role : protosModel.getRoleGraph().getRoles()) {
					HLGroup hlGroup = new HLGroup(role.getName(), hlProcess,
							new HLID(role.getID()));
					hlProcess.addOrReplace(hlGroup);
					// Creates as many HLResources as there are entered as
					// available in the Protos model.
					for (int i = 1; i <= role.getAvailable(); i++) {
						HLResource hlRes = new HLResource(role.getName() + i,
								hlProcess);
						hlProcess.addOrReplace(hlRes);
						hlGroup.addResource(hlRes);
					}
				}

				// Maps a HLRole to each HLActivity based on the mapping in
				// Protos.
				for (ProtosFlowElement node : process.getActivities()) {
					HLActivity act = hlProcess
							.getActivity(vertexToHLActivityMapping.get(node));
					if (node.getRole() == null) { // the executorid of <<No
						// role>> is null.
						// The CPN export expects each task to have a role =>
						// add Nobody if no role in protos.
						act.setGroup(hlProcess.getNobodyGroupID());
					} else {
						for (HLGroup grp : hlProcess.getGroups()) {
							if (grp.getID().getName().equals(node.getRole())) { // HLID
								// has
								// id
								// and
								// name,
								// name
								// is
								// equal
								// to
								// the
								// roleID.
								act.setGroup(grp.getID());
							}
						}
					}
				}

				// Creates the HLAttributes on the global level.
				for (ProtosDataElement dataElement : process.getDataElements()) {
					if (dataElement.getType() == 0) {
						// 0 means scalar, which is the regular Protos
						// Data-element See@ProtosDataElement.
						// regardless the type given for the ProtosDataElement,
						// the HLAttribute is set to Nominal, i.e. a set of
						// strings.
						HLAttribute hlAtt = new HLNominalAttribute(dataElement
								.getName(), hlProcess, new HLID(dataElement
								.getID()));
						hlProcess.addOrReplace(hlAtt);
					}
				}

				// maps a set of input HLAttributes to each HLActivity based on
				// the mapping in Protos.
				for (ProtosFlowElement node : process.getActivities()) {
					HLActivity act = hlProcess
							.getActivity(vertexToHLActivityMapping.get(node));
					if (node.getInputDatas() != null) {
						for (ProtosActivityData data : node.getInputDatas()) {
							for (HLAttribute att : hlProcess.getAttributes()) {
								if (att.getID().getName().equals(
										data.getDataObjectID())) {
									act.addInputDataAttribute(att.getID());
								}
							}
						}
					}
				}

				// maps a set of output HLAttributes to each HLActivity based on
				// the mapping in Protos.
				// note that any ProtosActivityData that is not input data is
				// mapped on an output HLAttribute.
				for (ProtosFlowElement node : process.getActivities()) {
					HLActivity act = hlProcess
							.getActivity(vertexToHLActivityMapping.get(node));
					if (node.getOutputDatas() != null) {
						for (ProtosActivityData data : node.getDatas()) {
							for (HLAttribute att : hlProcess.getAttributes()) {
								if (att.getID().getName().equals(
										data.getDataObjectID())
										& !node.getInputDatas().contains(data)) {
									act.addOutputDataAttribute(att.getID());
								}
							}
						}
					}
				}

				// sets the duration (distribution) for each HLActivity based on
				// the
				// execution time (distribution) entered for the associated
				// Protos activity.
				for (ProtosFlowElement node : process.getActivities()) {
					ProtosStatisticalFunction duration = node.getDuration();
					HLGeneralDistribution execTime = new HLGeneralDistribution();
					HLActivity act = hlProcess
							.getActivity(vertexToHLActivityMapping.get(node));
					if (duration.getType() == 0) { // See
						// ProtosStatisticalFunction
						// this is not a proper translation from Nexp, mean = 0
						// and intensity = infinity
						execTime.setIntensity(1 / duration.getMean());
						execTime.getExponentialDistribution();
						execTime
								.setBestDistributionType(DistributionEnum.EXPONENTIAL_DISTRIBUTION);
						act.setExecutionTime(execTime);
					} else if (duration.getType() == 1) { // Beta implemented as
						// UniformDistribution
						execTime.getUniformDistribution();
						execTime.setMin(duration.getMinimum());
						execTime.setMax(duration.getMaximum());
						execTime
								.setBestDistributionType(DistributionEnum.UNIFORM_DISTRIBUTION);
						act.setExecutionTime(execTime);
					} else if (duration.getType() == 2) {
						execTime
								.getDistribution(DistributionEnum.NORMAL_DISTRIBUTION);
						execTime.setMean(duration.getMean());
						execTime.setVariance(duration.getVariance());
						execTime
								.setBestDistributionType(DistributionEnum.NORMAL_DISTRIBUTION);
						act.setExecutionTime(execTime);
					} else if (duration.getType() == 3) {
						execTime
								.getDistribution(DistributionEnum.ERLANG_DISTRIBUTION);
						execTime.setIntensity(1 / duration.getMean());
						execTime.setEmergenceOfEvents((int) duration
								.getNumber());
						execTime
								.setBestDistributionType(DistributionEnum.ERLANG_DISTRIBUTION);
						act.setExecutionTime(execTime);
					} else if (duration.getType() == 4) { // Gamma implemented
						// as
						// NormalDistribution
						execTime
								.getDistribution(DistributionEnum.NORMAL_DISTRIBUTION);
						execTime.setMean(duration.getMean());
						execTime.setVariance(duration.getVariance());
						execTime
								.setBestDistributionType(DistributionEnum.NORMAL_DISTRIBUTION);
						act.setExecutionTime(execTime);
					} else if (duration.getType() == 5) {
						execTime.getUniformDistribution();
						execTime.setMin(duration.getMinimum());
						execTime.setMax(duration.getMaximum());
						execTime
								.setBestDistributionType(DistributionEnum.UNIFORM_DISTRIBUTION);
						act.setExecutionTime(execTime);
					} else if (duration.getType() == 6) {
						execTime
								.getDistribution(DistributionEnum.CONSTANT_DISTRIBUTION);
						execTime.setConstant(duration.getMean());
						execTime
								.setBestDistributionType(DistributionEnum.CONSTANT_DISTRIBUTION);
						act.setExecutionTime(execTime);
					}
				}

				// sets the waiting time (no distribution) for each HLActivity
				// based on the
				// elapse entered for the trigger associated with the Protos
				// activity.
				// NB. Protos sets this value in seconds!
				for (ProtosFlowElement node : process.getFlowElements()) {
					if (node.isTrigger()) {
						int elapse = node.getWaitingTime();
						for (Edge edge : node.getOutEdges()) {
							ModelGraphVertex outNode = (ModelGraphVertex) edge
									.getHead();
							HLActivity act = hlProcess
									.getActivity(vertexToHLActivityMapping
											.get(outNode));
							HLGeneralDistribution distr = new HLGeneralDistribution();
							distr
									.getDistribution(DistributionEnum.CONSTANT_DISTRIBUTION);
							distr.setConstant(elapse);
							distr
									.setBestDistributionType(DistributionEnum.CONSTANT_DISTRIBUTION);
							act.setWaitingTime(distr);

						}
					}
				}

				// map the Protos routing information on HLConditions, arc
				// condition = dataExpression,
				// activity frequency (0..1) = frequency (1..+100), arc
				// frequency (0..1) = probability (0..1).
				// NB. In Protos, the priority has a distribution and can not be
				// mapped on any of the HLConditions.
				for (ProtosFlowElement node : process.getFlowElements()) {
					// see if node has related HLChoice
					HLChoice choice = hlProcess
							.getChoice(vertexToHLChoiceMapping.get(node));
					// add target to choice
					if (choice != null) {
						for (HLActivity targetAct : choice.getChoiceTargets()) {
							HLCondition cond = choice.addChoiceTarget(targetAct
									.getID());
							// set the frequency, this is derived from a
							// ProtosActivity
							for (ProtosFlowElement target : process
									.getFlowElements()) {
								if (targetAct.getName().equalsIgnoreCase(
										target.getName())) {
									// targets have a frequency (default 1) that
									// is mapped on
									// the condition frequency (default 100).
									double nodeFrequency = target
											.getFrequency() * 100;
									if (nodeFrequency != 0) {
										cond.setFrequency((int) nodeFrequency);
									}
								}
							}
							// set the probabilities and data expressions, these
							// are derived from a ProtosProcessArc.
							for (Object object : process.getArcs()) {
								if (object instanceof ProtosProcessArc) {
									// first, find a relevant arc
									ProtosProcessArc arc = (ProtosProcessArc) object;
									// the choice node is the source of the arc,
									// i.e., the fromNode
									ProtosFlowElement fromNode = (ProtosFlowElement) arc
											.getEdge().getSource();
									ProtosFlowElement toNode = (ProtosFlowElement) arc
											.getEdge().getDest();
									// when the fromNode is a status or an
									// activity and the toNode is an activity,
									// the toNode has to be a target
									if (fromNode == node
											&& targetAct.getName()
													.equalsIgnoreCase(
															toNode.getName())) {
										// arc is outgoing arc of choice node.
										// set the probability
										double arcFrequency = arc
												.getSimulationFrequency();
										cond.setProbability(arcFrequency);

										// set the data expression
										if (arc.hasCondition()) {
											ProtosExpression arcCondition = arc
													.getCondition();
											// if the protos expression has an
											// operator and two sub expressions,
											// i.e., type == 8,
											// it may be a data expression (this
											// is checked #getOperands)
											if (arcCondition.getType() == 8) {
												// get the operator and operands
												// from Protos.
												String protosOperator = arcCondition
														.getOperator();
												HLExpressionOperator operator = getOperator(
														protosOperator,
														arcCondition, arc,
														process);
												cond
														.setExpression(new HLDataExpression(
																operator));
											}
										}
									}
									// when the toNode is a place, its
									// successor(s) is the target(s)
									if (fromNode == node && toNode.isStatus()) {
										// arc is outgoing arc of choice node.
										// find the successor that is the target
										for (Object successor : toNode
												.getSuccessors()) {
											ProtosFlowElement target = (ProtosFlowElement) successor;
											if (targetAct.getName()
													.equalsIgnoreCase(
															target.getName())) {
												// set the probability
												double arcFrequency = arc
														.getSimulationFrequency();
												cond
														.setProbability(arcFrequency);

												// set the data expression
												if (arc.hasCondition()) {
													ProtosExpression arcCondition = arc
															.getCondition();
													// if the protos expression
													// has an operator and two
													// sub expressions, i.e.,
													// type == 8,
													// it may be a data
													// expression (this is
													// checked #getOperands)
													if (arcCondition.getType() == 8) {
														// get the operator and
														// operands from Protos.
														String protosOperator = arcCondition
																.getOperator();
														HLExpressionOperator operator = getOperator(
																protosOperator,
																arcCondition,
																arc, process);
														cond
																.setExpression(new HLDataExpression(
																		operator));
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}

			}
		}
	}

	/*
	 * Derives the expression operands from Protos and add them to the operator
	 * expression node.
	 * 
	 * @param operator the operator to which the operands should be added
	 * 
	 * @ param arcCondition the protos condition from which the operants are
	 * obtained
	 * 
	 * @param arc the protos arc that holds the condition, used for error
	 * message
	 * 
	 * @param process the protos subprocess for accessing the data information
	 */
	private HLExpressionOperator getOperator(String protosOperator,
			ProtosExpression arcCondition, ProtosProcessArc arc,
			ProtosSubprocess process) {
		HLExpressionOperator operator = null;
		if (protosOperator.equalsIgnoreCase("Greater")) {
			operator = new HLGreaterOperator();
			getOperands(operator, arcCondition, arc, process);
		} else if (protosOperator.equalsIgnoreCase("Less")) {
			operator = new HLSmallerOperator();
			getOperands(operator, arcCondition, arc, process);
		} else if (protosOperator.equalsIgnoreCase("LessOrEqual")) {
			operator = new HLSmallerEqualOperator();
			getOperands(operator, arcCondition, arc, process);
		} else if (protosOperator.equalsIgnoreCase("GreaterOrEqual")) {
			operator = new HLGreaterEqualOperator();
			getOperands(operator, arcCondition, arc, process);
		} else if (protosOperator.equalsIgnoreCase("Equal")) {
			operator = new HLEqualOperator();
			getOperands(operator, arcCondition, arc, process);
		}
		return operator;
	}

	/*
	 * Derives the expression operands from Protos and add them to the operator
	 * expression node.
	 * 
	 * @param operator the operator to which the operands should be added
	 * 
	 * @ param arcCondition the protos condition from which the operants are
	 * obtained
	 * 
	 * @param arc the protos arc that holds the condition, used for error
	 * message
	 * 
	 * @param process the protos subprocess for accessing the data information
	 */
	private void getOperands(HLExpressionOperator operator,
			ProtosExpression arcCondition, ProtosProcessArc arc,
			ProtosSubprocess process) {
		ProtosExpression expr1 = arcCondition.getSubExpression1();
		ProtosExpression expr2 = arcCondition.getSubExpression2();
		// one of the expressions is a data expression
		if (expr1.getType() == 5 || expr2.getType() == 5) {
			// go through first protos expression
			// ProtosExpression is boolean
			if (expr1.getType() == 0) {
				boolean item = expr1.getBooleanConstant();
				HLAttributeValue value = new HLBooleanValue(item);
				HLValueOperand valueOperand = new HLValueOperand(value);
				operator.addSubElement(valueOperand);
			}
			// ProtosExpression is integer
			if (expr1.getType() == 1) {
				int item = expr1.getIntegerConstant();
				HLAttributeValue value = new HLNumericValue(item);
				HLValueOperand valueOperand = new HLValueOperand(value);
				operator.addSubElement(valueOperand);
			}
			// ProtosExpression is float
			if (expr1.getType() == 2) {
				float item = expr1.getFloatConstant();
				HLAttributeValue value = new HLNumericValue((int) item);
				HLValueOperand valueOperand = new HLValueOperand(value);
				operator.addSubElement(valueOperand);
			}
			// ProtosExpression is string or time
			if (expr1.getType() == 3 || expr1.getType() == 4) {
				String item = expr1.getStringConstant();
				HLAttributeValue value = new HLNominalValue(item);
				HLValueOperand valueOperand = new HLValueOperand(value);
				operator.addSubElement(valueOperand);
			}
			// ProtosExpression is DataExpression
			if (expr1.getType() == 5) {
				String dataExpression = expr1.getStringConstant(); // is id of
				// data
				// element
				for (ProtosDataElement protosData : process.getDataElements()) {
					if (protosData.getID().equalsIgnoreCase(dataExpression)) {
						HLAttribute attr = hlProcess
								.findAttributeByName(protosData.getName());
						HLAttributeOperand attrOperand = new HLAttributeOperand(
								attr.getID(), hlProcess);
						operator.addSubElement(attrOperand);
					}
				}
			}

			// go through second protos expression.
			// ProtosExpression is boolean
			if (expr2.getType() == 0) {
				boolean item = expr2.getBooleanConstant();
				HLAttributeValue value = new HLBooleanValue(item);
				HLValueOperand valueOperand = new HLValueOperand(value);
				operator.addSubElement(valueOperand);
			}
			// ProtosExpression is integer
			if (expr2.getType() == 1) {
				int item = expr2.getIntegerConstant();
				HLAttributeValue value = new HLNumericValue(item);
				HLValueOperand valueOperand = new HLValueOperand(value);
				operator.addSubElement(valueOperand);
			}
			// ProtosExpression is float
			if (expr2.getType() == 2) {
				float item = expr2.getFloatConstant();
				HLAttributeValue value = new HLNumericValue((int) item);
				HLValueOperand valueOperand = new HLValueOperand(value);
				operator.addSubElement(valueOperand);
			}
			// ProtosExpression is string or time
			if (expr2.getType() == 3 || expr2.getType() == 4) {
				String item = expr2.getStringConstant();
				HLAttributeValue value = new HLNominalValue(item);
				HLValueOperand valueOperand = new HLValueOperand(value);
				operator.addSubElement(valueOperand);
			}
			// ProtosExpression is DataExpression
			if (expr2.getType() == 5) {
				String dataExpression = expr2.getStringConstant(); // is id of
				// data
				// element
				for (ProtosDataElement protosData : process.getDataElements()) {
					if (protosData.getID().equalsIgnoreCase(dataExpression)) {
						HLAttribute attr = hlProcess
								.findAttributeByName(protosData.getName());
						HLAttributeOperand attrOperand = new HLAttributeOperand(
								attr.getID(), hlProcess);
						operator.addSubElement(attrOperand);
					}
				}
			}

		} else {
			Message.add("The condition assigned to ProtosProcessArc "
					+ arc.getLabel() + " is not a data expression.",
					Message.ERROR);

		}
	}

	/*
	 * Maps each ProtosFlowElements.isActivity to a high-level activity.
	 */
	private void initActivities(ProtosSubprocess process) {
		for (ProtosFlowElement node : process.getActivities()) {
			// every activity gets a high-level activity
			if (node.isActivity()) {
				HLActivity hlActivity = new HLActivity(node.getName(),
						hlProcess);
				vertexToHLActivityMapping.put(node, hlActivity.getID());
			}
		}
	}

	/*
	 * Maps ProtosFlowElements.isStatus with more than one outgoing arc and XOR
	 * split tasks onto high-level choices.
	 */
	private void initChoices(ProtosSubprocess process) {
		// nodes need to be in place when creating the choices as they are
		// condition targets
		for (ProtosFlowElement node : process.getFlowElements()) {
			// only handle XOR splits for now as not clear how OR joins would
			// work in the simulation part
			if ((node.isActivity() && ((ProtosFlowElement) node).getSplitType() == YAWLTask.XOR)
					|| (node.isStatus() && node.outDegree() > 1)) {
				HLChoice hlChoice = new HLChoice(node.getName(), hlProcess);
				// get condition targets
				for (Edge edge : node.getOutEdges()) {
					ModelGraphVertex outNode = (ModelGraphVertex) edge
							.getHead();
					HLActivity act = null;
					if (((ProtosFlowElement) process.getFlowElement(outNode))
							.isActivity()) {
						act = findActivity(outNode);
						if (act != null) {
							hlChoice.addChoiceTarget(act.getID());
						}
					}
					// in case of a status being the target, add all following
					// tasks as condition targets
					else if (((ProtosFlowElement) process
							.getFlowElement(outNode)).isStatus()) {
						if (outNode.getOutEdges() != null) {
							for (Edge outCond : outNode.getOutEdges()) {
								ModelGraphVertex outTask = (ModelGraphVertex) outCond
										.getHead();
								act = findActivity(outTask);
								if (act != null) {
									hlChoice.addChoiceTarget(act.getID());
								}
							}
						}
					}
				}
				// add choice
				vertexToHLChoiceMapping.put(node, hlChoice.getID());
			}
		}
	}

	/**
	 * Get the underlying (low-level) Protos model.
	 * 
	 * @return the actual Protos model of this high-level Protos model
	 */
	public ProtosModel getProtosModel() {
		return (ProtosModel) model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.hlmodel.HLModel#getGraphNodes
	 * ()
	 */
	public List<ModelGraphVertex> getGraphNodes() {
		ArrayList<ModelGraphVertex> returnNodes = new ArrayList<ModelGraphVertex>();
		for (ProtosSubprocess process : ((ProtosModel) model).getSubprocesses()) {
			// check only for the nodes of the root net
			if (process.isRoot() == true) {
				for (ProtosFlowElement node : process.getFlowElements()) {
					returnNodes.add(node);
					// returnNodes.addAll(process.getFlowElements());
				}
			}
		}
		return returnNodes;
	}

	public String toString() {
		return "Protos model: " + hlProcess.getGlobalInfo().getName();
	}

}
