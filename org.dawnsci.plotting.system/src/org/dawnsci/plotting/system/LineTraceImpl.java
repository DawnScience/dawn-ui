/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system;

import org.dawb.common.ui.macro.ColorMacroEvent;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.dawnsci.plotting.draw2d.swtxy.LineTrace;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.IErrorDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.macro.api.IMacroService;
import org.eclipse.dawnsci.macro.api.MacroEventObject;
import org.eclipse.dawnsci.macro.api.MacroUtils;
import org.eclipse.dawnsci.macro.api.MethodEventObject;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceContainer;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.ITraceListener;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This class only wraps line traces, images have their own IImageTrace implementor.
 * @see ILineTrace
 * @author Matthew Gerring
 *
 */
public class LineTraceImpl implements ILineTrace, ITraceListener{

	private static IMacroService mservice;
	public static void setMacroService(IMacroService s) {
		mservice = s;
	}
	public LineTraceImpl() { // Used for OSGi
		
	}

	private LineTrace          trace;
	private String             dataName;
	private AbstractPlottingSystem sys;
	private boolean errorBarExplicitySet;

	LineTraceImpl(AbstractPlottingSystem sys, final LineTrace trace) {
		this.sys   = sys;
		this.trace = trace;
		if (trace instanceof ITraceContainer) {
			((ITraceContainer)trace).setTrace(this);
		}
		IDataProvider prov = trace.getDataProvider();
		if (prov!=null && prov.hasErrors()) {
			trace.setErrorBarEnabled(getPreferenceStore().getBoolean(PlottingConstants.GLOBAL_SHOW_ERROR_BARS));
			trace.setErrorBarColor(ColorConstants.red);
		}
		
		trace.addListener(this);
	}
	
	private IPreferenceStore getPreferenceStore() {
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
	}

	
	public IDataProvider getDataProvider() {
		return trace.getDataProvider();
	}

	public ILineTrace.TraceType getTraceType() {
		
		org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType type = trace.getTraceType();
		switch(type) {
		case SOLID_LINE:
			return ILineTrace.TraceType.SOLID_LINE;
		case DASH_LINE:
			return ILineTrace.TraceType.DASH_LINE;
		case POINT:
			return ILineTrace.TraceType.POINT;
		case BAR:
			return ILineTrace.TraceType.HISTO;
		case AREA:
			return ILineTrace.TraceType.AREA;
		case STEP_VERTICALLY:
			return ILineTrace.TraceType.STEP_VERTICALLY;
		case STEP_HORIZONTALLY:
			return ILineTrace.TraceType.STEP_HORIZONTALLY;
		}
		return ILineTrace.TraceType.SOLID_LINE; 
	}
	public void setTraceType(ILineTrace.TraceType traceType) {
		if (trace==null) return;
		
		switch(traceType) {
		case SOLID_LINE:
			trace.setTraceType(org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType.SOLID_LINE);
			return;
		case DASH_LINE:
			trace.setTraceType(org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType.DASH_LINE);
			return;
		case POINT:
			trace.setTraceType(org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType.POINT);
			return;
		case HISTO:
			trace.setTraceType(org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType.BAR);
			return;
		case AREA:
			trace.setTraceType(org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType.AREA);
			return;
		case STEP_VERTICALLY:
			trace.setTraceType(org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType.STEP_VERTICALLY);
			return;
		case STEP_HORIZONTALLY:
			trace.setTraceType(org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType.STEP_HORIZONTALLY);
			return;
		}
	}

	public void setTraceColor(Color traceColor) {
		trace.setTraceColor(traceColor);
	}

	public Color getTraceColor() {
		return trace.getTraceColor();
	}


	public ILineTrace.PointStyle getPointStyle() {
		if (trace==null) return ILineTrace.PointStyle.NONE;
		
		org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle style = trace.getPointStyle();
		
		switch(style) {
		case NONE:
			return ILineTrace.PointStyle.NONE;
		case POINT:
			return ILineTrace.PointStyle.POINT;
		case CIRCLE:
			return ILineTrace.PointStyle.CIRCLE;
		case FILLED_CIRCLE:
			return ILineTrace.PointStyle.FILLED_CIRCLE;
		case TRIANGLE:
			return ILineTrace.PointStyle.TRIANGLE;
		case FILLED_TRIANGLE:
			return ILineTrace.PointStyle.FILLED_TRIANGLE;
		case SQUARE:
			return ILineTrace.PointStyle.SQUARE;
		case FILLED_SQUARE:
			return ILineTrace.PointStyle.FILLED_SQUARE;
		case DIAMOND:
			return ILineTrace.PointStyle.DIAMOND;
		case FILLED_DIAMOND:
			return ILineTrace.PointStyle.FILLED_DIAMOND;
		case XCROSS:
			return ILineTrace.PointStyle.XCROSS;
		case CROSS:
			return ILineTrace.PointStyle.CROSS;
		case BAR:
			return ILineTrace.PointStyle.BAR;
		}
		return ILineTrace.PointStyle.NONE; 
	}

