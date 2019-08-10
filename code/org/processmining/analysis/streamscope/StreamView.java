/*
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.analysis.streamscope;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.analysis.streamscope.cluster.ClusterNode;
import org.processmining.analysis.streamscope.cluster.Node;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.ProcessInstance;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class StreamView extends JComponent {

	protected Color colorBlip = new Color(120, 255, 120);
	protected Color colorTransparent = new Color(0, 0, 0, 0);
	protected Color colorCluster = new Color(0, 80, 180, 160);
	protected Color colorFlag = new Color(60, 60, 60, 180);
	protected Color colorShadow = new Color(10, 10, 10, 120);
	protected Color colorText = new Color(200, 200, 200, 180);
	protected Color colorInspectorText = new Color(200, 200, 220);
	protected Color colorInspectorBg = new Color(80, 80, 80, 220);
	protected Color colorBorder = new Color(20, 25, 20);

	protected AffineTransform shadowTransform = AffineTransform
			.getTranslateInstance(2, 3);

	protected SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yy HH:mm:ss.SSS");

	protected float vRaster = 1f;
	protected float hRaster = 3f;
	protected float fuzziness = 2f;

	protected int level = 0;
	protected List<Node> levelNodes = null;

	protected float northBorder = 20;
	protected float southBorder = 15;
	protected float westBorder = 60;
	protected float eastBorder = 200;

	protected int mouseX = -1;
	protected int mouseY = -1;

	protected int activeIndex = -1;

	protected BufferedImage buffer = null;

	protected String name;
	protected AuditTrailEntryList ateList;
	protected StreamLogView parent;
	protected EventClassTable ecTable;

	public StreamView(ProcessInstance instance, StreamLogView parent) {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		this.ateList = instance.getAuditTrailEntryList();
		this.name = instance.getName();
		this.parent = parent;
		this.ecTable = parent.getClusters().getOrderedEventClassTable();
		this.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				repaint();
			}

			public void mouseMoved(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				repaint();
			}
		});
		this.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) { /* ignored */
			}

			public void mouseEntered(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				repaint();
			}

			public void mouseExited(MouseEvent e) {
				mouseX = -1;
				mouseY = -1;
				repaint();
			}

			public void mousePressed(MouseEvent e) { /* ignored */
			}

			public void mouseReleased(MouseEvent e) { /* ignored */
			}
		});
		adjustSize();
	}

	protected void adjustSize() {
		int size = ateList.size();
		int width = (int) ((size * hRaster) + westBorder + eastBorder);
		int height = (int) ((ecTable.size() * vRaster) + northBorder + southBorder);
		Dimension dim = new Dimension(width, height);
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		this.setPreferredSize(dim);
		this.setSize(dim);
	}

	public void setHorizontalZoom(float zoom) {
		hRaster = zoom;
		buffer = null;
		adjustSize();
		revalidate();
		repaint();
	}

	public void setVerticalZoom(float zoom) {
		vRaster = zoom;
		buffer = null;
		adjustSize();
		revalidate();
		repaint();
	}

	public void setFuzziness(float fuzziness) {
		this.fuzziness = fuzziness;
		buffer = null;
		repaint();
	}

	public void setLevel(int level, List<Node> nodes) {
		this.level = level;
		this.levelNodes = nodes;
		repaint();
	}

	public void setActiveIndex(int index) {
		this.activeIndex = index;
		if (index >= 0) {
			int x = (int) (westBorder + (index * hRaster));
			this
					.scrollRectToVisible(new Rectangle(x - 100, 0, 200,
							getHeight()));
		}
		repaint();
	}

	protected ClusterNode resolveCluster(int index) {
		if (this.levelNodes == null) {
			return null;
		}
		for (Node node : this.levelNodes) {
			int[] indices = node.getIndices();
			if (indices.length == 1 && indices[0] == index) {
				return null; // elementary node
			} else if (node instanceof ClusterNode) {
				for (int clusterIndex : indices) {
					if (clusterIndex == index) {
						return (ClusterNode) node;
					}
				}
			}
		}
		// not found in clusters
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		Rectangle clip = g.getClipBounds();
		float clipWest = clip.x;
		int startX = 0;
		if (clipWest > westBorder) {
			startX = (int) Math.floor((clipWest - westBorder) / hRaster);
		}
		float clipEast = clip.x + clip.width;
		int endX = (int) Math.floor((clipEast - westBorder) / hRaster);
		// start painting
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(new Color(0, 0, 0));
		g2d.fill(clip);
		// draw border
		g2d.setColor(colorBorder);
		g2d.draw(new RoundRectangle2D.Float(0, 0, this.getWidth() - 1, this
				.getHeight() - 1, 10, 10));
		// paint cluster backgrounds
		g2d.setColor(colorCluster);
		for (int index = startX; (index < endX && index < ateList.size()); index++) {
			int vIndex;
			try {
				vIndex = ecTable.getIndex(ateList.get(index).getElement());
			} catch (IOException e) {
				e.printStackTrace();
				// abort
				return;
			}
			ClusterNode currentNode = resolveCluster(vIndex);
			if (currentNode != null) {
				float x = westBorder + (index * hRaster);
				float yTop = northBorder
						+ (currentNode.getMinIndex() * vRaster);
				float yBottom = northBorder
						+ (currentNode.getMaxIndex() * vRaster);
				g2d
						.fill(new Rectangle2D.Float(x, yTop, hRaster, yBottom
								- yTop));
			}
		}
		// paint range
		for (int index = startX; (index < endX && index < ateList.size()); index++) {
			paintIndex(index, g2d);
		}
		// paint instance ID
		g.setFont(g.getFont().deriveFont(10f));
		Rectangle2D traceNameBounds = g2d.getFontMetrics().getStringBounds(
				name, g2d);
		if (clip.height > 25 && clip.width > traceNameBounds.getWidth() + 10) {
			GeneralPath flag = new GeneralPath();
			flag.moveTo(clip.x + 1, clip.y + 5);
			flag.lineTo(clip.x + (float) traceNameBounds.getWidth() + 10,
					clip.y + 5);
			flag.lineTo(clip.x + (float) traceNameBounds.getWidth() + 17,
					clip.y + 15);
			flag.lineTo(clip.x + (float) traceNameBounds.getWidth() + 10,
					clip.y + 25);
			flag.lineTo(clip.x + 1, clip.y + 25);
			flag.closePath();
			GeneralPath shadowFlag = (GeneralPath) flag.clone();
			shadowFlag.transform(shadowTransform);
			g2d.setColor(colorShadow);
			g2d.fill(shadowFlag);
			g2d.setColor(colorFlag);
			g2d.fill(flag);
			g2d.setColor(colorShadow);
			g.drawString(name, clip.x + 8, clip.y + 21);
			g.setColor(colorText);
			g.drawString(name, clip.x + 7, clip.y + 19);
		}
		// paint inspector on mouse-over
		if (mouseX > 0 && mouseY > 0) {
			g.setFont(g.getFont().deriveFont(9f));
			int eventIndex = (int) Math.floor((mouseX - westBorder) / hRaster);
			String date = "(no timestamp)";
			if (eventIndex >= 0 && eventIndex < ateList.size()) {
				try {
					Date timestamp = ateList.get(eventIndex).getTimestamp();
					if (timestamp != null) {
						date = dateFormat.format(timestamp);
					}
				} catch (IOException e) {
					// nevermind..
					e.printStackTrace();
				}
				int ecIndex = 0;
				String eventName = "";
				if (eventIndex >= 0 && eventIndex < ateList.size()) {
					try {
						eventName = ateList.get(eventIndex).getElement();
						ecIndex = ecTable.getIndex(eventName);
					} catch (IOException e) {
						// whatever...
						e.printStackTrace();
					}
				}
				float inspectorY = northBorder + (ecIndex * vRaster)
						+ (vRaster / 2);
				Rectangle2D nameBounds = g.getFontMetrics().getStringBounds(
						eventName, g2d);
				Rectangle2D dateBounds = g.getFontMetrics().getStringBounds(
						date, g2d);
				float inspectorWidth = (float) Math.max(nameBounds.getWidth(),
						dateBounds.getWidth());
				GeneralPath path = new GeneralPath();
				path.moveTo(mouseX + 1, inspectorY);
				path.lineTo(mouseX + 21f, inspectorY - 5);
				path.lineTo(mouseX + 21f, inspectorY + 5);
				path.closePath();
				GeneralPath shadowPath = (GeneralPath) path.clone();
				shadowPath.transform(shadowTransform);
				g2d.setColor(colorShadow);
				g2d.fill(shadowPath);
				RoundRectangle2D.Float bg = new RoundRectangle2D.Float(
						mouseX + 20, inspectorY - 15, inspectorWidth + 10f, 30,
						10, 10);
				GeneralPath bgShadow = new GeneralPath(bg);
				bgShadow.transform(shadowTransform);
				g2d.fill(bgShadow);
				g2d.setColor(colorInspectorBg);
				g2d.fill(path);
				g2d.fill(bg);
				g2d.setColor(colorShadow);
				g2d.drawString(eventName, mouseX + 26, inspectorY);
				g2d.drawString(date, mouseX + 26, inspectorY
						+ (float) dateBounds.getHeight() + 2);
				g2d.setColor(colorInspectorText);
				g2d.drawString(eventName, mouseX + 25, inspectorY - 2);
				g2d.drawString(date, mouseX + 25, inspectorY
						+ (float) dateBounds.getHeight());
			}
		}
	}

	protected void paintIndex(int index, Graphics2D g2d) {
		if (buffer == null) {
			float vSize = vRaster + (2 * vRaster * fuzziness);
			float hSize = hRaster + (2 * hRaster * fuzziness);
			if (vSize > 1 && hSize > 1) {
				buffer = new BufferedImage((int) Math.ceil(hSize), (int) Math
						.ceil(vSize), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2db = buffer.createGraphics();
				g2db.setColor(colorBlip);
				g2db.fill(new Rectangle2D.Float(fuzziness * hRaster, 0,
						hRaster, vRaster));
				GradientPaint gradient = new GradientPaint(0, 0,
						colorTransparent, fuzziness * hRaster, 0, colorBlip,
						false);
				g2db.setPaint(gradient);
				g2db.fill(new Rectangle2D.Float(0, 0, fuzziness * hRaster,
						vRaster));
				gradient = new GradientPaint((fuzziness * hRaster) + hRaster,
						0, colorBlip, (2 * fuzziness * hRaster) + hRaster, 0,
						colorTransparent, false);
				g2db.setPaint(gradient);
				g2db.fill(new Rectangle2D.Float(
						(fuzziness * hRaster) + hRaster, 0,
						(fuzziness * hRaster), vRaster));
				g2db.dispose();
			}
		}
		try {
			int vIndex = ecTable.getIndex(ateList.get(index).getElement());
			float x = westBorder + (index * hRaster);
			float y = northBorder + (vIndex * vRaster);
			if (buffer == null) {
				g2d.setColor(colorBlip);
				g2d.fill(new Rectangle2D.Float(x, y, 1, 1));
			} else {
				g2d.drawImage(buffer, (int) (x - (hRaster * fuzziness)),
						(int) y, null);
			}
			// draw active indicator, if appropriate
			if (index == activeIndex) {
				float radius = hRaster * 1.2f;
				Ellipse2D indicator = new Ellipse2D.Float(x - radius, y
						- radius, radius * 2, radius * 2);
				g2d = (Graphics2D) g2d.create();
				g2d.setColor(new Color(220, 220, 220, 80));
				g2d.fill(indicator);
				g2d.setColor(new Color(230, 230, 230, 220));
				g2d.setStroke(new BasicStroke(2f));
				g2d.draw(new Line2D.Float(x + radius, y, x + radius, y - 4
						* radius));
				g2d.draw(indicator);
				g2d.dispose();
			}
			/*
			 * NOTE: location bar painting temporarily removed (find less
			 * distracting alternative) int eventIndex = (int)Math.floor((mouseX
			 * - westBorder) / hRaster); if(eventIndex == index) { // draw focus
			 * indicator //g2d.setColor(new Color(20, 160, 20, 40));
			 * //g2d.draw(new Rectangle2D.Float(x - 3, 3, hRaster + 6,
			 * getHeight() - 6)); g2d.setColor(new Color(20, 160, 20, 30));
			 * g2d.fill(new Rectangle2D.Float(x, 6, hRaster, getHeight() - 12));
			 * }
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
