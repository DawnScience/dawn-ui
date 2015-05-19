package org.dawnsci.processing.ui.preference;


import org.dawnsci.processing.ui.Activator;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessingPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessingPreferencePage.class);
	
	Combo combo;
	Spinner spinner;
	Button button;

	@Override
	protected Control createContents(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		main.setLayoutData(gdc);
		
		Label label = new Label(main, SWT.NONE);
		label.setText("Select runner");
		
		combo = new Combo(main, SWT.READ_ONLY);
		
		label = new Label(main, SWT.NONE);
		label.setText("Set Graph Pool Size");
		
		spinner = new Spinner(main, SWT.NONE);
		spinner.setMaximum(16);
		spinner.setMinimum(1);
		
		label = new Label(main, SWT.NONE);
		label.setText("Parallelise if possible");
		
		button = new Button(main, SWT.CHECK);
		
		setUpFromPreferences();
		
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				e.toString();
				String text = ((Combo)e.getSource()).getText();
				
				if (text.equals(ExecutionType.SERIES.toString())) {
					spinner.setEnabled(false);
					button.setEnabled(true);
				} else {
					spinner.setEnabled(true);
					button.setEnabled(false);
				}
			}

		});
		
		return main;
	}
	
	private void setUpFromPreferences(){
		String[] items = {ExecutionType.SERIES.toString(), ExecutionType.GRAPH.toString()};
		combo.setItems(items);
		String string = getPreferenceStore().getString(ProcessingConstants.EXECUTION_TYPE);
		for (int i = 0; i<items.length; i++) if (items[i].equals(string)) combo.select(i);
		
		int val = getPreferenceStore().getInt(ProcessingConstants.POOL_SIZE);

		spinner.setSelection(val);
		button.setSelection(getPreferenceStore().getBoolean(ProcessingConstants.USE_PARRALLEL));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	@Override
	public boolean performOk() {
		String text = combo.getText();
		
		try {
			ExecutionType val = ExecutionType.valueOf(text);
			getPreferenceStore().setValue(ProcessingConstants.EXECUTION_TYPE, val.toString());
		} catch (Exception e) {
			logger.error("Could not change runner: " + e.getMessage());
		}
		
		getPreferenceStore().setValue(ProcessingConstants.POOL_SIZE, spinner.getSelection());
		
		return super.performOk();
	}
	
	@Override
	protected void performDefaults() {
		getPreferenceStore().setValue(
				ProcessingConstants.EXECUTION_TYPE, getPreferenceStore().getDefaultString(
						ProcessingConstants.EXECUTION_TYPE));
		
		getPreferenceStore().setValue(
				ProcessingConstants.POOL_SIZE, getPreferenceStore().getDefaultInt(
						ProcessingConstants.POOL_SIZE));
		
		getPreferenceStore().setValue(
				ProcessingConstants.USE_PARRALLEL, getPreferenceStore().getDefaultBoolean(
						ProcessingConstants.USE_PARRALLEL));
		
		setUpFromPreferences();
	}

}
