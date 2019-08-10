package org.processmining.analysis.redesign.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;

import org.deckfour.gantzgraf.layout.GGDimension;
import org.deckfour.gantzgraf.model.GGNode;
import org.deckfour.gantzgraf.painter.GGNodeRoundRectanglePainter;

/**
 * Author: Mariska Netjes (c) 2008 Technische Universiteit Eindhoven and STW
 */

public class RedesignNodePainter extends GGNodeRoundRectanglePainter {

	// Color(red,green,blue)
	// Reminder: Color(255,255,255) = white and Color(0,0,0) = black
	protected Color colorBackground;
	protected Color colorBorder;
	protected Color ColorText;
	private static Color background = new Color(255, 255, 255);
	private static Color border = new Color(40, 50, 40);
	private static Color text = new Color(20, 20, 20);
	private static Color gray = new Color(145, 145, 160);
	private static Color blue = new Color(170, 170, 200);
	// private static Color blue2 = new Color(0, 255, 255);
	/**
	 * 10-class diverging RYG color scheme for performance comparison
	 * 
	 * @see http://www.personal.psu.edu/cab38/ColorBrewer/ColorBrewer.html
	 */
	private static Color greenTheBest = new Color(0, 104, 55);
	private static Color greenBest = new Color(26, 152, 80);
	private static Color greenSecondBest = new Color(120, 189, 99);
	private static Color greenThirdBest = new Color(166, 217, 106);
	private static Color greenFourthBest = new Color(217, 239, 139);
	private static Color yellow = new Color(254, 224, 139);
	private static Color redFourthWorst = new Color(253, 174, 97);
	private static Color redThirdWorst = new Color(244, 109, 67);
	private static Color redSecondWorst = new Color(215, 48, 39);
	private static Color redWorst = new Color(165, 0, 38);

	public RedesignNodePainter(Color background, Color border, Color text) {
		super(background, border, text);
		/**
		 * do not change colors at mouse over
		 */
		super.setColorsMouseOver(background, border, text);
		/**
		 * do not change colors at selection
		 */
		super.setColorsSelected(background, border, text);
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
		 * specific drawing of the nodes: thick border width if the node is
		 * selected in the graph green filling if simulated otherwise no
		 * filling, normal border color if selected for simulation otherwise
		 * faded border.
		 */

		RedesignNode redesignNode = (RedesignNode) node;

		/**
		 * node is selected in the graph: thick border width other drawing
		 * parameters are determined in the following if clauses
		 */
		if (redesignNode.isSelected()) {
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth * 3, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);

			/**
			 * node is simulated and is the original node
			 */
			if (redesignNode.isSimulated() && redesignNode.getModelID() == 0) {
				/**
				 * fill background node with yellow (is equal to original node)
				 */
				colorBackground = yellow;
				/**
				 * original node is always selected for simulation: normal
				 * lining and text colors
				 */
				colorBorder = border;
				colorText = text;
			}

			/**
			 * node is simulated and performs significantly better than original
			 */
			else if (redesignNode.isSimulated()
					&& redesignNode.isBetterPerforming()) {
				/**
				 * fill background node with green, intensity depends on
				 * performance
				 */
				setColorBackgroundForBetter(redesignNode);
				/**
				 * node is selected for simulation: normal lining and text
				 * colors
				 */
				if (redesignNode.isSelectedForSimulation()) {
					colorBorder = border;
					colorText = text;
					/**
					 * node is not selected for simulation: faded lining and
					 * text
					 */
				} else if (!redesignNode.isSelectedForSimulation()) {
					colorBorder = gray;
					colorText = gray;
				}
			}

			/**
			 * node is simulated and performs the same as the original
			 */
			else if (redesignNode.isSimulated()
					&& redesignNode.isEqualPerforming()) {
				/**
				 * fill background node with yellow
				 */
				colorBackground = yellow;
				/**
				 * node is selected for simulation: normal lining and text
				 * colors
				 */
				if (redesignNode.isSelectedForSimulation()) {
					colorBorder = border;
					colorText = text;
					/**
					 * node is not selected for simulation: faded lining and
					 * text
					 */
				} else if (!redesignNode.isSelectedForSimulation()) {
					colorBorder = gray;
					colorText = gray;
				}
			}

			/**
			 * node is simulated and performs significantly worse than original
			 */
			else if (redesignNode.isSimulated()
					&& redesignNode.isWorsePerforming()) {
				/**
				 * fill background node with red, intensity depends on
				 * performance
				 */
				setColorBackgroundForWorse(redesignNode);
				/**
				 * node is selected for simulation: normal lining and text
				 * colors
				 */
				if (redesignNode.isSelectedForSimulation()) {
					colorBorder = border;
					colorText = text;
					/**
					 * node is not selected for simulation: faded lining and
					 * text
					 */
				} else if (!redesignNode.isSelectedForSimulation()) {
					colorBorder = gray;
					colorText = gray;
				}
			}

			/**
			 * node is not simulated
			 */
			else if (!redesignNode.isSimulated()) {
				/**
				 * fill background node with white
				 */
				colorBackground = background;
				/**
				 * node is selected for simulation: normal lining and text
				 */
				if (redesignNode.isSelectedForSimulation()) {
					colorBorder = border;
					colorText = text;
					/**
					 * node is not selected for simulation: faded lining and
					 * text
					 */
				} else if (!redesignNode.isSelectedForSimulation()) {
					colorBorder = gray;
					colorText = gray;
				}
			}
		}

