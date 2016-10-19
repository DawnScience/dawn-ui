package org.dawnsci.processing.ui.service;

import java.util.HashMap;
import java.util.Map;

import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.api.IOperationUIService;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationUIServiceImpl implements IOperationUIService {

	static {
		System.out.println("Starting operationUI service");
	}

	private final static Logger logger = LoggerFactory.getLogger(OperationUIServiceImpl.class);

	private Map<String, IOperationSetupWizardPage> operationSetupWizardPages;

	public OperationUIServiceImpl() {
		// Intentionally do nothing -> OSGI...
	}

	@Override
	public IOperationSetupWizardPage getWizardPage(String operation_id) {
		checkOperationSetupWizardPages();
		return operationSetupWizardPages.get(operation_id);
	}

	private synchronized void checkOperationSetupWizardPages() {
		if (operationSetupWizardPages != null)
			return;

		operationSetupWizardPages = new HashMap<>();

		/*// look for the extension points
		IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.dawnsci.analysis.api.operationsetupwizardpage");
		for (IConfigurationElement e : eles) {
			if (!e.getName().equals("operationsetupwizardpage")) continue;
			final String id = e.getAttribute("id");
			final String operationid = e.getAttribute("operationid");
			String old = operationSetupWizardPages.put(operationid, id);
			if (old != null) {
				logger.warn("OperationDialog has been changed from {} to {} for operation {}", old, id, operationid);
			}
		}*/
	}

}
