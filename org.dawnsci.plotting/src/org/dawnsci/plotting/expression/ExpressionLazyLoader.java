package org.dawnsci.plotting.expression;

import gda.analysis.io.ScanFileHolderException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dawb.common.services.IVariableManager;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.expressions.IExpressionEngine;
import org.dawb.common.services.expressions.IExpressionService;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.ILazyLoader;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

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
	public AbstractDataset getDataset(IMonitor mon,
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
		final Set<List<String>> names = engine.getVariableNamesFromExpression();
		
		Map<String,Object> context = new HashMap<String,Object>();
		//JexlContext context = new MapContext();
				
	    for (List<String> entry : names) {
	    	final String name = entry.get(0);
	    	try {
	    		ILazyDataset set = manager.getLazyValue(name, mon);
	    		context.put(name, set.getSlice(mon, start, stop, step));
	    		
	    	} catch (Throwable ignored) {
	    		// We try to add the unsliced value as they may be putting a 
	    		// variable into a function which gives the right size, e.g
	    		// medium of a stack for flat field images.
	    		try {
		    		IDataset set = manager.getVariableValue(name, mon);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    if (!(output instanceof AbstractDataset))return null;
		AbstractDataset value = (AbstractDataset)output;
		value.setName("Slice of "+variableName);
		
		return value;

	}

}
