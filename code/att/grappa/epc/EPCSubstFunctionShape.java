/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package att.grappa.epc;

import java.awt.*;
import java.awt.geom.*;

import org.processmining.framework.models.epcpack.*;
import att.grappa.*;

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
public class EPCSubstFunctionShape extends GrappaShape implements
		CustomRenderer {

	private Element element;
	private static final float F = (float) 0.95;

	private static final float ID = 3 * PERIPHERY_GAP / 2;
	private float x;
	private float y;
	private float w;
	private float h;
	private float off;
	private float off2;

	public EPCSubstFunctionShape(Element element, double x_, double y_,
			double width_, double height_) {
		super(CUSTOM_SHAPE, x_, y_, width_, height_, 0, 0, 0, 0, 0, false,
				false, null);

		x = (float) x_;
		y = (float) y_;
		w = (float) width_;
		h = (float) height_;
		off = (float) 0.2 * h;
		off2 = (float) 0.3 * h;

		path = new GeneralPath(Grappa.windingRule);

		this.element = element;
		EPCFunction t = (EPCFunction) element.object;

		path.moveTo(x, y);
		path.lineTo(x + w, y);
		path.lineTo(x + w, y + h - off);
		path.lineTo(x + w - off, y + h);
		path.lineTo(x + w - off2, y + h);
		path.lineTo(x + w, y + h - off2);
		path.lineTo(x + w, y + h);
		path.lineTo(x, y + h);
		path.lineTo(x, y);

	}

	/**
	 * The method called when the element needs to be drawn. When used with an
	 * extention of <i>GrappaShape</i>, the default behavior is obtained by:
	 * 
	 * <pre>
	 * public void draw(java.awt.Graphics2D g2d) {
	 * 	g2d.draw(this);
	 * }
	 * </pre>
	 * 
	 * @param g2d
	 *            the Graphics2D context to be used for drawing
	 */
	public void draw(java.awt.Graphics2D g2d) {
		g2d.draw(this);
	}

	/**
	 * The method called when the element needs to be filled. When used with an
	 * extention of <i>GrappaShape</i>, the default behavior is obtained by:
	 * 
	 * <pre>
	 * public void fill(java.awt.Graphics2D g2d) {
	 * 	g2d.fill(this);
	 * }
	 * </pre>
	 * 
	 * @param g2d
	 *            the Graphics2D context to be used for drawing
	 */
	public void fill(java.awt.Graphics2D g2d) {
		g2d.fill(this);
	}

	/**
	 * The method called when the element needs to draw its background image.
	 * When used with an extention of <i>GrappaShape</i> that provides the
	 * underlying element as a global variable, the default behavior is obtained
	 * by:
	 * 
	 * <pre>
	 * public void drawImage(java.awt.Graphics2D g2d) {
	 * 	Rectangle sbox = this.getBounds();
	 * 	Shape clip = g2d.getClip();
	 * 	g2d.clip(this);
	 * 	g2d.drawImage(element.getGrappaNexus().getImage(), sbox.x, sbox.y,
	 * 			sbox.width, sbox.height, null);
	 * 	g2d.setClip(clip);
	 * }
	 * </pre>
	 * 
	 * @param g2d
	 *            the Graphics2D context to be used for drawing
	 */
	public void drawImage(java.awt.Graphics2D g2d) {
		Image image = element.getGrappaNexus().getImage();

		if (image != null) {
			Rectangle sbox = this.getBounds();
			Shape clip = g2d.getClip();
			// prevent reshaping
			double w = ((double) image.getWidth(null)) / (double) sbox.width;
			double h = ((double) image.getHeight(null)) / (double) sbox.height;
			int width = (int) (((double) image.getWidth(null)) / Math.max(w, h));
			int height = (int) (((double) image.getHeight(null)) / Math.max(w,
					h));
			g2d.clip(this);
			g2d.drawImage(image, sbox.x + (sbox.width - width) / 2, sbox.y
					+ (sbox.height - height) / 2, width, height, null);
			g2d.setClip(clip);
		}
	}
}
