/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.services.IVariableManager;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.expressions.IExpressionEngine;
import org.dawb.common.services.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.ILazyLoader;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;

class ExpressionLazyLoader implements ILazyLoader {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6955921350112844303L;
	
	private final String           variableName;
	private final String           expressionString;
	private final IVariableManager manager;
	private IExpressionEngine       engine;

	public ExpressionLazyLoader(final String           variableName,
			                    final String           expressionString,
			                    final IVariableManager manager) {
		
		this.variableName     = variableName;
		this.expressionString = expressionString;
		this.manager          = manager;
		
		try {
			IExpressionService service = (IExpressionService)ServiceManager.getService(IExpressionService.class);
			this.engine = service.getExpressionEngine();
		} catch (Exception e) {
			// TODO Auto-generated catch block, find out what happens when there is no service
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean isFileReadable() {
		return true;
	}

	@Override
	public Dataset getDataset(IMonitor mon,
			 							int[] shape,
			 							int[] start,
			 							int[] stop, 
			 							int[] step) throws ScanFileHolderException {
		
		try {
			engine.createExpression(expressionString);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		final Collection<String> names = engine.getVariableNamesFromExpression();
		
		Map<String,Object> context = new HashMap<String,Object>();
		//JexlContext context = new MapContext();
				
	    for (final String name : names) {
	    	try {
	    		ILazyDataset set = manager.getLazyValue(name, mon);
	    		context.put(name, set.getSlice(mon, start, stop, step));
	    		
	    	} catch (Throwable ignored) {
	    		// We try to add the unsliced value as they may be putting a 
	    		// variable into a function which gives the right size, e.g
	    		// medium of a stack for flat field images.
	    		try {
		    		IDataset set = manager.getVariableValue(name, mon);
		    		if (set==null) continue;
		    		context.put(name, set);
	    		} catch (Throwable ignored2) { // Might get memory problems here.
	    			continue;
	    		}
	    	}
	    }
       
	    Object output = null;
		try {
			engine.setLoadedVariables(context);
			output = engine.evaluate();
		} catch (Exception e) {
			return null;
		}
	    if (!(output instanceof Dataset))return null;
		Dataset value = (Dataset)output;
		value.setName("Slice of "+variableName);
		
		return value;

	}

}
