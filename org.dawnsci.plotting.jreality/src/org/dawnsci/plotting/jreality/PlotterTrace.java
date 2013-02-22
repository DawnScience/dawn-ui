package org.dawnsci.plotting.jreality;

import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.trace.TraceEvent;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

class PlotterTrace {
	
	protected String                 name;
	protected List<AbstractDataset>  axes;
	protected List<String>           axesNames;
	protected JRealityPlotViewer     plotter;
	protected boolean                active;
	protected AbstractPlottingSystem plottingSystem;
	protected ROIBase                window;


	public PlotterTrace(JRealityPlotViewer plotter2, String name2) {
		this.plotter = plotter2;
		this.name    = name2;
	}
	public String getName() {
		return name;
	}

	public List<AbstractDataset> getAxes() {
		return axes;
	}

	public boolean isActive() {
		return active;
	}

	protected final void setActive(boolean active) {
		this.active = active;
		if (active) {
			if (plottingSystem!=null) plottingSystem.fireTraceAdded(new TraceEvent(this));
		}
	}
	protected List<AxisValues> createAxisValues() {
		
		final AxisValues xAxis = new AxisValues(getLabel(0), axes!=null?axes.get(0):null);
		final AxisValues yAxis = new AxisValues(getLabel(1), axes!=null?axes.get(1):null);
		final AxisValues zAxis = new AxisValues(getLabel(2), axes!=null?axes.get(2):null);
		return Arrays.asList(xAxis, yAxis, zAxis);
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

	public AbstractPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

	public void setPlottingSystem(AbstractPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
	}

    public ROIBase getWindow() {
		return window;
	}

}
