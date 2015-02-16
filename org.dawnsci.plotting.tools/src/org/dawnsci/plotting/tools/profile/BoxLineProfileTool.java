/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
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

import org.dawb.common.ui.util.DisplayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

/**
 * BoxLine profile tool
 */
public class BoxLineProfileTool extends ProfileTool implements IProfileToolPage{

	private static final Logger logger = LoggerFactory.getLogger(BoxLineProfileTool.class);
	private IAxis xPixelAxis;
	private IAxis yPixelAxis;
	private boolean isVertical;

	private boolean isAveragePlotted;
	private boolean isEdgePlotted;
	private boolean isXAxisROIVisible = true;
	private String traceName1;
	private String traceName2;
	private String traceName3;
	private ILineTrace x_trace;
	private ILineTrace y_trace;
	private ILineTrace av_trace;

	private ProfileJob profileJob;
	private IRegion xAxisROI;

	public BoxLineProfileTool() {
		this(false);
		this.profileJob = new ProfileJob();
	}
	/**
	 * Constructor to this profile tool
	 * @param isVertical
	 */
	public BoxLineProfileTool(boolean isVertical){
		this.isVertical = isVertical;
	}

	@Override
	protected void configurePlottingSystem(IPlottingSystem plottingSystem) {
		if (xPixelAxis == null) {
			this.xPixelAxis = plottingSystem.getSelectedXAxis();
		}
		if (yPixelAxis == null) {
			plottingSystem.getSelectedYAxis().setTitle("Intensity");
			this.yPixelAxis = plottingSystem.getSelectedYAxis();
		}

		profilePlottingSystem.setShowLegend(false);
		profilePlottingSystem.setTitle("");
		logger.debug("profilePlottingSystem configured");
	}

	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return (type==RegionType.BOX)||(type==RegionType.PERIMETERBOX)||(type==RegionType.XAXIS)||(type==RegionType.YAXIS);
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
	protected ITrace createProfile(IImageTrace image, IRegion region,
								IROI rbs, boolean tryUpdate, boolean isDrag,
								IProgressMonitor monitor) {
		if (monitor.isCanceled()) return null;
		if (image==null) return null;
		
		if (!isRegionTypeSupported(region.getRegionType())) return null;

		RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null) return null;
		if (!region.isVisible()) return null;

