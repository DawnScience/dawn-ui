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

import org.dawnsci.plotting.jreality.impl.DataSet3DPlot1D;

import de.jreality.math.Matrix;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

/**
 * An PlotAction tool for jReality for 1D Plots. This can be used to call
 * specific action to be taken when a specific part of the graph has been
 * selected with a single right mouse point click
 */

public class PlotActionTool extends AbstractTool {

	protected static final InputSlot pointerSlot = InputSlot.getDevice("PointerTransformation");
	protected Matrix pointerTrans = new Matrix();
	protected boolean geometryMatched;
	protected double[] pickedPointOC;
	protected LinkedList<PlotActionEventListener> listeners;
	
	/**
	 * PlotActionTool default constructor
	 */
	public PlotActionTool() {
		addCurrentSlot(pointerSlot);
		listeners = new LinkedList<PlotActionEventListener>();
		
	}
	
	/**
	 * @param slot
	 */
	public PlotActionTool(InputSlot slot) {
		super(slot);
		addCurrentSlot(pointerSlot);
		listeners = new LinkedList<PlotActionEventListener>();
	}
	
    @Override
	public void activate(ToolContext tc){
    	perform(tc);
    }  	

    private void notifyListeners(PlotActionEvent evt)
    {
			ListIterator<PlotActionEventListener> iter = listeners.listIterator();
			while (iter.hasNext()) {
				PlotActionEventListener listener = iter.next();
				listener.plotActionPerformed(evt);
			}    	
    }
    
    @Override
	public void perform(ToolContext tc){  
		tc.getTransformationMatrix(pointerSlot).toDoubleArray(pointerTrans.getArray());		
		geometryMatched=(!(tc.getCurrentPick() == null));
    	if(geometryMatched){
   			pickedPointOC=tc.getCurrentPick().getObjectCoordinates();
   		 
    		if((tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT) ||
           	   (tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_LINE)) 
           	{
        		String testStr = tc.getCurrentPick().getPickPath().getLastComponent().getName();
        		if (testStr.indexOf(DataSet3DPlot1D.GRAPHNAMEPREFIX) != -1)
        		{
    				PlotActionEvent event = new PlotActionEvent(this,pickedPointOC,1);
    				notifyListeners(event);
        		}
           	} else {
           		PlotActionEvent event = new PlotActionEvent(this, pickedPointOC,-1);
           		notifyListeners(event);
           	}
    	}
    }    
   
	/**
	 * Add another PlotActionEventListener to the listener list
	 * @param newListener
	 */
	public void addPlotActionEventListener(PlotActionEventListener newListener)
	{
		listeners.add(newListener);
	}
	
	@Override
	public void deactivate(ToolContext tc) {
	}   
}
