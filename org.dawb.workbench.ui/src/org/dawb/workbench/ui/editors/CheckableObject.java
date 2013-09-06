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

import java.util.List;

import org.dawb.common.services.IExpressionObject;
import org.dawb.common.services.IExpressionObjectService;
import org.dawb.common.services.IVariableManager;
import org.dawb.hdf5.editor.H5Path;
import org.dawnsci.slicing.api.data.ICheckableObject;
import org.eclipse.ui.PlatformUI;

public class CheckableObject implements H5Path, ICheckableObject{

	private boolean           checked;
	private String            name;
	private String            variable;
	private IExpressionObject expression;
	private String            mementoKey;
	private IExpressionObjectService service;

	private static int expressionCount=0;

	public CheckableObject() {
		expressionCount++;
		this.variable   = "expr"+expressionCount;
		this.service  = (IExpressionObjectService)PlatformUI.getWorkbench().getService(IExpressionObjectService.class);
	}

	public CheckableObject(final String name) {
		this.name     = name;
		this.service  = (IExpressionObjectService)PlatformUI.getWorkbench().getService(IExpressionObjectService.class);
		this.variable = service.getSafeName(name);
	}

	public CheckableObject(IExpressionObject expression2) {
		this.expression = expression2;
		expressionCount++;
		this.variable   = "expr"+expressionCount;
		this.service  = (IExpressionObjectService)PlatformUI.getWorkbench().getService(IExpressionObjectService.class);
		expression2.setExpressionName(variable);
	}

	public static boolean isMementoKey(final String key) {
		if (key==null)      return false;
		if ("".equals(key)) return false;
		return key.matches("CheckableObject\\$(\\d)+\\$(.+)");
	}

	private String generateMementoKey() {
		return "CheckableObject$"+System.currentTimeMillis()+"$"+getVariable();
	}

	public static String getVariable(String memento) {
		final String[] parts = memento.split(DELIMITER);
		return parts[0];
	}

	public static String getName(String memento) {
		final String[] parts = memento.split(DELIMITER);
		return parts[1];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (checked ? 1231 : 1237);
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((variable == null) ? 0 : variable.hashCode());
		result = prime * result + yaxis;
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
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		if (yaxis != other.yaxis)
			return false;
		return true;
	}

	@Override
	public String getName() {
		if (expression!=null) return expression.getExpressionString();
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public IExpressionObject getExpression() {
		return expression;
	}

	@Override
	public void setExpression(IExpressionObject expression) {
		this.expression = expression;
	}

    @Override
	public boolean isExpression() {
    	return expression!=null;
    }

    @Override
	public boolean isChecked() {
		return checked;
	}

	@Override
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	@Override
	public String toString() {
		if (expression!=null) return expression.toString();
		return name;
	}

	@Override
	public String getPath() {
		return name;
	}

	@Override
	public String getAxis(List<ICheckableObject> selections, boolean is2D, boolean isXFirst) {
		
		if (is2D) return isChecked() ? "-" : "";
		int axis = getAxisIndex(selections, isXFirst);
		if (axis<0) return "";
		if (axis==0) {
			return isXFirst ? "X" : "Y1";
		}
		return "Y"+axis;
	}

	@Override
	public int getAxisIndex(List<ICheckableObject> selections, boolean isXFirst) {
		if (selections!=null&&!selections.isEmpty()) {
			if (selections.size()>1) {
				if (selections.contains(this)) {
					if (selections.indexOf(this)==0) {
						return isXFirst ? 0 : 1;
					}
					return yaxis;
				}
			} if (selections.size()==1 && selections.contains(this)) {
				return yaxis;
			}
		}
		
        return -1;
    }
	
	private int yaxis = 1;

	@Override
	public int getYaxis() {
		return yaxis;
	}

	@Override
	public void setYaxis(int yaxis) {
		this.yaxis = yaxis;
	}

    @Override
	public String getVariable() {
		return variable;
	}

	@Override
	public void setVariable(String variable) {
		this.variable = variable;
		if (expression!=null) {
			expression.clear();
			expression.setExpressionName(variable);
		}
	}
	
	private static final String DELIMITER = "£";
	
	@Override
	public void createExpression(IVariableManager psData, String mementoKey, String memento) {
		final String[] parts = memento.split(DELIMITER);
		this.variable   = parts[0];
		this.expression = service.createExpressionObject(psData, variable, parts[1]);
	}
	
	@Override
	public String getMemento() {
		return variable+DELIMITER+getName();
	}
	
	@Override
	public String getMementoKey() {
		if (mementoKey==null) mementoKey = generateMementoKey();
		return mementoKey;
	}

	@Override
	public void setMementoKey(String mementoKey) {
		this.mementoKey = mementoKey;
	}

 }
