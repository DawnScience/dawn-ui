/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.fileviewer.table;

import java.io.File;

import org.dawnsci.fileviewer.Utils.SortType;
import org.dawnsci.fileviewer.table.FileTableUtils.FilterType;
import org.dawnsci.fileviewer.Utils.SortDirection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class RetrieveFileListJob extends Job {
	private FileTableContent[] dirList;
	private int dirListCount;
	private final File workerStateDir;
	private final SortType sortType;
	private final SortDirection direction;
	private final String filter;
	private final FilterType filterType;
	private final boolean quick;
	
	public RetrieveFileListJob(File workerStateDir, SortType sortType, SortDirection direction, String filter, FilterType filterType, boolean quick) {
		super("Retrieving file list..");
		this.workerStateDir = workerStateDir;
		this.sortType = sortType;
		this.direction = direction;
		this.filter = filter;
		this.filterType = filterType;
		this.quick = quick;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (quick) {
			dirListCount = FileTableUtils.getDirectoryListCount(workerStateDir, filter, filterType);
		} else {
			dirList = FileTableUtils.getDirectoryList(workerStateDir, sortType, direction, filter, filterType);
			dirListCount = dirList.length;
		}
		if (dirList == null)
			return Status.CANCEL_STATUS;
		return Status.OK_STATUS;
	}

	public FileTableContent[] getDirList() {
		return dirList;
	}
	
	public int getDirListCount() {
		return dirListCount;
	}
	
}