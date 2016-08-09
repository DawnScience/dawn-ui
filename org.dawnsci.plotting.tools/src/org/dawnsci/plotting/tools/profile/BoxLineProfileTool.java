/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
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

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.tool.IProfileToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

/**
 * BoxLine profile tool
 */
public class BoxLineProfileTool extends ProfileTool implements IProfileToolPage {

	private static final Logger logger = LoggerFactory.getLogger(BoxLineProfileTool.class);
	private IAxis xPixelAxis;
	private IAxis yPixelAxis;
	private boolean isVertical;

	private boolean isAveragePlotted;
	private boolean isEdgePlotted;
	private boolean isXAxisROIVisible = false;

	private ProfileJob profileJob;
	private IRegion xAxisROI;
	private IRegion region;

	public BoxLineProfileTool() {
		this(false, true, false);
		this.profileJob = new ProfileJob();
	}

	/**
	 * Constructor to this profile tool
	 * 
	 * @param isVertical
	 */
	public BoxLineProfileTool(boolean isVertical) {
		this.isVertical = isVertical;
	}

	/**
	 * Constructor to this profile tool
	 * 
	 * @param isVertical
	 * @param isEdgePlotted
	 * @param isAveragePlotted
	 */
	public BoxLineProfileTool(boolean isVertical, boolean isEdgePlotted, boolean isAveragePlotted) {
		this.isVertical = isVertical;
		this.isEdgePlotted = isEdgePlotted;
		this.isAveragePlotted = isAveragePlotted;
	}

	@Override
	protected void configurePlottingSystem(IPlottingSystem<?> plottingSystem) {
		if (xPixelAxis == null) {
			this.xPixelAxis = plottingSystem.getSelectedXAxis();
			xPixelAxis.setTitle("X Pixel");
		}
		if (yPixelAxis == null) {
			this.yPixelAxis = plottingSystem.getSelectedYAxis();
			plottingSystem.getSelectedYAxis().setTitle("Intensity");
		}

		profilePlottingSystem.setShowLegend(false);
		profilePlottingSystem.setTitle("");

		Action plotAverage = new Action("Plot Average Box Profiles", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				setPlotAverageProfile(isChecked());
				ILineTrace average_trace = (ILineTrace) profilePlottingSystem.getTrace("Average");
				if (average_trace != null) {
					average_trace.setVisible(isChecked());
				}
				if (region != null)
					update(region, (RectangularROI) region.getROI(), false);
			}
		};
		plotAverage.setToolTipText("Toggle On/Off Average Profiles");
		plotAverage.setText("Plot Average Box Profiles");
		plotAverage.setImageDescriptor(Activator.getImageDescriptor("icons/average.png"));

