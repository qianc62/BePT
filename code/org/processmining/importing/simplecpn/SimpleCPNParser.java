package org.processmining.importing.simplecpn;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java_cup.runtime.Symbol;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.processmining.framework.models.erlangnet.ErlangNetPlaceType;
import org.processmining.framework.models.petrinet.AnnotatedPetriNet;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.mining.MiningResult;

public abstract class SimpleCPNParser<ArcParser extends java_cup.runtime.lr_parser, PlaceAnnotation extends Object, TransitionAnnotation extends Object, EdgeAnnotation extends Object, GuardAnnotation extends Object, Net extends AnnotatedPetriNet> {

	public abstract ArcParser getArcParser(Reader reader);

	public abstract Net getNewEmptyNet();

	public final MiningResult parse(InputStream input) throws IOException {
		try {
			SAXBuilder parser = new SAXBuilder();
			Document document = parser.build(new InputStreamReader(input));
			List<Net> nets = new ArrayList<Net>();

			for (Object obj : document.getRootElement().getChild("cpnet")
					.getChildren()) {
				if (obj instanceof Element) {
					Element elm = (Element) obj;
					if (elm.getName().equals("page")) {
						nets.add(parseNet(elm));
					}
				}
			}
			return new SimpleCPNResult<Net>(nets);
		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		}
	}

	private Net parseNet(Element elm) throws Exception {
		Net net = getNewEmptyNet();
		net.setIdentifier(elm.getChild("pageattr").getAttributeValue("name"));
		Map<String, Place> places = new LinkedHashMap<String, Place>();
		Map<String, Transition> transitions = new LinkedHashMap<String, Transition>();
		for (Object obj : elm.getChildren("place")) {
			parsePlace((Element) obj, net, places);
		}
		for (Object obj : elm.getChildren("trans")) {
			parseTransition((Element) obj, net, transitions);
		}
		for (Object obj : elm.getChildren("arc")) {
			parseEdge((Element) obj, net, places, transitions);
		}
		return net;
	}

	private void parseEdge(Element elm, Net net, Map<String, Place> places,
			Map<String, Transition> transitions) throws Exception {
		Place place = places.get(elm.getChild("placeend").getAttributeValue(
				"idref"));
		Transition transition = transitions.get(elm.getChild("transend")
				.getAttributeValue("idref"));
		String orientation = elm.getAttributeValue("orientation");
		PNEdge edge;
		if (orientation.equals("PtoT")) {
			edge = new PNEdge(place, transition);
			net.addAndLinkEdge(edge, place, transition);
		} else if (orientation.equals("TtoP")) {
			edge = new PNEdge(transition, place);
			net.addAndLinkEdge(edge, transition, place);
		} else {
			edge = new PNEdge(place, transition);
			net.addAndLinkEdge(edge, place, transition);
			edge = new PNEdge(transition, place);
			net.addAndLinkEdge(edge, transition, place);
		}
		String label = elm.getChild("annot").getChildText("text");
		ArcParser parser = getArcParser(new StringReader(label));
		Symbol symbol = null;
		try {
			symbol = parser.parse();
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "Error parsing arc inscription on edge ";
			if (orientation.equals("PtoT"))
				msg += "from " + place.getIdentifier() + " to "
						+ transition.getIdentifier();
			else if (orientation.equals("TtoP"))
				msg += "from " + transition.getIdentifier() + " to "
						+ place.getIdentifier();
			else
				msg += "between " + place.getIdentifier() + " and "
						+ transition.getIdentifier();
			msg += ":\n\n\t\t\t\t" + label.replaceAll("\n", "\n\t\t\t\t")
					+ "\n\nImport aborted!\n\nDetailed error message:\n"
					+ e.getMessage();
			throw new Exception(msg);
		}
		if (symbol != null) {
			net.setAnnotation(edge, (EdgeAnnotation) symbol.value);
		}
	}

	private void parseTransition(Element elm, Net net,
			Map<String, Transition> transitions) {
		Transition transition = new Transition(elm.getChildText("text")
				.replace("\\", "\\\\"), net);
		transitions.put(elm.getAttributeValue("id"), transition);
		net.addTransition(transition);
	}

	private void parsePlace(Element elm, Net net, Map<String, Place> places) {
		Place place = new Place(elm.getChildText("text").replace("\\", "\\\\"),
				net);
		places.put(elm.getAttributeValue("id"), place);
		net.setAnnotation(place, getPlaceAnnotation(elm.getChild("type")
				.getChildText("text")));
		net.addPlace(place);
	}

	public abstract PlaceAnnotation getPlaceAnnotation(String type);
}
