/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.plotting.histogram.functions.ColourCategoryContribution;
import org.dawnsci.plotting.histogram.functions.ColourSchemeContribution;
import org.dawnsci.plotting.histogram.functions.TransferFunctionContribution;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

public class ExtensionPointManager {
	private static final String TRANSFER_FUNCTION_ID = "org.dawnsci.plotting.histogram.channelColourScheme";
	private static final String COLOUR_CATEGORY_ID = "org.dawnsci.plotting.histogram.colourCategory";
	private static final String COLOUR_SCHEME_ID = "org.dawnsci.plotting.histogram.colourScheme";
	private List<TransferFunctionContribution> transferFunctions;
	private List<ColourCategoryContribution> colourCategories;
	private List<ColourSchemeContribution> colourSchemes;

	private static ExtensionPointManager staticManager;
	public static ExtensionPointManager getManager() {
		if (staticManager==null) staticManager = new ExtensionPointManager();
		return staticManager;
	}
	private ExtensionPointManager() {
		
	}

	/**
	 * Get all the extensions for a particular ID
	 * @param extensionPointId The ID which is referenced
	 * @return
	 */
	private IExtension[] getExtensions(String extensionPointId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(extensionPointId);
		IExtension[] extensions = point.getExtensions();
		return extensions;
	}

	/**
	 * Get all the relevant transfer Function Contributions
	 * 
	 * @return
	 */
	public List<TransferFunctionContribution> getTransferFunctionContributions() {
		if (transferFunctions != null) {
			return transferFunctions;
		}
		transferFunctions = new ArrayList<TransferFunctionContribution>();
		IExtension[] extensions = getExtensions(TRANSFER_FUNCTION_ID);
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				IConfigurationElement config = configElements[j];
				transferFunctions.add(TransferFunctionContribution.getTransferFunctionContribution(config));
			}
		}
		return transferFunctions;
	}

	/**
	 * Get all the relevant transfer Function Contributions
	 * 
	 * @return
	 */
	public List<ColourCategoryContribution> getColourCategoryContributions() {
		if (colourCategories != null) {
			return colourCategories;
		}
		colourCategories = new ArrayList<ColourCategoryContribution>();
		IExtension[] extensions = getExtensions(COLOUR_CATEGORY_ID);
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				IConfigurationElement config = configElements[j];
				colourCategories.add(ColourCategoryContribution.getColourCategoryContribution(config));
			}
		}
		return colourCategories;
	}

	/**
	 * Get all the Colour Scheme Contributions
	 * @return
	 */
	public List<ColourSchemeContribution> getColourSchemeContributions() {

		if (colourSchemes != null) {
			return colourSchemes;
		}

		colourSchemes = new ArrayList<ColourSchemeContribution>();
		
		IExtension[] extensions = getExtensions(COLOUR_SCHEME_ID);

		for(int i=0; i<extensions.length; i++) {

			IExtension extension = extensions[i];
			IConfigurationElement[] configElements = extension.getConfigurationElements();	

			for(int j=0; j<configElements.length; j++) {
				IConfigurationElement config = configElements[j];
				
				colourSchemes.add(ColourSchemeContribution.getColourSchemeContribution(config));
			
			}
		}
		
		return colourSchemes;
	}

	/**
	 * Get a transfer function contribution by name
	 * @param name the name of the Function
	 * @return
	 */
	public TransferFunctionContribution getTransferFunction(String name) {
		for (TransferFunctionContribution function : getTransferFunctionContributions()) {
			if (function.getName().compareTo(name) == 0) {
				return function;
			}
		}
		throw new IllegalArgumentException("Could not find an appropriate Transfer Function");
	}

	/**
	 * Get a transfer function contribution by name
	 * @param name the name of the Function
	 * @return
	 * @deprecated Use {@link #getTransferFunctionFromID(String)}
	 */
	@Deprecated
	public TransferFunctionContribution getTransferFunctionByID(String id) {
		return getTransferFunctionFromID(id);
	}

	/**
	 * Get a colour scheme contribution by name
	 * @param name the name of the colour scheme
	 * @return
	 */
	public ColourSchemeContribution getColourSchemeContribution(String name) {
		for (ColourSchemeContribution colourScheme : getColourSchemeContributions()) {
			if(colourScheme.getName().compareTo(name) == 0) {
				return colourScheme;
			}
		}
		throw new IllegalArgumentException("Could not find an appropriate Colour Scheme '" + name + "'");
	}

	/**
	 * Get a transfer function contribution by ID
	 * 
	 * @param id
	 *            the ID of the function
	 * @return
	 */
	public TransferFunctionContribution getTransferFunctionFromID(String id) {
		for (TransferFunctionContribution function : getTransferFunctionContributions()) {
			if (function.getId().compareTo(id) == 0) {
				return function;
			}
		}
		throw new IllegalArgumentException("Could not find an appropriate id");
	}

	/**
	 * Get a colour category contribution by ID
	 * 
	 * @param id
	 *            the ID of the category
	 * @return
	 */
	public ColourCategoryContribution getColourCategoryFromID(String id) {
		for (ColourCategoryContribution category : getColourCategoryContributions()) {
			if (category.getId().compareTo(id) == 0) {
				return category;
			}
		}
		throw new IllegalArgumentException("Could not find an appropriate id");
	}
}
