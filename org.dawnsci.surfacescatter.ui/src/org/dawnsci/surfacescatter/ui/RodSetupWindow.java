package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.MethodSettingEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

public class RodSetupWindow {
	
	private DatDisplayer datDisplayer;
	private GeometricParametersWindows paramField;
	private Combo correctionsDropDown;
	private Group experimentalSetup;
	private Group methodSetting;
	private Group parametersSetting;
	private AnglesAliasWindow anglesAliasWindow;
		
	public RodSetupWindow(CTabFolder folder,
			SurfaceScatterViewStart ssvs,
			SurfaceScatterPresenter ssp){
	
		CTabItem setup = new CTabItem(folder, SWT.NONE);
		setup.setText("Setup Parameters");
		setup.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite setupComposite = new Composite(folder, SWT.FILL);
		setupComposite.setLayout(new GridLayout());
		setupComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	
		setup.setControl(setupComposite);
	
		SashForm setupSash = new SashForm(setupComposite, SWT.FILL);
		setupSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	
		SashForm left = new SashForm(setupSash, SWT.VERTICAL);
		left.setLayout(new GridLayout());
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	
		SashForm right = new SashForm(setupSash, SWT.VERTICAL);
		right.setLayout(new GridLayout());
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	
		setupSash.setWeights(new int[] { 65, 35 });
	
		//////////////// setupLeft///////////////////////////////////////////////
	
		/////////////////////////// Window 1 LEFT
		/////////////////////////// SETUP////////////////////////////////////////////////////
		
		try {
	
			datDisplayer = new DatDisplayer(left, SWT.FILL, ssp, ssvs, RodSetupWindow.this);
			datDisplayer.setLayout(new GridLayout());
			datDisplayer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		///////////////// setupRight////////////////////////////////////////
	
		/////////////////////////// Window 2 RIGHT SETUP
		/////////////////////////// ////////////////////////////////////////////////////
	
		try {
			
			experimentalSetup = new Group(right, SWT.FILL);
			GridLayout experimentalSetupLayout = new GridLayout(1, true);
			GridData experimentalSetupData = new GridData(GridData.FILL_BOTH);
			experimentalSetupData.minimumWidth = 50;
			experimentalSetup.setLayout(experimentalSetupLayout);
			experimentalSetup.setLayoutData(experimentalSetupData);
			experimentalSetup.setText("Experimental Setup");
			
			methodSetting = new Group(experimentalSetup, SWT.FILL);
			GridLayout methodSettingLayout = new GridLayout(1, true);
			GridData methodSettingData = new GridData(GridData.FILL_HORIZONTAL);
			methodSettingData.minimumWidth = 50;
			methodSetting.setLayout(methodSettingLayout);
			methodSetting.setLayoutData(methodSettingData);
			methodSetting.setText("SXRD / Reflectivity");
	
			correctionsDropDown = new Combo(methodSetting, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);
			
			for(org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting  t: MethodSettingEnum.MethodSetting.values()){
				 correctionsDropDown.add(org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting.toString(t));
			}
	
			
			correctionsDropDown.select(0);
			correctionsDropDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
			
			Group parametersAlias = new Group(experimentalSetup, SWT.FILL);
			GridLayout parametersAliasLayout = new GridLayout(1, true);
			GridData parametersAliasData = new GridData(GridData.FILL_BOTH);
			parametersAliasData.minimumWidth = 50;
			parametersAlias.setLayout(parametersAliasLayout);
			parametersAlias.setLayoutData(parametersAliasData);
			parametersAlias.setText("Parameter Aliases");
			
			anglesAliasWindow = new AnglesAliasWindow(parametersAlias, SWT.FILL, ssp, ssvs);
			anglesAliasWindow.setLayout(new GridLayout());
			anglesAliasWindow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
		
			parametersSetting = new Group(experimentalSetup, SWT.FILL);
			GridLayout parametersSettingLayout = new GridLayout(1, true);
			GridData parametersSettingData = new GridData(GridData.FILL_BOTH);
			parametersSettingData.minimumWidth = 50;
			parametersSetting.setLayout(parametersSettingLayout);
			parametersSetting.setLayoutData(parametersSettingData);
			parametersSetting.setText("Geometric Parameters");
			
			paramField = new GeometricParametersWindows(parametersSetting, SWT.FILL, ssp, ssvs);
			paramField.setLayout(new GridLayout());
			paramField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			this.setupRightEnabled(false);
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public AnglesAliasWindow getAnglesAliasWindow() {
		return anglesAliasWindow;
	}

	public void setupRightEnabled(boolean enabled){
		
		experimentalSetup.setEnabled(enabled);
		
		for (Control r: experimentalSetup.getChildren()){
			r.setEnabled(enabled);
		}
		
		for (Control r: methodSetting.getChildren()){
			r.setEnabled(enabled);
		}
		
		paramField.setEnabled(enabled);
		parametersSetting.setEnabled(enabled);
		
	}
	
	public DatDisplayer getDatDisplayer() {
		return datDisplayer;
	}
	
	public GeometricParametersWindows getParamField() {
		return paramField;
	}
	
	public Group getExperimentalSetup(){
		return experimentalSetup;
	}
	
	public Group getMethodSetting(){
		return methodSetting;
	}

	public Group getParametersSetting(){
		return parametersSetting;
	}
	
	public Combo getCorrectionsDropDown(){
		return correctionsDropDown;
	}
}	