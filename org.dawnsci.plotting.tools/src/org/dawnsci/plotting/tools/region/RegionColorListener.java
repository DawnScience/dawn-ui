/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.region;

import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Color;

public class RegionColorListener implements ISelectionChangedListener {

	private IRegion previousRegion;
	private Color previousColor;

	@Override
	public void selectionChanged(SelectionChangedEvent event) {

		resetSelectionColor();

		final IStructuredSelection sel = (IStructuredSelection) event
				.getSelection();
		if (!(sel.getFirstElement() instanceof IRegion))
			return;
		final IRegion region = (IRegion) sel.getFirstElement();
		previousRegion = region;
		if ((region != null) && region.isActive()) {
			region.setRegionColor(ColorConstants.green);
			region.setAlpha(51); // 20%
		} else if ((region != null) && !region.isActive()) {
			region.setRegionColor(ColorConstants.gray);
			region.setAlpha(51); // 20%
		}
		previousColor = region != null ? region.getRegionColor() : null;

		if (region != null) {
			region.setRegionColor(ColorConstants.red);
			region.setAlpha(51); // 20%
		}
	}

	public void resetSelectionColor() {
		if (previousRegion != null)
			previousRegion.setRegionColor(previousColor);
		previousRegion = null;
		previousColor = null;
	}
}