package org.dawnsci.plotting.roi;


import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.PerimeterBoxROI;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.diamond.scisoft.analysis.roi.RingROI;
import uk.ac.diamond.scisoft.analysis.roi.XAxisBoxROI;
import uk.ac.diamond.scisoft.analysis.roi.YAxisBoxROI;
//import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

public enum ROIType {

	LINEAR(LinearROI.class),
	//POLYGONAL(PolygonalROI.class),   // TODO
	POINT(PointROI.class),
	RECTANGULAR(RectangularROI.class),
	PERIMETERBOX(PerimeterBoxROI.class),
	XAXIS(XAxisBoxROI.class),
	YAXIS(YAxisBoxROI.class),
	SECTOR(SectorROI.class),
	RING(RingROI.class),
	ELLIPICAL(EllipticalROI.class);
	
	private Class<? extends IROI> clazz;

	ROIType(Class<? extends IROI> clazz) {
		this.clazz = clazz;
	}
	
	public int getIndex() {
		final ROIType[] ops = ROIType.values();
		for (int i = 0; i < ops.length; i++) if (ops[i]==this) return i;
		return -1;
	}

	public static String[] getTypes() {
		final ROIType[] ops = ROIType.values();
		final String[] names = new String[ops.length];
		for (int i = 0; i < ops.length; i++) {
			names[i] = ops[i].getName();
		}
		return names;
	}

	public String getName() {
		return clazz.getSimpleName();
	}

	public static ROIType getType(int index) {
		final ROIType[] ops = ROIType.values();
		return ops[index];
	}

	public IROI getRoi() throws InstantiationException, IllegalAccessException {
		return clazz.newInstance();
	}

	public static int getIndex(Class<? extends IROI> class1) {
		final ROIType[] ops = ROIType.values();
		for (ROIType roiType : ops) {
			if (roiType.clazz == class1) return roiType.getIndex();
		}
		return -1;
	}

	public static IROI createNew(int selectionIndex) throws InstantiationException, IllegalAccessException {
		final ROIType roi = getType(selectionIndex);
		return roi.clazz.newInstance();
	}
}
