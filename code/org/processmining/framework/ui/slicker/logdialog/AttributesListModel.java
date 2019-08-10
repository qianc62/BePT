package org.processmining.framework.ui.slicker.logdialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.swing.AbstractListModel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.ProcessInstance;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class AttributesListModel extends AbstractListModel {

	protected Map<String, String> attributes;
	protected ArrayList<String> keys;

	protected AttributesListModel(Map<String, String> attributes) {
		if (attributes != null) {
			this.attributes = attributes;
			this.keys = new ArrayList<String>(attributes.keySet());
			Collections.sort(this.keys);
		} else {
			this.attributes = null;
			this.keys = null;
		}
	}

	public AttributesListModel(AuditTrailEntry ate) {
		this(ate == null ? null : ate.getAttributes());
	}

	public AttributesListModel(ProcessInstance instance) {
		this(instance == null ? null : instance.getAttributes());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int index) {
		return "<html><b>" + keys.get(index) + "</b>:  "
				+ attributes.get(keys.get(index)) + "</html>";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		if (keys != null) {
			return keys.size();
		} else {
			return 0;
		}
	}

}