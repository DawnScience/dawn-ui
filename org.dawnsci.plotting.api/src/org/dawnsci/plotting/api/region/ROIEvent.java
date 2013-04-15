package org.dawnsci.plotting.api.region;

import java.util.EventObject;

import uk.ac.diamond.scisoft.analysis.roi.IROI;

public class ROIEvent extends EventObject {

	public enum DRAG_TYPE{
		RESIZE,
		TRANSLATE;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 5892437380421200585L;
	private IROI roi;

	public ROIEvent(Object source, IROI region) {
		super(source);
		this.roi = region;
	}

	public IROI getROI() {
		return roi;
	}

	public DRAG_TYPE getDragType() {
		return dragType;
	}

	public void setDragType(DRAG_TYPE dragType) {
		this.dragType = dragType;
	}

	private DRAG_TYPE dragType;
}
