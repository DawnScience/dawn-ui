package org.dawnsci.processing.ui.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.ServiceManager;
import org.dawnsci.processing.ui.api.IOperationModelWizard;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.api.IOperationUIService;
import org.dawnsci.processing.ui.model.ConfigureOperationModelWizardPage;
import org.dawnsci.processing.ui.model.OperationModelWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationUIServiceImpl implements IOperationUIService {

	static {
		System.out.println("Starting operationUI service");
	}

	private final static Logger logger = LoggerFactory.getLogger(OperationUIServiceImpl.class);

	private Map<String, Class<? extends IOperationSetupWizardPage>> operationSetupWizardPages;

	public OperationUIServiceImpl() {
		// Intentionally do nothing -> OSGI...
	}

	@Override
	public IOperationSetupWizardPage getWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation, boolean returnDefaultIfNotFound) {
		checkOperationSetupWizardPages();
		
		// The hash only contains the Class wizardPage belongs to.
		// We need to construct it here.
		Class <? extends IOperationSetupWizardPage> klazz = operationSetupWizardPages.get(operation.getId());
		if (klazz == null) {
			logger.info("No OperationSetupWizardPage found for {}", operation.getId());
			return returnDefaultIfNotFound ? new ConfigureOperationModelWizardPage(operation) : null;
		}
			
		try {
			Constructor<?> constructor = klazz.getConstructor(IOperation.class);
			IOperationSetupWizardPage rv = (IOperationSetupWizardPage) constructor.newInstance(operation);
			logger.info("{}-arg constructor instance generated for {}", 1, klazz.getName());
			return rv;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Cannot construct instance of {}. ", klazz.getName());
			e.printStackTrace();
		}
	
		return returnDefaultIfNotFound ? new ConfigureOperationModelWizardPage(operation) : null;
	}

	private synchronized void checkOperationSetupWizardPages() {
		if (operationSetupWizardPages != null)
			return;

		operationSetupWizardPages = new HashMap<>();

		// look for the extension points
		IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.dawnsci.analysis.api.operationsetupwizardpage");
		for (IConfigurationElement e : eles) {
			if (!e.getName().equals("operationsetupwizardpage")) continue;
			final String klazz = e.getAttribute("class");
			final String operationid = e.getAttribute("operationid");
			Class<? extends IOperationSetupWizardPage> old = null;
			try {
				old = operationSetupWizardPages.put(operationid, ((IOperationSetupWizardPage) e.createExecutableExtension("class")).getClass());
			} catch (CoreException e1) {
				logger.error("Exception in createExecutableExtension", e1);
				continue;
			}
			if (old != null) {
				logger.warn("IOperationSetupWizardPage has been changed from {} to {} for operation {}", old.getName(), klazz, operationid);
			}
		}
	}

	@Override
	public IOperationSetupWizardPage getWizardPage(
			IOperation<? extends IOperationModel, ? extends OperationData> operation) {
		return getWizardPage(operation, true);
	}

	@Override
	public IOperationModelWizard getWizard(IDataset initialData,
			List<IOperationSetupWizardPage> startPages,
			List<IOperation<? extends IOperationModel, ? extends OperationData>> operations,
			List<IOperationSetupWizardPage> endPages) {
		
		// not all three arguments may be null
		if ((startPages == null || startPages.isEmpty()) && (operations == null || operations.isEmpty()) && (endPages == null || endPages.isEmpty())) {
			logger.error("Not all arguments may be null or empty lists");
			return null;
		}
		
		List<IOperationSetupWizardPage> allPages = new ArrayList<>();
		
		if (startPages != null) {
			allPages.addAll(startPages);
		}
		
		if (operations != null) {
			for (IOperation<? extends IOperationModel, ? extends OperationData> op : operations) {
				allPages.add(getWizardPage(op)); // if no dedicated page is available, the default will be used!
			}
		}
		
		if (endPages != null) {
			allPages.addAll(endPages);
		}
		
		return new OperationModelWizard(initialData, allPages);
	}

	@Override
	public IOperationModelWizard getWizard(IDataset initialData, 
			List<IOperationSetupWizardPage> startPages,
			String operationsFile,
			List<IOperationSetupWizardPage> endPages) {
		
		// the challenging part here is processing the operationsFile
		IOperation<? extends IOperationModel, ? extends OperationData>[] operations = null;
		
		try {
			IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
			IPersistentFile pf = service.getPersistentFile(operationsFile);
			operations = pf.getOperations();
		} catch (Exception e) {
			logger.error("Could not get operations from " + operationsFile, e);
			return null;
		}
		
		List <IOperation<? extends IOperationModel, ? extends OperationData>> operationsList = Arrays.asList(operations);
		
		return getWizard(initialData, startPages, operationsList, endPages);
	}

}
