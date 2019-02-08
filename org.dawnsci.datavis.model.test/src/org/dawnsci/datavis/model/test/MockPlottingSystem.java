package org.dawnsci.datavis.model.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.plotting.api.IPlotActionSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.axis.IPositionListener;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.dawnsci.plotting.api.trace.IImageStackTrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineStackTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.IMulti2DTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPlane3DTrace;
import org.eclipse.dawnsci.plotting.api.trace.IScatter3DTrace;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.IVectorTrace;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeRenderTrace;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;

public class MockPlottingSystem implements IPlottingSystem<Object> {

	private static Set<ITrace> traces = new HashSet<ITrace>();
	
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IImageTrace createImageTrace(String traceName) {
		MockImageTrace mit = new MockImageTrace(traceName);
		traces.add(mit);
		return mit;
	}

	@Override
	public ILineTrace createLineTrace(String traceName) {
		MockLineTrace mlt = new MockLineTrace(traceName);
		traces.add(mlt);
		return mlt;
	}

	@Override
	public IVectorTrace createVectorTrace(String traceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISurfaceTrace createSurfaceTrace(String traceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IIsosurfaceTrace createIsosurfaceTrace(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVolumeRenderTrace createVolumeRenderTrace(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMulti2DTrace createMulti2DTrace(String traceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILineStackTrace createLineStackTrace(String traceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IScatter3DTrace createScatter3DTrace(String traceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IImageStackTrace createImageStackTrace(String traceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPlane3DTrace createPlane3DTrace(String traceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTrace(ITrace trace) {
		traces.add(trace);
		
	}

	@Override
	public void removeTrace(ITrace trace) {
		traces.remove(trace);
		
	}

	@Override
	public ITrace getTrace(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ITrace> getTraces() {
		return new ArrayList<>(traces);
	}

	@Override
	public void clearTraces() {
		traces.clear();
		
	}

	@Override
	public Collection<ITrace> getTraces(Class<? extends ITrace> clazz) {
		List<ITrace> l = new ArrayList<>();
		
		for (ITrace t : traces) {
			if (clazz.isInstance(t)) l.add(t);
		}
		
		return l;
	}

	@Override
	public <T extends ITrace> Collection<T> getTracesByClass(Class<T> clazz) {
		List<T> l = new ArrayList<>();
		
		for (ITrace t : traces) {
			if (clazz.isInstance(t)) l.add(clazz.cast(t));
		}
		
		return l;
	}
	
	@Override
	public void addTraceListener(ITraceListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTraceListener(ITraceListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renameTrace(ITrace trace, String name) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveTrace(String oldName, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isXFirst() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setXFirst(boolean xFirst) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fireWillPlot(TraceWillPlotEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fireTraceUpdated(TraceEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fireTraceAdded(TraceEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IRegion createRegion(String name, RegionType regionType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addRegion(IRegion region) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRegion(IRegion region) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IRegion getRegion(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IRegion> getRegions(RegionType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addRegionListener(IRegionListener l) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeRegionListener(IRegionListener l) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearRegions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearRegionTool() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<IRegion> getRegions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void renameRegion(IRegion region, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IAxis createAxis(String title, boolean isYAxis, int side) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAxis removeAxis(IAxis axis) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IAxis> getAxes() {
		return new ArrayList<IAxis>();
	}

	@Override
	public IAxis getAxis(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAxis getSelectedYAxis() {
		// TODO Auto-generated method stub
		return new MockAxis();
	}

	@Override
	public void setSelectedYAxis(IAxis yAxis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IAxis getSelectedXAxis() {
		// TODO Auto-generated method stub
		return new MockAxis();
	}

	@Override
	public void setSelectedXAxis(IAxis xAxis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void autoscaleAxes() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addPositionListener(IPositionListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePositionListener(IPositionListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addClickListener(IClickListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeClickListener(IClickListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IAnnotation createAnnotation(String name) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAnnotation(IAnnotation region) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAnnotation(IAnnotation region) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IAnnotation getAnnotation(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearAnnotations() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renameAnnotation(IAnnotation annotation, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printScaledPlotting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printPlotting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copyPlotting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String savePlotting(String filename) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void savePlotting(String filename, String filetype) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTitleColor(Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBackgroundColor(Color color) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getPlotName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x, List<? extends IDataset> ys, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x, List<? extends IDataset> ys, String title, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x, List<? extends IDataset> ys, List<String> dataNames, String title,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ITrace> updatePlot1D(IDataset x, List<? extends IDataset> ys, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ITrace> updatePlot1D(IDataset x, List<? extends IDataset> ys, List<String> dataNames,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ITrace> updatePlot1D(IDataset x, List<? extends IDataset> ys, String plotTitle,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITrace createPlot2D(IDataset image, List<? extends IDataset> axes, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITrace createPlot2D(IDataset image, List<? extends IDataset> axes, String dataName,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITrace updatePlot2D(IDataset image, List<? extends IDataset> axes, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITrace updatePlot2D(IDataset image, List<? extends IDataset> axes, String dataName,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPlotType(PlotType plotType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void append(String dataSetName, Number xValue, Number yValue, IProgressMonitor monitor) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset() {
		traces.clear();
		
	}

	@Override
	public void resetAxes() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		traces.clear();
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repaint() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repaint(boolean autoScale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlotType getPlotType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean is2D() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IActionBars getActionBars() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPlotActionSystem getPlotActionSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultCursor(int cursorType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setKeepAspect(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShowIntensity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setShowIntensity(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isShowValueLabels() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setShowValueLabels(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setShowLegend(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDisposed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setColorOption(ColorOption colorOption) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRescale() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRescale(boolean rescale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOrigin(ImageOrigin origin) {
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IWorkbenchPart getPart() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Control setControl(Control alternative, boolean showPlotToolbar) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void createPlotPart(Object parent, String plotName, IActionBars bars, PlotType hint, IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getPlotComposite() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends ITrace> U createTrace(String traceName, Class<U> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITrace createTrace(String traceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Class<? extends ITrace>> getRegisteredTraceClasses() {
		// TODO Auto-generated method stub
		return null;
	}

}
