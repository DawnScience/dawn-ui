/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.editors.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.dawb.common.util.io.FileUtils;

public class ZipUtils {

	private static final Map<String, Class<? extends java.io.InputStream>> CLASSES;
	static {
		CLASSES = new HashMap<String, Class<? extends java.io.InputStream>>(3);
		CLASSES.put("gz",  GZIPInputStream.class);
		CLASSES.put("zip", ZipInputStream.class);
		CLASSES.put("bz2", CBZip2InputStream.class);
	}
	
	
	public static boolean isExtensionSupported(final String ext) {
		return CLASSES.containsKey(ext);
	}

	public static InputStream getStreamForFile(final String path) throws Exception {
		return getStreamForStream(new FileInputStream(new File(path)), FileUtils.getFileExtension(path));
	}

	public static InputStream getStreamForFile(final File file) throws Exception {
		return getStreamForStream(new FileInputStream(file), FileUtils.getFileExtension(file));
	}

	public static InputStream getStreamForStream(final InputStream inputStream, final String ext) throws Exception {
		
		final Class<? extends InputStream> clazz = CLASSES.get(ext);
		if (clazz == null)
			throw new IllegalArgumentException("Can not handle the extension: " + ext);
		final Constructor<? extends InputStream> c = clazz.getConstructor(InputStream.class);
		
		final InputStream in = c.newInstance(inputStream);
		
		// Hack zip files
		if (in instanceof ZipInputStream) {
			((ZipInputStream)in).getNextEntry();
		}
		
		return in;
	}

}
