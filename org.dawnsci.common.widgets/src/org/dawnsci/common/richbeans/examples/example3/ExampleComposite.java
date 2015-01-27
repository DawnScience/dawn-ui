package org.dawnsci.common.richbeans.examples.example3;

import org.dawnsci.common.richbeans.components.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ExampleComposite extends Composite {

	private VerticalListEditor items;

	public ExampleComposite(Composite parent, int style) {
		super(parent, style);
		createContent();
	}

	private void createContent() {
		
		setLayout(new GridLayout(2, false));
	
		// List of ExampleItems
		this.items = new VerticalListEditor(this, SWT.NONE);
		items.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		items.setMinItems(0);
		items.setMaxItems(25);
		items.setDefaultName("NewItem");
		items.setEditorClass(ExampleItem.class);
		items.setNameField("itemName"); // Where the name comes from if inside the bean
		items.setListHeight(80);
		items.setRequireSelectionPack(false);
		items.setAdditionalFields(new String[]{"choice"});
		items.setColumnWidths(new int []{100, 150});
		items.setShowAdditionalFields(true);
		
		final ExampleItemComposite itemComp = new ExampleItemComposite(this, SWT.NONE);
		itemComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		items.setEditorUI(itemComp);

	}

	public VerticalListEditor getItems() {
		return items;
	}
}
