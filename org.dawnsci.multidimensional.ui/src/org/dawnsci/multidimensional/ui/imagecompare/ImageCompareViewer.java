package org.dawnsci.multidimensional.ui.imagecompare;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.plotting.actions.ActionBarWrapper;
import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.dawnsci.plotting.histogram.ui.HistogramViewer;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.AxisEvent;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IAxisListener;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageCompareViewer extends Composite {
	
	private static final Logger logger = LoggerFactory.getLogger(ImageCompareViewer.class);
	
	private static final String LEFT_PLOT = "Left Plot";
	private static final String RIGHT_PLOT = "Right Plot";
	private static final String[] plotOptions = {LEFT_PLOT,RIGHT_PLOT};
	
	private IPlottingSystem<Composite> plot1;
	private IPlottingSystem<Composite> plot2;
	private HistogramViewer histogramWidget;
	private SharedPaletteListener paletteListener;
	private SharedAxisListener axisListener;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private AtomicReference<Runnable> atomicRunnable = new AtomicReference<Runnable>(null);
	private Combo imageSelector;
	private Combo imageSelector2;
	private Combo plotSelect;
	
	private AtomicReference<IImageTrace> leftTrace = new AtomicReference<>(null);
	private AtomicReference<IImageTrace> rightTrace = new AtomicReference<>(null);
	
	private AtomicReference<List<ILazyDataset>> images = new AtomicReference<>(null);
	
	private boolean shareHistogramRange = false;
	private boolean shareAxisRange = false;
			
	public ImageCompareViewer(Composite parent, int style, IPlottingService plotService) {
		super(parent, style);
		
		try {
			plot1 = plotService.createPlottingSystem();
			plot2 = plotService.createPlottingSystem();

		} catch (Exception e) {
			logger.error("Could not create plotting systems!");
			return;
		}
		
		
		this.setLayout(new FillLayout());
		
		SashForm sashForm = new SashForm(this, SWT.VERTICAL);
		
		Composite topComposite = new Composite(sashForm, SWT.None);
		
		topComposite.setLayout(new GridLayout(2,true));
		
		imageSelector = new Combo(topComposite, SWT.READ_ONLY);
		imageSelector.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).create());
		
		imageSelector2 = new Combo(topComposite, SWT.READ_ONLY);
		imageSelector2.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).create());
		
		ActionBarWrapper actionBarWrapper1 = ActionBarWrapper.createActionBars(topComposite, null);
		actionBarWrapper1.getToolbarControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		ActionBarWrapper actionBarWrapper2 = ActionBarWrapper.createActionBars(topComposite, null);
		actionBarWrapper2.getToolbarControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		plot1.createPlotPart(topComposite, "Image_compare_left", actionBarWrapper1, PlotType.IMAGE, (IWorkbenchPart)null);
		plot1.getPlotComposite().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		plot2.createPlotPart(topComposite, "Image_compare_right", actionBarWrapper2, PlotType.IMAGE, (IWorkbenchPart)null);
		plot2.getPlotComposite().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		Composite panel = new Composite(sashForm, SWT.NONE);
		panel.setLayout(new GridLayout(2,false));
		Composite innerPanel = new Composite(panel, SWT.NONE);
		innerPanel.setLayout(new GridLayout());
		innerPanel.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).create());
		
		
		plotSelect = new Combo(innerPanel, SWT.None);
		plotSelect.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).create());
		plotSelect.setItems(plotOptions);
        plotSelect.select(0);
        
		SelectionListener adapter = SelectionListener.widgetSelectedAdapter(e -> {

			int plotIndex = plotSelect.getSelectionIndex();
			updateHisto(plotIndex);
			
		});
		
		plotSelect.addSelectionListener(adapter);
		
		Button shareRangeBtn = new Button(innerPanel, SWT.CHECK);
		shareRangeBtn.setText("Share Histo Range?");
		shareRangeBtn.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).create());
		
		SelectionListener rangeCheckAdapter = SelectionListener.widgetSelectedAdapter(e -> {
			shareHistogramRange = shareRangeBtn.getSelection();
		});
		
		shareRangeBtn.addSelectionListener(rangeCheckAdapter);
		
		SelectionListener imageAdapter = SelectionListener.widgetSelectedAdapter(e -> {
			int i = plotSelect.getSelectionIndex();
			updateOnSelection(images.get(), i);
		});
		
		Button shareAxisRangeBtn = new Button(innerPanel, SWT.CHECK);
		shareAxisRangeBtn.setText("Share Axis Range?");
		shareAxisRangeBtn.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).create());
		
		SelectionListener axisRangeCheckAdapter = SelectionListener.widgetSelectedAdapter(e -> {
			shareAxisRange = shareAxisRangeBtn.getSelection();
		});
		
		shareAxisRangeBtn.addSelectionListener(axisRangeCheckAdapter);
		
		imageSelector.addSelectionListener(imageAdapter);
		imageSelector2.addSelectionListener(imageAdapter);
		
		try {
			histogramWidget = new HistogramViewer(panel, "histogram", null, null);
		} catch (Exception e) {
			logger.error("Cannot locate any plotting systems!", e);
		}

		GridData create = GridDataFactory.fillDefaults().grab(true, true).create();
		histogramWidget.getControl().setLayoutData(create);
		histogramWidget.setContentProvider(new ImageHistogramProvider());
		
		sashForm.setWeights(3,1);
		
		paletteListener = new SharedPaletteListener();
		
		axisListener = new SharedAxisListener();
		
		plot1.getSelectedXAxis().addAxisListener(axisListener);
		plot1.getSelectedYAxis().addAxisListener(axisListener);
		
		plot2.getSelectedXAxis().addAxisListener(axisListener);
		plot2.getSelectedYAxis().addAxisListener(axisListener);
		
	}
	
	private void updateHisto(int plotIndex) {
		IImageTrace ft1 = leftTrace.get();
		IImageTrace ft2 = rightTrace.get();
		
		String plot = plotOptions[plotIndex];
		switch (plot) {
		case LEFT_PLOT: {
			if (ft1 != null) {
				histogramWidget.setInput(ft1);
			}
			break;
		} case RIGHT_PLOT: {
			if (ft2 != null) {
				histogramWidget.setInput(ft2);
			}
			break;
		}
		}
	}
	
	private IDataset sliceImage(ILazyDataset lz) {

		try {
			IDataset slice = lz.getSlice();
			slice.squeeze();
			return slice;
		} catch (DatasetException e) {
			logger.error("Could not slice image", e);
			return null;
		}
	}

	private void runUpdate(List<ILazyDataset> images, int leftIndex, int rightIndex, int plotIndex) {
		
		if (images == null) {
			return;
		}
		
       Runnable r = new Runnable() {
			
			@Override
			public void run() {
				IDataset image1 = null;
				IDataset image2 = null;
				IImageTrace t1 = null;
				IImageTrace t2 = null;
				
				
				image1 = sliceImage(images.get(leftIndex));
				image2 = sliceImage(images.get(rightIndex));
				
				
				if (image1 != null) {
					t1 = plot1.createTrace(image1.getName(), IImageTrace.class);
					t1.setData(image1, null, false);
				}
				
				if (image2 != null) {
					t2 = plot2.createTrace(image2.getName(), IImageTrace.class);
					t2.setData(image2, null, false);
				}
				
				leftTrace.set(t1);
				rightTrace.set(t2);
				

				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						
						IImageTrace ft1 = leftTrace.get();
						IImageTrace ft2 = rightTrace.get();
						
						updateHisto(plotIndex);

						plot1.clear();
						plot2.clear();
						if (ft1 != null) plot1.addTrace(ft1);
						if (ft2 != null) plot2.addTrace(ft2);
						
						plot1.setShowIntensity(false);
						plot2.setShowIntensity(false);

						ft1.addPaletteListener(ImageCompareViewer.this.paletteListener);
						ft2.addPaletteListener(ImageCompareViewer.this.paletteListener);
						
						histogramWidget.rescaleAxis();
					}
				});
				
			}
		};
		
		atomicRunnable.set(r);
		
		executor.execute( new Runnable() {
			@Override
			public void run() {
				Runnable run = atomicRunnable.getAndSet(null);
				if (run == null) return;
				run.run();
			}
		});
		
	}
	
	public void clear() {
	    this.images.set(null);
	    this.plot1.reset();
	    this.plot2.reset();
	}
	
	private void updateOnSelection(List<ILazyDataset> images, int plotSelect) {
		int index1 = imageSelector.getSelectionIndex();
		int index2 = imageSelector2.getSelectionIndex();
		runUpdate(images, index1, index2, plotSelect); 
	}
	
	public void setImages(List<ILazyDataset> images) {
		
		int initFirst = 0;
		int initSecond = -1;
		
		if (this.images.get() != null) {
			initFirst = imageSelector.getSelectionIndex();
			initSecond = imageSelector2.getSelectionIndex();
		}
		
		this.images.set(images);
		
		if (images == null) {
			return;
		}
		
		List<String> list = images.stream().map(ILazyDataset::getName).toList();
		String[] array = list.toArray(new String[list.size()]);
		
		if (initSecond < 0 || initSecond >= array.length) {
			initSecond = array.length > 1 ? 1 : 0;
		}
		
		if (initFirst > array.length) {
			initFirst = 0;
		}
		
		imageSelector.setItems(array);
		imageSelector.select(initFirst);
		
		imageSelector2.setItems(array);
		imageSelector2.select(initSecond);
		
		int i = plotSelect.getSelectionIndex();
		
		updateOnSelection(images, i);
		
		
	}
	
	private class SharedPaletteListener extends IPaletteListener.Stub {
		
		boolean inUpdate = false;
		
		@Override
		public void minChanged(PaletteEvent evt) {
			
			if (!shareHistogramRange) {
				return;
			}
			
			if (inUpdate) {
				return;
			}
			try {
				inUpdate = true;
				updateTrace(evt.getSource(), true);
			} finally {
				inUpdate = false;
			}
		}

		@Override
		public void maxChanged(PaletteEvent evt) {
			
			if (!shareHistogramRange) {
				return;
			}
			
			if (inUpdate) {
				return;
			}
			try {
				inUpdate = true;
				updateTrace(evt.getSource(), false);
			} finally {
				inUpdate = false;
			}
		}
		
		private void updateTrace(Object sourceObject, boolean isMin) {
			
			IImageTrace ft1 = leftTrace.get();
			IImageTrace ft2 = rightTrace.get();
			
			IImageTrace source;
			IImageTrace dest;
			
			if (sourceObject == ft1) {
				source = ft1;
				dest = ft2;
			} else if (sourceObject == ft2) {
				source = ft2;
				dest = ft1;
			} else {
				return;
			}
			if (isMin) {
				dest.setMin(source.getMin());
			} else {
				dest.setMax(source.getMax());
			}
			//required to push update?
			dest.setPaletteData(dest.getPaletteData());
		}
	}
	
	private class SharedAxisListener implements IAxisListener {
		
		private boolean inUpdate = false;

		public void rangeChanged(AxisEvent evt) {
			
			if (!shareAxisRange) {
				return;
			}
			
			if (inUpdate) {
				return;
			}
			try {
				inUpdate = true;
				update(evt.getAxis());
			} finally {
				inUpdate = false;
			}
		}
		
		public void revalidated(AxisEvent evt) {
			//do nothing
		}
		
		private void update(IAxis source) {
			IAxis x1 = plot1.getSelectedXAxis();
			IAxis y1 = plot1.getSelectedYAxis();
			
			IAxis x2 = plot2.getSelectedXAxis();
			IAxis y2 = plot2.getSelectedYAxis();
			
			IAxis dest;
			
			if (source == x1) {
				dest = x2;
			} else if (source == x2) {
				dest = x1;
			} else if (source == y1) {
				dest = y2;
			} else if (source == y2) {
				dest = y1;
			} else {
				//Shouldn't fall in here but if it does, just return
				return;
			}
			
			double lower = source.getLower();
			double upper = source.getUpper();

			dest.setRange(lower, upper);
		}

	}

}
