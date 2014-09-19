/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.algorithm.ui.workflow;

public class WorkflowUpdaterCreator {

	/**
	 * 
	 * @param modelFilePath
	 * @return
	 *       a workflow updater
	 */
	public static IWorkflowUpdater createWorkflowUpdater(String modelFilePath){
		return new WorkflowUpdaterImpl(modelFilePath);
	}
}
