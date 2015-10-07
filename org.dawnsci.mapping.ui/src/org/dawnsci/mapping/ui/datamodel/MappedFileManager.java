package org.dawnsci.mapping.ui.datamodel;

import java.lang.reflect.InvocationTargetException;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.mapping.ui.LocalServiceManager;
import org.dawnsci.mapping.ui.MapPlotManager;
import org.dawnsci.mapping.ui.dialog.RegistrationDialog;
import org.dawnsci.mapping.ui.wizards.ImportMappedDataWizard;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

public class MappedFileManager {

	private MapPlotManager plotManager;
	private MappedDataArea mappedDataArea;
	private Viewer viewer;


	public MappedFileManager(MapPlotManager plotManager, MappedDataArea mappedDataArea, Viewer viewer) {
		this.plotManager = plotManager;
		this.mappedDataArea = mappedDataArea;
		this.viewer = viewer;
	}
	
	public void removeFile(MappedDataFile file) {
		mappedDataArea.removeFile(file);
		plotManager.clearAll();
		viewer.refresh();
	}
	
	public boolean contains(String path) {
		return mappedDataArea.contains(path);
	}
	
	public void importFile(final String path, final MappedDataFileBean bean) {
		if (contains(path)) return;
		IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
		try {
			service.busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
					IMonitor m = new ProgressMonitorWrapper(monitor);
					monitor.beginTask("Loading data...", -1);
					final MappedDataFile mdf = MappedFileFactory.getMappedDataFile(path, bean, m);
					if (m.isCancelled()) return;


					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							boolean load = true;
							if (!mappedDataArea.isInRange(mdf)) {
								load = MessageDialog.openConfirm(viewer.getControl().getShell(), "No overlap!", "Are you sure you want to load this data?");
							} 

							if (load)mappedDataArea.addMappedDataFile(mdf);
							plotManager.clearAll();
							plotManager.plotMap(null);
							viewer.refresh();
							if (viewer instanceof TreeViewer) {
								((TreeViewer)viewer).expandToLevel(mdf, 1);
							}
						}
					});


				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 

	
	public void importFile(final String path) {
		if (contains(path)) return;
		final ImportMappedDataWizard wiz = new ImportMappedDataWizard(path);
		wiz.setNeedsProgressMonitor(true);
		final WizardDialog wd = new WizardDialog(Display.getDefault().getActiveShell(),wiz);
		wd.setPageSize(new Point(900, 500));
		wd.create();
		
		if (wiz.isImageImport()) {
			IDataset im;
			try {
				im = LocalServiceManager.getLoaderService().getDataset(path, null);
				RegistrationDialog dialog = new RegistrationDialog(Display.getDefault().getActiveShell(), plotManager.getTopMap().getMap(),im);
				if (dialog.open() != IDialogConstants.OK_ID) return;
				AssociatedImage asIm = new AssociatedImage("Registered", (RGBDataset)dialog.getRegisteredImage());
				mappedDataArea.addMappedDataFile(MappedFileFactory.getMappedDataFile(path, asIm));
				viewer.refresh();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}

		if (wd.open() == WizardDialog.CANCEL) return;
		
		importFile(path, wiz.getMappedDataFileBean());
		
	}
	
	
}
