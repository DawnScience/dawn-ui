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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
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
	private List<AbstractDataset> axes;
	private BoxLineType type;
	private double xPreviousStartValue;
//	private double yPreviousStartValue;

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

			if(type == BoxLineType.VERTICAL_TYPE){
				if(axes != null){
					AbstractDataset axis = axes.get(1);
					double min = axis.getDouble(0);
					double max = axis.getDouble(axis.argMax());
					xPixelAxis.setRange(min, max);
					xPixelAxis.setTitle(axis.getName());
					profilePlottingSystem.setSelectedXAxis(xPixelAxis);
				}
			} else if (type == BoxLineType.HORIZONTAL_TYPE){
				if(axes != null){
					AbstractDataset axis = axes.get(0);
					double min = axis.getDouble(0);
					double max = axis.getDouble(axis.argMax());
					xPixelAxis.setRange(min, max);
					xPixelAxis.setTitle(axis.getName());
					profilePlottingSystem.setSelectedXAxis(xPixelAxis);
				}
			}
		}
		if (yPixelAxis == null) {
//			profilePlottingSystem.setAxisAndTitleVisibility(false, "");
//				this.yPixelAxis = plottingSystem.createAxis(plottingSystem.getSelectedYAxis().getTitle(), false, SWT.TOP);

//			plottingSystem.getSelectedYAxis().setTitle("Intensity");
			this.yPixelAxis = plottingSystem.getSelectedYAxis();
		}
		profilePlottingSystem.setShowLegend(false);
		profilePlottingSystem.setTitle("");
		logger.debug("profilePlottingSystem configured");
	}

	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return (type==RegionType.BOX)||(type==RegionType.XAXIS)||(type==RegionType.YAXIS);
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.BOX;
	}

	@Override
	protected void createProfile(final IImageTrace image, IRegion region, ROIBase rbs, boolean tryUpdate, boolean isDrag,
			IProgressMonitor monitor) {
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
		
		final AbstractDataset line1 = box[0];
		line1.setName("X "+region.getName());
		AbstractDataset xi = IntegerDataset.arange(line1.getSize());
		AbstractDataset x_indices = xi;
		
		final AbstractDataset line2 = box[1];
		line2.setName("Y "+region.getName());
		AbstractDataset yi = IntegerDataset.arange(line2.getSize());
		final AbstractDataset y_indices = yi;

		//if (monitor.isCanceled()) return;
		final ILineTrace x_trace = (ILineTrace)profilePlottingSystem.getTrace("X "+region.getName());
		final ILineTrace y_trace = (ILineTrace)profilePlottingSystem.getTrace("Y "+region.getName());
		if (tryUpdate && x_trace!=null && y_trace!=null) {
			
			getControl().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					if(type == BoxLineType.VERTICAL_TYPE){
						if(axes != null){
							AbstractDataset axis = axes.get(1);
//							xPixelAxis.setLabelDataAndTitle(axis);
							double min = axis.getDouble(0);
							double max = axis.getDouble(axis.argMax());

							updateAxes(image, axis);

							x_trace.setTraceColor(ColorConstants.blue);
							y_trace.setTraceColor(ColorConstants.red);
							x_trace.setData(axis, line1);
							y_trace.setData(axis, line2);
//							System.out.println(min+","+max);
							createXAxisBoxRegion(profilePlottingSystem, new RectangularROI(min, 0, (max-min)/2, 100, 0 ), "X_Axis_box_1");
						}
					} else if (type == BoxLineType.HORIZONTAL_TYPE){
						if(axes != null){
							AbstractDataset axis = axes.get(0);
//							xPixelAxis.setLabelDataAndTitle(axis);
							double min = axis.getDouble(0);
							double max = axis.getDouble(axis.argMax());
							
							updateAxes(image, axis);
							
							x_trace.setTraceColor(ColorConstants.darkGreen);
							y_trace.setTraceColor(ColorConstants.orange);
							x_trace.setData(axis, line1);
							y_trace.setData(axis, line2);
//							System.out.println(min+","+(max-min)/2);
							createXAxisBoxRegion(profilePlottingSystem, new RectangularROI(min, 0, (max-min)/2, 100, 0), "X_Axis_box_2");
						}
					}
				}
			});
		} else {
			profilePlottingSystem.setSelectedXAxis(xPixelAxis);
			profilePlottingSystem.setSelectedYAxis(yPixelAxis);
			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(x_indices, Arrays.asList(new AbstractDataset[]{line1}), monitor);
			registerTraces(region, plotted);
			
//			profilePlottingSystem.setSelectedXAxis(yPixelAxis);
			plotted = profilePlottingSystem.updatePlot1D(y_indices, Arrays.asList(new AbstractDataset[]{line2}), monitor);
			registerTraces(region, plotted);	
		}
	}

	/**
	 * Slice the axes when a zoom occurs on the main plot
	 * TODO axes not correctly being updated yet
	 * @param image
	 * @param data
	 */
	private void updateAxes(IImageTrace image, AbstractDataset data){
		System.out.println("XAxis:"+getPlottingSystem().getSelectedXAxis().getLower());
		IAxis xAxis = getPlottingSystem().getSelectedXAxis();
		IAxis yAxis = getPlottingSystem().getSelectedYAxis();
		
		double xStartValue = xAxis.getLower();
		double yStartValue = yAxis.getLower();
		double xStopValue = xAxis.getUpper();
		double yStopValue = yAxis.getUpper();
		// if there is a change of axes (a zoom on the main plot for example), 
		// we reset the axes on the profile plotting system
		if(xPreviousStartValue != xStartValue){// || yPreviousStartValue != yStartValue){
			xPreviousStartValue = xStartValue;
//			yPreviousStartValue = yStartValue;
			int xStart = xAxis.getValuePosition(xStartValue);
			int yStart = yAxis.getValuePosition(yStartValue);
			int xStop = xAxis.getValuePosition(xStopValue);
			int yStop = yAxis.getValuePosition(yStopValue);
			int xInc = xStart < xStop ? 1 : -1;
			int yInc = yStart < yStart  ? 1 : -1;

			try {
				AbstractDataset slice =  image.getData().getSlice(
					new int[] { yStart, xStart },
					new int[] { yStop, xStop },
					new int[] {yInc, xInc});
				double min = slice.min().doubleValue();
				double max = slice.max().doubleValue();
				xPixelAxis.setRange(min, max);
				xPixelAxis.setTitle(data.getName());
				profilePlottingSystem.setSelectedXAxis(xPixelAxis);
//				xPixelAxis.setLabelDataAndTitle(slice);
			} catch(Exception e){
				
			}
		}
	}

	/**
	 * Returns the tool plotting system
	 */
	public AbstractPlottingSystem getToolPlottingSystem(){
		return profilePlottingSystem;
	}

	public void setAxes(List<AbstractDataset> axes){
		this.axes = axes;
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
