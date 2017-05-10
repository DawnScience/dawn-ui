/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.util.DisplayUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.dawnsci.plotting.PlottingActionBarManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.RMIServerProvider;
import org.eclipse.dawnsci.macro.api.IMacroService;
import org.eclipse.dawnsci.macro.api.MacroEventObject;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.IPrintablePlotting;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.axis.IPositionListener;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.remote.RemotePlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.dawnsci.plotting.api.trace.IImage3DTrace;
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
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of IPlottingSystem<Composite>, not designed to be public.
 *
 * THIS CLASS SHOULD NOT BE USED OUTSIDE THIS PLUGIN!
 *
 * THIS CLASS IS plugin private, do not export org.dawb.workbench.plotting.system from this plugin.
 *
 * @author gerring
 *
 */
public class PlottingSystemImpl<T> extends AbstractPlottingSystem<T> {

	private Logger logger = LoggerFactory.getLogger(PlottingSystemImpl.class);

	private static IMacroService mservice;
	public void setMacroService(IMacroService s) {
		mservice = s;
	}

	private T              parent;
	private StackLayout    stackLayout;

	private PlotActionsManagerImpl       actionBarManager;

	private List<IPlottingSystemViewer<T>>  viewers;
	private IPlottingSystemViewer<T>        activeViewer;

	/**
	 * Boolean to set if the intensity value labels should be shown at high zoom.
	 */
	private boolean showValueLabels = true;

	public PlottingSystemImpl() {

		super();
		PlottingSystemActivator.getPlottingPreferenceStore().setDefault("org.dawnsci.plotting.showValueLabels", true);
		showValueLabels = PlottingSystemActivator.getPlottingPreferenceStore().getBoolean("org.dawnsci.plotting.showValueLabels");

		this.actionBarManager     = (PlotActionsManagerImpl)super.actionBarManager;
		viewers = createViewerList();

		for (IPlottingSystemViewer<T> v : viewers) {
			if (v instanceof LightWeightPlotViewer) {
				activeViewer      = v;
				break;
			}
		}
	}

	public boolean isShowValueLabels() {
		return showValueLabels;
	}

	public void setShowValueLabels(boolean showValueLabels) {
		this.showValueLabels = showValueLabels;
		PlottingSystemActivator.getPlottingPreferenceStore().setValue("org.dawnsci.plotting.showValueLabels", showValueLabels);
	}

	private List<IPlottingSystemViewer<T>> createViewerList() {

		IConfigurationElement[] es = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.dawnsci.plotting.api.plottingViewer");
        if (es == null || es.length <1) throw new RuntimeException("There are no plot viewers defined!");

        List<IPlottingSystemViewer<T>>  viewers = new ArrayList<IPlottingSystemViewer<T>>(es.length);
        for (IConfigurationElement ie : es) {
        	try {
				viewers.add((IPlottingSystemViewer<T>)ie.createExecutableExtension("class"));
			} catch (CoreException e) {
				// It is possible for DAWN to work without all these extension points.
				logger.error("Cannot make class "+ie.getAttribute("class")+" It will not be available to DAWN", e);
				continue;
			}
		}
		return viewers;
	}

	private boolean containerOverride = false;

	@Override
	public void createPlotPart(final T              container,
							   final String         plotName,
							   final IActionBars    bars,
							   final PlotType       hint,
							   final IWorkbenchPart part) {

		super.createPlotPart(container, plotName, bars, hint, part);
		this.plottingMode = hint;
		if (container instanceof IFigure) {
			createFigurePlotPart((IFigure)container, plotName, bars, hint, part);
		} else if (container instanceof Composite) {
			createCompositePlotPart((Composite)container, plotName, bars, hint, part);
		} else {
			throw new IllegalArgumentException("Cannot deal with plots of type "+container.getClass());
		}
		// We make the viewerless plotting system before the viewer so that
		// any macro listener can import numpy.
		try {
			RMIServerProvider.getInstance().exportAndRegisterObject(
					IPlottingSystem.RMI_PREFIX + plotName,
					new RemotePlottingSystem(this));
		} catch (Exception e) {
			logger.error("Unable to register plotting system " + plotName, e);
		}

		if (mservice != null) {
			mservice.publish(new MacroEventObject(this));
		}
		DisplayUtils.asyncExec(new Runnable() {
			@Override
			public void run() {
				PlottingFactory.notityPlottingSystemCreated(plotName, PlottingSystemImpl.this);
			}
		});
	}

	private void createFigurePlotPart(final IFigure container,
			final String plotName, final IActionBars bars, final PlotType hint,
			final IWorkbenchPart part) {
		this.parent = (T)container;
		createViewer(PlotType.XY);
	}

