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


import java.util.LinkedList;
import java.util.ListIterator;

import org.dawnsci.plotting.jreality.core.IDataSet3DCorePlot;

import de.jreality.math.Matrix;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

/**
 * An Area Select tool for jReality for 1D Plots
 */
public class AreaSelectTool extends AbstractTool {

	private static final InputSlot pointerSlot = InputSlot.getDevice("PointerTransformation");
	private Matrix pointerTrans = new Matrix();
	private boolean geometryMatched;
	private boolean initial = false;
	private double[] pickedPointOC;
	private int primID = -1;
	private LinkedList<AreaSelectListener> listeners;

	/**
	 * Constructor of an AreaSelectTool
	 */
	
	public AreaSelectTool() {
		super(InputSlot.getDevice("RotateActivation"));
		addCurrentSlot(pointerSlot);
		listeners = new LinkedList<AreaSelectListener>();
	}
	
	@Override
	public void activate(ToolContext tc) {
		initial = true;
		perform(tc);
		ListIterator<AreaSelectListener> iter = listeners.listIterator();
		while (iter.hasNext()) {
			AreaSelectListener listener = iter.next();
			AreaSelectEvent event = new AreaSelectEvent(this,pickedPointOC,
													    (char)0,primID);
			listener.areaSelectStart(event);
		}		
		initial = false;
	}

	/**
	 * Add another AreaSelectListener to the listener list
	 * @param newListener
	 */
	public void addAreaSelectListener(AreaSelectListener newListener)
	{
		listeners.add(newListener);
	}
	
	@Override
	public void perform(ToolContext tc) {  
		tc.getTransformationMatrix(pointerSlot).toDoubleArray(pointerTrans.getArray());
		
		geometryMatched=(!(tc.getCurrentPick() == null));
		if(geometryMatched) {
			String name = tc.getCurrentPick().getPickPath().getLastComponent().getName();
			primID = -1;
			if (name.contains(IDataSet3DCorePlot.OVERLAYPREFIX)) {
				String testStr = name.substring(IDataSet3DCorePlot.OVERLAYPREFIX.length());
				try {
					primID = Integer.parseInt(testStr);
				} catch (NumberFormatException ex) { primID = -1; }
			}
			pickedPointOC=tc.getCurrentPick().getObjectCoordinates();
		}
		if (!initial)
		{
			ListIterator<AreaSelectListener> iter = listeners.listIterator();
			while (iter.hasNext()) {
				AreaSelectListener listener = iter.next();
				AreaSelectEvent event = new AreaSelectEvent(this,pickedPointOC,(char)1,primID);
				listener.areaSelectDragged(event);
			}		
		}
 	}
	
	@Override
	public void deactivate(ToolContext tc) {
		ListIterator<AreaSelectListener> iter = listeners.listIterator();
		while (iter.hasNext()) {
			AreaSelectListener listener = iter.next();
			AreaSelectEvent event = new AreaSelectEvent(this,pickedPointOC,(char)2,primID);
			listener.areaSelectEnd(event);
		}
	}	
	
}
