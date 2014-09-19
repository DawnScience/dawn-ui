/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.tool;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.dawnsci.plotting.jreality.core.IDataSet3DCorePlot;

import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.ToolContext;

/**
 *
 */
public class PlotActionTool2D extends PlotActionTool {
	   
		@Override
		public void perform(ToolContext tc){  
			tc.getTransformationMatrix(pointerSlot).toDoubleArray(pointerTrans.getArray());		
			geometryMatched=(!(tc.getCurrentPick() == null));
	    	if(geometryMatched) {
	    		if((tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_FACE ||
	    			tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_LINE ||
	    			tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT)) 
	           	{
					List<PickResult> results = tc.getCurrentPicks();
					Iterator<PickResult> iter = results.iterator();
					while (iter.hasNext()) {
						PickResult result = iter.next();
						String name = result.getPickPath().getLastComponent().getName();
		        		if (name.contains(IDataSet3DCorePlot.GRAPHNODENAME)) {
		        			pickedPointOC=result.getObjectCoordinates();
		        			ListIterator<PlotActionEventListener> actionIter = listeners.listIterator();
		        			while (actionIter.hasNext()) {
		        				PlotActionEventListener listener = actionIter.next();
		        				PlotActionEvent event = new PlotActionEvent(this,pickedPointOC);
		        				listener.plotActionPerformed(event);
		        			}
		        			break;
		        		}
	        		}
	           	}
	    	}
	    }    
}
