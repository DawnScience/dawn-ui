package org.dawnsci.jzy3d.vbo;

import java.nio.IntBuffer;

import org.dawnsci.jzy3d.glsl.ColormapTexture;
import org.dawnsci.jzy3d.glsl.GLSLProgram;
import org.dawnsci.jzy3d.glsl.ShaderFilePair;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.IMultiColorable;
import org.jzy3d.io.IGLLoader;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.primitives.vbo.buffers.FloatVBO;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO;
import org.jzy3d.plot3d.rendering.view.Camera;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.glu.GLU;

public class ShaderWaterfallDrawableVBO extends DrawableVBO implements IMultiColorable {
	
	private ColorMapper mapper;
	
	protected int elementName2[] = new int[1];
	
	public ShaderWaterfallDrawableVBO(ShaderWaterfallVBOBuilder loader, ColorMapper mapper) {
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
		shaderProgram.setUniform(gl.getGL2(), "min_max", new float[] {(float) mapper.getMin(), (float) mapper.getMax(),(float) 0,},3);
		this.setGeometry(GL2.GL_LINES);
		super.draw(gl, glu, cam);
//		this.setGeometry(GL2.GL_LINES);
//		bindSecondIndices(gl);
//		super.draw(gl, glu, cam);
		shaderProgram.unbind(gl.getGL2());
		gl.getGL2().glEnable(GL.GL_BLEND);
		
		if (disposed) {
			gl.glDeleteBuffers(1, arrayName, 0);
			gl.glDeleteBuffers(1, elementName, 0);
			return;
		}
	}
	
	protected void applyVertices(GL gl) {
        gl.getGL2().glDrawElements(GL.GL_LINES, ((ShaderWaterfallVBOBuilder)loader).getOutlineIndexSize(), GL.GL_UNSIGNED_INT, 0);
//        gl.getGL2().glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
        shaderProgram.setUniform(gl.getGL2(), "min_max", new float[] {(float) mapper.getMin(), (float) mapper.getMax(),(float) 1,},3);
        gl.getGL2().glDrawElements(GL.GL_TRIANGLES, ((ShaderWaterfallVBOBuilder)loader).getFillIndexSize(), GL.GL_UNSIGNED_INT, ((ShaderWaterfallVBOBuilder)loader).getOutlineIndexSize()*4);
        doBindGL2(gl);
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
	

	protected void pointers(GL gl) {
		gl.getGL2().glVertexPointer(dimensions, GL.GL_FLOAT, 0, pointer);
//        gl.getGL2().glVertexPointer(dimensions, GL.GL_FLOAT, byteOffset, pointer);
//        gl.getGL2().glNormalPointer(GL.GL_FLOAT, byteOffset, normalOffset);
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
