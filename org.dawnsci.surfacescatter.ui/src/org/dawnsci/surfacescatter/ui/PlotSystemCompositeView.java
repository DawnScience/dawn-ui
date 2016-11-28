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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

public class PlotSystemCompositeView extends Composite {


	private Slider slider;
    private IPlottingSystem<Composite> plotSystem;
    private IPlottingSystem<Composite> subImagePlotSystem;
    private IDataset image;
    private IRegion region;
    private Button outputControl;
    private Button zoom;
    private Button run;
    private int extra;
    private int numberOfImages;
    private Dataset nullImage;
    private SurfaceScatterPresenter ssp;
    private SurfaceScatterViewStart ssvs;
    private Text xValue;
    private Text imageNumber;
    
     
    public PlotSystemCompositeView(Composite parent, 
    							   int style,
    							   IDataset image, 
    							   int extra,
    							   int numberOfImages,
    							   Dataset nullImage,
    							   SurfaceScatterPresenter ssp,
    							   SurfaceScatterViewStart ssvs) {
    	
    	
        super(parent, style);

        this.slider=slider;
        this.extra = extra;
        this.numberOfImages = numberOfImages;
        this.nullImage = nullImage;
        this.ssp = ssp;
        this.ssvs = ssvs;
        
        new Label(this, SWT.NONE).setText("Raw Image");
        
        try {
			plotSystem = PlottingFactory.createPlottingSystem();
			subImagePlotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
        
        this.createContents(image); 
   
    }
     
    public void createContents(IDataset image) {

    	
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
      
        Group indicators = new Group(this, SWT.NONE);
    	GridLayout indicatorsLayout = new GridLayout(3,true);
    	
        if(extra == 1){
        	indicatorsLayout = new GridLayout(4,true);
    	}
        indicators.setLayout(indicatorsLayout);
		GridData indicatorsData = new GridData(SWT.FILL, SWT.NULL, true, false);
		indicators.setLayoutData(indicatorsData);
		if(extra == 1){
			Label outputControlLabel = new Label(indicators, SWT.NULL);
			outputControlLabel.setText("Take Ouput Marker");
		}
		Label xValueLabel = new Label(indicators, SWT.NULL);
		xValueLabel.setText("Variable");
		
		Label imageNumberLabel = new Label(indicators, SWT.NULL);
		imageNumberLabel.setText("Image No.");
		
		Button go = new Button(indicators, SWT.PUSH | SWT.FILL);
		go.setText("Go");
		
       
 
        if(extra == 1){
        	 outputControl = new Button (indicators, SWT.CHECK);
        	 outputControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }
        else{
        }
        
        xValue = new Text(indicators,SWT.SINGLE);
        xValue.setText(String.valueOf(ssp.getXValue(slider.getSelection())));
        
        imageNumber = new Text(indicators,SWT.SINGLE);
        imageNumber.setText("   " + slider.getSelection());
        
        
      
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(this, null);
        
        final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_secondField.grabExcessVerticalSpace = true;
        gd_secondField.grabExcessVerticalSpace = true;
        
        plotSystem.createPlotPart(this, 
        						  "ExamplePlot", 
        						  actionBarComposite, 
        						  PlotType.IMAGE, 
        						  null);
        
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
 
        ssp.regionOfInterestSetter(startROI);
        
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
					
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					
				}
			});
		    
		    run = new Button (this, SWT.PUSH);
			
			
		    run.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    run.setText("Run");
			
			
		    run.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					
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
		ssp.regionOfInterestSetter(region.getROI());	
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
	
	public Text getXValue(){
		return xValue;
	}
	
	public Text getImageNo(){
		return imageNumber;
	}

}
    




