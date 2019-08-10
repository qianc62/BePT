package org.processmining.converting;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JComponent;

import org.processmining.analysis.decisionmining.DecisionPointAnalysisResult;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.PetriNetNavigation;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningResult;

public class PostProcessOfUML2SequenceChartImport implements ConvertingPlugin {

	public boolean accepts(ProvidedObject object) {
		boolean foundPetriNet = false, foundLogReader = false;
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PetriNet) {
				foundPetriNet = true;
			} else if (object.getObjects()[i] instanceof LogReader) {
				LogReader logReader = (LogReader) object.getObjects()[i];
				String plugin = logReader.getLogSummary().getWorkflowLog()
						.getData().get("plugin");
				foundLogReader = true;// plugin != null && plugin.equals("XMI");
			}
			if (foundPetriNet && foundLogReader)
				return true;
		}
		return false;
	}

	public MiningResult convert(ProvidedObject object) {
		PetriNet providedPN = null;
		LogReader providedLR = null;

		for (int i = 0; (providedPN == null || providedLR == null)
				&& i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PetriNet) {
				providedPN = (PetriNet) object.getObjects()[i];
			} else if (object.getObjects()[i] instanceof LogReader) {
				providedLR = (LogReader) object.getObjects()[i];
			}
		}

		if (providedPN == null || providedLR == null) {
			return null;
		}

		PetriNet reducedPN = reduce(providedPN, providedLR);
		HLPetriNet hlPetriNet = generateGuards(reducedPN, providedLR);
		final DecisionPointAnalysisResult dpar = new DecisionPointAnalysisResult(
				hlPetriNet, reducedPN, providedLR);

		return new DecisionPointAnalysisMiningResult(providedLR, dpar);
	}

	private class DecisionPointAnalysisMiningResult implements MiningResult,
			Provider {

		private final LogReader providedLR;

		private final DecisionPointAnalysisResult dpar;

		public DecisionPointAnalysisMiningResult(LogReader logReader,
				DecisionPointAnalysisResult dpar) {
			this.providedLR = logReader;
			this.dpar = dpar;
		}

		public LogReader getLogReader() {
			return providedLR;
		}

		public JComponent getVisualization() {
			return dpar;
		}

		public ProvidedObject[] getProvidedObjects() {
			return dpar.getProvidedObjects();
		}

	}

	private HLPetriNet generateGuards(PetriNet petriNet, LogReader logReader) {
		HLPetriNet hlpn = new HLPetriNet(petriNet);
		for (HLChoice choice : hlpn.getHLProcess().getChoices()) {
			for (Transition transition : PetriNetNavigation
					.getOutgoingTransitions(hlpn
							.findModelGraphVertexForChoice(choice.getID()))) {
				String guard = extractGuard(transition, logReader);
				if (guard != null) {
					HLActivity target = hlpn.findActivity(transition);
					HLCondition cond = choice.getCondition(target.getID());
					// TODO: instead of setting CPN guard string, provide the
					// expression
					// as expression object (can be converted to CPN-compliant
					// string from there)
					// cond.setExpression(guard);
				}
			}
		}
		return hlpn;
	}

	private String extractGuard(Transition transition, LogReader logReader) {
		String name = null, eventType = null;
		if (transition.getLogEvent() == null) {
			if (transition.getIdentifier().startsWith("DUMMYMESSAGE")) {
				StringTokenizer st = new StringTokenizer(transition
						.getIdentifier(), "\\n");
				name = st.nextToken();
				eventType = st.nextToken();
			} else
				return null;
		} else {
			name = transition.getLogEvent().getModelElementName();
			eventType = transition.getLogEvent().getEventType();
		}
		for (ProcessInstance processInstance : logReader.getInstances()) {
			for (AuditTrailEntry ate : processInstance.getListOfATEs()) {
				if (ate.getName().equals(name)
						&& ate.getType().equals(eventType)) {
					return ate.getAttributes().get("guard");
				}
			}
		}
		return null;
	}

	private PetriNet reduce(PetriNet providedPN, LogReader providedLR) {
		PetriNet result = (PetriNet) providedPN.clone();
		for (Transition transition : new ArrayList<Transition>(result
				.getTransitions())) {
			if (transition.isInvisibleTask()
					|| transition.getIdentifier().startsWith("DUMMYMESSAGE")) {
				test: {
					List<Place> inPlaces = PetriNetNavigation
							.getIncomingPlaces(transition);
					List<Place> outPlaces = PetriNetNavigation
							.getOutgoingPlaces(transition);
					if (inPlaces.size() == 1 && inPlaces.equals(outPlaces)) {
						result.removeVertex(transition);
						break test;
					}
					for (Place place : inPlaces)
						if (outPlaces.contains(place))
							break test;
					if (inPlaces.size() == 1
							&& inPlaces.get(0).outDegree() == 1
							&& inPlaces.get(0).inDegree() == 1) {
						result.removeVertex(transition);
						Transition inTransition = PetriNetNavigation
								.getIncomingTransitions(inPlaces.get(0)).get(0);
						for (Place outPlace : new ArrayList<Place>(outPlaces)) {
							result.addAndLinkEdge(new PNEdge(inTransition,
									outPlace), inTransition, outPlace);
						}
						result.removeVertex(inPlaces.get(0));
					} else if (outPlaces.size() == 1
							&& outPlaces.get(0).outDegree() == 1
							&& outPlaces.get(0).inDegree() == 1) {
						result.removeVertex(transition);
						Transition outTransition = PetriNetNavigation
								.getOutgoingTransitions(outPlaces.get(0))
								.get(0);
						for (Place inPlace : new ArrayList<Place>(inPlaces)) {
							result.addAndLinkEdge(new PNEdge(inPlace,
									outTransition), inPlace, outTransition);
						}
						result.removeVertex(outPlaces.get(0));
					} else if ((inPlaces.size() == 1 && inPlaces.get(0)
							.outDegree() == 1)
							&& (outPlaces.size() == 1 && outPlaces.get(0)
									.inDegree() == 1)) {
						result.removeVertex(transition);
						for (Transition outTransition : PetriNetNavigation
								.getOutgoingTransitions(outPlaces.get(0))) {
							result.addAndLinkEdge(new PNEdge(inPlaces.get(0),
									outTransition), inPlaces.get(0),
									outTransition);
						}
						result.removeVertex(outPlaces.get(0));
					} else if (transition.inDegree() == 1
							&& transition.outDegree() == 1
							&& transition.getIdentifier().startsWith(
									"DUMMYMESSAGE") && outPlaces.size() == 1
							&& outPlaces.get(0).outDegree() == 1) {
						Place inPlace = PetriNetNavigation.getIncomingPlaces(
								transition).get(0);
						Place outPlace = PetriNetNavigation.getOutgoingPlaces(
								transition).get(0);
						result.removeVertex(transition);
						for (Transition inTransition : PetriNetNavigation
								.getIncomingTransitions(outPlace))
							result.addAndLinkEdge(new PNEdge(inTransition,
									inPlace), inTransition, inPlace);
						for (Transition outTransition : PetriNetNavigation
								.getOutgoingTransitions(outPlace))
							result.addAndLinkEdge(new PNEdge(inPlace,
									outTransition), inPlace, outTransition);
						result.removeVertex(outPlace);
					} else if (transition.getIdentifier().startsWith(
							"DUMMYMESSAGE")) {
						transition.setLogEvent(null);
					}
				}
			}
		}
		return result;
	}

	public String getHtmlDescription() {
		return "<p>Removes all redundant silent transitions from the Petri net</p>";
	}

	public String getName() {
		return "Post process model from an XMI MXML log";
	}

}
