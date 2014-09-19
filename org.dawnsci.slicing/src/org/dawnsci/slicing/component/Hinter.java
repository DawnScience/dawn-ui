/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.component;

import org.dawnsci.slicing.Activator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

class Hinter {

	public static void showHint(CellEditor cellEd, final String hint) {
		
		if (!Activator.getDefault().getPreferenceStore().getBoolean(SliceConstants.SHOW_HINTS)) return;
		
		final Control control = cellEd.getControl();
		control.getDisplay().asyncExec(new Runnable() {
			public void run() {

				final DefaultToolTip tooltip = new DefaultToolTip(control, ToolTip.NO_RECREATE, true);
				tooltip.setText(hint);
				tooltip.setHideOnMouseDown(true);
				tooltip.setHideDelay(20000);
				tooltip.setRespectDisplayBounds(true);
				
				Listener listener = new Listener() {

					@Override
					public void handleEvent(Event event) {
						if (!control.isDisposed()) {
							tooltip.hide();
						}
						control.removeListener(SWT.FocusOut, this);
						control.removeListener(SWT.Dispose,  this);
					}
				};
				control.addListener(SWT.Dispose, listener);
				control.addListener(SWT.FocusOut, listener);

				final GC    gc   = new GC(control);
				final Point size = gc.textExtent(hint);
				tooltip.show(new Point(-size.x-15, 0));

			}
		});

	}
}
