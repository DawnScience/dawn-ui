/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.jmx;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

class JMXSystemObject {

	private ObjectName            remotePlotName;
	private MBeanServerConnection client;

	/**
	 * The name of the plotting system as registered in the PlottingFactory.
	 * @param name
	 * @throws MalformedObjectNameException 
	 */
	protected JMXSystemObject(final String plotName, final String hostName, final int port) throws Exception {

		JMXServiceURL serverUrl     = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+hostName+":"+port+"/plottingservice");
		JMXConnector  conn = JMXConnectorFactory.connect(serverUrl);
		client             = conn.getMBeanServerConnection();

	    this.remotePlotName = new ObjectName("remote.plotting.system/"+plotName+":type=RemotePlottingSystem");
	}

	protected static final String getMethodName ( StackTraceElement ste[] ) {  
		   
	    String methodName = "";  
	    boolean flag = false;  
	   
	    for ( StackTraceElement s : ste ) {  
	   
	        if ( flag ) {  
	   
	            methodName = s.getMethodName();  
	            break;  
	        }  
	        flag = s.getMethodName().equals( "getStackTrace" );  
	    }  
	    return methodName;  
	}

	/**
	 * Calls method in a SWT thread safe way.
	 * @param methodName
	 * @param args
	 */
	protected Object call(final String methodName, final Object... args) {
		
		final String[] classes = args!=null ? new String[args.length] : null;
		if (classes!=null) {
			for (int i = 0; i < args.length; i++) classes[i]=args[i].getClass().getName();
		}
		return call(methodName, classes, args);
	}
	
	/**
	 * Calls method in a SWT thread safe way.
	 * @param methodName
	 * @param args
	 */
	protected Object call(final String methodName, final String[] classes, final Object... args) {
		
		try {
			return client.invoke(remotePlotName, methodName, args, classes);
		} catch (Throwable e) {
			System.out.println("Unable to process remote plotting system command: "+methodName);
			e.printStackTrace();
			return null;
		}
	}

}
