/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.plotting.histogram.Activator;
import org.dawnsci.plotting.histogram.ExtensionPointManager;
import org.dawnsci.plotting.histogram.functions.ColourCategoryContribution;
import org.dawnsci.plotting.histogram.functions.ColourSchemeContribution;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.histogram.ITransferFunction;
import org.eclipse.dawnsci.plotting.api.histogram.functions.FunctionContainer;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

public class PaletteService extends AbstractServiceFactory implements IPaletteService {

	private static IPaletteService pservice;
	private ExtensionPointManager extensionManager;
	public PaletteService() {
		this.extensionManager = ExtensionPointManager.getManager();
	}
	private Collection<String> colourSchemeNames;
	private Collection<String> colourCategoryNames;

	/**
	 * Colour map inverted
	 */
	private boolean isInverted = Activator.getPlottingPreferenceStore().getBoolean(PlottingConstants.CM_INVERTED);

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
			colourSchemeName = getColorSchemes().iterator().next();
		}
		ColourSchemeContribution csc = extensionManager.getColourSchemeContribution(colourSchemeName);
		int[] red   = extensionManager.getTransferFunctionFromID(csc.getRedID()).getFunction().getArray();
		int[] green = extensionManager.getTransferFunctionFromID(csc.getGreenID()).getFunction().getArray();
		int[] blue  = extensionManager.getTransferFunctionFromID(csc.getBlueID()).getFunction().getArray();
		
		if (isInverted) {
			red = invert(red);
			green = invert(green);
			blue = invert(blue);
		} else {
			if (csc.getRedInverted()) {
				red = invert(red);
			}
			if (csc.getGreenInverted()) {
				green = invert(green);
			}
			if (csc.getBlueInverted()) {
				blue = invert(blue);
			}
		}

		RGB[] rgbs = new RGB[ITransferFunction.SIZE];

		for (int i = 0; i < ITransferFunction.SIZE; i++) {
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
			colourSchemeName = getColorSchemes().iterator().next();
		}
		ColourSchemeContribution csc = extensionManager.getColourSchemeContribution(colourSchemeName);
		
		ITransferFunction    red  = extensionManager.getTransferFunctionFromID(csc.getRedID()).getFunction();
		ITransferFunction   blue  = extensionManager.getTransferFunctionFromID(csc.getBlueID()).getFunction();
		ITransferFunction    grn  = extensionManager.getTransferFunctionFromID(csc.getGreenID()).getFunction();
		ITransferFunction   alpha = extensionManager.getTransferFunctionFromID(csc.getAlphaID()).getFunction();
		if (red==null || blue == null || grn == null ) return null;
		boolean redInverted = csc.getRedInverted();
		boolean greenInverted = csc.getGreenInverted();
		boolean blueInverted = csc.getBlueInverted();
		boolean alphaInverted = csc.getAlphaInverted();
		if (isInverted) {
			redInverted = !redInverted;
			greenInverted = !greenInverted;
			blueInverted = !blueInverted;
			alphaInverted = !alphaInverted;
		}
		return new FunctionContainer(red, grn, blue, alpha, redInverted, greenInverted, blueInverted, alphaInverted);
	}

	@Override
	public void setInverted(boolean inverted) {
		this.isInverted = inverted;
	}

	/**
	 * used by osgi injection
	 * @param ps
	 */
	public void setPaletteService(IPaletteService ps) {
		pservice = ps;
	}

	public static IPaletteService getPaletteService() {
		return pservice;
	}

	@Override
	public List<String> getColorsByCategory(String sCategory) {
		List<String> colours = new ArrayList<String>();
		List<ColourSchemeContribution> contributions = extensionManager.getColourSchemeContributions();
		for (ColourSchemeContribution contrib : contributions) {
			ColourCategoryContribution categoryContrib = extensionManager.getColourCategoryFromID(contrib.getCategory());
			String name = categoryContrib.getName();
			if (sCategory.equals(name)) {
				colours.add(contrib.getName());
			} else if (sCategory.equals("All")) {
				colours.add(contrib.getName());
			}
		}
		return colours;
	}

	@Override
	public String getColorCategory(String colour) {
		ColourSchemeContribution colourSchemeContrib = extensionManager.getColourSchemeContribution(colour);
		ColourCategoryContribution categoryContrib = extensionManager.getColourCategoryFromID(colourSchemeContrib.getCategory());
		return categoryContrib.getName();
	}

	@Override
	public Collection<String> getColorCategories() {
		if (colourCategoryNames != null)
			return colourCategoryNames;
		colourCategoryNames = new ArrayList<String>(7);
		final List<ColourCategoryContribution> cc = extensionManager.getColourCategoryContributions();
		for (ColourCategoryContribution ccc : cc) {
			colourCategoryNames.add(ccc.getName());
		}
		return colourCategoryNames;
	}

	@Override
	public String getDefaultColorScheme() {
		return Activator.getPlottingPreferenceStore().getString(PlottingConstants.COLOUR_SCHEME);
	}

	@Override
	public void setDefaultColorScheme(String color) {
		if (getColorSchemes().contains(color)) {
			Activator.getPlottingPreferenceStore().setValue(PlottingConstants.COLOUR_SCHEME, color);
		} else {
			throw new IllegalArgumentException("Default color scheme must match one of the names returned by getColorSchemes");
		}
	}
}
