/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
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

import org.dawnsci.plotting.histogram.ExtensionPointManager;
import org.dawnsci.plotting.histogram.functions.ColourSchemeContribution;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.histogram.ITransferFunction;
import org.eclipse.dawnsci.plotting.api.histogram.functions.FunctionContainer;
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
	private boolean isInverted = false;

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
	public static void setPaletteService(IPaletteService ps) {
		pservice = ps;
	}

	public static IPaletteService getPaletteService() {
		return pservice;
	}

}
