/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.internal;



/**
 * @author fcp94556
 *
 */
public class StringUtils {

	/**
	 * Returns a StringBuilder with only the digits and . contained
	 * in the original string.
	 * 
	 * @param text
	 * @param decimalPlaces 
	 * @return StringBuilder
	 */
	public static final StringBuilder keepDigits(final String text,
			                                           int    decimalPlaces) {
		
		// Used to make algorithm below simpler, bit of a hack.
		if (decimalPlaces==0) decimalPlaces = -1;
		
		final StringBuilder buf = new StringBuilder();
		// Remove non digits
		final char [] ca   = text.toCharArray();		
		int decCount = 0;
		for (int i =0;i<ca.length;++i) {
			if (i==0&&ca[i]=='-') {
				buf.append(ca[i]);
				continue;
			}
	        if (StringUtils.isDigit(ca[i])) {
				if ('.'==ca[i]||decCount>0) {
					++decCount;
				}
	        	if (decCount<=decimalPlaces+1) buf.append(ca[i]);
	        } else {
	        	break;
	        }
		}
        return buf;
	}
	
	/**
	 * Returns true if digit or .
	 * @param c
	 * @return boolean
	 */
	public static final boolean isDigit(final char c) {
		if (Character.isDigit(c)) return true;
		if ('.'==c) return true;
		return false;
	}

}

	