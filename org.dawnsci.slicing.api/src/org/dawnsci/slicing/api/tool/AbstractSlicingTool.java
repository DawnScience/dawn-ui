/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.api.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.ServiceManager;
import org.dawnsci.slicing.api.system.DimsData;
import org.dawnsci.slicing.api.system.DimsDataList;
import org.dawnsci.slicing.api.system.ISliceSystem;
import org.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.jface.action.IAction;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDatasetMathsService;

/**
 * Convenience class for extending to provide a tool.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractSlicingTool implements ISlicingTool {

	protected ISliceSystem slicingSystem;
	protected String       toolId;

	/**
	 * Does nothing but demilitarize() unless overridden.
	 */
	@Override
	public void dispose() {
		demilitarize();
	}

	@Override
	public String getToolId() {
		return toolId;
	}

	@Override
	public void setToolId(String toolId) {
		this.toolId = toolId;
	}

	@Override
	public ISliceSystem getSlicingSystem() {
		return slicingSystem;
	}

	@Override
	public void setSlicingSystem(ISliceSystem slicingSystem) {
		this.slicingSystem = slicingSystem;
	}
	
	
	/**
	 * Does nothing unless overridden.
	 */
	@Override
	public void demilitarize() {
		
	}

	@Override
	public Object getAdapter(Class clazz) {
        return null;
	}
	
	
	/**
	 * May be null. Returns the axes in dimensional order.
	 * @return
	 */
	protected List<IDataset> getNexusAxes() throws Exception {
		
		final Map<Integer, String> names = getSlicingSystem().getAxesNames();
		final DimsDataList           ddl = getSlicingSystem().getDimsDataList();
		final int[]            dataShape = getSlicingSystem().getData().getLazySet().getShape();
		
		final List<IDataset>         ret = new ArrayList<IDataset>(3);
		for (DimsData dd : ddl.getDimsData()) {
			
			IDataset axis = null;
			try {
				final String name = names.get(dd.getDimension()+1);
				axis = SliceUtils.getAxis(getSlicingSystem().getCurrentSlice(), getSlicingSystem().getData().getVariableManager(), name, false, null);
			} catch (Throwable e) {
				ret.add(null);
				continue;
			}
            if (axis==null) {
            	final IDatasetMathsService service = (IDatasetMathsService)ServiceManager.getService(IDatasetMathsService.class);
            	axis = service.arange(dataShape[dd.getDimension()], IDatasetMathsService.INT);
            }
            ret.add(axis);
		}
		return ret;
	}
	
	/**
	 * The action to be used for the tool. In this case we return
	 * null and an action is created from the extension point.
	 * @return
	 */
	public IAction createAction() {
		return null;
	}

}
