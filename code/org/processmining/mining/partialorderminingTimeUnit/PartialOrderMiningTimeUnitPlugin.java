package org.processmining.mining.partialorderminingTimeUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.partialordermining.PartialOrderMiningResult;

public class PartialOrderMiningTimeUnitPlugin implements MiningPlugin {

	private PartialOrderMiningUI ui = null;
	private HashMap boxesCR = null;
	private Progress progress = null;

	// default constructor
	public void PartialOrderMiningTimeUnitPlugin() {

	}

	public String getHtmlDescription() {
		return null;
	}

	public String getName() {
		return "Partial Order Mining TimeUnit";
	}

	public JPanel getOptionsPanel(LogSummary summary) {
		if (ui == null) {
			ui = new PartialOrderMiningUI();
		}
		return ui;
	}

	public synchronized MiningResult mine(LogReader log) {
		progress = new Progress("Progress:");
		progress.setMaximum(100);
		// create part structure of boxesCR;
		boxesCR = new HashMap();
		for (int i = 0; i < log.getInstances().size(); i++) {
			boxesCR.put(log.getInstance(i), new ArrayList());
		}

		return minePoTimeUnit(log, progress);
	}

	public MiningResult minePoTimeUnit(LogReader log, Progress progress) {
		if (!(log instanceof BufferedLogReader)) {
			Message
					.add("This Log-reader is READ-ONLY, so the partial order will not be stored."
							+ " Please select another Log-Reader under help.");
		}

		// start working on making 'boxes' for ate which happen in the same
		// timeunit
		// and put them in a data structure
		Iterator<ProcessInstance> it = log.instanceIterator();
		while (it.hasNext()) {
			ProcessInstance pi = it.next();
			Iterator<AuditTrailEntry> itate = pi.getAuditTrailEntryList()
					.iterator();
			int counter = 0;
			long curDateMill = -1; // current date (timeunit) in milliseconds
			ArrayList currentBox = null;
			while (itate.hasNext()) {
				AuditTrailEntry ate = itate.next();
				long dateMillAte = ate.getTimestamp().getTime()
						/ ui.getSelectedTimeUnit().getConversionValue();
				if (curDateMill == dateMillAte) {
					// belong to the same 'box'
					// add to the current box
					currentBox.add(new AtePositionPiTuple(ate, counter));
				} else if (dateMillAte > curDateMill) {
					// do not belong to the same 'box'
					// so generate a new box and add it to the structure
					ArrayList newBox = new ArrayList();
					currentBox = newBox;
					newBox.add(new AtePositionPiTuple(ate, counter));
					((ArrayList) boxesCR.get(pi)).add(currentBox);
					// update the curDateMill;
					curDateMill = dateMillAte;
				} else {
					// raise exception
					new Exception(
							"The ates in the log are not ordered on timestamp!");
				}
				counter++;
			}
		}

		progress.setNote("Finished determining partial orders");
		progress.setProgress(50);

		// add the partial orders which exist between the boxes to the log
		int overwriteExisting = -1;
		ArrayList a = new ArrayList();
		Iterator<ProcessInstance> keysIt = boxesCR.keySet().iterator();
		while (keysIt.hasNext()) {
			ProcessInstance pi = keysIt.next();
			a.add(pi.getName());
			if (pi.getAttributes().containsKey(ProcessInstance.ATT_PI_PO)
					&& pi.getAttributes().get(ProcessInstance.ATT_PI_PO)
							.equals("true") && (overwriteExisting < 1)) {
				// OverwriteExisting == 0 means that we asked and we should not
				// overwrite
				if (overwriteExisting == 0) {
					continue;
				}
				// OverwriteExisting == -1 means that we did not ask yet
				int i = JOptionPane
						.showConfirmDialog(
								MainUI.getInstance(),
								"This log already contains partially ordered process instances. \n"
										+ "Should this information be overwritten?\n"
										+ "Click YES to overwrite the partial order with a new partial order, \n"
										+ "click NO  to skip all process instances that already contain a partial order.",
								"Warning", JOptionPane.YES_NO_OPTION);
				if (i == JOptionPane.NO_OPTION) {
					overwriteExisting = 0;
					continue;
				}
				if (i == JOptionPane.YES_OPTION) {
					overwriteExisting = 1;
				}
			}

			// This process instance becomes a partial order
			pi.setAttribute(ProcessInstance.ATT_PI_PO, "true");
			progress.setNote(pi.getName());
			progress.inc();

			// Make ids for all ates
			// for all ates in the boxes
			Iterator<ArrayList> boxesIt = ((ArrayList) boxesCR.get(pi))
					.iterator();
			while (boxesIt.hasNext()) {
				ArrayList box = boxesIt.next();
				Iterator<AtePositionPiTuple> boxIt = box.iterator();
				while (boxIt.hasNext()) {
					AtePositionPiTuple atePos = boxIt.next();
					atePos.getAte().setAttribute(ProcessInstance.ATT_ATE_ID,
							"id" + atePos.getPositionPi());
					atePos.getAte().setAttribute(ProcessInstance.ATT_ATE_POST,
							"");
					atePos.getAte().setAttribute(ProcessInstance.ATT_ATE_PRE,
							"");
					try {
						pi.getAuditTrailEntryList().replace(atePos.getAte(),
								atePos.getPositionPi());
					} catch (IOException ex3) {
					} catch (IndexOutOfBoundsException ex3) {
					}
				}
			}

			int maxIndex = pi.getAuditTrailEntryList().size();
			for (int k = 0; k < maxIndex; k++) {
				AuditTrailEntry ate = null;

				try {
					ate = pi.getAuditTrailEntryList().get(k);
					ate.setAttribute(ProcessInstance.ATT_ATE_ID, "id" + k);
					ate.setAttribute(ProcessInstance.ATT_ATE_POST, "");
					ate.setAttribute(ProcessInstance.ATT_ATE_PRE, "");
					pi.getAuditTrailEntryList().replace(ate, k);

				} catch (IOException ex3) {
				} catch (IndexOutOfBoundsException ex3) {
				}
			}
			// write the 'boxes' and their po's
			ArrayList boxes = (ArrayList) boxesCR.get(pi);
			for (int i = 0; i < boxes.size(); i++) {
				ArrayList<AtePositionPiTuple> currentBox = (ArrayList) boxes
						.get(i);
				// write pre for current box (if possible)
				if (i > 0) {
					ArrayList<AtePositionPiTuple> preBox = (ArrayList) boxes
							.get(i - 1);
					for (int j = 0; j < currentBox.size(); j++) {
						for (int k = 0; k < preBox.size(); k++) {
							AuditTrailEntry curBoxAte = currentBox.get(j)
									.getAte();
							AuditTrailEntry preBoxAte = preBox.get(k).getAte();
							String pre = curBoxAte.getAttributes().get(
									ProcessInstance.ATT_ATE_PRE);
							// add the information to the pre
							pre += (pre.length() == 0 ? "" : ",")
									+ preBoxAte.getAttributes().get(
											ProcessInstance.ATT_ATE_ID);
							// set the pre set of the current ate
							curBoxAte.setAttribute(ProcessInstance.ATT_ATE_PRE,
									pre);
							// update curBoxAte in the list
							try {
								pi.getAuditTrailEntryList().replace(curBoxAte,
										currentBox.get(j).getPositionPi());
							} catch (IOException ex4) {
							} catch (IndexOutOfBoundsException ex4) {
							}
						}
					}
				}
				// write post for current box (if possible)
				if (i + 1 < boxes.size()) {
					ArrayList<AtePositionPiTuple> postBox = (ArrayList) boxes
							.get(i + 1);
					for (int j = 0; j < currentBox.size(); j++) {
						for (int k = 0; k < postBox.size(); k++) {
							AuditTrailEntry curBoxAte = currentBox.get(j)
									.getAte();
							AuditTrailEntry postBoxAte = postBox.get(k)
									.getAte();
							String post = curBoxAte.getAttributes().get(
									ProcessInstance.ATT_ATE_POST);
							// add the information to the post
							post += (post.length() == 0 ? "" : ",")
									+ postBoxAte.getAttributes().get(
											ProcessInstance.ATT_ATE_ID);
							// set the post set of the current ate
							curBoxAte.setAttribute(
									ProcessInstance.ATT_ATE_POST, post);
							// update curBoxAte in the list
							try {
								pi.getAuditTrailEntryList().replace(curBoxAte,
										currentBox.get(j).getPositionPi());
							} catch (IOException ex4) {
							} catch (IndexOutOfBoundsException ex4) {
							}
						}
					}
				}
				// check if partial orders need to be defined for the events in
				// the box itself
				if (ui.isParOptionChecked()) {
					for (int l = 0; l < currentBox.size(); l++) {
						for (int m = 0; m < currentBox.size(); m++) {
							AtePositionPiTuple firstAtePos = currentBox.get(l);
							AtePositionPiTuple secondAtePos = currentBox.get(m);
							if (firstAtePos != secondAtePos) { // we don't need
								// a partial
								// order from a
								// event to
								// itself

								String pre = firstAtePos.getAte()
										.getAttributes().get(
												ProcessInstance.ATT_ATE_PRE);
								// add the information to the pre of firstAtePos
								pre += (pre.length() == 0 ? "" : ",")
										+ secondAtePos
												.getAte()
												.getAttributes()
												.get(ProcessInstance.ATT_ATE_ID);
								// set the pre set of the current ate
								firstAtePos.getAte().setAttribute(
										ProcessInstance.ATT_ATE_PRE, pre);
								// update curBoxAte in the list
								try {
									pi.getAuditTrailEntryList().replace(
											firstAtePos.getAte(),
											firstAtePos.getPositionPi());
								} catch (IOException ex4) {
								} catch (IndexOutOfBoundsException ex4) {
								}

								String post = firstAtePos.getAte()
										.getAttributes().get(
												ProcessInstance.ATT_ATE_POST);
								// add the information to the post of
								// firstAtePos
								post += (post.length() == 0 ? "" : ",")
										+ secondAtePos
												.getAte()
												.getAttributes()
												.get(ProcessInstance.ATT_ATE_ID);
								// set the post set of the current ate
								firstAtePos.getAte().setAttribute(
										ProcessInstance.ATT_ATE_POST, post);
								// update curBoxAte in the list
								try {
									pi.getAuditTrailEntryList().replace(
											firstAtePos.getAte(),
											firstAtePos.getPositionPi());
								} catch (IOException ex4) {
								} catch (IndexOutOfBoundsException ex4) {
								}

							}
						}
					}
				}
			}

		}

		progress.setProgress(100);
		progress.close();
		return new PartialOrderMiningResult(log, a);
	}

}
