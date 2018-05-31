package org.dawnsci.plotting.tools.reduction;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;


/** Very much like TimeResolvedToolPage, but without the stuff that makes me want to cut myself ;-).
 * 
 * @author awf63395
 *
 */
public class DataReduction2DTool extends AbstractToolPage implements IRegionListener, ITraceListener {

	private SashForm rootComposite;
	private IPlottingSystem<Composite> plottingSystem;
	private IImageTrace imageTrace;
	private boolean dataLoaded = false;
	private DataReduction2DToolModel toolPageModel = new DataReduction2DToolModel();
	private DataReduction2DToolSpectraTableComposite spectraTableComposite; 
	private DataReduction2DToolSpectraRegionComposite spectraRegionTableComposite;
	private final List<ILineTrace> stackList = new LinkedList<>();
	private Label statusLabel;
	private Dataset[] axes0;
	private Dataset[] axes1;

	
	@Override
	public void createControl(Composite parent) {

		rootComposite = new SashForm(parent, SWT.VERTICAL);
		rootComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final SashForm tableComposite = new SashForm(rootComposite, SWT.HORIZONTAL);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createSpectraTable(tableComposite);
		createSpectraRegionTable(tableComposite);
		tableComposite.setWeights(new int[]{1,1});
		createPlotView(rootComposite);
		rootComposite.setWeights(new int[]{1,3});
		createActions();
		doBinding();
		loadExistingData();
	}

	private String getDataFilePath(IImageTrace image) {
		IMetadata metaData = image.getData().getFirstMetadata(IMetadata.class);
		if (metaData != null) {
			return metaData.getFilePath();
		}
		SliceFromSeriesMetadata ssm = image.getData().getFirstMetadata(SliceFromSeriesMetadata.class);
		if (ssm != null) {
			return ssm.getFilePath();
		}
		if (getPlottingSystem().getPart() instanceof EditorPart) {
			IEditorInput editorInput = ((EditorPart) getPlottingSystem().getPart()).getEditorInput();
			if (editorInput instanceof FileEditorInput) {
				return ((FileEditorInput) editorInput).getURI().getPath();
			}
			
			if (editorInput instanceof IURIEditorInput) {
				URI uri = ((IURIEditorInput) editorInput).getURI();
				return uri.getPath();
			}
		}
		return null;
	}

	private void removeFromPlottingSystem(ILineTrace trace) {
		int index = stackList.indexOf(trace);
		stackList.remove(index);
		plottingSystem.removeTrace(trace);
		for (ILineTrace myTrace : stackList.subList(index, stackList.size())) {
			DoubleDataset xData = DatasetUtils.cast(DoubleDataset.class, myTrace.getXData());
			DoubleDataset yData = DatasetUtils.cast(DoubleDataset.class, myTrace.getYData());
			yData.isubtract(toolPageModel.getTraceStack());
			myTrace.setData(xData, yData);
		}
		if (!plottingSystem.isDisposed())
			plottingSystem.repaint();
	}
	
	private ILineTrace plotSpectrum(DataReduction2DToolSpectrumDataNode spectrum) {
		int index  = spectrum.getIndex();
		DoubleDataset data = (DoubleDataset) imageTrace.getData().getSlice(new int[]{index,0}, new int[]{index + 1, imageTrace.getData().getShape()[1]}, new int[]{1, 1});
		data.squeeze();
		String name = Integer.toString(spectrum.getIndex());
		ILineTrace trace = plottingSystem.createLineTrace(name);
		IDataset energyClone = null;
		if (axes1.length > 0) {
			energyClone = axes1[0].clone().squeeze(); // FIXME
			energyClone.setName(axes1[0].getName());
		}
		IDataset dataClone = data.clone();
		dataClone.setName(name.trim());
		trace.setData(energyClone, dataClone);
		trace.setUserObject(spectrum);
		addToPlottingSystem(trace);
		return trace;
	}
	
