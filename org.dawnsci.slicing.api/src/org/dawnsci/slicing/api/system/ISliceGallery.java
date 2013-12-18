/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.slicing.api.system;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;

/**
 * This interface marks a workbench part as being able to display a gallery
 * from a currently configured slice.
 * @author fcp94556
 *
 */
public interface ISliceGallery {

	/**
	 * For instance when a slice is asked to be opened in a gallery view,
	 * this method will be called to send the slice to the gallery.
	 * 
	 * @param shape
	 * @param slice
	 */
	public void updateSlice(final ILazyDataset lazySet, final SliceObject slice);
}
