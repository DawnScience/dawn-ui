
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
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PlotSystem1CompositeView extends Composite {

//	private final static Logger logger = LoggerFactory.getLogger(PlotSystem1Composite.class);

    private IPlottingSystem<Composite> plotSystem1;
    private IDataset image1;
    private Button button; 
    private Button button1;
    private Button button2;
    private Button button3;
    private Button trackerOnButton;
    private Combo comboDropDown0;
	private Combo comboDropDown1;
	private Combo comboDropDown2;
    private Text boundaryBoxText;
//    private String[] methodologies;
    private int extra;
    private SurfaceScatterPresenter ssp;
//    private RegionSetterZoomedView rszv;
    private Boolean trackerOn;
	
  
    public PlotSystem1CompositeView(Composite parent, 
    		int style,
//    		PlotSystemCompositeView customComposite, 
    		int trackingMarker, 
    		int extra,
    		SurfaceScatterPresenter ssp){
//    		RegionSetterZoomedView rszv) {
    	
        super(parent, style);

        this.extra= extra;
        this.ssp = ssp;
//        this.rszv = rszv;
        
       
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
	    methodSettingData .minimumWidth = 50;

	    methodSetting.setLayout(methodSettingLayout);
	    methodSetting.setLayoutData(methodSettingData);
	    
	    this.setLayout(methodSettingLayout);
    	this.setLayoutData(methodSettingData);
	    
	    
	    String[] setup = ssp.getAnalysisSetup(0);
	    
	    Label bgMethod = new Label(methodSetting, SWT.FILL);
	    bgMethod.setText("Background Method:");
	    Label polynomialPower = new Label(methodSetting, SWT.FILL);
	    polynomialPower.setText("Polynomial Power:");
	    
	    comboDropDown0 = new Combo(methodSetting, SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
	    comboDropDown0.setText(setup[0]); 
	    
	   	comboDropDown1 = new Combo(methodSetting, SWT.DROP_DOWN | SWT.BORDER | SWT.RIGHT);
	   	comboDropDown1.setText(setup[1]);
	   
	    Label trMethod = new Label(methodSetting, SWT.FILL);
	    trMethod.setText("Tracking Method:");
	    Label bBox = new Label(methodSetting, SWT.FILL);
	    bBox.setText("Boundary Box:");
	   	
	   	comboDropDown2 = new Combo(methodSetting, SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
	   	comboDropDown2.setText(setup[2]);
	    boundaryBoxText = new Text(methodSetting, SWT.SINGLE);
	    boundaryBoxText.setText("000" + setup[3]);
	    comboDropDown0.setData(new GridData(SWT.FILL));
	    comboDropDown1.setData(new GridData(SWT.FILL));
	    comboDropDown2.setData(new GridData(SWT.FILL));
	    boundaryBoxText.setData(new GridData(SWT.FILL));
	    
	    for(Methodology  t: AnalaysisMethodologies.Methodology.values()){
	    	comboDropDown0.add(AnalaysisMethodologies.toString(t));
	    }
	    
	    for(FitPower  i: AnalaysisMethodologies.FitPower.values()){
	    	comboDropDown1.add(String.valueOf(AnalaysisMethodologies.toInt(i)));
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
				}
				else{
					trackerOnButton.setText("Tracker Off");
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
       
	   String boundaryBox = boundaryBoxText.getText();
       ssp.updateAnalysisMethodology(methodologySelection, 
     		  						 fitPowerSelection, 
     		  						 trackerSelection, 
     		  						 boundaryBox);
       
       ssp.backgroundBoxesManager();
       
       ssp.regionOfInterestSetter();
      
      
//       rszv.dummyProcessTrigger();
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
	   boundaryBoxText.setText(String.valueOf(in));
   }
   

   public void setBoundaryBox (String in){
	   boundaryBoxText.setText(in);
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
