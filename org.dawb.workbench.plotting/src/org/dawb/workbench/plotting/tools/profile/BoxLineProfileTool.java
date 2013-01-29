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

package org.dawb.workbench.plotting.tools.profile;

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
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.BoxLineType;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * BoxLine profile tool
 */
public class BoxLineProfileTool extends ProfileTool implements IProfileToolPage{

	private static final Logger logger = LoggerFactory.getLogger(BoxLineProfileTool.class);
	private IAxis xPixelAxis;
	private IAxis yPixelAxis;
	private BoxLineType type;
	public BoxLineProfileTool() {
		this(BoxLineType.HORIZONTAL_TYPE);
	}
	/**
	 * Constructor to this profile tool
	 * @param type can be either VERTICAL_TYPE or HORIZONTAL_TYPE
	 */
	public BoxLineProfileTool(BoxLineType type){
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
		return (type==RegionType.BOX)||(type==RegionType.COLORBOX)||(type==RegionType.XAXIS)||(type==RegionType.YAXIS);
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.COLORBOX;
	}

	@Override
	protected void createProfile(final IImageTrace image, IRegion region, ROIBase rbs, boolean tryUpdate, boolean isDrag,
			final IProgressMonitor monitor) {
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (!isRegionTypeSupported(region.getRegionType())) return;

		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;
		AbstractDataset[] box = null;
		
		box = ROIProfile.boxLine(image.getData(), image.getMask(), bounds, true, type);
		
		if (box==null) return;
		
		String traceName1 = "", traceName2 = "";
		if(type == BoxLineType.HORIZONTAL_TYPE){
			traceName1 = "Horizontal Profile 1";
			traceName2 = "Horizontal Profile 2";
		} else if(type == BoxLineType.VERTICAL_TYPE){
			traceName1 = "Vertical Profile 1";
			traceName2 = "Vertical Profile 2";
		}
			
		final AbstractDataset line1 = box[0];
		line1.setName(traceName1);
		AbstractDataset xi = IntegerDataset.arange(line1.getSize());
		final AbstractDataset x_indices = xi;
		
		final AbstractDataset line2 = box[1];
		line2.setName(traceName2);
		AbstractDataset yi = IntegerDataset.arange(line2.getSize());
		final AbstractDataset y_indices = yi;

		final List<AbstractDataset> boxesLines = new ArrayList<AbstractDataset>(2);
		boxesLines.add(line1);
		boxesLines.add(line2);
		
		final ILineTrace x_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName1);
		final ILineTrace y_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName2);
		final List<ILineTrace> traces = new ArrayList<ILineTrace>(2);
		traces.add(x_trace);
		traces.add(y_trace);
		
		if (tryUpdate && x_trace!=null && y_trace!=null) {
			Control control = getControl();
			if(control != null && !control.isDisposed()) {
					control.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							List<AbstractDataset> axes = image.getAxes();
							if(type == BoxLineType.VERTICAL_TYPE){

								updateAxes(traces, boxesLines, axes.get(1), bounds.getPointY());
								x_trace.setTraceColor(ColorConstants.blue);
								y_trace.setTraceColor(ColorConstants.red);

							} else if (type == BoxLineType.HORIZONTAL_TYPE){

								updateAxes(traces, boxesLines, axes.get(0), bounds.getPointX());
								x_trace.setTraceColor(ColorConstants.darkGreen);
								y_trace.setTraceColor(ColorConstants.orange);

							}
						}
					});
			}
		} else {
			profilePlottingSystem.setSelectedXAxis(xPixelAxis);
			profilePlottingSystem.setSelectedYAxis(yPixelAxis);
			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(x_indices, Arrays.asList(new AbstractDataset[]{line1}), monitor);
			registerTraces(region, plotted);
			
			plotted = profilePlottingSystem.updatePlot1D(y_indices, Arrays.asList(new AbstractDataset[]{line2}), monitor);
			registerTraces(region, plotted);	
		}
	}

	/**
	 * Updates the profile axes according to the ROI start point
	 * @param profiles
	 * @param boxLines
	 * @param originalAxis
	 * @param axis
	 * @param startPoint
	 */
	private void updateAxes(List<ILineTrace> profiles, List<AbstractDataset> boxesLines,
			AbstractDataset axis, double startPoint){
		// shift the xaxis by yStart
		try {
			double xStart = axis.getElementDoubleAbs((int)Math.round(startPoint));
			double min = axis.getDouble(0);

			axis = new DoubleDataset(axis);
			xStart = axis.getElementDoubleAbs((int)Math.round(startPoint));
			min = axis.getDouble(0);
			axis.iadd(xStart-min);

			profiles.get(0).setData(axis.getSlice(new Slice(0, boxesLines.get(0).getShape()[0], 1)), boxesLines.get(0));
			profiles.get(1).setData(axis.getSlice(new Slice(0, boxesLines.get(1).getShape()[0], 1)), boxesLines.get(1));

			xPixelAxis.setTitle(axis.getName());
			double max = axis.getDouble(axis.argMax());
			createXAxisBoxRegion(profilePlottingSystem, new RectangularROI(min, 0, (max-min)/2, 100, 0), "X_Axis_box");
		
		} catch (ArrayIndexOutOfBoundsException ae) {
			//do nothing
		} catch (Exception e) {
			logger.debug("An exception has occured:"+e);
		}

	}

	/**
	 * Returns the tool plotting system
	 */
	public AbstractPlottingSystem getToolPlottingSystem(){
		return profilePlottingSystem;
	}

	public BoxLineType getType(){
		return type;
	}

	@Override
	public DataReductionInfo export(DataReductionSlice bean) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	public void setLineType(BoxLineType type) {
		this.type = type;
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
}
