/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
