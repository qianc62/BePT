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
import org.processmining.framework.models.bpmn.BpmnGateway;
import org.processmining.framework.models.bpmn.BpmnGatewayType;
import org.processmining.framework.models.bpmn.BpmnGraphVertex;
import org.processmining.framework.models.bpmn.BpmnObject;
import org.processmining.framework.models.bpmn.BpmnTask;

import att.grappa.CustomRenderer;
import att.grappa.Element;
import att.grappa.GrappaShape;

public class GatewayShape extends GrappaShape implements CustomRenderer {
	private Element element;
	protected BpmnGateway bpmnGateway;

	public GatewayShape(Element element, double x_, double y_, double width_,
			double height_) {
		super(CUSTOM_SHAPE, x_, y_, width_, height_, 0, 0, 0, 0, 0, false,
				false, null);
		this.element = element;
		if (element.object == null)
			return;
		if (element.object instanceof BpmnGraphVertex) {
			BpmnGraphVertex bVertex = (BpmnGraphVertex) element.object;
			if (bVertex.getBpmnObject() instanceof BpmnGateway) {
				this.bpmnGateway = (BpmnGateway) bVertex.getBpmnObject();

				// use the dimension in the arguments
				// might try to use the dimesion in the event object
				float x = (float) x_;
				float y = (float) y_;
				float w = (float) width_;
				float h = (float) height_;

				// define gateway is a diamond
				defineGatewayShape(x, y, w, h);

				// add a gateway type shape
				defineGatewayTypeShape(x, y, w, h);
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

	// define a gateway is diamond
	protected void defineGatewayShape(float x, float y, float width,
			float height) {
		if (this.bpmnGateway != null) {

			path.moveTo(x + width / 2, y);
			path.lineTo(x + width, y + height / 2);
			path.lineTo(x + width / 2, y + height);
			path.lineTo(x, y + height / 2);
			path.lineTo(x + width / 2, y);
		}
	}

	/**
	 * Define the shape of GatewayType
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void defineGatewayTypeShape(float x, float y, float width,
			float height) {
		if (this.bpmnGateway != null) {

			BpmnGatewayType trType = bpmnGateway.getType();

			if (trType == BpmnGatewayType.AND) {
				// shape of AND
				defineANDShape(x, y, width, height);
			} else if (trType == BpmnGatewayType.OR) {
				// shape of OR
				defineORShape(x, y, width, height);
			} else if (trType == BpmnGatewayType.Complex) {
				// shape of Complex
				defineComplexShape(x, y, width, height);
			} else if (trType == BpmnGatewayType.XOR) {
				// shape of XOR
				defineXORShape(x, y, width, height);
			}

		}
	}

	// shape of "and" type
	protected void defineANDShape(float x, float y, float w, float h) {
		// shape of and
		path.moveTo(x + w / 2, y + h / 4);
		path.lineTo(x + w / 2, y + h * 3 / 4);
		path.moveTo(x + w / 4, y + h / 2);
		path.lineTo(x + w * 3 / 4, y + h / 2);
	}

	// shape of "or" type
	protected void defineORShape(float x, float y, float w, float h) {
		// shape of or
		path.append(new Ellipse2D.Double(x + w / 4, y + h / 4, w / 2, h / 2),
				false);

	}

	// shape of "complex" type
	protected void defineComplexShape(float x, float y, float w, float h) {
		// shape of or
		float x0 = x + w / 4, y0 = y + h / 4, h0 = h / 2, w0 = w / 2;
		path.moveTo(x0, y0);
		path.lineTo(x0 + w0, y0 + h0);
		path.moveTo(x0, y0 + h0 / 2);
		path.lineTo(x0 + w0, y0 + h0 / 2);
		path.moveTo(x0, y0 + h0);
		path.lineTo(x0 + w0, y0);
		path.moveTo(x0 + w0 / 2, y0);
		path.lineTo(x0 + w0 / 2, y0 + h0);
	}

	// shape of "xor" type
	protected void defineXORShape(float x, float y, float w, float h) {
		// shape of xor
		float x0 = x + w / 4, y0 = y + h / 4, h0 = h / 2, w0 = w / 2;
		path.moveTo(x0, y0);
		path.lineTo(x0 + w0, y0 + h0);
		path.moveTo(x0, y0 + h0);
		path.lineTo(x0 + w0, y0);
	}
}
