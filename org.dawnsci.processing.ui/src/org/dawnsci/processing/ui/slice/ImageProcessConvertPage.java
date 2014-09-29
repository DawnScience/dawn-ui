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

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.ui.wizard.AbstractSliceConversionPage;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;

public class ImageProcessConvertPage extends AbstractSliceConversionPage  {

	IWorkbench workbench;
	
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
		super.createContentAfterFileChoose(container);
		sliceComponent.setSliceActionEnabled(PlotType.XY,      true);
		sliceComponent.setSliceActionEnabled(PlotType.IMAGE,   true);
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
	
	@Override
	public IConversionContext getContext() {
		if (context == null) return null;
		IConversionContext context = super.getContext();
		
		return context;
	}
	
}
