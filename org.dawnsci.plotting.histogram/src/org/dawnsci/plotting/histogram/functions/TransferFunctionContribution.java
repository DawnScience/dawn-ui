package org.dawnsci.plotting.histogram.functions;

import org.dawnsci.plotting.api.histogram.ITransferFunction;
import org.dawnsci.plotting.histogram.functions.classes.AbstractTransferFunction;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * This class wrappers a Transfer Function extension point so that it can be easily accessed
 * @author ssg37927
 *
 */
public class TransferFunctionContribution {

	private static final String ATT_ID = "id";
	private static final String ATT_NAME = "name";
	private static final String ATT_CLASS = "array_provider_class";
	
	private String name; 
	private String id;
	private AbstractTransferFunction function;
		
	public static TransferFunctionContribution getTransferFunctionContribution(
			IConfigurationElement config) {
		TransferFunctionContribution transferFunctionContribution = new TransferFunctionContribution();
		// try to get things out of the config which are required
		try {
			transferFunctionContribution.name      = config.getAttribute(ATT_NAME);
			transferFunctionContribution.id        = config.getAttribute(ATT_ID);
			transferFunctionContribution.function  = (AbstractTransferFunction) config.createExecutableExtension(ATT_CLASS);
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot create TransferFunctionContribution contribution due to the following error",e);
		}
		
		return transferFunctionContribution; 
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public ITransferFunction getFunction() {
		return function;
	}
	
}
