package org.processmining.framework.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.Utils;

public class ShowMessagesAction extends CatchOutOfMemoryAction {

	private static final long serialVersionUID = -5990568245469944092L;

	public static Icon getIcon() {
		String customIconPath = UISettings.getInstance()
				.getPreferredIconTheme()
				+ "/toolbar_messages.png";
		if ((new File(customIconPath).exists())) {
			return new ImageIcon(customIconPath);
		} else {
			return Utils.getStandardIcon("general/Open24");
		}
	}

	public ShowMessagesAction(MDIDesktopPane desktop, String label) {
		super(label, desktop);
		putValue(SHORT_DESCRIPTION, "Show or hide the message console");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_K));
	}

	public ShowMessagesAction(MDIDesktopPane desktop) {
		super("Toggle console...", ShowMessagesAction.getIcon(), desktop);
		putValue(SHORT_DESCRIPTION, "Show or hide the message console");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_K));
	}

	@Override
	protected void execute(ActionEvent e) {
		MainUI.getInstance().toggleMessagesVisible();
	}

	@Override
	protected void handleOutOfMem() {
		// TODO Auto-generated method stub

	}

}
