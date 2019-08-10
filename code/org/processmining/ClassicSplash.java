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

package org.processmining;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import org.processmining.framework.ui.About;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;

public class ClassicSplash extends JWindow implements ProMSplash {

	private int x, y;
	private int width = 400;
	private int height = 300;

	private JLabel label = new JLabel("   ");
	private ArrayList log;

	public ClassicSplash() {
		super();
		log = new ArrayList();
		log.add("** Starting plugin import **");
		log.add(new Integer(Message.NORMAL));

		String name = System.getProperty("user.dir", "")
				+ System.getProperty("file.separator") + "images"
				+ System.getProperty("file.separator") + "icon48.gif";

		// load custom app icon, if available
		String customIconPath = UISettings.getInstance()
				.getPreferredIconTheme()
				+ "/applogo_small.png";
		if ((new File(customIconPath)).exists()) {
			name = customIconPath;
		}

		getRootPane().setPreferredSize(new Dimension(width, height));
		getRootPane().setSize(new Dimension(width, height));
		getRootPane().setBorder(BorderFactory.createEtchedBorder());

		JPanel p = new JPanel(new FlowLayout());
		p.add(new JLabel(About.NAME_FULL));
		getRootPane().getContentPane().add(p, BorderLayout.NORTH);

		JPanel p2 = new JPanel(new BorderLayout());

		p2.add(new JLabel(new ImageIcon(name)), BorderLayout.CENTER);

		p = new JPanel(new FlowLayout());
		p.add(new JLabel("<html>Please wait while loading plugins.</html>"));
		p2.add(p, BorderLayout.SOUTH);

		getRootPane().getContentPane().add(p2, BorderLayout.CENTER);

		p = new JPanel(new FlowLayout());
		p.add(label);
		getRootPane().getContentPane().add(p, BorderLayout.SOUTH);

		pack();

		width = getRootPane().getContentPane().getWidth();
		height = getRootPane().getContentPane().getHeight();

		x = (Toolkit.getDefaultToolkit().getScreenSize().width - width) / 2;
		y = (Toolkit.getDefaultToolkit().getScreenSize().height - height) / 2;
		open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.PSplash#open()
	 */
	public void open() {
		setBounds(x, y, width, height);
		setVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.PSplash#close()
	 */
	public void close() {
		setVisible(false);
		dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.PSplash#changeText(java.lang.String, int)
	 */
	public void changeText(String s, int status) {
		label.setText(s);
		log.add("* " + s);
		log.add(new Integer(status));
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.PSplash#getLog()
	 */
	public ArrayList getLog() {
		return log;
	}

	public void setProgress(int progress) {
		// TODO Auto-generated method stub

	}
}
