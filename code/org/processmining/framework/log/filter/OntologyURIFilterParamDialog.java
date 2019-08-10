package org.processmining.framework.log.filter;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.TreeMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;

public class OntologyURIFilterParamDialog extends LogFilterParameterDialog {

	private static final long serialVersionUID = 8613557259588528964L;

	private Map<String, JTextField> textFields;
	private JFileChooser fc = null;

	public OntologyURIFilterParamDialog(LogSummary summary, LogFilter filter) {
		super(summary, filter);
	}

	public Map<String, String> getResultMapping() {
		Map<String, String> result = new TreeMap<String, String>();

		for (Map.Entry<String, JTextField> item : textFields.entrySet()) {
			result.put(item.getKey(), item.getValue().getText());
		}
		return result;
	}

	public LogFilter getNewLogFilter() {
		return new OntologyURIFilter(getResultMapping());
	}

	protected JPanel getPanel() {
		JPanel body = new JPanel(new GridBagLayout());
		int i = 0;

		if (fc == null) {
			fc = new JFileChooser();
		}
		textFields = new TreeMap<String, JTextField>();
		for (Map.Entry<String, String> item : ((OntologyURIFilter) this.filter)
				.getMapping().entrySet()) {
			final JTextField text = new JTextField(item.getValue(), 50);
			textFields.put(item.getKey(), text);

			JPanel labelPanel = new JPanel();
			JPanel textPanel = new JPanel();

			JLabel label = new JLabel(item.getKey());
			labelPanel.add(label);
			body.add(labelPanel, new GridBagConstraints(0, i * 2, 1, 1, 1.0,
					1.0, GridBagConstraints.LINE_START,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

			JButton browse = new JButton("...");
			browse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (fc.showOpenDialog(OntologyURIFilterParamDialog.this) == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						String filename = file.toURI().toString();
						text.setText(filename);
					}
				}
			});
			textPanel.add(text);
			textPanel.add(browse);
			body.add(textPanel,
					new GridBagConstraints(0, i * 2 + 1, 1, 1, 1.0, 1.0,
							GridBagConstraints.LINE_START,
							GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0,
									0), 0, 0));
			i++;
		}

		JLabel title1 = new JLabel(
				"Please enter the ontology URIs you would like to use.");
		JLabel title2 = new JLabel(
				"If you leave a field blank, then all model references to that ontology will be removed.");
		JPanel titlePanel = new JPanel();
		title1.setAlignmentX((float) 0.5);
		title2.setAlignmentX((float) 0.5);
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.PAGE_AXIS));
		titlePanel.add(title1);
		titlePanel.add(title2);

		JPanel main = new JPanel(new BorderLayout());
		main.add(titlePanel, BorderLayout.NORTH);
		main.add(body, BorderLayout.CENTER);
		return main;
	}

	protected boolean getAllParametersSet() {
		return true;
	}
}
