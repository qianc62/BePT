package org.processmining.framework.models.petrinet.pattern;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.util.CenterOnScreen;

public class ProgressWithCancelView implements ActionListener {

	private final JDialog dialog;

	private final JProgressBar progress;

	private final JButton cancel;

	private final int target;

	private boolean isCancelled;

	public ProgressWithCancelView(Frame frame, PetriNet petriNet) {
		super();
		target = petriNet.numberOfTransitions() - 1;
		progress = new JProgressBar(0, target);
		cancel = new JButton("Cancel operation");
		cancel.addActionListener(this);
		isCancelled = false;
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(progress);
		panel.add(cancel);
		cancel.setAlignmentX(Frame.CENTER_ALIGNMENT);
		dialog = new JDialog(frame);
		dialog.add(panel);
		panel.validate();
		panel.repaint();
		dialog.pack();
	}

	public void showProgressWithCancelView() {
		dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		CenterOnScreen.center(dialog);
		dialog.setVisible(true);
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Thread.yield();
				return null;
			}
		};
		worker.start();
		worker.interrupt();
	}

	public void hideProcessWithCancelView() {
		dialog.setVisible(false);
		dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void updateProgress(PetriNet petriNet) {
		progress.setValue(target - petriNet.numberOfTransitions() + 1);
		progress.validate();
		progress.repaint();
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == cancel) {
			isCancelled = true;
			hideProcessWithCancelView();
		}
	}

	public boolean isCancelled() {
		return isCancelled;
	}

}
