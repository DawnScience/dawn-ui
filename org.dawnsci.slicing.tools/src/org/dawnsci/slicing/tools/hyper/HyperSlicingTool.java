/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.slicing.tools.Activator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceEvent;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceListener;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimensionalEvent;
import org.eclipse.dawnsci.slicing.api.system.DimensionalListener;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.system.SliceSource;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class installs the special Hyper3D slicing tool which
 * replaces the traditional plotting system with an alternative
 * slicer.
 * 
 * @author fcp94556
 *
 */
public class HyperSlicingTool extends AbstractSlicingTool {
	
	private static final Logger logger = LoggerFactory.getLogger(HyperSlicingTool.class);

	private HyperComponent hyperComponent;
	private Control        originalPlotControl;
	private HyperType      hyperType = HyperType.Box_Axis;

	private DimensionalListener dimensionalListener;
	private AxisChoiceListener  axisChoiceListener;
	
	public HyperSlicingTool() {
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
	
	/**
	 * We actually install the HyperComponent instead of the plotting system.
	 */
	@Override
	public void militarize(boolean newData) {
		
		getSlicingSystem().setSliceType(getSliceType());
		getSlicingSystem().setSliceTypeInfo(hyperType.getLabel(), Activator.getImageDescriptor(hyperType.getIconPath()));

		final IPlottingSystem plotSystem = getSlicingSystem().getPlottingSystem();
        if (hyperComponent==null) {
        	hyperComponent = new HyperComponent(plotSystem.getPart());
        	hyperComponent.createControl(plotSystem.getPlotComposite());
        }
        
        originalPlotControl = plotSystem.setControl(hyperComponent.getControl(), false);
        
 		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList!=null) dimsDataList.setThreeAxesOnly(AxisType.X, AxisType.Y, AxisType.Z);   		
		
 		getSlicingSystem().refresh();
 		getSlicingSystem().update(false);
 		
        update(); // Now that the axes are right we can do a hyper slice!

		getSlicingSystem().addDimensionalListener(dimensionalListener);
		getSlicingSystem().addAxisChoiceListener(axisChoiceListener);
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

		if (originalPlotControl==null) return;
        final IPlottingSystem plotSystem = getSlicingSystem().getPlottingSystem();
		plotSystem.setControl(originalPlotControl, true);
		originalPlotControl = null;
		

	}

	private void update() {
		try {
			final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
			// Make sure the plot axes count is the same as the current types dimensions
			if (dimsDataList.getAxisCount()!=hyperType.getDimensions()) return;
          
			final SliceSource data = getSlicingSystem().getData();
            setData(data.getLazySet(), getAbstractNexusAxes(), getSlices(), getOrder(), hyperType);
        } catch (Exception ne) {
        	logger.error("Cannot set data to HyperComponent!", ne);
        }
	}

	public void setData(ILazyDataset lazy, List<IDataset> daxes, Slice[] slices, int[] order, HyperType hyperType) {
		
		try {
			getSlicingSystem().setEnabled(false);
			
			switch (hyperType) {
			case Box_Axis:
				hyperComponent.setData(lazy, daxes, slices, order);
				break;
			case Line_Line:
				hyperComponent.setData(lazy, daxes, slices, order,new ArpesMainImageReducer(),new ArpesSideImageReducer());
				break;
			case Line_Axis:
				hyperComponent.setData(lazy, daxes, slices, order,new TraceLineReducer(),new ImageTrapeziumBaselineReducer());
				break;
			}
		} finally {
			getSlicingSystem().setEnabled(true);
		}
		
	}
	/**
	 * Currently always gets the dimension order of the 
	 * @return
	 */
	private int[] getOrder() {
		final DimsDataList dims = getSlicingSystem().getDimsDataList();
		final int[] ret = new int[3];
		
		for (int i = 0; i < dims.size(); i++) {
			int axis = dims.getDimsData(i).getPlotAxis().getIndex();
			
			if (axis > -1 && axis < 3) {
				ret[axis] = i;
			}
		}
		
		return ret;
	}
	
	private List<IDataset> getAbstractNexusAxes() throws Exception {
		
		final int[] dataShape    = getSlicingSystem().getData().getLazySet().getShape();
		List<IDataset> nexusAxes = getNexusAxes();
		List<IDataset> ia        = null;
		if (nexusAxes==null || nexusAxes.isEmpty()) {
			ia = new ArrayList<IDataset>(dataShape.length);
			for (int i = 0; i < dataShape.length; i++) ia.add(null);
		} else {
			ia = new ArrayList<IDataset>(nexusAxes);
		}
		while(ia.size()<hyperType.getDimensions()) ia.add(null);
		
		final DimsDataList ddl = getSlicingSystem().getDimsDataList();
		
		IDataset[] ret = new IDataset[3];
		ret.toString();
		for (int i = 0; i < ddl.size(); i++) {
			int axis = ddl.getDimsData(i).getPlotAxis().getIndex();
			
			if (axis > -1 && axis < 3) {
				IDataset id = ia.get(i);
				if (id == null) {
					id = DatasetFactory.createRange(dataShape[i], Dataset.INT);
					id.setName("indices");
				}
				ret[axis] = ((Dataset)id);
			}
			
		}
		
		return Arrays.asList(ret);
	}
	
	public IAction createAction() {
		
		final MenuAction menuAction = new MenuAction("Hyper 3D slicing");
		
		final CheckableActionGroup grp = new CheckableActionGroup();
		for (final HyperType type : HyperType.values()) {
			final Action action = new Action(type.getLabel(), IAction.AS_CHECK_BOX) {
				public void run() {
	        		hyperType = type;
                    getSlicingSystem().militarize(HyperSlicingTool.this);
                    menuAction.setSelectedAction(this);
				}
			};
			action.setImageDescriptor(Activator.getImageDescriptor(type.getIconPath()));
			menuAction.add(action);
			
			if (type==hyperType) {
				action.setChecked(true);
				menuAction.setSelectedAction(action);
			}
			grp.add(action);
		}
		
		return menuAction;
	}
	
	@Override
	public void dispose() {
		demilitarize();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum getSliceType() {
		return hyperType;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == HyperComponent.class) {
			return hyperComponent;
		}else if (clazz == HyperType.class) {
			return hyperType;
		}
		return super.getAdapter(clazz);
	}
	
	@Override
	public boolean isSliceRequired() {
		return false;
	}

	@Override
	public boolean isAdvancedSupported() {
		return false;
	}

}
