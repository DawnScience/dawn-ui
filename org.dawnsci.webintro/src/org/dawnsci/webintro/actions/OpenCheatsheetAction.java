/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.webintro.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;
import org.eclipse.ui.intro.IIntroPart;

/**
 * An exported class which extensions can contribute back as an introAction to the org.dawnsci.webintro.item extension point
 * This allows a specific cheatsheet to be launched from the welcome page. 
 * <p>
 * The cheatsheet can be specified when adding to the extension point by specifying an initialisation parameter: 
 * {@code org.dawnsci.webintro.actions.OpenCheatsheetAction:org.example.cheatsheets.MyDemoCheatsheet}
 * 
 * 
 * @author David Taylor
 *
 */
public class OpenCheatsheetAction implements IActionDelegate, IExecutableExtension {

	private String id;
	
	@Override
	public void run(IAction a) {
		
		final Action action = new OpenCheatSheetAction(id);
		
		//close the intro part
		IIntroPart part = PlatformUI.getWorkbench().getIntroManager().getIntro();
		PlatformUI.getWorkbench().getIntroManager().closeIntro(part);
		//run the appropriate launcher
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				if (action != null)
					action.run();
			}
			
		});
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		id = (String) data;
		
	}

	
}
