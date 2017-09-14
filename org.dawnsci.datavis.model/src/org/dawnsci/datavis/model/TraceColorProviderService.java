package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

public class TraceColorProviderService {

	private static TraceColorProviderService instance;
	private ITraceColourProvider[] providers;
	
	private TraceColorProviderService(){
		buildProviders();
	}
	
	public static TraceColorProviderService getInstance(){
		if (instance == null) {
			instance = new TraceColorProviderService();
		}
		
		return instance;
	}
	
	public ITraceColourProvider[] getColorProviders(){
		return providers;
	}
	
	private void buildProviders(){
		
		List<TraceColorProvider> plist = new ArrayList<>();
		plist.add(new TraceColorProvider("Black", new RGB[]{new RGB(0, 0, 0)}));
		
		PaletteData paletteData = ServiceManager.getPaletteService().getDirectPaletteData("Jet (Blue-Cyan-Green-Yellow-Red)");
		RGB[] rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Jet", rgbs));
		
		paletteData = ServiceManager.getPaletteService().getDirectPaletteData("Viridis (blue-green-yellow)");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Viridis", rgbs));
		
		paletteData = ServiceManager.getPaletteService().getDirectPaletteData("Spectral");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Spectral", rgbs));
		
		paletteData = ServiceManager.getPaletteService().getDirectPaletteData("Accent");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Accent", rgbs));
		
		paletteData = ServiceManager.getPaletteService().getDirectPaletteData("Winter (Blue-Green)");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Winter", rgbs));
		
		paletteData = ServiceManager.getPaletteService().getDirectPaletteData("Prism (Sine)");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Prism", rgbs));
		
		paletteData = ServiceManager.getPaletteService().getDirectPaletteData("Nipy Spectral");
		rgbs = paletteData.getRGBs();
		
		plist.add(new TraceColorProvider("Nipy Spectral", rgbs));
		
		providers = plist.toArray(new TraceColorProvider[plist.size()]);
	}
}
