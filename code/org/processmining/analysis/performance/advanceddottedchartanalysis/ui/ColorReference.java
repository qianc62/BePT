/*
 * Created on Sep 18, 2008
 *
 * Author: Minseok Song
 * (c) 2005 Technische Universiteit Eindhoven, Minseok Song
 * all rights reserved
 *
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */

package org.processmining.analysis.performance.advanceddottedchartanalysis.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Random;

// added
//import java.io.IOException;
//import java.util.*;
//import org.jdom.*;
//import org.jdom.input.SAXBuilder;
//import java.io.InputStream;
//import java.io.File;
//import java.io.Writer;

/**
 * ColorRepository.
 * 
 * For assigning colors to keys automatically, and preserving these associations
 * over time. Keeps string ids assigned to colors and preserves mappings.
 * Provides a set of 10 standard colors for mappings, when full it will return
 * random colors.
 * 
 * @author Minseok Song
 */
public class ColorReference {

	protected HashMap<String, Color> mappings = null;
	protected Color[] colors = null;
	protected boolean[] assigned = null;
	protected Random rnd = null;

	/**
	 * constructor
	 */
	public ColorReference() {
		mappings = new HashMap<String, Color>();
		rnd = new Random();
		// standard colors
		colors = new Color[10];
		colors[0] = new Color(200, 50, 50); // red
		colors[1] = new Color(50, 200, 50); // green
		colors[2] = new Color(50, 50, 200); // blue
		colors[3] = new Color(200, 100, 50); // orange
		colors[4] = new Color(200, 50, 200); // violet
		colors[5] = new Color(150, 150, 50); // dark yellow
		colors[6] = new Color(20, 20, 20); // dark grey
		colors[7] = new Color(50, 50, 100); // dark blue
		colors[8] = new Color(100, 50, 50); // dark red
		colors[9] = new Color(50, 100, 100); // dark green
		// assignment map
		assigned = new boolean[10];
		for (int i = 0; i < assigned.length; i++) {
			assigned[i] = false;
		}
	}

	/**
	 * Retrieves the color mapped to the given key (identity preserved). If no
	 * color was previously mapped, a new one is taken from the standard
	 * repository and, if all taken, a random color is assigned.
	 * 
	 * @param key
	 *            the key to map a color to
	 * @return mapped color instance
	 */
	public Color getColor(String key) {
		if (mappings.containsKey(key)) {
			// return previously mapped color
			return mappings.get(key);
		} else {
			// check for available standard color
			for (int i = 0; i < assigned.length; i++) {
				if (assigned[i] == false) {
					assigned[i] = true;
					mappings.put(key, colors[i]);
					return colors[i];
				}
			}
			// create random color
			Color random = new Color(rnd.nextInt(245), rnd.nextInt(245), rnd
					.nextInt(245));
			mappings.put(key, random);
			return random;
		}
	}

	/**
	 * Frees a keyed color object for new assignment,
	 * 
	 * @param key
	 *            previously assigned key
	 */
	public void freeColor(String key) {
		Color c = mappings.remove(key);
		for (int i = 0; i < colors.length; i++) {
			if (colors[i].equals(c)) {
				assigned[i] = false;
				return;
			}
		}
	}

	/**
	 * Assign a color to the object,
	 * 
	 * @param key
	 *            previously assigned key
	 * @param color
	 *            color to be assigned
	 */
	public void assignColor(String key, Color color) {
		mappings.put(key, color);
	}

	/**
	 * Assign a color to the object,
	 * 
	 * @param key
	 *            previously assigned key
	 * @param color
	 *            color to be assigned
	 */
	public void randomizeColor(String key) {
		Color random = new Color(rnd.nextInt(245), rnd.nextInt(245), rnd
				.nextInt(245));
		mappings.put(key, random);
	}

	/**
	 * Export to OrgModel file.
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 *             If writing fails
	 */
	/*
	 * public void writeToXML(Writer bw) throws IOException {
	 * bw.write("<ColorReferenceExport>"); Iterator<String> itr =
	 * mappings.keySet().iterator(); while (itr.hasNext()){ String key =
	 * (String) itr.next(); bw.write("<ColorUnit>\n<Key>"+key+"</Key>\n");
	 * bw.write("<ColorRGB>"+((Color)
	 * mappings.get(key)).getRGB()+"</ColorRGB>\n</ColorUnit>"); }
	 * bw.write("\n</ColorReferenceExport>"); }
	 * 
	 * public void readFile(InputStream input){
	 * 
	 * SAXBuilder builder = new SAXBuilder(); try { Document doc =
	 * builder.build(input); Element root = doc.getRootElement(); List<Element>
	 * children = root.getChildren(); for (int i = 0; i < children.size(); i++)
	 * { Element element = (Element) children.get(i); if (element != null) if
	 * ("ColorUnit".equals(element.getName())){ String key =
	 * element.getChildText("Key"); int colorRGB = (int)
	 * Integer.valueOf(element.getChildText("ColorRGB")); mappings.remove(key);
	 * mappings.put(key,new Color(colorRGB)); } } } catch (JDOMException e) {
	 * System.out.println("JDOMException"); } catch (IOException e) {
	 * System.out.println("IOException"); }
	 * 
	 * }
	 * 
	 * public void readFile(String fileName){ SAXBuilder builder = new
	 * SAXBuilder(); try{ Document doc = builder.build(new File(fileName));
	 * Element root = doc.getRootElement(); List children = root.getChildren();
	 * for (int i = 0; i < children.size(); i++) { Element element = (Element)
	 * children.get(i); if (element != null) if
	 * ("ColorUnit".equals(element.getName())) { String key =
	 * element.getChildText("Key"); int colorRGB = (int)
	 * Integer.valueOf(element.getChildText("ColorRGB")); mappings.remove(key);
	 * mappings.put(key, new Color(colorRGB)); } } } catch (JDOMException e) {
	 * System.out.println("JDOMException"); } catch (IOException e) {
	 * System.out.println("IOException"); } }
	 */
}
