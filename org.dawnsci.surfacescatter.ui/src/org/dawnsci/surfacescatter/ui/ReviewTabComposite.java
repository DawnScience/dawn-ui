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
import org.dawnsci.surfacescatter.GoodPointStripper;
import org.dawnsci.surfacescatter.ReviewCurvesModel;
import org.dawnsci.surfacescatter.SavingFormatEnum;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;
import org.dawnsci.surfacescatter.SavingUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class ReviewTabComposite extends Composite{

    private Group methodSetting;
    private SashForm form;
    private SashForm rightForm;
    private SashForm leftForm;
    private Button clearCurves;
    private Button addCurve;
    private Button selectAll;
    private Button remove;
    private Button showErrors;
    private Button save;
    private Button saveGoodPoints;
	private Combo outputFormatSelection;
    private IPlottingSystem<Composite> plotSystem;
    private ReviewCurvesModel rcm;
	private String nexusFolderPath;
	private boolean errorDisplayFlag = true;
	private Combo xAxis;
	private Combo yAxis;
	private Combo rodToSave;
	private AxisEnums.xAxes xAxisSelection = xAxes.SCANNED_VARIABLE;
	private AxisEnums.yAxes yAxisSelection = yAxes.SPLICEDY;
	private Table rodDisplayTable;
	private boolean selectDeslect = true;
	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;
	private boolean useGoodPointsOnly = false;
	private Button showOnlyGoodPoints;
	
	public ReviewTabComposite(Composite parent, 
							  int style,
							  SurfaceScatterPresenter ssp,
							  SurfaceScatterViewStart ssvs) throws Exception {
        super(parent, style);

        
        this.ssvs = ssvs;
        this.ssp = ssp;
        
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
	
		Composite setupComposite = new Composite(this, SWT.FILL);
		setupComposite.setLayout(new GridLayout());
		setupComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		form = new SashForm(setupComposite, SWT.FILL);
		form.setLayout(new GridLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		form.setOrientation(SWT.HORIZONTAL);
		
///////////////////////////left		
		leftForm = new SashForm(form, SWT.VERTICAL);
		leftForm.setLayout(new GridLayout());
		leftForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Group rodSelector = new Group(leftForm, SWT.V_SCROLL | SWT.FILL);
		GridLayout rodSelectorLayout = new GridLayout(1, true);
		GridData rodSelectorData = new GridData((GridData.FILL_BOTH));
		rodSelector.setLayout(rodSelectorLayout);
		rodSelector.setLayoutData(rodSelectorData);
		rodSelector.setText("Rods");
		
		clearCurves = new Button(rodSelector, SWT.PUSH | SWT.FILL);
		clearCurves.setText("Clear Curves");
		clearCurves.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		clearCurves.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					
					for(int cv = 0; cv<rodDisplayTable.getItems().length; cv++){
						rodDisplayTable.remove(cv);
					}
					
					rodDisplayTable.removeAll();	
					plotSystem.clear();
					rcm.setCsdpList(null);
					rcm.setCsdpLatest(null);
					
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
		});
		

	    addCurve = new Button (rodSelector, SWT.PUSH);
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
       
		selectAll= new Button(rodSelector, SWT.PUSH | SWT.FILL);
		selectAll.setText("Select All");
		selectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		selectAll.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
		
				for(TableItem f :rodDisplayTable.getItems()){
					f.setChecked(selectDeslect);
				}				
				
				if(selectDeslect){
					selectDeslect=false;
					selectAll.setText("De-Select All");
				}
				else{
					selectDeslect = true;
					selectAll.setText("Select All");
				}
				
				refreshCurvesFromTable();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		remove= new Button(rodSelector, SWT.PUSH | SWT.FILL);
		remove.setText("Remove Selected");
		remove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		remove.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				for(TableItem f :rodDisplayTable.getItems()){
					if(f.getChecked()){
						CurveStitchDataPackage csdp = bringMeTheOneIWant(f.getText(), rcm.getCsdpList());
						rcm.getCsdpList().remove(csdp);
					}
				}
				
				refreshTable();
				refreshCurves();
				plotSystem.autoscaleAxes();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});

		
		
		rodDisplayTable = new Table(rodSelector, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		rodDisplayTable.setEnabled(true);
		
		GridData rodDisplayTableData = new GridData(GridData.FILL_BOTH);

		rodDisplayTable.setLayoutData(rodDisplayTableData);
		rodDisplayTable.setLayout(new GridLayout());
		rodDisplayTable.getVerticalBar().setEnabled(true);

		rodDisplayTable.getVerticalBar().setEnabled(true);
		rodDisplayTable.getVerticalBar().setIncrement(1);
		rodDisplayTable.getVerticalBar().setThumb(1);
		
		rodDisplayTable.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshCurvesFromTable();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		
		
////////////////////////////////right		
		rightForm = new SashForm(form, SWT.VERTICAL);
		rightForm.setLayout(new GridLayout());
		rightForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		methodSetting = new Group(rightForm, SWT.FILL | SWT.FILL);
        GridLayout methodSettingLayout = new GridLayout(1, true);
	    GridData methodSettingData = new GridData(GridData.FILL_HORIZONTAL);
	    methodSetting.setLayout(methodSettingLayout);
	    methodSetting.setLayoutData(methodSettingData);
	    methodSetting.setText("Rod Display");
		
	    Group curveSettings = new Group(rightForm, SWT.NONE);
        GridLayout curveSettingsLayout = new GridLayout(2, true);
        curveSettings.setLayout(curveSettingsLayout);
        
        final GridData curveSettingsData = new GridData(SWT.FILL, SWT.FILL, true, true);
        curveSettingsData.grabExcessVerticalSpace = true;
        curveSettingsData.heightHint = 100;
        curveSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
	    
        Group saveSettings = new Group(rightForm, SWT.NONE);
        GridLayout saveSettingsLayout = new GridLayout(2, true);
        saveSettings.setLayout(saveSettingsLayout);
        
        final GridData saveSettingsData = new GridData(SWT.FILL, SWT.FILL, true, true);
        saveSettingsData.grabExcessVerticalSpace = true;
        saveSettingsData.heightHint = 100;
        saveSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
         
        save = new Button(saveSettings, SWT.PUSH);
        save.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        save.setData(new GridData(SWT.FILL));
        save.setText("Save Single Rod");
        
        save.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				saveRod(false);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
        
        saveGoodPoints = new Button(saveSettings, SWT.PUSH);
        saveGoodPoints.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        saveGoodPoints.setData(new GridData(SWT.FILL));
        saveGoodPoints.setText("Save Only Good Points");
        
        saveGoodPoints.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveRod(true);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
        
        showOnlyGoodPoints = new Button(saveSettings, SWT.PUSH);
        showOnlyGoodPoints.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        showOnlyGoodPoints.setData(new GridData(SWT.FILL));
        showOnlyGoodPoints.setText("Show Only Good Points");
        
        showOnlyGoodPoints.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				flipUseGoodPointsOnly();
				refreshCurvesFromTable();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
        
        Button storeAsNexus = new Button(saveSettings, SWT.PUSH);
        storeAsNexus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        storeAsNexus.setData(new GridData(SWT.FILL));
        storeAsNexus.setText("Store As Nexus");
        
        storeAsNexus.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				FileDialog fd = new FileDialog(ssvs.getShell(), SWT.SAVE);

				if(ssp.getNexusPath()!=null){
					fd.setFilterPath(ssp.getNexusPath());
				}
				
				String stitle = "r";
				String path = "p";

				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();

				}
				
				ssp.setNexusPath(path);
				
				String title = path + File.separator + stitle + ".nxs";

				ssp.setRodName(stitle);
				
				ssp.writeNexus(title);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
        
        Group storedCurves = new Group(rightForm, SWT.NONE);
        GridLayout storedCurvesLayout = new GridLayout();
        storedCurves.setLayout(storedCurvesLayout);
        
        final GridData storedCurvesData = new GridData(SWT.FILL, SWT.FILL, true, true);
        storedCurvesData.grabExcessVerticalSpace = true;
        storedCurvesData.heightHint = 100;
        storedCurves.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
        
        
        
