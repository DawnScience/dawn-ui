
package org.dawnsci.surfacescatter.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class GeometricParametersRepeaterTable extends Composite {

	
	private SurfaceScatterPresenter ssp;
	private GeometricParametersWindows gpw;

	public GeometricParametersRepeaterTable(Composite parent, int style, SurfaceScatterPresenter ssp,
			SurfaceScatterViewStart ssvs) {

		super(parent, style);
		this.ssp = ssp;
		this.createContents();
		

	}

	public void createContents() {
		
		gpw= new GeometricParametersWindows(this, SWT.FILL, ssp);
		gpw.setLayout(new GridLayout());
		gpw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		

	
	}

	public Composite getComposite() {
		return this;
	}

	
	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
		gpw.setSsp(ssp);
	}
}
