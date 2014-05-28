package org.dawnsci.plotting.histogram.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.plotting.api.histogram.IPaletteService;
import org.dawnsci.plotting.api.histogram.ITransferFunction;
import org.dawnsci.plotting.api.histogram.functions.FunctionContainer;
import org.dawnsci.plotting.histogram.ExtensionPointManager;
import org.dawnsci.plotting.histogram.functions.ColourSchemeContribution;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

public class PaletteService extends AbstractServiceFactory implements IPaletteService {

	private ExtensionPointManager extensionManager;
	public PaletteService() {
		this.extensionManager = ExtensionPointManager.getManager();
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
	public PaletteData getDirectPaletteData(String colourSchemeName) {
		
		if ("".equals(colourSchemeName)) {
			colourSchemeName = "Film Negative";
		}
		ColourSchemeContribution csc = extensionManager.getColourSchemeContribution(colourSchemeName);
		int[] red   = extensionManager.getTransferFunctionFromID(csc.getRedID()).getFunction().getArray();
		int[] green = extensionManager.getTransferFunctionFromID(csc.getGreenID()).getFunction().getArray();
		int[] blue  = extensionManager.getTransferFunctionFromID(csc.getBlueID()).getFunction().getArray();
		
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
	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface, 
			             IServiceLocator parentLocator,
			             IServiceLocator locator) {
		
        if (serviceInterface==IPaletteService.class) {
        	return this;
        }
		return null;
	}

	@Override
	public FunctionContainer getFunctionContainer(String colourSchemeName) {
		if ("".equals(colourSchemeName)) {
			colourSchemeName = "Film Negative";
		}
		ColourSchemeContribution csc = extensionManager.getColourSchemeContribution(colourSchemeName);
		
		ITransferFunction    red  = extensionManager.getTransferFunctionFromID(csc.getRedID()).getFunction();
		ITransferFunction   blue  = extensionManager.getTransferFunctionFromID(csc.getBlueID()).getFunction();
		ITransferFunction    grn  = extensionManager.getTransferFunctionFromID(csc.getGreenID()).getFunction();
		ITransferFunction   alpha = extensionManager.getTransferFunctionFromID(csc.getAlphaID()).getFunction();
		if (red==null || blue == null || grn == null ) return null;
		return new FunctionContainer(red, grn, blue, alpha, csc.getRedInverted(), csc.getGreenInverted(), csc.getBlueInverted(), csc.getAlphaInverted());
	}

}