	private void addToPlottingSystem(ILineTrace trace) {
		((DoubleDataset) trace.getYData()).iadd(stackList.size() * toolPageModel.getTraceStack());
		plottingSystem.addTrace(trace);
		stackList.add(trace);
		plottingSystem.repaint();
	}
	
	private void doBinding() {
		spectraTableComposite.getSpectraTable().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selectedItems = ((Table) e.getSource()).getSelection();
				List<DataReduction2DToolSpectrumDataNode> newSelection = new ArrayList<>();
				for (TableItem item: selectedItems) {
					Object spectrumNode = item.getData("spectrumNode");
					if (spectrumNode != null && spectrumNode instanceof DataReduction2DToolSpectrumDataNode) {
						DataReduction2DToolSpectrumDataNode spectrum = (DataReduction2DToolSpectrumDataNode) spectrumNode;
						newSelection.add(spectrum);
					}
				}
				// currently selected spectra have been identified
				// remove those that have been deselected
				BusyIndicator.showWhile(Display.getCurrent(), () -> {
					spectraTableComposite.getSelectedSpectraList().removeAll(newSelection);
					spectraTableComposite.getSelectedSpectraList().forEach(spectrum -> {
						if (!plottingSystem.isDisposed()) {
							removeFromPlottingSystem((ILineTrace) spectrum.getTrace());
						}
						spectrum.clearTrace();
					});
					for (DataReduction2DToolSpectrumDataNode spectrum : newSelection) {
						if (spectrum.getTrace() == null && !plottingSystem.isDisposed()) {
							ITrace trace = DataReduction2DTool.this.plotSpectrum(spectrum);
							spectrum.setTrace(trace);
						}
					}
				});
				
				spectraTableComposite.getSelectedSpectraList().clear();
				spectraTableComposite.getSelectedSpectraList().addAll(newSelection);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		spectraRegionTableComposite.getSelectedRegionSpectraList().addListChangeListener(new IListChangeListener() {
			private static final int ADDED_ALPHA_FOR_SELECTED_VALUE = 20;
			@Override
			public void handleListChange(ListChangeEvent event) {
				event.diff.accept(new ListDiffVisitor() {
					@Override
					public void handleRemove(int index, Object element) {
						IRegion region = ((DataReduction2DToolSpectraRegionDataNode) element).getRegion();
						if (region.getAlpha() > 20) {
							region.setAlpha(region.getAlpha() - ADDED_ALPHA_FOR_SELECTED_VALUE);
						}
						DataReduction2DTool.this.getPlottingSystem().repaint();
					}

					@Override
					public void handleAdd(int index, Object element) {
						IRegion region = ((DataReduction2DToolSpectraRegionDataNode) element).getRegion();
						if (region.getAlpha() < 255 - ADDED_ALPHA_FOR_SELECTED_VALUE) {
							region.setAlpha(region.getAlpha() + ADDED_ALPHA_FOR_SELECTED_VALUE);
						} else {
							region.setAlpha(255);
						}
						DataReduction2DTool.this.getPlottingSystem().repaint();
					}
				});
			}
		});

		spectraRegionTableComposite.addPropertyChangeListener(DataReduction2DToolSpectraRegionComposite.SPECTRA_REGION_TRACE_SHOULD_ADD, evt -> {
			DataReduction2DToolSpectraRegionDataNode spectraRegion = (DataReduction2DToolSpectraRegionDataNode) evt.getNewValue();
			addTracesForRegion(spectraRegion);
		});

		spectraRegionTableComposite.addPropertyChangeListener(DataReduction2DToolSpectraRegionComposite.SPECTRA_REGION_TRACE_SHOULD_REMOVE, evt -> {
			DataReduction2DToolSpectraRegionDataNode spectraRegion = (DataReduction2DToolSpectraRegionDataNode) evt.getNewValue();
			removeTracesForRegion(spectraRegion);
		});
	}
	
