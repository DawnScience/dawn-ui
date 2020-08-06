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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.dawnsci.conversion.schemes.ProcessConversionScheme;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.january.model.ISliceChangeListener;
import org.dawnsci.january.model.NDimensions;
import org.dawnsci.january.model.SliceChangeEvent;
import org.dawnsci.january.ui.dataconfigtable.DataConfigurationTable;
import org.dawnsci.plotting.actions.ActionBarWrapper;
import org.dawnsci.processing.ui.ServiceHolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetUpProcessWizardPage extends WizardPage {

	private static final Object[] LINE_OPTIONS = {"X"};
	private static final Object[] IMAGE_OPTIONS = {"X","Y"};
	
	public SetUpProcessWizardPage() {
		super("Set up input data");
	}

	private IPlottingSystem<Composite> system;
	private IConversionContext context;
	private ComboViewer cviewer;
	private String rootName = null;
	private DataConfigurationTable table;
	private NDimensions nDimensions;
	private DataOptions dataOption;
	private LoadedFile loadedFile;
	private Executor executor;
	
	private static final Logger logger = LoggerFactory.getLogger(SetUpProcessWizardPage.class);
	
	protected SetUpProcessWizardPage(IConversionContext context) {
		super("Set up input data");
		setDescription("Select dataset, axes, whether to process as images [2D] or lines [1D] and which dimensions of the array are the data dimensions");
		setTitle("Set up data for processing");
		this.context = context;
		context.setConversionScheme(new ProcessConversionScheme());
		executor = Executors.newSingleThreadExecutor();
	}
	
	@Override
	public void createControl(Composite parent) {
		
		SashForm sashForm= new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		
		setControl(sashForm);
		
		final Composite left = new Composite(sashForm, SWT.NONE);
		left.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).create());
		Composite right = new Composite(sashForm, SWT.NONE);
		right.setLayout(GridLayoutFactory.fillDefaults().create());
		
		Label l = new Label(left, SWT.NONE);
		l.setText("Select dataset:");
		
		cviewer = new ComboViewer(left);
		cviewer.setContentProvider(new BasicContentProvider());
		cviewer.setLabelProvider(new ViewLabelProvider());
		cviewer.getControl().setLayoutData(GridDataFactory.fillDefaults().create());
		
		final Button d1 = new Button(left, SWT.RADIO);
		d1.setText("Line [1D]");
		final Button d2 = new Button(left, SWT.RADIO);
		d2.setText("Image [2D]");
		
		table = new DataConfigurationTable();
		table.createControl(left);


		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		table.setLayoutData(data);
		
		d1.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!d1.getSelection()) return;
				updateDataset(dataOption.getName(), LINE_OPTIONS);
				updatePlot();
				
			}
			
		});
		
		d2.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!d2.getSelection()) return;
				updateDataset(dataOption.getName(), IMAGE_OPTIONS);
				updatePlot();
				
			}

		});
		
		cviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				@SuppressWarnings("unchecked")
				Entry<String, int[]> sel =(Entry<String, int[]>)((StructuredSelection)event.getSelection()).getFirstElement();
				Object[] options = null;
				if (sel.getValue().length < 2) {
					d1.setSelection(true);
					d2.setSelection(false);
					d2.setEnabled(false);
					options = LINE_OPTIONS;
				} else {
					d1.setSelection(false);
					d2.setSelection(true);
					d2.setEnabled(true);
					options = IMAGE_OPTIONS;
				}
				
				updateDataset(sel.getKey(), options);
				
			}
		});
		
		
		createPlottingSystem(right);
		
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					
					try {
						
						IDataHolder dh = ServiceHolder.getLoaderService().getData(context.getFilePaths().get(0), null);
						
						loadedFile = new LoadedFile(dh);
						
						Map<String, int[]> datasetNames = sortedByRankThenLength(loadedFile.getDataOptions());
						
						String dsName = "";
						if (context.getDatasetNames() != null) {
							dsName = context.getDatasetNames().get(0);
						} else {
							dsName = datasetNames.keySet().iterator().next();
						}
						
						final String dn = dsName;

						dataOption = loadedFile.getDataOption(dn);
						
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
								
								
								@SuppressWarnings("unchecked")
								Entry<String, int[]> sel =(Entry<String, int[]>)((StructuredSelection)cviewer.getSelection()).getFirstElement();
								
								Object[] options = null;
								
								if (sel.getValue().length < 2) {
									d1.setSelection(true);
									d2.setSelection(false);
									options = LINE_OPTIONS;
								} else {
									d1.setSelection(false);
									d2.setSelection(true);
									options = IMAGE_OPTIONS;
								}
								
								DataOptions dop = loadedFile.getDataOption(dn);
								nDimensions = dop.buildNDimensions();
								nDimensions.setSliceFullRange(true);
								nDimensions.addSliceListener(new ISliceChangeListener() {
									
									@Override
									public void sliceChanged(SliceChangeEvent event) {
										// TODO Auto-generated method stub
										
									}
									
									@Override
									public void optionsChanged(SliceChangeEvent event) {
										updatePlot();
										
									}
									
									@Override
									public void axisChanged(SliceChangeEvent event) {
										updatePlot();
									}
								});
								nDimensions.setOptions(options);
								table.setMaxSliceNumber(Integer.MAX_VALUE);
								table.setInput(nDimensions);

								updatePlot();
								
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
	
	private void updatePlot() {

			NDimensions nd = new NDimensions(nDimensions);
			for (int i = 0; i < nd.getRank(); i++) {
				if (nd.getDescription(i).isEmpty()) {
					nd.setSlice(i, new Slice(0,1,1));
				}
			}
			
			SliceND s = nd.buildSliceND();
			
			executor.execute(() -> {
				try {
					IDataset d = dataOption.getLazyDataset().getSlice(s).squeeze();
					Display.getDefault().asyncExec(() -> MetadataPlotUtils.plotDataWithMetadata(d, system));
				} catch (DatasetException e) {
					logger.error("Error slicing data for plot");
				}
			});
	}
	
	private void updateDataset(String name, Object[] options) {
		dataOption = loadedFile.getDataOption(name);
		nDimensions = dataOption.buildNDimensions();
		nDimensions.setSliceFullRange(true);
		nDimensions.setOptions(options);
		nDimensions.addSliceListener(new ISliceChangeListener() {
			
			@Override
			public void sliceChanged(SliceChangeEvent event) {
			}
			
			@Override
			public void optionsChanged(SliceChangeEvent event) {
				updatePlot();
				
			}
			
			@Override
			public void axisChanged(SliceChangeEvent event) {
				updatePlot();
				
			}
		});
			table.setInput(nDimensions);
		
		updatePlot();
	}
	
	private Map<String, int[]> sortedByRankThenLength(List<DataOptions> options) {
		
		Map<String, int[]> map = new HashMap<>();
		for (DataOptions d : options) {
			int[] shape = d.getLazyDataset().getShape();
			map.put(d.getName(), shape);
		}
		
		List<Entry<String, int[]>> ll = new LinkedList<Entry<String, int[]>>(map.entrySet());
		
		Collections.sort(ll, new Comparator<Entry<String, int[]>>() {
			// sort by greatest rank, largest size then name
			@Override
			public int compare(Entry<String, int[]> o1, Entry<String, int[]> o2) {
				int[] shape1 = o1.getValue();
				int[] shape2 = o2.getValue();
				int val = Integer.compare(shape2.length, shape1.length);
				
				if (val == 0) {
					val = Long.compare(ShapeUtils.calcLongSize(shape2), ShapeUtils.calcLongSize(shape1));
					if (val == 0) {
						val = Integer.compare(o1.getKey().length(), o2.getKey().length());
					}
				}
				
				return val;
			}
		});
		
		Map<String, int[]> lhm = new LinkedHashMap<String, int[]>();
		
		for (Entry<String, int[]> e : ll) {
			if (ShapeUtils.calcLongSize(e.getValue()) > 1) {
				lhm.put(e.getKey(), e.getValue());
			}	
		}
		
		return lhm;
		
	}
	
	
	public void populateContext() {
		
		@SuppressWarnings("unchecked")
		Entry<String, int[]> selection = (Entry<String, int[]>)((IStructuredSelection)cviewer.getSelection()).getFirstElement();
		context.setDatasetName(selection.getKey());
		
		Map<Integer,String> axesNames = new HashMap<>();
		
		String[] axesName = nDimensions.buildAxesNames();
		
		for (int i = 0; i < nDimensions.getRank(); i++) {
			
			if (axesName[i] != null && !axesName[i].isEmpty()) {
				//1 based indexing?
				axesNames.put(i+1, axesName[i]);
			}
			
			if (!nDimensions.getDescription(i).isEmpty()) {
				continue;
			}
			
			Slice slice = nDimensions.getSlice(i);
						
			context.addSliceDimension(i, slice.isSliceComplete() ? "all" : slice.toString());
		}
		
		context.setAxesNames(axesNames);
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
	
	@Override
	public void dispose() {
		super.dispose();
		if (system != null && !system.isDisposed()) system.dispose();
	}
	
	private class BasicContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			@SuppressWarnings("unchecked")
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
			@SuppressWarnings("unchecked")
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
