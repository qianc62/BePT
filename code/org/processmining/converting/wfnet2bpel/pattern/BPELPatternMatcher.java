package org.processmining.converting.wfnet2bpel.pattern;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.converting.wfnet2bpel.BPELParser;
import org.processmining.converting.wfnet2bpel.BPELRetriever;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.bpel.BPELActivity;
import org.processmining.framework.models.bpel.BPELEmpty;
import org.processmining.framework.models.bpel.BPELStructured;
import org.processmining.framework.models.bpel.util.Pair;
import org.processmining.framework.models.petrinet.Choice;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.pattern.WellStructuredGraphComponent;
import org.processmining.framework.models.petrinet.pattern.ExplicitChoiceComponent;
import org.processmining.framework.models.petrinet.pattern.ImplicitChoiceComponent;
import org.processmining.framework.models.petrinet.pattern.LibraryComponent;
import org.processmining.framework.models.petrinet.pattern.PatternMatcher;

import att.grappa.Edge;
import att.grappa.Node;

public class BPELPatternMatcher extends PatternMatcher {

	private static final Map<Integer, BPELActivity> cachedBPELActivities = new LinkedHashMap<Integer, BPELActivity>();

	/**
	 * Finds a maximal FLOW-component in a Petri net
	 * 
	 * @param wfnet
	 * @param choices
	 * @return A workflow net that is a marked graph, or null if no such exists
	 */
	public static FlowComponent getMaximalFlowComponent(PetriNet wfnet,
			Map<String, BPELActivity> annotations, Map<String, Choice> choices,
			Set<PetriNet> components) {
		List<WellStructuredGraphComponent> acyclicMarkedGraphs = getWellStructuredGraphComponents(
				wfnet, components);
		for (WellStructuredGraphComponent component : acyclicMarkedGraphs) {
			Node flowSource = component.getWfnet().getSource();
			if (flowSource != wfnet.getSource() && flowSource instanceof Place
					&& component.getWfnet().getSource().outDegree() > 1)
				BPELPatternMatcher.doPlaceBorderedFlowComponentTransformation(
						wfnet, component.getWfnet(), annotations);
			return new FlowComponent(component.getWfnet());
		}
		return null;
	}

	/**
	 * Finds a PICK-component in a WF-net
	 * 
	 * @param wfnet
	 *            - A WF-net
	 * @param choices
	 *            - A map over all places in the WF-net to their choice type
	 * @return A WF-net that represent an implicit choice, or null if none is
	 *         found
	 */
	public static PickComponent getPick(PetriNet wfnet,
			Map<String, Choice> choices) {
		List<ImplicitChoiceComponent> implicitChoiceComponents = PatternMatcher
				.getImplicitChoiceComponents(wfnet, choices);
		if (implicitChoiceComponents.isEmpty())
			return null;
		return new PickComponent(implicitChoiceComponents.get(0).getWfnet());
	}

	/**
	 * Finds a SWITCH-component in a WF-net
	 * 
	 * @param wfnet
	 *            - A WF-net
	 * @param choices
	 *            - A map describing the type of choice each place has
	 * @return A workflow net that represent an explicit choice
	 */
	public static SwitchComponent getSwitch(PetriNet wfnet,
			Map<String, Choice> choices) {
		List<ExplicitChoiceComponent> explicitChoiceComponents = getExplicitChoiceComponents(
				wfnet, choices);
		if (explicitChoiceComponents.isEmpty())
			return null;
		return new SwitchComponent(explicitChoiceComponents.get(0).getWfnet());
	}

