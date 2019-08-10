/**
 * 
 */
package org.processmining.converting.erlangnet2erlang;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.processmining.framework.log.LogReader;
import org.processmining.mining.MiningResult;

/**
 * @author Kristian Bisgaard Lassen
 * 
 */
public class ErlangResult implements MiningResult {

	private JTextArea pane;

	public ErlangResult(String source) {
		pane = new JTextArea(source);
		pane.setTabSize(2);
		pane.setEditable(false);
	}

	/**
	 * @see org.processmining.mining.MiningResult#getLogReader()
	 */
	public LogReader getLogReader() {
		return null;
	}

	/**
	 * @see org.processmining.mining.MiningResult#getVisualization()
	 */
	public JComponent getVisualization() {
		return new JScrollPane(pane);
	}

}
