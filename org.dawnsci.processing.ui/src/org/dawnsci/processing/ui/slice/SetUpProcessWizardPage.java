package org.dawnsci.processing.ui.slice;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.plotting.services.util.DatasetTitleUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext.ConversionScheme;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.slicer.Slicer;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.slicing.api.SlicingFactory;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceEvent;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceListener;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimensionalEvent;
import org.eclipse.dawnsci.slicing.api.system.DimensionalListener;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.dawnsci.slicing.api.system.RangeMode;
import org.eclipse.dawnsci.slicing.api.system.SliceSource;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.dawnsci.slicing.api.tool.ISlicingTool;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetUpProcessWizardPage extends WizardPage {

	private static ILoaderService lservice;
	public static void setLoaderService(ILoaderService s) {
		lservice = s;
	}
	public SetUpProcessWizardPage() {// Used by OSGI only.
		super("Set up input data");
	}

	private ISliceSystem sliceComponent;
	private IPlottingSystem system;
	private IConversionContext context;
	private ComboViewer cviewer;
	private String rootName = null;
	
	private final static Logger logger = LoggerFactory.getLogger(SetUpProcessWizardPage.class);
	
	protected SetUpProcessWizardPage(IConversionContext context) {
		super("Set up input data");
		setDescription("Select dataset, axes, whether to process as images [2D] or lines [1D] and which dimensions of the array are the data dimensions");
		setTitle("Set up data for processing");
		this.context = context;
		context.setConversionScheme(ConversionScheme.PROCESS);
	}
	
	@Override
	public void createControl(Composite parent) {
		
		SashForm sashForm= new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		
		setControl(sashForm);
		
		final Composite left = new Composite(sashForm, SWT.NONE);
		left.setLayout(new GridLayout(2, false));
		Composite right = new Composite(sashForm, SWT.NONE);
		right.setLayout(new GridLayout());
		
		Label l = new Label(left, SWT.NONE);
		l.setText("Select dataset:");
		
		cviewer = new ComboViewer(left);
		cviewer.setContentProvider(new BasicContentProvider());
		cviewer.setLabelProvider(new ViewLabelProvider());
		
		cviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Entry<String, int[]> sel =(Entry<String, int[]>)((StructuredSelection)event.getSelection()).getFirstElement();
				updateDataset(sel.getKey());
				
			}
		});
		
		final Button d1 = new Button(left, SWT.RADIO);
		d1.setText("Line [1D]");
		final Button d2 = new Button(left, SWT.RADIO);
		d2.setText("Image [2D]");
		
		try {
			this.sliceComponent = SlicingFactory.createSliceSystem("org.dawb.workbench.views.h5GalleryView");
		} catch (Exception e) {
			logger.error("Cannot create slice system!", e);
			return;
		}

	    sliceComponent.setRangeMode(RangeMode.MULTI_RANGE);

	    final Control slicer = sliceComponent.createPartControl(left);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		data.minimumHeight=560;
		slicer.setLayoutData(data);
		sliceComponent.setVisible(true);
		sliceComponent.setSliceActionsEnabled(false);
		sliceComponent.setToolbarVisible(false);

		sliceComponent.addAxisChoiceListener(new AxisChoiceListener() {
			
			@Override
			public void axisChoicePerformed(AxisChoiceEvent evt) {
				updatePlot(context);
				
			}
		});
		
		final ISlicingTool image = getImageSlicingTool();
		image.setSlicingSystem(sliceComponent);
		
		final ISlicingTool line = getLineSliceTool();
		line.setSlicingSystem(sliceComponent);
		
		d1.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!d1.getSelection()) return;
				sliceComponent.militarize(line);
				updatePlot(context);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		d2.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!d2.getSelection()) return;
				sliceComponent.militarize(image);
				updatePlot(context);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		sliceComponent.addDimensionalListener(new DimensionalListener() {
			
			@Override
			public void dimensionsChanged(DimensionalEvent evt) {
				updatePlot(context);
			}
		});
		
		cviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Entry<String, int[]> sel =(Entry<String, int[]>)((StructuredSelection)event.getSelection()).getFirstElement();
				updateDataset(sel.getKey());
				if (sel.getValue().length < 2) {
					d1.setSelection(true);
					d2.setSelection(false);
					sliceComponent.militarize(line);
				} else {
					sliceComponent.militarize(image);
					d2.setSelection(true);
					d1.setSelection(false);
				}
				
			}
		});
		
		
		createPlottingSystem(right);
		
		//Everything that needs datasetnames after here
		
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					
					try {
						final Map<String,int[]> datasetNames = getDatasetInfo();
						String dsName = "";
						if (context.getDatasetNames() != null) {
							dsName = context.getDatasetNames().get(0);
						} else {
							dsName = datasetNames.keySet().iterator().next();
						}
						
						final String dn = dsName;

						// TODO Changed this to leave loading image stacks
						// out of this because we are looking for dsName
						// which should be there - is this right?
						final IDataHolder dh  = lservice.getData(context.getFilePaths().get(0), null);
						ILazyDataset lzGlobal = dh.getLazyDataset(dsName);
						//local copy since we are messing with metadata
						final ILazyDataset lz = lzGlobal.getSliceView();
						lz.clearMetadata(null);
						
						Display.getDefault().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								cviewer.setInput(datasetNames);
								
								if (context.getDatasetNames() != null) {
									String name = context.getDatasetNames().get(0);
									int i = 0;
									for (String n: datasetNames.keySet()) {
										if (name.equals(n)) {
											cviewer.getCombo().select(i);
											break;
										}
										
										i++;
									}
								} else {
									cviewer.getCombo().select(0);
								}
								
								left.layout();
								
								Entry<String, int[]> sel =(Entry<String, int[]>)((StructuredSelection)cviewer.getSelection()).getFirstElement();
								
								if (sel.getValue().length < 2) {
									d1.setSelection(true);
									d2.setSelection(false);
								} else {
									d1.setSelection(false);
									d2.setSelection(true);
								}
								
								final SliceSource source = new SliceSource(dh, lz, dn, context.getFilePaths().get(0), false);
								sliceComponent.setData(source);

								updatePlot(context);
								
							}
						});
						
					} catch (Exception e) {
						logger.error("Error getting dataset information",e);
					}
					
				}
			});
		} catch (Exception e) {
			logger.error("Error getting dataset infomation",e);
		}
	}
	
	private void updatePlot(IConversionContext context) {
		Entry<String, int[]> selection = (Entry<String, int[]>)((IStructuredSelection)cviewer.getSelection()).getFirstElement();
		String path = context.getFilePaths().get(0);
		IDataHolder dh;
		try {
			dh = lservice.getData(path, new IMonitor.Stub());
			ILazyDataset lazyDataset = dh.getLazyDataset(selection.getKey());
			//local copy so not to change data holder copies metadata
			lazyDataset = lazyDataset.getSliceView();
			lazyDataset.clearMetadata(null);
			final DimsDataList dims = sliceComponent.getDimsDataList();
			Map<Integer, String> sliceDims = new HashMap<Integer, String>();
			
			for (DimsData dd : dims.iterable()) {
				if (dd.isSlice()) {
					sliceDims.put(dd.getDimension(), String.valueOf(dd.getSlice()));
				} else if (dd.isTextRange()) {
					sliceDims.put(dd.getDimension(), dd.getSliceRange()!=null ? dd.getSliceRange() : "all");
				}
			}
			
			AxesMetadata ax = lservice.getAxesMetadata(lazyDataset, path, sliceComponent.getAxesNames());
			lazyDataset.setMetadata(ax);
			IDataset firstSlice = Slicer.getFirstSlice(lazyDataset, sliceDims);
			
			SlicedDataUtils.plotDataWithMetadata(firstSlice, system, Slicer.getDataDimensions(lazyDataset.getShape(), sliceDims));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void updateDataset(String name) {
		IDataHolder dh;
		try {
			dh = lservice.getData(context.getFilePaths().get(0), null);
			ILazyDataset lz  = dh.getLazyDataset(name);
			//take local copy!
			lz = lz.getSliceView();
			final SliceSource source = new SliceSource(dh, lz, name, context.getFilePaths().get(0), false);
			sliceComponent.setData(source);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		updatePlot(context);
	}
	
	private Map<String, int[]> getDatasetInfo() throws Exception{
		
		final ConversionScheme scheme = context.getConversionScheme();
		final Map<String, int[]>     names = SlicedDataUtils.getDatasetInfo(context.getFilePaths().get(0), scheme);

        rootName = DatasetTitleUtils.getRootName(names.keySet());
        return names;
	}
	
	private Map<String, int[]> sortedByRankThenLength(Map<String, int[]> map) {
		
		List<Entry<String, int[]>> ll = new LinkedList<Entry<String, int[]>>(map.entrySet());
		
		Collections.sort(ll, new Comparator<Entry<String, int[]>>() {

			@Override
			public int compare(Entry<String, int[]> o1, Entry<String, int[]> o2) {
				int val = Integer.compare(o2.getValue().length, o1.getValue().length);
				
				if (val == 0) val = Integer.compare(o1.getKey().length(), o2.getKey().length());
				
				return val;
			}
		});
		
		Map<String, int[]> lhm = new LinkedHashMap<String, int[]>();
		
		for (Entry<String, int[]> e : ll) lhm.put(e.getKey(), e.getValue());
		
		return lhm;
		
	}
	
	
	public void populateContext() {
		Entry<String, int[]> selection = (Entry<String, int[]>)((IStructuredSelection)cviewer.getSelection()).getFirstElement();
		context.setDatasetName(selection.getKey());
		final DimsDataList dims = sliceComponent.getDimsDataList();
		for (DimsData dd : dims.iterable()) {
			if (dd.isSlice()) {
				context.addSliceDimension(dd.getDimension(), String.valueOf(dd.getSlice()));
			} else if (dd.isTextRange()) {
				context.addSliceDimension(dd.getDimension(), dd.getSliceRange()!=null ? dd.getSliceRange() : "all");
			}
		}
		
		context.setAxesNames(sliceComponent.getAxesNames());
	}
	
	private ISlicingTool getLineSliceTool(){
		return new AbstractSlicingTool() {
			
			@Override
			public void militarize(boolean newData) {
				
				
				boolean wasImage = getSlicingSystem().getSliceType()==PlotType.IMAGE || 
						           getSlicingSystem().getSliceType()==PlotType.SURFACE;
				getSlicingSystem().setSliceType(getSliceType());
				
				final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
				if (dimsDataList!=null) {
					if (wasImage&&dimsDataList.isXFirst()) {
						dimsDataList.setSingleAxisOnly(AxisType.Y, AxisType.X);   		
					} else {
						dimsDataList.setSingleAxisOnly(AxisType.X, AxisType.X);
					}
				}
				getSlicingSystem().update(false);
			}

			@Override
			public Enum getSliceType() {
				return PlotType.XY;
			}
		};
	}
	
	private ISlicingTool getImageSlicingTool() {
		return new AbstractSlicingTool() {
			
			@Override
			public void militarize(boolean newData) {
				
				getSlicingSystem().setSliceType(getSliceType());
				
				final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
				if (dimsDataList!=null) dimsDataList.setTwoAxesOnly(AxisType.Y, AxisType.X);   		
				getSlicingSystem().refresh();
				getSlicingSystem().update(false);
				
			}

			@Override
			public Enum getSliceType() {
				return PlotType.IMAGE;
			}
		};
	}
	
	private void createPlottingSystem(Composite right){
		Composite plotComp = new Composite(right, SWT.NONE);
		plotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		plotComp.setLayout(new GridLayout());
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(plotComp, null);
		Composite displayPlotComp  = new Composite(plotComp, SWT.BORDER);
		displayPlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		displayPlotComp.setLayout(new FillLayout());
		
		
		try {
			system = PlottingFactory.createPlottingSystem();
			system.createPlotPart(displayPlotComp, "Slice", actionBarWrapper, PlotType.IMAGE, null);
			
		} catch (Exception e) {
			logger.error("cannot create plotting system",e);
		}
	}
	
	private class BasicContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			Map<String, int[]> vals = (Map<String, int[]>)inputElement;
			Set<Entry<String, int[]>> entrySet = vals.entrySet();
			
			return entrySet.toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private class ViewLabelProvider extends ColumnLabelProvider {
	
		@Override
		public String getText(Object obj) {
			Entry<String, int[]> ent = (Entry<String, int[]>)obj;
			String name = ent.getKey();
			if (rootName != null) name = name.substring(rootName.length());
			return name + " " + Arrays.toString(ent.getValue());
		}
		
		@Override
		public String getToolTipText(Object obj) {
			return "";
		}
		
	}

}
