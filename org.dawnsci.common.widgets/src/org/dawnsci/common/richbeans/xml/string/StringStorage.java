/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.xml.string;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;

public class StringStorage implements IStorage {

	private String string;
	private String name;

	public StringStorage(String input) {
		this(input, null);
	}

	public StringStorage(String input, String name) {
		this.string = input;
		this.name = name;
	}
	
	@Override
	public InputStream getContents() throws CoreException {
		try {
			return new ByteArrayInputStream(string.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			CoreException c = new CoreException(Status.CANCEL_STATUS);
			c.setStackTrace(e.getStackTrace());
			throw c;
		}
	}

	@Override
	public IPath getFullPath() {
		return null;
	}

	// Suppress warning on implemented method signature
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public String getName() {
		if( name != null){
			return name;
		}
		final String[] lines = string.split("\n");
		final String tag = lines[1].trim();
		return tag.substring(1, tag.length() - 2);
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

}
