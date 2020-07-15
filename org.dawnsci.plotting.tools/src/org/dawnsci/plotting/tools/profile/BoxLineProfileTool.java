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
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
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
import org.eclipse.january.dataset.Slice;
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
@SuppressWarnings("unchecked")
public class BoxLineProfileTool extends ProfileTool implements IProfileToolPage {

	private static final Logger logger = LoggerFactory.getLogger(BoxLineProfileTool.class);
	private IAxis xPixelAxis;
	private IAxis yPixelAxis;
	private boolean isVertical;

	private boolean isAveragePlotted;
	private boolean isEdgePlotted;
	private boolean isXAxisROIVisible = false;

	private IRegion xAxisROI;
	private IRegion region;

	private static final String EDGE1 = "Edge 1";
	private static final String EDGE2 = "Edge 2";
	private static final String AVERAGE = "Average";

	public BoxLineProfileTool() {
		this(false, true, false);
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
				ILineTrace trace1 = (ILineTrace) profilePlottingSystem.getTrace(EDGE1);
				ILineTrace trace2 = (ILineTrace) profilePlottingSystem.getTrace(EDGE2);
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
		if (region != null)
			update(region, (RectangularROI) region.getROI(), false);
	}

	public boolean isEdgePlotted() {
		return isEdgePlotted;
	}

	@Override
	public void setPlotEdgeProfile(boolean isEdgePlotted) {
		this.isEdgePlotted = isEdgePlotted;
		if (region != null)
			update(region, (RectangularROI) region.getROI(), false);
	}

	@Override
	public void setXAxisROIVisible(boolean isXAxisROIVisible) {
		this.isXAxisROIVisible = isXAxisROIVisible;
		if (xAxisROI != null) {
			xAxisROI.setVisible(isXAxisROIVisible);
		} else {
			double ptx = xPixelAxis.getLower();
			double width = xPixelAxis.getUpper() - ptx;
			IROI roi = new XAxisBoxROI(ptx, 0, width, 0, 0);
			xAxisROI = createXAxisBoxRegion(roi);
		}
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

		// get the profiles and x indices (edge 1, edge 2, and average)
		Dataset[] profile = getProfiles(image, region, rbs, tryUpdate, isDrag, monitor);
		if (profile == null)
			return null;

		final Dataset indices = profile[0];
		final Dataset edge1 = profile[1];
		final Dataset edge2 = profile[2];
		Dataset average = profile[3];

		if (!isRegionTypeSupported(region.getRegionType()))
			return null;

		ILineTrace trace1 = (ILineTrace) profilePlottingSystem.getTrace(EDGE1);
		ILineTrace trace2 = (ILineTrace) profilePlottingSystem.getTrace(EDGE2);
		ILineTrace average_trace = (ILineTrace) profilePlottingSystem.getTrace(AVERAGE);
		// hide traces accordingly
		setTracesVisible(trace1, trace2, average_trace);
		
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

	private void setTracesVisible(final ITrace edge1, final ITrace edge2, final ITrace average) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (average != null) {
					average.setVisible(isAveragePlotted);
				}
				if (edge1 != null && edge2 != null) {
					edge1.setVisible(isEdgePlotted);
					edge2.setVisible(isEdgePlotted);
				}
			}
		});
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
	 * Create the Profiles as well as the x indices if custom axes.
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
		if (data == null) {
			return null;
		}

		Dataset md = DatasetUtils.convertToDataset(image.getMask());
		List<IDataset> axes = image.getAxes();

		Dataset[] boxLine = ROIProfile.boxLine(data, md, bounds, true, isVertical);
		if (boxLine == null)
			return null;

		Dataset average = null;
		if (isAveragePlotted) {
			Dataset[] boxMean = ROIProfile.boxMean(data, md, bounds, true);
			if (isVertical)
				average = boxMean[1];
			else
				average = boxMean[0];
			average.setName(AVERAGE);
		}

		Dataset xi = null;

		double ang = bounds.getAngle();
		int a = isVertical ? 1 : 0;
		if (axes != null && ang == 0) {

			int[] spt = bounds.getIntPoint();
			int[] len = bounds.getIntLengths();

			IDataset xFull = axes.get(a);

			if (xFull != null) {
				int xstart = Math.max(0, spt[a]);
				int xend = Math.min(spt[a] + len[a] + 1, data.getShapeRef()[a]);
				xend = Math.min(xend, xFull.getSize()); // assume axis is 1D
				xi = DatasetUtils.convertToDataset(xFull.getSlice(new Slice(xstart, xend)));
				xi.setName(xFull.getName());
			}
		}

		Dataset intensity1 = boxLine[0];
		intensity1.setName(EDGE1);
		if (xi == null || !Arrays.equals(xi.getShapeRef(), intensity1.getShapeRef())) {
			double xStart = bounds.getPoint()[a];
			double xEnd = xStart + bounds.getLength(a);
			xi = DatasetFactory.createLinearSpace(IntegerDataset.class, xStart, xEnd, intensity1.getSize());
			xi.setName(isVertical ? "Y Pixel" : "X Pixel");
		}

		Dataset intensity2 = boxLine[1];
		intensity2.setName(EDGE2);

		return new Dataset[] { xi, intensity1, intensity2, average};
	}

	/**
	 * Set the traces colour given the type of profile
	 */
	private void setTracesColor() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				ILineTrace edge1_trace = (ILineTrace) profilePlottingSystem.getTrace(EDGE1);
				ILineTrace edge2_trace = (ILineTrace) profilePlottingSystem.getTrace(EDGE2);
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
				ILineTrace av_trace = (ILineTrace) profilePlottingSystem.getTrace(AVERAGE);
				if (av_trace != null)
					av_trace.setTraceColor(ColorConstants.cyan);
			}
		});
	}

	/**
	 * Adds xaxis region
	 * 
	 * @param plottingSystem
	 * @param roi
	 * @param roiName
	 * @return
	 */
	private IRegion createXAxisBoxRegion(IROI roi) {
		try {
			if (roi instanceof RectangularROI) {
				RectangularROI rroi = (RectangularROI) roi;
				IRegion region = profilePlottingSystem.getRegion("XAxis Region");
				// Test if the region is already there and update the
				// currentRegion
				if (region != null) {
					region.setROI(region.getROI());
					region.setVisible(isXAxisROIVisible);
					return region;
				} else {
					IRegion newRegion = profilePlottingSystem.createRegion("XAxis Region", RegionType.XAXIS);
					newRegion.setRegionColor(ColorConstants.blue);
					newRegion.setROI(rroi);
					newRegion.setVisible(isXAxisROIVisible);
					profilePlottingSystem.addRegion(newRegion);
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
}
