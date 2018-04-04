package org.dawnsci.jzy3d;

import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.trace.IWaterfallTrace;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.plot3d.builder.concrete.WaterfallTessellator;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Shape;

import com.jogamp.opengl.util.glsl.ShaderCode;

public class WaterfallTraceImpl extends Abstract2DJZY3DTrace implements IWaterfallTrace {
	
	public WaterfallTraceImpl(IPaletteService paletteService, IImageService imageService, String pallette) {
		super(paletteService, imageService,pallette);
	}

	protected AbstractDrawable buildShape(float[] x, float[] y, float[] z, ColorMapper mapper) {
//		return  MeshTessellator.buildShape(x, y, z, mapper);
		WaterfallMeshVBOBuilder coloredMeshVBOBuilder = new WaterfallMeshVBOBuilder(x, y, z, mapper);
		ShaderWaterfallDrawableVBO shaderMeshDrawableVBO = new ShaderWaterfallDrawableVBO(coloredMeshVBOBuilder, mapper);
		coloredMeshVBOBuilder.earlyInitalise(shaderMeshDrawableVBO);
		return shaderMeshDrawableVBO;
	}

	@Override
	protected void configureShape(Shape shape) {
		shape.setColorMapper(colorMapper);
		shape.setFaceDisplayed(true);
		shape.setWireframeDisplayed(false);
	}

}
