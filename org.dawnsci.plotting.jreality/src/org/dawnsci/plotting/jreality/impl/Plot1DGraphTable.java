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

package org.dawnsci.plotting.jreality.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Plot1DGraphColourTable 
 */

public class Plot1DGraphTable {

	
	private List<Plot1DAppearance> legendEntries = 
		Collections.synchronizedList(new LinkedList<Plot1DAppearance>());
	
	/**
	 * Get the number of legend entries
	 * @return the number of legend entries
	 */
	
	public synchronized int getLegendSize()
	{
		return legendEntries.size();
	}
	
	/**
	 * Get the entry / description of a specific position in the legend map
	 * @param nr entry number
	 * @return Description of the entry at the asked position
	 */
	
	public synchronized Plot1DAppearance getLegendEntry(int nr)
	{
		assert nr < legendEntries.size();
		return legendEntries.get(nr);
	}
	
	/**
	 * Push an entry on the top of the list of the legend
	 * @param newEntry name of the new entry
	 */
	public synchronized void pushEntryOnLegend(Plot1DAppearance newEntry)
	{
		legendEntries.add(0,newEntry);
	}
	
	/**
	 * Add an entry on the back of the list of the legend
	 * @param newEntry name of the new entry
	 * @return the index added.
	 */
	
	public synchronized int addEntryOnLegend(Plot1DAppearance newEntry)
	{
		legendEntries.add(newEntry);
		return legendEntries.size()-1;
	}

	/**
	 * Add entry at given index
	 * @param i index
	 * @param newEntry
	 */
	public synchronized void addEntryOnLegend(int i, Plot1DAppearance newEntry)
	{
		legendEntries.add(i, newEntry);
	}

	/**
	 * Delete an entry in the legend map at a specific position
	 * @param nr position in the legend map
	 */
	public synchronized void deleteLegendEntry(int nr)
	{
		assert nr < legendEntries.size() -1 && nr >= 0;
		legendEntries.remove(nr);
	}
	
	public synchronized void deleteLegendEntry(Plot1DAppearance entry)
	{
		legendEntries.remove(entry);
	}

	/**
	 * Clear the whole legend map
	 */
	
	public synchronized void clearLegend()
	{
		legendEntries.clear();
	}

	public int size() {
		return legendEntries.size();
	}
		
}
