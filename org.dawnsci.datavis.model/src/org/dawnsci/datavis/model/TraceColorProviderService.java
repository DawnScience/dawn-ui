package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

public class TraceColorProviderService implements ITraceColorProviderService {

	private IPaletteService paletteService;
	
	public void setPaletteService(IPaletteService service) {
		paletteService = service;
	}
	
	private ITraceColourProvider[] providers;
	
	/* (non-Javadoc)
	 * @see org.dawnsci.datavis.model.ITraceColorProviderService#getColorProviders()
	 */
	@Override
	public ITraceColourProvider[] getColorProviders(){
		if (providers == null) {
			buildProviders();
		}
		return providers;
	}
	
	private void buildProviders(){
		
		List<TraceColorProvider> plist = new ArrayList<>();
		plist.add(new TraceColorProvider("Black", new RGB[]{new RGB(0, 0, 0)}));
		
		PaletteData paletteData = paletteService.getDirectPaletteData("Jet (Blue-Cyan-Green-Yellow-Red)");
		RGB[] rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Jet", rgbs));
		
		paletteData = paletteService.getDirectPaletteData("Viridis (blue-green-yellow)");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Viridis", rgbs));
		
		paletteData = paletteService.getDirectPaletteData("Spectral");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Spectral", rgbs));
		
		paletteData = paletteService.getDirectPaletteData("Accent");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Accent", rgbs));
		
		paletteData = paletteService.getDirectPaletteData("Winter (Blue-Green)");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Winter", rgbs));
		
		paletteData = paletteService.getDirectPaletteData("Prism (Sine)");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Prism", rgbs));
		
		paletteData = paletteService.getDirectPaletteData("Nipy Spectral");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Nipy Spectral", rgbs));
		
		providers = plist.toArray(new TraceColorProvider[plist.size()]);
	}
}
