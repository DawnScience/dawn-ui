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
