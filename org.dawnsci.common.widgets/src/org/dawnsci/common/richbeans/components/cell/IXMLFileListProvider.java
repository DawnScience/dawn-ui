/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.cell;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

public interface IXMLFileListProvider {
	/**
	 * Get a list of all file in the directory for this scan type.
	 * @param dir to search
	 * @return list of files, will be non-<code>null</code>
	 */
	public List<IFile> getSortedFileList(IFolder dir);
}
