/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.editors.util;

import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ButtonBorder;
import org.eclipse.draw2d.ButtonBorder.ButtonScheme;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToggleButton;

public class FigureUtils {

	public static void setButtonBorder(final IFigure toolbar, ButtonScheme scheme) {
		
		for (Object child : toolbar.getChildren()) {
			if (child instanceof Button) {
				final Button       button = (Button)child;
				button.setBorder(new ButtonBorder(scheme));
			} else if (child instanceof ToggleButton) {
				final ToggleButton       button = (ToggleButton)child;
				button.setBorder(new ButtonBorder(scheme));
			}
		}		
	}

}
