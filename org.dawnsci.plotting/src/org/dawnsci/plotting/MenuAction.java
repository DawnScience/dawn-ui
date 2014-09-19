/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawnsci.plotting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Simple action which will have other actions in a drop down menu.
 * This is a copy of various other versions to increase modularity.
 */
class MenuAction extends Action implements IMenuCreator {
	
	private Menu fMenu;
	private List<IAction> actions;
	private Action selectedAction;
	private List<Integer> indexes;
	private int iSeparator;

	public MenuAction(final String text) {
		super(text, IAction.AS_DROP_DOWN_MENU);
		setMenuCreator(this);
		this.actions = new ArrayList<IAction>(7);
		this.iSeparator = 0;
		this.indexes = new ArrayList<Integer>();
	}


	@Override
	public void dispose() {
		if (fMenu != null)  {
			fMenu.dispose();
			fMenu= null;
		}
	}


	@Override
	public Menu getMenu(Menu parent) {
		if (fMenu != null) fMenu.dispose();

		fMenu= new Menu(parent);

		for (IAction action : actions) {
			addActionToMenu(fMenu, action);
		}
		for (int i=0; i<iSeparator;i++)
			addSeparatorToMenu(fMenu, i);
		return fMenu;
	}

	public void add(final IAction action) {
		actions.add(action);
	}

	public void add(int pos, MenuAction action) {
		actions.add(pos, action);
	}

	public void remove(IAction action) {
		actions.remove(action);
	}

	@Override
	public Menu getMenu(Control parent) {
		
		if (parent==null) return null;
		if (fMenu != null) fMenu.dispose();

		fMenu= new Menu(parent);

		for (IAction action : actions) {
			addActionToMenu(fMenu, action);
		}
		for (int i=0; i<iSeparator;i++)
			addSeparatorToMenu(fMenu, i);
		return fMenu;
	}

	protected void addActionToMenu(Menu parent, IAction action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}


	/**
	 * Get's rid of the menu, because the menu hangs on to * the searches, etc.
	 */
	public void clear() {
		actions.clear();
		indexes.clear();
		this.iSeparator = 0;
	}

	public void setSelectedAction(int iAction) {
		setSelectedAction(actions.get(iAction));
	}
	public void setCheckedAction(int iAction, boolean isChecked) {
		actions.get(iAction).setChecked(isChecked);
	}
	public IAction getSelectedAction() {
		for (IAction action : actions) {
			if (action.isChecked()) return action;
		}
		return null;
	}
	public IAction getAction(int iAction) {
		return actions.get(iAction);
	}
	public int size() {
		if (actions==null) return 0;
		return actions.size();
	}
	
	public void addActionsTo(final MenuAction man) {
		for (IAction action : actions) {
			man.add(action);
		}
	}

	public void setSelectedAction(IAction action) {
		if (action.getImageDescriptor()!=null) this.setImageDescriptor(action.getImageDescriptor());
		setText(action.getText());
		//setToolTipText(action.getToolTipText());
		this.selectedAction = (Action)action;
	}

	public void addSeparator() {
		indexes.add(actions.size()+iSeparator);
		iSeparator++;
	}

	protected void addSeparatorToMenu(Menu parent, int index) {
		new MenuItem(parent, SWT.SEPARATOR, indexes.get(index));
	}

	public void runWithEvent(Event e) {
		if (selectedAction==null) {
			
			final Control parent = e.widget !=null && e.widget instanceof ToolItem
					             ? ((ToolItem)e.widget).getParent()
					             : null;
			Menu m = getMenu(parent);
			if (m != null) {
				// position the menu below the drop down item
				Point point = parent.toDisplay(new Point(e.x, e.y));
				m.setLocation(point.x, point.y); // waiting
											     // for SWT
				// 0.42
				m.setVisible(true);// for SWT
			}
		
		} else {
			run();
		}
	}
	public void run() {
		if (selectedAction!=null) {
			selectedAction.run();
		} 
	}
	
	public String toString() {
		if (getText()!=null) return getText();
		if (getToolTipText()!=null) return getToolTipText();
		return super.toString();
	}


	public IAction findAction(String id) {
		for (IAction action : actions) {
			if (id.equals(action.getId())) return action;
		}
		return null;
	}


	public boolean isEmpty() {
		return actions==null || actions.isEmpty();
	}



}