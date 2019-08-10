package org.processmining.framework.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Utils;

public class ShowNavigationPanelAction extends CatchOutOfMemoryAction {

	private static final long serialVersionUID = 8243503602805411509L;

	public ShowNavigationPanelAction(MDIDesktopPane desktop) {
		super("Toggle navigation panel...", Utils.getStandardIcon(
				"toolbar_navigation.png", "general/History24"), desktop);
		putValue(SHORT_DESCRIPTION,
				"Show or hide the navigation panel on the right side");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
	}

	@Override
	protected void execute(ActionEvent e) {
		MainUI.getInstance().toggleNavigationPanelVisible();
	}

	@Override
	protected void handleOutOfMem() {
	}
}
