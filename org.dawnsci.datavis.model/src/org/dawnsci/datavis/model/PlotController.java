package org.dawnsci.datavis.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.IDataPackage;
import org.dawnsci.datavis.api.ILazyPlotMode;
import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.datavis.model.PlotEventObject.PlotEventType;
import org.dawnsci.january.model.NDimensions;
import org.dawnsci.january.ui.utils.DisplayWrapper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.PlotExportConstants;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for the plotting system
 * 
 * On selection of a checked DataOption, for a checked loaded file, the default
 * plot mode, with its default slicing should be plotted.
 * 
 * if a different DataOption is checked in the same file, the default plot mode,
 * with its default slicing should be plotted. If the plot mode support multiple,
 * all traces of the same type should remain, if it doesn't the initial DataOption should be unchecked
 * and its trace removed. The a DataOption is added from a different file, and the plot mode
 * does not support multiple, the initial file should be uncheck and its trace removed.
 * 
 * On change of PlotMode all files except for the current should be deselected, and data options, and 
 * their traces removed from the plot.
 * 
 * When I file is unloaded all its traces should be removed.
 * 
 * When the slice changes, the traces should be updated or updated and added.
 * 
 * 
 * @author jacobfilik
 */
public class PlotController implements IPlotController, ILoadedFileInitialiser {
	
	private static final Logger logger = LoggerFactory.getLogger(PlotController.class);
	
	private static class PlotModeData implements Comparable<PlotModeData> {
		private IPlotMode mode;
		private String name;
		private int priority;
		
		PlotModeData(IPlotMode mode, String name, int priority) {
			this.mode = mode;
			this.name = name;
			this.priority = priority;
		}
		
		PlotModeData(IPlotMode mode, String name) {
			this.mode = mode;
			this.name = name;
			this.priority = Integer.MAX_VALUE;
		}
		
		@Override
		public int compareTo(PlotModeData o) {
			if (this.priority == o.priority) {
				return this.name.compareTo(o.name);
			}
			return o.priority - this.priority;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
			    return false;

			if (this.getClass() != obj.getClass())
			    return false;
			
			return this.mode == ((PlotModeData) obj).mode;
		}
	}
	
	static {
		List<PlotModeData> modeDataList = new ArrayList<>();
		// look for the extension points
		IConfigurationElement[] eles = Platform.isRunning() ? Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.datavis.api.plotmode") : new IConfigurationElement[0];
		for (IConfigurationElement e : eles) {
			logger.debug("Found Plot Mode: {}", e.getAttribute("name"));
			if (!e.getName().equals("plotmode"))
				continue;
			IPlotMode plotMode = null;
			int priority = 0;
			try {
				plotMode = (IPlotMode) e.createExecutableExtension("class");
			} catch (CoreException e1) {
				logger.error("Exception in createExecutableExtension", e1);
				continue;
			}
			try {
				priority = Integer.parseInt(e.getAttribute("priority"));
			} catch (NumberFormatException e2) {
				// bad string means 0
			}
			String name = e.getAttribute("name");
			modeDataList.add(new PlotModeData(plotMode, name, priority));
		}
		modeDataList.sort(null);
		// image mode index must match MODE_2D
		modeDataList.add(0, new PlotModeData(new PlotModeImage(), "PlotModeImage"));
		modeDataList.add(0, new PlotModeData(new PlotModeXY(true), "PlotModeScatter"));
		modeDataList.add(0, new PlotModeData(new PlotModeXY(), "PlotModeXY"));

		modes = modeDataList.stream().map(data -> data.mode).toArray(IPlotMode[]::new);
	}

	/**
	 * This constant should used in testing only
	 */
	public static final int MODE_2D = 2;

	private  IPlottingService plotService;
	private  IFileController fileController;
	
	//OSGI service injection
	
	public void setPlotService(IPlottingService service) {
		plotService = service;
	}
	
	public void setFileController(IFileController controller) {
		fileController = controller;
	}
	
