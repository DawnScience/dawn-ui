package org.dawnsci.jzy3d;

import org.eclipse.dawnsci.plotting.api.trace.ISurfaceMeshTrace;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Shape;

public class SurfaceMeshTraceImpl extends Abstract2DJZY3DTrace implements ISurfaceMeshTrace {

	public SurfaceMeshTraceImpl(String pallette) {
		super(pallette);
	}

	protected AbstractDrawable buildShape(float[] x, float[] y, float[] z, ColorMapper mapper) {
		return  MeshTessellator.buildShapeVBO(x, y, z,mapper);
//		return  MeshTessellator.buildShape(x, y, z);
	}

	@Override
	protected void configureShape(Shape shape) {
		shape.setColorMapper(colorMapper);
		shape.setFaceDisplayed(true);
		shape.setWireframeDisplayed(false);
	}
}
