package org.dawnsci.jzy3d;

import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.IGLBindedResource;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Quad;
import org.jzy3d.plot3d.primitives.vbo.buffers.FloatVBO;
import org.jzy3d.plot3d.primitives.vbo.builders.VBOBuilder;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.rendering.view.ViewportConfiguration;
import org.jzy3d.plot3d.transform.Transform;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.glu.GLU;

public class Texture3D extends AbstractDrawable implements IGLBindedResource{

	/** The GL texture ID. */
    private int texID;
    private Buffer buffer;
    private int[] shape;
    private boolean mounted = false;
    private Quad quad;
    private DrawableVBO shapeVBO;
    private GLSLProgram shaderProgram;
    private float min;
    private float max;
    private ColormapTexture colormapTexure;
    
    private int program;
	private boolean disposed;
	private ColorMapper mapper;
    
    public Texture3D(Buffer buffer, int[] shape, float min, float max, ColorMapper mapper) {
    	this.buffer = buffer;
    	buffer.rewind();
    	this.shape = shape;
    	bbox = new BoundingBox3d(0,1,0,1,0,1);
    	quad = new Quad();
    	quad.add(new Point(new Coord3d(0, 0, 0)));
    	quad.add(new Point(new Coord3d(0, 1, 1)));
    	quad.add(new Point(new Coord3d(0, 1, 0)));
    	quad.add(new Point(new Coord3d(1, 1, 0)));
    	shapeVBO = buildShape();
    	this.min = min;
    	this.max = max;
    	this.mapper = mapper;
    }
	
	@Override
	public void mount(GL gl) {
		if (!mounted) {
			shapeVBO.mount(gl);
			shaderProgram = new GLSLProgram();
			ShaderFilePair sfp = new ShaderFilePair(this.getClass(), "volume.vert", "volume.frag");
			shaderProgram.loadAndCompileShaders(gl.getGL2(),sfp);
			shaderProgram.link(gl.getGL2());
//			buildProgram(gl);
			bind(gl);
			colormapTexure = new ColormapTexture(mapper,"transfer",shaderProgram.getProgramId());
			colormapTexure.bind(gl);
			mounted = true;
		}
		
	}
	
	public void setMin(Number min) {
		this.min = min.floatValue();
	}
	
	public void setMax(Number max) {
		this.max = max.floatValue();
	}

	@Override
	public boolean hasMountedOnce() {
		return mounted;
	}
	
