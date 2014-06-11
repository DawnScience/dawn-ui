package org.dawnsci.plotting.system.preference;

import org.dawnsci.plotting.api.preferences.PlottingConstants;
import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ErrorBarPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ErrorBarPreferencePage() {
		super(GRID);
		setPreferenceStore(PlottingSystemActivator.getPlottingPreferenceStore());
		setDescription("Configure error bars when a large number of plots are shown.");
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void createFieldEditors() {
		
		BooleanFieldEditor bfe = new BooleanFieldEditor(PlottingConstants.GLOBAL_SHOW_ERROR_BARS, "Show error bars (global setting)", getFieldEditorParent());
		addField(bfe);
		
		final IntegerFieldEditor sizeEditor = new IntegerFieldEditor(PlottingConstants.AUTO_HIDE_ERROR_SIZE, "Number of traces to automatically set error bars off", getFieldEditorParent());
		addField(sizeEditor);
		sizeEditor.setValidRange(2, 1000);
		sizeEditor.setEnabled(getPreferenceStore().getBoolean(PlottingConstants.GLOBAL_SHOW_ERROR_BARS), getFieldEditorParent());

		final Button button = (Button)bfe.getDescriptionControl(getFieldEditorParent());
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sizeEditor.setEnabled(button.getSelection(), getFieldEditorParent());
			}	
		});
		
	}

}

