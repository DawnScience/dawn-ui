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
import java.lang.reflect.InvocationTargetException;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.util.io.FileUtils;
import org.dawnsci.io.h5.H5Loader;
import org.dawnsci.plotting.tools.ServiceLoader;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionVisitor;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataReductionWizard extends Wizard implements IExportWizard {

	public static final String ID = "org.dawb.workbench.plotting.dataReductionExportWizard";
	
	private static final Logger logger = LoggerFactory.getLogger(DataReductionWizard.class);
	
	
	private DataReductionWizardPage       sliceConfigurePage;
	private IConversionService            service;
	private IConversionContext            context;
	private IConversionVisitor            visitor;

	private DimsDataList           dimsList;
	
	public DataReductionWizard() {
		super();
		setWindowTitle("Export Reduced Data");
		
		// It's an OSGI service, not required to use ServiceManager
		try {
			this.service = ServiceLoader.getConversionService();
		} catch (Exception e) {
			logger.error("Cannot get conversion service!", e);
			return;
		}

	}
	
	public void addPages() {
		
		sliceConfigurePage = new DataReductionWizardPage("Data Reduction", null, null);
		sliceConfigurePage.setDescription("This wizard runs '"+visitor.getConversionSchemeName()+"' over a stack of data. Please check the data to slice, "+
                "confirm the export file and then press 'Finish' to run '"+visitor.getConversionSchemeName()+"' on each slice.");

		addPage(sliceConfigurePage);
		
	}
	
    public boolean canFinish() {
    	 
    	if (!sliceConfigurePage.isContextSet()) {
			sliceConfigurePage.setDefaltSliceDims(dimsList);
			sliceConfigurePage.setContext(context);
    	}
    	if (!sliceConfigurePage.isPageValid()) return false;
		return super.canFinish();
    }


	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	@Override
	public boolean performFinish() {
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {				 
				    
					try {	
						((ToolConversionVisitor)visitor).setNexusAxes(sliceConfigurePage.getNexusAxes());
						
						try { // Delete if it exists because some tools can accidentally append.
							final File output = new File(context.getOutputPath());
							if (output.exists()) FileUtils.recursiveDelete(output);
						} catch (Exception ne) {
							logger.error("Cannot delete "+context.getOutputPath(), ne);
						}
						
						IConversionContext context = sliceConfigurePage.getContext();
						context.setMonitor(new ProgressMonitorWrapper(monitor));
						
						// Bit with the juice
						monitor.beginTask(visitor.getConversionSchemeName(), context.getWorkSize());
						monitor.worked(1);
						service.process(context);
						
						EclipseUtils.refreshAndOpen(context.getOutputPath(), true, monitor);

					} catch (final Exception ne) {

						logger.error("Cannot run export process for data reduction from tool '"+visitor.getConversionSchemeName()+"'", ne);
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								ErrorDialog.openError(Display.getDefault().getActiveShell(),
										"Data Not Exported", 
										"Cannot run export process for data reduction from tool "+visitor.getConversionSchemeName()+".\n\nPlease contact your support representative.",
										new Status(IStatus.WARNING, "org.edna.workbench.actions", ne.getMessage(), ne));
							}
						});
	
					} 

				}
			});
		} catch (Exception ne) {
			logger.error("Cannot run export process for data reduction from tool "+visitor.getConversionSchemeName(), ne);
		}

		return true;
	}				 

	public boolean needsProgressMonitor() {
		return true;
	}

	public void setData(File file, String h5Path, IDataReductionToolPage tool, ILazyDataset lazy) {
		
		this.context  = service.open(file.getAbsolutePath());
		this.visitor  = new ToolConversionVisitor(tool);;

		final String sugFileName = FileUtils.getFileNameNoExtension(file.getName()).replace(' ',  '_')+"_"+tool.getTitle().replace(' ', '_')+".nxs";
		context.setOutputPath(file.getParent()+File.separator+sugFileName);
		context.setConversionVisitor(visitor);
		// if a h5path and the file is an hdf5 format, other wise try with lazydataset
		if (h5Path != null && H5Loader.isH5(file.getAbsolutePath()))
			context.setDatasetName(h5Path);
		else
			context.setLazyDataset(lazy);
		
	}
	
	public void setSlice(final ILazyDataset lazy, final DimsDataList dList) {
		if (dList!=null) {
			this.dimsList = dList.clone();
			for (DimsData dd : dimsList.iterable()) {
				if (dd.isSlice()) {
					context.addSliceDimension(dd.getDimension(), String.valueOf(dd.getSlice()));
					if (dd.getSlice()>0) {
					    dd.setSliceRange(dd.getSlice()+":"+(lazy.getShape()[dd.getDimension()]-1), true);
					} else {
						dd.setSliceRange("all", true);
					}
				    break; // Only one range allowed.
				} 
			}
		}
	}

}
