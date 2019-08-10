/**
 * 
 */
package org.processmining.framework.models.bpmn;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * 
 */
public interface BpmnDotOutput {
	/**
	 * Get the DOT file section of this element
	 * 
	 * @return String of the DOT file section of this element, line break
	 *         included
	 */
	public String toDotString();
}
