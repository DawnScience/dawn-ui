package org.dawnsci.datavis.view.perspective;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.view.parts.ServiceManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;

public class FileCloseHander extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveSite(event).getWorkbenchWindow().getSelectionService().getSelection("org.dawnsci.datavis.view.parts.LoadedFilePart");
		List<LoadedFile> list = getSelectedFiles(selection);

		ServiceManager.getFileController().unloadFiles(list);

		return null;
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		Object variable = HandlerUtil.getVariable(evaluationContext, "activeSite");
		if (variable != null && variable instanceof IWorkbenchSite) {
			ISelection selection = ((IWorkbenchSite)variable).getWorkbenchWindow().getSelectionService().getSelection("org.dawnsci.datavis.view.parts.LoadedFilePart");
			List<LoadedFile> list = getSelectedFiles(selection);
			setBaseEnabled(!list.isEmpty());
		}
	}
	
	private List<LoadedFile> getSelectedFiles(ISelection selection){

		if (selection instanceof StructuredSelection) {
			List<LoadedFile> list = Arrays.stream(((StructuredSelection)selection).toArray())
			.filter(LoadedFile.class::isInstance)
			.map(LoadedFile.class::cast).collect(Collectors.toList());
			
			return list;
		}
		
		return new ArrayList<LoadedFile>();
	}

}
