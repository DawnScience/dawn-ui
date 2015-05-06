package org.dawnsci.processing.ui.slice;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;

public interface ISetupContext {

	public IConversionContext init(String path);
	
	public boolean setup(IConversionContext context);
	
}
