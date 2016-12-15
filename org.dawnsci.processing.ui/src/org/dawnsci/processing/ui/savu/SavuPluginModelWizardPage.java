package org.dawnsci.processing.ui.savu;


import java.util.Map;

import org.dawnsci.processing.ui.model.AbstractOperationModelWizardPage;
import org.dawnsci.processing.ui.savu.ParameterEditor.ParameterEditor;
import org.dawnsci.processing.ui.savu.ParameterEditor.ParameterEditorTableViewModel;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;

import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SavuPluginModelWizardPage extends AbstractOperationModelWizardPage {
	

	private final static Logger logger = LoggerFactory.getLogger(SavuPluginModelWizardPage.class);

	public SavuPluginModelWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation) {
		super(operation);
	}

	public SavuPluginModelWizardPage() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		setControl(container);
		// create model and populate it
		Map<String, Object> pluginDict = null;
		Integer selectedItem = null;
		Boolean isMetaData = false;
		try {
			pluginDict = (Map<String, Object>) model.get("parameters");
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			selectedItem = (Integer) model.get("selectedItem");
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			isMetaData = (Boolean) model.get("MetaDataOnly");
			System.out.println("I am here"+isMetaData.toString());
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		ParameterEditorTableViewModel viewModel = new ParameterEditorTableViewModel("PaganinFilter", pluginDict);
		if (pluginDict!=null){
			viewModel.setPluginDict(pluginDict);
		}


		//create editor and set the model to it
		final ParameterEditor parameterEditor = new ParameterEditor(container, SWT.NONE);
		parameterEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		parameterEditor.initialiseTable(viewModel);
		Button checkBox = new Button(container,SWT.CHECK); // to figure out if we want it as metadata or not
		checkBox.setText("Save as metadata");
		System.out.println(isMetaData.toString());
		checkBox.setSelection(isMetaData);
		checkBox.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button) e.getSource();
				try {
				model.set("MetaDataOnly", btn.getSelection());
				System.out.println(btn.getSelection());
				} catch (Exception e1) {
					logger.error("Couldn't set the meta data switch", e1);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println("In default selection");

				
			}
		});

		SavuPluginChooser pluginChooser = new SavuPluginChooser(container, SWT.NONE);
		pluginChooser.initialiseCombo(selectedItem);
		pluginChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		pluginChooser.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					model.set("selectedItem", pluginChooser.getSelectionIndex());
				} catch (Exception e1) {
					logger.error("Could not get the selectionIndex", e1);
				}
				try {
					model.set("pluginPath", pluginChooser.getPluginPath());
				} catch (Exception e1) {
					logger.error("Could not set the pluginPath in model", e1);
				}
				try {
					model.set("pluginName", pluginChooser.getPluginName());
				} catch (Exception e1) {
					logger.error("Could not set the pluginName in model", e1);
				}
				try {
					model.set("pluginRank", pluginChooser.getPluginRank());
				} catch (Exception e1) {
					logger.error("Could not set the pluginRank in model", e1);
				}

				try {
					viewModel.updateModel(pluginChooser.getPluginName(),(Map<String, Object>) model.get("parameters"));
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				parameterEditor.update(viewModel);				
				
				try {
					model.set("parameters", viewModel.getPluginDict());
				} catch (Exception e1) {
					logger.error("Could not set the parameters in model", e1);
				}
				container.pack();
			}


			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
				System.out.println("In default selection");
			}
			
		});

//	container.pack();
	}
	

}

