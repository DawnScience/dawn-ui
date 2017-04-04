package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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
public class PlotController {
	
	private IPlottingService pService;
	private IPlottingSystem system;

	private IPlotMode[] modes = new IPlotMode[]{new PlotModeXY(), new PlotModeImage(), new PlotModeSurface()};
	private IPlotMode currentMode;
	
	private IFileController fileController = ServiceManager.getFileController();
	
	private ISliceChangeListener sliceListener;
	
	private Set<PlotModeChangeEventListener> listeners = new HashSet<PlotModeChangeEventListener>();
	
	private ExecutorService executor;
	private AtomicReference<Runnable> atomicRunnable = new AtomicReference<>();
	private AtomicReference<Future<?>> atomicFuture = new AtomicReference<Future<?>>();
	
	private static String id = "org.dawnsci.prototype.nano.model.PlotManager";
	
	private final static Logger logger = LoggerFactory.getLogger(PlotController.class);
	
	public PlotController (IPlottingSystem system) {
		this.system = system;
		init();
	}
	
	public PlotController(IPlottingService p) {
		this.pService = p;
		this.currentMode = modes[0];
		init();
		
	}
	
	private void init(){
		
		executor = Executors.newSingleThreadExecutor();
		
		fileController.addStateListener(new FileControllerStateEventListener() {
			
			@Override
			public void stateChanged(FileControllerStateEvent event) {
				
				if (!event.isSelectedDataChanged() && !event.isSelectedFileChanged()) return;
				updateOnFileStateChange();	
			}
		});
		
		sliceListener = new ISliceChangeListener() {

			@Override
			public void sliceChanged(SliceChangeEvent event) {
				//respond to this happening elsewhere
				if (event.isOptionsChanged()) return;
				if (!fileController.getCurrentFile().isSelected()) return;
				if (!fileController.getCurrentDataOption().isSelected()) return;
				final List<DataStateObject> state =fileController.getImmutableFileState();
				
				updatePlotStateInJob(state, currentMode);	
			};
		};
	}
	
	private void updateOnFileStateChange() {
		if (getPlottingSystem() == null) return;
		
		DataOptions dOption = fileController.getCurrentDataOption();
		LoadedFile file = fileController.getCurrentFile();
		if (dOption == null) { 
			updatePlotStateInJob(null, currentMode);
			return;
		}
		
		boolean selected = file.isSelected() && dOption.isSelected();
		
		PlottableObject plotObject = dOption.getPlottableObject();
		IPlotMode localMode = currentMode;
		
		if (plotObject != null && plotObject.getPlotMode() != currentMode && selected) {
			currentMode = plotObject.getPlotMode();
			localMode = currentMode;
		} else if (plotObject == null) {
			plotObject = getPlottableObject();
			if (selected) currentMode = plotObject.getPlotMode();
			localMode = plotObject.getPlotMode();
		}
		dOption.getPlottableObject().getNDimensions().addSliceListener(sliceListener);
		//update file state
		if (selected) updateFileState(file, dOption, currentMode);
		firePlotModeListeners(localMode, getCurrentPlotModes());
		//make immutable state object
		final List<DataStateObject> state = fileController.getImmutableFileState();
		//update plot
		updatePlotStateInJob(state, currentMode);
		
	}
	
