package org.dawnsci.processing.ui;

import java.util.EventObject;

import uk.ac.diamond.scisoft.analysis.dataset.Slice;

public class SliceChangeEvent extends EventObject {

	private Slice[] slices;
	
	public SliceChangeEvent(Object source, Slice[] slice) {
		super(source);
	}
	
	public Slice[] getSlices() {
		return slices;
	}


}
