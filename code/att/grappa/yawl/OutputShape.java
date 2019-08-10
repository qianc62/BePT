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

package att.grappa.yawl;

import java.awt.*;
import java.awt.geom.*;

import att.grappa.*;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class OutputShape extends GrappaShape implements CustomRenderer {
	private Element element;

	private static final float TD = 13;

	public OutputShape(Element element, double x_, double y_, double width_,
			double height_) {
		super(CUSTOM_SHAPE, x_, y_, width_, height_, 0, 0, 0, 0, 0, false,
				false, null);

		float x = (float) x_;
		float y = (float) y_;
		float w = (float) width_;
		float h = (float) height_;

		this.element = element;

		path.moveTo(x + w / 2 - TD / 2, y + h / 2 - TD / 2);
		path.lineTo(x + w / 2 + TD / 2, y + h / 2 - TD / 2);
		path.lineTo(x + w / 2 + TD / 2, y + h / 2 + TD / 2);
		path.lineTo(x + w / 2 - TD / 2, y + h / 2 + TD / 2);
		path.lineTo(x + w / 2 - TD / 2, y + h / 2 - TD / 2);

		path.append(new Ellipse2D.Double(x, y, w, h), false);
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
