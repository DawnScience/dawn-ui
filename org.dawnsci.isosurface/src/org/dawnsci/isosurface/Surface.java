package org.dawnsci.isosurface;

import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;


/**
 * 
 * @author nnb55016
 * A Surface object is defined by the 3 arrays to help build the triangle mesh of JavaFX
 */

public class Surface  {

	private float[] points;
	private float[] texCoords;
	private int[] faces;
	
	public Surface(float[] points, float[] texCoords, int[] faces){
		this.points = points;
		this.texCoords = texCoords;
		this.faces = faces;
		
	}

	
	public void setPoints(float[] points) {
		this.points = points;
	}

	public void setTexCoords(float[] texCoords) {
		this.texCoords = texCoords;
	}

	public void setFaces(int[] faces) {
		this.faces = faces;
	}


	public float[] GetPoints() {
		return points;
	}


	public float[] GetTexCoords() {
		return texCoords;
	}


	public int[] GetFaces() {
		return faces;
	}


	public Mesh createTrangleMesh() {
		final TriangleMesh mesh = new TriangleMesh();
		marry(mesh);
		return mesh;
	}

	public void marry(TriangleMesh mesh) {
		mesh.getPoints().setAll(points);
		mesh.getTexCoords().setAll(texCoords);
		mesh.getFaces().setAll(faces);
	}
}
