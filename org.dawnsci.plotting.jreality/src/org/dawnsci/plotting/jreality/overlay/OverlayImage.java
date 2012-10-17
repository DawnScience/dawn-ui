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

package org.dawnsci.plotting.jreality.overlay;

/**
 * Overlay image object
 */
public class OverlayImage {

	private byte[] data;
	private int width;
	private int height;
	private boolean isDirty;
	
	public OverlayImage(int width, int height) {
		data = new byte[width*height*4];
		this.width = width;
		this.height = height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public byte[] getImageData() {
		return data;
	}
	
	public void zap() {
		data = new byte[width*height*4];
		isDirty = true;
	}
	
	public void clear(short red, short green, short blue, short alpha) {
		for (int y = 0; y < height; y++)
			for (int x = 0; x <width; x++) {
				data[(x+y*width)*4] = (byte)red;
				data[(x+y*width)*4+1] = (byte)green;
				data[(x+y*width)*4+2] = (byte)blue;
				data[(x+y*width)*4+3] = (byte)alpha;
			}
		isDirty = true;
	}
	
	public boolean isDirty() {
		return isDirty;
	}
	
	public void putPixel(int x, int y,
			             short red, 
			             short green, 
			             short blue, 
			             short alpha) {
		
		if (x >= 0 && x < width &&
			y >= 0 && y < height) {
			data[(x+y*width)*4] = (byte)red;
			data[(x+y*width)*4+1] = (byte)green;
			data[(x+y*width)*4+2] = (byte)blue;
			data[(x+y*width)*4+3] = (byte)alpha;
		}
		isDirty = true;
	}
	
	public void clean() {
		isDirty = false;
	}

	public int[] getShape() {
		return new int[] { height, width };
	}
}
