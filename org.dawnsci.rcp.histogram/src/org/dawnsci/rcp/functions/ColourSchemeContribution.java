package org.dawnsci.rcp.functions;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * This class wrappers a Colour Scheme extention point so that it can be easily accessed
 * @author ssg37927
 *
 */
public class ColourSchemeContribution {

	private static final String ATT_ID = "id";
	private static final String ATT_NAME = "name";
	private static final String ATT_RED_ID = "red_transfer_function";
	private static final String ATT_GREEN_ID = "green_transfer_function";
	private static final String ATT_BLUE_ID = "blue_transfer_function";
	private static final String ATT_ALPHA_ID = "alpha_transfer_function";
	
	private String name; 
	private String id;
	private String redID;
	private String greenID;
	private String blueID;
	private String alphaID;
	
		
	public static ColourSchemeContribution getColourSchemeContribution(
			IConfigurationElement config) {
		ColourSchemeContribution colourSchemeContribution = new ColourSchemeContribution();
		// try to get things out of the config which are required
		try {
			colourSchemeContribution.name      = config.getAttribute(ATT_NAME);
			colourSchemeContribution.id        = config.getAttribute(ATT_ID);
			colourSchemeContribution.redID       = config.getAttribute(ATT_RED_ID);
			colourSchemeContribution.greenID     = config.getAttribute(ATT_GREEN_ID);
			colourSchemeContribution.blueID      = config.getAttribute(ATT_BLUE_ID);
			colourSchemeContribution.alphaID     = config.getAttribute(ATT_ALPHA_ID);
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

	
}
