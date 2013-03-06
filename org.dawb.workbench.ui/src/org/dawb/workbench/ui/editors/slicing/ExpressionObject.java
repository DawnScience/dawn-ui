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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.ExpressionImpl;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.plot.IPlottingSystemData;
import org.dawb.common.ui.slicing.DimsData;
import org.dawb.common.ui.slicing.DimsDataList;
import org.dawb.common.ui.slicing.SliceUtils;
import org.dawb.workbench.ui.Activator;
import org.dawnsci.jexl.utils.JexlUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;

public class ExpressionObject {
	
	private String expression;
	private String mementoKey;
	private IPlottingSystemData provider;
	private JexlEngine jexl;
	
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
			getDataSet(monitor);
			if (dataSet!=null) {
				return true;
			}
			if (jexl==null) jexl = JexlUtils.getDawnJexlEngine();
			ExpressionImpl ex    = (ExpressionImpl)jexl.createExpression(expression);
			Set<List<String>> names = ex.getVariables();
			
		    for (List<String> entry : names) {
		    	final String key = entry.get(0);
		    	if (monitor.isCanceled()) return false;
		    	if (!provider.isExpressionSetName(key, new ProgressMonitorWrapper(monitor))) return false;
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

	public String getShape(IProgressMonitor monitor) {
		if (dataSet==null) {
			try {
				getDataSet(monitor);
			} catch (Exception e) {
				return "0";
			}
		}
		return dataSet!=null ? Arrays.toString(dataSet.getShape()) : "0";
	}

	private AbstractDataset dataSet;
	public AbstractDataset getDataSet(IProgressMonitor monitor) throws Exception {
		
		if (dataSet!=null) return dataSet;
		
	    if (expression==null||provider==null) return new DoubleDataset();
	    
	    try {
	    	dataSet = getSlice();
	    	if (dataSet!=null) return dataSet;
	    } catch (Throwable ne) {
	    	// We try to parse it as an expression.
	    }
		
		final List<AbstractDataset> refs = getVariables(monitor);
		
		if (jexl==null) jexl = JexlUtils.getDawnJexlEngine();
		
		JexlContext context = new MapContext();

		for (AbstractDataset d : refs) {
			final String name = getSafeName(d.getName());
			context.set(name, d);
		}
		
		Expression ex = jexl.createExpression(expression);
        
		this.dataSet = (AbstractDataset)ex.evaluate(context);
		dataSet.setName(getExpression());
		return this.dataSet;
	}
	
	private static final Pattern SLICE = Pattern.compile("(.+)\\[([0-9:,\\-]+)\\]");

	private AbstractDataset getSlice() {
		
		final Matcher matcher = SLICE.matcher(getExpression());
		if (matcher.matches()) {
			try {
				final String filePath = provider.getFilePath();
				final String fullName = matcher.group(1);
				final String range    = matcher.group(2);
				final String[]  idx   = range.split(",");
				
				final IMetaData meta  = LoaderFactory.getMetaData(filePath, null);
				final int[]     shape = meta.getDataShapes().get(fullName);
				if (shape.length!=idx.length) {
					Activator.getDefault().getLog().log(new Status(IStatus.WARNING, "org.dawb.workbench.ui",
							"Cannot parse '"+getExpression()+"' as slice. The data is a different shape to the slice inside the []."));
					return null;
				}
				
				SliceObject sliceObject = new SliceObject();
				sliceObject.setPath(filePath);
				sliceObject.setName(fullName);
				
                final DimsDataList ddl = new DimsDataList(shape);
                int iaxis = 0;
                for (int index = 0; index < shape.length; index++) {
					final DimsData dd  = ddl.getDimsData(index);
					final String   inc = idx[index];
					if ("-".equals(inc) || "-1".equals(inc) || ":".equals(inc)) {
						dd.setAxis(iaxis);
						++iaxis;
					} else {
						dd.setAxis(-1);
						dd.setSlice(Integer.parseInt(inc));
					}
				}
                sliceObject = SliceUtils.createSliceObject(ddl, shape, sliceObject);
                AbstractDataset slice = LoaderFactory.getSlice(sliceObject, null);
    			slice = slice.squeeze();		
                slice.setName(getExpression());
                
                if (iaxis!=slice.getRank()) {
    				Activator.getDefault().getLog().log(new Status(IStatus.WARNING, "org.dawb.workbench.ui",
    						"Cannot parse '"+getExpression()+"' as slice. The rank of the slice is not the same as intended."));
                    return null;
                }
                
                return slice;
                
				
			} catch (Throwable ne) {
				Activator.getDefault().getLog().log(new Status(IStatus.WARNING, "org.dawb.workbench.ui",
				"Cannot parse '"+getExpression()+"' as slice. Slice syntax is: '/full_path[10,-,-] to get 10th image from 3D array."));
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(SLICE.matcher("/entry1/data[1,]").matches());
		System.out.println(SLICE.matcher("/entry1/data[1,-,-]").matches());
		System.out.println(SLICE.matcher("/entry1/data[1,-,-]").matches());
		System.out.println(SLICE.matcher("/entry1/data(1,2,)").matches());
		System.out.println(SLICE.matcher("fred(1,2,)").matches());
		System.out.println(SLICE.matcher("fred(1,2,)").matches());
		System.out.println(SLICE.matcher("/entry/exchange/white_z[14,-,-]").matches());
	}


	private List<AbstractDataset> getVariables(IProgressMonitor monitor) throws Exception {
		
		final List<AbstractDataset> refs = new ArrayList<AbstractDataset>(7);
		
		if (jexl==null) jexl = JexlUtils.getDawnJexlEngine();
		ExpressionImpl ex = (ExpressionImpl)jexl.createExpression(expression);
		final Set<List<String>> names = ex.getVariables();
		
	    for (List<String> entry : names) {
	    	final String key = entry.get(0);
	    	if (monitor.isCanceled()) return null;
	    	final AbstractDataset set = provider!=null 
	    			                  ? provider.getExpressionSet(key, new ProgressMonitorWrapper(monitor)) 
	    					          : null;
	    	if (set!=null) refs.add(set);
		}
	    
		if (refs.isEmpty()) throw new Exception("No variables recognized in expression.");

	    return refs;
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
