//@author JianHong.YE, collaborate with LiJie.WEN and Feng
package att.grappa.bpmn;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.processmining.framework.models.bpmn.BpmnEvent;

import org.processmining.framework.models.bpmn.BpmnEventTriggerType;
import org.processmining.framework.models.bpmn.BpmnEventType;
import org.processmining.framework.models.bpmn.BpmnGraphVertex;
import org.processmining.framework.models.bpmn.BpmnObject;

import att.grappa.CustomRenderer;
import att.grappa.Element;
import att.grappa.GrappaShape;

public class EventShape extends GrappaShape implements CustomRenderer {
	private Element element;
	protected BpmnEvent bpmnEvent;

	public EventShape(Element element, double x_, double y_, double width_,
			double height_) {
		super(CUSTOM_SHAPE, x_, y_, width_, height_, 0, 0, 0, 0, 0, false,
				false, null);

		this.element = element;
		if (element.object == null)
			return;
		if (element.object instanceof BpmnGraphVertex) {
			BpmnGraphVertex bVertex = (BpmnGraphVertex) element.object;

			if (bVertex.getBpmnObject() instanceof BpmnEvent) {
				this.bpmnEvent = (BpmnEvent) bVertex.getBpmnObject();

				// use the dimension in the arguments
				// might try to use the dimesion in the event object
				float x = (float) x_;
				float y = (float) y_;
				float w = (float) width_;
				float h = (float) height_;

				// define circle for Start, End or Intermediate
				defineEventShape(x, y, w, h);

				// add a trigger shape
				defineTriggerShape(x, y, w, h);
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

	public void draw(Graphics2D g2d) {
		// TODO Auto-generated method stub
		g2d.draw(this);
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
	public void drawImage(Graphics2D g2d) {
		// TODO Auto-generated method stub
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
	public void fill(Graphics2D g2d) {
		// TODO Auto-generated method stub
		g2d.fill(this);
	}

	protected void defineEventShape(float x, float y, float width, float height) {
		if (this.bpmnEvent != null) {

			BpmnEventType eType = bpmnEvent.getTypeTag();
			// plot a shape for start, inmediate, or end
			if (eType == BpmnEventType.Start) {
				path.append(new Ellipse2D.Double(x, y, width, height), false);
			} else if (eType == BpmnEventType.Intermediate) {
				// double ellipse for intermediate
				path.append(new Ellipse2D.Double(x, y, width, height), false);
				path.append(new Ellipse2D.Double(x + 2, y + 2, width - 4,
						height - 4), false);
			} else if (eType == BpmnEventType.End) {
				// solid ellipse for end
				path.append(new Ellipse2D.Double(x, y, width, height), false);
				path.append(new Ellipse2D.Double(x + 0.5, y + 0.5, width - 1,
						height - 1), false);
				path.append(new Ellipse2D.Double(x + 1, y + 1, width - 2,
						height - 2), false);
			}
		}
	}

	/**
	 * Define a trigger shape in GeneralPath
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void defineTriggerShape(float x, float y, float width,
			float height) {
		if (this.bpmnEvent != null) {

			BpmnEventTriggerType trType = bpmnEvent.getTrigger();

			if (trType == BpmnEventTriggerType.Message) {
				// shape of message trigger
				defineMessageShape(x, y, width, height);
			} else if (trType == BpmnEventTriggerType.Timer) {
				// shape of timer trigger
				defineTimerShape(x, y, width, height);
			} else if (trType == BpmnEventTriggerType.Error
					|| trType == BpmnEventTriggerType.Exception) {
				// shape of error and exception trigger
				defineErrorShape(x, y, width, height);
			} else if (trType == BpmnEventTriggerType.Compensation
					|| trType == BpmnEventTriggerType.Compensate) {
				// shape of Compensate trigger
				defineCompensateShape(x, y, width, height);
			} else if (trType == BpmnEventTriggerType.Cancel) {
				// shape of Cancel trigger
				defineCancelShape(x, y, width, height);
			} else if (trType == BpmnEventTriggerType.Rule) {
				// shape of Rule trigger
				defineRuleShape(x, y, width, height);
			} else if (trType == BpmnEventTriggerType.Link) {
				// shape of Link trigger
				defineLinkShape(x, y, width, height);
			} else if (trType == BpmnEventTriggerType.Terminate) {
				// shape of terminate trigger
				defineTerminateShape(x, y, width, height);
			} else if (trType == BpmnEventTriggerType.Multiple) {
				// shape of multiple trigger
				defineMultipleShape(x, y, width, height);
			}
		}
	}

	/**
	 * Define the shape of Timer trigger
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void defineTimerShape(float x, float y, float width, float height) {
		// shape of timer trigger
		float x0 = x + 4;
		float y0 = y + 4;
		float w0 = width - 8;
		float h0 = height - 8;
		path.append(new Ellipse2D.Double(x0, y0, w0, h0), false);
		path.moveTo(x0 + w0 / 2, y0);
		path.lineTo(x0 + w0 / 2, y0 + h0 / 5);
		path.moveTo(x0 + w0, y0 + h0 / 2);
		path.lineTo(x0 + w0 * 4 / 5, y0 + h0 / 2);
		path.moveTo(x0 + w0 / 2, y0 + h0);
		path.lineTo(x0 + w0 / 2, y0 + h0 * 4 / 5);
		path.moveTo(x0, y0 + h0 / 2);
		path.lineTo(x0 + w0 / 5, y0 + h0 / 2);
		path.moveTo(x0 + w0 * 7 / 24, y0 + h0 * 7 / 24);
		path.lineTo(x0 + w0 / 2, y0 + h0 / 2);
		path.lineTo(x0 + w0 * 3 / 4, y0 + h0 / 3);
	}

	/**
	 * Define the shape of Message trigger
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void defineMessageShape(float x, float y, float width,
			float height) {
		// shape of message trigger
		float x0 = x + width / 4;
		float y0 = y + height / 4;
		float w0 = width / 2;
		float h0 = height / 2;
		path.append(new Rectangle2D.Double(x0, y0, w0, h0), false);
		path.moveTo(x0, y0);
		path.lineTo(x0 + w0 / 2, y0 + h0 / 2);
		path.lineTo(x0 + w0, y0);
	}

	/**
	 * Define the shape of Error and Exception trigger
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void defineErrorShape(float x, float y, float width, float height) {
		// shape of error trigger
		float x0 = x + 2;
		float y0 = y + 2;
		float w0 = width - 4;
		float h0 = height - 4;
		path.moveTo(x0 + w0 * 1 / 6, y0 + h0 * 5 / 6);
		path.lineTo(x0 + w0 / 4, y0 + h0 / 4);
		path.lineTo(x0 + w0 * 3 / 4, y0 + h0 * 3 / 4);
		path.lineTo(x0 + w0 * 5 / 6, y0 + h0 * 1 / 6);
	}

	/**
	 * Define the shape of Compensate trigger
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void defineCompensateShape(float x, float y, float width,
			float height) {
		// shape of error compensate
		float x0 = x + 2;
		float y0 = y + 2;
		float w0 = width - 4;
		float h0 = height - 4;
		path.moveTo(x0 + w0 / 4, y0 + h0 / 2);
		path.lineTo(x0 + w0 / 2, y0 + h0 / 4);
		path.lineTo(x0 + w0 / 2, y0 + h0 / 2);
		path.lineTo(x0 + w0 * 3 / 4, y0 + h0 / 4);
		path.lineTo(x0 + w0 * 3 / 4, y0 + h0 * 3 / 4);
		path.lineTo(x0 + w0 / 2, y0 + h0 / 2);
		path.lineTo(x0 + w0 / 2, y0 + h0 * 3 / 4);
		path.lineTo(x0 + w0 / 4, y0 + h0 / 2);
	}

	/**
	 * Define the shape of Cancel trigger
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void defineCancelShape(float x, float y, float width, float height) {
		// shape of cancel trigger
		float x0 = x + 2;
		float y0 = y + 2;
		float w0 = width - 4;
		float h0 = height - 4;
		path.moveTo(x0 + w0 / 6, y0 + h0 / 6);
		path.lineTo(x0 + w0 * 5 / 6, y0 + h0 * 5 / 6);
		path.moveTo(x0 + w0 * 5 / 6, y0 + h0 / 6);
		path.lineTo(x0 + w0 / 6, y0 + h0 * 5 / 6);
	}

	/**
	 * Define the shape of Rule trigger
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void defineRuleShape(float x, float y, float width, float height) {
		// shape of rule trigger
		float x0 = x + width / 4;
		float y0 = y + height / 4;
		float w0 = width / 2;
		float h0 = height / 2;
		path.append(new Rectangle2D.Double(x0, y0, w0, h0), false);
		path.moveTo(x0 + w0 / 5, y0 + h0 / 5);
		path.lineTo(x0 + w0 * 4 / 5, y0 + h0 / 5);
		path.moveTo(x0 + w0 / 5, y0 + h0 / 2);
		path.lineTo(x0 + w0 * 4 / 5, y0 + h0 / 2);
		path.moveTo(x0 + w0 / 5, y0 + h0 * 4 / 5);
		path.lineTo(x0 + w0 * 4 / 5, y0 + h0 * 4 / 5);
	}

	/**
	 * Define the shape of Link trigger
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void defineLinkShape(float x, float y, float width, float height) {
		// shape of link trigger
		float x0 = x + 2;
		float y0 = y + 2;
		float w0 = width - 4;
		float h0 = height - 4;
		path.moveTo(x0 + w0 / 4, y0 + h0 / 2);
		path.lineTo(x0 + w0 * 3 / 4, y0 + h0 / 2);
		path.moveTo(x0 + w0 / 2, y0 + h0 / 4);
		path.lineTo(x0 + w0 * 3 / 4, y0 + h0 / 2);
		path.lineTo(x0 + w0 / 2, y0 + h0 * 3 / 4);
	}

	/**
	 * Define the shape of Terminate trigger
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void defineTerminateShape(float x, float y, float width,
			float height) {
		// shape of terminate trigger
		float x0 = x + 3;
		float y0 = y + 3;
		float w0 = width - 6;
		float h0 = height - 6;
		path.append(new Ellipse2D.Double(x0, y0, w0, h0), false);
		// construct a solid black circle
		int i = (int) width;
		while (i != 0) {
			x0 = (float) (x0 + 0.3);
			y0 = (float) (y0 + 0.3);
			w0 = (float) ((float) w0 - 0.6);
			h0 = (float) ((float) h0 - 0.6);
			path.append(new Ellipse2D.Double(x0, y0, w0, h0), false);
			i--;
		}
	}

	/**
	 * Define the shape of Multiple trigger
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void defineMultipleShape(float x, float y, float width,
			float height) {
		// shape of multiple trigger
		float x0 = x + 2;
		float y0 = y + 2;
		float w0 = width - 4;
		float h0 = height - 4;
		path.moveTo(x0 + w0 / 2, y0);
		path.lineTo(x0 + w0 * 3 / 4, y0 + h0 / 4);
		path.lineTo(x0 + w0, y0 + h0 / 4);
		path.lineTo(x0 + w0 * 3 / 4, y0 + h0 / 2);
		path.lineTo(x0 + w0, y0 + h0 * 3 / 4);

		path.lineTo(x0 + w0 * 3 / 4, y0 + h0 * 3 / 4);
		path.lineTo(x0 + w0 / 2, y0 + h0);
		path.lineTo(x0 + w0 / 4, y0 + h0 * 3 / 4);
		path.lineTo(x0, y0 + h0 * 3 / 4);

		path.lineTo(x0 + w0 / 4, y0 + h0 / 2);
		path.lineTo(x0, y0 + h0 / 4);
		path.lineTo(x0 + w0 / 4, y0 + h0 / 4);
		path.lineTo(x0 + w0 / 2, y0);
	}
}
