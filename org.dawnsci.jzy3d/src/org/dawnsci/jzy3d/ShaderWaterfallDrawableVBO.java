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

public class ShaderWaterfallDrawableVBO extends DrawableVBO {
	
	private ColorMapper mapper;
	
	public ShaderWaterfallDrawableVBO(WaterfallMeshVBOBuilder loader, ColorMapper mapper) {
		super(loader);
		this.mapper = mapper;
		this.setGeometry(GL2.GL_QUADS);
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
		
		colormapTexure.update(gl);
		
		gl.getGL2().glDisable(GL.GL_BLEND);
		shaderProgram.bind(gl.getGL2());
		shaderProgram.setUniform(gl.getGL2(), "min_max", new float[] {(float) mapper.getMin(), (float) mapper.getMax(),(float) mapper.getMin(), (float) mapper.getMax()},4);
		
		super.draw(gl, glu, cam);
		shaderProgram.unbind(gl.getGL2());
		gl.getGL2().glEnable(GL.GL_BLEND);
		
		if (disposed) {
			gl.glDeleteBuffers(1, arrayName, 0);
			gl.glDeleteBuffers(1, elementName, 0);
			return;
		}
	}
	
	@Override
	public void mount(GL gl) {
		try {
			loader.load(gl, this);
			hasMountedOnce = true;
			shaderProgram = new GLSLProgram();
			ShaderFilePair sfp = new ShaderFilePair(this.getClass(), "colour_mapped_waterfall.vert", "colour_mapped_waterfall.frag");
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
	
	public void setMapper(ColorMapper mapper) {
		this.mapper = mapper;
		if (colormapTexure != null) colormapTexure.updateColormap(mapper);
	}


}
