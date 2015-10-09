/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.isosurface.tool;

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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author Joel Ogden / nnb55016
 * The Job class for Isovalue visualisation feature
 */
public class IsosurfaceJob extends Job {

	private static final Logger logger = LoggerFactory.getLogger(IsosurfaceJob.class);
	
 	private IOperation<MarchingCubesModel, Surface> generator;
 	final private IPlottingSystem system;
 	private String name;
 	
 	private double value;
	private double opacity;
	private int[] boxSize;
	private RGB colour;
 	private String traceName;
 	
 	private ILazyDataset slice;
 	
	public IsosurfaceJob(String name, IPlottingSystem system,  ILazyDataset slice, IOperation<MarchingCubesModel, Surface> generator)
		
	{
		super(name);
		
		setUser(false);
		setPriority(Job.INTERACTIVE);
		
		
		
		this.name = name;
		this.system = system;
		this.slice = slice;
		this.generator = generator;
		
	}
	
	/**
	 * Call to calculate and draw the isosurface
	 * 
	 * @param boxSize - representing XYZ sizes, Int[3] array
	 * @param value - The value to be rendered
	 * @param opacity - The transparency
	 * @param colour - The colour of the surface
	 * @param traceName - The name of the surface trace
	 * 
	 */
	
	
	public void compute(int[] boxSize, double value,  double opacity, RGB colour, String traceName)//, IIsosurfaceTrace trace)
	{
		this.boxSize = boxSize;   
		this.value = value;     
		this.opacity = opacity;   
		this.colour = colour;
		this.traceName = traceName;
		
		cancel();
		schedule();
	}

	
	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		
		MarchingCubesModel model = this.generator.getModel();
		
		model.setBoxSize(boxSize);
		model.setOpacity(opacity);
		model.setIsovalue(value);
		model.setColour(colour.red, colour.green, colour.blue);
		
		if (Thread.currentThread() != null) // !! look into removing
		{
			Thread tempThread = Thread.currentThread();
			Thread.currentThread().setName(this.name);
		}
		
		try 
		{
			system.setDefaultCursor(IPlottingSystem.WAIT_CURSOR);
			
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			
			try
			{
				if (generator.getModel().getLazyData() != slice)
				{
					generator.getModel().setLazyData(slice);
				}
				
				Surface surface    = generator.execute(null, new ProgressMonitorWrapper(monitor));
				
				final IDataset points     = new FloatDataset(surface.getPoints(), surface.getPoints().length);
				final IDataset textCoords = new FloatDataset(surface.getTexCoords(), surface.getTexCoords().length);
				final IDataset faces      = new IntegerDataset(surface.getFaces(), surface.getFaces().length);
				final int[] colour = surface.getColour();
				final double opacity = surface.getOpacity();
				
				// if trace has not been created -> create trace
				if ((IIsosurfaceTrace) system.getTrace(traceName) == null)
				{
					final IIsosurfaceTrace trace = system.createIsosurfaceTrace(this.traceName);
				
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
					IIsosurfaceTrace trace = (IIsosurfaceTrace) system.getTrace(traceName);
					
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
		finally 
		{
            monitor.done();
			system.setDefaultCursor(IPlottingSystem.NORMAL_CURSOR);
		}
		return Status.OK_STATUS;
	}
	
	public void destroy()
	{
		system.getTrace(traceName).dispose();
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
