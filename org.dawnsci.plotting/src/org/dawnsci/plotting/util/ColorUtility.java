/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.plotting.util;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Class encapsulates generating unique colours for plots.
 */
public class ColorUtility {
	
	// NOTE Color choice used to be 9, however 17 is a better choice for scan plots as there
	// are regularly more than nine plots in a single scan.
	// See http://www.gnuplotting.org/tag/palette/
	public static final Color[] GRAPH_DEFAULT_COLORS = { 

			/* My primaries, secondaries and tertiaries, of varying shade */
			new Color(0, 0, 255),		// blue (dark)
		new Color(255, 0, 0),		// red (mid)
		new Color(0, 220, 0),		// green (bright)
		new Color(0,0,0),			// black (dark)
		new Color(170, 104, 0), 	// sienna (mid)
		new Color(0, 191, 191), 	// cyan (bright)
		new Color(141, 0, 134), 	// murex (dark)
		new Color(136, 136, 136),	// grey (mid)
		new Color(189, 173, 0), 	// gold (bright)
		new Color(21, 78, 86),		// teal (dark)
		new Color(220, 0, 206),		// magenta (mid)
		new Color(255, 131, 120),	// coral (bright)
		new Color(0, 85, 13),		// forest green (dark)
		new Color(153, 69, 255),	// purple (mid)
		new Color(188, 188, 188),	// grey (bright)
		new Color(116, 91, 24), 	// umber (dark)
		new Color(0, 135, 147),		// teal (mid)
		new Color(255, 85, 196),	// hot pink (bright)
		new Color(31, 60, 166), 	// lapis (dark)
		new Color(211, 64, 105),	// rose (mid)
		new Color(140, 184, 96),	// sage (bright)
		new Color(88, 88, 88),		// grey (dark)
		new Color(126, 109, 170),	// lavender (mid)
		new Color(247, 147, 30),	// orange(bright)
		new Color(124, 39, 72),		// plum (dark)
		new Color(84, 132, 0),		// spring green (mid)
		new Color(137, 155, 255),	// sky blue(bright)
		new Color(174, 0, 45),		// crimson (dark)
		
		
		//		new Color(179, 179, 0),		// goldish (mid)
//		new Color(0, 217, 217),		// turquoise (bright)
//		new Color(128, 0, 128),		// fuchsia (dark)
//		new Color(128, 64, 0),		// sienna (bright)
//		new Color(0, 180, 90),		// forest green (mid)
//		new Color(160, 32, 255),		// purple (dark)
//		new Color(0, 128, 255),		// azure (mid)
//		new Color(192, 0, 96),		// rose (bright)
//		new Color(78, 154, 0)		// spring green (dark)

		
		
		
	};
	
	public static final Collection<org.eclipse.swt.graphics.Color> DEFAULT_SWT_COLORS;
	static {
		DEFAULT_SWT_COLORS = new HashSet<org.eclipse.swt.graphics.Color>(GRAPH_DEFAULT_COLORS.length);
		for (Color col : GRAPH_DEFAULT_COLORS) {
			DEFAULT_SWT_COLORS.add(new org.eclipse.swt.graphics.Color(Display.getCurrent(), col.getRed(), col.getGreen(), col.getBlue()));
		}
	}
	
	private static final  int[]           GRAPH_DEFAULT_LINEWIDTHS;
	static {
		GRAPH_DEFAULT_LINEWIDTHS = new  int [GRAPH_DEFAULT_COLORS.length];
		for (int i = 0; i < GRAPH_DEFAULT_COLORS.length; i++) {
			GRAPH_DEFAULT_LINEWIDTHS[i] = 1; // NOTE: Changed from 2 to 1, walkthrough with Joachim
		}
	}
	
		
	/**
	 * Get the size of the colour table
	 * @return size of the colour table
	 */
	
	public static int getSize()
	{
		return GRAPH_DEFAULT_COLORS.length;
	}
	
	/**
	 * Get the colour in the colour table
	 * @param nr entry number in the colour table
	 * @return colour entry in the table as AWT colour
	 */
	public static final Color getDefaultColour(int nr) {
		return GRAPH_DEFAULT_COLORS[nr%GRAPH_DEFAULT_COLORS.length];
	}
	
	
	/**
	 * Get the line width in the colour table
	 * @param nr entry number in the line width table
	 * @return line width entry in the table
	 */
	public static final int getDefaultLineWidth(int nr) {
		return GRAPH_DEFAULT_LINEWIDTHS[nr%GRAPH_DEFAULT_LINEWIDTHS.length];
	}
	
	private static Map<String,Color> nameCache;
	/**
	 * 
	 * @param iplot
	 * @param name
	 * @return colour
	 */
	public static final Color getDefaultColour(final int iplot, final String name) {
		
		if (nameCache == null) nameCache = new HashMap<String,Color>(1000);
		if (nameCache.containsKey(name)) return nameCache.get(name);
		
		final Color color = getDefaultColour(iplot);
		nameCache.put(name, color);
		
		// More than 1000 names of different plots are less likely
		if (nameCache.size()>1000) nameCache.clear();
		
		return color;
	}
	
	/**
	 * Set a new default colour for example from the preferences
	 * @param nr number in the table that should be replaced
	 * @param color new colour
	 */
	public static final void setDefaultColour(int nr, java.awt.Color color) {
		if (nr < GRAPH_DEFAULT_COLORS.length) {
			GRAPH_DEFAULT_COLORS[nr] = color;
		}
	}
	
	/**
	 * Set a new default line width for example from the prefrences
	 * @param nr number in the table that should be replaced
	 * @param newLineWidth new line width
	 */
	public static final void setDefaultLineWidth(int nr, int newLineWidth) {
		if (nr < GRAPH_DEFAULT_LINEWIDTHS.length) {
			GRAPH_DEFAULT_LINEWIDTHS[nr] = newLineWidth;
		}
	}

	public static org.eclipse.swt.graphics.Color getSwtColour(final Collection<org.eclipse.swt.graphics.Color> taken, int iplot) {
		
		Color col = getDefaultColour(iplot);
		org.eclipse.swt.graphics.Color swtColor =  new org.eclipse.swt.graphics.Color(Display.getCurrent(), col.getRed(), col.getGreen(), col.getBlue());

		if (taken!=null) {
			int loopCount=0;
			while(taken.contains(swtColor)) {
				iplot++;
				col = getDefaultColour(iplot);
				swtColor =  new org.eclipse.swt.graphics.Color(Display.getCurrent(), col.getRed(), col.getGreen(), col.getBlue());
				loopCount++;
				if (loopCount>GRAPH_DEFAULT_COLORS.length) break;
			}
		}
		
		return swtColor;
	}

	public static org.eclipse.swt.graphics.Color getSwtColour(int iplot) {
		final Color col = getDefaultColour(iplot);
		return new org.eclipse.swt.graphics.Color(Display.getCurrent(), col.getRed(), col.getGreen(), col.getBlue());
	}

	
	public static final int[] getIntArray(RGB rgb) {
		if (rgb==null) return null;
		return new int[]{rgb.red, rgb.green, rgb.blue};
	}

	public static final RGB getRGB(int... color) {
		if (color==null) return null;
		return new RGB(color[0], color[1], color[2]);
	}
}
