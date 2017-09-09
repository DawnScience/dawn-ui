/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.tools.preference.detector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;


public class MissingDetectorModulesSelectorWrapper extends FieldComposite {
    private List<Integer> missingModules;
	
	
	public MissingDetectorModulesSelectorWrapper(DiffractionDetectorComposite parent, int style) {
		super(parent, style);
		
		missingModules = new ArrayList<>();
		
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Select any missing detector modules.");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MissingDetectorModulesSelectorDialog dialog = 
						new MissingDetectorModulesSelectorDialog(parent.getShell(), 
								                                 parent.getNumberOfHorizontalModules().getIntegerValue(), 
								                                 parent.getNumberOfVerticalModules().getIntegerValue(),
								                                 MissingDetectorModulesSelectorWrapper.this.missingModules);
				if(dialog.open() == Window.OK){
					setValue(dialog.getMissingModules());
				}
			}
		});
	}
	
	@Override
	public Object getValue() {
		return missingModules;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public void setValue(Object value) {
		missingModules =  (List<Integer>) value;
		Collections.sort(missingModules);
		
		final ValueEvent evt = new ValueEvent(this, getFieldName());
		evt.setValue(getValue());
		eventDelegate.notifyValueListeners(evt);
	}
}