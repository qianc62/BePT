/**
 * 
 */
package org.processmining.framework.models.bpmn;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * 
 */
public abstract class BpmnUtils {

	public static BpmnGraph createBpmnGraph(Document doc) {
		Element root = doc.getDocumentElement();
		return createBpmnGraph(root);
	}

	public static BpmnGraph createBpmnGraph(Element element) {
		BpmnGraph graph = null;
		BpmnProcessModel mainProcess = new BpmnProcessModel(null, element);
		graph = new BpmnGraph(null, mainProcess);
		return graph;
	}

	/**
	 * Check whether the node is a property element
	 * 
	 * @param node
	 * @return
	 */
	public static boolean isProperty(Node node) {
		return (node == null) ? false : ((node instanceof Element) && node
				.getNodeName().equals(BpmnXmlTags.BPMN_PROPERTY));
	}

	/**
	 * Whether the node is an expanded sub graph
	 * 
	 * @param bObject
	 * @return true if the node is an expanded sub graph, otherwise false
	 */
	public static boolean isExpandedSubGraph(BpmnObject bObject) {
		boolean result = false;
		if (bObject instanceof BpmnSubProcess) {
			result = ((BpmnSubProcess) bObject).isExpanded();
		}

		return result;
	}
}
