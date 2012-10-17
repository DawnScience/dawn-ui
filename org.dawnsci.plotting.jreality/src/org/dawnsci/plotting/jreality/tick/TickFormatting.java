/*
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

package org.dawnsci.plotting.jreality.tick;

/**
 *
 */
public enum TickFormatting {
	/**
	 * Plain mode no rounding no chopping maximum 6 figures before the 
	 * fraction point and four after
	 */
	plainMode,
	/**
	 * Rounded or chopped to the nearest decimal
	 */
	roundAndChopMode,
	/**
	 * Use Exponent 
	 */
	useExponent,
	/**
	 * Use SI units (k,M,G,etc.)
	 */
	useSIunits
}
