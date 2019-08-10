package org.processmining.importing.erlangnet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java_cup.runtime.Symbol;

import javax.swing.filechooser.FileFilter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.processmining.framework.models.erlangnet.ErlangNet;
import org.processmining.framework.models.erlangnet.ErlangNetPlaceType;
import org.processmining.framework.models.erlangnet.inscription.ArcInscription;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.importing.ImportPlugin;
import org.processmining.importing.erlangnet.arcinscriptionparser.Scanner;
import org.processmining.importing.erlangnet.arcinscriptionparser.parser;
import org.processmining.mining.MiningResult;

public class ErlangNetImport implements ImportPlugin {

	public FileFilter getFileFilter() {
		return new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith("erlang.cpn");
			}

			@Override
			public String getDescription() {
				return "Erlang net";
			}
		};
	}

	public MiningResult importFile(InputStream input) throws IOException {
		try {
			SAXBuilder parser = new SAXBuilder();
			Document document = parser.build(new InputStreamReader(input));
			List list = document.getRootElement().getChild("cpnet")
					.getChildren();
			List<ErlangNet> erlangNets = new ArrayList<ErlangNet>();

			for (Object obj : list) {
				if (obj instanceof Element) {
					Element elm = (Element) obj;
					if (elm.getName().equals("page")) {
						erlangNets.add(parseErlangNet(elm));
					}
				}
			}

			return new ErlangNetResult(erlangNets);
		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		}
	}

	private ErlangNet parseErlangNet(Element elm) throws Exception {
		ErlangNet net = new ErlangNet();
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

	private void parseEdge(Element elm, ErlangNet net,
			Map<String, Place> places, Map<String, Transition> transitions)
			throws Exception {
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
		if (net.getAnnotation(place) != ErlangNetPlaceType.MSG)
			edge.setAttribute("dotcolor", "red");
		else
			edge.setAttribute("dotcolor", "blue");
		parser parser = new parser(new Scanner(new StringReader(label)));
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
			ArcInscription inscription = (ArcInscription) symbol.value;
			net.setAnnotation(edge, inscription);
		}
	}

	private void parseTransition(Element elm, ErlangNet net,
			Map<String, Transition> transitions) {
		Transition transition = new Transition(elm.getChildText("text")
				.replace("\\", "\\\\"), net);
		transitions.put(elm.getAttributeValue("id"), transition);
		net.addTransition(transition);
	}

	private void parsePlace(Element elm, ErlangNet net,
			Map<String, Place> places) {
		Place place = new Place(elm.getChildText("text").replace("\\", "\\\\"),
				net);
		places.put(elm.getAttributeValue("id"), place);
		net.setAnnotation(place, ErlangNetPlaceType.valueOf(elm
				.getChild("type").getChildText("text")));
		if (net.getAnnotation(place) == ErlangNetPlaceType.MSG)
			place.setAttribute("dotcolor", "blue");
		net.addPlace(place);
	}

	public String getHtmlDescription() {
		return "<h3>Erlang net importer</h3>Import an Erlang net into ProM. Erlang nets are made"
				+ " using CPN Tools, and are recognized by their file extension is erlang.cpn. "
				+ "Each top page in the file is imported as a single Erlang net.";
	}

	public String getName() {
		return "Erlang net";
	}

}
