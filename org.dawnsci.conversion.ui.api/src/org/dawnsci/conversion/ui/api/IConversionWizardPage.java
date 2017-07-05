/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.ui.api;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.jface.wizard.IWizardPage;

public interface IConversionWizardPage extends IWizardPage {
	/**
	 *
	 * @return the context, including any modifications (which includes path changes and
	 * the setting of user objects)
	 */
	public IConversionContext getContext();

	/**
	 *
	 * @param context
	 */
	public void setContext(IConversionContext context);

	/**
	 * Should open the converted file after the conversion has happened.
	 * @return
	 */
	public boolean isOpen();
}
