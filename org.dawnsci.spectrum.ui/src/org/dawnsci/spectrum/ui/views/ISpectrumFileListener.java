package org.dawnsci.spectrum.ui.views;

import java.util.EventListener;

public interface ISpectrumFileListener extends EventListener {
	
	public void fileLoaded(SpectrumFileOpenedEvent event);

}
