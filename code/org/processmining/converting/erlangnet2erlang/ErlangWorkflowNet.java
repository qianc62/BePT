package org.processmining.converting.erlangnet2erlang;

import java.util.LinkedHashSet;
import java.util.Set;

import org.processmining.framework.models.erlang.Function;
import org.processmining.framework.models.petrinet.AnnotatedPetriNet;
import org.processmining.framework.models.petrinet.Choice;

public class ErlangWorkflowNet extends
		AnnotatedPetriNet<Choice, Function, Object, Object> {

	public final Set<String> variables = new LinkedHashSet<String>();

}
