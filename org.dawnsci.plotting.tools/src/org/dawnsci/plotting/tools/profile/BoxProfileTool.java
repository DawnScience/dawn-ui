/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.nexus.NexusFile;
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
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

public class BoxProfileTool extends ProfileTool {

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
		
		Dataset[] profile = showX || showY ? getProfile(image, region, rbs, tryUpdate, isDrag, monitor) : null;
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
		if (data instanceof RGBDataset) data = ((RGBDataset)data).getRedView();
		Dataset[] box = ROIProfile.box(data, DatasetUtils.convertToDataset(image.getMask()), bounds, true);
		if (box==null) return null;
		
		Dataset xi = null;
		Dataset yi = null;
		
		double ang = bounds.getAngle();
		//TODO probably better to deal with this in ROIProfile class, but this will do for now.
		if (image.getAxes() !=  null && ang == 0) {
			List<IDataset> axes = image.getAxes();
			
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
		if (xi == null || !Arrays.equals(xi.getShape(), x_intensity.getShape())){
			double xStart = bounds.getPointX();
			double xEnd = bounds.getPointX() + bounds.getLength(0);
			xi = DatasetFactory.createRange(IntegerDataset.class, xStart, xEnd, 1);
			xi.setName("X Pixel");
		}
		final Dataset x_indices = xi; // Maths.add(xi, bounds.getX()); // Real position
		
		final Dataset y_intensity = box[1];
		y_intensity.setName("Y "+region.getName());
		if (yi == null || !Arrays.equals(yi.getShape(), y_intensity.getShape())) {
			double yStart = bounds.getPointY();
			double yEnd = bounds.getPointY() + bounds.getLength(1);
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

	/**
	 * Same tool called recursively from the DataReductionWizard
	 */
	@Override
	public DataReductionInfo export(DataReductionSlice slice) throws Exception {

		final IImageTrace   image   = getImageTrace();
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		NexusFile file = slice.getFile();

		String dataGroupPath = slice.getParent();
		// Fix to http://jira.diamond.ac.uk/browse/SCI-1898
		GroupNode groupNode = file.getGroup(dataGroupPath, true);
		file.addAttribute(groupNode, new AttributeImpl(NexusFile.NXCLASS, "NXsubentry"));

		if (slice.getMonitor()!=null && slice.getMonitor().isCancelled()) return null;
		String dataPath = "";
		for (IRegion region : regions) {
			if (!isRegionTypeSupported(region.getRegionType())) continue;
			if (!region.isVisible())    continue;
			if (!region.isUserRegion()) continue;

			RectangularROI bounds = (RectangularROI)region.getROI();
			
			//create roi name group
			String datasetName = region.getName();
			if (datasetName.startsWith(dataGroupPath))
				datasetName = datasetName.substring(dataGroupPath.length());
			datasetName = datasetName.replace(' ', '_');
			file.getGroup(groupNode, datasetName, "NXdata", true);
			dataPath = dataGroupPath + Node.SEPARATOR + datasetName;

			//box profiles
			String regionGroup = dataGroupPath + Node.SEPARATOR + "profile";
			file.getGroup(groupNode, "profile", "NXdata", true);
			slice.setParent(regionGroup);

			Dataset[] box = showX || showY ? ROIProfile.box(DatasetUtils.convertToDataset(slice.getData()), DatasetUtils.convertToDataset(image.getMask()),
					(RectangularROI)region.getROI(), false) : null;

			if (showX) {
				final Dataset x_intensity = box[0];
				x_intensity.setName("X_Profile");
				
				slice.appendData(lazyWritables, x_intensity, exportIndex);
			}

			if (showY) {
				final Dataset y_intensity = box[1];
				y_intensity.setName("Y_Profile");
				slice.appendData(lazyWritables, y_intensity, exportIndex);
			}

			// Mean, Sum, Std deviation and region
			int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;
			int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;

			Dataset dataRegion = DatasetUtils.convertToDataset(slice.getData().getSlice(
					new int[] { (int) bounds.getPoint()[1], (int) bounds.getPoint()[0] },
					new int[] { (int) bounds.getEndPoint()[1],(int) bounds.getEndPoint()[0] },
					new int[] {yInc, xInc}));
			//mean
			Object mean = dataRegion.mean();
			Dataset meands = DatasetFactory.createFromObject(mean, new int[]{1});
			meands.setName("Mean");
			slice.appendData(lazyWritables, meands,dataPath, exportIndex);

			//Sum
			Object sum = dataRegion.sum();
			Dataset sumds = DatasetFactory.createFromObject(sum, new int[]{1});
			sumds.setName("Sum");
			slice.appendData(lazyWritables, sumds,dataPath, exportIndex);

			//Standard deviation
			Object std = dataRegion.stdDeviation();
			Dataset stds = DatasetFactory.createFromObject(std, new int[]{1});
			stds.setName("Std_Deviation");
			slice.appendData(lazyWritables, stds,dataPath, exportIndex);

			//region
			slice.setParent(dataPath);
			dataRegion.setName("Region_Slice");
			slice.appendData(lazyWritables, dataRegion, exportIndex);
		}
		return new DataReductionInfo(Status.OK_STATUS);
	}
}
