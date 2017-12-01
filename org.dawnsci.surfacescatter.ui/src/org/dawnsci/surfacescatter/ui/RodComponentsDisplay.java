
package org.dawnsci.surfacescatter.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class RodComponentsDisplay extends Composite {

	private SurfaceScatterPresenter ssp;
	private Table displayTable;

	public RodComponentsDisplay(Composite parent, int style, SurfaceScatterPresenter ssp) {

		super(parent, style);
		this.ssp = ssp;
		this.createContents();

	}

	public void createContents() {

		Label componentFiles = new Label(this, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		componentFiles.setText("Components: ");

		componentFiles.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		displayTable = new Table(this, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI);
		displayTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		displayTable.setLinesVisible(true);

		String[] pop = new String[] { "None" };

		try {
			pop = ssp.getDrm().getDatFilepaths();
		} catch (Exception j) {

		}
		populateTable(pop);

	}

	public Composite getComposite() {
		return this;
	}

	public void populateTable(String[] r) {

		displayTable.removeAll();

		for (String s : r) {

			TableItem t = new TableItem(displayTable, SWT.NONE);
			t.setText(s);

		}

	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;

		String[] pop = new String[] { "None" };

		try {
			pop = ssp.getDrm().getDatFilepaths();
		} catch (Exception j) {

		}
		populateTable(pop);

	}
}
