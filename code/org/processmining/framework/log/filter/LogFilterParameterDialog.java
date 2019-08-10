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

package org.processmining.framework.log.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.ProMHTMLEditorKit;
import org.processmining.framework.util.CenterOnScreen;

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
public abstract class LogFilterParameterDialog extends JDialog {

	private boolean ok = false;
	protected LogSummary summary;
	private JPanel abstrPanel;
	private JTextField text;

	protected LogFilter filter;

	public LogFilterParameterDialog(LogSummary summary, LogFilter filter) {
		super(MainUI.getInstance(), "Change settings", true);
		setUndecorated(false);

		this.filter = filter;

		JButton okButton = new JButton("    Ok    ");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = getAllParametersSet();
				setVisible(false);
			}
		});
		JButton helpButton = new JButton("   Help   ");
		helpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showHelpDialog(LogFilterParameterDialog.this.filter);
			}
		});

		JButton cancelButton = new JButton("  Cancel  ");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = false;
				setVisible(false);
			}
		});

		this.getContentPane().setLayout(new BorderLayout());
		JPanel p = new JPanel();
		p.add(okButton);
		p.add(cancelButton);
		p.add(helpButton);
		this.getContentPane().add(p, BorderLayout.SOUTH);

		text = new JTextField(filter.getName());
		JPanel p2 = new JPanel(new BorderLayout());
		p2.add(new JLabel("Set desired label: "), BorderLayout.WEST);
		p2.add(text, BorderLayout.CENTER);
		p2.add(new JLabel("  "), BorderLayout.SOUTH);
		this.getContentPane().add(p2, BorderLayout.NORTH);

		this.summary = summary;
		abstrPanel = getPanel();
		this.getContentPane().add(new JScrollPane(abstrPanel),
				BorderLayout.CENTER);

		pack();
		CenterOnScreen.center(this);
	}

	public boolean showDialog() {
		if (abstrPanel == null) {
			ok = true;
			return ok;
		}
		setVisible(true);
		return ok;
	}

	public void showHelpDialog(LogFilter f) {
		final int width = 600;
		int height = 400;
		JButton closeButton = new JButton("  Close  ");
		final JDialog d = new JDialog(MainUI.getInstance(), "Log Filter Help",
				true);
		d.getContentPane().setLayout(new BorderLayout());
		JEditorPane jtp = new JEditorPane() {
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}
		};
		jtp.setEditorKit(new ProMHTMLEditorKit(System.getProperty("user.dir")));
		jtp.setText("<html>" + f.getHelp() + "</html>");
		jtp.setEditable(false);
		jtp.setBackground(d.getBackground());
		jtp.setFont(closeButton.getFont());
		JScrollPane sp = new JScrollPane(jtp,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		d.getContentPane().add(sp, BorderLayout.CENTER);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				d.setVisible(false);
			}
		});
		JPanel p = new JPanel();
		p.add(closeButton);
		d.getContentPane().add(p, BorderLayout.SOUTH);
		d.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		d.setBounds(Math.max(0, (screenSize.width - width) / 2), Math.max(0,
				(screenSize.height - height) / 2), width, height);
		d.setSize(width, height);
		d.setLocation(Math.max(0, (screenSize.width - width) / 2), Math.max(0,
				(screenSize.height - height) / 2));

		d.setVisible(true);

	}

	protected abstract boolean getAllParametersSet();

	/**
	 * Returns a settings panel of the LogFilter filter. Note that this should
	 * be implemented in such a way that settings that have been made once are
	 * reflected when it is called again!
	 * 
	 * @return JPanel
	 */
	protected abstract JPanel getPanel();

	public JPanel getConfigurationPanel() {
		return getPanel();
	}

	public boolean isAllParametersSet() {
		return getAllParametersSet();
	}

	public abstract LogFilter getNewLogFilter();

	public String getLabel() {
		return text.getText();
	}

}
