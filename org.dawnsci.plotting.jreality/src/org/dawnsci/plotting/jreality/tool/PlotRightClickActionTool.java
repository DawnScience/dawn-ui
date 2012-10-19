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

import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

/**
 *
 */
public class PlotRightClickActionTool extends PlotActionTool {
	private static final InputSlot click = InputSlot.getDevice("PrimarySelection");

	/**
	 * 
	 */

	public PlotRightClickActionTool() {
		super(click);
		listeners = new LinkedList<PlotActionEventListener>();
	}

	@Override
	public void perform(ToolContext tc) {
		tc.getTransformationMatrix(pointerSlot).toDoubleArray(pointerTrans.getArray());
		geometryMatched = (!(tc.getCurrentPick() == null));
		boolean foundEvent = false;
		if (geometryMatched) {
			if ((tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT)
					|| (tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_LINE)) {
				String testStr = tc.getCurrentPick().getPickPath().getLastComponent().getName();
				int strIndex = testStr.indexOf(DataSet3DPlot1D.GRAPHNAMEPREFIX);
				if (strIndex != -1) {
					testStr = testStr.substring(strIndex + DataSet3DPlot1D.GRAPHNAMEPREFIX.length());
					int graphNr = -1;
					try {
						graphNr = Integer.parseInt(testStr);
					} catch (NumberFormatException ex) {
					}
					pickedPointOC = tc.getCurrentPick().getObjectCoordinates();
					foundEvent = true;
					ListIterator<PlotActionEventListener> iter = listeners.listIterator();
					while (iter.hasNext()) {
						PlotActionEventListener listener = iter.next();
						PlotActionEvent event = new PlotActionEvent(this, pickedPointOC, graphNr);
						listener.plotActionPerformed(event);
					}
				}
			}
		}

		if (!foundEvent && tc.getSource() == click) {
			ListIterator<PlotActionEventListener> iter = listeners.listIterator();
			while (iter.hasNext()) {
				PlotActionEventListener listener = iter.next();
				PlotActionEvent event = new PlotActionEvent(this, new double[] { 0, 0 }, -1);
				listener.plotActionPerformed(event);
			}
		}
	}

}