//	    try {
//			plotSystem = PlottingFactory.createPlottingSystem();
//				
//		} catch (Exception e2) {
//			e2.printStackTrace();
//		}
	        
		ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(storedCurves, 
																				null);
		  
	    plotSystem.createPlotPart(storedCurves, 
	        					  "Stored Curves", 
	        					  actionBarComposite, 
	        					  PlotType.IMAGE, 
	        					  null);
		
	    
	    plotSystem.getPlotComposite().setLayoutData(storedCurvesData);
	    
	   
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
				
			}
		});
        
	    InputTileGenerator tile1 = new InputTileGenerator("X Axis:", curveSettings);
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

			}
		});
	    
	    InputTileGenerator tile2 = new InputTileGenerator("Y Axis:", curveSettings);
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
				
			}
		});
	    
	    InputTileGenerator tile3 = new InputTileGenerator("Curve To Save:", curveSettings);
	    rodToSave = tile3.getCombo();
	    rodToSave.select(0);
	    
	    outputFormatSelection = new Combo(curveSettings, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);
		
		for(SaveFormatSetting  t: SavingFormatEnum.SaveFormatSetting.values()){
			outputFormatSelection.add(SaveFormatSetting.toString(t));
		}
	
		outputFormatSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		outputFormatSelection.select(0);
	    
	    rightForm.setWeights(new int[] {5,10, 10,75});
		
	    rcm.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
	
				try{
					String[] yAxes = setYAxes();
					String[] xAxes = setXAxes();
					
					xAxis.removeAll();
					yAxis.removeAll();
					try{
						for(String f : yAxes){
							yAxis.add(f);
						}
						for(String g : xAxes){
							xAxis.add(g);
						}
					}
					catch(Exception i){
						System.out.println(i.getMessage());
					}
					
					xAxis.setText(AxisEnums.toString(xAxisSelection));
					yAxis.setText(AxisEnums.toString(yAxisSelection));
					
					refreshCurves();
					refreshTable();
					plotSystem.autoscaleAxes();
				}
				catch(Exception b){
					System.out.println(b.getMessage());
				}
			}
		});
	    
	    plotSystem.setShowLegend(true);
	    
	    form.setWeights(new int[] {25, 75});
	    
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
	
	public SashForm getRightForm() {
		return rightForm;
	}

	public void setRightForm(SashForm form) {
		this.rightForm = form;
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
	
	
	private void saveRod(boolean writeOnlyGoodPoints){
		
		FileDialog fd = new FileDialog(ssvs.getShell(), SWT.SAVE);

		if(ssp.getSaveFolder()!=null){
			fd.setFilterPath(ssp.getSaveFolder());
		}
		
		String stitle = "r";
		String path = "p";

		if (fd.open() != null) {
			stitle = fd.getFileName();
			path = fd.getFilterPath();

		}
		
		if(ssp.getSaveFolder()==null){
			ssp.setSaveFolder(path);;
		}
		
		String title = path + File.separator + stitle;
	
		
		SavingUtils su = new SavingUtils();
		String rodSaveName = rodToSave.getText();
		
		CurveStitchDataPackage csdpToSave = bringMeTheOneIWant(rodSaveName, 
				rcm.getCsdpList());
				
		SaveFormatSetting sfs =SaveFormatSetting.toMethod(outputFormatSelection.getText());
		
		int saveIntensityState = AxisEnums.toInt(yAxisSelection);
		
		if (sfs == SaveFormatSetting.GenX) {
			su.genXSave(writeOnlyGoodPoints,
					title,
					csdpToSave,
					ssp.getDrm(),
					ssp.getDrm().getFms(),
					ssp.getGm());
		}
		if (sfs == SaveFormatSetting.Anarod) {
			su.anarodSave(writeOnlyGoodPoints,
					title,
					csdpToSave,
					ssp.getDrm(),
					ssp.getDrm().getFms(),
					ssp.getGm());
		}
		if (sfs == SaveFormatSetting.int_format) {
			su.intSave(writeOnlyGoodPoints,
					title,
					csdpToSave,
					ssp.getDrm(),
					ssp.getDrm().getFms(),
					ssp.getGm());
		}
		if (sfs == SaveFormatSetting.ASCII) {
			su.simpleXYYeSave(writeOnlyGoodPoints,
					title,
					saveIntensityState,
					csdpToSave,
					ssp.getDrm(),
					ssp.getDrm().getFms(),
					ssp.getGm());
		}

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
		
		if(!outputList.isEmpty()){
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
	
	private void refreshTable(){
		
		ArrayList<String> checked = new ArrayList<>();
		
		
		if(rodDisplayTable.getItems().length>0){
			for(TableItem de : rodDisplayTable.getItems()){
				if(de.getChecked()){
					checked.add(de.getText()); 
				}
			}
		
		
			for(int cv = 0; cv<rodDisplayTable.getItems().length; cv++){
				rodDisplayTable.remove(cv);
			}
		
			rodDisplayTable.removeAll();	
		
		}
		
		if(!rcm.getCsdpList().isEmpty()){
			for (int j = 0; j < rcm.getCsdpList().size(); j++) {
				
				TableItem t = new TableItem(rodDisplayTable, SWT.NONE);
				t.setText(rcm.getCsdpList().get(j).getRodName());
				String probe = rcm.getCsdpList().get(j).getRodName();
				
				for(String g : checked){
					if(probe.equals(g)){
						t.setChecked(true); 
					}
				}	
			}	
			
			String latestAddition = rcm.getCsdpLatest().getRodName();
		
		
			for(TableItem ry : rodDisplayTable.getItems()){
				
				if(ry.getText().equals(latestAddition)){
					ry.setChecked(true);
				}
				
			}
		}
		
		rodToSave.removeAll();
		
		for(TableItem fg :  rodDisplayTable.getItems()){
			rodToSave.add(fg.getText());
		}
	}
	
	private ILineTrace buildLineTrace(CurveStitchDataPackage csdp){

		ILineTrace lt =	plotSystem.createLineTrace(csdp.getRodName());
		
		IDataset x = DatasetFactory.zeros(new int[] {2,2}, Dataset.ARRAYFLOAT64);
		IDataset y[] = new IDataset[2];
		
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
		
		GoodPointStripper gps = new GoodPointStripper();

		x  = gps.splicedXGoodPointStripper(csdp, 
				  xAxisSelection,
				  !useGoodPointsOnly);

		
		y = gps.splicedYGoodPointStripper(csdp, 
							  yAxisSelection,
							  !useGoodPointsOnly);
					
		y[0].setErrors(y[1]);
		
		lt.setData(x, y[0]);
		
		lt.setErrorBarEnabled(errorDisplayFlag);
		
		
		return lt;
	}
		
	private void refreshCurves(){
		
		plotSystem.clear();
		
		if(!rcm.getCsdpList().isEmpty()){
			for(CurveStitchDataPackage csdp : rcm.getCsdpList()){
				
				ILineTrace lt = buildLineTrace(csdp);
				
				plotSystem.addTrace(lt);
//				plotSystem.autoscaleAxes();
				
			}
		}
	}
	
	public CurveStitchDataPackage bringMeTheOneIWant(String rodName,
													 ArrayList<CurveStitchDataPackage> csdps){
		
		for(CurveStitchDataPackage csdp : csdps){
			if(rodName.equals(csdp.getRodName())){
				return csdp;
			}
		}
		
		return null;
	}
	
	private void refreshCurvesFromTable(){
		
		plotSystem.clear();
		
		for(TableItem fd : rodDisplayTable.getItems()){
			if(fd.getChecked()){
				
				CurveStitchDataPackage csdp = bringMeTheOneIWant(fd.getText(), 
																 rcm.getCsdpList());
				
				buildLineTrace(csdp);
				
				ILineTrace lt =	buildLineTrace(csdp);
				
				plotSystem.addTrace(lt);
				plotSystem.autoscaleAxes();
			}
		}
	}
	

	public SashForm getLeftForm() {
		return leftForm;
	}

	public void setLeftForm(SashForm leftForm) {
		this.leftForm = leftForm;
	}
	
	
	public Combo getRodToSave(){
		return rodToSave;
	}
	
	public Combo getOutputFormatSelection(){
		return outputFormatSelection;
	}
	
	public AxisEnums.yAxes getyAxisSelection() {
		return yAxisSelection;
	}

	public void setyAxisSelection(AxisEnums.yAxes yAxisSelection) {
		this.yAxisSelection = yAxisSelection;
	}

	public void addCurrentTrace(CurveStitchDataPackage csdp){
			
		rcm.removeFromCsdpList(csdp);
		rcm.addToCsdpList(csdp);
		 
		 
	}
	
	private void flipUseGoodPointsOnly(){
		
		useGoodPointsOnly  = !useGoodPointsOnly;
		
		if(useGoodPointsOnly){
			showOnlyGoodPoints.setText("Include All Points");
		}
		else{
			showOnlyGoodPoints.setText("Disregard Bad Points");
		}
	}
	
	
}
		
		
		
		

