package org.dawnsci.jzy3d;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.jzy3d.preferences.PreferenceDialog;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.plotting.api.ActionType;
import org.eclipse.dawnsci.plotting.api.IPlotActionSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.ManagerType;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceMeshTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.IWaterfallTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.Settings;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.swt.CanvasNewtSWT;
import org.jzy3d.chart.swt.SWTChartComponentFactory;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Graph;
import org.jzy3d.plot3d.rendering.view.modes.CameraMode;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.swt.NewtCanvasSWT;

public class JZY3DPlotViewer extends IPlottingSystemViewer.Stub<Composite> {
	
	private Chart chart;
	private Composite control;
	
	@Override
	public void createControl(final Composite parent) {
		control = new Composite(parent, SWT.NONE);
		control.setLayout(new FillLayout());
		Settings.getInstance().setHardwareAccelerated(true);
		chart = SWTChartComponentFactory.chart(control, Quality.Intermediate);
		chart.getView().setCameraMode(CameraMode.ORTHOGONAL);
		chart.getCanvas().addMouseController(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (!JZY3DPlotViewer.this.getControl().isFocusControl()) JZY3DPlotViewer.this.setFocus();
				
			}
		});
		
		NewtCanvasSWT canvas = ((CanvasNewtSWT)chart.getCanvas()).getCanvas();
		canvas.addListener(SWT.FocusOut, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				if (event.type == SWT.FocusOut) {
					canvas.getNEWTChild().setVisible(false);
					canvas.getNEWTChild().setVisible(true);
				}
				
			}
		});
		chart.addKeyboardCameraController();
		chart.addMouseCameraController();
		chart.render();
		
		createToolbar();
	}
	
	private void createToolbar() {
		IPlotActionSystem plotActionSystem = system.getPlotActionSystem();
		
		Action configureAction = new Action("Configure") {
			@Override
			public void run() {
				PreferenceDialog prefs = new PreferenceDialog(Display.getDefault().getActiveShell(), chart);
				prefs.open();
			}
		};
		configureAction.setToolTipText("Configure plot settings");
		configureAction.setImageDescriptor(Activator.getImageDescriptor("icons/Configure.png"));
		
		Action squareAction = new Action("Square", Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				chart.getView().setSquared(this.isChecked());
				
			}
		};
		squareAction.setChecked(true);
		squareAction.setToolTipText("Set axes equal or use data aspect ratio");
		squareAction.setImageDescriptor(Activator.getImageDescriptor("icons/orthographic.png"));
		
		Action saveAction = new Action("Save Screenshot") {
			@Override
			public void run() {
				try {
					
					FileDialog fd = new FileDialog(Display.getDefault().getActiveShell(),SWT.SAVE);
					fd.setFilterExtensions(new String[] {".png"});
					String file = fd.open();
				
					if (file == null) return;
					File f = new File(file);
					int h = chart.getCanvas().getRendererHeight();
					int w = chart.getCanvas().getRendererWidth();
					Chart chart2 = AWTChartComponentFactory.chart(Quality.Intermediate, "offscreen,"+w+","+h);
					chart2.getScene().add(chart.getScene().getGraph().getAll());
					chart2.setViewPoint(chart.getViewPoint());
					chart2.screenshot(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		};
		saveAction.setToolTipText("Save as png");
		saveAction.setImageDescriptor(Activator.getImageDescriptor("icons/picture_save.png"));
		
		plotActionSystem.registerGroup("org.dawnsci.jzy3.jzy3dplotviewer.actions", ManagerType.TOOLBAR);
		plotActionSystem.registerAction("org.dawnsci.jzy3.jzy3dplotviewer.actions", configureAction, ActionType.JZY3D_COLOR, ManagerType.TOOLBAR);
		plotActionSystem.registerAction("org.dawnsci.jzy3.jzy3dplotviewer.actions", squareAction, ActionType.JZY3D_COLOR, ManagerType.TOOLBAR);
		plotActionSystem.registerAction("org.dawnsci.jzy3.jzy3dplotviewer.actions", saveAction, ActionType.JZY3D_COLOR, ManagerType.TOOLBAR);
		
		plotActionSystem.createToolDimensionalActions(ToolPageRole.ROLE_JUST_COLOUR, "org.dawb.workbench.plotting.views.toolPageView.Color");
	}

	
	@Override
	public boolean addTrace(ITrace trace){
		
		List<IDataset> axes = ((AbstractColorMapTrace)trace).getAxes();
		
		IDataset xD = axes.get(0);
		IDataset yD = axes.get(1);
		
		String x = "X";
		
		if (xD != null && xD.getName() != null) {
			x = MetadataPlotUtils.removeSquareBrackets(xD.getName());
		}
		
		String y = "Y";
		
		if (yD != null && yD.getName() != null) {
			y = MetadataPlotUtils.removeSquareBrackets(yD.getName());
		}
		
		chart.getAxeLayout().setXAxeLabel(x);
		chart.getAxeLayout().setYAxeLabel(y);
		
		if (trace instanceof SurfaceMeshTraceImpl) {
			chart.pauseAnimator();
			((SurfaceMeshTraceImpl) trace).setPalette(getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME));
			chart.getScene().add(((SurfaceMeshTraceImpl)trace).getShape());
			chart.resumeAnimator();
			return true;
		}
		if (trace instanceof WaterfallTraceImpl) {
			chart.pauseAnimator();
			((WaterfallTraceImpl) trace).setPalette(getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME));
			chart.getScene().add(((WaterfallTraceImpl)trace).getShape());
			chart.resumeAnimator();
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
