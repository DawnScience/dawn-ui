package org.dawb.workbench.ui.editors.test;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;


/**
 * Class simulates live updates from the plot server.
 * 
 * This is very useful for getting the plot server working!
 * 
 * @author fcp94556
 *
 */
public class SWTXYLiveUpdateTest {

	
	@Test
	public void testXYUpdate() throws Throwable {
		
		EclipseUtils.getPage().showView("uk.ac.diamond.scisoft.analysis.rcp.plotView1");
		EclipseUtils.getPage().setPartState(EclipseUtils.getPage().findViewReference("uk.ac.diamond.scisoft.analysis.rcp.plotView1"), IWorkbenchPage.STATE_MAXIMIZED);
		
        // Once a second starts a thread that updates plot with random data
        for (int i = 0; i < 1000; i++) {
			
        	final Thread thread = new Thread(new Runnable() {
        		public void run() {
                	final DoubleDataset data = new DoubleDataset(new int[]{2048});
                	for (int j = 0; j < 2048; j++) {
                		data.getData()[j] =  Math.random();
        			}
                	data.setName("Random xy");
                	try {
						SDAPlotter.plot("Plot 1", data);
					} catch (Exception e) {
						e.printStackTrace();
					}
        		}
        	});
        	thread.setDaemon(true);
        	thread.start();
        	
        	if (i%1000==0) System.out.println(i);
    		EclipseUtils.delay(900);
      	
		}
	
 		LoaderFactory.clear();
		EclipseUtils.delay(1000);
				
	}

	
	@Test
	public void testImageUpdate() throws Throwable {
		
		EclipseUtils.getPage().showView("uk.ac.diamond.scisoft.analysis.rcp.plotView1");
		EclipseUtils.getPage().setPartState(EclipseUtils.getPage().findViewReference("uk.ac.diamond.scisoft.analysis.rcp.plotView1"), IWorkbenchPage.STATE_MAXIMIZED);
		
        // Once a second starts a thread that updates plot with random data
        for (int i = 0; i < 1000; i++) {
			
        	final Thread thread = new Thread(new Runnable() {
        		public void run() {
                	final DoubleDataset data = new DoubleDataset(new int[]{2048, 2048});
                	for (int j = 0; j < 2048*2048; j++) {
                		data.getData()[j] =  Math.random();
        			}
                	data.setName("Random image");
                	try {
						SDAPlotter.imagePlot("Plot 1", data);
					} catch (Exception e) {
						e.printStackTrace();
					}
        		}
        	});
        	thread.setDaemon(true);
        	thread.start();
        	
        	if (i%1000==0) System.out.println(i);
    		EclipseUtils.delay(900);
      	
		}
	
 		LoaderFactory.clear();
		EclipseUtils.delay(1000);
				
	}

}
