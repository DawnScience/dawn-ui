package org.dawnsci.slicing.api.editor;

import java.util.Map;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
public interface ISelectedPlotting {

	public Map<String,IDataset> getSelected();
}
