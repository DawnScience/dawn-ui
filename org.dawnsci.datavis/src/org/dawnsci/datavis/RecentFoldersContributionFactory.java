package org.dawnsci.datavis;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dawnsci.plotting.api.PlottingEventConstants;
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

		MenuManager directoryMenu = new MenuManager("Recent Folders",
				"org.dawnsci.recent.folders");

		BundleContext bundleContext =
				FrameworkUtil.
				getBundle(this.getClass()).
				getBundleContext();

		final IRecentPlaces recentPlaces = bundleContext.getService(bundleContext.getServiceReference(IRecentPlaces.class));
		final EventAdmin admin = bundleContext.getService(bundleContext.getServiceReference(EventAdmin.class));

		//need since a completely empty menu won't show
		//we are populating it in a menu listener which clears the empty action
		final Action defaultAction = new Action("") {

			@Override
			public void run() {

			}
		};

		directoryMenu.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				directoryMenu.removeAll();
				Collection<String> lf = recentPlaces.getRecentDirectories();
				if (lf.isEmpty()) {
					directoryMenu.add(new Action("No History"){
						@Override
						public void run() {
							//do nothing
						}
					});
				}

				for (String f : lf) {
					directoryMenu.add(new OpenFileDialogAction(f,admin));
				}

				directoryMenu.add(new Separator());
				try {
					String property = System.getProperty("user.home");
					directoryMenu.add(new OpenFileDialogAction(property,admin));
				} catch (Exception e) {
					//TODO log
				}


				try {
					String property = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
					directoryMenu.add(new OpenFileDialogAction(property,admin));
				} catch (Exception e) {
					//TODO log
				}

			}
		});

		directoryMenu.add(defaultAction);

		additions.addContributionItem(directoryMenu, null);

		MenuManager fileMenu = new MenuManager("Recent Files",
				"org.dawnsci.recent.files");

		fileMenu.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fileMenu.removeAll();
				Collection<String> lf = recentPlaces.getRecentFiles();
				if (lf.isEmpty()) {
					fileMenu.add(new Action("No History"){
						@Override
						public void run() {
							//do nothing
						}
					});
				}

				for (String f : lf) {
					fileMenu.add(new OpenFileAction(f,admin));
				}

			}
		});

		fileMenu.add(defaultAction);

		additions.addContributionItem(fileMenu, null);

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
			props.put(PlottingEventConstants.MULTIPLE_FILE_PROPERTY, fileNames);
			admin.sendEvent(new Event(PlottingEventConstants.FILE_OPEN_EVENT, props));

		}

	}

	private class OpenFileAction extends Action {

		private String file;
		private EventAdmin admin;

		public OpenFileAction(String file, EventAdmin admin) {
			super(file);
			this.file = file;
			this.admin = admin;
		}

		@Override
		public void run() {

			Map<String,String[]> props = new HashMap<>();
			props.put("paths", new String[] {file});
			admin.sendEvent(new Event(PlottingEventConstants.FILE_OPEN_EVENT, props));

		}

	}

}
