package org.dawnsci.processing.ui.preference;



import org.dawnsci.processing.ui.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ProcessingPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	public ProcessingPreferencePage() {
	}
	
	private Text remoteURITextBox;
	private Button forceSeries;

	@Override
	protected Control createContents(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		main.setLayoutData(gdc);
		
		Label label = new Label(main, SWT.NONE);
		label.setText("Set Consumer URI");
		
		remoteURITextBox = new Text(main, SWT.BORDER);
		remoteURITextBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		forceSeries = new Button(main,SWT.CHECK);
		forceSeries.setText("Disable parallel processing");
		

//		try {
//			Composite savu = new SavuWindow2(parent, SWT.NONE);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

        
		setUpFromPreferences();
		
		return main;
	}
	
	private void setUpFromPreferences(){
		remoteURITextBox.setText(getPreferenceStore().getString(ProcessingConstants.REMOTE_RUNNER_URI));
		forceSeries.setSelection(getPreferenceStore().getBoolean(ProcessingConstants.FORCE_SERIES));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	@Override
	public boolean performOk() {
		String text = remoteURITextBox.getText();
		getPreferenceStore().setValue(ProcessingConstants.REMOTE_RUNNER_URI, text);
		getPreferenceStore().setValue(ProcessingConstants.FORCE_SERIES, forceSeries.getSelection());
		
		return super.performOk();
	}
	
	@Override
	protected void performDefaults() {
		getPreferenceStore().setValue(
				ProcessingConstants.REMOTE_RUNNER_URI, getPreferenceStore().getDefaultString(
						ProcessingConstants.REMOTE_RUNNER_URI));
		
		getPreferenceStore().setValue(
				ProcessingConstants.FORCE_SERIES, getPreferenceStore().getDefaultString(
						ProcessingConstants.FORCE_SERIES));
		
		setUpFromPreferences();
	}

}
