package org.dawnsci.isosurface.isogui;

import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.reflection.IBeanController;
import org.eclipse.richbeans.reflection.BeanService;

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
		
		isoValueListener = new ValueAdapter("IsoValueListner")
		{
			IsoItem previous;
			
			@Override
			public void valueChangePerformed(ValueEvent e) 
			{
				try 
				{
					System.out.println("Value change performed");
					// update view
					
					controller.uiToBean();
					
					Object test = e.getActingBean();
					
					if (test != null)
					{
						System.out.println("yay not null!!");
					}
					
					IsoItem current = (IsoItem)isoComp.getItems().getBean();
					
					if (!(current).equals(previous)) 
					{
						// run alg
						job.compute(
								new int[] {	current.getX(),
											current.getY(),
											current.getZ()},
								current.getValue(),
								current.getOpacity(),
								current.getColour(),
								current.getTraceKey());
						
						previous = (IsoItem)current.clone();
					}
					
					
					
				} 
				catch (Exception exc) 
				{
					exc.printStackTrace();
				}
			}
		};
		
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
		
		
	}
}
