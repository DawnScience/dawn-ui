package org.dawnsci.plotting.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardDescriptor;

public class ExportLineTraceCommand extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWizard wiz = openWizard("org.dawb.common.ui.wizard.plotdataconversion", false);
			WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
			wd.setTitle(wiz.getWindowTitle());
			wd.open();
		} catch (Exception e) {
			throw new ExecutionException("Cannot export trace!", e);
		}
		return Boolean.TRUE;
	}

	private IWizard openWizard(String id, boolean requireOpen) throws Exception {
		// First see if this is a "new wizard".
		IWizardDescriptor descriptor = PlatformUI.getWorkbench()
				.getNewWizardRegistry().findWizard(id);
		// If not check if it is an "import wizard".
		if  (descriptor == null) {
			descriptor = PlatformUI.getWorkbench().getImportWizardRegistry()
					.findWizard(id);
		}
		// Or maybe an export wizard
		if  (descriptor == null) {
			descriptor = PlatformUI.getWorkbench().getExportWizardRegistry()
					.findWizard(id);
		}
		// Then if we have a wizard, open it.
		if  (descriptor != null) {
			IWizard wizard = descriptor.createWizard();
			if (requireOpen) {
				WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wizard);
				wd.setTitle(wizard.getWindowTitle());
				wd.open();
			}
			return wizard;
		}
		return null;
	}

}
