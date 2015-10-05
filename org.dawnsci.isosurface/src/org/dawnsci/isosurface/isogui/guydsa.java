package org.dawnsci.isosurface.isogui;

import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.custom.ScrolledComposite;

public class guydsa extends ScrolledComposite
{
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public guydsa(Composite parent, int style)
	{
		super(parent, SWT.V_SCROLL);
		// generate the GUI
		
				GridLayout gridLayout = new GridLayout(5, false);
				gridLayout.verticalSpacing = 0;
				gridLayout.horizontalSpacing = 0;
				gridLayout.marginWidth = 0;
				gridLayout.marginHeight = 0;
				setLayout(gridLayout);
				
				Label nameLabel = new Label(this, SWT.NONE);
				nameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				nameLabel.setText("Name:");
				
				TextWrapper name = new TextWrapper(this, SWT.BORDER);
				name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
				
				Label lblIsosurfaceValue = new Label(this, SWT.NONE);
				lblIsosurfaceValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				lblIsosurfaceValue.setText("Isosurface Value");
				
				Scale isoSurfaceScaleValue = new Scale(this, SWT.NONE);
				GridData gd_scale = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
				gd_scale.widthHint = 151;
				isoSurfaceScaleValue.setLayoutData(gd_scale);
				
				ScaleBox value = new ScaleBox(this, SWT.NONE);
				GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
				gd_text.widthHint = 55;
				value.setLayoutData(gd_text);
				
				Label lblCubeSize = new Label(this, SWT.NONE);
				lblCubeSize.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				lblCubeSize.setText("Cube Size");
				
				Spinner x = new Spinner(this, SWT.BORDER);
				x.setEnabled(false);
				x.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				
				Spinner y = new Spinner(this, SWT.BORDER);
				y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				
				Spinner z = new Spinner(this, SWT.BORDER);
				z.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				new Label(this, SWT.NONE);
				
				Label lblColour = new Label(this, SWT.NONE);
				lblColour.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				lblColour.setText("Colour");
				 
				Button colour = new Button(this, SWT.NONE);
				
				new Label(this, SWT.NONE);
				new Label(this, SWT.NONE);
				new Label(this, SWT.NONE);
				
				Label lblOpacity = new Label(this, SWT.NONE);
				lblOpacity.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				lblOpacity.setText("Opacity");
				
				Scale opacity = new Scale(this, SWT.NONE);
				new Label(this, SWT.NONE);
				new Label(this, SWT.NONE);
				GridData gd_scale_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
				gd_scale_1.widthHint = 152;
				
	}
	
	@Override
	protected void checkSubclass()
	{
		// Disable the check that prevents subclassing of SWT components
	}
	
}
