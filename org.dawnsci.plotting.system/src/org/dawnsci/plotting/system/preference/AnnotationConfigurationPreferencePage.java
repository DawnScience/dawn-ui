/*
 * Copyright (c) 2026 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system.preference;

import org.dawb.common.ui.widgets.LabelFieldEditor;
import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class AnnotationConfigurationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
	
	public AnnotationConfigurationPreferencePage() {
		super(GRID);
		setPreferenceStore(PlottingSystemActivator.getPlottingPreferenceStore());
		setDescription("Configure plot annotation properties:");
	}

	@Override
	protected void createFieldEditors() {
		addField(new LabelFieldEditor("", getFieldEditorParent()));
		addField(new ColorFieldEditor(BasePlottingConstants.ANNOTATION_COLOUR, "Colour", getFieldEditorParent()));
		addField(new FontFieldEditor(BasePlottingConstants.ANNOTATION_FONT, "Font", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore store = getPreferenceStore();
		String def = store.getDefaultString(BasePlottingConstants.ANNOTATION_FONT);
		// set defaults dynamically as XYGraph relies on the system font and control foreground colour
		if (def.isEmpty()) {
			store.setDefault(BasePlottingConstants.ANNOTATION_FONT, StringConverter.asString(workbench.getDisplay().getSystemFont().getFontData()));
		}
		def = store.getDefaultString(BasePlottingConstants.ANNOTATION_COLOUR);
		if (def.isEmpty()) {
			store.setDefault(BasePlottingConstants.ANNOTATION_COLOUR, StringConverter.asString(workbench.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND).getRGB()));
		}
	}
}
