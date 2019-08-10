/**
 *
 */
package org.processmining.framework.models.recommendation.net;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.processmining.framework.models.recommendation.Recommendation;
import org.processmining.framework.models.recommendation.RecommendationResult;
import org.processmining.lib.xml.Document;
import org.processmining.lib.xml.Tag;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author christian
 * 
 */
public class RecommendationResultMarshal {

	protected SAXParserFactory parserFactory = SAXParserFactory.newInstance();

	public String marshal(RecommendationResult result) throws Exception {
		StringWriter writer = new StringWriter();
		// start document
		Document doc = new Document(writer, RecommendationQueryMarshal.UTF8);
		Tag root = doc.addNode("RecommendationResult");
		root.addAttribute("partialTraceValue", Double.toString(result
				.getPartialTraceValue()));
		root.addAttribute("queryId", result.getQueryId());
		Tag recommendationsTag = root.addChildNode("RecommendedTasks");
		// write recommendation list
		for (Recommendation rec : result) {
			Tag recNode = recommendationsTag.addChildNode("Recommendation");
			// recNode.addAttribute("weight", Double.toString(rec.getWeight()));
			// recNode.addAttribute("confidence",
			// Double.toString(rec.getConfidence()));
			recNode.addAttribute("doExpectedValue", Double.toString(rec
					.getDoExpectedValue()));
			recNode.addAttribute("dontExpectedValue", Double.toString(rec
					.getDontExpectedValue()));
			recNode.addAttribute("doExpectedSquaredValue", Double.toString(rec
					.getDoExpectedSquaredValue()));
			recNode.addAttribute("dontExpectedSquaredValue", Double
					.toString(rec.getDontExpectedSquaredValue()));
			recNode
					.addAttribute("doWeight", Double
							.toString(rec.getDoWeight()));
			recNode.addAttribute("dontWeight", Double.toString(rec
					.getDontWeight()));
			Tag taskNode = recNode.addChildNode("Task");
			taskNode.addTextNode(rec.getTask().trim());
			// write originator section, if appropriate
			if (rec.getUsers() != null || rec.getRoles() != null
					|| rec.getGroups() != null) {
				Tag resourcesNode = recNode.addChildNode("Originator");
				if (rec.getUsers() != null) {
					for (String s : rec.getUsers()) {
						Tag userTag = resourcesNode.addChildNode("User");
						userTag.addTextNode(s.trim());
					}
				}
				if (rec.getRoles() != null) {
					for (String t : rec.getRoles()) {
						Tag roleTag = resourcesNode.addChildNode("Role");
						roleTag.addTextNode(t.trim());
					}
				}
				if (rec.getGroups() != null) {
					for (String u : rec.getGroups()) {
						Tag groupTag = resourcesNode.addChildNode("Group");
						groupTag.addTextNode(u.trim());
					}
				}
			}
			// write rationale if present
			String rationale = rec.getRationale();
			if (rationale != null && rationale.length() > 0) {
				Tag rationaleTag = recNode.addChildNode("Rationale");
				rationaleTag.addTextNode(rationale.trim());
			}
			// write event type if present
			String eventType = rec.getEventType();
			if (eventType != null && eventType.length() > 0) {
				Tag typeTag = recNode.addChildNode("EventType");
				typeTag.addTextNode(eventType.trim());
			}
		}
		// finish up and return xml string
		doc.close();
		writer.flush();
		return writer.getBuffer().toString();
	}

	public RecommendationResult unmarshal(String resultXml) throws Exception {
		SAXParser parser = parserFactory.newSAXParser();
		RecommendationResultHandler handler = new RecommendationResultHandler();
		parser.parse(new InputSource(new StringReader(resultXml)), handler);
		return handler.getResult();
	}

	public class RecommendationResultHandler extends DefaultHandler {

		protected StringBuilder buffer = null;
		protected RecommendationResult result = null;
		protected Recommendation currentRecommendation = null;

		public RecommendationResultHandler() {
			buffer = new StringBuilder();
		}

