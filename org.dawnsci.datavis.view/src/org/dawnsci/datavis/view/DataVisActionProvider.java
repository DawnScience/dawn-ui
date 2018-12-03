package org.dawnsci.datavis.view;


import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

public class DataVisActionProvider extends CommonActionProvider {

	private OpenDataAction openAction;
	
	@Override
	public void init(ICommonActionExtensionSite aSite) {
		ICommonViewerSite viewSite = aSite.getViewSite();
		
		if (viewSite instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;
			openAction = new OpenDataAction(workbenchSite.getPage(),workbenchSite.getSelectionProvider());
		}
	}
	
	@Override
	public void fillActionBars(IActionBars actionBars) {
		
		if (openAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
		}
		
	}
}
