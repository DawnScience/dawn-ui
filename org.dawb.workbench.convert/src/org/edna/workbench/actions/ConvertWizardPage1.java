/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.edna.workbench.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.monitor.ProgressMonitorWrapper;

/**
 *   ConvertWizardPage1
 *
 *   @author gerring
 *   @date Aug 31, 2010
 *   @project org.edna.workbench.actions
 **/
public class ConvertWizardPage1 extends WizardPage {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ConvertWizardPage1.class);
	
	private CheckboxTableViewer checkboxTableViewer;
	private String[]            dataSetNames;

	private ISelection selection;

	/**
	 * Create the wizard.
	 */
	public ConvertWizardPage1() {
		super("wizardPage");
		setTitle("Convert Data");
		setDescription("Convert data from synchrotron formats and compressed files to common simple data formats.");
		 dataSetNames = new String[]{"Loading..."};
    }

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(1, false));
		
		Composite top = new Composite(container, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label convertLabel = new Label(top, SWT.NONE);
		convertLabel.setBounds(0, 0, 68, 17);
		convertLabel.setText("Convert to");
		
		Combo combo = new Combo(top, SWT.READ_ONLY);
		combo.setItems(new String[] {"Comma Separated Values (*.csv)"});
		combo.setToolTipText("Convert to file type by file extension");
		combo.setBounds(0, 0, 189, 29);
		combo.select(0);
		
		Composite main = new Composite(container, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		this.checkboxTableViewer = CheckboxTableViewer.newCheckList(main, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = checkboxTableViewer.getTable();
		table.setToolTipText("Select data to export to the csv.");
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		this.selection = EclipseUtils.getActivePage().getSelection();

		checkboxTableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {
				return dataSetNames;
			}
		});
		checkboxTableViewer.setInput(new Object());
		checkboxTableViewer.setAllGrayed(true);
		
		// We populate the names later using a wizard task.
		parent.getDisplay().asyncExec(new Runnable() {
			public void run() {
		        try {
					getDataSetNames();
				} catch (Exception e) {
					logger.error("Cannot extract data sets!", e);
				}
			}
		});
	}
	

	protected void getDataSetNames() throws Exception {
		
		getContainer().run(true, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				if (selection instanceof StructuredSelection) {
					StructuredSelection s = (StructuredSelection)selection;
					final Object        o = s.getFirstElement();
					if (o instanceof IFile) {
						try {
							
							// Attempt to use meta data, save memory
							final IFile        file = (IFile)o;
							final IMetaData    meta = LoaderFactory.getMetaData(file.getLocation().toOSString(), new ProgressMonitorWrapper(monitor));
							if (meta != null) {
								final Collection<String> names = meta.getDataNames();
								if (names !=null) {
									setDataNames(names.toArray(new String[names.size()]));
									return;
								}
					        }
							
							// Clobber the memory!
							final DataHolder  holder = LoaderFactory.getData(file.getLocation().toOSString(), new ProgressMonitorWrapper(monitor));
							final List<String> names = new ArrayList<String>(holder.getMap().keySet());
							Collections.sort(names);
							setDataNames(names.toArray(new String[names.size()]));
							return;
							
						} catch (Exception ne) {
							throw new InvocationTargetException(ne);
						}
					}
				}

			}
		});
	}

	protected void setDataNames(String[] array) {
		dataSetNames = array;
		getContainer().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				checkboxTableViewer.getTable().setEnabled(true);
				checkboxTableViewer.refresh();
				checkboxTableViewer.setAllChecked(true);
				checkboxTableViewer.setAllGrayed(false);
			}
		});
	}
	
	protected Object[] getSelected() {
		return checkboxTableViewer.getCheckedElements();
	}

	public IFile getFile() {
		if (selection instanceof StructuredSelection) {
			StructuredSelection s = (StructuredSelection)selection;
			final Object        o = s.getFirstElement();
			if (o instanceof IFile) return (IFile)o;
		}
		return null;
	}
}
