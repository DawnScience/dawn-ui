package org.dawnsci.surfacescatter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.CsdpFromNexusFile;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.ReviewCurvesModel;
import org.dawnsci.surfacescatter.SXRDNexusReader;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;

public class ReviewTabComposite extends Composite{

    private Group methodSetting;
    private SashForm form;
    private Button clearCurves;
    private Button addCurve;
    private Button showErrors;
    private Button switchAxes;
    private IPlottingSystem<Composite> plotSystem;
    private ReviewCurvesModel rcm;
	private String nexusFolderPath;
	
	public ReviewTabComposite(Composite parent, 
							  int style) throws Exception {
        super(parent, style);

        try {
        	plotSystem = PlottingFactory.createPlottingSystem();
			
        } 
        catch (Exception e2) {
			e2.printStackTrace();
		}
        setRcm(new ReviewCurvesModel());
        this.createContents();  
    }
	
	public void createContents() throws Exception {
		
		form = new SashForm(this, SWT.VERTICAL);
		form.setLayout(new GridLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		methodSetting = new Group(form, SWT.FILL | SWT.FILL);
        GridLayout methodSettingLayout = new GridLayout(2, true);
	    GridData methodSettingData = new GridData(GridData.FILL_HORIZONTAL);
	    methodSetting.setLayout(methodSettingLayout);
	    methodSetting.setLayoutData(methodSettingData);
		
	    Group storedCurves = new Group(form, SWT.NONE);
        GridLayout storedCurvesLayout = new GridLayout();
        storedCurves.setLayout(storedCurvesLayout);
        
        final GridData storedCurvesData = new GridData(SWT.FILL, SWT.FILL, true, true);
        storedCurvesData.grabExcessVerticalSpace = true;
        storedCurvesData.heightHint = 100;
        storedCurves.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
	    
	    try {
			plotSystem = PlottingFactory.createPlottingSystem();
				
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	        
		ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(storedCurves, 
																				null);
		  
	    plotSystem.createPlotPart(storedCurves, 
	        					  "Stored Curves", 
	        					  actionBarComposite, 
	        					  PlotType.IMAGE, 
	        					  null);
		
	    
	    plotSystem.getPlotComposite().setLayoutData(storedCurvesData);
	    
	    clearCurves = new Button (methodSetting, SWT.PUSH);
	    clearCurves.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    clearCurves.setText("Clear Curves");
	    clearCurves.setData(new GridData(SWT.FILL));
	    
	    clearCurves.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				plotSystem.clear();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    
	    addCurve = new Button (methodSetting, SWT.PUSH);
        addCurve.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addCurve.setText("Add Curve");
        addCurve.setData(new GridData(SWT.FILL));
        
		addCurve.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				FileDialog dlg = new FileDialog(ReviewTabComposite.this.getShell(), SWT.OPEN);
				
				if(nexusFolderPath != null){
					dlg.setFilterPath(nexusFolderPath);
				}

		        dlg.setText("Find a Nexus File!");

		        String stitle = "r";
				String path = "p";

				if (dlg.open() != null) {
					stitle = dlg.getFileName();
					path = dlg.getFilterPath();

				}
				
				String title = path + File.separator + stitle;

				CurveStitchDataPackage newCsdp = CsdpFromNexusFile.CsdpFromNexusFileGenerator(title);
				
				rcm.addToCsdpList(newCsdp);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
        
        
        
        showErrors = new Button(methodSetting, SWT.PUSH);
        showErrors.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        showErrors.setData(new GridData(SWT.FILL));
        showErrors.setText("Show Errors");
        
        switchAxes = new Button(methodSetting, SWT.PUSH);
        switchAxes.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        switchAxes.setData(new GridData(SWT.FILL));
        switchAxes.setText("Change Axes");
        
	    
	    form.setWeights(new int[] {10, 80});
		
	    rcm.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// TODO Auto-generated method stub
				
				CurveStitchDataPackage csdpLatest1 = rcm.getCsdpLatest();
								
				ILineTrace lt = plotSystem.createLineTrace(csdpLatest1.getName());
				
				IDataset[] ltData = new IDataset[] {csdpLatest1.getSplicedCurveX(), 
						                            csdpLatest1.getSplicedCurveYFhkl()};
				
				
				lt.setData(ltData[0], ltData[1]);		
				
				plotSystem.addTrace(lt);
				
				try{
					ITrace t = plotSystem.getTrace("Blank Curve");
					plotSystem.removeTrace(t);
				}
				catch(Exception n){
					
				}
				
				plotSystem.autoscaleAxes();
				
			}
		});
	    
	}
	   
	public Composite getComposite(){ 
		
	   	return this;
	}
	   
	public Group getMethodSetting() {
		return methodSetting;
	}

	public void setMethodSetting(Group methodSetting) {
		this.methodSetting = methodSetting;
	}

	public SashForm getForm() {
		return form;
	}

	public void setForm(SashForm form) {
		this.form = form;
	}

	public Button getClearCurves() {
		return clearCurves;
	}

	public void setClearCurves(Button clearCurves) {
		this.clearCurves = clearCurves;
	}

	public Button getAddCurve() {
		return addCurve;
	}

	public void setAddCurve(Button addCurve) {
		this.addCurve = addCurve;
	}

	public Button getShowErrors() {
		return showErrors;
	}

	public void setShowErrors(Button showErrors) {
		this.showErrors = showErrors;
	}

	public Button getSwitchAxes() {
		return switchAxes;
	}

	public void setSwitchAxes(Button switchAxes) {
		this.switchAxes = switchAxes;
	}

	public IPlottingSystem<Composite> getPlotSystem() {
		return plotSystem;
	}

	public void setPlotSystem(IPlottingSystem<Composite> plotSystem) {
		this.plotSystem = plotSystem;
	}

	public ReviewCurvesModel getRcm() {
		return rcm;
	}

	public void setRcm(ReviewCurvesModel rcm) {
		this.rcm = rcm;
	}
	
	public String getNexusFolderPath() {
		return nexusFolderPath;
	}

	public void setNexusFolderPath(String nexusFolderPath) {
		this.nexusFolderPath = nexusFolderPath;
	}

	   
}
		
		
		
		

