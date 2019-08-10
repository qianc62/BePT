/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.protos;

import java.io.*;
import java.util.*;

import org.processmining.framework.models.*;
import org.processmining.framework.models.protos.*;

import org.w3c.dom.*;

/**
 * <p>
 * Title: Protos drawing
 * </p>
 * 
 * <p>
 * Description: Holds a Protos drawing
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
 * @author Eric Verbeek
 * @version 1.0
 */
public class ProtosDrawing {
	static int DrawingEllipse = 0;
	static int DrawingLine = 1;
	static int DrawingRectangle = 2;
	static int DrawingTriangle = 3;
	static int DrawingText = 4;

	private int type; // One of the values above.

	private int x0; // Ellipse, Line, Rectangle, Triangle, Text
	private int y0; // Ellipse, Line, Rectangle, Triangle, Text
	private int x1; // Ellipse, Line, Rectangle, Triangle, Text
	private int y1; // Ellipse, Line, Rectangle, Triangle, Text
	private int x2; // Ellipse, Rectangle, Triangle, Text
	private int y2; // Ellipse, Rectangle, Triangle, Text
	private int x3; // Ellipse, Rectangle, Text
	private int y3; // Ellipse, Rectangle, Text
	private int fillColor; // Ellipse, Rectangle, Triangle, Text
	private boolean transparent; // Ellipse, Rectangle, Triangle, Text
	private int lineColor; // Ellipse, Line, Rectangle, Triangle, Text
	private int lineWidth; // Ellipse, Line, Rectangle, Triangle, Text
	private String lineStyle; // Ellipse, Line, Rectangle, Triangle, Text
	private String lineArrow; // Line
	private String objectID; // Ellipse, Line, Rectangle, Triangle, Text
	// (optional, could be NULL)
	private String alignment; // Text
	private String fontName; // Text
	private int fontSize; // Text
	private boolean fontBold; // Text
	private boolean fontItalic; // Text
	private boolean fontUnderline; // Text
	private int fontColor; // Text
	private String description; // Text

	public ProtosDrawing() {
	}

