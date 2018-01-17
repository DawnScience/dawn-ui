package org.dawnsci.datavis;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.IPlotController;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
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

public class RecentFoldersContributionFactory extends ExtensionContributionFactory {

	public RecentFoldersContributionFactory() {
		this.toString();
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		
		MenuManager search = new MenuManager("Recent Places",
                "org.dawnsci.cake");
		
//		BundleContext bundleContext =
//                FrameworkUtil.
//                getBundle(this.getClass()).
//                getBundleContext();
//		
//		final IRecentPlaces recentPlaces = bundleContext.getService(bundleContext.getServiceReference(IRecentPlaces.class));
//		final EventAdmin admin = bundleContext.getService(bundleContext.getServiceReference(EventAdmin.class));
//		
//		search.addMenuListener(new IMenuListener() {
//			
//			@Override
//			public void menuAboutToShow(IMenuManager manager) {
//				search.removeAll();
//				Collection<String> lf = recentPlaces.getRecentPlaces();
//				if (lf.isEmpty()) {
//					search.add(new Action("No History"){
//						@Override
//						public void run() {
//							//do nothing
//						}
//					});
//				}
//				
//				for (String f : lf) {
//					search.add(new OpenFileDialogAction(f,admin));
//				}
//				
//				search.add(new Separator());
//				try {
//					String property = System.getProperty("user.home");
//					search.add(new OpenFileDialogAction(property,admin));
//				} catch (Exception e) {
//					//TODO log
//				}
//				
//			
//				try {
//					String property = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
//					search.add(new OpenFileDialogAction(property,admin));
//				} catch (Exception e) {
//					//TODO log
//				}
//				
//			}
//		});

		search.add(new Action("") {
        	
        	@Override
        	public void run() {

        	}
		});

        additions.addContributionItem(search, null);

	}
	
	private class OpenFileDialogAction extends Action {
		
		private String folder;
		private EventAdmin admin;
		
		public OpenFileDialogAction(String folder, EventAdmin admin) {
			super(folder +"...");
			this.folder = folder;
			this.admin = admin;
		}
		
		@Override
    	public void run() {
    		Shell shell = Display.getDefault().getActiveShell();
    		FileDialog dialog = new FileDialog(shell,SWT.MULTI);
    		dialog.setFilterPath(folder);

    		if (dialog.open() == null) return;

    		String[] fileNames = dialog.getFileNames();
    		for (int i = 0; i < fileNames.length; i++) fileNames[i] = dialog.getFilterPath() + File.separator + fileNames[i];

    		Map<String,String[]> props = new HashMap<>();
    		props.put("paths", fileNames);
//
    		admin.sendEvent(new Event("org/dawnsci/events/file/OPEN", props));
    		
		}
		
	}

}
