package org.dawnsci.datavis.view.perspective;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.view.ActionServiceManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;

public class FileCloseHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<LoadedFile> list = getSelectedFiles(event);

		IFileController controller = ActionServiceManager.getFileController();
		controller.unloadFiles(list);

		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		List<LoadedFile> list = getSelectedFiles(evaluationContext);
		setBaseEnabled(!list.isEmpty() && list.get(0).getTree() != null);
	}

	private List<LoadedFile> getSelectedFiles(Object evaluationContext) {
		Object variable = evaluationContext instanceof ExecutionEvent ? HandlerUtil.getVariable((ExecutionEvent) evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME):
				HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);

		if (variable instanceof StructuredSelection) {
			List<LoadedFile> list = Arrays.stream(((StructuredSelection) variable).toArray())
			.filter(LoadedFile.class::isInstance)
			.map(LoadedFile.class::cast).collect(Collectors.toList());
			
			return list;
		}

		return Collections.emptyList();
	}
}
