/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.commandserver.processing.process;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.AbstractLockingPausableProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Extend to provide a connection between a running process.
 * 
 * This class has a user readable log file, represented by PrintStream out
 * and a logger. A given message might be applicable for both places, depending
 * on what message the user might need to see.
 * 
 * @param <T> the bean type, a subclass of {@link StatusBean}
 *
 * @author Matthew Gerring
 */
public abstract class ProgressableProcess<T extends StatusBean> extends AbstractLockingPausableProcess<T> {

	private static final Logger logger = LoggerFactory.getLogger(ProgressableProcess.class);


	private boolean            blocking    = false;
	protected Map<String, String> arguments;
	
	protected Thread thread;
	
	public ProgressableProcess(T bean, IPublisher<T> statusPublisher, boolean blocking) {
		
		super(bean,statusPublisher);
		this.blocking        = blocking;
		
		bean.setPreviousStatus(Status.SUBMITTED);
		bean.setStatus(Status.PREPARING);
		try {
			bean.setHostName(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			logger.warn("Cannot find local host!", e);
		}
		broadcast(bean);
	}
	
	private final void executeInternal() {
        try {
			thread = Thread.currentThread();
        	execute();
        	if (out!=System.out) {
        		out.close();
        		out = System.out;
        	}
        } catch (Exception ne) {
        	ne.printStackTrace(out);
			logger.error("Cannot run process!", ne);
        	
			bean.setStatus(Status.FAILED);
			bean.setMessage(ne.getMessage());
			bean.setPercentComplete(0);
			broadcast(bean);
        }
	}

	
	/**
	 * Please provide a termination for the process by implementing this method.
	 * If the process has a stop file, write it now; if it needs to be killed,
	 * get its pid and kill it; if it is running on a cluster, use the qdel or dramaa api.
	 * 
	 * @throws Exception
	 */
	public void terminate() throws EventException {
		if (thread!=null) thread.interrupt(); // In case it is paused.
	}
	

	/**
	 * Call to start the process and broadcast status
	 * updates. Subclasses may redefine what is done
	 * on the start method, by default a thread is started
	 * in daemon mode to run things.
	 */
	public void start() {
		
		if (isBlocking()) {
			executeInternal(); // Block until process has run, uses the calling thread.
		} else {
			this.thread = new Thread(new Runnable() {
				public void run() {
					executeInternal();
				}
			}, "Run "+bean.getName());
			thread.setDaemon(true);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
	}

    protected void pkill(int pid, String dir) throws Exception {
    	
    	// Use pkill, seems to kill all of the tree more reliably
    	ProcessBuilder pb = new ProcessBuilder();
		
		// Can adjust env if needed:
		// Map<String, String> env = pb.environment();
		pb.directory(new File(dir));
		
		File log = new File(dir, "xia2_kill.txt");
		pb.redirectErrorStream(true);
		pb.redirectOutput(Redirect.appendTo(log));
		
		pb.command("bash", "-c", "pkill -9 -s "+pid);
		
		Process p = pb.start();
		p.waitFor();
    }

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	
	
	public static final String getLegalFileName(String name) {
		name = name.replace(" ", "_");
		name = name.replaceAll("[^a-zA-Z0-9_]", "");
        return name;
	}

	public Map<String, String> getArguments() {
		return arguments;
	}

	public void setArguments(Map<String, String> arguments) {
		this.arguments = arguments;
	}


}
