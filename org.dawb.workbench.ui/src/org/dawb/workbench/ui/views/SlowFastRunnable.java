/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.workbench.ui.views;

/**
 * Adaptor class to be overridden. This allows tasks to be defined to run in repetition and
 * also once the repetition is over
 */
public class SlowFastRunnable implements Runnable {
	private boolean fast = false;

	final public void setFast(boolean fast) {
		this.fast = fast;
	}

	final public boolean isFast() {
		return fast;
	}

	/**
	 * Override this to repeatedly perform a task
	 */
	@Override
	public void run() {
	}

	/**
	 * Override this to perform a task once repetition is over
	 */
	public void stop() {
	}
}
