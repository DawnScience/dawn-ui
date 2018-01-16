package org.dawnsci.mapping.ui;

import java.util.Map;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.wizards.ImportMappedDataWizard;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class BeanBuilderWizard implements IBeanBuilderHelper {

	@Override
	public void build(String path, Map<String, int[]> datasetNames, IMetadata meta) {
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					build(path, datasetNames, meta);
				}
			});
			
			return;
		}

		final ImportMappedDataWizard wiz = new ImportMappedDataWizard(path, datasetNames, meta);
		wiz.setNeedsProgressMonitor(true);
		final WizardDialog wd = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),wiz);
		wd.setPageSize(new Point(900, 500));
		wd.create();
		
		if (wd.open() == WizardDialog.CANCEL) return;
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		IMapFileController f = bundleContext.getService(bundleContext.getServiceReference(IMapFileController.class));
		
		f.loadFile(path, wiz.getMappedDataFileBean(), null);

	}

}
