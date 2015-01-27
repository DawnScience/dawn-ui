package org.dawnsci.common.richbeans.examples.example1;

import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class SimpleComposite extends Composite {

	private ScaleBox x,y;

	public SimpleComposite(Composite parent, int style) {
		super(parent, style);
		createContent();
	}

	private void createContent() {
		
		setLayout(new GridLayout(2, false));
		
		Label label = new Label(this, SWT.NONE);
		label.setText("x");
		
		x = new ScaleBox(this, SWT.NONE);
		x.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		x.setUnit("°");
		x.setMinimum(0);
		x.setMaximum(1000);
		
		label = new Label(this, SWT.NONE);
		label.setText("y");
		
		y = new ScaleBox(this, SWT.NONE);
		y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		y.setIntegerBox(true);
		y.setUnit("m");
		y.setMinimum(0);
		y.setMaximum(x);
	}

	
	public IFieldWidget getX() {
		return x;
	}
	
	public IFieldWidget getY() {
		return y;
	}

}
