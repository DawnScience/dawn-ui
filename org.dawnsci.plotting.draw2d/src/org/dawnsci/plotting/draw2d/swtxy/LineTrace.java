/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceContainer;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Trace with drawPolyline(...) for faster rendering.
 * 
 * @author Matthew Gerring
 *
 */
public class LineTrace extends Trace implements ITraceContainer {
	
	protected String internalName; 
	
	public LineTrace(String name) {
		super(name);
	}
	
	public void init(Axis xAxis, Axis yAxis, IDataProvider dataProvider) {
		super.init(xAxis, yAxis, dataProvider);
		
		if (dataProvider != null) {
			if (dataProvider.hasErrors()) {
				setErrorBarEnabled(getPreferenceStore().getBoolean(PlottingConstants.GLOBAL_SHOW_ERROR_BARS));
				setErrorBarColor(ColorConstants.red);
			}
		}
	}
	
	private IPreferenceStore getPreferenceStore() {
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
	}

	public void dispose() {
		
		if (getParent()!=null) getParent().remove(this);
		removeAll();
		getHotSampleList().clear();
		setName(null);
		setInternalName(null);
		setDataProvider(null);
		setXAxis(null);	
		setYAxis(null);	
		setTraceColor(null);
		setTraceType(null);
		setBaseLine(null);
		setPointStyle(null);
		setYErrorBarType(null);
		setXErrorBarType(null);
		setErrorBarColor(null);
		setXYGraph((IXYGraph)null);
		setDataProvider(null);
	}

	public boolean isDisposed() {
		return getIXYGraph()==null;
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
