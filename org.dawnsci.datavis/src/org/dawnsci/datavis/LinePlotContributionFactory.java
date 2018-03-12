package org.dawnsci.datavis;

import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.PlotController;
import org.dawnsci.datavis.model.PlotModeXY;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class LinePlotContributionFactory extends ExtensionContributionFactory {

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		
		MenuManager menu = new MenuManager("Line Trace Options",
                "org.dawnsci.datavis.lineoptions");
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		final IPlotController plotController = bundleContext.getService(bundleContext.getServiceReference(IPlotController.class));
		
		menu.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				menu.removeAll();

				PlotModeXY xy = null;
				if (plotController instanceof PlotController) {
					PlotController pc = (PlotController)plotController;
					IPlotMode[] plotModes = pc.getPlotModes(1);
					
					
					for (IPlotMode m : plotModes) {
						if (m instanceof PlotModeXY) {
							xy = (PlotModeXY)m;
							break;
						}
					}
					
				}
				
				if (xy != null) {
					
					final PlotModeXY finalXY = xy;
					
					Action a = new Action("Show Error Bars"){
						@Override
						public void run() {
							finalXY.setErrorBarEnabled(!finalXY.isErrorBarEnabled());
							plotController.forceReplot();
						}
					};

					a.setChecked(xy.isErrorBarEnabled());

					menu.add(a);
				}
				
				
			}
		});

		menu.add(new Action("") {

			@Override
			public void run() {

			}
		});

        additions.addContributionItem(menu, null);

	}


}
