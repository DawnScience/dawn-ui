/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 *
 */
public abstract class ButtonComposite extends FieldComposite {


	protected SelectionListener buttonSelection;
	protected Button button;

	/**
	 * @param parent
	 * @param style
	 */
	public ButtonComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	
	/** NOTE Just used to make RCP Developer recognise bean **/
	private boolean buttonVisible = false;
	
	/**
	 * Called to show scale button.
	 * @param vis
	 */
	public void setButtonVisible(final boolean vis) {
		buttonVisible = vis;
		if (button!=null) button.setVisible(vis);
		
		if (vis) {
			createButton();
			button.addSelectionListener(buttonSelection);
		} else {
			if (buttonSelection!=null) button.removeSelectionListener(buttonSelection);
			buttonSelection = null;
		}
	}
	
	/**
	 * Returns true is scale button showing.
	 * @return true/false
	 */
	public boolean isButtonVisible() {
		return buttonVisible;
	}
	
	/**
	 * Called to add a scale button listener which will be run when the scale button is pressed.
	 * @param l 
	 */
	public void addButtonListener(final SelectionListener l) {
		if (buttonSelection!=null) button.removeSelectionListener(buttonSelection);
		buttonSelection = l;
		if (!buttonVisible) {
			createButton();
			buttonVisible = true;
			button.setVisible(true);
		}
		if (button != null) button.addSelectionListener(buttonSelection);
	}
	
	@Override
	public void dispose() {
		if (button!=null&&buttonSelection!=null) {
			button.removeSelectionListener(buttonSelection);
			button.dispose();
		}
		super.dispose();
	}
	
	protected void createButton() {
		
		if(this.button!=null) return;
		
		this.button = new Button(this, SWT.PUSH) {
			@Override
			protected void checkSubclass () {
			}
			@Override
			public boolean forceFocus(){
				return false;
			}
			@Override
			public boolean isFocusControl() {
				return false;
			}
			@Override
			public boolean setFocus() {
				return false;
			}
		};
		
		// Button not used normally.
		button.setText("..");
		if (getLayout() instanceof GridLayout){
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}
		layout();
	}

}