	private void createCompositePlotPart(final Composite container,
			final String plotName, final IActionBars bars, final PlotType hint,
			final IWorkbenchPart part) {
		if (container.getLayout() instanceof GridLayout) {
			GridUtils.removeMargins(container);
		}
		Composite cparent;
		if (container.getLayout() instanceof PageBook.PageBookLayout) {
			if (hint.is3D()) throw new RuntimeException("Cannot deal with "+PageBook.PageBookLayout.class.getName()+" and 3D at the moment!");
			cparent = container;
			logger.debug("Cannot deal with " + PageBook.PageBookLayout.class.getName() + " and 3D at the moment!");
		} else {
			this.containerOverride = true;
			cparent = new Composite(container, SWT.NONE);
			this.stackLayout = new StackLayout();
			cparent.setLayout(stackLayout);
			Color colorBgd = container.getBackground();
			cparent.setBackground(colorBgd != null? colorBgd : Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		}
		this.parent = (T)cparent;

		// We ignore hint, we create a light weight plot as default because
		// it looks nice. We swap this for a 3D one if required.
		IPlottingSystemViewer<T> lightWeightViewer = createViewer(PlotType.XY);
		if (lightWeightViewer!=null && cparent.getLayout() instanceof StackLayout) {
			final StackLayout layout = (StackLayout)cparent.getLayout();
			layout.topControl = (Composite)lightWeightViewer.getControl();
			container.layout();
		}

	}

	@Override
	public Control setControl(Control alternative, boolean showPlotToolbar) {

		if (!(parent instanceof Composite)) throw new IllegalArgumentException("Cannot call setControl on plotting of canvas type "+parent.getClass());
		if (stackLayout==null) throw new IllegalArgumentException("The plotting system is not in StackLayout mode and cannot show alternative controls!");
		Control previous = stackLayout.topControl;
		stackLayout.topControl = alternative;

		Control toolBar = ((ToolBarManager)getActionBars().getToolBarManager()).getControl();
		if (toolBar.getLayoutData() instanceof GridData) { // It is our toolbar
			Control toolbarControl = toolBar.getParent();
			if (toolbarControl.getLayoutData() instanceof GridData) {
				GridUtils.setVisible(toolbarControl, showPlotToolbar);
				toolbarControl.getParent().layout(new Control[]{toolbarControl});
			}
		}
		layout();
		return previous;
	}


	private void layout() {
		if (parent instanceof Composite) ((Composite)parent).layout();
		if (parent instanceof Figure) ((Figure)parent).revalidate();
	}

	@Override
	protected PlottingActionBarManager createActionBarManager() {
		return new PlotActionsManagerImpl(this);
	}

	@Override
	public T getPlotComposite() {
		if (containerOverride)  return parent;
		if (activeViewer!=null) return activeViewer.getControl();
		return null;
	}

	/**
	 * Does nothing if the viewer is already created.
	 * @param type
	 */
	private IPlottingSystemViewer<T> createViewer(PlotType type) {

		IPlottingSystemViewer<T> viewer = getViewer(type);
		if (viewer == null) {
			logger.error("Cannot find a plot viewer for plot type "+type);
			return null;
		}
		if (viewer.getControl()!=null) {
			return viewer;
		}
		viewer.init(this);
		viewer.createControl(parent);
		layout();
		return viewer;
	}

	private IPlottingSystemViewer<T> getViewer(PlotType type) {
		for (IPlottingSystemViewer<T> v : viewers) {
			if (v.isPlotTypeSupported(type))
				return v;
		}
		return null;
	}

	private IPlottingSystemViewer<T> getViewer(Class<? extends ITrace> type) {
		for (IPlottingSystemViewer<T> v : viewers) {
			if (v.isTraceTypeSupported(type))
				return v;
		}
		return null;
	}

	public void setFocus() {
		if (activeViewer!=null) activeViewer.setFocus();
	}

	public void addTraceListener(final ITraceListener l) {
		super.addTraceListener(l);
		if (activeViewer!=null) activeViewer.addImageTraceListener(l);
	}

	public void removeTraceListener(final ITraceListener l) {
		super.removeTraceListener(l);
		if (activeViewer!=null) activeViewer.removeImageTraceListener(l);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (activeViewer == null) return;

		DisplayUtils.syncExec(new Runnable() {
			@Override
			public void run() {
				if (activeViewer!=null) activeViewer.setEnabled(enabled);
			}
		});
	}

	@Override
	public boolean isEnabled() {
		if (activeViewer==null) return true;
		return activeViewer.isEnabled();
	}


	public List<ITrace> updatePlot1D(IDataset             x,
						             final List<? extends IDataset> ys,
						             final IProgressMonitor      monitor) {

		return updatePlot1D(x, ys, null, null, monitor);
	}

	public List<ITrace> updatePlot1D(IDataset                      x,
									 final List<? extends IDataset> ys,
									 final List<String>             dataNames,
									 final IProgressMonitor         monitor) {

        return updatePlot1D(x, ys, dataNames, null, monitor);
	}

	public List<ITrace> updatePlot1D(IDataset                       x,
									 final List<? extends IDataset> ys,
									 final String                   plotTitle,
									 final IProgressMonitor         monitor) {

		return updatePlot1D(x, ys, null, plotTitle, monitor);
	}

	private List<ITrace> updatePlot1D(IDataset                      x,
									 final List<? extends IDataset> ys,
									 final List<String>             dataNames,
									 final String                   plotTitle,
									 final IProgressMonitor         monitor) {


		final List<ITrace> updatedAndCreated = new ArrayList<ITrace>(3);
		final List<IDataset> unfoundYs    = new ArrayList<IDataset>(ys.size());
		final List<String>   unfoundNames = new ArrayList<String>(ys.size());

		for (int i = 0; i < ys.size(); i++) {

			final IDataset y        = ys.get(i);
			final String   dataName = dataNames!=null ? dataNames.get(i) : null;

			final ITrace trace = getTrace(y.getName());
			if (trace!=null && trace instanceof ILineTrace) {

				if (x == null) x = DatasetFactory.createRange(IntegerDataset.class, y.getSize());
				final IDataset finalX = x;
				final ILineTrace lineTrace = (ILineTrace) trace;
				updatedAndCreated.add(lineTrace);

				lineTrace.setErrorBarEnabled(y.hasErrors());

				DisplayUtils.syncExec(new Runnable() {
					@Override
					public void run() {
						lineTrace.setData(finalX, y);
					}
				});
				continue;
			}
			unfoundYs.add(y);
			unfoundNames.add(dataName);
		}
		if (!unfoundYs.isEmpty()) {
			if (x==null) x = DatasetFactory.createRange(IntegerDataset.class, unfoundYs.get(0).getSize());
			final Collection<ITrace> news = createPlot1D(x, unfoundYs, unfoundNames, plotTitle, monitor);
			updatedAndCreated.addAll(news);
		}
		return updatedAndCreated;
	}

	@Override
	public List<ITrace> createPlot1D(final IDataset       xIn,
					                 final List<? extends IDataset> ysIn,
					                 final IProgressMonitor      monitor) {

        return createPlot1D(xIn, ysIn, null, monitor);
	}

	/**
	 * Does not have to be called in UI thread.
	 */
	@Override
	public List<ITrace> createPlot1D(final IDataset       xIn,
					                 final List<? extends IDataset> ysIn,
					                 final String                title,
					                 final IProgressMonitor      monitor) {

		return createPlot1D(xIn, ysIn, null, title, monitor);
	}
	@Override
	public List<ITrace> createPlot1D(final IDataset             xIn,
									final List<? extends IDataset> ysIn,
									final List<String>          dataNames,
									final String                title,
									final IProgressMonitor      monitor) {

		if (monitor!=null) monitor.worked(1);

		// create index datasets if necessary
		final List<ITrace> traces = new ArrayList<ITrace>(7);
		final IDataset x;
		if (ysIn == null || ysIn.isEmpty()) {
			return traces;
		}

		if (xIn == null) {
			final int max = getMaxSize(ysIn);
			x = DatasetFactory.createRange(0, max, 1, Dataset.INT32);
			if (ysIn.size() == 1)
				x.setName("Index of " + ysIn.get(0).getName());
			else
				x.setName("Indices");
		} else {
			x = xIn;
		}

		DisplayUtils.syncExec(new Runnable() {
			@Override
			public void run() {
				List<ITrace> ts = createPlot1DInternal(x, ysIn, dataNames, title, monitor);
				if (ts != null) traces.addAll(ts);
			}
		});

		if (monitor!=null) monitor.worked(1);
		return traces;
	}

	@Override
	public void append( final String           name,
			            final Number           xValue,
					    final Number           yValue,
					    final IProgressMonitor monitor) throws Exception  {

		if (!this.plottingMode.is1D())
			throw new Exception("Can only add in 1D mode!");
		if (name == null || "".equals(name))
			throw new IllegalArgumentException("The dataset name must not be null or empty string!");

		DisplayUtils.syncExec(new Runnable() {
			@Override
			public void run() {
				appendInternal(name, xValue, yValue, monitor);
			}
		});
	}

	/**
     * Do not call before createPlotPart(...)
     */
	public void setPlotType(final PlotType mode) {
		DisplayUtils.syncExec(new Runnable() {
			@Override
			public void run() {
				switchPlottingType(mode);
			}
		});
	}

	@Override
	public ITrace updatePlot2D(final IDataset       data,
							   final List<? extends IDataset> axes,
							   final IProgressMonitor      monitor) {
		return updatePlot2D(data, axes, null, monitor);
	}

	@Override
	public ITrace updatePlot2D(final IDataset              data,
			 				   final List<? extends IDataset> axes,
			 				   final String                dataName,
				               final IProgressMonitor      monitor) {

		if (plottingMode.is1D()) {
			DisplayUtils.syncExec(new Runnable() {
				@Override
				public void run() {
					switchPlottingType(PlotType.IMAGE);
				}
			});
		}
		final Collection<ITrace> traces = plottingMode.is3D()
				                        ? getTraces(ISurfaceTrace.class)
				                        : getTraces(IImageTrace.class);

		if (monitor != null && monitor.isCanceled())
			return null;

		if (traces != null && traces.size() > 0) {
			ITrace image = traces.iterator().next();
			final int[] shape = image.getData() != null ? image.getData().getShape() : null;

			if (shape != null && Arrays.equals(shape, data.getShape())) {
				final List<ITrace> images = Arrays.asList(image);
				DisplayUtils.syncExec(new Runnable() {
					@Override
					public void run() {
						ITrace im = updatePlot2DInternal(images.get(0), data, axes, dataName, monitor);
						images.set(0, im);
					}
				});
				return images.get(0);
			} else {
				return createPlot2D(data, axes, dataName, monitor);
			}
		} else {
			return createPlot2D(data, axes, dataName, monitor);
		}
	}

	@SuppressWarnings("unchecked")
	private ITrace updatePlot2DInternal(final ITrace image,
			                          final IDataset       data,
								      final List<? extends IDataset> axes,
								      final String dataName,
								      final IProgressMonitor      monitor) {

		if (data.getName()!=null) if (activeViewer!=null) activeViewer.setTitle(data.getName());

		if (monitor!=null&&monitor.isCanceled()) return null;
		try {
			if (image instanceof IImageTrace) {
			    ((IImageTrace)image).setData(data, (List<IDataset>) axes, false);
			} else if (image instanceof ISurfaceTrace) {
			    ((ISurfaceTrace)image).setData(data, (List<IDataset>)axes);
			}
			return image;
		} catch (Throwable ne) { // We create a new one then
			clear();
			return createPlot2D(data, axes, monitor);
		}
	}

	/**
	 * Must be called in UI thread. Creates and updates image.
	 * NOTE removes previous traces if any plotted.
	 *
	 * @param data
	 * @param axes, x first.
	 * @param monitor
	 */
	@Override
	public ITrace createPlot2D(final IDataset       data,
							   final List<? extends IDataset> axes,
							   final IProgressMonitor      monitor) {
		return createPlot2D(data, axes, null, monitor);
	}
	@Override
	public ITrace createPlot2D(final IDataset       data,
								final List<? extends IDataset> axes,
								final String        dataName,
								final IProgressMonitor      monitor) {

		final List<ITrace> traces = new ArrayList<ITrace>(7);

		DisplayUtils.syncExec(new Runnable() {
			@Override
			public void run() {
				ITrace ts = createPlot2DInternal(data, axes, dataName, monitor);
				if (ts != null)
					traces.add(ts);
			}
		});

		return traces.size()>0 ? traces.get(0) : null;
	}

	@SuppressWarnings("unchecked")
	protected ITrace createPlot2DInternal(final IDataset            data,
										List<? extends IDataset>    axes,
										String                      dataName,
										final IProgressMonitor      monitor) {
		try {
			if (plottingMode.is1D()) {
				switchPlottingType(PlotType.IMAGE);
			}
			setAutoAspectRatio(data.getShape());

			clearPlotViewer(); // Only one image at a time!
			if (traceMap==null) traceMap = new LinkedHashMap<String, ITrace>(31);
			traceMap.clear();

			String traceName = data.getName();
			if (part!=null&&(traceName==null||"".equals(traceName))) {
				traceName = part.getTitle();
			}
			if (monitor!=null&&monitor.isCanceled()) return null;
			
			ITrace trace=null;
			if (plottingMode.is3D()) {
				trace = createSurfaceTrace(traceName);
				trace.setDataName(dataName);
				((ISurfaceTrace)trace).setData(data, (List<IDataset>)axes);
				addTrace(trace);

			} else {
				final IPlottingSystemViewer<T> viewer = getViewer(IImageTrace.class);
				IImageTrace imageTrace = createImageTrace(traceName);
				imageTrace.setData(data, (List<IDataset>) axes, false);
				trace = imageTrace;

				viewer.clearTraces();
				imageTrace.setDataName(dataName);

				addTrace(trace);
				if (data.getName()!=null) viewer.setTitle(data.getName());
			}

			if (mservice!=null) {
				mservice.publish(new MacroEventObject(data));
				if (axes!=null && !axes.isEmpty()) mservice.publish(new MacroEventObject(axes));
			}

			return trace;

		} catch (Throwable e) {
			logger.error("Cannot load file "+data.getName(), e);
			return null;
		}
	}

	/**
	 * Sets aspect ratio on/off given if the shape ratios are < 1/100
	 * See http://jira.diamond.ac.uk/browse/SCI-5379
	 * 
	 * @param shape
	 */
	public void setAutoAspectRatio(int[] shape) {
		double limitRatio = (double) 1 / (double) 100;
		double ratioWidth = (double) shape[0] / (double) shape[1];
		double ratioHeight = (double) shape[1] / (double) shape[0];
		boolean isAuto = ratioWidth > limitRatio && ratioHeight > limitRatio;
		boolean isAspectRatio = getPreferenceStore().getBoolean(PlottingConstants.ASPECT);
		if (!isAuto) {
			IContributionItem[] items = getActionBars().getToolBarManager().getItems();
			for (IContributionItem item : items) {
				if (item.getId() != null && item.getId().equals(PlottingConstants.ASPECT)) {
					ActionContributionItem action = (ActionContributionItem) item;
					action.getAction().setChecked(false);
					break;
				}
			}
			setKeepAspect(false);
		} else {
			setKeepAspect(isAspectRatio);
		}
	}

	@Override
	public IImageTrace createImageTrace(String traceName) {
		IImageTrace trace = (IImageTrace)getViewer(IImageTrace.class).createTrace(traceName, IImageTrace.class);
		fireTraceCreated(new TraceEvent(trace));
		return trace;
	}

	@Override
	public IImageStackTrace createImageStackTrace(String traceName) {
		IImageStackTrace trace = (IImageStackTrace)getViewer(IImageStackTrace.class).createTrace(traceName, IImageStackTrace.class);
		fireTraceCreated(new TraceEvent(trace));
		return trace;
	}

	/**
	 * An IdentityHashMap used to map Dataset to color used to plot it.
	 * records keys for both strings and sets so that different models for the
	 * file being plotted work. Sometimes dataset name is unique but the set is
	 * not, sometimes the dataset is unique but its name is not.
	 */
	private Map<Object, Color> colorMap; // Warning can be mem leak

	/**
	 * A map for recording traces to be used in the update method.
	 *
	 * Uses a map of abstract data set name to Trace to retrieve Trace on the
	 * update.
	 */
	private Map<String, ITrace> traceMap; // Warning can be mem leak

	private List<ITrace> createPlot1DInternal(final IDataset              xIn,
										      final List<? extends IDataset> ysIn,
										      final List<String>   dataNames,
										      final String                title,
										      final IProgressMonitor      monitor) {

		// Switch off error bars if very many plots.
		IPreferenceStore store = getPreferenceStore();

		boolean errorBarEnabled = store.getBoolean(PlottingConstants.GLOBAL_SHOW_ERROR_BARS);
		Collection<ITrace> existing = getTraces(ILineTrace.class);
		int traceCount = ysIn.size() + (existing!=null ? existing.size() : 0);
		if (errorBarEnabled && traceCount >= store.getInt(PlottingConstants.AUTO_HIDE_ERROR_SIZE)) errorBarEnabled = false;

		if (errorBarEnabled) {
			// No error dataset there then false again
			boolean foundErrors = false;
			for (IDataset ids : ysIn) {
				if (ids.hasErrors()) {
					foundErrors = true;
					break;
				}
			}
			if (!foundErrors) errorBarEnabled = false;
		}

		PlotType newType = null;
		if (plottingMode.is1Dor2D()) {
		    newType = PlotType.XY;
		} else if (plottingMode.isStacked3D()) {
			newType = PlotType.XY_STACKED_3D;
		} else if (plottingMode.isScatter3D()) {
			newType = PlotType.XY_SCATTER_3D;
		}
		if (newType != null)
			switchPlottingType(newType);

		if (colorMap == null && getColorOption()!=ColorOption.NONE) {
			if (getColorOption()==ColorOption.BY_NAME) {
				colorMap = new HashMap<Object,Color>(ysIn.size());
			} else {
				colorMap = new IdentityHashMap<Object,Color>(ysIn.size());
			}
		}
		if (traceMap==null) traceMap = new LinkedHashMap<String, ITrace>(31);

		final IPlottingSystemViewer<T> viewer = getViewer(plottingMode);
		List<ITrace> traces=null;

		if (plottingMode.is1D()) {
			if (viewer.getControl()==null) return null;
			List<ILineTrace> lines = viewer.createLineTraces(title, xIn, ysIn, dataNames, traceMap, colorMap, monitor);
			traces = new ArrayList<ITrace>(lines.size());
			traces.addAll(lines);

		} else if (plottingMode.isScatter3D()) {
			traceMap.clear();
			IScatter3DTrace trace = (IScatter3DTrace)viewer.createTrace(title, IScatter3DTrace.class);
			final IDataset x = DatasetUtils.convertToDataset(ysIn.get(0));
			final Dataset y = DatasetUtils.convertToDataset(ysIn.get(1));
			final Dataset z = DatasetUtils.convertToDataset(ysIn.get(2));
			if (dataNames!=null) trace.setDataName(dataNames.get(0));
			trace.setData(xIn, Arrays.asList(x,y,z));
			viewer.addTrace(trace);
			traceMap.put(trace.getName(), trace);
			traces = Arrays.asList((ITrace)trace);

		} else if (plottingMode.isStacked3D()) {
			traceMap.clear();

			ILineStackTrace trace = (ILineStackTrace)viewer.createTrace(title, ILineStackTrace.class);
			final IDataset x = xIn;
			final Dataset y = DatasetFactory.createRange(getMaxSize(ysIn), Dataset.INT32);
			final Dataset z = DatasetFactory.createRange(ysIn.size(), Dataset.INT32);
			if (dataNames!=null) trace.setDataName(dataNames.get(0));
			trace.setData(Arrays.asList(x,y,z), ysIn.toArray(new Dataset[ysIn.size()]));
			viewer.addTrace(trace);
			traceMap.put(trace.getName(), trace);
			traces = Arrays.asList((ITrace)trace);
		}

		Collection<ITrace> lineTraces = getTraces(ILineTrace.class);
		if (lineTraces!=null) for (ITrace iTrace : lineTraces) {
			((ILineTrace)iTrace).setErrorBarEnabled(errorBarEnabled);
		}

		if (mservice!=null) {
			mservice.publish(new MacroEventObject(ysIn));
			mservice.publish(new MacroEventObject(xIn));
		}
		fireTracesPlotted(new TraceEvent(traces));
        return traces;
	}

	private IPreferenceStore store;
	private IPreferenceStore getPreferenceStore() {
		if (store!=null) return store;
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
		return store;
	}

	@SuppressWarnings("unused")
	private boolean isAllInts(List<Dataset> ysIn) {
		for (Dataset a : ysIn) {
			if (a.getDType()!=Dataset.INT16 &&
				a.getDType()!=Dataset.INT32 &&
				a.getDType()!=Dataset.INT64) {
				return false;
			}
		}
		return true;
	}

	public ILineTrace createLineTrace(String traceName) {
		ILineTrace trace = (ILineTrace)getViewer(ILineTrace.class).createTrace(traceName, ILineTrace.class);
		return trace;
	}

	public IVectorTrace createVectorTrace(String traceName) {
		IVectorTrace trace = (IVectorTrace)getViewer(IVectorTrace.class).createTrace(traceName, IVectorTrace.class);
		return trace;
	}

	@Override
	public ISurfaceTrace createSurfaceTrace(String traceName) {
		ISurfaceTrace trace = (ISurfaceTrace)getViewer(ISurfaceTrace.class).createTrace(traceName, ISurfaceTrace.class);
		return (ISurfaceTrace) setPaletteData(trace);
	}

	@Override
	public IIsosurfaceTrace createIsosurfaceTrace(String traceName) {
		IIsosurfaceTrace trace = (IIsosurfaceTrace)getViewer(IIsosurfaceTrace.class).createTrace(traceName, IIsosurfaceTrace.class);
		return (IIsosurfaceTrace) setPaletteData(trace);
	}
	
	@Override
	public IVolumeRenderTrace createVolumeRenderTrace(String traceName) {
		IVolumeRenderTrace trace = (IVolumeRenderTrace)getViewer(IVolumeRenderTrace.class).createTrace(traceName, IVolumeRenderTrace.class);
		return (IVolumeRenderTrace) setPaletteData(trace);
	}


	@Override
	public IMulti2DTrace createMulti2DTrace(String traceName) {
		IMulti2DTrace trace = (IMulti2DTrace)getViewer(IMulti2DTrace.class).createTrace(traceName, IMulti2DTrace.class);
		return (IMulti2DTrace) setPaletteData(trace);
	}

	private IImage3DTrace setPaletteData(IImage3DTrace trace) {
		PaletteData palette = null;
		if (trace.getPaletteData()==null) {
			final String schemeName = PlottingSystemActivator.getPlottingPreferenceStore().getString(PlottingConstants.COLOUR_SCHEME);

			final Collection<ITrace> col = getTraces(IImageTrace.class);
			if (col!=null && col.size()>0) {
				palette = ((IImageTrace)col.iterator().next()).getPaletteData();
			} else {
				try {
					final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
					palette = pservice.getDirectPaletteData(schemeName);
				} catch (Exception e) {
					palette = null;
				}
			}
			trace.setPaletteData(palette);
			trace.setPaletteName(schemeName);
		}
		return trace;
	}

	@Override
	public ILineStackTrace createLineStackTrace(String traceName) {
		ILineStackTrace trace = (ILineStackTrace)getViewer(ILineStackTrace.class).createTrace(traceName, ILineStackTrace.class);
		return trace;
	}


	@Override
	public IScatter3DTrace createScatter3DTrace(String traceName) {
		IScatter3DTrace trace = (IScatter3DTrace)getViewer(IScatter3DTrace.class).createTrace(traceName, IScatter3DTrace.class);
		return trace;
	}

	@Override
	public IPlane3DTrace createPlane3DTrace(String traceName) {
		IPlane3DTrace trace = (IPlane3DTrace) getViewer(IPlane3DTrace.class).createTrace(traceName, IPlane3DTrace.class);
		return trace;
	}

	protected void switchPlottingType(PlotType type) {
		if (plottingMode != null && plottingMode.equals(type)) {
			return;
		}

		PlotType previous = plottingMode;
		plottingMode = type;
		actionBarManager.switchActions(plottingMode);

		T top = null;

		IPlottingSystemViewer<T> viewer = createViewer(type);
		if (viewer == null) return;

		activeViewer = viewer;
		top          = viewer.getControl();
		viewer.updatePlottingRole(type);

		if (parent instanceof Composite && top instanceof Control) {
			Composite cparent = (Composite)parent;
			if (parent != null && !cparent.isDisposed() && cparent.getLayout() instanceof StackLayout) {
				final StackLayout layout = (StackLayout)cparent.getLayout();
				layout.topControl = (Control)top;
				layout();
			}
		}

		if (isAutoHideRegions() && previous!=plottingMode) {
			// We auto-hide regions that are different plot type.
			final Collection<IRegion> regions = getRegions();
			if (regions!=null) for (IRegion iRegion : regions) {
				if (iRegion.isUserRegion())
					iRegion.setVisible(iRegion.getPlotType()==null || iRegion.getPlotType()==type);
			}
		}
	}

	/**
	 * Adds trace, makes visible
	 * @param traceName
	 * @return
	 */
	public void addTrace(ITrace trace) {

		IPlottingSystemViewer<T> viewer = getViewer(trace.getClass());
		
		if (viewer != activeViewer) {
			if (viewer.getControl()==null) {
				viewer.init(this);
				viewer.createControl(parent);
				Composite cparent = (Composite)parent;
				if (parent != null && !cparent.isDisposed() && cparent.getLayout() instanceof StackLayout) {
					final StackLayout layout = (StackLayout)cparent.getLayout();
					layout.topControl = (Control)viewer.getControl();
					layout();
				}
			}
			activeViewer = viewer;
		}
		
		boolean ok = viewer.addTrace(trace);
		if (!ok) return; // it has not added.

		if (traceMap==null) this.traceMap = new HashMap<String, ITrace>(7);
		traceMap.put(trace.getName(), trace);

		fireTraceAdded(new TraceEvent(trace));
	}

	/**
	 * Removes a trace.
	 * @param traceName
	 * @return
	 */
	public void removeTrace(ITrace trace) {
		if (traceMap!=null) traceMap.remove(trace.getName());

		IPlottingSystemViewer<T> viewer = getViewer(trace.getClass());
		viewer.removeTrace(trace);
		fireTraceRemoved(new TraceEvent(trace));
	}

	@Override
	public void renameTrace(final ITrace trace, String name) {
		if (name!=null && name.equals(trace.getName())) return;
		trace.setName(name);
		if (traceMap==null) traceMap = new LinkedHashMap<String, ITrace>(3);
		traceMap.put(name, trace);
	}
	@Override
	public void moveTrace(final String oldName, String name) {

		if (name!=null && name.equals(oldName)) return;
		if (traceMap!=null) {
			ITrace trace = traceMap.remove(oldName);
			traceMap.put(name, trace);
		}
	}

	public Collection<ITrace> getTraces() {
		if (traceMap==null) return Collections.emptyList();
		return new LinkedHashSet<ITrace>(traceMap.values());
	}

	@Override
	public ITrace getTrace(String name) {
		if (traceMap==null) return null;
		return traceMap.get(name);
	}

	private void appendInternal(final String           name,
					                  Number           xValue,
							    final Number           yValue,
							    final IProgressMonitor monitor) {
		final ITrace wrapper = traceMap.get(name);
		if (wrapper==null) return;

		final Trace trace = ((LineTraceImpl)wrapper).getTrace();

		LightWeightDataProvider prov = (LightWeightDataProvider)trace.getDataProvider();
		if (prov==null) return;

		prov.append(xValue, yValue);
	}
	/**
	 * Override this method to provide an implementation of title setting.
	 * @param title
	 */
	public void setTitle(final String title) {
		super.setTitle(title);
		activeViewer.setTitle(title);
	}
	/**
	 * Call to set the plot title color.
	 * @param title
	 */
	public void setTitleColor(final Color color) {
		activeViewer.setTitleColor(color);
	}
	@Override
	public void setBackgroundColor(final Color color) {
		activeViewer.setBackgroundColor(color);
	}

	public String getTitle() {
		return activeViewer.getTitle();
	}

	@Override
	public void setShowLegend(boolean b) {
		String id = ToolbarConfigurationConstants.CONFIG.getId() + BasePlottingConstants.XY_SHOWLEGEND;
		IAction action = actionBarManager.findAction(getPlotName()+"/"+id);
		if (action != null) {
			action.setChecked(b);
		}

		if (activeViewer != null) {
			activeViewer.setShowLegend(b);
		}
	}

	@Override
	public boolean isShowIntensity(){
		if (activeViewer!=null) return activeViewer.isShowIntensity();
		return false;
	}
	@Override
	public void setShowIntensity(boolean b){
		if (activeViewer!=null) activeViewer.setShowIntensity(b);
		repaint(false);
	}

	public Color get1DPlotColor(Object object) {
		if (getColorOption()==ColorOption.NONE) return null;
		if (colorMap==null) return null;
		if (object==null) return null;
		if (colorOption==ColorOption.BY_DATA) {
			return colorMap.get(object);
		} else if (colorOption==ColorOption.BY_NAME) {
			return colorMap.get((String)object);
		}
		return null;
	}

	private int getMaxSize(List<? extends IDataset> sets) {
		int max = 1; // Cannot be less than one
		for (IDataset set : sets) {
			if (set != null)
			    max = Math.max(max, set.getSize());
		}

		return max;
	}

	@Override
	public void reset() {
		DisplayUtils.syncExec(new Runnable() {
			@Override
			public void run() {
				resetInternal();
			}
		});
	}

	private void resetInternal() {
		if (traceMap!=null) traceMap.clear();
		if (colorMap!=null) colorMap.clear();
		for (IPlottingSystemViewer<T> v : viewers) {
			if (v.getControl()!=null) v.reset(true);
		}
		fireTracesCleared(new TraceEvent(this));
	}

	@Override
	public void clear() {
		DisplayUtils.syncExec(new Runnable() {
			@Override
			public void run() {
				clearInternal();
			}
		});
	}

	private void clearInternal() {
		if (activeViewer.getControl()!=null) {
			try {
				clearPlotViewer();
				if (colorMap!=null) colorMap.clear();
			} catch (Throwable e) {
				logger.error("Cannot remove traces!", e);
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		store = null;
		if (colorMap!=null) {
			colorMap.clear();
			colorMap = null;
		}
		clearPlotViewer();
		for (IPlottingSystemViewer<T> v : viewers) {
			if (v.getControl()!=null) v.dispose();
		}
		try {
			RMIServerProvider.getInstance().unbind(IPlottingSystem.RMI_PREFIX+plotName);
		} catch (Exception e) {
			logger.error("Unable to deregister plotting system");
		}
	}

	@Override
	public void clearTraces() {
		DisplayUtils.syncExec(new Runnable() {
			@Override
			public void run() {
				removeAllTraces();
			}
		});
	}

	private void removeAllTraces() {
		final Collection<ITrace> traces = getTraces();
		for (ITrace iTrace : traces) {
			removeTrace(iTrace);
		}
	}

	/**
	 *
	 */
	private void clearPlotViewer() {

		for (IPlottingSystemViewer<T> v : viewers) {
		  if (v.getControl()!=null)  v.clearTraces();
		}
		if (traceMap!=null) traceMap.clear();
		fireTracesCleared(new TraceEvent(this));
	}

	public void repaint() {
		repaint(isRescale());
	}

	public void repaint(final boolean autoScale) {
		if (activeViewer!=null) activeViewer.repaint(autoScale);
	}

	/**
	 * Creates an image of the same size as the Rectangle passed in.
	 * @param size, ignored for 3D plots, instead the size of the widget it used.
	 * @return
	 */
	@Override
	public Image getImage(Rectangle size) {
		return activeViewer.getImage(size);
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

		return activeViewer.createAxis(title, isYAxis, side);
	}

	@Override
	public IAxis removeAxis(final IAxis axis) {
		return activeViewer.removeAxis(axis);
	}

	@Override
	public List<IAxis> getAxes() {
		return activeViewer.getAxes();
	}

	@Override
	public IAxis getAxis(String name) {
		return activeViewer.getAxis(name);
	}

	@Override
	public IAxis getSelectedXAxis() {
		return activeViewer.getSelectedXAxis();
	}

	@Override
	public void setSelectedXAxis(IAxis selectedXAxis) {
		activeViewer.setSelectedXAxis(selectedXAxis);
	}

	@Override
	public IAxis getSelectedYAxis() {
		return activeViewer.getSelectedYAxis();
	}

	@Override
	public void setSelectedYAxis(IAxis selectedYAxis) {
		activeViewer.setSelectedYAxis(selectedYAxis);
	}

	public boolean addRegionListener(final IRegionListener l) {
		return activeViewer.addRegionListener(l);
	}

	public boolean removeRegionListener(final IRegionListener l) {
		return activeViewer.removeRegionListener(l);
	}

	/**
	 * Throws exception if region exists already.
	 * @throws Exception
	 */
	public IRegion createRegion(final String name, final RegionType regionType) throws Exception  {
		return activeViewer.createRegion(name, regionType);
	}

	public void clearRegions() {
		activeViewer.clearRegions();
	}

	@Override
	public void resetAxes() {
		DisplayUtils.syncExec(new Runnable() {
			@Override
			public void run() {
				if (activeViewer != null)
					activeViewer.resetAxes();
			}
		});
	}

	public void clearRegionTool() {
		activeViewer.clearRegionTool();
	}

	/**
	 * Add a selection region to the graph.
	 * @param region
	 */
	public void addRegion(final IRegion region) {
		activeViewer.addRegion(region);
	}

	/**
	 * Remove a selection region to the graph.
	 * @param region
	 */
	public void removeRegion(final IRegion region) {
		activeViewer.removeRegion(region);
	}

	@Override
	public void renameRegion(final IRegion region, String name) {
		activeViewer.renameRegion(region, name);
	}

	/**
	 * Get a region by name.
	 * @param name
	 * @return
	 */
	public IRegion getRegion(final String name) {
		return activeViewer.getRegion(name);
	}

	/**
	 * Get regions
	 * @param name
	 * @return
	 */
	public Collection<IRegion> getRegions() {
		return activeViewer.getRegions();
	}

	@Override
	public Collection<IRegion> getRegions(final RegionType type) {
		return activeViewer.getRegions(type);
	}

	@Override
	public IAnnotation createAnnotation(final String name) throws Exception {
		return activeViewer.createAnnotation(name);
	}

	@Override
	public void addAnnotation(final IAnnotation annotation) {
		activeViewer.addAnnotation(annotation);
	}

	@Override
	public void removeAnnotation(final IAnnotation annotation) {
		activeViewer.removeAnnotation(annotation);
	}

	@Override
	public void renameAnnotation(final IAnnotation annotation, String name) {
		activeViewer.renameAnnotation(annotation, name);
	}

	@Override
	public void clearAnnotations(){
		activeViewer.clearAnnotations();
	}


	@Override
	public IAnnotation getAnnotation(final String name) {
		return activeViewer.getAnnotation(name);
	}

	@Override
	public void autoscaleAxes() {
		activeViewer.autoscaleAxes();
	}

	@Override
	public void printPlotting(){
		if (activeViewer instanceof IPrintablePlotting) {
			((IPrintablePlotting)activeViewer).printPlotting();
		}
	}

	/**
	 * Print scaled plotting to printer
	 */
	public void printScaledPlotting(){
		if (activeViewer instanceof IPrintablePlotting) {
			((IPrintablePlotting)activeViewer).printScaledPlotting();
		}
	}

	@Override
	public void copyPlotting(){
		if (activeViewer instanceof IPrintablePlotting) {
			((IPrintablePlotting)activeViewer).copyPlotting();
		}
	}

	@Override
	public String savePlotting(final String filename) throws Exception{
		if (activeViewer instanceof IPrintablePlotting) {
			DisplayUtils.asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						((IPrintablePlotting) activeViewer).savePlotting(filename);
					} catch (Throwable t) {
						throw new RuntimeException(t);
					}
				}
			});
		}
		return null;
	}

	@Override
	public void savePlotting(final String filename, final String filetype) throws Exception{
		if (activeViewer instanceof IPrintablePlotting) {
			DisplayUtils.asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						((IPrintablePlotting) activeViewer).savePlotting(filename, filetype);
					} catch (Throwable t) {
						throw new RuntimeException(t);
					}
				}
			});
		}
	}

	public void setXFirst(boolean xfirst) {
		super.setXFirst(xfirst);
		activeViewer.setXFirst(xfirst);
	}

	public void setRescale(boolean rescale) {
		super.setRescale(rescale);
		activeViewer.setRescale(rescale);
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
	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		super.addPropertyChangeListener(listener);
		activeViewer.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		super.removePropertyChangeListener(listener);
		activeViewer.removePropertyChangeListener(listener);
	}

	public void setActionBars(IActionBars bars) {
		this.bars = bars;
	}

	@Override
	public void setDefaultCursor(int cursorType) {
		activeViewer.setDefaultCursor(cursorType);
	}

	@Override
	public void addPositionListener(IPositionListener l) {
		activeViewer.addPositionListener(l);
	}

	@Override
	public void removePositionListener(IPositionListener l) {
		activeViewer.removePositionListener(l);
	}

	@Override
	public void addClickListener(IClickListener l) {
		activeViewer.addClickListener(l);
	}

	@Override
	public void removeClickListener(IClickListener l) {
		activeViewer.removeClickListener(l);
	}

	public void setKeepAspect(boolean checked){
		activeViewer.setKeepAspect(checked);
	}
	
	public List<Class<? extends ITrace>> getRegisteredTraceClasses() {
		
		List<Class<? extends ITrace>> traceClazz = new ArrayList<Class<? extends ITrace>>();
		for (IPlottingSystemViewer<?> v : viewers) traceClazz.addAll(v.getSupportTraceTypes());
		
		return traceClazz;
	}
	
	public <U extends ITrace> U createTrace(String traceName, Class<U> clazz) {
		for (IPlottingSystemViewer<?> v : viewers) {
			if (v.isTraceTypeSupported(clazz)) {
				return v.createTrace(traceName, clazz);
			}
		}
		
		return null;
	}

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {

		if (adapter.isAssignableFrom(getClass())) return this;

		for (IPlottingSystemViewer<T> v : viewers) {
			if (v.getClass() == adapter) return v;
			if (adapter.isAssignableFrom(v.getClass())) return v;
			if (v instanceof IAdaptable) {
				Object inst = ((IAdaptable)v).getAdapter(adapter);
				if (inst!=null) return inst;
			}
		}
		return super.getAdapter(adapter);
	}
}

