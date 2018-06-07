package org.dawnsci.datavis.model;

import java.util.EventObject;

import org.eclipse.january.dataset.SliceND;


public class SliceChangeEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	private NDimensions nDimension;
	private boolean optionsChanged;
	private Object parent;

	public SliceChangeEvent(NDimensions nDimensions, boolean optionsChanged, Object parent) {
		super(nDimensions);
		this.nDimension = nDimensions;
		this.optionsChanged = optionsChanged;
		this.parent = parent;
	}

	public Object getParent() {
		return parent;
	}

	public boolean isOptionsChanged() {
		return optionsChanged;
	}

	public String[] getAxesNames() {
		return nDimension.buildAxesNames();
	}

	public Object[] getOptions() {
		return nDimension.getOptions();
	}

	public SliceND getSlice() {
		return  nDimension.buildSliceND();
	}
	
	public NDimensions getSource() {
		return nDimension;
	}

}
