package org.dawnsci.slicing.component;

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
		
		final Control control = cellEd.getControl();
		control.getDisplay().asyncExec(new Runnable() {
			public void run() {

				final DefaultToolTip tooltip = new DefaultToolTip(control, ToolTip.NO_RECREATE, true);
				tooltip.setText(hint);
				tooltip.setHideDelay(0);
				control.addListener(SWT.Dispose, new Listener() {

					@Override
					public void handleEvent(Event event) {
						if (!control.isDisposed()) tooltip.hide();
					}
				});
				control.addListener(SWT.FocusOut, new Listener() {

					@Override
					public void handleEvent(Event event) {
						if (!control.isDisposed()) tooltip.hide();
					}
				});
				final GC    gc   = new GC(control);
				final Point size = gc.textExtent(hint);
				tooltip.show(new Point(-size.x-15, 0));

			}
		});

	}
}
