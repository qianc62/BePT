/*
 * Created on Jun 17, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
 * all rights reserved
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning.freqstat;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;
import org.processmining.mining.MiningResult;

/**
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class ProximitySensingResults extends JPanel implements MiningResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3481081191948412734L;
	protected FrequencyStatistics statistics = null;
	protected LogReader reader = null;

	public ProximitySensingResults(FrequencyStatistics stats, LogReader aReader) {
		statistics = stats;
		reader = aReader;
		initGui();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getVisualization()
	 */
	public JComponent getVisualization() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getLogReader()
	 */
	public LogReader getLogReader() {
		return reader;
	}

	public void initGui() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		DistributionPanel display = new DistributionPanel(statistics
				.getNormalizedDistribution(500, 200), 500);
		display.setOpaque(true);
		this.add(display);
	}
}
