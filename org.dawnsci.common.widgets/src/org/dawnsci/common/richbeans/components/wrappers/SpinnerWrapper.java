/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.wrappers;

import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.components.ButtonComposite;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

/**
 * 
 * @author fcp94556
 *
 */
public class SpinnerWrapper extends ButtonComposite implements IFieldWidget {

    protected Spinner spinner;
	private ModifyListener modifyListener;
	/**
	 * 
	 * @param parent
	 * @param style
	 */
	public SpinnerWrapper(Composite parent, int style) {
		
		super(parent, SWT.NONE);
		setLayout(new GridLayout(1, false));
		
		this.spinner = new Spinner(this, style);
		spinner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		mainControl = spinner;
		
		this.modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				final ValueEvent evt = new ValueEvent(spinner,getFieldName());
				evt.setValue(getValue());
				eventDelegate.notifyValueListeners(evt);
			}
		};
		spinner.addModifyListener(modifyListener);
	}
	
	@Override
	public void dispose() {
		if (spinner!=null&&!spinner.isDisposed()) spinner.removeModifyListener(modifyListener);
		super.dispose();
	}

	@Override
	public Object getValue() {
		return spinner.getSelection(); // Integer
	}
	
	@Override
	public void setValue(Object value) {
		if (value==null) {
			spinner.setSelection(spinner.getMinimum());
			return;
		}
		
		if (value instanceof String) {
			double dblValue = Double.valueOf((String)value);
			long rounded = Math.round(dblValue);
			value = Integer.parseInt(new Long(rounded).toString());
		}
		
		spinner.setSelection(((Number)value).intValue());
		
		if (this.notifyType!=null&&(notifyType==NOTIFY_TYPE.ALWAYS||notifyType==NOTIFY_TYPE.VALUE_CHANGED)) {
			final ValueEvent evt = new ValueEvent(spinner,getFieldName());
			evt.setValue(getValue());
			eventDelegate.notifyValueListeners(evt);
		}
	}
	
	@Override
	public void setToolTipText(String text) {
		this.spinner.setToolTipText(text);
	}
	
	/*******************************************************************/
	/**        This section will be the same for many wrappers.       **/
	/*******************************************************************/
	@Override
	protected void checkSubclass () {
	}
	/**
	 * @param active the active to set
	 */
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		spinner.setVisible(active);
	}
	
	/**
	 * @param i
	 */
	public void setMaximum(int i) {
		spinner.setMaximum(i);
	}
	/**
	 * @param i
	 */
	public void setMinimum(int i) {
		spinner.setMinimum(i);
	}

	public void setDigits(int i) {
		spinner.setDigits(i);
	}

	public void setIncrement(int i) {
		spinner.setIncrement(i);
	}

	public int getMaximum() {
		return spinner.getMaximum();
	}
	public int getMinimum() {
		return spinner.getMinimum();
	}
	
	/*******************************************************************/

}

	