package org.dawnsci.jzy3d.volume;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

public class CubeVBO extends DrawableVBO {

	private static final Logger logger = LoggerFactory.getLogger(CubeVBO.class);

	boolean disposed = false;

	public CubeVBO(CubeVBOBuilder builder) {
		super(builder);
		this.setGeometry(GL2.GL_QUADS);
		this.setColor( new Color(1f, 0f, 1f, 1f));
		this.doSetBoundingBox(new BoundingBox3d(0, 1, 0, 1, 0, 1));
	}

	@Override
	public void draw(GL gl, GLU glu, Camera cam) {

		doTransform(gl, glu, cam);

		if (!hasMountedOnce) {
			mount(gl);
		}

		super.draw(gl, glu, cam);

		if (disposed) {
			gl.glDeleteBuffers(1, arrayName, 0);
			gl.glDeleteBuffers(1, elementName, 0);
			return;
		}

	}

	@Override
	protected void applyQuality(GL gl) {
		//Disable all smoothing, can cause gaps between triangles
		gl.glDisable(GL2.GL_POLYGON_SMOOTH);
		gl.glDisable(GL.GL_LINE_SMOOTH);
		gl.glDisable(GL2.GL_POINT_SMOOTH);
	}

	@Override
	public void mount(GL gl) {
		try {
			loader.load(gl, this);
			hasMountedOnce = true;
		} catch (Exception e) {
			logger.error("Could not mount VBO", e);
		}
	}

	@Override
	public void dispose() {
		disposed = true;
	}
}
