package org.dawnsci.surfacescatter.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class OpenDialogHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell s =PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell();

//		SurfaceScatterPresenter ssp = new SurfaceScatterPresenter();
//
//		ssp.setImageFolderPath(null);

		SurfaceScatterViewStart ssvs = new SurfaceScatterViewStart(s, 
				null, 
				0, 
				null,
				null,
				null);

//		ssp.setSsvs(ssvs);
		ssvs.open();
		
		return null;
	}

}
