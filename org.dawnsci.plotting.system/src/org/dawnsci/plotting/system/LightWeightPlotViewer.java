/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dawb.common.ui.printing.IPrintImageProvider;
import org.dawb.common.ui.printing.PlotExportPrintUtil;
import org.dawb.common.ui.printing.PlotPrintPreviewDialog;
import org.dawb.common.ui.printing.PrintSettings;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.PlotDataConversionWizard;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.dawnsci.plotting.AbstractPlottingViewer;
import org.dawnsci.plotting.draw2d.swtxy.AspectAxis;
import org.dawnsci.plotting.draw2d.swtxy.ImageStackTrace;
import org.dawnsci.plotting.draw2d.swtxy.ImageTrace;
import org.dawnsci.plotting.draw2d.swtxy.LineTrace;
import org.dawnsci.plotting.draw2d.swtxy.RegionArea;
import org.dawnsci.plotting.draw2d.swtxy.RegionCreationLayer;
import org.dawnsci.plotting.draw2d.swtxy.VectorTrace;
import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.dawnsci.plotting.draw2d.swtxy.selection.SelectionRegionFactory;
import org.dawnsci.plotting.system.dialog.XYRegionConfigDialog;
import org.dawnsci.plotting.util.ColorUtility;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.IPrintablePlotting;
import org.eclipse.dawnsci.plotting.api.ITraceActionProvider;
import org.eclipse.dawnsci.plotting.api.PlotLocationInfo;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.annotation.AnnotationUtils;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotationSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IAxisSystem;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.axis.IPositionListener;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.histogram.functions.FunctionContainer;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionContainer;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.IRegionSystem;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.dawnsci.plotting.api.trace.IImageStackTrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceContainer;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.IVectorTrace;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.PrintFigureOperation;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.nebula.visualization.internal.xygraph.toolbar.RemoveAnnotationDialog;
import org.eclipse.nebula.visualization.internal.xygraph.undo.AddAnnotationCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.RemoveAnnotationCommand;
import org.eclipse.nebula.visualization.widgets.datadefinition.IManualValueChangeListener;
import org.eclipse.nebula.visualization.widgets.figureparts.ColorMapRamp;
import org.eclipse.nebula.visualization.widgets.figures.ScaledSliderFigure;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.DAxis;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.nebula.visualization.xygraph.linearscale.AbstractScale.LabelSide;
import org.eclipse.nebula.visualization.xygraph.linearscale.LinearScaleTickLabels;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Package private. This class deals with plotting actions specific to the
 * LightWeight (non-3D) part of the plotting.
 * 
 * @author Matthew Gerring
 *
 */
public class LightWeightPlotViewer<T> extends AbstractPlottingViewer<T> implements IPlottingSystemViewer<T>, IAnnotationSystem, IRegionSystem, IAxisSystem, IPrintablePlotting, ITraceActionProvider, IAdaptable {

	private static final Logger logger = LoggerFactory.getLogger(LightWeightPlotViewer.class);

	// Canvas used in SWT mode
	private Canvas           figureCanvas;

	// Controls
	private XYRegionGraph          xyGraph;

	// Plotting stuff
	private PlottingSystemImpl<T>  system;
	private LightWeightPlotActions plotActionsCreator;
	private Figure                 plotContents;
	private Figure                 graphLayer;
	private ColorMapRamp           intensity;
	private ScaledSliderFigure     folderScale;
	private Axis                  colorMapAxis;
	public static final String XAXIS_DEFAULT_NAME = "X-Axis";
	public static final String YAXIS_DEFAULT_NAME = "Y-Axis";

	private static final double COLORMAP_WHEEL_SCALE = 10;

	private LayeredPane content;

	private static String lastPath;

	private static final Collection<Class<? extends ITrace>> SUPPORTED_TRACES = Arrays.asList(ILineTrace.class, IImageTrace.class, IVectorTrace.class, IImageStackTrace.class);

	public void init(IPlottingSystem<T> system) {
		this.system = (PlottingSystemImpl<T>)system;
	}

	/**
	 * Call to create plotting
	 * @param parent
	 * @param initialMode may be null
	 */
	public void createControl(final T parent) {

		if (plotContents!=null) return;
		
		LightweightSystem lws = null;

		if (parent instanceof Composite) {
			Canvas xyCanvas = new Canvas((Composite)parent, SWT.DOUBLE_BUFFERED|SWT.NO_REDRAW_RESIZE|SWT.NO_BACKGROUND);
			lws = new LightweightSystem(xyCanvas);

			// Stops a mouse wheel move corrupting the plotting area, but it wobbles a bit.
			xyCanvas.addMouseWheelListener(getMouseWheelListener());
			xyCanvas.addKeyListener(getKeyListener());

			lws.setControl(xyCanvas);

			final MenuManager popupMenu = new MenuManager();
			popupMenu.setRemoveAllWhenShown(true); // Remake menu each time
			xyCanvas.setMenu(popupMenu.createContextMenu(xyCanvas));
			popupMenu.addMenuListener(getIMenuListener());

			this.figureCanvas = xyCanvas;
		}
		this.xyGraph = new XYRegionGraph();
		xyGraph.setSelectionProvider(system.getSelectionProvider());
		IActionBars bars = system.getActionBars();
		PlotActionsManagerImpl actionBarManager = (PlotActionsManagerImpl)system.getPlotActionSystem();
		// We contain the action bars in an internal object
		// if the API user said they were null. This allows the API
		// user to say null for action bars and then use:
		// getPlotActionSystem().fillXXX() to add their own actions.
		if (bars==null) {
			bars = actionBarManager.createEmptyActionBars();
			system.setActionBars(bars);
		}

		actionBarManager.init(this);
		this.plotActionsCreator = new LightWeightPlotActions();
		plotActionsCreator.init(this, xyGraph, actionBarManager);
		plotActionsCreator.createLightWeightActions();

		// Create the layers (currently everything apart from the temporary 
		// region draw layer is on 0)
		this.content      = new LayeredPane();
		new RegionCreationLayer(content, xyGraph.getRegionArea());  
		graphLayer = new Layer();

		graphLayer.setLayoutManager(new BorderLayout());

		plotContents = new Figure();
		plotContents.setLayoutManager(new GridLayout(1, false));
		plotContents.add(xyGraph, new GridData(SWT.FILL, SWT.FILL, true, true));
		graphLayer.add(plotContents, BorderLayout.CENTER);


		colorMapAxis = new DAxis();
		colorMapAxis.setShowMaxLabel(true);
		colorMapAxis.setShowMinLabel(true);

		this.intensity = new ColorMapRamp(colorMapAxis);

		ColorMapMouseListener l = new ColorMapMouseListener();

		intensity.addMouseMotionListener(l);
		intensity.addMouseListener(l);
		
		intensity.setBorder(new LineBorder(ColorConstants.white, 5));
		graphLayer.add(intensity, BorderLayout.RIGHT);
		intensity.setVisible(false);

		this.folderScale = new ScaledSliderFigure();
		folderScale.setRange(0, 100);
		folderScale.setStepIncrement(1.0);
		folderScale.setValue(0);
		folderScale.setHorizontal(true);
		folderScale.setShowHi(false);
		folderScale.setShowLo(false);
		folderScale.setShowHihi(false);
		folderScale.setShowLolo(false);
		folderScale.setShowMarkers(false);
		folderScale.setDrawFocus(false);
		folderScale.addManualValueChangeListener(new IManualValueChangeListener() {			
			@Override
			public void manualValueChanged(double newValue) {
				setStackIndex((int)Math.round(newValue));
			}
		});

		content.add(graphLayer,     0);
		if (lws!=null) {
			lws.setContents(content);
		} else {
			((Figure)parent).add(content);
		}

		// Create status contribution for position
		IWorkbenchPart part = system.getPart();
		if (part!=null) {
			IStatusLineManager statusLine = null;
			if (part instanceof IViewPart) {
				bars = ((IViewPart)part).getViewSite().getActionBars();
				statusLine = bars.getStatusLineManager();
			} else if (part instanceof IEditorPart) {
				bars = ((IEditorPart)part).getEditorSite().getActionBars();
				statusLine = bars.getStatusLineManager();
			}
			if (statusLine!=null) {
				xyGraph.getRegionArea().setStatusLineManager(statusLine);
			}
		}

		// Configure axes
		xyGraph.getPrimaryXAxis().setShowMajorGrid(true);
		xyGraph.getPrimaryXAxis().setShowMinorGrid(true);		
		xyGraph.getPrimaryYAxis().setShowMajorGrid(true);
		xyGraph.getPrimaryYAxis().setShowMinorGrid(true);
		xyGraph.getPrimaryYAxis().setTitle("");

		if (system.getPlotType()!=null) {
			// Do not change this, the test is correct, remove axes in non-1D only. 1D always has axes.
			if (!PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.SHOW_AXES) && !system.getPlotType().is1D()) {
				xyGraph.getPrimaryXAxis().setVisible(false);
				xyGraph.getPrimaryYAxis().setVisible(false);
			}
		}
		if (bars!=null) bars.updateActionBars();
		if (bars!=null) bars.getToolBarManager().update(true);

