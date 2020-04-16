package org.dawnsci.surfacescatter.ui;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.dawnsci.surfacescatter.BatchRodDataTransferObject;
import org.dawnsci.surfacescatter.BatchRodModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

public class BatchSetupWindow {

	private BatchDatDisplayer batchDatDisplayer;
	private BatchRodModel brm;
	private BatchDisplay batchDisplay;

	

	public BatchSetupWindow(CTabFolder folder, SurfaceScatterViewStart ssvs, SurfaceScatterPresenter ssp) {

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

			batchDatDisplayer = new BatchDatDisplayer(left, SWT.FILL, ssp, ssvs, brm);
			batchDatDisplayer.setLayout(new GridLayout());
			batchDatDisplayer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		///////////////// setupRight////////////////////////////////////////

		/////////////////////////// Window 2 RIGHT SETUP
		/////////////////////////// ////////////////////////////////////////////////////

		try {

			batchDisplay = new BatchDisplay(right, SWT.FILL, ssvs, BatchSetupWindow.this, brm);
			batchDisplay.setLayout(new GridLayout());
			batchDisplay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage() + "sssssssssssdddddddddddddddfffffffffffff");
		}

		
	}

	public BatchDatDisplayer getBatchDatDisplayer() {
		return batchDatDisplayer;
	}

	public void pushTocheck(BatchRodDataTransferObject brt) {

		batchDatDisplayer.getRodDisplayTable().removeAll();
		batchDatDisplayer.getRodDisplayTable().setEnabled(true);
		batchDatDisplayer.enableRodConstruction(true);

		String[] completeDatNames = brt.getDatFiles();
		String[] displayDatNames = completeDatNames.clone();

		for (int i = 0; i < completeDatNames.length; i++) {
			displayDatNames[i] = StringUtils.substringAfterLast(completeDatNames[i], File.separator);
			TableItem ti = new TableItem(batchDatDisplayer.getRodDisplayTable(), SWT.NONE);
			ti.setText(displayDatNames[i]);
			ti.setChecked(true);
		}

		batchDatDisplayer.getParamFileTable().removeAll();

		TableItem p = new TableItem(batchDatDisplayer.getParamFileTable(), SWT.NONE);
		p.setChecked(true);
		p.setText(brt.getParamFiles());

		batchDatDisplayer.getUseTrajectoryButton().setEnabled(brt.isUseTrajectory());
		batchDatDisplayer.setUseTrajectory(brt.isUseTrajectory());
	}

	
	
	
}