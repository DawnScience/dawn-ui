package org.dawnsci.surfacescatter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.AxisEnums;
import org.dawnsci.surfacescatter.AxisEnums.xAxes;
import org.dawnsci.surfacescatter.AxisEnums.yAxes;
import org.dawnsci.surfacescatter.CsdpFromNexusFile;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.ReviewCurvesModel;
import org.dawnsci.surfacescatter.SXRDNexusReader;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
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
	private boolean errorDisplayFlag = true;
	private Combo xAxis;
	private Combo yAxis;
	private AxisEnums.xAxes xAxisSelection = xAxes.SCANNED_VARIABLE;
	private AxisEnums.yAxes yAxisSelection = yAxes.SPLICEDY;
	
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
		
		Display display = Display.getCurrent();
		Color red = display.getSystemColor(SWT.COLOR_RED);
		
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
        
        showErrors.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (errorDisplayFlag ==true){
					errorDisplayFlag = false;
				}
				else{
					errorDisplayFlag = true;
				}
				
				refreshCurves();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
        
        switchAxes = new Button(methodSetting, SWT.PUSH);
        switchAxes.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        switchAxes.setData(new GridData(SWT.FILL));
        switchAxes.setText("Change Axes");
        
	    switchAxes.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ArrayList<CurveStitchDataPackage> csdpList = rcm.getCsdpList();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
        
	    InputTileGenerator tile1 = new InputTileGenerator("X Axis:", methodSetting);
	    xAxis = tile1.getCombo();
	    xAxis.select(0);;
	   	
	    xAxis.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				String xSelectionT = xAxis.getText();
				xAxes xSel = AxisEnums.toXAxis(xSelectionT);
				
				xAxisSelection = xSel;
				
				refreshCurves();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    InputTileGenerator tile2 = new InputTileGenerator("Y Axis:", methodSetting);
	    yAxis = tile2.getCombo();
	    yAxis.select(0);

	    yAxis.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				String ySelectionT = yAxis.getText();
				yAxes ySel = AxisEnums.toYAxis(ySelectionT);
				
				yAxisSelection = ySel;
				
				refreshCurves();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    
	    form.setWeights(new int[] {11, 79});
		
	    rcm.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// TODO Auto-generated method stub
				
				String[] yAxes = setYAxes();
				String[] xAxes = setXAxes();
				
				xAxis.removeAll();
				yAxis.removeAll();
				
				for(String f : yAxes){
					yAxis.add(f);
				}
				for(String g : xAxes){
					xAxis.add(g);
				}
				
				xAxis.setText(AxisEnums.toString(xAxisSelection));
				yAxis.setText(AxisEnums.toString(yAxisSelection));
				
				refreshCurves();
				
				plotSystem.autoscaleAxes();
				
			}
		});
	    
	    plotSystem.setShowLegend(true);
	    
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

	private String[] setXAxes(){
		
		ArrayList<CurveStitchDataPackage> csdps = rcm.getCsdpList();
		ArrayList<String> outputList = new ArrayList<String>(); 
		
		boolean isXPresent = true;
		boolean isQPresent = true;
		
		for(CurveStitchDataPackage csdp: csdps){
			if(csdp.getSplicedCurveX() == null){
				isXPresent = false;
			}
			else{
				for(int i =0; i<csdp.getSplicedCurveX().getSize(); i++){
					double d = csdp.getSplicedCurveX().getDouble(i);
					
					if(d == -10000000000.0){
						isXPresent = false;
						break;
					}			
				}
			}
			
			if(csdp.getSplicedCurveQ() == null){
				isQPresent = false;
			}
			else{
				try{
					int g = csdp.getSplicedCurveQ().getSize();
					for(int i =0; i<csdp.getSplicedCurveQ().getSize(); i++){
						double d = csdp.getSplicedCurveQ().getDouble(i);
						
						if(d == -10000000000.0){
							isQPresent = false;
							break;
						}			
					}
				}
				catch(Exception z){
					isQPresent = false;
				}
			}
		}
			
		if(isXPresent){
			outputList.add(AxisEnums.toString(xAxes.SCANNED_VARIABLE));
		}
		if(isQPresent){
			outputList.add(AxisEnums.toString(xAxes.Q));
		}
		
		if (outputList.size()>0){
			String[] output = new String[outputList.size()];
			
			for(int y = 0; y<outputList.size(); y++){
				output[y] = outputList.get(y);
			}
			
			return output;
		}
		
		else{
			return null;
		}
	}
	
	private String[] setYAxes(){
		
		ArrayList<CurveStitchDataPackage> csdps = rcm.getCsdpList();
		ArrayList<String> outputList = new ArrayList<String>(); 
		
		boolean isYPresent = true;
		boolean isYRawPresent = true;
		boolean isYFhklPresent = true;
		
		for(CurveStitchDataPackage csdp: csdps){
			if(csdp.getSplicedCurveY() == null){
				isYPresent = false;
			}
			else{
				for(int i =0; i<csdp.getSplicedCurveY().getSize(); i++){
					double d = csdp.getSplicedCurveY().getDouble(i);
					
					if(d == -10000000000.0){
						isYPresent = false;
						break;
					}			
				}
			}
			
			if(csdp.getSplicedCurveYRaw() == null){
				isYRawPresent = false;
			}
			else{
				for(int i =0; i<csdp.getSplicedCurveYRaw().getSize(); i++){
					double d = csdp.getSplicedCurveYRaw().getDouble(i);
					
					if(d == -10000000000.0){
						isYRawPresent = false;
						break;
					}			
				}
			}
			
			if(csdp.getSplicedCurveYFhkl() == null){
				isYFhklPresent = false;
			}
			else{
				for(int i =0; i<csdp.getSplicedCurveYFhkl().getSize(); i++){
					double d = csdp.getSplicedCurveYFhkl().getDouble(i);
					
					if(d == -10000000000.0){
						isYFhklPresent = false;
						break;
					}			
				}
			}
		}
			
		if(isYRawPresent){
			outputList.add(AxisEnums.toString(yAxes.SPLICEDYRAW));
		}
		if(isYPresent){
			outputList.add(AxisEnums.toString(yAxes.SPLICEDY));
		}
		if(isYFhklPresent){
			outputList.add(AxisEnums.toString(yAxes.SPLICEDYFHKL));
		}
		
		if (outputList.size()>0){
			String[] output = new String[outputList.size()];
			
			for(int y = 0; y<outputList.size(); y++){
				output[y] = outputList.get(y);
			}
			
			return output;
		}
		
		else{
			return null;
		}
		
	}
	  
	private void refreshCurves(){
		
		plotSystem.clear();
		
		for(CurveStitchDataPackage csdp : rcm.getCsdpList()){
			
			ILineTrace lt =	plotSystem.createLineTrace(csdp.getRodName());
			
			IDataset x = DatasetFactory.zeros(new int[] {2,2}, Dataset.ARRAYFLOAT64);
			IDataset y = DatasetFactory.zeros(new int[] {2,2}, Dataset.ARRAYFLOAT64);
			
			if(xAxisSelection == null){
				xAxisSelection = xAxes.SCANNED_VARIABLE;
				
				boolean rg = true;
				
				for(String h :xAxis.getItems()){
					if(AxisEnums.toString(xAxisSelection).equals(h)){
						rg = false;
					}
				}
				
				if(rg){
					xAxis.add(AxisEnums.toString(xAxisSelection));
				}
				
			}
			
			if(yAxisSelection == null){
				yAxisSelection = yAxes.SPLICEDY;
				
				boolean rg = true;
				
				for(String h :yAxis.getItems()){
					if(AxisEnums.toString(yAxisSelection).equals(h)){
						rg = false;
					}
				}
				
				if(rg){
					yAxis.add(AxisEnums.toString(yAxisSelection));
				}
				
			}
			
			switch(xAxisSelection){
				case SCANNED_VARIABLE:
					x = csdp.getSplicedCurveX();
					break;
				case Q:
					x = csdp.getSplicedCurveQ();
					break;
			}
			
			switch(yAxisSelection){
				case SPLICEDY:
					y = csdp.getSplicedCurveY();
					try{
						y.setErrors(csdp.getSplicedCurveYError());
					}
					catch(Exception h){
						
					}
					break;
				case SPLICEDYRAW:
					y = csdp.getSplicedCurveYRaw();
					try{
						y.setErrors(csdp.getSplicedCurveYRawError());
					}
					catch(Exception h){
						
					}
					break;
				case SPLICEDYFHKL:
					y = csdp.getSplicedCurveYFhkl();
					try{
						y.setErrors(csdp.getSplicedCurveYFhklError());
					}
					catch(Exception h){
						
					}
					break;
			}
			
			lt.setData(x, y);
			
			lt.setErrorBarEnabled(errorDisplayFlag);
			
			plotSystem.addTrace(lt);
			plotSystem.autoscaleAxes();
			
		}
		
//		for(int j = 0; j<rcm.getCsdpList().get(0).getSplicedCurveX().getSize(); j++){
//			System.out.println("j: " + j + "  csdp.getSplicedCurveY() " + rcm.getCsdpList().get(0).getSplicedCurveY().getDouble(j) +
//					"  csdp.getSplicedCurveYRaw() " + rcm.getCsdpList().get(0).getSplicedCurveYRaw().getDouble(j) + 
//					"  csdp.getSplicedCurveYFhkl() " + rcm.getCsdpList().get(0).getSplicedCurveYFhkl().getDouble(j));
//		}
		
	}
}
		
		
		
		

