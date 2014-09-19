/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.data.wizard;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.wizard.ResourceChoosePage;
import org.dawb.common.util.io.FileUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonFilterPage extends ResourceChoosePage {
	
	private static final Logger logger = LoggerFactory.getLogger(PythonFilterPage.class);
	
	private Composite      exampleComposite;
    private static boolean lastNewFile = true;
    private static String staticPath = null;
	private String pythonContents;

	protected PythonFilterPage(final String    name) {
		super("Choose or create python filter", "Choose a python file to act as a filter.", null);
		setNewFile(lastNewFile);
		setPathEditable(true);
		setFileLabel("Python Filter Script");
		setOverwriteVisible(true);
	}
	
	protected void createContentBeforeFileChoose(Composite container) {
		
		final Group choice = new Group(container, SWT.NONE);
		choice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		choice.setLayout(new GridLayout(2, false));
		choice.setText("Python File");
		
		final Button createNew = new Button(choice, SWT.RADIO);
		createNew.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		createNew.setText("New");
		createNew.setSelection(isNewFile());
		createNew.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setNewFile(true);
				setOverwriteVisible(true);
				GridUtils.setVisible(exampleComposite, true);
				exampleComposite.getParent().layout();
				lastNewFile = true;
			}
		});
	
		final Button chooseExisting = new Button(choice, SWT.RADIO);
		chooseExisting.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		chooseExisting.setText("Existing");
		chooseExisting.setSelection(!isNewFile());
		chooseExisting.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setNewFile(false);
				setOverwriteVisible(false);
				GridUtils.setVisible(exampleComposite, false);
				exampleComposite.getParent().layout();
				lastNewFile = false;
			}
		});

		final Label sep = new Label(container, SWT.NONE);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
	}
	
	protected void createContentAfterFileChoose(Composite container) {

		final Label sep = new Label(container, SWT.SEPARATOR|SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		this.exampleComposite = new Composite(container, SWT.NONE);
		exampleComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		exampleComposite.setLayout(new GridLayout(1, false));

		final Button edit = new Button(exampleComposite, SWT.CHECK);
		edit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		edit.setText("Edit file contents before creating");
		edit.setSelection(false);

		final Text scriptExample = new Text(exampleComposite, SWT.MULTI|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
		scriptExample.setEditable(false);
		scriptExample.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		try {
			this.pythonContents = FileUtils.readFile(getClass().getResourceAsStream("filter.py")).toString();
			scriptExample.setText(pythonContents);
			scriptExample.addModifyListener(new ModifyListener() {		
				@Override
				public void modifyText(ModifyEvent e) {
					pythonContents = scriptExample.getText();
				}
			});
		} catch (Exception e) {
			logger.error("Cannot read filter.py", e);
		}
		
		edit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				scriptExample.setEditable(edit.getSelection());
			}
		});

		GridUtils.setVisible(exampleComposite, isNewFile());
		setPageComplete(false);
		setOverwriteVisible(isNewFile());
		if (staticPath!=null) setPath(staticPath);
	}
	
	protected void pathChanged() {
		
		try {
			if (getPath()==null || "".equals(getPath())) {
				setErrorMessage("Please choose a file.");
				setPageComplete(false);
				return;
			}
			
			final IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(getPath());
			if (isNewFile()) {
				
				if (file==null && !getPath().toLowerCase().endsWith(".py")) {
					setErrorMessage("The file '"+getPath()+"' does not end with \".py\"");
					setPageComplete(false);
					return;
				}
				if (file!=null && file.exists() && overwrite!=null && !overwrite.getSelection()) {
					setErrorMessage("The file '"+file.getName()+"' already exists please tick overwrite.");
					setPageComplete(false);
					return;
				}
				if (file!=null && file.exists() && !file.getName().toLowerCase().endsWith(".py")) {
					setErrorMessage("The file '"+file.getName()+"' does not end with \".py\"");
					setPageComplete(false);
					return;
				}
				
			} else {
				if (!file.exists()) {
					setErrorMessage("The file '"+file.getName()+"' does not exist.");
					setPageComplete(false);
					return;
				}
				if (!file.getName().toLowerCase().endsWith(".py")) {
					setErrorMessage("The file '"+file.getName()+"' does not end with \".py\"");
					setPageComplete(false);
					return;
				}
			}
		} catch (Exception ne) {
			logger.error("Cannot validate python wizard!", ne);
			setErrorMessage(ne.getMessage());
			setPageComplete(false);
			return;
		}
		
		this.staticPath = getPath();
		setErrorMessage(null);
		setPageComplete(true);
		
	}


	public String getPythonFile() {
		return getPath();
	}
	
	public String getPythonContents() {
		return pythonContents;
	}

	public boolean isNewFile() {
		return super.isNewFile();
	}
}
