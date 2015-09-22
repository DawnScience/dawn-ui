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

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author nnb55016
 * The Job class for Isovalue visualisation feature
 */
public class IsosurfaceJob extends Job {

	private static final Logger logger = LoggerFactory.getLogger(IsosurfaceJob.class);
	
	private IIsosurfaceTrace trace = null;
 	private IOperation<MarchingCubesModel, Surface> generator;
 	final private IPlottingSystem system;
 	
 	private ILazyDataset slice;
 	
	public IsosurfaceJob(String name, IPlottingSystem system,  ILazyDataset slice)
	{
		super(name);
		setUser(false);
		setPriority(Job.INTERACTIVE);
		this.system = system;
		this.slice = slice;
	}

	
	/**
	 * Call to update when updating the isovalue or
	 * box size.
	 */
//	public void compute(IOperation<MarchingCubesModel, Surface>  generator) {
//		compute(null, generator);
//	}
	
	/**
	 * Call to update if lazy data changed.
	 * Regenerates the box size and isoValue.
	 * 
	 * @param slice
	 */
	
	public void compute(IOperation<MarchingCubesModel, Surface>  generator)
	{
		this.generator = generator;
		cancel();
		schedule();
	}

	
	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		
//		final IPlottingSystem system = tool.getSlicingSystem().getPlottingSystem(); // does this change dynamically?
		
		try 
		{
			system.setDefaultCursor(IPlottingSystem.WAIT_CURSOR);
			
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			
			try
			{
				if (generator.getModel().getLazyData() != slice) // !! is this a pointer check or a data check???
				{
					generator.getModel().setLazyData(slice);
				}
				
				Surface surface    = generator.execute(null, new ProgressMonitorWrapper(monitor));
				
				final IDataset points     = new FloatDataset(surface.getPoints(), surface.getPoints().length);
				final IDataset textCoords = new FloatDataset(surface.getTexCoords(), surface.getTexCoords().length);
				final IDataset faces      = new IntegerDataset(surface.getFaces(), surface.getFaces().length);
				final int[] colour = surface.getColour();
				final double opacity = surface.getOpacity();
				
				// if new slice given re-calculate
				
				if (trace == null)
				{
					trace = system.createIsosurfaceTrace("isosurface");
					
					trace.setData(points, textCoords, faces, null);
					trace.setMaterial(colour[0], colour[1] , colour[2], opacity);
					
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							system.addTrace(trace);
				    	}
				    });
				}
				else
				{
					trace.setData(points, textCoords, faces, null);
					trace.setMaterial(colour[0], colour[1] , colour[2], opacity);
				}
				
			} 
			catch (UnsupportedOperationException e)
			{
				e.printStackTrace();
				showErrorMessage("The number of vertices has exceeded "+ generator.getModel().getVertexLimit(), "The surface cannot be rendered. Please increase the box size.");
				return Status.CANCEL_STATUS;
				
			} 
			catch (Exception e) 
			{
				logger.error("Cannot run algorithm "+ generator.getClass().getSimpleName(), e);
				return Status.CANCEL_STATUS;
				
			} 
			catch (OutOfMemoryError e)
			{
				e.printStackTrace();
				showErrorMessage("Out of memory Error", "There is not enough memory to render the surface. Please increase the box size.");
				return Status.CANCEL_STATUS;
			}
					
		}
		finally {
			system.setDefaultCursor(IPlottingSystem.NORMAL_CURSOR);
		}
		return Status.OK_STATUS;
	}

	private void showErrorMessage(final String title, final String message) {
		Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(), title, message);
			}
		});
	}
	
	
	public IOperation<MarchingCubesModel, Surface> getGenerator()
	{
		return this.generator;
	}

}
