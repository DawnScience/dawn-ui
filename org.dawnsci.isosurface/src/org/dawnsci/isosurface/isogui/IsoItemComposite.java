package org.dawnsci.isosurface.isogui;

import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.BoundsProvider;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.ColorSelectorWrapper;
import org.eclipse.richbeans.widgets.wrappers.ScaleWrapper;
import org.eclipse.richbeans.widgets.wrappers.SpinnerWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class IsoItemComposite extends Composite
{
	private TextWrapper name;
	private NumberBox value;
	private ScaleWrapper opacity;
	private SpinnerWrapper x, y, z;
	private ColorSelectorWrapper colour;
	
	public IsoItemComposite(Composite parent, int style)
	{
		super(parent, SWT.FILL);
		createUI();
	}
	
	/**
	 * Generate the UI to edit the Item
	 */
	private void createUI()
	{
		
		// generate the GUI
		GridLayout gridLayout = new GridLayout(7, false);
		setLayout(gridLayout);
				
		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		nameLabel.setText("Name ");
		
		name = new TextWrapper(this, SWT.BORDER);
		GridData gridDataText = new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1);
		name.setLayoutData(gridDataText);

		new Label(this, SWT.NONE);
		
		Label lblIsosurfaceValue = new Label(this, SWT.NONE);
		lblIsosurfaceValue.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblIsosurfaceValue.setText("Isosurface Value ");
		
		value = new ScaleBox(this, SWT.NONE);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1);
		gd_text.widthHint = 55;
		value.setLayoutData(gd_text);
		value.setButtonVisible(true);
		
		new Label(this, SWT.NONE);
		
		Label lblCubeSize = new Label(this, SWT.NONE);
		lblCubeSize.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblCubeSize.setText("Cube Size ");
		
		x = new SpinnerWrapper(this, SWT.BORDER);
		GridData xgd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		xgd.widthHint = 60;
		x.setLayoutData(xgd);
		x.setMaximum(999);
		x.setMinimum(1);
		
		y = new SpinnerWrapper(this, SWT.BORDER);
		GridData ygd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		ygd.widthHint = 60;
		y.setLayoutData(ygd);
		y.setMaximum(999);
		y.setMinimum(1);
		
		z = new SpinnerWrapper(this, SWT.BORDER);
		GridData zgd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		zgd.widthHint = 60;
		z.setLayoutData(zgd);
		z.setMaximum(999);
		z.setMinimum(1);
		
		Button upButton = new Button(this, SWT.NONE);
		upButton.setImage(IsoGUIUtil.getImageDescriptor("up.png").createImage());
		upButton.addSelectionListener(new SelectionListener() {
			
			// add one to the cube size if possible
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				x.setValue((int)x.getValue() + 1);
				y.setValue((int)y.getValue() + 1);
				z.setValue((int)z.getValue() + 1);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		Button downButton = new Button(this, SWT.NONE);
		downButton.setImage(IsoGUIUtil.getImageDescriptor("down.png").createImage());
		downButton.addSelectionListener(new SelectionListener() {
			
			// add one to the cube size if possible
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				x.setValue(((int)x.getValue() - 1 >= 1) ? (int)x.getValue() - 1 : 1);
				y.setValue(((int)y.getValue() - 1 >= 1) ? (int)y.getValue() - 1 : 1);
				z.setValue(((int)z.getValue() - 1 >= 1) ? (int)z.getValue() - 1 : 1);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		new Label(this, SWT.NONE);
		
		Label lblColour = new Label(this, SWT.NONE);
		lblColour.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblColour.setText("Colour ");
		 
		colour = new ColorSelectorWrapper(this, SWT.NONE);
		colour.setValue(new RGB(255, 0, 0));
		
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		Label lblOpacity = new Label(this, SWT.NONE);
		lblOpacity.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblOpacity.setText("Opacity ");
		
		opacity = new ScaleWrapper(this, SWT.NONE);
		GridData gd_scale_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1);
		gd_scale_1.widthHint = 152;
		opacity.setLayoutData(gd_scale_1);
		opacity.setMaximumScale(100);
		opacity.setMaximumValue(1);
		opacity.setMinimumValue(0);
		
	}
		
	public IFieldWidget getName()
	{
		return this.name;
	}	
	public IFieldWidget getValue()
	{
		return this.value;
	}	
	public IFieldWidget getX()
	{
		return this.x;
	}
	public IFieldWidget getY()
	{
		return this.y;
	}
	public IFieldWidget getZ()
	{
		return this.z;
	}
	public IFieldWidget getOpacity()
	{
		return this.opacity;
	}
	public void setOpacity(Object newValue)
	{
		this.opacity = (ScaleWrapper) newValue;
	}
	public IFieldWidget getColour()
	{
		return this.colour;
	}
	
	// !! look into
	public void setMinMaxIsoValue(final double min, final double max)
	{		
		value.setMaximum(new BoundsProvider()
		{
			@Override
			public double getBoundValue()
			{
				return max;
			}
			
			@Override
			public void addValueListener(ValueListener l)
			{
				// do not add a value listener
			}
		});
		
		value.setMinimum(new BoundsProvider()
		{
			@Override
			public double getBoundValue()
			{
				return min;
			}
			
			@Override
			public void addValueListener(ValueListener l)
			{
				// do not add a value listener
			}
		});
	}

	
	
	
	
	
}





