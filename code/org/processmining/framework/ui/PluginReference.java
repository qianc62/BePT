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

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.AnalysisPluginCollection;
import org.processmining.converting.ConvertingPlugin;
import org.processmining.converting.ConvertingPluginCollection;
import org.processmining.exporting.ExportPlugin;
import org.processmining.exporting.ExportPluginCollection;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.filter.LogFilterCollection;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.PluginCollection;
import org.processmining.framework.util.BrowserLauncher;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.importing.ImportPlugin;
import org.processmining.importing.ImportPluginCollection;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningPluginCollection;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class PluginReference extends JFrame {

	public final static String INDEX = "plugin://";

	private JEditorPane jep = new JEditorPane();

	public PluginReference() {
		try {
			jbInit();
			CenterOnScreen.center(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void show(Plugin plugin) {
		// test start
		if (plugin != null) {
			String html = plugin.getHtmlDescription();
			if (html.startsWith("http://")) {
				// external url, launch in browser
				BrowserLauncher.openURL(html);
				return;
			}
		}
		// test end

		String page = "plugin://";

		if (plugin != null) {
			if (plugin instanceof MiningPlugin) {
				page += "mining/" + plugin.getName();
			} else if (plugin instanceof AnalysisPlugin) {
				page += "analysis/" + plugin.getName();
			} else if (plugin instanceof ExportPlugin) {
				page += "export/" + plugin.getName();
			} else if (plugin instanceof ImportPlugin) {
				page += "import/" + plugin.getName();
			} else if (plugin instanceof ConvertingPlugin) {
				page += "convert/" + plugin.getName();
			}
		}
		show(page);
	}

	public void show(String page) {
		setPage(page);
		CenterOnScreen.center(this);
		setVisible(true);
	}

	private void setPage(String url) {
		// check for URL
		if (url.startsWith("http://")) {
			// external url, launch in browser
			BrowserLauncher.openURL(url);
			return;
		} else if (url.startsWith("plugin://")) {
			url = url.substring("plugin://".length());

			String type = "", name = "";
			Plugin plugin = null;

			if (url.indexOf('/') > 0) {
				type = url.substring(0, url.indexOf('/'));
				name = url.substring(url.indexOf('/') + 1);

				if (type.equals("logfilter")) {
					LogFilter f = LogFilterCollection.getInstance().get(name);
					String URLBase = f.getClass().getName();
					URLBase = URLBase.substring(URLBase.lastIndexOf(".") + 1)
							+ File.separator;
					jep.setEditorKit(new ProMHTMLEditorKit(About.HELPLOCATION()
							+ URLBase));
					jep.setText(getFilterPage(f, type));
				} else {
					if (type.equals("mining")) {
						plugin = MiningPluginCollection.getInstance().get(name);
					} else if (type.equals("analysis")) {
						plugin = AnalysisPluginCollection.getInstance().get(
								name);
					} else if (type.equals("export")) {
						plugin = ExportPluginCollection.getInstance().get(name);
					} else if (type.equals("import")) {
						plugin = ImportPluginCollection.getInstance().get(name);
					} else if (type.equals("convert")) {
						plugin = ConvertingPluginCollection.getInstance().get(
								name);
					}
					String URLBase = plugin.getClass().getName();
					URLBase = URLBase.substring(URLBase.lastIndexOf(".") + 1)
							+ File.separator;
					jep.setEditorKit(new ProMHTMLEditorKit(About.HELPLOCATION()
							+ URLBase));
					jep.setText(getPluginPage(plugin, type));
				}
			} else {
				type = url;
				jep.setEditorKit(new ProMHTMLEditorKit(About.HELPLOCATION()));
				jep.setText(getPluginsOverview(type));
			}

		} else {
			try {
				jep.setEditorKit(new ProMHTMLEditorKit(About.HELPLOCATION()));
				jep.setPage(url);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(),
						"Could not display help page",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		jep.setCaretPosition(0);
	}

	private String getFilterPage(LogFilter f, String type) {
		if (f != null) {
			return getHTMLPluginDescription(f);
		} else {
			return getPluginsOverview(type);
		}

	}

	private String getPluginPage(Plugin plugin, String type) {

		if (plugin == null) {
			return getPluginsOverview(type);
		} else {
			return getHTMLPluginDescription(plugin, type);
		}
	}

	private String getPluginsOverview(String type) {
		String html = "<html><head><title>Overview of plugins</title></head><body>";

		html += "<table border=0 width=\"95%\" align=center>";
		html += "<tr><td colspan=2><table border=0 width=\"100%\" align=center><tr>"
				+ "<td align=center width=\"14.2%\"><a href=\"plugin://\">All plugins</a></td>"
				+ "<td align=center width=\"14.2%\"><a href=\"plugin://mining\">Mining plugins</a></td>"
				+ "<td align=center width=\"14.2%\"><a href=\"plugin://analysis\">Analysis plugins</a></td>"
				+ "<td align=center width=\"14.2%\"><a href=\"plugin://export\">Export plugins</a></td>"
				+ "<td align=center width=\"14.2%\"><a href=\"plugin://import\">Import plugins</a></td>"
				+ "<td align=center width=\"14.2%\"><a href=\"plugin://convert\">Conversion plugins</a></td>"
				+ "<td align=center width=\"14.2%\"><a href=\"plugin://logfilter\">Log Filters</a></td>"
				+ "</tr></table></td></tr>";
		html += "<tr><td colspan=2><h1>Overview of " + type
				+ " plugins</h1></td></tr>";

		if (type.equals("") || type.equals("mining")) {
			html += "<tr><td colspan=2><b>Mining plugins</b></td></tr>";
			html += "<tr><td width=\"10%\">&nbsp;</td><td align=left>"
					+ getPluginList(MiningPluginCollection.getInstance(),
							"mining") + "</td></tr>";
		}
		if (type.equals("") || type.equals("analysis")) {
			html += "<tr><td colspan=2><b>Analysis plugins</b></td></tr>";
			html += "<tr><td width=\"10%\">&nbsp;</td><td align=left>"
					+ getPluginList(AnalysisPluginCollection.getInstance(),
							"analysis") + "</td></tr>";
		}
		if (type.equals("") || type.equals("export")) {
			html += "<tr><td colspan=2><b>Export plugins</b></td></tr>";
			html += "<tr><td width=\"10%\">&nbsp;</td><td align=left>"
					+ getPluginList(ExportPluginCollection.getInstance(),
							"export") + "</td></tr>";
		}
		if (type.equals("") || type.equals("import")) {
			html += "<tr><td colspan=2><b>Import plugins</b></td></tr>";
			html += "<tr><td width=\"10%\">&nbsp;</td><td align=left>"
					+ getPluginList(ImportPluginCollection.getInstance(),
							"import") + "</td></tr>";
		}
		if (type.equals("") || type.equals("convert")) {
			html += "<tr><td colspan=2><b>Conversion plugins</b></td></tr>";
			html += "<tr><td width=\"10%\">&nbsp;</td><td align=left>"
					+ getPluginList(ConvertingPluginCollection.getInstance(),
							"convert") + "</td></tr>";
		}
		if (type.equals("") || type.equals("logfilter")) {
			html += "<tr><td colspan=2><b>Log Filters</b></td></tr>";
			html += "<tr><td width=\"10%\">&nbsp;</td><td align=left>"
					+ getPluginList(LogFilterCollection.getInstance())
					+ "</td></tr>";
		}
		html += "</table>";
		html += "</body></html>";
		return html;
	}

	private String getPluginList(PluginCollection collection, String type) {

		Plugin[] plugins = new Plugin[collection.size()];
		for (int i = 0; i < plugins.length; i++) {
			plugins[i] = collection.get(i);
		}
		Arrays.sort(plugins, new Comparator<Plugin>() {
			public int compare(Plugin o1, Plugin o2) {
				if ((o1 == null || o1.getName() == null)
						&& (o2 == null || o2.getName() == null))
					return 0;
				if (o1 == null || o1.getName() == null)
					return -1;
				if (o2 == null || o2.getName() == null)
					return 1;
				String s1 = o1.getName().toLowerCase();
				String s2 = o2.getName().toLowerCase();
				return s1.compareTo(s2);
			}

			public boolean equals(Object obj) {
				return false;
			}
		});

		String html = "<table border=0 align=left width=\"100%\">";

		for (int i = 0; i < plugins.length; i++) {
			Plugin plugin = plugins[i];
			if (plugin == null) {
				continue;
			}
			String desc = getHTMLPluginDescription(plugin, type);

			html += "<tr><td width=\"100%\" align=left>";
			if (desc == null || desc.equals("")) {
				html += plugin.getName()
						+ " <font size=2>(no documentation available)</font>";
			} else if (desc.startsWith("http://")) {
				html += "<a href=\"" + desc + "\">" + plugin.getName()
						+ "</a> <font size=2>(online documentation)</font>";
			} else {
				html += "<a href=\"plugin://" + type + "/" + plugin.getName()
						+ "\">" + plugin.getName() + "</a>";
			}
			html += "</td></tr>";
		}
		return html + "</table>";
	}

	private String getPluginList(LogFilterCollection collection) {
		String html = "<table border=0 align=left width=\"100%\">";

		for (int i = 0; i < collection.size(); i++) {
			LogFilter plugin = collection.get(i);
			String desc = getHTMLPluginDescription(plugin);

			html += "<tr><td width=\"100%\" align=left>";
			if (desc == null || desc.equals("")) {
				html += plugin.getName()
						+ " <font size=2>(no documentation available)</font>";
			} else if (desc.startsWith("http://")) {
				html += "<a href=\"" + desc + "\">" + plugin.getName()
						+ "</a> <font size=2>(online documentation)</font>";
			} else {
				html += "<a href=\"plugin://logfilter/" + plugin.getName()
						+ "\">" + plugin.getName() + "</a>";
			}
			html += "</td></tr>";
		}
		return html + "</table>";
	}

	private void jbInit() throws Exception {
		setUndecorated(false);
		String name = System.getProperty("user.dir", "")
				+ System.getProperty("file.separator") + "images"
				+ System.getProperty("file.separator") + "icon.gif";

		setIconImage(Toolkit.getDefaultToolkit().getImage(name));

		jep.setEditable(false);
		jep.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						setPage(evt.getDescription());
					} catch (Exception e) {
						JOptionPane.showMessageDialog(PluginReference.this, e
								.getMessage(), "Could not display help page",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		jep.setContentType("text/html");

		getContentPane().add(new JScrollPane(jep));

		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setTitle(About.NAME + " - Plugin reference");
	}

	private String getHTMLPluginDescription(LogFilter p) {
		String html = p.getHelp();

		if (html == null || html.equals("")) {
			return null;
		} else if (html.startsWith("http://")) {
			return html; // real URL
		} else {
			// Direct plugin documentation is available.
			// add <html> tags
			html = "<html>"
					+ "<body> &nbsp; <a href=\"plugin://logfilter\">Back to overview</a><hr>"
					+ "<table border=0 width=\"95%\" align=center><tr><td>"
					+ html + "</td></tr></table></body></html>";
		}
		return html;
	}

	private String getHTMLPluginDescription(Plugin p, String type) {
		String html = p.getHtmlDescription();

		if (html == null || html.equals("")) {
			return null;
		} else if (html.startsWith("http://")) {
			return html; // real URL
		} else {
			// Direct plugin documentation is available.
			// add <html> tags
			html = "<html>" + "<body> &nbsp; <a href=\"plugin://" + type
					+ "\">Back to overview</a><hr>"
					+ "<table border=0 width=\"95%\" align=center><tr><td>"
					+ html + "</td></tr></table></body></html>";
		}
		return html;
	}
}
