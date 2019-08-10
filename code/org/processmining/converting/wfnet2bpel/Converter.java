package org.processmining.converting.wfnet2bpel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.processmining.converting.wfnet2bpel.log.BPELLog;
import org.processmining.converting.wfnet2bpel.pattern.BPELLibraryComponent;
import org.processmining.converting.wfnet2bpel.pattern.BPELPatternMatcher;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.bpel.BPEL;
import org.processmining.framework.models.bpel.BPELActivity;
import org.processmining.framework.models.bpel.BPELEmpty;
import org.processmining.framework.models.bpel.BPELInvoke;
import org.processmining.framework.models.bpel.BPELProcess;
import org.processmining.framework.models.bpel.util.Pair;
import org.processmining.framework.models.bpel.util.Quintuple;
import org.processmining.framework.models.petrinet.Choice;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.PetriNetNavigation;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.PnmlWriter;
import org.processmining.framework.models.petrinet.pattern.Component;
import org.processmining.framework.models.petrinet.pattern.ComponentDescription;
import org.processmining.framework.models.petrinet.pattern.NodeHash;
import org.processmining.framework.models.petrinet.pattern.log.Log;
import org.processmining.mining.MiningResult;
import org.processmining.mining.bpel.BPELResult;

import att.grappa.Node;

/**
 * <p>
 * Title: Converter
 * </p>
 * 
 * <p>
 * Description: Converts Workflow nets to BPEL. For efficiency reasons it
 * memorizes the loaded library components, so next time a component is matched
 * against a library component, there is no need for disc access.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: University of Aarhus
 * </p>
 * 
 * @author Kristian Bisgaard Lassen (<a
 *         href="mailto:K.B.Lassen@daimi.au.dk">mailto
 *         :K.B.Lassen@daimi.au.dk</a>)
 * @version 1.0
 */
public class Converter {

	private List<ComponentDescription> matchingOrder = new ArrayList<ComponentDescription>();

	private static final String matchingOrderXML = "lib/plugins/BPEL4WS Conversion/matching-order.xml";

	private static final String matchingOrderXSD = "lib/plugins/BPEL4WS Conversion/matching-order.xsd";

	private static final Namespace namespace = Namespace
			.getNamespace("http://www.processmining.org/bpel4ws_conversion/matching-order");

	private boolean librarySet = false;

	private Boolean storeLog;

	public String path;

	public int count;

	private BPELLog log;

	public boolean askForComponent = true;

	private String[] template = new String[] { "Sequence", "Switch", "Pick",
			"While", "Flow" };

	private static final Namespace matchingOrderNamespace = Namespace
			.getNamespace("http://www.processmining.org/bpel4ws_conversion/matching-order");

