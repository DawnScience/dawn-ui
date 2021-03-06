/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.SliceObject;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.dawnsci.slicing.api.system.SliceSource;
import org.eclipse.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SliceJob extends Job {
	
	private static final Logger logger = LoggerFactory.getLogger(SliceJob.class);
	 
	private SliceObject  slice;
	private Enum         sliceType;
	private ISliceSystem system;
	
	public SliceJob(ISliceSystem system) {
		super("Slice");
		this.system = system;
		setPriority(INTERACTIVE);
		setUser(false); // Shows a job in the bottom right but not in a dialog.
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		try {
			if (slice==null) return Status.CANCEL_STATUS;
			monitor.beginTask("Slice "+slice.getName(), 10);
			monitor.worked(1);
			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		
			if (sliceType instanceof PlotType) {
				final SliceSource data = system.getData();
				system.getPlottingSystem().setPlotType((PlotType)sliceType);
				
				if (system.getActiveTool()!=null && !system.getActiveTool().isSliceRequired()) {
					return Status.CANCEL_STATUS;
				}

				IDataset slicedData = plotSlice(data,
												slice, 
												(PlotType)sliceType, 
												system.getPlottingSystem(), 
												monitor);

				system.setSlice(slicedData);
				system.setSliceMetadata(slicedData!=null ? slicedData.getFirstMetadata(IMetadata.class) : null);
			}

		} catch (Exception e) {
			logger.error("Cannot slice "+slice.getName(), e);
			System.out.println(slice);
		} finally {
			
			if (!system.isEnabled()) Display.getDefault().syncExec(new Runnable() {
				public void run() {
					system.setEnabled(true);
				}
			});

			monitor.done();
		}	
		
		return Status.OK_STATUS;
	}


	/**
	 * Thread safe and time consuming part of the slice.
	 * @param currentSlice
	 * @param dataShape
	 * @param type
	 * @param plottingSystem - may be null, but if so no plotting will happen.
	 * @param monitor
	 * @return dataset, may be null
	 * @throws Exception
	 */
	private static IDataset plotSlice(final SliceSource       sliceSource,
				                     final SliceObject       currentSlice,
				                     final PlotType          type,
				                     final IPlottingSystem<?>   plottingSystem,
				                     final IProgressMonitor  monitor) throws Exception {

		if (plottingSystem==null) return null;
		if (monitor!=null) monitor.worked(1);
		if (monitor!=null&&monitor.isCanceled()) return null;
		
		final ILazyDataset lazySet = sliceSource.getLazySet();
		final int[]      dataShape = lazySet.getShape();
		currentSlice.setFullShape(dataShape);
		IDataset slice;
		final int[] slicedShape = currentSlice.getSlicedShape();
		if (lazySet instanceof IDataset && Arrays.equals(slicedShape, lazySet.getShape())) {
			slice = (IDataset)lazySet;
			if (currentSlice.getX() > currentSlice.getY() && slice.getShape().length==2) {
				// transpose clobbers name
				final String name = slice.getName();
				slice = DatasetUtils.transpose(slice);
				if (name!=null) slice.setName(name);
			}
		} else {
			slice = SliceUtils.getSlice(lazySet, currentSlice,monitor);
		}
		if (slice==null) return slice;
		
		// DO NOT CANCEL the monitor now, we have done the hard part the slice.
		// We may as well plot it or the plot will look very slow.
		if (monitor!=null) monitor.worked(1);

		boolean requireScale = plottingSystem.isRescale()
				               || type!=plottingSystem.getPlotType();

		
		// TODO FIXME This test has got too big. Reduce to methods or table prototype design pattern.
		// Cannot do now because in middle of release.
		if (type==PlotType.XY) {
  		    final IDataset x = SliceUtils.getAxis(currentSlice, sliceSource.getVariableManager(), slice.getShape()[0], currentSlice.getX()+1, true, monitor);
			if (!plottingSystem.isXFirst()) plottingSystem.setXFirst(true);
				
			// If we have 1 line trace plotted only, reuse that.
			Collection<ITrace> traces = plottingSystem.getTraces();
			if (traces!=null && traces.size()==1 && traces.iterator().next() instanceof ILineTrace) {
				
				final ITrace line = traces.iterator().next();
				final IDataset s  = slice;
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						ILineTrace lt = (ILineTrace)line;
						lt.setData(x, s);
						lt.setName(sliceSource.getDataName());
						// set axes name
						String xaxisname = (x != null && x.getName() != null && !x.getName().isEmpty()) ? x.getName() : "X-Axis";
						plottingSystem.getSelectedXAxis().setTitle(xaxisname);
						plottingSystem.getSelectedYAxis().setTitle(s.getName());
						plottingSystem.setTitle(s.getName() + " against " + xaxisname);
					}
				});
			} else {
				plottingSystem.clearTraces();
				plottingSystem.createPlot1D(x, Arrays.asList(slice), Arrays.asList(sliceSource.getDataName()), slice.getName(), monitor);
			}
		} else if (type==PlotType.XY_STACKED || type==PlotType.XY_STACKED_3D || type == PlotType.XY_SCATTER_3D) {
			
			plottingSystem.clearTraces();
						
			final int[]         shape = slice.getShape();
			
			IDataset xAxis = null;
			
			int xd = currentSlice.getX();
			if (type==PlotType.XY_STACKED) {
				int yd = currentSlice.getY();
				// We look for the dimension with the same size as x
				int ySize = currentSlice.getySize();
				for (int i = 0; i < shape.length; i++) {
				    if (shape[i] == ySize) {
				    	xd = i; // 0 or 1
				    	break;
				    }
				}
				yd    = xd==0 ? 1 : 0;
				xAxis = SliceUtils.getAxis(currentSlice, sliceSource.getVariableManager(), shape[yd], currentSlice.getX()+1, true, monitor);
				
			} else {
				
				xAxis = SliceUtils.getAxis(currentSlice, sliceSource.getVariableManager(), shape[xd], xd+1, true, monitor);
			}
		
			final List<IDataset> ys    = new ArrayList<IDataset>(shape[xd]);
			
			final Slice[] slices = new Slice[2];
			for (int index = 0; index < shape[xd]; index++) {
				slices[xd]  = new Slice(index, index+1, 1);
				IDataset set = (IDataset)slice.getSliceView(slices);
				set = set.squeeze();
				set.setName(String.valueOf(index));
				ys.add(set);
			}

			plottingSystem.setXFirst(true);
			plottingSystem.createPlot1D(xAxis, ys, currentSlice.getName(), monitor);

			final IDataset xAxisFinal = xAxis;
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (plottingSystem.getSelectedXAxis()!=null) plottingSystem.getSelectedXAxis().setTitle(xAxisFinal.getName());
					if (plottingSystem.getSelectedYAxis()!=null) plottingSystem.getSelectedYAxis().setTitle("");
				}
			});
		} else if (type==PlotType.IMAGE || type==PlotType.SURFACE){
			IDataset y = SliceUtils.getAxis(currentSlice, sliceSource.getVariableManager(), slice.getShape()[0], currentSlice.getX()+1, false, monitor);
			IDataset x = SliceUtils.getAxis(currentSlice, sliceSource.getVariableManager(), slice.getShape()[1], currentSlice.getY()+1, false, monitor);		

			// Nullify user objects because the ImageHistoryTool uses
			// user objects to know if the image came from it. Since we
			// use update here, we update (as its faster) but we also 
			// nullify the user object.
			ITrace trace = SliceUtils.getImageTrace(plottingSystem);
			if (trace!=null) {
				trace.setUserObject(null);
			}
			
			// No point giving axes where non are required.
			List<IDataset> axes = x==null&&y==null ? null : Arrays.asList(x,y);
			plottingSystem.updatePlot2D(slice, axes, sliceSource.getDataName(), monitor); 			
		}
		plottingSystem.repaint(requireScale);
		
		return slice;
	}

	public void schedule(Enum sliceType, SliceObject cs, boolean force) {
		if (force==false && slice!=null && slice.equals(cs)) return;
		// DO NOT: cancel();
		this.slice          = cs;
		this.sliceType      = sliceType;
		schedule();
	}	
}
