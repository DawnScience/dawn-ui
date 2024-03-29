package org.dawnsci.jzy3d;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.jzy3d.hidpi.CanvasNewtScaledSWT;
import org.dawnsci.jzy3d.hidpi.NewtCanvasSWT;
import org.dawnsci.jzy3d.hidpi.SWTScaledChartComponentFactory;
import org.dawnsci.jzy3d.toolbar.ConfigDialog;
import org.dawnsci.jzy3d.volume.Texture3D;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.plotting.api.ActionType;
import org.eclipse.dawnsci.plotting.api.IPlotActionSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.ManagerType;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceMeshTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeTrace;
import org.eclipse.dawnsci.plotting.api.trace.IWaterfallTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.Settings;
import org.jzy3d.chart.swt.SWTChartComponentFactory;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.IGLBindedResource;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Graph;
import org.jzy3d.plot3d.rendering.view.modes.CameraMode;
import org.jzy3d.plot3d.rendering.view.modes.ViewBoundMode;
import org.jzy3d.plot3d.transform.squarifier.XYSquarifier;
import org.jzy3d.plot3d.transform.squarifier.XZSquarifier;
import org.jzy3d.plot3d.transform.squarifier.YXSquarifier;
import org.jzy3d.plot3d.transform.squarifier.YZSquarifier;
import org.jzy3d.plot3d.transform.squarifier.ZXSquarifier;
import org.jzy3d.plot3d.transform.squarifier.ZYSquarifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;

public class JZY3DPlotViewer extends IPlottingSystemViewer.Stub<Composite> {
	
	private static final String ACTION_ID = "org.dawnsci.jzy3.jzy3dplotviewer.actions";
	
	private static final Logger logger = LoggerFactory.getLogger(JZY3DPlotViewer.class);
	
	private Chart chart;
	private Composite control;
	private int[] shape;
	
	private MenuAction volumeQuality;
	private int downsampling = 1;
	
	@Override
	public void createControl(final Composite parent) {
		String os = System.getProperty("os.name").toLowerCase();
		control = new Composite(parent, SWT.NONE);
		control.setLayout(new FillLayout());
		Settings.getInstance().setHardwareAccelerated(true);
		if (isMac(os)) {
			chart = SWTChartComponentFactory.chart(control, Quality.Intermediate);
		} else {
			chart = SWTScaledChartComponentFactory.chart(control, Quality.Intermediate);
		}
		
		chart.getView().setCameraMode(CameraMode.ORTHOGONAL);
		
		if (isNix(os)) {

			chart.getCanvas().addMouseController(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					
					if (!JZY3DPlotViewer.this.getControl().isFocusControl()) {
						JZY3DPlotViewer.this.setFocus();
					}

				}

			});
			