	/**
	 * This method takes a place bordered FLOW-component and extend it with a
	 * dummy place and transition. This is to control the synchronization of
	 * flows that run into the component. See the WF-net to BPEL paper for more
	 * information.
	 * 
	 * @param wfnet
	 * @param flow
	 * @param annotations
	 */
	private static void doPlaceBorderedFlowComponentTransformation(
			PetriNet wfnet, PetriNet flow, Map<String, BPELActivity> annotations) {
		Place source = (Place) flow.getSource();
		String tname = wfnet.getName() + "TransitionDummy" + source.hashCode(); //$NON-NLS-1$
		Transition dummyTransition = new Transition(new LogEvent(tname,
				"complete"), wfnet);
		annotations.put(dummyTransition.getName(), new BPELEmpty(tname));
		wfnet.addTransition(dummyTransition);
		flow.addTransition(dummyTransition);
		String pname = wfnet.getName() + "PlaceDummy" + source.hashCode(); //$NON-NLS-1$
		Place dummyPlace = new Place(pname, wfnet);
		wfnet.addPlace(dummyPlace);
		flow.addPlace(dummyPlace);
		wfnet.addAndLinkEdge(new PNEdge(source, dummyTransition), source,
				dummyTransition);
		wfnet.addAndLinkEdge(new PNEdge(dummyTransition, dummyPlace),
				dummyTransition, dummyPlace);
		List<Edge> moveArcs = new LinkedList<Edge>(source.getOutEdges());
		for (Edge arc : moveArcs) {
			if (((PNEdge) arc).getTransition() != dummyTransition) {
				wfnet.addAndLinkEdge(new PNEdge(dummyPlace, ((PNEdge) arc)
						.getTransition()), dummyPlace, ((PNEdge) arc)
						.getTransition());
				wfnet.removeEdge((PNEdge) arc);
			}
		}
	}

	public static BPELLibraryComponent getBPELLibraryComponent(PetriNet wfnet,
			String libraryComponent, String libraryPath,
			Set<PetriNet> components, Map<String, BPELActivity> annotations)
			throws FileNotFoundException, Exception {
		LibraryComponent componentFromLibrary = getComponentFromLibrary(wfnet,
				libraryComponent, libraryPath, components);
		if (componentFromLibrary == null)
			return null;
		BPELActivity activity = getBPELActivityFromLibrary(componentFromLibrary
				.getPath(), libraryPath);
		substituteActivities(activity.cloneActivity(), componentFromLibrary
				.getWfnet().getTransitions(), annotations, componentFromLibrary
				.getIsomorphism());
		return new BPELLibraryComponent(componentFromLibrary.getWfnet(),
				componentFromLibrary.getPath(), componentFromLibrary
						.getIsomorphism(), activity);
	}

	public static BPELActivity substituteActivities(BPELActivity activity,
			ArrayList<Transition> transitions,
			Map<String, BPELActivity> annotations, Map<Node, Node> isomorphism) {
		for (Transition transition : transitions)
			if (annotations.get(transition.getName()) instanceof BPELStructured) {
				activity = BPELRetriever.instance.replaceActivity(
						((Transition) isomorphism.get(transition))
								.getIdentifier(), activity, annotations
								.get(transition.getName()));
				annotations.put(transition.getName(), activity);
			}
		return activity;
	}

	private static BPELActivity getBPELActivityFromLibrary(String path,
			String libraryPath) {
		File file = new File(libraryPath + "/" + path + ".bpel");
		BPELActivity result = cachedBPELActivities.get(path);
		if (result == null) {
			result = BPELParser.parseActivityFromFile(file);
		}
		return result;
	}

	/**
	 * Reduces the input WF-net by the input component
	 * 
	 * @param wfnet
	 *            - The WF-net
	 * @param component
	 *            - The component
	 * @param annotation
	 *            - An annotation describing the component
	 * @param annotations
	 *            - A map from transition names to annotations
	 * @param choices
	 *            - A map from place names to their choice types
	 */
	public static void reduce(PetriNet wfnet, PetriNet component,
			BPELActivity annotation, Map<String, BPELActivity> annotations,
			Map<String, Choice> choices) {
		Pair<Transition, Set<Node>> pair = reduce(wfnet, component);
		annotations.put(pair.first.getName(), annotation);

		for (Node node : pair.second) {
			if (node instanceof Place)
				choices.remove(node.getName());
			else
				annotations.remove(node.getName());
		}
	}

}
