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

package org.processmining.framework.ui;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.processmining.framework.ui.filters.GenericFileFilter;

public class Utils {
	private Utils() {
	}

	public static Icon getStandardIcon(String filename,
			String defaultJavaIconName) {
		String customIconPath = UISettings.getInstance()
				.getPreferredIconTheme()
				+ "/" + filename;
		String defaultIconPath = UISettings.getInstance().getDefaultIconPath()
				+ "/" + filename;

		if ((new File(customIconPath).exists())) {
			return new ImageIcon(customIconPath);
		} else if ((new File(defaultIconPath).exists())) {
			return new ImageIcon(defaultIconPath);
		} else {
			return getStandardIcon(defaultJavaIconName);
		}
	}

	public static synchronized Icon getStandardIcon(String name) {
		String imgLocation = "toolbarButtonGraphics/" + name + ".gif";
		java.net.URL imageURL = Thread.currentThread().getContextClassLoader()
				.getResource(imgLocation);

		if (imageURL == null) {
			Message.add("Resource not found: " + imgLocation, Message.ERROR);
			return null;
		} else {
			return new ImageIcon(imageURL);
		}
	}

	public static synchronized String openImportFileDialog(Component parent,
			javax.swing.filechooser.FileFilter filter) {
		JFileChooser chooser = new JFileChooser(UISettings.getInstance()
				.getLastOpenedImportFile());

		chooser.setFileFilter(filter);
		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			return filter instanceof GenericFileFilter ? ((GenericFileFilter) filter)
					.addExtension(chooser.getSelectedFile().getPath())
					: chooser.getSelectedFile().getPath();
		}
		return "";
	}

	public static synchronized String saveFileDialog(Component parent,
			javax.swing.filechooser.FileFilter filter) {
		JFileChooser chooser = new JFileChooser(UISettings.getInstance()
				.getLastExportLocation());

		chooser.setFileFilter(filter);
		if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			UISettings.getInstance().setLastExportLocation(
					chooser.getCurrentDirectory());
			return filter instanceof GenericFileFilter ? ((GenericFileFilter) filter)
					.addExtension(chooser.getSelectedFile().getPath())
					: chooser.getSelectedFile().getPath();
		}
		return "";
	}

	public static synchronized String getTempPath() {
		String path = System.getProperty("java.io.tmpdir", "");
		String sep = System.getProperty("file.separator", "\\");

		return path.endsWith(sep) ? path : path + sep;
	}
}
