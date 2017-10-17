package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.BatchRodModel;
import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.BatchSetupMiscellaneousProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class BatchConfigurationSettings extends Dialog {

	private BatchSavingAdvancedSettings[] bsas;
	private BatchSetupMiscellaneousProperties bsmps;

	public BatchConfigurationSettings(Shell parent, BatchRodModel brm) {

		super(parent);

		this.bsas = brm.getBsas();
		this.bsmps =brm.getBsmps();

		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SashForm selectionSash = new SashForm(container, SWT.FILL);
		selectionSash.setLayout(new GridLayout());

		Composite left = new Composite(selectionSash, SWT.FILL);
		left.setLayout(new GridLayout());
		left.setLayoutData(new GridData(GridData.FILL));
		
		Composite right = new Composite(selectionSash, SWT.FILL);
		right.setLayout(new GridLayout());
		right.setLayoutData(new GridData(GridData.FILL));

		BatchSettingMiscellaneous bsm = new BatchSettingMiscellaneous(left, SWT.FILL, bsmps);
		bsm.setLayout(new GridLayout());
		bsm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		AdvancedBatchSettings abs =  new AdvancedBatchSettings(right, SWT.FILL, bsas);
		abs.setLayout(new GridLayout());
		abs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		selectionSash.setWeights(new int[] {50,50});		
		
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Batch Output Configuration");
	}

	@Override
	protected Point getInitialSize() {
		Rectangle rect = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		int h = rect.height;
		int w = rect.width;

		return new Point((int) Math.round(0.7 * w), (int) Math.round(0.4 * h));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

}
