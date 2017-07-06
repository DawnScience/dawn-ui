/*
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.fileviewer.preferences;

import org.dawnsci.fileviewer.Activator;
import org.dawnsci.fileviewer.FileViewerConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class FileViewerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor showSize;
	private BooleanFieldEditor showType;
	private BooleanFieldEditor showModified;
	private BooleanFieldEditor displayWithSIUnit;

	public FileViewerPreferencePage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {

		showSize = new BooleanFieldEditor(FileViewerConstants.SHOW_SIZE_COLUMN, "Show size column",
				getFieldEditorParent());
		addField(showSize);

		showType = new BooleanFieldEditor(FileViewerConstants.SHOW_TYPE_COLUMN, "Show type column",
				getFieldEditorParent());
		addField(showType);

		showModified = new BooleanFieldEditor(FileViewerConstants.SHOW_MODIFIED_COLUMN, "Show modified column",
				getFieldEditorParent());
		addField(showModified);

		displayWithSIUnit = new BooleanFieldEditor(FileViewerConstants.DISPLAY_WITH_SI_UNITS,
				"Display file size with SI Units", getFieldEditorParent());
		displayWithSIUnit.getDescriptionControl(getFieldEditorParent()).setToolTipText("If SI Units are used, 1kB = 1000Bytes, "
				+ "if not, binary Units are used (1kB = 1024Bytes)");
		addField(displayWithSIUnit);
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		setPreferenceStore(store);
		setDescription("Preferences for viewing a file system using the File Viewer:");
	}

	/**
	 * Adjust the layout of the field editors so that they are properly aligned.
	 */
	@Override
	protected void adjustGridLayout() {
		super.adjustGridLayout();
		((GridLayout) getFieldEditorParent().getLayout()).numColumns = 1;
	}

	@Override
	protected void checkState() {
		super.checkState();
		setErrorMessage(null);
		setValid(true);
	}
}
