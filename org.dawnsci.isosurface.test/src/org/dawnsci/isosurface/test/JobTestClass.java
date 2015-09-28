package org.dawnsci.isosurface.test;

import org.dawnsci.isosurface.Activator;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;

public class JobTestClass extends AbstractSlicingTool
{	
	public JobTestClass(Composite parent) throws Exception{
		
		final ILazyDataset lz = Random.lazyRand(1024, 1024, 1024);
		
		IPlottingSystem system = getSlicingSystem().getPlottingSystem();
		
		IsosurfaceJob job = new IsosurfaceJob("test", system, lz);
		
		final IOperationService service = (IOperationService) Activator
				.getService(IOperationService.class);
		
		IOperation<MarchingCubesModel, Surface>  generator =
				(IOperation<MarchingCubesModel, Surface>)service.create("org.dawnsci.isosurface.marchingCubes");
		
		MarchingCubesModel model = generator.getModel();
		model.setLazyData(lz);
		model.setBoxSize(new int[]{1,1,1});
		model.setColour(100, 100, 100);
		model.setIsovalue(1);
		model.setOpacity(100);
		
		job.compute(generator);
		
		
	}

	@Override
	public void militarize(boolean newSlice)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Enum getSliceType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAction createAction()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
