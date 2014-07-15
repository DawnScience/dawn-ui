package org.dawnsci.plotting.system.example;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;

public class TestCommand extends AbstractHandler implements IHandler {

	/**
	 * In order to activate this example command, there is a commented out section
	 * in plugin.xml of this plugin.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
        final IPlottingSystem system = (IPlottingSystem)event.getApplicationContext();
        System.out.println(system.getPlotName());
		return null;
	}

}
