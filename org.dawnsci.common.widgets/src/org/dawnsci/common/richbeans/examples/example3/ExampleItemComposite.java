package org.dawnsci.common.richbeans.examples.example3;

import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.components.BoundsProvider;
import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.dawnsci.common.richbeans.components.wrappers.ComboWrapper;
import org.dawnsci.common.richbeans.components.wrappers.TextWrapper;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.event.ValueListener;
import org.dawnsci.common.richbeans.examples.example3.ExampleItem.ItemChoice;
import org.dawnsci.common.richbeans.internal.GridUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ExampleItemComposite extends Composite {

	private TextWrapper  itemName;
	private ScaleBox     x,y;
	private ComboWrapper choice;
	private ScaleBox     r,theta;

	public ExampleItemComposite(Composite parent, int style) {
		super(parent, style);
		createContent();
	}

	private void createContent() {
		
		setLayout(new GridLayout(2, false));
		
		Label label = new Label(this, SWT.NONE);
		label.setText("Name");
	
		this.itemName = new TextWrapper(this, SWT.BORDER);
		itemName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		label = new Label(this, SWT.NONE);
		label.setText("Coordinate System");
		
		choice = new ComboWrapper(this, SWT.READ_ONLY);
		choice.setItems(ExampleItem.ItemChoice.names());
		choice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		choice.addValueListener(new ValueAdapter("Conditional Visibility Example") {	
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateVisibility();
			}
		});
		choice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		x = new ScaleBox(this, SWT.NONE);
		x.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		x.setLabel("x     ");
		x.setUnit("m");
		x.setMinimum(0);
		x.setMaximum(1000);
				
		y = new ScaleBox(this, SWT.NONE);
		y.setLabel("y     ");
		y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		y.setUnit("m");
		y.setMinimum(0);
		y.setMaximum(new BoundsProvider() {
			
			@Override
			public double getBoundValue() {
				return x.getNumericValue()*10;
			}
			
			@Override
			public void addValueListener(ValueListener l) {
				x.addValueListener(l);
			}
		});
		
		r = new ScaleBox(this, SWT.NONE);
		r.setLabel("Radius      ");
		r.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		r.setUnit("m");
		r.setMinimum(0);
		r.setMaximum(1000);
		
		theta = new ScaleBox(this, SWT.NONE);
		theta.setLabel("Angle (θ)   ");
		theta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		theta.setUnit("rad");
		theta.setMinimum(0);
		theta.setMaximum(2*Math.PI);

		GridUtils.setVisible(r,     false);
		GridUtils.setVisible(theta, false);

	}

	public IFieldWidget getItemName() {
		return itemName;
	}
	
	public IFieldWidget getX() {
		return x;
	}
	
	public IFieldWidget getY() {
		return y;
	}
	
	public IFieldWidget getR() {
		return r;
	}
	
	public IFieldWidget getTheta() {
		return theta;
	}
	
	public IFieldWidget getChoice() {
		return choice;
	}

	void updateVisibility() {
		GridUtils.setVisible(x,     choice.getValue() == ItemChoice.XY);
		GridUtils.setVisible(y,     choice.getValue() == ItemChoice.XY);
		GridUtils.setVisible(r,     choice.getValue() == ItemChoice.POLAR);
		GridUtils.setVisible(theta, choice.getValue() == ItemChoice.POLAR);
		layout();
	}

}
