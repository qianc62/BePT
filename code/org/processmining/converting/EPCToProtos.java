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

package org.processmining.converting;

import org.processmining.mining.protosmining.ProtosResult;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.models.protos.ProtosModel;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.mining.petrinetmining.PetriNetResult;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.WFNet;
import org.processmining.framework.models.yawl.YAWLModel;
import org.processmining.framework.models.yawl.YAWLDecomposition;
import org.processmining.framework.models.yawl.YAWLNode;
import org.processmining.framework.models.yawl.YAWLCondition;
import org.processmining.framework.models.yawl.YAWLTask;
import org.processmining.framework.models.protos.ProtosSubprocess;
import org.processmining.framework.models.protos.ProtosFlowElement;
import java.util.HashMap;
import java.util.HashSet;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title: EPCToProtos
 * </p>
 * 
 * <p>
 * Description: Converts an EPC into a Protos model
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class EPCToProtos implements ConvertingPlugin {
	public EPCToProtos() {
	}

	public String getName() {
		return "EPC to Protos";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:epc2protos";
	}

	public ProtosResult convert(ProvidedObject object) {
		ConfigurableEPC provided = null;
		LogReader log = null;

		for (int i = 0; provided == null && i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof ConfigurableEPC) {
				provided = (ConfigurableEPC) object.getObjects()[i];
			}
			if (object.getObjects()[i] instanceof EPC) {
				provided = (ConfigurableEPC) object.getObjects()[i];
			}
			if (object.getObjects()[i] instanceof LogReader) {
				log = (LogReader) object.getObjects()[i];
			}
		}

		if (provided == null) {
			return null;
		}

		ProtosModel protos = result(provided);
		return new ProtosResult(log, protos);
	}

	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof EPC) {
				return true;
			}
			if (object.getObjects()[i] instanceof ConfigurableEPC) {
				return true;
			}
		}
		return false;
	}

	public ProtosModel result(ConfigurableEPC epc) {
		ProtosModel model = new ProtosModel("Model");
		EPCToPetriNetConverterPlugin epc2pn = new EPCToPetriNetConverterPlugin();
		PetriNetToWFNet pn2wfn = new PetriNetToWFNet();
		WFNetToYAWL wfn2yawl = new WFNetToYAWL();
		ProvidedObject epcObject = new ProvidedObject("EPC",
				new Object[] { epc });
		PetriNetResult pnr = (PetriNetResult) epc2pn.convert(epcObject);
		PetriNet pn = pnr.getPetriNet();
		WFNet wfn = pn2wfn.convert(pn);
		YAWLModel yawl = wfn2yawl.convert(wfn);
		HashMap<YAWLNode, ProtosFlowElement> map = new HashMap<YAWLNode, ProtosFlowElement>();
		YAWLCondition inputCondition = null, outputCondition = null;

		// Now, we need to convert the YAWL model into a Protos model.
		HashSet<YAWLDecomposition> decompositions = new HashSet<YAWLDecomposition>(
				yawl.getDecompositions());
		for (YAWLDecomposition decomposition : decompositions) {
			if (decomposition.isRoot()) {
				int nofStatuses = 0, nofActivities = 0, nofArcs = 0, nofStartActivities = 0, nofEndActivities = 0;
				ProtosSubprocess subprocess = model.addSubprocess("Subprocess");
				for (YAWLNode node : decomposition.getNodes()) {
					if (node instanceof YAWLCondition) {
						YAWLCondition condition = (YAWLCondition) node;
						if (condition.getPredecessors().isEmpty()) {
							// Input condition.
							inputCondition = condition;
						} else if (condition.getSuccessors().isEmpty()) {
							// Output condition
							outputCondition = condition;
						} else {
							int i = condition.getIdentifier().indexOf("\\n");
							subprocess.addStatus(condition.getName(),
									i == -1 ? condition.getIdentifier()
											: condition.getIdentifier()
													.substring(0, i));
							nofStatuses++;
						}
					} else if (node instanceof YAWLTask) {
						YAWLTask task = (YAWLTask) node;
						int i = task.getIdentifier().indexOf("\\n");
						ProtosFlowElement activity = subprocess.addActivity(
								task.getName(), i == -1 ? task.getIdentifier()
										: task.getIdentifier().substring(0, i));
						activity.setJoinType(task.getJoinType());
						activity.setSplitType(task.getSplitType());
						nofActivities++;
					}
				}
				for (YAWLNode node : decomposition.getNodes()) {
					if (node instanceof YAWLTask) {
						HashSet<YAWLNode> nodes = new HashSet<YAWLNode>(node
								.getPredecessors());
						for (YAWLNode predNode : nodes) {
							if (predNode == inputCondition) {
								subprocess.setStartEndActivity(node.getName(),
										true);
								nofStartActivities++;
							} else {
								subprocess.addArc(predNode.getName(), node
										.getName());
								nofArcs++;
							}
						}
					} else if (node instanceof YAWLCondition) {
						HashSet<YAWLNode> nodes = new HashSet<YAWLNode>(node
								.getPredecessors());
						for (YAWLNode predNode : nodes) {
							if (node == outputCondition) {
								subprocess.setStartEndActivity(predNode
										.getName(), false);
								nofEndActivities++;
							} else {
								subprocess.addArc(predNode.getName(), node
										.getName());
								nofArcs++;
							}
						}
					}
				}
				Message.add("<EPCToProtos nofStatuses=\"" + nofStatuses
						+ "\" nofActivities=\"" + nofActivities
						+ "\" nofArcs=\"" + nofArcs
						+ "\" nofStartActivities=\"" + nofStartActivities
						+ "\" nofEndActivities=\"" + nofEndActivities + "\"/>",
						Message.TEST);
			}
		}
		return model;
	}
}
