package org.dawnsci.mapping.ui.actions;

import java.util.Collections;
import java.util.List;

import org.dawnsci.january.ui.utils.SelectionUtils;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.dialog.DynamicDialog;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class DynamicViewerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object evaluationContext = event.getApplicationContext();
		
		List<MappedDataBlock> s = getListFromContext(evaluationContext);
		if (s.size() == 1 && s.get(0).getLazy().getRank() == 3) {
		
		DynamicDialog dialog = new DynamicDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), s.get(0));
		dialog.open();
		}
		
		return null;
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		
		List<MappedDataBlock> s = getListFromContext(evaluationContext);

		if (s.size() == 1 && s.get(0).getLazy().getRank() == 3 && !s.get(0).isRemappingRequired()) {
			setBaseEnabled(true);
		} else {
			setBaseEnabled(false);
		}
	}
	
	private List<MappedDataBlock> getListFromContext(Object context) {
		Object variable = context instanceof ExecutionEvent ? HandlerUtil.getVariable((ExecutionEvent) context, ISources.ACTIVE_CURRENT_SELECTION_NAME):
			HandlerUtil.getVariable(context, ISources.ACTIVE_CURRENT_SELECTION_NAME);
		
		if (variable instanceof ISelection) {
			return SelectionUtils.getFromSelection((ISelection)variable, MappedDataBlock.class);
		}
		
		return Collections.emptyList();
		
	}

}
