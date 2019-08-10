package org.processmining.framework.ui.slicker.logdialog;

import java.io.IOException;

import javax.swing.AbstractListModel;

import org.processmining.framework.log.AuditTrailEntryList;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class AuditTrailEntryListModel extends AbstractListModel {

	protected AuditTrailEntryList ateList;

	public AuditTrailEntryListModel(AuditTrailEntryList list) {
		ateList = list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int index) {
		try {
			return ateList.get(index);
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		return ateList.size();
	}

}
