/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.editors;

import org.dawb.hdf5.editor.H5Path;
import org.dawb.workbench.ui.editors.slicing.ExpressionObject;

public class CheckableObject implements H5Path{

	private boolean          checked;
	private String           name;
    private ExpressionObject expression;
	public CheckableObject() {
		
	}
	public CheckableObject(final String name) {
		this.name = name;
	}
	
	public CheckableObject(ExpressionObject expression2) {
		this.expression = expression2;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (checked ? 1231 : 1237);
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CheckableObject other = (CheckableObject) obj;
		if (checked != other.checked)
			return false;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	public String getName() {
		if (expression!=null) return expression.getExpression();
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ExpressionObject getExpression() {
		return expression;
	}
	public void setExpression(ExpressionObject expression) {
		this.expression = expression;
	}
    public boolean isExpression() {
    	return expression!=null;
    }
    public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public String toString() {
		if (expression!=null) return expression.toString();
		return name;
	}
	
	public String getPath() {
		return name;
	}
}