	private IPlottingSystem<?> system;
	
	private static final IPlotMode[] modes;
	
	private IPlotMode currentMode;
	
	private boolean modeSwitching = false;
	
	private boolean coSlicingEnabled = false;

	private IPlotDataModifier[] modifiers = new IPlotDataModifier[]{ new PlotDataModifierStack()};
	private IPlotDataModifier currentModifier;
	
	private ITraceColourProvider colorProvider;
	private List<Color> colorcache = null;
	
	private FileControllerStateEventListener fileStateListener;
	
	private Set<PlotModeChangeEventListener> listeners = new HashSet<>();
	
	private ExecutorService executor;
	private AtomicReference<Runnable> atomicRunnable = new AtomicReference<>();
	private AtomicReference<Future<?>> atomicFuture = new AtomicReference<>();

	private Map<String, List<DataOptions>> singlePlots = new LinkedHashMap<>();
	
	public PlotController (IPlottingSystem<?> system, IFileController controller, ExecutorService exService) {
		this.system = system;
		this.fileController = controller;
		this.executor = exService;
		init();
	}
	
	public PlotController() {
		this.currentMode = modes[0];
	}
	
	public void init(){
		
		if (fileStateListener != null) return;
		
		if (executor == null) executor = Executors.newSingleThreadExecutor();
		
		fileStateListener  = new FileControllerStateEventListener() {

			@Override
			public void stateChanged(FileControllerStateEvent event) {
				if (!event.isSelectedDataChanged() && !event.isSelectedFileChanged()) return;
				
				updateOnFileStateChange(event.getLoadedFile(),event.getOption());	
			}
			
		};
		
		fileController.addStateListener(fileStateListener);
		fileController.setFileInitialiser(this);
	}
	
	private void updateOnFileStateChange(LoadedFile f, DataOptions d) {
		IPlottingSystem<?> system = getPlottingSystem();
		if (system == null) return;
		//TODO UPDATE FILE STATE
		
		if (d != null && d.isSelected() && d.getParent().isSelected()) {
			IPlotMode localMode = currentMode;
			currentMode = d.getPlottableObject().getPlotMode();
			if (currentMode != localMode) {
				system.reset();
				if (system.getSelectedXAxis() != null) system.getSelectedXAxis().setTitle("");
				if (system.getSelectedYAxis() != null) system.getSelectedYAxis().setTitle("");
				system.setTitle("");
				for (PlotModeChangeEventListener l : listeners) l.plotModeChanged();
			}
		}
		
		if (d != null) updateFileState(d, currentMode);

		updatePlotStateInJob(currentMode, true);
	}
	
	private void updatePlotState(List<? extends IDataPackage> state, IPlotMode mode) {

		//start update
		firePlotEvent(new PlotEventObject(PlotEventType.LOADING, "Loading data..."));
		
		
		IPlotMode localCurrentMode = currentMode;
		IPlotDataModifier localModifier = currentModifier;
		if (localModifier != null) localModifier.init();
		
		IPlottingSystem<?> system = getPlottingSystem();
		if (localCurrentMode != mode) system.reset();
		
		final Collection<ITrace> traces = system.getTraces();
		
		if (!traces.isEmpty() && localCurrentMode instanceof IPlotModeColored) {
			ITrace next = traces.iterator().next();
			if (next instanceof IPaletteTrace && !((IPaletteTrace)next).isRescaleHistogram()) {
				ImageServiceBean bean = ((IPaletteTrace)next).getImageServiceBean();
				
				Number[] minMax = new Number[] {bean.getMin(),bean.getMax()};
				((IPlotModeColored)localCurrentMode).setMinMax(minMax);
			} else {
				((IPlotModeColored)localCurrentMode).setMinMax(null);
			}
			
			if (!system.isRescale() && system.getSelectedXAxis() != null && system.getSelectedYAxis() != null) {
				IAxis xa = system.getSelectedXAxis();
				IAxis ya = system.getSelectedYAxis();
				((IPlotModeColored)localCurrentMode).setAxesRange(new double[] {xa.getLower(),xa.getUpper(),ya.getLower(),ya.getUpper()});
			} else {
				((IPlotModeColored)localCurrentMode).setAxesRange(null);
			}
		}
		
		final Set<String> uniqueLabelSet = new HashSet<>();
		Set<String> duplicates = new HashSet<>();
		
		for (IDataPackage dp : state) {
			if (dp instanceof DataOptions) {
				DataOptions d = (DataOptions) dp;

				String label = d.getLabel();
				if (label.isEmpty()) continue;
				
				if (uniqueLabelSet.contains(label)) {
					uniqueLabelSet.remove(label);
					duplicates.add(label);
				} else if (!duplicates.contains(label)){
					uniqueLabelSet.add(label);
				}
			}
		}
		
		final List<Runnable> uiRunnables = new ArrayList<>();
		
		if (!traces.isEmpty()) {
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					
					for (ITrace t : traces) {
						system.removeTrace(t);
					}
				}
			};
			
