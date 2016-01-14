package org.dawnsci.isosurface.isogui;

import org.dawnsci.isosurface.tool.IsosurfaceJob;
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
	
	public IsoHandler(Object ui, Object bean, IsosurfaceJob newJob)
	{
		this.isoComp = (IsoComposite)ui;
		
		this.job = newJob;
		
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
			
			System.out.println("\nController not set - Default value is NULL");
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
							if ( 	e.getFieldName().contains("x") || 
									e.getFieldName().contains("y") || 
									e.getFieldName().contains("z") || 
									e.getFieldName().contains("value"))
							{
								job.compute(
										new int[] {	current.getX(),
													current.getY(),
													current.getZ()},
										current.getValue(),
										current.getOpacity(),
										current.getColour(),
										current.getTraceKey(),
										current.getName());
							}
							else
							{
								job.update(
										new int[] {	current.getX(),
													current.getY(),
													current.getZ()},
										current.getValue(),
										current.getOpacity(),
										current.getColour(),
										current.getTraceKey(),
										current.getName());
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
