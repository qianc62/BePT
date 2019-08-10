package org.processmining.framework.models.petrinet.pattern;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.processmining.framework.models.bpel.util.Pair;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.PnmlReader;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.util.CenterOnScreen;

public class MatchingOrder extends JPanel implements ActionListener,
		WindowListener, FocusListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2309647816111419021L;

	private final List<ComponentDescription> order = new ArrayList<ComponentDescription>();

	private final JButton revertToTemplate, manage, save;

	private final JPanel simplePanel;

	private final String[] template;

	private final boolean showCost;

	private final String matchingOrderXSD;

	private final JPanel simpleList;

	private String errorMsg;

	private final JTextField libraryComponentPath;

	private final JButton libraryComponentPathButton;

	private final BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(
			1);

	private final Map<JButton, ComponentDescription> upButtons = new LinkedHashMap<JButton, ComponentDescription>();

	private final Map<JButton, ComponentDescription> downButtons = new LinkedHashMap<JButton, ComponentDescription>();

	private final Map<JButton, ComponentDescription> showButtons = new LinkedHashMap<JButton, ComponentDescription>();

	private final Map<JTextField, ComponentDescription> nameFields = new LinkedHashMap<JTextField, ComponentDescription>();

	private final Map<JTextField, ComponentDescription> costFields = new LinkedHashMap<JTextField, ComponentDescription>();

	private JDialog dialog;

	private JButton closeDialog;

	private JButton addFolder;

	private JButton addPNML;

	private JPanel editComponentPanel;

	private JPanel editPresentationPanel;

	private Map<JButton, ComponentDescription> deleteButtons = new LinkedHashMap<JButton, ComponentDescription>();

	private final Namespace namespace;

	private final String matchingOrderXML;

	private final Map<String, ComponentDescription> nameToPenalty = new LinkedHashMap<String, ComponentDescription>();

	private final String[] suffixes;

	private final boolean showMinimalUnstructured;

	public MatchingOrder(boolean showCost, boolean showMinimalUnstructured,
			boolean addUserComponents, String matchingOrderXML,
			String matchingOrderXSD, String[] template, Namespace namespace,
			String[] suffixes) {
		this.showCost = showCost;
		this.showMinimalUnstructured = showMinimalUnstructured;
		this.suffixes = suffixes;
		this.matchingOrderXML = new File(matchingOrderXML).getAbsolutePath();
		this.matchingOrderXSD = new File(matchingOrderXSD).getAbsolutePath();
		this.template = template;
		this.namespace = namespace;

		libraryComponentPath = new JTextField(matchingOrderXML);
		libraryComponentPath.setEnabled(false);
		libraryComponentPathButton = new JButton("Change path...");
		libraryComponentPathButton.addActionListener(this);
		JPanel libraryPanel = new JPanel();
		JLabel label = new JLabel("matching-order.xml path: ");
		libraryPanel.add(label);
		libraryPanel.add(libraryComponentPath);
		libraryPanel.add(libraryComponentPathButton);

		manage = new JButton("Manage library components...");
		manage.addActionListener(this);
		manage.setAlignmentX(CENTER_ALIGNMENT);
		simpleList = new JPanel();
		errorMsg = null;
		try {
			order.addAll(readListOfComponents());
			updateSimplePresentationList();
		} catch (JDOMException e) {
			errorMsg = "Could not parse selected mapping-order.xml";
			System.err.println(e.toString());
		} catch (IOException e) {
			errorMsg = "Could not read seleted mapping-order.xml";
			System.err.println(e.toString());
		}
		simplePanel = new JPanel();
		simplePanel.setLayout(new BoxLayout(simplePanel, BoxLayout.Y_AXIS));
		if (errorMsg == null) {
			simplePanel.add(manage);
			simplePanel.add(new JScrollPane(simpleList));
		} else
			simplePanel.add(new JLabel(errorMsg));

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(libraryPanel);
		add(simplePanel);

		validate();
		repaint();

		// Setup dialog

		addPNML = new JButton("Add PNML");
		addPNML.addActionListener(this);
		addFolder = new JButton("Add folder");
		addFolder.addActionListener(this);
		revertToTemplate = new JButton("Restore defaults");
		revertToTemplate.addActionListener(this);
		closeDialog = new JButton("Close");
		closeDialog.addActionListener(this);
		save = new JButton("Save list");
		save.addActionListener(this);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		if (addUserComponents) {
			buttonPanel.add(addPNML);
			buttonPanel.add(addFolder);
		}
		buttonPanel.add(revertToTemplate);
		buttonPanel.add(save);
		buttonPanel.add(closeDialog);

		editComponentPanel = new JPanel();
		editPresentationPanel = new JPanel();
		editPresentationPanel.add(new JLabel());
		JSplitPane editPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		editPanel.add(new JScrollPane(editComponentPanel));
		editPanel.add(editPresentationPanel);
		updateComponentList();

		JPanel setupPanel = new JPanel(new BorderLayout());
		setupPanel.add(editPanel, BorderLayout.CENTER);
		setupPanel.add(buttonPanel, BorderLayout.SOUTH);

		dialog = new JDialog(MainUI.getInstance(), "Manage library components",
				true);
		dialog.add(setupPanel);
		setupPanel.setMinimumSize(new Dimension(1024, 768));
		setupPanel.setPreferredSize(new Dimension(1024, 768));
		dialog.addWindowListener(this);
		dialog.pack();
		dialog.validate();
		dialog.repaint();
	}

	private java.awt.Component loadNetVisualization(String path) {
		try {
			PetriNet net = new PnmlReader().read(new FileInputStream(new File(
					new File(matchingOrderXML).getParent() + "/" + path
							+ ".pnml")));
			return net.getGrappaVisualization();
		} catch (FileNotFoundException e) {
			return new JLabel(e.toString());
		} catch (Exception e) {
			return new JLabel(e.toString());
		}
	}

	private void updateComponentList() {
		upButtons.clear();
		downButtons.clear();
		costFields.clear();
		deleteButtons.clear();
		showButtons.clear();
		nameToPenalty.clear();
		editComponentPanel.removeAll();
		editComponentPanel.setLayout(new GridLayout(order.size(), 1));
		for (ComponentDescription componentDescription : order) {
			JPanel panel = new JPanel(new FlowLayout());
			panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			if (componentDescription.isPredefined())
				panel.add(new JLabel(componentDescription.getName() + "."));
			else {
				JTextField nameField = new JTextField(componentDescription
						.getName());
				nameFields.put(nameField, componentDescription);
				nameField.addFocusListener(this);
				panel.add(nameField);
			}
			if (showCost && componentDescription.getCost() != null) {
				panel.add(new JLabel("Cost:"));
				JTextField cost = new JTextField(componentDescription.getCost()
						.toString());
				cost
						.addFocusListener(new CheckIfBetweenOneAndZeroFocusListener(
								cost));
				cost.addFocusListener(this);
				costFields.put(cost, componentDescription);
				panel.add(cost);
				nameToPenalty.put(componentDescription.getName(),
						componentDescription);
			}
			JButton up = new JButton("Up");
			up.addActionListener(this);
			upButtons.put(up, componentDescription);
			JButton down = new JButton("Down");
			down.addActionListener(this);
			downButtons.put(down, componentDescription);
			JButton show = new JButton("Show");
			show.addActionListener(this);
			showButtons.put(show, componentDescription);
			JButton delete = new JButton("Delete");
			delete.addActionListener(this);
			deleteButtons.put(delete, componentDescription);
			panel.add(up);
			panel.add(down);
			panel.add(show);
			panel.add(delete);
			editComponentPanel.add(panel);
		}
		editComponentPanel.validate();
		editComponentPanel.repaint();
	}

	private void updateSimplePresentationList() {
		simpleList.removeAll();
		simpleList.setLayout(new FlowLayout());
		StringBuilder str = new StringBuilder();
		str
				.append("<table width=\"100%\" border=\"1\"><tr><th>Type</th><th>Name</th>");
		if (showCost)
			str.append("<th>Cost</th>");
		str.append("</tr>");

		for (ComponentDescription componentDescription : order) {
			str.append("<tr><td>");
			if (componentDescription.isPredefined())
				str.append("Predefined");
			else
				str.append("User defined");
			str.append("</td><td>" + componentDescription.getName() + "</td>");
			if (showCost) {
				if (componentDescription.getCost() == null) {
					if (componentDescription.getName().equals(
							"Maximal sequence"))
						str.append("&#8721;<sub>t &#8712; T</sub> &#964;(t)");
					else if (componentDescription.getName().equals(
							"Explicit choice"))
						str
								.append("1.5 &#183; &#8721;<sub>t &#8712; T</sub> &#964;(t)");
					else if (componentDescription.getName().equals(
							"Implicit choice"))
						str
								.append("1.5 &#183; &#8721;<sub>t &#8712; T</sub> &#964;(t)");
					else if (componentDescription.getName().equals("While"))
						str
								.append("&#8721;<sub>t &#8712; {i,o}</sub>&#964;(t) + 2 &#183; &#8721;<sub>t &#8712; T-{i,o}</sub>&#964;(t)");
					else if (componentDescription.getName().equals(
							"Maximal marked graph"))
						str
								.append("2 &#183; &#8721;<sub>t &#8712; T</sub> &#964;(t) &#183; <b>diff</b>(T)");
					else if (componentDescription.getName().equals(
							"Maximal state machine"))
						str
								.append("2 &#183; &#8721;<sub>t &#8712; T</sub> &#964;(t) &#183; <b>diff</b>(P)");
					else if (componentDescription.getName().equals(
							"Maximal well-structured"))
						str
								.append("2 &#183; &#8721;<sub>t &#8712; T</sub> &#964;(t) &#183; <b>diff</b>(P) &#183; <b>diff</b>(T)");
				} else
					str.append(componentDescription.getCost().toString());
			}
			str.append("</tr>");
		}
		if (showMinimalUnstructured) {
			str
					.append("<tr><td>Predefined</td><td>Minimal unstructured</td><td>5 &#183; (||F| - |P&#8746;T|| + 1) &#183; &#8721;<sub>t &#8712; T</sub>&#964;(t) &#183; <b>diff</b>(P) &#183; <b>diff</b>(T)");
		}
		str.append("</table>");
		simpleList.add(new JScrollPane(new JEditorPane("text/html", str
				.toString())));
		simpleList.validate();
		simpleList.repaint();
	}

	private List<ComponentDescription> readListOfComponents()
			throws JDOMException, IOException {
		List<ComponentDescription> result = new ArrayList<ComponentDescription>();
		SAXBuilder parser = new SAXBuilder();
		parser.setValidation(true);
		parser.setProperty(
				"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
				"http://www.w3.org/2001/XMLSchema");
		parser.setProperty(
				"http://java.sun.com/xml/jaxp/properties/schemaSource",
				new File(matchingOrderXSD).getAbsolutePath().replaceAll(" ",
						"%20"));
		Document mappings;
		mappings = parser.build(new File(matchingOrderXML).toURL());
		for (Object obj : mappings.getRootElement().getChildren()) {
			Element map = (Element) obj;
			String componentName;
			boolean isPredefined = map.getName().equals("predefined");
			if (isPredefined)
				componentName = map.getAttributeValue("name");
			else
				componentName = map.getAttributeValue("path");
			result
					.add(new ComponentDescription(
							isPredefined,
							componentName,
							showCost && map.getAttributeValue("cost") != null ? Double
									.parseDouble(map.getAttributeValue("cost"))
									: null));
		}
		return result;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == save) {
			String message = "Save list?\nThis will overwrite the current settings on disk";

			// Modal dialog with yes/no button
			int answer = JOptionPane.showConfirmDialog(dialog, message);
			if (answer == JOptionPane.YES_OPTION) {
				saveComponentList();
			}
		} else if (e.getSource() == libraryComponentPathButton) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select library component directory");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				libraryComponentPath.setText(chooser.getSelectedFile()
						.getAbsolutePath());
				updateSimplePresentationList();
			}
		} else if (e.getSource() == manage) {
			CenterOnScreen.center(dialog);
			dialog.setVisible(true);

			final Object[] obj = new Object[1];
			SwingWorker worker = new SwingWorker() {
				public Object construct() {
					try {
						Thread.yield();
						obj[0] = queue.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			worker.start();
			while (obj[0] == null)
				Thread.yield();
			worker.interrupt();
		} else if (e.getSource() == closeDialog) {
			closeDialog();
		} else if (e.getSource() == revertToTemplate) {
			order.clear();
			for (String string : template) {
				order.add(new ComponentDescription(true, string, 1.0));
			}
			updateComponentList();
		} else if (upButtons.containsKey(e.getSource())) {
			int index = order.indexOf(upButtons.get(e.getSource()));
			if (index > 0) {
				ComponentDescription tmp = upButtons.get(e.getSource());
				order.remove(tmp);
				order.add(index - 1, tmp);
				updateComponentList();
			}
		} else if (downButtons.containsKey(e.getSource())) {
			int index = order.indexOf(downButtons.get(e.getSource()));
			if (index < order.size() - 1) {
				ComponentDescription tmp = downButtons.get(e.getSource());
				order.remove(tmp);
				order.add(index + 1, tmp);
				updateComponentList();
			}
		} else if (showButtons.containsKey(e.getSource())) {
			editPresentationPanel.removeAll();
			editPresentationPanel.setLayout(new GridLayout(1, 1));
			if (!showButtons.get(e.getSource()).isPredefined()) {
				editPresentationPanel.add(loadNetVisualization(showButtons.get(
						e.getSource()).getName()));
				editPresentationPanel.validate();
				editPresentationPanel.repaint();
				dialog.validate();
				dialog.repaint();
			}
		} else if (deleteButtons.containsKey(e.getSource())) {
			String message = "Delete component "
					+ deleteButtons.get(e.getSource()).getName() + "?";

			// Modal dialog with yes/no button
			int answer = JOptionPane.showConfirmDialog(dialog, message);
			if (answer == JOptionPane.YES_OPTION) {
				order.remove(deleteButtons.get(e.getSource()));
				updateComponentList();
			}
		} else if (e.getSource() == addPNML) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select PNML file");
			chooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().endsWith(".pnml");
				}

				@Override
				public String getDescription() {
					return "PNML file";
				}
			});
			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File selected = chooser.getSelectedFile();
				addPNMLFileToLibrary(selected);
			}
		} else if (e.getSource() == addFolder) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select folder");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File root = chooser.getSelectedFile();
				Queue<File> queue = new LinkedList<File>();
				queue.add(root);
				while (!queue.isEmpty()) {
					File dir = queue.remove();
					for (File pnml : dir.listFiles(new java.io.FileFilter() {
						public boolean accept(File arg0) {
							return arg0.getName().endsWith(".pnml");
						}
					})) {
						addPNMLFileToLibrary(pnml);
					}
					for (File subdir : dir.listFiles(new java.io.FileFilter() {
						public boolean accept(File arg0) {
							return arg0.isDirectory();
						}
					})) {
						queue.add(subdir);
					}
				}
			}
		}
	}

	/**
	 * @param selected
	 */
	private void addPNMLFileToLibrary(File selected) {
		try {
			// Create channel on the source
			FileChannel srcChannel = new FileInputStream(selected
					.getAbsolutePath()).getChannel();

			// Create channel on the destination
			FileChannel dstChannel = new FileOutputStream(new File(
					matchingOrderXML).getParent()
					+ "/" + selected.getName()).getChannel();

			// Copy file contents from source to destination
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

			// Close the channels
			srcChannel.close();
			dstChannel.close();
			order.add(new ComponentDescription(false, selected.getName()
					.replaceAll(".pnml", ""), 1.0));
			updateComponentList();
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(dialog,
					"Could not add the PNML file " + selected.getName()
							+ " to the library!");
		}
	}

	private void saveComponentList() {
		Element matchingOrder = new Element("matching-order", namespace);
		for (ComponentDescription componentDescription : order) {
			Element content = new Element(
					componentDescription.isPredefined() ? "predefined"
							: "component", namespace);
			content.setAttribute(componentDescription.isPredefined() ? "name"
					: "path", componentDescription.getName());
			if (showCost)
				content.setAttribute("cost", componentDescription.getCost()
						.toString());
			matchingOrder.addContent(content);
		}
		Document document = new Document(matchingOrder);
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
		FileOutputStream out;
		try {
			out = new FileOutputStream(new File(matchingOrderXML));
			xmlOut.output(document, out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<ComponentDescription> getOrder() {
		return order;
	}

	public boolean exists() {
		return errorMsg == null;
	}

	public void windowActivated(WindowEvent arg0) {
	}

	public void windowClosed(WindowEvent arg0) {
	}

	public void windowClosing(WindowEvent arg0) {
		closeDialog();
	}

	/**
	 * 
	 */
	private void closeDialog() {
		try {
			updateSimplePresentationList();
			dialog.setVisible(false);
			queue.put(new Object());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowOpened(WindowEvent arg0) {
	}

	public void focusGained(FocusEvent e) {

	}

	public void focusLost(FocusEvent e) {
		if (costFields.containsKey(e.getSource())) {
			JTextField field = (JTextField) e.getSource();
			ComponentDescription cd = costFields.get(field);
			cd.setCost(Double.parseDouble(field.getText()));
		} else if (nameFields.containsKey(e.getSource())) {
			JTextField field = (JTextField) e.getSource();
			ComponentDescription cd = nameFields.get(field);
			String beforeName = cd.getName();
			if (!beforeName.equals(field.getText().trim())) {
				String message = "You have to save the matching order to change the name\nDo you wish to continue?";

				// Modal dialog with yes/no button
				int answer = JOptionPane.showConfirmDialog(dialog, message);
				if (answer == JOptionPane.YES_OPTION) {
					cd.setName(field.getText().trim());
					saveComponentList();
					List<Pair<String, String>> movedFiles = new ArrayList<Pair<String, String>>();
					for (String suffix : suffixes) {
						String oldName = new File(matchingOrderXML).getParent()
								+ "/" + beforeName + "." + suffix;
						String newName = new File(matchingOrderXML).getParent()
								+ "/" + field.getText().trim() + "." + suffix;
						boolean success = new File(oldName).renameTo(new File(
								newName));
						if (success)
							movedFiles.add(Pair.create(oldName, newName));
						else {
							cd.setName(beforeName);
							for (Pair<String, String> movedFile : movedFiles)
								new File(movedFile.second).renameTo(new File(
										movedFile.first));
							saveComponentList();
							JOptionPane.showMessageDialog(dialog,
									"Could not rename file " + oldName + " to "
											+ newName + "!");
							break;
						}
					}
					field.setColumns(field.getText().length());
					dialog.pack();
					dialog.validate();
					dialog.repaint();
				}
			}
		}
	}

	public Double getPenalty(Component component) {
		return nameToPenalty.get(component.toString()).getCost();
	}

}
