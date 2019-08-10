package com.iise.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.iise.bpplus.FileNameSelector;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.unfolding.AbstractCondition;
import org.jbpt.petri.unfolding.ProperCompletePrefixUnfolding;
import org.processmining.exporting.DotPngExport;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.importing.pnml.PnmlImport;
import org.jbpt.petri.unfolding.AbstractEvent;

public class UnfoldingGenerator
{
	public static void jbptTest() throws Exception
	{
		File folder = new File("./model");
		File[] arModels = folder.listFiles(new FileNameSelector("xml"));
		for(File file: arModels)
		{
			//print the file name
			System.out.println("========" + file.getName() + "========");

			//initialize the counter for conditions and events
			AbstractEvent.count = 0;
			AbstractCondition.count = 0;

			//get the file path
			String filePrefix = file.getPath();
			filePrefix = filePrefix.substring(0, filePrefix.lastIndexOf('.'));
			String filePNG = filePrefix + ".png";
			String fileCPU = filePrefix + "-cpu.png";
			String fileNet = filePrefix + "-net.png";

			PnmlImport pnmlImport = new PnmlImport();
			PetriNet p1 = pnmlImport.read(new FileInputStream(file));

			// ori
			ProvidedObject po1 = new ProvidedObject("petrinet", p1);
			DotPngExport dpe1 = new DotPngExport();
			OutputStream image1 = new FileOutputStream(filePNG);
			dpe1.export(po1, image1);

			NetSystem ns = PetriNetConversion.convert(p1);
			ProperCompletePrefixUnfolding cpu = new ProperCompletePrefixUnfolding(ns);

			// cpu
			PetriNet p2 = PetriNetConversion.convert(cpu);
			ProvidedObject po2 = new ProvidedObject("petrinet", p2);
			DotPngExport dpe2 = new DotPngExport();
			OutputStream image2 = new FileOutputStream(fileCPU);
			dpe2.export(po2, image2);

			// net
			NetSystem nsCPU = PetriNetConversion.convertCPU2NS(cpu);
			PetriNet pnCPU = PetriNetConversion.convertNS2PN(nsCPU);
			ProvidedObject po3 = new ProvidedObject("petrinet", pnCPU);
			DotPngExport dpe3 = new DotPngExport();
			OutputStream image3 = new FileOutputStream(fileNet);
			dpe3.export(po3, image3);

			System.out.println( "Finished\n" );
		}
	}

	public static void main(String[] args) throws Exception {
			jbptTest();
	}
}