		if (system.getPlotType()!=null) {
			actionBarManager.switchActions(system.getPlotType());
		}

		if (parent instanceof Composite) {
			((Composite)parent).layout();
		} else {
			((Figure)parent).revalidate();
		}

		//CSS colors applied late so need to do this at the end to avoid
		//the wrong border color
		if (figureCanvas != null) {
			((LineBorder)intensity.getBorder()).setColor(figureCanvas.getBackground());
		}

	}
	public boolean isPlotTypeSupported(PlotType type) {
		return type.is1D() || type.is2D();
	}

	private void setStackIndex(int index) {
		if (system.getTraces()!=null && !system.getTraces().isEmpty()) {
			final ITrace trace = (ITrace) system.getTraces().iterator().next();
			if (trace instanceof IImageStackTrace) {
				IImageStackTrace stack = (IImageStackTrace)trace;
				stack.setStackIndex(index); // Updates the plot.
			}
		}
	}

	private void setFolderScaleVisible(boolean vis) {
		if (vis) {
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
			data.heightHint=80;
			plotContents.add(folderScale, data);
		} else {
			if (folderScale.getParent()==null) return;
			plotContents.remove(folderScale);
		}
	}


	public void addImageTraceListener(final ITraceListener l) {
		if (xyGraph!=null) ((RegionArea)xyGraph.getPlotArea()).addImageTraceListener(l);
	}

	public void removeImageTraceListener(final ITraceListener l) {
		if (xyGraph!=null) ((RegionArea)xyGraph.getPlotArea()).removeImageTraceListener(l);
	}

	final static double ZOOM_RATIO = 0.1; // from Axis

	private MouseWheelListener mouseWheelListener;
	private MouseWheelListener getMouseWheelListener() {
		if (mouseWheelListener == null) mouseWheelListener = new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent e) {

				int direction = e.count > 0 ? 1 : -1;

				IFigure fig = getFigureAtCurrentMousePosition(null);

				if (isColorMapFigure(fig)) {

					ImageTrace image = getImageTrace();
					if (image == null) return;

					ImageServiceBean bean = image.getImageServiceBean();
					if (bean == null) return;

					double max = bean.getMax().doubleValue();
					double min = bean.getMin().doubleValue();

					double currentValue = colorMapAxis.getPositionValue(e.y-intensity.getLocation().y, false);
					if (currentValue > max) currentValue = max;
					if (currentValue < min) currentValue = min;
					double fraction = (currentValue - min) / (max  - min);
					double offset = ((max-min)/COLORMAP_WHEEL_SCALE)*direction ;
					double minOffset = min+(offset*fraction);
					double maxOffset = max-(offset*(1-fraction));
					image.getImageServiceBean().setMin(minOffset);
					image.setMin(minOffset);
					image.getImageServiceBean().setMax(maxOffset);
					image.setMax(maxOffset);
					image.setPaletteData(image.getPaletteData());

					return;
				}

				if (fig!=null && fig.getParent() instanceof Axis) {
					Axis axis = (Axis)fig.getParent();
					final double center = axis.getPositionValue(axis.isHorizontal() ? e.x : e.y, false);
					axis.zoomInOut(center, direction * ZOOM_RATIO);
					xyGraph.repaint();
					return;
				}

				if (xyGraph==null) return;
				if (e.count==0)    return;
				String level  = System.getProperty("org.dawb.workbench.plotting.system.zoomLevel");
				double factor = level != null ? Double.parseDouble(level) : ZOOM_RATIO;

				boolean useWhite = PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.ZOOM_INTO_WHITESPACE);
				xyGraph.setZoomLevel(e, direction*factor, useWhite);
				redraw();
			}	
		};
		return mouseWheelListener;
	}

	private boolean isColorMapFigure(IFigure fig) {
		return (fig != null &&
				fig.getParent() != null &&
				(fig.getParent() instanceof ColorMapRamp || fig.getParent().getParent() instanceof ColorMapRamp));
	}

	private void redraw() {
		if (figureCanvas!=null && !figureCanvas.isDisposed()) figureCanvas.redraw();	
		if (content!=null) content.revalidate();
	}

	private KeyListener keyListener;
	private KeyListener getKeyListener() {

		final IActionBars bars = system.getActionBars();
		if (keyListener==null) keyListener = new KeyAdapter() {
			private ZoomType previousTool;

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode==27) { // Esc
					xyGraph.clearRegionTool();

				} else if (e.keyCode==16777230 || e.character=='h') {
					final IContributionItem action = bars.getToolBarManager().find("org.dawb.workbench.plotting.histo");
					if (action!=null && action.isVisible() && action instanceof ActionContributionItem) {
						ActionContributionItem iaction = (ActionContributionItem)action;
						iaction.getAction().setChecked(!iaction.getAction().isChecked());
						iaction.getAction().run();
					}

				} else if (e.character=='f') {
					final ImageTrace trace = xyGraph.getRegionArea().getImageTrace();
					// Force functional creation of image rather than 8-bit
					if (trace!=null) try {
						IPaletteService pservice = PlottingSystemActivator.getService(IPaletteService.class);
						if (pservice !=null) {
							FunctionContainer container = pservice.getFunctionContainer(trace.getPaletteName());
							if (container!=null) {
								trace.getImageServiceBean().setFunctionObject(container);
								trace.remask();
								trace.getImageServiceBean().setFunctionObject(null);
							}
						}
					} catch (Exception e1) {
						logger.debug("Cannot apply custom palette function", e1);
					}



				} else if (e.keyCode== SWT.F11) {
					final IContributionItem action = bars.getToolBarManager().find("org.dawb.workbench.fullscreen");
					if (action!=null && action.isVisible() && action instanceof ActionContributionItem) {
						ActionContributionItem iaction = (ActionContributionItem)action;
						iaction.getAction().setChecked(!iaction.getAction().isChecked());
						iaction.getAction().run();
					}
				} else if (e.keyCode==16777217) {//Up
					Point point = Display.getDefault().getCursorLocation();
					point.y-=1;
					Display.getDefault().setCursorLocation(point);

				} else if (e.keyCode==16777218) {//Down
					Point point = Display.getDefault().getCursorLocation();
					point.y+=1;
					Display.getDefault().setCursorLocation(point);

				} else if (e.keyCode==16777219) {//Left
					Point point = Display.getDefault().getCursorLocation();
					point.x-=1;
					Display.getDefault().setCursorLocation(point);

				} else if (e.keyCode==16777220) {//Right
					Point point = Display.getDefault().getCursorLocation();
					point.x+=1;
					Display.getDefault().setCursorLocation(point);
				} else if (e.keyCode==127) {//Delete
					IFigure fig = getFigureAtCurrentMousePosition(IRegionContainer.class);
					if (fig!=null && fig instanceof IRegionContainer) {
						final IRegion region = (AbstractSelectionRegion<?>) ((IRegionContainer)fig).getRegion();
						if (region.isUserRegion())
							xyGraph.removeRegion(region);
					}
				}
				if (e.keyCode == 131072) { // SHIFT
					xyGraph.getRegionArea().setShiftDown(true);
				}
				if (e.keyCode == 262144) { // CONTROL
					xyGraph.getRegionArea().setControlDown(true);
					previousTool = xyGraph.getZoomType();
					xyGraph.setZoomType(ZoomType.NONE);
				}
				xyGraph.getRegionArea().setKeyEvent(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {

				if ((e.stateMask & SWT.SHIFT)==SWT.SHIFT) {
					xyGraph.getRegionArea().setShiftDown(false);
				}
				if ((e.stateMask & SWT.CONTROL)==SWT.CONTROL) {
					xyGraph.getRegionArea().setControlDown(false);
					if (previousTool!=null) {
						xyGraph.setZoomType(previousTool);
						previousTool = null;
					}
				}
				if (e.character == 'x') {
					if (system.getPlotType().is1D()) {
						IAxis xAxis = system.getSelectedXAxis();
						if (xAxis != null) {
							xAxis.setLog10(!xAxis.isLog10());
						}
					}
				}
				if (e.character == 'y') {
					if (system.getPlotType().is1D()) {
						IAxis yAxis = system.getSelectedYAxis();
						if (yAxis != null) {
							yAxis.setLog10(!yAxis.isLog10());
						}
					}
				}
				xyGraph.getRegionArea().setKeyEvent(null);
			}
		};
		return keyListener;
	}


	private IMenuListener popupListener;
	private IMenuListener getIMenuListener() {
		if (popupListener == null) {
			popupListener = new IMenuListener() {			
				@Override
				public void menuAboutToShow(IMenuManager manager) {

					IRegion beanRegion = null;
					ITrace beanTrace = null;
					IAxis beanAxis = null;

					IFigure fig = getFigureAtCurrentMousePosition(IRegionContainer.class);
					if (fig!=null && fig instanceof IRegionContainer && ((IRegionContainer)fig).getRegion().isUserRegion()) {
						final IRegion region = ((IRegionContainer)fig).getRegion();
						beanRegion = region;
						SelectionRegionFactory.fillActions(manager, region, xyGraph, getSystem());

						final Action configure = new Action("Configure '"+region.getName()+"'", PlottingSystemActivator.getImageDescriptor("icons/RegionProperties.png")) {
							public void run() {
								final XYRegionConfigDialog dialog = new XYRegionConfigDialog(Display.getCurrent().getActiveShell(), xyGraph, system.isRescale());
								dialog.setSelectedRegion(region);
								dialog.open();
							}
						};
						manager.add(configure);

						manager.add(new Separator("org.dawb.workbench.plotting.system.region.end"));

					} else {
						fig = getFigureAtCurrentMousePosition(null);
						if (fig instanceof ITraceContainer) {
							final ITrace trace = ((ITraceContainer) fig).getTrace();
							beanTrace = trace;
							fillTraceActions(manager, trace, system);
						}
						if (fig instanceof Label && fig.getParent() instanceof Annotation) {
							fillAnnotationConfigure(manager, (Annotation) fig.getParent(), system);
						}
						if (fig instanceof LinearScaleTickLabels) {
							LinearScaleTickLabels label = (LinearScaleTickLabels)fig;
							Axis scale = (Axis)label.getScale();
							if (scale instanceof IAxis) beanAxis = (IAxis)scale;
							fillAxisConfigure(manager, scale);
						}
					}

					if (isColorMapFigure(fig)) {
						fillColorMapConfigure(manager);
					} else {
						system.getPlotActionSystem().fillZoomActions(manager);
					}

					double[] xyPosition = getXYPosition();

					PlotLocationInfo bean = new PlotLocationInfo(system, xyPosition[0], xyPosition[1], beanRegion, beanTrace, beanAxis);

					system.getPlotActionSystem().fillPopupActions(manager, bean);

					manager.update();
				}
			};
		}
		return popupListener;
	}

	private double[] getXYPosition() {
		Point   pnt       = Display.getDefault().getCursorLocation();
		Point   par       = toDisplay(new Point(0,0));

		final int xOffset = par.x+xyGraph.getLocation().x;
		final int yOffset = par.y+xyGraph.getLocation().y;
		final double xVal   = xyGraph.getSelectedXAxis().getPositionValue(pnt.x - xOffset);
		final double yVal   = xyGraph.getSelectedYAxis().getPositionValue(pnt.y - yOffset);
		return new double[] {xVal,yVal};
	}

	protected IFigure getFigureAtCurrentMousePosition(Class<?> type) {
		Point   pnt       = Display.getDefault().getCursorLocation();
		Point   par       = toDisplay(new Point(0,0));

		final int xOffset = par.x+xyGraph.getLocation().x;
		final int yOffset = par.y+xyGraph.getLocation().y;

		IFigure fig = xyGraph.findFigureAt(pnt.x-xOffset, pnt.y-yOffset);
		if (fig!=null && type==null)          return fig;
		if (fig!=null && type.isInstance(fig)) return fig;


		fig = intensity.findFigureAt(pnt.x-par.x, pnt.y-par.y);

		if (fig!=null && type==null)          return fig;
		if (fig!=null && type.isInstance(fig)) return fig;

		// We loop +-5 around the click point to find what we want
		for (int x = 1;x<=5; x++){
			fig = xyGraph.findFigureAt(pnt.x-xOffset+x, pnt.y-yOffset);
			if (fig!=null && type==null)          return fig;
			if (fig!=null && type.isInstance(fig)) return fig;

			fig = xyGraph.findFigureAt(pnt.x-xOffset-x, pnt.y-yOffset);
			if (fig!=null && type==null)          return fig;
			if (fig!=null && type.isInstance(fig)) return fig;
		}
		for (int y = 1;y<=5; y++){
			fig = xyGraph.findFigureAt(pnt.x-xOffset, pnt.y-yOffset+y);
			if (fig!=null && type==null)          return fig;
			if (fig!=null && type.isInstance(fig)) return fig;

			fig = xyGraph.findFigureAt(pnt.x-xOffset, pnt.y-yOffset-y);
			if (fig!=null && type==null)          return fig;
			if (fig!=null && type.isInstance(fig)) return fig;
		}

		return null;
	}

	private Point toDisplay(Point point) {
		if (figureCanvas!=null) return figureCanvas.toDisplay(point);
		return point; // TODO is this right?s
	}

	protected void fillAnnotationConfigure(IMenuManager manager,
			final Annotation annotation,
			final IPlottingSystem<T> system) {

		final Action configure = new Action("Configure '"+annotation.getName()+"'", PlottingSystemActivator.getImageDescriptor("icons/Configure.png")) {
			public void run() {
				final XYRegionConfigDialog dialog = new XYRegionConfigDialog(Display.getDefault().getActiveShell(), xyGraph, getSystem().isRescale());
				dialog.setPlottingSystem(system);
				dialog.setSelectedAnnotation(annotation);
				dialog.open();
			}
		};
		manager.add(configure);	

		final Action delAnnotation = new Action("Remove Annotation...", PlottingSystemActivator.getImageDescriptor("icons/Del_Annotation.png")) {
			public void run() {
				RemoveAnnotationDialog dialog = new RemoveAnnotationDialog(Display.getCurrent().getActiveShell(),
						(IXYGraph) xyGraph);
				if(dialog.open() == Window.OK && dialog.getAnnotation() != null){
					xyGraph.removeAnnotation(dialog.getAnnotation());
					xyGraph.getOperationsManager()
					.addCommand(new RemoveAnnotationCommand((IXYGraph) xyGraph, dialog.getAnnotation()));
				}

			}
		};
		manager.add(delAnnotation);	

		manager.add(new Separator("org.dawb.workbench.plotting.system.configure.group"));
	}

	private void fillAxisConfigure(IMenuManager manager, final Axis axis) {
		final Action configure = new Action("Configure '" + (axis.isHorizontal() ? XAXIS_DEFAULT_NAME : YAXIS_DEFAULT_NAME) + "'",
				PlottingSystemActivator.getImageDescriptor("icons/Configure.png")) {
			public void run() {
				final XYRegionConfigDialog dialog = new XYRegionConfigDialog(Display.getDefault().getActiveShell(), xyGraph, getSystem().isRescale());
				dialog.setSelectedAxis(axis);
				dialog.open();
			}
		};
		manager.add(configure);
		manager.add(new Separator("org.dawb.workbench.plotting.system.configure.group"));
	}

	private void fillColorMapConfigure(IMenuManager manager) {
		final ImageTrace imageTrace = getImageTrace();

		if (imageTrace == null) return;

		ImageServiceBean bean = imageTrace.getImageServiceBean();		

		if (bean == null) return;

		if (xyGraph!=null) {
			final Action configure = new Action("Configure Histogramming", PlottingSystemActivator.getImageDescriptor("icons/TraceProperties.png")) {
				public void run() {
					final XYRegionConfigDialog dialog = new XYRegionConfigDialog(Display.getDefault().getActiveShell(), xyGraph, getSystem().isRescale());
					dialog.setPlottingSystem(system);
					dialog.setSelectedTrace(imageTrace);
					dialog.open();
				}
			};
			manager.add(configure);
			manager.add(new Separator());
		}

		boolean log = bean.isLogColorScale();
		final Action logAction = new Action("Log colour scale", IAction.AS_CHECK_BOX) {
			public void run() {
				bean.setLogColorScale(!log);
				if (imageTrace.isRescaleHistogram()) imageTrace.rehistogram();
				PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.CM_LOGSCALE, this.isChecked());
			}
		};

		logAction.setChecked(log);

		final Action invertColorScale = new Action("Invert color scale", IAction.AS_CHECK_BOX) {
			public void run() {
				PlottingSystemActivator.getService(IPaletteService.class).setInverted(this.isChecked());
				Collection<IPaletteTrace> ps = system.getTracesByClass(IPaletteTrace.class);

				if (ps != null && !ps.isEmpty()) {
					for (IPaletteTrace p : ps) {
						p.setPalette(p.getPaletteName());
					}
					PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.CM_INVERTED, this.isChecked());
				}
			}
		};

		invertColorScale.setChecked(PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.CM_INVERTED));


		boolean locked= !imageTrace.isRescaleHistogram();
		final Action lockAction = new Action("Lock", IAction.AS_CHECK_BOX) {
			public void run() {
				imageTrace.setRescaleHistogram(!imageTrace.isRescaleHistogram());
			}
		};
		lockAction.setChecked(locked);

		manager.add(logAction);
		manager.add(invertColorScale);
		manager.add(lockAction);
	}

	/**
	 * 
	 * Problems:
	 * 1. Line trace bounds extend over other line traces so the last line trace added, will
	 * always be the figure that the right click detects.
	 * 
	 * Useful things, visible, annotation, quick set to line or points, open configure page.
	 * 
	 * @param manager
	 * @param trace
	 * @param xyGraph
	 */
	@Override
	public <R> void fillTraceActions(final IContributionManager manager, final ITrace trace, final IPlottingSystem<R> sys) {

		manager.add(new Separator("org.dawb.workbench.plotting.system.trace.start"));

		final String name = trace!=null&&trace.getName()!=null?trace.getName():"";

		if (trace instanceof ILineTrace) { // Does actually work for images but may confuse people.
			final Action visible = new Action("Hide '"+name+"'", PlottingSystemActivator.getImageDescriptor("icons/TraceVisible.png")) {
				public void run() {
					trace.setVisible(false);
				}
			};
			manager.add(visible);

			if (trace instanceof LineTraceImpl) {
				final Action export = new Action("Export '"+name+"' to ascii (dat file)", PlottingSystemActivator.getImageDescriptor("icons/save_edit.png")) {
					public void run() {
						try {
							PlotDataConversionWizard wiz = (PlotDataConversionWizard)EclipseUtils.openWizard(PlotDataConversionWizard.ID, false);
							wiz.setFilePath(lastPath);
							WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
							wd.setTitle(wiz.getWindowTitle());
							wiz.setPlottingSystem(system);
							wd.open();
							lastPath = wiz.getFilePath();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				manager.add(export);
			}
		}

		if (xyGraph!=null) {

			if (SelectionRegionFactory.getStaticBuffer()!=null) {
				final Action pasteRegion = new Action("Paste '"+SelectionRegionFactory.getStaticBuffer().getName()+"'", PlottingSystemActivator.getImageDescriptor("icons/RegionPaste.png")) {
					public void run() {
						AbstractSelectionRegion<?> region = null;
						try {
							region = (AbstractSelectionRegion<?>) sys.createRegion(SelectionRegionFactory.getStaticBuffer().getName(), SelectionRegionFactory.getStaticBuffer().getRegionType());
							region.sync(SelectionRegionFactory.getStaticBuffer().getBean());
						} catch (Exception ne) {
							final String name = RegionUtils.getUniqueName("Region", sys);
							boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Cannot paste '"+SelectionRegionFactory.getStaticBuffer().getName()+"'",
									"A region with the name '"+SelectionRegionFactory.getStaticBuffer().getName()+"' already exists.\n\nWould you like to name the region '"+name+"'?");
							if (ok) {
								try {
									region = (AbstractSelectionRegion<?>) sys.createRegion(name, SelectionRegionFactory.getStaticBuffer().getRegionType());
								} catch (Exception e) {
									logger.error("Cannot create new region.", e);
									return;
								}
							} else {

								return;
							}
						}

						region.setROI(SelectionRegionFactory.getStaticBuffer().getROI().copy());
						sys.addRegion(region);
					}
				};
				manager.add(pasteRegion);
			}

			final Action addAnnotation = new Action("Add annotation to '"+name+"'", PlottingSystemActivator.getImageDescriptor("icons/TraceAnnotation.png")) {
				public void run() {
					final String annotName = AnnotationUtils.getUniqueAnnotation(name+" annotation ", sys);
					if (trace instanceof LineTraceImpl) {
						final LineTraceImpl lt = (LineTraceImpl)trace;
						xyGraph.addAnnotation(new Annotation(annotName, lt.getTrace()));
					} else {
						xyGraph.addAnnotation(new Annotation(annotName, xyGraph.getPrimaryXAxis(), xyGraph.getPrimaryYAxis()));
					}
				}
			};
			manager.add(addAnnotation);
		}

		if (trace instanceof ILineTrace) {
			final ILineTrace lt = (ILineTrace)trace;
			if (lt.getTraceType()!=TraceType.POINT) { // Give them a quick change to points
				final Action changeToPoints = new Action("Plot '"+name+"' as scatter", PlottingSystemActivator.getImageDescriptor("icons/TraceScatter.png")) {
					public void run() {
						lt.setTraceType(TraceType.POINT);
						lt.setPointSize(8);
						lt.setPointStyle(PointStyle.XCROSS);
					}
				};
				manager.add(changeToPoints);
			} else if (lt.getTraceType()!=TraceType.SOLID_LINE) {
				final Action changeToLine = new Action("Plot '"+name+"' as line", PlottingSystemActivator.getImageDescriptor("icons/TraceLine.png")) {
					public void run() {
						lt.setTraceType(TraceType.SOLID_LINE);
						lt.setLineWidth(1);
						lt.setPointSize(1);
						lt.setPointStyle(PointStyle.NONE);
					}
				};
				manager.add(changeToLine);
			}
		}

		if (xyGraph!=null) {
			final Action configure = new Action("Configure '"+name+"'", PlottingSystemActivator.getImageDescriptor("icons/TraceProperties.png")) {
				public void run() {
					final XYRegionConfigDialog dialog = new XYRegionConfigDialog(Display.getDefault().getActiveShell(), xyGraph, getSystem().isRescale());
					dialog.setPlottingSystem(sys);
					dialog.setSelectedTrace(trace);
					dialog.open();
				}
			};
			manager.add(configure);
		}
		manager.add(new Separator("org.dawb.workbench.plotting.system.trace.end"));
	}


	public T getControl() {
		if (figureCanvas!=null) return (T)figureCanvas;
		return (T)content;
	}

	public void setFocus() {
		if (figureCanvas!=null && !figureCanvas.isDisposed()) figureCanvas.setFocus();
		if (content!=null) content.requestFocus();
	}

	public void setTitle(String name) {
		if(xyGraph!=null) {
			xyGraph.setTitle(name);
			xyGraph.repaint();
		}
	}

	@Override
	public void setTitleColor(Color color) {
		if(xyGraph!=null) {
			xyGraph.setTitleColor(color);
		}
	}

	@Override
	public void setBackgroundColor(Color color) {
		if(xyGraph!=null) {
			xyGraph.getRegionArea().setBackgroundColor(color);
		}
	}

	public void clearTraces() {
		if (xyGraph!=null) {
			ImageTrace trace = xyGraph.getRegionArea().getImageTrace();
			if(trace != null)
				trace.removePaletteListener(paletteListener);
			xyGraph.clearTraces();
		}
	}


	protected IImageTrace createImageTrace(String traceName) {
		final ImageTrace trace = xyGraph.createImageTrace(traceName, intensity);
		trace.setPlottingSystem(system);
		trace.addPaletteListener(paletteListener);
		return trace;
	}

	private IPaletteListener paletteListener = new IPaletteListener.Stub() {
		@Override
		public void rescaleHistogramChanged(PaletteEvent evt) {
			boolean locked = !((IPaletteTrace)evt.getSource()).isRescaleHistogram();
			plotActionsCreator.getHistoLock().setChecked(locked);
		}
	};

	public boolean isTraceTypeSupported(Class<? extends ITrace> clazz) {
		if (ILineTrace.class.isAssignableFrom(clazz)) {
			return true;
		} else if (IVectorTrace.class.isAssignableFrom(clazz)) {
			return true;
		} else if (IImageTrace.class.isAssignableFrom(clazz)) {
			return true;
		} else if (IImageStackTrace.class.isAssignableFrom(clazz)) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public <U extends ITrace> U createTrace(String name, Class<? extends ITrace> clazz) {
		if (ILineTrace.class.isAssignableFrom(clazz)) {
			return (U) createLineTrace(name);
		} else if (IVectorTrace.class.isAssignableFrom(clazz)) {
			return (U) createVectorTrace(name);
		} else if (IImageTrace.class.isAssignableFrom(clazz)) {
			return (U) createImageTrace(name);
		} else if (IImageStackTrace.class.isAssignableFrom(clazz)) {
			return (U) createImageStackTrace(name);
		} else {
			throw new RuntimeException("Trace type not supported "+clazz.getSimpleName());
		}
	}

	protected ILineTrace createLineTrace(String traceName) {
		final LineTrace   trace    = new LineTrace(traceName);
		final LineTraceImpl wrapper = new LineTraceImpl(getSystem(), trace);
		system.getLineTracePreferences().applyPreferences(wrapper);
		return wrapper;
	}

	protected IVectorTrace createVectorTrace(String traceName) {
		final VectorTrace trace    = new VectorTrace(traceName);
		return trace;
	}

	protected IImageStackTrace createImageStackTrace(String traceName) {
		final ImageStackTrace trace = xyGraph.createImageStackTrace(traceName, intensity);
		trace.setPlottingSystem(system);
		return trace;
	}

	private List<IDataset> getLineTracesData() {
		Collection<ILineTrace> traces = system.getTracesByClass(ILineTrace.class);
		List<IDataset> data = new ArrayList<>();
		for (ILineTrace line : traces) {
			data.add(line.getData());
		}
		return data;
	}

	/**
	 * Internal usage only
	 * 
	 * @param title
	 * @param x
	 * @param ys
	 * @param traceMap, may be null
	 * @return
	 */
	@Override
	public List<ILineTrace> createLineTraces(final String                title, 
			final IDataset              x, 
			final List<? extends IDataset> ys,
			final List<String>          dataNames,
			final Map<String,ITrace>    traceMap,
			final Map<Object, Color>    colorMap,
			final IProgressMonitor      monitor) {

		final String rootName = system.getRootName();

		final IAxis xAxis = getSelectedXAxis();
		xAxis.setLabelDataAndTitle(null);
		final IAxis yAxis = getSelectedYAxis();
		yAxis.setLabelDataAndTitle(null);

		xAxis.setVisible(true);
		yAxis.setVisible(true);

		if (title==null) {
			setTitle(getTitle(x, ys, getLineTracesData(), true, rootName));
		} else {
			setTitle(title);
		}
		String n = getName(x,rootName);
		// only override if axis dataset has name
		if (n != null && n.trim().length() > 0) {
			xAxis.setTitle(n);
		}

		//create a trace data provider, which will provide the data to the trace.
		int iplot = 0;

		final List<ILineTrace> traces = new ArrayList<ILineTrace>(ys.size());
		for (int i = 0; i < ys.size(); i++) {

			IDataset y = ys.get(i);
			if (y==null) continue;
			// set yaxis name
			String dataName = dataNames!=null ? dataNames.get(i) : null;
			if (dataName == null) {
				dataName = y.getName();
			}
			if (dataName == null || dataName.isEmpty()) {
				dataName = YAXIS_DEFAULT_NAME;
			}
			yAxis.setTitle(dataName);
			final LineTrace trace = new LineTrace(getName(y,rootName));
			LineTraceImpl wrapper = new LineTraceImpl(system, trace);
			wrapper.setDataName(dataName);
			traces.add(wrapper);

			final TraceWillPlotEvent evt = new TraceWillPlotEvent(wrapper, x, y);
			system.fireWillPlot(evt);
			if (!evt.doit) continue;

			LightWeightDataProvider traceDataProvider = new LightWeightDataProvider(evt.getXData(), evt.getYData());

			//create the trace
			trace.init((Axis) xAxis, (Axis) yAxis, traceDataProvider);

			if (y.getName()!=null && !"".equals(y.getName())) {
				if (traceMap!=null) traceMap.put(y.getName(), wrapper);
				trace.setInternalName(y.getName());
			}

			system.getLineTracePreferences().applyPreferences(wrapper);
			if (traceDataProvider.hasErrors()) {
				trace.setErrorBarColor(ColorConstants.red);
			}

			//set trace property
			int index = system.getTraces().size()+iplot-1;
			if (index<0) index=0;
			final Color plotColor = ColorUtility.getSwtColour(colorMap!=null?colorMap.values():null, index);
			//			final Color plotColor = ColorUtility.getSwtColour(null, iplot);
			if (colorMap!=null) {
				if (system.getColorOption()==ColorOption.BY_NAME) {
					colorMap.put(y.getName(),plotColor);
				} else {
					colorMap.put(y,          plotColor);
				}
			}
			trace.setTraceColor(plotColor);

			//add the trace to xyGraph
			xyGraph.addTrace(trace, (Axis) xAxis, (Axis) yAxis, false);


			if (monitor!=null) monitor.worked(1);
			iplot++;
		}

		redraw();

		if (system.isRescale()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					system.autoscaleAxes();
				}
			});
		}
		return traces;
	}

	private boolean showIntensity = PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.SHOW_INTENSITY);

	public boolean addTrace(ITrace trace) {

		if (trace instanceof IImageTrace) {
			system.setPlotType(PlotType.IMAGE); // Only one image allowed at a time
		} else if (!(trace instanceof IVectorTrace)){
			system.setPlotType(PlotType.XY);
		}

		final TraceWillPlotEvent evt = new TraceWillPlotEvent(trace, true);
		system.fireWillPlot(evt);
		if (!evt.doit) return false;

		final IAxis xAxis = getSelectedXAxis();
		final IAxis yAxis = getSelectedYAxis();
		if (trace instanceof IImageTrace) {
			final IImageTrace image = (IImageTrace) trace;
			xAxis.setLog10(false);
			yAxis.setLog10(false);
			trace.initialize(xAxis, yAxis);
			for (IRegion r : xyGraph.getRegions()) {
				r.getCoordinateSystem().update(image, xAxis, yAxis);
			}

			boolean autoscale = image.getGlobalRange()!= null && !system.isRescale() ? false : true;

			if (!image.setData(evt.getImage(), evt.getAxes(), autoscale)) return false; // But not plotted

			xyGraph.addImageTrace((ImageTrace)image);
			removeAdditionalAxes(); // Do not have others with images.

			if (DTypeUtils.getDType(trace.getData()) == Dataset.RGB) {
				intensity.setVisible(false);
			} else {
				intensity.setVisible(showIntensity);
			}

			// If we are a stack, show the scale for iterating images.
			if (image instanceof IImageStackTrace) {
				IImageStackTrace stack = (IImageStackTrace) image;
				if (stack.getStackSize()>1) {
					setFolderScaleVisible(true);
					folderScale.setRange(0, stack.getStackSize()-1);
					folderScale.setValue(stack.getStackIndex());
				} else {
					setFolderScaleVisible(false);
				}
			} else {
				setFolderScaleVisible(false);
			}
		} else if (trace instanceof IVectorTrace) {
			trace.initialize(xAxis, yAxis);
			final IVectorTrace vector = (IVectorTrace)trace;
			xyGraph.addVectorTrace((VectorTrace)vector);
			vector.setVisible(true);
		} else {
			xAxis.setLabelDataAndTitle(null);
			yAxis.setLabelDataAndTitle(null);
			trace.initialize(xAxis, yAxis);
			xyGraph.addTrace(((LineTraceImpl) trace).getTrace(), (Axis) xAxis, (Axis) yAxis, true);
			intensity.setVisible(false);
		}
		redraw();

		return true;
	}

	public void removeTrace(ITrace trace) {
		if (trace instanceof LineTraceImpl) {
			xyGraph.removeTrace(((LineTraceImpl) trace).getTrace());
		} else if (trace instanceof ImageTrace) {
			((ImageTrace)trace).removePaletteListener(paletteListener);
			xyGraph.removeImageTrace((ImageTrace) trace);
		}else if (trace instanceof VectorTrace) {
			xyGraph.removeVectorTrace((VectorTrace) trace);
		}

		redraw();
	}

	public void setShowLegend(boolean b) {
		if (xyGraph!=null) {
			xyGraph.setShowLegend(b);
			redraw();
		}
	}

	public void reset(boolean force) {
		if (xyGraph!=null) {
			try {
				clearAnnotations();
				clearRegions(force);
				resetAxes();
				clearTraces();
				setTitle("");
			} catch (Throwable e) {
				throw new RuntimeException(e); // We cannot deal with it here.
			}
		}
	}

	public void resetAxes() {
		if (xyGraph == null)
			return;
		removeAdditionalAxes();
		xyGraph.getPrimaryXAxis().setTitle(IXYGraph.X_AXIS);
		xyGraph.getPrimaryYAxis().setTitle(IXYGraph.Y_AXIS);
	}

	protected void removeAdditionalAxes() {
		xyGraph.setSelectedXAxis((IAxis) xyGraph.getPrimaryXAxis());
		xyGraph.setSelectedYAxis((IAxis) xyGraph.getPrimaryYAxis());
		for (Axis axis : xyGraph.getAxisList()) {
			if (axis!=xyGraph.getPrimaryXAxis() && axis!=xyGraph.getPrimaryYAxis()) {
				axis.setVisible(false); 
				removeAxis((IAxis) axis);
			}
		}
	}

	@Override
	public IAnnotation createAnnotation(final String name) throws Exception {

		final List<Annotation> anns = xyGraph.getPlotArea().getAnnotationList();
		for (Annotation annotation : anns) {
			if (annotation.getName() != null && annotation.getName().equals(name)) {
				throw new Exception("The annotation name '" + name + "' is already taken.");
			}
		}

		final Axis xAxis = (Axis) getSelectedXAxis();
		final Axis yAxis = (Axis) getSelectedYAxis();

		return new AnnotationWrapper(name, xAxis, yAxis);
	}

	@Override
	public void addAnnotation(final IAnnotation annotation) {
		final AnnotationWrapper wrapper = (AnnotationWrapper) annotation;
		xyGraph.addAnnotation(wrapper.getAnnotation());
		xyGraph.getOperationsManager()
		.addCommand(new AddAnnotationCommand((IXYGraph) xyGraph, wrapper.getAnnotation()));
	}

	@Override
	public void removeAnnotation(final IAnnotation annotation) {
		final AnnotationWrapper wrapper = (AnnotationWrapper) annotation;
		xyGraph.removeAnnotation(wrapper.getAnnotation());
		xyGraph.getOperationsManager()
		.addCommand(new RemoveAnnotationCommand((IXYGraph) xyGraph, wrapper.getAnnotation()));
	}

	@Override
	public void renameAnnotation(final IAnnotation annotation, String name) {
		final AnnotationWrapper wrapper = (AnnotationWrapper) annotation;
		wrapper.getAnnotation().setName(name);
	}

	public void clearAnnotations(){
		final List<Annotation>anns = new ArrayList<Annotation>(xyGraph.getPlotArea().getAnnotationList());
		for (Annotation annotation : anns) {
			if (annotation==null) continue;
			xyGraph.getPlotArea().removeAnnotation(annotation);
		}
	}

	@Override
	public IAnnotation getAnnotation(final String name) {
		final List<Annotation> anns = xyGraph.getPlotArea().getAnnotationList();
		for (Annotation annotation : anns) {
			if (annotation.getName() != null && annotation.getName().equals(name)) {
				return new AnnotationWrapper(annotation);
			}
		}
		return null;
	}

	@Override
	public void autoscaleAxes() {
		if (xyGraph==null) return;
		xyGraph.performAutoScale();
		xyGraph.performAutoScale();
	}

	public boolean addRegionListener(final IRegionListener l) {
		return xyGraph.addRegionListener(l);
	}

	public boolean removeRegionListener(final IRegionListener l) {
		if (xyGraph==null) return false;
		return xyGraph.removeRegionListener(l);
	}

	/**
	 * Throws exception if region exists already.
	 * @throws Exception 
	 */
	public IRegion createRegion(final String name, final RegionType regionType) throws Exception  {

		final IAxis xAxis = getSelectedXAxis();
		final IAxis yAxis = getSelectedYAxis();

		IRegion region = xyGraph.createRegion(name, xAxis, yAxis, regionType, true);
		// set the plot type in which the region was created
		region.setPlotType(system.getPlotType());
		return region;
	}

	public void clearRegions() {
		clearRegions(false);
	}

	/**
	 * Thread safe
	 */
	public void clearRegions(boolean force) {
		if (xyGraph==null) return;

		xyGraph.clearRegions(force);
	}		

	public void clearRegionTool() {
		if (xyGraph==null) return;

		xyGraph.clearRegionTool();
	}

	/**
	 * Add a selection region to the graph.
	 * @param region
	 */
	public void addRegion(final IRegion region) {
		final AbstractSelectionRegion<?> r = (AbstractSelectionRegion<?>) region;
		if (xyGraph!=null) xyGraph.addRegion(r);
	}

	/**
	 * Remove a selection region to the graph.
	 * @param region
	 */
	public void removeRegion(final IRegion region) {
		final AbstractSelectionRegion<?> r = (AbstractSelectionRegion<?>) region;
		if (xyGraph!=null) xyGraph.removeRegion(r);
	}

	@Override
	public void renameRegion(final IRegion region, String name) {
		if (xyGraph == null) return;
		if (xyGraph != null) xyGraph.renameRegion((AbstractSelectionRegion<?>) region, name);
	}

	/**
	 * Get a region by name.
	 * @param name
	 * @return
	 */
	public IRegion getRegion(final String name) {
		if (xyGraph == null) return null;
		return xyGraph.getRegion(name);
	}

	@Override
	public Collection<IRegion> getRegions(final RegionType type) {

		final Collection<IRegion> regions = getRegions();
		if (regions==null) return null;

		final Collection<IRegion> ret= new ArrayList<IRegion>();
		for (IRegion region : regions) {
			if (region.getRegionType()==type) {
				ret.add(region);
			}
		}

		return ret; // may be empty
	}

	/**
	 * Get regions
	 * @param name
	 * @return
	 */
	public Collection<IRegion> getRegions() {
		if (xyGraph == null) return null;
		return xyGraph.getRegions();
	}

	/**
	 * Use this method to create axes other than the default y and x axes.
	 * 
	 * @param title
	 * @param isYAxis, normally it is.
	 * @param side - either SWT.LEFT, SWT.RIGHT, SWT.TOP, SWT.BOTTOM
	 * @return
	 */
	@Override
	public IAxis createAxis(final String title, final boolean isYAxis, int side) {

		AspectAxis axis = new AspectAxis(title, isYAxis);
		if (side==SWT.LEFT||side==SWT.BOTTOM) {
			axis.setTickLabelSide(LabelSide.Primary);
		} else {
			axis.setTickLabelSide(LabelSide.Secondary);
		}
		axis.setAutoScaleThreshold(0.1);
		axis.setShowMajorGrid(true);
		axis.setShowMinorGrid(true);	
		xyGraph.addAxis(axis);

		return axis;
	}	

	@Override
	public IAxis removeAxis(final IAxis axis) {
		if (axis.isPrimaryAxis()) return null;
		if (!(axis instanceof Axis)) return null;
		xyGraph.removeAxis((Axis) axis);
		return axis;
	}	

	@Override
	public List<IAxis> getAxes() {

		List<Axis> axes = xyGraph.getAxisList();
		List<IAxis> ret = new ArrayList<IAxis>(axes.size());
		for (Axis axis : axes) {
			if (!(axis instanceof IAxis)) continue;
			ret.add((IAxis)axis);
		}
		return ret;
	}

	@Override
	public IAxis getAxis(String title) {

		List<Axis> axes = xyGraph.getAxisList();
		for (Axis axis : axes) {
			if (!(axis instanceof IAxis)) continue;
			if (axis.getTitle().equals(title)) return (IAxis)axis;
		}
		return null;
	}


	@Override
	public IAxis getSelectedXAxis() {
		if (xyGraph==null) return null;
		return xyGraph.getSelectedXAxis();
	}

	@Override
	public void setSelectedXAxis(IAxis selectedXAxis) {
		xyGraph.setSelectedXAxis(selectedXAxis);
	}

	@Override
	public IAxis getSelectedYAxis() {
		if (xyGraph==null) return null;
		return xyGraph.getSelectedYAxis();
	}

	@Override
	public void setSelectedYAxis(IAxis selectedYAxis) {
		xyGraph.setSelectedYAxis(selectedYAxis);
	}

	public Image getImage(Rectangle size) {
		if (xyGraph==null) return null;
		return xyGraph.getImage(size);
	}

	@Override
	public void addPositionListener(IPositionListener l) {
		if (xyGraph==null || xyGraph.getRegionArea()==null) return;
		xyGraph.getRegionArea().addPositionListener(l);
	}

	@Override
	public void removePositionListener(IPositionListener l) {
		if (xyGraph==null || xyGraph.getRegionArea()==null) return;
		xyGraph.getRegionArea().removePositionListener(l);
	}

	@Override
	public void addClickListener(IClickListener l) {
		if (xyGraph==null || xyGraph.getRegionArea()==null) return;
		xyGraph.getRegionArea().addClickListener(l);
	}

	@Override
	public void removeClickListener(IClickListener l) {
		if (xyGraph==null || xyGraph.getRegionArea()==null) return;
		xyGraph.getRegionArea().removeClickListener(l);
	}


	public void dispose() {
		if (plotActionsCreator!=null) {
			plotActionsCreator.dispose();
		}
		if (xyGraph!=null) {
			xyGraph.dispose();
			xyGraph = null;
		}
		if (figureCanvas!=null && !figureCanvas.isDisposed()) {
			figureCanvas.removeMouseWheelListener(getMouseWheelListener());
			figureCanvas.removeKeyListener(getKeyListener());
			figureCanvas.dispose();
		}
	}

	/**
	 * Thread safe!
	 * @param autoScale
	 */
	public void repaint(final boolean autoScale) {
		if (Display.getDefault().getThread()==Thread.currentThread()) {
			repaintInternal(autoScale);
		} else {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					repaintInternal(autoScale);
				}
			});
		}
	}
	private void repaintInternal(final boolean autoScale) {
		if (figureCanvas!=null) {
			if (autoScale){
				xyGraph.performAutoScale();
				xyGraph.performAutoScale();
			}
			figureCanvas.layout(figureCanvas.getChildren());
		}
		if (xyGraph != null) {
			xyGraph.revalidate();
			xyGraph.repaint();
		}
	}

	/**
	 * NOTE This listener is *not* notified once for each configuration setting made on 
	 * the configuration but once whenever the form is applied by the user (and many things
	 * are changed) 
	 * 
	 * You then have to read the property you require from the object (for instance the axis
	 * format) in case it has changed. This is not ideal, later there may be more events fired and
	 * it will be possible to check property name, for now it is always set to "Graph Configuration".
	 * 
	 * @param listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		xyGraph.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		xyGraph.removePropertyChangeListener(listener);
	}


	// Print / Export methods
	private PrintSettings settings;

	@Override
	public void printPlotting(){
		if (settings==null) settings = new PrintSettings();
		final IPrintImageProvider prov = new IPrintImageProvider() {
			@Override
			public Image getImage(Rectangle size) {
				return xyGraph.getImage(size);
			}
			@Override
			public Rectangle getBounds() {
				org.eclipse.draw2d.geometry.Rectangle rect = xyGraph.getBounds();
				return new Rectangle(rect.x, rect.y, rect.width, rect.height);
			}			
		};
		PlotPrintPreviewDialog dialog = new PlotPrintPreviewDialog(prov, Display.getDefault(), settings);
		settings=dialog.open();
	}

	/**
	 * Print scaled plotting to printer
	 */
	public void printScaledPlotting(){

		PrintDialog dialog      = new PrintDialog(Display.getDefault().getActiveShell(), SWT.NULL);
		PrinterData printerData = dialog.open();
		// TODO There are options on PrintFigureOperation
		if (printerData != null) {
			final PrintFigureOperation op = new PrintFigureOperation(new Printer(printerData), xyGraph);
			op.run("Print "+xyGraph.getTitle());
		}
	}

	@Override
	public void copyPlotting(){
		PlotExportPrintUtil.copyGraph(xyGraph.getImage());
	}

	@Override
	public String savePlotting(String filename) throws Exception {
		FileDialog dialog = new FileDialog (Display.getDefault().getActiveShell(), SWT.SAVE);
		String [] filterExtensions = new String [] {"*.png;*.PNG;*.jpg;*.JPG;*.jpeg;*.JPEG", "*.ps;*.eps", "*.svg;*.SVG"};
		if (filename!=null) {
			dialog.setFilterPath((new File(filename)).getParent());
		} else {
			String filterPath = "/";
			String platform = SWT.getPlatform();
			if (platform.equals("win32") || platform.equals("wpf")) {
				filterPath = "c:\\";
			}
			dialog.setFilterPath (filterPath);
		}
		dialog.setFilterNames (PlotExportPrintUtil.FILE_TYPES);
		dialog.setFilterExtensions (filterExtensions);
		filename = dialog.open();
		if (filename == null)
			return null;
		try {
			final File file = new File(filename);
			if (file.exists()) {
				boolean yes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Confirm Overwrite", "The file '"+file.getName()+"' exists.\n\nWould you like to overwrite it?");
				if (!yes) return filename;
			}

			PlotExportPrintUtil.saveGraph(filename, PlotExportPrintUtil.FILE_TYPES[dialog.getFilterIndex()], plotContents.getParent());
			//logger.debug("Plot saved");
		} catch (Exception e) {
			throw e;
		}
		return filename;
	}

	@Override
	public void savePlotting(String filename, String filetype)  throws Exception{
		if (filename == null)
			return;
		try {
			PlotExportPrintUtil.saveGraph(filename, filetype, xyGraph.getParent());
			logger.debug("Plotting saved");
		} catch (Exception e) {
			throw e;
		}
	}

	protected XYRegionGraph getXYRegionGraph() {
		return xyGraph;
	}

	AbstractPlottingSystem<T> getSystem() {
		return system;
	}

	public void setXFirst(boolean xfirst) {
		this.plotActionsCreator.setXfirstButtons(xfirst);
	}

	public void setRescale(boolean rescale) {
		this.plotActionsCreator.setRescaleButton(rescale);
	}

	public void setOrigin(ImageOrigin origin) {
		this.plotActionsCreator.setImageOrigin(origin);
	}

	public String getTitle() {
		return xyGraph.getTitle();
	}

	public void setDefaultCursor(int cursorType) {
		Cursor cursor = Cursors.ARROW;
		if (cursorType == IPlottingSystem.CROSS_CURSOR) cursor = Cursors.CROSS;

		if (xyGraph==null || xyGraph.getRegionArea()==null) return;
		xyGraph.getRegionArea().setCursor(cursor);
		ZoomType.NONE.setCursor(cursor);
	}

	/**
	 * Also clears any region or zoom tools assuming that this
	 * special cursor is to do with a custom drawing mode.
	 * For instance masking.
	 * @param cursor
	 */
	public void setSelectedCursor(Cursor cursor) {
		xyGraph.getRegionArea().setSelectedCursor(cursor);
	}

	public void updatePlottingRole(PlotType type) {
		intensity.setVisible(type == null ? false : (type.is2D() && showIntensity));
	}

	public boolean isShowIntensity() {
		return showIntensity;
	}

	public void setShowIntensity(boolean checked) {
		showIntensity = checked;
		intensity.setVisible(checked);
	}

	public void setKeepAspect(boolean checked){
		xyGraph.setKeepAspect(checked);
	}

	public void setShiftPoint(org.eclipse.draw2d.geometry.Point point) {
		xyGraph.getRegionArea().setShiftPoint(point);
	}

	public org.eclipse.draw2d.geometry.Point getShiftPoint() {
		return xyGraph.getRegionArea().getShiftPoint();
	}

	public void setEnabled(boolean enabled) {
		xyGraph.setEnabled(enabled);
		if (enabled) {
			xyGraph.setBackgroundColor(ColorConstants.white);
			xyGraph.setCursor(null);
			xyGraph.getRegionArea().setBackgroundColor(ColorConstants.white);
			xyGraph.getRegionArea().setCursor(null);
		} else {
			xyGraph.setBackgroundColor(ColorConstants.lightGray);
			xyGraph.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT));
			xyGraph.getRegionArea().setBackgroundColor(ColorConstants.lightGray);
			xyGraph.getRegionArea().setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT));
		}
	}

	public boolean isEnabled() {
		return xyGraph.isEnabled();
	}

	public static String getTitle(final IDataset xIn, final List<IDataset> ysIn, final boolean isFileName) {
		return getTitle(xIn, ysIn, null, isFileName, null);
	}

	@SuppressWarnings("unchecked")
	public static String getTitle(final IDataset xIn, 
			final List<? extends IDataset> ysIn, 
			final List<? extends IDataset> list,
			final boolean isFileName, final String  rootName) {

		final IDataset       x;
		final List<IDataset> ys;
		if (ysIn==null) {
			ys = new ArrayList<IDataset>(1);
			ys.add(xIn);
			x = DatasetFactory.createRange(DoubleDataset.class, ys.get(0).getSize());
			x.setName("Index of "+xIn.getName());
		} else {
			x  = xIn;
			ys = (List<IDataset>) ysIn;
		}

		final String[] names = new String[2]; // store first and last name
		final Pattern pattern = isFileName ? Pattern.compile("(.*) \\(.*\\)") : null;
		int total = ys.size();
		int i = 0; // number of names contributing to title
		if (list != null) {
			total += list.size();
			i = getTitleNames(list, rootName, names, pattern, i);
		}
		i = getTitleNames(ys, rootName, names, pattern, i);

		final StringBuilder buf = new StringBuilder();
		buf.append("Plot of ");
		buf.append(names[0]);
		if (i > 0 && total > 1) {
			buf.append(total == 2 ? "," : "...");
			buf.append(names[1]);
		}
		buf.append(" against ");
		buf.append((x != null && x.getName() != null && !x.getName().isEmpty()) ? x.getName() : XAXIS_DEFAULT_NAME);
		return buf.toString();
	}

	private static int getTitleNames(final List<? extends IDataset> list, final String rootName, final String[] names,
			final Pattern pattern, int i) {
		for (IDataset dataset : list) {
			String name = getName(dataset, rootName);
			if (name == null) {
				continue;
			}
			if (pattern != null) {
				// Strip off file name
				final Matcher matcher = pattern.matcher(name);
				if (matcher.matches()) name = matcher.group(1);
			}
			if (i == 0) {
				names[i++] = name;
			} else if (!name.equals(names[0])) {
				names[1] = name; // overwrite second name
			}
		}
		return i;
	}

	/**
	 * 
	 * @param x
	 * @param rootName
	 * @return
	 */
	public static String getName(IDataset x, String rootName) {
		if (x==null) return null;
		String name = x.getName();
		if (rootName != null && name != null) {
			int l = rootName.length();
			if (name.length() > l) {
				name = name.substring(l);
			}
		}
		return name;
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == XYRegionGraph.class) {
			return getXYRegionGraph();
		} 
		return null;
	}

	public Cursor getSelectedCursor() {
		return getXYRegionGraph().getRegionArea().getSelectedCursor();
	}

	public void addMouseMotionListener(MouseMotionListener mml) {
		getXYRegionGraph().getRegionArea().addAuxilliaryMotionListener(mml);
	}

	public void addMouseClickListener(MouseListener mcl) {
		getXYRegionGraph().getRegionArea().addAuxilliaryClickListener(mcl);
	}

	public void removeMouseMotionListener(MouseMotionListener mml) {
		getXYRegionGraph().getRegionArea().removeAuxilliaryMotionListener(mml);
	}

	/**
	 * Please override for draw2d listeners.
	 * @deprecated draw2d Specific
	 */
	public void removeMouseClickListener(MouseListener mcl) {
		if (getXYRegionGraph()                ==null) return;
		if (getXYRegionGraph().getRegionArea()==null) return;
		getXYRegionGraph().getRegionArea().removeAuxilliaryClickListener(mcl);
	}

	@Override
	public Collection<Class<? extends ITrace>> getSupportTraceTypes() {
		return new ArrayList<>(SUPPORTED_TRACES);
	}

	private ImageTrace getImageTrace() {
		PlotArea plotArea = xyGraph.getPlotArea();
		if (plotArea instanceof RegionArea) {
			ImageTrace image = ((RegionArea)plotArea).getImageTrace();
			return image;
		}

		return null;
	}

	class ColorMapMouseListener extends MouseMotionListener.Stub implements MouseListener {

		private org.eclipse.draw2d.geometry.Point start;
		private boolean armed;
		double startValue = 0;

		@Override
		public void mousePressed(org.eclipse.draw2d.MouseEvent me) {

			start = me.getLocation().getCopy();
			startValue = colorMapAxis.getPositionValue(start.y-intensity.getLocation().y, false);
			armed = true;

		}

		@Override
		public void mouseReleased(org.eclipse.draw2d.MouseEvent me) {
			armed = false;

		}

		@Override
		public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent me) {
			//do nothing
		}

		@Override
		public void mouseDragged(org.eclipse.draw2d.MouseEvent me) {
			if (!armed) return;

			ImageTrace image = getImageTrace();
			if (image == null) return;

			ImageServiceBean bean = image.getImageServiceBean();
			if (bean == null) return;

			org.eclipse.draw2d.geometry.Point location = me.getLocation();
			double currentValue = colorMapAxis.getPositionValue(location.y-intensity.getLocation().y, false);
			double offset = startValue-currentValue;

			double max = bean.getMax().doubleValue();
			double min = bean.getMin().doubleValue();

			image.getImageServiceBean().setMin(min+offset);
			image.setMin(min+offset);
			image.getImageServiceBean().setMax(max+offset);
			image.setMax(max+offset);
			image.setPaletteData(image.getPaletteData());

		}

	}
}
