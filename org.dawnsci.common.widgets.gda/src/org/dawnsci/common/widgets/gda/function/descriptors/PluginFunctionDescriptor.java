package org.dawnsci.common.widgets.gda.function.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.common.widgets.gda.function.FunctionExtensionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PluginFunctionDescriptor extends FunctionDescriptor {
	private static final Logger logger = LoggerFactory
			.getLogger(PluginFunctionDescriptor.class);
	private final String name;

	public static FunctionDescriptor[] getDescriptors() {
		List<FunctionDescriptor> descList = new ArrayList<>();
		final FunctionExtensionFactory factory = FunctionExtensionFactory
				.getFunctionExtensionFactory();
		String[] fittingFunctionNames = factory.getFittingFunctionNames();
		for (final String name : fittingFunctionNames) {
			try {
				IFunction myFunction = factory.getFittingFunction(name);
				descList.add(new PluginFunctionDescriptor(myFunction, name));

			} catch (CoreException e) {
				logger.error("Extension point defines function '" + name
						+ "' which cannot be instantiated", e);
			}
		}

		return descList.toArray(new FunctionDescriptor[0]);
	}

	public PluginFunctionDescriptor(IFunction describingFunction, String name) {
		super(describingFunction);
		this.name = name;
	}

	@Override
	public IFunction getFunction() throws FunctionInstantiationFailedException {
		try {
			FunctionExtensionFactory factory = FunctionExtensionFactory
					.getFunctionExtensionFactory();
			return factory.getFittingFunction(name);
		} catch (CoreException e) {
			throw new FunctionInstantiationFailedException(e);
		}
	}
}