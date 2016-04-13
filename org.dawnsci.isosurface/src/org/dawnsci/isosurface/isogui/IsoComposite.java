package org.dawnsci.isosurface.isogui;

import org.dawnsci.plotting.util.ColorUtility;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class IsoComposite extends Composite
{
	
	private VerticalListEditor items;
	private IsoItemComposite itemComp;
	private double min, max; // !! try and remove these, seem unnecessary
	private int[] defaultCubeSize;
		
	private static int ISO_COUNT = 0; // keeps count the number of items created
	
	/**
	 * 
	 * @param parent - The parent composite
	 * @param style - The SWT style used
	 * @param slice - The dataslice used
	 */
	public IsoComposite(Composite parent, int style)
	{
		super(parent, style);
		
		setLayout(new GridLayout(2, false));
		
		createContent();
	}
	
	
	/**
	 * Internal use. Generates the content of the composite.
	 * 
	 */
	private void createContent()
	{
		this.items = new VerticalListEditor(this, SWT.NONE)
		{
			// overriding the methods to give this class some more flexibility on bean creation and deletion
			@Override
			protected void beanRemove(Object bean) // quick fix, makes me cry
			{
				((IsoItem)bean).deleteBean();
				notifyValueListeners();
			}
			
			@Override
			protected void beanAdd(Object bean)
			{
				// set the initial data
				((IsoItem)bean).setInfo( 
						((min + max)/2),
						defaultCubeSize,
						0.5d,
						ColorUtility.GRAPH_DEFAULT_COLORS[ISO_COUNT++]);
				
				if (ISO_COUNT >= ColorUtility.GRAPH_DEFAULT_COLORS.length)
				{
					ISO_COUNT = 0;
				}
			}
		};
		
		this.items.setAddButtonImage(IsoGUIUtil.getImageDescriptor("add.png").createImage());
		this.items.setDeleteButtonImage(IsoGUIUtil.getImageDescriptor("delete.png").createImage());
		
		this.items.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		this.items.setMinItems(0);
		this.items.setMaxItems(25);
		this.items.setDefaultName("New IsoSurface");
		this.items.setEditorClass(IsoItem.class);
		this.items.setNameField("name"); // Where the name comes from if inside the bean
		
		this.items.setListHeight(80);
		this.items.setRequireSelectionPack(false);
		
		this.items.setAdditionalFields(new String[] {"value", "opacity"});
		this.items.setColumnWidths(new int[] {125, 125, 125});
		this.items.setShowAdditionalFields(true);
				
		itemComp = new IsoItemComposite(this);
		
		itemComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		this.items.setEditorUI(itemComp);		
		}
		
	/**
	 * Set the min and max iso value to be used
	 * @param minMax - minMax[0] new minimum, minMax[1] new maximum
	 * @param defaultCubeSize - The new default cube size.
	 */
	public void setMinMaxIsoValueAndCubeSize(double[] minMax, int[] defaultCubeSize) // !! look into changing
	{
		this.min = minMax[0];
		this.max = minMax[1];
		this.defaultCubeSize = defaultCubeSize;
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
	
	// pretty much only used to create the inital surface bean
	public void addNewSurface()
	{
		// items.addBean();
	}

	
}


