package org.dawnsci.isosurface.test;

import java.util.Random;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.isosurface.Activator;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.junit.Test;

public class defaultValue
{
	
	
	
	@Test
	public void Testing() throws Exception
	{
		
		int runCount = 10;
		
		testHillClimbing(new int[]{20,20,20}, 0.5, 1);
		
	}
	
	
	
	public void testHillClimbing(int [] testBoxSize, double valueAim, double timeAim) throws Exception
	{
		
		int[] boxSize = { 20 , 20, 20 };
		
//		double value = valueAim + ((1 - new Random().nextDouble()));
		double value = new Random().nextDouble();
		final ILazyDataset lz = org.eclipse.dawnsci.analysis.dataset.impl.Random.lazyRand(1024, 1024, 1024);
		
		final IOperationService service = (IOperationService) Activator.getService(IOperationService.class);
		
		IOperation<MarchingCubesModel, Surface>  generator = 
				(IOperation<MarchingCubesModel, Surface>)service.create("org.dawnsci.isosurface.marchingCubes");
		
		MarchingCubesModel test = generator.getModel();
		
		test.setBoxSize(boxSize);
		test.setColour(100, 100, 100);
		test.setIsovalue(value);
		
		
		double timeTaken = run(generator, lz);
		
				
		
	}
	
	
	public double run(IOperation<MarchingCubesModel, Surface>  generator, ILazyDataset slice)
	{
		double startTime = System.currentTimeMillis();
		
		generator.getModel().setLazyData(slice);
		
		Surface surface    = generator.execute(null, new ProgressMonitorWrapper(new IProgressMonitor()
		{
			
			@Override
			public void worked(int work)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void subTask(String name)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setTaskName(String name)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCanceled(boolean value)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isCanceled()
			{
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void internalWorked(double work)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void done()
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beginTask(String name, int totalWork)
			{
				// TODO Auto-generated method stub
				
			}
		}));
		
		final IDataset points     = new FloatDataset(surface.getPoints(), surface.getPoints().length);
		final IDataset textCoords = new FloatDataset(surface.getTexCoords(), surface.getTexCoords().length);
		final IDataset faces      = new IntegerDataset(surface.getFaces(), surface.getFaces().length);
		final int[] colour = surface.getColour();
		final double opacity = surface.getOpacity();
		
		double endTime = System.currentTimeMillis();
		double totalTime = endTime - startTime;
		
		return totalTime;	
				
		
	}
}
