/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.importing.fuzzyModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.importing.ImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.fuzzymining.graph.ClusterNode;
import org.processmining.mining.fuzzymining.graph.MutableFuzzyGraph;
import org.processmining.mining.fuzzymining.metrics.binary.BinaryMetric;
import org.processmining.mining.fuzzymining.metrics.unary.UnaryMetric;
import org.processmining.mining.fuzzymining.ui.FuzzyModelViewResult;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyModelImport implements ImportPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.importing.ImportPlugin#getFileFilter()
	 */
	public FileFilter getFileFilter() {
		return new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".fmz");
			}

			@Override
			public String getDescription() {
				return "Compact Fuzzy Model";
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.importing.ImportPlugin#importFile(java.io.InputStream)
	 */
	public MiningResult importFile(InputStream input) throws IOException {
		// set up a specialized SAX2 handler to fill the container
		FuzzyModelHandler fmHandler = new FuzzyModelHandler();
		// set up SAX parser and parse provided log file into the container
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = parserFactory.newSAXParser();
			input = new BufferedInputStream(new GZIPInputStream(input));
			parser.parse(input, fmHandler);
			return new FuzzyModelViewResult(fmHandler.getFuzzyModel());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "Imports a Fuzzy Model";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Fuzzy Model import";
	}

	protected class FuzzyModelHandler extends DefaultHandler {

		protected StringBuffer buffer;
		protected int clusterIndex = 0;
		protected String clusterName = null;

		protected MutableFuzzyGraph model;
		protected int size;
		protected LogEvents events;
		protected UnaryMetric nodeSignificance;
		protected BinaryMetric edgeSignificance;
		protected BinaryMetric edgeCorrelation;
		protected HashMap<String, String> attributeMap;

		protected FuzzyModelHandler() {
			buffer = new StringBuffer();
		}

		protected MutableFuzzyGraph getFuzzyModel() {
			if (attributeMap != null) {
				for (String key : attributeMap.keySet()) {
					model.setAttribute(key, attributeMap.get(key));
				}
			}
			return model;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			buffer.append(ch, start, length);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
		 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			String tagName = localName;
			if (tagName.equalsIgnoreCase("")) {
				tagName = qName;
			}
			if (tagName.equalsIgnoreCase("fuzzyModel")) {
				size = Integer.parseInt(attributes.getValue("size"));
			} else if (tagName.equalsIgnoreCase("logEvents")) {
				events = new LogEvents();
			} else if (tagName.equalsIgnoreCase("logEvent")) {
				String element = attributes.getValue("element");
				String type = attributes.getValue("type");
				int occurrenceCount = Integer.parseInt(attributes
						.getValue("occurrence"));
				events.add(new LogEvent(element, type, occurrenceCount));
			} else if (tagName.equalsIgnoreCase("attributes")) {
				attributeMap = new HashMap<String, String>();
			} else if (tagName.equalsIgnoreCase("attribute")) {
				String key = attributes.getValue("key");
				String value = attributes.getValue("value");
				attributeMap.put(key, value);
			} else if (tagName.equalsIgnoreCase("unarySignificance")) {
				int mSize = Integer.parseInt(attributes.getValue("size"));
				nodeSignificance = new UnaryMetric("Unary significance",
						"Node significance", mSize);
			} else if (tagName.equalsIgnoreCase("binarySignificance")) {
				int mSize = Integer.parseInt(attributes.getValue("size"));
				edgeSignificance = new BinaryMetric("Binary significance",
						"edge significance", mSize);
			} else if (tagName.equalsIgnoreCase("binaryCorrelation")) {
				int mSize = Integer.parseInt(attributes.getValue("size"));
				edgeCorrelation = new BinaryMetric("Binary correlation",
						"edge correlation", mSize);
			} else if (tagName.equalsIgnoreCase("abstractedNode")) {
				int aIndex = Integer.parseInt(attributes.getValue("index"));
				model.setNodeAliasMapping(aIndex, null);
			} else if (tagName.equalsIgnoreCase("cluster")) {
				clusterIndex = Integer.parseInt(attributes.getValue("index"));
				String tmpName = attributes.getValue("name");
				if (clusterName != null && clusterName.trim().length() == 0) {
					clusterName = tmpName;
				} else {
					clusterName = null;
				}
			}
			// empty buffer
			buffer.delete(0, buffer.length());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
		 * java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			String tagName = localName;
			if (tagName.equalsIgnoreCase("")) {
				tagName = qName;
			}
			if (tagName.equalsIgnoreCase("metrics")) {
				// construct fuzzy graph after metrics have passed by
				model = new MutableFuzzyGraph(nodeSignificance,
						edgeSignificance, edgeCorrelation, events);
			} else if (tagName.equalsIgnoreCase("unarySignificance")) {
				// parse unary significance
				String val[] = buffer.toString().split(";");
				if (val.length != size) {
					throw new SAXException(
							"Incorrect number of values in unary metric!");
				}
				for (int i = 0; i < size; i++) {
					nodeSignificance.setMeasure(i, Double.parseDouble(val[i]));
				}
			} else if (tagName.equalsIgnoreCase("binarySignificance")) {
				// parse binary significance
				String val[] = buffer.toString().split(";");
				if (val.length != (size * size)) {
					throw new SAXException(
							"Incorrect number of values in binary significance metric!");
				}
				int index = 0;
				for (int x = 0; x < size; x++) {
					for (int y = 0; y < size; y++) {
						edgeSignificance.setMeasure(x, y, Double
								.parseDouble(val[index]));
						index++;
					}
				}
			} else if (tagName.equalsIgnoreCase("binaryCorrelation")) {
				// parse binary correlation
				String val[] = buffer.toString().split(";");
				if (val.length != (size * size)) {
					throw new SAXException(
							"Incorrect number of values in binary correlation metric!");
				}
				int index = 0;
				for (int x = 0; x < size; x++) {
					for (int y = 0; y < size; y++) {
						edgeCorrelation.setMeasure(x, y, Double
								.parseDouble(val[index]));
						index++;
					}
				}
			} else if (tagName
					.equalsIgnoreCase("transformedBinarySignificance")) {
				// parse transformed binary significance
				String val[] = buffer.toString().split(";");
				if (val.length != (size * size)) {
					throw new SAXException(
							"Incorrect number of values in transformed binary significance metric!");
				}
				int index = 0;
				for (int x = 0; x < size; x++) {
					for (int y = 0; y < size; y++) {
						model.setBinarySignificance(x, y, Double
								.parseDouble(val[index]));
						index++;
					}
				}
			} else if (tagName.equalsIgnoreCase("transformedBinaryCorrelation")) {
				// parse transformed binary correlation
				String val[] = buffer.toString().split(";");
				if (val.length != (size * size)) {
					throw new SAXException(
							"Incorrect number of values in transformed binary correlation metric!");
				}
				int index = 0;
				for (int x = 0; x < size; x++) {
					for (int y = 0; y < size; y++) {
						model.setBinaryCorrelation(x, y, Double
								.parseDouble(val[index]));
						index++;
					}
				}
			} else if (tagName.equalsIgnoreCase("cluster")) {
				// parse cluster node definition
				String val[] = buffer.toString().split(";");
				ClusterNode cluster = new ClusterNode(model, clusterIndex);
				if (clusterName != null) {
					cluster.setElementName(clusterName);
				}
				model.addClusterNode(cluster);
				for (int i = 0; i < val.length; i++) {
					int nodeIndex = Integer.parseInt(val[i]);
					cluster.add(model.getPrimitiveNode(nodeIndex));
					model.setNodeAliasMapping(nodeIndex, cluster);
				}
			}
		}

	}

}
