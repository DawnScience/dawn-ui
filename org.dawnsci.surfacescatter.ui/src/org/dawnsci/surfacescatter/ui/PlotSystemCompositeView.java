package org.dawnsci.surfacescatter.ui;

import java.util.Arrays;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.ExampleModel;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.ProcessingMethodsEnum.ProccessingMethod;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class PlotSystemCompositeView extends Composite {

	private Slider slider;
    private IPlottingSystem<Composite> plotSystem;
    private IPlottingSystem<Composite> subImageBgPlotSystem;
	private PlotSystem1CompositeView customComposite1;
    private IDataset image;
    private IRegion region;
    private IRegion bgRegion;
    private IRegion secondBgRegion;
    private Button outputControl;
    private Group processing;
    private Button run;
    private Combo processingMode;;
    private int numberOfImages;
    private Dataset nullImage;
    private SurfaceScatterPresenter ssp;
    private Text xValue;
    private Text imageNumber;
    private Button replay;
	private Button go;
	private TabFolder folder;
	private Text xCoord;
	private Text xLen;
	private Text yCoord;
	private Text yLen;
	private Text lorentz;
	private Text polarisation;
	private Text areaCorrection;
	private Text rawIntensity;
	private Text reflectivityAreaCorr;
	private Text reflectivityFluxCorr;
	private SashForm form;
	private TabItem subBgI;
	private TabItem correctionsTab;
	private Button centreRegion;
	private Button centreSecondBgRegion;
	private Button increment;
	private Button decrement;
	private Group manualControls;
	private SurfaceScatterViewStart ssvs;
	private Button accept;
	private Button acceptBack;

	
    public PlotSystemCompositeView(Composite parent, 
    							   int style,
    							   IDataset image, 
    							   int extra,
    							   int numberOfImages,
    							   Dataset nullImage,
    							   SurfaceScatterPresenter ssp,
    							   SurfaceScatterViewStart ssvs
    							   ) {
        super(parent, style);
        this.numberOfImages = numberOfImages;
        this.nullImage = nullImage;
        this.ssp = ssp;
        this.ssvs = ssvs;
        
        try {
			plotSystem = PlottingFactory.createPlottingSystem();
			
		} catch (Exception e2) {
			e2.printStackTrace();
		}
        
        this.createContents(image); 
    }
     
    public void createContents(IDataset image) {

    	this.image = image;
    	
    	 ssp.addStateListener(new  IPresenterStateChangeEventListener() {
				
				@Override
				public void update() {
					generalUpdate();
					
				}
		});
    	
    	Display display = Display.getCurrent();
        Color gold = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
        Color transparent = display.getSystemColor(SWT.COLOR_TRANSPARENT);
        
		form = new SashForm(this, SWT.VERTICAL);
		form.setLayout(new GridLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    	       
		Group mainImage = new Group(form, SWT.FILL);
		GridLayout mainImageLayout = new GridLayout(1,true);
		mainImage.setLayout(mainImageLayout);
		GridData mainImageData = new GridData(SWT.FILL, SWT.FILL, true,false);
		mainImage.setLayoutData(mainImageData);
		
        slider = new Slider(mainImage, SWT.HORIZONTAL);
        
        slider.setMinimum(0);
	    slider.setMaximum(numberOfImages);
	    slider.setIncrement(1);
	    slider.setThumb(1);
        
        final GridData gd_firstField = new GridData(SWT.FILL, SWT.CENTER, true, false);
        slider.setLayoutData(gd_firstField);
      
        Group indicators = new Group(mainImage, SWT.NONE);
        GridLayout 	indicatorsLayout = new GridLayout(3,true);
        indicators.setLayout(indicatorsLayout);
		GridData indicatorsData = new GridData(SWT.FILL, SWT.NULL, true, false);
		indicators.setLayoutData(indicatorsData);
		
		InputTileGenerator tile1 = new InputTileGenerator("X Variable:", String.valueOf(ssp.getXValue(slider.getSelection())), indicators);
		xValue = tile1.getText();
		InputTileGenerator tile2 = new InputTileGenerator("Image No.:", String.valueOf(slider.getSelection()), indicators);
		imageNumber = tile2.getText();
		InputTileGenerator tile3 = new InputTileGenerator("ROI x coord:", String.valueOf(ssp.getLenPt()[1][0]), indicators);
		xCoord = tile3.getText();		
		InputTileGenerator tile4 = new InputTileGenerator("x Len:", String.valueOf(ssp.getLenPt()[0][0]), indicators);
		xLen = tile4.getText();
		InputTileGenerator tile5 = new InputTileGenerator("ROI y coord:",  String.valueOf(ssp.getLenPt()[1][1]), indicators);
		yCoord = tile5.getText();
		InputTileGenerator tile6 = new InputTileGenerator("y len:",  String.valueOf(ssp.getLenPt()[0][1]), indicators);
		yLen = tile6.getText();
		
		Label outputControlLabel = new Label(indicators, SWT.NULL);
		outputControlLabel.setText("Take Ouput Marker:");
		outputControlLabel.setVisible(false);
		
		outputControl = new Button (indicators, SWT.CHECK);
        outputControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        outputControl.setSelection(true);
        outputControl.setVisible(false);
        
        outputControlLabel.dispose();
        outputControl.dispose();
        
        Group buttons = new Group (mainImage,SWT.NONE);  
        GridLayout 	buttonsLayout = new GridLayout(2,true);
        buttons.setLayout(buttonsLayout);
		GridData buttonsData = new GridData(SWT.FILL, SWT.NULL, true, false);
		buttons.setLayoutData(buttonsData);
        
		go = new Button(buttons, SWT.PUSH | SWT.FILL);
		go.setText("Go");
		go.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        replay = new Button(buttons, SWT.PUSH | SWT.FILL);
		replay.setText("Replay");
		replay.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		replay.setEnabled(false);
        
		Group images = new Group(form, SWT.NONE);
        GridLayout imagesLayout = new GridLayout(1,true);
        images.setLayout(imagesLayout);
		GridData imagesData = new GridData(SWT.FILL, SWT.FILL, true, true);
		imagesData.grabExcessVerticalSpace = true;
      
		images.setLayoutData(imagesData);
		
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(images, null);
   
        plotSystem.createPlotPart(images, 
        						  "Raw Image", 
        						  actionBarComposite, 
        						  PlotType.IMAGE, 
        						  null);
        
        plotSystem.getPlotComposite().setLayoutData(imagesData);
        
        plotSystem.createPlot2D(nullImage, null, null);
        
        try {
			region =plotSystem.createRegion("myRegion", RegionType.BOX);
			bgRegion =plotSystem.createRegion("bgRegion", RegionType.BOX);
			secondBgRegion =plotSystem.createRegion("Background Region", RegionType.BOX);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		   
        Group centringButtons = new Group (images,SWT.NONE);  
        GridLayout 	centringButtonsLayout = new GridLayout(2,true);
        centringButtons.setLayout(centringButtonsLayout);
		GridData centringButtonsData = new GridData(SWT.FILL, SWT.NULL, true, false);
		centringButtons.setLayoutData(centringButtonsData);
        
		centreRegion = new Button(centringButtons, SWT.PUSH | SWT.FILL);
		centreRegion.setText("Centre Region");
		centreRegion.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		centreRegion.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetRegion(region);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
        centreSecondBgRegion = new Button(centringButtons, SWT.PUSH | SWT.FILL);
        centreSecondBgRegion.setText("Centre Background Region");
        centreSecondBgRegion.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        centreSecondBgRegion.setEnabled(false);
		
        centreSecondBgRegion.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetRegion(secondBgRegion);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
        folder = new TabFolder(form, SWT.BORDER | SWT.CLOSE);
    	folder.setLayout(new GridLayout());
    		
    	folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        	
		////////////////////////////////////////////////////////
		//		Tab 1 Setup
		//////////////////////////////////////////////////////////
        	
    	TabItem subI = new TabItem(folder, SWT.NONE);
    	subI.setText("Background Options");
    	subI.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
    		
    	Composite subIComposite = new Composite(folder, SWT.NONE | SWT.FILL);
    	subIComposite.setLayout(new GridLayout());
    	subIComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    		
    	subI.setControl(subIComposite);
    	
    	GridData ld2 = new GridData(SWT.FILL, SWT.FILL, true, true);
    	
    	customComposite1 = new PlotSystem1CompositeView(subIComposite,
    												    SWT.NONE,
    												    0, 
    												    0, 
    												    ssp,
    												    ssvs);
    		
    	customComposite1.setLayout(new GridLayout());
    	customComposite1.setLayoutData(ld2);
					
		plotSystem.addRegion(region);
		RectangularROI startROI = new RectangularROI(100,100,50,50,0);
		region.setROI(startROI);
		
		RectangularROI bgStartROI = new RectangularROI(90,90,70,70,0);
		bgRegion.setROI(bgStartROI);
		bgRegion.setRegionColor(gold);
		bgRegion.setUserRegion(false);
		bgRegion.setLineWidth(3);
		bgRegion.setMobile(false);
		plotSystem.addRegion(bgRegion);
		
		RectangularROI secondBgStartROI = new RectangularROI(10,10,20,20,0);
		secondBgRegion.setROI(secondBgStartROI);
		secondBgRegion.setRegionColor(transparent);
		secondBgRegion.setUserRegion(true);
		secondBgRegion.setLineWidth(3);
		secondBgRegion.setMobile(true);
		
		getSecondBgRegion().setVisible(false);
		secondBgRegion.setFill(false);
		plotSystem.addRegion(secondBgRegion);
		secondBgRegion.setVisible(false);
		
		////////////////////////////////////////////////////////
		//		Tab 2 Setup
		//////////////////////////////////////////////////////////

		correctionsTab = new TabItem(folder, SWT.NONE);
		correctionsTab.setText("Raw Output and Corrections");
		correctionsTab.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite correctionsTabComposite = new Composite(folder, SWT.NONE | SWT.FILL);
		correctionsTabComposite.setLayout(new GridLayout());
		correctionsTabComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
		correctionsTab.setControl(correctionsTabComposite);
		
		if (ssp.getCorrectionSelection() == MethodSetting.SXRD ){
			
			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
		    GridLayout 	correctionsLayout = new GridLayout(2,true);
		    corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);
			
			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:", String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Lorentz Correction:", String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			lorentz = subTile1.getText();
			InputTileGenerator subTile2 = new InputTileGenerator("Polarisation Correction:", String.valueOf(ssp.getCurrentPolarisationCorrection()), corrections);
			polarisation = subTile2.getText();
			InputTileGenerator subTile3 = new InputTileGenerator("Area Correction:", String.valueOf(ssp.getCurrentAreaCorrection()), corrections);
			setAreaCorrection(subTile3.getText());
			
		}
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction ){
			
			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
		    GridLayout 	correctionsLayout = new GridLayout(2,true);
		    corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);
			
			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:", String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Area Correction:", String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			reflectivityAreaCorr= subTile1.getText();
			InputTileGenerator subTile2 = new InputTileGenerator("Flux Correction:", String.valueOf(ssp.getCurrentPolarisationCorrection()), corrections);
			reflectivityFluxCorr = subTile2.getText();
		}
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction ){
			
			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
		    GridLayout 	correctionsLayout = new GridLayout(2,true);
		    corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);
			
			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:", String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Area Correction:", String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			reflectivityAreaCorr = subTile1.getText();
		}
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction){
			
			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
		    GridLayout 	correctionsLayout = new GridLayout(2,true);
		    corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);
			
			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:", String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
		}
		
		folder.pack();
		
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
			
			public void roiStandard(ROIEvent evt) {
				
				IRectangularROI greenRectangle = region.getROI().getBounds();
				int[] len = greenRectangle.getIntLengths();
				int[] pt = greenRectangle.getIntPoint();
				
				int[][] lenPt = { len, pt };
				
				RectangularROI[] bgRegionROI = ssp.trackingRegionOfInterestSetter(lenPt);
				
				int[] ly = bgRegionROI[1].getIntLengths();
				int[] py = bgRegionROI[1].getIntPoint();
				
				IRectangularROI yellowRectangle = bgRegion.getROI().getBounds();
				int[] ylen = yellowRectangle.getIntLengths();
				int[] ypt = yellowRectangle.getIntPoint();
				
				
				
//				RectangularROI bgROI = new RectangularROI(bgRegionROI[0],
//						  bgRegionROI[1],
//						  bgRegionROI[2],
//						  bgRegionROI[3],
//						  bgRegionROI[4]);
				
				if(!Arrays.equals(ypt, py) ||
						!Arrays.equals(ylen, ly)){
					
					bgRegion.setROI(bgRegionROI[1]);
					plotSystem.repaint();
					
				}
				
				else{
				}
				ssvs.updateDisplay();
			}
		});
        
		slider.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ssp.sliderMovemementMainImage(slider.getSelection());
					
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		processing = new Group (form,SWT.NONE);  
	    GridLayout processingLayout = new GridLayout(2,true);
	    processing.setLayout(processingLayout);
	    GridData processingData = new GridData(SWT.FILL, SWT.NULL, true, false);
	    processing.setLayoutData(processingData);

	    InputTileGenerator tile0 = new InputTileGenerator("Processing Method:", processing);
	    processingMode = tile0.getCombo();

	    for(ProccessingMethod  i: ProccessingMethod.values()){
	    	processingMode.add(ProccessingMethod.toString(i));
	    }
	    
	    manualControls = new Group(processing,SWT.NONE);
	   
	    
	    changeProcessingMode();
	   
	    addChangeProcessingMethodListeners();

		form.setWeights(new int[]{19, 45, 29, 7});
    }
	
    
    public void addChangeProcessingMethodListeners(){
    	
	    processingMode.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ssp.setProcessingMethodSelection(ProccessingMethod.toMethodology(processingMode.getSelectionIndex()));
				changeProcessingMode();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	}
    
    public void changeProcessingMode(){
    	
    	for(Control i : manualControls.getChildren()){
    		i.dispose();
    	}
    	
    	if(ssp.getProcessingMethodSelection() == ProccessingMethod.AUTOMATIC){

    	    processingMode.select(0);
    	    
    	    GridLayout manualControlsLayout = new GridLayout(1,true);
    	    manualControls .setLayout(manualControlsLayout);
    	    GridData manualControlsData = new GridData(SWT.FILL, SWT.NULL, true, false);
    	    manualControls .setLayoutData(manualControlsData);
    	    
    		run = new Button (manualControls, SWT.PUSH);	
    		run.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    		run.setText("Run");
    		addRunListener();
    		
    	}
    	
    	if(ssp.getProcessingMethodSelection() == ProccessingMethod.MANUAL){

    	    GridLayout manualControlsLayout = new GridLayout(4,true);
    	    manualControls .setLayout(manualControlsLayout);
    	    GridData manualControlsData = new GridData(SWT.FILL, SWT.NULL, true, false);
    	    manualControls .setLayoutData(manualControlsData);
    	    
    	    decrement = new Button (manualControls, SWT.PUSH);	
    	    decrement.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	    decrement.setText("<");
    		

    		acceptBack = new Button (manualControls, SWT.PUSH);	
    		acceptBack.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    		acceptBack.setText("< Accept");
    	    
    		accept = new Button (manualControls, SWT.PUSH);	
    		accept.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    		accept.setText("Accept >");
    		
    		increment = new Button (manualControls, SWT.PUSH);	
    	    increment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	    increment.setText(">");
    	    
    	    addManualListeners();
    	
    	}
    	
    	form.setWeights(new int[]{19, 45, 29, 7});
    	 
    	manualControls.layout(true, true);
    	manualControls.redraw();
    }
    
    
    public void addRunListener(){
    	
    	run.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ssvs.fireRun();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
    }
   
    public void addManualListeners(){
    	
    	acceptBack.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ssvs.fireAccept();
				ssp.setSliderPos(slider.getSelection() -1);
				generalUpdate();
				ssvs.sliderMovementGeneralUpdate();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	accept.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ssvs.fireAccept();
				ssp.setSliderPos(slider.getSelection() +1);
				generalUpdate();
				ssvs.sliderMovementGeneralUpdate();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	increment.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				slider.setSelection(slider.getSelection() +1);
				ssp.setSliderPos(slider.getSelection());
				generalUpdate();
				ssvs.sliderMovementGeneralUpdate();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	decrement.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				slider.setSelection(slider.getSelection() -1);
				ssp.setSliderPos(slider.getSelection());
				generalUpdate();
				
				ssvs.sliderMovementGeneralUpdate();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    
    public Combo getProcessingMode() {
		return processingMode;
	}

	public void setProcessingMode(Combo processingMode) {
		this.processingMode = processingMode;
	}

	public int getSliderPos(){
	   int sliderPos = slider.getSelection();
	   return sliderPos;
   }
      
   public Composite getComposite(){
   	
   	return this;
   }
   
   public IPlottingSystem<Composite> getPlotSystem(){
	   return plotSystem;
   }

   public IDataset getImage(){
	   return image;
   }
   
   public PlotSystem1CompositeView getPlotSystem1CompositeView(){
	   return customComposite1;
   }
   
	public Button getOutputControl(){
		return outputControl;
		
	}

	public Slider getSlider(){
		return slider;
	}

	public void getBoxPosition(){
		
		IRectangularROI greenRectangle = region.getROI().getBounds();
		int[] len = greenRectangle.getIntLengths();
		int[] pt = greenRectangle.getIntPoint();
		
		int[][] lenPt = { len, pt };
		
		double[] bgRegionROI = ssp.regionOfInterestSetter1(lenPt);
		
//		RectangularROI bgROI = new RectangularROI(bgRegionROI[0],
//				  bgRegionROI[1],
//				  bgRegionROI[2],
//				  bgRegionROI[3],
//				  bgRegionROI[4]);
//		
//		
//		
//		ssp.regionOfInterestSetter(region.getROI());	
	}
	
	public IPlottingSystem<Composite> getSubImagePlotSystem(){
		return customComposite1.getPlotSystem(); 
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
	
	public IRegion getBgRegion(){
		return bgRegion;
	}
	
	public IRegion getSecondBgRegion(){
		return secondBgRegion;
	}
	
	public void setRegion(int[][] lenpt){
		
		boolean areLengthsEqual = false;
		boolean arePtsEqual = false;
		
		int[] lengths =region.getROI().getBounds().getIntLengths();
		int[] points =region.getROI().getBounds().getIntPoint();
		
		if(lengths[0] == (lenpt[0][0]) && lengths[1] == (lenpt[0][1])){
			areLengthsEqual = true;
		}
		
		if(points[0] == (lenpt[1][0]) && points[1] == (lenpt[1][1])){
			arePtsEqual = true;
		}
		
		if(areLengthsEqual == false || arePtsEqual ==false){
			RectangularROI newROI = new RectangularROI(lenpt[1][0],
												   lenpt[1][1],
												   lenpt[0][0],
												   lenpt[0][1],
												   0);
			if(Arrays.equals(region.getROI().getBounds().getIntLengths(), lenpt[0]) &&
					Arrays.equals(region.getROI().getBounds().getIntPoint(), lenpt[1])){
				
				
				
			}
			else{
				region.setROI(newROI);
			}
		}
	}

	public IRegion getGreenRegion(){
		return region;
	}
	
	public void resetRegion(IRegion re){
		
		int[] ad = ssp.getImage(ssp.getSliderPos()).getShape();
		
		RectangularROI newROI = new RectangularROI((int) Math.round(ad[1]*3/8),(int) Math.round(ad[0]*3/8),(int) Math.round(ad[1]*0.25),(int) Math.round(ad[0]*0.25),0);
		re.setROI(newROI);
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
	
	public void resetCorrectionsTab(){
		
		correctionsTab.dispose();
		
		
		correctionsTab = new TabItem(folder, SWT.NONE);
		correctionsTab.setText("Raw Output and Corrections");
		correctionsTab.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite correctionsTabComposite = new Composite(folder, SWT.NONE | SWT.FILL);
		correctionsTabComposite.setLayout(new GridLayout());
		correctionsTabComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
		correctionsTab.setControl(correctionsTabComposite);
		
		if (ssp.getCorrectionSelection() == MethodSetting.SXRD ){
			
			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
		    GridLayout 	correctionsLayout = new GridLayout(2,true);
		    corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);
			
			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:", String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Lorentz Correction:", String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			lorentz = subTile1.getText();
			InputTileGenerator subTile2 = new InputTileGenerator("Polarisation Correction:", String.valueOf(ssp.getCurrentPolarisationCorrection()), corrections);
			polarisation = subTile2.getText();
			InputTileGenerator subTile3 = new InputTileGenerator("Area Correction:", String.valueOf(ssp.getCurrentAreaCorrection()), corrections);
			setAreaCorrection(subTile3.getText());
			
		}
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction ){
			
			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
		    GridLayout 	correctionsLayout = new GridLayout(2,true);
		    corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);
			
			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:", String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Area Correction:", String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			reflectivityAreaCorr= subTile1.getText();
			InputTileGenerator subTile2 = new InputTileGenerator("Flux Correction:", String.valueOf(ssp.getCurrentPolarisationCorrection()), corrections);
			reflectivityFluxCorr = subTile2.getText();
		}
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction ){
			
			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
		    GridLayout 	correctionsLayout = new GridLayout(2,true);
		    corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);
			
			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:", String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Area Correction:", String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			reflectivityAreaCorr = subTile1.getText();
		}
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction){
			
			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
		    GridLayout 	correctionsLayout = new GridLayout(2,true);
		    corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);
			
			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:", String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
		}
		
		folder.pack();	
		form.setWeights(new int[]{19, 45, 29, 7});
		
	}
	
	
	
	public void setROITexts(int[][] LenPt){
		
		String[] values = new String[4];
		
		values[0] = String.valueOf(LenPt[1][0]);
		values[1] = String.valueOf(LenPt[0][0]);
		values[2] = String.valueOf(LenPt[1][1]);
		values[3] = String.valueOf(LenPt[0][1]);
		
		setROITexts(values);
				
	}
	
	public void generalCorrectionsUpdate(){
		
		if (ssp.getCorrectionSelection() == MethodSetting.SXRD){
				
			lorentz.setText(String.valueOf(ssp.getCurrentLorentzCorrection()));
			polarisation.setText(String.valueOf(ssp.getCurrentPolarisationCorrection()));
			
			double raw = ssp.getCurrentRawIntensity();
			rawIntensity.setText(String.valueOf(raw));
			areaCorrection.setText(String.valueOf(ssp.getCurrentAreaCorrection()));
			
		}
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction){
			
			rawIntensity.setText(String.valueOf(ssp.getCurrentRawIntensity()));
			
			double f = ssp.getCurrentReflectivityFluxCorrection();
			
			String fluxCorr = String.valueOf(f);
			
			reflectivityFluxCorr.setText(fluxCorr);
			reflectivityAreaCorr.setText(String.valueOf(ssp.getCurrentReflectivityAreaCorrection()));
			
		}
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction){
			
			rawIntensity.setText(String.valueOf(ssp.getCurrentRawIntensity()));
			reflectivityAreaCorr.setText(String.valueOf(ssp.getCurrentReflectivityAreaCorrection()));
			
		}
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction){
			
			rawIntensity.setText(String.valueOf(ssp.getCurrentRawIntensity()));
		}
		
		this.update();
		
	}

	public void generalCorrectionsSet(double lorentzCorrection,
									  double polarisationCorrection,
									  double rawIntensityValue,
									  double areaCorrectionValue){
		
		lorentz.setText(String.valueOf(lorentzCorrection));
		polarisation.setText(String.valueOf(polarisationCorrection));
		rawIntensity.setText(String.valueOf(rawIntensityValue));
		areaCorrection.setText(String.valueOf(areaCorrectionValue));
	
	}

	public void generalReflectivityCorrectionsSet(double areaCorrection,
									  double rawIntensityValue,
									  double fluxCorrection){
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction){
			
			rawIntensity.setText(String.valueOf(rawIntensityValue));
			reflectivityFluxCorr.setText(String.valueOf(fluxCorrection));
			reflectivityAreaCorr.setText(String.valueOf(areaCorrection));
			
		}
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction){
			
			rawIntensity.setText(String.valueOf(rawIntensityValue));
			reflectivityAreaCorr.setText(String.valueOf(areaCorrection));
			
		}
		
		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction){
			
			rawIntensity.setText(String.valueOf(rawIntensityValue));
		}
		
		this.update();

	}

	public void generalUpdate(){
		
		plotSystem.updatePlot2D(ssp.getImage(ssp.getSliderPos()), null, null);
		setRegion(ssp.getLenPt());
		plotSystem.repaint();
		generalCorrectionsUpdate();
		
	}
	
	public TabItem getBackgroundSubtractedSubImage(){
		return subBgI;
	}
	
	public void appendBackgroundSubtractedSubImage(){
		
		try{
			TabItem m = folder.getItem(2);
			m.dispose();
			folder.pack();
		}
		catch(Exception f){
			
		}
		
		subBgI = new TabItem(folder, SWT.NONE);
		subBgI.setText("Bg Subtracted Image");
		subBgI.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite subBgIComposite = new Composite(folder, SWT.NONE | SWT.FILL);
		subBgIComposite.setLayout(new GridLayout());
		subBgIComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
		subBgI.setControl(subBgIComposite);
		
		try {
			subImageBgPlotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		subImageBgPlotSystem.createPlotPart(subBgIComposite, "Region of interest", null, PlotType.IMAGE, null);
		subImageBgPlotSystem.getPlotComposite().setLayoutData( new GridData(GridData.FILL_BOTH));
		subImageBgPlotSystem.createPlot2D(nullImage, null, null);	
		
		folder.pack();		
		
	}
	
	public void removeBackgroundSubtractedSubImage(){
		
		try{
			TabItem m = folder.getItem(2);
			m.dispose();
		}
		catch(Exception o){
			
		}
		folder.pack();
		subBgI = null;
		form.setWeights(new int[]{19, 45, 29, 7});
		folder.setSelection(0);
	}	
	
	public Button getGo(){
		return go;
	}
	
	public Button getCentreSecondBgRegion(){
		return centreSecondBgRegion;
	}
	
	public SashForm getSash(){
		return form;
	}

	public Text getAreaCorrection() {
		return areaCorrection;
	}

	public void setAreaCorrection(Text areaCorrection) {
		this.areaCorrection = areaCorrection;
	}
	
	public Text getLorentz() {
		return lorentz;
	}

	public void setLorentz(Text lorentz) {
		this.lorentz = lorentz;
	}

	public Text getPolarisation() {
		return polarisation;
	}

	public void setPolarisation(Text polarisation) {
		this.polarisation = polarisation;
	}

	public Text getRawIntensity() {
		return rawIntensity;
	}

	public void setRawIntensity(Text rawIntensity) {
		this.rawIntensity = rawIntensity;
	}

	public Button getIncrement() {
		return increment;
	}

	public void setIncrement(Button increment) {
		this.increment = increment;
	}

	public Button getDecrement() {
		return decrement;
	}

	public void setDecrement(Button decrement) {
		this.decrement = decrement;
	}

	public Button getAccept() {
		return accept;
	}

	public void setAccept(Button accept) {
		this.accept = accept;
	}

	public Button getAcceptBack() {
		return acceptBack;
	}

	public void setAcceptBack(Button acceptBack) {
		this.acceptBack = acceptBack;
	}

}
    




