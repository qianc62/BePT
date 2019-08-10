package org.processmining.mining.fsm;

import javax.swing.JComponent;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.models.fsm.FSM;
import org.processmining.framework.models.fsm.FSMState;

import org.processmining.mining.Miner;
import org.processmining.framework.models.fsm.AcceptFSM;
import org.processmining.converting.fsm.FsmModificationSettings;
import org.processmining.converting.fsm.FsmModificationConvertor;

/**
 * <p>
 * Title: FSM Miner
 * </p>
 * 
 * <p>
 * Description: Mining an FSM from a log file
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
 * 
 *          Code rating: Red
 * 
 *          Review rating: Red
 */
public class FsmMiner {

	@Miner(name = "FSM miner", help = "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:mining:fsm", settings = FsmGui.class)
	/*
	 * * Mine the log for an FSM using the given settings.
	 * 
	 * @param log LogReader the log.
	 * 
	 * @param settings FsmSettings the settings.
	 * 
	 * @return JComponent the mined FSM as an FSMResult.
	 */
	public static JComponent mine(LogReader log, FsmSettings settings) {
		AcceptFSM fsm = new AcceptFSM(log.getLogSummary().getSource().getName());

		try {
			Progress progress = null;
			int progressCtr = 0;
			if (settings.hasGUI()) {
				// First count how many stpes we have to do for the progress
				// bar.
				progressCtr = 0;
				for (ProcessInstance pi : log.getInstances()) {
					progressCtr += pi.getAuditTrailEntryList().size();
				}
				// Create the progress bar.
				progress = new Progress("Constructing FSM...", 0,
						progressCtr - 1);
			}
			// And now for the real thing.
			progressCtr = 0;
			for (ProcessInstance pi : log.getInstances()) {
				AuditTrailEntryList atel = pi.getAuditTrailEntryList();
				for (int i = 0; i < atel.size(); i++) {
					if (settings.hasGUI()) {
						progress.setProgress(progressCtr++);
					}
					AuditTrailEntry ate = atel.get(i);

					/**
					 * An AuditTrailEntry corresponds to a transition in the
					 * FSM. First, construct the payload of the state preceding
					 * the transition.
					 */
					FsmMinerPayload fromPayload = new FsmMinerPayload(settings);

					// Use the horizon settings.
					for (int mode = 0; mode < FsmMinerPayload.LAST; mode++) {
						mineBwd(atel, i, 1, settings.getHorizonSettings(true,
								mode), fromPayload, mode);
						mineFwd(atel, i, 0, settings.getHorizonSettings(false,
								mode), fromPayload, mode);
					}

					// Use the attribute settings.
					if (settings.getUseAttributes()) {
						for (int j = 0; j < i; j++) {
							AuditTrailEntry ate2 = atel.get(j);
							DataSection dataSection = ate2.getDataAttributes();
							for (String attribute : dataSection.keySet()) {
								if (settings.getAttributeSettings()
										.containsKey(attribute)) {
									String cluster = settings
											.getAttributeSettings().get(
													attribute).get(
													dataSection.get(attribute));
									if (cluster != null) {
										fromPayload.getAttributePayload().put(
												attribute, cluster);
									}
								}
							}
						}
					}

					/**
					 * Second, in a similar way, create the payload of the state
					 * succeding the transition.
					 */
					FsmMinerPayload toPayload = new FsmMinerPayload(settings);
					for (int mode = 0; mode < FsmMinerPayload.LAST; mode++) {
						mineBwd(atel, i, 0, settings.getHorizonSettings(true,
								mode), toPayload, mode);
						mineFwd(atel, i, 1, settings.getHorizonSettings(false,
								mode), toPayload, mode);
					}

					if (settings.getUseAttributes()) {
						for (int j = 0; j <= i; j++) {
							AuditTrailEntry ate2 = atel.get(j);
							DataSection dataSection = ate2.getDataAttributes();
							for (String attribute : dataSection.keySet()) {
								if (settings.getAttributeSettings()
										.containsKey(attribute)) {
									String cluster = settings
											.getAttributeSettings().get(
													attribute).get(
													dataSection.get(attribute));
									if (cluster != null) {
										toPayload.getAttributePayload().put(
												attribute, cluster);
									}
								}
							}
						}
					}

					// Create both states with the constructed payloads.
					FSMState fromState = new FSMState(fsm, fromPayload
							.toString(), fromPayload);
					FSMState toState = new FSMState(fsm, toPayload.toString(),
							toPayload);

					// Cerate the transition.
					/**
					 * If the model element is filtered out, the empty string is
					 * used as the model element.
					 */
					String label;
					if (settings.getVisibleFilter().contains(ate.getElement())) {
						label = ate.getElement() + "\\n" + ate.getType();
					} else {
						label = "";
					}
					/**
					 * Note: if the transition already exists, a new one will
					 * not be added.
					 */
					fsm.addTransition(fromState, toState, label);

					if (i == 0) {
						FSMState state = fsm.getState(fromState.getPayload());
						fsm.setStartState(state);
					} else if (i == atel.size() - 1) {
						FSMState state = fsm.getState(toState.getPayload());
						fsm.addAcceptState(state);
					}
				}
			}
		} catch (Exception e) {
			Message.add(e.toString(), Message.ERROR);
			return null;
		}

		// Now apply selected modifications.
		FsmModificationSettings modificationSettings = settings
				.getModificationSettings();
		FsmModificationConvertor modificationConvertor = new FsmModificationConvertor();
		fsm = modificationConvertor.convert(fsm, modificationSettings);

		// Construct the FSM result.
		return new FsmResult(fsm);
	}

