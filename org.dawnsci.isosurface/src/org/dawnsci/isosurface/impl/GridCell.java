package org.dawnsci.isosurface.impl;

import java.util.Collection;
/**
 * 
 * @author nnb55016
 * GridCell class defines the basic cell used in the marching cubes algorithm
 * A cell is defined by an array of coordinates and an array of corresponding values
 * 
 * The algorithm initialises the array of actual intersection points as well as the set of triangles 
 * for every GridCell object
 */
public class GridCell {

	private Point[]  cellCoords;   // the array of coordinates of vertices of a cell (i.e. a cube)
	private double[] cellValues; // the values at the vertices of a cell
	private Point[]  vertexList = new Point[12];  // the array of actual vertices where the isosurface intersects the cell 
	private Collection<Triangle> trianglesList;
	
	public Collection<Triangle> getTrianglesList() {
		return trianglesList;
	}

	public void setTrianglesList(Collection<Triangle> trianglesList) {
		this.trianglesList = trianglesList;
	}

	public GridCell(Point[] cellCoords, double[] cellValues){
		this.cellCoords = cellCoords;
		this.cellValues = cellValues;
		}

	public Point[] getCellCoords() {
		return cellCoords;
	}

	public double[] getCellValues() {
		return cellValues;
	}

	public Point[] getVertexList() {
		return vertexList;
	}

	
}
