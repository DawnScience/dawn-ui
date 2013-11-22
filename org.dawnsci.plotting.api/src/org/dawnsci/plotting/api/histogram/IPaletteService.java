/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
