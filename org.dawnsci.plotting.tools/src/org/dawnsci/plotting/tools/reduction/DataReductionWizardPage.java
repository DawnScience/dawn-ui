/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.reduction;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.ui.wizard.AbstractSliceConversionPage;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimensionalEvent;
import org.eclipse.dawnsci.slicing.api.system.DimensionalListener;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;

public class DataReductionWizardPage extends AbstractSliceConversionPage {

	public DataReductionWizardPage(String pageName, String description, ImageDescriptor icon) {
		super(pageName, description, icon);
		setFileLabel("Output File");
		setDirectory(false);
		setNewFile(true);
		setPathEditable(true);
	}

	@Override
	protected void createAdvanced(Composite parent) {
		

	}
	
	@Override
	protected void createContentAfterFileChoose(Composite container) {
        super.createContentAfterFileChoose(container);
		sliceComponent.setAxesVisible(true);
        sliceComponent.addDimensionalListener(new DimensionalListener() {			
			@Override
			public void dimensionsChanged(DimensionalEvent evt) {
				isPageValid();
			}
		});
	}

	@Override
	public void setContext(IConversionContext context) {
		
		super.setContext(context);
		if (context.getOutputPath()!=null) {
			setPath(context.getOutputPath());
		}
	}
	
	public boolean isPageValid() {
        if (!super.isPageValid()) return false;
        
        // Check dimensionality of slice again that which the tool
        // normally acts on.
        int rank=0;
        final DimsDataList dl = sliceComponent.getDimsDataList();
        for (DimsData dd : dl.iterable()) {
			if (dd.isTextRange()||dd.isSlice()) continue;
			rank++;
		}
        
        final IToolPage page = ((ToolConversionVisitor)context.getConversionVisitor()).getTool();
        if (rank!=page.getToolPageRole().getPreferredRank()) {
        	setErrorMessage("The tool '"+page.getTitle()+"' should be used with slices which give data with "+page.getToolPageRole().getPreferredRank()+" dimensions.");
            return false;
        }
        
		final File output = new File(getAbsoluteFilePath());
		if (output.exists() && (!output.canRead() || !output.canWrite())) {
			setErrorMessage("Cannot write to file '"+output.getName()+"'. Please choose a different file.");
			return false;
		}

        
		setErrorMessage(null);

        return true;
	}
	
	/**
	 * May be null
	 * @return
	 */
	protected List<IDataset> getNexusAxes() throws Exception {
		final Map<Integer, String> names = sliceComponent.getAxesNames();
		final DimsDataList ddl = sliceComponent.getDimsDataList();
		
		IDataset x=null; IDataset y=null;
		for (DimsData dd : ddl.iterable()) {
			
			if (dd.getPlotAxis()==AxisType.X) {
				final String name = names.get(dd.getDimension()+1);
				try {
					x = SliceUtils.getAxis(sliceComponent.getCurrentSlice(), sliceComponent.getData().getVariableManager(), name, false, null);
				} catch (Throwable e) {
					return null;
				}
			}
			if (dd.getPlotAxis()==AxisType.Y) {
				final String name = names.get(dd.getDimension()+1);
				try {
					y = SliceUtils.getAxis(sliceComponent.getCurrentSlice(), sliceComponent.getData().getVariableManager(), name, false, null);
				} catch (Throwable e) {
					return null;
				}
			}

		}
		if (x==null && y==null) return null;
		if (x!=null && y==null) return Arrays.asList(x);
		if (x!=null && y!=null) return Arrays.asList(x,y);
		return null;
	}

}
