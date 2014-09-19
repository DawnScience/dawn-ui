/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui;

import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.viewers.ILabelProvider;

public class OperationPropertyDescriptorData {
	public IOperationModel model;
	public String name;
	public ILabelProvider labelProvider;

	public OperationPropertyDescriptorData() {
	}
}