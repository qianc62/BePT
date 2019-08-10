/**
 * 
 */
package org.processmining.exporting.wwf.xoml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author Kristian Bisgaard Lassen
 */
public class XomlExport implements ExportPlugin {

	private static final Namespace library = Namespace.getNamespace("petrinet",
			"http://schemas.com/PetriNetLibrary");
	private static final Namespace workflow = Namespace
			.getNamespace("workflow",
					"http://schemas.microsoft.com/winfx/2006/xaml/workflow");
	private static final Namespace xaml = Namespace.getNamespace("xaml",
			"http://schemas.microsoft.com/winfx/2006/xaml");

	/**
	 * @see org.processmining.exporting.ExportPlugin#accepts(org.processmining.framework.plugin.ProvidedObject)
	 */
	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.processmining.exporting.ExportPlugin#export(org.processmining.framework.plugin.ProvidedObject,
	 *      java.io.OutputStream)
	 */
	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				Document document = translate((PetriNet) o[i]);
				new XMLOutputter(Format.getPrettyFormat()).output(document,
						System.out);
				new XMLOutputter(Format.getPrettyFormat()).output(document,
						output);
			}
		}
	}

	private Document translate(PetriNet petriNet) {
		Element root = new Element("PetriNet", library);
		root.setAttribute("Name", petriNet.getName().replaceAll(" ", ""), xaml);
		root
				.setAttribute("Class", petriNet.getName().replaceAll(" ", ""),
						xaml);
		root.addNamespaceDeclaration(workflow);

		Element petriNetArcs = new Element("PetriNet.Arcs", library);
		root.addContent(petriNetArcs);
		Element arcCollection = new Element("ArcCollection", library);
		petriNetArcs.addContent(arcCollection);
		List<ModelGraphEdge> edges = petriNet.getEdges();
		Place source = (Place) petriNet.getSource();

		for (ModelGraphEdge edge : edges) {
			Element arc = new Element("Arc", library);
			arc.setAttribute("TransitionCondition", "{" + xaml.getPrefix()
					+ ":Null}", workflow);
			arcCollection.addContent(arc);

			Element arcDestinationActivity = new Element(
					"Arc.DestinationActivity", library);
			arc.addContent(arcDestinationActivity);
			String destinationName = clean(((ModelGraphVertex) edge.getHead())
					.getIdentifier());
			if (edge.getHead() instanceof Place) {
				Element place = new Element("Place", library);
				arcDestinationActivity.addContent(place);
				place.setAttribute("Name", destinationName, xaml);
			} else {
				Element dummyTransition = new Element("DummyTransition",
						library);
				arcDestinationActivity.addContent(dummyTransition);
				dummyTransition.setAttribute("Name", destinationName, xaml);
			}

			Element arcSourceActivity = new Element("Arc.SourceActivity",
					library);
			arc.addContent(arcSourceActivity);
			String sourceName = clean(((ModelGraphVertex) edge.getTail())
					.getIdentifier());
			if (edge.getTail() instanceof Place) {
				Element place = new Element("Place", library);
				arcSourceActivity.addContent(place);
				place.setAttribute("Name", sourceName, xaml);
				if (edge.getTail() == source)
					place.setAttribute("PetriNet.Tokens", "1", library);
			} else {
				Element dummyTransition = new Element("DummyTransition",
						library);
				arcSourceActivity.addContent(dummyTransition);
				dummyTransition.setAttribute("Name", sourceName, xaml);
			}
		}
		for (Place place : petriNet.getPlaces()) {
			Element elm = new Element("Place", library).setAttribute("Name",
					clean(clean(place.getIdentifier())), xaml);
			if (place == source)
				elm.setAttribute("PetriNet.Tokens", "1", library);
			root.addContent(elm);
		}
		for (Transition transition : petriNet.getTransitions())
			root.addContent(new Element("DummyTransition", library)
					.setAttribute("Name", clean(transition.getIdentifier()),
							xaml));

		return new Document(root);
	}

	private String clean(String str) {
		String result = str.replaceAll("\\\\n", "_").replaceAll(" ", "_");
		return result;
	}

	/**
	 * @see org.processmining.exporting.ExportPlugin#getFileExtension()
	 */
	public String getFileExtension() {
		return "xoml";
	}

	/**
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return null;
	}

	/**
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "XOML export file";
	}

}
