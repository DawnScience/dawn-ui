package org.dawnsci.datavis;

import org.dawnsci.datavis.model.IFileController;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class DatasetContributionFactory extends ExtensionContributionFactory {

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		MenuManager search = new MenuManager("Dataset Options",
                "org.dawnsci.datavis.dataset");
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		final IFileController fileController = bundleContext.getService(bundleContext.getServiceReference(IFileController.class));
		

		search.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {

				search.removeAll();


				Action a = new Action("Filter Datasets"){
					@Override
					public void run() {
						boolean onlySignals = fileController.isOnlySignals();
						fileController.setOnlySignals(!onlySignals);
					}
				};

				a.setChecked(fileController.isOnlySignals());

				search.add(a);
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
