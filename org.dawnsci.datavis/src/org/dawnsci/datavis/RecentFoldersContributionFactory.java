package org.dawnsci.datavis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.datavis.model.FileController;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
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
		
		search.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				search.removeAll();
//				Collection<String> lf = FileController.getInstance().getLastFolders();
				Collection<String> lf = new ArrayList<>();
				if (lf.isEmpty()) {
					search.add(new Action("No History"){
						@Override
						public void run() {
							//do nothing
						}
					});
				}
				
				for (String f : lf) {
					
					search.add(new Action(f + "...") {
			        	
			        	@Override
			        	public void run() {
			        		Shell shell = Display.getDefault().getActiveShell();
			        		FileDialog dialog = new FileDialog(shell,SWT.MULTI);
			        		dialog.setFilterPath(f);

			        		if (dialog.open() == null) return;

			        		String[] fileNames = dialog.getFileNames();
			        		for (int i = 0; i < fileNames.length; i++) fileNames[i] = dialog.getFilterPath() + File.separator + fileNames[i];

			        		Map<String,String[]> props = new HashMap<>();
			        		props.put("paths", fileNames);
			//
			        		EventAdmin eventAdmin = ServiceManager.getEventAdmin();
			        		eventAdmin.sendEvent(new Event("org/dawnsci/events/file/OPEN", props));
			        		
			        	}
					});
				}
			}
		});

		search.add(new Action("") {
        	
        	@Override
        	public void run() {

        	}
		});

        additions.addContributionItem(search, null);

	}

}
