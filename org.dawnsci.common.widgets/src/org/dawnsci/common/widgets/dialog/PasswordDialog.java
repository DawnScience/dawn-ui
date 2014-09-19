/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PasswordDialog extends Dialog {

	private String message;
	private String password;
	private String title;
	
	public PasswordDialog(final String title, final String message) {
		this(title, message, Display.getDefault().getActiveShell());
	}

	public PasswordDialog(final String title, final String message, Shell parentShell) {
		super(parentShell);
		this.title   = title;
		this.message = message;
	}

	protected Control createDialogArea(Composite parent) {
		getShell().setText(title);
		final Composite content = (Composite)super.createDialogArea(parent);
		if (message!=null) {
			final Label msg = new Label(content, SWT.WRAP);
			msg.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			msg.setText(message);
		}
		
		final Text pswd = new Text(content, SWT.PASSWORD|SWT.BORDER);
		pswd.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		pswd.setFocus();
		pswd.addModifyListener(new ModifyListener() {		
			@Override
			public void modifyText(ModifyEvent e) {
				password = pswd.getText();
			}
		});
		
		pswd.setFocus();
		
		return content;
	}

	public String getMessage() {
		return message;
	}

	public String getPassword() {
		return password;
	}
}
