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

import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;

/**
 * PrintSettings is a class to hold typical printer settings.<br>
 * <br>
 * The settings for margins can be given with units, e.g. "1.234 cm". The numerical values are stored in inches since
 * devices, such as Printers and Displays, have a DPI (dots per inch) setting, which is important for displaying images,
 * and this is the easiest way to use the DPI.<br>
 * <br>
 * Note that at the time this was written SWT has no means of setting the orientation in the Printer. Thus PrintSettings
 * cannot be used to do that for the user. The orientation can be used for previews and the like, however.<br>
 * 
 * @author Kenneth Evans, Jr.
 */
public class PrintSettings {

	private static final String DEFAULT_UNITS_FORMAT = "%.3f";
	private String unitsFormat = DEFAULT_UNITS_FORMAT;
	private Units units = Units.INCH;
	private double left = 1;
	private double right = 1;
	private double top = 1;
	private double bottom = 1;
	private int verticalAlign = SWT.TOP;
	private int horizontalAlign = SWT.CENTER;
	private Orientation orientation = Orientation.PORTRAIT;
	private PrinterData printerData = null;
	private Scale scale = Scale.DEFAULT;
	private Resolution resolution = Resolution.MEDIUM;

	// If new fields are added be sure to adjust clone()

	/**
	 * Orientation is a complex enum that represents possible printer orientations and that also includes String names
	 * for the items.
	 * 
	 * @author Kenneth Evans, Jr.
	 * @author Baha El Kassaby (resolution, scale, modif of orientation) 
	 */
	public static enum Orientation {
		PORTRAIT("Portrait", 1), LANDSCAPE("Landscape", 2);
		private final String name;
		private final int value;

		/**
		 * Orientation constructor.
		 * 
		 * @param name
		 */
		Orientation(String name, int value) {
			this.name = name;
			this.value = value;
		}

		/**
		 * Get the name.
		 * 
		 * @return name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Get the value.
		 * 
		 * @return value
		 */
		public int getValue() {
			return value;
		}
	}
	
	/**
	 * Scale is a complex enum that represents possible image scales and that also includes String names
	 * and Double values for the items.
	 */
	public static enum Scale {
		DEFAULT("100%",0.5), PERCENT75("75%",2.0), PERCENT66("66%",3.0), 
		PERCENT50("50%", 4.0), PERCENT33("33%", 5.0), PERCENT25("25%", 6.0), PERCENT10("10%", 7.0);
		private final String name;
		private final Double value;

		/**
		 * Scale constructor.
		 * 
		 * @param name
		 */
		Scale(String name, Double value) {
			this.name = name;
			this.value = value;
		}

		/**
		 * Get the name.
		 * 
		 * @return name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Get the value.
		 * 
		 * @return value
		 */
		public Double getValue() {
			return value;
		}
	}
	
	/**
	 * Resolution is a complex enum that represents possible image resolution and that also includes String names
	 * and Double values for the items.
	 */
	public static enum Resolution {
		LOW("Low", 1), MEDIUM("Medium", 2), MEDIUMHIGH("Medium High", 3), HIGH("High", 4);
		private final String name;
		private final int value;

		/**
		 * Scale constructor.
		 * 
		 * @param name
		 */
		Resolution(String name, int value) {
			this.name = name;
			this.value = value;
		}

		/**
		 * Get the name.
		 * 
		 * @return name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Get the value.
		 * 
		 * @return value
		 */
		public int getValue() {
			return value;
		}
	}

	/**
	 * Units is a complex enum that represents possible units and that also includes scaling and String names for the
	 * items.
	 * 
	 * @author Kenneth Evans, Jr.
	 */
	public static enum Units {
		INCH("in", 1), CENTIMETER("cm", 1. / 2.54), MILLIMETER("mm", 1. / 254.);
		private final String name;
		private final double scale;

		/**
		 * Units constructor.
		 * 
		 * @param name
		 * @param scale
		 */
		Units(String name, double scale) {
			this.name = name;
			this.scale = scale;
		}

		/**
		 * Get the name.
		 * 
		 * @return name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Get the scale.
		 * 
		 * @return double scale
		 */
		public double getScale() {
			return scale;
		}

		/**
		 * Convert to inches.
		 * 
		 * @param val
		 * @return double vale*scale
		 */
		public double scaleToInches(double val) {
			return val * scale;
		}

		/**
		 * Convert from inches.
		 * 
		 * @param val
		 * @return double val/scale
		 */
		public double scaleFromInches(double val) {
			return val / scale;
		}

	}

