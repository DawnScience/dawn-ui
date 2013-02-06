package org.dawnsci.rcp.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.services.IPaletteService;
import org.dawnsci.rcp.functions.ColourSchemeContribution;
import org.dawnsci.rcp.histogram.ExtentionPointManager;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

public class PaletteService extends AbstractServiceFactory implements IPaletteService {

	private ExtentionPointManager extensionManager;
	public PaletteService() {
		this.extensionManager = ExtentionPointManager.getManager();
	}
	private Collection<String> colourSchemeNames;
	@Override
	public Collection<String> getColorSchemes() {
		if (colourSchemeNames!=null) return colourSchemeNames;
		colourSchemeNames = new ArrayList<String>(7);
		final List<ColourSchemeContribution> cs = extensionManager.getColourSchemeContributions(); 
		for (ColourSchemeContribution csc : cs) {
			colourSchemeNames.add(csc.getName());
		}
		return colourSchemeNames;
	}

	@Override
	public PaletteData getPaletteData(String colourSchemeName) {
		
		if ("".equals(colourSchemeName)) {
			colourSchemeName = "Film Negative";
		}
		ColourSchemeContribution csc = extensionManager.getColourSchemeContribution(colourSchemeName);
		int[] red   = extensionManager.getTransferFunctionByID(csc.getRedID()).getFunction().getArray();
		int[] green = extensionManager.getTransferFunctionByID(csc.getGreenID()).getFunction().getArray();
		int[] blue  = extensionManager.getTransferFunctionByID(csc.getBlueID()).getFunction().getArray();
		
		if (csc.getRedInverted()) {
			red = invert(red);
		}
		if (csc.getGreenInverted()) {
			green = invert(green);
		}
		if (csc.getBlueInverted()) {
			blue = invert(blue);
		}

		RGB[] rgbs = new RGB[256];

		for (int i = 0; i < 256; i++) {
			rgbs[i] = new RGB(red[i], green[i], blue[i]);
		}
		return new PaletteData(rgbs);
	}
	
	private int[] invert(int[] array) {
		int[] result = new int[array.length];
		for(int i = 0; i < array.length; i++) {
			result[i] = array[array.length-1-i];
		}
		return result;
	}

	@Override
	public Object create(Class           serviceInterface, 
			             IServiceLocator parentLocator,
			             IServiceLocator locator) {
		
        if (serviceInterface==IPaletteService.class) {
        	return this;
        }
		return null;
	}

}
