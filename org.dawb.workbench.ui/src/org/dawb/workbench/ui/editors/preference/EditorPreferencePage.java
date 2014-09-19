/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.ui.editors.preference;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.widgets.LabelFieldEditor;
import org.dawb.workbench.ui.Activator;
import org.dawb.workbench.ui.data.PlotDataSelection;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.plotting.api.PlottingSelectionProvider;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class EditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "org.edna.workbench.editors.preferencePage";
	
	private StringFieldEditor formatFieldEditor;

	private IPropertyChangeListener dataModeListener;
	/**
	 * @wbp.parser.constructor
	 */
	public EditorPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for viewing data sets available in nexus and ascii data.");
	}
	
	@Override
	protected void createFieldEditors() {
		
  
     	// Choice between diamond and light weight plotting
		final String[][] namesAndValues = getPlottingPreferenceChoices();
		ComboFieldEditor plotChoice = new ComboFieldEditor(EditorConstants.PLOTTING_SYSTEM_CHOICE, "Plotting Technology*", namesAndValues, getFieldEditorParent());
		addField(plotChoice);
	   	new LabelFieldEditor("(* Please close and reopen the part after changing plotting preference.)\n\n", getFieldEditorParent());
		
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.diamond.scisoft.analysis.data.set.filter");
				
		if (config!=null && config.length>0) {
			final Label sep = new Label(getFieldEditorParent(), SWT.NONE);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 5));
			BooleanFieldEditor showAll = new BooleanFieldEditor(EditorConstants.IGNORE_DATASET_FILTERS,"Show all possible data sets",getFieldEditorParent());
	      	addField(showAll);
	
			final StringBuilder buf = new StringBuilder("Current data set filters:\n");
			
			for (IConfigurationElement e : config) {
				buf.append("\t-    ");
				final String pattern     = e.getAttribute("regularExpression");
				buf.append(pattern);
				buf.append("\n");
			}
			buf.append("\nData set filters reduce the content available to plot and\ncompare for simplicity but can be turned off.\nAll data is shown in the nexus tree, filters are not applied.");
			final Label label = new Label(getFieldEditorParent(), SWT.WRAP);
			label.setText(buf.toString());
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		BooleanFieldEditor showXY = new BooleanFieldEditor(EditorConstants.SHOW_XY_COLUMN,"Show XY column.",getFieldEditorParent());
      	addField(showXY);

		BooleanFieldEditor showSize = new BooleanFieldEditor(EditorConstants.SHOW_DATA_SIZE,"Show size column.",getFieldEditorParent());
      	addField(showSize);
      	
		BooleanFieldEditor showDims = new BooleanFieldEditor(EditorConstants.SHOW_DIMS, "Show dimensions column.",getFieldEditorParent());
      	addField(showDims);
     	
		BooleanFieldEditor showShape = new BooleanFieldEditor(EditorConstants.SHOW_SHAPE, "Show shape column.",getFieldEditorParent());
      	addField(showShape);
 
		BooleanFieldEditor showVarName = new BooleanFieldEditor(EditorConstants.SHOW_VARNAME, "Show expression variable name.",getFieldEditorParent());
      	addField(showVarName);
      	
		BooleanFieldEditor showLocalName = new BooleanFieldEditor(EditorConstants.SHOW_LOCALNAME, "Use local name attribute.",getFieldEditorParent());
      	addField(showLocalName);

      	new LabelFieldEditor("\nEditors with a 'Data' tab, show the data of the current plot.\nThis option sets the number format for the table and the csv file, if the data is exported.", getFieldEditorParent());

		formatFieldEditor = new StringFieldEditor(EditorConstants.DATA_FORMAT, "Number format:", getFieldEditorParent());
		addField(formatFieldEditor);
		
		new LabelFieldEditor("Examples: #0.0000, 0.###E0, ##0.#####E0, 00.###E0", getFieldEditorParent());
		
		new LabelFieldEditor("\n", getFieldEditorParent());
		
		IntegerFieldEditor playSpeed = new IntegerFieldEditor(EditorConstants.PLAY_SPEED,"Speed of slice play for n-Dimensional data sets (ms):",getFieldEditorParent()) {
			@Override
			protected boolean checkState() {
				if (!super.checkState()) return false;
				if (getIntValue()<10||getIntValue()>10000) return false;
				return true;
			}
		};
		addField(playSpeed);
		
		new LabelFieldEditor("\n", getFieldEditorParent());
		new LabelFieldEditor("\n", getFieldEditorParent());
		
		PlotDataSelection[] pds = PlotDataSelection.values();
		int nTypes = pds.length;
		String[][] pdsTypes = new String[nTypes][1];
		for (int i = 0; i < nTypes; i++) {
			String label = pds[i].getLabel();
			String name  = pds[i].toString();
			pdsTypes[i] = new String[] {label, name};
		}

		OpenComboFieldEditor saveDataChoice = new OpenComboFieldEditor(EditorConstants.SAVE_SEL_DATA, "Automatically open dataset(s)", pdsTypes, getFieldEditorParent());
      	addField(saveDataChoice);
      	
      	final StringFieldEditor datasetNameEditor = new StringFieldEditor(EditorConstants.DATA_SEL, "Data name(s)", getFieldEditorParent());
      	addField(datasetNameEditor);
      	datasetNameEditor.setEnabled(PlotDataSelection.isFixed(getPreferenceStore().getString(EditorConstants.SAVE_SEL_DATA)), getFieldEditorParent());
      	
		BooleanFieldEditor saveLogFormat = new BooleanFieldEditor(EditorConstants.SAVE_LOG_FORMAT, "Save the last axis log format by file extension.",getFieldEditorParent());
      	addField(saveLogFormat);
		BooleanFieldEditor saveTimeFormat = new BooleanFieldEditor(EditorConstants.SAVE_TIME_FORMAT, "Save the last axis time format by file extension.",getFieldEditorParent());
      	addField(saveTimeFormat);
		BooleanFieldEditor saveFormatString = new BooleanFieldEditor(EditorConstants.SAVE_FORMAT_STRING, "Save the last axis format string by file extension.",getFieldEditorParent());
      	addField(saveFormatString);

      	dataModeListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				
				final String propValue = getPreferenceStore().getString(EditorConstants.SAVE_SEL_DATA);
				updateMode(datasetNameEditor, PlotDataSelection.valueOf(propValue));
			}
      	};
      	getPreferenceStore().addPropertyChangeListener(dataModeListener);
      	
      	final Combo combo = saveDataChoice.getCombo(getFieldEditorParent());
      	if (combo!=null) {
      		combo.addSelectionListener(new SelectionAdapter() {
      			public void widgetSelected(SelectionEvent e) {
      				PlotDataSelection sel = PlotDataSelection.values()[combo.getSelectionIndex()];
    				updateMode(datasetNameEditor, sel);
     			}
			});
      	}
 	}
	
    protected void updateMode(StringFieldEditor datasetNameEditor,
			                  PlotDataSelection pds) {
    	
      	datasetNameEditor.setEnabled(pds.isFixed(), getFieldEditorParent());
      	if (!pds.isActive()) {
      		datasetNameEditor.setStringValue("");
      	} else {
      		datasetNameEditor.setStringValue(getPreferenceStore().getString(EditorConstants.DATA_SEL));
      	}
		
	}

	public void dispose() {
    	getPreferenceStore().removePropertyChangeListener(dataModeListener);
        super.dispose();
    }
	
	public static String[][] getPlottingPreferenceChoices() {
		
		final List<String[]> choices = new ArrayList<String[]>(7);
        IConfigurationElement[] systems = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.dawnsci.plotting.api.plottingClass");
        for (IConfigurationElement ia : systems) {
        	choices.add(new String[]{ia.getAttribute("visible_type"), ia.getAttribute("id")});
		}
        
        final String [][] ret = new String[choices.size()][];
        for (int i = 0; i < choices.size(); i++) {
        	ret[i] = choices.get(i);
		}
        return ret;
	}


	@Override
	public void init(IWorkbench workbench) {
		
		
	}
	
    /**
     * Adjust the layout of the field editors so that
     * they are properly aligned.
     */
    @Override
	protected void adjustGridLayout() {
        super.adjustGridLayout();
        ((GridLayout) getFieldEditorParent().getLayout()).numColumns = 1;
    }


	
	@Override
	protected void checkState() {
		super.checkState();
		
		try {
			DecimalFormat format = new DecimalFormat(formatFieldEditor.getStringValue());
			format.format(100.001);
		} catch (IllegalArgumentException ne) {
			setErrorMessage("The format '"+formatFieldEditor.getStringValue()+"' is not valid.");
			setValid(false);
			return;
		}
		
		setErrorMessage(null);
		setValid(true);
		
	}

	private class OpenComboFieldEditor extends ComboFieldEditor {

		public OpenComboFieldEditor(String name, String labelText,
				String[][] entryNamesAndValues, Composite parent) {
			super(name, labelText, entryNamesAndValues, parent);
		}
		public Combo getCombo(Composite parent) {
			try {
	            final Method method = ComboFieldEditor.class.getDeclaredMethod("getComboBoxControl", parent.getClass());
	            try {
	                method.setAccessible(true);
	                return (Combo)method.invoke(this, parent);
	            } finally {
	                method.setAccessible(false);
	            }
			} catch (Exception ne) {
				ne.printStackTrace();
				return null;
			}
		}
	}
}