	/**
	 * ValueWithUnits is a simple class that handles a double value and an associates unit. This class is used by
	 * methods that handle String representations of a value with units, e.g. "1.234 cm".
	 * 
	 * @author Kenneth Evans, Jr.
	 */
	public class ValueWithUnits {
		private double val;
		private String unitsName;

		/**
		 * ValueWithUnits constructor.
		 * 
		 * @param val
		 * @param unitsName
		 */
		ValueWithUnits(double val, String unitsName) {
			this.val = val;
			this.unitsName = unitsName;
		}

		/**
		 * @return The string representation, e.g. "1.234 cm".
		 */
		public String getString() {
			return String.format(unitsFormat + " %s", val, unitsName);
		}

		/**
		 * @return The value.
		 */
		public double getVal() {
			return val;
		}

		/**
		 * @return The name of the units.
		 */
		public String getUnitsName() {
			return unitsName;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public PrintSettings clone() {

		PrintSettings newSettings = new PrintSettings();
		newSettings.setUnits(units);
		newSettings.setLeft(left);
		newSettings.setRight(right);
		newSettings.setTop(top);
		newSettings.setBottom(bottom);
		newSettings.setVerticalAlign(verticalAlign);
		newSettings.setHorizontalAlign(horizontalAlign);
		newSettings.setOrientation(orientation);
		newSettings.setResolution(resolution);
		newSettings.setScale(scale);
		newSettings.setPrinterData(getPrinterData());

		return newSettings;
	}

	/**
	 * Converts the value to inches given its units. The result should be inconsistent and only the value part should be
	 * used after this.
	 * 
	 * @param vwu
	 * @return ValueWithUnits
	 */
	public ValueWithUnits scaleToInches(ValueWithUnits vwu) {
		Units convertUnit = this.units;
		for (Units unit : Units.values()) {
			if (unit.getName().equals(vwu.getUnitsName())) {
				convertUnit = unit;
				break;
			}
		}
		return new ValueWithUnits(convertUnit.scaleToInches(vwu.getVal()), convertUnit.getName());
	}

	/**
	 * Converts the value from inches given its units. The input should have the value in inches with the units to be
	 * used in the result and hence not be consistent. The result should be consistent.
	 * 
	 * @param vwu
	 * @return ValueWithUnits
	 */
	public ValueWithUnits scaleFromInches(ValueWithUnits vwu) {
		Units convertUnit = this.units;
		for (Units unit : Units.values()) {
			if (unit.getName().equals(vwu.getUnitsName())) {
				convertUnit = unit;
				break;
			}
		}
		return new ValueWithUnits(convertUnit.scaleFromInches(vwu.getVal()), Units.INCH.getName());
	}

	/**
	 * Parses the input String and determines the value and units.
	 * 
	 * @param string
	 * @return ValueWithUnits
	 */
	public ValueWithUnits parseUnitsString(String string) {
		// Match one or more white space
		String[] tokens = string.trim().split("\\s+");
		if (tokens.length == 0) {
			return null;
		}
		double val = 0;
		String name = this.units.getName();
		try {
			val = Double.parseDouble(tokens[0]);
		} catch (NumberFormatException ex) {
			val = 0;
		}
		if (tokens.length >= 2) {
			for (Units unit : Units.values()) {
				if (tokens[1].toLowerCase().equals(unit.getName())) {
					name = unit.getName();
					break;
				}
			}
		}
		return new ValueWithUnits(val, name);
	}

	// Getters and setters

	/**
	 * @return The name of units.
	 */
	public String getUnitsName() {
		return units.getName();
	}

	/**
	 * @return The value of units.
	 */
	public Units getUnits() {
		return units;
	}

	/**
	 * @param units
	 *            The new value for units.
	 */
	public void setUnits(Units units) {
		this.units = units;
	}

	/**
	 * @param name
	 *            The new name of the units. Defaults to INCH is there is no match.
	 */
	public void setUnits(String name) {
		for (Units units : Units.values()) {
			if (units.getName().equals(name)) {
				this.units = units;
				return;
			}
		}
		this.units = Units.INCH;
	}

	/**
	 * @return The value of left in inches.
	 */
	public double getLeft() {
		return left;
	}

	/**
	 * @param left
	 *            The new value for left in inches.
	 */
	public void setLeft(double left) {
		this.left = left;
	}

	/**
	 * @return The value of left with units.
	 */
	public String getLeftString() {
		return new ValueWithUnits(units.scaleFromInches(left), units.getName()).getString();
	}

	/**
	 * @param string left
	 *            The new value for left with units, for example "1.23 in".
	 */
	public void setLeftString(String string) {
		left = scaleToInches(parseUnitsString(string)).getVal();
	}

	/**
	 * @return The value of right in inches.
	 */
	public double getRight() {
		return right;
	}

	/**
	 * @param right
	 *            The new value for right in inches.
	 */
	public void setRight(double right) {
		this.right = right;
	}

	/**
	 * @return The value of right with units.
	 */
	public String getRightString() {
		return new ValueWithUnits(units.scaleFromInches(right), units.getName()).getString();
	}

	/**
	 * @param string right
	 *            The new value for right with units, for example "1.23 in".
	 */
	public void setRightString(String string) {
		right = scaleToInches(parseUnitsString(string)).getVal();
	}

	/**
	 * @return The value of top in inches.
	 */
	public double getTop() {
		return top;
	}

	/**
	 * @param top
	 *            The new value for top in inches.
	 */
	public void setTop(double top) {
		this.top = top;
	}

	/**
	 * @return The value of top with units.
	 */
	public String getTopString() {
		return new ValueWithUnits(units.scaleFromInches(top), units.getName()).getString();
	}

	/**
	 * @param string top
	 *            The new value for top with units, for example "1.23 in".
	 */
	public void setTopString(String string) {
		top = scaleToInches(parseUnitsString(string)).getVal();
	}

	/**
	 * @return The value of bottom in inches.
	 */
	public double getBottom() {
		return bottom;
	}

	/**
	 * @param bottom
	 *            The new value for bottom in inches.
	 */
	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	/**
	 * @return The value of bottom with units.
	 */
	public String getBottomString() {
		return new ValueWithUnits(units.scaleFromInches(bottom), units.getName()).getString();
	}

	/**
	 * @param string bottom
	 *            The new value for bottom with units, for example "1.23 in".
	 */
	public void setBottomString(String string) {
		bottom = scaleToInches(parseUnitsString(string)).getVal();
	}

	/**
	 * @return The value of verticalAlign.
	 */
	public int getVerticalAlign() {
		return verticalAlign;
	}

	/**
	 * @param verticalAlign
	 *            The new value for verticalAlign.
	 */
	public void setVerticalAlign(int verticalAlign) {
		this.verticalAlign = verticalAlign;
	}

	/**
	 * @return The value of horizontalAlign.
	 */
	public int getHorizontalAlign() {
		return horizontalAlign;
	}

	/**
	 * @param horizontalAlign
	 *            The new value for horizontalAlign.
	 */
	public void setHorizontalAlign(int horizontalAlign) {
		this.horizontalAlign = horizontalAlign;
	}

	/**
	 * @return The value of unitsFormat.
	 */
	public String getUnitsFormat() {
		return unitsFormat;
	}

	/**
	 * @param unitsFormat
	 *            The new value for unitsFormat, something like "%.3f".
	 */
	public void setUnitsFormat(String unitsFormat) {
		this.unitsFormat = unitsFormat;
	}

	/**
	 * Resets the units format to the default.
	 */
	public void resetUnitsFormat() {
		this.unitsFormat = DEFAULT_UNITS_FORMAT;
	}

	/**
	 * @return The value of orientation.
	 */
	public Orientation getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation
	 *            The new value for orientation.
	 */
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	/**
	 * @param name
	 *            The new name of the units. Defaults to DEFAULT is there is no match.
	 */
	public void setOrientation(String name) {
		for (Orientation orientation : Orientation.values()) {
			if (orientation.getName().equals(name)) {
				this.orientation = orientation;
				return;
			}
		}
		//this.orientation = Orientation.DEFAULT;
	}

	/**
	 * @return the printerData
	 */
	public PrinterData getPrinterData() {
		if (printerData == null)
			printerData = Printer.getDefaultPrinterData();
		if (printerData == null)
			printerData = Printer.getPrinterList() != null ? Printer.getPrinterList()[0] : Printer.getDefaultPrinterData();
		return printerData;
	}

	/**
	 * @param printerData
	 *            the printerData to set
	 */
	public void setPrinterData(PrinterData printerData) {
		this.printerData = printerData;
	}

	/**
	 * @return the scale
	 */
	public Scale getScale() {
		return scale;
	}
	
	/**
	 * @param scale
	 *            the scale of the image
	 */
	public void setScale(Scale scale) {
		this.scale = scale;
	}
	
	/**
	 * @return the resolution
	 */
	public Resolution getResolution() {
		return resolution;
	}

	/**
	 * @param resolution
	 *            the scale of the image
	 */
	public void setResolution(Resolution resolution) {
		this.resolution = resolution;
	}

}
