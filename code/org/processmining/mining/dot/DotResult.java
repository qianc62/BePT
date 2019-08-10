package org.processmining.mining.dot;

import org.processmining.mining.MiningResult;
import org.processmining.framework.log.LogReader;
import javax.swing.JComponent;
import java.awt.BorderLayout;
import org.processmining.framework.models.dot.DotModel;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import org.processmining.framework.models.ModelGraphPanel;

/**
 * <p>
 * Title: DotResult
 * </p>
 * 
 * <p>
 * Description: Contains a dot model.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class DotResult implements MiningResult {

	protected DotModel model;
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JScrollPane netContainer = new JScrollPane();

	/**
	 * Create a DotResult from some Dot model.
	 * 
	 * @param model
	 *            DotModel The given Dot model.
	 */
	public DotResult(DotModel model) {
		this.model = model;
		ModelGraphPanel gp = model.getGrappaVisualization();
		netContainer.setViewportView(gp);
		netContainer.invalidate();
		netContainer.repaint();
	}

	/**
	 * No log reader, sorry...
	 * 
	 * @return LogReader
	 */
	public LogReader getLogReader() {
		return null;
	}

	/**
	 * Simple thing. Nothing fancy.
	 * 
	 * @return JComponent
	 */
	public JComponent getVisualization() {
		mainPanel.add(netContainer, BorderLayout.CENTER);
		return mainPanel;
	}
}
