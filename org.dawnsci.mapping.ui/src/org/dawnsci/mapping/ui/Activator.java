package org.dawnsci.mapping.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.dawnsci.mapping.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin("org.dawnsci.mapping.ui", path);
    }
    		 
    public static Image getImage(String path) {
        return getImageDescriptor(path).createImage();
    }

    /**
     * 
     * @return the JMS URI which the acquisition is using
     */
    public static final String getAcquisitionJmsUri() {
    	String uri = null;
	    if (uri == null) uri = System.getProperty("org.eclipse.scanning.broker.uri");
	    if (uri == null) uri = System.getProperty("GDA/gda.activemq.broker.uri"); // GDA specific but not a compilation dependency.
	    if (uri == null) uri = System.getProperty("gda.activemq.broker.uri"); // GDA specific but not a compilation dependency.		
		return uri; // It is legal for there to be no URI
	}


}
