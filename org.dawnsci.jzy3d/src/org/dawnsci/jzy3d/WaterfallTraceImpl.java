package org.dawnsci.jzy3d;

import org.dawnsci.jzy3d.vbo.ShaderWaterfallDrawableVBO;
import org.dawnsci.jzy3d.vbo.ShaderWaterfallVBOBuilder;
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
//		ShaderWaterfallVBOBuilder builder = new ShaderWaterfallVBOBuilder(x, y, z, mapper);
//		ShaderWaterfallDrawableVBO vbo = new  ShaderWaterfallDrawableVBO(builder, mapper);
//		builder.earlyInitalise(vbo);
//		return vbo;
	}

	@Override
	protected void configureShape(Shape shape) {
		shape.setColorMapper(colorMapper);
		
	}

}
