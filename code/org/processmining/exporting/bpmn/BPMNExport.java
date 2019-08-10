package org.processmining.exporting.bpmn;

import java.io.IOException;
import java.io.OutputStream;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.models.bpmn.BpmnGraph;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import org.processmining.framework.ui.Message;
import java.util.StringTokenizer;

/**
 * <p>
 * Title: BPMNExport
 * </p>
 * 
 * <p>
 * Description: Export a BPMN Model to a BPMN file
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * @version 1.0
 */
public class BPMNExport implements ExportPlugin {

	public BPMNExport() {
	}

	public String getName() {
		return "ILog BPMN file";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof BpmnGraph) {
				return true;
			}
		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof BpmnGraph) {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));
				String export = ((BpmnGraph) o[i]).writeToBPMN();
				bw.write(export);
				bw.close();

				StringTokenizer lineTokenizer = new StringTokenizer(export,
						"\n");
				int nofLines = lineTokenizer.countTokens();
				int nofChars = export.length();
				Message.add("<BPMNExport nofLines=\"" + nofLines
						+ "\" nofChars=\"" + nofChars + "\"/>", Message.TEST);

				return;
			}
		}
	}

	public String getFileExtension() {
		return "ibp";
	}

	public String getHtmlDescription() {
		return "";
	}
}
