package org.dawnsci.plotting.examples;

import org.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDatasetMathsService;

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
			final IDatasetMathsService mservice = (IDatasetMathsService)Activator.getService(IDatasetMathsService.class);
			
			// Create a 1D dataset programmatically. Can also use 
			final IDataset set = mservice.arange(0, 100000, 1000, IDatasetMathsService.INT);
			
			final IAxis otherX = system.createAxis("top", false, SWT.TOP);
			final IAxis otherY = system.createAxis("right", true, SWT.RIGHT);
		    
			
			
			system.setTitle("Axis Example");

		} catch (Exception e) {
  		    e.printStackTrace(); // Or your favourite logging
		}
		
		
    }
	
	protected String getImageName() {
		return "duke_football.jpg";
	}

}