	private void removeTracesForRegion(DataReduction2DToolSpectraRegionDataNode region) {
		for(ITrace object : region.getTraces()) {
			removeFromPlottingSystem((ILineTrace) object);
		}
		region.clearTrace();
	}
	
	private void addTracesForRegion(DataReduction2DToolSpectraRegionDataNode region) {
		DoubleDataset data = region.getDataset((DoubleDataset) imageTrace.getData());
		int noOfSpectra = data.getShape()[0];
		int noOfChannels = data.getShape()[1];
		String name = region.toString();
		for (int i = 0; i < noOfSpectra; i++) {
			DoubleDataset dataItem = (DoubleDataset) data.getSliceView(new int[]{i, 0}, new int[]{i + 1, noOfChannels}, null);
			dataItem.setName(name + " " + i);
			dataItem.squeeze();
			ILineTrace trace = plottingSystem.createLineTrace(name + " " + i);
			region.addTrace(trace);
			trace.setData(axes1.length == 0 ? null : axes1[0].getSlice(null, new int[]{1, axes1[0].getShape()[1]}, null).squeeze(), dataItem); // FIXME
			addToPlottingSystem(trace);
		}
	}
	
	private void loadExistingData() {
		if (getPlottingSystem() != null && getPlottingSystem().getTracesByClass(IImageTrace.class).size() == 1) {
			IImageTrace trace = getPlottingSystem().getTracesByClass(IImageTrace.class).toArray(new IImageTrace[1])[0];
			validateAndLoadSpectra((IImageTrace) trace);
		}
	}

	private void createActions() {
		createToolPageActions();
	}

	private Dataset[] getAllAxes(int axisNr, AxesMetadata firstAxesMetadata) {
		List<Dataset> rvList = new ArrayList<>();
		ILazyDataset[] allAxes = firstAxesMetadata != null ? firstAxesMetadata.getAxis(axisNr) : new ILazyDataset[0];
		
		if (allAxes == null)
			allAxes = new ILazyDataset[0];
		
		for (ILazyDataset axis : allAxes) {
			if (axis == null)
				continue;
			try {
				Dataset tempAxis = DatasetUtils.sliceAndConvertLazyDataset(axis);
				rvList.add(tempAxis);
			} catch (DatasetException e) {
				// move on to the next
			}
		}
		
		return rvList.toArray(new Dataset[rvList.size()]);
	}
	
	private void validateAndLoadSpectra(IImageTrace image) {
		if (dataLoaded)
			return;
		axes0 = null;
		axes1 = null;
		imageTrace = image;
		String fullFilePath = getDataFilePath(image);
		if (fullFilePath == null) {
			DataReduction2DToolHelper.showError("Could not determine file path", "unknown");
			//not sure what the consequences will be ...
		}
		toolPageModel.setDataFile(fullFilePath == null ? null : new File(fullFilePath));
		// image.getAxes is a bit weird and unreliable to work with
		AxesMetadata firstAxesMetadata = image.getData().getFirstMetadata(AxesMetadata.class);
		
		if (firstAxesMetadata != null) {
			axes0 = getAllAxes(0, firstAxesMetadata);
			axes1 = getAllAxes(1, firstAxesMetadata);
		}

		// update model??
		toolPageModel.setAxes0(axes0);
		toolPageModel.setAxes1(axes1);
		toolPageModel.setDataImagePlotting(getPlottingSystem());
		toolPageModel.setImageTrace(imageTrace);
		
		int totalNumberOfSpectra = image.getData().getShape()[0]; // TODO: support both axes!
		final List<DataReduction2DToolSpectrumDataNode> spectrumDataNodes = toolPageModel.getSpectrumDataNodes();
		spectrumDataNodes.clear();
		IntStream.range(0, totalNumberOfSpectra)
				 .forEachOrdered(i -> spectrumDataNodes.add(new DataReduction2DToolSpectrumDataNode(i, Arrays.stream(axes0).mapToDouble(axis -> axis.getDouble(i,0)).toArray())));
		
		toolPageModel.getAxesNames().clear();
		toolPageModel.getAxesNames().addAll(Arrays.stream(axes0)
												  .map(axis -> MetadataPlotUtils.removeSquareBrackets(axis.getName().substring(axis.getName().lastIndexOf('/')+1))).collect(Collectors.toList())
												  );
		if (axes1.length == 0)
			plottingSystem.getSelectedXAxis().setTitle("Indices");
		else
			plottingSystem.getSelectedXAxis().setTitle(axes1[0].getName());
	
		if (spectraTableComposite != null)
			spectraTableComposite.createDataColumnsAndPopulate();

		dataLoaded = true;
	}

