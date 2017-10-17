package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.AxisEnums;
import org.dawnsci.surfacescatter.AxisEnums.yAxes;
import org.dawnsci.surfacescatter.BatchSetupMiscellaneousProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class BatchSettingMiscellaneous extends Composite {

	BatchSetupMiscellaneousProperties bsmps;

	public BatchSettingMiscellaneous(Composite parent, int selector, BatchSetupMiscellaneousProperties bsmps) {

		super(parent, selector);
		this.bsmps = bsmps;

		this.createContents();
	}

	protected void createContents() {

		Group container = new Group(this, SWT.NONE);
		GridLayout containerLayout = new GridLayout(1, true);
		GridData containerData = new GridData((GridData.FILL_HORIZONTAL));
		container.setLayout(containerLayout);
		container.setLayoutData(containerData);

		Group f = new Group(container, SWT.NONE);
		GridLayout fLayout = new GridLayout(2, true);
		GridData fData = new GridData((GridData.FILL_HORIZONTAL));
		f.setLayout(fLayout);
		f.setLayoutData(fData);

		Group f1 = localSmallGroup(f);
		Label fLabel1 = new Label(f1, SWT.FILL);
		fLabel1.setText("Plot output against computed q:");

		Group f2 = localSmallGroup(f);
		Button useQ = new Button(f2, SWT.CHECK);
		useQ.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group g = new Group(container, SWT.NONE);
		GridLayout gLayout = new GridLayout(1, true);
		GridData gData = new GridData((GridData.FILL_HORIZONTAL));
		g.setLayout(gLayout);
		g.setLayoutData(gData);

		Table yAxisTable = new Table(container, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		yAxisTable.setEnabled(true);

		GridData yAxisTableData = new GridData(GridData.FILL_BOTH);

		yAxisTable.setLayoutData(yAxisTableData);
		yAxisTable.setLayout(new GridLayout());
		yAxisTable.getVerticalBar().setEnabled(true);

		yAxisTable.getVerticalBar().setEnabled(true);
		yAxisTable.getVerticalBar().setIncrement(1);
		yAxisTable.getVerticalBar().setThumb(1);

		for (yAxes y : AxisEnums.yAxes.values()) {
			TableItem t = new TableItem(yAxisTable, SWT.NONE);
			t.setText(y.getYAxisName());
			t.setChecked(true);
		}

		yAxisTable.getVerticalBar().setEnabled(true);

		Button apply = new Button(g, SWT.PUSH);
		apply.setText("Apply Settings");
		apply.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		apply.setEnabled(true);

		apply.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (TableItem ti : yAxisTable.getItems()) {
					AxisEnums.yAxes y = AxisEnums.toYAxis(ti.getText());
					bsmps.getBsya()[y.getYAxisNumber()].setUse(ti.getChecked());
				}

				bsmps.setUseQ(useQ.getSelection());
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

}