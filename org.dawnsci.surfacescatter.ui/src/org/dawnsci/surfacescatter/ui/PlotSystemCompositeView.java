package org.dawnsci.surfacescatter.ui;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.ExampleModel;
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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class PlotSystemCompositeView extends Composite {


	private Slider slider;
    private IPlottingSystem<Composite> plotSystem;
    private IPlottingSystem<Composite> subImagePlotSystem;
    private IPlottingSystem<Composite> subImageBgPlotSystem;
    private PlotSystem1CompositeView customComposite1;
    private IDataset image;
    private IRegion region;
    private Button outputControl;
    private Button run;
//    private int extra;
    private int numberOfImages;
    private Dataset nullImage;
    private SurfaceScatterPresenter ssp;
    private SurfaceScatterViewStart ssvs;
    private Text xValue;
    private Text imageNumber;
    private Button replay;
	private TabFolder folder;
	private Text xCoord;
	private Text xLen;
	private Text yCoord;
	private Text yLen;
	private SashForm form;
	
	
     
    public PlotSystemCompositeView(Composite parent, 
    							   int style,
    							   IDataset image, 
    							   int extra,
    							   int numberOfImages,
    							   Dataset nullImage,
    							   SurfaceScatterPresenter ssp,
    							   SurfaceScatterViewStart ssvs) {
    	
    	
        super(parent, style);


        this.numberOfImages = numberOfImages;
        this.nullImage = nullImage;
        this.ssp = ssp;
        this.ssvs = ssvs;
        
       
        
        try {
			plotSystem = PlottingFactory.createPlottingSystem();
			subImagePlotSystem = PlottingFactory.createPlottingSystem();
			subImageBgPlotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
        
        this.createContents(image); 
   
    }
     
    public void createContents(IDataset image) {

    	
    	
		form = new SashForm(this, SWT.VERTICAL);
		form.setLayout(new GridLayout());
		form.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
    	
    	
//    	GridLayout gridLayout = new GridLayout();
//        gridLayout.numColumns = 1;
//        setLayout(gridLayout);
//        
		Group mainImage = new Group(form, SWT.FILL);
		GridLayout mainImageLayout = new GridLayout();
		mainImage.setLayout(mainImageLayout);
		GridData mainImageData = new GridData(SWT.FILL, SWT.FILL, true, true);
		mainImage.setLayoutData(mainImageData);
		
        slider = new Slider(mainImage, SWT.HORIZONTAL);
        
        slider.setMinimum(0);
	    slider.setMaximum(numberOfImages);
	    slider.setIncrement(1);
	    slider.setThumb(1);
        
        final GridData gd_firstField = new GridData(SWT.FILL, SWT.CENTER, true, false);
        slider.setLayoutData(gd_firstField);
      
        Group indicators = new Group(mainImage, SWT.NONE);
        GridLayout 	indicatorsLayout = new GridLayout(4,true);
    	
        indicators.setLayout(indicatorsLayout);
		GridData indicatorsData = new GridData(SWT.FILL, SWT.NULL, true, false);
		indicators.setLayoutData(indicatorsData);
		
		Label outputControlLabel = new Label(indicators, SWT.NULL);
		outputControlLabel.setText("Take Ouput Marker:");
		
		Label xValueLabel = new Label(indicators, SWT.NULL);
		xValueLabel.setText("X Variable:");
		
		Label imageNumberLabel = new Label(indicators, SWT.NULL);
		imageNumberLabel.setText("Image No.:");
		
		Button go = new Button(indicators, SWT.PUSH | SWT.FILL);
		go.setText("Go");
		
        outputControl = new Button (indicators, SWT.CHECK);
        outputControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        xValue = new Text(indicators,SWT.SINGLE);
        xValue.setText(String.valueOf(ssp.getXValue(slider.getSelection())));
        
        imageNumber = new Text(indicators,SWT.SINGLE);
        imageNumber.setText("   " + slider.getSelection());
        
        replay = new Button(indicators, SWT.PUSH | SWT.FILL);
		replay.setText("Replay");
        
//		Group rOIpositionIndicators = new Group(indicators, SWT.NONE);
//    	GridLayout rOIpositionIndicatorsLayout = new GridLayout(4,true);
//    	rOIpositionIndicators.setLayout(rOIpositionIndicatorsLayout);
    	
		Label xPt = new Label(indicators, SWT.NULL);
		xPt.setText("ROI x coord:");
		
		xCoord = new Text(indicators,SWT.SINGLE);
	    xCoord.setText("  " + String.valueOf(ssp.getLenPt()[1][0]));
	    
	    Label xLenLabel = new Label(indicators, SWT.NULL);
	    xLenLabel.setText("x Len:");
		
		xLen = new Text(indicators,SWT.SINGLE);
	    xLen.setText("  " + String.valueOf(ssp.getLenPt()[0][0]));
        
	    Label yPt = new Label(indicators, SWT.NULL);
		yPt.setText("ROI y coord:");
		
		yCoord = new Text(indicators,SWT.SINGLE);
	    yCoord.setText("  " + String.valueOf(ssp.getLenPt()[1][1]));
	    
	    Label yLenLabel = new Label(indicators, SWT.NULL);
	    yLenLabel.setText("y Len:");
		
		yLen = new Text(indicators,SWT.SINGLE);
	    yLen.setText("  " + String.valueOf(ssp.getLenPt()[0][1]));
        
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(form, null);
        
        final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_secondField.grabExcessVerticalSpace = true;
        gd_secondField.grabExcessVerticalSpace = true;
        
       
        
        plotSystem.createPlotPart(form, 
        						  "Raw Image", 
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
		
//        ActionBarWrapper actionBarComposite1 = ActionBarWrapper.createActionBars(this, null);
//        
        folder = new TabFolder(form, SWT.BORDER | SWT.CLOSE);
    	folder.setLayout(new GridLayout());
    		
    	folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        	
			////////////////////////////////////////////////////////
			//		Tab 1 Setup
			//////////////////////////////////////////////////////////
        	
    		TabItem subI = new TabItem(folder, SWT.NONE);
    		subI.setText("Background Options");
    		subI.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
//    		
    		Composite subIComposite = new Composite(folder, SWT.NONE | SWT.FILL);
    		subIComposite.setLayout(new GridLayout());
    		subIComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    		
    		GridData ld2 = new GridData(SWT.FILL, SWT.FILL, true, true);

    		customComposite1 = new PlotSystem1CompositeView(subIComposite,
    				SWT.NONE,
    				0, 
    				0, 
    				ssp);
    		
    		customComposite1.setLayout(new GridLayout());
    		customComposite1.setLayoutData(ld2);
			
			////////////////////////////////////////////////////////
			//		Tab 2 Setup
			//////////////////////////////////////////////////////////
			
			TabItem subBgI = new TabItem(folder, SWT.NONE);
			subBgI.setText("Bg Subtracted Image");
			subBgI.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			Composite subBgIComposite = new Composite(folder, SWT.NONE | SWT.FILL);
			subBgIComposite.setLayout(new GridLayout());
			subBgIComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			subBgI.setControl(subBgIComposite);
			
//			ActionBarWrapper actionBarComposite1 = ActionBarWrapper.createActionBars(subBgI, null);
			subImageBgPlotSystem.createPlotPart(subBgIComposite, "Region of interest", null, PlotType.IMAGE, null);
			subImageBgPlotSystem.getPlotComposite().setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true));
			subImageBgPlotSystem.createPlot2D(nullImage, null, null);
//			subImageBgPlotSystem.
     
        
        
        
		plotSystem.addRegion(region);
		
		RectangularROI startROI = new RectangularROI(100,100,50,50,0);
		
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
				
			}
		});
        
		slider.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ssp.sliderMovemementMainImage(slider.getSelection(), 
											  plotSystem);
				
				
