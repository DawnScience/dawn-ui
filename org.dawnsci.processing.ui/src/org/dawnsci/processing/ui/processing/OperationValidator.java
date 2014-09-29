/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.processing;

import java.util.List;

import org.dawnsci.common.widgets.table.ISeriesItemDescriptor;
import org.dawnsci.common.widgets.table.ISeriesValidator;
import org.dawnsci.processing.ui.Activator;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;

final class OperationValidator implements ISeriesValidator {

	
	private IOperationService service;

	OperationValidator() {
		this.service     = (IOperationService)Activator.getService(IOperationService.class);
	}
    	
	@Override
	public String getErrorMessage(List<ISeriesItemDescriptor> series) {
		
		if (series==null || series.size()<1) return null;
		
		// TODO Actually will need to be data source sent
		try {
		    service.validate(null, getOperations(series));
		} catch (Exception ne) {
			return ne.getMessage();
		}
		return null;
	}

	private IOperation[] getOperations(List<ISeriesItemDescriptor> series) throws InstantiationException {
		final IOperation[] ret = new IOperation[series.size()];
		int i = 0;
		for (ISeriesItemDescriptor item : series) {
			ret[i] = (IOperation)item.getSeriesObject();
			i++;
		}
		return ret;
	}

}
