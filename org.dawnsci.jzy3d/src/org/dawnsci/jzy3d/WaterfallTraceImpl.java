package org.dawnsci.jzy3d;

import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.trace.IWaterfallTrace;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.plot3d.builder.concrete.WaterfallTessellator;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Shape;

public class WaterfallTraceImpl extends Abstract2DJZY3DTrace implements IWaterfallTrace {
	
	public WaterfallTraceImpl(IPaletteService paletteService, IImageService imageService, String pallette) {
		super(paletteService, imageService,pallette);
	}

	protected AbstractDrawable buildShape(float[] x, float[] y, float[] z, ColorMapper mapper) {
		WaterfallTessellator t = new WaterfallTessellator();
		return t.build(x, y, z);
	}

	@Override
	protected void configureShape(Shape shape) {
		shape.setColorMapper(colorMapper);
		
	}

}
