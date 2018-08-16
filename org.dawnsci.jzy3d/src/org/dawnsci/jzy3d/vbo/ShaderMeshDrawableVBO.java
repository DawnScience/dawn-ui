package org.dawnsci.jzy3d.vbo;

import org.dawnsci.jzy3d.glsl.ColormapTexture;
import org.dawnsci.jzy3d.glsl.GLSLProgram;
import org.dawnsci.jzy3d.glsl.ShaderFilePair;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.IMultiColorable;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO;
import org.jzy3d.plot3d.rendering.compat.GLES2CompatUtils;
import org.jzy3d.plot3d.rendering.view.Camera;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.glu.GLU;

public class ShaderMeshDrawableVBO extends DrawableVBO implements IMultiColorable{
	
	private ColorMapper mapper;
	
	public ShaderMeshDrawableVBO(ShaderMeshVBOBuilder loader, ColorMapper mapper) {
		super(loader);
		this.mapper = mapper;
		this.setGeometry(GL2.GL_TRIANGLES);
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
		int idc = gl.getGL2().glGetUniformLocation(shaderProgram.getProgramId(), "transfer");
    	gl.getGL2().glUniform1i(idc, 1);
		super.draw(gl, glu, cam);
		shaderProgram.unbind(gl.getGL2());
		gl.getGL2().glEnable(GL.GL_BLEND);
		gl.glDisable(GL2.GL_TEXTURE_1D);
		
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
			ShaderFilePair sfp = new ShaderFilePair(this.getClass(), "colour_mapped_surface.vert", "colour_mapped_surface.frag");
			shaderProgram.loadAndCompileShaders(gl.getGL2(),sfp);
			shaderProgram.link(gl.getGL2());
			colormapTexure = new ColormapTexture(mapper,"transfer",shaderProgram.getProgramId());
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
	
	protected void pointers(GL gl) {
		gl.getGL2().glVertexPointer(dimensions, GL.GL_FLOAT, 0, pointer);
//        gl.getGL2().glVertexPointer(dimensions, GL.GL_FLOAT, byteOffset, pointer);
//        gl.getGL2().glNormalPointer(GL.GL_FLOAT, byteOffset, normalOffset);
    }
	
	protected void configure(GL gl) {
        // gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
        // gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
        // gl.glColor4f(1f,0f,1f,0.6f);
        // gl.glLineWidth(0.00001f);
        if (gl.isGL2()) {
            gl.getGL2().glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
        } else {
            GLES2CompatUtils.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
        }
        call(gl, color);
    }

	@Override
	public ColorMapper getColorMapper() {
		return mapper;
	}

	@Override
	public void setColorMapper(ColorMapper mapper) {
		this.mapper = mapper;
		if (colormapTexure != null) colormapTexure.updateColormap(mapper);
		
	}



}
