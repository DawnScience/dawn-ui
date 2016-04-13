package org.dawnsci.isosurface.isogui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.ColorSelectorWrapper;
import org.eclipse.richbeans.widgets.wrappers.ScaleWrapper;
import org.eclipse.richbeans.widgets.wrappers.SpinnerWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class IsoItemComposite extends Composite
{
	private NumberBox value;
	private ScaleWrapper opacity;
	private SpinnerWrapper x, y, z;
	private ColorSelectorWrapper colour;
	
	public IsoItemComposite(Composite parent)
	{
		super(parent, SWT.FILL);
		createUI();
	}
	
	/**
	 * Generate the UI to edit the Item
	 */
	private void createUI()
	{
		setLayout(new GridLayout(7, false));
						
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
		lblCubeSize.setText("Cuboid Size ");
		
		x = createSpinner();
		y = createSpinner();
		z = createSpinner();

		List<IFieldWidget> cubeSizeWidgets = Arrays.asList(x,y,z);
		
		Button upButton = new Button(this, SWT.NONE);
		upButton.setImage(IsoGUIUtil.getImageDescriptor("up.png").createImage());
		upButton.addSelectionListener(new IncrementGroupSelectionListener(cubeSizeWidgets, 1));
		
		Button downButton = new Button(this, SWT.NONE);
		downButton.setImage(IsoGUIUtil.getImageDescriptor("down.png").createImage());
		downButton.addSelectionListener(new IncrementGroupSelectionListener(cubeSizeWidgets, -1));
		
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

	private SpinnerWrapper createSpinner() {
		SpinnerWrapper widget = new SpinnerWrapper(this, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 60;
		widget.setLayoutData(gridData);
		widget.setMinimum(1);
		widget.setMaximum(999);
		return widget;
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
	
	public void setMinMaxIsoValue(final double min, final double max)
	{		
		value.setMinimum(min);
		value.setMaximum(max);
	}
}





