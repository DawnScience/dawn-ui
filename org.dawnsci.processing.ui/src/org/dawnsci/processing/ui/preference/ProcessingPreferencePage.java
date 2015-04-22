package org.dawnsci.processing.ui.preference;


import org.dawnsci.processing.ui.Activator;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessingPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessingPreferencePage.class);
	
	Combo combo;

	@Override
	protected Control createContents(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		main.setLayoutData(gdc);
		
		Label label = new Label(main, SWT.NONE);
		label.setText("Select runner");
		
		combo = new Combo(main, SWT.READ_ONLY);
		String[] items = {ExecutionType.SERIES.toString(), ExecutionType.GRAPH.toString()};
		combo.setItems(items);
		
		String string = getPreferenceStore().getString(ProcessingConstants.EXECUTION_TYPE);
		for (int i = 0; i<items.length; i++) if (items[i].equals(string)) combo.select(i);
		
		return main;
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
		
		return super.performOk();
	}
	
	@Override
	protected void performDefaults() {
		getPreferenceStore().setValue(
				ProcessingConstants.EXECUTION_TYPE, getPreferenceStore().getDefaultString(
						ProcessingConstants.EXECUTION_TYPE));
	}

}
