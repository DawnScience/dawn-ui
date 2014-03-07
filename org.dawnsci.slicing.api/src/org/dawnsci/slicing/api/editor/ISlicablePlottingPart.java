/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.slicing.api.editor;

import org.dawnsci.plotting.api.IPlottingContainer;
import org.dawnsci.slicing.api.data.ITransferableDataObject;
import org.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A workbench part which does plots and can participate in slicing.
 * 
 * @author fcp94556
 *
 */
public interface ISlicablePlottingPart extends IWorkbenchPart, IPlottingContainer {

	/**
	 * Update the plot with checkables selected by the user.
	 * @param selections
	 * @param participant
	 * @param useTask
	 */
	void updatePlot(final ITransferableDataObject[] selections, 
		            final ISliceSystem              sliceSystem,
		            final boolean                   useTask);

}
