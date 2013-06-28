/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawnsci.plotting.actions;

import org.dawnsci.plotting.Activator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.SubActionBars2;

public class ActionBarWrapper extends SubActionBars2 {


	private IToolBarManager    alternativeToolbarManager;
	private IMenuManager       alternativeMenuManager;
	private IStatusLineManager alternativeStatusManager;
	private IToolBarManager    rightManager;
	private Composite          toolbarControl;

	/**
	 * alternatives may be null.
	 * @param alternativeToolbarManager
	 * @param alternativeMenuManager
	 * @param alternativeStatusManager
	 * @param parent
	 */
	public ActionBarWrapper(final IToolBarManager    alternativeToolbarManager,
			                final IMenuManager       alternativeMenuManager,
			                final IStatusLineManager alternativeStatusManager,
			                IActionBars2 parent) {
		super(parent);
		this.alternativeToolbarManager = alternativeToolbarManager;
		this.alternativeMenuManager    = alternativeMenuManager;
		this.alternativeStatusManager  = alternativeStatusManager;
	}

	@Override
	public IMenuManager getMenuManager() {
		if (alternativeMenuManager!=null) return alternativeMenuManager;
		return super.getMenuManager();
	}

	/**
	 * Returns the status line manager. If items are added or removed from the
	 * manager be sure to call <code>updateActionBars</code>.
	 * 
	 * @return the status line manager
	 */
	@Override
	public IStatusLineManager getStatusLineManager() {
		if (alternativeStatusManager!=null) return alternativeStatusManager;
		return super.getStatusLineManager();
	}

	/**
	 * Returns the tool bar manager. If items are added or removed from the
	 * manager be sure to call <code>updateActionBars</code>.
	 * 
	 * @return the tool bar manager
	 */
	@Override
	public IToolBarManager getToolBarManager() {
		if (alternativeToolbarManager!=null) return alternativeToolbarManager;
		return super.getToolBarManager();
	}

	public void setToolbarControl(Composite tools) {
		this.toolbarControl = tools;
	}

	public void updateActionBars() {
		super.updateActionBars();
		if (toolbarControl!=null) {
			toolbarControl.layout(toolbarControl.getChildren());
			toolbarControl.getParent().layout(toolbarControl.getChildren());
		}
	}
	
	private static void removeMargins(Composite area) {
		final GridLayout layout = (GridLayout)area.getLayout();
		if (layout==null) return;
		layout.horizontalSpacing=0;
		layout.verticalSpacing  =0;
		layout.marginBottom     =0;
		layout.marginTop        =0;
		layout.marginLeft       =0;
		layout.marginRight      =0;
		layout.marginHeight     =0;
		layout.marginWidth      =0;

	}
	/**
	 * Convenience method for adding action bars at the top of a composite.
	 * 
	 * @param main
	 * @param originalBars, may be null.
	 * @return
	 */
	public static ActionBarWrapper createActionBars(Composite main, IActionBars originalBars) {
		
		Composite toolbarControl = new Composite(main, SWT.RIGHT);
		toolbarControl.setLayout(new GridLayout(2, false));
		removeMargins(toolbarControl);
		toolbarControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// We use a local toolbar to make it clear to the user the tools
		// that they can use, also because the toolbar actions are 
		// hard coded.
		ToolBarManager toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT|SWT.WRAP);
		final ToolBar  toolBar = toolMan.createControl(toolbarControl);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		ToolBarManager rightMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT|SWT.WRAP);
		final ToolBar          rightBar = rightMan.createControl(toolbarControl);
		rightBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		final MenuManager    menuMan = new MenuManager();
	    Action menuAction = new Action("", Activator.getImageDescriptor("/icons/DropDown.png")) {
	    	@Override
	    	public void run() {
	    		final Menu   mbar = menuMan.createContextMenu(toolBar);
	    		mbar.setVisible(true);
	    	}
	    };
	    rightMan.add(menuAction);
	    rightMan.update(false);
		ActionBarWrapper wrapper = new ActionBarWrapper(toolMan,menuMan,null,originalBars!=null?(IActionBars2)originalBars: new EmptyActionBars());
		wrapper.rightManager     = rightMan;                
		wrapper.toolbarControl   = toolbarControl;

		return wrapper;
	}
	
	public ToolBar getToolBar() {
		return ((ToolBarManager)getToolBarManager()).getControl();
	}

	public IToolBarManager getRightManager() {
		return rightManager;
	}

	public void setRightManager(IToolBarManager rightManager) {
		this.rightManager = rightManager;
	}
	
	public void setVisible(boolean isVisible) {
		setVisible(toolbarControl, isVisible);
		toolbarControl.getParent().layout(new Control[]{toolbarControl});
	}
	private static void setVisible(final Control widget, final boolean isVisible) {
		
		if (widget == null) return;
		if (widget.getLayoutData() instanceof GridData) {
			final GridData data = (GridData) widget.getLayoutData();
			data.exclude = !isVisible;
		}
		widget.setVisible(isVisible);
	}

	/**
	 * Updates the menu and toolbar only.
	 * @param force
	 */
	public void update(boolean force) {
		if (getToolBarManager()!=null) getToolBarManager().update(force);
		if (getMenuManager()!=null)    getMenuManager().update(force);
		if (rightManager!=null)        rightManager.update(force);
	}

	public void clear() {
		if (getToolBarManager()!=null) getToolBarManager().removeAll();
		if (getMenuManager()!=null)    getMenuManager().removeAll();
		update(true);
	}
}
