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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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

	final AtomicInteger mapIndex = new AtomicInteger(0);
	
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
		
		float[] points = convertMapToPointsArray(v); 

		float[] texCoords = {0,0,0,1,1,1}; //{ 0, 0, (float) 0.5, (float) 0.5, 1, 1 };

		int[] faces = createFaces(triangles, v);

		if (points==null || points.length<1) throw new OperationException(this, "No isosurface found!");

		return new Surface(points, texCoords, faces);
	}
	
	private int[] createFaces(Set<Triangle> triangles, Map<Point,Integer> v)
	{
		int[] faces = new int[6 * triangles.size()];

		int k = 0;

		for (Triangle t: triangles) {
						
			faces[k] = v.get(t.getA());
			faces[k + 1] = 0;
			faces[k + 2] = v.get(t.getB());
			faces[k + 3] = 1;
			faces[k + 4] = v.get(t.getC());
			faces[k + 5] = 2;
			k += 6;
		}
		
		return faces;
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
		
		Set<Triangle> triangles  = new HashSet<Triangle>(128);
		final ConcurrentHashMap<Point, Integer> sharedMap = new ConcurrentHashMap<Point, Integer>(128);
		
		int[] xyzLimit = {
				lazyData.getShape()[0],
				lazyData.getShape()[1],
				lazyData.getShape()[2]};

		if(xyzLimit[0] % boxSize[0] != 0){
        	xyzLimit[0] = xyzLimit[0] - xyzLimit[0] % boxSize[0];
		}
		
		if(xyzLimit[1] % boxSize[1] != 0){
			xyzLimit[1] = xyzLimit[1] - xyzLimit[1] % boxSize[1];
		}
		
		if(xyzLimit[2] % boxSize[2] != 0){
			xyzLimit[2] = xyzLimit[2] - xyzLimit[2] % boxSize[2];
		}
		
		/*
		 * create the list of threads - For parallel running of the algorithm
		 */
		List<MarchingCubesSliceProcessor> MCAThreadList = new ArrayList<MarchingCubesSliceProcessor>();
		
		int[] start = {0,0,0};
		int[] end = {0,0,0};
		int[] segmentCount = {4, 2, 2}; // this is for debugging, make dynamic in the future
		
		
		int[] dataShape = {
				lazyData.getShape()[0] ,
				lazyData.getShape()[1] ,
				lazyData.getShape()[2] };
		
		int[] segmentSize = {
				(int)( ((int)((float)dataShape[0] / 2))),
				(int)( ((int)((float)dataShape[1] / 2))),
				(int)( ((int)((float)dataShape[2] / 2)))};
		
		segmentSize[0] -= segmentSize[0] % boxSize[0]; 
		segmentSize[1] -= segmentSize[1] % boxSize[1]; 
		segmentSize[2] -= segmentSize[2] % boxSize[2]; 
		
		segmentSize[0] += boxSize[0]; 
		segmentSize[1] += boxSize[1]; 
		segmentSize[2] += boxSize[2]; 
		
		
		for (int x = 0; x < segmentCount[0]; x ++)
		{                                                                                                   
			for (int y = 0; y <  segmentCount[1]; y ++)
			{                                                                             
				for (int z = 0; z <  segmentCount[2]; z ++)	                              
				{
					
					start = new int[]{                                                    
							(int)(((x * segmentSize[0]))),                                
							(int)(((y * segmentSize[1]))),                                                  
							(int)(((z * segmentSize[2])))};                                                 
					                                                                                        
					end = new int[]{ 
							(int)((start[0] + segmentSize[0]) + boxSize[0]),
							(int)((start[1] + segmentSize[1]) + boxSize[1]),
							(int)((start[2] + segmentSize[2]) + boxSize[2])};
					
					int[] offset = new int[]{
							start[0] / boxSize[0],
							start[1] / boxSize[1],
							start[2] / boxSize[2]};
					
					ILazyDataset lazyDataSlice = lazyData.getSliceView(start, end, boxSize);
					
					MCAThreadList.add(new MarchingCubesSliceProcessor(
												lazyDataSlice, 
												offset, 
												isovalue, 
												boxSize,
												this.mapIndex, 
												sharedMap));
					
				}
			}
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
					
		return new Object[]{triangles, sharedMap};
	}
	
	/**
	 * 
	 * 
	 * @param 
	 * @return
	 */
	private float[] convertMapToPointsArray(Map<Point, Integer> map)
	{
		float[] returnArray = new float[mapIndex.get()*3];
		
		Iterator<Entry<Point, Integer>> iterator = map.entrySet().iterator();
		
	    while (iterator.hasNext()) 
	    {
	        Map.Entry current = (Map.Entry)iterator.next();
	        returnArray[((int) current.getValue()*3)]   = (float)((Point)current.getKey()).getxCoord();
	        returnArray[((int) current.getValue()*3)+1] = (float)((Point)current.getKey()).getyCoord();
	        returnArray[((int) current.getValue()*3)+2] = (float)((Point)current.getKey()).getzCoord();
	    }
	    
	    return returnArray;
	}
	
	/**
	 * Method that gets the points for each slice
	 * @param slicedImage
	 * @param k
	 * @param boxSize
	 * @param yLimit
	 * @param xLimit
	 * @return
	 * new Object[2]{values, points}
	 */

	
	
}
