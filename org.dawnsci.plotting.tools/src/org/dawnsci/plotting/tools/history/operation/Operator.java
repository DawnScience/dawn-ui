/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.history.operation;

/**
 * 
 * @author Baha El Kassaby - duplicated code from org.dawb.common.gpu (this code
 *         has been moved here as it is only used for HistoryTool)
 *
 */
public enum Operator {

	ADD("     +     "),
	SUBTRACT("     -     "),
	MULTIPLY("     x     "),
	DIVIDE("     ÷     ");
	
	private String name;

	Operator(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getIndex() {
		final Operator[] ops = Operator.values();
		for (int i = 0; i < ops.length; i++) if (ops[i]==this) return i;
		return -1;
	}

	public static String[] getOperators() {
		final Operator[] ops = Operator.values();
		final String[] names = new String[ops.length];
		for (int i = 0; i < ops.length; i++) {
			names[i] = ops[i].getName();
		}
		return names;
	}

	public static Operator getOperator(int index) {
		final Operator[] ops = Operator.values();
		return ops[index];
	}
	
}