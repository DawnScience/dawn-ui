/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.ui.editors.slicing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.plot.IPlottingSystemData;
import org.eclipse.core.runtime.IProgressMonitor;
import org.nfunk.jep.JEP;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.SymbolTable;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public class ExpressionObject {
	
	private String expression;
	private String mementoKey;
	private IPlottingSystemData provider;
	
	public ExpressionObject(IPlottingSystemData provider) {
		this(provider, null, generateMementoKey());
	}


	public ExpressionObject(final IPlottingSystemData provider, String expression, String mementoKey) {
		this.provider   = provider;
		this.expression = expression;
		this.mementoKey = mementoKey;
	}

	public static boolean isExpressionKey(final String key) {
		if (key==null)      return false;
		if ("".equals(key)) return false;
		return key.matches("Expression\\:(\\d)+");
	}

	private static String generateMementoKey() {
		return "Expression:"+((new Date()).getTime());
	}


	/**
	 * @return Returns the expression.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression The expression to set.
	 */
	public void setExpression(String expression) {
		this.dataSet    = null;
		this.expression = expression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((mementoKey == null) ? 0 : mementoKey.hashCode());
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
		ExpressionObject other = (ExpressionObject) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (mementoKey == null) {
			if (other.mementoKey != null)
				return false;
		} else if (!mementoKey.equals(other.mementoKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return expression!=null ? expression : "";
	}
	
	public boolean isValid(IProgressMonitor monitor) {
		try {
			final SymbolTable vars = getSymbolTable();
		    for (Object key : vars.keySet()) {
		    	final Object value = vars.getValue(key);
		    	if (value==null) {
		    		if (monitor.isCanceled()) return false;
		    		if (!provider.isExpressionSetName(key.toString(), new ProgressMonitorWrapper(monitor))) return false;
		    	}
			}
			return true;
		} catch (Exception ne) {
			return false;
		}
	}

	/**
	 * Returns the size of the expression in the current environment.
	 * @return the size
	 */
	public int getSize(IProgressMonitor monitor) {
		if (dataSet==null) {
			try {
				getDataSet(monitor);
			} catch (Exception e) {
				return 0;
			}
		}
		return dataSet!=null ? dataSet.getSize() : 0;
	}

	private JEP             jepParser;
	private AbstractDataset dataSet;
	public AbstractDataset getDataSet(IProgressMonitor monitor) throws Exception {
		
		if (dataSet!=null) return dataSet;
		
	    if (expression==null||provider==null) return new DoubleDataset();
		
		final List<AbstractDataset> refs = getVariables(monitor);
		final double[]       data = new double[refs.get(0).getSize()];
		
		// TODO evaluate expression with numpy-like level not individual
		for (int i = 0; i < data.length; i++) {
			for (AbstractDataset d : refs) {
				final String name = getSafeName(d.getName());
				jepParser.addVariable(name, d.getDouble(i));
			}
			jepParser.parseExpression(expression);
			data[i] = jepParser.getValue();
		}
		
		this.dataSet = new DoubleDataset(data);
		dataSet.setName(getExpression());
		return this.dataSet;
	}

	private List<AbstractDataset> getVariables(IProgressMonitor monitor) throws Exception {
		
		final List<AbstractDataset> refs = new ArrayList<AbstractDataset>(7);
		final SymbolTable vars = getSymbolTable();
	    for (Object key : vars.keySet()) {
	    	final Object value = vars.getValue(key);
	    	if (value==null) {
	    		if (monitor.isCanceled()) return null;
	    		final AbstractDataset set = provider!=null 
	    		                          ? provider.getExpressionSet(key.toString(), new ProgressMonitorWrapper(monitor)) 
	    		                          : null;
	    		if (set!=null) refs.add(set);
	    	}
		}
	    
		if (refs.isEmpty()) throw new Exception("No variables recognized in expression.");
		
		// Check all same size
		final int size = refs.get(0).getSize();
		for (IDataset dataSet : refs) {
			if (dataSet.getSize()!=size) throw new Exception("Data sets in expression are not all the same size.");
		}

	    return refs;
	}

	private SymbolTable getSymbolTable() throws ParseException {
		jepParser = new JEP();
		jepParser.addStandardFunctions();
		jepParser.addStandardConstants();
		jepParser.setAllowUndeclared(true);
		jepParser.setImplicitMul(true);
		
	    jepParser.parse(expression);
	    return jepParser.getSymbolTable();
	}

	/**
	 * @return Returns the provider.
	 */
	public IPlottingSystemData getProvider() {
		return provider;
	}


	/**
	 * @param provider The provider to set.
	 */
	public void setProvider(IPlottingSystemData provider) {
		this.provider = provider;
	}


	/**
	 * @return Returns the mementoKey.
	 */
	public String getMementoKey() {
		return mementoKey;
	}


	/**
	 * @param mementoKey The mementoKey to set.
	 */
	public void setMementoKey(String mementoKey) {
		this.mementoKey = mementoKey;
	}


	/**
	 * Clears the current calculated data set from memory.
	 * Does not 
	 */
	public void clear() {
		this.dataSet = null;
	}


	/**
	 * Generates a safe expression name from an
	 * unsafe data set name. Possibly might not be 
	 * unique.
	 * 
	 * @param n
	 * @return
	 */
	public static String getSafeName(String n) {
		
		if (n==null) return null;
		
		if (n.matches("[a-zA-Z0-9_]+")) return n;
		
		final StringBuilder buf = new StringBuilder();
		for (char c : n.toCharArray()) {
			if (String.valueOf(c).matches("[a-zA-Z0-9_]")) {
				buf.append(c);
			} else {
				if (buf.length()<1 || "_".equals(buf.substring(buf.length()-1))) continue;
				buf.append("_");
			}
		}
		
		if (buf.length()<1) {
			buf.append("Invalid_name");
		} else if (buf.substring(0, 1).matches("[0-9]")) {
			buf.append("var", 0, 3);
		}
		
		return buf.toString();
	}
	
}
