package org.dawnsci.spectrum.ui.file;

import java.util.EventObject;


public class SpectrumFileOpenedEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ISpectrumFile result;

	public SpectrumFileOpenedEvent(Object source, ISpectrumFile result) {
		super(source);
		this.result = result;
	}
	
	public ISpectrumFile getFile() {
		return result;
	}

}
