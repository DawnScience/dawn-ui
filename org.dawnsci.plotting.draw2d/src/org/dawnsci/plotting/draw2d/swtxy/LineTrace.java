package org.dawnsci.plotting.draw2d.swtxy;

import org.csstudio.swt.xygraph.dataprovider.IDataProvider;
import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.Trace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceContainer;

/**
 * Trace with drawPolyline(...) for faster rendering.
 * 
 * @author fcp94556
 *
 */
public class LineTrace extends Trace implements ITraceContainer {
	
	protected String internalName; 
	
	public LineTrace(String name, Axis xAxis, Axis yAxis, IDataProvider dataProvider) {
		super(name, xAxis, yAxis, dataProvider);
	}
	

	public void dispose() {
		
		if (getParent()!=null) getParent().remove(this);
		removeAll();
		getHotSampleList().clear();
		name=null;
		internalName=null;
		traceDataProvider=null;
		xAxis=null;	
		yAxis=null;	
		traceColor=null;
		traceType=null;
		baseLine=null;
		pointStyle=null;
		yErrorBarType=null;
		xErrorBarType=null;
		errorBarColor=null;
		xyGraph=null;
		traceDataProvider=null;
	}

	public boolean isDisposed() {
		return xyGraph==null;
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
