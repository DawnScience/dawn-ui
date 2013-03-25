/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.editors.test;

import java.io.File;

import org.dawb.workbench.ui.editors.ImageEditor;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import org.dawb.common.ui.util.EclipseUtils;


/**
 * Run as a plugin test and set the PYTHONPATH so
 * that fabio works.
 * 
 * @author gerring
 *
 */
public class EditorStressTest {
	
	private long size1 = -1;
	@Before
	public void before() {
		size1 = -1;
		LoaderFactory.clear();
	}
	
	@After
	public void after() throws Exception {
		System.gc();
		EclipseUtils.delay(1000);
		long size2 = Runtime.getRuntime().freeMemory();
		if (size1-size2 > 10000) throw new Exception("The memory leak was "+(size1-size2)+"b which is too large!");
	    System.out.println("Testing many editors for a memory leak pass. They had a memory leak of "+(size1-size2)+"b compared to open editor.");
	}
	
	@Test
	public void testOpeningEditorManyTimesEDF() throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");
		String path = (bun.getLocation()+"/src/org/dawb/workbench/ui/editors/test/billeA.edf");
		path = path.substring("reference:file:".length());
		if (path.startsWith("/C:")) path = path.substring(1);
				
		size1 = openManyTimes(1,  path, 2000);
		openManyTimes(25, path, 500);
	}
	
	/** 
	 * Proves that Jep memory leaks even when used with Fabio image viewer.
	 */
	@Test
	public void testOpeningEditorManyTimesCBF() throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");
		String path = (bun.getLocation()+"/src/org/dawb/workbench/ui/editors/test/tln_1_0001.cbf");
		path = path.substring("reference:file:".length());
				
		size1 = openManyTimes(1,  path, 2000);
		openManyTimes(25, path, 1500);
	}
	
	
	/** 
	 * Proves that Jep memory leaks even when used with Fabio image viewer.
	 */
	@Test
	public void testOpeningEditorManyTimesMCCD() throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");
		String path = (bun.getLocation()+"/src/org/dawb/workbench/ui/editors/test/ref-screentest-crystal1_1_001.mccd");
		path = path.substring("reference:file:".length());
		
		size1 = openManyTimes(1,  path, 2000);
		openManyTimes(25, path, 800);
	}

	private long openManyTimes(final int numberTimes, String path, final long openTime) throws Exception {
		
		for (int i = 0; i < numberTimes; i++) {
			final IWorkbenchPage     page = EclipseUtils.getPage();		
			final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
	 		final IEditorPart      editor = page.openEditor(new FileStoreEditorInput(externalFile), ImageEditor.ID);
			
	 		page.activate(editor);
	 		page.setPartState(page.getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
	 
	 		EclipseUtils.delay(openTime);
			
			EclipseUtils.getPage().closeEditor(editor, false);
		}
		
		
		EclipseUtils.delay(1000);
		return Runtime.getRuntime().freeMemory();
	}

	
}
