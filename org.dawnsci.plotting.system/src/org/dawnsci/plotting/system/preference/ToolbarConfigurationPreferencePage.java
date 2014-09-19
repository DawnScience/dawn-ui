/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system.preference;

import org.dawb.common.ui.widgets.LabelFieldEditor;
import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.eclipse.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
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
