/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.isosurface.tool;

import org.dawnsci.isosurface.Activator;
import org.dawnsci.isosurface.testingBeans.TestValueListener;
import org.dawnsci.isosurface.testingBeans.testComposite;
import org.dawnsci.isosurface.testingBeans.testingBean;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceEvent;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceListener;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimensionalEvent;
import org.eclipse.dawnsci.slicing.api.system.DimensionalListener;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.system.SliceSource;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.richbeans.api.reflection.IBeanController;
import org.eclipse.richbeans.reflection.BeanService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author nnb55016 Class for visualising isosurfaces in DAWN
 */
public class IsosurfaceTool extends AbstractSlicingTool
{
	
	private static final Logger logger = LoggerFactory
			.getLogger(IsosurfaceTool.class);
	
	// Listeners
	private DimensionalListener dimensionalListener;
	private AxisChoiceListener axisChoiceListener;
	
	// UI Stuff
	private ExpandBar exBar;
	// private List<IsoSurfaceTab> tabList;
	private Composite controls;
	
	private testComposite ui;
	private testingBean bean;
	
	@SuppressWarnings("unchecked")
	public IsosurfaceTool()
	{
		
		this.dimensionalListener = new DimensionalListener()
		{
			@Override
			public void dimensionsChanged(DimensionalEvent evt)
			{
				update();
			}
		};
		
		this.axisChoiceListener = new AxisChoiceListener()
		{
			@Override
			public void axisChoicePerformed(AxisChoiceEvent evt)
			{
				update();
			}
		};
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
	}
	
	/**
	 * Create controls for the surface in the user interface
	 */
	public void createToolComponent(Composite parent)
	{
				
		this.ui = new testComposite(parent, SWT.FILL);
		ui.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ui.setVisible(true);
		setControlsVisible(false);
		
		bean = new testingBean();
				
	}
	
	// private void addTab(ExpandBar parent, String name)
	// {
	//
	// try
	// {
	//
	// final IOperationService service =
	// (IOperationService)Activator.getService(IOperationService.class);
	// final IOperation<MarchingCubesModel, Surface> generator;
	//
	// generator = (IOperation<MarchingCubesModel, Surface>)
	// service.create("org.dawnsci.isosurface.marchingCubes");
	//
	// IsoSurfaceTab newTab = new IsoSurfaceTab(parent, SWT.NONE, new
	// IsosurfaceJob("Computing isosurface", this, generator,
	// getSlicingSystem().getPlottingSystem()));
	//
	// ExpandItem exItem = new ExpandItem(exBar, SWT.NONE, 0);
	// exItem.setText(name);
	// exItem.setHeight(newTab.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	// exItem.setControl(newTab);
	//
	// // tabList.add(newTab);
	//
	// update();
	//
	// }
	// catch (Exception e)
	// {
	// System.out.println("Can not add new tab");
	// e.printStackTrace();
	// }
	//
	// }
	
	/**
	 * Method that shows the display of the isosurface while the corresponding
	 * button is selected
	 */
	@Override
	public void militarize(boolean newData)
	{
		
		boolean alreadyIso = getSlicingSystem().getSliceType() == getSliceType();
		if (!newData && alreadyIso)
			return;
		
		getSlicingSystem().setSliceType(getSliceType());
		
		
		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList != null)
			dimsDataList.setThreeAxesOnly(AxisType.X, AxisType.Y, AxisType.Z);
		
		getSlicingSystem().update(false);
		getSlicingSystem().addDimensionalListener(dimensionalListener);
		getSlicingSystem().addAxisChoiceListener(axisChoiceListener);
		
