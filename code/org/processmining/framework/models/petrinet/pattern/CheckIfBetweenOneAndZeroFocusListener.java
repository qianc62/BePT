/**
 * 
 */
package org.processmining.framework.models.petrinet.pattern;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class CheckIfBetweenOneAndZeroFocusListener implements FocusListener {

	private final JTextField field;

	public CheckIfBetweenOneAndZeroFocusListener(JTextField field) {
		this.field = field;
	}

	public void focusGained(FocusEvent arg0) {
	}

	public void focusLost(FocusEvent arg0) {
		Double d = null;
		try {
			d = Double.parseDouble(field.getText());
		} catch (Throwable t) {
			field.setText("1.0");
		}
		if (d <= 0 || d > 1)
			field.setText("1.0");
	}

}