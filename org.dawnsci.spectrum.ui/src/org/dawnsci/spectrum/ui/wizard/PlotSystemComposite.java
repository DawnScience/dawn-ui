package org.dawnsci.spectrum.ui.wizard;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.AggregateDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class PlotSystemComposite extends Composite {


    final private Slider slider;
    private IPlottingSystem<Composite> plotSystem;
    private IDataset image;
    private IRegion region;
    
     
    public PlotSystemComposite(Composite parent, int style
    		, AggregateDataset aggDat, String title, ExampleModel model) {
        super(parent, style);
        
        new Label(this, SWT.NONE).setText(title);
        //composite = new Composite(parent, SWT.NONE);
        slider = new Slider(this, SWT.HORIZONTAL);
        
        
        try {
			plotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        
        this.createContents(aggDat, model); 
//        System.out.println("Test line");
        
    }
     
    public void createContents(AggregateDataset aggDat, ExampleModel model) {

    	
    	final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        setLayout(gridLayout);
         
//        slider = new Slider(this, SWT.HORIZONTAL);
        
        slider.setMinimum(0);
	    slider.setMaximum(aggDat.getShape()[0]);
	    slider.setIncrement(1);
	    slider.setThumb(1);
        
        final GridData gd_firstField = new GridData(SWT.FILL, SWT.CENTER, true, false);
        slider.setLayoutData(gd_firstField);
        
        
        
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(this, null);;
        
        //plotSystem.createPlotPart(this, "ExamplePlot", actionBarComposite, PlotType.IMAGE, null);
        
        
        final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_secondField.grabExcessVerticalSpace = true;
        gd_secondField.grabExcessVerticalSpace = true;
        

         System.out.println(plotSystem.getClass());
        
        SliceND slice = new SliceND(aggDat.getShape());
        slice.setSlice(0, 1, 2, 1);
		IDataset i = null;
		try {
			i = aggDat.getSlice(slice);
		} catch (DatasetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		i.squeeze();
		image =i;
		
        slider.addSelectionListener(new SelectionListener() {
        	
		public void widgetSelected(SelectionEvent e) {
			int selection = slider.getSelection();
			
		    try {
				slice.setSlice(0, selection, selection+1, 1);
				IDataset i = aggDat.getSlice(slice);
				i.squeeze();
				plotSystem.createPlot2D(i, null, null);
				image = i;
				model.setImageNumber(selection);
		    } 
		    catch (Exception f) {
				// TODO Auto-generated catch block
				f.printStackTrace();
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			try {
		    } 
		    catch (Exception f) {
				// TODO Auto-generated catch block
				f.printStackTrace();
			}
		}
			
		});
        

        plotSystem.createPlotPart(this, "ExamplePlot", actionBarComposite, PlotType.IMAGE, null);
        plotSystem.getPlotComposite().setLayoutData(gd_secondField);
        plotSystem.createPlot2D(i, null, null);
        

        try {
			region =plotSystem.createRegion("myRegion", RegionType.BOX);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		plotSystem.addRegion(region);
		
		RectangularROI startROI = new RectangularROI(10,10,100,100,0);
		region.setROI(startROI);
 
        model.setROI(startROI);
		region.addROIListener(new IROIListener() {

			@Override
			public void roiDragged(ROIEvent evt) {
				model.setROI(region.getROI());
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				// TODO Auto-generated method stub
				model.setROI(region.getROI());			}

			@Override
			public void roiSelected(ROIEvent evt) {
				model.setROI(region.getROI());			}

		});
        
    }
		
   
   public int getSliderPos(){
	   int sliderPos = slider.getSelection();
	   return sliderPos;
   }
   

   
   public Composite getComposite(){
   	
   	return this;
   }
   
   public IPlottingSystem getPlotSystem(){
	   return plotSystem;
   }

   public IDataset getImage(){
	   return image;
   }
   
}
    




