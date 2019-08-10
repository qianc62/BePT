package org.processmining.analysis.epc;

import org.processmining.framework.models.epcpack.ConfigurableEPC;
import javax.swing.JPanel;
import org.processmining.framework.models.transitionsystem.TransitionSystem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.plugin.ProvidedObject;
import java.awt.BorderLayout;
import javax.swing.JTextPane;
import org.processmining.framework.ui.ProMHTMLEditorKit;
import javax.swing.JEditorPane;
import javax.swing.JComponent;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class EPCSoundnessUI extends JPanel implements Provider {

	private ConfigurableEPC epc;
	private TransitionSystem transitionSystem;
	private String message;

	private JTabbedPane jTabbedPane1 = new JTabbedPane();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JEditorPane textPane = new JEditorPane();
	private JSplitPane split = new JSplitPane();

	public EPCSoundnessUI(ConfigurableEPC epc,
			TransitionSystem transitionSystem, String message) {
		this.epc = epc;
		this.transitionSystem = transitionSystem;
		this.message = message;

		jbInit();
	}

	private void jbInit() {
		setLayout(new BorderLayout());

		split.setOrientation(JSplitPane.VERTICAL_SPLIT);
		split.setOneTouchExpandable(true);
		split.setResizeWeight(0.8);
		JComponent vis;

		textPane.setEditorKit(new ProMHTMLEditorKit(""));
		textPane.setText(message);
		textPane.setEditable(false);
		jScrollPane1.getViewport().add(textPane);

		JPanel epcPanel = new JPanel(new BorderLayout());
		vis = epc.getGrappaVisualization();
		if (vis != null) {
			epcPanel.add(vis, BorderLayout.CENTER);
		}
		jTabbedPane1.addTab("EPC", epcPanel);

		JPanel tsPanel = new JPanel(new BorderLayout());
		vis = transitionSystem.getGrappaVisualization();
		if (vis != null) {
			tsPanel.add(vis, BorderLayout.CENTER);
		}
		jTabbedPane1.addTab("Pruned Transition System", tsPanel);

		jTabbedPane1.setSelectedIndex(0);

		split.add(jScrollPane1, JSplitPane.BOTTOM);
		split.add(jTabbedPane1, JSplitPane.TOP);

		this.add(split, BorderLayout.CENTER);

	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] {
				new ProvidedObject("EPC", new Object[] { epc }),
				new ProvidedObject("Pruned Transition System",
						new Object[] { transitionSystem }),
				new ProvidedObject("Soundness result", new Object[] { message }) };
	}

}
