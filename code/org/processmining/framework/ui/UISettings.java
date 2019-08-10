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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.processmining.framework.ui.menus.LogReaderMenu;
import org.processmining.framework.util.RuntimeUtils;

/**
 * Saves and loads UI settings from an .ini file.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public class UISettings {

	private static String settingsFile;
	private static String executionLogFile;
	private static UISettings instance = null;

	public final static String LOGTYPE = "MXML Log File";

	private Properties settings;

	public static String getProMDirectoryPath() {
		String promDir = System.getProperty("user.dir");
		if (promDir.endsWith(".")) {
			promDir = promDir.substring(0, promDir.length() - 1);
		}
		if (promDir.endsWith(System.getProperty("file.separator")) == false) {
			promDir += System.getProperty("file.separator");
		}
		return promDir;
	}

	private UISettings() {
		settingsFile = System.getProperty("user.home", "");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			settingsFile += "/Library/Application Support/ProM";
			(new File(settingsFile)).mkdirs();
		}
		if (!settingsFile.equals("")) {
			settingsFile += System.getProperty("file.separator", "");
		}
		settingsFile += "processmining.ini";

		executionLogFile = System.getProperty("user.home", "");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			executionLogFile += "/Library/Application Support/ProM";
		}
		if (!executionLogFile.equals("")) {
			executionLogFile += System.getProperty("file.separator", "");
		}
		executionLogFile += "ProM execution log";

		load();
	}

	public String getExecutionLogFileName() {
		return executionLogFile;
	}

	/**
	 * Provides the path to the last export location.
	 * 
	 * @param folder
	 *            the folder to which the last file was exported
	 */
	public void setLastExportLocation(File folder) {
		settings.setProperty("last_export_location", folder.getAbsolutePath());
		save();
	}

	/**
	 * Retrieves the last export location from the ini file.
	 * 
	 * @return the folder to which the last file was exported
	 */
	public File getLastExportLocation() {
		return new File(settings.getProperty("last_export_location", System
				.getProperty("user.dir")));
	}

	public String getLastOpenedLogFile() {
		return settings.getProperty("last_log_file", "");
	}

	public String getLastOpenedImportFile() {
		return settings.getProperty("last_import_file", "");
	}

	public void setLastOpenedLogFile(String name) {
		settings.setProperty("last_log_file", name);
		save();
	}

	public void setLastOpenedImportFile(String name) {
		settings.setProperty("last_import_file", name);
		save();
	}

	public String getLastUsedAlgorithm() {
		return settings.getProperty("last_algorithm", "");
	}

	public void setLastUsedAlgorithm(String name) {
		settings.setProperty("last_algorithm", name == null ? "" : name);
		save();
	}

	public String getLastUsedAnalysis() {
		return settings.getProperty("last_analysis", "");
	}

	public String getLastUsedConversion() {
		return settings.getProperty("last_conversion", "");
	}

	public String getLastExecutionID() {
		return settings.getProperty("last_exec_id", "-1");
	}

	public boolean getKeepHistory() {
		/*
		 * REMOVED FOR RELEASE THE FUNCTIONALITY TO KEEP HISTORY return
		 * Boolean.valueOf(settings.getProperty("keep_log",
		 * "True")).booleanValue();
		 */
		return Boolean.valueOf(settings.getProperty("keep_log", "True"))
				.booleanValue();
	}

	public boolean getTest() {
		return Boolean.valueOf(settings.getProperty("test", "False"))
				.booleanValue();
	}

	public int getBackgroundRefreshRate() {
		return Integer.valueOf(
				settings.getProperty("backgroundrefreshrate", "10")).intValue();
	}

	public void setLastUsedAnalysis(String name) {
		settings.setProperty("last_analysis", name == null ? "" : name);
		save();
	}

	public void setLastUsedConversion(String name) {
		settings.setProperty("last_conversion", name == null ? "" : name);
		save();
	}

	public void setLastExecutionID(String name) {
		settings.setProperty("last_exec_id", name == null ? "-1" : name);
		save();
	}

	public void setKeepHistory(boolean keepHistory) {
		settings.setProperty("keep_log", Boolean.toString(keepHistory));
		save();
	}

	public void setPreferredLogReader(String logReaderId) {
		settings.setProperty("log_reader_implementation", logReaderId);
		save();
	}

	public String getPreferredLogReader() {
		return settings.getProperty("log_reader_implementation",
				LogReaderMenu.ACTION_BUFFERED_NIKEFS2_AGGRESSIVE);
	}

	public void setPreferredIconTheme(String iconThemePath) {
		if (iconThemePath.contains(File.separator)) {
			iconThemePath = iconThemePath.substring(iconThemePath
					.lastIndexOf(File.separatorChar) + 1);
		}
		settings.setProperty("icon_theme", iconThemePath);
		save();
	}

	public String getPreferredIconTheme() {
		String iconThemePath = UISettings.getProMDirectoryPath()
				+ "images/icons/" + settings.getProperty("icon_theme", "1984");
		if ((new File(iconThemePath)).exists() == false) {
			iconThemePath = UISettings.getProMDirectoryPath()
					+ "images/icons/1984";
		}
		return iconThemePath;
	}

	public String getDefaultIconPath() {
		return UISettings.getProMDirectoryPath() + "images/icons";
	}

	public void setPreferredDesktopBackground(String desktopBgPath) {
		if (desktopBgPath.contains(File.separator)) {
			desktopBgPath = desktopBgPath.substring(desktopBgPath
					.lastIndexOf(File.separatorChar) + 1);
		}
		settings.setProperty("desktop_bg", desktopBgPath);
		save();
	}

	public String getPreferredDesktopBackground() {
		if (settings.getProperty("desktop_bg", "wooden desk.jpg")
				.equals("NONE")) {
			return "NONE";
		}
		String desktopPath = UISettings.getProMDirectoryPath()
				+ "images/desktop/"
				+ settings.getProperty("desktop_bg", "wooden desk.jpg");
		if ((new File(desktopPath)).exists() == false) {
			desktopPath = UISettings.getProMDirectoryPath()
					+ "images/desktop/wooden desk.jpg";
		}
		return desktopPath;
	}

	public void setCustomDotLocation(String dotLocation) {
		settings.setProperty("custom_dot_executable_location", dotLocation);
		save();
	}

	public String getCustomDotLocation() {
		String location = settings
				.getProperty("custom_dot_executable_location");
		if (location != null && location.length() > 0) {
			return location;
		} else {
			return null;
		}
	}

	public File getCustomDotExecutable() {
		String customDot = getCustomDotLocation();
		if (customDot != null) {
			File dot = new File(customDot);
			if (dot.exists()) {
				return dot;
			}
		}
		return null;
	}

	public static UISettings getInstance() {
		if (instance == null) {
			instance = new UISettings();
		}
		return instance;
	}

	// Variable used to make sure that the check for existence of recent
	// files is executed only once per session. (network drives may
	// cause this check to be very slow in Windows)
	private static boolean FIRST_CALL = true;

	// returns an ArrayList of String[2], where
	// ArrayList.get(i)[0] is the file name of the i'th file
	// ArrayList.get(i)[1] is the algorithm used to open of the i'th file
	public ArrayList getRecentFiles() {
		int numFiles = Integer.parseInt(settings.getProperty(
				"num_recent_files", "0"));
		ArrayList result = new ArrayList();

		for (int i = numFiles - 1; i >= 0; i--) {
			String[] file = new String[2];
			Iterator iterator = result.iterator();
			boolean duplicate = false;

			file[0] = settings.getProperty("recent_file_name_" + i);
			file[1] = settings.getProperty("recent_file_algo_" + i);
			if (file[1] == null) {
				file[1] = LOGTYPE;
			}
			if (file[0] != null && recentFileExists(file[0])) {
				while (iterator.hasNext() && !duplicate) {
					String[] s = (String[]) iterator.next();
					duplicate |= s[0].equals(file[0]) && s[1].equals(file[1]);
				}
				if (!duplicate) {
					result.add(file);
				}
			}
		}
		FIRST_CALL = false;
		return result;
	}

	protected boolean recentFileExists(String fileName) {
		if (!FIRST_CALL) {
			return true;
		}
		if (fileName == null) {
			return false;
		} else if ((new File(fileName)).exists() == true) {
			return true;
		} else if (fileName.startsWith("zip://")) {
			String zipFileName = fileName.substring(6, fileName
					.lastIndexOf('#'));
			if ((new File(zipFileName)).exists() == true) {
				return true;
			}
		}
		return false;
	}

	public void addRecentFile(String file, String algorithm) {
		int numFiles = Integer.parseInt(settings.getProperty(
				"num_recent_files", "0"));

		settings.setProperty("recent_file_name_" + numFiles, file);
		if (algorithm != null) {
			settings.setProperty("recent_file_algo_" + numFiles, algorithm);
		}

		numFiles++;
		settings.setProperty("num_recent_files", "" + numFiles);
		save();
	}

	public void save() {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(settingsFile);
			settings.store(out, null);
			out.close();
		} catch (IOException ex) {
		}
	}

	private void load() {
		FileInputStream in = null;

		settings = new Properties();
		try {
			in = new FileInputStream(settingsFile);
			settings.load(in);
			in.close();
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		}
	}
}
