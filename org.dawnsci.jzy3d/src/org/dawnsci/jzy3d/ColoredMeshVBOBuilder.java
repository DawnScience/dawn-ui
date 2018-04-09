package org.dawnsci.jzy3d;

import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jzy3d.colors.ColorMapper;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.vbo.buffers.FloatVBO;
import org.jzy3d.plot3d.primitives.vbo.builders.VBOBuilder;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

public class ColoredMeshVBOBuilder extends VBOBuilder {

	private float[] x;
	private float[] y;
	private float[] z;
	private ColorMapper mapper;
	private FloatVBO vbo;
	
	public ColoredMeshVBOBuilder(float[] x, float[] y, float[] z, ColorMapper mapper) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.mapper = mapper;
	}
	
	public void earlyInitalise(DrawableVBO drawable) {
//		vbo = initFloatVBO(drawable, true, (y.length-1)*(x.length-1)*4);
		int size = (y.length-1)*(x.length-1);
		vbo = new FloatVBO(size*36, size*4);
		fillFromArray(drawable, x,y,z,mapper, vbo);
		drawable.doSetBoundingBox(vbo.getBounds());
	}

	@Override
	public void load(GL gl, DrawableVBO drawable) throws Exception {
        drawable.setData(gl, vbo);
        drawable.setGeometry(GL2.GL_QUADS);
	}
	
	private void fillFromArray(DrawableVBO drawable, float[] x, float[] y, float[] z, ColorMapper colors, FloatVBO vbo) {
		FloatBuffer vertices= vbo.getVertices();
		IntBuffer indices = vbo.getIndices();
//        drawable.setHasColorBuffer(true);

        int size = 0;
        Coord3d c = new Coord3d();
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;
        
        for (int i = 0; i < x.length;i++) {
        	float t = x[i];
        	if (t < minX) minX = t;
        	if (t > maxX) maxX = t;
        }
        
        for (int i = 0; i < y.length;i++) {
        	float t = y[i];
        	if (t < minY) minY = t;
        	if (t > maxY) maxY = t;
        }
        
        for(int yi=0; yi<y.length-1; yi++){
			for(int xi=0; xi<x.length-1; xi++){
				
				int pos = xi+yi*x.length;
				int posNext = xi+(yi+1)*x.length;
				
				indices.put(size++);
				c.x = x[xi];
				c.y = y[yi+1];
				c.z = z[posNext];
				
				if (c.z < minZ) minZ = c.z;
	        	if (c.z > maxZ) maxZ = c.z;
				
	            putCoord(vertices, c);
//	            putColor(vertices, colors.getColor(c));
				
				indices.put(size++);
				c.x = x[xi];
				c.y = y[yi];
				c.z = z[pos];
				
				if (c.z < minZ) minZ = c.z;
	        	if (c.z > maxZ) maxZ = c.z;
				
	            putCoord(vertices, c);
//	            putColor(vertices, colors.getColor(c));
	            
	            indices.put(size++);
				c.x = x[xi+1];
				c.y = y[yi];
				c.z = z[pos+1];
				
				if (c.z < minZ) minZ = c.z;
	        	if (c.z > maxZ) maxZ = c.z;
				
	            putCoord(vertices, c);
//	            putColor(vertices, colors.getColor(c));
	            
	            
	            indices.put(size++);
				c.x = x[xi+1];
				c.y = y[yi+1];
				c.z = z[posNext+1];
				
				if (c.z < minZ) minZ = c.z;
	        	if (c.z > maxZ) maxZ = c.z;
				
	            putCoord(vertices, c);
//	            putColor(vertices, colors.getColor(c));
                
			}
		}	
        
        vertices.rewind();
        indices.rewind();
        vbo.setBounds(new BoundingBox3d(minX, maxX, minY, maxY, minZ, maxZ));
    }
}

