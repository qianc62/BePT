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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * A class used for drawing the graph.
 * 
 * @version 1.2, 07 Jul 2003; Copyright 1996 - 2003 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a
 *         href="http://www.research.att.com">Research @ AT&T Labs</a>
 */
public class GrappaPanel extends javax.swing.JPanel implements
		att.grappa.GrappaConstants, ComponentListener, AncestorListener,
		PopupMenuListener, MouseListener, MouseMotionListener, Printable,
		Scrollable {

	private Graph graph;
	Subgraph subgraph;
	GrappaBacker backer;
	boolean nodeLabels, edgeLabels, subgLabels;
	AffineTransform transform = null;
	AffineTransform oldTransform = null;
	AffineTransform inverseTransform = null;
	Vector elementVector = null;
	int nextElement = -1;
	boolean scaleToFit = false;
	GrappaSize scaleToSize = null;
	GrappaListener grappaListener = null;

	private Element pressedElement = null;
	private GrappaPoint pressedPoint = null;
	private int pressedModifiers = 0;
	private GrappaStyle selectionStyle = null;
	private GrappaStyle deletionStyle = null;
	private double scaleFactor = 1;
	private double scaleInfo = 1;
	private GrappaBox outline = null;
	private GrappaBox savedOutline = null;
	private boolean inMenu = false;
	private boolean scaleChanged = false;

	private JSlider slider;
	private JPanel graphPanel;
	private GrappaGraphPanel grappaGraphPanel;
	private JPanel rightPanel;
	private JScrollPane scrollPane;
	private GrappaPanel preview;
	private JLabel previewLabel;
	private GrappaBox rectangle;
	private Point2D realPressedPoint;
	private Point2D realDragStartPoint;
	private Point2D realDragEndPoint;
	private Point2D realReleasedPoint;
	private boolean firstPaint = true;
	private GrappaPanel parentOfPreview = null;

	protected JSplitPane split;
	protected JSplitPane vsplit;

	/**
	 * Constructs a new canvas associated with a particular subgraph. Keep in
	 * mind that Graph is a sub-class of Subgraph so that usually a Graph object
	 * is passed to the constructor.
	 * 
	 * @param subgraph
	 *            the subgraph to be rendered on the canvas
	 */
	public GrappaPanel(Subgraph subgraph) {
		this(subgraph, null);
	}

	/**
	 * Constructs a new canvas associated with a particular subgraph.
	 * 
	 * @param subgraph
	 *            the subgraph to be rendered on the canvas.
	 * @param backer
	 *            used to draw a background for the graph.
	 */
	public GrappaPanel(Subgraph subgraph, GrappaBacker backer) {
		this(subgraph, backer, true);
	}

	/**
	 * Constructs a new canvas associated with a particular subgraph.
	 * 
	 * @param subgraph
	 *            the subgraph to be rendered on the canvas.
	 * @param backer
	 *            used to draw a background for the graph.
	 */
	public GrappaPanel(Subgraph subgraph, GrappaBacker backer, boolean top) {
		super();
		this.subgraph = subgraph;
		this.backer = backer;
		this.graph = subgraph.getGraph();

		addAncestorListener(this);
		addComponentListener(this);

		selectionStyle = (GrappaStyle) (graph
				.getGrappaAttributeValue(GRAPPA_SELECTION_STYLE_ATTR));
		deletionStyle = (GrappaStyle) (graph
				.getGrappaAttributeValue(GRAPPA_DELETION_STYLE_ATTR));

		if (top) {
			grappaGraphPanel = new GrappaGraphPanel();
		} else {
			grappaGraphPanel = new GrappaGraphPanel() {
				public final boolean getScrollableTracksViewportWidth() {
					return (true);
				}

				public final boolean getScrollableTracksViewportHeight() {
					return (true);
				}

				protected boolean isPreview() {
					return true;
				}
			};
		}
		grappaGraphPanel.setBorder(BorderFactory.createEmptyBorder());
		graphPanel = new JPanel(new BorderLayout()) {
			public String getToolTipText(MouseEvent mev) {
				return grappaGraphPanel.getToolTipText(mev);
			}
		};
		graphPanel.setBorder(BorderFactory.createEmptyBorder());

		setLayout(new BorderLayout());
		graphPanel.add(grappaGraphPanel, BorderLayout.CENTER);

		scrollPane = new JScrollPane(graphPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		if (!top) {
			scrollPane
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			add(scrollPane, BorderLayout.CENTER);
			scrollPane.setAutoscrolls(true);
			PreviewMouseHandler previewMouseHandler = new PreviewMouseHandler();
			scrollPane.addMouseMotionListener(previewMouseHandler);
			scrollPane.addMouseListener(previewMouseHandler);
		} else {
			scrollPane
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.getHorizontalScrollBar().addAdjustmentListener(
					new AdjustmentListener() {
						public void adjustmentValueChanged(AdjustmentEvent e) {
							// Message.add("Horizontal scrollbar value changed",
							// Message.DEBUG);
							repaintPreview();
						}
					});
			scrollPane.getVerticalScrollBar().addAdjustmentListener(
					new AdjustmentListener() {
						public void adjustmentValueChanged(AdjustmentEvent e) {
							// Message.add("Vertical scrollbar value changed",
							// Message.DEBUG);
							repaintPreview();
						}
					});
		}
		slider = new JSlider(JSlider.VERTICAL, 50, 1200, 100);
		slider.setOpaque(false);

		if (top) {
			rightPanel = new JPanel(new BorderLayout());
			rightPanel.setBorder(BorderFactory.createEmptyBorder());
			slider.setMinorTickSpacing(50);
			slider.setPaintTicks(true);

			slider.addChangeListener(new ChangeListener() {
				private int lastValue = slider.getValue();

				public void stateChanged(ChangeEvent e) {
					// if ((slider.getValue() % slider.getMinorTickSpacing() ==
					// 0) ||
					// (slider.getValue() % slider.getMinorTickSpacing() == 1)
					// ||
					// (slider.getValue() % slider.getMinorTickSpacing() ==
					// slider.getMinorTickSpacing() - 1)
					// ) {
					int v = slider.getValue();
					if (v != lastValue) {
						setScaleToFit(false);
						setScaleToSize(null);
						lastValue = v;
						setScaleFactor((double) v / (double) 100);
						if (previewLabel != null) {
							previewLabel.setText("Zoom: " + slider.getValue()
									+ " %");
						}
					}
				}
			});
			setScaleToFit(true);
			previewLabel = new JLabel("" + slider.getValue(), JLabel.CENTER);
			previewLabel.setFont(previewLabel.getFont().deriveFont(10.0f));
			previewLabel.setBorder(BorderFactory.createEmptyBorder(3, 1, 5, 1));
			previewLabel.setOpaque(false);
			preview = new GrappaPanel(subgraph, null, false);
			preview.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));
			preview.parentOfPreview = this;
			preview.setScaleToFit(true);
			preview.setPreferredSize(new Dimension(90, 90));
			preview.setSize(new Dimension(90, 90));
			preview.setMinimumSize(new Dimension((int) slider.getMinimumSize()
					.getWidth(), (int) slider.getMinimumSize().getWidth()));
			vsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, preview,
					slider);
			vsplit.setBorder(BorderFactory.createEmptyBorder());
			vsplit.setResizeWeight(0);
			vsplit.setOneTouchExpandable(true);
			rightPanel.add(vsplit);
			rightPanel.add(previewLabel, BorderLayout.SOUTH);

			split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
					scrollPane, rightPanel);
			split.setBorder(BorderFactory.createEmptyBorder());
			split.setResizeWeight(1);
			split.setOneTouchExpandable(true);
			add(split);
		}
		transform = new AffineTransform();
		try {
			inverseTransform = transform.createInverse();
		} catch (NoninvertibleTransformException nite) {
			inverseTransform = null;
		}
	}

	/**
	 * Adds the specified listener to receive mouse events from this graph.
	 * 
	 * @param listener
	 *            the event listener.
	 * @return the previous event listener.
	 * 
	 * @see GrappaAdapter
	 */
	public GrappaListener addGrappaListener(GrappaListener listener) {
		GrappaListener oldGL = grappaListener;

		// PVDB following two lines added to solve bug with adding multiple
		// Listeners to one instance
		graphPanel.removeMouseListener(this);
		graphPanel.removeMouseMotionListener(this);

		grappaListener = listener;
		if (grappaListener == null) {
			scrollPane.addMouseListener(null);
			graphPanel.addMouseListener(null);
			graphPanel.addMouseMotionListener(null);
			graphPanel.setToolTipText(null);
		} else {
			scrollPane.addMouseListener(this);
			graphPanel.addMouseListener(this);
			graphPanel.addMouseMotionListener(this);
			String tip = graph.getToolTipText();
			if (tip == null) {
				tip = Grappa.getToolTipText();
			}
			graphPanel.setToolTipText(tip);
		}
		return (oldGL);
	}

	/**
	 * Removes the current listener from this graph. Equivalent to
	 * <TT>addGrappaListener(null)</TT>.
	 * 
	 * @return the event listener just removed.
	 */
	public GrappaListener removeGrappaListener() {
		return (addGrappaListener(null));
	}

	public void setBackgroundColor(Color color) {
		super.setBackground(color);
		graphPanel.setBackground(color);
		grappaGraphPanel.setBackground(color);
		preview.setBackground(color);
		rightPanel.setBackground(color);
		scrollPane.setBackground(color);
		split.setBackground(color);
		vsplit.setBackground(color);
	}

	public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
		GrappaSize prevToSize = scaleToSize;
		boolean prevToFit = scaleToFit;
		double prevScale = scaleFactor;

		if (pi >= 1) {
			return Printable.NO_SUCH_PAGE;
		}
		try {
			scaleToFit = false;
			scaleToSize = new GrappaSize(pf.getImageableWidth(), pf
					.getImageableHeight());
			((Graphics2D) g).translate(pf.getImageableX(), pf.getImageableY());
			// graphPanel.paint(g);
			// paintComponent(g);
			componentPaint(g, false);
		} finally {
			scaleToSize = prevToSize;
			scaleToFit = prevToFit;
			scaleFactor = prevScale;
			repaint();
		}
		return Printable.PAGE_EXISTS;
	}

	// private int paint_i=0;

	private void componentPaint(Graphics g, boolean isPreview) {

		if (subgraph == null || !subgraph.reserve()) {
			return;
		}

		Graphics2D g2d = (Graphics2D) g;
		// Message.add("painting"+paint_i++);
		Container prnt;

		// Color origBackground = g2d.getBackground();
		// //Composite origComposite = g2d.getComposite();
		// Paint origPaint = g2d.getPaint();
		// RenderingHints origRenderingHints = g2d.getRenderingHints();
		// Stroke origStroke = g2d.getStroke();
		// AffineTransform origAffineTransform = g2d.getTransform();
		// Font origFont = g2d.getFont();

		elementVector = null;

		GrappaBox bbox = new GrappaBox(subgraph.getBoundingBox());

		if (bbox == null) {
			return;
		}

		GrappaSize margins = (GrappaSize) (subgraph
				.getAttributeValue(MARGIN_ATTR));

		if (margins != null) {
			double x_margin = PointsPerInch * margins.width;
			double y_margin = PointsPerInch * margins.height;

			bbox.x -= x_margin;
			bbox.y -= y_margin;
			bbox.width += 2.0 * x_margin;
			bbox.height += 2.0 * y_margin;
		}

		subgLabels = subgraph.getShowSubgraphLabels();
		nodeLabels = subgraph.getShowNodeLabels();
		edgeLabels = subgraph.getShowEdgeLabels();
		if (Grappa.useAntiAliasing) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		} else {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		if (Grappa.antiAliasText) {
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		} else {
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
		if (Grappa.useFractionalMetrics) {
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		}
		g2d.setStroke(GrappaStyle.defaultStroke);

		oldTransform = transform;
		transform = new AffineTransform();
		if (scaleToFit || scaleToSize != null) {
			scaleFactor = 1;
			double scaleToWidth = 0;
			double scaleToHeight = 0;
			if (scaleToFit) {
				Dimension sz = scrollPane.getViewport().getSize();
				scaleToWidth = sz.width;
				scaleToHeight = sz.height;

			} else {
				scaleToWidth = scaleToSize.width;
				scaleToHeight = scaleToSize.height;
			}
			double widthRatio = scaleToWidth / bbox.getWidth();
			double heightRatio = scaleToHeight / bbox.getHeight();
			double xTranslate = 0;
			double yTranslate = 0;
			if (widthRatio < heightRatio) {
				xTranslate = (scaleToWidth - widthRatio * bbox.getWidth())
						/ (2.0 * widthRatio);
				yTranslate = (scaleToHeight - widthRatio * bbox.getHeight())
						/ (2.0 * widthRatio);
				transform.scale(widthRatio, widthRatio);
				scaleInfo = widthRatio;
			} else {
				xTranslate = (scaleToWidth - heightRatio * bbox.getWidth())
						/ (2.0 * heightRatio);
				yTranslate = (scaleToHeight - heightRatio * bbox.getHeight())
						/ (2.0 * heightRatio);
				transform.scale(heightRatio, heightRatio);
				scaleInfo = heightRatio;
			}
			transform.translate(xTranslate, yTranslate);
			graphPanel.setSize(new Dimension((int) Math.ceil(scaleToWidth),
					(int) Math.ceil(scaleToHeight)));
			graphPanel.setPreferredSize(new Dimension((int) Math
					.ceil(scaleToWidth), (int) Math.ceil(scaleToHeight)));
			transform.translate(-bbox.getMinX(), -bbox.getMinY());
			scaleFactor = scaleInfo;
			slider.setValue((int) (scaleFactor * 100));
			if (!isPreview) {
				previewLabel.setText("Zoom: " + slider.getValue() + " %");
			}
			repaintPreview();
		} else if (scaleFactor != 1) {
			// Rectangle r = graphPanel.getVisibleRect();

			Rectangle r = new Rectangle(scrollPane.getViewport()
					.getViewPosition().x, scrollPane.getViewport()
					.getViewPosition().y, scrollPane.getViewport()
					.getExtentSize().width, scrollPane.getViewport()
					.getExtentSize().height);

			Point2D cpt = null;
			prnt = null;

			if (scaleChanged) {
				prnt = graphPanel.getParent();
				if (prnt instanceof javax.swing.JViewport
						&& inverseTransform != null) {
					Point2D pt = new Point2D.Double(r.x, r.y);
					cpt = new Point2D.Double(r.x + r.width, r.y + r.height);
					inverseTransform.transform(pt, pt);
					inverseTransform.transform(cpt, cpt);
					cpt.setLocation(pt.getX() + (cpt.getX() - pt.getX()) / 2.,
							pt.getY() + (cpt.getY() - pt.getY()) / 2.);
				} else {
					// to save checking again below
					prnt = null;
				}
				scaleChanged = false;

			}

			transform.scale(scaleFactor, scaleFactor);
			scaleInfo = scaleFactor;
			int w = (int) Math.ceil(bbox.getWidth() * scaleFactor);
			int h = (int) Math.ceil(bbox.getHeight() * scaleFactor);
			w = w < r.width ? r.width : w;
			h = h < r.height ? r.height : h;
			graphPanel.setSize(new Dimension(w, h));
			graphPanel.setPreferredSize(new Dimension(w, h));
			transform.translate(-bbox.getMinX(), -bbox.getMinY());

			if (prnt != null) {
				javax.swing.JViewport viewport = (javax.swing.JViewport) prnt;
				transform.transform(cpt, cpt);
				Dimension viewsize = viewport.getExtentSize();
				Point p = new Point(
						Math
								.max(
										0,
										(int) (cpt.getX() - ((double) viewsize.width) / 2.)),
						Math
								.max(
										0,
										(int) (cpt.getY() - ((double) viewsize.height) / 2.)));

				viewport.setViewPosition(p);
			}
		} else {
			Point2D cpt = null;
			prnt = null;

			if (scaleChanged) {
				prnt = graphPanel.getParent();
				if (prnt instanceof javax.swing.JViewport
						&& inverseTransform != null) {
					Rectangle r = graphPanel.getVisibleRect();
					Point2D pt = new Point2D.Double(r.x, r.y);
					cpt = new Point2D.Double(r.x + r.width, r.y + r.height);
					inverseTransform.transform(pt, pt);
					inverseTransform.transform(cpt, cpt);
					cpt.setLocation(pt.getX() + (cpt.getX() - pt.getX()) / 2.,
							pt.getY() + (cpt.getY() - pt.getY()) / 2.);
				} else {
					// to save checking again below
					prnt = null;
				}
				scaleChanged = false;
			}
			scaleInfo = 1;
			graphPanel.setSize(new Dimension((int) Math.ceil(bbox.getWidth()),
					(int) Math.ceil(bbox.getHeight())));
			graphPanel.setPreferredSize(new Dimension((int) Math.ceil(bbox
					.getWidth()), (int) Math.ceil(bbox.getHeight())));
			transform.translate(-bbox.getMinX(), -bbox.getMinY());

			if (prnt != null) {
				javax.swing.JViewport viewport = (javax.swing.JViewport) prnt;
				transform.transform(cpt, cpt);
				Dimension viewsize = viewport.getExtentSize();
				Point p = new Point(
						Math
								.max(
										0,
										(int) (cpt.getX() - ((double) viewsize.width) / 2.)),
						Math
								.max(
										0,
										(int) (cpt.getY() - ((double) viewsize.height) / 2.)));

				viewport.setViewPosition(p);
			}
		}

		if (scaleInfo < Grappa.nodeLabelsScaleCutoff) {
			nodeLabels = false;
		}

		if (scaleInfo < Grappa.edgeLabelsScaleCutoff) {
			edgeLabels = false;
		}

		if (scaleInfo < Grappa.subgLabelsScaleCutoff) {
			subgLabels = false;
		}

		try {
			inverseTransform = transform.createInverse();
		} catch (NoninvertibleTransformException nite) {
			inverseTransform = null;
		}
		g2d.transform(transform);

		Rectangle clip = g2d.getClipBounds();
		// grow bounds to account for Java's frugal definition of what
		// constitutes the intersectable area of a shape
		clip.x--;
		clip.y--;
		clip.width += 2;
		clip.height += 2;

		synchronized (graph) {

			GrappaNexus grappaNexus = subgraph.grappaNexus;

			if (grappaNexus != null) {

				Color bkgdColor = null;

				// do fill now in case there is a Backer supplied
				g2d
						.setPaint(bkgdColor = (Color) (graph
								.getGrappaAttributeValue(GRAPPA_BACKGROUND_COLOR_ATTR)));
				g2d.fill(clip);
				if (grappaNexus.style.filled || grappaNexus.image != null) {
					if (grappaNexus.style.filled) {
						g2d.setPaint(bkgdColor = grappaNexus.fillcolor);
						grappaNexus.fill(g2d);
						// g2d.setPaint(grappaNexus.style.line_color);
						g2d.setPaint(grappaNexus.color);
					}
					grappaNexus.drawImage(g2d);
					// for the main graph, only outline when filling/imaging
					if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
						g2d.setStroke(grappaNexus.style.stroke);
						grappaNexus.draw(g2d);
						g2d.setStroke(GrappaStyle.defaultStroke);
					} else {
						grappaNexus.draw(g2d);
					}
				}

				if (backer != null && Grappa.backgroundDrawing) {
					backer.drawBackground(g2d, graph, bbox, clip);
				}

				paintSubgraph(g2d, subgraph, clip, bkgdColor, isPreview);

			}

		}
		if (rectangle != null) {
			g2d.setPaint(Color.RED);
			g2d.setStroke(new BasicStroke(5));
			g2d.draw(rectangle);
		}
		if (preview == null) {
			g2d.setPaint(Color.BLACK);
			g2d.setStroke(new BasicStroke(2));
			g2d.draw(bbox);
		}
		// g2d.setBackground(origBackground);
		// //g2d.setComposite(origComposite);
		// g2d.setPaint(origPaint);
		// g2d.setRenderingHints(origRenderingHints);
		// g2d.setStroke(origStroke);
		// g2d.setTransform(origAffineTransform);
		// g2d.setFont(origFont);
		subgraph.release();
		if ((preview != null) && firstPaint) {
			repaintPreview();
		}
		firstPaint = false;

	}

	/**
	 * Get the AffineTransform that applies to this drawing.
	 * 
	 * @return the AffineTransform that applies to this drawing.
	 */
	public AffineTransform getTransform() {
		return (AffineTransform) (transform.clone());
	}

	/**
	 * Get the inverse AffineTransform that applies to this drawing.
	 * 
	 * @return the inverse AffineTransform that applies to this drawing.
	 */
	public AffineTransform getInverseTransform() {
		return inverseTransform;
	}

	/**
	 * Registers the default text to display in a tool tip. Setting the default
	 * text to null turns off tool tips. The default text is displayed when the
	 * mouse is outside the graph boundaries, but within the panel.
	 * 
	 * @see Graph#setToolTipText(String)
	 */
	public void setToolTipText(String tip) {
		// System.err.println("tip set to: " + tip);
		super.setToolTipText(tip);
	}

	/**
	 * Generate an appropriate tooltip based on the mouse location provided by
	 * the given event.
	 * 
	 * @return if a GrappaListener is available, the result of its
	 *         <TT>grappaTip()</TT> method is returned, otherwise null.
	 * 
	 * @see GrappaPanel#setToolTipText(String)
	 */
	public String getToolTipText(MouseEvent mev) {
		if (inverseTransform == null || grappaListener == null) {
			return (null);
		}
		// System.err.println("tip requested");

		Point2D pt = inverseTransform.transform(mev.getPoint(), null);
		return (grappaListener
				.grappaTip(subgraph, findContainingElement(subgraph,
						inverseTransform.transform(mev.getPoint(), null)),
						new GrappaPoint(pt.getX(), pt.getY()), mev
								.getModifiers(), this));
	}

	/**
	 * Enable/disable scale-to-fit mode.
	 * 
	 * @param setting
	 *            if true, the graph drawing is scaled to fit the panel,
	 *            otherwise the graph is drawn full-size.
	 */
	public void setScaleToFit(boolean setting) {
		scaleToFit = setting;
		invalidate();
		repaint();
	}

	/**
	 * Scale the graph drawing to a specific size.
	 */
	public void setScaleToSize(Dimension2D scaleSize) {

		if (scaleSize == null) {
			scaleToSize = null;
		} else {
			scaleToSize = new GrappaSize(scaleSize.getWidth(), scaleSize
					.getHeight());
		}
	}

	/**
	 * Get the subgraph being drawn on this panel.
	 * 
	 * @return the subgraph being drawn on this panel.
	 */
	public Subgraph getSubgraph() {
		return (subgraph);
	}

	/**
	 * Reset the scale factor to one.
	 */
	public void resetZoom() {
		scaleChanged = scaleFactor != 1;
		scaleFactor = 1;
		slider.setValue((int) (scaleFactor * 100));
	}

	/**
	 * Check if a swept outline is still available.
	 * 
	 * @return true if there is an outline available.
	 */
	public boolean hasOutline() {
		return (savedOutline != null);
	}

	/**
	 * Clear swept outline, if any.
	 */
	public void clearOutline() {
		savedOutline = null;
	}

	public void clearGrappaPanel() {
		this.removeAll();
		backer = null;
		deletionStyle = null;
		elementVector = null;
		graph = null;
		if (graphPanel != null) {
			graphPanel.removeAll();
			graphPanel = null;
		}
		grappaGraphPanel = null;
		grappaListener = null;
		inverseTransform = null;
		oldTransform = null;
		outline = null;
		if (parentOfPreview != null) {
			parentOfPreview = null;
		}
		pressedElement = null;
		pressedPoint = null;
		if (preview != null) {
			preview = null;
		}
		previewLabel = null;
		realDragEndPoint = null;
		realDragStartPoint = null;
		realPressedPoint = null;
		realReleasedPoint = null;
		rectangle = null;
		if (rightPanel != null) {
			rightPanel.removeAll();
			rightPanel = null;
		}
		savedOutline = null;
		scaleToSize = null;
		if (scrollPane != null) {
			scrollPane.removeAll();
			scrollPane = null;
		}
		selectionStyle = null;
		if (slider != null) {
			slider.removeAll();
			slider = null;
		}
		if (split != null) {
			split.removeAll();
			split = null;
		}
		subgraph = null;
		transform = null;
		if (vsplit != null) {
			vsplit.removeAll();
			vsplit = null;
		}
	}

	/**
	 * Zoom the drawing to the outline just swept with the mouse, if any.
	 * 
	 * @return the box corresponding to the swept outline, or null.
	 */
	public GrappaBox zoomToOutline() {
		if (savedOutline != null) {

			double hratio = Math.abs(((double) grappaGraphPanel.getWidth())
					/ (double) (realDragStartPoint.getX() - realDragEndPoint
							.getX()));
			double vratio = Math.abs(((double) grappaGraphPanel.getHeight())
					/ (double) (realDragStartPoint.getY() - realDragEndPoint
							.getY()));
			setScaleFactor(Math.min(hratio, vratio));
			scrollPane.getViewport().setViewPosition(
					new Point((int) (scaleFactor * Math.min(realDragStartPoint
							.getX(), realDragEndPoint.getX())),
							(int) (scaleFactor * Math.min(realDragStartPoint
									.getY(), realDragEndPoint.getY()))));
			savedOutline = null;
		}
		return (null);
	}

	/**
	 * Adjust the scale factor by the supplied multiplier.
	 * 
	 * @param multiplier
	 *            multiply the scale factor by this amount.
	 * @return the value of the previous scale factor.
	 */
	public double multiplyScaleFactor(double multiplier) {
		return setScaleFactor(scaleFactor * multiplier);
	}

	/**
	 * Adjust the scale factor by the supplied multiplier.
	 * 
	 * @param factor
	 *            set the scale factor to this amount.
	 * @return the value of the previous scale factor.
	 */
	public double setScaleFactor(double factor) {
		double old = scaleFactor;
		scaleFactor = factor;
		if (scaleFactor == 0) {
			scaleFactor = old;
		}
		scaleChanged = scaleFactor != old;
		slider.setValue((int) (scaleFactor * 100));
		if (scaleChanged) {
			clearOutline();
			graphPanel.repaint(scrollPane.getViewportBorderBounds());
			repaintPreview();
			// previewLabel.setText("Zoom: " + slider.getValue() + " %");
		}

		return (old);
	}

	private void repaintPreview() {
		if ((preview != null) && (inverseTransform != null)) {
			Point2D tl = inverseTransform.transform(new Point(graphPanel
					.getVisibleRect().x, graphPanel.getVisibleRect().y), null);
			Point2D wh = inverseTransform.transform(new Point(graphPanel
					.getVisibleRect().width
					+ graphPanel.getVisibleRect().x, graphPanel
					.getVisibleRect().height
					+ graphPanel.getVisibleRect().y), null);
			GrappaBox box = new GrappaBox(tl.getX(), tl.getY(), (wh.getX() - tl
					.getX()), (wh.getY() - tl.getY()));
			preview.setRect(box);
			preview.repaint();
		}
	}

	private void setRect(GrappaBox rectangle) {
		this.rectangle = rectangle;
	}

	// //////////////////////////////////////////////////////////////////////
	//
	// Private methods
	//
	// //////////////////////////////////////////////////////////////////////

	private void paintSubgraph(Graphics2D g2d, Subgraph subg, Shape clipper,
			Color bkgdColor, boolean isPreview) {
		if (subg != subgraph && !subg.reserve()) {
			return;
		}

		Rectangle2D bbox = subg.getBoundingBox();
		GrappaNexus grappaNexus = subg.grappaNexus;

		if (bbox != null && grappaNexus != null && subg.visible
				&& !grappaNexus.style.invis && clipper.intersects(bbox)) {

			Enumeration enu = null;

			int i;

			if (subg != subgraph) {
				g2d.setPaint(grappaNexus.color);
				if (grappaNexus.style.filled) {
					g2d.setPaint(bkgdColor = grappaNexus.fillcolor);
					grappaNexus.fill(g2d);
					g2d.setPaint(grappaNexus.color);
					// g2d.setPaint(grappaNexus.style.line_color);
				} else if (grappaNexus.color == bkgdColor) { // using == is OK
					// (caching)
					g2d.setPaint(grappaNexus.color);
					// g2d.setPaint(grappaNexus.style.line_color);
				}
				grappaNexus.drawImage(g2d);
				if (subg.isCluster() || Grappa.outlineSubgraphs) {
					if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
						g2d.setStroke(grappaNexus.style.stroke);
						grappaNexus.draw(g2d);
						g2d.setStroke(GrappaStyle.defaultStroke);
					} else {
						grappaNexus.draw(g2d);
					}
				}
			}

			if ((subg.highlight & DELETION_MASK) == DELETION_MASK) {
				g2d.setPaint(deletionStyle.line_color);
				if (GrappaStyle.defaultStroke != deletionStyle.stroke) {
					g2d.setStroke(deletionStyle.stroke);
					grappaNexus.draw(g2d);
					g2d.setStroke(GrappaStyle.defaultStroke);
				} else {
					grappaNexus.draw(g2d);
				}
			} else if ((subg.highlight & SELECTION_MASK) == SELECTION_MASK) {
				g2d.setPaint(selectionStyle.line_color);
				if (GrappaStyle.defaultStroke != selectionStyle.stroke) {
					g2d.setStroke(selectionStyle.stroke);
					grappaNexus.draw(g2d);
					g2d.setStroke(GrappaStyle.defaultStroke);
				} else {
					grappaNexus.draw(g2d);
				}
			}

			if (grappaNexus.lstr != null && subgLabels && !isPreview) {
				g2d.setFont(grappaNexus.font);
				g2d.setPaint(grappaNexus.font_color);
				for (i = 0; i < grappaNexus.lstr.length; i++) {
					g2d.drawString(grappaNexus.lstr[i],
							(int) grappaNexus.lpos[i].x,
							(int) grappaNexus.lpos[i].y);
				}
			}

			enu = subg.subgraphElements();
			Subgraph subsubg = null;
			while (enu.hasMoreElements()) {
				subsubg = (Subgraph) (enu.nextElement());
				if (subsubg != null) {
					paintSubgraph(g2d, subsubg, clipper, bkgdColor, isPreview);
				}
			}
			Node node;
			enu = subg.nodeElements();
			while (enu.hasMoreElements()) {
				node = (Node) (enu.nextElement());
				if (node == null || !node.reserve()) {
					continue;
				}
				if ((grappaNexus = node.grappaNexus) != null && node.visible
						&& !grappaNexus.style.invis
						&& clipper.intersects(grappaNexus.rawBounds2D())) {
					g2d.setPaint(grappaNexus.color);
					if (grappaNexus.style.filled) {
						g2d.setPaint(grappaNexus.fillcolor);
						grappaNexus.fill(g2d);
						g2d.setPaint(grappaNexus.color);
						// g2d.setPaint(grappaNexus.style.line_color);
					}
					grappaNexus.drawImage(g2d);
					if ((node.highlight & DELETION_MASK) == DELETION_MASK) {
						g2d.setPaint(deletionStyle.line_color);
						if (GrappaStyle.defaultStroke != deletionStyle.stroke) {
							g2d.setStroke(deletionStyle.stroke);
							grappaNexus.draw(g2d);
							g2d.setStroke(GrappaStyle.defaultStroke);
						} else {
							grappaNexus.draw(g2d);
						}
					} else if ((node.highlight & SELECTION_MASK) == SELECTION_MASK) {
						g2d.setPaint(selectionStyle.line_color);
						if (GrappaStyle.defaultStroke != selectionStyle.stroke) {
							g2d.setStroke(selectionStyle.stroke);
							grappaNexus.draw(g2d);
							g2d.setStroke(GrappaStyle.defaultStroke);
						} else {
							grappaNexus.draw(g2d);
						}
					} else {
						if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
							g2d.setStroke(grappaNexus.style.stroke);
							grappaNexus.draw(g2d);
							g2d.setStroke(GrappaStyle.defaultStroke);
						} else {
							grappaNexus.draw(g2d);
						}
					}
					if (grappaNexus.lstr != null && nodeLabels && !isPreview) {
						g2d.setFont(grappaNexus.font);
						g2d.setPaint(grappaNexus.font_color);
						for (i = 0; i < grappaNexus.lstr.length; i++) {
							g2d.drawString(grappaNexus.lstr[i],
									(int) grappaNexus.lpos[i].x,
									(int) grappaNexus.lpos[i].y);
						}
					}
				}
				node.release();
			}

			Edge edge;
			enu = subg.edgeElements();
			while (enu.hasMoreElements()) {
				edge = (Edge) (enu.nextElement());
				if (edge == null || !edge.reserve()) {
					continue;
				}
				if ((grappaNexus = edge.grappaNexus) != null && edge.visible
						&& !grappaNexus.style.invis
						&& clipper.intersects(grappaNexus.rawBounds2D())) {
					grappaNexus.drawImage(g2d);
					if ((edge.highlight & DELETION_MASK) == DELETION_MASK) {
						g2d.setPaint(deletionStyle.line_color);
						grappaNexus.fill(g2d);
						if (GrappaStyle.defaultStroke != deletionStyle.stroke) {
							g2d.setStroke(deletionStyle.stroke);
							grappaNexus.draw(g2d);
							g2d.setStroke(GrappaStyle.defaultStroke);
						} else {
							grappaNexus.draw(g2d);
						}
					} else if ((edge.highlight & SELECTION_MASK) == SELECTION_MASK) {
						g2d.setPaint(selectionStyle.line_color);
						grappaNexus.fill(g2d);
						if (GrappaStyle.defaultStroke != selectionStyle.stroke) {
							g2d.setStroke(selectionStyle.stroke);
							grappaNexus.draw(g2d);
							g2d.setStroke(GrappaStyle.defaultStroke);
						} else {
							grappaNexus.draw(g2d);
						}
					} else {
						g2d.setPaint(grappaNexus.color);
						grappaNexus.fill(g2d);
						if (GrappaStyle.defaultStroke != grappaNexus.style.stroke) {
							g2d.setStroke(grappaNexus.style.stroke);
							grappaNexus.draw(g2d);
							g2d.setStroke(GrappaStyle.defaultStroke);
						} else {
							grappaNexus.draw(g2d);
						}
					}
					if (grappaNexus.lstr != null && edgeLabels && !isPreview) {
						g2d.setFont(grappaNexus.font);
						g2d.setPaint(grappaNexus.font_color);
						for (i = 0; i < grappaNexus.lstr.length; i++) {
							g2d.drawString(grappaNexus.lstr[i],
									(int) grappaNexus.lpos[i].x,
									(int) grappaNexus.lpos[i].y);
						}
					}
				}
				edge.release();
			}
		}
		subg.release();
	}

	private Element findContainingElement(Subgraph subg, Point2D pt) {
		return (findContainingElement(subg, pt, null));
	}

	private Element findContainingElement(Subgraph subg, Point2D pt,
			Element crnt) {
		Element elem;
		Element[] stash = new Element[2];

		stash[0] = crnt;
		stash[1] = null;

		if ((elem = reallyFindContainingElement(subg, pt, stash)) == null) {
			elem = stash[1];
		}
		return (elem);
	}

	private Element reallyFindContainingElement(Subgraph subg, Point2D pt,
			Element[] stash) {

		Enumeration enu;

		Rectangle2D bb = subg.getBoundingBox();

		GrappaNexus grappaNexus = null;

		if (bb.contains(pt)) {

			if ((Grappa.elementSelection & EDGE) == EDGE) {
				enu = subg.edgeElements();
				Edge edge;
				while (enu.hasMoreElements()) {
					edge = (Edge) enu.nextElement();
					if ((grappaNexus = edge.grappaNexus) == null) {
						continue;
					}
					if (grappaNexus.rawBounds2D().contains(pt)) {
						if (grappaNexus.contains(pt.getX(), pt.getY())) {
							if (stash[0] == null) {
								return ((Element) edge);
							}
							if (stash[1] == null) {
								stash[1] = edge;
							}
							if (stash[0] == edge) {
								stash[0] = null;
							}
						}
					}
				}
			}

			if ((Grappa.elementSelection & NODE) == NODE) {
				enu = subg.nodeElements();
				Node node;
				while (enu.hasMoreElements()) {
					node = (Node) enu.nextElement();
					if ((grappaNexus = node.grappaNexus) == null) {
						continue;
					}
					if (grappaNexus.rawBounds2D().contains(pt)) {
						if (grappaNexus.contains(pt)) {
							if (stash[0] == null) {
								return ((Element) node);
							}
							if (stash[1] == null) {
								stash[1] = node;
							}
							if (stash[0] == node) {
								stash[0] = null;
							}
						}
					}
				}
			}

			Element subelem = null;

			enu = subg.subgraphElements();
			while (enu.hasMoreElements()) {
				if ((subelem = reallyFindContainingElement((Subgraph) (enu
						.nextElement()), pt, stash)) != null) {
					if (stash[0] == null) {
						return (subelem);
					}
					if (stash[1] == null) {
						stash[1] = subelem;
					}
					if (stash[0] == subelem) {
						stash[0] = null;
					}
				}
			}

			if ((Grappa.elementSelection & SUBGRAPH) == SUBGRAPH) {
				if (stash[0] == null) {
					return ((Element) subg);
				}
				if (stash[1] == null) {
					stash[1] = subg;
				}
				if (stash[0] == subg) {
					stash[0] = null;
				}
			}
		}
		return (null);
	}

	// /////////////////////////////////////////////////////////////////
	//
	// AncestorListener Interface
	//
	// /////////////////////////////////////////////////////////////////

	public void ancestorMoved(AncestorEvent aev) {
		// don't care
	}

	public void ancestorAdded(AncestorEvent aev) {
		graph.addPanel(this);
	}

	public void ancestorRemoved(AncestorEvent aev) {
		graph.removePanel(this);
	}

	// /////////////////////////////////////////////////////////////////
	//
	// ComponentListener Interface
	//
	// /////////////////////////////////////////////////////////////////

	public void componentHidden(ComponentEvent cev) {
		// don't care
	}

	public void componentMoved(ComponentEvent cev) {
		// don't care
	}

	public void componentResized(ComponentEvent cev) {
		// Needed to reset JScrollPane scrollbars, for example
		revalidate();
	}

	public void componentShown(ComponentEvent cev) {
		// don't care
	}

	// /////////////////////////////////////////////////////////////////
	//
	// PopupMenuListener Interface
	//
	// /////////////////////////////////////////////////////////////////

	public void popupMenuCanceled(PopupMenuEvent pmev) {
		// don't care
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent pmev) {
		inMenu = false;
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent pmev) {
		inMenu = true;
	}

	// /////////////////////////////////////////////////////////////////
	//
	// MouseListener Interface
	//
	// /////////////////////////////////////////////////////////////////

	public void mouseClicked(MouseEvent mev) {
		if (inverseTransform == null || grappaListener == null || inMenu) {
			return;
		}
		int x = mev.getPoint().x;
		int y = mev.getPoint().y;
		if (mev.getSource() == graphPanel) {
			x += graphPanel.getLocation().x;
			y += graphPanel.getLocation().y;
		}

		Point2D pt = inverseTransform.transform(new Point(x, y), null);

		grappaListener
				.grappaClicked(
						subgraph,
						findContainingElement(
								subgraph,
								inverseTransform
										.transform(mev.getPoint(), null),
								(subgraph.currentSelection != null
										&& subgraph.currentSelection instanceof Element ? ((Element) subgraph.currentSelection)
										: null)), new GrappaPoint(pt.getX(), pt
								.getY()), mev.getModifiers(), mev
								.getClickCount(), this);
	}

	public void mousePressed(MouseEvent mev) {
		if (inverseTransform == null || grappaListener == null || inMenu) {
			return;
		}

		int x = mev.getPoint().x;
		int y = mev.getPoint().y;
		realPressedPoint = new Point2D.Double(x / scaleFactor, y / scaleFactor);
		if (mev.getSource() == graphPanel) {
			x += graphPanel.getLocation().x;
			y += graphPanel.getLocation().y;
		}

		Point2D pt = inverseTransform.transform(new Point(x, y), null);

		outline = null;

		grappaListener.grappaPressed(subgraph,
				(pressedElement = findContainingElement(subgraph,
						inverseTransform.transform(mev.getPoint(), null))),
				(pressedPoint = new GrappaPoint(pt.getX(), pt.getY())),
				(pressedModifiers = mev.getModifiers()), this);
	}

	public void mouseReleased(MouseEvent mev) {
		if (inverseTransform == null || grappaListener == null || inMenu) {
			return;
		}

		int modifiers = mev.getModifiers();

		int x = mev.getPoint().x;
		int y = mev.getPoint().y;
		int offx = 0;
		int offy = 0;
		if (mev.getSource() == graphPanel) {
			offx = graphPanel.getLocation().x;
			x += offx;
			offy = graphPanel.getLocation().y;
			y += offy;
		}

		realReleasedPoint = new Point2D.Double(mev.getPoint().x / scaleFactor,
				mev.getPoint().y / scaleFactor);
		Point2D pt = inverseTransform.transform(new Point(x, y), null);

		GrappaPoint gpt = new GrappaPoint(pt.getX(), pt.getY());

		double hgap = (pt.getX() - inverseTransform.transform(mev.getPoint(),
				null).getX());
		double vgap = (pt.getY() - inverseTransform.transform(mev.getPoint(),
				null).getY());

		GrappaBox gb = null;
		if (outline != null) {
			gb = new GrappaBox(outline.x - hgap, outline.y - vgap,
					outline.width, outline.height);
		}
		grappaListener.grappaReleased(subgraph, findContainingElement(subgraph,
				inverseTransform.transform(mev.getPoint(), null)), gpt,
				modifiers, pressedElement, pressedPoint, pressedModifiers, gb,
				this);

		if ((modifiers & java.awt.event.InputEvent.BUTTON1_MASK) != 0
				&& (modifiers & java.awt.event.InputEvent.BUTTON1_MASK) == modifiers) {
			if (outline != null) {
				// System.err.println("saving outline");
				savedOutline = GrappaSupport.boxFromCorners(outline,
						pressedPoint.x - offx, pressedPoint.y - offy, gpt.x
								- offx, gpt.y - offy);
				outline = null;
			} else {
				// System.err.println("clearing outline");
				savedOutline = null;
			}
		}

	}

	public void recordDragPoints() {
		realDragStartPoint = new Point2D.Double(realPressedPoint.getX(),
				realPressedPoint.getY());
		realDragEndPoint = new Point2D.Double(realReleasedPoint.getX(),
				realReleasedPoint.getY());
	}

	public void mouseEntered(MouseEvent mev) {
		// don't care
	}

	public void mouseExited(MouseEvent mev) {
		// don't care
	}

	// /////////////////////////////////////////////////////////////////
	//
	// MouseMotionListener Interface
	//
	// /////////////////////////////////////////////////////////////////

	public void mouseDragged(MouseEvent mev) {
		if (inverseTransform == null || grappaListener == null || inMenu) {
			return;
		}

		int modifiers = mev.getModifiers();

		int x = mev.getPoint().x;
		int y = mev.getPoint().y;

		int offx = 0;
		int offy = 0;
		if (mev.getSource() == graphPanel) {
			offx = graphPanel.getLocation().x;
			offy = graphPanel.getLocation().y;
			x += offx;
			y += offy;
		}

		Point2D pt = inverseTransform.transform(new Point(x, y), null);

		GrappaPoint gpt = new GrappaPoint(pt.getX(), pt.getY());

		grappaListener.grappaDragged(subgraph, gpt, modifiers, pressedElement,
				pressedPoint, pressedModifiers, outline, this);

		if ((modifiers & java.awt.event.InputEvent.BUTTON1_MASK) != 0
				&& (modifiers & java.awt.event.InputEvent.BUTTON1_MASK) == modifiers) {
			outline = GrappaSupport.boxFromCorners(outline, pressedPoint.x,
					pressedPoint.y, gpt.x, gpt.y);
		}
	}

	public void mouseMoved(MouseEvent mev) {
		// don't care
	}

	// --- Scrollable interface ----------------------------------------

	/**
	 * Returns the size of the bounding box of the graph augmented by the margin
	 * attribute and any scaling.
	 * 
	 * @return The preferredSize of a JViewport whose view is this Scrollable.
	 * @see JViewport#getPreferredSize
	 */
	public Dimension getPreferredScrollableViewportSize() {

		// preferred size is set above as needed, so just return it
		return getPreferredSize();
	}

	/**
	 * Always returns 1 since a GrappaPanel has not logical rows or columns.
	 * 
	 * @param visibleRect
	 *            The view area visible within the viewport
	 * @param orientation
	 *            Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
	 * @param direction
	 *            Less than zero to scroll up/left, greater than zero for
	 *            down/right.
	 * @return The "unit" increment for scrolling in the specified direction,
	 *         which in the case of a GrappaPanel is always 1.
	 * @see JScrollBar#setUnitIncrement
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return (1);
	}

	/**
	 * Returns 90% of the view area dimension that is in the orientation of the
	 * requested scroll.
	 * 
	 * @param visibleRect
	 *            The view area visible within the viewport
	 * @param orientation
	 *            Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
	 * @param direction
	 *            Less than zero to scroll up/left, greater than zero for
	 *            down/right.
	 * @return The "unit" increment for scrolling in the specified direction,
	 *         which in the case of a GrappaPanel is 90% of the visible width
	 *         for a horizontal increment or 90% of the visible height for a
	 *         vertical increment.
	 * @see JScrollBar#setBlockIncrement
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		int block;

		if (orientation == javax.swing.SwingConstants.VERTICAL) {
			block = (int) (visibleRect.height * 0.9);
		} else {
			block = (int) (visibleRect.width * 0.9);
		}
		if (block < 1) {
			block = 1;

		}
		return (block);
	}

	/**
	 * Always returns true as the viewport should force the width of this
	 * GrappaPanel to match the width of the viewport.
	 * 
	 * @return true
	 */
	public final boolean getScrollableTracksViewportWidth() {
		return (true);
	}

	/**
	 * Always returns true as the viewport should force the height of this
	 * GrappaPanel to match the height of the viewport.
	 * 
	 * @return true
	 */
	public final boolean getScrollableTracksViewportHeight() {
		return (true);
	}

	/**
	 * Returns the slider controlling the zoom factor of the displayed graph
	 * 
	 * @return a JSlider instance
	 */
	public JSlider getZoomSlider() {
		return slider;
	}

	/**
	 * Returns the scroll pane which encloses the graph
	 * 
	 * @return
	 */
	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	class GrappaGraphPanel extends JPanel implements Scrollable {

		public GrappaGraphPanel() {
			this.setBorder(BorderFactory.createEmptyBorder());
		}

		public void paintComponent(Graphics g) {
			if (Grappa.synchronizePaint || graph.getSynchronizePaint()) {
				if (graph.setPaint(true)) {
					componentPaint(g, isPreview());
					graph.setPaint(false);
				}
			} else {
				componentPaint(g, isPreview());
			}
		}

		public boolean getScrollableTracksViewportWidth() {
			return (false);
		}

		public boolean getScrollableTracksViewportHeight() {
			return (false);
		}

		public Dimension getPreferredScrollableViewportSize() {
			return graphPanel.getPreferredSize();
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			return (1);
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			int block;

			if (orientation == javax.swing.SwingConstants.VERTICAL) {
				block = (int) (visibleRect.height * 0.9);
			} else {
				block = (int) (visibleRect.width * 0.9);
			}
			if (block < 1) {
				block = 1;

			}
			return (block);
		}

		public String getToolTipText(MouseEvent mev) {
			return GrappaPanel.this.getToolTipText(mev);
		}

		protected boolean isPreview() {
			return false;
		}

	}

	class PreviewMouseHandler implements MouseListener, MouseMotionListener {
		public void mouseDragged(MouseEvent evt) {
			int modifiers = evt.getModifiers();
			if ((modifiers & java.awt.event.InputEvent.BUTTON1_MASK) != 0
					&& (modifiers & java.awt.event.InputEvent.BUTTON1_MASK) == modifiers) {
				// a is the point in the graph where I dragged to
				if (pressPoint == null) {
					// I didn't start dragging inside rectangle.
					return;
				}
				Rectangle2D box = subgraph.getBoundingBox();
				Point2D a = inverseTransform.transform(evt.getPoint(), null);
				if (!box.contains(a)) {
					// I am no longer in the graph.
					return;
				}
				double x = rectangle.getMinX() - (pressPoint.getX() - a.getX());
				if (x < box.getMinX()) {
					x = box.getMinX();
				}
				if (x > (box.getMaxX() - rectangle.getWidth())) {
					x = (box.getMaxX() - rectangle.getWidth());
				}
				double y = rectangle.getMinY() - (pressPoint.getY() - a.getY());
				if (y < box.getMinY()) {
					y = box.getMinY();
				}
				if (y > (box.getMaxY() - rectangle.getHeight())) {
					y = (box.getMaxY() - rectangle.getHeight());
				}
				rectangle.setRect(x, y, rectangle.getWidth(), rectangle
						.getHeight());

				pressPoint = a;

				drawMain(evt);
			}
		}

		private Point2D pressPoint = null;

		public void mouseClicked(MouseEvent e) {
			// don't care
		}

		public void mouseEntered(MouseEvent e) {
			// don't care
		}

		public void mouseExited(MouseEvent e) {
			// don't care
		}

		public void mousePressed(MouseEvent e) {
			// store the point where I clicked the mouse
			pressPoint = null;
			Point2D a = inverseTransform.transform(e.getPoint(), null);
			if (rectangle.contains(a)) {
				pressPoint = a;
			}
		}

		public void mouseReleased(MouseEvent e) {
			// don't care
		}

		public void drawMain(MouseEvent e) {
			Point2D tl = parentOfPreview.transform.transform(new GrappaPoint(
					rectangle.getMinX(), rectangle.getMinY()), null);
			Point p = new Point(Math.max(0, (int) (tl.getX())), Math.max(0,
					(int) (tl.getY())));

			parentOfPreview.scrollPane.getViewport().setViewPosition(p);
		}

		public void mouseMoved(MouseEvent e) {
		}
	}

}
