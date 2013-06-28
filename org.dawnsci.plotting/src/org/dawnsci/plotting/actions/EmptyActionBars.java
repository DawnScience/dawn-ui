package org.dawnsci.plotting.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.services.IServiceLocator;

public class EmptyActionBars implements IActionBars2 {

	private IToolBarManager toolBarManager;
	private IMenuManager menuManager;
	private IStatusLineManager statusLineManager;


	public EmptyActionBars() {
		this(new ToolBarManager(), new MenuManager(), new StatusLineManager());
	}

	public EmptyActionBars(IToolBarManager toolBarManager,
			IMenuManager menuManager, IStatusLineManager statusLineManager) {
		this.toolBarManager = toolBarManager;
		this.menuManager    = menuManager;
		this.statusLineManager = statusLineManager;
	}

	@Override
	public void clearGlobalActionHandlers() {
		// TODO Auto-generated method stub

	}

	@Override
	public IAction getGlobalActionHandler(String actionId) {
		return null;
	}

	@Override
	public IMenuManager getMenuManager() {
		return menuManager;
	}

	@Override
	public IServiceLocator getServiceLocator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IStatusLineManager getStatusLineManager() {
		return statusLineManager;
	}

	@Override
	public IToolBarManager getToolBarManager() {
		return toolBarManager;
	}

	@Override
	public void setGlobalActionHandler(String actionId, IAction handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateActionBars() {
		// TODO Auto-generated method stub

	}

	@Override
	public ICoolBarManager getCoolBarManager() {
		return null;
	}

}
