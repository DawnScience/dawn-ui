package org.dawnsci.processing.ui.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.api.IOperationUIService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
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
	public IOperationSetupWizardPage getWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation) {
		checkOperationSetupWizardPages();
		
		// The hash only contains the Class wizardPage belongs to.
		// We need to construct it here.
		Class <? extends IOperationSetupWizardPage> klazz = operationSetupWizardPages.get(operation.getId());
		if (klazz == null) {
			logger.info("No OperationSetupWizardPage found for {}", operation.getId());
			return null;
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
	
		/*List<Class<?>> ctorClassArgs = new ArrayList<Class<?>>();
		ctorClassArgs.add(String.class);
		ctorClassArgs.add(String.class);
		ctorClassArgs.add(ImageDescriptor.class);
		
		for (int i = 3 ; i >= 1 ; i--) {
			try {
				constructor = klazz.getConstructor(ctorClassArgs.toArray(new Class<?>[ctorClassArgs.size()]));
			} catch (NoSuchMethodException | SecurityException e) {
				// if failure -> go to next
				ctorClassArgs.remove(ctorClassArgs.size()-1);
				continue;
			}
			String name = operation_id, description = null;
			ImageDescriptor image = null;
			try {
				name = ServiceHolder.getOperationService().getName(operation_id);
			} catch (Exception e) {
				// do nothing
			}
			try {
				description = ServiceHolder.getOperationService().getDescription(operation_id);
			} catch (Exception e) {
				// do nothing
			}
			// no images for now...
			List<Object> ctorArgs = new ArrayList<>();
			switch (i) {
			case 3:
				ctorArgs.add(0, image);
			case 2:
				ctorArgs.add(0, description);
			case 1:
				ctorArgs.add(0, name);
				break;
			default:
				logger.error("Unexpected case {} in constructor loop", i);
				return null;
			}
			
			try {
				IOperationSetupWizardPage rv = (IOperationSetupWizardPage) constructor.newInstance(ctorArgs.toArray(new Object[ctorArgs.size()]));
				logger.info("{}-arg constructor instance generated for {}", operation_id);
				return rv;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				logger.warn("Cannot construct instance of {}. Trying another constructor...", operation_id);
				continue;
			}
		}
		*/
		return null;
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

}
