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

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * This class provides a PathIterator for GrappaNexus shapes.
 * 
 * @version 1.2, 07 Jul 2003; Copyright 1996 - 2003 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a
 *         href="http://www.research.att.com">Research @ AT&T Labs</a>
 */
public class GrappaPathIterator implements PathIterator {
	GrappaNexus grappaNexus;
	AffineTransform affine;
	PathIterator shapeIterator = null;
	PathIterator areaIterator = null;
	double[] pts = new double[6];
	int type;

	// //////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// //////////////////////////////////////////////////////////////////////

	/**
	 * Constructs a new <code>GrappaPathIterator</code> given a GrappaNexus.
	 */
	public GrappaPathIterator(GrappaNexus shape) {
		this(shape, null);
	}

	/**
	 * Constructs a new <code>GrappaPathIterator</code> given a GrappaNexus and
	 * an optional AffineTransform.
	 */
	public GrappaPathIterator(GrappaNexus shape, AffineTransform at) {
		if (shape == null) {
			throw new IllegalArgumentException("shape cannot be null");
		}
		this.grappaNexus = shape;
		this.affine = at;
		if (shape.shape != null) {
			shapeIterator = shape.shape.getPathIterator(this.affine);
			if (shapeIterator.isDone()) {
				shapeIterator = null;
			}
		}
		if (shape.textArea != null
				&& (Grappa.shapeClearText || shape.clearText)) {
			areaIterator = shape.textArea.getPathIterator(this.affine);
			if (areaIterator.isDone()) {
				areaIterator = null;
			}
		}
		if (shapeIterator != null) {
			type = shapeIterator.currentSegment(pts);
		} else if (areaIterator != null) {
			type = areaIterator.currentSegment(pts);
		} else {
			throw new RuntimeException(
					"cannot initialize; nothing to iterate over");
		}
	}

	// //////////////////////////////////////////////////////////////////////
	//
	// PathIterator interface
	//
	// //////////////////////////////////////////////////////////////////////

	public int currentSegment(double[] coords) {
		System.arraycopy(pts, 0, coords, 0, 6);
		return (type);
	}

	public int currentSegment(float[] coords) {
		coords[0] = (float) pts[0];
		coords[1] = (float) pts[1];
		coords[2] = (float) pts[2];
		coords[3] = (float) pts[3];
		coords[4] = (float) pts[4];
		coords[5] = (float) pts[5];
		return (type);
	}

	/**
	 * Return the winding rule for determining the interior of the path.
	 */
	public int getWindingRule() {
		return (grappaNexus.getWindingRule());
	}

	public boolean isDone() {
		return ((shapeIterator == null || shapeIterator.isDone()) && (areaIterator == null || areaIterator
				.isDone()));
	}

	public void next() {
		if (shapeIterator != null) {
			if (shapeIterator.isDone()) {
				shapeIterator = null;
			} else {
				shapeIterator.next();
				if (shapeIterator.isDone()) {
					shapeIterator = null;
				} else {
					type = shapeIterator.currentSegment(pts);
				}
				return;
			}
		}
		if (areaIterator != null) {
			if (areaIterator.isDone()) {
				areaIterator = null;
			} else {
				areaIterator.next();
				if (areaIterator.isDone()) {
					areaIterator = null;
				} else {
					type = areaIterator.currentSegment(pts);
				}
				return;
			}
		}
	}

}
