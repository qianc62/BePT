package org.processmining.importing.erlangnet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.erlangnet.ErlangNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

public class ErlangNetResult extends JSplitPane implements MiningResult,
		ActionListener, Provider {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7210738272570489044L;
	private final List<ErlangNet> nets;
	private final Map<JButton, Integer> buttons = new LinkedHashMap<JButton, Integer>();
	private final JPanel buttonPanel;
	private int selected;

	public ErlangNetResult(List<ErlangNet> nets) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.nets = nets;
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		selected = 0;
		int index = 0;
		for (ErlangNet net : nets) {
			JButton button = new JButton(net.getIdentifier());
			button.addActionListener(this);
			buttonPanel.add(button);
			buttons.put(button, index++);
		}
		add(buttonPanel, JSplitPane.LEFT);
		PetriNetResult result = new PetriNetResult(nets.get(0));
		add(result.getVisualization(), JSplitPane.RIGHT);
	}

	public LogReader getLogReader() {
		return null;
	}

	public JComponent getVisualization() {
		return this;
	}

	public void actionPerformed(ActionEvent e) {
		Integer index = buttons.get(e.getSource());
		if (index != null) {
			removeAll();
			PetriNetResult result = new PetriNetResult(nets.get(index));
			add(buttonPanel, JSplitPane.LEFT);
			add(result.getVisualization(), JSplitPane.RIGHT);
			selected = index;
			validate();
			repaint();
		}
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("Petri net", nets
				.get(selected)) };
	}

}
