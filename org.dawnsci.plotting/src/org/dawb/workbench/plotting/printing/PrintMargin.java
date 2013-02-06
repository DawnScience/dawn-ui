/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.plotting.printing;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;

/**
 * Class based on the preview SWT dialog example found in "Professional Java Interfaces with SWT/JFace" Jackwind Li
 * Guojie John Wiley & Sons 2005
 * 
 * @author Baha El Kassaby
 */
public class PrintMargin {
	// Margin to the left side, in pixels
	public int left;
	// Margins to the right side, in pixels
	public int right;
	// Margins to the top side, in pixels
	public int top;
	// Margins to the bottom side, in pixels
	public int bottom;

	private PrintMargin(int left, int right, int top, int bottom) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	/**
	 * Returns a PrintMargin object containing the true border margins for the specified printer with the given margin
	 * in inches. Note: all four sides share the same margin width.
	 * 
	 * @param printer
	 * @param margin
	 * @return PrintMargin
	 */
	static PrintMargin getPrintMargin(Printer printer, double margin) {
		return getPrintMargin(printer, 0, margin, 0, margin);
	}

	/**
	 * Returns a PrintMargin object containing the true border margins for the specified printer with the given margin
	 * width (in inches) for each side.
	 */
	static PrintMargin getPrintMargin(Printer printer, double marginLeft, double marginRight, double marginTop,
			double marginBottom) {
		Rectangle clientArea = printer.getClientArea();
		Rectangle trim = printer.computeTrim(0, 0, 0, 0);

		Point dpi = printer.getDPI();

		int leftMargin = (int) (marginLeft * dpi.x) - trim.x;
		int rightMargin = clientArea.width + trim.width - (int) (marginRight * dpi.x) - trim.x;
		int topMargin = (int) (marginTop * dpi.y) - trim.y;
		int bottomMargin = clientArea.height + trim.height - (int) (marginBottom * dpi.y) - trim.y;

		return new PrintMargin(leftMargin, rightMargin, topMargin, bottomMargin);
	}

	public String toString() {
		return "Margin { " + left + ", " + right + "; " + top + ", " + bottom + " }";
	}
}