			uiRunnables.add(r);
		}

		boolean isXY = PlotModeXY.class.isAssignableFrom(localCurrentMode.getClass());
		Set<String> names = new HashSet<>();
		int n = 0;
		for (IDataPackage dp : state) {
			if (dp instanceof DataOptions) {
				n++;
				names.add(((DataOptions) dp).getName());
			}
		}
		boolean useName = n <= 1 || names.size() > 1;
		for (IDataPackage dp : state) {
			if (dp instanceof DataOptions) {
				DataOptions d = (DataOptions) dp;

				if (!isXY || checkForMultiplePoints(d)) {
					Runnable r = updatePlottedData(useName, d, localCurrentMode, localModifier, uniqueLabelSet);
					if (r != null) {
						uiRunnables.add(r);
					}
				}
			}
		}
		if (!singlePlots.isEmpty()) {
			generateSinglePointPlots(uiRunnables, localCurrentMode, localModifier, uniqueLabelSet);
		}

		DisplayWrapper.syncExec(new Runnable() {
			
			@Override
			public void run() {
				
				firePlotEvent(new PlotEventObject(PlotEventType.PAINTING, "Painting..."));
				
				for (Runnable r : uiRunnables) {
					try {
						r.run();
					} catch (Exception e) {
						firePlotEvent(new PlotEventObject(PlotEventType.ERROR, "Error painting..."));
						logger.error("Error running plot update",e);
					}
					
				}
				
				if (colorProvider != null) {
					List<Color> local = null;
					if (colorcache == null) {
						RGB[] rgbs = colorProvider.getRGBs();
						local = new ArrayList<Color>(rgbs.length);
						Display display = Display.getDefault();
						for (int i = 0; i < rgbs.length; i++) {
							local.add(new Color(display, rgbs[i]));
						}
						colorcache = local;
					}
					local = colorcache;
					
					Collection<ITrace> traces = system.getTraces(ILineTrace.class);
					double count = 0;
					for (ITrace trace : traces)
						if (trace.isUserTrace())
							count++;

					double val = (local.size()-1) / (count - 1);
					if (Double.isNaN(val)) val = 0;
					int i = 0;
					for (ITrace trace : traces) {
						if (trace.isUserTrace()) {
							((ILineTrace) trace).setTraceColor(local.get((int) val * i));
							i++;
						}

					}
				}
				
			
				
				getPlottingSystem().repaint();
				
				firePlotEvent(new PlotEventObject(PlotEventType.READY, "Ready"));
			}
		});
		
		List<IAxis> axes = system.getAxes();
		if (axes != null) for (IAxis axis : axes) if (axis != null) axis.setAxisAutoscaleTight(true);
	}

	// adds single point options to singlePlots too
	private boolean checkForMultiplePoints(DataOptions d) {
		PlottableObject po = d.getPlottableObject();
		NDimensions nd = po.getNDimensions();
		SliceND s = nd.buildSliceND();
		if (ShapeUtils.calcSize(s.getShape()) != 1) {
			return true;
		}

		String n = d.getShortName(); // prefer to use short name if name is not already used
		if (n == null || (!singlePlots.containsKey(n) && singlePlots.containsKey(d.getName()))) {
			n = d.getName();
		}

		List<DataOptions> l = singlePlots.get(n);
		if (l == null) {
			l = new ArrayList<>();
			singlePlots.put(n, l);
		}
		l.add(d);
		return false;
	}

	
	private IMetadata createExportMetadata(DataOptions dataOp) {
		Map<String, Serializable> kv = new LinkedHashMap<>();
		LoadedFile lf = dataOp.getParent();
		String l = lf.getLabelName();
	
		if (!l.isEmpty()) {
			Dataset lv = lf.getLabelValue();
			if (lv != null) {
				kv.put(PlotExportConstants.LABEL_NAME, DataOptionsUtils.shortenDatasetPath(l, false));
				kv.put(PlotExportConstants.LABEL_VALUE, (Serializable) lv.getObject());
			}
		}

		Dataset scanNumber = lf.getLabelValue("/entry1/entry_identifier");
		if (scanNumber == null) {
			kv.put(PlotExportConstants.SCAN, new File(lf.getFilePath()).getName());
		} else {
			kv.put(PlotExportConstants.SCAN, scanNumber.getString());
		}

		String name = dataOp.getShortName();
		if (name == null) {
			name = DataOptionsUtils.shortenDatasetPath(dataOp.getName(), true);
		}
		kv.put(PlotExportConstants.PLOT_NAME, name);

		try {
			return MetadataFactory.createMetadata(IMetadata.class, kv);
		} catch (MetadataException e) {
			logger.error("Could not create export metadata", e);
		}
		return null;
	}

	private Runnable updatePlottedData(boolean useName, DataOptions dataOp, IPlotMode mode, IPlotDataModifier modifier, Set<String> uniqueLabelSet) {
		//update the data in the plot
		
		IPlottingSystem<?> system = getPlottingSystem();
		
		PlottableObject plotObject = dataOp.getPlottableObject();
		
		NDimensions nd = plotObject.getNDimensions();
		
		
		String[] axes = nd.buildAxesNames();
		SliceND slice= nd.buildSliceND();
		Object[] options = nd.getOptions();
		
		dataOp.setAxes(axes);
		

		IDataset[] data = null;
		IMetadata md = createExportMetadata(dataOp);
		try {
			ILazyDataset view = dataOp.getLazyDataset().getSliceView();

			if (md != null) { // name format as 'scan:data opt (label:value)'
				try (Formatter f = new Formatter()) {
	
					Serializable s = md.getMetaValue(PlotExportConstants.LABEL_NAME);
					if (useName) {
						if (s != null) {
							f.format("%s=%s (%s:%s)", s, md.getMetaValue(PlotExportConstants.LABEL_VALUE), md.getMetaValue(PlotExportConstants.PLOT_NAME), md.getMetaValue(PlotExportConstants.SCAN));
						} else {
							f.format("%s:%s", md.getMetaValue(PlotExportConstants.PLOT_NAME), md.getMetaValue(PlotExportConstants.SCAN));
						}
					} else {
						if (s != null) {
							f.format("%s=%s (%s)", s, md.getMetaValue(PlotExportConstants.LABEL_VALUE), md.getMetaValue(PlotExportConstants.SCAN));
						} else {
							f.format("%s", md.getMetaValue(PlotExportConstants.SCAN));
						}
					}
					view.setName(f.toString());
				}
			}

			if (!useName) {
				if (view.getRank() == 1) {
					IAxis axis = system.getAxes().get(1);
					String title = axis.getTitle();
					String name = md.getMetaValue(PlotExportConstants.PLOT_NAME).toString();
					if (title == null || !title.equals(name)) {
						Display.getDefault().asyncExec(() -> axis.setTitle(name));
					}
				} else if (view.getRank() >= 2) {
					String title = system.getTitle();
					String name = md.getMetaValue(PlotExportConstants.PLOT_NAME).toString();
					if (title == null || !title.equals(name)) {
						Display.getDefault().asyncExec(() -> system.setTitle(name));
					}
				}
			}
			data = mode.sliceForPlot(view, slice, options, system);

			LoadedFile lf = dataOp.getParent();
			String l = lf.getLabelName();
			if (data != null && data.length == 1 && uniqueLabelSet.contains(l)) {
				data[0].setName(l);
			}
		} catch (Exception e) {
			
			//see if exception caused by out of memory
			//if so add extra message
			String mem = "";
			Throwable t = e;
			
			while (t != null) {
				
				if (t instanceof OutOfMemoryError) {
					mem = " Out of Memory!";
					break;
				}
				
				t = t.getCause();
			}
			
			firePlotEvent(new PlotEventObject(PlotEventType.ERROR, "Error loading data..." + mem));
			logger.error("Could not slice data for plotting", e);
		}
		
		if (mode instanceof ILazyPlotMode) {

			return ()->  {

				try {
					((ILazyPlotMode)mode).displayData(null, system, dataOp);
					getPlottingSystem().repaint();
				} catch (Exception e) {
					logger.error("Error plotting", e);
				}
			};
		}

		if (data == null) {
			return null;
		}
		
		SourceInformation si = new SourceInformation(dataOp.getFilePath(), dataOp.getName(), dataOp.getLazyDataset());
		SliceInformation s = new SliceInformation(slice, slice, new SliceND(dataOp.getLazyDataset().getShape()), mode.getDataDimensions(options), 1, 0);
		SliceFromSeriesMetadata smd = new SliceFromSeriesMetadata(si, s);
		
		for (int i = 0; i < data.length ; i++) {
			IDataset d = data[i];
			if (modifier != null && modifier.supportsRank(mode.getMinimumRank())){
				d = modifier.modifyForDisplay(d);
			}
			d.setMetadata(smd);
			d.addMetadata(md);
			data[i] = d;
		}
		
	
		final IDataset[] finalData = data;
		
		return () -> {

			try {
				mode.displayData(finalData, null, system, dataOp);
			} catch (Exception e) {
				logger.error("Error displaying data", e);
			}
		};
		
	}

	private void generateSinglePointPlots(List<Runnable> uiRunnables, IPlotMode mode, IPlotDataModifier localModifier, Set<String> uniqueLabelSet) {
		for (Entry<String, List<DataOptions>> e : singlePlots.entrySet()) {
			Runnable r = createPlottedData(e.getKey(), e.getValue(), mode, localModifier, uniqueLabelSet);
			if (r != null) {
				uiRunnables.add(r);
			}
		}
		singlePlots.clear();
	}

	// gather single-point data from given options to create single plot
	private Runnable createPlottedData(String n, List<DataOptions> list, IPlotMode mode, IPlotDataModifier localModifier, Set<String> uniqueLabelSet) {
		int l = list.size();
		if (l == 0) {
			return null;
		}
		DoubleDataset x = DatasetFactory.zeros(l);
		DoubleDataset y = DatasetFactory.zeros(l);
		String label = null;
		for (int i = 0; i < l; i++) {
			DataOptions d = list.get(i);
			LoadedFile f = d.getParent();
			if (label == null) {
				label = f.getLabelName();
			}
			PlottableObject po = d.getPlottableObject();
			NDimensions nd = po.getNDimensions();
			SliceND slice = nd.buildSliceND();
			Object[] options = nd.getOptions();
			IDataset[] data = null;
			try {
				data = mode.sliceForPlot(d.getLazyDataset().getSliceView(), slice, options, system);
			} catch (Exception e) {
				logger.error("Could not slice data for {} from {}", n, f.getFilePath(), e);
			}

			Dataset lv = f.getLabelValue();
			x.set(lv == null ? i : lv.getDouble(), i);
			Dataset pd = DatasetUtils.convertToDataset(data[0]);
			y.set(pd.getDouble(), i);
		}
		if (label == null || label.isEmpty()) {
			label = list.get(0).getFilePath();
		}
		x.setName(label);
		y.setName(n);
		try {
			AxesMetadata am = MetadataFactory.createMetadata(AxesMetadata.class, 1);
			am.setAxis(0, x);
			y.addMetadata(am);
		} catch (MetadataException e) {
			logger.error("Could not create axes metadata for plotting", e);
		}

		final IDataset[] plotData = new IDataset[] {y};
		return () -> {
			try {
				mode.displayData(plotData, null, system, list.get(0));
			} catch (Exception e) {
				logger.error("Error displaying data", e);
			}
		};
	}


	public IPlotMode[] getPlotModes(DataOptions dOptions) {
		Integer rank = getDataRank(dOptions);
		if (rank == null) return null;

		return getPlotModes(rank);
	}
	
	public IPlotDataModifier[] getCurrentPlotModifiers() {
		int minimumRank = currentMode.getMinimumRank();
		return getPlotModifiers(minimumRank);
	}
	
	private Integer getDataRank(DataOptions dOptions) {
		if (dOptions == null) return null;
		
		return PlotShapeUtils.getPlottableRank(dOptions.getLazyDataset(), dOptions.getParent() instanceof IRefreshable);
	}
	
	private IPlotDataModifier[] getPlotModifiers(int rank) {
		
		List<IPlotDataModifier> m = new ArrayList<>();
		for (IPlotDataModifier mod : modifiers) {
			if (mod.supportsRank(rank)) {
				m.add(mod);
			}
		}
		
		return m.toArray(new IPlotDataModifier[m.size()]);
		
	}
	
	public IPlotMode[] getPlotModes(int rank) {
		
		List<IPlotMode> m = new ArrayList<>();
		for (IPlotMode mode : modes) {
			if (mode.getMinimumRank() <= rank) m.add(mode);
		}
		
		return m.toArray(new IPlotMode[m.size()]);
		
	}
	
	private IPlotMode getDefaultMode(int rank) {
		if (rank > 1) return modes[MODE_2D];
		return modes[0];
	}
	
	public void enablePlotModifier(IPlotDataModifier modifier) {
		if (modifier == currentModifier) return;
		if (modifier != null) modifier.configure(getPlottingSystem());
		currentModifier = modifier;
		forceReplot();
	}
	
	@Override
	public void forceReplot() {
		//should not force a replot while mode is switching
		if (modeSwitching) return;
		updatePlotStateInJob(currentMode, false);
	}
	
	@Override
	public void replotOnSlice(DataOptions option) {
		if (modeSwitching) return;

		if (!coSlicingEnabled || option == null || !currentMode.supportsMultiple()) {
			forceReplot();
			return;
		}
		
		//dont co-slice if not plotted
		if (!option.isSelected() || !option.getParent().isSelected()) {
			forceReplot();
			return;
		}
		
		IPlotMode mode = currentMode;
		
		SliceND s = option.getPlottableObject().getNDimensions().buildSliceND();
		Slice[] sarray = s.convertToSlice();
		int[] dd = mode.getDataDimensions(option.getPlottableObject().getNDimensions().getOptions());
		int[] shape = s.getSourceShape();
		
		//validator in this case should always return true
		//we are just using it to do the iteration in the file controller
		IFileStateValidator v = new IFileStateValidator() {

			@Override
			public boolean validate(LoadedFile f) {

				if (!f.isSelected()) return true;

				for (DataOptions o : f.getDataOptions()) {
					if (!o.isSelected()) continue;
					if (option.getName().equals(o.getName()) && option.getFilePath().equals(o.getFilePath())) continue;

					int rank = shape.length;
					int[] shapeInner = o.getLazyDataset().getShape();

					if (rank != shapeInner.length) {
						continue;
					}

					int[] dDinner = mode.getDataDimensions(o.getPlottableObject().getNDimensions().getOptions());

					if (Arrays.equals(dd, dDinner)) {

						NDimensions nDinner = o.getPlottableObject().getNDimensions();

						HashSet<Integer> dimSet = Arrays.stream(dDinner)
								.boxed()
								.collect(Collectors.toCollection(HashSet::new));

						boolean canCoSlice = true;

						for (int i = 0 ; i < rank; i++) {
							if (dimSet.contains(i)) continue;
							if (shape[i] != shapeInner[i]) {
								canCoSlice = false;
								break;
							}
						}

						if (canCoSlice) {
							for (int i = 0 ; i < rank; i++) {
								if (dimSet.contains(i)) continue;
								nDinner.setSlice(i, sarray[i].clone());
							}
						}
					}
				}	

				return true;
			}
		};

		//Don't get the file controller to validate directly as that triggers a state change
		//event which is not useful here (file/dataset state has not changed)
		fileController.getLoadedFiles().stream().forEach(v::validate);

		updatePlotStateInJob(currentMode, false);
	}
	
	public IPlotDataModifier getEnabledPlotModifier() {
		return currentModifier;
	}
	
	public void switchPlotMode(IPlotMode mode, DataOptions dOption) {
		
		//if not selected for plot, just update internal state and return
		if (!dOption.isSelected() || !dOption.getParent().isSelected()) {
			updateInnerPlotMode(dOption, mode);
			return;
		}
		
		if (mode == currentMode) return;
		
		try {
			modeSwitching = true;
			getPlottingSystem().reset();
			getPlottingSystem().setTitle("");
			
			currentMode = mode;
			updateInnerPlotMode(dOption, currentMode);
			
			updateFileState(dOption,currentMode);
			updatePlotStateInJob(currentMode, false);
		} finally {
			modeSwitching = false;
		}
		
		for (PlotModeChangeEventListener l : listeners) l.plotModeChanged();
	}
	
	private void updateInnerPlotMode(DataOptions d, IPlotMode m) {
		
		PlottableObject po = d.getPlottableObject();
		if (po.getPlotMode() != m) {
			NDimensions nd = po.getNDimensions();
			nd.setOptions(m.getOptions());
			d.setPlottableObject(new PlottableObject(m, nd));
		}
		
	}

	private void updatePlotStateInJob(IPlotMode mode, boolean resetIfNoData){
		final List<? extends IDataPackage> state = fileController.getImmutableFileState();

		if (resetIfNoData && state.isEmpty()) {
			system.reset();
			if (system.getSelectedXAxis() != null) system.getSelectedXAxis().setTitle("");
			if (system.getSelectedYAxis() != null) system.getSelectedYAxis().setTitle("");
			system.setTitle("");
		}

		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try {
					updatePlotState(state, mode);
				} catch (Exception e) {
					logger.error("Error in plot controller runnable",e);
				}
				
			}
		};
		
		atomicRunnable.set(r);
		
		atomicFuture.set(executor.submit(new Runnable() {
			
			@Override
			public void run() {
				Runnable run = atomicRunnable.getAndSet(null);
				if (run == null) return;
				run.run();
				
			}
		}));
	}
	
	private void updateFileState(DataOptions option, IPlotMode mode) {
		
		IFileStateValidator v = new IFileStateValidator() {
			
			@Override
			public boolean validate(LoadedFile f) {
				
					if (!option.isSelected() || !option.getParent().isSelected()) {
						//state should be valid on deselection only
						return true;
					}
				
					if (!f.isSelected()) return true;
					boolean thisFile = f == option.getParent();
					
					for (DataOptions o : f.getDataOptions()) {
						if (!o.isSelected()) continue;
						if (option.getName().equals(o.getName()) && option.getFilePath().equals(o.getFilePath())) continue;
						
						if (!mode.supportsMultiple() || o.getPlottableObject().getPlotMode() != mode) {
							if (thisFile) {
								o.setSelected(false);
							} else {
								f.setSelected(false);
							}
						}
					}	
				
				return false;
			}
		};
		
		fileController.validateState(v);
		

	}

	@Override
	public IPlottingSystem<?> getPlottingSystem() {
		if (system == null) {
			system = plotService.getPlottingSystem("Plot");
		}
		return system;
	}

	public IPlotMode getCurrentMode() {
		return currentMode;
	}
	
	private PlottableObject getPlottableObject(DataOptions d, IPlotMode mode) {
		
		NDimensions nd = d.buildNDimensions();
		
		ILazyDataset lz = d.getLazyDataset();
		int[] max = null;
		if (lz instanceof IDynamicDataset) {
			max = ((IDynamicDataset) lz).getMaxShape();
		}
		
		int rank = PlotShapeUtils.getPlottableRank(lz, d.getParent() instanceof IRefreshable);
		
		if (mode != null) {
			IPlotMode[] plotModes = getPlotModes(rank);
			for (IPlotMode m : plotModes) {
				if (mode.equals(m)) {
					nd.setOptions(m.getOptions(), max);
					return  new PlottableObject(m, nd);
				}
			}
		}
		
		IPlotMode defaultMode = getDefaultMode(rank);
		nd.setOptions(defaultMode.getOptions(), max);
		
		return new PlottableObject(defaultMode, nd);
	}
	
	public void waitOnJob(){
		
		Future<?> future = atomicFuture.get();
		if (future != null) {
			try {
				future.get();
			} catch (Exception e) {
				logger.info("Error from future", e);
			} 
		}
		
	}

	@Override
	public void dispose() {
		if (system != null && !system.isDisposed()) {
			try {
				system.dispose();
			} catch (Exception e) {
				logger.error("Error disposing plot");
			}
			
		}
		system = null;
	}

	@Override
	public ITraceColourProvider getColorProvider() {
		return colorProvider;
	}

	@Override
	public void setColorProvider(ITraceColourProvider colorProvider) {
		this.colorProvider = colorProvider;
		if (colorcache != null) {
			colorcache.stream().forEach(Color::dispose);
			colorcache = null;
		}
		if (colorProvider == null) {
			getPlottingSystem().clearTraces();
		}

		updatePlotStateInJob(currentMode, false);
	}

	@Override
	public void initialise(LoadedFile file) {
		List<DataOptions> dataOptions = new ArrayList<>();
		
		boolean mayGrow = file instanceof IRefreshable;
				
		if (mayGrow) {
			dataOptions = ((IRefreshable) file).getUninitialisedDataOptions();
		} else {
			dataOptions = file.getDataOptions(false);
		}
		
		for (DataOptions d : dataOptions) {
			PlottableObject po = d.getPlottableObject();
			if (po == null) {
				ILazyDataset lz = d.getLazyDataset();
				int rank = PlotShapeUtils.getPlottableRank(lz,mayGrow);
				
				po = getPlottableObject(d, getDefaultMode(rank));
				d.setPlottableObject(po);
			}
		}
	}
	
	private void firePlotEvent(PlotEventObject event) {
		for (PlotModeChangeEventListener l : listeners) {
			l.plotStateEvent(event);
		}
	}
	
	@Override
	public void addPlotModeListener(PlotModeChangeEventListener l) {
		listeners.add(l);
	}
	
	@Override
	public void removePlotModeListener(PlotModeChangeEventListener l) {
		listeners.remove(l);
	}
	
	public boolean isCoSlicingEnabled() {
		return coSlicingEnabled;
	}

	public void setCoSlicingEnabled(boolean coSlicingEnabled) {
		this.coSlicingEnabled = coSlicingEnabled;
	}
}
