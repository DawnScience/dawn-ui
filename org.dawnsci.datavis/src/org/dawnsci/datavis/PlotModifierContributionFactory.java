package org.dawnsci.datavis;

import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.IPlotDataModifier;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class PlotModifierContributionFactory extends ExtensionContributionFactory {

	public PlotModifierContributionFactory() {
		this.toString();
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		
		MenuManager search = new MenuManager("Plot Modifiers",
                "org.dawnsci.cake.more");
		
//		BundleContext bundleContext =
//                FrameworkUtil.
//                getBundle(this.getClass()).
//                getBundleContext();
//		
//		final IPlotController plotController = bundleContext.getService(bundleContext.getServiceReference(IPlotController.class));
//		
//		search.addMenuListener(new IMenuListener() {
//			
//			@Override
//			public void menuAboutToShow(IMenuManager manager) {
//				search.removeAll();
//				
//				Action a = new Action("No Modifiers"){
//					@Override
//					public void run() {
//						plotController.enablePlotModifier(null);
//					}
//				};
//				
//				if (plotController.getEnabledPlotModifier() == null) {
//					a.setChecked(true);
//				}
//				
//				search.add(a);
//				
//				
//				
//				try {
//					
//				
//				
//				IPlotDataModifier[] pm = plotController.getCurrentPlotModifiers();
//				
//				for (IPlotDataModifier f : pm) {
//					
//					Action ac = new Action(f.getName()) {
//			        	
//			        	@Override
//			        	public void run() {
//			        		plotController.enablePlotModifier(f);
//			        		
//			        	}
//					};
//					
//					if (plotController.getEnabledPlotModifier() == f) {
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
