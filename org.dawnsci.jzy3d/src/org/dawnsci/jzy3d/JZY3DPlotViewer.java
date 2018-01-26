package org.dawnsci.jzy3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceMeshTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.IWaterfallTrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.Settings;
import org.jzy3d.chart.swt.SWTChartComponentFactory;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.axes.layout.IAxeLayout;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.legends.colorbars.AWTColorbarLegend;
import org.jzy3d.plot3d.rendering.ordering.DefaultOrderingStrategy;
import org.jzy3d.plot3d.rendering.scene.Graph;
import org.jzy3d.plot3d.rendering.view.modes.CameraMode;

public class JZY3DPlotViewer extends IPlottingSystemViewer.Stub<Composite> {
	
	private Chart chart;
	private Composite control;
	
	public JZY3DPlotViewer() {
	}
	
	public void createControl(final Composite parent) {
		control = new Composite(parent, SWT.NONE);
		control.setLayout(new FillLayout());
		Settings.getInstance().setHardwareAccelerated(true);
		chart = SWTChartComponentFactory.chart(control, Quality.Intermediate);
		chart.getView().setCameraMode(CameraMode.ORTHOGONAL);
//		chart.getScene().getGraph().setSort(false);
//		chart.getScene().getGraph().setStrategy(new DefaultOrderingStrategy());
//		chart.getView().setSquared(false);
		ChartLauncher.openChart(chart);
	}
	
	@Override
	public boolean addTrace(ITrace trace){
		if (trace instanceof SurfaceMeshTraceImpl) {
			chart.pauseAnimator();
//			chart.clear();
//			ChartLauncher.openChart(chart);
//			chart.getScene().remove(((SurfaceMeshTraceImpl)trace).getShape());
			((SurfaceMeshTraceImpl) trace).setPalette(getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME));
//			IAxeLayout layout = chart.getView().getAxe().getLayout();
//			AbstractDrawable shape = ((SurfaceMeshTraceImpl)trace).getShape();
//			AWTColorbarLegend legend = new AWTColorbarLegend(shape, layout);
//			shape.setLegend(legend);
			
//			List<IDataset> axes = ((SurfaceMeshTraceImpl)trace).getAxes();
			
			chart.getScene().add(((SurfaceMeshTraceImpl)trace).getShape());
			
			
			
			
			chart.resumeAnimator();
			return true;
		}
		if (trace instanceof WaterfallTraceImpl) {
			chart.pauseAnimator();
//			chart.clear();
//			ChartLauncher.openChart(chart);
//			chart.getScene().remove(((SurfaceMeshTraceImpl)trace).getShape());
//			((WaterfallTraceImpl) trace).setPalette(getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME));
			chart.resumeAnimator();
			chart.getScene().add(((WaterfallTraceImpl)trace).getShape());
			
			return true;
		}
		return false;
	}
	
	@Override
	public void removeTrace(ITrace trace) {
		if (trace instanceof SurfaceMeshTraceImpl) {
//			chart.clear();
//			ChartLauncher.openChart(chart);
			chart.pauseAnimator();
			chart.getScene().remove(((SurfaceMeshTraceImpl)trace).getShape(),false);

		}
		
		if (trace instanceof WaterfallTraceImpl) {
//			chart.clear();
//			ChartLauncher.openChart(chart);
			chart.pauseAnimator();
			chart.getScene().remove(((WaterfallTraceImpl)trace).getShape(),false);

		}
	}
	
	@Override
	public void repaint(boolean autoscale) {
		chart.render();
	}
	
	public Composite getControl() {
		return control;
	}
	
	/**
	 * Returns true if this viewer can deal with this plot type.
	 * @param clazz
	 * @return
	 */
	@Override
	public boolean isTraceTypeSupported(Class<? extends ITrace> trace) {
		if (ISurfaceMeshTrace.class.isAssignableFrom(trace)) {
			return true;
		}
		
		if (IWaterfallTrace.class.isAssignableFrom(trace)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns array of trace classes supported by this viewer
	 * @return clazzArray
	 */
	public Collection<Class<? extends ITrace>> getSupportTraceTypes(){
		List<Class<? extends ITrace>> l = new ArrayList<>();
		l.add(ISurfaceMeshTrace.class);
		l.add(IWaterfallTrace.class);
		return l;
	}
	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public <U extends ITrace> U createTrace(String name, Class<? extends ITrace> clazz){
		if (clazz == ISurfaceMeshTrace.class) {
			SurfaceMeshTraceImpl trace = new SurfaceMeshTraceImpl(ServiceManager.getPaletteService(),ServiceManager.getImageService(),getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME));
			trace.setName(name);
			return (U)trace;
		}
		
		if (clazz == IWaterfallTrace.class) {
			IWaterfallTrace trace = new WaterfallTraceImpl(ServiceManager.getPaletteService(),ServiceManager.getImageService());
			trace.setName(name);
			return (U)trace;
		}
		return null;
	}
	
	@Override
	public void clearTraces() {
		Graph graph = chart.getScene().getGraph();
		List<AbstractDrawable> all = graph.getAll();
		for (AbstractDrawable a : all) {
			graph.remove(a);
		}
		
	}
	@Override
	public void reset(boolean force) {
		Graph graph = chart.getScene().getGraph();
		List<AbstractDrawable> all = graph.getAll();
		for (AbstractDrawable a : all) {
			graph.remove(a);
		}
		
	}
	
	@Override
	public void setFocus() {
		control.setFocus();
	}

	
	private IPreferenceStore store;
	private IPreferenceStore getPreferenceStore() {
		if (store!=null) return store;
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
		return store;
	}
	
}
