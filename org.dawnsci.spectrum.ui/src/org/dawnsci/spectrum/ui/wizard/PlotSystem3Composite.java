package org.dawnsci.spectrum.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.january.dataset.AggregateDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class PlotSystem3Composite extends Composite {

    private IPlottingSystem<Composite> plotSystem3;
    private IDataset image1;
    
    public PlotSystem3Composite(Composite parent, int style
    		, AggregateDataset aggDat, ExampleModel model) throws Exception {
        super(parent, style);
        //composite = new Composite(parent, SWT.NONE);

        new Label(this, SWT.NONE).setText("Region of Interest 3D");
        
        try {
			plotSystem3 = PlottingFactory.createPlottingSystem();
		} 
        catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        
        this.createContents(aggDat, model); 
        
    }
     
    public void createContents(AggregateDataset aggDat,
    		ExampleModel model) throws Exception {

    	
    	final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        setLayout(gridLayout);
        
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(this, null);;
        
        plotSystem3.createPlotPart(PlotSystem3Composite.this, "ExamplePlot2", actionBarComposite, PlotType.IMAGE, null);
		
//		plotSystemComposite.returnSlider().addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				IDataset j = plotSystem2Composite.getImage();
//				image1= j;
//
//				plotSystem3.setPlotType(PlotType.SURFACE);
//				plotSystem3.createPlot2D(j, null, null);
//			
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
		model.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				IDataset j = model.getCurrentImage();
				image1= j;
				
				plotSystem3.setPlotType(PlotType.SURFACE);
				plotSystem3.createPlot2D(j, null, null);
			}
//
//			@Override
//			public void roiChanged(ROIEvent evt) {
//				// TODO Auto-generated method stub
//				IDataset j = plotSystem2Composite.getImage();
//				image1= j;
//				
//				plotSystem3.setPlotType(PlotType.SURFACE);
//				plotSystem3.createPlot2D(j, null, null);
////				
//			}
//
//			@Override
//			public void roiSelected(ROIEvent evt) {
//				// TODO Auto-generated method stub
//				IDataset j = plotSystem2Composite.getImage();
//				image1= j;
//				
//				plotSystem3.setPlotType(PlotType.SURFACE);
//				plotSystem3.createPlot2D(j, null, null);
//				
//			}
//
//				// TODO Auto-generated method stub
//				
//			}
		});
        
        
        final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_secondField.grabExcessVerticalSpace = true;
        gd_secondField.grabExcessVerticalSpace = true;
        
        IDataset image1 = null;

        plotSystem3.createPlotPart(this, "ExamplePlot3", actionBarComposite, PlotType.IMAGE, null);
        plotSystem3.getPlotComposite().setLayoutData(gd_secondField);
        plotSystem3.createPlot2D(image1, null, null);
        
	}
    
   public Composite getComposite(){   	
	   return this;
   }
   
   public IPlottingSystem<Composite> getPlotSystem(){
	   return plotSystem3;
   }

   public IDataset getImage(){
	   return image1;
   }
   


}