	private void createSpectraTable(Composite parent) {
		spectraTableComposite = new DataReduction2DToolSpectraTableComposite(parent, SWT.None, toolPageModel);
		spectraTableComposite.setLayout(DataReduction2DToolHelper.createGridLayoutWithNoMargin(1, false));
		toolPageModel.addPropertyChangeListener(DataReduction2DToolModel.TRACE_STACK_PROP_NAME, evt	-> updateStackOffset((double) evt.getOldValue(), (double) evt.getNewValue()));
		spectraTableComposite.addPropertyChangeListener(DataReduction2DToolSpectraTableComposite.NEW_REGION_PROP_NAME, evt -> addSpectraRegion((DataReduction2DToolSpectraRegionDataNode) evt.getNewValue()));
	}

	private void createSpectraRegionTable(Composite parent) {
		spectraRegionTableComposite = new DataReduction2DToolSpectraRegionComposite(parent, SWT.None, toolPageModel);
		spectraRegionTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//repopulateCachedSpectraRegion();
	}
	
	private void addSpectraRegion(DataReduction2DToolSpectraRegionDataNode spectraRegion) {
		spectraRegionTableComposite.getSpectraRegionList().add(spectraRegion);
	}
	
	private void updateStackOffset(double oldOffset, double newOffset) {
		for (ILineTrace myTrace : stackList) {
			DoubleDataset xData = DatasetUtils.cast(DoubleDataset.class, myTrace.getXData());
			DoubleDataset yData = DatasetUtils.cast(DoubleDataset.class, myTrace.getYData());
			yData.isubtract(stackList.indexOf(myTrace) * oldOffset);
			yData.iadd(stackList.indexOf(myTrace) * newOffset);
			myTrace.setData(xData, yData);
		}
		plottingSystem.repaint();
	}
	
