package org.dawnsci.isosurface.alg;

import java.io.Serializable;

import uk.ac.diamond.scisoft.analysis.processing.OperationData;


/**
 * 
 * @author nnb55016
 * A Surface object is defined by the 3 arrays to help build the triangle mesh of JavaFX
 */

public class Surface extends OperationData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3131741986216278390L;

	
	private float[] points;
	private float[] texCoords;
	private int[] faces;
	
	public Surface(float[] points, float[] texCoords, int[] faces){
		super();
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


	public float[] getPoints() {
		return points;
	}


	public float[] getTexCoords() {
		return texCoords;
	}


	public int[] getFaces() {
		return faces;
	}

}
