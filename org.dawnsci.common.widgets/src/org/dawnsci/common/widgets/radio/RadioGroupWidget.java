/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.radio;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a set of Radio buttons given a list of Actions<br>
 * @author wqk87977
 *
 */
public class RadioGroupWidget {

	private static final Logger logger = LoggerFactory.getLogger(RadioGroupWidget.class);
	private List<Widget> radiosList;
	private Widget parent;

	/**
	 * Create a set of Radio buttons given a list of Actions<br>
	 * The radio button text is coming from the text defined for each action, so is the ToolTipText.
	 * @param parent
	 *          can be a Composite or a Menu
	 */
	public RadioGroupWidget(Widget parent) {
		this.radiosList = new ArrayList<Widget>();
		this.parent = parent;

	}

	/**
	 * Creates a list of Actions as Menu items or Buttons given the parent Widget<br>
	 * The radio item text is coming from the text defined for each action
	 * @param actions
	 */
	public void setActions(List<Action> actions) {
		setActions(actions, false);
	}

	/**
	 * Creates a list of Actions as Menu items or Buttons given the parent Widget<br>
	 * The radio item text is coming from the text defined for each action
	 * @param actions
	 */
	public void setActions(List<Action> actions, boolean repectCurrentSelections) {
		if(actions == null) return;
		if (parent instanceof Composite) {
			int i = 0;
			Composite comp = (Composite) parent;
			for (final Action action : actions) {
				final Button radioButton = new Button(comp, SWT.RADIO);
				radioButton.setText(action.getText());
				if (action.getToolTipText() != null)
					radioButton.setToolTipText(action.getToolTipText());
				radioButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if(((Button)e.getSource()).getSelection())
							action.run();
					}
				});
				if((!repectCurrentSelections && i == 0) || action.isChecked())
					radioButton.setSelection(true);
				radiosList.add(radioButton);
				i++;
			}
		} else if (parent instanceof Menu) {
			int i = 0;
			Menu menu = (Menu) parent;
			for (final Action action : actions) {
				final MenuItem radioButton = new MenuItem(menu, SWT.RADIO);
				radioButton.setText(action.getText());
				radioButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if(((MenuItem)e.getSource()).getSelection())
							action.run();
					}
				});
				if((!repectCurrentSelections && i == 0) || action.isChecked())
					radioButton.setSelection(true);
				radiosList.add(radioButton);
				i++;
			}
		} else {
			logger.error("The parent widget provided:" + parent.getClass() + " is not supported");
		}
		
	}

	public List<? extends Widget> getRadiosList() {
		return radiosList;
	}

	public void setEnabled(boolean b) {
		for (Widget radio : radiosList) {
			if (radio instanceof MenuItem) {
				((MenuItem) radio).setEnabled(b);
			} else if (radio instanceof Button) {
				((Button) radio).setEnabled(b);
			}
		}
	}
}
