/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.functions;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * This class wrappers a colour category extension point so that it can be
 * easily accessed
 * 
 * @author Baha El Kassaby
 *
 */
public class ColourCategoryContribution {

	private static final String ATT_ID = "id";
	private static final String ATT_NAME = "name";
	private static final String ATT_DESCRIPTION = "description";

	private String name;
	private String description;
	private String id;

	public static ColourCategoryContribution getColourCategoryContribution(IConfigurationElement config) {
		ColourCategoryContribution colourCategoryContribution = new ColourCategoryContribution();
		// try to get things out of the config which are required
		try {
			colourCategoryContribution.name = config.getAttribute(ATT_NAME);
			colourCategoryContribution.description = config.getAttribute(ATT_DESCRIPTION);
			colourCategoryContribution.id = config.getAttribute(ATT_ID);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Cannot create ColourCategoryContribution contribution due to the following error", e);
		}

		return colourCategoryContribution;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}
}
