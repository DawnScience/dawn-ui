package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class AdvancedBatchSettings extends Dialog {

	private Button override;

	public AdvancedBatchSettings(Shell parentShell, int selector, String note) {

		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);

	}

	@Override
	protected Control createDialogArea(Composite parent) {

		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		BatchSavingAdvancedSettings[] bsas = new BatchSavingAdvancedSettings[SaveFormatSetting.values().length];

		for (SaveFormatSetting sfs : SaveFormatSetting.values()) {

			Group g = new Group(container, SWT.NONE);
			GridLayout gLayout = new GridLayout(3, true);
			GridData gData = new GridData((GridData.FILL_HORIZONTAL));
			g.setLayout(gLayout);
			g.setLayoutData(gData);

			Label gLabel = new Label(g, SWT.FILL);
			gLabel.setText(sfs.getDisplayName());

			Button allPoints = new Button(g, SWT.CHECK);
			allPoints.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			allPoints.setSelection(false);

			Button goodPoints = new Button(g, SWT.CHECK);
			goodPoints.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			goodPoints.setSelection(false);

			BatchSavingAdvancedSettings bsa = new BatchSavingAdvancedSettings(sfs, allPoints.getSelection(),
					goodPoints.getSelection());

			bsas[sfs.getPosition()] = bsa;

			allPoints.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					bsa.setAllPoints(allPoints.getSelection());

				}

			});

			goodPoints.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					bsa.setGoodPoints(goodPoints.getSelection());

				}

			});

		}

		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Error Warning");
	}

	@Override
	protected Point getInitialSize() {
		Rectangle rect = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		int h = rect.height;
		int w = rect.width;

		return new Point((int) Math.round(0.4 * w), (int) Math.round(0.2 * h));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "OK", true);
		override = createButton(parent, IDialogConstants.CANCEL_ID, "Override", false);
	}

	public Button getOverride() {
		return override;
	}

}