	/**
	 * Collect infromation for the payload of a given state by taking backwards
	 * step.
	 * 
	 * @param atel
	 *            AuditTrailEntryList the list of audti trail entries for this
	 *            process instance.
	 * @param i
	 *            int the index of the current audit trail entry in this list.
	 * @param offset
	 *            int offset from i to start taking steps.
	 * @param settings
	 *            FsmHorizonSettings the settings to use.
	 * @param payload
	 *            FsmMinerPayload the payload to store the results in.
	 * @param mode
	 *            int whether to collect model element info, originator info, or
	 *            event type info.
	 */
	private static void mineBwd(AuditTrailEntryList atel, int i, int offset,
			FsmHorizonSettings settings, FsmMinerPayload payload, int mode) {
		// Skip if these settings should not be used.
		if (settings.getUse()) {
			int k = 0;
			// Initialize number of visible steps to take.
			int horizon = settings.getFilteredHorizon();
			for (int j = i - offset; horizon != 0 && j >= 0; j--) {
				try {
					// Get the audit trail entry.
					AuditTrailEntry ate2 = atel.get(j);
					// Get the info. This depends on mode.
					String s;
					switch (mode) {
					case (FsmMinerPayload.MODELELEMENT): {
						s = ate2.getElement();
						break;
					}
					case (FsmMinerPayload.ORIGINATOR): {
						s = ate2.getOriginator();
						break;
					}
					case (FsmMinerPayload.EVENTTYPE): {
						s = ate2.getType();
						break;
					}
					default: {
						s = "";
					}
					}
					// Check whether not filtered out.
					if (settings.getFilter().contains(s)) {
						// Not filtered out, add to payload if number of steps
						// not exceeded.
						if (settings.getHorizon() < 0
								|| settings.getHorizon() + j + offset > i) {
							switch (settings.getAbstraction()) {
							case (FsmHorizonSettings.SEQ): {
								payload.addBwdSeq(mode, s);
								break;
							}
							case (FsmHorizonSettings.SET): {
								payload.addBwdSet(mode, s);
								break;
							}
							case (FsmHorizonSettings.BAG): {
								payload.addBwdBag(mode, s);
								break;
							}
							}
						}
						// Found an unfiltered step: decrease counter.
						horizon--;
					}
				} catch (Exception e) {
					Message.add(e.toString(), Message.ERROR);
				}
			}
		}
	}

	/**
	 * Collect infromation for the payload of a given state by taking forward
	 * steps.
	 * 
	 * @param atel
	 *            AuditTrailEntryList the list of audti trail entries for this
	 *            process instance.
	 * @param i
	 *            int the index of the current audit trail entry in this list.
	 * @param offset
	 *            int offset from i to start taking steps.
	 * @param settings
	 *            FsmHorizonSettings the settings to use.
	 * @param payload
	 *            FsmMinerPayload the payload to store the results in.
	 * @param mode
	 *            int whether to collect model element info, originator info, or
	 *            event type info.
	 */
	private static void mineFwd(AuditTrailEntryList atel, int i, int offset,
			FsmHorizonSettings settings, FsmMinerPayload payload, int mode) {
		/**
		 * See MineBwd for adiditonal comments.
		 */
		if (settings.getUse()) {
			int k = 0;
			int horizon = settings.getFilteredHorizon();
			for (int j = i + offset; horizon != 0 && j < atel.size(); j++) {
				try {
					AuditTrailEntry ate2 = atel.get(j);
					String s;
					switch (mode) {
					case (FsmMinerPayload.MODELELEMENT): {
						s = ate2.getElement();
						break;
					}
					case (FsmMinerPayload.ORIGINATOR): {
						s = ate2.getOriginator();
						break;
					}
					case (FsmMinerPayload.EVENTTYPE): {
						s = ate2.getType();
						break;
					}
					default: {
						s = "";
					}
					}
					if (settings.getFilter().contains(s)) {
						if (settings.getHorizon() < 0
								|| settings.getHorizon() + i + offset > j) {
							switch (settings.getAbstraction()) {
							case (FsmHorizonSettings.SEQ): {
								payload.addFwdSeq(mode, s);
								break;
							}
							case (FsmHorizonSettings.SET): {
								payload.addFwdSet(mode, s);
								break;
							}
							case (FsmHorizonSettings.BAG): {
								payload.addFwdBag(mode, s);
								break;
							}
							}
						}
						horizon--;
					}
				} catch (Exception e) {
					Message.add(e.toString(), Message.ERROR);
				}
			}
		}
	}
}
