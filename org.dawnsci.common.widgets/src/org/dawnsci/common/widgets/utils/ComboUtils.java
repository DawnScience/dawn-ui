/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.utils;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Utility class for SWT Combo buttons
 * @author wqk87977
 *
 */
public class ComboUtils {

	/**
	 * Create a set of action items in a Combo box given a list of actions
	 * @param parent
	 * @param actions
	 * @throws Exception
	 * @return comboButton
	 */
	public static Combo createComboControls(Composite parent, final List<Entry<String, Action>> actions) throws Exception{
		if(actions == null) return null;
		final Combo comboButton = new Combo(parent, SWT.BORDER);
		for (final Entry<String, Action> action : actions) {
			comboButton.add(action.getKey());
		}
		
		comboButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				int index = ((Combo)e.getSource()).getSelectionIndex();
				actions.get(index).getValue().run();
			}
		});
		comboButton.select(0);
		return comboButton;
	}
}
