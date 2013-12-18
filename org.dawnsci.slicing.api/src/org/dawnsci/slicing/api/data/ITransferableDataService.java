/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.api.data;

import org.dawb.common.services.IExpressionObject;

import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;

/**
 * A service for making ITransferableDataObjects
 * 
 * These objects can be used for providing data into different parts of the UI.
 * 
 * @author fcp94556
 *
 */
public interface ITransferableDataService {

	/**
	 * Create a normal ITransferableDataObject. The name must reference one of the 
	 * keys in the IDataHolder.
	 * 
	 * @param holder
	 * @param meta
	 * @param name
	 * @return
	 */
	public ITransferableDataObject createData(IDataHolder holder, IMetaData meta, String name);

	/**
	 * Create a ITransferableDataObject which will be used to hold an expression
	 * @param holder
	 * @param meta
	 * @return
	 */
	public ITransferableDataObject createExpression(IDataHolder holder, IMetaData meta) ;
	
	/**
	 * Create a ITransferableDataObject expression
	 * @param holder
	 * @param meta
	 * @param expression
	 * @return
	 */
	public ITransferableDataObject createExpression(IDataHolder holder, IMetaData meta, IExpressionObject expression);
	

	/**
	 * Gets the current copied object, if any.
	 * 
	 * @return
	 */
	public ITransferableDataObject getBuffer();
	
	/**
	 * 
	 * @param buf
	 * @return previous bufer or null if there was none.
	 */
	public ITransferableDataObject setBuffer(ITransferableDataObject buf);

}
