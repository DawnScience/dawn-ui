package org.dawnsci.jzy3d;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.vbo.buffers.FloatVBO;
import org.jzy3d.plot3d.primitives.vbo.builders.VBOBuilder;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO;

import com.jogamp.opengl.GL;

public class CubeVBOBuilder extends VBOBuilder {

	private float xMin;
	private float xMax;
	private float yMin;
	private float yMax;
	private float zMin;
	private float zMax;

	public CubeVBOBuilder(BoundingBox3d bbox) {
		this.xMin = bbox.getXmin();
		this.xMax = bbox.getXmax();
		this.yMin = bbox.getYmin();
		this.yMax = bbox.getYmax();
		this.zMin = bbox.getZmin();
		this.zMax = bbox.getZmax();

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

