
package org.dawnsci.fileviewer.handlers;

import javax.inject.Inject;

import org.dawnsci.conversion.ui.ConvertWizard;
import org.dawnsci.fileviewer.FileViewer;
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

	/**
	 * TODO
	 * Make the StructuredSelection an ITransferableDataObject
	 */
	@Execute
	public void execute() {
		ConvertWizard cwizard = new ConvertWizard();
		IStructuredSelection sel = fileviewer.getSelection();
		cwizard.setSelectionOverride(sel.toList());
		WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), cwizard);
		dialog.setPageSize(new Point(400, 450));
		dialog.create();
		dialog.open();
	}

}