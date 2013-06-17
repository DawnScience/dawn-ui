package org.dawnsci.plotting.system;

import org.csstudio.swt.xygraph.dataprovider.IDataProvider;
import org.csstudio.swt.xygraph.figures.Trace;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITraceContainer;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.dawnsci.plotting.draw2d.swtxy.LineTrace;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IErrorDataset;

/**
 * This class only wraps line traces, images have their own IImageTrace implementor.
 * @see ILineTrace
 * @author fcp94556
 *
 */
public class LineTraceImpl implements ILineTrace {

	private LineTrace          trace;
	private PlottingSystemImpl sys;
	private boolean errorBarExplicitySet;

	LineTraceImpl(PlottingSystemImpl sys, final LineTrace trace) {
		this.sys   = sys;
		this.trace = trace;
		if (trace instanceof ITraceContainer) {
			((ITraceContainer)trace).setTrace(this);
		}
		if (trace.getDataProvider() instanceof LightWeightDataProvider) {
			LightWeightDataProvider prov = (LightWeightDataProvider)trace.getDataProvider();
			if (prov.hasErrors()) {
				trace.setErrorBarEnabled(true);
			}
		}
	}
	
	public IDataProvider getDataProvider() {
		return trace.getDataProvider();
	}

	public ILineTrace.TraceType getTraceType() {
		
		org.csstudio.swt.xygraph.figures.Trace.TraceType type = trace.getTraceType();
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
			trace.setTraceType(org.csstudio.swt.xygraph.figures.Trace.TraceType.SOLID_LINE);
			return;
		case DASH_LINE:
			trace.setTraceType(org.csstudio.swt.xygraph.figures.Trace.TraceType.DASH_LINE);
			return;
		case POINT:
			trace.setTraceType(org.csstudio.swt.xygraph.figures.Trace.TraceType.POINT);
			return;
		case HISTO:
			trace.setTraceType(org.csstudio.swt.xygraph.figures.Trace.TraceType.BAR);
			return;
		case AREA:
			trace.setTraceType(org.csstudio.swt.xygraph.figures.Trace.TraceType.AREA);
			return;
		case STEP_VERTICALLY:
			trace.setTraceType(org.csstudio.swt.xygraph.figures.Trace.TraceType.STEP_VERTICALLY);
			return;
		case STEP_HORIZONTALLY:
			trace.setTraceType(org.csstudio.swt.xygraph.figures.Trace.TraceType.STEP_HORIZONTALLY);
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
		
		org.csstudio.swt.xygraph.figures.Trace.PointStyle style = trace.getPointStyle();
		
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
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.NONE);
			return;
		case POINT:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.POINT);
			return;
		case CIRCLE:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.CIRCLE);
			return;
		case FILLED_CIRCLE:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.FILLED_CIRCLE);
			return;
		case TRIANGLE:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.TRIANGLE);
			return;
		case FILLED_TRIANGLE:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.FILLED_TRIANGLE);
			return;
		case SQUARE:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.SQUARE);
			return;
		case FILLED_SQUARE:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.FILLED_SQUARE);
			return;
		case DIAMOND:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.DIAMOND);
			return;
		case FILLED_DIAMOND:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.FILLED_DIAMOND);
			return;
		case XCROSS:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.XCROSS);
			return;
		case CROSS:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.CROSS);
			return;
		case BAR:
			trace.setPointStyle(org.csstudio.swt.xygraph.figures.Trace.PointStyle.BAR);
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
		trace.setInternalName(name);
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

	private ILineTrace.ErrorBarType getErrorType(org.csstudio.swt.xygraph.figures.Trace.ErrorBarType ebt) {
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
	private org.csstudio.swt.xygraph.figures.Trace.ErrorBarType getErrorType(ILineTrace.ErrorBarType ebt) {
		switch(ebt) {
		case NONE:
			return org.csstudio.swt.xygraph.figures.Trace.ErrorBarType.NONE;
		case PLUS:
			return org.csstudio.swt.xygraph.figures.Trace.ErrorBarType.PLUS;
		case MINUS:
			return org.csstudio.swt.xygraph.figures.Trace.ErrorBarType.MINUS;
		case BOTH:
			return org.csstudio.swt.xygraph.figures.Trace.ErrorBarType.BOTH;
		}
		return org.csstudio.swt.xygraph.figures.Trace.ErrorBarType.NONE;
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
	public AbstractDataset getData() {
		return getYData();
	}

	@Override
	public AbstractDataset getYData() {
		return sys.getData(getName(), trace, true);
	}
	
	@Override
	public AbstractDataset getXData() {
		return sys.getData(getName(), trace, false);
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

	/**
	 * You may need a repaint after calling this
	 */
	@Override
	public void setData(IDataset xData, IDataset yData) {
		
		LightWeightDataProvider prov = (LightWeightDataProvider)trace.getDataProvider();
		if (prov!=null) {
			prov.removeDataProviderListener(trace);
		} else {
			prov = new LightWeightDataProvider();
		}
		
		if (sys!=null) try {
			if (sys.getTraces().contains(this)) {
				final TraceWillPlotEvent evt = new TraceWillPlotEvent(this, false);
				sys.fireWillPlot(evt);
				if (!evt.doit) return;
				if (evt.isNewLineDataSet()) {
					xData = evt.getXData();
					yData = evt.getYData();
				}
			}
		} catch (Throwable ignored) {
			// We allow things to proceed without a warning.
		}
		
		prov.setData(xData,yData);
		trace.setDataProvider(prov);
		
		if (xData instanceof IErrorDataset) {
			if (((IErrorDataset)xData).hasErrors() && !errorBarExplicitySet) {
				trace.setErrorBarEnabled(true);
			}
		}
		if (yData instanceof IErrorDataset) {
			if (((IErrorDataset)yData).hasErrors() && !errorBarExplicitySet) {
				trace.setErrorBarEnabled(true);
			}
		}
		
		if (sys!=null) try {
			if (sys.getTraces().contains(this)) {
				sys.fireTraceUpdated(new TraceEvent(this));
			}
		} catch (Throwable ignored) {
			// We allow things to proceed without a warning.
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
		if (trace!=null) trace.dispose();
		sys=null;
	}

}
