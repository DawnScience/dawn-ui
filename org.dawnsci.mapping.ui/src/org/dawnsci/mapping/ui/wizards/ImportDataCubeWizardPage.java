package org.dawnsci.mapping.ui.wizards;

import java.util.Map;

import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


public class ImportDataCubeWizardPage extends WizardPage implements IDatasetWizard {
	
	protected DatasetAndAxesWidget widget;
	private MappedDataFileBean mdfbean;
	
	protected ImportDataCubeWizardPage(String name) {
		super(name);
		this.setTitle("Import Data Blocks");
		this.setDescription("Select all the full data blocks, their axes, and which dimensions correspond to the map X and Y directions");
	}

	@Override
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.None);
		main.setLayout(new GridLayout(1,false));
		widget = new DatasetAndAxesWidget(mdfbean.getBlocks());
		widget.addCompleteListener(new IDataWidgetCompleteListener() {
			
			@Override
			public void dataComplete(boolean complete) {
				setPageComplete(complete);
			}
		});
		widget.createControl(main);
		setControl(main);
		final Button onlyNexusTagged = new Button(main, SWT.CHECK);
		onlyNexusTagged.setText("Only signal tagged datasets");
		onlyNexusTagged.setSelection(true);
		onlyNexusTagged.setLayoutData(new GridData());
		onlyNexusTagged.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				widget.onlySignalTagged(onlyNexusTagged.getSelection());
			}
		});
		
	}
	
	
	@Override
	public void setDatasetMaps(Map<String,int[]> datasetNames, Map<String,int[]> nexusDatasetNames) {
		widget.setDatasetMaps(datasetNames, nexusDatasetNames);
	}
	
	@Override
	public void setMapBean(MappedDataFileBean bean) {
		this.mdfbean = bean;
		setPageComplete(bean.checkValid());
	}
	
}
