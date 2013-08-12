package org.dawnsci.spectrum.ui.views;

import java.util.EventListener;

import org.dawnsci.spectrum.ui.file.SpectrumFileOpenedEvent;

public interface ISpectrumFileListener extends EventListener {
	
	public void fileLoaded(SpectrumFileOpenedEvent event);

}
