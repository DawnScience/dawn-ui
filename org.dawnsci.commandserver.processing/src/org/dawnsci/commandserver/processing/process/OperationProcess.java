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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.bean.OperationBean;

/**
 * Runs the OperationPipeline by executing a dawn command.
 * This command runs in a separate process which can include
 * a cluster command if required.
 * 
 * @author Matthew Gerring
 *
 */
public class OperationProcess extends ProgressableProcess<OperationBean> {	
	
	private String   processingDir;
    private Process  process;
	
    private final static Logger logger = LoggerFactory.getLogger(OperationProcess.class);

	public OperationProcess(OperationBean bean, IPublisher<OperationBean> status) {
		
		super(bean, status, false);
		
        final String runDir;
		if (isWindowsOS()) {
			// We are likely to be a test consumer, anyway the unix paths
			// from ISPyB will certainly not work, so we process in C:/tmp/
			runDir  = bean.getRunDirectory();
		} else {
			runDir  = bean.getRunDirectory();
		}
		
		String name = bean.getName();
		
		try {
			String dataPath = new File(bean.getFilePath()).getName();
			String processingPath = new File(bean.getProcessingPath()).getName();
			name = dataPath + "_"+ processingPath + "_" + bean.getDatasetPath();
		} catch (Exception e) {
			logger.error("Could not build full processing name", e);
		}
		
		
		

 		final File   dir = getUnique(new File(runDir), getLegalFileName(name)+"_", 1);
 		dir.mkdirs();
		
	    processingDir = dir.getAbsolutePath();
		bean.setRunDirectory(processingDir);
		
 		try {
			setLoggingFile(new File(dir, "operationProcessLog.txt"));
		} catch (IOException e1) {
			logger.error("Could not set logging file", e1);
		}
		
		// We record the bean so that reruns of reruns are possible.
		try {
			writeProjectBean(processingDir, "operationBean.json");
		} catch (Exception e) {
			logger.error("Could write project bean", e);
		}
	}

	@Override
	public void execute() throws EventException {
		
		// Right we a starting the reconstruction, tell them.
		bean.setStatus(Status.SUBMITTED);
		bean.setPercentComplete(0d);
		broadcast(bean);
				
		try {
			bean.setPublisherURI(publisher.getUri().getPath());
			File path = new File(processingDir, "operationBean.json");
			if (!path.exists()) throw new Exception("Cannot find path to OperationBean!");
			
			final Map<String,String> args = createApplicationArgs(path);
			String workSpace = bean.getRunDirectory() + File.separator + "workspace";
			new File(workSpace).mkdirs();
			args.put("data", workSpace);
			
			ApplicationProcess process = new ApplicationProcess(args, arguments);
			process.setApplicationName("org.dawnsci.commandserver.processing.processing");
			process.setOutFileName("operation_out.txt");
			process.setErrFileName("operation_err.txt");
			process.setPropagateSysProps(false);
			if (bean instanceof OperationBean)process.setXmx(((OperationBean)bean).getXmx());
			Process p = process.start();
			if (isBlocking()) p.waitFor();
			
			
		} catch (Throwable ne) {
			logger.error("Could not run processing", ne);
			bean.setStatus(Status.FAILED);
			bean.setMessage(ne.getMessage());
			bean.setPercentComplete(0);
			broadcast(bean);
		}
	}

	private Map<String, String> createApplicationArgs(File path) {
		final Map<String,String> args = new HashMap<String, String>(1);
		args.put("path", path.getAbsolutePath());
		args.put("ncores", Integer.toString(bean.getNumberOfCores()));
		args.put("publisheruri",publisher.getUri().toString());
		return args;
	}
    
	@Override
	public void terminate() throws EventException {
		if (process!=null) process.destroy();
	}

	public String getProcessingDir() {
		return processingDir;
	}

	public void setProcessingDir(String processingDir) {
		this.processingDir = processingDir;
	}
}
