/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.ServiceLoader;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.RGBByteDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

public class BoxProfileTool extends ProfileTool {
	
	private static final Logger logger = LoggerFactory.getLogger(BoxProfileTool.class);

	private IAxis xPixelAxis;
	private IAxis yPixelAxis;
	private boolean showX = true;
	private boolean showY = true;

	@Override
	protected void configurePlottingSystem(IPlottingSystem<?> plotter) {
		if (xPixelAxis==null) {
			this.xPixelAxis = plotter.getSelectedXAxis();
			xPixelAxis.setTitle("X Pixel");
		}
		
		if (yPixelAxis==null) {
			this.yPixelAxis = plotter.createAxis("Y Pixel", false, SWT.TOP);
			plotter.getSelectedYAxis().setTitle("Intensity");
		}

		final MenuAction plots = new MenuAction("Profile plots");
		plots.setImageDescriptor(Activator.getImageDescriptor("icons/plotindex.png"));
		getSite().getActionBars().getToolBarManager().add(plots);
		getSite().getActionBars().getMenuManager().add(plots);

		final Action xAction = new Action("X", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				showX = isChecked();
				update(null, null, false);
			}
		};
		plots.add(xAction);
		xAction.setChecked(true);

		final Action yAction = new Action("Y", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				showY = isChecked();
				update(null, null, false);
			}
		};
		plots.add(yAction);
		yAction.setChecked(true);
	}

	@Override
	protected Collection<? extends ITrace> createProfile(IImageTrace  image, 
			                     IRegion      region, 
			                     IROI      rbs, 
			                     boolean      tryUpdate,
			                     boolean      isDrag,
			                     IProgressMonitor monitor) {

		//if (monitor.isCanceled()) return null;
		final ILineTrace x_trace = (ILineTrace)profilePlottingSystem.getTrace("X "+region.getName());
		final ILineTrace y_trace = (ILineTrace)profilePlottingSystem.getTrace("Y "+region.getName());
		
		Dataset[] profile = null;
		if (showX || showY) {
			profile = getProfile(image, region, rbs, tryUpdate, isDrag, monitor);
			if (profile == null) {
				return null;
			}
		}
		final Dataset x_indices   = showX ? profile[0] : null;
		final Dataset x_intensity = showX ? profile[1] : null;
		final Dataset y_indices   = showY ? profile[2] : null;
		final Dataset y_intensity = showY ? profile[3] : null;

		updatePlot(monitor, tryUpdate, showX, region, xPixelAxis, x_trace, x_indices, x_intensity);
		updatePlot(monitor, tryUpdate, showY, region, yPixelAxis, y_trace, y_indices, y_intensity);
		if (!showX && !showY) { // avoid ugly empty plot
			getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					xPixelAxis.setVisible(true);
				}
			});
		}
		return Arrays.asList(y_trace, x_trace);
	}

	private void updatePlot(IProgressMonitor monitor, boolean tryUpdate, boolean show,
			IRegion region, IAxis axis, ILineTrace trace, final Dataset indices, final Dataset intensity) {
		if (!tryUpdate || trace == null) {
			getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					axis.setVisible(show);
				}
			});
			if (show) {
				profilePlottingSystem.setSelectedXAxis(axis);
				Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(indices, Arrays.asList(new IDataset[]{intensity}), monitor);
				registerTraces(region, plotted);
			}
		} else {
			getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					axis.setVisible(show);
					trace.setVisible(show);
					if (show) {
						profilePlottingSystem.setSelectedXAxis(axis);
						trace.setData(indices, intensity);
					}
				}
			});
		}
	}

	/**
	 * 
	 * @param image
	 * @param region
	 * @param rbs
	 * @param tryUpdate
	 * @param isDrag
	 * @param monitor
	 * @return x_indices, x_intensity, y_indices, y_intensity   OR null
	 */
	protected Dataset[] getProfile(IImageTrace  image, 
					            IRegion      region, 
					            IROI      rbs, 
					            boolean      tryUpdate,
					            boolean      isDrag,
					            IProgressMonitor monitor) {
		
		if (monitor.isCanceled()) return null;
		if (image==null) return null;
		
		if (!isRegionTypeSupported(region.getRegionType())) return null;

		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null)
			return null;
		if (!region.isVisible())
			return null;

		if (monitor.isCanceled()) return null;
		
		Dataset data = DatasetUtils.convertToDataset(image.getData());
		if (data == null) {
			return null;
		}
		Dataset md = DatasetUtils.convertToDataset(image.getMask());
		List<IDataset> axes = image.getAxes();

		if (data instanceof RGBByteDataset) {
			data = ((RGBByteDataset) data).getRedView();
		} else if (data instanceof RGBDataset) {
			data = ((RGBDataset) data).getRedView();
		}

		Dataset[] box = ROIProfile.box(data, md, bounds, true);
		if (box==null) return null;
		
		Dataset xi = null;
		Dataset yi = null;
		
		double ang = bounds.getAngle();
		//TODO probably better to deal with this in ROIProfile class, but this will do for now.
		if (axes !=  null && ang == 0) {
			int[] spt = bounds.getIntPoint();
			int[] len = bounds.getIntLengths();
			
			final int xstart  = Math.max(0,  spt[1]);
			final int xend   = Math.min(spt[1] + len[1],  data.getShapeRef()[0]);
			final int ystart = Math.max(0,  spt[0]);
			final int yend   = Math.min(spt[0] + len[0],  data.getShapeRef()[1]);
			
			IDataset xFull = axes.get(0);
			if (xFull != null) {
				xi = DatasetUtils.convertToDataset(xFull.getSlice(new Slice(ystart , yend)));
				xi.setName(xFull.getName());
			}

			IDataset yFull = axes.get(1);
			if (yFull != null) {
				yi = DatasetUtils.convertToDataset(yFull.getSlice(new Slice(xstart , xend)));
				yi.setName(yFull.getName());
			}
		}

		//if (monitor.isCanceled()) return;
				
		final Dataset x_intensity = box[0];
		x_intensity.setName("X "+region.getName());
		if (xi == null || !Arrays.equals(xi.getShapeRef(), x_intensity.getShapeRef())){
			double xStart = Math.floor(bounds.getPointX());
			double xEnd = xStart + x_intensity.getSize();
			xi = DatasetFactory.createRange(IntegerDataset.class, xStart, xEnd, 1);
			xi.setName("X Pixel");
		}
		final Dataset x_indices = xi; // Maths.add(xi, bounds.getX()); // Real position
		
		final Dataset y_intensity = box[1];
		y_intensity.setName("Y "+region.getName());
		if (yi == null || !Arrays.equals(yi.getShapeRef(), y_intensity.getShapeRef())) {
			double yStart = Math.floor(bounds.getPointY());
			double yEnd = yStart + y_intensity.getSize();
			yi = DatasetFactory.createRange(IntegerDataset.class, yStart, yEnd, 1);
			yi.setName("Y Pixel");
		}
		final Dataset y_indices = yi; // Maths.add(yi, bounds.getY()); // Real position
		
		return new Dataset[]{x_indices, x_intensity, y_indices, y_intensity};
	}

	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return (type==RegionType.BOX)||(type==RegionType.XAXIS)||(type==RegionType.YAXIS)||type==RegionType.PERIMETERBOX;
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.BOX;
	}
	
	protected Action getReductionAction() {
		return new Action("Data reduction...", Activator.getImageDescriptor("icons/run_workflow.gif")) {
			@Override
			public void run() {
				
				try {
					
					final Collection<IRegion> regions = getPlottingSystem().getRegions();
					
					List<IOperation<?, ?>> ops = new ArrayList<>();
					
					for (IRegion region : regions) {
						if (!isRegionTypeSupported(region.getRegionType())) continue;
						
						final RectangularROI bounds = (RectangularROI)region.getROI();
						if (bounds==null)        continue;
						if (!region.isVisible()) continue;
						
						BoxProfileToolOperation op = (BoxProfileToolOperation)ServiceLoader.getOperationService().create(new BoxProfileToolOperation().getId());
						BoxProfileToolModel model = op.getModel();
						model.setRoi(bounds);
						op.setPassUnmodifiedData(true);
						op.setStoreOutput(true);
						ops.add(op);
					}
					
					if (ops.isEmpty()) {
						//show something
						return;
					}
					
					IOperation<?, ?> last = ops.get(ops.size()-1);
					last.setPassUnmodifiedData(false);
					last.setStoreOutput(false);
					
					ServiceLoader.getOperationUIService().runProcessingWithUI(ops.toArray(new IOperation<?, ?>[ops.size()]), sliceMetadata, null);
				} catch (Exception e) {
					MessageDialog.openError(getSite().getShell(), "Error Reducing Data!", "Could not reduce data! " + e.getMessage());
					logger.error("Could not reduce data!", e);
				}
				
			}
		};
	}
}
