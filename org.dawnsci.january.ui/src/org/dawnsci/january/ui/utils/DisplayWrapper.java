/*
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.january.ui.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper class for SWT Display exec methods
 * <p>
 * Designed for UI controller classes tested in junit, to prevent deadlock
 * when multiple threads attempt to run code using syncExec.
 * <p>
 * When not in test mode, calls are directly delegated to Display, in test
 * mode a single threaded executor acts as the UI thread.
 */
public class DisplayWrapper {
	
	private static boolean testMode = false;
	private static volatile ExecutorService testDisplayThread;
	private static Object lock = new Object();
	
	private static final Logger logger = LoggerFactory.getLogger(DisplayWrapper.class);
	
	private DisplayWrapper() {};
	
	public static void syncExec(Runnable r) {
		if (!testMode) {
			Display.getDefault().syncExec(r);
		} else {
			testRun(r, true);
		}
		
	}
	
	public static void asyncExec(Runnable r) {
		if (!testMode) {
			Display.getDefault().asyncExec(r);
		} else {
			testRun(r,false);
		}
	}
	
	private static void testRun(Runnable r, boolean blocking) {
		ExecutorService executor = getSingleThreadExecutor();
		
		Future<?> f = executor.submit(r);
		
		if (blocking) {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Error waiting on future", e);
			}
		}
	}
	
	/**
	 * Threadsafe constuction of the test UI thread
	 * 
	 * @return executor
	 */
	private static ExecutorService getSingleThreadExecutor() {
		ExecutorService local = testDisplayThread;
		if (local == null) {
			synchronized(lock) {
				local = testDisplayThread;
				if (local == null) {
					local = testDisplayThread = Executors.newSingleThreadExecutor();
				}
			}
		}
		
		return local;
	}
	
	public static void setTestMode() {
		testMode = true;
	}

}
