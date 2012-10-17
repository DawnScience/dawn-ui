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

package org.dawnsci.plotting.jreality.util;

import org.dawnsci.plotting.jreality.core.ScaleType;

/**
 * Utility class for different type of value scaling operations
 */

public class ScalingUtility {

	private static boolean smallLogFlag = true;
	private static final double LOG2 = Math.log10(2.0);
	public static void setSmallLogFlag(boolean flag)
	{
		smallLogFlag = flag;
	}
	
	/**
	 * @param in
	 *            input z value
	 * @param currentScaling
	 *            current scaling type
	 * @return scaled value
	 */
	public static double valueScaler(double in, ScaleType currentScaling) {
		double out = in;
		switch (currentScaling) {
			case LINEAR:
			break;
			case LOG2:
				if (smallLogFlag)
				{
					out = Math.log10(in) / LOG2;
				} else {
					boolean negFlag = (in < 0.0);
					if (negFlag) in = -in;
					if (in < 2.0) {
						in += (2.0 -in) / 2.0; 
					}
					out = Math.log10(in) / LOG2;
					out = negFlag ? (-out):out;
				}
			break;
			case LOG10:
				if (smallLogFlag)
				{
					out = Math.log10(in);
				} else {
					boolean negFlag = (in < 0.0);
					if (negFlag) in = -in;
					if (in < 10.0) {
						in += (10.0 - in) / 10.0;
					}
					out = Math.log10(in);
					out = negFlag ? (-out):out;
				}
			break;
			case LN:
				if (smallLogFlag)
				{
					out = Math.log(in);
				} else {
					boolean negFlag = (in < 0.0);
					if (negFlag) in = -in;
					if (in < Math.E) {
						in += (Math.E - in) / Math.E;
					}
					out = Math.log(in);
					out = negFlag ? (-out):out;
				}
			break;
		}
		return out;
	}

	/**
	 * Compute the inverse operation of the axis scaling
	 * 
	 * @param in
	 *            scaled value
	 * @param currentScaling
	 *            current scaling mode
	 * @return inverse scaled value
	 */

	public static double inverseScaler(double in, ScaleType currentScaling) {
		double out = in;
		switch (currentScaling) {
			case LINEAR:
			break;
			case LOG2:
				if (smallLogFlag)
				{
					out = Math.pow(2.0, in);
				} else {
					boolean negFlag = (in < 0.0);
					if (negFlag)
						in = -in;
					if (in < 1.0) {
						out = (Math.pow(2,in+1) - 2.0);
					} else {
						out = Math.pow(2,in);
					}
					out = negFlag?(-out):out;
				}
			break;
			case LOG10:
				if (smallLogFlag)
				{
					out = Math.pow(10.0, in);					
				} else {
					boolean negFlag = (in < 0.0);
					if (negFlag)
						in = -in;
					if (in < 1.0) {
						out = (Math.pow(10,in+1) - 10.0) / 9.0;
					} else {
						out = Math.pow(10,in);
					}
					out = negFlag?(-out):out;
				}
			break;
			case LN:
				if (smallLogFlag)
				{
					out = Math.exp(in);
				} else {
					boolean negFlag = (in < 0.0);
					if (negFlag)
						in = -in;
					if (in < 1.0) {
						out = (Math.exp(in+1) - Math.E) / (Math.E-1.0);						
					} else {
						out = Math.exp(in);
					}
					out = negFlag?(-out):out;	
				}
			break;
		}
		return out;
	}
	
}
