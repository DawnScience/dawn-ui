/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.internal;

import java.io.InputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class IStorageUtils {

	/**
	 * Wrapper for {@link IStorage#getContents()} that attempts an 
	 * {@link IResource#refreshLocal(int, IProgressMonitor)} if the initial call
	 * to getContents throws an {@link IResourceStatus#OUT_OF_SYNC_LOCAL}
	 * @param file the IStorage to getContents on
	 * @param monitor optional monitor
	 * @return an input stream containing the contents of this storage
	 * @throws CoreException if the contents of this storage could not be accessed.
	 */
	public static InputStream getContents(IStorage file, IProgressMonitor monitor) throws CoreException {
		try {
			return file.getContents();
		} catch (CoreException ce) {
			if (ce.getStatus().getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
				// try to refresh the file, and then try again to get the contents
				Object adapterObj = file.getAdapter(IResource.class);
				if (adapterObj != null) {
					((IResource)adapterObj).refreshLocal(IResource.DEPTH_ZERO, monitor);
					return file.getContents();
				} 
			}
			throw ce;
		}
	}
	
	/**
	 * Wrapper for {@link IStorage#getContents()} that attempts an 
	 * {@link IResource#refreshLocal(int, IProgressMonitor)} if the initial call
	 * to getContents throws an {@link IResourceStatus#OUT_OF_SYNC_LOCAL}
	 * @param file the IStorage to getContents on
	 * @return an input stream containing the contents of this storage
	 * @throws CoreException if the contents of this storage could not be accessed.
	 */
	public static InputStream getContents(IStorage file) throws CoreException {
		return getContents(file, null);
	}
}
