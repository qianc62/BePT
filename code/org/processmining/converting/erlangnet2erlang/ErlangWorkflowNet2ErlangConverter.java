package org.processmining.converting.erlangnet2erlang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import org.processmining.framework.models.bpel.util.Pair;
import org.processmining.framework.models.erlang.AtomicStatement;
import org.processmining.framework.models.erlang.ChoiceStatement;
import org.processmining.framework.models.erlang.Function;
import org.processmining.framework.models.erlang.ReceiveStatement;
import org.processmining.framework.models.erlang.SendStatement;
import org.processmining.framework.models.erlang.SpawnStatement;
import org.processmining.framework.models.erlang.Statement;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.PetriNetNavigation;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.pattern.Component;
import org.processmining.framework.models.petrinet.pattern.MarkedGraphComponent;
import org.processmining.framework.models.petrinet.pattern.PatternMatcher;
import org.processmining.framework.models.petrinet.pattern.SequenceComponent;
import org.processmining.framework.models.petrinet.pattern.StateMachineComponent;
import org.processmining.mining.MiningResult;

import att.grappa.Node;

public class ErlangWorkflowNet2ErlangConverter {

	public MiningResult convert(ErlangWorkflowNet wfnet) {
		PetriNet pn = (PetriNet) wfnet.clone();
		Component component = null;
		Map<String, Function> annotations = new LinkedHashMap<String, Function>();
		for (Transition transition : pn.getTransitions()) {
			List<Statement> statements = new ArrayList<Statement>();
			statements.add(new AtomicStatement("io:format(\""
					+ transition.getIdentifier() + "\\n\")"));
			annotations.put(transition.getName(), new Function(clean(transition
					.getIdentifier()), statements));
		}
		while (!isTrivial(pn) && (component = matchComponent(pn)) != null) {
			Pair<Transition, Set<Node>> pair = PatternMatcher.reduce(pn,
					component.getWfnet());
			Function translation = translate(pair.first, component, annotations);
			assert (translation != null);
			for (Node node : pair.second) {
				annotations.remove(node.getName());
			}
			annotations.put(pair.first.getName(), translation);
		}
		if (pn.numberOfTransitions() == 1) {
			Transition t = pn.getTransitions().get(0);
			StringBuilder builder = new StringBuilder();
			String name = wfnet.getIdentifier().replaceAll(" ", "_");
			name = name.substring(0, 1).toLowerCase() + name.substring(1);
			builder
					.append("-module(" + name + ").\n" + "-export(["
							+ "start/0");
			Function function = annotations.get(t.getName());
			Function startFunction = new Function("start", function.statements,
					function.functions, function.visibleFunctions);
			for (Function visibleFunction : startFunction.visibleFunctions) {
				builder.append(", " + visibleFunction.name + "/"
						+ visibleFunction.numberOfArguments());
			}
			builder.append("]).\n\n" + startFunction.toString());
			return new ErlangResult(builder.toString());
		}
		return null;
	}

	private boolean isTrivial(PetriNet pn) {
		return pn.getTransitions().size() == 1;
	}

	private Function translate(Transition replacement, Component component,
			Map<String, Function> annotations) {
		if (component instanceof SequenceComponent) {
			return translateSequence(replacement, component, annotations);
		} else if (component instanceof StateMachineComponent) {
			return translateStateMachine(replacement, component, annotations);
		} else if (component instanceof MarkedGraphComponent) {
			return translateMarkedGraph(replacement, component, annotations);
		}
		return null;
	}

	private Function translateMarkedGraph(Transition replacement,
			Component component, Map<String, Function> annotations) {
		List<Statement> statements = new ArrayList<Statement>();
		List<Function> functions = new ArrayList<Function>();
		List<Function> visibleFunctions = new ArrayList<Function>();
		List<Function> nonRootFunctions = new ArrayList<Function>();
		Node source = component.getWfnet().getSource();
		if (source instanceof Place) {
			source = PetriNetNavigation.getOutgoingTransitions(source).get(0);
		}
		List<Transition> sortedTransitions = topologicalSort((Transition) source);
		Collections.reverse(sortedTransitions);
		assert (sortedTransitions.size() == component.getWfnet()
				.numberOfTransitions());
		for (Transition transition : sortedTransitions) {
			Function function = annotations.get(transition.getName());
			if (transition != source) {
				List<String> synchronizePids = new ArrayList<String>();
				List<Statement> newStatements = new ArrayList<Statement>();
				List<Transition> inputTransitions = PetriNetNavigation
						.getIncomingTransitions(transition);
				for (Transition inTransition : inputTransitions) {
					newStatements.add(new ReceiveStatement("message_"
							+ clean(inTransition.getIdentifier())));
				}
				newStatements.addAll(function.statements);
				List<Transition> outputTransitions = PetriNetNavigation
						.getOutgoingTransitions(transition);
				for (Transition outTransition : outputTransitions) {
					String pid = "PID_" + clean(outTransition.getIdentifier());
					if (outTransition == source) {
						newStatements
								.add(new SendStatement(pid, "message_"
										+ clean(((Transition) source)
												.getIdentifier())));
					} else {
						newStatements.add(new SendStatement(pid, "message_"
								+ clean(transition.getIdentifier())));
					}
					synchronizePids.add(pid);
				}
				Function newFunction = new Function(clean(transition
						.getIdentifier()), newStatements,
						new ArrayList<Function>(), new ArrayList<Function>(),
						synchronizePids);
				functions.add(newFunction);
				visibleFunctions.add(newFunction);
				nonRootFunctions.add(newFunction);
			}
			functions.addAll(function.functions);
			visibleFunctions.addAll(function.visibleFunctions);
		}
		statements.addAll(annotations.get(source.getName()).statements);
		for (Function nonRootFunction : nonRootFunctions) {
			statements.add(new SpawnStatement("PID_" + nonRootFunction.name,
					nonRootFunction.name, nonRootFunction.synchronizePids));
		}
		for (Transition outTransition : PetriNetNavigation
				.getOutgoingTransitions(source)) {
			statements.add(new SendStatement("PID_"
					+ clean(outTransition.getIdentifier()), "message_"
					+ clean(((Transition) source).getIdentifier())));
		}
		return new Function(clean(replacement.getIdentifier()), statements,
				functions, visibleFunctions, new ArrayList<String>());
	}