	private void createPlotView(Composite parent) {
		Composite plotParent = new Composite(parent, SWT.None);
		plotParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plotParent.setLayout(new GridLayout(1, true));
		ActionBarWrapper actionbarWrapper = ActionBarWrapper.createActionBars(plotParent, null);
		try {
			if (plottingSystem == null) {
				plottingSystem = PlottingFactory.createPlottingSystem();
				plottingSystem.createPlotPart(plotParent,
						getTitle(),
						actionbarWrapper,
						PlotType.XY,
						this.getViewPart());
				plottingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				plottingSystem.getSelectedXAxis().setAxisAutoscaleTight(true);
				plottingSystem.setRescale(true);
				toolPageModel.setSpectraPlotting(plottingSystem);
			}
		} catch (Exception e) {
			logger.error("Unable to create plotting system", e);
		}
		Composite statusComponent = new Composite(plotParent, SWT.None);
		statusComponent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		statusComponent.setLayout(new GridLayout(1, false));
		statusLabel = new Label(statusComponent, SWT.None);
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return plottingSystem;
		}
		return super.getAdapter(clazz);
	}
	
	@Override
	public Control getControl() {
		return rootComposite;
	}

	@Override
	public void setFocus() {
		if (!rootComposite.isDisposed()) {
			rootComposite.setFocus();
		}	
	}

	@Override
	public void traceCreated(TraceEvent evt) {
		
	}

	private void clearData() {
		if (spectraRegionTableComposite != null) {
			spectraTableComposite.getSelectedSpectraList().forEach(spectrum -> {
				if (!plottingSystem.isDisposed()) {
					removeFromPlottingSystem((ILineTrace) spectrum.getTrace());
				}
				spectrum.clearTrace();
			});
		}
		dataLoaded = false;
		if (spectraTableComposite != null) {
			spectraTableComposite.clearSelectedSpectraList();
			if (!spectraTableComposite.getSpectraTable().isDisposed())
				spectraTableComposite.getSpectraTable().removeAll();
		}
		if (spectraRegionTableComposite != null)
			spectraRegionTableComposite.clearRegionData();
		toolPageModel.getDeletedIndices().clear();
		toolPageModel.getSpectrumDataNodes().clear();
	}
	
	@Override
	public void traceUpdated(TraceEvent evt) {
		if (evt.getSource() instanceof IImageTrace) {
			clearData();
			validateAndLoadSpectra((IImageTrace) evt.getSource());
		}
	}

	@Override
	public void traceAdded(TraceEvent evt) {
		if (evt.getSource() instanceof IImageTrace) {
			validateAndLoadSpectra((IImageTrace) evt.getSource());
		}	
	}

	@Override
	public void traceRemoved(TraceEvent evt) {
		clearData();
	}

	@Override
	public void tracesUpdated(TraceEvent evt) {
		
	}

	@Override
	public void tracesRemoved(TraceEvent evet) {
		evet.getSource();
	}

	@Override
	public void tracesAdded(TraceEvent evt) {
	}

	@Override
	public void traceWillPlot(TraceWillPlotEvent evt) {
	}

	@Override
	public void regionCreated(RegionEvent evt) {
		
	}

	@Override
	public void regionCancelled(RegionEvent evt) {
		
	}

	@Override
	public void regionNameChanged(RegionEvent evt, String oldName) {
		
	}

	@Override
	public void regionAdded(RegionEvent evt) {
		IRegion region = evt.getRegion();
		region.setShowPosition(true);
		if (dataLoaded && region.getRegionType() == RegionType.YAXIS && region.getUserObject() == null) {
			DataReduction2DToolSpectraRegionDataNode spectraRegion = new DataReduction2DToolSpectraRegionDataNode(region, toolPageModel, null);
			addSpectraRegion(spectraRegion);
		}	
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		Object userObject = evt.getRegion().getUserObject();
		if (userObject instanceof DataReduction2DToolSpectraRegionDataNode) {
			DataReduction2DToolSpectraRegionDataNode spectraRegion = (DataReduction2DToolSpectraRegionDataNode) userObject;
			spectraRegionTableComposite.getSpectraRegionList().remove(spectraRegion);
		}
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		
	}

	@Override
	public void deactivate() {
		clearData();
		if (getPlottingSystem() != null) {
			getPlottingSystem().removeRegionListener(this);
			getPlottingSystem().removeTraceListener(this);
			if (getPlottingSystem().getRegions() != null)
				getPlottingSystem().getRegions().stream().forEach(region -> getPlottingSystem().removeRegion(region));
		}
		super.deactivate();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		plottingSystem.getRegions().stream().forEach(region -> plottingSystem.removeRegion(region));
		plottingSystem.getTraces().stream().forEach(trace -> plottingSystem.removeTrace(trace));
		plottingSystem.dispose();
	}
	
	@Override
	public void activate() {
		if (isActive())
			return;
		getPlottingSystem().addRegionListener(this);
		getPlottingSystem().addTraceListener(this);
		super.activate();
		Collection<IImageTrace> traces = getPlottingSystem().getTracesByClass(IImageTrace.class);
		if (!traces.isEmpty())
			validateAndLoadSpectra(traces.stream().findFirst().get());
	}
}
