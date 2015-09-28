package org.dawnsci.isosurface.isogui;

import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.reflection.IBeanController;

public class IsoValueListener extends ValueAdapter{

	private IBeanController controller;
	
	public IsoValueListener(IBeanController controller)
	{
		this.controller = controller;
	}
	
	@Override
	public void valueChangePerformed(ValueEvent e) 
	{
		try 
		{
			controller.uiToBean();
		} 
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		
	}

}
