package org.processmining.converting.wfnet2bpel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.processmining.framework.models.bpel.BPEL;
import org.processmining.framework.models.bpel.BPELActivity;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.importing.bpel.BPELImport;
import org.processmining.mining.bpel.BPELResult;

/**
 * <p>
 * Title: BPELParser
 * </p>
 * 
 * <p>
 * Description: A toolbox for parsing BPEL from a file
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: University of Aarhus
 * </p>
 * 
 * @author Kristian Bisgaard Lassen (<a
 *         href="mailto:K.B.Lassen@daimi.au.dk">mailto
 *         :K.B.Lassen@daimi.au.dk</a>)
 * @version 1.0
 */
public class BPELParser {

	public static BPELActivity parseActivityFromFile(File file) {
		BPELResult bpelResult = null;
		try {
			bpelResult = (BPELResult) new BPELImport()
					.importFile(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ProvidedObject[] po = bpelResult.getProvidedObjects();
		Object[] objects = po[0].getObjects();
		BPEL bpel = (BPEL) objects[0];
		return bpel.getProcess().getActivity();
	}

}
