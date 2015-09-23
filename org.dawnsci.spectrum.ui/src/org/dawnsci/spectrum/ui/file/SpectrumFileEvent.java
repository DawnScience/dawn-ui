package org.dawnsci.spectrum.ui.file;

import java.util.EventObject;

import org.dawnsci.spectrum.ui.file.ISpectrumFile;

public class SpectrumFileEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ISpectrumFile result;

	public SpectrumFileEvent(Object source, ISpectrumFile result) {
		super(source);
		this.result = result;
	}

	public ISpectrumFile getFile() {
		return result;
	}
}
