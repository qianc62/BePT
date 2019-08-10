package org.processmining.analysis.hierarchicaldatavisualization;

import java.util.List;

import org.processmining.framework.models.DotFormatter;
import org.processmining.framework.models.ModelGraph;

public interface HierarchicalData extends Iterable<HierarchicalDataElement> {
	DotFormatter getDotFormatter();

	List<String> getAvailableNumberFormatNames();

	String formatNumber(int format, double value); // the given format will be a

	// valid index in the list
	// returned by
	// getAvailableNumberFormats

	List<ModelGraph> graphsToExclude();

	// These two functions should return the type of hierarchy as a string. For
	// example: Ontology / Ontologies.
	// The names should start with a capital letter.
	String getSingularHierarchyName();

	String getPluralHierarchyName();
}