		/**
		 * node is not selected in the graph: normal border width other
		 * parameters are defined in the following if clauses
		 */
		if (!redesignNode.isSelected()) {
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);

			/**
			 * node is simulated and is the original node
			 */
			if (redesignNode.isSimulated() && redesignNode.getModelID() == 0) {
				/**
				 * fill background node with yellow
				 */
				colorBackground = yellow;
				/**
				 * original node is always selected for simulation: normal
				 * lining and text colors
				 */
				colorBorder = border;
				colorText = text;
			}

			/**
			 * node is simulated and performs significantly better than original
			 */
			else if (redesignNode.isSimulated()
					&& redesignNode.isBetterPerforming()) {
				/**
				 * fill background node with green, intensity depends on
				 * performance
				 */
				setColorBackgroundForBetter(redesignNode);
				/**
				 * node is selected for simulation: normal lining and text
				 * colors
				 */
				if (redesignNode.isSelectedForSimulation()) {
					colorBorder = border;
					colorText = text;
					/**
					 * node is not selected for simulation: faded lining and
					 * text
					 */
				} else if (!redesignNode.isSelectedForSimulation()) {
					colorBorder = gray;
					colorText = gray;
				}
			}

			/**
			 * node is simulated and performs the same as the original
			 */
			else if (redesignNode.isSimulated()
					&& redesignNode.isEqualPerforming()) {
				/**
				 * fill background node with yellow
				 */
				colorBackground = yellow;
				/**
				 * node is selected for simulation: normal lining and text
				 * colors
				 */
				if (redesignNode.isSelectedForSimulation()) {
					colorBorder = border;
					colorText = text;
					/**
					 * node is not selected for simulation: faded lining and
					 * text
					 */
				} else if (!redesignNode.isSelectedForSimulation()) {
					colorBorder = gray;
					colorText = gray;
				}
			}

			/**
			 * node is simulated and performs significantly worse than original
			 */
			else if (redesignNode.isSimulated()
					&& redesignNode.isWorsePerforming()) {
				/**
				 * fill background node with red, intensity depends on
				 * performance
				 */
				setColorBackgroundForWorse(redesignNode);
				/**
				 * node is selected for simulation: normal lining and text
				 * colors
				 */
				if (redesignNode.isSelectedForSimulation()) {
					colorBorder = border;
					colorText = text;
					/**
					 * node is not selected for simulation: faded lining and
					 * text
					 */
				} else if (!redesignNode.isSelectedForSimulation()) {
					colorBorder = gray;
					colorText = gray;
				}
			}

			/**
			 * node is not simulated
			 */
			else if (!redesignNode.isSimulated()) {
				/**
				 * fill background node with white
				 */
				colorBackground = background;
				/**
				 * node is selected for simulation: normal lining and text
				 */
				if (redesignNode.isSelectedForSimulation()) {
					colorBorder = border;
					colorText = text;
					/**
					 * node is not selected for simulation: faded lining and
					 * text
					 */
				} else if (!redesignNode.isSelectedForSimulation()) {
					colorBorder = gray;
					colorText = gray;
				}
			}
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

	/**
	 * fill background of node with green, intensity depends on performance
	 */
	public Color setColorBackgroundForBetter(RedesignNode node) {
		if (node.isBestThePerforming) {
			colorBackground = greenTheBest;
		}
		if (node.isBestPerforming) {
			colorBackground = greenBest;
		}
		if (node.isBestSecondPerforming) {
			colorBackground = greenSecondBest;
		}
		if (node.isBestThirdPerforming) {
			colorBackground = greenThirdBest;
		}
		if (node.isBestFourthPerforming) {
			colorBackground = greenFourthBest;
		}
		return colorBackground;
	}

