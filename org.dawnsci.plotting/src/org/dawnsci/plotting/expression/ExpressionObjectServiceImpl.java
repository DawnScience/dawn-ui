package org.dawnsci.plotting.expression;

import org.dawb.common.services.IExpressionObject;
import org.dawb.common.services.IExpressionObjectService;
import org.dawb.common.services.IVariableManager;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public class ExpressionObjectServiceImpl extends AbstractServiceFactory implements IExpressionObjectService {

	@Override
	public IExpressionObject createExpressionObject(IVariableManager manager, String expression) {
		return new ExpressionObject(manager, expression);
	}
	
	@Override
	public String validate(IVariableManager manager, String variableName) throws Exception {

		if (variableName==null) throw new Exception("A blank variable name is not allowed!");
		
		variableName = variableName.trim();
		if ("".equals(variableName)) throw new Exception("A blank variable name is not allowed!");
		
		final String    safeName = getSafeName(variableName);				
		if (!variableName.equals(safeName) || variableName==null || "".equals(variableName)) {
			throw new Exception("The expression variable name '"+safeName+"' is not allowed.");
		}
		
		if ("new".equals(safeName)) throw new Exception("The name '"+safeName+"' is not allowed as a variable name!");
		if ("e".equals(safeName)) throw new Exception("The name '"+safeName+"' is not allowed as a variable name!");

		
		final boolean isExistingVariable = manager.isVariableName(safeName, new IMonitor.Stub());				
		if (isExistingVariable) throw new Exception("The name '"+safeName+"' is not unique!");
		
		return safeName;
	}

	@Override
	public String getSafeName(String name) {
		return ExpressionObject.getSafeName(name);
	}

	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface==IExpressionObjectService.class) {
			return new ExpressionObjectServiceImpl();
		} 
		return null;
	}

}