		public RecommendationResult getResult() {
			return result;
		}

		public void startDocument() throws SAXException {
			result = null;
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// create tag name
			String tagName = localName;
			if (tagName.equalsIgnoreCase("")) {
				tagName = qName;
			}
			if (tagName.equalsIgnoreCase("RecommendationResult")) {
				// root element
				String queryId = attributes.getValue("queryId");
				String att = attributes.getValue("partialTraceValue");
				double attVal = Double.parseDouble(att);
				result = new RecommendationResult(queryId, attVal);
				currentRecommendation = null;
			} else if (tagName.equalsIgnoreCase("Recommendation")) {
				// create new recommendation
				currentRecommendation = new Recommendation();
				String att;
				double attVal;
				boolean boolVal;
				att = attributes.getValue("doExpectedValue");
				attVal = Double.parseDouble(att);
				currentRecommendation.setDoExpectedValue(attVal);
				att = attributes.getValue("dontExpectedValue");
				attVal = Double.parseDouble(att);
				currentRecommendation.setDontExpectedValue(attVal);
				att = attributes.getValue("doExpectedSquaredValue");
				attVal = Double.parseDouble(att);
				currentRecommendation.setDoExpectedSquaredValue(attVal);
				att = attributes.getValue("dontExpectedSquaredValue");
				attVal = Double.parseDouble(att);
				currentRecommendation.setDontExpectedSquaredValue(attVal);
				att = attributes.getValue("doWeight");
				attVal = Double.parseDouble(att);
				currentRecommendation.setDoWeight(attVal);
				att = attributes.getValue("dontWeight");
				attVal = Double.parseDouble(att);
				currentRecommendation.setDontWeight(attVal);

				// String conf = attributes.getValue("confidence");
				// Double confidence = Double.parseDouble(conf);
				// currentRecommendation.setConfidence(confidence);
				// String wght = attributes.getValue("weight");
				// / double weight = Double.parseDouble(wght);
				// currentRecommendation.setWeight(weight);
			}
			// reset buffer
			buffer.delete(0, buffer.length());
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// create tag name
			String tagName = localName;
			if (tagName.equalsIgnoreCase("")) {
				tagName = qName;
			}
			if (tagName.equalsIgnoreCase("Recommendation")) {
				// add recommmendation
				if (currentRecommendation != null) {
					result.add(currentRecommendation);
				}
				// reset current recommendation
				currentRecommendation = null;
			} else if (tagName.equalsIgnoreCase("Task")
					&& currentRecommendation != null) {
				// set task
				currentRecommendation.setTask(buffer.toString().trim());
			} else if (tagName.equalsIgnoreCase("User")
					&& currentRecommendation != null) {
				// set user
				currentRecommendation.addUser(buffer.toString().trim());
			} else if (tagName.equalsIgnoreCase("Role")
					&& currentRecommendation != null) {
				// set user
				currentRecommendation.addRole(buffer.toString().trim());
			} else if (tagName.equalsIgnoreCase("Group")
					&& currentRecommendation != null) {
				// set user
				currentRecommendation.addGroup(buffer.toString().trim());
			} else if (tagName.equalsIgnoreCase("EventType")
					&& currentRecommendation != null) {
				// set event type
				currentRecommendation.setEventType(buffer.toString().trim());
			} else if (tagName.equalsIgnoreCase("rationale")
					&& currentRecommendation != null) {
				// set rationale
				currentRecommendation.setRationale(buffer.toString().trim());
			}
			// reset buffer
			buffer.delete(0, buffer.length());
		}

		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException {
			buffer.append(arg0, arg1, arg2);
		}

		public void error(SAXParseException arg0) throws SAXException {
			System.err
					.println("Error parsing recommendation result (unmarshalling):");
			arg0.printStackTrace();
		}

		public void fatalError(SAXParseException arg0) throws SAXException {
			System.err
					.println("Fatal error parsing recommendation result (unmarshalling):");
			arg0.printStackTrace();
			result = null;
		}

	}

}
