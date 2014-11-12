package org.dawnsci.processing.ui.slice;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.plotting.services.util.DatasetTitleUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.slice.Slicer;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
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
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class SetUpProcessWizardPage extends WizardPage {

	private ISliceSystem sliceComponent;
	private IPlottingSystem system;
	private IConversionContext context;
	private ComboViewer cviewer;
	private String rootName = null;
	
	private final static Logger logger = LoggerFactory.getLogger(SetUpProcessWizardPage.class);
	
	protected SetUpProcessWizardPage(IConversionContext context) {
		super("Set up input data");
		this.context = context;
		context.setConversionScheme(ConversionScheme.PROCESS);
	}
	
	@Override
	public void createControl(Composite parent) {
		
		SashForm sashForm= new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		
		setControl(sashForm);
		
		Composite left = new Composite(sashForm, SWT.NONE);
		left.setLayout(new GridLayout(2, false));
		Composite right = new Composite(sashForm, SWT.NONE);
		right.setLayout(new GridLayout());
		
		Map<String,int[]> datasetNames = null;
		
		try {
			datasetNames = getDatasetInfo();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Label l = new Label(left, SWT.NONE);
		l.setText("Select dataset:");
		
		cviewer = new ComboViewer(left);
		cviewer.setContentProvider(new BasicContentProvider());
		cviewer.setLabelProvider(new ViewLabelProvider());
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
		
		Entry<String, int[]> sel =(Entry<String, int[]>)((StructuredSelection)cviewer.getSelection()).getFirstElement();
		
		if (sel.getValue().length < 2) {
			d1.setSelection(true);
			d2.setSelection(false);
		} else {
			d1.setSelection(false);
			d2.setSelection(true);
		}
		
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

		sliceComponent.addAxisChoiceListener(new AxisChoiceListener() {
			
			@Override
			public void axisChoicePerformed(AxisChoiceEvent evt) {
				updatePlot(context);
				
			}
		});
		
		final ISlicingTool image = new AbstractSlicingTool() {
			
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
		image.setSlicingSystem(sliceComponent);
		
		final ISlicingTool line = new AbstractSlicingTool() {
			
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
		
		String dsName = "";
		
		if (context.getDatasetNames() != null) {
			dsName = context.getDatasetNames().get(0);
		} else {
			dsName = datasetNames.keySet().iterator().next();
		}
		
		IDataHolder dh;
		try {
			dh = LoaderFactory.getData(context.getFilePaths().get(0), true, true, null);
			ILazyDataset lz  = dh.getLazyDataset(dsName);
			final SliceSource source = new SliceSource(dh, lz, dsName, context.getFilePaths().get(0), false);
			sliceComponent.setData(source);
		} catch (Exception e1) {
			logger.error("Cannot get data", e1);
		}

		DimsDataList ddl = sliceComponent.getDimsDataList();
		
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
		
		updatePlot(context);
	}
	
	private void updatePlot(IConversionContext context) {
		Entry<String, int[]> selection = (Entry<String, int[]>)((IStructuredSelection)cviewer.getSelection()).getFirstElement();
		String path = context.getFilePaths().get(0);
		IDataHolder dh;
		try {
			dh = LoaderFactory.getData(path);
			ILazyDataset lazyDataset = dh.getLazyDataset(selection.getKey());
			
			final DimsDataList dims = sliceComponent.getDimsDataList();
			Map<Integer, String> sliceDims = new HashMap<Integer, String>();
			
			for (DimsData dd : dims.iterable()) {
				if (dd.isSlice()) {
					sliceDims.put(dd.getDimension(), String.valueOf(dd.getSlice()));
				} else if (dd.isTextRange()) {
					sliceDims.put(dd.getDimension(), dd.getSliceRange()!=null ? dd.getSliceRange() : "all");
				}
			}
			
			AxesMetadata amd = SlicedDataUtils.createAxisMetadata(path, lazyDataset, sliceComponent.getAxesNames());
			lazyDataset.setMetadata(amd);
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
			dh = LoaderFactory.getData(context.getFilePaths().get(0), true, true, null);
			ILazyDataset lz  = dh.getLazyDataset(name);
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
		final IMetadata        meta   = LoaderFactory.getMetadata(context.getFilePaths().get(0), null);
        final Map<String, int[]>     names  = new HashMap<String, int[]>();
        
        if (meta!=null && !meta.getDataNames().isEmpty()){
        	for (String name : meta.getDataShapes().keySet()) {
        		int[] shape = meta.getDataShapes().get(name);
        		if (shape != null) {
        			//squeeze to get usable rank
        			int[] ss = AbstractDataset.squeezeShape(shape, false);
        			if (scheme!=null && scheme.isRankSupported(ss.length)) {
        				names.put(name, shape);
        			} 
        		} else {
        			//null shape is a bad sign
        			names.clear();
        			break;
        		}
        	}
        }
        
        if (names.isEmpty()) {
        	final IDataHolder  dataHolder = LoaderFactory.getData(context.getFilePaths().get(0), true, true, null);
        	if (dataHolder!=null) for (String name : dataHolder.getNames()) {
        		if (name.contains("Image Stack")) continue;
        		if (!names.containsKey(name)) {

        			int[] shape = dataHolder.getLazyDataset(name).getShape();
        			int[] ss = AbstractDataset.squeezeShape(shape, false);
        			if (scheme!=null && scheme.isRankSupported(ss.length)) {
        				names.put(name, shape);
        			} 

        		}
        	}
        }

        rootName = DatasetTitleUtils.getRootName(names.keySet());
        
        return sortedByRankThenLength(names);
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
