package org.dawnsci.dedi;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


public class Activator extends AbstractUIPlugin {
   // The plug-in ID
   public static final String PLUGIN_ID = "dedi";

   // The shared instance
   private static Activator plugin;


   /**
    * This method is called upon plug-in activation.
    */
   @Override
   public void start(BundleContext context) throws Exception {
      super.start(context);
      plugin = this;
   }

   
   /**
    * This method is called when the plug-in is stopped.
    */
   @Override
   public void stop(BundleContext context) throws Exception {
      plugin = null;
      super.stop(context);
   }

   /**
    * Returns the shared instance
    */
   public static Activator getDefault() {
      return plugin;
   }

   /**
    * Returns an image descriptor for the image file at the given
    * plug-in relative path
    *
    * @param path the path
    * @return the image descriptor
    */
   public static ImageDescriptor getImageDescriptor(String path) {
      return imageDescriptorFromPlugin(PLUGIN_ID, path);
   }
   
   
   public static <T> T getService(Class<T> serviceClass) {
		ServiceReference<T> ref = plugin.getBundle().getBundleContext().getServiceReference(serviceClass);
		return plugin.getBundle().getBundleContext().getService(ref);
	}
}
