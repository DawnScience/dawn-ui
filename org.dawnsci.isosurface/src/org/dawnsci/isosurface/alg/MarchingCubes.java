/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.isosurface.alg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperationBase;
/**
 * 
 * @author nnb55016
 * The MarchingCubes class holds the algorithm with the same name which provides the triangular
 * mesh for a particular three dimensional dataset
 */
public class MarchingCubes extends AbstractOperationBase<MarchingCubesModel, Surface> {

	public MarchingCubes() {
		setModel(new MarchingCubesModel()); // We must always have a model for this maths.
	}
	
	@Override
	public String getId() {
		return "org.dawnsci.isosurface.marchingCubes";
	}
	
	@Override
	public Surface execute(IDataset slice, IMonitor moni1tor) throws OperationException {
				
		final Object[]           data      = parseVertices();
		final Set<Triangle>      triangles = (HashSet<Triangle>) data[0];
		final Map<Point,Integer> v         = (Map<Point, Integer>) data[1];

		Point[] vertices = v.keySet().toArray(new Point[v.size()]);
		
		float[] points = getCoordinates(vertices);

		float[] texCoords = {0,0,0,1,1,1}; //{ 0, 0, (float) 0.5, (float) 0.5, 1, 1 };

		int[] faces = new int[6 * triangles.size()];

		int k = 0;

		for (Triangle t: triangles) {
			
			faces[k] = v.get(t.getC());
			faces[k + 1] = 0;
			faces[k + 2] = v.get(t.getB());
			faces[k + 3] = 1;
			faces[k + 4] = v.get(t.getA());
			faces[k + 5] = 2;
			k += 6;
		}

		if (points==null || points.length<1) throw new OperationException(this, "No isosurface found!");

		return new Surface(points, texCoords, faces);
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.THREE;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.NONE;
	}
	
	
	@SuppressWarnings("unchecked")
	
	private Object[] parseVertices() throws OperationException 
	{	
		final ILazyDataset lazyData = model.getLazyData();
		final int[] boxSize         = model.getBoxSize();
		final double isovalue       = model.getIsovalue();
		
		int xLimit = lazyData.getShape()[2];
		int yLimit = lazyData.getShape()[1];
		int zLimit = lazyData.getShape()[0];
		
        if(xLimit % boxSize[0] != 0){
			xLimit = xLimit - xLimit % boxSize[0];
		}
		
		if(yLimit % boxSize[1] != 0){
			yLimit = yLimit - yLimit % boxSize[1];
		}
		
		if(zLimit % boxSize[2] != 0){
			zLimit = zLimit - zLimit % boxSize[2];
		}
		
		Map<Point, Integer> vertices  = new LinkedHashMap<Point, Integer>(89);
		
		// declare the variables external to the loop
		// should make things slightly faster
		int[] sliceStart = new int[3];
		int[] sliceStop = new int[3];
		int[] sliceStep = new int[3];
				
		// scan through the Z coord of the data
		// iterating steps determined by the XYZ boxsize

		final Set<Triangle> triangles  = new HashSet<Triangle>(89);
		
		/*
		 * create the list of threads - For parallel running of the algorithm
		 */
		
		List<MarchingCubesSliceProcessor> MCAThreadList = new ArrayList<MarchingCubesSliceProcessor>();
		
		
		// fill the list
		// give each callable the required data
		for(int k = 0; k < zLimit - 2 * boxSize[2]; k += boxSize[2])
		{
			sliceStart[0] = k;
			sliceStart[1] = 0;
			sliceStart[2] = 0;
			
			sliceStop[0] = k + 2 * boxSize[2];
			sliceStop[1] = yLimit;
			sliceStop[2] = xLimit;
			
			sliceStep[0] = boxSize[2];
			sliceStep[1] = boxSize[1];
			sliceStep[2] = boxSize[0];
			
			IDataset slicedImage = lazyData.getSlice(sliceStart,sliceStop, sliceStep);	
			Object[] currentSlice = slicedData(slicedImage, k, boxSize, xLimit, yLimit);
			
			MCAThreadList.add(
					new MarchingCubesSliceProcessor(
							currentSlice,
							yLimit,
							isovalue,
							boxSize,
							k));
			
		}
		
		// generate the results list
		ExecutorService executor = Executors.newCachedThreadPool();
		
		List<Future<Set<Triangle>>> result = null;
		
		try 
		{
			result = executor.invokeAll(MCAThreadList);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		executor.shutdown();
		
		if (result != null)
		{
			for (Future<Set<Triangle>> currentSet : result)
			{
				try 
				{
					triangles.addAll(currentSet.get());
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				catch (ExecutionException e) 
				{
					e.printStackTrace();
				}
			}
		}
		
		// index the triangles
		vertices = mapTriangleList(triangles);
			
		return new Object[]{triangles, vertices};
	}
	
	private Map<Point, Integer> mapTriangleList(Set<Triangle> listToIndex)
	{
		
		Map<Point, Integer> vertices  = new HashMap<Point, Integer>();
		
		for (Triangle tri : listToIndex)
		{
			
			if(!vertices.containsKey(tri.getA())){
				vertices.put(tri.getA(), vertices.size());
			}
			if(!vertices.containsKey(tri.getB())){
				vertices.put(tri.getB(), vertices.size());
			}
			if(!vertices.containsKey(tri.getC())){
				vertices.put(tri.getC(), vertices.size());
			}
			
		}
		return vertices;
	}
	

	/**
	 * This method creates the array of coordinates that is needed for the
	 * TriangleMesh class of JavaFX It uses the array of distinct points and
	 * gets the x,y,z coordinates
	 * 
	 * @param pointsWithoutRepetition
	 * @return
	 */
	public float[] getCoordinates(Point[] pointsWithoutRepetition) {
		float[] points = new float[3 * pointsWithoutRepetition.length];
		int j = 0;
		for (int i = 0; i < pointsWithoutRepetition.length; i++) {
			points[j] = (float) pointsWithoutRepetition[i].getxCoord();
			points[j + 1] = (float) pointsWithoutRepetition[i].getyCoord();
			points[j + 2] = (float) pointsWithoutRepetition[i].getzCoord();
			j += 3;
		}
		return points;
	}
	
	/**
	 * Method that gets the points for each slice
	 * @param slicedImage
	 * @param k
	 * @param boxSize
	 * @param xLimit
	 * @param yLimit
	 * @return
	 * new Object[2]{values, points}
	 */
	
	public Object[] slicedData(IDataset slicedImage, int k, int[] boxSize, int xLimit, int yLimit){
		
		Map<Point, Number> values = new HashMap<Point, Number>();
		Point[] points = new Point[2 * (xLimit/boxSize[0]) * (yLimit/boxSize[1])];
		
		
		int a = 0; // index in the array of points

		for(int i=0; i<xLimit; i+=boxSize[0]){
			for(int j=0; j<yLimit; j+=boxSize[1]){
								
				points[a] = new Point(i,j,k);
				points[a+1] = new Point(i,j,k+boxSize[2]);
				values.put(points[a], slicedImage.getDouble(0,j/boxSize[1],i/boxSize[0]));
				values.put(points[a+1], slicedImage.getDouble(1,j/boxSize[1],i/boxSize[0]));
				
				a+=2;
			}
		}
		return new Object[]{values, points};
	}
	
}
