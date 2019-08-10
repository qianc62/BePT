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

package org.processmining.analysis.ltlchecker;

import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import org.processmining.analysis.ltlchecker.formulatree.DateValueNode;
import org.processmining.analysis.ltlchecker.formulatree.NumberValueNode;
import org.processmining.analysis.ltlchecker.formulatree.SetValueNode;
import org.processmining.analysis.ltlchecker.formulatree.StringValueNode;
import org.processmining.analysis.ltlchecker.formulatree.ValueNode;
import org.processmining.analysis.ltlchecker.parser.Attribute;
import org.processmining.analysis.ltlchecker.parser.DateAttribute;
import org.processmining.analysis.ltlchecker.parser.FormulaParameter;
import org.processmining.analysis.ltlchecker.parser.LTLParser;
import org.processmining.analysis.ltlchecker.parser.NumberAttribute;
import org.processmining.analysis.ltlchecker.parser.SetAttribute;
import org.processmining.analysis.ltlchecker.parser.StringAttribute;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.ui.MainUI;

/**
 * ParamData is an implementation of the {@see TableModel} class to be used as
 * model for the tabel for filling the parameters of a template ltl formula.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public class ParamData extends AbstractTableModel {

	private static final long serialVersionUID = 6518423353141449187L;

	/** Column names. */
	private String[] columnNames = { "Name", "Type", "Value" };

	/** Parameter data. */
	private Object[][] data;

	/** has params? */
	boolean isFinal;

	private OntologyCollection semanticLog;

	private List<FormulaParameter> params;

	public ParamData(List<FormulaParameter> params) {
		this(params, null);
	}

	public ParamData(List<FormulaParameter> params,
			OntologyCollection semanticLog) {

		super();
		this.params = params;
		this.semanticLog = semanticLog;
		isFinal = semanticLog != null;
		data = new Object[params.size()][3];
		Attribute var;

		Iterator<FormulaParameter> i = params.iterator();
		int j = 0;
		Date currentDate = new Date();

		while (i.hasNext()) {
			// Initialize every row of the table with initial values and
			// setting the name and type of the parameters.
			FormulaParameter parameter = i.next();
			var = parameter.getParam();

			switch (var.getType()) {

			case Attribute.NUMBER: {
				data[j][0] = var.getValue();
				data[j][1] = "number";
				data[j][2] = parameter.hasDefaultValue() ? parameter
						.getDefaultValue() : "0.0";
			}
				break;

			case Attribute.STRING: {
				data[j][0] = var.getValue();
				data[j][1] = "string";
				data[j][2] = "type a string";
				data[j][2] = parameter.hasDefaultValue() ? cutQuotes(parameter
						.getDefaultValue()) : "type a string";
			}
				break;

			case Attribute.SET: {
				data[j][0] = var.getValue();
				data[j][1] = "set";
				data[j][2] = "type a string";
				data[j][2] = parameter.hasDefaultValue() ? cutQuotes(parameter
						.getDefaultValue()) : "type a string";
			}
				break;

			case Attribute.DATE: {
				SimpleDateFormat sdf = var.getDateParser();
				data[j][0] = var.getValue();
				data[j][1] = "date";
				data[j][2] = parameter.hasDefaultValue() ? cutQuotes(parameter
						.getDefaultValue()) : sdf.format(currentDate);
			}
				break;
			}

			j++;
		}
		;
	}

	private String cutQuotes(String defaultValue) {
		if (defaultValue.length() >= 2 && defaultValue.startsWith("\"")
				&& defaultValue.endsWith("\"")) {
			return defaultValue.substring(1, defaultValue.length() - 1);
		}
		return defaultValue;
	}

	public OntologyCollection getSemanticLogReader() {
		return semanticLog;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {

		if (data[row][col] == null) {
			// When a value is null, that value can not be used for checking
			// the selected property. Therefor a empty string is returned.
			// Actually, only if "" can be parsed to a number of date (in case
			// of such parameter) it can be that there is a parse error
			// generated while checking. In that case a comparison returns
			// always false.
			return "";

		} else {

			return data[row][col];

		}
	}

	public boolean isCellEditable(int row, int col) {
		if (!isFinal) {

			if (col == 2) {

				return true;

			} else {

				return false;

			}

		} else {

			return false;

		}
	}

	public void setValueAt(Object value, int row, int col) {

		if (value instanceof ValueNode
				|| correctValue((String) value, row, col)) {
			// Only parseble strings may be set as value, this is important
			// for parameters of type number and date.
			data[row][col] = value;
			fireTableCellUpdated(row, col);

		} else {

			// beep
			Toolkit.getDefaultToolkit().beep();

		}
	}

	private boolean correctValue(String value, int row, int col) {
		// Compute if a string is a correct value for the parameter.
		boolean result = false;
		Attribute param = params.get(row).getParam();

		switch (param.getType()) {

		case Attribute.NUMBER:
			try {
				(new Float(value)).floatValue();
				result = true;
			} catch (NumberFormatException e) {
				return false;
			}
			break;

		case Attribute.STRING:
			result = true;
			break;

		case Attribute.SET:
			result = true;
			break;

		case Attribute.CONCEPTSET:
			result = true;
			break;

		case Attribute.DATE:
			try {
				SimpleDateFormat sdf = param.getDateParser();
				sdf.parse(value);
				result = true;
			} catch (java.text.ParseException e) {
				return false;
			}
			break;
		}
		return result;
	}

	public void removeData() {
		data = new Object[0][3];
	}

	/**
	 * The parameters of this table are used as values for the selected formula.
	 * These values must be added as a list of so called {@see Substitutes},
	 * therefor this method, to create such a list.
	 * 
	 * @param parser
	 *            The LTLParser containing all the information needed to create
	 *            the substitutes list.
	 * 
	 * @return The substitutes list of parameters of the selected formula.
	 */
	public Substitutes getSubstitutes(LTLParser parser) {
		Substitutes result = new Substitutes();
		List<String> errors = new ArrayList<String>();

		// add substitutions
		for (int i = 0; i < data.length; i++) {
			// for each parameter;

			ValueNode val = null;
			Attribute par = params.get(i).getParam();

			switch (par.getType()) {

			case Attribute.NUMBER:
				NumberValueNode nval = new NumberValueNode(
						NumberValueNode.VALUE);
				nval.setValue(new NumberAttribute((String) getValueAt(i, 2),
						Attribute.LITERAL, params.get(i).getParam()));
				val = nval;
				break;

			case Attribute.SET:
				Object cellValue = getValueAt(i, 2);

				if (cellValue instanceof SetValueNode) {
					val = (SetValueNode) cellValue;
				} else {
					String value = ((String) cellValue).trim();
					val = createSetValueNode(value, i);
				}
				break;

			case Attribute.STRING:
				StringValueNode sval = new StringValueNode(
						StringValueNode.VALUE);
				sval.setValue(new StringAttribute((String) getValueAt(i, 2),
						Attribute.LITERAL, params.get(i).getParam()));
				val = sval;
				break;

			case Attribute.DATE:
				DateValueNode dval = new DateValueNode(DateValueNode.VALUE);
				dval.setValue(new DateAttribute((String) getValueAt(i, 2),
						Attribute.LITERAL, params.get(i).getParam()));
				val = dval;
				break;
			}
			;

			result.add((String) data[i][0], val);
		}
		;

		if (errors.size() > 0) {
			StringBuffer msg = new StringBuffer(
					"The following errors were found:\n\n");
			for (String s : errors) {
				msg.append("  - " + s + "\n");
			}
			msg
					.append("\nThe LTL checker will continue, and assume that these concepts have no instances.");
			JOptionPane.showMessageDialog(MainUI.getInstance(), msg.toString());
		}
		return result;
	}

	public SetValueNode createSetValueNode(String value, int indexInTable) {
		TreeSet<String> strings = new TreeSet<String>();

		if (value.length() >= 3 && value.indexOf('|') > 0) {
			String[] parts = value.split("\\|");
			for (String s : parts) {
				String trimmed = s.trim();
				if (trimmed.length() > 0) {
					strings.add(trimmed);
				}
			}
		} else {
			strings.add(value);
		}

		if (strings.size() == 1) {
			SetValueNode setval = new SetValueNode(SetValueNode.VALUE);
			setval.setValue(new SetAttribute(strings.first(),
					Attribute.LITERAL, params.get(indexInTable).getParam()));
			return setval;
		} else {
			SetValueNode setval = new SetValueNode(SetValueNode.SET);
			setval.setSet(strings);
			return setval;
		}
	}
}