	public void setPointStyle(ILineTrace.PointStyle pointStyle) {
		
		if (trace==null) return;
		
		switch(pointStyle) {
		case NONE:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.NONE);
			return;
		case POINT:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.POINT);
			return;
		case CIRCLE:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.CIRCLE);
			return;
		case FILLED_CIRCLE:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.FILLED_CIRCLE);
			return;
		case TRIANGLE:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.TRIANGLE);
			return;
		case FILLED_TRIANGLE:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.FILLED_TRIANGLE);
			return;
		case SQUARE:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.SQUARE);
			return;
		case FILLED_SQUARE:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.FILLED_SQUARE);
			return;
		case DIAMOND:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.DIAMOND);
			return;
		case FILLED_DIAMOND:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.FILLED_DIAMOND);
			return;
		case XCROSS:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.XCROSS);
			return;
		case CROSS:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.CROSS);
			return;
		case BAR:
			trace.setPointStyle(org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle.BAR);
			return;
		}
	}

	public void setLineWidth(int lineWidth) {
		trace.setLineWidth(lineWidth);
	}

	public void setPointSize(int pointSize) {
		trace.setPointSize(pointSize);
	}

	public void setAreaAlpha(int areaAlpha) {
		trace.setAreaAlpha(areaAlpha);
	}

	public void setAntiAliasing(boolean antiAliasing) {
		trace.setAntiAliasing(antiAliasing);
	}

	public void setName(String name) {		
		if (sys!=null) sys.moveTrace(getName(), name);
		trace.setInternalName(name);
		if (!name.equals(trace.getName())) {
			trace.setName(name, false);
		}
		trace.repaint();
	}

	public String getName() {
		return trace.getInternalName();
	}

	public int getPointSize() {
		return trace.getPointSize();
	}

	public int getAreaAlpha() {
		return trace.getAreaAlpha();
	}

	public void setErrorBarEnabled(boolean errorBarEnabled) {
		trace.setErrorBarEnabled(errorBarEnabled);
		errorBarExplicitySet = true;
	}
	
	public ILineTrace.ErrorBarType getYErrorBarType() {
		return getErrorType(trace.getYErrorBarType());
	}

	public ILineTrace.ErrorBarType getXErrorBarType() {
		return getErrorType(trace.getXErrorBarType());
	}


	public void setYErrorBarType(ILineTrace.ErrorBarType errorBarType) {
		trace.setYErrorBarType(getErrorType(errorBarType));
	}

	private ILineTrace.ErrorBarType getErrorType(org.eclipse.nebula.visualization.xygraph.figures.Trace.ErrorBarType ebt) {
		switch(ebt) {
		case NONE:
			return ILineTrace.ErrorBarType.NONE;
		case PLUS:
			return ILineTrace.ErrorBarType.PLUS;
		case MINUS:
			return ILineTrace.ErrorBarType.MINUS;
		case BOTH:
			return ILineTrace.ErrorBarType.BOTH;
		}
		return ILineTrace.ErrorBarType.NONE;
	}
	private org.eclipse.nebula.visualization.xygraph.figures.Trace.ErrorBarType getErrorType(ILineTrace.ErrorBarType ebt) {
		switch(ebt) {
		case NONE:
			return org.eclipse.nebula.visualization.xygraph.figures.Trace.ErrorBarType.NONE;
		case PLUS:
			return org.eclipse.nebula.visualization.xygraph.figures.Trace.ErrorBarType.PLUS;
		case MINUS:
			return org.eclipse.nebula.visualization.xygraph.figures.Trace.ErrorBarType.MINUS;
		case BOTH:
			return org.eclipse.nebula.visualization.xygraph.figures.Trace.ErrorBarType.BOTH;
		}
		return org.eclipse.nebula.visualization.xygraph.figures.Trace.ErrorBarType.NONE;
	}

	public void setXErrorBarType(ILineTrace.ErrorBarType errorBarType) {
		trace.setXErrorBarType(getErrorType(errorBarType));
	}

	public void setErrorBarCapWidth(int errorBarCapWidth) {
		trace.setErrorBarCapWidth(errorBarCapWidth);
	}

	public void setErrorBarColor(Color errorBarColor) {
		trace.setErrorBarColor(errorBarColor);
	}

	public int getLineWidth() {
		return trace.getLineWidth();
	}


	public int getErrorBarCapWidth() {
		return trace.getErrorBarCapWidth();
	}

	public Color getErrorBarColor() {
		return trace.getErrorBarColor();
	}

	@Override
	public boolean isAntiAliasing() {
		return trace.isAntiAliasing();
	}

	@Override
	public boolean isErrorBarEnabled() {
		return trace.isErrorBarEnabled();
	}
	
	
	@Override
	public IDataset getData() {
		return getYData();
	}

	@Override
	public IDataset getYData() {
		LightWeightDataProvider prov = (LightWeightDataProvider)trace.getDataProvider();
		if (prov==null) return null;
		return prov.getY();
	}
	
	@Override
	public IDataset getXData() {
		LightWeightDataProvider prov = (LightWeightDataProvider)trace.getDataProvider();
		if (prov==null) return null;
		return prov.getX();
	}

	public int getErrorBarWidth() {
		return trace.getErrorBarCapWidth();
	}
	/**
	 * The size of the error bar in pixels not real coordinates.
	 * @param errorBarCapWidth
	 */
	public void setErrorBarWidth(int errorBarCapWidth) {
		trace.setErrorBarCapWidth(errorBarCapWidth);
	}

	public Trace getTrace() {
		return trace;
	}

	private boolean settingLineData = false;
	/**
	 * You may need a repaint after calling this
	 */
	@Override
	public void setData(IDataset xData, IDataset yData) {
		
		if (settingLineData) return;
		try {
			settingLineData = true;
			
			if (xData==null && yData!=null) {
				xData = DatasetFactory.createRange(yData.getSize(), Dataset.INT);
			}
			
			LightWeightDataProvider prov = (LightWeightDataProvider)trace.getDataProvider();
			if (prov!=null) {
				prov.removeDataProviderListener(trace);
			} else {
				prov = new LightWeightDataProvider();
			}
			
			if (sys!=null) try {
				final TraceWillPlotEvent evt = new TraceWillPlotEvent(this, false);
				evt.setLineData(xData, yData);
				sys.fireWillPlot(evt);
				if (!evt.doit) return;
				if (evt.isNewLineDataSet()) {
					xData = evt.getXData();
					yData = evt.getYData();
				}
			} catch (Throwable ignored) {
				// We allow things to proceed without a warning.
			}
			
			prov.setData(xData,yData);
			trace.setDataProvider(prov);
			
			if (xData instanceof IErrorDataset) {
				if (((IErrorDataset)xData).hasErrors() && !errorBarExplicitySet) {
					trace.setErrorBarEnabled(getPreferenceStore().getBoolean(PlottingConstants.GLOBAL_SHOW_ERROR_BARS));
				}
			}
			if (yData instanceof IErrorDataset) {
				if (((IErrorDataset)yData).hasErrors() && !errorBarExplicitySet) {
					trace.setErrorBarEnabled(getPreferenceStore().getBoolean(PlottingConstants.GLOBAL_SHOW_ERROR_BARS));
				}
			}
			
			if (sys!=null) try {
				if (sys.getTraces().contains(this)) {
					sys.fireTraceUpdated(new TraceEvent(this));
				}
			} catch (Throwable ignored) {
				// We allow things to proceed without a warning.
			}
		} finally {
			settingLineData = false;
		}
	}

	@Override
	public boolean isVisible() {
		return trace.isVisible();
	}

	@Override
	public void setVisible(boolean isVisible) {
		trace.setVisible(isVisible);
	}

	@Override
	public void repaint() {
		trace.repaint();
	}
	
	private boolean userTrace = true;
	@Override
	public boolean isUserTrace() {
		return userTrace;
	}

	@Override
	public void setUserTrace(boolean isUserTrace) {
		this.userTrace = isUserTrace;
	}
	
	private Object userObject;

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	public String toString() {
		return trace.getName()+" (Internal name "+trace.getInternalName()+")";
	}
	
	@Override
	public boolean is3DTrace() {
		return false;
	}
	
	public void dispose() {
		
		if (trace!=null) {
			trace.removeListener(this);
			trace.dispose();
		}
		sys=null;
	}
	@Override
	public int getRank() {
		return 1;
	}

	@Override
	public IAxis getXAxis() {
		return (IAxis)trace.getXAxis();
	}

	@Override
	public IAxis getYAxis() {
		return (IAxis)trace.getYAxis();
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	@Override
	public void traceWidthChanged(Trace trace, int old, int newWidth) {
		if (mservice!=null) {
			String traceName = "trace_"+MacroUtils.getLegalName(getName());
			MethodEventObject evt = new MethodEventObject(traceName, "setLineWidth", this, newWidth);
			evt.prepend(traceName+" = ps.getTrace(\""+getName()+"\")");
		    mservice.publish(evt);
		}
	}

	@Override
	public void traceNameChanged(Trace trace, final String oldName, final String newName) {
		if (trace == this.trace && !newName.equals(oldName)) {
			
			String traceName = "trace_"+MacroUtils.getLegalName(newName);
			if (mservice!=null) {
				mservice.publish(new MacroEventObject(this, traceName+" = ps.getTrace(\""+oldName+"\")", true));
			}

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setName(newName);
				}
			});

			if (mservice!=null) {
			    mservice.publish(new MethodEventObject(traceName, "setName", this, newName));
			}
		}
	}

	@Override
	public void traceYAxisChanged(Trace trace, Axis oldName, Axis newName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void traceTypeChanged(
			Trace trace,
			org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType old,
			org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType newTraceType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void traceColorChanged(Trace trace, Color old, Color newColor) {
		if (trace == this.trace && old!=null && old.equals(newColor)) return;
		if (mservice!=null) {
			String traceName = "trace_"+MacroUtils.getLegalName(getName());
			ColorMacroEvent evt = new ColorMacroEvent(traceName, "setTraceColor", this, newColor);
			evt.prepend(traceName+" = ps.getTrace(\""+getName()+"\")");
			mservice.publish(evt);
		}
	}

}
