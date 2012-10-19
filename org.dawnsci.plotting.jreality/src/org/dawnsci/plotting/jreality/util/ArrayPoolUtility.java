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

/**
 * Class for pooling medium sized coordinate and edge arrays to prevent
 * excessive amount of GC and memory fragmentation and also memory stall 
 * request these pools can be used for frequently used access like plots
 */

public class ArrayPoolUtility {

	private static final int INITSIZE = 4096;
	private static final float EXTENSIONFACTOR = 1.25f;
	private static double[][] doubleDoubleArray = null;
	private static int[][] intIntArray = null;
	
	/**
	 * Constructor 
	 */
	public ArrayPoolUtility()
	{
		
	}
	
	/**
	 * Generate and pool a larger DoubleDoubleArray that can be used
	 * for storing coordinates in 3D space
	 * @param length of the array
	 * @return a doubleDouble array that has at least the desired length
	 */
	
	public static double[][] getDoubleArray(int length) {
	   assert length > 0;
	   if (doubleDoubleArray == null)
	   {
		   doubleDoubleArray = new double[Math.max(INITSIZE,length)][3];
	   } else {
		   if (length > doubleDoubleArray.length)
			   doubleDoubleArray = new double[(int)(length*EXTENSIONFACTOR)][3];
	   }
	   return doubleDoubleArray;
	}
	
	/**
	 * Generate and pool a larger IntIntArray that can be used for
	 * storing edges in 3D space
	 * @param length of the array
	 * @return a intInt array that has at least the desired length
	 */
	
	public static int[][] getIntArray(int length) {
		assert length > 0;
		if (intIntArray == null)
		{
			intIntArray = new int[Math.max(INITSIZE,length)][2];
		} else {
		   if (length > intIntArray.length)
			   intIntArray = new int[(int)(length*EXTENSIONFACTOR)][2];
		}
		return intIntArray;
	}
}
