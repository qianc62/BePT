package org.processmining.tests.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.processmining.exporting.DotPngExport;
import org.processmining.exporting.epcs.EpmlExport;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.importing.epml.EpmlImport;
import org.processmining.mining.epcmining.EPCResult;

public class EPCTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String epmlFile = "C:\\Users\\shudi\\Desktop\\epc\\e1_53e4d59a161c73e586c15789.epml";
		EpmlImport epmlImport = new EpmlImport();
		EPCResult epcResult = (EPCResult) epmlImport.importFile(new FileInputStream(epmlFile));
		ProvidedObject po = new ProvidedObject("ConfigurableEPC", epcResult.getFirstConfigurableEPC());
//		DotPngExport dpe = new DotPngExport();
//		OutputStream image = new FileOutputStream("C:\\Users\\shudi\\Desktop\\epc\\2.png");
//		dpe.export(po, image);
		EpmlExport epmlExport = new EpmlExport();
		String epmlFile2 = "C:\\Users\\shudi\\Desktop\\epc\\e1.epml";
		epmlExport.export(po, new FileOutputStream(epmlFile2));
	}

}
