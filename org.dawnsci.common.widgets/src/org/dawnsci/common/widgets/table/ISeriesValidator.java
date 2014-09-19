/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.table;

import java.util.List;

public interface ISeriesValidator {

	/**
	 * 
	 * @param series
	 * @return null if series is valid or the message to show to the user
	 * if the series is valid.
	 */
	public String getErrorMessage(List<ISeriesItemDescriptor> series);
}
