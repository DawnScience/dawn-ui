/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.table;

import java.util.Collection;

public interface ISeriesItemFilter {

	/**
	 * Return the list of ISeriesItemDescriptor's which may follow itemDescriptor.
	 * 
	 * If itemDescriptor is null, should return complete list of all possible ISeriesItemDescriptor's
	 * 
	 * @param itemDescriptor, may be null
	 * @return
	 */
	Collection<ISeriesItemDescriptor> getDescriptors(String contents, int position, ISeriesItemDescriptor itemDescriptor);

}