		update();
		
	}
	
	private void setControlsVisible(boolean vis)
	{
		ui.setVisible(vis);
		// GridUtils.setVisible(controls, vis);
		// controls.getParent().layout();
	}
	
	/**
	 * Called to update when lazy data changed.
	 */
	private void update()
	{
		
		final SliceSource data = getSlicingSystem().getData();
		
		// look into
		ILazyDataset slice = data.getLazySet().getSliceView(getSlices());
		slice = slice.squeezeEnds();
		slice.setName("Sliced " + data.getLazySet().getName());
		if (slice.getRank() != 3)
			throw new RuntimeException("Invalid slice for isosurface tool!");
		
		final ILazyDataset finalSlice = slice;
		
		double min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		double[] minMax = estimateMinMaxFromDataSet(finalSlice);
		
		min = minMax[0];
		max = minMax[1];
		
		//!! find some way to find the min max value of a slice
		ui.setminMaxIsoValue(min,max);
			
		
		try
		{
			// Connect the UI and bean
			final IBeanController controller = BeanService.getInstance()
					.createController(ui, bean);
			controller.addValueListener(new TestValueListener(controller));
			// controller.addValueListener(new
			// ExampleJSONWritingValueListener(controller, null)); // !! look
			// into removing
			controller.beanToUI();
			controller.switchState(true);
			
		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		bean.setJob(finalSlice, getSlicingSystem().getPlottingSystem());
		
		ui.setVisible(false);
		setControlsVisible(true);
		
	}
	
	private double[] estimateMinMaxFromDataSet(ILazyDataset lz)
	{
		double min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		
		// test first third
		IDataset slice = lz.getSlice(
				new int[] { lz.getShape()[0]/3, 0,0}, 
				new int[] {1+lz.getShape()[0]/3, lz.getShape()[1], lz.getShape()[2]},
				new int[] {1,1,1});
		
		double[] minMaxStats = getStatsFromSlice(slice);
		if (min >= minMaxStats[0])
		{
			min = minMaxStats[0];
		}
		if (max <= minMaxStats[1])
		{
			max = minMaxStats[1];
		}
		
		// test center
		slice = lz.getSlice(
				new int[] { lz.getShape()[0]/2, 0,0}, 
				new int[] {1+lz.getShape()[0]/2, lz.getShape()[1], lz.getShape()[2]},
				new int[] {1,1,1});
		
		minMaxStats = getStatsFromSlice(slice);
		if (min >= minMaxStats[0])
		{
			min = minMaxStats[0];
		}
		if (max <= minMaxStats[1])
		{
			max = minMaxStats[1];
		}
		
		// test second third
		slice = lz.getSlice(
				new int[] {2 * lz.getShape()[0]/3, 0,0}, 
				new int[] {1+lz.getShape()[0]/3, lz.getShape()[1], lz.getShape()[2]},
				new int[] {1,1,1});
		
		minMaxStats = getStatsFromSlice(slice);
		if (min >= minMaxStats[0])
		{
			min = minMaxStats[0];
		}
		if (max <= minMaxStats[1])
		{
			max = minMaxStats[1];
		}
		
		
		return new double[] {min, max};
	}
	
	private double[] getStatsFromSlice(IDataset slice)
	{
		final IImageService service = (IImageService)Activator.getService(IImageService.class);
		return service.getFastStatistics(new ImageServiceBean((Dataset)slice, HistoType.MEAN));
	}
	
	/**
	 * Does nothing unless overridden.
	 */
	@Override
	public void demilitarize()
	{
		
		if (dimensionalListener != null)
		{
			getSlicingSystem().removeDimensionalListener(dimensionalListener);
		}
		if (axisChoiceListener != null)
		{
			getSlicingSystem().removeAxisChoiceListener(axisChoiceListener);
		}
		
		setControlsVisible(false);
		
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Enum getSliceType()
	{
		return PlotType.ISOSURFACE;
	}
	
	@Override
	public boolean isSliceRequired()
	{
		return false;
	}
	
	@Override
	public boolean isAdvancedSupported()
	{
		return false;
	}
	
	protected void updateUI()
	{
		// update all the UI -> not good !!
		// for (IsoSurfaceTab tab: tabList)
		// {
		// tab.updateUI();
		// }
	}
}
