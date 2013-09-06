package org.dawnsci.slicing.api.system;

import java.util.EventObject;

public class DimensionalEvent extends EventObject {

	private DimsDataList dimsList;

	public DimensionalEvent(Object source, DimsDataList dl) {
		super(source);
		this.dimsList = dl;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4461117258433723938L;

	public DimsDataList getDimsList() {
		return dimsList;
	}

	public void setDimsList(DimsDataList dimsList) {
		this.dimsList = dimsList;
	}

}
