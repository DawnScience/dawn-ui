package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.BatchRodModel;
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

public class BatchSetupWindow {
	
	private BatchDatDisplayer datDisplayer;
	private GeometricParametersWindows paramField;
	private Combo correctionsDropDown;
	private Group batchDisplay;
	private Group methodSetting;
	private Group parametersAlias;
	private Group parametersSetting;
	private AnglesAliasWindow anglesAliasWindow;
	private BatchRodModel brm;
		
	public BatchSetupWindow(CTabFolder folder,
			SurfaceScatterViewStart ssvs,
			SurfaceScatterPresenter ssp){
	
		brm = new BatchRodModel();
		
		CTabItem setup = new CTabItem(folder, SWT.NONE);
		setup.setText("Batch Setup");
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
	
			datDisplayer = new BatchDatDisplayer(left, SWT.FILL, ssp, ssvs, BatchSetupWindow.this, brm);
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
			
			batchDisplay = new Group(right, SWT.FILL);
			GridLayout batchDisplaySetupLayout = new GridLayout(1, true);
			GridData batchDisplaySetupData = new GridData(GridData.FILL_BOTH);
			batchDisplaySetupData.minimumWidth = 50;
			batchDisplay.setLayout(batchDisplaySetupLayout);
			batchDisplay.setLayoutData(batchDisplaySetupData);
			batchDisplay.setText("Batch");
			
			BatchDisplay bdy = new BatchDisplay(batchDisplay,SWT.FILL, ssp, ssvs, BatchSetupWindow.this, brm); 
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public AnglesAliasWindow getAnglesAliasWindow() {
		return anglesAliasWindow;
	}

	public void setupRightEnabled(boolean enabled){
		
		batchDisplay.setEnabled(enabled);
		
		for (Control r: batchDisplay.getChildren()){
			r.setEnabled(enabled);
		}
		
		for (Control r: methodSetting.getChildren()){
			r.setEnabled(enabled);
		}
		
		for (Control r: parametersAlias.getChildren()){
			r.setEnabled(enabled);
		}
		
		for (Control r: anglesAliasWindow.getChildren()){
			r.setEnabled(enabled);
		}
		
		
		paramField.setEnabled(enabled);
		parametersSetting.setEnabled(enabled);
		parametersAlias.setEnabled(enabled);
		anglesAliasWindow.setEnabled(enabled);
		anglesAliasWindow.switchEnbaled(enabled);
	}
	
	public BatchDatDisplayer getBatchDatDisplayer() {
		return datDisplayer;
	}
	
	public GeometricParametersWindows getParamField() {
		return paramField;
	}
	
	public Group getExperimentalSetup(){
		return batchDisplay;
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