package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.BatchRodModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class BatchSetupWindow {
	
	private BatchDatDisplayer datDisplayer;
	private BatchRodModel brm;
	private BatchDisplay batchDisplay;
		
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
			

			
			batchDisplay = new BatchDisplay(right,SWT.FILL, ssp, ssvs, BatchSetupWindow.this, brm); 
			batchDisplay.setLayout(new GridLayout());
			batchDisplay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage() + "sssssssssssdddddddddddddddfffffffffffff");
		}
	}


	
	public BatchDatDisplayer getBatchDatDisplayer() {
		return datDisplayer;
	}
	

}	