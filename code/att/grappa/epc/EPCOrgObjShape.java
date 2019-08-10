package att.grappa.epc;

import java.awt.*;

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
public class EPCOrgObjShape extends GrappaShape implements CustomRenderer {
	private Element element;
	private static final float F = (float) 0.95;

	private static final float ID = 3 * PERIPHERY_GAP / 2;
	private float x;
	private float y;
	private float w;
	private float h;
	private float off;
	private float off2;

	public EPCOrgObjShape(Element element, double x_, double y_, double width_,
			double height_) {
		/*
		 * public GrappaShape(int type, double x, double y, double width, double
		 * height, int sidesArg, int peripheriesArg, double distortionArg,
		 * double skewArg, double orientationArg, boolean roundedArg, boolean
		 * diagonalsArg, Object extra) {
		 */
		super(OVAL_SHAPE, x_ + 0.5 * width_, y_ + 0.5 * height_, width_,
				height_, 0, 1, 0, 0, 0, false, false, null);

		x = (float) (x_ + 0.5 * width_);
		y = (float) (y_ + 0.5 * height_);
		w = (float) width_;
		h = (float) height_;
		off = (float) 0.2 * h;
		off2 = (float) 0.3 * h;

		// path = new GeneralPath(Grappa.windingRule);

		this.element = element;

		GrappaPoint Ppt = new GrappaPoint();
		GrappaPoint Rpt = new GrappaPoint();
		GrappaPoint Pt0 = new GrappaPoint(w * CIRCLE_XDIAG / 2.0, h
				* CIRCLE_YDIAG / 2.0);

		Ppt.x = x - Pt0.x;
		Ppt.y = y - Pt0.y;
		Rpt.x = Ppt.x;
		Rpt.y = Ppt.y + (2.0 * Pt0.y) - 1.0;

		path.moveTo((float) Ppt.x, (float) Ppt.y);
		path.lineTo((float) Rpt.x, (float) Rpt.y);

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
