/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.tools.preference.detector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class MissingDetectorModulesSelectorDialog extends TitleAreaDialog {
	private int numberOfHorizontalModules;
	private int numberOfVerticalModules;
	private List<Integer> missingModules;
	
	public MissingDetectorModulesSelectorDialog(Shell shell, int numberOfHorizontalModules, int numberOfVerticalModules, List<Integer> missingModules) {
		super(shell);
		this.numberOfHorizontalModules = numberOfHorizontalModules;
		this.numberOfVerticalModules = numberOfVerticalModules;
		this.missingModules = (missingModules == null) ? new ArrayList<>() : new ArrayList<>(missingModules);
	}

	@Override
    public void create() {
        super.create();
        setTitle("Missing detector modules");
        setMessage("Select the missing modules", IMessageProvider.INFORMATION);
    }
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(numberOfHorizontalModules, false);
        container.setLayout(layout);

        for(int i = 0; i < numberOfHorizontalModules*numberOfVerticalModules; i++){
        	Integer index = i;
        	Button button = new Button(container, SWT.CHECK);
        	button.addSelectionListener(new SelectionAdapter() {
        		@Override
        		public void widgetSelected(SelectionEvent e) {
        			if(((Button)e.getSource()).getSelection())
        				missingModules.add(index);
        			else 
        				missingModules.remove(index);
        		}
			});
        	if(missingModules.contains(index)) button.setSelection(true);
        }

        return area;
	}
	
	
	public List<Integer> getMissingModules(){
		return missingModules;
	}
}