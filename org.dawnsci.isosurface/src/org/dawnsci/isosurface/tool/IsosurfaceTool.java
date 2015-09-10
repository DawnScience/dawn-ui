/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.isosurface.tool;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.isosurface.Activator;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceEvent;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceListener;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimensionalEvent;
import org.eclipse.dawnsci.slicing.api.system.DimensionalListener;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.system.SliceSource;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author nnb55016
 * Class for visualising isosurfaces in DAWN
 */
public class IsosurfaceTool extends AbstractSlicingTool {
	
	private static final Logger logger = LoggerFactory.getLogger(IsosurfaceTool.class);

    // Listeners
	private DimensionalListener dimensionalListener;
	private AxisChoiceListener  axisChoiceListener;
	
	// Maths
//	private List<IOperation<MarchingCubesModel, Surface>> generatorList;
	
	// UI Stuff
	private ExpandBar exBar;
	private List<IsoSurfaceTab> tabList;
	private Composite controls;
	private Scale     isovalue;
	private Text      isoText;
	private Spinner   xDim, yDim, zDim;
	
	@SuppressWarnings("unchecked")
	public IsosurfaceTool() {
				
		this.dimensionalListener = new DimensionalListener() {			
			@Override
			public void dimensionsChanged(DimensionalEvent evt) {
				update();
			}
		};

		this.axisChoiceListener = new AxisChoiceListener() {
			@Override
			public void axisChoicePerformed(AxisChoiceEvent evt) {
				update();
			}
		};
	}
	
	@Override
	public void dispose() {
		super.dispose();
 	}
	
	/**
	 *  Create controls for the surface in the user interface
	 */
	public void createToolComponent(Composite parent) {

		controls = new Composite(parent, SWT.NONE);
		controls.setLayout(new FillLayout());
		controls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tabList = new ArrayList<IsoSurfaceTab>();
		
		exBar = new ExpandBar(controls, SWT.V_SCROLL);
		
		final Button decrease = new Button(parent, SWT.PUSH);
		decrease.setToolTipText("Nudge whole box 10% smaller");
		decrease.setImage(Activator.getImage("icons/down.png").createImage());
		decrease.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));
		decrease.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				addTab(exBar, "new tab");
			}
		});
		
		setControlsVisible(false);
		
	}
	
	private void addTab(ExpandBar parent, String name)
	{
		
		try
		{
			
			final IOperationService service = (IOperationService)Activator.getService(IOperationService.class);
			final IOperation<MarchingCubesModel, Surface> generator;
			
			generator = (IOperation<MarchingCubesModel, Surface>) service.create("org.dawnsci.isosurface.marchingCubes");
						
			IsoSurfaceTab newTab = new IsoSurfaceTab(parent, SWT.NONE, new IsosurfaceJob("Computing isosurface", this, generator, getSlicingSystem().getPlottingSystem()));
			
			ExpandItem exItem = new ExpandItem(exBar, SWT.NONE, 0);
			exItem.setText(name);
			exItem.setHeight(newTab.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			exItem.setControl(newTab);
			
			tabList.add(newTab);
			
			update(); 
			
		} 
		catch (Exception e)
		{
			System.out.println("Can not add new tab");
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Method that shows the display of the isosurface while the corresponding button is selected
	 */
	@Override
	public void militarize(boolean newData) {
		
		boolean alreadyIso = getSlicingSystem().getSliceType() == getSliceType();
		if (!newData && alreadyIso) return;
		
	    getSlicingSystem().setSliceType(getSliceType());
       
		setControlsVisible(true);
		
 		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList!=null) dimsDataList.setThreeAxesOnly(AxisType.X, AxisType.Y, AxisType.Z);   		
		
 		getSlicingSystem().update(false);
		getSlicingSystem().addDimensionalListener(dimensionalListener);
		getSlicingSystem().addAxisChoiceListener(axisChoiceListener);
		
		update();

	}
	
	private void setControlsVisible(boolean vis) {
		GridUtils.setVisible(controls, vis);
		controls.getParent().layout();
	}

	/**
	 * Called to update when lazy data changed.
	 */
	private void update() {
		
		for (final IsoSurfaceTab tab: tabList)
		{
			try {
				final SliceSource data = getSlicingSystem().getData();
				
				MarchingCubesModel model = tab.getJob().getGenerator().getModel();
				if (data.getLazySet()==model.getLazyData()) 
					return;
				
				// look into 
				ILazyDataset slice = data.getLazySet().getSliceView(getSlices());
				slice = slice.squeezeEnds();
				slice.setName("Sliced "+data.getLazySet().getName());
				if (slice.getRank()!=3) throw new RuntimeException("Invalid slice for isosurface tool!");
				if (slice==model.getLazyData()) 
					return; // Unlikely, will be new instances
				// 
				
				final ILazyDataset finalSlice = slice;
				
				tab.getJob().getGenerator().getModel().setLazyData(finalSlice); // this is a very quick fix, try and find a better way
				
				tab.getJob().compute(finalSlice);
				tab.updateUI();
				
				
			} catch (Exception e) {
				logger.error("Cannot compute iso-surface!", e);
			}
		}
	}
	
	/**
	 * Does nothing unless overridden.
	 */
	@Override
	public void demilitarize() {

		if (dimensionalListener!=null) {
			getSlicingSystem().removeDimensionalListener(dimensionalListener);
		}
		if (axisChoiceListener!=null) {
			getSlicingSystem().removeAxisChoiceListener(axisChoiceListener);
		}

		setControlsVisible(false);

	}

	@SuppressWarnings("rawtypes")
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
	
	
	protected void updateUI() {
		// update all the UI -> not good !!
		for (IsoSurfaceTab tab: tabList)
		{
			tab.updateUI();
		}
	}
}