	/**
	 * Constructs a Drawing object (except for its type) out of a "ellipse",
	 * "line", "rectangle", "triangle", or "text" Node.
	 * 
	 * @param drawingNode
	 *            Node The "ellipse", "line", "rectangle", "triangle", or "text"
	 *            node that contains the Drawing.
	 * @return String Any error message.
	 */
	private String readXMLExportDrawing(Node drawingNode) {
		String msg = "";
		NodeList nodes = drawingNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.X0)) {
				x0 = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Y0)) {
				y0 = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.X1)) {
				x1 = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Y1)) {
				y1 = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.X2)) {
				x2 = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Y2)) {
				y2 = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.X3)) {
				x3 = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Y3)) {
				y3 = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Fillcolor)) {
				fillColor = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Transparent)) {
				transparent = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.Linecolor)) {
				lineColor = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Linewidth)) {
				lineWidth = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.Linestyle)) {
				lineStyle = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Linearrow)) {
				lineArrow = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Object)) {
				objectID = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Alignment)) {
				alignment = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Font)) {
				NodeList subNodes = node.getChildNodes();
				for (int j = 1; j < subNodes.getLength(); j++) {
					Node subNode = subNodes.item(j);
					if (node.getNodeName().equals(ProtosString.Name)) {
						fontName = ProtosUtil.readString(subNode);
					} else if (node.getNodeName().equals(ProtosString.Size)) {
						fontSize = ProtosUtil.readInt(subNode);
					} else if (node.getNodeName()
							.equals(ProtosString.Underline)) {
						fontUnderline = ProtosUtil.readBool(subNode);
					} else if (node.getNodeName().equals(ProtosString.Bold)) {
						fontBold = ProtosUtil.readBool(subNode);
					} else if (node.getNodeName().equals(ProtosString.Italic)) {
						fontItalic = ProtosUtil.readBool(subNode);
					} else if (node.getNodeName().equals(ProtosString.Color)) {
						fontColor = ProtosUtil.readInt(subNode);
					}
				}
			} else if (node.getNodeName().equals(ProtosString.Description)) {
				description = ProtosUtil.readString(node);
			}
		}
		return msg;
	}

	/**
	 * Constructs a Drawing object out of a "drawing" Node.
	 * 
	 * @param drawingNode
	 *            Node The "drawing" node that contains the Drawing.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node drawingNode) {
		String msg = "";
		NodeList nodes = drawingNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Ellipse)) {
				type = DrawingEllipse;
				msg += readXMLExportDrawing(node);
			} else if (node.getNodeName().equals(ProtosString.Line)) {
				type = DrawingLine;
				msg += readXMLExportDrawing(node);
			} else if (node.getNodeName().equals(ProtosString.Rectangle)) {
				type = DrawingRectangle;
				msg += readXMLExportDrawing(node);
			} else if (node.getNodeName().equals(ProtosString.Triangle)) {
				type = DrawingTriangle;
				msg += readXMLExportDrawing(node);
			} else if (node.getNodeName().equals(ProtosString.Text)) {
				type = DrawingText;
				msg += readXMLExportDrawing(node);
			}
		}
		return msg;
	}

	/**
	 * Returns the Drawing object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Drawing object.
	 * @return String The Drawing object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";

		xml += "<" + tag + ">";

		if (type == DrawingEllipse) {
			xml += "<" + ProtosString.Ellipse + ">";
		} else if (type == DrawingLine) {
			xml += "<" + ProtosString.Line + ">";
		} else if (type == DrawingRectangle) {
			xml += "<" + ProtosString.Rectangle + ">";
		} else if (type == DrawingTriangle) {
			xml += "<" + ProtosString.Triangle + ">";
		} else if (type == DrawingText) {
			xml += "<" + ProtosString.Text + ">";
		}

		xml += ProtosUtil.writeInt(ProtosString.X0, x0);
		xml += ProtosUtil.writeInt(ProtosString.Y0, y0);
		xml += ProtosUtil.writeInt(ProtosString.X1, x1);
		xml += ProtosUtil.writeInt(ProtosString.Y1, y1);
		if (type == DrawingEllipse || type == DrawingRectangle
				|| type == DrawingTriangle || type == DrawingText) {
			xml += ProtosUtil.writeInt(ProtosString.X2, x2);
			xml += ProtosUtil.writeInt(ProtosString.Y2, y2);
		}
		if (type == DrawingEllipse || type == DrawingRectangle
				|| type == DrawingText) {
			xml += ProtosUtil.writeInt(ProtosString.X3, x3);
			xml += ProtosUtil.writeInt(ProtosString.Y3, y3);
		}
		if (type == DrawingEllipse || type == DrawingRectangle
				|| type == DrawingTriangle || type == DrawingText) {
			xml += ProtosUtil.writeInt(ProtosString.Fillcolor, fillColor);
			xml += ProtosUtil.writeBool(ProtosString.Transparent, transparent);
		}
		xml += ProtosUtil.writeInt(ProtosString.Linecolor, lineColor);
		xml += ProtosUtil.writeInt(ProtosString.Linewidth, lineWidth);
		xml += ProtosUtil.writeString(ProtosString.Linestyle, lineStyle);
		if (type == DrawingLine) {
			xml += ProtosUtil.writeString(ProtosString.Linearrow, lineArrow);
		}
		xml += ProtosUtil.writeStringIfNonEmpty(ProtosString.Object, objectID);
		if (type == DrawingText) {
			xml += ProtosUtil.writeString(ProtosString.Alignment, alignment);
			xml += "<" + ProtosString.Font + ">";
			{
				xml += ProtosUtil.writeString(ProtosString.Name, fontName);
				xml += ProtosUtil.writeInt(ProtosString.Size, fontSize);
				xml += ProtosUtil.writeBool(ProtosString.Underline,
						fontUnderline);
				xml += ProtosUtil.writeBool(ProtosString.Bold, fontBold);
				xml += ProtosUtil.writeBool(ProtosString.Italic, fontItalic);
				xml += ProtosUtil.writeInt(ProtosString.Color, fontColor);
			}
			xml += "</" + ProtosString.Font + ">";
			xml += ProtosUtil
					.writeString(ProtosString.Description, description);
		}

		if (type == DrawingEllipse) {
			xml += "</" + ProtosString.Ellipse + ">";
		} else if (type == DrawingLine) {
			xml += "</" + ProtosString.Line + ">";
		} else if (type == DrawingRectangle) {
			xml += "</" + ProtosString.Rectangle + ">";
		} else if (type == DrawingTriangle) {
			xml += "</" + ProtosString.Triangle + ">";
		} else if (type == DrawingText) {
			xml += "</" + ProtosString.Text + ">";
		}

		xml += "</" + tag + ">";

		return xml;
	}
}
