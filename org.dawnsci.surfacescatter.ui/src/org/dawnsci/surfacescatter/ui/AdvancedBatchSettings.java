package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class AdvancedBatchSettings extends Composite {

	private BatchSavingAdvancedSettings[] bsas;

	public AdvancedBatchSettings(Composite parent, int selector, BatchSavingAdvancedSettings[] bsas) {

		super(parent, selector);
		this.bsas = bsas;
		
		this.createContents();
	}

	protected void createContents() {

		Group container = new Group(this, SWT.NONE);
		GridLayout containerLayout = new GridLayout(3, true);
		GridData containerData = new GridData((GridData.FILL_HORIZONTAL));
		container.setLayout(containerLayout);
		container.setLayoutData(containerData);

		Group f1 = localSmallGroup(container);
		Label fLabel1 = new Label(f1, SWT.FILL);
		fLabel1.setText("Save format:");

		Group f2 = localSmallGroup(container);
		Label fLabel2 = new Label(f2, SWT.FILL);
		fLabel2.setText("Save All Points");

		Group f3 = localSmallGroup(container);
		Label fLabel3 = new Label(f3, SWT.FILL);
		fLabel3.setText("Save Only Good Points");

		Button[] allPointsArray = new Button[SaveFormatSetting.values().length];
		Button[] goodPointsArray = new Button[SaveFormatSetting.values().length];

		for (SaveFormatSetting sfs : SaveFormatSetting.values()) {

			if (bsas[sfs.getPosition()] == null) {
				BatchSavingAdvancedSettings bsa = new BatchSavingAdvancedSettings(sfs);
				bsas[sfs.getPosition()] = bsa;
			}

			Group g1 = localSmallGroup(container);
			Label gLabel = new Label(g1, SWT.FILL);
			gLabel.setText(sfs.getDisplayName());

			Group g2 = localSmallGroup(container);
			Button allPoints = new Button(g2, SWT.CHECK);
			allPoints.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			allPoints.setSelection(bsas[sfs.getPosition()].isAllPoints());
			allPointsArray[sfs.getPosition()] = allPoints;

			allPoints.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					bsas[sfs.getPosition()].setAllPoints(allPoints.getSelection());

				}
			});

			Group g3 = localSmallGroup(container);
			Button goodPoints = new Button(g3, SWT.CHECK);
			goodPoints.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			goodPoints.setSelection(bsas[sfs.getPosition()].isGoodPoints());
			goodPointsArray[sfs.getPosition()] = goodPoints;

			goodPoints.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					bsas[sfs.getPosition()].setGoodPoints(goodPoints.getSelection());

				}
			});

		}

		Label hLabel1 = new Label(container, SWT.FILL);
		hLabel1.setText("");

		Group h2 = localSmallGroup(container);
		Button checkAllGood = new Button(h2, SWT.PUSH);
		checkAllGood.setText("Select All");
		checkAllGood.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		checkAllGood.setEnabled(true);

		checkAllGood.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controlAllPointsArrays(allPointsArray, true, true);
			}

		});

		Group h3 = localSmallGroup(container);
		Button checkSomeGood = new Button(h3, SWT.PUSH);
		checkSomeGood.setText("Select All");
		checkSomeGood.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		checkSomeGood.setEnabled(true);

		checkSomeGood.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controlAllPointsArrays(allPointsArray, true, false);
			}

		});

		Label iLabel1 = new Label(container, SWT.FILL);
		iLabel1.setText("");

		Group i2 = localSmallGroup(container);
		Button unCheckAllGood = new Button(i2, SWT.PUSH);
		unCheckAllGood.setText("Deselect All");
		unCheckAllGood.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		unCheckAllGood.setEnabled(true);

		unCheckAllGood.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controlAllPointsArrays(allPointsArray, false, true);
			}

		});

		Group i3 = localSmallGroup(container);
		Button unCheckSomeGood = new Button(i3, SWT.PUSH);
		unCheckSomeGood.setText("Deselect All");
		unCheckSomeGood.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		unCheckSomeGood.setEnabled(true);

		unCheckSomeGood.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controlAllPointsArrays(goodPointsArray, false, false);
			}

		});

	}

	private Group localSmallGroup(Composite container) {

		Group f = new Group(container, SWT.NONE);
		GridLayout fLayout = new GridLayout(1, true);
		GridData fData = new GridData((GridData.FILL_HORIZONTAL));
		f.setLayout(fLayout);
		f.setLayoutData(fData);

		return f;
	}

	private void controlAllPointsArrays(Button[] allPointsArray, boolean in, boolean useAllPoints) {

		if (useAllPoints) {
			for (int i = 0; i < SaveFormatSetting.values().length; i++) {
				bsas[i].setAllPoints(in);
				allPointsArray[i].setSelection(in);
			}
		}

		else {
			for (int i = 0; i < SaveFormatSetting.values().length; i++) {
				bsas[i].setGoodPoints(in);
				allPointsArray[i].setSelection(in);
			}
		}

	}

}