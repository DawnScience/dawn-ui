package org.dawnsci.plotting.examples;

import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

/**
 * View which creates a sector selection region
 * and listens to that region moving.
 * 
 * @author fcp94556
 *
 */
public class SectorExample extends ImageExample {
	

	public SectorExample() {
		super();			
	}
	
	public void createPartControl(Composite parent) {
		
		super.createPartControl(parent); // plots an image for us
		
		try {
			final IRegion sector = system.createRegion("Sector 1", RegionType.SECTOR);
			sector.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			sector.setROI(new SectorROI(1024, 1024, 0, 500, 0, Math.PI/3));
			sector.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
			system.addRegion(sector);
			
			// NOTE there are clases (like ROIProfile.sector(...)) which can do maths based
			// on where a user moves a sector.
			
			system.setTitle("Sector Example");
			
		} catch (Exception e) {
  		    e.printStackTrace(); // Or your favourite logging
		}
		
		
    }
	
	protected String getFileName() {
		return "duke_football.jpg";
	}

}