	private void updatePlotState(List<DataStateObject> state, IPlotMode mode) {

		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				Cursor cursor = Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT);
				Shell[] shells = Display.getCurrent().getShells();
				for (Shell s : shells) {
					if (s!= null) {
						s.setCursor(cursor);
						s.setData(id);
					}
				}
				
			}
		});
		
		IPlottingSystem system = getPlottingSystem();

		final Map<DataOptions, List<ITrace>> traceMap = collectTracesFromPlot();

		if (state == null) state = new ArrayList<DataStateObject>();
		
		Map<DataOptions, List<ITrace>> updateMap = new HashMap<>();
		//have to do multiple iterations so image traces arent removed after correct
		// one added
		for (DataStateObject object : state) {
			if (traceMap.get(object.getOption()) != null && traceMap.get(object.getOption()).get(0) != null && mode.isThisMode((traceMap.get(object.getOption()).get(0)))) updateMap.put(object.getOption(), traceMap.remove(object.getOption()));	
		}
		
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				for (List<ITrace> traces : traceMap.values()) {
					for (ITrace t : traces) system.removeTrace(t);
				}
			}
		});
		
		
		for (DataStateObject object : state) {

			List<ITrace> list = updateMap.remove(object.getOption());
			
			if (list == null) list = new ArrayList<ITrace>();

			if (!object.isChecked() && !list.isEmpty()) {
				for (ITrace t : list){
					system.removeTrace(t);
				}
			} else if (object.isChecked()) {
				updatePlottedData(object, list, currentMode);
			}
		}
		
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				Shell[] shells = Display.getCurrent().getShells();
				for (Shell s : shells) {
					if (s!= null && id.equals(s.getData())) {
						s.setCursor(null);
					}
				}
			}
		});
	}
	
	private void updatePlottedData(DataStateObject stateObject,final List<ITrace> traces, IPlotMode mode) {
		//remove traces if not the same as mode
		//update the data in the plot
		
		IPlottingSystem system = getPlottingSystem();
		
		PlottableObject plotObject = stateObject.getPlotObject();
		NDimensions nd = plotObject.getNDimensions();
		
		
		String[] axes = nd.buildAxesNames();
		SliceND slice= nd.buildSliceND();
		Object[] options = nd.getOptions();
		
		DataOptions dataOp = stateObject.getOption();
		dataOp.setAxes(axes);
		
		IDataset[] data = null;
		try {
			ILazyDataset view = dataOp.getLazyDataset().getSliceView();
			view.setName(dataOp.getFilePath() + ":" + dataOp.getName());

			data = mode.sliceForPlot(view, slice,options);
		} catch (Exception e) {
			logger.error("Could not slice data for plotting", e);
		}
		
		if (data == null) return;	
		
		SourceInformation si = new SourceInformation(dataOp.getFilePath(), dataOp.getName(), dataOp.getLazyDataset());
		SliceInformation s = new SliceInformation(slice, slice, new SliceND(dataOp.getLazyDataset().getShape()), mode.getDataDimensions(options), 1, 0);
		SliceFromSeriesMetadata md = new SliceFromSeriesMetadata(si, s);
		
		for (IDataset d : data) d.setMetadata(md);
	
		final IDataset[] finalData = data;
		
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
					
					try {
						mode.displayData(finalData, traces.isEmpty() ? null : traces.toArray(new ITrace[traces.size()]), system, dataOp);
					} catch (Exception e) {
						logger.error("Error displaying data", e);
					}
				
			getPlottingSystem().repaint();
			}
		});
		
	}
	
	
	public IPlotMode[] getCurrentPlotModes() {
		if (fileController.getCurrentDataOption() == null) return null;
		
		int[] shape = fileController.getCurrentDataOption().getLazyDataset().getShape();
		shape = ShapeUtils.squeezeShape(shape, false);
		int rank = shape.length;

		return getPlotModes(rank);
	}
	
	private IPlotMode[] getPlotModes(int rank) {
		
		List<IPlotMode> m = new ArrayList<>();
		for (IPlotMode mode : modes) {
			if (mode.getMinimumRank() <= rank) m.add(mode);
		}
		
		return m.toArray(new IPlotMode[m.size()]);
		
	}
	
	private IPlotMode getDefaultMode(int rank) {
		if (rank > 1) return modes[1];
		return modes[0];
	}
	
	public void switchPlotMode(IPlotMode mode) {
		if (mode == currentMode) return;
		
		DataOptions dOption = fileController.getCurrentDataOption();
		
		boolean selected =  dOption.isSelected() && fileController.getCurrentDataOption().isSelected();
		if (!selected) return;
		
		currentMode = mode;
		PlottableObject po = getPlottableObject();
		NDimensions nd = po.getNDimensions();
		nd.setOptions(currentMode.getOptions());
		
		dOption.setPlottableObject(new PlottableObject(currentMode, nd));
		
		updateFileState(fileController.getCurrentFile(), fileController.getCurrentDataOption(),currentMode);
		final List<DataStateObject> state = fileController.getImmutableFileState();
		//update plot
		updatePlotStateInJob(state, currentMode);
	}
	
	private void updatePlotStateInJob(List<DataStateObject> state, IPlotMode mode){
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				updatePlotState(state, mode);
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
	
	private void updateFileState(LoadedFile file, DataOptions option, IPlotMode mode) {
		
		List<IDataObject> objects = new ArrayList<>();
		
		for (LoadedFile f : fileController.getLoadedFiles()) {
			if (!f.isSelected()) continue;
			
			boolean thisFile = f == file;
			
			for (DataOptions o : f.getDataOptions()) {
				if (!o.isSelected()) continue;
				if (option == o) continue;
				if (o.getPlottableObject() == null) continue;
				if (!mode.supportsMultiple() || o.getPlottableObject().getPlotMode() != mode) {
					if (thisFile) {
						objects.add(o);
					} else {
						objects.add(f);
					}
				}
			}	
		}
		
		if (!objects.isEmpty()) fileController.deselect(objects);
	}

	private Map<DataOptions, List<ITrace>> collectTracesFromPlot() {
		
		IPlottingSystem system = getPlottingSystem();
		
		Collection<ITrace> traces = system.getTraces();
		
		Map<DataOptions, List<ITrace>> optionTraceMap = new HashMap<>();
		
		for (ITrace t : traces) {
			if (t.getUserObject() instanceof DataOptions) {
				DataOptions option = (DataOptions)t.getUserObject();
				if (optionTraceMap.containsKey(option)) {
					optionTraceMap.get(option).add(t);
				} else {
					List<ITrace> list = new ArrayList<ITrace>();
					list.add(t);
					optionTraceMap.put(option, list);
				}
			}
		}
		
		return optionTraceMap;
	}
	
	private IPlottingSystem getPlottingSystem() {
		if (system == null) {
			system = pService.getPlottingSystem("Plot");
		}
		return system;
	}
	
	private void firePlotModeListeners(IPlotMode mode, IPlotMode[] modes) {
		PlotModeEvent e = new PlotModeEvent(this, mode, modes);
		for (PlotModeChangeEventListener l : listeners) l.plotModeChanged(e);
	}
	
	public void addPlotModeListener(PlotModeChangeEventListener l) {
		listeners.add(l);
	}
	
	public void removePlotModeListener(PlotModeChangeEventListener l) {
		listeners.remove(l);
	}
	

	public IPlotMode getCurrentMode() {
		return currentMode;
	}
	
	public PlottableObject getPlottableObject(){
		DataOptions dataOptions = fileController.getCurrentDataOption();
		if (dataOptions == null) return null;
		if (dataOptions.getPlottableObject() != null) return dataOptions.getPlottableObject();
		
		NDimensions nd = dataOptions.buildNDimensions();
		int[] shape = ShapeUtils.squeezeShape(dataOptions.getLazyDataset().getShape(), false);
		int rank = shape.length;
		IPlotMode defaultMode = getDefaultMode(rank);
		nd.setOptions(defaultMode.getOptions());
		
		PlottableObject po = new PlottableObject(defaultMode, nd);
		dataOptions.setPlottableObject(po);
		
		return po;
		
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
}
