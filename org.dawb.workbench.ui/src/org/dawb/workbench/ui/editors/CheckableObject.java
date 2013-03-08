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

import org.dawb.common.ui.plot.IExpressionPlottingManager;
import org.dawb.hdf5.editor.H5Path;
import org.dawb.workbench.ui.editors.slicing.ExpressionObject;

public class CheckableObject implements H5Path{

	private boolean          checked;
	private String           name;
	private String           variable;
	private ExpressionObject expression;
	private String           mementoKey;
	
	private static int expressionCount=0;
	
	public CheckableObject() {
		expressionCount++;
		this.variable   = "expr"+expressionCount;
	}
	public CheckableObject(final String name) {
		this.name     = name;
		this.variable = ExpressionObject.getSafeName(name);
	}
	
	public CheckableObject(ExpressionObject expression2) {
		this.expression = expression2;
		expressionCount++;
		this.variable   = "expr"+expressionCount;
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
	public String getName() {
		if (expression!=null) return expression.getExpressionString();
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
	
	/**
	 * Get the axis, X, Y1..Y4
	 * 
	 * If this object is not in 
	 * 
	 * @param selections
	 * @return
	 */
	public String getAxis(List<CheckableObject> selections, boolean is2D, boolean isXFirst) {
		
		if (is2D) return isChecked() ? "-" : "";
		int axis = getAxisIndex(selections, isXFirst);
		if (axis<0) return "";
		if (axis==0) {
			return isXFirst ? "X" : "Y1";
		}
		return "Y"+axis;
	}
	
	public int getAxisIndex(List<CheckableObject> selections, boolean isXFirst) {
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
	public int getYaxis() {
		return yaxis;
	}
	public void setYaxis(int yaxis) {
		this.yaxis = yaxis;
	}
    public String getVariable() {
		return variable;
	}
	public void setVariable(String variable) {
		this.variable = variable;
		if (expression!=null) expression.clear();
	}
	
	private static final String DELIMITER = "£";
	
	public void createExpression(IExpressionPlottingManager psData, String mementoKey, String memento) {
		final String[] parts = memento.split(DELIMITER);
		this.variable   = parts[0];
		this.expression = new ExpressionObject(psData, parts[1]);
	}
	
	public String getMemento() {
		return variable+DELIMITER+getName();
	}
	
	/**
	 * @return Returns the mementoKey.
	 */
	public String getMementoKey() {
		if (mementoKey==null) mementoKey = generateMementoKey();
		return mementoKey;
	}


	/**
	 * @param mementoKey The mementoKey to set.
	 */
	public void setMementoKey(String mementoKey) {
		this.mementoKey = mementoKey;
	}

 }
