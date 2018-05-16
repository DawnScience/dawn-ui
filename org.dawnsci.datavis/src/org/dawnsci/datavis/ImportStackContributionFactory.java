package org.dawnsci.datavis;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.FileJoining;
import org.dawnsci.datavis.model.IFileController;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class ImportStackContributionFactory extends ExtensionContributionFactory {

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		MenuManager search = new MenuManager("Import Stack",
                "org.dawnsci.importstack");
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		final IRecentPlaces recentPlaces = bundleContext.getService(bundleContext.getServiceReference(IRecentPlaces.class));
		final IFileController admin = bundleContext.getService(bundleContext.getServiceReference(IFileController.class));
		
		search.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				search.removeAll();
				Collection<String> lf = recentPlaces.getRecentPlaces();
				
				String path = null;
				
				if (!lf.isEmpty()) {
					path = lf.iterator().next();
				}
				

				search.add(new OpenFileDialogAction(path,admin));
				search.add(new OpenFolderDialogAction(path,admin));
				
				
			}
		});

		search.add(new Action("") {
        	
        	@Override
        	public void run() {

        	}
		});

        additions.addContributionItem(search, null);

	}
	
	private class OpenFileDialogAction extends Action {
		
		private String folder;
		private IFileController controller;
		
		public OpenFileDialogAction(String folder, IFileController controller) {
			super("From files...");
			this.folder = folder;
			this.controller = controller;
		}
		
		@Override
    	public void run() {
    		Shell shell = Display.getDefault().getActiveShell();
    		FileDialog dialog = new FileDialog(shell,SWT.MULTI);
    		dialog.setFilterPath(folder);

    		if (dialog.open() == null) return;

    		String[] fileNames = dialog.getFileNames();
    		for (int i = 0; i < fileNames.length; i++) fileNames[i] = dialog.getFilterPath() + File.separator + fileNames[i];
    		
    		String joined = FileJoining.autoFileJoiner(Arrays.asList(fileNames));
    		
    		if (controller instanceof FileController) {
    			((FileController) controller).loadFiles(new String[] {joined}, null, false);
    		}

    		
		}
	}
		
		private class OpenFolderDialogAction extends Action {
			
			private String folder;
			private IFileController controller;
			
			public OpenFolderDialogAction(String folder, IFileController controller) {
				super("From directory...");
				this.folder = folder;
				this.controller = controller;
			}
			
			@Override
	    	public void run() {
	    		Shell shell = Display.getDefault().getActiveShell();
	    		DirectoryDialog dialog = new DirectoryDialog(shell,SWT.NONE);
	    		dialog.setFilterPath(folder);

	    		String open = dialog.open();
	    		
	    		if (open == null) return;
	    		
	    		String joined = FileJoining.autoFileJoiner(Arrays.asList(new String[] {open}));
	    		
	    		if (controller instanceof FileController) {
	    			((FileController) controller).loadFiles(new String[] {joined}, null, false);
	    		}

	    		
			}
		
	}

}
