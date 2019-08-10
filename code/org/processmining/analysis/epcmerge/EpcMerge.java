package org.processmining.analysis.epcmerge;

/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2008 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.processmining.analysis.Analyzer;
import org.processmining.converting.AggregationGraphToEPC;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderException;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;
import org.processmining.framework.log.rfb.AuditTrailEntryListImpl;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.models.LogEventProvider;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEdge;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.util.StringSimilarity;
import org.processmining.framework.util.ToolTipComboBox;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.writing.LogSetSequential;
import org.processmining.lib.mxml.writing.ProcessInstanceType;
import org.processmining.lib.mxml.writing.impl.LogSetSequentialImpl;
import org.processmining.lib.mxml.writing.persistency.LogPersistency;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyDir;
import org.processmining.mining.epcmining.EPCResult;
import org.processmining.mining.logabstraction.FSMLogRelationBuilder;
import org.processmining.mining.logabstraction.LogAbstraction;
import org.processmining.mining.logabstraction.LogAbstractionImpl;
import org.processmining.mining.logabstraction.LogRelations;
import org.processmining.mining.logabstraction.MinValueLogRelationBuilder;
import org.processmining.mining.logabstraction.TimeIntervalLogRelationBuilder;
import org.processmining.mining.partialordermining.AggregationGraphResult;
import org.processmining.mining.partialordermining.PartialOrderAggregationPlugin;
import org.processmining.mining.partialordermining.PartialOrderGeneratorPlugin;
import org.processmining.mining.partialordermining.PartialOrderMiningResult;

/**
 * <p>
 * Title: EPC Merge
 * </p>
 * <p>
 * Description: Plugin that merges two EPCs into one EPC representing at least
 * the behaviour of both the EPCs
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author fgottschalk
 * @version 1.0
 */
public class EpcMerge {
	@Analyzer(name = "EPC Merge", names = { "EPC 1", "EPC 2" }, connected = false, help = "http://www.floriangottschalk.de/255.html")
	public static EPCMergeSettings analyze(ConfigurableEPC net,
			ConfigurableEPC net2) {
		return new EPCMergeSettings(net2, net, true);
	}

	/**
	 * Provides user documentation for the plugin.
	 * 
	 * @return a URL that will be opened in the default browser of the user
	 */
	public String getHtmlDescription() {
		return "http://www.floriangottschalk.de/255.html";
	}

}
