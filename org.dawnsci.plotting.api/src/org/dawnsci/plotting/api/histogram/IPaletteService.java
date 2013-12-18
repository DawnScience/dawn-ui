/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.api.histogram;

import java.util.Collection;

import org.eclipse.swt.graphics.PaletteData;

/**
 * A service for managing colour schemes.
 * 
 * The colour schemes are contributed by an extension point contributed by the
 * org.dawnsci.rcp.histogram plugin. This plugin also contribute  this service.
 * The service provides the names of at the colour schemes and a way to get the scheme 
 * as a PaletteData object.
 */
public interface IPaletteService {

	/**
	 * Names of schemes
	 * @return
	 */
	public Collection<String> getColorSchemes();
	
	/**
	 * 8-bit Palette data from scheme.
	 * 
	 * @param colourSchemeName
	 * @return
	 */
	public PaletteData getPaletteData(final String colourSchemeName);
}
