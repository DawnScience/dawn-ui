package org.dawnsci.datavis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultColormapContributionFactory extends ExtensionContributionFactory {
	
	Logger logger = LoggerFactory.getLogger(DefaultColormapContributionFactory.class);

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		
		MenuManager defaultColors = new MenuManager("Default Colormap",
                "org.dawnsci.datavis.defaultcolormapmenu");
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		final IPaletteService paletteService = bundleContext.getService(bundleContext.getServiceReference(IPaletteService.class));
		
		String schemeName = paletteService.getDefaultColorScheme();
		List<Action> actions =  new ArrayList<>();
		Collection<String> categoryNames = paletteService.getColorCategories();
		for (String c : categoryNames) {
			if (!c.equals("All")) {
				MenuManager subMenu = new MenuManager(c);
				Collection<String> colours = paletteService.getColorsByCategory(c);
				for (final String colour : colours) {
					final Action action = new Action(colour, IAction.AS_CHECK_BOX) {
						public void run() {
							try {
								String t = getText();
								paletteService.setDefaultColorScheme(t);
								actions.stream().forEach(a -> a.setChecked(a.getText().equals(t)));
							} catch (Exception ne) {
								logger.error("Cannot create palette data!", ne);
							}
						}
					};
					action.setId(colour);
					subMenu.add(action);
					action.setChecked(colour.equals(schemeName));
					actions.add(action);
				}
				defaultColors.add(subMenu);
			}
		}
		
		additions.addContributionItem(defaultColors, null);

	}
}
