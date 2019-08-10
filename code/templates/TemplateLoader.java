package templates;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TemplateLoader {
	
	
	private final String dir = "src/templates/";
	public static final String AND_SPLIT = "ANDSplit.xml";
	public static final String AND_JOIN = "ANDJoin.xml";
	public static final String AND_JOIN_SIMPLE = "ANDJoinSimple.xml";
	public static final String SKIP = "Skip.xml";
	public static final String LOOP_SPLIT = "LoopSplit.xml";
	public static final String LOOP_JOIN = "LoopJoin.xml";
	public static final String XOR = "XOR.xml";
	public static final String OR = "OR.xml";
	public static final String RIGID = "Rigid.xml";
	public static final String RIGID_MAIN = "RigidMain.xml";
	public static final String RIGID_DEV = "RigidDeviations.xml";
	public static final String EVENT_SPLIT = "EventSplit.xml";
	
	private String action ="";
	private String object= "";
	private String addition="";
	
	public void loadTemplate(String template) {
		action = "";
		object = "";
		addition = "";
		try {
			File file = new File(dir + template);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();

			NodeList nodes = doc.getElementsByTagName("action");
			action = nodes.item(0).getTextContent();
			
			nodes = doc.getElementsByTagName("object");
			object = nodes.item(0).getTextContent();
			
			nodes = doc.getElementsByTagName("addition");
			addition = nodes.item(0).getTextContent();
			
		} catch (Exception ex) {
			 ex.printStackTrace();
		}
	}
	
	public String getAction() {
		return action;
	}
	
	public String getObject() {
		return object;
	}
	
	public String getAddition() {
		return addition;
	}
	
}
