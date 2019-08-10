/*
 * Created on 15 juin 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.processmining.mining.patternsmining;

//import java.awt.*;
import javax.swing.JPanel;

import org.processmining.framework.log.LogSummary;

/**
 * @author WALID
 * 
 * 
 */
public class WorkflowPatternsOptions extends JPanel {
	private LogSummary summary;

	public WorkflowPatternsOptions(LogSummary summary) {
		this.summary = summary;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		// this method is created by JBuilder when
		// you create a new subclass of JPanel

		// you can initialize the widgets here
		// and you can use the summary variable
	}

}
