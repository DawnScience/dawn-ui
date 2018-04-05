package org.dawnsci.jzy3d;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.vbo.buffers.FloatVBO;
import org.jzy3d.plot3d.primitives.vbo.builders.VBOBuilder;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

public class WaterfallMeshVBOBuilder extends VBOBuilder {

	private float[] x;
	private float[] y;
	private float[] z;
	private ColorMapper mapper;
	private FloatVBO vbo;
	
	public WaterfallMeshVBOBuilder(float[] x, float[] y, float[] z, ColorMapper mapper) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.mapper = mapper;
	}
	
	public void earlyInitalise(DrawableVBO drawable) {
		vbo = initFloatVBO(drawable, true, (y.length)*(x.length-1)*4);
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
        drawable.setHasColorBuffer(true);

        int size = 0;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;

        
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
        
        if(x.length*y.length != z.length)
			throw new IllegalArgumentException("length of y must equal x.length*z.length");
		
		//Calculate min and max to determine sensible vale
		float min = Float.POSITIVE_INFINITY;
		float max = Float.NEGATIVE_INFINITY;
		
		for (int i = 0; i < z.length; i++) {
			if (z[i] < min) min = z[i];
			if (z[i] > max) max = z[i];
		}
		
		//Apply small offset to min for drawing bottom line
		if (min == max) {
			min = min - 10E-3f;
		} else {
			min = min-((max-min)/10E3f);
		}
		
		for (int i = 0; i < y.length; i++) {

			for (int j = 0; j < x.length-1; j++) {
				Coord3d c0 = new Coord3d(x[j], y[i], min);

				Coord3d c1 = new Coord3d(x[j],y[i], z[j + (i*x.length)]);
				Coord3d c2 = new Coord3d(x[j+1],y[i], z[j + (i*x.length) +1]);

				Coord3d c3 = new Coord3d(x[j+1],y[i], min);
				
				indices.put(size++);
	            putCoord(vertices, c0);
	            

	            putColor(vertices, new Color(0, 1f, 0));

	            
//	            putColor(vertices, new Color(255, 0, 0));
	            indices.put(size++);
	            putCoord(vertices, c1);
	            putColor(vertices, new Color(0, 0, 1f));
	            indices.put(size++);
	            putCoord(vertices, c2);

	            putColor(vertices, new Color(1f, 0, 0));
	            
	            indices.put(size++);
	            putCoord(vertices, c3);
	            putColor(vertices, new Color(0, 0, 1f));

			}
		}
        
        vertices.rewind();
        indices.rewind();
        vbo.setBounds(new BoundingBox3d(minX, maxX, minY, maxY, min, max));
    }
}

