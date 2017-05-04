package org.dawnsci.datavis;

import org.dawnsci.datavis.model.IPlotDataModifier;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

public class PlotModifierContributionFactory extends ExtensionContributionFactory {

	public PlotModifierContributionFactory() {
		this.toString();
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		
		MenuManager search = new MenuManager("Plot Modifiers",
                "org.dawnsci.cake.more");
		
		search.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				search.removeAll();
				
				Action a = new Action("No Modifiers"){
					@Override
					public void run() {
						ServiceManager.getPlotController().enablePlotModifier(null);
					}
				};
				
				if (ServiceManager.getPlotController().getEnabledPlotModifier() == null) {
					a.setChecked(true);
				}
				
				search.add(a);
				
				
				
				try {
					
				
				
				IPlotDataModifier[] pm = ServiceManager.getPlotController().getCurrentPlotModifiers();
				
				for (IPlotDataModifier f : pm) {
					
					Action ac = new Action(f.getName()) {
			        	
			        	@Override
			        	public void run() {
			        		ServiceManager.getPlotController().enablePlotModifier(f);
			        		
			        	}
					};
					
					if (ServiceManager.getPlotController().getEnabledPlotModifier() == f) {
						ac.setChecked(true);
					}
					
					search.add(ac);
				}
			} catch (Exception e){};
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
