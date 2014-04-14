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

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.passerelle.common.project.PasserelleProjectUtils;
import org.dawb.workbench.ui.project.DataProjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 * You must define the system property 'org.dawb.large.folder' to use this test.
 * For instance -Dorg.dawb.large.folder="/buffer/linpickard1/results/large test files/"
 * 
 * It should contain some large files. If nexus files, all the tabs will be selected.
 * 
 * @author gerring
 *
 */
public class LargeFilesTest {

	@BeforeClass
	public static void before() throws Exception {
		
		PasserelleProjectUtils.createWorkflowProject("workflows", ResourcesPlugin.getWorkspace().getRoot(), true, null);
		DataProjectUtils.createDataProject("data", ResourcesPlugin.getWorkspace().getRoot(), true, null);
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		
		// We will now import the 'large test files' folder to the project 'workspace'
		final IProject data = ResourcesPlugin.getWorkspace().getRoot().getProject("data");
        final File     dir  = new File(System.getProperty("org.dawb.large.folder"));
        if (!dir.exists()) throw new Exception("Please set the large files folder system property and ensure it exists, 'org.dawb.large.folder'");
		final IFolder folder = data.getFolder(dir.getName());
		folder.createLink(dir.toURI(), IResource.DEPTH_ONE, null);
		folder.refreshLocal(IResource.DEPTH_ONE, null);
	}
	
	@Test
	public void testOpeningLargeFiles() throws Throwable {
		

	    final IProject data = ResourcesPlugin.getWorkspace().getRoot().getProject("data");
        final File     dir  = new File(System.getProperty("org.dawb.large.folder"));
        
		final IFolder folder = data.getFolder(dir.getName());
	    final IResource[] res= folder.members();
	    for (IResource iResource : res) {
			if (!(iResource instanceof IFile)) continue;
			
			System.out.println("Opening "+iResource.getName()+" size "+org.eclipse.core.filesystem.EFS.getStore(iResource.getLocationURI()).fetchInfo().getLength());
			final IEditorPart part = EclipseUtils.openEditor((IFile)iResource);
			if (part==null) throw new Exception("Did not open part for "+iResource);

			EclipseUtils.getPage().setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
			
			EclipseUtils.delay(1000);
			
			if (part instanceof MultiPageEditorPart) {
				final MultiPageEditorPart mp = (MultiPageEditorPart)part;
			
				IEditorPart[] eds = mp.findEditors(mp.getEditorInput());
				for (int i = 0; i < eds.length; i++) {
					mp.setActiveEditor(eds[i]);
				}

			}
			
			EclipseUtils.delay(1000);
			EclipseUtils.getPage().closeEditor(part, false);
		}
		
	    EclipseUtils.getActivePage().closeAllEditors(false);
	}
	
}
