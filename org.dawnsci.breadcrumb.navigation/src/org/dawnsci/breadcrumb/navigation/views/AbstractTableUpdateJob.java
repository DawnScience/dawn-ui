/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.breadcrumb.navigation.views;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Job to populate table, required for AbstractTableDelegates.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractTableUpdateJob extends Job {

	protected List<Object>  selections;
	protected Object        bean;
	
	protected final AbstractTableDelegate table;

	public AbstractTableUpdateJob(AbstractTableDelegate view) {
		super("Update "+view.getTableId());
		setPriority(Job.INTERACTIVE);
		setUser(false);
		this.table = view;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		final List<Object> collections = getContent(selections, monitor);
		
		if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		
		table.setContent(collections);
		
		return Status.OK_STATUS;
	}

	/**
	 * Called from thread to get content from current selections.
	 * @param selections
	 * @param monitor
	 * @return
	 */
	protected abstract List<Object> getContent(List<Object> selections, IProgressMonitor monitor) ;

	
	public void schedule(List<Object> selections, Object connection) {
		cancel();
		this.selections = selections;
		this.bean       = connection;
		schedule(5);	
	}

}
