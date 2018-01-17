package org.dawnsci.datavis;

import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.ITraceColorProviderService;
import org.dawnsci.datavis.model.ITraceColourProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class LineTraceColorContributionFactory extends ExtensionContributionFactory {

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		MenuManager search = new MenuManager("Line Plot Colors",
                "org.dawnsci.datavis.linecolormenu");
		
//		BundleContext bundleContext =
//                FrameworkUtil.
//                getBundle(this.getClass()).
//                getBundleContext();
//		
//		final IPlotController plotController = bundleContext.getService(bundleContext.getServiceReference(IPlotController.class));
//		final ITraceColorProviderService traceColourProvider = bundleContext.getService(bundleContext.getServiceReference(ITraceColorProviderService.class));
//		
//		
//		search.addMenuListener(new IMenuListener() {
//			
//			@Override
//			public void menuAboutToShow(IMenuManager manager) {
//				
//				search.removeAll();
//				
//				ITraceColourProvider current = plotController.getColorProvider();
//				
//				Action a = new Action("Default Colours"){
//					@Override
//					public void run() {
//						plotController.setColorProvider(null);
//					}
//				};
//				
//				if (current == null) {
//					a.setChecked(true);
//				}
//				
//				search.add(a);
//				
//				try {
//					
//				
//				
//				ITraceColourProvider[] pm = traceColourProvider.getColorProviders();
//				
//				for (ITraceColourProvider f : pm) {
//					
//					Action ac = new Action(f.getName()) {
//			        	
//			        	@Override
//			        	public void run() {
//			        		plotController.setColorProvider(f);
//			        		
//			        	}
//					};
//					
//					if (current == f) {
//						ac.setChecked(true);
//					}
//					
//					search.add(ac);
//				}
//			} catch (Exception e){};
//			}
//		});

		search.add(new Action("") {

			@Override
			public void run() {

			}
		});

        additions.addContributionItem(search, null);

	}

}
