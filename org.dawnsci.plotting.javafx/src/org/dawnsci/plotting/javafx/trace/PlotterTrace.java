/*
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.javafx.trace;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;

class PlotterTrace {
	
	
	protected String                name;
	protected String                dataName;
	protected List<IDataset> 		axes;
	protected List<String>          axesNames;
	protected IPlottingSystemViewer<?> plotter;
	protected boolean               active;
	protected IPlottingSystem<?> 		plottingSystem;
	protected IROI                  window;

	public void dispose() {
		axes = null;
		if (axesNames!=null) axesNames.clear();
		axesNames = null;
		plotter = null;
		plottingSystem=null;
		window=null;
	}
	
	public PlotterTrace(IPlottingSystemViewer<?> plotter, String name) {
		this.plotter = plotter;
		this.name    = name;
	}

	public String getName() {
		return name;
	}

	public List<IDataset> getAxes() {
		return axes;
	}

	public boolean isActive() {
		return active;
	}

	protected void setActive(boolean active) {
		this.active = active;
		if (active) {
			if (plottingSystem!=null) plottingSystem.fireTraceAdded(new TraceEvent(this));
		}
	}

	protected String getLabel(int i) {
		String label = axesNames!=null ? axesNames.get(i) : null;
		if  (label==null) label = (axes!=null && axes.get(i)!=null) ? axes.get(i).getName() : null;
		return label;
	}

	public List<String> getAxesNames() {
		return axesNames;
	}

	public void setAxesNames(List<String> axesNames) {
		this.axesNames = axesNames;
	}

	private Object userObject;

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	/**
	 * True if visible
	 * @return
	 */
	public boolean isVisible() {
		return isActive();
	}

	/**
	 * True if visible
	 * @return
	 */
	public void setVisible(boolean isVisible) {
		// TODO FIXME What to do to make plots visible/invisible?
	}

	private boolean isUserTrace=true;

	public boolean isUserTrace() {
		return isUserTrace;
	}

	public void setUserTrace(boolean isUserTrace) {
		this.isUserTrace = isUserTrace;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IPlottingSystem<?> getPlottingSystem() {
		return plottingSystem;
	}

	public void setPlottingSystem(IPlottingSystem<?> plottingSystem) {
		this.plottingSystem = plottingSystem;
	}

	public IROI getWindow() {
		return window;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

}
