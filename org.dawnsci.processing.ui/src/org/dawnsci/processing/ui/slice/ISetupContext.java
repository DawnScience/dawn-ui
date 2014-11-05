package org.dawnsci.processing.ui.slice;

import org.dawb.common.services.conversion.IConversionContext;

public interface ISetupContext {

	public IConversionContext init(String path);
	
	public boolean setup(IConversionContext context);
	
}
