/***********************************************************
 *      This software is part of the graphviz package      *
 *                http://www.graphviz.org/                 *
 *                                                         *
 *            Copyright (c) 1994-2004 AT&T Corp.           *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *                      by AT&T Corp.                      *
 *                                                         *
 *        Information and Software Systems Research        *
 *              AT&T Research, Florham Park NJ             *
 **********************************************************/

package att.grappa;

/**
 * An interface for describing the drawing of custom shapes that cannot be
 * captured via a single GeneralPath. This interface would generally be used
 * when the Attribute SHAPE_ATTR=custom and CUSTOM_ATTR is set to the name of a
 * user provided class which would be an extension of GrappaShape and implements
 * this interface. Note that if the custom shape desired by the user can be
 * expressed as a single general path, then there is no need to use this
 * interface or provide the methods it requires.
 * 
 * @version 1.2, 07 Jul 2003; Copyright 1996 - 2003 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a
 *         href="http://www.research.att.com">Research @ AT&T Labs</a>
 */
public interface CustomRenderer {
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
	public void draw(java.awt.Graphics2D g2d);

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
	public void fill(java.awt.Graphics2D g2d);

	/**
	 * The method called when the element needs to draw its background image.
	 * When used with an extention of <i>GrappaShape</i> that provides the
	 * underlying element as a global variable, the default behavior is obtained
	 * by:
	 * 
	 * <pre>
	 * public void drawImage(java.awt.Graphics2D g2d) {
	 * Image image = element.getGrappaNexus().getImage();
	 * 
	 * if (image != null) {
	 * 	 Rectangle sbox = this.getBounds();
	 * 	 Shape clip = g2d.getClip();
	 * 	 // prevent reshaping
	 * 	 double w = ((double) image.getWidth(null)) / (double) sbox.width;
	 * 	 double h = ((double) image.getHeight(null)) / (double) sbox.height;
	 * 	 int width = (int) (((double) image.getWidth(null)) / Math.max(w, h));
	 * 	 int height = (int) (((double) image.getHeight(null)) / Math.max(w, h));
	 * 	 g2d.clip(this);
	 * 	 g2d.drawImage(image, sbox.x + (sbox.width - width) / 2,
	 * 			 sbox.y + (sbox.height - height) / 2, width, height, null);
	 * 	 g2d.setClip(clip);
	 * }
	 * </pre>
	 * 
	 * @param g2d
	 *            the Graphics2D context to be used for drawing
	 */
	public void drawImage(java.awt.Graphics2D g2d);

}
