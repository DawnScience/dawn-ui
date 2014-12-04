/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.slice;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawb.common.ui.wizard.AbstractSliceConversionPage;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.slice.Slicer;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.slicing.api.SlicingFactory;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceEvent;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceListener;
import org.eclipse.dawnsci.slicing.api.system.DimensionalEvent;
import org.eclipse.dawnsci.slicing.api.system.DimensionalListener;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.system.RangeMode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;

public class ImageProcessConvertPage extends AbstractSliceConversionPage  {

	private static ILoaderService lservice;
	public static void setLoaderService(ILoaderService s) {
		lservice = s;
	}


	IWorkbench workbench;
	IPlottingSystem system;
	
	public ImageProcessConvertPage() {
		super("wizardPage", "Page for processing HDF5 data.", null);
		setTitle("Process");
		setDirectory(true);
		setFileLabel("Export to");
	}

	public boolean isOpen() {
		return false;
	}

	@Override
	protected void createAdvanced(Composite parent) {
		
		final File source = new File(getSourcePath(context));
		setPath(source.getParent()+File.separator+"output");

	}
	
	@Override
	protected void createContentAfterFileChoose(Composite container) {
//		super.createContentAfterFileChoose(container);
		
		try {
			this.sliceComponent = SlicingFactory.createSliceSystem("org.dawb.workbench.views.h5GalleryView");
		} catch (Exception e) {
//			logger.error("Cannot create slice system!", e);
			return;
		}

	    sliceComponent.setRangeMode(RangeMode.MULTI_RANGE);

	    final Control slicer = sliceComponent.createPartControl(container);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		data.minimumHeight=560;
		slicer.setLayoutData(data);
		sliceComponent.setVisible(true);
		sliceComponent.setSliceActionsEnabled(false);
		createAdvanced(container);
		pathChanged();
		sliceComponent.setSliceActionEnabled(PlotType.XY,      true);
		sliceComponent.setSliceActionEnabled(PlotType.IMAGE,   true);
		sliceComponent.addAxisChoiceListener(new AxisChoiceListener() {
			
			@Override
			public void axisChoicePerformed(AxisChoiceEvent evt) {
				updatePlot();
				
			}
		});
		
		sliceComponent.addDimensionalListener(new DimensionalListener() {
			
			@Override
			public void dimensionsChanged(DimensionalEvent evt) {
				updatePlot();
			}
		});
		
		Composite plotComp = new Composite(container, SWT.NONE);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setWorkbench(IWorkbench workbench) {
		this.workbench = workbench;
	}
	
	@Override
	public boolean isPageComplete() {
    	return true;
    }
	
	@Override
	public void setContext(IConversionContext context) {
		
		super.setContext(context);
		if (context.getOutputPath()!=null) {
			setPath(context.getOutputPath());
		}
		
		
	}
	
	protected void nameChanged() {
		super.nameChanged();
		updatePlot();
	}
	
	private void updatePlot() {
		String path = context.getFilePaths().get(0);
		IDataHolder dh;
		try {
			dh = lservice.getData(path, new IMonitor.Stub());
			ILazyDataset lazyDataset = dh.getLazyDataset(datasetName);
			
			final DimsDataList dims = sliceComponent.getDimsDataList();
			Map<Integer, String> sliceDims = new HashMap<Integer, String>();
			
			for (DimsData dd : dims.iterable()) {
				if (dd.isSlice()) {
					sliceDims.put(dd.getDimension(), String.valueOf(dd.getSlice()));
				} else if (dd.isTextRange()) {
					sliceDims.put(dd.getDimension(), dd.getSliceRange()!=null ? dd.getSliceRange() : "all");
				}
			}
			
			
			IDataset firstSlice = Slicer.getFirstSlice(lazyDataset, sliceDims);
			AxesMetadata amd = SlicedDataUtils.createAxisMetadata(path, lazyDataset, sliceComponent.getAxesNames());
			firstSlice.setMetadata(amd);
			SlicedDataUtils.plotDataWithMetadata(firstSlice, system, Slicer.getDataDimensions(lazyDataset.getShape(), sliceDims));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public IConversionContext getContext() {
		if (context == null) return null;
		IConversionContext context = super.getContext();
		
		return context;
	}
	
}