	public void bind(final GL gl) throws GLException {
		gl.glEnable(GL2.GL_TEXTURE_3D);
        validateTexID(gl, true);
        gl.glBindTexture(GL2.GL_TEXTURE_3D, texID);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP);
        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MAG_FILTER,
                GL.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MIN_FILTER,
                GL.GL_LINEAR);
        
        setTextureData(gl,buffer,shape);
    }
	
	public void setTextureData(final GL gl, Buffer buffer, int[] shape) {
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		gl.getGL2().glTexImage3D(GL2.GL_TEXTURE_3D, 0, GL.GL_R32F, shape[2], shape[1], shape[0], 0, GL2.GL_RED, GL.GL_FLOAT, buffer);
//		gl.getGL2().glTexSubImage3D(GL2.GL_TEXTURE_3D,0,0, 0,0, shape[0], shape[1], shape[2], GL2ES2.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
//		gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
//		gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
//		gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_R, GL.GL_CLAMP_TO_EDGE);
	}
	
	private boolean validateTexID(final GL gl, final boolean throwException) {
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glEnable(GL2.GL_TEXTURE_3D);
		int id = gl.getGL2().glGetUniformLocation(shaderProgram.getProgramId(), "volumeTexture");
		
		if ( id >= 0) {
			texID = id;
		}
		
		
//        if( 0 == texID ) {
//            if( null != gl ) {
//                final int[] tmp = new int[1];
//                gl.glGenTextures(1, tmp, 0);
//                texID = tmp[0];
//                if ( 0 == texID && throwException ) {
//                    throw new GLException("Create texture ID invalid: texID "+texID+", glerr 0x"+Integer.toHexString(gl.glGetError()));
//                }
//            } else if ( throwException ) {
//                throw new GLException("No GL context given, can't create texture ID");
//            }
//        }
        return 0 != texID;
    }
	
	public void setMapper(ColorMapper mapper) {
		this.mapper = mapper;
		if (colormapTexure != null) colormapTexure.updateColormap(mapper);
	}

	@Override
	public void draw(GL gl, GLU glu, Camera cam) {
		
		if (!mounted) {
			mount(gl);
		}
		
		doTransform(gl, glu, cam);
    	
    	float mvmatrix[] = new float[16];
    	float projmatrix[] = new float[16];
    	
    	Coord3d eye = cam.getEye();
    	eye = eye.sub(cam.getTarget());
    	eye = eye.normalizeTo(1);
    	
//    	String glGetString = gl.glGetString(GL.GL_VERSION);
//    	String glGetString2 = gl.glGetString(GL2.GL_SHADING_LANGUAGE_VERSION);
    	gl.getGL2().glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
    	gl.getGL2().glGetFloatv(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
    	
    	shaderProgram.bind(gl.getGL2());
//    	shaderProgram.setUniform(gl.getGL2(), "modelViewMatrix", mvmatrix,4);
//    	shaderProgram.setUniform(gl.getGL2(), "projectionMatrix", projmatrix,4);
    	shaderProgram.setUniform(gl.getGL2(), "eye", eye.toArray(),3);
    	shaderProgram.setUniform(gl.getGL2(), "minMax", new float[] {min,max},2);
    	int idt = gl.getGL2().glGetUniformLocation(shaderProgram.getProgramId(), "volumeTexture");
    	int idc = gl.getGL2().glGetUniformLocation(shaderProgram.getProgramId(), "transfer");
    	gl.getGL2().glUniform1i(idt, 0);
    	gl.getGL2().glUniform1i(idc, 1);
    	
    	
       gl.glEnable(GL2.GL_BLEND);
       gl.glEnable(GL2.GL_CULL_FACE);
       gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
       gl.getGL2().glPolygonMode(GL.GL_FRONT, GL2GL3.GL_FILL);
       gl.glCullFace(gl.GL_BACK);
		
       shapeVBO.draw(gl, glu, cam);
       shaderProgram.unbind(gl.getGL2());
       
       if (disposed) {
    	   gl.glDeleteTextures(1, new int[] {texID}, 0);
       }
         
	}

	@Override
	public void applyGeometryTransform(Transform transform) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dispose() {
		disposed = true;
		shapeVBO.dispose();
	}

	@Override
	public void updateBounds() {
		bbox = new BoundingBox3d(0,1,0,1,0,1);
		
	}
	
	
	public static DrawableVBO buildShape() {
		
		DrawableVBO v =  new DrawableVBO(new CubeVBO(0,1,0,1,0,1)) {
			
			boolean disposed = false;
			
			@Override
		    public void draw(GL gl, GLU glu, Camera cam) {
				
				doTransform(gl, glu, cam);
				
				if (!hasMountedOnce) {
					mount(gl);
				}
				
				if (disposed) {
					
					gl.glDeleteBuffers(1, arrayName, 0);
					gl.glDeleteBuffers(1, elementName, 0);
					return;
				}
				
				
				super.draw(gl, glu, cam);
				
			}
			

			
			
			 @Override
			    public void mount(GL gl) {
			        try {
			            loader.load(gl, this);
			            hasMountedOnce = true;
			        } catch (Exception e) {
			            e.printStackTrace();
//			            Logger.getLogger(DrawableVBO.class).error(e, e);
			        }
			    }
			 
			
			@Override
			public void dispose() {
				disposed = true;
			}
			
		};
		v.setGeometry(GL2.GL_QUADS);
		v.setColor( new Color(1f, 0f, 1f, 1f));

		
		v.doSetBoundingBox(new BoundingBox3d(0, 1, 0, 1, 0, 1));
		
		return v;
		
	}

public static class CubeVBO extends VBOBuilder {
	
	
	private float xMin;
	private float xMax;
	private float yMin;
	private float yMax;
	private float zMin;
	private float zMax;

	public CubeVBO(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax) {
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.zMin = zMin;
		this.zMax = zMax;
		
	}
	

	@Override
	public void load(GL gl, DrawableVBO drawable) throws Exception {
		FloatVBO vbo = initFloatVBO(drawable, true, 24);
		fillFromArray(drawable,  xMin,  xMax,  yMin,  yMax,  zMin,  zMax, vbo);
        drawable.setData(gl, vbo);
	}
	
	private void fillFromArray(DrawableVBO drawable, float xMin, float xMax, float yMin, float yMax, float zMin, float zMax, FloatVBO vbo) {
		FloatBuffer vertices= vbo.getVertices();
		IntBuffer indices = vbo.getIndices();
		drawable.setHasColorBuffer(true);

		int size = 0;
		Coord3d c = new Coord3d();
		//zMin
		indices.put(size++);
		c.x = xMin;
		c.y = yMin;
		c.z = zMin;

		putCoord(vertices, c);
		putColor(vertices, new Color(0,0,0));
		
		indices.put(size++);
		c.x = xMin;
		c.y = yMax;
		c.z = zMin;

		putCoord(vertices, c);
		putColor(vertices, new Color(0,255,0));
		
		indices.put(size++);
		c.x = xMax;
		c.y = yMax;
		c.z = zMin;

		putCoord(vertices, c);
		putColor(vertices, new Color(255,255,0));
		
		indices.put(size++);
		c.x = xMax;
		c.y = yMin;
		c.z = zMin;

		putCoord(vertices, c);
		putColor(vertices, new Color(255,0,0));
		
		
		
		//xMin
		indices.put(size++);
		c.x = xMin;
		c.y = yMin;
		c.z = zMin;

		putCoord(vertices, c);
		putColor(vertices, new Color(0,0,0));
		
		indices.put(size++);
		c.x = xMin;
		c.y = yMin;
		c.z = zMax;

		putCoord(vertices, c);
		putColor(vertices, new Color(0,0,255));
		
		indices.put(size++);
		c.x = xMin;
		c.y = yMax;
		c.z = zMax;

		putCoord(vertices, c);
		putColor(vertices, new Color(0,255,255));
		
		indices.put(size++);
		c.x = xMin;
		c.y = yMax;
		c.z = zMin;

		putCoord(vertices, c);
		putColor(vertices, new Color(0,255,0));
		
		
		
		

		//yMin
		indices.put(size++);
		c.x = xMin;
		c.y = yMin;
		c.z = zMin;

		putCoord(vertices, c);
		putColor(vertices, new Color(0,0,0));
		
		indices.put(size++);
		c.x = xMax;
		c.y = yMin;
		c.z = zMin;

		putCoord(vertices, c);
		putColor(vertices, new Color(255,0,0));
		
		indices.put(size++);
		c.x = xMax;
		c.y = yMin;
		c.z = zMax;

		putCoord(vertices, c);
		putColor(vertices, new Color(255,0,255));
		
		indices.put(size++);
		c.x = xMin;
		c.y = yMin;
		c.z = zMax;

		putCoord(vertices, c);
		putColor(vertices, new Color(0,0,255));
		
		
		
		
		
		//zMax
				indices.put(size++);
				c.x = xMax;
				c.y = yMax;
				c.z = zMax;

				putCoord(vertices, c);
				putColor(vertices, new Color(255,255,255));
				
				indices.put(size++);
				c.x = xMin;
				c.y = yMax;
				c.z = zMax;

				putCoord(vertices, c);
				putColor(vertices, new Color(0,255,255));
				
				indices.put(size++);
				c.x = xMin;
				c.y = yMin;
				c.z = zMax;

				putCoord(vertices, c);
				putColor(vertices, new Color(0,0,255));
				
				indices.put(size++);
				c.x = xMax;
				c.y = yMin;
				c.z = zMax;

				putCoord(vertices, c);
				putColor(vertices, new Color(255,0,255));
				
				
				
				
				//xMax
				indices.put(size++);
				c.x = xMax;
				c.y = yMax;
				c.z = zMax;

				putCoord(vertices, c);
				putColor(vertices, new Color(255,255,255));
				
				indices.put(size++);
				c.x = xMax;
				c.y = yMin;
				c.z = zMax;

				putCoord(vertices, c);
				putColor(vertices, new Color(255,0,255));
				
				indices.put(size++);
				c.x = xMax;
				c.y = yMin;
				c.z = zMin;

				putCoord(vertices, c);
				putColor(vertices, new Color(255,0,0));
				
				indices.put(size++);
				c.x = xMax;
				c.y = yMax;
				c.z = zMin;

				putCoord(vertices, c);
				putColor(vertices, new Color(255,255,0));

				//yMax
				
				indices.put(size++);
				c.x = xMax;
				c.y = yMax;
				c.z = zMin;

				putCoord(vertices, c);
				putColor(vertices, new Color(255,255,0));
				
				indices.put(size++);
				c.x = xMin;
				c.y = yMax;
				c.z = zMin;

				putCoord(vertices, c);
				putColor(vertices, new Color(0,255,0));
				
				indices.put(size++);
				c.x = xMin;
				c.y = yMax;
				c.z = zMax;

				putCoord(vertices, c);
				putColor(vertices, new Color(0,255,255));
				indices.put(size++);
				c.x = xMax;
				c.y = yMax;
				c.z = zMax;

				putCoord(vertices, c);
				putColor(vertices, new Color(255,255,255));
				
				
				
				
				
				
		
		vertices.rewind();
		indices.rewind();
		vbo.setBounds(new BoundingBox3d(xMin, yMin, xMax, yMax, zMin, zMax));
    }
}
}
