/*-
 * Copyright 2018 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.processing.ui;

public class ProcessingEventConstants {

	/**
	 * Topic name for data update event
	 */
	public static final String DATA_UPDATE = "org/dawnsci/events/processing/DATAUPDATE";

	/**
	 * Topic name for initial update event
	 */
	public static final String INITIAL_UPDATE = "org/dawnsci/events/processing/INITIALUPDATE";

	/**
	 * Topic name for process update event
	 */
	public static final String PROCESS_UPDATE = "org/dawnsci/events/processing/PROCESSUPDATE";

	/**
	 * Topic name for error
	 */
	public static final String ERROR = "org/dawnsci/events/processing/ERROR";
}