	private List<Transition> topologicalSort(Transition source) {
		List<Transition> L = new ArrayList<Transition>();
		Queue<Transition> Q = new LinkedList<Transition>();
		Set<Pair<Transition, Transition>> removedEdges = new LinkedHashSet<Pair<Transition, Transition>>();
		Q.add(source);
		while (!Q.isEmpty()) {
			Transition transition = Q.remove();
			L.add(transition);
			for (Transition outTransition : PetriNetNavigation
					.getOutgoingTransitions(transition)) {
				removedEdges.add(Pair.create(transition, outTransition));
				boolean allRemoved = true;
				for (Transition t : PetriNetNavigation
						.getIncomingTransitions(outTransition)) {
					if (!removedEdges.contains(Pair.create(t, outTransition))) {
						allRemoved = false;
						break;
					}
				}
				if (allRemoved)
					Q.add(outTransition);
			}
		}
		return L;
	}

	private String clean(String s) {
		s = s.substring(0, 1).toLowerCase() + s.substring(1);
		s = s.replaceAll("\\\\n", "_").replaceAll("-", "_").replaceAll("[+]",
				"_");
		return s;
	}

	private Function translateStateMachine(Transition replacement,
			Component component, Map<String, Function> annotations) {
		List<Statement> statements = new ArrayList<Statement>();
		List<Function> functions = new ArrayList<Function>();
		List<Function> visibleFunctions = new ArrayList<Function>();
		Node source = component.getWfnet().getSource();
		for (Transition transition : component.getWfnet().getTransitions()) {
			Function function = annotations.get(transition.getName());
			if (source != transition) {
				List<Statement> newStatements = new ArrayList<Statement>(
						function.statements);
				List<Transition> outTransitions = PetriNetNavigation
						.getOutgoingTransitions(transition);
				if (!outTransitions.isEmpty()) {
					List<Pair<String, ? extends Statement>> choices = new ArrayList<Pair<String, ? extends Statement>>();
					int index = 0;
					for (Transition outTransition : outTransitions) {
						choices.add(Pair.create("exp" + (++index),
								new AtomicStatement(clean(outTransition
										.getIdentifier())
										+ "()")));
					}
					newStatements.add(new ChoiceStatement(choices));
				}
				functions.add(new Function(clean(transition.getIdentifier()),
						newStatements));
			}
			functions.addAll(function.functions);
			visibleFunctions.addAll(function.visibleFunctions);
		}
		if (source instanceof Transition) {
			statements.addAll(annotations.get(source.getName()).statements);
		}
		List<Pair<String, ? extends Statement>> choices = new ArrayList<Pair<String, ? extends Statement>>();
		int index = 0;
		for (Transition transition : PetriNetNavigation
				.getOutgoingTransitions(source)) {
			choices.add(Pair.create("exp" + (++index), new AtomicStatement(
					clean(transition.getIdentifier()) + "()")));
		}
		statements.add(new ChoiceStatement(choices));
		return new Function(clean(replacement.getIdentifier()), statements,
				functions, visibleFunctions);
	}

	private Function translateSequence(Transition replacement,
			Component component, Map<String, Function> annotations) {
		List<Statement> statements = new ArrayList<Statement>();
		List<Function> functions = new ArrayList<Function>();
		List<Function> visibleFunctions = new ArrayList<Function>();
		for (Transition transition : component.getWfnet().getTransitions()) {
			Function function = annotations.get(transition.getName());
			statements.addAll(function.statements);
			functions.addAll(function.functions);
			visibleFunctions.addAll(function.visibleFunctions);
		}
		return new Function(clean(replacement.getIdentifier()), statements,
				functions, visibleFunctions);
	}

	private Component matchComponent(PetriNet pn) {
		Component result = PatternMatcher.getMaximalSequence(pn);
		if (result != null)
			return result;
		TreeSet<PetriNet> components = PatternMatcher.getComponents(pn);
		List<? extends Component> results = PatternMatcher
				.getStateMachineComponents(pn, components);
		if (!results.isEmpty())
			return results.get(results.size() - 1);
		results = PatternMatcher.getMarkedGraphComponents(pn, components);
		if (!results.isEmpty())
			return results.get(results.size() - 1);
		return null;
	}

}
