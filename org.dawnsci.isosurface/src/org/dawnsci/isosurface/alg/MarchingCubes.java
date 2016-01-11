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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
		
		Map<Point, Integer> vertices  = new LinkedHashMap<Point, Integer>(128);
		
		// declare the variables external to the loop
		// should make things slightly faster
		int[] sliceStart = new int[3];
		int[] sliceStop = new int[3];
		int[] sliceStep = new int[3];
				
		// scan through the Z coord of the data
		// iterating steps determined by the XYZ boxsize

		Set<Triangle> triangles  = new HashSet<Triangle>(128);
		final ConcurrentHashMap<Point, Integer> sharedMap = new ConcurrentHashMap<Point, Integer>(128);
		
		/*
		 * create the list of threads - For parallel running of the algorithm
		 */
		List<MarchingCubesSliceProcessor> MCAThreadList = new ArrayList<MarchingCubesSliceProcessor>();
		
		int[] start = {0,0,0};
		
		ILazyDataset culledData = lazyData.getSliceView(start, xyzLimit, boxSize); // cull the data top remove all data between boxes
		
		int[] dataShape = {
				culledData.getShape()[0],
				culledData.getShape()[1],
				culledData.getShape()[2]};
		
		int[] segmentCount = {2, 2, 3};
				
//		int[] segmentSize = {
//				(int)(((float)dataShape[0] / segmentCount[0]) + 0.99f),
//				(int)(((float)dataShape[1] / segmentCount[1]) + 0.99f),
//				(int)(((float)dataShape[2] / segmentCount[2]) + 0.99f)};
		
		int[] segmentSize = {
				(int)(((float)dataShape[0] / segmentCount[0])),      
				(int)(((float)dataShape[1] / segmentCount[1])),      
				(int)(((float)dataShape[2] / segmentCount[2]))};     
		
		int[] end = {0,0,0};
		
		for (int x = 0; x < segmentCount[0]; x ++)
		{
			for (int y = 0; y <  segmentCount[1]; y ++)
			{
				for (int z = 0; z <  segmentCount[2]; z ++)	
				{
							
					start = new int[]{
							(int)(x * segmentSize[0]),
							(int)(y * segmentSize[1]),
							(int)(z * segmentSize[2])};
					
					end = new int[]{ 
							(int)(start[0] + segmentSize[0] + 1),
							(int)(start[1] + segmentSize[1] + 1),
							(int)(start[2] + segmentSize[2] + 1)};
					
					if (end[0] >= 	dataShape[0])
						end[0] =  	dataShape[0] + 1;
					if (end[1] >= 	dataShape[1])
						end[1] = 	dataShape[1] + 1;
					if (end[2] >= 	dataShape[2])
						end[2] = 	dataShape[2] + 1;
										
					MCAThreadList.addAll(runOnChunk(
							boxSize,
							sliceStart,
							sliceStop,
							sliceStep,
							start,
							culledData.getSliceView(start, end, new int[]{1,1,1}),
							isovalue,
							sharedMap));

					
				}
			}
		}
		
		// fill the list
		// give each callable the required data
		
//		for(int k = 0; k < zLimit - 2 * boxSize[2]; k += boxSize[2])
//		{
//			sliceStart[0] = k;
//			sliceStart[1] = 0;
//			sliceStart[2] = 0;
//			
//			sliceStop[0] = k + 2 * boxSize[2];
//			sliceStop[1] = yLimit;
//			sliceStop[2] = xLimit;
//			
//			sliceStep[0] = boxSize[2];
//			sliceStep[1] = boxSize[1];
//			sliceStep[2] = boxSize[0];
//			
//			IDataset slicedImage = lazyData.getSlice(sliceStart,sliceStop, sliceStep);	
//			Object[] currentSlice = slicedData(slicedImage, k, boxSize, xLimit, yLimit);
//			
//			MCAThreadList.add(
//					new MarchingCubesSliceProcessor(
//							currentSlice,
//							yLimit,
//							isovalue,
//							boxSize,
//							mapIndex,
//							sharedMap));
//			
//		}
		
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
		// vertices = mapTriangleList(triangles);
			
		return new Object[]{triangles, sharedMap};
	}
	
	private List<MarchingCubesSliceProcessor> runOnChunk(
			int[] boxSize,
			int[] sliceStart,
			int[] sliceStop,
			int[] sliceStep,
			int[] offset,
			ILazyDataset lazyData,
			double isovalue,
			Map<Point, Integer> sharedMap)
	{
		List<MarchingCubesSliceProcessor> MCAThreadList = new ArrayList<MarchingCubesSliceProcessor>();
		int[] xyzLimit = lazyData.getShape();
		
		for(int i = 0; i < xyzLimit[2] - 1; i ++)  
		{
			
			sliceStart[0] = 0;                                                                      
			sliceStart[1] = 0;                                                                      
			sliceStart[2] = i;                                                                      
			                                                                                        
			sliceStop[0] = xyzLimit[0];                                                      
			sliceStop[1] = xyzLimit[1];                                                                  
			sliceStop[2] = i + 2;                                                          
			
			IDataset slicedImage = lazyData.getSlice(sliceStart,sliceStop, new int[]{1,1,1});	            
			Object[] currentSlice = slicedData(slicedImage, i, offset, boxSize, xyzLimit[0], xyzLimit[1]);            
			                                                                                        
			MCAThreadList.add(                                                                      
					new MarchingCubesSliceProcessor(                                                
							currentSlice,                                                           
							xyzLimit[0],                                                                 
							isovalue,                                                               
							boxSize,                                                                
							mapIndex,                                                               
							sharedMap));
			
			
		}   
		System.out.println(MCAThreadList.size());
		return MCAThreadList;
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

	public Object[] slicedData(IDataset slicedImage, int k, int[]offset, int[] boxSize, int xLimit, int yLimit)
	{
		Point[] points = new Point[2 * xLimit * yLimit];
		Map<Point, Number> values = new HashMap<Point, Number>();		
		
		int[] newOffset = new int[]{
			offset[0]*boxSize[0],
			offset[1]*boxSize[1],
			offset[2]*boxSize[2]};		
				
		int a = 0; // index in the array of points

		for(int iy=0; iy<yLimit; iy++){
			for(int ix=0; ix<xLimit; ix++){
				
				points[a]   = 	new Point(newOffset[0] + (ix*boxSize[0]) ,newOffset[1] + (iy*boxSize[1]), newOffset[2] + (k*boxSize[2]));
				points[a+1] = 	new Point(newOffset[0] + (ix*boxSize[0]) ,newOffset[1] + (iy*boxSize[1]), newOffset[2] + ((k+1)*boxSize[2]));
			
				values.put(points[a]	, slicedImage.getDouble(ix,iy,0));
				values.put(points[a+1]	, slicedImage.getDouble(ix,iy,1));
				
				a+=2;
			}
		}
		return new Object[]{values, points};
	}
	
}
