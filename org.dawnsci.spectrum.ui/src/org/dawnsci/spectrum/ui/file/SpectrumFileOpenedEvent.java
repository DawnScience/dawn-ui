/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.file;

import java.util.EventObject;


public class SpectrumFileOpenedEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ISpectrumFile result;

	public SpectrumFileOpenedEvent(Object source, ISpectrumFile result) {
		super(source);
		this.result = result;
	}
	
	public ISpectrumFile getFile() {
		return result;
	}

}
