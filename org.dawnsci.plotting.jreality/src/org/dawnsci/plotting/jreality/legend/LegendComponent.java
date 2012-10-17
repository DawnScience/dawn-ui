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

package org.dawnsci.plotting.jreality.legend;

import java.util.LinkedList;

import org.dawnsci.plotting.jreality.impl.Plot1DGraphTable;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract LegendComponent that can be added to the DataSetPlotter as acting
 * legend
 */
public abstract class LegendComponent extends Composite {

	protected LinkedList<LegendChangeEventListener> listeners;
	
	/**
	 * @param parent
	 * @param style
	 */

	public LegendComponent(Composite parent, int style)
	{
		super(parent,style);
		listeners = new LinkedList<LegendChangeEventListener>();
	}
	
	/**
	 * Add a LegendChangeEventListener
	 * @param listener LegendChangeEventListener that should be added
	 */
	
	public void addLegendChangeEventListener(LegendChangeEventListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove a LegendChangeEventListener
	 * @param listener LegendChangeEventListener that should be removed
	 */
	
	public void removeLegendChangeEventListener(LegendChangeEventListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Remove all LegendChangeEventListeners
	 */
	
	public void removeAllLegendChangeEventListener()
	{
		listeners.clear();
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
		if (listeners != null)
			listeners.clear();
	}
	
	/**
	 * Update the legend table
	 * @param table List containing all the Plot appearance information
	 */
	public abstract void updateTable(Plot1DGraphTable table);

}
