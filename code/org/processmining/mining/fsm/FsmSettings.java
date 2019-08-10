package org.processmining.mining.fsm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;

import org.processmining.framework.log.LogSummary;
import org.processmining.converting.fsm.FsmModificationSettings;

/**
 * <p>
 * Title: FSMSettings
 * </p>
 * 
 * <p>
 * Description: Holds settings for the FSM miner
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
public class FsmSettings {

	// Backward horizon settings.
	private FsmHorizonSettings[] bwdHorizonSettings = new FsmHorizonSettings[FsmMinerPayload.LAST];
	// Forward horizon settings.
	private FsmHorizonSettings[] fwdHorizonSettings = new FsmHorizonSettings[FsmMinerPayload.LAST];

	// Whether the attribute settings have been selected for use.
	private Boolean useAttributeSettings;
	// The attribute seetings.
	private HashMap<String, HashMap<String, String>> attributeSettings;

	// The general filter for model elements.
	private HashSet<String> visibleFilter;

	private FsmModificationSettings modificationSettings;

	private boolean gui;

	public FsmSettings(LogSummary summary) {
		// Initialize the horizon settings.
		for (int mode = 0; mode < FsmMinerPayload.LAST; mode++) {
			bwdHorizonSettings[mode] = new FsmHorizonSettings();
			fwdHorizonSettings[mode] = new FsmHorizonSettings();
		}

		// Initially, do not use the attribute settings.
		useAttributeSettings = false;
		// Initialize attribute settings.
		attributeSettings = new HashMap<String, HashMap<String, String>>();

		// Initialize general model element filter.
		/**
		 * Transitions in the resulting FSM will be labeled by the model
		 * elements. Irrelevant model elements can be filtered out by this
		 * filter. Typically, model elemtns that were filtered out result in
		 * unlabeled transitions. As only one transition with a certain label
		 * (unlabeled corresponds to an empty label), restricting this filter
		 * one obtains less transitions in the resulting FSM.
		 * 
		 * For originators and event types it makes no sense to have similar
		 * filters.
		 */
		visibleFilter = new HashSet<String>();

		// Initialize the filters for all horizon settings.
		bwdHorizonSettings[FsmMinerPayload.MODELELEMENT].addAllToFilter(summary
				.getModelElements());
		fwdHorizonSettings[FsmMinerPayload.MODELELEMENT].addAllToFilter(summary
				.getModelElements());
		bwdHorizonSettings[FsmMinerPayload.ORIGINATOR].addAllToFilter(summary
				.getOriginators());
		fwdHorizonSettings[FsmMinerPayload.ORIGINATOR].addAllToFilter(summary
				.getOriginators());
		bwdHorizonSettings[FsmMinerPayload.EVENTTYPE].addAllToFilter(summary
				.getEventTypes());
		fwdHorizonSettings[FsmMinerPayload.EVENTTYPE].addAllToFilter(summary
				.getEventTypes());

		modificationSettings = new FsmModificationSettings();

		// By default, assume there's a GUI to report to.
		gui = true;
	}

	/**
	 * Gets the given horizon setting.
	 * 
	 * @param isBwd
	 *            boolean if true, then get backward horizon setting, else get
	 *            forward horizon setting.
	 * @param mode
	 *            int if FsmMinerPayload.MODELELEMENT then get model element
	 *            horizon setting, etc.
	 * @return FsmHorizonSettings possibly null.
	 */
	public FsmHorizonSettings getHorizonSettings(boolean isBwd, int mode) {
		FsmHorizonSettings settings = null;
		if (mode >= 0 && mode < FsmMinerPayload.LAST) {
			if (isBwd) {
				settings = bwdHorizonSettings[mode];
			} else {
				settings = fwdHorizonSettings[mode];
			}
		}
		return settings;
	}

	/**
	 * Sets the given horizon setting.
	 * 
	 * @param isBwd
	 *            boolean if true, then get backward horizon setting, else get
	 *            forward horizon setting.
	 * @param mode
	 *            int if FsmMinerPayload.MODELELEMENT then get model element
	 *            horizon setting, etc.
	 * @param newSettings
	 *            FsmHorizonSettings new settings.
	 * @return FsmHorizonSettings old settings, possibly null.
	 */
	public FsmHorizonSettings setHorizonSettings(boolean isBwd, int mode,
			FsmHorizonSettings newSettings) {
		FsmHorizonSettings oldSettings = null;
		if (mode >= 0 && mode < FsmMinerPayload.LAST) {
			if (isBwd) {
				oldSettings = bwdHorizonSettings[mode];
				bwdHorizonSettings[mode] = newSettings;
			} else {
				oldSettings = fwdHorizonSettings[mode];
				fwdHorizonSettings[mode] = newSettings;
			}
		}
		return oldSettings;
	}

	/**
	 * Gets whether the attribute settings have been selected for use.
	 * 
	 * @return boolean
	 */
	public boolean getUseAttributes() {
		return useAttributeSettings;
	}

	/**
	 * Sets whether the attribute settings have been selected for use.
	 * 
	 * @param newUse
	 *            boolean new value.
	 * @return boolean old value.
	 */
	public boolean setUseAttributes(boolean newUse) {
		boolean oldUse = useAttributeSettings;
		useAttributeSettings = newUse;
		return oldUse;
	}

	/**
	 * Gets the attribute settings.
	 * 
	 * @return Map
	 */
	public Map<String, HashMap<String, String>> getAttributeSettings() {
		return attributeSettings;
	}

	/**
	 * Sets the attribute settings.
	 * 
	 * @param newMap
	 *            HashMap new settings.
	 * @return Map old settings.
	 */
	public Map<String, HashMap<String, String>> setAttributeSettings(
			HashMap<String, HashMap<String, String>> newMap) {
		Map<String, HashMap<String, String>> oldMap = attributeSettings;
		attributeSettings = newMap;
		return oldMap;
	}

	/**
	 * Gets the general filter for model elements.
	 * 
	 * @return Collection
	 */
	public Collection<String> getVisibleFilter() {
		return visibleFilter;
	}

	/**
	 * Sets the general filter for model elements.
	 * 
	 * @param newFilter
	 *            HashSet new filter.
	 * @return Collection old filter.
	 */
	public Collection<String> SetVisibleFilter(HashSet<String> newFilter) {
		HashSet<String> oldFilter = visibleFilter;
		visibleFilter = newFilter;
		return oldFilter;
	}

	public FsmModificationSettings getModificationSettings() {
		return modificationSettings;
	}

	public FsmModificationSettings setModificationSettings(
			FsmModificationSettings newSettings) {
		FsmModificationSettings oldSettings = modificationSettings;
		modificationSettings = newSettings;
		return oldSettings;
	}

	/**
	 * Gets whether we have a GUI to report to.
	 * 
	 * @return boolean
	 */
	public boolean hasGUI() {
		return gui;
	}

	/**
	 * Sets whether we have a GUI to teport to.
	 * 
	 * @param newGUI
	 *            boolean new value.
	 * @return boolean old value.
	 */
	public boolean setHasGUI(boolean newGUI) {
		boolean oldGUI = gui;
		gui = newGUI;
		return oldGUI;
	}
}
