/*
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
/*
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

package org.dawnsci.plotting.tools;

import java.text.DecimalFormat;

import org.dawnsci.plotting.tools.preference.InfoPixelConstants;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.TraceUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;

public class InfoPixelLabelProvider extends ColumnLabelProvider {


	private final int column;
	private final InfoPixelTool tool;

	private static final Logger logger = LoggerFactory.getLogger(InfoPixelLabelProvider.class);

	public InfoPixelLabelProvider(InfoPixelTool tool, int i) {

		this.column = i;
		this.tool   = tool;
	}


	@Override
	public String getText(Object element) {
		//TODO could use ToolPageRole on the tool to separate 1D and 2D cases better
		double xIndex = 0.0;
		double yIndex = 0.0;
		double xLabel = Double.NaN;
		double yLabel = Double.NaN;
		
		final IImageTrace trace = tool.getImageTrace();
		ICoordinateSystem coords = null;
		try {
			if (element instanceof IRegion){
				
				final IRegion region = (IRegion)element;
				coords = region.getCoordinateSystem();

				if (region.getRegionType()==RegionType.POINT) {
					PointROI pr = (PointROI)tool.getBounds(region);
					xIndex = pr.getPointX();
					yIndex = pr.getPointY();
					
					// Sometimes the image can have axes set. In this case we need the point
					// ROI in the axes coordinates
					if (trace!=null) {
						try {
							pr = (PointROI)trace.getRegionInAxisCoordinates(pr);
							xLabel = pr.getPointX();
							yLabel = pr.getPointY();
						} catch (Exception aie) {
						    return "-";
						}
					}
					
				} else if (region.getRegionType() == RegionType.XAXIS_LINE){

					xIndex = region.getROI().getPointX();
					yIndex = region.getROI().getPointY();
			
					final double[] dp = new double[]{xIndex, yIndex};
					try {
						if (trace!=null) trace.getPointInAxisCoordinates(dp);
						xLabel = dp[0];
						yLabel = dp[1];
					} catch (Exception aie) {
					    return "-";
					}
				}
	
			}else {
				return null;
			}
			
			if (Double.isNaN(xLabel)) xLabel = xIndex;
			if (Double.isNaN(yLabel)) yLabel = yIndex;
	
			IDiffractionMetadata dmeta = null;
			Dataset set = null;
			if (trace!=null) {
				set = (Dataset)trace.getData();
				final IMetadata      meta = set.getMetadata();
				if (meta instanceof IDiffractionMetadata) {
	
					dmeta = (IDiffractionMetadata)meta;
				}
			}
	
			QSpace qSpace  = null;
			Vector3dutil vectorUtil= null;
			if (dmeta != null) {
	
				try {
					DetectorProperties detector2dProperties = dmeta.getDetector2DProperties();
					DiffractionCrystalEnvironment diffractionCrystalEnvironment = dmeta.getDiffractionCrystalEnvironment();
					
					if (!(detector2dProperties == null)){
						qSpace = new QSpace(detector2dProperties,
								diffractionCrystalEnvironment);
										
						vectorUtil = new Vector3dutil(qSpace, xIndex, yIndex);
					}
				} catch (Exception e) {
					logger.error("Could not create a detector properties object from metadata", e);
				}
			}
							
			final boolean isCustom = TraceUtils.isCustomAxes(trace)  || tool.getToolPageRole() == ToolPageRole.ROLE_1D;
			if (isCustom && (column == 1 || column == 2)) {
				if (coords == null) return null;
				double[] axisPt = new double[2];
				try {
					axisPt = coords.getValueAxisLocation(new double[]{xLabel, yLabel});
					xLabel = axisPt[0];
					yLabel = axisPt[1];
				} catch (Exception e) {
					logger.debug("error: "+e);
				}
			}

			IPreferenceStore store = Activator.getPlottingPreferenceStore();

			switch(column) {
			case 0: // "Point Id"
				return ( ( (IRegion)element).getRegionType() == RegionType.POINT) ? ((IRegion)element).getName(): "";
			case 1: // "X position"
				DecimalFormat pixelPosFormat   = new DecimalFormat(store.getString(InfoPixelConstants.PIXEL_POS_FORMAT));
				return pixelPosFormat.format(xLabel);
				//return isCustom ? String.format("% 1.1f", xLabel)
				//		: String.format("% 1d", (int)Math.floor(xLabel));
			case 2: // "Y position"
				pixelPosFormat   = new DecimalFormat(store.getString(InfoPixelConstants.PIXEL_POS_FORMAT));
				return pixelPosFormat.format(yLabel);
				//return isCustom ? String.format("% 1.1f", yLabel)
				//		: String.format("% 1d", (int)Math.floor(yLabel));
			case 3: // "Data value"
				//if (set == null || vectorUtil==null || vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (set == null) return "-";
				DecimalFormat dataFormat   = new DecimalFormat(store.getString(InfoPixelConstants.DATA_FORMAT));
				return dataFormat.format(set.getDouble((int)Math.floor(yIndex), (int) Math.floor(xIndex)));
//				return String.format("% 4.4f", set.getDouble((int)Math.floor(yIndex), (int) Math.floor(xIndex)));
			case 4: // q X
				//if (vectorUtil==null || vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null ) return "-";
				DecimalFormat qFormat   = new DecimalFormat(store.getString(InfoPixelConstants.Q_FORMAT));
				return qFormat.format(vectorUtil.getQx());
//				return String.format("% 4.4f", vectorUtil.getQx());
			case 5: // q Y
				//if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null) return "-";
				qFormat   = new DecimalFormat(store.getString(InfoPixelConstants.Q_FORMAT));
				return qFormat.format(vectorUtil.getQy());
//				return String.format("% 4.4f", vectorUtil.getQy());
			case 6: // q Z
				//if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null) return "-";
				qFormat   = new DecimalFormat(store.getString(InfoPixelConstants.Q_FORMAT));
				return qFormat.format(vectorUtil.getQz());
//				return String.format("% 4.4f", vectorUtil.getQz());
			case 7: // 20
				if (vectorUtil==null || qSpace == null) return "-";
				DecimalFormat thetaFormat   = new DecimalFormat(store.getString(InfoPixelConstants.THETA_FORMAT));
				return thetaFormat.format(Math.toDegrees(vectorUtil.getQScatteringAngle(qSpace)));
//				return String.format("% 3.3f", Math.toDegrees(vectorUtil.getQScatteringAngle(qSpace)));
			case 8: // resolution
				//if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null ) return "-";
				DecimalFormat resolutionFormat   = new DecimalFormat(store.getString(InfoPixelConstants.RESOLUTION_FORMAT));
				return resolutionFormat.format((2*Math.PI)/vectorUtil.getQlength());
//				return String.format("% 4.4f", (2*Math.PI)/vectorUtil.getQlength());
			case 9: // Dataset name
				if (set == null) return "-";
				return set.getName();
	
			default:
				return "Not found";
			}
		} catch (Throwable ne) { 
			// Must not throw anything from this method - user sees millions of messages!
			logger.error("Cannot get label!", ne);
			return "";
		}
		
	}

	@Override
	public String getToolTipText(Object element) {
		return "Any selection region can be used in information box tool.";
	}

}
