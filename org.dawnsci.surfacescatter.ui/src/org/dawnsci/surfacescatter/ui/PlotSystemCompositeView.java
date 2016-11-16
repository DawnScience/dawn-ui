package org.dawnsci.surfacescatter.ui;
import java.util.ArrayList;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.DataModel;
import org.dawnsci.surfacescatter.ExampleModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.SuperModel;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

public class PlotSystemCompositeView extends Composite {


	private Slider slider;
    private IPlottingSystem<Composite> plotSystem;
    private IPlottingSystem<Composite> subImagePlotSystem;
    private IDataset image;
    private IRegion region;
    private Button outputControl;
    private ExampleModel model;
    private DataModel dm;
    private GeometricParametersModel gm;
    private ArrayList<ExampleModel> models;
    private SuperModel sm;
    private Button zoom;
    private Button run;
    private int extra;
    private int numberOfImages;
    private Dataset nullImage;
    private SurfaceScatterPresenter ssp;
    private SurfaceScatterViewStart ssvs;
     
    public PlotSystemCompositeView(Composite parent, 
    		int style,
    		ArrayList<ExampleModel> models, 
    		SuperModel sm,
    		IDataset image, 
    		int extra,
    		int numberOfImages,
    		Dataset nullImage,
    		SurfaceScatterPresenter ssp,
    		SurfaceScatterViewStart ssvs) {
    	
    	
        super(parent, style);

        this.slider=slider;
        this.sm=sm;
        this.models = models;
        this.model = models.get(sm.getSelection());
        this.extra = extra;
        this.numberOfImages = numberOfImages;
        this.nullImage = nullImage;
        this.ssp = ssp;
        this.ssvs = ssvs;
        
        new Label(this, SWT.NONE).setText("Raw Image");
        //composite = new Composite(parent, SWT.NONE);
//        slider = new Slider(this, SWT.HORIZONTAL);
        
     
        try {
			plotSystem = PlottingFactory.createPlottingSystem();
			subImagePlotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        
        this.createContents(model, image); 
//        System.out.println("Test line");
        
    }
     
    public void createContents(ExampleModel model, IDataset image) {

    	
    	final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        setLayout(gridLayout);
        
        slider = new Slider(this, SWT.HORIZONTAL);
        
        slider.setMinimum(0);
	    slider.setMaximum(numberOfImages);
	    slider.setIncrement(1);
	    slider.setThumb(1);
        
        final GridData gd_firstField = new GridData(SWT.FILL, SWT.CENTER, true, false);
        slider.setLayoutData(gd_firstField);
        
        if(extra != 1){
        	int pos = ssp.getSliderPos();
        	slider.setSelection(pos);
        }
      
        outputControl = new Button (this, SWT.CHECK);
 
        if(extra == 1){
        	 outputControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        outputControl.setText("Take Output Marker");
        }
        else{
        	outputControl.setVisible(false);
        }
        
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(this, null);
        
        //plotSystem.createPlotPart(this, "ExamplePlot", actionBarComposite, PlotType.IMAGE, null);
        
        
        final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_secondField.grabExcessVerticalSpace = true;
        gd_secondField.grabExcessVerticalSpace = true;
        
         System.out.println(plotSystem.getClass());
        

        plotSystem.createPlotPart(this, "ExamplePlot", actionBarComposite, PlotType.IMAGE, null);
        plotSystem.getPlotComposite().setLayoutData(gd_secondField);
        plotSystem.createPlot2D(nullImage, null, null);
        
        try {
			region =plotSystem.createRegion("myRegion", RegionType.BOX);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        ActionBarWrapper actionBarComposite1 = ActionBarWrapper.createActionBars(this, null);
        
        if(extra == 1){
	        subImagePlotSystem.createPlotPart(this, "Region of interest", actionBarComposite1, PlotType.IMAGE, null);
			subImagePlotSystem.getPlotComposite().setLayoutData(gd_secondField);
			subImagePlotSystem.createPlot2D(nullImage, null, null);
        }
        
		plotSystem.addRegion(region);
		
		
		RectangularROI startROI = new RectangularROI(100,100,50,50,0);
		
		if (extra != 1){
			IRectangularROI o = ssp.getROI().getBounds();
			int[] len = o.getIntLengths();
			int[] pt = o.getIntPoint();
			startROI = new RectangularROI(pt[0],pt[1],len[0],len[1],0);
		}
		
		region.setROI(startROI);
 
        model.setROI(startROI);
        
		region.addROIListener(new IROIListener() {

			@Override
			public void roiDragged(ROIEvent evt) {
				roiStandard(evt);
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				roiStandard(evt);
			}

			@Override
			public void roiSelected(ROIEvent evt) {
				roiStandard(evt);
			}
			
			@SuppressWarnings("unchecked")
			public void roiStandard(ROIEvent evt) {
				ssp.regionOfInterestSetter(region.getROI());
				if(extra == 1){
					ssp.sliderZoomedArea(slider.getSelection(), region.getROI(), subImagePlotSystem);
				}
//					models.get(sm.getSelection()).setBox(startROI);	
			}
			
		});
        
		slider.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ssp.sliderMovemementMainImage(slider.getSelection(), plotSystem);
				
				if(extra == 1){
					ssp.sliderZoomedArea(slider.getSelection(), region.getROI(), subImagePlotSystem);			
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		if (extra == 1){
			
			zoom = new Button (this, SWT.PUSH);
			
			
		    zoom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    zoom.setText("Zoom and set");
			
			
		    zoom.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
		    
		    run = new Button (this, SWT.PUSH);
			
			
		    run.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    run.setText("Run");
			
			
		    run.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
		}    
		
		if (extra != 1){
			ssp.sliderMovemementMainImage(slider.getSelection(), plotSystem);
		}
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
   

	public Button getOutputControl(){
		return outputControl;
	}

	public Slider getSlider(){
		return slider;
	}

	public void getBoxPosition(){
		models.get(sm.getSelection()).setROI(region.getROI());
	}
	
	public void getBoxPosition(int g){
		models.get(g).setROI(region.getROI());
	}
	
	public void setModels(ExampleModel model1){
		this.model= model1;

	}
	
	public IPlottingSystem<Composite> getSubImagePlotSystem(){
		return subImagePlotSystem; 
	}
	
	public void updateImage(IDataset image){
		plotSystem.updatePlot2D(image, null, null);
	}
	
   
	public void sliderReset(ExampleModel model1){
		slider.setMinimum(0);
	    slider.setMaximum(model1.getDatImages().getShape()[0]);
	    slider.setIncrement(1);
	    slider.setThumb(1);
	}
	
	public void setRegion(int[][] lenpt){
		RectangularROI newROI = new RectangularROI(lenpt[1][0],lenpt[1][1],lenpt[0][0],lenpt[0][1],0);
		region.setROI(newROI);
	}

	public IRegion getGreenRegion(){
		return region;
	}
	
	public Button getZoom(){
		return zoom;
	}
	
	public Button getRun(){
		return run;
	}


}
    




