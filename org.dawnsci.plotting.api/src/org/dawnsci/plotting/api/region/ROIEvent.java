package org.dawnsci.plotting.api.region;

import java.util.EventObject;

import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

public class ROIEvent extends EventObject {

	public enum DRAG_TYPE{
		RESIZE,
		TRANSLATE;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 5892437380421200585L;
	private ROIBase roi;

	public ROIEvent(Object source, ROIBase region) {
		super(source);
		this.roi = region;
	}

	public ROIBase getROI() {
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
