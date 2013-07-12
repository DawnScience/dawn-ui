package org.dawnsci.plotting.system.preference;

import org.dawb.common.ui.widgets.LabelFieldEditor;
import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ToolbarConfigurationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
	
	public ToolbarConfigurationPreferencePage() {
		super(GRID);
		setPreferenceStore(PlottingSystemActivator.getLocalPreferenceStore());
		setDescription("Show the following action groups:");
	}

	@Override
	protected void createFieldEditors() {
		
		addField(new LabelFieldEditor("", getFieldEditorParent()));
		for (ToolbarConfigurationConstants type : ToolbarConfigurationConstants.values()) {
			addField(new BooleanFieldEditor(type.getId(), type.getLabel(), getFieldEditorParent()));
		}	
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

}
