/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.scalebox;

import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.event.ValueListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Standard box has the following event properties:
 * 1. Bounds are checked whenever a value is changed.
 * 2. ValueListeners are notified when the enter key is pressed or when focus is lost.
 */
public class StandardBox extends NumberBox {
	
	public StandardBox(Composite parent, int style) {
		super(parent, style);
	}
	
	
//	addSelectionListerner(ISelectionListener listener)
	
	/**
	 * Change listeners to only notify if focus lost or enter pressed.
	 */
	@Override
	protected void createTextListeners(final StyledText text) {
		createFocusListener(text);
		createModifyListener(text);
		createVerifyKeyListener(text);
	}
	
	@Override
	protected void createVerifyKeyListener(StyledText text) {
		
		verifyListener = new VerifyKeyListener() {	
			@Override
			public void verifyKey(VerifyEvent event) {
				if (event.character == SWT.CR) {// They hit enter
					textUpdateAndFireListeners();
				}
			}
		};
		text.addVerifyKeyListener(verifyListener);
	}

	@Override
	protected void createModifyListener(final StyledText text) {
	    this.modifyListener = new ModifyListener()  {
			@Override
			public void modifyText(final ModifyEvent e) {
				textUpdate(); // Checks the entry for bounds (and unit)
				              // Do not fire events.
			} 	
	    };
	    text.addModifyListener(modifyListener);
	}
	
	/**
	 * Change loss of focus to fire value.
	 */
	@Override
	protected void createFocusListener(final StyledText text) {
		this.focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textUpdateAndFireListeners();
			}
	    };
	    text.addFocusListener(focusListener);
	}
}
