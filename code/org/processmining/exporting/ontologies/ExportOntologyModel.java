package org.processmining.exporting.ontologies;

import java.io.OutputStream;

import org.processmining.exporting.Exporter;
import org.processmining.framework.models.ontology.OntologyModel;

public class ExportOntologyModel {

	@Exporter(extension = "wsml", name = "Export ontology to WSML")
	public void export(OntologyModel ontology, OutputStream out)
			throws Exception {
		out.write(ontology.serialize().getBytes());
	}
}
