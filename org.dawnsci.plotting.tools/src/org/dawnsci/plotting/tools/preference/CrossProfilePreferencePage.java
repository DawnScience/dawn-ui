package org.dawnsci.plotting.tools.preference;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CrossProfilePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public CrossProfilePreferencePage() {
		super();
		
		setPreferenceStore(Activator.getLocalPreferenceStore());
		setDescription("Set the preferences for slicing in the z-direction,\nwhere the line profiles of the cross intersect.\n"
				+ "This assumes that the lazy dataset for getting the z-values,\ncan be determined. If it cannot no z-profile is made\n\n"
				+ "The y direction, when the spot is clicked, is implicit in the cross.");
	}

	@Override
	protected void createFieldEditors() {
		
        addField(new IntegerFieldEditor(CrossProfileConstants.PLUS_X,  "+x pixel", getFieldEditorParent(), 25));
        addField(new IntegerFieldEditor(CrossProfileConstants.MINUS_X, "-x pixel", getFieldEditorParent(), 25));
        addField(new IntegerFieldEditor(CrossProfileConstants.PLUS_Z,  "+z pixel", getFieldEditorParent(), 25));
        addField(new IntegerFieldEditor(CrossProfileConstants.MINUS_Z, "-z pixel", getFieldEditorParent(), 25));

	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
