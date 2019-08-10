package org.processmining.converting;

/*
 * Author: Mariska Netjes
 * (c) 2008 Technische Universiteit Eindhoven and STW
 */

import java.util.HashMap;
import java.util.Iterator;

import org.processmining.analysis.edithlprocess.EditHighLevelProcessGui;
import org.processmining.converting.protos.ProtosToPetriNet;
import org.processmining.converting.protos.ProtosToProtos;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.yawl.YAWLTask;
import org.processmining.framework.models.hlprocess.*;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.hlprocess.hlmodel.HLProtos;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.protos.ProtosFlowElement;
import org.processmining.framework.models.protos.ProtosModel;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;

import att.grappa.Edge;
import org.processmining.framework.models.hlprocess.expr.HLDataExpression;

/**
 * This class converts a high-level Protos model (i.e., the Protos control flow
 * plus so-called high-level information about data, resources, link conditions
 * etc.) into a high-level Petri net model. <br>
 * This means that the control-flow structure is converted while the high-level
 * information is preserved.
 * 
 * @see HLYAWLToHLPetriNet
 */
public class HLProtosToHLPetriNet implements ConvertingPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.converting.ConvertingPlugin#accepts(org.processmining
	 * .framework.plugin.ProvidedObject)
	 */
	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof HLProtos) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.converting.ConvertingPlugin#convert(org.processmining
	 * .framework.plugin.ProvidedObject)
	 */
	public MiningResult convert(ProvidedObject object) {
		HLProtos providedProtos = null;
		LogReader log = null;

		for (int i = 0; i < object.getObjects().length; i++) {
			if (providedProtos == null
					&& object.getObjects()[i] instanceof HLProtos) {
				providedProtos = (HLProtos) object.getObjects()[i];
			}
		}

		if (providedProtos == null) {
			return null;
		}

		HLPetriNet pn = convert(providedProtos);
		return new EditHighLevelProcessGui(pn);
	}

	/**
	 * Provides user documentation for the plugin
	 * 
	 * @return a URL that will be opened in the default browser of the user
	 */
	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/online/hlprotostohlpn";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "HLProtos to HLPetriNet";
	}

	/**
	 * Actually converts the given HLProtos model into an HLPetriNet.
	 * 
	 * @param hlProtos
	 *            the given input model
	 * @return the converted model
	 */
	protected HLPetriNet convert(HLProtos hlProtos) {
		// first convert the low-level Protos model into a Petri net
		ProtosModel protosModel = hlProtos.getProtosModel();
		HashMap map0 = new HashMap();
		HashMap map1 = new HashMap();
		ProtosModel model1 = ProtosToProtos.addImplicitConditions(protosModel,
				map0);
		PetriNet petriNetResult = ProtosToPetriNet.convert(model1, map1);

		// project the mapping from protos activities to pn transitions
		HashMap<ProtosFlowElement, Transition> protosPnActivityMapping = new HashMap<ProtosFlowElement, Transition>();
		HashMap<ProtosFlowElement, Place> protosPnChoiceMapping = new HashMap<ProtosFlowElement, Place>();
		for (ModelGraphVertex actVert : hlProtos.getActivityNodes()) {
			ProtosFlowElement mappedVert = (ProtosFlowElement) map0
					.get(protosModel.getRootSubprocess()
							.getFlowElement(actVert));
			if (mappedVert.isActivity()) {
				Object pnVert = (Object) map1.get(mappedVert);
				if (pnVert instanceof Transition
						&& ((Transition) pnVert).isInvisibleTask() == false) {
					protosPnActivityMapping.put((ProtosFlowElement) mappedVert,
							(Transition) pnVert); // XOR-split wordt hier niet
					// gemapt, want pnVert is
					// plaats!!

					if (mappedVert.getSplitType() == YAWLTask.XOR) { // &&
						// pnVert
						// instanceof
						// Place)
						// {
						// protosPnChoiceMapping.put(mappedVert, (Place)
						// pnVert);

						// look for the place that corresponds to the choice
						// node for this explicit split task
						// this is the place that follows the XOR-split
						assert (((Transition) pnVert).outDegree() == 1); // added
						Place splitPlace = (Place) ((Transition) pnVert)
								.getOutEdges().get(0).getHead(); // added
						protosPnChoiceMapping.put(mappedVert,
								(Place) splitPlace); // added
					}
				}
			}
		}
		for (ModelGraphVertex actVert : hlProtos.getChoiceNodes()) {
			ProtosFlowElement mappedVert = (ProtosFlowElement) map0
					.get(protosModel.getRootSubprocess()
							.getFlowElement(actVert));
			// map deferred choice construct, which are directly mapped onto PN
			// places
			if (mappedVert.isStatus()) {
				Place pnPlace = (Place) map1.get(mappedVert);
				protosPnChoiceMapping.put((ProtosFlowElement) mappedVert,
						(Place) pnPlace);
			}
		}

		HLPetriNet hlPetriNet = makeHLPetriNet(hlProtos, petriNetResult,
				protosPnActivityMapping, protosPnChoiceMapping);
		copyHighLevelInformation(hlProtos, hlPetriNet);

		return hlPetriNet;
	}

	/**
	 * Create high-level Petri net and re-link activities and choices based on
	 * the mapping.
	 * 
	 * @param hlProtos
	 *            the source HLProtos model
	 * @param petriNetResult
	 *            the converted low-level Petri net model
	 * @param protosPnActivityMapping
	 *            the mapping from activity nodes to PN transitions
	 * @param protosPnChoiceMapping
	 *            the mapping from choice nodes to PN places
	 * @return the created target HLPetriNet model
	 * @see HLYAWLToHLPetriNet
	 */
	private HLPetriNet makeHLPetriNet(HLProtos hlProtos,
			PetriNet petriNetResult,
			HashMap<ProtosFlowElement, Transition> protosPnActivityMapping,
			HashMap<ProtosFlowElement, Place> protosPnChoiceMapping) {
		HLPetriNet hlPetriNet = new HLPetriNet(petriNetResult);
		// re-establish the activity mapping
		for (ModelGraphVertex graphNodeProtos : hlProtos.getActivityNodes()) {
			for (ModelGraphVertex graphNodePN : hlPetriNet.getGraphNodes()) {
				ProtosFlowElement protosNode = hlProtos.getProtosModel()
						.getRootSubprocess().getFlowElement(graphNodeProtos);
				if (protosNode.isActivity()
						&& protosPnActivityMapping.get(protosNode) == graphNodePN) {
					HLActivity clonedAct = (HLActivity) hlProtos.findActivity(
							graphNodeProtos).clone();
					hlPetriNet.setActivity(graphNodePN, clonedAct);
					break;
				}
			}
		}

		// re-establisch the choices
		for (ModelGraphVertex graphNodeProtos : hlProtos.getChoiceNodes()) {
			HLChoice oldChoice = (HLChoice) hlProtos
					.findChoice(graphNodeProtos);
			for (ModelGraphVertex graphNodePN : hlPetriNet.getGraphNodes()) {
				ProtosFlowElement protosNode = hlProtos.getProtosModel()
						.getRootSubprocess().getFlowElement(graphNodeProtos);
				// deferred choice is mapped one on one (place on place).
				if (protosNode.getName().equalsIgnoreCase(
						graphNodePN.getIdentifier())
						&& protosNode.isStatus()
						&& graphNodeProtos.outDegree() > 1) {
					HLChoice newChoice = new HLChoice(graphNodePN
							.getIdentifier(), hlPetriNet.getHLProcess());
					for (Edge edge : graphNodePN.getOutEdges()) {
						Transition trans = (Transition) edge.getHead();
						HLActivity target = hlPetriNet.findActivity(trans);
						// set choice target
						HLCondition newCondition = newChoice
								.addChoiceTarget(target.getID());
						HLCondition oldCondition = oldChoice
								.getCondition(target.getID());
						HLDataExpression expression = oldCondition
								.getExpression();
						if (expression != null) {
							newCondition.setExpression(expression);
						}
						int frequency = oldCondition.getFrequency();
						newCondition.setFrequency(frequency);
						double prob = oldCondition.getProbability();
						newCondition.setProbability(prob);
					}
					hlPetriNet.setChoice(graphNodePN, newChoice); // also
					// replaces
					// the
					// choice in
					// hlprocess
					break;
				}
				// explicit choice (activity with XOR-split) is mapped on a
				// place.
				// a new choice and new choice targets are added.
				if (protosNode.isActivity()
						&& protosNode.getSplitType() == YAWLTask.XOR
						&& protosPnChoiceMapping.get(protosNode) == graphNodePN) {
					Place splitPlace = (Place) graphNodePN;
					HLChoice newChoice = new HLChoice(splitPlace
							.getIdentifier(), hlPetriNet.getHLProcess());
					for (Edge edge : splitPlace.getOutEdges()) {
						Transition trans = (Transition) edge.getHead();
						HLActivity newTarget = hlPetriNet.findActivity(trans);
						if (newTarget == null) {
							Message.add(
									"Error during adaptation of transition targets. Target "
											+ trans.getIdentifier()
											+ " not found", Message.ERROR);
							break;
						}
						// set choice target
						HLCondition newCondition = newChoice
								.addChoiceTarget(newTarget.getID());
						// the new targets should only have one output place
						assert (trans.outDegree() == 1);
						Place outPlace = (Place) trans.getOutEdges().get(0)
								.getHead();
						// the condition of the old targets is set for the new
						// targets.
						for (Edge targEdge : outPlace.getOutEdges()) {
							Transition oldTarget = (Transition) targEdge
									.getHead();
							HLActivity oldAct = hlPetriNet
									.findActivity(oldTarget);
							if (oldAct != null) {
								HLCondition oldCondition = oldChoice
										.getCondition(oldAct.getID());
								if (oldCondition.getExpression() != null) {
									HLDataExpression expression = oldCondition
											.getExpression();
									newCondition.setExpression(expression);
								}
								int frequency = oldCondition.getFrequency();
								newCondition.setFrequency(frequency);
								double prob = oldCondition.getProbability();
								newCondition.setProbability(prob);
							} else {
								Message.add(
										"Error during adaptation of transition targets. Target "
												+ oldTarget.getIdentifier()
												+ " not found", Message.ERROR);
							}
						}
					}

					hlPetriNet.setChoice(graphNodePN, newChoice);
					break;
				}
			}
		}
		return hlPetriNet;
	}

	/**
	 * Copies the high-level information from the HLProtos model to the target
	 * HLPetriNet.
	 * 
	 * @param hlProtos
	 *            the source of the high-level information to be copied
	 * @param hlPetriNet
	 *            the target of the high-level information to be copied
	 */
	private void copyHighLevelInformation(HLProtos hlProtos,
			HLPetriNet hlPetriNet) {
		hlPetriNet.getHLProcess().getGlobalInfo().setName(
				hlProtos.getHLProcess().getGlobalInfo().getName());
		hlPetriNet.getHLProcess().getGlobalInfo().setTimeUnit(
				hlProtos.getHLProcess().getGlobalInfo().getTimeUnit());
		hlPetriNet.getHLProcess().getGlobalInfo().setCaseGenerationScheme(
				hlProtos.getHLProcess().getGlobalInfo()
						.getCaseGenerationScheme());
		Iterator<HLAttribute> attributes = hlProtos.getHLProcess()
				.getAttributes().iterator();
		while (attributes.hasNext()) {
			HLAttribute att = attributes.next();
			HLAttribute clonedAtt = (HLAttribute) att.clone();
			hlPetriNet.getHLProcess().addOrReplace(clonedAtt);
			// also add input attribute to activities
			Iterator<HLActivity> actWithInputIt = hlProtos.getHLProcess()
					.getActivitiesForInputAttribute(att.getID()).iterator();
			while (actWithInputIt.hasNext()) {
				HLActivity protosAct = actWithInputIt.next();
				HLActivity pnAct = hlPetriNet.getHLProcess().getActivity(
						protosAct.getID());
				if (pnAct != null) {
					pnAct.addInputDataAttribute(att.getID());
				} else {
					Message
							.add(
									"Failure while reconnecting high-level information: attribute "
											+ att.getName()
											+ " could not be assigned as input to activity "
											+ protosAct.getName()
											+ ". Activity ID in Protos model: "
											+ protosAct.getID().toString()
											+ ".", Message.ERROR);
				}
			}
			// also add output attribute to activities
			Iterator<HLActivity> actWithOutputIt = hlProtos.getHLProcess()
					.getActivitiesForOutputAttribute(att.getID()).iterator();
			while (actWithOutputIt.hasNext()) {
				HLActivity protosAct = actWithOutputIt.next();
				HLActivity pnAct = hlPetriNet.getHLProcess().getActivity(
						protosAct.getID());
				if (pnAct != null) {
					pnAct.addOutputDataAttribute(att.getID());
				} else {
					Message
							.add(
									"Failure while reconnecting high-level information: attribute "
											+ att.getName()
											+ " could not be assigned as output to activity "
											+ protosAct.getName()
											+ ". Activity ID in Protos model: "
											+ protosAct.getID().toString()
											+ ".", Message.ERROR);
				}
			}
		}
		Iterator<HLResource> resources = hlProtos.getHLProcess().getResources()
				.iterator();
		while (resources.hasNext()) {
			HLResource res = resources.next();
			HLResource clonedRes = (HLResource) res.clone();
			hlPetriNet.getHLProcess().addOrReplace(clonedRes);
		}
		// first, remove the automatically created nobody and anybody groups in
		// the target hlModel
		hlPetriNet.getHLProcess().removeGroupWithoutAffectingAssigmnets(
				hlPetriNet.getHLProcess().getNobodyGroupID());
		hlPetriNet.getHLProcess().removeGroupWithoutAffectingAssigmnets(
				hlPetriNet.getHLProcess().getAnybodyGroupID());
		// then, update nobody and anybody ID as activities will still point to
		// nobody and anybody IDs
		// from the source hlModel (cloning of IDs is not needed)
		hlPetriNet.getHLProcess().setNobodyGroupID(
				hlProtos.getHLProcess().getNobodyGroupID());
		hlPetriNet.getHLProcess().setAnybodyGroupID(
				hlProtos.getHLProcess().getAnybodyGroupID());
		// now, clone the real group objects (including nobody and anybody of
		// source hlModel)
		Iterator<HLGroup> groups = hlProtos.getHLProcess().getAllGroups()
				.iterator();
		while (groups.hasNext()) {
			HLGroup grp = groups.next();
			HLGroup clonedGrp = (HLGroup) grp.clone();
			hlPetriNet.getHLProcess().addOrReplace(clonedGrp);
		}
		Iterator<HLTypes.Perspective> perspectives = hlProtos.getHLProcess()
				.getGlobalInfo().getPerspectives().iterator();
		while (perspectives.hasNext()) {
			HLTypes.Perspective per = perspectives.next();
			hlPetriNet.getHLProcess().getGlobalInfo().addPerspective(per);
		}
	}
}
