package org.dawnsci.isosurface.test.marchingCubes;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.dawnsci.isosurface.alg.MarchingCubes;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.january.dataset.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Random;
import org.junit.Ignore;
import org.junit.Test;

public class Cancellation 
{

	static int SEED = 123456789;
		
		// benchmarking
		int[] dataSize = {100,100,100};
		int[] boxSize = {4,4,4};
		
		// general
		MarchingCubes algorithm;
		IDataset lz;
		MarchingCubesModel model;
		Surface testResult;
		
		double stopTime = 0;
		double startTime = 0;
		
		boolean cancelled = false;
		
		int duringCount = 0;
		int afterCount = 0;
		
		/**
		 * used to initialise the required information
		 * might have to copy and paste to increase coupling
		 * @throws Exception 
		 */
		public void start(int[] dataSetSizeXYZ, int[] boxSizeXYZ, IProgressMonitor monitor) throws DatasetException
		{
			
			lz = Random.lazyRand(dataSetSizeXYZ).getSlice();

			IntegerDataset axis = DatasetFactory.createRange(IntegerDataset.class, dataSetSizeXYZ[0]);
			List<IntegerDataset> axes = Arrays.asList(axis, axis, axis);
			
			Random.seed(SEED);
			
			model = new MarchingCubesModel(lz,axes,0.5,boxSizeXYZ,new int[]{1,1,1}, 1,"traceID");
			algorithm = new MarchingCubes(model);	
			
			// execute the algorithmA
			testResult = algorithm.execute(monitor);
			
		}

		@Ignore
		@Test
		public void cancellation_Significance_Test() throws DatasetException
		{
			IProgressMonitor monitor = new IProgressMonitor() {
				
				@Override
				public void worked(int work) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void subTask(String name) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void setTaskName(String name) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void setCanceled(boolean value) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public boolean isCanceled() {
					// TODO Auto-generated method stub
					duringCount++;
					if (cancelled)
						afterCount++;
					
					return cancelled;
				}
				
				@Override
				public void internalWorked(double work) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void done() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void beginTask(String name, int totalWork) {
					// TODO Auto-generated method stub
					
				}
			};
						
			cancelled = false;

			new Thread(new Runnable() {
		        public void run(){
		        	double startWhileTime = System.currentTimeMillis();
		        	
		        	while (System.currentTimeMillis() - startWhileTime < 100)
					{
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					cancelled = true;
		        }
		    }).start();	

			start(new int[]{100,100,100},new int[] {1,1,1}, monitor);
						
			assertTrue("The agorithm continued for more than 100ms after being canceled.",
					afterCount < 10);
		}
	
	
}
