/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.isosurface.tool;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.dawnsci.isosurface.Activator;
import org.dawnsci.isosurface.isogui.IsoBean;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceListener;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimensionalListener;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.richbeans.api.generator.IListenableProxyFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author nnb55016 Class for visualising isosurfaces in DAWN
 */
public class IsosurfaceTool extends AbstractSlicingTool {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(IsosurfaceTool.class);

	// Listeners
	private DimensionalListener dimensionalListener = (event) -> update();
	private AxisChoiceListener axisChoiceListener = (event) -> update();
	
	// UI Stuff
	private IsoBean isoBean;
	private Control gui;

	private RenderingPropertyChangeListener isoController;
	
	/**
	 * Create controls for the surface in the user interface
	 */
	public void createToolComponent(Composite parent) {	
		IListenableProxyFactory listenableProxyFactory = Activator.getService(IListenableProxyFactory.class);
		isoBean = new IsoBean();
		isoBean.setListenableProxyFactory(listenableProxyFactory);
		
		IGuiGeneratorService guiGeneratorService = Activator.getService(IGuiGeneratorService.class);
		gui = guiGeneratorService.generateGui(isoBean, parent);
		gui.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		gui.setVisible(false);
		
		isoController = new RenderingPropertyChangeListener(isoBean,
				new IsosurfaceJob("isoSurfaceJob", slicingSystem.getPlottingSystem()), 
				new VolumeRenderJob("volumeRender"),
				getSlicingSystem().getPlottingSystem());
		isoBean.setRenderingHandler(isoController);
	}

	/**
	 * Method that shows the display of the isosurface while the corresponding
	 * button is selected
	 */
	@Override
	public void militarize(boolean newData) {
		((GridData) gui.getLayoutData()).exclude = false;
		gui.setVisible(true);	
		gui.getParent().getParent().pack();
		
		ISliceSystem slicingSystem = getSlicingSystem();
		slicingSystem.setSliceType(getSliceType());

		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList != null)
			dimsDataList.setThreeAxesOnly(AxisType.X, AxisType.Y, AxisType.Z);

		slicingSystem.update(false);
		
		slicingSystem.addDimensionalListener(dimensionalListener);
		slicingSystem.addAxisChoiceListener(axisChoiceListener);

		update();
	}

	/**
	 * Called to update when lazy data changed.
	 */
	private void update() {
		ISliceSystem sliceSystem = getSlicingSystem();
		ILazyDataset dataSlice = createSlice(sliceSystem);
		
		// check if the dataslice is compatible
		if (dataSlice.getRank() != 3)
			throw new RuntimeException("Invalid slice for isosurface tool!");

		isoController.setData(dataSlice, acquireAxes(dataSlice, null));
		isoController.propertyChange(null);

		gui.setVisible(true);
	}

	private ILazyDataset createSlice(ISliceSystem sliceSystem) {
		List<DimsData> dimData = sliceSystem.getDimsDataList().getDimsData();
		int xIndex = getIndexForDimension(0, dimData);
		int yIndex = getIndexForDimension(1, dimData);
		int zIndex = getIndexForDimension(2, dimData);

		return sliceSystem
				.getData()
				.getLazySet()
				.getSliceView(getSlices())
				.getTransposedView(xIndex, yIndex, zIndex)
				.squeezeEnds();
	}

	private int getIndexForDimension(int i, List<DimsData> dimData) {
		return dimData.get(i).getPlotAxis().getIndex();
	}

	/**
	 * Does nothing unless overridden.
	 */
	@Override
	public void demilitarize() {
		gui.setVisible(false);
		((GridData) gui.getLayoutData()).exclude = true;

		getSlicingSystem().removeDimensionalListener(dimensionalListener);
		getSlicingSystem().removeAxisChoiceListener(axisChoiceListener);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enum getSliceType() {
		return PlotType.ISOSURFACE;
	}

	@Override
	public boolean isSliceRequired() {
		return false;
	}

	@Override
	public boolean isAdvancedSupported() {
		return false;
	}

	private List<IDataset> acquireAxes(ILazyDataset data, IProgressMonitor monitor) {
		List<IDataset> axes = Stream.of(0,1,2).map(i -> {
			Optional<IDataset> axis = getAxisFromSlicingSystem(monitor, i);			
			return axis.orElseGet(()-> generateIndexAxis(data.getShape()[i]));	
		}).collect(Collectors.toList());
		
		return Collections.unmodifiableList(axes);
	}

	private Optional<IDataset> getAxisFromSlicingSystem(IProgressMonitor monitor, Integer i) {
		ISliceSystem slicingSystem = getSlicingSystem();
		DimsDataList dimsDataList = slicingSystem.getDimsDataList();
		int index = dimsDataList.getDimsData(i).getPlotAxis().getIndex();
		try{
			return Optional.ofNullable(SliceUtils.getAxis(
					slicingSystem.getCurrentSlice(), 
					slicingSystem.getData().getVariableManager(),
					dimsDataList.getDimsData(index), 
					monitor
				));
		} catch (Throwable e){
			return Optional.empty();
		}
	}

	private IDataset generateIndexAxis(int max) {
		double[] axis = IntStream.range(0, max).mapToDouble(i -> i).toArray();
		return DatasetFactory.createFromObject(axis);
	}
}
