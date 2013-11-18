/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.wrappers;

import java.util.List;

import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * This field edits a choice and an optional number. It is
 * for field introduced to the XML which are of the form:
 *   string <number>
 *   
 *   Where string is a choice and number is an optional 
 *   extra piece of information. Currently the values
 *   are separated by a space. 
 */
public class ComboAndNumberWrapper extends ComboWrapper {
	
	private final ScaleBox     secondValue;
	private final List<String> visibleChoices;
	private SelectionAdapter selectionListener;

	/**
	 * @param parent
	 * @param style
	 * @param visibleChoices 
	 */
	public ComboAndNumberWrapper(Composite parent, int style, final List<String> visibleChoices) {
		super(parent, style);
		
		this.visibleChoices = visibleChoices;
		this.secondValue    = new ScaleBox(this, SWT.NONE);
		secondValue.on();
		secondValue.setActive(false);
		
		secondValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		this.selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (visibleChoices!=null) {
					if (visibleChoices.contains(combo.getItem(combo.getSelectionIndex()))) {
						secondValue.setActive(true);
					} else if (itemMap!=null && visibleChoices.contains(itemMap.get(combo.getItem(combo.getSelectionIndex())))){
						secondValue.setActive(true);
					} else {
						secondValue.setActive(false);
				    }
				} else {
					secondValue.setActive(true);
				}
			}
		};
		combo.addSelectionListener(selectionListener);
	}
	
	@Override
	public void dispose() {
		if(!combo.isDisposed()) 
			combo.removeSelectionListener(selectionListener);
		if(!super.isDisposed()) 
			super.dispose();
	}
	
	@Override
	public Object getValue() {
		final String t = (String)super.getValue();
		
		if (visibleChoices.contains(t)) {
			return t+" "+secondValue.getValue();
		} 
		return t;
	}
	
	@Override
	public void setValue(final Object value) {
		if (value==null) {
			super.setValue(value);
			secondValue.setVisible(false);
			return;
		}
		
		final String txt = (String)value;
		if (txt.indexOf(' ')>-1) {
			final String[] vals = txt.split(" ");
			super.setValue(vals[0]);
			secondValue.setVisible(true);
			secondValue.setValue(vals[1]);
		} else {
			secondValue.setVisible(false);
			super.setValue(value);
		}
	}
	
	/**
	 * @return the ScaleBox editing the second value.
	 */
	public ScaleBox getValueField() {
		return this.secondValue;
	}
	

}
