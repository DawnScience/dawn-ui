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
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.isosurface.alg.MarchingCubes;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.visualization.xygraph.linearscale.Tick;
import org.eclipse.nebula.visualization.xygraph.linearscale.TickFactory;
import org.eclipse.nebula.visualization.xygraph.linearscale.TickFactory.TickFormatting;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author nnb55016 / Joel Ogden
 * The Job class for Isovalue visualisation feature
 */
public class IsosurfaceJob extends Job {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(IsosurfaceJob.class);
	
 	// private IOperation<MarchingCubesModel, Surface> generator;
 	final private IPlottingSystem<?> system;
 	@SuppressWarnings("unused")
	private String name;
 	
 	AtomicReference<MarchingCubesModel> modelRef;
 	
	public IsosurfaceJob(String name, IPlottingSystem<?> system)
	{
		super(name);
		
		setUser(false);
		setPriority(Job.INTERACTIVE);
		
		this.name = name;
		this.system = system;
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
		Thread.currentThread().setName("IsoSurface - " + model.getName());
		final IIsosurfaceTrace trace;
		
		this.setName(model.getName());
		// create the trace if required, if not get the trace
		if ((IIsosurfaceTrace) system.getTrace(model.getTraceID()) == null)
		{
			trace = system.createIsosurfaceTrace(model.getTraceID());
			trace.setName(model.getTraceID());
		}
		else
		{
			trace = (IIsosurfaceTrace) system.getTrace(model.getTraceID());
		}
		
		try 
		{
			system.setDefaultCursor(IPlottingSystem.WAIT_CURSOR);
			
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			
			MarchingCubes alg = new MarchingCubes(model);
			Surface surface =  alg.execute(monitor);
						
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			
			IDataset points     = new FloatDataset(surface.getPoints(), surface.getPoints().length);         
			IDataset textCoords = new FloatDataset(surface.getTexCoords(), surface.getTexCoords().length);   
			IDataset faces      = new IntegerDataset(surface.getFaces(), surface.getFaces().length);         
			
			final ArrayList<IDataset> axis = new ArrayList<IDataset>();
			
			TickFactory tickGenerator = new TickFactory(TickFormatting.autoMode, null);
			
			// set the data set size
			axis.add(new IntegerDataset(model.getLazyData().getShape(), null));
			
			axis.add(convertTodatasetAxis(tickGenerator.generateTicks(0, model.getLazyData().getShape()[0], 15, false, false)));
			axis.add(convertTodatasetAxis(tickGenerator.generateTicks(0, model.getLazyData().getShape()[1], 15, false, false)));
			axis.add(convertTodatasetAxis(tickGenerator.generateTicks(0, model.getLazyData().getShape()[2], 15, false, false)));
							
			final int[] traceColour =	model.getColour();
			final double traceOpacity = model.getOpacity();
						
			trace.setMaterial(traceColour[0], traceColour[1] , traceColour[2], traceOpacity);
			trace.setData(points, textCoords, faces, axis );
			
			if ((IIsosurfaceTrace) system.getTrace(model.getTraceID()) == null)
			{
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						system.addTrace(trace);
			    	}
			    });
			}
			
		}
		finally
		{
            monitor.done();
			system.setDefaultCursor(IPlottingSystem.NORMAL_CURSOR);
		}
		return Status.OK_STATUS;
	}
	
	
	private FloatDataset convertTodatasetAxis(List<Tick> tickList) {
		
				
		float[] ticks = new float[tickList.size()];
		
		int i = 0;
		for (Tick t: tickList)
		{
			ticks[i] = (float) t.getValue();
			i++;
		}		
		
		return new FloatDataset(ticks, null);
	}

	/*
	 * look into improving !!
	 */
	@SuppressWarnings("unused")
	private ArrayList<IDataset> generateDuplicateAxes(int count, int step, int[] shape)
	{
		ArrayList<IDataset> axis = new ArrayList<IDataset>();
		
		float[] axisArray = new float[10];
		for (int i = 0; i < count; i ++)
		{
			axisArray[i] = i*step;
		}
		
		axis.add(new FloatDataset(new float[]{
										shape[0],
										shape[1],
										shape[2]}));
		axis.add(new FloatDataset(axisArray , null));
		axis.add(new FloatDataset(axisArray , null));
		axis.add(new FloatDataset(axisArray , null));
	
		return axis;
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
		
	public void destroy(String traceName)
	{
		if (system.getTrace(traceName) != null)
		{ 
			system.getTrace(traceName).dispose();
		}
	}

}
