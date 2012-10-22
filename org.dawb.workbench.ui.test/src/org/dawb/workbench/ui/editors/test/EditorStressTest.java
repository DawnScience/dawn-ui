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

import org.dawb.common.util.eclipse.BundleUtils;
import org.dawb.workbench.ui.editors.ImageEditor;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Test;
import org.osgi.framework.Bundle;

import fable.framework.toolbox.EclipseUtils;


/**
 * Run as a plugin test and set the PYTHONPATH so
 * that fabio works.
 * 
 * @author gerring
 *
 */
public class EditorStressTest {

	// Size should be increased for true stress testing but is
	// decreased for everyday unit tests.
	private static int SIZE = 10;
	
	@Test
	public void testOpeningEditorManyTimesEDF() throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");
		String path = (bun.getLocation()+"/src/org/dawb/workbench/ui/editors/test/billeA.edf");
		path = path.substring("reference:file:".length());
		if (path.startsWith("/C:")) path = path.substring(1);
		
		openManyTimes(path, 500);
	}
	
	/** 
	 * Proves that Jep memory leaks even when used with Fabio image viewer.
	 */
	//@Test
	public void testOpeningEditorManyTimesCBF() throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");
		String path = (bun.getLocation()+"/src/org/dawb/workbench/ui/editors/test/tln_1_0001.cbf");
		path = path.substring("reference:file:".length());
		
		openManyTimes(path, 1500);
	}
	
	
	/** 
	 * Proves that Jep memory leaks even when used with Fabio image viewer.
	 */
	@Test
	public void testOpeningEditorManyTimesMCCD() throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");
		String path = (bun.getLocation()+"/src/org/dawb/workbench/ui/editors/test/ref-screentest-crystal1_1_001.mccd");
		path = path.substring("reference:file:".length());
		
		openManyTimes(path, 800);
	}

	private void openManyTimes(String path, final long openTime) throws Exception {
		
		final long startSize = Runtime.getRuntime().totalMemory();
		for (int i = 0; i < SIZE; i++) {
			final IWorkbenchPage     page = EclipseUtils.getPage();		
			final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
	 		final IEditorPart      editor = page.openEditor(new FileStoreEditorInput(externalFile), ImageEditor.ID);
			
	 		page.activate(editor);
	 		page.setPartState(page.getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
	 
	 		
	 		EclipseUtils.delay(openTime);
			
			EclipseUtils.getPage().closeEditor(editor, false);
		}
		
		System.gc();
		
 		EclipseUtils.delay(500);

 		final long endSize = Runtime.getRuntime().totalMemory();
 		final long leak    = (endSize-startSize);
 		if (leak>800000000) throw new Exception("The memory leak is too large! It is "+leak);
 		System.out.println("The memory leak opening "+SIZE+" "+path+" files is:\n   "+leak);
	}

	@Test
	public void testMultipleEditorsDifferentFiles() throws Throwable {
		
		// We open the right editor for each example folder
		doMultiEditorTest(null, true);
		
		// This can be used to compare with Diamond
		//doMultiEditorTest("uk.ac.diamond.scisoft.analysis.rcp.editors.PlotEditor", false);
		//doMultiEditorTest("fable.imageviewer.editor.ImageEditor",   true);
	}
	
	public void doMultiEditorTest(final String editorID, final boolean closeEditor) throws Throwable {
		
		final File  dir = new File(BundleUtils.getBundleLocation("org.dawb.workbench.examples")+"/data");
		final File[] fa = dir.listFiles();
		
		final long startSize = Runtime.getRuntime().totalMemory();

		for (int i = 0; i < fa.length; i++) {
			
			if (fa[i].isDirectory()) continue;
			final IWorkbenchPage page = EclipseUtils.getPage();	
			
			IEditorPart    editor;
			if (editorID==null) {
				editor = EclipseUtils.openExternalEditor(fa[i].getAbsolutePath());
			} else {
				final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(fa[i]);
		 		editor = page.openEditor(new FileStoreEditorInput(externalFile), editorID);
			}
			
	 		page.activate(editor);
	 		page.setPartState(page.getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
	 		
	 		EclipseUtils.delay(500);
			
	 		if (closeEditor) EclipseUtils.getPage().closeEditor(editor, false);
		}
		
		System.gc();
		
 		EclipseUtils.delay(500);

 		final long endSize = Runtime.getRuntime().totalMemory();
 		final long leak    = (endSize-startSize);
 		if (leak>800000000) throw new Exception("The memory leak is too large! It is "+leak);
		System.out.println("The memory leak opening example files is "+leak);

	}
	

	
}
