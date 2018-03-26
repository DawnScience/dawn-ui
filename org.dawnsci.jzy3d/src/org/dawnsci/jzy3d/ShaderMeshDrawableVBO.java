package org.dawnsci.jzy3d;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.io.IGLLoader;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO;
import org.jzy3d.plot3d.rendering.view.Camera;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

public class ShaderMeshDrawableVBO extends DrawableVBO {
	
	private ColorMapper mapper;
	
	public ShaderMeshDrawableVBO(ColoredMeshVBOBuilder loader, ColorMapper mapper) {
		super(loader);
		this.mapper = mapper;
		this.setGeometry(GL2.GL_QUADS);
//		this.setColor( new Color(1f, 0f, 0f, 1f));
//		float minY = Float.MAX_VALUE;
//		float maxY = -Float.MAX_VALUE;
//		float minX = Float.MAX_VALUE;
//		float maxX = -Float.MAX_VALUE;
//		float minZ = Float.MAX_VALUE;
//		float maxZ = -Float.MAX_VALUE;
//
//		for (int i = 0; i < x.length;i++) {
//			float t = x[i];
//			if (t < minX) minX = t;
//			if (t > maxX) maxX = t;
//		}
//
//		for (int i = 0; i < y.length;i++) {
//			float t = y[i];
//			if (t < minY) minY = t;
//			if (t > maxY) maxY = t;
//		}
//
//		for (int i = 0; i < z.length;i++) {
//			float t = z[i];
//			if (t < minZ) minZ = t;
//			if (t > maxZ) maxZ = t;
//		}
//
//		this.doSetBoundingBox(new BoundingBox3d(minX, maxX, minY, maxY, minZ, maxZ));
	}

	boolean disposed = false;
	private GLSLProgram shaderProgram; 
	private ColormapTexture colormapTexure;
	
	@Override
    public void draw(GL gl, GLU glu, Camera cam) {
		
		if (!hasMountedOnce) {
			mount(gl);
			this.doSetBoundingBox(this.getBounds());
		}
		
		if (disposed) {
			
			gl.glDeleteBuffers(1, arrayName, 0);
			gl.glDeleteBuffers(1, elementName, 0);
			return;
		}
		
//		if (!hasMountedOnce) {
//			badmount(gl);
//		}
//		
		gl.getGL2().glDisable(GL.GL_BLEND);
		shaderProgram.bind(gl.getGL2());
		shaderProgram.setUniform(gl.getGL2(), "min_max", new float[] {(float) mapper.getMin(), (float) mapper.getMax(),(float) mapper.getMin(), (float) mapper.getMax()},4);
		
		super.draw(gl, glu, cam);
		shaderProgram.unbind(gl.getGL2());
		gl.getGL2().glEnable(GL.GL_BLEND);
	}
	
	@Override
	public void mount(GL gl) {
		try {
			loader.load(gl, this);
			hasMountedOnce = true;
			shaderProgram = new GLSLProgram();
			ShaderFilePair sfp = new ShaderFilePair(this.getClass(), "colour_mapped_surface.vert", "colour_mapped_surface.frag");
			shaderProgram.loadAndCompileShaders(gl.getGL2(),sfp);
			shaderProgram.link(gl.getGL2());
			colormapTexure = new ColormapTexture(mapper);
			colormapTexure.bind(gl);
		} catch (Exception e) {
			e.printStackTrace();
			//	            Logger.getLogger(DrawableVBO.class).error(e, e);
		}
	}

	@Override
	public void dispose() {
		disposed = true;
	}


}
