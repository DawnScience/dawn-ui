/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.workbench.ui.transferable;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.plotting.api.expressions.IExpressionObject;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataService;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

/**
 * 
 * @author Matthew Gerring
 *
 */
public class TransferableDataService extends AbstractServiceFactory implements ITransferableDataService {

	@Override
	public ITransferableDataObject createData(IDataHolder holder, IMetadata meta, String name) {
		return new TransferableDataObject(holder, meta, name);
	}

	@Override
	public ITransferableDataObject createExpression(IDataHolder holder, IMetadata meta) {
		return new TransferableDataObject(holder, meta);
	}

	@Override
	public ITransferableDataObject createExpression(IDataHolder holder, IMetadata meta, IExpressionObject expression) {
		return new TransferableDataObject(holder, meta, expression);
	}

	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface==ITransferableDataService.class) {
			return new TransferableDataService();
		} 
		return null;
	}

	private static ITransferableDataObject currentCopiedData;

	@Override
	public ITransferableDataObject getBuffer() {
		return currentCopiedData;
	}
	
	@Override
	public ITransferableDataObject setBuffer(ITransferableDataObject buf) {
		ITransferableDataObject old = currentCopiedData;
		currentCopiedData = buf;
		return old;
	}

}

