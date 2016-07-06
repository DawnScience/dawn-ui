/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.isosurface.tool;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dawnsci.isosurface.alg.MarchingCubes;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IImage3DTrace;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author nnb55016 / Joel Ogden
 * The Job class for Isovalue visualisation feature
 */

public class IsosurfaceJob extends Job {

	private static final Logger logger = LoggerFactory.getLogger(IsosurfaceJob.class);
	
 	@SuppressWarnings("unused")
	private String name;
 	
 	AtomicReference<MarchingCubesModel> modelRef;

	private IPlottingSystem<Object> plottingSystem;
 	
	public IsosurfaceJob(String name, IPlottingSystem<Object> plottingSystem)
	{
		super(name);
		this.plottingSystem = plottingSystem;
	
		setUser(false);
		setPriority(Job.INTERACTIVE);
		
		this.name = name;
		this.modelRef = new AtomicReference<MarchingCubesModel>();
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
	public void compute(MarchingCubesModel model)
	{
		this.modelRef.set(model);
		
		cancel();
		schedule();
	}
	
	
	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		MarchingCubesModel model = this.modelRef.get();
		
		
		Thread.currentThread().setName("IsoSurface");
		final IIsosurfaceTrace trace;
		plottingSystem.setPlotType(PlotType.ISOSURFACE);
		
		this.setName("IsoSurface");
		// create the trace if required, if not get the trace
		if ((IIsosurfaceTrace) plottingSystem.getTrace(model.getTraceID()) == null)
		{
			trace = plottingSystem.createIsosurfaceTrace(model.getTraceID());
			trace.setName(model.getTraceID());
		}
		else
		{
			trace = (IIsosurfaceTrace) plottingSystem.getTrace(model.getTraceID());
		}
		
		try 
		{
			plottingSystem.setDefaultCursor(IPlottingSystem.WAIT_CURSOR);
			
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
						
			final MarchingCubes alg = new MarchingCubes(model);
			final Surface surface =  alg.execute(monitor);
						
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			
			IDataset points     = DatasetFactory.createFromObject(surface.getPoints());
			IDataset textCoords = DatasetFactory.createFromObject(surface.getTexCoords());
			IDataset faces      = DatasetFactory.createFromObject(surface.getFaces());
									
			final int[] traceColour =	model.getColour();
			final double traceOpacity = model.getOpacity();
						
			trace.setMaterial(traceColour[0], traceColour[1] , traceColour[2], traceOpacity);
			trace.setData(	
					points, 
					textCoords, 
					faces, 
					model.getAxes());
			
			if ((IIsosurfaceTrace) plottingSystem.getTrace(model.getTraceID()) == null)
			{
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						plottingSystem.addTrace(trace);
			    	}
			    });
			}
			
		}
		finally
		{
            monitor.done();
			plottingSystem.setDefaultCursor(IPlottingSystem.NORMAL_CURSOR);
		}
		return Status.OK_STATUS;
	}
		
	@SuppressWarnings("unused")
	private void showErrorMessage(final String title, final String message) {
		Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(), title, message);
			}
		});
	}

	public void destroyOthers(Stream<String> toKeep) {
		cancel();		
		
		Set<String> traceNames = plottingSystem.getTraces().stream().map(trace -> trace.getName()).collect(Collectors.toSet());
		traceNames.removeAll(toKeep.collect(Collectors.toSet()));
		
		traceNames.stream()
			.map(name -> plottingSystem.getTrace(name))
			.filter(t -> t instanceof IImage3DTrace)
			.map(Optional::ofNullable)
			.forEach(trace -> { 
				try{
					logger.info("disposing of trace {}", trace);
					trace.ifPresent(ITrace::dispose);
				} catch (Exception ex) {
					logger.info("dispose of trace failed", ex);
				}
			});
		
	}

}
