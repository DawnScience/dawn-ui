package org.dawnsci.mapping.ui.actions;

import org.dawnsci.mapping.ui.Activator;
import org.dawnsci.mapping.ui.datamodel.HighAspectImageDisplay;
import org.dawnsci.mapping.ui.datamodel.IMapPlotController;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public abstract class AbstractHighAspectImageHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IMapPlotController service = Activator.getService(IMapPlotController.class);
		service.setHighAspectImageDisplayMode(getDisplayMode());
		return null;
	}
	
	protected abstract HighAspectImageDisplay getDisplayMode();

}
