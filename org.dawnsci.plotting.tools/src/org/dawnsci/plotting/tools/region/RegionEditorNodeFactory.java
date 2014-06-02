package org.dawnsci.plotting.tools.region;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.RingROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

/**
 * Class used to create nodes with for the Region Tree Editor
 * @author wqk87977
 *
 */
public class RegionEditorNodeFactory {

	public static final String ANGLE = "Angle";
	public static final String INTENSITY = "Intensity";
	public static final String SUM = "Sum";

	/**
	 * 
	 * @param region
	 * @return a Map of key-value pairs defining a ROI
	 */
	public static Map<String, Double> getRegionNodeInfos(IROI roi) {
		if (roi == null)
			return null;
		Map<String, Double> roiInfos = new LinkedHashMap<String, Double>();
		if (roi instanceof RectangularROI) {
			roiInfos.put("X Start", ((RectangularROI)roi).getPointX());
			roiInfos.put("Y Start", ((RectangularROI)roi).getPointY());
			roiInfos.put("Width", ((RectangularROI)roi).getLengths()[0]);
			roiInfos.put("Height", ((RectangularROI)roi).getLengths()[1]);
			roiInfos.put("Angle", ((RectangularROI)roi).getAngleDegrees());
			roiInfos.put("Max Intensity", Double.NaN);
			roiInfos.put("Sum", Double.NaN);
		} else if (roi instanceof LinearROI) {
			roiInfos.put("X Start", ((LinearROI)roi).getPointX());
			roiInfos.put("Y Start", ((LinearROI)roi).getPointY());
			roiInfos.put("X End", ((LinearROI)roi).getEndPoint()[0]);
			roiInfos.put("Y End", ((LinearROI)roi).getEndPoint()[1]);
			roiInfos.put("Angle", ((LinearROI)roi).getAngleDegrees());
			roiInfos.put("Intensity", Double.NaN);
		} else if (roi instanceof CircularROI) {
			roiInfos.put("X Centre", ((CircularROI)roi).getPointX());
			roiInfos.put("Y Centre", ((CircularROI)roi).getPointY());
			roiInfos.put("Radius", ((CircularROI)roi).getRadius());
		} else if (roi instanceof SectorROI) {
			roiInfos.put("X Centre", ((SectorROI)roi).getPointX());
			roiInfos.put("Y Centre", ((SectorROI)roi).getPointY());
			roiInfos.put("Inner Radius", ((SectorROI)roi).getRadii()[0]);
			roiInfos.put("Outer Radius", ((SectorROI)roi).getRadii()[1]);
			roiInfos.put("Angle 1", ((SectorROI)roi).getAnglesDegrees()[0]);
			roiInfos.put("Angle 2", ((SectorROI)roi).getAnglesDegrees()[1]);
		} else if (roi instanceof RingROI) {
			roiInfos.put("X Centre", ((RingROI)roi).getPointX());
			roiInfos.put("Y Centre", ((RingROI)roi).getPointY());
			roiInfos.put("Inner Radius", ((RingROI)roi).getRadii()[0]);
			roiInfos.put("Outer Radius", ((RingROI)roi).getRadii()[1]);
		} if (roi instanceof PointROI) {
			roiInfos.put("X Start", ((PointROI)roi).getPointX());
			roiInfos.put("Y Start", ((PointROI)roi).getPointY());
			roiInfos.put("Intensity", Double.NaN);
		}
		return roiInfos;
	}
}
