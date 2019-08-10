package org.processmining.analysis.redesign.ui.petri;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;

import org.deckfour.gantzgraf.layout.GGDimension;
import org.deckfour.gantzgraf.model.GGNode;
import org.deckfour.gantzgraf.painter.GGNodeRoundRectanglePainter;

public class PetriNetTransitionPainter extends GGNodeRoundRectanglePainter {

	// Color(red,green,blue)
	// Reminder: Color(255,255,255) = white and Color(0,0,0) = black
	protected Color colorBackground;
	protected Color colorBorder;
	protected Color ColorText;
	private static Color background = new Color(255, 255, 255);
	private static Color border = new Color(40, 50, 40);
	private static Color text = new Color(20, 20, 20);
	private static Color black = new Color(0, 0, 0);
	private static Color white = new Color(255, 255, 255);
	private static Color blue2 = new Color(0, 255, 255);
	private static Color green2 = new Color(200, 200, 170);
	private static Color yellow2 = new Color(220, 190, 80);
	private static Color red2 = new Color(180, 20, 20);
	private static Color blue = new Color(170, 170, 200);
	private static Color green = new Color(20, 180, 20);
	private static Color yellow = new Color(220, 190, 80);
	private static Color red = new Color(180, 100, 100);
	private static Color gray = new Color(145, 145, 160);

	public PetriNetTransitionPainter(Color background, Color border, Color text) {
		super(background, border, text);
		/**
		 * do not change colors at mouse over
		 */
		super.setColorsMouseOver(background, border, text);
		/**
		 * do not change colors at selection of node(s)
		 */
		super.setColorsSelected(background, border, text);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.gantzgraf.painter.GGNodeShapePainter#paint(org.deckfour.gantzgraf.layout.GGDimension,
	 *      java.awt.Graphics2D, org.deckfour.gantzgraf.model.GGNode) specific
	 *      drawing of the nodes: black if invisible task, thick if selected,
	 *      else normal lining
	 */
	public void paint(GGDimension canvasDimension, Graphics2D g2d, GGNode node) {
		/**
		 * get shape
		 */
		Shape shape = createShape(canvasDimension, node);
		/**
		 * adjust font size
		 */
		float fontSize = canvasDimension.getFontSize();
		/**
		 * determine coordinates using canvas for scaling
		 */
		float x = canvasDimension.translateX(node.x());
		float y = canvasDimension.translateY(node.y());
		float width = canvasDimension.translateX(node.width());
		float height = canvasDimension.translateY(node.height());

		/**
		 * specific drawing of the nodes: invisible task is black thick border
		 * width if the node is selected in the graph
		 */
		PetriNetTransition t = (PetriNetTransition) node;

		if (t.isSelectedInComponent() && !t.original().isInvisibleTask()) {
			/**
			 * transition is selected and included in a component: green
			 * background
			 */
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth * 1, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			/**
			 * set colors
			 */
			colorBackground = green;
			colorBorder = border;
			colorText = text;
		} else if (t.toBeSelectedInComponent()
				&& !t.original().isInvisibleTask()) {
			/**
			 * transition is not yet selected and included in a component, but
			 * it is possible to add it: yellow background
			 */
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth * 1, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			/**
			 * set colors
			 */
			colorBackground = yellow;
			colorBorder = border;
			colorText = text;
		} else if (t.notToBeSelectedInComponent()
				&& !t.original().isInvisibleTask()) {
			/**
			 * transition cannot be included in this component: red background
			 */
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth * 1, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			/**
			 * set colors
			 */
			colorBackground = red;
			colorBorder = border;
			colorText = text;
		} else if (t.isSelectedInComponent() && t.original().isInvisibleTask()) {
			/**
			 * transition is invisible: black node with white letters transition
			 * is selected and included in a component: green border
			 */
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth * 3, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			/**
			 * set colors
			 */
			colorBackground = black;
			colorBorder = green;
			colorText = white;
		} else if (t.toBeSelectedInComponent()
				&& t.original().isInvisibleTask()) {
			/**
			 * transition is invisible: black node with white letters transition
			 * is not yet selected and included in a component, but it is
			 * possible to add it: yellow border
			 */
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth * 3, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			/**
			 * set colors
			 */
			colorBackground = black;
			colorBorder = yellow;
			colorText = white;
		} else if (t.notToBeSelectedInComponent()
				&& t.original().isInvisibleTask()) {
			/**
			 * transition is invisible: black node with white letters transition
			 * cannot be included in this component: red border
			 */
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth * 3, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			/**
			 * set colors
			 */
			colorBackground = black;
			colorBorder = red;
			colorText = white;
		} else if (!t.original().isInvisibleTask()) {
			/**
			 * transition not set with regard to component selection: basic
			 * layout
			 */
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			/**
			 * set colors
			 */
			colorBackground = background;
			colorBorder = border;
			colorText = text;
		} else if (t.original().isInvisibleTask()) {
			/**
			 * transition is invisible: black background with white letters
			 * transition not set with regard to component selection: basic
			 * layout
			 */
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			/**
			 * set colors
			 */
			colorBackground = black;
			colorBorder = black;
			colorText = white;
		}
		/**
		 * paint background
		 */
		GradientPaint gradient = new GradientPaint(x, y, colorBackground, x, y
				+ height, colorBackground, false);
		g2d.setPaint(gradient);
		g2d.fill(shape);
		/**
		 * paint text
		 */
		paintText(g2d, node.label(), x, y, width, height, fontSize, colorText);
		/**
		 * paint border
		 */
		gradient = new GradientPaint(x, y, colorBorder, x, y + height,
				colorBorder, false);
		g2d.setPaint(gradient);
		g2d.draw(shape);
	}
}