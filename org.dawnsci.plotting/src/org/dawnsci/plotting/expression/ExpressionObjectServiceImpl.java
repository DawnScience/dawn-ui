package org.dawnsci.plotting.expression;

import java.util.List;

import org.dawb.common.services.IExpressionObject;
import org.dawb.common.services.IExpressionObjectService;
import org.dawb.common.services.IVariableManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public class ExpressionObjectServiceImpl extends AbstractServiceFactory implements IExpressionObjectService {

	@Override
	public IExpressionObject createExpressionObject(IVariableManager manager, String expressionName, String expression) {
		return new ExpressionObject(manager, expressionName, expression);
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

	@SuppressWarnings("rawtypes")
	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface==IExpressionObjectService.class) {
			return new ExpressionObjectServiceImpl();
		} 
		return null;
	}
	
	/**
	 * 
	 * @param sourcePath, may be null
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<IExpressionObject> getActiveExpressions(String sourcePath) throws Exception {
		
	    final IWorkbenchPage page = getPage();
	    final IViewPart dataPart  = page.findView("org.dawb.workbench.views.dataSetView");
	    if (dataPart!=null) {
	    	final IFile currentData = (IFile)dataPart.getAdapter(IFile.class);
	    	
	    	sourcePath = sourcePath!=null ? sourcePath.replace('\\','/') : null;
	    	if (currentData!=null && sourcePath!=null) {
	    		final String curPath = currentData.getLocation().toOSString();
	    		if (curPath!=null && curPath.replace('\\','/').equals(sourcePath)) {
	    			return (List<IExpressionObject>)dataPart.getAdapter(List.class);
	    		}
	    	} else if (sourcePath==null) {
	    		return (List<IExpressionObject>)dataPart.getAdapter(List.class);
	    	}
	    }
	    
	    return null;
	}

	public static IWorkbenchPage getPage() {
		IWorkbenchPage activePage = getActivePage();
		if (activePage!=null) return activePage;
		return getDefaultPage();
	}
	
	/**
	 * @return IWorkbenchPage
	 */
	public static IWorkbenchPage getActivePage() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench==null) return null;
		final IWorkbenchWindow window = bench.getActiveWorkbenchWindow();
		if (window==null) return null;
		return window.getActivePage();
	}
	
	public static IWorkbenchPage getDefaultPage() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench==null) return null;
		final IWorkbenchWindow[] windows = bench.getWorkbenchWindows();
		if (windows==null) return null;
		
		return windows[0].getActivePage();
	}

}
