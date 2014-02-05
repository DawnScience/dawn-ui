package org.dawnsci.plotting.examples;

import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * View which creates a sector selection region
 * and listens to that region moving.
 * 
 * @author fcp94556
 *
 */
public class AxisExample extends XYExample {
	

	public AxisExample() {
		super();			
	}
	
	public void createPartControl(Composite parent) {
		
		super.createPartControl(parent); // plots an image for us
		
		try {
			
			
			final IAxis otherX = system.createAxis("top", false, SWT.TOP);
			

			// TODO Need way in API of creating regions
			
		} catch (Exception e) {
  		    e.printStackTrace(); // Or your favourite logging
		}
		
		
    }
	
	protected String getImageName() {
		return "duke_football.jpg";
	}

}
