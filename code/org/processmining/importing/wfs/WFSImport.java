package org.processmining.importing.wfs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.ImportPlugin;
import org.processmining.mining.MiningResult;

/**
 * ProM import plug-in that converts a given WFState file to an SML file that
 * can be loaded into a previously generated simulation model. <br>
 * Uses source code from the corresponding YAWL export functions.
 * 
 * @author Anne Rozinat (a.rozinat at tue.nl)
 */
public class WFSImport implements ImportPlugin {

	WFStateCPN cpnwriter = new WFStateCPN();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/workflowstateimport";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Workflow State file";
	}

	/**
	 * WFS files have xml extensions
	 * 
	 * @return File filter for xml files
	 */
	public FileFilter getFileFilter() {
		return new GenericFileFilter("xml");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.importing.ImportPlugin#importFile(java.io.InputStream)
	 */
	public MiningResult importFile(InputStream input) throws IOException {
		String response = "";
		// get the XML content from the file to re-use Moes conversion
		// method in 'WFStateCPN' class
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		String xmlresponse = "";
		String temp = "";
		while (temp != null) {
			xmlresponse += temp;
			temp = in.readLine();
		}
		// parse the WFS file for process specification IDs
		// there may be several processes running on the same workflow system
		String specID = xmlresponse.replaceFirst(
				".*<Process\\s+id\\s*=\\s*[\"'](.+?)[\"'].*", "$1");
		String timeUnit = "Secs";
		response = cpnwriter.convertToSML(specID, timeUnit, xmlresponse);
		response += writeFile(response, "initialstate.sml", cpnwriter);
		return new DummyResult();
	}

	/*
	 * Class generating a visual component containing a user message stating the
	 * import result.
	 */
	class DummyResult implements MiningResult {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.mining.MiningResult#getLogReader()
		 */
		public LogReader getLogReader() {
			// there is no log reader
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.mining.MiningResult#getVisualization()
		 */
		public JComponent getVisualization() {
			String warnings = "";
			HashMap Warnings = cpnwriter.getWarnings();
			if (Warnings.size() > 0) {
				warnings += ("----Warnings----");
				Iterator it = Warnings.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry) it.next();
					String error = pairs.getKey().toString();
					String msg = (String) pairs.getValue();
					warnings += error + ":" + msg;
				}
			}
			Message.add("");
			JPanel result = new JPanel();
			JLabel message = new JLabel("<HTML>SML file exported<BR>"
					+ "<BR>----Warnings----<BR>" + warnings + "</HTML>");
			result.add(message);
			return result;
		}
	}

	// ////////// Source code adapted from Moe 'CurrentStateConverter.java'
	// /////////////
	// ////////// (only FileChooser added to allow to determine export location)
	// /////////////

	private String writeFile(String data, String fileName, WFStateCPN cpnwriter) {
		String response = "";
		if (data != "") {
			File file = new File(fileName);
			try {
				// actually save to file
				JFileChooser saveDialog = new JFileChooser();
				saveDialog.setSelectedFile(new File(fileName));
				if (saveDialog.showSaveDialog(MainUI.getInstance()) == JFileChooser.APPROVE_OPTION) {
					File outFile = saveDialog.getSelectedFile();
					try {
						BufferedWriter outWriter = new BufferedWriter(
								new FileWriter(outFile));
						outWriter.write(data);
						outWriter.flush();
						outWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				response = "successfully saved the info to "
						+ file.getCanonicalPath();

			} catch (IOException e) {
				System.out.println("IOException:");
				e.printStackTrace();
				response = "IO Exception: unable to write to file.";
			}

		} else {
			response = "Export unsuccessful to " + fileName
					+ ". No data supplied.";
		}
		return response;
	}

}
