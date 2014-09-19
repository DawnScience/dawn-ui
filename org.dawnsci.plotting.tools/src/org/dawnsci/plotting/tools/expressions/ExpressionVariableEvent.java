/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.expressions;

import java.util.EventObject;
import java.util.List;

public class ExpressionVariableEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	private List<String> names;

	public ExpressionVariableEvent(Object source, List<String> names) {
		super(source);
		this.names = names;
	}

	public List<String> getNames() {
		return names;
	}
}
