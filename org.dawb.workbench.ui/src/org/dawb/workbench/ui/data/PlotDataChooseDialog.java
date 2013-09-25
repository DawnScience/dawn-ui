package org.dawb.workbench.ui.data;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Class for choosing error on a plotted dataset.
 * 
 * Can be used for setting error for instance.
 * 
 * @author fcp94556
 *
 */
class PlotDataChooseDialog extends Dialog {

	private List<ITransferableDataObject> plottedSelections;
	private ITransferableDataObject error, selection;

	public PlotDataChooseDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	public void init(List<ITransferableDataObject> selections, ITransferableDataObject error) {
		this.plottedSelections = selections;
		this.error     = error;
		this.selection = selections.get(0); // Throws index and NPE if selections empty or null.
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout  = new GridLayout(2, false);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(composite);
		
		// Add a choice list to choose the existing data. 
		Label label = new Label(composite, SWT.WRAP);
		label.setText("Please choose the plotted data to which the error '"+error.getName()+"' should be assigned.");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1));
		
		label = new Label(composite, SWT.WRAP);
		label.setText("Plotted data ");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		
		final Combo combo = new Combo(composite, SWT.READ_ONLY|SWT.BORDER);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		combo.setItems(getSelectionNames());
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final String name = combo.getItem(combo.getSelectionIndex());
				selection = getSelectionByName(name);
			}
		});
		combo.select(0);
		
		return composite;
	}
	
	protected ITransferableDataObject getSelectionByName(String name) {
		for (ITransferableDataObject ob : plottedSelections) {
			if (name.equals(ob.getName())) return ob;
		}
		return null;
	}

	private String[] getSelectionNames() {
		final List<String> names = new ArrayList<String>(plottedSelections.size());
		for (ITransferableDataObject ob : plottedSelections) names.add(ob.getName());
		return names.toArray(new String[names.size()]);
	}

	/**
	 * Opens the dialog and returns the chosen ICheckableObject or
	 * null if none was selected.
	 * @return
	 */
	public ITransferableDataObject choose() {
		
		final int ok = open();
		if (ok == Dialog.OK) return selection;
		return null;
	}

}
