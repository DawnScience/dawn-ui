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

package org.dawnsci.plotting.jreality.tool;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.dawnsci.plotting.jreality.core.IDataSet3DCorePlot;

import de.jreality.math.Matrix;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

/**
 * Image position tool 
 */
public class ImagePositionTool extends AbstractTool {

	private static final InputSlot pointerSlot = InputSlot.getDevice("PointerTransformation");
	private static final InputSlot leftMouseButtonSlot = InputSlot.getDevice("PrimaryAction");
	private static final InputSlot rightMouseButtonSlot = InputSlot.getDevice("PrimarySelection");
	private static final InputSlot ctrlKeySlot = InputSlot.getDevice("Meta");
	private static final InputSlot shiftKeySlot = InputSlot.getDevice("Secondary");
	private Matrix pointerTrans = new Matrix();
	private boolean geometryMatched;
	private boolean leftMButton = false;
	private boolean rightMButton = false;
	private double[] pickedPointTC;
	private int[] pickedPointInPixel = new int[2];
	private boolean initial = false;
	private LinkedList<ImagePositionListener> listeners;
	private int primID = -1;
	int imageWidth;
	int imageHeight;
	double maxX;
	double maxY;
	/**
	 * Constructor for an ImagePositionTool
	 * @param width image width
	 * @param height image height
	 * @param maxX maximum X coordinates
	 * @param maxY maximum Y coordinates
	 */
	public ImagePositionTool(int width, int height,
							 double maxX, double maxY) {
		super(leftMouseButtonSlot,rightMouseButtonSlot);
		addCurrentSlot(pointerSlot);
		addCurrentSlot(ctrlKeySlot);
		addCurrentSlot(shiftKeySlot);
		this.maxX = maxX;
		this.maxY = maxY;
		this.imageWidth = width;
		this.imageHeight = height;
		listeners = new LinkedList<ImagePositionListener>();
	}
	
	/**
	 * Set a new maximum x and y coordinate
	 * @param maxX new maximum x coordinate
	 * @param maxY new maximum y coordinate
	 */
	
	public void setMaxXY(double maxX, double maxY)
	{
		this.maxX = maxX;
		this.maxY = maxY;
	}
	
	/**
	 * Set a new image width
	 * @param width image width
	 */
	public void setImageWidth(int width) {
		imageWidth = width;
	}

	/**
	 * Set a new image height
	 * @param height image height
	 */
	public void setImageHeight(int height) {
		imageHeight = height;
	}
	
    @Override
	public void activate(ToolContext tc){
		initial = true;
		perform(tc);
		ListIterator<ImagePositionListener> iter = listeners.listIterator();
		short flags = 0;
		
		if (tc.getAxisState(rightMouseButtonSlot).isPressed()) {
			rightMButton = true;
			flags += IImagePositionEvent.RIGHTMOUSEBUTTON;
		}
		if (tc.getAxisState(leftMouseButtonSlot).isPressed()) {
			leftMButton = true;
			flags += IImagePositionEvent.LEFTMOUSEBUTTON;
		}
		if (tc.getAxisState(ctrlKeySlot).isPressed()) {
			flags += IImagePositionEvent.CTRLKEY;
		}
		if (tc.getAxisState(shiftKeySlot).isPressed()) {
			flags += IImagePositionEvent.SHIFTKEY;
		}
		while (iter.hasNext()) {
			ImagePositionListener listener = iter.next();
			ImagePositionEvent event = new ImagePositionEvent(this,pickedPointTC,
															  pickedPointInPixel,
															  primID,
															  flags,
															  IDataPositionEvent.Mode.START);
			listener.imageStart(event);
		}
		initial = false;
    }
    
