/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.tools.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.axis.IAxis;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.tool.IProfileToolPage;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * BoxLine profile tool
 */
public class BoxLineProfileTool extends ProfileTool implements IProfileToolPage{

	private static final Logger logger = LoggerFactory.getLogger(BoxLineProfileTool.class);
	private IAxis xPixelAxis;
	private IAxis yPixelAxis;
	private int type;

	private boolean isAveragePlotted;
	private boolean isEdgePlotted;
	private String traceName1;
	private String traceName2;
	private String traceName3;
	private ILineTrace x_trace;
	private ILineTrace y_trace;
	private ILineTrace av_trace;

	private ProfileJob profileJob;

	public BoxLineProfileTool() {
		this(SWT.HORIZONTAL);
		this.profileJob = new ProfileJob();
	}
	/**
	 * Constructor to this profile tool
	 * @param type can be either VERTICAL or HORIZONTAL
	 */
	public BoxLineProfileTool(int type){
		this.type = type;
	}

	@Override
	protected void configurePlottingSystem(AbstractPlottingSystem plottingSystem) {
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
	public int getType(){
		return type;
	}

	@Override
	public DataReductionInfo export(DataReductionSlice bean) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	public void setLineType(int type) {
		this.type = type;
	}

	@Override
	protected void createProfile(IImageTrace image, IRegion region,
			ROIBase rbs, boolean tryUpdate, boolean isDrag,
			IProgressMonitor monitor) {
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (!isRegionTypeSupported(region.getRegionType())) return;

		RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;
		bounds = getPositiveBounds(bounds);
		// vertical and horizontal profiles
		if (isEdgePlotted && !isAveragePlotted) {
			updatePerimeterProfile(image, bounds, region, tryUpdate, monitor);
		} else if (!isEdgePlotted && isAveragePlotted) {
			updateAverageProfile(image, bounds, region, tryUpdate, monitor);
		} else if (isEdgePlotted && isAveragePlotted) {
			updatePerimeterAndAverageProfile(image, bounds, region, tryUpdate, monitor);
		} else if (!isEdgePlotted && !isAveragePlotted) {
			removeTraces();
		}
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
	 * @param profilePlottingSystem
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
		AbstractDataset[] boxLine = ROIProfile.boxLine((AbstractDataset)image.getData(), (AbstractDataset)image.getMask(), bounds, true, type);
		AbstractDataset[] boxMean = ROIProfile.boxMean((AbstractDataset)image.getData(), (AbstractDataset)image.getMask(), bounds, true);

		if (boxLine == null) return;
		if (boxMean == null) return;

		setTraceNames();
		AbstractDataset line3 = null;

		if (type == SWT.HORIZONTAL) line3 = boxMean[0];
		else if (type == SWT.VERTICAL) line3 = boxMean[1];

		AbstractDataset line1 = boxLine[0];
		line1.setName(traceName1);
		AbstractDataset xi = IntegerDataset.arange(line1.getSize());
		final AbstractDataset x_indices = xi;

		AbstractDataset line2 = boxLine[1];
		line2.setName(traceName2);
		AbstractDataset yi = IntegerDataset.arange(line2.getSize());
		final AbstractDataset y_indices = yi;

		// Average profile
		line3.setName(traceName3);
		AbstractDataset av_indices = IntegerDataset.arange(line3.getSize());

		final List<AbstractDataset> lines = new ArrayList<AbstractDataset>(3);
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
			Control control = getControl();
			if (control != null && !control.isDisposed()) {
				control.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						List<AbstractDataset> axes = image.getAxes();
						if (axes != null) {
							if (type == SWT.VERTICAL) {
								updateAxes(traces, lines, (AbstractDataset)axes.get(1), bounds.getPointY());
							} else if (type == SWT.HORIZONTAL) {
								updateAxes(traces, lines, (AbstractDataset)axes.get(0), bounds.getPointX());
							}
						} else { // if no axes we set them manually according to
									// the data shape
							int[] shapes = image.getData().getShape();
							if (type == SWT.VERTICAL) {
								int[] verticalAxis = new int[shapes[1]];
								for (int i = 0; i < verticalAxis.length; i++) {
									verticalAxis[i] = i;
								}
								AbstractDataset vertical = new IntegerDataset(verticalAxis, shapes[1]);
								updateAxes(traces, lines, vertical, bounds.getPointY());
							} else if (type == SWT.HORIZONTAL) {
								int[] horizontalAxis = new int[shapes[0]];
								for (int i = 0; i < horizontalAxis.length; i++) {
									horizontalAxis[i] = i;
								}
								AbstractDataset horizontal = new IntegerDataset(horizontalAxis, shapes[0]);
								updateAxes(traces, lines, horizontal, bounds.getPointX());
							}
						}
						setTracesColor();
						// clean traces if necessary
						removeTraces();
					}
				});
			}
		} else {
			profilePlottingSystem.setSelectedXAxis(xPixelAxis);
			profilePlottingSystem.setSelectedYAxis(yPixelAxis);
			
			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(x_indices, Arrays.asList(new AbstractDataset[] { line1 }), monitor);
			registerTraces(region, plotted);
			plotted = profilePlottingSystem.updatePlot1D(y_indices, Arrays.asList(new AbstractDataset[] { line2 }), monitor);
			registerTraces(region, plotted);
			plotted = profilePlottingSystem.updatePlot1D(av_indices, Arrays.asList(new AbstractDataset[] { line3 }), monitor);
		}
		setTracesColor();
		removeTraces();
	}

	/**
	 * Update the perimeter profiles if no average profile
	 * @param profilePlottingSystem
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
		AbstractDataset[] boxLine = ROIProfile.boxLine((AbstractDataset)image.getData(), (AbstractDataset)image.getMask(), bounds, true, type);
		if (boxLine == null) return;

		setTraceNames();

		AbstractDataset line1 = boxLine[0];
		line1.setName(traceName1);
		AbstractDataset xi = IntegerDataset.arange(line1.getSize());
		final AbstractDataset x_indices = xi;

		AbstractDataset line2 = boxLine[1];
		line2.setName(traceName2);
		AbstractDataset yi = IntegerDataset.arange(line2.getSize());
		final AbstractDataset y_indices = yi;

		final List<AbstractDataset> lines = new ArrayList<AbstractDataset>(3);
		lines.add(line1);
		lines.add(line2);

		x_trace = (ILineTrace) profilePlottingSystem.getTrace(traceName1);
		y_trace = (ILineTrace) profilePlottingSystem.getTrace(traceName2);

		final List<ILineTrace> traces = new ArrayList<ILineTrace>(3);
		traces.add(x_trace);
		traces.add(y_trace);

		if (tryUpdate && x_trace != null && y_trace != null) {
			Control control = getControl();
			if (control != null && !control.isDisposed()) {
				control.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						List<AbstractDataset> axes = image.getAxes();
						if (axes != null && axes.size() > 0) {
							if (type == SWT.VERTICAL) {
								updateAxes(traces, lines, (AbstractDataset)axes.get(1), bounds.getPointY());
							} else if (type == SWT.HORIZONTAL) {
								updateAxes(traces, lines, (AbstractDataset)axes.get(0), bounds.getPointX());
							}
						} else { // if no axes we set them manually according to
									// the data shape
							int[] shapes = image.getData().getShape();
							if (type == SWT.VERTICAL) {
								int[] verticalAxis = new int[shapes[1]];
								for (int i = 0; i < verticalAxis.length; i++) {
									verticalAxis[i] = i;
								}
								AbstractDataset vertical = new IntegerDataset(verticalAxis, shapes[1]);
								updateAxes(traces, lines, vertical, bounds.getPointY());
							} else if (type == SWT.HORIZONTAL) {
								int[] horizontalAxis = new int[shapes[0]];
								for (int i = 0; i < horizontalAxis.length; i++) {
									horizontalAxis[i] = i;
								}
								AbstractDataset horizontal = new IntegerDataset(horizontalAxis, shapes[0]);
								updateAxes(traces, lines, horizontal, bounds.getPointX());
							}
						}
						setTracesColor();
						// clean traces if necessary
						removeTraces();
					}
				});
			}
		}

		else {
			profilePlottingSystem.setSelectedXAxis(xPixelAxis);
			profilePlottingSystem.setSelectedYAxis(yPixelAxis);
			Collection<ITrace> plotted = null;
			plotted = profilePlottingSystem.updatePlot1D(x_indices, Arrays.asList(new AbstractDataset[] { line1 }), monitor);
			registerTraces(region, plotted);
			plotted = profilePlottingSystem.updatePlot1D(y_indices, Arrays.asList(new AbstractDataset[] { line2 }), monitor);
		}
		setTracesColor();
		removeTraces();
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
		AbstractDataset[] boxMean = ROIProfile.boxMean((AbstractDataset)image.getData(), (AbstractDataset)image.getMask(), bounds, true);

		if (boxMean==null) return;

		setTraceNames();
		AbstractDataset line3 = null;
		if (type == SWT.HORIZONTAL) line3 = boxMean[0];
		else if (type == SWT.VERTICAL) line3 = boxMean[1];

		// Average profile
		line3.setName(traceName3);
		AbstractDataset av_indices = IntegerDataset.arange(line3.getSize());

		final List<AbstractDataset> lines = new ArrayList<AbstractDataset>(1);
		lines.add(line3);

		av_trace = (ILineTrace) profilePlottingSystem.getTrace(traceName3);

		final List<ILineTrace> traces = new ArrayList<ILineTrace>(1);
		traces.add(av_trace);

		if (tryUpdate && av_trace != null) {
			Control control = getControl();
			if (control != null && !control.isDisposed()) {
				control.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						List<AbstractDataset> axes = image.getAxes();
						if (axes != null) {
							if (type == SWT.VERTICAL) {
								updateAxes(traces, lines, (AbstractDataset)axes.get(1), bounds.getPointY());
							} else if (type == SWT.HORIZONTAL) {
								updateAxes(traces, lines, (AbstractDataset)axes.get(0), bounds.getPointX());
							}
						} else { // if no axes we set them manually according to
									// the data shape
							int[] shapes = image.getData().getShape();
							if (type == SWT.VERTICAL) {
								int[] verticalAxis = new int[shapes[1]];
								for (int i = 0; i < verticalAxis.length; i++) {
									verticalAxis[i] = i;
								}
								AbstractDataset vertical = new IntegerDataset(verticalAxis, shapes[1]);
								updateAxes(traces, lines, vertical, bounds.getPointY());
							} else if (type == SWT.HORIZONTAL) {
								int[] horizontalAxis = new int[shapes[0]];
								for (int i = 0; i < horizontalAxis.length; i++) {
									horizontalAxis[i] = i;
								}
								AbstractDataset horizontal = new IntegerDataset(horizontalAxis, shapes[0]);
								updateAxes(traces, lines, horizontal, bounds.getPointX());
							}
						}
						setTracesColor();
						// clean traces if necessary
						removeTraces();
					}
				});
			}
		} else {
			profilePlottingSystem.setSelectedXAxis(xPixelAxis);
			profilePlottingSystem.setSelectedYAxis(yPixelAxis);
			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(av_indices, Arrays.asList(new AbstractDataset[] { line3 }), monitor);
			registerTraces(region, plotted);
		}
		setTracesColor();
		removeTraces();
	}

	private void removeTraces(){
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				setTraceNames();
				if(!isEdgePlotted){
					x_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName1);
					y_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName2);
					if(x_trace != null && y_trace != null){
						profilePlottingSystem.removeTrace(x_trace);
						profilePlottingSystem.removeTrace(y_trace);
					}
				}
				if(!isAveragePlotted){
					av_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName3);
					if(av_trace != null){
						profilePlottingSystem.removeTrace(av_trace);
					}
				}
			}
		});
	}

	/**
	 * Set the trace names given the type of profile
	 */
	private void setTraceNames(){
		if (type == SWT.HORIZONTAL) {
			traceName1 = "Top Profile";
			traceName2 = "Bottom Profile";
			traceName3 = "Horizontal Average Profile";
		} else if (type == SWT.VERTICAL) {
			traceName1 = "Left Profile";
			traceName2 = "Right Profile";
			traceName3 = "Vertical Average Profile";
		}
	}

	/**
	 * Set the traces colour given the type of profile
	 */
	private void setTracesColor(){
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				setTraceNames();
				x_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName1);
				y_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName2);
				if (type == SWT.VERTICAL) {
					if(x_trace != null && y_trace != null){
						x_trace.setTraceColor(ColorConstants.blue);
						y_trace.setTraceColor(ColorConstants.red);
					}
				} else if (type == SWT.HORIZONTAL) {
					if(x_trace != null && y_trace != null){
						x_trace.setTraceColor(ColorConstants.darkGreen);
						y_trace.setTraceColor(ColorConstants.orange);
					}
				}
				av_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName3);
				if(av_trace != null)
					av_trace.setTraceColor(ColorConstants.cyan);
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
	private void updateAxes(List<ILineTrace> profiles, List<AbstractDataset> lines, 
			AbstractDataset axis, double startPoint){
		// shift the xaxis by yStart
		try {
			double xStart = axis.getElementDoubleAbs((int)Math.round(startPoint));
			double min = axis.getDouble(0);

			axis = new DoubleDataset(axis);
			xStart = axis.getElementDoubleAbs((int)Math.round(startPoint));
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

			double max = axis.getDouble(axis.argMax());
			xPixelAxis.setTitle(axis.getName());
			createXAxisBoxRegion(profilePlottingSystem, new RectangularROI(min, 0, (max-min)/2, 100, 0), "X_Axis_box");
		
		} catch (ArrayIndexOutOfBoundsException ae) {
			//do nothing
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("An exception has occured:"+e);
		}
	}

	private void createXAxisBoxRegion(final AbstractPlottingSystem plottingSystem, 
			final ROIBase roi, final String roiName){
		try {
			if(roi instanceof RectangularROI){
				RectangularROI rroi = (RectangularROI)roi;
				IRegion region = plottingSystem.getRegion(roiName);
				//Test if the region is already there and update the currentRegion
				if(region!=null&&region.isVisible()){
					region.setROI(region.getROI());
				}else {
					IRegion newRegion = plottingSystem.createRegion(roiName, RegionType.XAXIS);
					newRegion.setROI(rroi);
					plottingSystem.addRegion(newRegion);
				}
			}
		} catch (Exception e) {
			logger.error("Couldn't create ROI", e);
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
