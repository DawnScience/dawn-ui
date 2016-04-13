package org.dawnsci.isosurface.isogui;

import java.util.Arrays;

import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.binding.BeanService;

public class IsoHandler 
{
	
	final private IsosurfaceJob job;
	
	private IBeanController controller;
	private IsoComposite isoComp; // not sure if this is ok
	
	private ValueAdapter isoValueListener;
	
	private ILazyDataset lazyDataset;
	
	private IPlottingSystem<?> system;
	
	public IsoHandler(Object ui, Object bean, IsosurfaceJob newJob, ILazyDataset lazyDataset, IPlottingSystem<?> system)
	{
		this.isoComp = (IsoComposite)ui;
		
		this.job = newJob;
		
		this.lazyDataset = lazyDataset;
		
		this.system = system;
		
		// only create a relevant listener if the job is available for use -> this is mainly the case in unit tests
		if (newJob != null)
		{
			createValueListener();
		}
		else
		{
			isoValueListener = new ValueAdapter("IsoValueListner") {
				
				@Override
				public void valueChangePerformed(ValueEvent e) {
					// do nothing as there is no job					
				}
			};
		}
		
		controller = null;
		try
		{
			controller = BeanService.getInstance()
					.createController(ui, bean);
			controller.addValueListener(isoValueListener);
			controller.beanToUI();
			controller.switchState(true);
		}
		catch (Exception e1)
		{
			
			System.err.println("\nController not set - Default value is NULL");
			e1.printStackTrace();
		}
		
		// create the initial surface
		isoComp.addNewSurface();
	}
	
	private void createValueListener() 
	{
		isoValueListener = new ValueAdapter("IsoValueListner")
		{
			IsoItem previous;
			
			@Override
			public void valueChangePerformed(ValueEvent e) 
			{
				try 
				{
					// update view
					controller.uiToBean();
					
					IsoItem current = null;
					
					if (isoComp.getItems().getListSize() > 0 && isoComp.getItems().getSelectedIndex() < isoComp.getItems().getListSize())
					{
						current = (IsoItem)isoComp.getItems().getBean();
					}
						
					if (current != null)
					{
						if (current.beanDeleted()) // this is a quick fix remove asap
						{
							job.destroy(current.getTraceKey());
						}
						
						if (current != null && !(current).equals(previous) && e.getFieldName() != null)
						{
							// run alg
							if ( 	e.getFieldName().contains("colour") ||
									e.getFieldName().contains("opacity") || 
									e.getFieldName().contains("name"))
							{
								if (system.getTrace(current.getTraceKey()) != null)
								{
									((IIsosurfaceTrace)system.getTrace(current.getTraceKey())).setMaterial(
													current.getColour().red,
													current.getColour().green,
													current.getColour().blue,
													current.getOpacity());
									((IIsosurfaceTrace)system.getTrace(current.getTraceKey())).setData(null, null, null, null);
								}
							}
							else
							{
								job.compute(
										new MarchingCubesModel(
												lazyDataset,
												current.getValue(),
												new int[] {
													current.getX(),
													current.getY(),
													current.getZ()},
												new int[]{
													current.getColour().red,
													current.getColour().green,
													current.getColour().blue},
												current.getOpacity(),
												current.getTraceKey(),
												current.getName()));
								
							}
							previous = (IsoItem)current.clone();
						}
					}
				}
				catch (Exception exc) 
				{
					exc.printStackTrace();
				}
			}
		};
	}
}
