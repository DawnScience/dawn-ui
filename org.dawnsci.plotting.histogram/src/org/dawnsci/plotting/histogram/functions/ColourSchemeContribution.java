/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.functions;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * This class wrappers a Colour Scheme extention point so that it can be easily accessed
 * @author ssg37927
 * @author Baha El-Kassaby
 *
 */
public class ColourSchemeContribution {

	private static final String ATT_ID = "id";
	private static final String ATT_NAME = "name";
	private static final String ATT_RED_ID = "red_transfer_function";
	private static final String ATT_GREEN_ID = "green_transfer_function";
	private static final String ATT_BLUE_ID = "blue_transfer_function";
	private static final String ATT_ALPHA_ID = "alpha_transfer_function";
	private static final String ATT_RED_INV = "red_inverted";
	private static final String ATT_GREEN_INV = "green_inverted";
	private static final String ATT_BLUE_INV = "blue_inverted";
	private static final String ATT_ALPHA_INV = "alpha_inverted";
	private static final String ATT_CATEGORY = "category";
	
	private String name; 
	private String id;
	private String redID;
	private String greenID;
	private String blueID;
	private String alphaID;
	private String redInverted;
	private String greenInverted;
	private String blueInverted;
	private String alphaInverted;
	private String category;

	public static ColourSchemeContribution getColourSchemeContribution(
			IConfigurationElement config) {
		ColourSchemeContribution colourSchemeContribution = new ColourSchemeContribution();
		// try to get things out of the config which are required
		try {
			colourSchemeContribution.name          = config.getAttribute(ATT_NAME);
			colourSchemeContribution.id            = config.getAttribute(ATT_ID);
			colourSchemeContribution.redID         = config.getAttribute(ATT_RED_ID);
			colourSchemeContribution.greenID       = config.getAttribute(ATT_GREEN_ID);
			colourSchemeContribution.blueID        = config.getAttribute(ATT_BLUE_ID);
			colourSchemeContribution.alphaID       = config.getAttribute(ATT_ALPHA_ID);
			colourSchemeContribution.redInverted   = config.getAttribute(ATT_RED_INV);
			colourSchemeContribution.greenInverted = config.getAttribute(ATT_GREEN_INV);
			colourSchemeContribution.blueInverted  = config.getAttribute(ATT_BLUE_INV);
			colourSchemeContribution.alphaInverted = config.getAttribute(ATT_ALPHA_INV);
			colourSchemeContribution.category          = config.getAttribute(ATT_CATEGORY);
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot create TransferFunctionContribution contribution due to the following error",e);
		}
		
		return colourSchemeContribution; 
	}


	public String getName() {
		return name;
	}


	public String getId() {
		return id;
	}


	public String getRedID() {
		return redID;
	}


	public String getGreenID() {
		return greenID;
	}


	public String getBlueID() {
		return blueID;
	}


	public String getAlphaID() {
		return alphaID;
	}

	private final static String TRUE = "true";

	public Boolean getRedInverted() {
		return TRUE.equalsIgnoreCase(redInverted);
	}


	public Boolean getGreenInverted() {
		return TRUE.equalsIgnoreCase(greenInverted);
	}


	public Boolean getBlueInverted() {
		return TRUE.equalsIgnoreCase(blueInverted);
	}


	public Boolean getAlphaInverted() {
		return TRUE.equalsIgnoreCase(alphaInverted);
	}
	
	public String getCategory() {
		return category;
	}
}
