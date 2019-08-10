package org.processmining.converting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.analysis.edithlprocess.EditHighLevelProcessGui;
import org.processmining.converting.yawl2pn.YawlToPetriNet;
import org.processmining.converting.yawl2pn.YawlToPetriNetSettings;
import org.processmining.converting.yawl2yawl.YawlToYawl;
import org.processmining.converting.yawl2yawl.YawlToYawlSettings;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.hlprocess.hlmodel.HLYAWL;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.yawl.YAWLCondition;
import org.processmining.framework.models.yawl.YAWLModel;
import org.processmining.framework.models.yawl.YAWLNode;
import org.processmining.framework.models.yawl.YAWLTask;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;

import att.grappa.Edge;

/**
 * This class converts a high-level YAWL model (i.e., a YAWL graph plus
 * so-called high-level information about data, resources, link conditions etc.)
 * into a high-level Petri net model. <br>
 * This means that the control-flow structure is converted while the high-level
 * information is preserved.
 * 
 * @author Anne Rozinat
 */
public class HLYAWLToHLPetriNet implements ConvertingPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.converting.ConvertingPlugin#accepts(org.processmining
	 * .framework.plugin.ProvidedObject)
	 */
	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof HLYAWL) {
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
		HLYAWL providedYAWL = null;
		LogReader log = null;

		for (int i = 0; i < object.getObjects().length; i++) {
			if (providedYAWL == null
					&& object.getObjects()[i] instanceof HLYAWL) {
				providedYAWL = (HLYAWL) object.getObjects()[i];
			}
		}

		if (providedYAWL == null) {
			return null;
		}

		HLPetriNet pn = convert(providedYAWL);
		return new EditHighLevelProcessGui(pn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/hlyawltohlpn";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "HLYAWL to HLPetriNet";
	}

	/**
	 * Actually converts the given HLYAWL model into an HLPetriNet.
	 * 
	 * @param hlYAWL
	 *            the given input model
	 * @return the converted model
	 */
	protected HLPetriNet convert(HLYAWL hlYAWL) {
		// first convert the low-level YAWL model into a Petri net
		YAWLModel yawlModel = hlYAWL.getYAWLModel();
		HashMap map0 = new HashMap();
		HashMap map1 = new HashMap();
		YawlToYawlSettings settings0 = new YawlToYawlSettings();
		YawlToPetriNetSettings settings1 = new YawlToPetriNetSettings();
		YAWLModel model1 = YawlToYawl.addImplicitConditions(yawlModel,
				settings0, map0);
		PetriNet petriNetResult = YawlToPetriNet.convert(model1, settings1,
				map1);

		// project the mapping from yawl nodes to pn nodes
		HashMap<YAWLTask, Transition> yawlPnActivityMapping = new HashMap<YAWLTask, Transition>();
		HashMap<YAWLNode, Place> yawlPnChoiceMapping = new HashMap<YAWLNode, Place>();
		for (ModelGraphVertex actVert : hlYAWL.getActivityNodes()) {
			YAWLNode mappedVert = (YAWLNode) map0.get(actVert);
			if (mappedVert instanceof YAWLTask) {
				for (Object pnVert : (ArrayList) map1.get(mappedVert)) {
					if (pnVert instanceof Transition
							&& ((Transition) pnVert).isInvisibleTask() == false) {
						// look for the visible transition among the mapped
						// objects in the list
						// as the corresponding activity node in the PN model
						yawlPnActivityMapping.put((YAWLTask) actVert,
								(Transition) pnVert);
					}
					if (((YAWLTask) mappedVert).getSplitType() == EPCConnector.XOR
							&& pnVert instanceof Place) {
						// look for the place that corresponds to the choice
						// node for this explicit
						// split task
						yawlPnChoiceMapping.put((YAWLTask) actVert,
								(Place) pnVert);
					}
				}
			}
		}
		for (ModelGraphVertex actVert : hlYAWL.getChoiceNodes()) {
			YAWLNode mappedVert = (YAWLNode) map0.get(actVert);
			if (mappedVert instanceof YAWLCondition) {
				Place pnPlace = (Place) map1.get(mappedVert);
				// look for deferred choice constructs, which are directly
				// mapped onto PN places
				yawlPnChoiceMapping.put((YAWLCondition) mappedVert, pnPlace);
			}
		}

		// TODO: check whether to remove event type from transition name already
		// in newYAWL import
		Iterator<Transition> transitions = petriNetResult.getTransitions()
				.iterator();
		while (transitions.hasNext()) {
			Transition trans = transitions.next();
			String name = trans.getIdentifier();
			String[] splitName = name.split("\\\\n");
			name = splitName[0]; // ignore everything from \n on
			trans.setIdentifier(name);
		}

		HLPetriNet hlPetriNet = makeHLPetriNet(hlYAWL, petriNetResult,
				yawlPnActivityMapping, yawlPnChoiceMapping);
		updateConditionTargets(yawlPnChoiceMapping, hlPetriNet);
		copyHighLevelInformation(hlYAWL, hlPetriNet);

		return hlPetriNet;
	}

	/**
	 * Create high-level Petri net and re-link activities and choices based on
	 * the mapping.
	 * 
	 * @param hlYAWL
	 *            the source HLYAWL model
	 * @param petriNetResult
	 *            the converted low-level Petri net model
	 * @param yawlPnActivityMapping
	 *            the mapping from YAWL task nodes to PN transitions
	 * @param yawlPnChoiceMapping
	 *            the mapping from YAWL choice nodes to PN places
	 * @return the created target HLPetriNet model
	 */
	private HLPetriNet makeHLPetriNet(HLYAWL hlYAWL, PetriNet petriNetResult,
			HashMap<YAWLTask, Transition> yawlPnActivityMapping,
			HashMap<YAWLNode, Place> yawlPnChoiceMapping) {
		HLPetriNet hlPetriNet = new HLPetriNet(petriNetResult);
		// re-establish the activity mapping
		for (ModelGraphVertex graphNodeYAWL : hlYAWL.getActivityNodes()) {
			for (ModelGraphVertex graphNodePN : hlPetriNet.getGraphNodes()) {
				if (graphNodeYAWL instanceof YAWLTask
						&& yawlPnActivityMapping.get(graphNodeYAWL) == graphNodePN) {
					HLActivity clonedAct = (HLActivity) hlYAWL.findActivity(
							graphNodeYAWL).clone();
					hlPetriNet.setActivity(graphNodePN, clonedAct);
					break;
				}
			}
		}
		// re-establisch the choices
		for (ModelGraphVertex graphNodeYAWL : hlYAWL.getChoiceNodes()) {
			for (ModelGraphVertex graphNodePN : hlPetriNet.getGraphNodes()) {
				if ((graphNodeYAWL instanceof YAWLCondition && graphNodeYAWL
						.outDegree() > 1)
						|| (graphNodeYAWL instanceof YAWLTask
								&& ((YAWLTask) graphNodeYAWL).getSplitType() == EPCConnector.XOR && yawlPnChoiceMapping
								.get(graphNodeYAWL) == graphNodePN)) {
					HLChoice clonedCh = (HLChoice) hlYAWL.findChoice(
							graphNodeYAWL).clone();
					hlPetriNet.setChoice(graphNodePN, clonedCh); // also
					// replaces
					// the
					// choice in
					// hlprocess
					break;
				}
			}
		}
		return hlPetriNet;
	}

	/**
	 * Replaces the condition targets for choices that stem from an XOR split
	 * task.
	 * <p>
	 * Instead of taking the succeeding YAWL tasks as a choice target, the newly
	 * added invisible split transitions belonging to this XOR split are
	 * assigned as the condition targets.
	 * <p>
	 * This is needed if the process should be simulated and the choice should
	 * be made based on a probability or data condition. Otherwise pre-condition
	 * might lead to deadlock situation for explicit choices. Note that deferred
	 * choices are preserved in the way they are in the HLYAWL model.
	 * 
	 * @param yawlPnChoiceMapping
	 *            the mapping of YAWL choice nodes to PN places
	 * @param hlPetriNet
	 *            the target high-level Petri net (resulting from this
	 *            conversion)
	 */
	private void updateConditionTargets(
			HashMap<YAWLNode, Place> yawlPnChoiceMapping, HLPetriNet hlPetriNet) {
		for (YAWLNode choiceNode : yawlPnChoiceMapping.keySet()) {
			if (choiceNode instanceof YAWLTask) {
				Place place = yawlPnChoiceMapping.get(choiceNode);
				HLChoice choice = hlPetriNet.findChoice(place);
				for (Edge edge : place.getOutEdges()) {
					Transition trans = (Transition) edge.getHead();
					HLActivity newTarget = hlPetriNet.findActivity(trans);
					if (newTarget == null) {
						Message.add(
								"Error during adaptation of transition targets. Target "
										+ trans.getIdentifier() + " not found",
								Message.ERROR);
						return;
					}
					// XOR split transitions should only have one output place
					assert (trans.outDegree() == 1);
					Place outPlace = (Place) trans.getOutEdges().get(0)
							.getHead();
					// replace all targets that descend from this place by the
					// new split transition (might reduce the NO. of targets)
					for (Edge targEdge : outPlace.getOutEdges()) {
						Transition targTrans = (Transition) targEdge.getHead();
						if (targTrans.isInvisibleTask() == true) {
							// XOR join transitions should only have one output
							// place
							assert (targTrans.outDegree() == 1);
							Place newOutPlace = (Place) targTrans.getOutEdges()
									.get(0).getHead();
							assert (newOutPlace.outDegree() == 1);
							targTrans = (Transition) newOutPlace.getOutEdges()
									.get(0).getHead();
						}
						HLID actID = hlPetriNet.getActivityID(targTrans);
						if (actID != null) {
							choice
									.replaceChoiceTarget(actID, newTarget
											.getID());
						} else {
							Message.add(
									"Error during adaptation of transition targets. Target "
											+ targTrans.getIdentifier()
											+ " not found", Message.ERROR);
						}
					}
				}
			}
		}
	}

	/**
	 * Copies the high-level information from the HLYAWL model to the target
	 * HLPetriNet.
	 * 
	 * @param hlYAWL
	 *            the source of the high-level information to be copied
	 * @param hlPetriNet
	 *            the target of the high-level information to be copied
	 */
	private void copyHighLevelInformation(HLYAWL hlYAWL, HLPetriNet hlPetriNet) {
		hlPetriNet.getHLProcess().getGlobalInfo().setName(
				hlYAWL.getHLProcess().getGlobalInfo().getName());
		hlPetriNet.getHLProcess().getGlobalInfo().setTimeUnit(
				hlYAWL.getHLProcess().getGlobalInfo().getTimeUnit());
		hlPetriNet.getHLProcess().getGlobalInfo()
				.setCaseGenerationScheme(
						hlYAWL.getHLProcess().getGlobalInfo()
								.getCaseGenerationScheme());
		Iterator<HLAttribute> attributes = hlYAWL.getHLProcess()
				.getAttributes().iterator();
		while (attributes.hasNext()) {
			HLAttribute att = attributes.next();
			HLAttribute clonedAtt = (HLAttribute) att.clone();
			hlPetriNet.getHLProcess().addOrReplace(clonedAtt);
			// also add input attribute to activities
			Iterator<HLActivity> actWithInputIt = hlYAWL.getHLProcess()
					.getActivitiesForInputAttribute(att.getID()).iterator();
			while (actWithInputIt.hasNext()) {
				HLActivity yawlAct = actWithInputIt.next();
				HLActivity pnAct = hlPetriNet.getHLProcess().getActivity(
						yawlAct.getID());
				if (pnAct != null) {
					pnAct.addInputDataAttribute(att.getID());
				} else {
					Message.add(
							"Failure while reconnecting high-level information: attribute "
									+ att.getName()
									+ " could not be assigned to activity "
									+ yawlAct.getName()
									+ ". Activity ID in YAWL model: "
									+ yawlAct.getID().toString() + ".",
							Message.ERROR);
				}
			}
			// also add output attribute to activities
			Iterator<HLActivity> actWithOutputIt = hlYAWL.getHLProcess()
					.getActivitiesForOutputAttribute(att.getID()).iterator();
			while (actWithOutputIt.hasNext()) {
				HLActivity yawlAct = actWithOutputIt.next();
				HLActivity pnAct = hlPetriNet.getHLProcess().getActivity(
						yawlAct.getID());
				if (pnAct != null) {
					pnAct.addOutputDataAttribute(att.getID());
				} else {
					Message.add(
							"Failure while reconnecting high-level information: attribute "
									+ att.getName()
									+ " could not be assigned to activity "
									+ yawlAct.getName()
									+ ". Activity ID in YAWL model: "
									+ yawlAct.getID().toString() + ".",
							Message.ERROR);
				}
			}
		}
		Iterator<HLResource> resources = hlYAWL.getHLProcess().getResources()
				.iterator();
		while (resources.hasNext()) {
			HLResource res = resources.next();
			HLResource clonedRes = (HLResource) res.clone();
			hlPetriNet.getHLProcess().addOrReplace(clonedRes);
		}
		// update nobody and anybody ID as activities will still point to nobody
		// and anybody IDs
		// from the source hlModel (cloning of IDs is not needed)
		hlPetriNet.getHLProcess().removeGroupWithoutAffectingAssigmnets(
				hlPetriNet.getHLProcess().getNobodyGroupID());
		hlPetriNet.getHLProcess().setNobodyGroupID(
				hlYAWL.getHLProcess().getNobodyGroupID());
		hlPetriNet.getHLProcess().removeGroupWithoutAffectingAssigmnets(
				hlPetriNet.getHLProcess().getAnybodyGroupID());
		hlPetriNet.getHLProcess().setAnybodyGroupID(
				hlYAWL.getHLProcess().getAnybodyGroupID());
		// now clone the real group objects
		Iterator<HLGroup> groups = hlYAWL.getHLProcess().getAllGroups()
				.iterator();
		while (groups.hasNext()) {
			HLGroup grp = groups.next();
			HLGroup clonedGrp = (HLGroup) grp.clone();
			hlPetriNet.getHLProcess().addOrReplace(clonedGrp);
		}
		Iterator<HLTypes.Perspective> perspectives = hlYAWL.getHLProcess()
				.getGlobalInfo().getPerspectives().iterator();
		while (perspectives.hasNext()) {
			HLTypes.Perspective per = perspectives.next();
			hlPetriNet.getHLProcess().getGlobalInfo().addPerspective(per);
		}
	}
}
