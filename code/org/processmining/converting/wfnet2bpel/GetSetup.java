package org.processmining.converting.wfnet2bpel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.jdom.Namespace;
import org.processmining.framework.models.bpel.util.Pair;
import org.processmining.framework.models.petrinet.pattern.ComponentDescription;
import org.processmining.framework.models.petrinet.pattern.MatchingOrder;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.util.CenterOnScreen;

/**
 * <p>
 * Title: GetSetup
 * </p>
 * 
 * <p>
 * Description: Queries the user how the converter should be setup before
 * starting.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: University of Aarhus
 * </p>
 * 
 * @author Kristian Bisgaard Lassen (<a
 *         href="mailto:K.B.Lassen@daimi.au.dk">mailto
 *         :K.B.Lassen@daimi.au.dk</a>)
 * @version 1.0
 */
public class GetSetup extends JPanel implements ActionListener, WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 383806833809163657L;

	private BlockingQueue<Pair<List<ComponentDescription>, Boolean>> queue = new ArrayBlockingQueue<Pair<List<ComponentDescription>, Boolean>>(
			1);

	private JCheckBox storeLog;

	private JButton button;

	private JDialog dialog;

	private final MatchingOrder matchingOrder;

	protected GetSetup(String matchingOrderXML, String matchingOrderXSL,
			String[] template, Namespace namespace) {
		matchingOrder = new MatchingOrder(false, false, false,
				matchingOrderXML, matchingOrderXSL, template, namespace,
				new String[] { "pnml", "bpel" });
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		storeLog = new JCheckBox("Include log in result", true);
		button = new JButton("Begin conversion");
		button.addActionListener(this);

		add(matchingOrder);
		add(storeLog);
		add(button);
	}

	@SuppressWarnings("unchecked")
	public Pair<List<ComponentDescription>, Boolean> getSetup() {
		final Pair<List<ComponentDescription>, Boolean>[] result = (Pair<List<ComponentDescription>, Boolean>[]) new Pair[1];
		dialog = new JDialog(MainUI.getInstance(), "Setup", true);
		dialog.add(this);
		dialog.pack();
		dialog.addWindowListener(this);
		CenterOnScreen.center(dialog);
		dialog.setVisible(true);
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				try {
					result[0] = queue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		worker.start();
		do {
			Thread.yield();
		} while (result[0] == null);
		return result[0];
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == button) {
			try {
				dialog.setVisible(false);
				queue.put(Pair.create(matchingOrder.getOrder(), storeLog
						.isSelected()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void windowActivated(WindowEvent arg0) {
	}

	public void windowClosed(WindowEvent arg0) {
	}

	public void windowClosing(WindowEvent arg0) {
		try {
			dialog.setVisible(false);
			queue.put(Pair.create((List<ComponentDescription>) null, false));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowOpened(WindowEvent arg0) {
	}

}
