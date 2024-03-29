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

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.utils.ToolUtils;
import org.dawnsci.processing.ui.api.IOperationUIService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class ZoomTool extends ProfileTool {
	
	private static Logger logger = LoggerFactory.getLogger(ZoomTool.class);
	
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
						
						ZoomProfileToolOperation op = (ZoomProfileToolOperation)ServiceProvider.getService(IOperationService.class).create(new ZoomProfileToolOperation().getId());
						ZoomProfileToolModel model = op.getModel();
						model.setRegion(bounds);
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
					
					ServiceProvider.getService(IOperationUIService.class).runProcessingWithUI(ops.toArray(new IOperation<?, ?>[ops.size()]), sliceMetadata, null);
				} catch (Exception e) {
					MessageDialog.openError(getSite().getShell(), "Error Reducing Data!", "Could not reduce data! " + e.getMessage());
					logger.error("Could not reduce data!", e);
				}
				
			}
		};
	}

	@Override
	protected void configurePlottingSystem(IPlottingSystem<?> plotter) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected String getRegionName() {
		return "Zoom";
	}

	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return type==RegionType.BOX||type==RegionType.PERIMETERBOX;
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.BOX;
	}

	@Override
	protected Collection<? extends ITrace> createProfile(final IImageTrace  image, 
			                     IRegion      region,
			                     IROI         rbs, 
			                     boolean      tryUpdate, 
			                     boolean      isDrag,
			                     IProgressMonitor monitor) {
		
		try {
		    createZoom(image, region, rbs, tryUpdate, isDrag, monitor);
		} catch (IllegalArgumentException ne) {
			// Occurs when slice outside
			logger.trace("Slice outside bounds of image!", ne);
		} catch (Throwable ne) {
			logger.warn("Problem slicing image in "+getClass().getSimpleName(), ne);
		}
        return profilePlottingSystem.getTraces();
	}
	

	protected Dataset createZoom(final IImageTrace  image, 
					            IRegion      region,
					            IROI         rbs, 
					            boolean      tryUpdate, 
					            boolean      isDrag,
					            IProgressMonitor monitor) {

		if (!(region.getROI() instanceof RectangularROI)) return null;
		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null) return null;
		if (!region.isVisible()) return null;

		if (monitor.isCanceled()) return null;

		final int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;
		final int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;

		Dataset im    = DatasetUtils.convertToDataset(image.getData());
		if (im == null) {
			return null;
		}
		Dataset slice = DatasetUtils.convertToDataset(ToolUtils.getClippedSlice(im, bounds));
		slice.setName(region.getName());
		// Calculate axes to have real values not size
		Dataset yLabels = null;
		Dataset xLabels = null;
		if (image.getAxes()!=null && image.getAxes().size() > 0) {
			Dataset xl = DatasetUtils.convertToDataset(image.getAxes().get(0));
			if (xl!=null) xLabels = ZoomTool.getLabelsFromLabels(xl, bounds, 0);
			Dataset yl = DatasetUtils.convertToDataset(image.getAxes().get(1));
			if (yl!=null) yLabels = ZoomTool.getLabelsFromLabels(yl, bounds, 1);
		}

		if (yLabels==null) yLabels = DatasetFactory.createRange(IntegerDataset.class, bounds.getPoint()[1], bounds.getEndPoint()[1], yInc);
		if (xLabels==null) xLabels = DatasetFactory.createRange(IntegerDataset.class, bounds.getPoint()[0], bounds.getEndPoint()[0], xInc);

		final IImageTrace zoom_trace = (IImageTrace)profilePlottingSystem.updatePlot2D(slice, Arrays.asList(new IDataset[]{xLabels, yLabels}), monitor);
		registerTraces(region, Arrays.asList(new ITrace[]{zoom_trace}));
		Display.getDefault().syncExec(new Runnable()  {
			public void run() {
				zoom_trace.setPaletteData(image.getPaletteData());
			}
		});

		return slice;

	}


	static Dataset getLabelsFromLabels(Dataset xl, RectangularROI bounds, int axisIndex) {
		try {
			int fromIndex = (int)bounds.getPoint()[axisIndex];
			int toIndex   = (int)bounds.getEndPoint()[axisIndex];
			int step      = toIndex>fromIndex ? 1 : -1;
			final Dataset slice = xl.getSlice(new int[]{fromIndex}, new int[]{toIndex}, new int[]{step});
			return slice;
		} catch (Exception ne) {
			return null;
		}
	}
}
