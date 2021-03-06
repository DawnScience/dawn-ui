/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy;

import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceContainer;
import org.eclipse.dawnsci.plotting.api.trace.LineTracePreferences;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;

/**
 * Trace with drawPolyline(...) for faster rendering.
 * 
 * @author Matthew Gerring
 *
 */
public class LineTrace extends Trace implements ITraceContainer {
	
	protected String internalName; 

	/**
	 * {@link #init(Axis, Axis, IDataProvider)} needs to be called if a trace is
	 * created with this constructor
	 */
	public LineTrace(String name) {
		super(name);
	}

	public LineTrace(String name, Axis xAxis, Axis yAxis, IDataProvider dataProvider, LineTracePreferences prefs) {
		super(name, xAxis, yAxis, dataProvider);
		if (dataProvider != null) {
			if (dataProvider.hasErrors()) {
				setErrorBarEnabled(prefs.getBoolean(LineTracePreferences.ERROR_BAR_ON));
				setErrorBarColor(ColorConstants.red);
			}
		}
	}

	public void dispose() {
		setInternalName(null);
		super.dispose();
	}

	public boolean isDisposed() {
		return getXYGraph()==null;
	}

	public String getInternalName() {
		if (internalName!=null) return internalName;
		return getName();
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	private ITrace trace;

	@Override
	public ITrace getTrace() {
		return trace;
	}

	@Override
	public void setTrace(ITrace trace) {
		this.trace = trace;
	}
}
