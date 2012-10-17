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

import java.util.LinkedList;

/**
 * Tick factory produces the different axis ticks. When specifying a format and
 * given the screen size parameters and range it will return a list of Ticks
 */

public class TickFactory {
	
	private TickFormatting formatOfTicks;
	private final static int TICKMINDIST_IN_PIXELS_X = 60;
	private final static int TICKMINDIST_IN_PIXELS_Y = 30;
	private final static double EPSILON = 1.0E-20;
	
	private int numTicks = 0;
	private double tickUnit = 0.0;
	private double internalGraphmin;
	private double realGraphmin;
	private double graphmax;
	private int nfrac;
	private boolean overwriteMinAnyway = false;
	
	/**
	 * @param format
	 */
	public TickFactory(TickFormatting format)
	{
	   formatOfTicks = format;	
	}
	

	/**
	 * Set the TickFactory to a new formating mode
	 * @param format new format to be used 
	 */
	
	public void setTickMode(TickFormatting format)
	{
		formatOfTicks = format;
	}
	
	private double nicenum(double x, boolean round)
	{
	    int expv;				/* exponent of x */
	    double f;				/* fractional part of x */
	    double nf;				/* nice, rounded fraction */

	    expv = (int) Math.floor(Math.log10(x));
	    f = x/Math.pow(10., expv);		/* between 1 and 10 */
	    if (round) 
	    	if (f<1.5) nf = 1.;
	    	else if (f<3.) nf = 2.;
	    	else if (f<7.) nf = 5.;
	    	else nf = 10.;
	    else
	    	if (f<=1.) nf = 1.;
	    	else if (f<=2.) nf = 2.;
	    	else if (f<=5.) nf = 5.;
	    	else nf = 10.;
	    return nf*Math.pow(10., expv);
	}
	
	private void determineNumTicks(int size, 
								   double min, 
								   double max, 
								   short axisNo,
								   boolean allowMinMaxOver)
	{
		int maximumNumTicks = 0;
		overwriteMinAnyway = false;
		switch(axisNo)
		{
			case 0:
				maximumNumTicks = Math.min(8,size / TICKMINDIST_IN_PIXELS_X);
			break;
			case 1:
				maximumNumTicks = Math.min(8, size / TICKMINDIST_IN_PIXELS_Y);
			break;
			case 2:
				maximumNumTicks = size;
			break;
		}
		double range = max - min;

		numTicks = Math.max(2, maximumNumTicks);
		
		// tick points to dense do not do anything
		if (Math.abs(range) < EPSILON) {
			numTicks = 0;
		} else {
		    range = nicenum(max-min, false);
		    tickUnit = nicenum(range/(numTicks-1), true);
		    if (allowMinMaxOver) {
		    	internalGraphmin = Math.floor(min/tickUnit)*tickUnit;
		    	// check if difference is too large
		    	if ((min - internalGraphmin) > 1000)
		    	{
		    		overwriteMinAnyway = true;
		    		realGraphmin = min;
		    	} else
		    		realGraphmin = internalGraphmin;
		    	graphmax = Math.ceil(max/tickUnit)*tickUnit;
		    } else {
		    	internalGraphmin = min;
		    	realGraphmin = min;
		    	graphmax = max;
		    }
		    nfrac = (int) Math.max(-Math.floor(Math.log10(tickUnit)), 0);	
		}
	}
	
	String getTickString(double value)
	{
		String returnString = "";
		switch (formatOfTicks)
		{
			case plainMode:
			{
				String formatStr = String.format("%%.%df",nfrac);
				returnString = String.format(formatStr,value);
			}
			break;
			case roundAndChopMode:
			{
				long newValue = Math.round(value);
				returnString = String.format("%d", newValue);
			}
			break;
			case useExponent:
			{
				String formatStr = String.format("%%.%de",nfrac);
				returnString = String.format(formatStr,value);
			}
			break;
			case useSIunits:
				double absValue = Math.abs(value);
				if (absValue <= 1E-12 && absValue != 0.0)
				{
					returnString = String.format("%6.2f", value*1E12)+"p";
				} else if (absValue <= 1E-9 && absValue != 0.0)
				{
					returnString = String.format("%6.2f", value*1E9)+"n";
				} else if (absValue <= 1E-6 && absValue != 0.0)
				{
					returnString = String.format("%6.2f",value*1E6)+"µ";
				} else if (absValue <= 1E-3 & absValue != 0.0)
				{
					returnString = String.format("%6.2f", value*1E3)+"m";
				}else if (absValue < 1E3)
				{
					returnString = String.format("%6.2f",value);
				}else if (absValue < 1E6) {
					returnString = String.format("%6.2f",value/1E3)+"k";
				} else if (absValue < 1E9) {
					returnString = String.format("%6.2f",value/1E6)+"M";
				} else if (absValue < 1E12) {
					returnString = String.format("%6,2f",value/1E9)+"G";
				} else if (absValue < 1E15) {
					returnString = String.format("%6.2f",value/1E12)+"T";
				} else if (absValue < 1E18)
					returnString = String.format("%6,2f",value/1E15)+"P";
			break;
		}
		return returnString;
	}
	
	/**
	 * @return the size of a tick unit
	 */
	
	public double getTickUnit()
	{
		return tickUnit;
	}

	/**
	 * @return the label minimum
	 */
	
	public double getLabelMin()
	{
		return realGraphmin;
	}
	
	/**
	 * @return the label maximum
	 */	
	
	public double getLabelMax()
	{
		return graphmax;
	}
	
	/**
	 * @param displaySize 
	 * @param min
	 * @param max
	 * @param axisNo
	 * @param allowMinMaxOver allow min/maximum overwrite
	 * @return a list of the ticks for the axis
	 */
	
	public LinkedList<Tick> generateTicks(int displaySize, 
										  double min, 
										  double max, 
										  short axisNo,
										  boolean allowMinMaxOver)
	{
		LinkedList<Tick> ticks = new LinkedList<Tick>();
		determineNumTicks(displaySize, min, max, axisNo,allowMinMaxOver);
		double i = internalGraphmin;
		while (i < graphmax + 0.5 * tickUnit) {
			Tick newTick = new Tick();
			if (i == internalGraphmin && overwriteMinAnyway)
				newTick.setTickValue(realGraphmin);
			else
				newTick.setTickValue(i);
			newTick.setTickName(getTickString(newTick.getTickValue()));
			if (allowMinMaxOver || i <= max)
				ticks.add(newTick);
			 double newTickValue = i + tickUnit;
			 if (i == newTickValue)
				 break;
			 i = newTickValue;
		}
		return ticks;
	}
	
}
