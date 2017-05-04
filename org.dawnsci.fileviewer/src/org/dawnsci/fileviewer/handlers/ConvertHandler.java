
package org.dawnsci.fileviewer.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.dawnsci.conversion.ui.ConvertWizard;
import org.dawnsci.fileviewer.FileViewer;
import org.dawnsci.fileviewer.table.FileTableContent;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class ConvertHandler {

	private FileViewer fileviewer;

	@Inject
	public ConvertHandler(FileViewer viewer) {
		this.fileviewer = viewer;
	}

	@SuppressWarnings("unchecked")
	@Execute
	public void execute() {
		ConvertWizard cwizard = new ConvertWizard();
		IStructuredSelection sel = fileviewer.getSelections();
		@SuppressWarnings("rawtypes")
		List selFiles;
		if (sel.getFirstElement() instanceof FileTableContent) {
			selFiles = new ArrayList<>();
			for (Object selElement : sel.toArray()) {
				selFiles.add(((FileTableContent)selElement).getFile());
			}
		} else {
			selFiles = sel.toList();
		}
		cwizard.setSelectionOverride(selFiles);
		WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), cwizard);
		dialog.setPageSize(new Point(400, 450));
		dialog.create();
		dialog.open();
	}

}