		if (monitor.isCanceled()) return null;
		bounds = getPositiveBounds(bounds);
		// vertical and horizontal profiles
		if (isEdgePlotted && !isAveragePlotted) {
			updatePerimeterProfile(image, bounds, region, tryUpdate, monitor);
		} else if (!isEdgePlotted && isAveragePlotted) {
			updateAverageProfile(image, bounds, region, tryUpdate, monitor);
		} else if (isEdgePlotted && isAveragePlotted) {
			updatePerimeterAndAverageProfile(image, bounds, region, tryUpdate, monitor);
		} else if (!isEdgePlotted && !isAveragePlotted) {
			hideTraces();
		}
		return null; // TODO more than one trace so for now, do not return.
	}

	private RectangularROI getPositiveBounds(RectangularROI bounds){
		double[] startpt = bounds.getPoint();
		if(startpt[0] < 0) startpt[0] = 0;
		if(startpt[1] < 0) startpt[1] = 0;
		bounds.setPoint(startpt);
		bounds.setEndPoint(bounds.getEndPoint());
		return bounds;
	}

	/**
	 * Update the perimeter profiles and the average profile
	 * TODO Make a single generic method called updateProfile in order to lower number of lines of code
	 * @param image
	 * @param bounds
	 * @param region
	 * @param tryUpdate
	 * @param monitor
	 */
	private void updatePerimeterAndAverageProfile(
			final IImageTrace image, final RectangularROI bounds,
			IRegion region, boolean tryUpdate,
			IProgressMonitor monitor) {
		Dataset[] boxLine = ROIProfile.boxLine((Dataset)image.getData(), (Dataset)image.getMask(), bounds, true, isVertical);
		Dataset[] boxMean = ROIProfile.boxMean((Dataset)image.getData(), (Dataset)image.getMask(), bounds, true);

		if (boxLine == null) return;
		if (boxMean == null) return;

		setTraceNames();
		Dataset line3 = boxMean[isVertical ? 1 : 0];

		Dataset line1 = boxLine[0];
		line1.setName(traceName1);
		Dataset xi = IntegerDataset.createRange(line1.getSize());
		final Dataset x_indices = xi;

		Dataset line2 = boxLine[1];
		line2.setName(traceName2);
		Dataset yi = IntegerDataset.createRange(line2.getSize());
		final Dataset y_indices = yi;

		// Average profile
		line3.setName(traceName3);
		Dataset av_indices = IntegerDataset.createRange(line3.getSize());

		final List<Dataset> lines = new ArrayList<Dataset>(3);
		lines.add(line1);
		lines.add(line2);
		lines.add(line3);

		x_trace = (ILineTrace) profilePlottingSystem.getTrace(traceName1);
		y_trace = (ILineTrace) profilePlottingSystem.getTrace(traceName2);
		av_trace = (ILineTrace) profilePlottingSystem.getTrace(traceName3);

		final List<ILineTrace> traces = new ArrayList<ILineTrace>(3);
		traces.add(x_trace);
		traces.add(y_trace);
		traces.add(av_trace);

		if (tryUpdate && x_trace != null && y_trace != null && av_trace != null) {
			updateAxes(image, traces, lines, bounds);
		} else {
			profilePlottingSystem.setSelectedXAxis(xPixelAxis);
			profilePlottingSystem.setSelectedYAxis(yPixelAxis);
			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(x_indices, Arrays.asList(new IDataset[] { line1 }), monitor);
			registerTraces(region, plotted);
			plotted = profilePlottingSystem.updatePlot1D(y_indices, Arrays.asList(new IDataset[] { line2 }), monitor);
			registerTraces(region, plotted);
			plotted = profilePlottingSystem.updatePlot1D(av_indices, Arrays.asList(new IDataset[] { line3 }), monitor);
			registerTraces(region, plotted);
			hideTraces();
		}
	}

	/**
	 * Update the perimeter profiles if no average profile
	 * @param image
	 * @param bounds
	 * @param region
	 * @param tryUpdate
	 * @param monitor
	 */
	private void updatePerimeterProfile(
			final IImageTrace image, final RectangularROI bounds,
			IRegion region, boolean tryUpdate,
			IProgressMonitor monitor) {
		Dataset[] boxLine = ROIProfile.boxLine((Dataset)image.getData(), (Dataset)image.getMask(), bounds, true, isVertical);
		if (boxLine == null) return;

		setTraceNames();

		Dataset line1 = boxLine[0];
		line1.setName(traceName1);
		Dataset xi = IntegerDataset.createRange(line1.getSize());
		final Dataset x_indices = xi;

		Dataset line2 = boxLine[1];
		line2.setName(traceName2);
		Dataset yi = IntegerDataset.createRange(line2.getSize());
		final Dataset y_indices = yi;

		final List<Dataset> lines = new ArrayList<Dataset>(3);
		lines.add(line1);
		lines.add(line2);

		x_trace = (ILineTrace) profilePlottingSystem.getTrace(traceName1);
		y_trace = (ILineTrace) profilePlottingSystem.getTrace(traceName2);

		final List<ILineTrace> traces = new ArrayList<ILineTrace>(3);
		traces.add(x_trace);
		traces.add(y_trace);

		if (tryUpdate && x_trace != null && y_trace != null) {
			updateAxes(image, traces, lines, bounds);
		} else {
			profilePlottingSystem.setSelectedXAxis(xPixelAxis);
			profilePlottingSystem.setSelectedYAxis(yPixelAxis);
			Collection<ITrace> plotted = null;
			plotted = profilePlottingSystem.updatePlot1D(x_indices, Arrays.asList(new IDataset[] { line1 }), monitor);
			registerTraces(region, plotted);
			plotted = profilePlottingSystem.updatePlot1D(y_indices, Arrays.asList(new IDataset[] { line2 }), monitor);
			registerTraces(region, plotted);
		}
	}

	/**
	 * Update the average profile without the perimeter profiles
	 * @param image
	 * @param bounds
	 * @param region
	 * @param tryUpdate
	 * @param monitor
	 */
	private void updateAverageProfile(
			final IImageTrace image, final RectangularROI bounds,
			IRegion region, boolean tryUpdate,
			IProgressMonitor monitor) {
		Dataset[] boxMean = ROIProfile.boxMean((Dataset)image.getData(), (Dataset)image.getMask(), bounds, true);

		if (boxMean==null) return;

		setTraceNames();
		Dataset line3 = boxMean[isVertical ? 1 : 0];

		// Average profile
		line3.setName(traceName3);
		Dataset av_indices = IntegerDataset.createRange(line3.getSize());

		final List<Dataset> lines = new ArrayList<Dataset>(1);
		lines.add(line3);

		av_trace = (ILineTrace) profilePlottingSystem.getTrace(traceName3);

		final List<ILineTrace> traces = new ArrayList<ILineTrace>(1);
		traces.add(av_trace);

		if (tryUpdate && av_trace != null) {
			updateAxes(image, traces, lines, bounds);
		} else {
			profilePlottingSystem.setSelectedXAxis(xPixelAxis);
			profilePlottingSystem.setSelectedYAxis(yPixelAxis);
			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(av_indices, Arrays.asList(new IDataset[] { line3 }), monitor);
			registerTraces(region, plotted);
		}
	}

	/**
	 * Set the trace names given the type of profile
	 */
	private void setTraceNames(){
		if (isVertical) {
			traceName1 = "Left Profile";
			traceName2 = "Right Profile";
			traceName3 = "Vertical Average Profile";
		} else {
			traceName1 = "Top Profile";
			traceName2 = "Bottom Profile";
			traceName3 = "Horizontal Average Profile";
		}
	}

	private void updateAxes(final IImageTrace image, final List<ILineTrace> traces, final List<Dataset> lines, final RectangularROI bounds){
		DisplayUtils.runInDisplayThread(false, getControl(), new Runnable() {
			@Override
			public void run() {
				List<IDataset> axes = image.getAxes();
				if (axes != null && axes.size() > 0) {
					if (isVertical) {
						updateAxes(traces, lines, (Dataset)axes.get(1), bounds.getPointY());
					} else {
						updateAxes(traces, lines, (Dataset)axes.get(0), bounds.getPointX());
					}
				} else { // if no axes we set them manually according to
							// the data shape
					int[] shapes = image.getData().getShape();
					if (isVertical) {
						int[] verticalAxis = new int[shapes[1]];
						for (int i = 0; i < verticalAxis.length; i++) {
							verticalAxis[i] = i;
						}
						Dataset vertical = new IntegerDataset(verticalAxis, shapes[1]);
						updateAxes(traces, lines, vertical, bounds.getPointY());
					} else {
						int[] horizontalAxis = new int[shapes[0]];
						for (int i = 0; i < horizontalAxis.length; i++) {
							horizontalAxis[i] = i;
						}
						Dataset horizontal = new IntegerDataset(horizontalAxis, shapes[0]);
						updateAxes(traces, lines, horizontal, bounds.getPointX());
					}
				}
				setTracesColor();
				hideTraces();
			}
		});
	}

	/**
	 * Updates the profile axes according to the ROI start point
	 * @param profiles
	 * @param boxLines
	 * @param originalAxis
	 * @param axis
	 * @param startPoint
	 */
	private void updateAxes(List<ILineTrace> profiles, List<Dataset> lines, 
			Dataset axis, double startPoint){
		// shift the xaxis by yStart
		try {
			double xStart = axis.getDouble((int)Math.round(startPoint));
			double min = axis.getDouble(0);

			axis = new DoubleDataset(axis);
			xStart = axis.getDouble((int)Math.round(startPoint));
			min = axis.getDouble(0);
			axis.iadd(xStart-min);

			if(profiles == null) return;
			if(isEdgePlotted && !isAveragePlotted){
				profiles.get(0).setData(axis.getSlice(new Slice(0, lines.get(0).getShape()[0], 1)), lines.get(0));
				profiles.get(1).setData(axis.getSlice(new Slice(0, lines.get(1).getShape()[0], 1)), lines.get(1));
			}
			else if(!isEdgePlotted && isAveragePlotted)
				profiles.get(0).setData(axis.getSlice(new Slice(0, lines.get(0).getShape()[0], 1)), lines.get(0));
			else if(isEdgePlotted && isAveragePlotted){
				profiles.get(0).setData(axis.getSlice(new Slice(0, lines.get(0).getShape()[0], 1)), lines.get(0));
				profiles.get(1).setData(axis.getSlice(new Slice(0, lines.get(1).getShape()[0], 1)), lines.get(1));
				profiles.get(2).setData(axis.getSlice(new Slice(0, lines.get(2).getShape()[0], 1)), lines.get(2));
			}

			double max = axis.getElementDoubleAbs(axis.argMax());
			xPixelAxis.setTitle(axis.getName());
			xAxisROI = createXAxisBoxRegion(profilePlottingSystem, new RectangularROI(min, 0, (max-min)/2, 100, 0), "X_Axis_box");
		
		} catch (ArrayIndexOutOfBoundsException ae) {
			//do nothing
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("An exception has occured:"+e);
		}
	}

	/**
	 * Set the traces colour given the type of profile
	 */
	private void setTracesColor(){
		setTraceNames();
		x_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName1);
		y_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName2);
		if (isVertical) {
			if(x_trace != null && y_trace != null){
				x_trace.setTraceColor(ColorConstants.red);
				y_trace.setTraceColor(ColorConstants.darkGreen);
			}
		} else {
			if(x_trace != null && y_trace != null){
				x_trace.setTraceColor(ColorConstants.blue);
				y_trace.setTraceColor(ColorConstants.orange);
			}
		}
		av_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName3);
		if(av_trace != null)
			av_trace.setTraceColor(ColorConstants.cyan);
	}

	private void hideTraces(){
		DisplayUtils.runInDisplayThread(true, getControl(), new Runnable(){
			@Override
			public void run() {
				if(x_trace != null && y_trace != null){
					x_trace.setVisible(isEdgePlotted);
					y_trace.setVisible(isEdgePlotted);
				} 
				if(av_trace != null)
					av_trace.setVisible(isAveragePlotted);
			}
		});
	}

	private IRegion createXAxisBoxRegion(final IPlottingSystem plottingSystem, 
			final IROI roi, final String roiName){
		try {
			if(roi instanceof RectangularROI){
				RectangularROI rroi = (RectangularROI)roi;
				IRegion region = plottingSystem.getRegion(roiName);
				//Test if the region is already there and update the currentRegion
				if(region!=null){
					region.setROI(region.getROI());
					region.setVisible(isXAxisROIVisible);
					return region;
				}else {
					IRegion newRegion = plottingSystem.createRegion(roiName, RegionType.XAXIS);
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
		if(region == null) return;
		if(region.getROI() == null) return;
		if(region.getROI() instanceof RectangularROI)
			update(region, (RectangularROI)region.getROI(), false);
	}

	protected synchronized void update(IRegion r, RectangularROI rb, boolean isDrag) {
		if (!isActive()) return;
		if (r!=null) {
			if(!isRegionTypeSupported(r.getRegionType())) return; // Nothing to do.
			if (!r.isUserRegion()) return; // Likewise
		}
		if(rb == null) return;
		profileJob.profile(r, rb, isDrag);
	}

	private final class ProfileJob extends Job {

		private   IRegion                currentRegion;
		private   RectangularROI currentROI;
		private   boolean                isDrag;

		ProfileJob() {
			super(getRegionName()+" update");
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		public void profile(IRegion r, RectangularROI rb, boolean isDrag) {
			this.currentRegion = r;
			this.currentROI    = rb;
			this.isDrag        = isDrag;
			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			createProfile(getImageTrace(), currentRegion, currentROI, true, isDrag, monitor);
			return Status.OK_STATUS;
		}
	}

}
