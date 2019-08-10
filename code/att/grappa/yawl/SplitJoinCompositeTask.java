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

import org.processmining.framework.models.yawl.*;
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

public class SplitJoinCompositeTask extends GrappaShape implements
		CustomRenderer {
	private Element element;
	private static final float F = (float) 0.95;

	private static final float ID = 3 * PERIPHERY_GAP / 2;
	private float x;
	private float y;
	private float w;
	private float h;

	private boolean done = false;

	public SplitJoinCompositeTask(Element element, double x_, double y_,
			double width_, double height_) {
		super(CUSTOM_SHAPE, x_, y_, width_, height_, 0, 0, 0, 0, 0, false,
				false, null);

		x = (float) x_;
		y = (float) y_;
		w = (float) width_;
		h = (float) height_;

		path = new GeneralPath(Grappa.windingRule);

		this.element = element;
		YAWLTask t = (YAWLTask) element.object;
		if (t.getJoinType() != YAWLTask.NONE) {
			x += ID;
			w -= ID;
			path.moveTo(x - ID, y);
			path.lineTo(x, y);
			path.lineTo(x, y + h);
			path.lineTo(x - ID, y + h);
			path.lineTo(x - ID, y);
		}
		if (t.getSplitType() != YAWLTask.NONE) {
			w -= ID;
			path.moveTo(x + w + ID, y);
			path.lineTo(x + w, y);
			path.lineTo(x + w, y + h);
			path.lineTo(x + w + ID, y + h);
			path.lineTo(x + w + ID, y);
		}

		path.moveTo(x, y);
		path.lineTo(x + w, y);
		path.lineTo(x + w, y + h);
		path.lineTo(x, y + h);
		path.lineTo(x, y);
		path.moveTo(x + (1 - F) * w, y + (1 - F) * h);
		path.lineTo(x + (1 - F) * w, y + F * h);
		path.lineTo(x + F * w, y + F * h);
		path.lineTo(x + F * w, y + (1 - F) * h);
		path.lineTo(x + (1 - F) * w, y + (1 - F) * h);
		done = false;

		if (t.getJoinType() != YAWLTask.NONE) {
			x -= ID;
			w += ID;
		}
		if (t.getSplitType() != YAWLTask.NONE) {
			w += ID;
		}
	}

	private void buildPath() {
		// Check if element.object instanceof YAWLTask
		done = true;
		if (element.object == null) {
			return;
		}
		if (element.object instanceof YAWLTask) {
			// Check the split and join types
			YAWLTask t = (YAWLTask) element.object;
			x += ID;
			w -= ID + ID;
			if (t.getJoinType() == YAWLTask.AND) {
				path.moveTo(x - ID, y);
				path.lineTo(x, y + h / 2);
				path.lineTo(x - ID, y + h);
			}
			if (t.getJoinType() == YAWLTask.XOR) {
				path.moveTo(x, y);
				path.lineTo(x - ID, y + h / 2);
				path.lineTo(x, y + h);
			}
			if (t.getJoinType() == YAWLTask.OR) {
				path.moveTo(x - ID / 2, y);
				path.lineTo(x, y + h / 2);
				path.lineTo(x - ID / 2, y + h);
				path.lineTo(x - ID, y + h / 2);
				path.lineTo(x - ID / 2, y);
			}
			if (t.getJoinType() != YAWLTask.NONE) {
				path.moveTo(x, y);
				path.lineTo(x, y + h);
			}

			if (t.getSplitType() == YAWLTask.AND) {
				path.moveTo(x + w + ID, y);
				path.lineTo(x + w, y + h / 2);
				path.lineTo(x + w + ID, y + h);
			}
			if (t.getSplitType() == YAWLTask.XOR) {
				path.moveTo(x + w, y);
				path.lineTo(x + w + ID, y + h / 2);
				path.lineTo(x + w, y + h);
			}
			if (t.getSplitType() == YAWLTask.OR) {
				path.moveTo(x + w + ID / 2, y);
				path.lineTo(x + w, y + h / 2);
				path.lineTo(x + w + ID / 2, y + h);
				path.lineTo(x + w + ID, y + h / 2);
				path.lineTo(x + w + ID / 2, y);
			}
			if (t.getSplitType() != YAWLTask.NONE) {
				path.moveTo(x + w, y);
				path.lineTo(x + w, y + h);
			}
		}

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
		if (!done) {
			buildPath();
		}
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
		if (!done) {
			buildPath();
		}
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
		if (!done) {
			buildPath();
		}
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
