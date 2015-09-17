package org.dawnsci.isosurface.testingBeans;

import org.eclipse.richbeans.widgets.selector.BeanSelectionEvent;
import org.eclipse.richbeans.widgets.selector.BeanSelectionListener;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class testComposite extends Composite
{
	
	private VerticalListEditor items;
	
	public testComposite(Composite parent, int style)
	{
		super(parent, style);
		parent.setBackground(parent.getDisplay()
				.getSystemColor(SWT.COLOR_GREEN));
		
		setLayout(new GridLayout(2, false));
		
		createContent();
	}
	
	private void createContent()
	{
		this.items = new VerticalListEditor(this, SWT.NONE);
		items.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		items.setMinItems(0);
		items.setMaxItems(25);
		items.setDefaultName("New IsoSurface");
		items.setEditorClass(testItem.class);
		items.setNameField("name"); // Where the name comes from if inside the
									// bean
		
		items.setListHeight(80);
		items.setRequireSelectionPack(false);
		
		items.setAdditionalFields(new String[] { "value", "opacity", "x", "y",
				"z", "colour" });
		items.setColumnWidths(new int[] { 50, 50, 50, 50, 50, 50 });
		items.setShowAdditionalFields(true);
		
		items.setBackground(items.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		
		final testItemComposite itemComp = new testItemComposite(this, SWT.NONE);
		
		itemComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2,
				1));
		
		items.setEditorUI(itemComp);
		
		items.addBeanSelectionListener(new BeanSelectionListener()
		{
			@Override
			public void selectionChanged(BeanSelectionEvent evt)
			{
				itemComp.updateVisibility();
			}
		});
		
	}
	
	public VerticalListEditor getItems()
	{
		return this.items;
	}
	
}
