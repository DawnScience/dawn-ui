package org.dawnsci.datavis.view.perspective;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class FileCloseHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<LoadedFile> list = getSelectedFiles(event);

		IFileController controller = ServiceProvider.getService(IFileController.class);
		controller.unloadFiles(list);

		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		List<LoadedFile> list = getSelectedFiles(evaluationContext);
		setBaseEnabled(!list.isEmpty() && list.get(0).getTree() != null);
	}

	private List<LoadedFile> getSelectedFiles(Object evaluationContext) {
		Object variable = evaluationContext instanceof ExecutionEvent executionEvent ?
				HandlerUtil.getVariable(executionEvent, ISources.ACTIVE_CURRENT_SELECTION_NAME):
				HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);

		if (variable instanceof StructuredSelection sel) {
			return Arrays.stream(sel.toArray())
			.filter(LoadedFile.class::isInstance)
			.map(LoadedFile.class::cast).toList();
		}

		return Collections.emptyList();
	}
}
