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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class MappedFileManager {

	private MapPlotManager plotManager;
	private MappedDataArea mappedDataArea;
	private Viewer viewer;


	public MappedFileManager(MapPlotManager plotManager, MappedDataArea mappedDataArea, Viewer viewer) {
		this.plotManager = plotManager;
		this.mappedDataArea = mappedDataArea;
		this.viewer = viewer;
	}
	
	
	public void importFile(final String path) {
		
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
				mappedDataArea.getDataFile(0).addMapObject("Registered", asIm);
				viewer.refresh();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}

		if (wd.open() == WizardDialog.CANCEL) return;
		
		ProgressMonitorDialog pm = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
		
		try {
			pm.run(true, true, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					IMonitor m = new ProgressMonitorWrapper(monitor);
					monitor.beginTask("Loading data...", -1);
					final MappedDataFile mdf = MappedFileFactory.getMappedDataFile(path, wiz.getMappedDataFileBean(),m);
					if (m.isCancelled()) return;
					mappedDataArea.addMappedDataFile(mdf);
					Display.getDefault().syncExec(new Runnable() {
						
						@Override
						public void run() {
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
	
	
}
