package org.dawnsci.plotting.expression;

import gda.analysis.io.ScanFileHolderException;

import java.util.List;
import java.util.Set;

import org.apache.commons.jexl2.ExpressionImpl;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.dawb.common.services.IVariableManager;
import org.dawnsci.jexl.utils.JexlUtils;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.ILazyLoader;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public class ExpressionLazyLoader implements ILazyLoader {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6955921350112844303L;
	
	private final String           variableName;
	private final String           expressionString;
	private final IVariableManager manager;
	private final JexlEngine       jexl;

	public ExpressionLazyLoader(final String           variableName,
			                    final String           expressionString,
			                    final IVariableManager manager) {
		
		this.variableName     = variableName;
		this.expressionString = expressionString;
		this.manager          = manager;
		this.jexl             = JexlUtils.getDawnJexlEngine();
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
		
		ExpressionImpl ex = (ExpressionImpl)jexl.createExpression(expressionString);
		final Set<List<String>> names = ex.getVariables();

		JexlContext context = new MapContext();
				
	    for (List<String> entry : names) {
	    	final String name = entry.get(0);
	    	try {
	    		ILazyDataset set = manager.getLazyValue(name, mon);
	    		context.set(name, set.getSlice(mon, start, stop, step));
	    		
	    	} catch (Throwable ignored) {
	    		// We try to add the unsliced value as they may be putting a 
	    		// variable into a function which gives the right size, e.g
	    		// medium of a stack for flat field images.
	    		try {
		    		IDataset set = manager.getVariableValue(name, mon);
		    		context.set(name, set);
	    		} catch (Throwable ignored2) { // Might get memory problems here.
	    			continue;
	    		}
	    	}
	    }
       
		AbstractDataset value = (AbstractDataset)ex.evaluate(context);
		value.setName("Slice of "+variableName);
		
		return value;

	}

}
