package org.dawnsci.plotting.system.preference;

import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ToolbarConfigurationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
	
	public ToolbarConfigurationPreferencePage() {
		super(GRID);
		setPreferenceStore(PlottingSystemActivator.getLocalPreferenceStore());
		setDescription("Toolbar preferences");
	}

	@Override
	protected void createFieldEditors() {
		
		for (ToolbarConfigurationConstants type : ToolbarConfigurationConstants.values()) {
			addField(new BooleanFieldEditor(type.getId(), type.getLabel(), getFieldEditorParent()));
		}	
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

}
