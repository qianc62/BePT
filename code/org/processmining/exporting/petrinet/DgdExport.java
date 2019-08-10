package org.processmining.exporting.petrinet;

import org.processmining.exporting.ExportPlugin;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.models.petrinet.PetriNet;
import java.io.OutputStream;
import java.io.BufferedWriter;
import org.processmining.framework.models.petrinet.Place;
import java.util.ArrayList;
import att.grappa.GrappaPoint;
import org.processmining.framework.models.petrinet.Transition;
import att.grappa.Edge;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class DgdExport implements ExportPlugin {

	public DgdExport() {
	}

	public String getName() {
		return "DiaGraphica Diagram";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				return true;
			}
		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));
				PetriNet pn = (PetriNet) o[i];
				ArrayList<Place> places = pn.getPlaces();
				ArrayList<Transition> transitions = pn.getTransitions();

				double minx = 1.0, maxx = 0.0, miny = 1.0, maxy = 0.0;
				for (Place place : places) {
					GrappaPoint pos = (GrappaPoint) place.visualObject
							.getAttributeValue(place.POS_ATTR);
					if (minx > maxx) {
						minx = maxx = pos.x;
					} else {
						if (minx > pos.x) {
							minx = pos.x;
						}
						if (maxx < pos.x) {
							maxx = pos.x;
						}
					}
					if (miny > maxy) {
						miny = maxy = pos.y;
					} else {
						if (miny > pos.y) {
							miny = pos.y;
						}
						if (maxy < pos.y) {
							maxy = pos.y;
						}
					}
					bw.write("<!--" + place.getIdentifier() + ","
							+ pos.toString() + "-->\n");
				}
				for (Transition transition : transitions) {
					GrappaPoint pos = (GrappaPoint) transition.visualObject
							.getAttributeValue(transition.POS_ATTR);
					if (minx > maxx) {
						minx = maxx = pos.x;
					} else {
						if (minx > pos.x) {
							minx = pos.x;
						}
						if (maxx < pos.x) {
							maxx = pos.x;
						}
					}
					if (miny > maxy) {
						miny = maxy = pos.y;
					} else {
						if (miny > pos.y) {
							miny = pos.y;
						}
						if (maxy < pos.y) {
							maxy = pos.y;
						}
					}
					bw.write("<!--" + transition.getIdentifier() + ","
							+ pos.toString() + "-->\n");
				}

				if ((maxx - minx) > (maxy - miny)) {
					double d = (maxx - minx) - (maxy - miny);
					maxy += d / 2.0;
					miny -= d / 2.0;
				} else if ((maxx - minx) < (maxy - miny)) {
					double d = (maxy - miny) - (maxx - minx);
					maxx += d / 2.0;
					minx -= d / 2.0;
				}

				double r = 25.0 / (maxx - minx);
				if (r > 1.0) {
					r = 1.0;
				}

				bw.write("<?xml version=\"1.0\" ?>\n");
				bw.write("<Diagram><File>" + pn.getName() + ".fsm</File>\n");

				for (Transition transition : transitions) {
					GrappaPoint pos = (GrappaPoint) transition.visualObject
							.getAttributeValue(transition.POS_ATTR);
					double x = (0.95 - r) * (2 * pos.x - (maxx + minx))
							/ (maxx - minx);
					double y = (0.95 - r) * (2 * pos.y - (maxy + miny))
							/ (maxy - miny);

					ArrayList<Edge> edges = transition.getInEdges();
					if (edges != null) {
						for (Edge edge : edges) {
							Place place = (Place) edge.getTail();
							GrappaPoint ppos = (GrappaPoint) place.visualObject
									.getAttributeValue(place.POS_ATTR);
							double px = (0.95 - r)
									* (2 * ppos.x - (maxx + minx))
									/ (maxx - minx);
							double py = (0.95 - r)
									* (2 * ppos.y - (maxy + miny))
									/ (maxy - miny);
							double ex = (x + px) / 2.0;
							double ey = (y + py) / 2.0;
							double erx = (x - px) / 2.0 - r;
							double ery = (py - y) / 2.0;
							bw.write("\n<Shape>\n");
							bw.write("<XCenter>" + ex + "</XCenter>\n");
							bw.write("<YCenter>" + -ey + "</YCenter>\n");
							bw.write("<XDistanceFromCenter>" + erx
									+ "</XDistanceFromCenter>\n");
							bw.write("<YDistanceFromCenter>" + -ery
									+ "</YDistanceFromCenter>\n");
							bw.write("<XHinge>0</XHinge>\n");
							bw.write("<YHinge>0</YHinge>\n");
							bw.write("<AngleCenter>0</AngleCenter>\n");
							bw.write("<Type>TYPE_LINE</Type>\n");
							bw.write("<LineWidth>0.5</LineWidth>\n");
							bw
									.write("<LineColor><Red>0.75</Red><Green>0.75</Green><Blue>0.75</Blue><Alpha>1</Alpha></LineColor>\n");
							bw
									.write("<FillColor><Red>0.75</Red><Green>0.75</Green><Blue>0.75</Blue><Alpha>1</Alpha></FillColor>\n");
							bw
									.write("<XCenterDOF><Attribute /><Value>0</Value><Value>0</Value></XCenterDOF>\n");
							bw
									.write("<YCenterDOF><Attribute /><Value>0</Value><Value>0</Value></YCenterDOF>\n");
							bw
									.write("<WidthDOF><Attribute /><Value>0</Value><Value>0</Value></WidthDOF>\n");
							bw
									.write("<HeightDOF><Attribute /><Value>0</Value><Value>0</Value></HeightDOF>\n");
							bw
									.write("<AngleDOF><Attribute /><Value>0</Value><Value>0</Value></AngleDOF>\n");
							bw
									.write("<ColorDOF><Attribute /><Value>0</Value><Value>0</Value>");
							bw
									.write("<AuxilaryValue>0</AuxilaryValue><AuxilaryValue>0</AuxilaryValue></ColorDOF>\n");
							bw
									.write("<OpacityDOF><Attribute /><Value>0</Value><Value>0</Value></OpacityDOF>\n");
							bw.write("</Shape>\n");
						}
					}
					edges = transition.getOutEdges();
					if (edges != null) {
						for (Edge edge : edges) {
							Place place = (Place) edge.getHead();
							GrappaPoint ppos = (GrappaPoint) place.visualObject
									.getAttributeValue(place.POS_ATTR);
							double px = (0.95 - r)
									* (2 * ppos.x - (maxx + minx))
									/ (maxx - minx);
							double py = (0.95 - r)
									* (2 * ppos.y - (maxy + miny))
									/ (maxy - miny);
							double ex = (x + px) / 2.0;
							double ey = (y + py) / 2.0;
							double erx = (px - x) / 2.0 - r;
							double ery = (y - py) / 2.0;
							bw.write("\n<Shape>\n");
							bw.write("<XCenter>" + ex + "</XCenter>\n");
							bw.write("<YCenter>" + -ey + "</YCenter>\n");
							bw.write("<XDistanceFromCenter>" + erx
									+ "</XDistanceFromCenter>\n");
							bw.write("<YDistanceFromCenter>" + -ery
									+ "</YDistanceFromCenter>\n");
							bw.write("<XHinge>0</XHinge>\n");
							bw.write("<YHinge>0</YHinge>\n");
							bw.write("<AngleCenter>0</AngleCenter>\n");
							bw.write("<Type>TYPE_LINE</Type>\n");
							bw.write("<LineWidth>0.5</LineWidth>\n");
							bw
									.write("<LineColor><Red>0.75</Red><Green>0.75</Green><Blue>0.75</Blue><Alpha>1</Alpha></LineColor>\n");
							bw
									.write("<FillColor><Red>0.75</Red><Green>0.75</Green><Blue>0.75</Blue><Alpha>1</Alpha></FillColor>\n");
							bw
									.write("<XCenterDOF><Attribute /><Value>0</Value><Value>0</Value></XCenterDOF>\n");
							bw
									.write("<YCenterDOF><Attribute /><Value>0</Value><Value>0</Value></YCenterDOF>\n");
							bw
									.write("<WidthDOF><Attribute /><Value>0</Value><Value>0</Value></WidthDOF>\n");
							bw
									.write("<HeightDOF><Attribute /><Value>0</Value><Value>0</Value></HeightDOF>\n");
							bw
									.write("<AngleDOF><Attribute /><Value>0</Value><Value>0</Value></AngleDOF>\n");
							bw
									.write("<ColorDOF><Attribute /><Value>0</Value><Value>0</Value>");
							bw
									.write("<AuxilaryValue>0</AuxilaryValue><AuxilaryValue>0</AuxilaryValue></ColorDOF>\n");
							bw
									.write("<OpacityDOF><Attribute /><Value>0</Value><Value>0</Value></OpacityDOF>\n");
							bw.write("</Shape>\n");

						}
					}

					bw.write("\n<Shape>\n");
					bw.write("<XCenter>" + x + "</XCenter>\n");
					bw.write("<YCenter>" + -y + "</YCenter>\n");
					bw.write("<XDistanceFromCenter>" + r
							+ "</XDistanceFromCenter>\n");
					bw.write("<YDistanceFromCenter>" + r
							+ "</YDistanceFromCenter>\n");
					bw.write("<XHinge>0</XHinge>\n");
					bw.write("<YHinge>0</YHinge>\n");
					bw.write("<AngleCenter>0</AngleCenter>\n");
					bw.write("<Type>TYPE_RECT</Type>\n");
					bw.write("<LineWidth>1</LineWidth>\n");
					bw
							.write("<LineColor><Red>0.5</Red><Green>0.5</Green><Blue>0.5</Blue><Alpha>1</Alpha></LineColor>\n");
					bw
							.write("<FillColor><Red>0.75</Red><Green>0.75</Green><Blue>0.75</Blue><Alpha>1</Alpha></FillColor>\n");
					bw
							.write("<XCenterDOF><Attribute /><Value>0</Value><Value>0</Value></XCenterDOF>\n");
					bw
							.write("<YCenterDOF><Attribute /><Value>0</Value><Value>0</Value></YCenterDOF>\n");
					bw
							.write("<WidthDOF><Attribute /><Value>0</Value><Value>0</Value></WidthDOF>\n");
					bw
							.write("<HeightDOF><Attribute /><Value>0</Value><Value>0</Value></HeightDOF>\n");
					bw
							.write("<AngleDOF><Attribute /><Value>0</Value><Value>0</Value></AngleDOF>\n");
					bw
							.write("<ColorDOF><Attribute /><Value>0</Value><Value>0</Value>");
					bw
							.write("<AuxilaryValue>0</AuxilaryValue><AuxilaryValue>0</AuxilaryValue></ColorDOF>\n");
					bw
							.write("<OpacityDOF><Attribute /><Value>0</Value><Value>0</Value></OpacityDOF>\n");
					bw.write("</Shape>\n");
				}

				for (Place place : places) {
					GrappaPoint pos = (GrappaPoint) place.visualObject
							.getAttributeValue(place.POS_ATTR);
					double x = (0.95 - r) * (2 * pos.x - (maxx + minx))
							/ (maxx - minx);
					double y = (0.95 - r) * (2 * pos.y - (maxy + miny))
							/ (maxy - miny);
					bw.write("\n<Shape>\n");
					bw.write("<XCenter>" + x + "</XCenter>\n");
					bw.write("<YCenter>" + -y + "</YCenter>\n");
					bw.write("<XDistanceFromCenter>" + r
							+ "</XDistanceFromCenter>\n");
					bw.write("<YDistanceFromCenter>" + r
							+ "</YDistanceFromCenter>\n");
					bw.write("<XHinge>0</XHinge>\n");
					bw.write("<YHinge>0</YHinge>\n");
					bw.write("<AngleCenter>0</AngleCenter>\n");
					bw.write("<Type>TYPE_ELLIPSE</Type>\n");
					bw.write("<LineWidth>1</LineWidth>\n");
					bw
							.write("<LineColor><Red>0</Red><Green>0</Green><Blue>0</Blue><Alpha>1</Alpha></LineColor>\n");
					bw
							.write("<FillColor><Red>1</Red><Green>1</Green><Blue>1</Blue><Alpha>1</Alpha></FillColor>\n");
					bw
							.write("<XCenterDOF><Attribute /><Value>0</Value><Value>0</Value></XCenterDOF>\n");
					bw
							.write("<YCenterDOF><Attribute /><Value>0</Value><Value>0</Value></YCenterDOF>\n");
					bw
							.write("<WidthDOF><Attribute /><Value>0</Value><Value>0</Value></WidthDOF>\n");
					bw
							.write("<HeightDOF><Attribute /><Value>0</Value><Value>0</Value></HeightDOF>\n");
					bw
							.write("<AngleDOF><Attribute /><Value>0</Value><Value>0</Value></AngleDOF>\n");
					bw
							.write("<ColorDOF><Attribute>"
									+ place.getIdentifier()
									+ "</Attribute><Value>0</Value><Value>0.3</Value><Value>0</Value>");
					bw
							.write("<AuxilaryValue>0</AuxilaryValue><AuxilaryValue>0</AuxilaryValue><AuxilaryValue>0</AuxilaryValue></ColorDOF>\n");
					bw
							.write("<OpacityDOF><Attribute>"
									+ place.getIdentifier()
									+ "</Attribute><Value>0</Value><Value>1</Value><Value>1</Value></OpacityDOF>\n");
					bw.write("</Shape>\n");
				}

				bw.write("\n</Diagram>\n");
				bw.close();
				return;
			}
		}
	}

	public String getFileExtension() {
		return "dgd";
	}

	public String getHtmlDescription() {
		String s = "<html>";
		s += "<head><title>ProM Framework: Petri net DiaGraphica Diagram Export plug-in</title</head>";
		s += "<body><h1>Petri net DiaGraphica Diagram Export plug-in</h1>";
		s += "<p>The Petri net DiaGraphica Diagram Export plug-in exports a Petri net as a DiaGraphica Diagram.</p>";
		s += "</body></html>";
		return s;
	}
}
