
package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.AnalaysisMethodologies;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.FitPower;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.TrackingMethodology;
import org.dawnsci.surfacescatter.TrackingMethodology.TrackerType1;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;

public class PlotSystem1CompositeView extends Composite {

    private IPlottingSystem<Composite> plotSystem1;
    private IDataset image1;
    private Button button; 
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
	private Button trackerOnButton;
    private Combo comboDropDown0;
	private Combo comboDropDown1;
	private Combo comboDropDown2;
    private Spinner boundaryBoxText;
    private SurfaceScatterPresenter ssp;
    private Boolean trackerOn =false;
    private SurfaceScatterViewStart ssvs;
	private Button useQAxis;
    
	
  
    public PlotSystem1CompositeView(Composite parent, 
    		int style, 
    		int trackingMarker, 
    		int extra,
    		SurfaceScatterPresenter ssp,
    		SurfaceScatterViewStart ssvs){
    	
        super(parent, style);
        this.ssp = ssp;
        this.ssvs = ssvs;

        try {
			plotSystem1 = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			
		}

        this.createContents(trackingMarker); 
        
    }
     
    public void createContents(
    						   int trackingMarker) {
        
    	try{
    		trackerOn = ssp.getTrackerOn();
    	}
    	catch(NullPointerException r){
    		trackerOn = false;
    	}
    	trackerOnButton = new Button (this, SWT.PUSH);
    	trackerOnButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	
    	
    	
    	if(ssp.getTrackerOn()){
    		trackerOnButton.setText("Tracker On");
    	}
    	else{
    		trackerOnButton.setText("Tracker Off");
    	}
    		
    	Group methodSetting = new Group(this, SWT.FILL | SWT.FILL);
        GridLayout methodSettingLayout = new GridLayout(2, true);
	    GridData methodSettingData = new GridData(GridData.FILL_HORIZONTAL);
	    methodSetting.setLayout(methodSettingLayout);
	    methodSetting.setLayoutData(methodSettingData);
	    
	    this.setLayout(methodSettingLayout);
    	this.setLayoutData(methodSettingData);
	   
	    String[] setup = ssp.getAnalysisSetup(0);
	        
	    InputTileGenerator tile0 = new InputTileGenerator("Background:", methodSetting);
	    comboDropDown0 = tile0.getCombo();
	    comboDropDown0.setText(setup[0]); 
	    
	    InputTileGenerator tile1 = new InputTileGenerator("Polynomial Power:", methodSetting);
	    comboDropDown1 = tile1.getCombo();
	   	comboDropDown1.setText(setup[1]);
	   	
	    InputTileGenerator tile2 = new InputTileGenerator("Tracking Method:", methodSetting);
	   	comboDropDown2 = tile2.getCombo();
	   	comboDropDown2.setText(setup[2]);
	   	
	   	InputTileGenerator tile3 = new InputTileGenerator("Boundary Box:", methodSetting, 0, 1000, 1);   
	    boundaryBoxText = tile3.getSpinner();
	    
	    
	    for(Methodology  t: AnalaysisMethodologies.Methodology.values()){
	    	comboDropDown0.add(AnalaysisMethodologies.toString(t));
	    }
	    
	    for(FitPower  i: AnalaysisMethodologies.FitPower.values()){
	    	comboDropDown1.add(AnalaysisMethodologies.toString(i));
	    }
	    
	    for(TrackerType1  i: TrackingMethodology.TrackerType1.values()){
	    	comboDropDown2.add(TrackingMethodology.toString(i));
	    }
	    
	    comboDropDown0.addSelectionListener(new SelectionListener() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	          generalUpdate();
	        }
	    	@Override
	        public void widgetDefaultSelected(SelectionEvent e) {
	          
	        }

	    });
	    
	    comboDropDown1.addSelectionListener(new SelectionListener() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		generalUpdate();
	        }
	    	@Override
	        public void widgetDefaultSelected(SelectionEvent e) {
	          
	        }

	     });
	    
	    comboDropDown2.addSelectionListener(new SelectionListener() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		generalUpdate();
	        }
	    	@Override
	        public void widgetDefaultSelected(SelectionEvent e) {
	          
	        }

	      });
	    
	    boundaryBoxText.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				generalUpdate();
			}
	    	
	    });
	    
	    trackerOnButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(trackerOn){
					trackerOn = false;
				}
				
				else{
					trackerOn= true;
				}
				
				if(trackerOn){
					trackerOnButton.setText("Tracker On");
					if((ssp.getTrackerType() == TrackerType1.INTERPOLATION ||
							ssp.getTrackerType() == TrackerType1.SPLINE_INTERPOLATION)){
						button4.setEnabled(true);
						button5.setEnabled(true);
					}
				}
				
				else{
					trackerOnButton.setText("Tracker Off");
					button4.setEnabled(false);
					button5.setEnabled(false);
				}
				
				ssp.setTrackerOn(trackerOn);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub	
			}
		});

        button2 = new Button (methodSetting, SWT.PUSH);
        button2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button3 = new Button (methodSetting, SWT.PUSH);
        button3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
       
        button2.setText("Save Parameters");
        button3.setText("Load Parameters");
        button2.setData(new GridData(SWT.FILL));
        button3.setData(new GridData(SWT.FILL));
        
        button4 = new Button(methodSetting, SWT.PUSH);
        button4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button4.setData(new GridData(SWT.FILL));
        button4.setText("Accept Position");
        button4.setEnabled(false);
        
        button5 = new Button(methodSetting, SWT.PUSH);
        button5.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button5.setData(new GridData(SWT.FILL));
        button5.setText("Reject Position");
        button5.setEnabled(false);
        

        useQAxis= new Button(this, SWT.CHECK);
        useQAxis.setText("use q Axis");
        useQAxis.setEnabled(false);
        useQAxis.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        useQAxis.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ssp.setTrackWithQ(useQAxis.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
        
        
        button4.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ssvs.interpolationTrackerBoxesAccept();
		
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
        
        
        button5.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
			
				
				double u =(double) ssp.getSliderPos();
				
				if(ssp.getInterpolatorBoxes() != null){
					for(int j = 0; j<ssp.getInterpolatorBoxes().size(); j++){
						if(ssp.getInterpolatorBoxes().get(j)[2][0] == u){
							ssp.getInterpolatorBoxes().remove(j);
							ssvs.getPlotSystemCompositeView().getPlotSystem().removeRegion(ssp.getInterpolatorRegions().get(j));
							ssp.getInterpolatorRegions().remove(j);
							ssvs.getPlotSystemCompositeView().getPlotSystem().repaint(false);
						}
					}
				}
				
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
        
    }
    
   public Composite getComposite(){   	
	   return this;
   }
   
   public IPlottingSystem<Composite> getPlotSystem(){
	   return plotSystem1;
   }

   public IDataset getImage(){
	   return image1;
   }
   
   public int[] getMethodology(){
	   
	   int[] returns = new int[3]; 
	   
	   Display.getDefault().syncExec(new Runnable() {
		      public void run() {
		    	  returns[0] = comboDropDown0.getSelectionIndex();
				   returns[1] = comboDropDown1.getSelectionIndex();
				   try{
				   returns[2] = Integer.parseInt(boundaryBoxText.getText());
				   }
				   catch(Exception e){
					returns[2] = 10;
				   }
		      }
		    });
		   
	   
	   return returns;
   }
   
   public void generalUpdate(){
	   
	   int methodologySelection = comboDropDown0.getSelectionIndex();
	   int fitPowerSelection = comboDropDown1.getSelectionIndex();
	   int trackerSelection = comboDropDown2.getSelectionIndex();
       
	   String boundaryBox = String.valueOf(boundaryBoxText.getSelection());
	   
       ssvs.updateAnalysisMethodology(methodologySelection, 
     		  						 fitPowerSelection, 
     		  						 trackerSelection, 
     		  						 boundaryBox);
       
       ssp.backgroundBoxesManager(ssvs.getPlotSystemCompositeView().getBgRegion(),
    		   					  ssvs.getPlotSystemCompositeView().getSecondBgRegion(),
    		   					  ssvs.getPlotSystemCompositeView().getCentreSecondBgRegion());
       
       
       IRectangularROI greenRectangle = ssvs.getPlotSystemCompositeView().getPlotSystem().
   			getRegion("myRegion").getROI().getBounds();
		int[] len = greenRectangle.getIntLengths();
		int[] pt = greenRectangle.getIntPoint();
		
		int[][] lenPt = { len, pt };
		
		RectangularROI[] bgRegionROI = ssp.trackingRegionOfInterestSetter(lenPt);
       
       ssvs.getPlotSystemCompositeView().getBgRegion().setROI(bgRegionROI[1]);

       
       if(ssp.getDrm().getFms().get(ssp.getSliderPos()).getTrackingMethodology() != TrackerType1.INTERPOLATION ||
    		   ssp.getDrm().getFms().get(ssp.getSliderPos()).getTrackingMethodology() != TrackerType1.SPLINE_INTERPOLATION){
    	   
    	   button4.setEnabled(false);
    	   button5.setEnabled(false);
    	   
    	   try{
    		   IRegion u = ssvs.getPlotSystemCompositeView().getPlotSystem().getRegion("Interpolated trajectory");
    		   ssvs.getPlotSystemCompositeView().getPlotSystem().removeRegion(u);    	   
    	   }
    	   catch(Exception k){
    		   
    	   }
       } 
       
       
       if((TrackingMethodology.intToTracker1(trackerSelection) == TrackerType1.INTERPOLATION 
				|| TrackingMethodology.intToTracker1(trackerSelection) == TrackerType1.SPLINE_INTERPOLATION)
						&& ssp.getTrackerOn()){
	
    	   button4.setEnabled(true);
    	   button5.setEnabled(true);
		}
      
   }
   
   public Combo[] getCombos(){
	   return new Combo[] {comboDropDown0, comboDropDown1, comboDropDown2};
   }
   
   public void setMethodologyDropDown(String in){
	   Methodology in1 = AnalaysisMethodologies.toMethodology(in);
	   setMethodologyDropDown(in1);
   }
   
   public void setMethodologyDropDown(Methodology in){
	   for (int i =0 ; i<AnalaysisMethodologies.Methodology.values().length; i++){
			  if ( in == AnalaysisMethodologies.Methodology.values()[i]){
				  comboDropDown0.select(i);
			  }
		  }
	   }
	   

   public void setFitPowerDropDown(FitPower m){
		  for (int i =0 ; i<AnalaysisMethodologies.FitPower.values().length; i++){
			  if ( m == AnalaysisMethodologies.FitPower.values()[i]){
				  comboDropDown1.select(i);
			  }
		  }
   }
   
   public void setFitPowerDropDown(int in){
	   FitPower in1 = AnalaysisMethodologies.toFitPower(in);
	   setFitPowerDropDown(in1);
   }
   
   
   public void setFitPowerDropDown1(String in){
		  FitPower m = AnalaysisMethodologies.toFitPower(Integer.parseInt(in));
		  setFitPowerDropDown(m);
	}
      
	   
   public void setTrackerTypeDropDown(TrackerType1 m){
		  for (int i =0 ; i<TrackingMethodology.TrackerType1.values().length; i++){
			  if ( m == TrackingMethodology.TrackerType1.values()[i]){
				  comboDropDown2.select(i);
			  }
		  }
   }
   
   public TrackerType1 getTrackerTypeDropDown(){
	   return TrackingMethodology.toTracker1(comboDropDown2.getText());
   }
   
   public void setTrackerTypeDropDown(String in){
	   TrackerType1 m = TrackingMethodology.toTracker1(in);
	   setTrackerTypeDropDown(m);
   }
   
   public void setBoundaryBox (int in){
	   boundaryBoxText.setSelection(in);
   }
   

   public void setBoundaryBox (String in){
	   boundaryBoxText.setSelection(Integer.valueOf(in));
   }
   
   public Button getRunButton(){
	   return button1;
   }
   
   public Button getSaveButton(){
	   return button2;
   }
   
   public Button getLoadButton(){
	   return button3;
   }

   public Button getProceedButton(){
		return button;
   }
	
   public Button getTrackerOnButton(){
	   return trackerOnButton;
   }
   
   public void checkTrackerOnButton(){
	 
	   if(ssp.getTrackerOn() ==false){
		   trackerOnButton.setText("Tracker Off");
		   button4.setEnabled(false);
		   button5.setEnabled(false);
	   }
	   else{
		   trackerOnButton.setText("Tracker On");
	    }
	    
   }
	   
	public Button getAcceptLocation() {
		return button4;
	}
	
	public void setAcceptLocation(Button button4) {
		this.button4 = button4;
	}
	public Button getRejectLocation() {
		return button5;
	}
	
	public void setRejectLocation(Button button5) {
		this.button5 = button5;
	}
	

	public Button getUseQAxis() {
		return useQAxis;
	}

	public void setUseQAxis(Button useQAxis) {
		this.useQAxis = useQAxis;
	}


class operationJob extends Job {

	private IDataset input;
	

	public operationJob() {
		super("updating image...");
	}

	public void setData(IDataset input) {
		this.input = input;
	}
	
	protected IStatus run(IProgressMonitor monitor) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
			plotSystem1.clear();
			plotSystem1.updatePlot2D(input, null, monitor);
    		plotSystem1.repaint(true);
			}
    	
		});	
	
		return Status.OK_STATUS;
	}
   }
}
