package org.dawnsci.plotting.api.trace;

import java.util.EventObject;

import org.eclipse.swt.graphics.PaletteData;

/**
 * Event used for palette changes, including change of Palette Data.
 * @author fcp94556
 *
 */
public class PaletteEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5574618484168809185L;

	protected IPaletteTrace trace;
	protected PaletteData paletteData;
	
	public PaletteEvent(Object source, PaletteData paletteData) {
		super(source);
		this.trace       = (IPaletteTrace)source;
		this.paletteData = paletteData;
	}

	public IPaletteTrace getTrace() {
		return trace;
	}

	public void setTrace(IImageTrace trace) {
		this.trace = trace;
	}

	/**
	 * May be null!
	 * @return
	 */
	public PaletteData getPaletteData() {
		return paletteData;
	}

	public void setPaletteData(PaletteData paletteData) {
		this.paletteData = paletteData;
	}

	public IImageTrace getImageTrace() {
		return (IImageTrace)trace;
	}

}