			NewtCanvasSWT canvas = ((CanvasNewtScaledSWT)chart.getCanvas()).getCanvas();
			canvas.addListener(SWT.FocusOut, new Listener() {
				
				@Override
				public void handleEvent(Event event) {
					if (event.type == SWT.FocusOut) {
						canvas.getNEWTChild().setVisible(false);
						canvas.getNEWTChild().setVisible(true);
						logger.debug("Manual focus out");
					}
					
				}
			});
		}
		
		chart.getCanvas().addMouseController(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()>1) {
					chart.resumeAnimator();
				}
			}
			
			@Override
			 public void mousePressed(final MouseEvent e) {
				chart.resumeAnimator();
			 }
			 @Override
			 public void mouseReleased(final MouseEvent e) {
				 chart.pauseAnimator();
			 }
			 
			 @Override
			 public void mouseWheelMoved(final MouseEvent e) {
				 chart.render();
			 }
		});

		chart.addKeyboardCameraController();
		chart.addMouseCameraController();
		chart.pauseAnimator();
		chart.render();
		
		createToolbar();
		createMenu();
	}
	
	private Texture3D getVolume() {
		Graph graph = chart.getScene().getGraph();
		List<AbstractDrawable> all = graph.getAll();
		for (AbstractDrawable a : all) {
			if (a instanceof Texture3D) {
				return (Texture3D)a;
			}
		}
		
		return null;
	}
	
	private void createMenu() {
		final IPaletteService pservice = Activator.getService(IPaletteService.class);
		final Collection<String> names = pservice.getColorSchemes();

		String schemeName = pservice.getDefaultColorScheme();

		MenuAction lutCombo = new MenuAction("Color");
		lutCombo.setImageDescriptor(Activator.getImageDescriptor("icons/color_wheel.png"));

		final Map<String, IAction> paletteActions = new HashMap<String, IAction>(names.size());
		CheckableActionGroup group      = new CheckableActionGroup();
		
		Collection<String> categoryNames = pservice.getColorCategories();
		for (String c : categoryNames) {
			if (!c.equals("All")) {
				MenuAction subMenu = new MenuAction(c);
				Collection<String> colours = pservice.getColorsByCategory(c);
				for (final String colour : colours) {
					final Action action = new Action(colour, IAction.AS_CHECK_BOX) {
						public void run() {
							try {
								pservice.setDefaultColorScheme(getText());
							} catch (Exception ne) {
								logger.error("Cannot create palette data!", ne);
							}
						}
					};
					action.setId(colour);
					subMenu.add(action);
					group.add(action);
					action.setChecked(colour.equals(schemeName));
					paletteActions.put(colour, action);
				}
				lutCombo.add(subMenu);
			}
		}
		IPlotActionSystem plotActionSystem = system.getPlotActionSystem();
		plotActionSystem.registerGroup(ACTION_ID, ManagerType.MENUBAR);
		plotActionSystem.registerAction(ACTION_ID, lutCombo, ActionType.JZY3D_COLOR, ManagerType.MENUBAR);
	}
	
	private void createToolbar() {
		IPlotActionSystem plotActionSystem = system.getPlotActionSystem();
		
		volumeQuality = new MenuAction("Quality");
		volumeQuality.setToolTipText("Set volume rendering quality/speed");
		
		Action best = new Action("Best/No downsampling", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				downsampling = 1;
				Texture3D volume = getVolume();
				if (volume != null) {
					volume.setDownsampling(1);
					chart.render();
				}
			}
		};
		
		volumeQuality.add(best);
		best.setChecked(true);
		
		Action good = new Action("Good/x2", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				downsampling = 2;
				Texture3D volume = getVolume();
				if (volume != null) {
					volume.setDownsampling(2);
					chart.render();
				}
			}
		};
		
		volumeQuality.add(good);
		
		Action fast = new Action("Fast/x4", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				downsampling = 4;
				Texture3D volume = getVolume();
				if (volume != null) {
					volume.setDownsampling(4);
					chart.render();
				}
			}
		};
		
		volumeQuality.add(fast);
		
		Action fastest = new Action("Fastest/x8", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				downsampling = 8;
				Texture3D volume = getVolume();
				if (volume != null) {
					volume.setDownsampling(8);
					chart.render();
				}
			}
		};
		
		volumeQuality.add(fastest);
		
		Action configureAction = new Action("Configure") {
			@Override
			public void run() {
				ConfigDialog prefs = new ConfigDialog(Display.getDefault().getActiveShell(), chart, shape);
				prefs.open();
			}
		};
		configureAction.setToolTipText("Configure plot settings");
		configureAction.setImageDescriptor(Activator.getImageDescriptor("icons/Configure.png"));
		
		Action saveAction = new Action("Save Screenshot") {
			@Override
			public void run() {
				try {
					
					FileDialog fd = new FileDialog(Display.getDefault().getActiveShell(),SWT.SAVE);
					fd.setFilterExtensions(new String[] {".png"});
					String file = fd.open();
				
					if (file == null) return;
					File f = new File(file);
					chart.render();
					chart.screenshot(f);
				} catch (IOException e) {
					MessageBox dialog = new MessageBox(control.getShell(),SWT.ICON_ERROR | SWT.OK);
					dialog.setText("Error saving screenshot!");
					dialog.setMessage(e.getMessage());
					dialog.open();
					logger.error("Error saving screenshot",e);
				}
			}
		};
		saveAction.setToolTipText("Save as png");
		saveAction.setImageDescriptor(Activator.getImageDescriptor("icons/picture_save.png"));
		
		MenuAction menuAction = new MenuAction("Axes Aspect Ratio");
		menuAction.setToolTipText("Set squaring of axes");
		menuAction.setImageDescriptor(Activator.getImageDescriptor("icons/orthographic.png"));
		
		Action s = new Action("Square", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				chart.getView().setSquarifier(null);
				chart.getView().setSquared(true);
				
			}
		};
		
		s.setChecked(true);
		
		menuAction.add(s);
		
		menuAction.add(new Action("Axes Aspect Ratio", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				chart.getView().setSquared(false);
				chart.render();
			}
		});
		
		menuAction.add(new Action("Square XZ", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				chart.getView().setSquarifier(new XZSquarifier());
				chart.getView().setSquared(true);
				chart.render();
			}
		});
		
		menuAction.add(new Action("Square ZX", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				chart.getView().setSquarifier(new ZXSquarifier());
				chart.getView().setSquared(true);
				chart.render();
			}
		});
		
		menuAction.add(new Action("Square YZ", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				chart.getView().setSquarifier(new YZSquarifier());
				chart.getView().setSquared(true);
				chart.render();
			}
		});
		
		menuAction.add(new Action("Square ZY", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				chart.getView().setSquarifier(new ZYSquarifier());
				chart.getView().setSquared(true);
				chart.render();
			}
		});
		
		menuAction.add(new Action("Square XY", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				chart.getView().setSquarifier(new XYSquarifier());
				chart.getView().setSquared(true);
				chart.render();
			}
		});
		
		menuAction.add(new Action("Square YX", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				chart.getView().setSquarifier(new YXSquarifier());
				chart.getView().setSquared(true);
				chart.render();
			}
		});
		
		menuAction.add(new Action("Custom...", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				if (!this.isChecked()) {
					return;
				}
				CustomSquarifier customSquarifier = new CustomSquarifier();
				chart.getView().setSquarifier(customSquarifier);
				chart.getView().setSquared(true);
				Dialog d = new AxesAspectDialog(Display.getDefault().getActiveShell(), customSquarifier);
				d.create();
				d.open();
			}
		});
		
		plotActionSystem.registerGroup(ACTION_ID, ManagerType.TOOLBAR);
		plotActionSystem.registerAction(ACTION_ID, volumeQuality, ActionType.JZY3D_COLOR, ManagerType.TOOLBAR);
		plotActionSystem.registerAction(ACTION_ID, configureAction, ActionType.JZY3D_COLOR, ManagerType.TOOLBAR);
		plotActionSystem.registerAction(ACTION_ID, menuAction, ActionType.JZY3D_COLOR, ManagerType.TOOLBAR);
		plotActionSystem.registerAction(ACTION_ID, saveAction, ActionType.JZY3D_COLOR, ManagerType.TOOLBAR);
		
		plotActionSystem.createToolDimensionalActions(ToolPageRole.ROLE_JUST_COLOUR, "org.dawb.workbench.plotting.views.toolPageView.Color");
	}

	
	@Override
	public boolean addTrace(ITrace trace){
		
		// set shape where [0] is x, [1] is y, and [2] is z max value
		int[] originalShape = trace.getData().getShape();
		int maxDataValue = trace.getData().max(true).intValue();
		shape = new int[] { originalShape[0], originalShape[1], maxDataValue };

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
		
		String z = "Z";
		
		if (axes.size() > 2 && axes.get(2) != null) {
			z = MetadataPlotUtils.removeSquareBrackets( axes.get(2).getName());
		}
		
		chart.getAxeLayout().setXAxeLabel(x);
		chart.getAxeLayout().setYAxeLabel(y);
		chart.getAxeLayout().setZAxeLabel(z);
		
		if (trace instanceof IVolumeTrace) {
			volumeQuality.setEnabled(true);
			((IVolumeTrace)trace).setDownsampling(downsampling);
		} else {
			volumeQuality.setEnabled(false);
		}
		
		if (trace instanceof Abstract2DJZY3DTrace) {
			((Abstract2DJZY3DTrace) trace).setPalette(getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME));
			((Abstract2DJZY3DTrace) trace).addPaletteListener(new IPaletteListener.Stub() {
				
				protected void updateEvent(PaletteEvent evt) {
					chart.render();
				}
			});
			chart.getScene().add(((Abstract2DJZY3DTrace)trace).getShape(),true);
			chart.getView().setBoundMode(ViewBoundMode.AUTO_FIT);
			return true;
		}
		

		return false;
	}
	
	@Override
	public void removeTrace(ITrace trace) {
		if (trace instanceof Abstract2DJZY3DTrace) {

			AbstractDrawable s = ((Abstract2DJZY3DTrace)trace).getShape();

			if (s instanceof IGLBindedResource) {
				s.dispose();
				chart.render();
			}
			chart.getScene().remove(((Abstract2DJZY3DTrace)trace).getShape(),false);

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
		
		if (IVolumeTrace.class.isAssignableFrom(trace)) {
			return true;
		}
		
		return false;
	}

	/**
	 * Returns array of trace classes supported by this viewer
	 * @return clazzArray
	 */
	@Override
	public Collection<Class<? extends ITrace>> getSupportTraceTypes() {
		List<Class<? extends ITrace>> l = new ArrayList<>();
		l.add(ISurfaceMeshTrace.class);
		l.add(IWaterfallTrace.class);
		l.add(IVolumeTrace.class);
		return l;
	}

	/**
	 * Create trace
	 * @param name
	 * @param clazz
	 * @return trace of given name and class
	 */
	@SuppressWarnings("unchecked")
	public <U extends ITrace> U createTrace(String name, Class<U> clazz) {
		U trace = null;
		if (clazz == ISurfaceMeshTrace.class) {
			trace = (U) new SurfaceMeshTraceImpl(getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME));
		} else if (clazz == IWaterfallTrace.class) {
			trace = (U) new WaterfallTraceImpl(getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME));
		} else if (clazz == IVolumeTrace.class) {
			trace = (U) new VolumeTraceImpl(getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME));
		}

		if (trace != null) {
			trace.setName(name);
		}

		return trace;
	}

	@Override
	public void clearTraces() {
		Graph graph = chart.getScene().getGraph();
		List<AbstractDrawable> all = graph.getAll();
		
		for (AbstractDrawable a : all) {
			if (a instanceof IGLBindedResource) {
				a.dispose();
				
			}
		}
		chart.render();
		
		all.clear();
		
		chart.getView().setBoundManual(new BoundingBox3d(0f,1f,0f,1f,0f,1f));
		
		chart.render();
	}
	
	@Override
	public void reset(boolean force) {
		clearTraces();
		
	}
	
	@Override
	public void setFocus() {
		control.setFocus();
	}

	
	private IPreferenceStore store;
	private IPreferenceStore getPreferenceStore() {
		if (store!=null) return store;
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, IPlottingSystem.PREFERENCE_STORE);
		return store;
	}
	
	private boolean isMac(String os) {
		return os.contains("mac");
	}
	
	private boolean isNix(String os) {
		//lets just say not mac or windows
		return !(os.contains("mac") || os.contains("windows"));
	}
	
}
