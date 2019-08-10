package org.processmining.framework.models.petrinet;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import att.grappa.Edge;

public class AnnotatedPetriNet<PlaceAnnotationType, TransitionAnnotationType, EdgeAnnotationType, GuardAnnotationType>
		extends PetriNet {

	private final Map<String, PlaceAnnotationType> placeAnnotations;
	private final Map<String, TransitionAnnotationType> transitionAnnotations;
	private final Map<String, EdgeAnnotationType> edgeAnnotations;
	private final Map<String, GuardAnnotationType> guardAnnotations;

	public AnnotatedPetriNet() {
		placeAnnotations = new LinkedHashMap<String, PlaceAnnotationType>();
		transitionAnnotations = new LinkedHashMap<String, TransitionAnnotationType>();
		edgeAnnotations = new LinkedHashMap<String, EdgeAnnotationType>();
		guardAnnotations = new LinkedHashMap<String, GuardAnnotationType>();
	}

	public final void setAnnotation(Place place, PlaceAnnotationType annotation) {
		placeAnnotations.put(place.getName(), annotation);
	}

	public final PlaceAnnotationType getAnnotation(Place place) {
		return placeAnnotations.get(place.getName());
	}

	public final void setAnnotation(Transition transition,
			TransitionAnnotationType annotation) {
		transitionAnnotations.put(transition.getName(), annotation);
	}

	public final TransitionAnnotationType getAnnotation(Transition transition) {
		return transitionAnnotations.get(transition.getName());
	}

	public final void setAnnotation(Edge edge, EdgeAnnotationType annotation) {
		edgeAnnotations.put(edge.getName(), annotation);
	}

	public final EdgeAnnotationType getAnnotation(Edge edge) {
		return edgeAnnotations.get(edge.getName());
	}

	public final void setGuardAnnotation(Transition transition,
			GuardAnnotationType annotation) {
		guardAnnotations.put(transition.getName(), annotation);
	}

	public final GuardAnnotationType getGuardAnnotation(Transition transition) {
		return guardAnnotations.get(transition.getName());
	}

	protected void writeTransitionsToDot(Writer bw) throws IOException {
		Iterator it = getTransitions().iterator();
		while (it.hasNext()) {
			Transition t = (Transition) (it.next());
			String label = t.getIdentifier();
			// write to dot
			TransitionAnnotationType ta = getAnnotation(t);
			if (ta != null) {
				bw.write("t" + t.getNumber() + " [shape=\"record\",label=\""
						+ label + " | " + ta + "\"];\n");
			} else {
				bw.write("t" + t.getNumber() + " [shape=\"box\",label=\""
						+ label);
				if (getAnnotation(t) != null) {
					bw.write("\n"
							+ getAnnotation(t).toString().replace('"', '\''));
				}
				bw.write("\"];\n");
			}
			// connect Petri net nodes to later grappa components
			nodeMapping.put(new String("t" + t.getNumber()), t);
		}
	}

	protected void writePlacesToDot(Writer bw) throws IOException {
		Iterator it = this.getPlaces().iterator();
		while (it.hasNext()) {
			Place p = (Place) (it.next());
			bw.write("p"
					+ p.getNumber()
					+ " [shape=\"circle\",label=\""
					+ (p.getIdentifier() == null ? "" : p.getIdentifier()
							.replace('"', '\''))
					+ "\""
					+ (p.getAttribute("dotcolor") == null ? "" : ",color=\""
							+ p.getAttributeValue("dotcolor") + "\"") + "];\n");

			// connect Petri net nodes to later grappa components
			nodeMapping.put(new String("p" + p.getNumber()), p);
		}
	}

	@Override
	protected void writeEdgesToDot(Writer bw) throws IOException {
		Iterator it = this.getEdges().iterator();
		while (it.hasNext()) {
			PNEdge e = (PNEdge) (it.next());
			if (e.isPT()) {
				Place p = (Place) e.getSource();
				Transition t = (Transition) e.getDest();
				bw.write("p" + p.getNumber() + " -> t" + t.getNumber());
			} else {
				Place p = (Place) e.getDest();
				Transition t = (Transition) e.getSource();
				bw.write("t" + t.getNumber() + " -> p" + p.getNumber());
			}
			boolean added = false;
			bw.write(" [");
			if (e.getAttributeValue("dotcolor") != null) {
				added = true;
				bw.write("color=\"" + e.getAttributeValue("dotcolor") + "\"");
			}
			if (getAnnotation(e) != null) {
				String label = getAnnotation(e).toString().replaceAll("\"",
						"\\\\\"").replaceAll("\n", "\\\\n");
				if (added)
					bw.write(",");
				bw.write("label=\"" + label
						+ "\",fontname=\"Arial\",fontsize=\"8\"");
			}
			bw.write("];\n");
		}
	}

	@Override
	public Object clone() {
		AnnotatedPetriNet<PlaceAnnotationType, TransitionAnnotationType, EdgeAnnotationType, GuardAnnotationType> clone = (AnnotatedPetriNet<PlaceAnnotationType, TransitionAnnotationType, EdgeAnnotationType, GuardAnnotationType>) super
				.clone();
		for (Place place : clone.getPlaces())
			clone.setAnnotation(place, getAnnotation(place));
		for (Transition transition : clone.getTransitions()) {
			clone.setAnnotation(transition, getAnnotation(transition));
			clone
					.setGuardAnnotation(transition,
							getGuardAnnotation(transition));
		}
		for (Object obj : clone.getEdges())
			clone.setAnnotation((Edge) obj, getAnnotation((Edge) obj));
		return clone;
	}

	public PNEdge addAndLinkEdge(Place place, Transition transition) {
		PNEdge edge = new PNEdge(place, transition);
		addAndLinkEdge(edge, place, transition);
		return edge;
	}

	public PNEdge addAndLinkEdge(Transition transition, Place place) {
		PNEdge edge = new PNEdge(transition, place);
		addAndLinkEdge(edge, transition, place);
		return edge;
	}

}
