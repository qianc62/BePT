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

package org.processmining.framework.ui;

import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class ProMHTMLEditorKit extends HTMLEditorKit {

	private final String URLBase;

	public ProMHTMLEditorKit(String URLBase) {
		super();
		this.URLBase = URLBase;
	}

	public ViewFactory getViewFactory() {
		return new HTMLFactoryX(URLBase);
	}

	public static class HTMLFactoryX extends HTMLFactory implements ViewFactory {

		private final String URLBase;

		public HTMLFactoryX(String URLBase) {
			super();
			this.URLBase = URLBase;
		}

		public View create(Element elem) {
			Object o = elem.getAttributes().getAttribute(
					StyleConstants.NameAttribute);
			if (o instanceof HTML.Tag) {
				HTML.Tag kind = (HTML.Tag) o;
				if (kind == HTML.Tag.IMG) {
					return new ProMImageView(URLBase, elem);
				}
			}
			return super.create(elem);
		}
	}
}
