package org.dawnsci.mapping.ui.actions;

import java.util.Collections;
import java.util.List;

import org.dawnsci.january.ui.utils.SelectionUtils;
import org.dawnsci.mapping.ui.Activator;
import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.IMapPlotController;
import org.dawnsci.mapping.ui.dialog.MapPropertiesDialog;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;

public class TransparencyHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IMapFileController fc = Activator.getService(IMapFileController.class);
		IMapPlotController pc = Activator.getService(IMapPlotController.class);
		Object evaluationContext = event.getApplicationContext();

		List<AbstractMapData> s = getListFromContext(evaluationContext);
			if (s.size() == 1) {
				MapPropertiesDialog d = new MapPropertiesDialog(Display.getDefault().getActiveShell(), s.get(0), pc,fc);
				d.open();
			}
			
		
		return null;
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		
		List<AbstractMapData> s = getListFromContext(evaluationContext);
		if (s.size() == 1) {
			setBaseEnabled(true);
		} else {
			setBaseEnabled(false);
		}
	}
	
	private List<AbstractMapData> getListFromContext(Object context) {
		Object variable = context instanceof ExecutionEvent ? HandlerUtil.getVariable((ExecutionEvent) context, ISources.ACTIVE_CURRENT_SELECTION_NAME):
			HandlerUtil.getVariable(context, ISources.ACTIVE_CURRENT_SELECTION_NAME);
		
		if (variable instanceof ISelection) {
			return SelectionUtils.getFromSelection((ISelection)variable, AbstractMapData.class);
		}
		
		return Collections.emptyList();
		
	}

}