	@Override
	public void perform(ToolContext tc) {  
		tc.getTransformationMatrix(pointerSlot).toDoubleArray(pointerTrans.getArray());
		
		geometryMatched=(!(tc.getCurrentPick() == null));
		if(geometryMatched) {
			PickResult currentPick = null;
			if((tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_FACE ||
				tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_LINE ||
				tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT)) {
				List<PickResult> results = tc.getCurrentPicks();
				Iterator<PickResult> iter = results.iterator();
				primID = -1;
				while (iter.hasNext()) {
					PickResult result = iter.next();
					String name = result.getPickPath().getLastComponent().getName();
					if (name.length() >= IDataSet3DCorePlot.OVERLAYPREFIX.length() &&
						name.contains("overlayPrim")) {
						String testStr = "";
						testStr = name.substring(IDataSet3DCorePlot.OVERLAYPREFIX.length());
						try {
							int testID = Integer.parseInt(testStr);
							if (testID > primID)
								primID = testID;
						} catch (NumberFormatException ex) {}
					} else if (name.contains(IDataSet3DCorePlot.GRAPHNODENAME) ||
							   name.contains(IDataSet3DCorePlot.BACKGROUNDNODENAME)) {
						currentPick = result;
					}
				}
				if (currentPick != null) {
					pickedPointTC = currentPick.getObjectCoordinates();
					if (pickedPointTC[0] < 0.0)
						pickedPointTC[0] = 0.0;
					if (pickedPointTC[0] > maxX)
						pickedPointTC[0] = maxX;

					if (pickedPointTC[1] < 0.0)
						pickedPointTC[1] = 0.0;
					if (pickedPointTC[1] > maxY)
						pickedPointTC[1] = maxY;
					
					pickedPointInPixel[0] = (int)((pickedPointTC[0]/maxX) * imageWidth);
					pickedPointInPixel[1] = (int)(((maxY-pickedPointTC[1])/maxY) * imageHeight);
				}
           	}
		}
		if (!initial) {
			ListIterator<ImagePositionListener> iter = listeners.listIterator();
			short flags = 0;
			
			if (tc.getAxisState(rightMouseButtonSlot).isPressed()) {
				rightMButton = true;
				flags += IImagePositionEvent.RIGHTMOUSEBUTTON;
			}
			if (tc.getAxisState(leftMouseButtonSlot).isPressed()) {
				leftMButton = true;
				flags += IImagePositionEvent.LEFTMOUSEBUTTON;
			}
			if (tc.getAxisState(ctrlKeySlot).isPressed()) {
				flags += IImagePositionEvent.CTRLKEY;
			}
			if (tc.getAxisState(shiftKeySlot).isPressed()) {
				flags += IImagePositionEvent.SHIFTKEY;
			}
			while (iter.hasNext()) {
				ImagePositionListener listener = iter.next();
				ImagePositionEvent event = new ImagePositionEvent(this,pickedPointTC,
																  pickedPointInPixel,
																  primID,
																  flags,
																  IDataPositionEvent.Mode.DRAG);
				listener.imageDragged(event);
			}
		}
 	}
	
	@Override
	public void deactivate(ToolContext tc) {
		ListIterator<ImagePositionListener> iter = listeners.listIterator();
		short flags = 0;
		
		if (tc.getAxisState(rightMouseButtonSlot).isReleased() && rightMButton) {
			flags += IImagePositionEvent.RIGHTMOUSEBUTTON;
		}
		if (tc.getAxisState(leftMouseButtonSlot).isReleased() && leftMButton) {
			flags += IImagePositionEvent.LEFTMOUSEBUTTON;
		}
		if (tc.getAxisState(ctrlKeySlot).isPressed()) {
			flags += IImagePositionEvent.CTRLKEY;
		}
		if (tc.getAxisState(shiftKeySlot).isPressed()) {
			flags += IImagePositionEvent.SHIFTKEY;
		}		
		while (iter.hasNext()) {
			ImagePositionListener listener = iter.next();
			ImagePositionEvent event = new ImagePositionEvent(this,pickedPointTC,
															  pickedPointInPixel,
															  primID,
															  flags,
															  IDataPositionEvent.Mode.END);
			listener.imageFinished(event);
		}
		rightMButton = false;
		leftMButton = false;
	}
	
	/**
	 * Add another ImagePositionListener to the listener list
	 * @param newListener
	 */
	public void addImagePositionListener(ImagePositionListener newListener)
	{
		listeners.add(newListener);
	}
	
	/**
	 * Remove an ImagePositionListener from the listener list
	 * @param newListener listener to be removed
	 */
	public void removeImagePositionListener(ImagePositionListener newListener)
	{
		listeners.remove(newListener);
	}
	
}
