package org.dawnsci.spectrum.ui.file;

import java.util.EventListener;


public interface ISpectrumFileListener extends EventListener {
	
	public void fileLoaded(SpectrumFileOpenedEvent event);

}
