package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

public class OpenDialogHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection ss = (StructuredSelection)selection;
			
			List<String> selectedFile = new ArrayList<String>();
			
			Iterator it = ss.iterator();
			
			while (it.hasNext()) {
				Object ob = it.next();
				if (ob instanceof IContain1DData) {
					selectedFile.add(((IContain1DData)ob).getLongName());
				}
			}
			
			if (!selectedFile.isEmpty()) {
				String string = selectedFile.get(0);
				MessageBox box = new MessageBox(Display.getCurrent().getActiveShell());
				box.setMessage(string);
				box.open();
			}

		}

		return null;
	}

}
