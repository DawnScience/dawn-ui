package org.dawnsci.spectrum.ui.views;

import java.util.EventObject;

public class SpectrumFileOpenedEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SpectrumFile result;

	public SpectrumFileOpenedEvent(Object source, SpectrumFile result) {
		super(source);
		this.result = result;
	}
	
	public SpectrumFile getFile() {
		return result;
	}

}
