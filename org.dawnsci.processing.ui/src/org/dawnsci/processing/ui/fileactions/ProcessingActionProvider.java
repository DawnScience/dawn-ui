package org.dawnsci.processing.ui.fileactions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

public class ProcessingActionProvider extends CommonActionProvider {
	
	OpenAction openAction;
	
	public ProcessingActionProvider() {
		
	}
	
	public void init(ICommonActionExtensionSite aSite) {
		ICommonViewerSite viewSite = aSite.getViewSite();
		
		if (viewSite instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;
			openAction = new OpenAction(workbenchSite.getPage(),workbenchSite.getSelectionProvider());
		}
	}
	
	public void fillActionBars(IActionBars actionBars) {
		
		if (openAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
		}
		
	}
	
	public void fillContextMenu(IMenuManager menu) {
		if (openAction.isEnabled()) {
			//menu.appendToGroup(ICommonActionConstants.OPEN, openAction);
		}
	}
}
