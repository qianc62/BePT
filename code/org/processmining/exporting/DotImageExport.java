/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.exporting;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.imageio.ImageIO;

import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.fuzzymining.vis.DotTools;

/**
 * Exports a shared DOT file in ProM to a PNG-formatted image file.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public abstract class DotImageExport implements ExportPlugin {

	protected String format;

	protected DotImageExport(String format) {
		this.format = format;
	}

	public String getName() {
		return format + " image";
	}

	public boolean accepts(ProvidedObject original) {
		int i = 0;
		boolean isDotWriter = false;
		while ((i < original.getObjects().length)) {
			isDotWriter = isDotWriter
					|| (original.getObjects()[i] instanceof DotFileWriter);
			i++;
		}
		return isDotWriter;
	}

	public void export(ProvidedObject object, OutputStream output) throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof DotFileWriter) {
				StringWriter sWriter = new StringWriter();
				((DotFileWriter) o[i]).writeToDot(sWriter);
				sWriter.close();
				BufferedImage image = (BufferedImage) (new DotTools()).renderImage(sWriter.toString());
				ImageIO.write(image, format, output);
				return;
			}
		}
	}

	public String getFileExtension() {
		return format.toLowerCase();
	}

	public String getHtmlDescription() {
		return "<p>This plugin accepts any graph that can be visualized in ProM. The export "
				+ "writes a "
				+ format
				+ " file. Note that some custom figures, such as the "
				+ "elements of a YAWL model will not be visualized correctly. Instead, they are "
				+ "substituted by a box.";
	}

}
