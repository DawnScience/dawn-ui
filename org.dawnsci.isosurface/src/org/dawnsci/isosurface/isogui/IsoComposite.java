package org.dawnsci.isosurface.isogui;

import org.dawnsci.plotting.util.ColorUtility;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class IsoComposite extends Composite
{
	
	private VerticalListEditor items;
	private IsoItemComposite itemComp;
	private ILazyDataset slice;
	private double min, max; // !! try and remove these, seem unnecessary
		
	private static int ISO_COUNT = 0; // keeps count the number of items created
	
	private boolean beanRemoved = false;
		
	public IsoComposite(Composite parent, int style, ILazyDataset slice)
	{
		super(parent, style);
		
		this.slice = slice;
		
		setLayout(new GridLayout(2, false));
		
		createContent();
	}
	
	/**
	 * Set the data sliced to be used by the job
	 * @param slice - The slice
	 */
	public void setSlice(ILazyDataset slice)
	{
		this.slice = slice;
	}
	
	/**
	 * create the gui content
	 */
	private void createContent()
	{
		this.items = new VerticalListEditor(this, SWT.NONE)
		{
			
			@Override
			protected void beanRemove(Object bean) // quick fix, makes me cry
			{
				((IsoItem)bean).deleteBean();
				notifyValueListeners();
			}
			
			@Override
			protected void beanAdd(Object bean)
			{
				
				// find default boxsize
				int[] defaultBoxSize= new int[] {
						(int) Math.max(1, Math.ceil(slice.getShape()[2]/20.0)),
                        (int) Math.max(1, Math.ceil(slice.getShape()[1]/20.0)),
                        (int) Math.max(1, Math.ceil(slice.getShape()[0]/20.0))};
				
				// set the initial data
				((IsoItem)bean).setInfo( 
						((min + max)/2),
						defaultBoxSize,
						0.5d,
						ColorUtility.GRAPH_DEFAULT_COLORS[ISO_COUNT++]);
				
				if (ISO_COUNT >= ColorUtility.GRAPH_DEFAULT_COLORS.length)
				{
					ISO_COUNT = 0;
				}
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
		
		this.items.setAdditionalFields(new String[] {"value", "opacity"});
		this.items.setColumnWidths(new int[] { 50, 50, 50, 50, 50, 50 });
		this.items.setShowAdditionalFields(true);
		
		ScrolledComposite sc = new ScrolledComposite(this, SWT.V_SCROLL);
		
		sc.setBackground(new Color(this.getDisplay(), new RGB(255, 0, 0)));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		
		itemComp = new IsoItemComposite(sc, SWT.NONE);
		
		itemComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		this.items.setEditorUI(itemComp);
		
		sc.setContent(itemComp);
		sc.setMinSize(itemComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
	}
	
	/**
	 * set the min and max iso value to be used
	 * @param min - The new minimum
	 * @param max - The new maximum
	 */
	public void setminMaxIsoValue(double min, double max) // !! look into changing
	{
		this.min = min;
		this.max = max;
		itemComp.setMinMaxIsoValue(min, max);
	}
		
	/**
	 * Get the list of items
	 * @return the item list
	 */
	public VerticalListEditor getItems()
	{
		return this.items;
	}

	
}


