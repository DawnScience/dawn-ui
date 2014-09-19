/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.descriptors;

public class FunctionInstantiationFailedException extends Exception {
	private static final long serialVersionUID = -1090396994436597929L;

	public FunctionInstantiationFailedException(String message) {
		super(message);
	}

	public FunctionInstantiationFailedException(Throwable cause) {
		super(cause);
	}

	public FunctionInstantiationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