	/**
	 * fill background of node with red, intensity depends on performance
	 */
	public Color setColorBackgroundForWorse(RedesignNode node) {
		if (node.isWorstPerforming) {
			colorBackground = redWorst;
		}
		if (node.isWorstSecondPerforming) {
			colorBackground = redSecondWorst;
		}
		if (node.isWorstThirdPerforming) {
			colorBackground = redThirdWorst;
		}
		if (node.isWorstFourthPerforming) {
			colorBackground = redFourthWorst;
		}
		return colorBackground;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.gantzgraf.painter.GGNodeShapePainter#paint(org.deckfour.gantzgraf.layout.GGDimension,
	 *      java.awt.Graphics2D, org.deckfour.gantzgraf.model.GGNode) specific
	 *      drawing of the nodes: no filling, dashed, thick and normal lining
	 */
	public void paint1(GGDimension canvasDimension, Graphics2D g2d, GGNode node) {
		// get shape
		Shape shape = createShape(canvasDimension, node);
		// adjust font size
		float fontSize = canvasDimension.getFontSize();
		// determine coordinates using canvas for scaling
		float x = canvasDimension.translateX(node.x());
		float y = canvasDimension.translateY(node.y());
		float width = canvasDimension.translateX(node.width());
		float height = canvasDimension.translateY(node.height());

		// specific drawing of the nodes: no filling, dashed, thick and normal
		// lining
		RedesignNode redesignNode = (RedesignNode) node;
		float[] dash = new float[] { 9 };
		// node for simulation, but not selected: normal lining
		if (redesignNode.isSelectedForSimulation()
				&& !redesignNode.isSelected()) {
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			// node for simulation and selected: thick normal lining
		} else if (redesignNode.isSelectedForSimulation() && node.isSelected()) {
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth * 3, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			// node is selected, but not for simulation: thick, dashed lining
		} else if (!redesignNode.isSelectedForSimulation() && node.isSelected()) {
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth * 3, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND, 0, dash, 0);
			g2d.setStroke(stroke);
			// node is not selected and not simulated: dashed lining
		} else if (!redesignNode.isSelectedForSimulation()
				&& !node.isSelected()) {
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND, 0, dash, 0);
			g2d.setStroke(stroke);
		}
		// paint background
		GradientPaint gradient = new GradientPaint(x, y, colorBgTop, x, y
				+ height, colorBgBottom, false);
		g2d.setPaint(gradient);
		g2d.fill(shape);
		// paint text
		paintText(g2d, node.label(), x, y, width, height, fontSize, colorText);
		// paint border
		gradient = new GradientPaint(x, y, colorBorderTop, x, y + height,
				colorBorderBottom, false);
		g2d.setPaint(gradient);
		g2d.draw(shape);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.deckfour.gantzgraf.painter.GGNodeShapePainter#paint(org.deckfour.
	 * gantzgraf.layout.GGDimension, java.awt.Graphics2D,
	 * org.deckfour.gantzgraf.model.GGNode) specific drawing of the nodes:
	 * white, blue or green filling, normal and thick lining
	 */
	public void paint2(GGDimension canvasDimension, Graphics2D g2d, GGNode node) {
		// get shape
		Shape shape = createShape(canvasDimension, node);
		// adjust font size
		float fontSize = canvasDimension.getFontSize();
		// determine coordinates using canvas for scaling
		float x = canvasDimension.translateX(node.x());
		float y = canvasDimension.translateY(node.y());
		float width = canvasDimension.translateX(node.width());
		float height = canvasDimension.translateY(node.height());

		// specific drawing of the nodes: white and blue filling, normal and
		// thick lining
		RedesignNode redesignNode = (RedesignNode) node;
		// node for simulation, but not selected: blue filling, normal lining
		if (redesignNode.isSelectedForSimulation()
				&& !redesignNode.isSelected()) {
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			GradientPaint gradient = new GradientPaint(x, y, blue, x, y
					+ height, blue, false);
			g2d.setPaint(gradient);
			g2d.fill(shape);
			// node for simulation and selected: blue filling, thick normal
			// lining
		} else if (redesignNode.isSelectedForSimulation() && node.isSelected()) {
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth * 3, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			GradientPaint gradient = new GradientPaint(x, y, blue, x, y
					+ height, blue, false);
			g2d.setPaint(gradient);
			g2d.fill(shape);
			// node is selected, but not for simulation: no filling, thick
			// lining
		} else if (!redesignNode.isSelectedForSimulation() && node.isSelected()) {
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth * 3, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			GradientPaint gradient = new GradientPaint(x, y, colorBgTop, x, y
					+ height, colorBgBottom, false);
			g2d.setPaint(gradient);
			g2d.fill(shape);
			// node is not selected and not simulated: no filling, normal lining
		} else if (!redesignNode.isSelectedForSimulation()
				&& !node.isSelected()) {
			BasicStroke stroke = new BasicStroke(canvasDimension
					.getBorderWidth()
					* borderWidth, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			// paint background
			GradientPaint gradient = new GradientPaint(x, y, colorBgTop, x, y
					+ height, colorBgBottom, false);
			g2d.setPaint(gradient);
			g2d.fill(shape);
		}
		// paint text
		paintText(g2d, node.label(), x, y, width, height, fontSize, colorText);
		// paint border
		GradientPaint gradient = new GradientPaint(x, y, colorBorderTop, x, y
				+ height, colorBorderBottom, false);
		g2d.setPaint(gradient);
		g2d.draw(shape);
	}

}