/*-
 * Copyright 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.mapping.ui.actions;

import java.util.Collection;
import java.util.Iterator;

import org.dawnsci.mapping.ui.Activator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;

/**
 * Handler that finds the upper most image trace in the Map plot in the mapping perspective,
 * reads the min and max colourmap values from it, then applies this
 * same range to all the other image traces and locks them.
 *
 */
public class LockColourMapsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPlottingService service = Activator.getService(IPlottingService.class);
		
		IPlottingSystem<Object> plottingSystem = service.getPlottingSystem("Map");
		
		if (plottingSystem == null) return null;
		
		Collection<IPaletteTrace> traces = plottingSystem.getTracesByClass(IPaletteTrace.class);
		
		IPaletteTrace last = null;
		
		Iterator<IPaletteTrace> iterator = traces.iterator();
		
		while (iterator.hasNext()) last = iterator.next();
		
		if (last == null) return null;
		
		Number max = last.getMax();
		Number min = last.getMin();
		
		iterator = traces.iterator();
		
		while (iterator.hasNext()) {
			IPaletteTrace next = iterator.next();
			next.setMax(max);
			next.setMin(min);
			next.setRescaleHistogram(false);
			next.setPaletteData(next.getPaletteData());
		}
		
		return null;
	}

}
