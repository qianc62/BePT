/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.epcpack;

import java.util.*;

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
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class ConfigurableEPCConfiguration {

	public final static Integer OR_AS_OR = new Integer(1);
	public final static Integer OR_AS_XOR = new Integer(2);
	public final static Integer OR_AS_AND = new Integer(4);
	public final static Integer OR_AS_SEQ = new Integer(8);

	public final static Integer XOR_AS_XOR = new Integer(16);
	public final static Integer XOR_AS_SEQ = new Integer(32);

	public final static Integer AND_AS_AND = new Integer(64);

	public final static Integer FUN_AS_ON = new Integer(128);
	public final static Integer FUN_AS_OFF = new Integer(256);
	public final static Integer FUN_AS_OPT = new Integer(512);

	private ConfigurableEPC epc;

	private HashMap configMapping = new HashMap();
	private HashMap sequenceMapping = new HashMap();

	public ConfigurableEPCConfiguration(ConfigurableEPC epc) {
		this.epc = epc;
	}

	/**
	 * Returns true if applying this configuration to its EPC yields an EPC
	 * where no object is configured anymore.
	 * 
	 * @return boolean
	 */
	public boolean isComplete() {
		ArrayList a = new ArrayList();
		a.addAll(epc.getConfigurableObjects());
		// if the collection changes when one object is not present in the
		// configMapping
		// return false
		if (a.retainAll(configMapping.keySet())) {
			return false;
		}
		// All functions should be ON for now
		Iterator it = configMapping.values().iterator();
		while (it.hasNext()) {
			Integer value = (Integer) it.next();
			if ((value == FUN_AS_OPT) || (value == FUN_AS_OFF)) {
				return false;
			}
		}
		return true;
	}

	public Integer addConfigItem(EPCConfigurableObject object,
			Integer configuration) {
		if (!object.isConfigurable()) {
			return null;
		}
		if (configuration.equals(OR_AS_SEQ) || configuration.equals(XOR_AS_SEQ)) {
			return null;
		}
		eraseConfig(object);
		configMapping.put(new Integer(object.getId()), configuration);
		return configuration;
	}

	public Integer addConfigItem(EPCConfigurableObject object,
			Integer configuration, EPCObject sequenceObject) {
		if (!object.isConfigurable()) {
			return null;
		}
		if ((configuration.equals(OR_AS_SEQ) || configuration
				.equals(XOR_AS_SEQ))
				&& sequenceObject != null) {
			eraseConfig(object);
			configMapping.put(new Integer(object.getId()), configuration);
			sequenceMapping.put(new Integer(object.getId()), sequenceObject);
			return configuration;
		}
		return null;
	}

	private void eraseConfig(EPCConfigurableObject object) {
		configMapping.remove(object);
		sequenceMapping.remove(object);
	}

	public Integer getConfiguration(EPCConfigurableObject object) {
		return (Integer) configMapping.get(object);
	}

	public Integer getSequence(EPCConfigurableObject object) {
		return (Integer) sequenceMapping.get(object);
	}

}
