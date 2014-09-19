/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.celleditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

public abstract class AppliableCellEditor extends CellEditor {

    public AppliableCellEditor() {
    	super();
    }
    
	public AppliableCellEditor(Composite parent, int style) {
		super(parent,style);
	}

	public AppliableCellEditor(Composite parent) {
		super(parent);
	}

	public abstract void applyEditorValueAndDeactivate();

}
