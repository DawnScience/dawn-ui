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

package org.dawnsci.plotting.jreality.data;

/**
 * A small image data container with the minimum set of functionality
 */
public class ColourImageData {
	
	private int imgWidth;
	private int imgHeight;
	private int[] dataContainer;
	
	public ColourImageData(final int width, final int height) {
		imgWidth = width;
		imgHeight = height;
		dataContainer = new int[width * height];
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) dataContainer[x+y*width] = 0;
	}

	public void set(int value, int pos) {
		if (pos < imgWidth * imgHeight && pos >= 0)
			dataContainer[pos] = value;
	}
	
	public void set(int value, int x, int y) {
		if (x >= 0 && x < imgWidth && y >=0 && y < imgHeight) {
			dataContainer[x+y*imgWidth] = value;
		}
	}
	
	public int getHeight() {
		return imgHeight;
	}
	
	public int getWidth() {
		return imgWidth;
	}
	
	public int get(int pos) {
		if (pos >= 0 && pos < imgWidth * imgHeight)
			return dataContainer[pos];
		return -1;
	}

	public int get(int x, int y) {
		return get(x+y*imgWidth);
	}
}
