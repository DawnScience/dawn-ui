package org.dawnsci.datavis.model.test;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.dawnsci.plotting.api.trace.LineTracePreferences;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPart;

public class MockPlottingSystem implements IPlottingSystem<Object> {
	private static Set<ITrace> traces = new HashSet<ITrace>();

	@Override
	public <T> T getAdapter(Class<T> adapter) {
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
		return null;
	}

	@Override
	public ISurfaceTrace createSurfaceTrace(String traceName) {
		return null;
	}

	@Override
	public IIsosurfaceTrace createIsosurfaceTrace(String string) {
		return null;
	}

	@Override
	public IVolumeRenderTrace createVolumeRenderTrace(String string) {
		return null;
	}

	@Override
	public IMulti2DTrace createMulti2DTrace(String traceName) {
		return null;
	}

	@Override
	public ILineStackTrace createLineStackTrace(String traceName) {
		return null;
	}

	@Override
	public IScatter3DTrace createScatter3DTrace(String traceName) {
		return null;
	}

	@Override
	public IImageStackTrace createImageStackTrace(String traceName) {
		return null;
	}

	@Override
	public IPlane3DTrace createPlane3DTrace(String traceName) {
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
			if (clazz.isInstance(t))
				l.add(t);
		}
		return l;
	}

	@Override
	public <T extends ITrace> Collection<T> getTracesByClass(Class<T> clazz) {
		List<T> l = new ArrayList<>();
		for (ITrace t : traces) {
			if (clazz.isInstance(t))
				l.add(clazz.cast(t));
		}
		return l;
	}

	@Override
	public void addTraceListener(ITraceListener l) {
	}

	@Override
	public void removeTraceListener(ITraceListener l) {
	}

	@Override
	public void renameTrace(ITrace trace, String name) throws Exception {
	}

	@Override
	public void moveTrace(String oldName, String name) {
	}

	@Override
	public boolean isXFirst() {
		return false;
	}

	@Override
	public void setXFirst(boolean xFirst) {
	}

	@Override
	public void fireWillPlot(TraceWillPlotEvent evt) {
	}

	@Override
	public void fireTraceUpdated(TraceEvent evt) {
	}

	@Override
	public void fireTraceAdded(TraceEvent evt) {
	}

	@Override
	public IRegion createRegion(String name, RegionType regionType) throws Exception {
		return null;
	}

	@Override
	public void addRegion(IRegion region) {
	}

	@Override
	public void removeRegion(IRegion region) {
	}

	@Override
	public IRegion getRegion(String name) {
		return null;
	}

	@Override
	public Collection<IRegion> getRegions(RegionType type) {
		return null;
	}

	@Override
	public boolean addRegionListener(IRegionListener l) {
		return false;
	}

	@Override
	public boolean removeRegionListener(IRegionListener l) {
		return false;
	}

	@Override
	public void clearRegions() {
	}

	@Override
	public void clearRegionTool() {
	}

	@Override
	public Collection<IRegion> getRegions() {
		return null;
	}

	@Override
	public void renameRegion(IRegion region, String name) {
	}

	@Override
	public IAxis createAxis(String title, boolean isYAxis, int side) {
		return null;
	}

	@Override
	public IAxis removeAxis(IAxis axis) {
		return null;
	}

	@Override
	public List<IAxis> getAxes() {
		
		MockAxis x = new MockAxis();
		x.setTitle("x");
		
		MockAxis y = new MockAxis();
		x.setTitle("y");
		
		return Arrays.asList(x,y);
	}

	@Override
	public IAxis getAxis(String name) {
		return null;
	}

	@Override
	public IAxis getSelectedYAxis() {
		return new MockAxis();
	}

	@Override
	public void setSelectedYAxis(IAxis yAxis) {
	}

	@Override
	public IAxis getSelectedXAxis() {
		return new MockAxis();
	}

	@Override
	public void setSelectedXAxis(IAxis xAxis) {
	}

	@Override
	public void autoscaleAxes() {
	}

	@Override
	public void addPositionListener(IPositionListener l) {
	}

	@Override
	public void removePositionListener(IPositionListener l) {
	}

	@Override
	public void addClickListener(IClickListener l) {
	}

	@Override
	public void removeClickListener(IClickListener l) {
	}

	@Override
	public IAnnotation createAnnotation(String name) throws Exception {
		return null;
	}

	@Override
	public void addAnnotation(IAnnotation region) {
	}

	@Override
	public void removeAnnotation(IAnnotation region) {
	}

	@Override
	public IAnnotation getAnnotation(String name) {
		return null;
	}

	@Override
	public void clearAnnotations() {
	}

	@Override
	public void renameAnnotation(IAnnotation annotation, String name) {
	}

	@Override
	public void printScaledPlotting() {
	}

	@Override
	public void printPlotting() {
	}

	@Override
	public void copyPlotting() {
	}

	@Override
	public String savePlotting(String filename) throws Exception {
		return null;
	}

	@Override
	public void savePlotting(String filename, String filetype) throws Exception {
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public void setTitle(String title) {
	}

	@Override
	public void setTitleColor(Color color) {
	}

	@Override
	public void setBackgroundColor(Color color) {
	}

	@Override
	public String getPlotName() {
		return null;
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x, List<? extends IDataset> ys, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x, List<? extends IDataset> ys, String title, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x, List<? extends IDataset> ys, List<String> dataNames, String title,
			IProgressMonitor monitor) {
		return null;
	}

	@Override
	public List<ITrace> updatePlot1D(IDataset x, List<? extends IDataset> ys, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public List<ITrace> updatePlot1D(IDataset x, List<? extends IDataset> ys, List<String> dataNames,
			IProgressMonitor monitor) {
		return null;
	}

	@Override
	public List<ITrace> updatePlot1D(IDataset x, List<? extends IDataset> ys, String plotTitle,
			IProgressMonitor monitor) {
		return null;
	}

	@Override
	public ITrace createPlot2D(IDataset image, List<? extends IDataset> axes, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public ITrace createPlot2D(IDataset image, List<? extends IDataset> axes, String dataName,
			IProgressMonitor monitor) {
		return null;
	}

	@Override
	public ITrace updatePlot2D(IDataset image, List<? extends IDataset> axes, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public ITrace updatePlot2D(IDataset image, List<? extends IDataset> axes, String dataName,
			IProgressMonitor monitor) {
		return null;
	}

	@Override
	public void setPlotType(PlotType plotType) {
	}

	@Override
	public void append(String dataSetName, Number xValue, Number yValue, IProgressMonitor monitor) throws Exception {
	}

	@Override
	public void reset() {
		traces.clear();
	}

	@Override
	public void resetAxes() {
	}

	@Override
	public void clear() {
		traces.clear();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void repaint() {
	}

	@Override
	public void repaint(boolean autoScale) {
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	@Override
	public PlotType getPlotType() {
		return null;
	}

	@Override
	public boolean is2D() {
		return false;
	}

	@Override
	public IActionBars getActionBars() {
		return null;
	}

	@Override
	public IPlotActionSystem getPlotActionSystem() {
		return null;
	}

	@Override
	public void setDefaultCursor(int cursorType) {
	}

	@Override
	public void setKeepAspect(boolean b) {
	}

	@Override
	public void setEnabled(boolean enabled) {
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean isShowIntensity() {
		return false;
	}

	@Override
	public void setShowIntensity(boolean b) {
	}

	@Override
	public boolean isShowValueLabels() {
		return false;
	}

	@Override
	public void setShowValueLabels(boolean b) {
	}

	@Override
	public void setShowLegend(boolean b) {
	}

	@Override
	public boolean isDisposed() {
		return false;
	}

	@Override
	public void setColorOption(ColorOption colorOption) {
	}

	@Override
	public boolean isRescale() {
		return false;
	}

	@Override
	public void setRescale(boolean rescale) {
	}

	@Override
	public void setFocus() {
	}

	@Override
	public IWorkbenchPart getPart() {
		return null;
	}

	@Override
	public Control setControl(Control alternative, boolean showPlotToolbar) {
		return null;
	}

	@Override
	public void createPlotPart(Object parent, String plotName, IActionBars bars, PlotType hint, IWorkbenchPart part) {
	}

	@Override
	public Object getPlotComposite() {
		return null;
	}

	@Override
	public <U extends ITrace> U createTrace(String traceName, Class<U> clazz) {
		return null;
	}

	@Override
	public ITrace createTrace(String traceName) {
		return null;
	}

	@Override
	public List<Class<? extends ITrace>> getRegisteredTraceClasses() {
		return null;
	}

	@Override
	public LineTracePreferences getLineTracePreferences() {
		return null;
	}

	@Override
	public void restorePreferences(IMemento memento) {
	}

	@Override
	public void savePreferences(IMemento memento) {
	}
}