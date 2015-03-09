package org.dawnsci.plotting.histogram;

import java.util.List;

import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;


public class ColourMapProvider implements IStructuredContentProvider{

	protected ComboViewer colourMapViewer;

	private IPaletteListener paletteChangedListener = new PaletteChangedListener();
	private IPaletteTrace image;

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.colourMapViewer = (ComboViewer) viewer;

		if (newInput != oldInput){
			// Remove listener(s) on old input
			if (oldInput != null){
				IPaletteTrace oldImage = (IPaletteTrace) oldInput;
				oldImage.removePaletteListener(paletteChangedListener);
			}
			// Set image and add listener(s) to new input
			if (newInput instanceof IPaletteTrace){
				IPaletteTrace image = (IPaletteTrace) newInput;
				setImage(image);
				setColourScheme(image);
				image.addPaletteListener(paletteChangedListener);
			}
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
		return ((List<String>)(pservice.getColorSchemes())).toArray();
	}

	private final class PaletteChangedListener extends IPaletteListener.Stub{
		@Override
		public void paletteChanged(PaletteEvent event) {
			IPaletteTrace trace = event.getTrace();
			setColourScheme(trace);
		}
	};

	private void setImage(IPaletteTrace image){
		this.image = image;

	}

	private void setColourScheme(IPaletteTrace trace){
		String name = trace != null ? trace.getPaletteName() : null;
		if (name != null) {
			colourMapViewer.setSelection(new StructuredSelection(name), true);
		}
	}
}
