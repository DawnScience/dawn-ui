package org.dawnsci.processing.ui.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.processing.ui.ServiceHolder;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.api.IOperationUIService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
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
	public IOperationSetupWizardPage getWizardPage(String operation_id) {
		checkOperationSetupWizardPages();
		
		// The hash only contains the Class wizardPage belongs to.
		// We need to construct it here.
		// Try the 3 arg constructor first, followed by the 1 arg constructor.
		Class <? extends IOperationSetupWizardPage> klazz = operationSetupWizardPages.get(operation_id);
		if (klazz == null)
			return null;
	
		Constructor<?> constructor = null;
		
		try {
			constructor = klazz.getConstructor(String.class, String.class, ImageDescriptor.class);
			//
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
			try {
				return (IOperationSetupWizardPage) constructor.newInstance(name, description, image);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				logger.error("Could not instantiate IOperationSetupWizardPage for {}", klazz.getName());
				logger.error("Stacktrace: ", e);
				return null;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			try {
				constructor = klazz.getConstructor(String.class);
				String name = operation_id;
				try {
					name = ServiceHolder.getOperationService().getName(operation_id);
				} catch (Exception e2) {
					// do nothing
				}
				try {
					return (IOperationSetupWizardPage) constructor.newInstance(name);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e2) {
					logger.error("Could not instantiate IOperationSetupWizardPage for {}", klazz.getName());
					logger.error("Stacktrace: ", e2);
					return null;
				}
			} catch (NoSuchMethodException | SecurityException e1) {
				logger.error("Could not find a suitable constructor for {}!", klazz.getName());
				return null;
			}
		}
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
