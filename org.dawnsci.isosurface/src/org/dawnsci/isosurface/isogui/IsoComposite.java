package org.dawnsci.isosurface.isogui;

import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class IsoComposite extends Composite
{
	
	private VerticalListEditor items;
	private IPlottingSystem system;
	private IsoItemComposite itemComp;
	private ILazyDataset slice;
	
	private static int JOB_COUNT = 0;
	
	public IsoComposite(Composite parent, int style, IPlottingSystem system, ILazyDataset slice)
	{
		super(parent, style);
		
		this.slice = slice;
		this.system = system;
		setLayout(new GridLayout(2, false));
		
		createContent();
	}
	
	public void setSlice(ILazyDataset slice)
	{
		this.slice = slice;
	}
	
	private void createContent()
	{
		this.items = new VerticalListEditor(this, SWT.NONE)
		{
			
			// destroy the bean and :. remove the isosurface from javafx
			@Override
			protected void beanRemove(Object bean) 
			{
				((IsoItem)bean).destroy();
			}
			
			// set the job of the bean
			@Override
			protected void beanAdd(Object bean) 
			{
				int[] defaultBoxSize= new int[] {
						(int) Math.max(1, Math.ceil(slice.getShape()[2]/20.0)),
                        (int) Math.max(1, Math.ceil(slice.getShape()[1]/20.0)),
                        (int) Math.max(1, Math.ceil(slice.getShape()[0]/20.0))};
				
				
				((IsoItem)bean).setInfo(
						new IsosurfaceJob("isosurface job - #" + JOB_COUNT++, system, slice), 
						0,
						defaultBoxSize,
						0.5f);
			}
			
		};
		
		this.items.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		this.items.setMinItems(0);
		this.items.setMaxItems(25);
		this.items.setDefaultName("New IsoSurface");
		this.items.setEditorClass(IsoItem.class);
		this.items.setNameField("name"); // Where the name comes from if inside the bean
		
		this.items.setListHeight(80);
		this.items.setRequireSelectionPack(false);
		
		this.items.setAdditionalFields(new String[] { "value", "opacity", "x", "y",
				"z", "colour" });
		this.items.setColumnWidths(new int[] { 50, 50, 50, 50, 50, 50 });
		this.items.setShowAdditionalFields(true);
				
		itemComp = new IsoItemComposite(this, SWT.NONE);
		
		itemComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		this.items.setEditorUI(itemComp);
		
	}
	
	public void setminMaxIsoValue(double min, double max)
	{
		itemComp.setMinMaxIsoValue(min, max);
	}
		
	
	public VerticalListEditor getItems()
	{
		return this.items;
	}

	
	
}


