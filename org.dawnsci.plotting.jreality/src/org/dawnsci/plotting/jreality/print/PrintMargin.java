/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.jreality.print;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;

/**
 * Class based on the preview SWT dialog example found in "Professional Java Interfaces with SWT/JFace" Jackwind Li
 * Guojie John Wiley & Sons 2005
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

	@Override
	public String toString() {
		return "Margin { " + left + ", " + right + "; " + top + ", " + bottom + " }";
	}
}
