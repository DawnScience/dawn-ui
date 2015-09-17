package org.dawnsci.isosurface.testingBeans;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.ColorSelectorWrapper;
import org.eclipse.richbeans.widgets.wrappers.ScaleWrapper;
import org.eclipse.richbeans.widgets.wrappers.SpinnerWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class testItemComposite extends Composite
{
	private TextWrapper name;
	private ScaleBox value;
	private ScaleWrapper isoSurfaceScaleValue;
	private ScaleWrapper opacity;
	private SpinnerWrapper x, y, z;
	private ColorSelectorWrapper colour;
		
	public testItemComposite(final Composite parent, int style) 
	{	
		super(parent, style);
		
		createUI();
	}
	
	private void createUI()
	{
		
		setLayout(new GridLayout(5, false));
		
		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		nameLabel.setText("Name:");
		
		name = new TextWrapper(this, SWT.BORDER);
		GridData gridDataText = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		name.setLayoutData(gridDataText);
		
		Label lblIsosurfaceValue = new Label(this, SWT.NONE);
		lblIsosurfaceValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblIsosurfaceValue.setText("Isosurface Value");
		
		isoSurfaceScaleValue = new ScaleWrapper(this, SWT.NONE);
		GridData gd_scale = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
		gd_scale.widthHint = 151;
		isoSurfaceScaleValue.setLayoutData(gd_scale);
		
		value = new ScaleBox(this, SWT.NONE);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_text.widthHint = 55;
		value.setLayoutData(gd_text);
		
		Label lblCubeSize = new Label(this, SWT.NONE);
		lblCubeSize.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblCubeSize.setText("Cube Size");
		
		x = new SpinnerWrapper(this, SWT.BORDER);
		x.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		y = new SpinnerWrapper(this, SWT.BORDER);
		y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		z = new SpinnerWrapper(this, SWT.BORDER);
		z.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(this, SWT.NONE);
		
		Label lblColour = new Label(this, SWT.NONE);
		lblColour.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblColour.setText("Colour");
		 
		colour = new ColorSelectorWrapper(this, SWT.NONE);
		colour.setValue(new RGB(255, 0, 0));
		
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		Label lblOpacity = new Label(this, SWT.NONE);
		lblOpacity.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblOpacity.setText("Opacity");
		
		opacity = new ScaleWrapper(this, SWT.NONE);
		GridData gd_scale_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
		gd_scale_1.widthHint = 152;
		opacity.setLayoutData(gd_scale_1);
		
//		Button btnOn = new Button(this, SWT.RADIO);
//		btnOn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		btnOn.setText("On");
		
	}
		
	public IFieldWidget getName()
	{
		return this.name;
	}
	
	public IFieldWidget getValue()
	{
		return this.value;
	}
	
	public IFieldWidget getIsoSurfaceScaleValue()
	{
		return this.isoSurfaceScaleValue;
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
	public IFieldWidget getColour()
	{
		return this.colour;
	}
	
	
	
	
//	public IFieldWidget getColour()
//	{
//		return this.colour;
//	}
//	
//	public 
//	
//	private double opacity;
//	private int[] cubeSize;
//	private RGB colour;
		
	
	

	public void updateVisibility() {
		
	}
		
	
}
