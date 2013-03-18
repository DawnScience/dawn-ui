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

import org.dawb.tango.extensions.editors.MultiScanEditor;
import org.dawb.tango.extensions.editors.MultiScanMultiEditor;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Test;
import org.osgi.framework.Bundle;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

import fable.framework.toolbox.EclipseUtils;


public class EditorsTest {

	
	@Test
	public void testOpeningSpecEditor1() throws Throwable {
		
		testSpec("inhouse_testfile_one_scan.dat", "Scan 1", "L", "MA0", "MA1", "MA2", "MA3");
		
	}
	
	@Test
	public void testOpeningSpecEditor2() throws Throwable {
		
		testSpec("optics_april20110402.dat", "Scan 3", "pico0",  "pico1",  "pico2",  "pico3",  "pico4");
		
		testSpec("optics_april20110402.dat", "Scan 150", "pico0",  "pico1",  "pico2",  "pico3",  "pico4");
		
		testSpec("optics_april20110402.dat", "Scan 300", "pico0",  "pico1",  "pico2",  "pico3",  "pico4");
	}
	
	private void testSpec(String fileName, String scanName, String... dataNames) throws Exception {

		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");
		String path = (bun.getLocation()+"/src/org/dawb/workbench/ui/editors/test/"+fileName);
		path = path.substring("reference:file:".length());
		if (path.startsWith("/C:")) path = path.substring(1);
	
		final IWorkbenchPage     page = EclipseUtils.getPage();		
		final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
 
		System.gc();
		final long startSize = Runtime.getRuntime().totalMemory();

		final MultiScanMultiEditor      editor = (MultiScanMultiEditor)page.openEditor(new FileStoreEditorInput(externalFile), "org.dawb.tango.extensions.specEditor");

 		page.activate(editor);
 		page.setPartState(page.getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
 		
 		final MultiScanEditor sed = (MultiScanEditor)editor.getActiveEditor();
 		sed.setPlot(scanName, dataNames);
 		
 		EclipseUtils.delay(2000);
		
		EclipseUtils.getPage().closeEditor(editor, false);
	
 		LoaderFactory.clear();
		System.gc();
		EclipseUtils.delay(1000);
		final long endSize = Runtime.getRuntime().totalMemory();
		final long leak    = (endSize-startSize);
		if (leak>800000000) throw new Exception("The memory leak is too large! It is "+leak);
		System.out.println("The memory leak opening "+path+" is:\n   "+leak);
	}
	

}
