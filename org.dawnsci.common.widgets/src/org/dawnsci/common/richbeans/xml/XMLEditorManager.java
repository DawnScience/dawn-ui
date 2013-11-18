/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.xml;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.common.richbeans.xml.string.StringInput;
import org.dawnsci.common.richbeans.xml.string.StringStorage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class XMLEditorManager {

	/**
	 * Opens the editors for the xml in these strings.
	 * @param files
	 */
	public static void openXmlEditorsFromFiles(final List<IFile> files)  throws Exception {
		
		final List<IStorageEditorInput> inputs = new ArrayList<IStorageEditorInput>(files.size());
		for (IFile file : files) {
			inputs.add(new FileEditorInput(file));
		}
		openXmlEditors(inputs);
	}
	/**
	 * Opens the editors for the xml in these strings.
	 * @param files
	 */
	public static void openXmlEditorsFromStrings(final List<String> files)  throws Exception {
		
		final List<IStorageEditorInput> inputs = new ArrayList<IStorageEditorInput>(files.size());
		for (String file : files) {
			inputs.add(new StringInput(new StringStorage(file)));
		}
		openXmlEditors(inputs);
	}
	
	/**
	 * Sets the current xml editors open to be the ones in the passed in IEditorInput list.
	 * Will reuse and close editors as apppropriate.
	 * 
	 * @param files
	 */
	public static void openXmlEditors(final List<IStorageEditorInput> files) throws Exception {
		
		final IWorkbenchPage page  = getActivePage();
		if (page==null) throw new Exception("Active page not found!");
		
		final List<String> ids     = getIds(files);
	
		// Close unwanted editors but leave others open (loses order)
		final IEditorReference[] editors  = page.getEditorReferences();
		final List<String>       checked  = new ArrayList<String>(editors.length);
		for (int i = 0; i < editors.length; i++) {
			final String id = editors[i].getId();
			if (!ids.contains(id) || checked.contains(id)) { // Closes if not wanted or one of it already exists.
				final IEditorPart editor = editors[i].getEditor(false);
				if (editor.isDirty()) editor.doSave(new NullProgressMonitor());
				page.closeEditor(editor, false);
			}
			checked.add(id);
		}
	
		// Open editors or reuse for a given type if there already.
		for (int i = 0; i < files.size(); i++) {
			final IStorageEditorInput input = files.get(i);
			
			final String               id    = ids.get(i);
			final IEditorReference[]   eds   = page.findEditors(null, id, IWorkbenchPage.MATCH_ID);
			if (eds!=null && eds.length>0) {
				ED_LOOP: for (int j = 0; j < eds.length; j++) {
					final IEditorPart editor = eds[j].getEditor(false);
					if (editor instanceof IReusableEditor) {
						if (editor.isDirty()) editor.doSave(new NullProgressMonitor());
						page.reuseEditor((IReusableEditor)editor, input);
						break ED_LOOP;
					}
				}
			} else {
				page.openEditor(input, id);
			}
			
		}
	}

	private static List<String> getIds(List<IStorageEditorInput> files) {
		
		final List<String> ids = new ArrayList<String>(files.size());
		for (IStorageEditorInput file : files) {
			ids.add(XMLBeanContentDescriberFactory.getInstance().getId(file));
		}
		return ids;
	}
	
	/**
	 * @return IWorkbenchPage
	 */
	public static IWorkbenchPage getActivePage() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench==null) return null;
		final IWorkbenchWindow window = bench.getActiveWorkbenchWindow();
		if (window==null) return null;
		return window.getActivePage();
	}


}
