package org.dawnsci.isosurface.testingBeans;

import org.dawnsci.isosurface.Activator;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.BoundsProvider;
import org.eclipse.richbeans.widgets.decorator.FloatDecorator;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class testItemComposite extends Composite
{
	
	private Text text;
	
	private Color color;
	
	public testItemComposite(final Composite parent, int style) 
	{	
		super(parent, style);
		
		createUI();
	}
	
	private void createUI()
	{
		
		setLayout(new GridLayout(5, false));
		
		Label lblIsosurfaceValue = new Label(this, SWT.NONE);
		lblIsosurfaceValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblIsosurfaceValue.setText("isosurface value");
		
		Scale scale = new Scale(this, SWT.NONE);
		GridData gd_scale = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_scale.widthHint = 151;
		scale.setLayoutData(gd_scale);
		
		text = new Text(this, SWT.BORDER);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 55;
		text.setLayoutData(gd_text);
		
		Label lblCubeSize = new Label(this, SWT.NONE);
		lblCubeSize.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblCubeSize.setText("Cube Size");
		
		Spinner spinner = new Spinner(this, SWT.BORDER);
		
		Spinner spinner_1 = new Spinner(this, SWT.BORDER);
		
		Spinner spinner_2 = new Spinner(this, SWT.BORDER);
		new Label(this, SWT.NONE);
		
		Label lblColour = new Label(this, SWT.NONE);
		lblColour.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblColour.setText("Colour");
		
		ColorSelector colourSelector = new ColorSelector(this);
		colourSelector.setColorValue(new RGB(0, 0, 0));
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		Label lblOpacity = new Label(this, SWT.NONE);
		lblOpacity.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblOpacity.setText("Opacity");
		Scale scale_1 = new Scale(this, SWT.NONE);
		GridData gd_scale_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_scale_1.widthHint = 152;
		scale_1.setLayoutData(gd_scale_1);
		
		Button btnOn = new Button(this, SWT.RADIO);
		btnOn.setText("On");
		
	}

	public void updateVisibility() {
		
	}
		
	
}
