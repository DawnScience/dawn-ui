package org.dawnsci.processing.ui.savu;


import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.dawnsci.processing.ui.model.AbstractOperationModelWizardPage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;

import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavuPluginModelWizardPage extends AbstractOperationModelWizardPage {
	

	private static final Logger logger = LoggerFactory.getLogger(SavuPluginModelWizardPage.class);

	private Map<String, Object> pluginDict;
	
	@SuppressWarnings("unchecked")
	public SavuPluginModelWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation) {
		super(operation);

		final String wspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();

		try (
			FileInputStream fileIn = new FileInputStream(wspacePath + File.separator + "savu_plugin_info.ser");// just
			ObjectInputStream in = new ObjectInputStream(fileIn);
				) {
			pluginDict = (Map<String, Object>) in.readObject();
		} catch (Exception e) {
			logger.warn("Couldn't open the file for "+ wspacePath + File.separator + "savu_plugin_info.ser", e);
			// better to display a wizardpage with an appropriate message...
		}
	}

	public SavuPluginModelWizardPage() {
		super();
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout containerLayout = new GridLayout(1, true);
		container.setLayout(containerLayout);
		setControl(container);
		// create model and populate it
		Map<String, Map<String,Object>> pluginParameterDict = null;
		boolean isMetaData = false;
		String pluginName = null;
		try {
			pluginName = (String) model.get("pluginName");
		} catch (Exception e) {
			logger.warn("Couldn't get the pluginName");
		}
		try {
			pluginParameterDict = (Map<String, Map<String, Object>>) model.get("parameters"); // this cannot fail! It will be empty initially though...
		} catch (Exception e2) {
			logger.warn("Couldn't get the model parameters",e2);
		}
		try {
			isMetaData = (boolean) model.get("metaDataOnly");
		} catch (Exception e2) {
			logger.warn("Couldn't select the metadata radar box",e2);
		}
		// initialise the parameter editor model.
		final SavuParameterEditorTableViewModel viewModel = new SavuParameterEditorTableViewModel(pluginParameterDict);
		
		//Initialise the parameter editor gui
		final SavuParameterEditor parameterEditor = new SavuParameterEditor(container, viewModel, SWT.NONE);

		GridData parameterEditorLayout = new GridData(SWT.FILL, SWT.FILL, true, true);
		//parameterEditorLayout.heightHint = parameterEditor.getHeight();
		//parameterEditorLayout.widthHint = parameterEditor.getWidth();
		parameterEditor.setLayoutData(parameterEditorLayout);
		
		
		Combo pluginChooser = new Combo(container, SWT.READ_ONLY);
		GridData pluginLayout = new GridData();
		pluginLayout.horizontalSpan=1;
		pluginLayout.verticalSpan=1;
		pluginLayout.verticalAlignment = GridData.END;	
		pluginChooser.setLayoutData(pluginLayout);
		String[] pluginNameArray = pluginDict.keySet().toArray(new String[0]);
		pluginChooser.setItems(pluginNameArray);

		pluginChooser.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedPluginName = pluginChooser.getText();
				@SuppressWarnings("unchecked")
				Map<String,Object> selectedPluginParamsDict = (Map<String, Object>) pluginDict.get(selectedPluginName);
				try {
					model.set("pluginPath", selectedPluginParamsDict.get("path2plugin"));
				} catch (Exception e1) {
					logger.error("Could not set the pluginPath in model", e1);
				}
				try {
					model.set("pluginName", selectedPluginName);
				} catch (Exception e1) {
					logger.error("Could not set the pluginName in model", e1);
				}
				try {
					model.set("pluginRank", selectedPluginParamsDict.get("input rank"));
				} catch (Exception e1) {
					logger.error("Could not set the pluginRank in model", e1);
				}
				try {
					viewModel.updateModel(selectedPluginName);
				} catch (Exception e2) {
					logger.error("Couldn't update model!",e2);
				}
				parameterEditor.updateTable();		
				//container.pack();
			}
		});

		parameterEditor.initialiseTable();
		
		Button checkBox = new Button(container,SWT.CHECK); // to figure out if we want it as metadata or not
		checkBox.setText("Save as metadata");
		GridData checkboxLayout = new GridData();
		checkboxLayout.horizontalSpan=1;
		checkboxLayout.verticalSpan=1;
		checkBox.setLayoutData(checkboxLayout);
		checkBox.setSelection(isMetaData);

		checkBox.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button) e.getSource();
				try {
				model.set("MetaDataOnly", btn.getSelection());
				} catch (Exception e1) {
					logger.error("Couldn't set the meta data switch", e1);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				logger.debug("In default selection");

				
			}
		});
		
		if (pluginName != null) {
			pluginChooser.select(ArrayUtils.indexOf(pluginNameArray, pluginName));
			try {
				viewModel.updateModel(pluginName);
			} catch (Exception e2) {
				logger.error("Couldn't update model!",e2);
			}
			parameterEditor.updateTable();		
		}
		
	}


}

