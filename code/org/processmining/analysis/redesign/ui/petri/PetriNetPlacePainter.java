package org.processmining.analysis.redesign.ui.petri;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;

import org.deckfour.gantzgraf.layout.GGDimension;
import org.deckfour.gantzgraf.model.GGNode;
import org.deckfour.gantzgraf.painter.GGNodeCirclePainter;

public class PetriNetPlacePainter extends GGNodeCirclePainter {

	// Color(red,green,blue)
	// Reminder: Color(255,255,255) = white and Color(0,0,0) = black
	protected Color colorBackground;
	protected Color colorBorder;
	protected Color ColorText;
	private static Color background = new Color(255, 255, 255);
	private static Color border = new Color(40, 50, 40);
	private static Color text = new Color(20, 20, 20);

	public PetriNetPlacePainter(Color background, Color border, Color text) {
		super(background, border, text);
		// do not change colors at mouse over
		// super.setColorsMouseOver(background, border, text);
		// do not change colors at selection of node(s)
		// super.setColorsSelected(background, border, text);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.gantzgraf.painter.GGNodeShapePainter#paint(org.deckfour.gantzgraf.layout.GGDimension,
	 *      java.awt.Graphics2D, org.deckfour.gantzgraf.model.GGNode) specific
	 *      drawing of the nodes: green filling (if simulated), faded, thick and
	 *      normal lining
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
		 * specific drawing of the places
		 */
		// 
		// PetriNetPlace p = (PetriNetPlace) node;
		// /**
		// * place is selected in the graph: thick border width
		// */
		// if (p.isSelected()) {
		// BasicStroke stroke = new BasicStroke(canvasDimension.getBorderWidth()
		// * borderWidth * 3,
		// BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		// g2d.setStroke(stroke);
		// } else {
		// /**
		// * place is not selected in the graph: normal border width
		// */
		BasicStroke stroke = new BasicStroke(canvasDimension.getBorderWidth()
				* borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g2d.setStroke(stroke);
		// }
		/**
		 * set colors
		 */
		colorBackground = background;
		colorBorder = border;
		colorText = text;

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