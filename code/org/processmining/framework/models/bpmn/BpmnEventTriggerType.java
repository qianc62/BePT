/**
 * 
 */
package org.processmining.framework.models.bpmn;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng this the tag of
 *         "trigger" in event
 */
public enum BpmnEventTriggerType {
	None, Message, Timer, Exception, Error, Cancel, Compensation, Compensate, Rule, Link, Terminate, Multiple
}