		final Action plotEdge = new Action("Plot Edge Box Profiles", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				setPlotEdgeProfile(isChecked());
				ILineTrace trace1 = (ILineTrace) profilePlottingSystem.getTrace("Edge 1");
				ILineTrace trace2 = (ILineTrace) profilePlottingSystem.getTrace("Edge 2");
				if (trace1 != null && trace2 != null) {
					trace1.setVisible(isChecked());
					trace2.setVisible(isChecked());
				}
				if (region != null)
					update(region, (RectangularROI) region.getROI(), false);
			}
		};
		plotEdge.setToolTipText("Toggle On/Off Perimeter Profiles");
		plotEdge.setText("Plot Edge Box Profiles");
		plotEdge.setChecked(true);
		plotEdge.setImageDescriptor(Activator.getImageDescriptor("icons/edge-color-box.png"));
		if (getSite() != null) {
			getSite().getActionBars().getToolBarManager().add(new Separator("average"));
			getSite().getActionBars().getToolBarManager().add(plotAverage);
			getSite().getActionBars().getToolBarManager().add(plotEdge);
			getSite().getActionBars().getToolBarManager().add(new Separator("edge"));
		}

		logger.debug("profilePlottingSystem configured");
	}

	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return (type == RegionType.BOX) || (type == RegionType.PERIMETERBOX) || (type == RegionType.XAXIS)
				|| (type == RegionType.YAXIS);
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.PERIMETERBOX;
	}

	public boolean isAveragePlotted() {
		return isAveragePlotted;
	}

	@Override
	public void setPlotAverageProfile(boolean isAveragePlotted) {
		this.isAveragePlotted = isAveragePlotted;
	}

	public boolean isEdgePlotted() {
		return isEdgePlotted;
	}

	@Override
	public void setPlotEdgeProfile(boolean isEdgePlotted) {
		this.isEdgePlotted = isEdgePlotted;
	}

	@Override
	public void setXAxisROIVisible(boolean isXAxisROIVisible) {
		this.isXAxisROIVisible = isXAxisROIVisible;
		if (xAxisROI == null)
			return;
		xAxisROI.setVisible(isXAxisROIVisible);
	}

	@Override
	public DataReductionInfo export(DataReductionSlice bean) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLineOrientation(boolean vertical) {
		this.isVertical = vertical;
	}

	@Override
	protected Collection<? extends ITrace> createProfile(IImageTrace image, IRegion region, IROI rbs, boolean tryUpdate,
			boolean isDrag, IProgressMonitor monitor) {
		this.region = region;
		if (image == null)
			return null;

		Dataset[] profile = getProfiles(image, region, rbs, tryUpdate, isDrag, monitor);
		if (profile == null)
			return null;

		final Dataset indices = profile[0];
		final Dataset edge1 = profile[1];
		final Dataset edge2 = profile[2];
		Dataset average = profile[3];

		if (!isRegionTypeSupported(region.getRegionType()))
			return null;

		final ILineTrace trace1 = (ILineTrace) profilePlottingSystem.getTrace("Edge 1");
		final ILineTrace trace2 = (ILineTrace) profilePlottingSystem.getTrace("Edge 2");
		ILineTrace average_trace = (ILineTrace) profilePlottingSystem.getTrace("Average");
		profilePlottingSystem.setSelectedXAxis(xPixelAxis);
		if (isEdgePlotted && !isAveragePlotted) {
			if (tryUpdate && trace1 != null && trace2 != null) {
				trace1.setData(indices, edge1);
				trace2.setData(indices, edge2);
			} else {
				Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(indices,
						Arrays.asList(new IDataset[] { edge1 }), monitor);
				registerTraces(region, plotted);
				plotted = profilePlottingSystem.updatePlot1D(indices, Arrays.asList(new IDataset[] { edge2 }), monitor);
				registerTraces(region, plotted);
			}
			setTracesColor();
			return Arrays.asList(trace2, trace1);
		} else if (isEdgePlotted && isAveragePlotted) {
			if (tryUpdate && trace1 != null && trace2 != null && average_trace != null) {
				trace1.setData(indices, edge1);
				trace2.setData(indices, edge2);
				average_trace.setData(indices, average);
			} else {
				Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(indices,
						Arrays.asList(new IDataset[] { edge1 }), monitor);
				registerTraces(region, plotted);
				plotted = profilePlottingSystem.updatePlot1D(indices, Arrays.asList(new IDataset[] { edge2 }), monitor);
				registerTraces(region, plotted);
				plotted = profilePlottingSystem.updatePlot1D(indices, Arrays.asList(new IDataset[] { average }), monitor);
				registerTraces(region, plotted);
			}
			setTracesColor();
			return Arrays.asList(trace2, trace1, average_trace);
		} else if (!isEdgePlotted && isAveragePlotted) {
			if (tryUpdate && average_trace != null) {
				average_trace.setData(indices, average);
			} else {
				Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(indices,
						Arrays.asList(new IDataset[] { average }), monitor);
				registerTraces(region, plotted);
			}
			setTracesColor();
			return Arrays.asList(average_trace);
		}
		return null;
	}

	@SuppressWarnings("unused")
	private RectangularROI getPositiveBounds(RectangularROI bounds) {
		double[] startpt = bounds.getPoint();
		if (startpt[0] < 0)
			startpt[0] = 0;
		if (startpt[1] < 0)
			startpt[1] = 0;
		bounds.setPoint(startpt);
		bounds.setEndPoint(bounds.getEndPoint());
		return bounds;
	}

	/**
	 * 
	 * @param image
	 * @param region
	 * @param rbs
	 * @param tryUpdate
	 * @param isDrag
	 * @param monitor
	 * @return indices, intensity1, intensity2, average OR null
	 */
	protected Dataset[] getProfiles(IImageTrace image, IRegion region, IROI rbs, boolean tryUpdate, boolean isDrag,
			IProgressMonitor monitor) {

		if (monitor.isCanceled())
			return null;
		if (image == null)
			return null;

		if (!isRegionTypeSupported(region.getRegionType()))
			return null;

		final RectangularROI bounds = (RectangularROI) (rbs == null ? region.getROI() : rbs);
		if (bounds == null)
			return null;
		if (!region.isVisible())
			return null;

		if (monitor.isCanceled())
			return null;

		Dataset data = DatasetUtils.convertToDataset(image.getData());
		if (data instanceof RGBDataset)
			data = ((RGBDataset) data).getRedView();

		Dataset id = DatasetUtils.convertToDataset(image.getData());
		Dataset md = DatasetUtils.convertToDataset(image.getMask());

		Dataset[] boxLine = ROIProfile.boxLine(id, md, bounds, true, isVertical);
		if (boxLine == null)
			return null;

		Dataset average = null;
		if (isAveragePlotted) {
			Dataset[] boxMean = ROIProfile.boxMean(id, md, bounds, true);
			if (isVertical)
				average = boxMean[1];
			else
				average = boxMean[0];
			average.setName("Average");
		}

		Dataset xi = null;

		double ang = bounds.getAngle();
		if (image.getAxes() != null && ang == 0) {
			List<IDataset> axes = image.getAxes();

			int[] spt = bounds.getIntPoint();
			int[] len = bounds.getIntLengths();

			int xstart = 0;
			int xend = 0;
			if (isVertical) {
				xstart = Math.max(0, spt[1]);
				xend = Math.min(spt[1] + len[1], image.getData().getShape()[1]);
			} else {
				xstart = Math.max(0, spt[0]);
				xend = Math.min(spt[0] + len[0], image.getData().getShape()[0]); // ,
			}
			if (isVertical) {
				try {
					IDataset xFull = axes.get(1);
					xi = DatasetUtils.convertToDataset(
							xFull.getSlice(new int[] { xstart }, new int[] { xend + 1 }, new int[] { 1 }));
					xi.setName(xFull.getName());
				} catch (Exception ne) {
					// ignore
				}
			} else {
				try {
					IDataset xFull = axes.get(0);
					xi = DatasetUtils.convertToDataset(
							xFull.getSlice(new int[] { xstart }, new int[] { xend + 1 }, new int[] { 1 }));
					xi.setName(xFull.getName());
				} catch (Exception ne) {
					// ignore
				}
			}
		}

		Dataset intensity1 = boxLine[0];
		intensity1.setName("Edge 1");
		if (xi == null || !Arrays.equals(xi.getShape(), intensity1.getShape())) {
			double xStart = bounds.getPointX();
			double xEnd = bounds.getPointX() + bounds.getLength(0);
			xi = DatasetFactory.createRange(IntegerDataset.class, xStart, xEnd, 1);
			xi.setName("X Pixel");
		}
		final Dataset indices = xi; // Maths.add(xi, bounds.getX()); // Real
									// position

		Dataset intensity2 = boxLine[1];
		intensity2.setName("Edge 2");

		return new Dataset[] { indices, intensity1, intensity2, average};
	}

	/**
	 * Set the traces colour given the type of profile
	 */
	private void setTracesColor() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				ILineTrace edge1_trace = (ILineTrace) profilePlottingSystem.getTrace("Edge 1");
				ILineTrace edge2_trace = (ILineTrace) profilePlottingSystem.getTrace("Edge 2");
				if (isVertical) {
					if (edge1_trace != null && edge2_trace != null) {
						edge1_trace.setTraceColor(ColorConstants.red);
						edge2_trace.setTraceColor(ColorConstants.darkGreen);
					}
				} else {
					if (edge1_trace != null && edge2_trace != null) {
						edge1_trace.setTraceColor(ColorConstants.blue);
						edge2_trace.setTraceColor(ColorConstants.orange);
					}
				}
				ILineTrace av_trace = (ILineTrace) profilePlottingSystem.getTrace("Average");
				if (av_trace != null)
					av_trace.setTraceColor(ColorConstants.cyan);
			}
		});
	}

	/**
	 * TODO add xaxis region (used in perimeter tool)
	 * @param plottingSystem
	 * @param roi
	 * @param roiName
	 * @return
	 */
	@SuppressWarnings("unused")
	private IRegion createXAxisBoxRegion(final IPlottingSystem<?> plottingSystem, final IROI roi,
			final String roiName) {
		try {
			if (roi instanceof RectangularROI) {
				RectangularROI rroi = (RectangularROI) roi;
				IRegion region = plottingSystem.getRegion(roiName);
				// Test if the region is already there and update the
				// currentRegion
				if (region != null) {
					region.setROI(region.getROI());
					region.setVisible(isXAxisROIVisible);
					return region;
				} else {
					IRegion newRegion = plottingSystem.createRegion(roiName, RegionType.XAXIS);
					newRegion.setRegionColor(ColorConstants.blue);
					newRegion.setROI(rroi);
					newRegion.setVisible(isXAxisROIVisible);
					plottingSystem.addRegion(newRegion);
					return newRegion;
				}
			}
			return null;
		} catch (Exception e) {
			logger.error("Couldn't create ROI", e);
			return null;
		}
	}

	@Override
	public void update(IRegion region) {
		this.region = region;
		if (region == null)
			return;
		if (region.getROI() == null)
			return;
		if (region.getROI() instanceof RectangularROI)
			update(region, (RectangularROI) region.getROI(), false);
	}

	protected synchronized void update(IRegion r, RectangularROI rb, boolean isDrag) {
		if (!isActive())
			return;
		if (r != null) {
			if (!isRegionTypeSupported(r.getRegionType()))
				return; // Nothing to do.
			if (!r.isUserRegion())
				return; // Likewise
		}
		if (rb == null)
			return;
		profileJob.profile(r, rb, isDrag);
	}

	private final class ProfileJob extends Job {

		private IRegion currentRegion;
		private RectangularROI currentROI;
		private boolean isDrag;

		ProfileJob() {
			super(getRegionName() + " update");
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		public void profile(IRegion r, RectangularROI rb, boolean isDrag) {
			this.currentRegion = r;
			this.currentROI = rb;
			this.isDrag = isDrag;
			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			createProfile(getImageTrace(), currentRegion, currentROI, true, isDrag, monitor);
			return Status.OK_STATUS;
		}
	}

}
