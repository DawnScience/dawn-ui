package org.dawnsci.common.richbeans.examples.example7.ui;

import org.dawnsci.common.richbeans.components.selector.BeanSelectionEvent;
import org.dawnsci.common.richbeans.components.selector.BeanSelectionListener;
import org.dawnsci.common.richbeans.components.selector.VerticalListEditor;
import org.dawnsci.common.richbeans.examples.example7.data.ExampleItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
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
		
		setLayout(new GridLayout(1, false));
		
	
		// List of ExampleItems
		this.items = new VerticalListEditor(this, SWT.NONE);
		items.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		items.setMinItems(0);
		items.setMaxItems(25);
		items.setDefaultName("NewItem");
		items.setEditorClass(ExampleItem.class);
		items.setNameField("itemName"); // Where the name comes from if inside the bean
		items.setListHeight(80);
		items.setRequireSelectionPack(false);
		
		// Show the values of another field than itemName in the table.
		items.setAdditionalFields(new String[]{"choice"});
		items.setColumnWidths(new int []{100, 150});
		items.setShowAdditionalFields(true);
		
		final ScrolledComposite comp = new ScrolledComposite(this, SWT.V_SCROLL|SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
		data.heightHint = 300;
		comp.setLayoutData(data);
		
		final ExampleItemComposite itemComp = new ExampleItemComposite(comp, SWT.NONE);
		items.setEditorUI(itemComp);

		// In order to update the conditional visibility you have to implement 
		// a bean listener which will manually update value.
		// This design allows control of when UI visibility is actually changed.
		items.addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				itemComp.updateVisibility();
			}
		});
		
		comp.setContent(itemComp);
		comp.setExpandHorizontal(true);
		comp.setExpandVertical(true);
		comp.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle r = comp.getClientArea();
				comp.setMinSize(comp.computeSize(r.width, SWT.DEFAULT));
			}
		});

	}

	public VerticalListEditor getItems() {
		return items;
	}
}
