/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.fileviewer.table;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class FileTableContentProvider implements ILazyContentProvider {

	private TableViewer viewer;
	private FileTableContent[] input;

	public FileTableContentProvider(TableViewer viewer) {
		this.viewer = viewer;
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.input = (FileTableContent[]) newInput;
	}

	public void updateElement(int index) {
		viewer.replace(input[index], index);
	}
}