	protected synchronized MiningResult convert(LogReader logReader, PetriNet pn) {
		if (isWorkflowNet(pn)) {
			Pair<List<ComponentDescription>, Boolean> userInfo = getUserInfo(pn);
			if (userInfo == null)
				return null;
			// matchingOrder = MatchingOrder.readListOfComponents(
			// componentLibrary, mappingOrderXML);
			try {
				return translate(logReader, (PetriNet) pn.clone());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private Pair<List<ComponentDescription>, Boolean> getUserInfo(PetriNet pn) {
		Pair<List<ComponentDescription>, Boolean> setup = null;
		if (!librarySet) {
			setup = new GetSetup(matchingOrderXML, matchingOrderXSD, template,
					namespace).getSetup();
			if (setup.first == null)
				return null;
			storeLog = setup.second;
			if (storeLog) {
				log = new BPELLog();
			}
			librarySet = true;
			matchingOrder.clear();
			matchingOrder.addAll(setup.first);
		}
		return setup;
	}

	/**
	 * Figures out whethet or not the input Petri net is a Workflow net
	 * 
	 * @param pn
	 *            - A Petri net
	 * @return The answer
	 */
	private boolean isWorkflowNet(PetriNet pn) {
		// @TODO provide more accurate estimate :)
		return true;
	}

	/**
	 * Translates a WF-net to a BPEL4WS specification
	 * 
	 * @param logReader
	 * 
	 * @param wfnet
	 *            - A WF-net
	 * @return A BPEL4WS specification corresponding to the input WF-net
	 * @throws Exception
	 * @throws FileNotFoundException
	 */
	private synchronized MiningResult translate(LogReader logReader,
			PetriNet wfnet) throws FileNotFoundException, Exception {
		BPEL bpel = new BPEL(wfnet.getName());
		BPELTranslator.fragmentCount = 0;
		Component component = null;
		Map<String, BPELActivity> annotations = new HashMap<String, BPELActivity>();
		for (Transition transition : wfnet.getTransitions())
			annotations.put(transition.getName(), new BPELInvoke(transition
					.getIdentifier()));
		Map<String, Choice> choices = new HashMap<String, Choice>();
		for (Place place : wfnet.getPlaces())
			choices.put(place.getName(), NodeHash.getChoice(place));

		try {
			testAllChoices(wfnet, choices);
		} catch (Exception e) {
			return null;
		}

		while (!isTrivial(wfnet)
				&& (component = matchComponent(wfnet, annotations, choices)) != null) {
			storeLogMatch(wfnet, component);
			BPELActivity annotation = BPELTranslator.translate(component,
					annotations);
			BPELPatternMatcher.reduce(wfnet, component.getWfnet(), annotation,
					annotations, choices);
		}
		boolean isTrivial = isTrivial(wfnet);
		if (isTrivial) {
			BPELActivity activity = annotations.get(wfnet.getTransitions().get(
					0).getName());
			BPELProcess process = new BPELProcess(wfnet.getName());
			process.setChildActivity(activity.cloneActivity());
			bpel.setProcess(process);
		}
		if (storeLog)
			return showLog(wfnet, component, annotations, bpel);
		if (isTrivial)
			return new BPELResult(logReader, bpel);
		return null;
	}

	private Log showLog(PetriNet wfnet, Component component,
			Map<String, BPELActivity> annotations, BPEL bpel) {
		log.prepareToShowLog(wfnet, annotations, bpel);
		return log;
	}

	/**
	 * @param wfnet
	 * @param component
	 */
	private void storeLogMatch(PetriNet wfnet, Component component) {
		if (storeLog) {
			log.storeLogMatch(wfnet, component, 0, 0, 0, 0);
		}
	}

	/**
	 * Matches a component in the input WF-net. The order is that of the paper
	 * on WF-net to BPEL4WS
	 * 
	 * @param wfnet
	 *            - A WF-net
	 * @param annotations
	 *            - Annotations of the transitions in the WF-net
	 * @param choices
	 *            - Choices of the places in the WF-net
	 * @return A matched component, or null if no match was found
	 * @throws Exception
	 * @throws FileNotFoundException
	 */
	private Component matchComponent(PetriNet wfnet,
			Map<String, BPELActivity> annotations, Map<String, Choice> choices)
			throws FileNotFoundException, Exception {
		Component match = null;
		TreeSet<PetriNet> components = null;
		for (ComponentDescription componentDescription : matchingOrder) {
			if (componentDescription.isPredefined()) {
				if (componentDescription.getName().equals("Sequence"))
					match = BPELPatternMatcher.getMaximalSequence(wfnet);
				else if (componentDescription.getName().equals("Switch"))
					match = BPELPatternMatcher.getSwitch(wfnet, choices);
				else if (componentDescription.getName().equals("Pick"))
					match = BPELPatternMatcher.getPick(wfnet, choices);
				else if (componentDescription.getName().equals("While"))
					match = BPELPatternMatcher.getWhile(wfnet);
				else if (componentDescription.getName().equals("Flow")) {
					if (components == null)
						components = BPELPatternMatcher.getComponents(wfnet);
					match = BPELPatternMatcher.getMaximalFlowComponent(wfnet,
							annotations, choices, components);
				}
			} else {
				if (components == null)
					components = BPELPatternMatcher.getComponents(wfnet);
				match = BPELPatternMatcher.getBPELLibraryComponent(wfnet,
						componentDescription.getName(), new File(
								matchingOrderXML).getParent(), components,
						annotations);
			}
			if (match != null)
				break;
		}

		if (match == null) {
			Quintuple<PetriNet, BPELActivity, String, Integer, Map<Node, Node>> result = null;
			if (!askForComponent) {
				System.err.println("Making library component: " + path + count);
				result = Quintuple.create(new ArrayList<PetriNet>(components)
						.get(0), (BPELActivity) new BPELEmpty("dummy"), path
						+ count, matchingOrder.size(), result.fifth);
				count++;
			} else {
				if (components == null)
					components = BPELPatternMatcher.getComponents(wfnet);
				result = new ManualTranslationWizard().translateAComponent(
						new ArrayList<PetriNet>(components), matchingOrder);
			}
			if (result.first == null) {
				return null;
			}
			storeComponent(result.first, result.second, result.third,
					result.fourth);
			BPELActivity activity = BPELPatternMatcher.substituteActivities(
					result.second, result.first.getTransitions(), annotations,
					result.fifth);
			match = new BPELLibraryComponent(result.first, result.third,
					result.fifth, activity);
		}

		return match;
	}

	private void storeComponent(PetriNet component, BPELActivity activity,
			String name, Integer position) {
		try {
			File pnmlFile = new File(new File(matchingOrderXML).getParent()
					+ "/" + name + ".pnml");
			createSubdirectories(pnmlFile);
			pnmlFile.createNewFile();
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(pnmlFile)));
			PnmlWriter.write(false, true, component, bw);
			bw.close();

			File bpelFile = new File(new File(matchingOrderXML).getParent()
					+ "/" + name + ".bpel");
			createSubdirectories(bpelFile);
			if (!bpelFile.createNewFile()) {
				bpelFile.delete();
				bpelFile.createNewFile();
			}
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(bpelFile)));
			TransformerFactory xformFactory = TransformerFactory.newInstance();
			Transformer idTransform = xformFactory.newTransformer();
			Source input = new DOMSource(activity.getElement());
			StringWriter buffer = new StringWriter();
			idTransform.transform(input, new StreamResult(buffer));
			String xml = buffer.getBuffer().toString();
			xml = xml.substring(xml.indexOf(">") + 1);
			bw.write("<process>\n");
			bw.write(xml);
			bw.write("</process>");
			bw.flush();
			bw.close();
			Element matchingOrderElement = new Element("matching-order",
					matchingOrderNamespace);

			List<ComponentDescription> tmpMatchingOrder = new ArrayList<ComponentDescription>();
			int index = 0;
			for (ComponentDescription componentDescription : matchingOrder) {
				if (index == position) {
					matchingOrderElement.addContent(new Element("component",
							matchingOrderNamespace).setAttribute("path", name));
					tmpMatchingOrder.add(new ComponentDescription(false, name,
							null));
				}
				if (componentDescription.isPredefined())
					matchingOrderElement.addContent(new Element("predefined",
							matchingOrderNamespace).setAttribute("name",
							componentDescription.getName()));
				else
					matchingOrderElement.addContent(new Element("component",
							matchingOrderNamespace).setAttribute("path",
							componentDescription.getName()));
				tmpMatchingOrder.add(componentDescription);
				index++;
			}
			if (index == position) {
				matchingOrderElement.addContent(new Element("component",
						matchingOrderNamespace).setAttribute("path", name));
				tmpMatchingOrder
						.add(new ComponentDescription(false, name, null));
			}
			matchingOrder.clear();
			matchingOrder.addAll(tmpMatchingOrder);

			Document document = new Document(matchingOrderElement);
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			FileOutputStream out = new FileOutputStream(new File(
					matchingOrderXML));
			xmlOut.output(document, out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	private void createSubdirectories(File file) {
		int index = file.getAbsolutePath().lastIndexOf(File.separator);
		String str = file.getAbsolutePath().substring(0, index);
		new File(str).mkdirs();
	}

	/**
	 * Tests if a Petri net is trivial in the sense of the WF-net to BPEL4WS
	 * paper
	 * 
	 * @param wfnet
	 *            - A WF-net
	 * @return The answer
	 */
	private boolean isTrivial(PetriNet wfnet) {
		return wfnet.getTransitions().size() == 1;
	}

	private void testAllChoices(PetriNet wfnet, Map<String, Choice> choices)
			throws Exception {
		Map<String, Pair<PetriNet, Choice>> unclearChoices = new LinkedHashMap<String, Pair<PetriNet, Choice>>();
		for (Entry<String, Choice> choice : choices.entrySet()) {
			if (choice.getValue().equals(Choice.NOT_DEFINED)) {
				Set<Node> nodes = new LinkedHashSet<Node>();
				Place place = null;
				for (Place aPlace : wfnet.getPlaces())
					if (choice.getKey().equals(aPlace.getName())) {
						place = aPlace;
						break;
					}
				nodes.add(place);
				nodes.addAll(PetriNetNavigation.getOutgoingTransitions(place));
				PetriNet choiceNet = wfnet.extractNet(nodes);
				choiceNet.getClusters().clear();
				unclearChoices.put(place.getName(), Pair.create(choiceNet,
						choice.getValue()));
			}
		}
		if (!unclearChoices.isEmpty()) {
			Map<String, Choice> result = new UnclearChoiceHandler()
					.handleChoices(unclearChoices);
			if (result == null) {
				throw new Exception();
			}
			choices.putAll(result);
		}
	}
}
