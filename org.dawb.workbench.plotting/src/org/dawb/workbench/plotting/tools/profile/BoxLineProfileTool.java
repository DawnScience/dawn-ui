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
//			xPixelAxis.setTitle(xPixelAxis.getTitle());
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
	protected void createProfile(IImageTrace image, IRegion region, ROIBase rbs, boolean tryUpdate, boolean isDrag,
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
		final AbstractDataset x_indices = xi; // Maths.add(xi, bounds.getX()); // Real position
		//x_indices.setName("X Pixel");
		
		final AbstractDataset line2 = box[1];
		line2.setName("Y "+region.getName());
		AbstractDataset yi = IntegerDataset.arange(line2.getSize());
		final AbstractDataset y_indices = yi; // Maths.add(yi, bounds.getY()); // Real position
		//y_indices.setName("Y Pixel");

		//if (monitor.isCanceled()) return;
		final ILineTrace x_trace = (ILineTrace)profilePlottingSystem.getTrace("X "+region.getName());
		final ILineTrace y_trace = (ILineTrace)profilePlottingSystem.getTrace("Y "+region.getName());

		if (tryUpdate && x_trace!=null && y_trace!=null) {
			
			getControl().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					if(type == BoxLineType.VERTICAL_TYPE){
//						profilePlottingSystem.setSelectedXAxis(yPixelAxis);
						if(axes != null){
							AbstractDataset xAxis = axes.get(0);
//							xAxis.reverse(null);
//							AbstractDataset yAxis = axes.get(1);
//							xPixelAxis.setMaximumRange(0, y_trace.getXData().getSize());
//							yPixelAxis.setMaximumRange(0, y_trace.getYData().getSize());
//							xPixelAxis.setLabelDataAndTitle(xAxis);
							yPixelAxis.setLabelDataAndTitle(xAxis);
//							xPixelAxis.setRange(xAxis.getDouble(xAxis.argMin()), xAxis.getDouble(xAxis.argMax()));
//							yPixelAxis.setRange(yAxis.getDouble(yAxis.argMin()), yAxis.getDouble(yAxis.argMax()));
							
							profilePlottingSystem.setSelectedXAxis(xPixelAxis);
							profilePlottingSystem.setSelectedYAxis(yPixelAxis);
							x_trace.setData(line1, xAxis);
//							profilePlottingSystem.setSelectedXAxis(yPixelAxis);
							y_trace.setData(line2, xAxis);
						}
					} else if (type == BoxLineType.HORIZONTAL_TYPE){
//						profilePlottingSystem.setSelectedXAxis(yPixelAxis);
						if(axes != null){
							AbstractDataset xAxis = axes.get(0);
//							AbstractDataset yAxis = axes.get(1);
//							xPixelAxis.setMaximumRange(0, y_trace.getXData().getSize());
//							yPixelAxis.setMaximumRange(0, y_trace.getYData().getSize());
//							xPixelAxis.setLabelDataAndTitle(xAxis);
							yPixelAxis.setLabelDataAndTitle(xAxis);
//							xPixelAxis.setRange(xAxis.getDouble(xAxis.argMin()), xAxis.getDouble(xAxis.argMax()));
//							yPixelAxis.setRange(yAxis.getDouble(yAxis.argMin()), yAxis.getDouble(yAxis.argMax()));
							
							profilePlottingSystem.setSelectedXAxis(xPixelAxis);
							profilePlottingSystem.setSelectedYAxis(yPixelAxis);
							x_trace.setData(xAxis, line1);
//							profilePlottingSystem.setSelectedXAxis(yPixelAxis);
							y_trace.setData(xAxis, line2);
						}
					}
				}
			});

			
		} else {
						
//			profilePlottingSystem.setSelectedXAxis(yPixelAxis);
			profilePlottingSystem.setSelectedXAxis(xPixelAxis);
			profilePlottingSystem.setSelectedYAxis(yPixelAxis);
			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(x_indices, Arrays.asList(new AbstractDataset[]{line1}), monitor);
			registerTraces(region, plotted);
			
//			profilePlottingSystem.setSelectedXAxis(yPixelAxis);
			plotted = profilePlottingSystem.updatePlot1D(y_indices, Arrays.asList(new AbstractDataset[]{line2}), monitor);
			registerTraces(region, plotted);	
		}
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

	
}
