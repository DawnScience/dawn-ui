package org.dawnsci.common.richbeans.examples.example2.ui;

import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.dawnsci.common.richbeans.components.selector.VerticalListEditor;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper;
import org.dawnsci.common.richbeans.components.wrappers.TextWrapper;
import org.dawnsci.common.richbeans.examples.example2.data.ExampleItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ExampleComposite extends Composite {

	private ScaleBox stop;
	private ScaleBox start;
	private TextWrapper edge;
	private TextWrapper element;
	private VerticalListEditor items;

	public ExampleComposite(Composite parent, int style) {
		super(parent, style);
		createContent();
	}

	private void createContent() {
		
		setLayout(new GridLayout(2, false));
	
		final Label elementLabel = new Label(this, SWT.NONE);
		elementLabel.setText("Element");

		element = new TextWrapper(this, SWT.BORDER);
		final GridData gd_element = new GridData(SWT.FILL, SWT.CENTER, true, false);
		element.setLayoutData(gd_element);

		final Label edgeLabel = new Label(this, SWT.NONE);
		edgeLabel.setText("Edge");

		edge = new TextWrapper(this, SWT.BORDER);
		final GridData gd_edge = new GridData(SWT.FILL, SWT.CENTER, true, false);
		edge.setLayoutData(gd_edge);

		final Label startLabel = new Label(this, SWT.NONE);
		startLabel.setText("Start");

		start = new ScaleBox(this, SWT.NONE);
		final GridData gd_start = new GridData(SWT.FILL, SWT.CENTER, false, false);
		start.setLayoutData(gd_start);

		final Label stopLabel = new Label(this, SWT.NONE);
		stopLabel.setText("Stop");

		stop = new ScaleBox(this, SWT.NONE);
		final GridData gd_stop = new GridData(SWT.FILL, SWT.CENTER, false, false);
		stop.setLayoutData(gd_stop);
		
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
		
		final ExampleItemComposite itemComp = new ExampleItemComposite(this, SWT.NONE);
		itemComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		items.setEditorUI(itemComp);

	}

	public VerticalListEditor getItems() {
		return items;
	}

	/**
	 * @return the element
	 */
	public TextWrapper getElement() {
		return element;
	}
	/**
	 * @return the edge
	 */
	public TextWrapper getEdge() {
		return edge;
	}
	/**
	 * @return the start
	 */
	public ScaleBox getStart() {
		return start;
	}
	/**
	 * @return the stop
	 */
	public ScaleBox getStop() {
		return stop;
	}

}
