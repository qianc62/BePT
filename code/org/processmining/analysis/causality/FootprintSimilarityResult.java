package org.processmining.analysis.causality;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import org.processmining.framework.models.causality.*;
import org.processmining.framework.models.epcpack.*;
import org.processmining.framework.ui.*;
import org.w3c.dom.*;
import org.processmining.analysis.epc.similarity.Similarities;
import org.processmining.framework.util.StringNormalizer;

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
public class FootprintSimilarityResult {

	private static final String PREFIX = "ProM_cfp_file";
	private String tempDir;
	private List<String> basePaths;
	private List<ConfigurableEPC> baseEpcs;
	private List<String> compPaths;
	private List<ConfigurableEPC> compEpcs;
	private Similarities similarities;

	public FootprintSimilarityResult(List<String> basePaths,
			List<ConfigurableEPC> baseEpcs, List<String> compPaths,
			List<ConfigurableEPC> compEpcs, String directory) {

		tempDir = directory;
		if (!tempDir.endsWith(File.separator)) {
			tempDir += File.separator;
		}
		this.basePaths = basePaths;
		this.baseEpcs = baseEpcs;
		this.compPaths = compPaths;
		this.compEpcs = compEpcs;

	}

	public void setSimilarities(Similarities similarities) {
		this.similarities = similarities;
	}

	public CausalFootprint getBaseFootPrint(int i, Progress progress) {
		return getFootprint(i, progress, basePaths, baseEpcs);
	}

	public CausalFootprint getCompareToFootPrint(int i, Progress progress) {
		return getFootprint(i, progress, compPaths, compEpcs);

	}

	private CausalFootprint getFootprint(int i, Progress progress,
			List<String> paths, List<ConfigurableEPC> epcs) {
		// Message.add("Check if footprint exists for EPC: " + path,
		// Message.DEBUG);
		CausalFootprint cfp = readFootprint(i, paths, epcs);
		if (cfp == null) {
			Message
					.add("Retrieval failed, so constructing causal footprint for EPC: "
							+ getFilename(paths.get(i)));
			String oldNote = progress.getNote();
			progress.setNote("Calculating footprint");
			ConfigurableEPC epc;
			synchronized (epcs) {
				epc = epcs.get(i);
			}
			synchronized (epc) {
				cfp = CausalityFootprintFactory.make(epc, progress);
			}
			if (progress.isCanceled()) {
				return null;
			}
			// Message.add("Build causal closure for footprint of EPC: " +path,
			// Message.DEBUG);
			// THIS IS HANDLED WHEN CREATING THE CFP.
			// progress.setNote("Calculating causal closure");
			// synchronized (cfp) {
			// cfp.closeTransitively(progress);
			// }
			// if (progress.isCanceled()) {
			// return null;
			// }
			// Message.add("Storing footprint of EPC: " + path
			// +" for future reference.", Message.DEBUG);
			writeFootprint(cfp, paths.get(i));
			progress.setNote(oldNote);
		}
		return cfp;
	}

	public int getBaseIndex(String path) {
		return basePaths.indexOf(path);
	}

	public int getCompareToIndex(String path) {
		return compPaths.indexOf(path);
	}

	public double getSimilarity(String basePath, String compareToPath) {
		int baseIndex = basePaths.indexOf(basePath);
		int compIndex = compPaths.indexOf(compareToPath);
		if ((baseIndex < 0) || (compIndex < 0)) {
			return -1;
		} else {
			return similarities.get(baseIndex, compIndex);
		}
	}

	private void writeFootprint(CausalFootprint cfp, String path) {
		File cfpFile = new File(getFilename(path));
		// Message.add("Writing file: " + cfpFile.getPath(), Message.DEBUG);
		// cfpFile.deleteOnExit();
		try {

			OutputStream stream = new FileOutputStream(cfpFile);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					stream));
			bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			bw.write("<ProMCausalFootprint>\n");
			cfp.writeXML(bw);
			bw.write("</ProMCausalFootprint>\n");
			bw.close();

			stream.close();
		} catch (IOException ex) {
			// don't care, can't write to the file, so have to construct the
			// footprint again later.
			return;
		}
	}

	private CausalFootprint readFootprint(int i, List<String> paths,
			List<ConfigurableEPC> epcs) {
		File cfpFile = new File(getFilename(paths.get(i)));
		CausalFootprint cfp = null;
		InputStream stream = null;
		try {
			stream = new FileInputStream(cfpFile);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			Document doc;

			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);

			doc = dbf.newDocumentBuilder().parse(stream);
			NodeList netNodes;

			// check if root element is a <pnml> tag
			if (!doc.getDocumentElement().getTagName().equals(
					"ProMCausalFootprint")) {
				throw new IOException("ProMCausalFootprint tag not found");
			}

			netNodes = doc.getDocumentElement().getElementsByTagName(
					"causalfootprint");
			if (netNodes.getLength() > 0) {
				cfp = CausalFootprint.readXML(netNodes.item(0), epcs.get(i));
			}
			stream.close();

		} catch (FileNotFoundException ex) {
			// No problem.
			return null;
		} catch (Exception ex) {
			/** @todo Handle this exception */
			Message.add("Problem while opening file: "
					+ getFilename(paths.get(i)) + " (" + ex.getMessage() + ")",
					Message.ERROR);
		}

		return cfp;
	}

	/**
	 * getFilename
	 * 
	 * @param baseModel
	 *            ModelGraph
	 * @return String
	 */
	private String getFilename(String path) {
		String file = StringNormalizer.normalize(PREFIX + path);
		file = (file.length() > 252 ? file.substring(0, 252) : file);
		file += ".cfp";
		return tempDir + file;
	}

	public List<String> getBasePaths() {
		return basePaths;
	}

	public List<ConfigurableEPC> getBaseEpcs() {
		return baseEpcs;
	}

	public List<String> getCompareToPaths() {
		return compPaths;
	}

	public List<ConfigurableEPC> getCompareToEpcs() {
		return compEpcs;
	}

	public Similarities getSimilarities() {
		return similarities;
	}

}