//				ssp.sliderZoomedArea(slider.getSelection(),
//										 region.getROI(), 
//										 subImagePlotSystem);
					
				ssp.bgImageUpdate(subImageBgPlotSystem,
									  slider.getSelection());
					
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
			
		    
		    run = new Button (form, SWT.PUSH);
			
			
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
		    
		
		    form.setWeights(new int[] { 20,5, 40, 30, 5 });
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
	
	public IPlottingSystem<Composite> getSubImageBgPlotSystem(){
		return subImageBgPlotSystem; 
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
	
	public IRegion getIRegion(){
		return region;
	}
	
	public void setRegion(int[][] lenpt){
		RectangularROI newROI = new RectangularROI(lenpt[1][0],
												   lenpt[1][1],
												   lenpt[0][0],
												   lenpt[0][1],
												   0);
		region.setROI(newROI);
	}

	public IRegion getGreenRegion(){
		return region;
	}
	
	
	public Button getRun(){
		return run;
	}
	
	public Button getReplay(){
		return replay;
	}
	
	public Text getXValue(){
		return xValue;
	}
	
	public Text getImageNo(){
		return imageNumber;
	}
	
	public TabFolder getFolder(){
		return folder;
	}
	
	public Text getXCoord(){
		return xCoord;
	}
	
	public Text getXLen(){
		return xLen;
	}
	public Text getYCoord(){
		return yCoord;
	}
	
	public Text getYLen(){
		return yLen;
	}
	
	public Text[] getROITexts(){
		Text[] texts = new Text [4];
		
		texts[0] = xCoord;
		texts[1] = xLen;
		texts[2] = yCoord;
		texts[3] = yLen;
		
		return texts;
	}
	
	public void setROITexts(String[] values){
		
		xCoord.setText(values[0]);
		xLen.setText(values[1]);
		yCoord.setText(values[2]);
		yLen.setText(values[3]);
		
	}
	
	
	public void setROITexts(int[][] LenPt){
		
		String[] values = new String[4];
		
		values[0] = String.valueOf(LenPt[1][0]);
		values[1] = String.valueOf(LenPt[0][0]);
		values[2] = String.valueOf(LenPt[1][1]);
		values[3] = String.valueOf(LenPt[0][1]);
		
		setROITexts(values);
				
	}
}
    




