package org.processmining.converting.wfnet2bpel.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.processmining.framework.models.bpel.BPEL;
import org.processmining.framework.models.bpel.BPELActivity;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.pattern.log.Log;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.bpel.BPELResult;

public class BPELLog extends Log {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4278674431528583879L;

	private JTabbedPane bpelTranslationPane;

	private BPEL bpel;

	public BPELLog() {
		super(false);
	}

	public void prepareToShowLog(PetriNet wfnet,
			Map<String, BPELActivity> annotations, BPEL bpel) {
		this.bpel = bpel;
		super.prepareToShowLog(wfnet);
		if (wfnet.getTransitions().size() == 1) {
			bpelTranslationPane = createBPELTranslationPane(wfnet, annotations,
					bpel);
			add("BPEL", bpelTranslationPane);
		}
		validate();
		repaint();
	}

	private JTabbedPane createBPELTranslationPane(PetriNet wfnet,
			Map<String, BPELActivity> annotations, BPEL bpelProcess) {
		Transition transition = wfnet.getTransitions().iterator().next();
		TransformerFactory xformFactory = TransformerFactory.newInstance();
		String bpel = null;
		try {
			Transformer idTransform = xformFactory.newTransformer();
			Source input = new DOMSource(annotations.get(transition.getName())
					.getElement());
			StringWriter result = new StringWriter();
			idTransform.transform(input, new StreamResult(result));
			bpel = result.toString();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		SAXBuilder builder = new SAXBuilder();
		ByteArrayOutputStream out = null;
		try {
			Document document = builder.build(new StringReader(bpel));
			out = new ByteArrayOutputStream();
			XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
			xmlOut.output(document, out);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String output = out.toString();
		output = output.substring(new String(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>").length()); //$NON-NLS-1$
		JEditorPane editorPane = new JEditorPane("text/plain", output);
		editorPane.setEditable(false);

		JTabbedPane result = new JTabbedPane();
		result.add("Source", new JScrollPane(editorPane));

		BPELResult bpelResult = new BPELResult(null, bpelProcess);
		result.add("Diagram", bpelResult.getVisualization());

		return result;
	}

	/**
	 * @see org.processmining.framework.models.petrinet.pattern.log.Log#getProvidedObjects()
	 */
	@Override
	public ProvidedObject[] getProvidedObjects() {
		if (getSelectedComponent() == bpelTranslationPane)
			return new ProvidedObject[] { new ProvidedObject(
					"Selected BPEL 1.1", new Object[] { bpel }) };
		return super.getProvidedObjects();
	}

}
