package org.dawnsci.isosurface.tool;

import org.dawnsci.isosurface.Activator;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.richbeans.widgets.decorator.FloatDecorator;
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
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class IsoSurfaceTab extends Composite
{
	
	private static Spinner xDim, yDim, zDim;
	private Scale isovalue;
	private Text isoText;
	private final IsosurfaceJob job;
	
	private Color color;
	
	public IsoSurfaceTab(final ExpandBar parent, int style, IsosurfaceJob newJob)
	{
		super(parent, style);
		this.job = newJob;
		
		GridLayout layout = new GridLayout(6, false);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 20;
		layout.verticalSpacing = 10;
		this.setLayout(layout);
		
		Label isovalueLabel = new Label(this, SWT.NONE);
		isovalueLabel.setText("Isovalue");
		isovalueLabel.setToolTipText("Use the box at the end to enter an actual value or the left and right arrows to nudge.");
		
		isovalue = new Scale(this, SWT.NONE);
		isovalue.setMaximum(1000);
		isovalue.setMinimum(0);
		isovalue.setIncrement(1);
		isovalue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		isovalue.setToolTipText("Use the box at the end to enter an actual value or the left and right arrows to nudge.");
		
		isoText = new Text(this, SWT.BORDER);
		isoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		Label boxLabel = new Label(this, SWT.NONE);
		boxLabel.setText("Box Size   ");
		boxLabel.setToolTipText("The box size is the size of box used for the marching cubes algorithm.");
		
		zDim = new Spinner(this, SWT.BORDER);
		zDim.setMinimum(1);
		zDim.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		yDim = new Spinner(this, SWT.BORDER);
		yDim.setMinimum(1);
		yDim.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		xDim = new Spinner(this, SWT.BORDER);
		xDim.setMinimum(1);
		xDim.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		final Button decrease = new Button(this, SWT.PUSH);
		decrease.setToolTipText("Nudge whole box 10% smaller");
		decrease.setImage(Activator.getImage("icons/down.png").createImage());
		decrease.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));
		decrease.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				nudge(-0.1f, xDim, 2);
				nudge(-0.1f, yDim, 1);
				nudge(-0.1f, zDim, 0);
				MarchingCubesModel model = job.getGenerator().getModel();
				int[] boxSize = new int[] { xDim.getSelection(), yDim.getSelection(), zDim.getSelection() };
				model.setBoxSize(boxSize);
				job.compute();
			}
		});
		
		final Button increase = new Button(this, SWT.PUSH);
		increase.setToolTipText("Nudge whole box 10% larger");
		increase.setImage(Activator.getImage("icons/up.png").createImage());
		increase.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false));
		increase.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				nudge(0.1f, xDim, 2);
				nudge(0.1f, yDim, 1);
				nudge(0.1f, zDim, 0);
				
				MarchingCubesModel model = job.getGenerator().getModel();
				int[] boxSize = new int[] { xDim.getSelection(), yDim.getSelection(), zDim.getSelection() };
				model.setBoxSize(boxSize);
				job.compute();
			}
		});
		
		isovalue.addListener(SWT.Selection, new Listener()
		{
			
			@Override
			public void handleEvent(Event event)
			{
				int currentValue = isovalue.getSelection();
				
				MarchingCubesModel model = job.getGenerator().getModel();
				double isoVal = ((model.getIsovalueMax() - model.getIsovalueMin()) / 1000.0) * currentValue + model.getIsovalueMin();
				isoText.setText(String.valueOf(isoVal));
				model.setIsovalue(isoVal);
				job.compute();
			}
			
		});
		
		isoText.addModifyListener(new ModifyListener()
		{
			
			FloatDecorator floatText = new FloatDecorator(isoText);
			
			@Override
			public void modifyText(ModifyEvent e)
			{
				double currentValue = floatText.getValue().doubleValue();
				
				MarchingCubesModel model = job.getGenerator().getModel();
				isovalue.setSelection((int) ((currentValue - model.getIsovalueMin()) * 1000.0 / (model.getIsovalueMax() - model.getIsovalueMin())));
				model.setIsovalue(currentValue);
				job.compute();
			}
		});
		
		xDim.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int xSize = xDim.getSelection();
				
				MarchingCubesModel model = job.getGenerator().getModel();
				if (xSize > 0 && xSize < model.getLazyData().getShape()[2])
				{
					int[] boxSize = new int[] { xSize, model.getBoxSize()[1], model.getBoxSize()[2] };
					model.setBoxSize(boxSize);
				}
				job.compute();
			}
			
			public void widgetDefaultSelected(SelectionEvent e)
			{
				job.compute();
			}
		});
		
		yDim.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int ySize = yDim.getSelection();
				
				MarchingCubesModel model = job.getGenerator().getModel();
				if (ySize > 0 && ySize < model.getLazyData().getShape()[1])
				{
					int[] boxSize = new int[] { model.getBoxSize()[0], ySize, model.getBoxSize()[2] };
					model.setBoxSize(boxSize);
				}
				job.compute();
			}
			
			public void widgetDefaultSelected(SelectionEvent e)
			{
				job.compute();
			}
		});
		
		zDim.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int zSize = zDim.getSelection();
				
				MarchingCubesModel model = job.getGenerator().getModel();
				
				if (zSize > 0 && zSize < model.getLazyData().getShape()[0])
				{
					int[] boxSize = new int[] { model.getBoxSize()[0], model.getBoxSize()[1], zSize };
					model.setBoxSize(boxSize);
				}
				job.compute();
			}
			
			public void widgetDefaultSelected(SelectionEvent e)
			{
				job.compute();
			}
		});
		
		/*
		 * 
		 */
		
		color = new Color(parent.getShell().getDisplay(), new RGB(0, 255, 0));
		
		// Use a label full of spaces to show the color
		final Label colorLabel = new Label(this, SWT.NONE);
		colorLabel.setText("                              ");
		colorLabel.setBackground(color);
		
		Button button = new Button(this, SWT.PUSH);
		button.setText("Color...");
		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				
				// Create the color-change dialog
				ColorDialog dlg = new ColorDialog(parent.getShell());
				
				// Set the selected color in the dialog from
				// user's selected color
				dlg.setRGB(colorLabel.getBackground().getRGB());
				
				// Change the title bar text
				dlg.setText("Choose a Color");
				
				RGB rgb = dlg.open();
				if (rgb != null)
				{
					// Dispose the old color, create the
					// new one, and set into the label
					color.dispose();
					color = new Color(parent.getShell().getDisplay(), rgb);
					colorLabel.setBackground(color);
					
					MarchingCubesModel model = job.getGenerator().getModel();
					model.setColour(color.getRed(), color.getGreen(), color.getBlue());
					job.compute();
					
					
				}
				
			}
		});
		
		/*
		 * 
		 */
		
	}
	
	protected void nudge(float factor, Spinner spinner, int dim)
	{
		
		float amount = spinner.getSelection() * factor;
		if (0 < amount && amount < 1)
			amount = 1; // Increment less than 1 not much good.
		if (-1 < amount && amount < 0)
			amount = -1;
		
		float val = spinner.getSelection() + amount;
		final int size = job.getGenerator().getModel().getLazyData().getShape()[dim];
		if (val > size / 5f)
			val = Math.round(size / 5f);
		if (val < 1)
			val = 1;
		spinner.setSelection(Math.round(val));
	}
	
	protected void updateUI()
	{
		try
		{
			
			MarchingCubesModel model = job.getGenerator().getModel();
			final ILazyDataset set = model.getLazyData();
			final int[] shape = set.getShape();
			
			isovalue.setSelection((int) ((model.getIsovalue() - model.getIsovalueMin()) * 1000 / (model.getIsovalueMax() - model.getIsovalueMin())));
			isoText.setText(String.valueOf(model.getIsovalue()));
			
			xDim.setMaximum(shape[2] / 5);
			xDim.setToolTipText("1 - " + shape[2]);
			xDim.setSelection(model.getBoxSize()[0]);
			
			yDim.setMaximum(shape[1] / 5);
			xDim.setToolTipText("1 - " + shape[1]);
			yDim.setSelection(model.getBoxSize()[1]);
			
			zDim.setMaximum(shape[0] / 5);
			xDim.setToolTipText("1 - " + shape[0]);
			zDim.setSelection(model.getBoxSize()[2]);
			
		}
		finally
		{
			
		}
	}

	
	/*
	 *  gets
	 */
	
	
	public IsosurfaceJob getJob() // !! might not be very safe !!
	{
		return this.job;
	}
		
}
