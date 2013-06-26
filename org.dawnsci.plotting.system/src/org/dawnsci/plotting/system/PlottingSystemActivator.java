package org.dawnsci.plotting.system;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class PlottingSystemActivator extends AbstractUIPlugin {
	
	private final static String ID = "org.dawnsci.plotting.system";

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(ID, path);
	}
	public static Image getImage(String path) {
		return getImageDescriptor(path).createImage();
	}
	
	private static IPreferenceStore plottingPreferenceStore;
	public static IPreferenceStore getPlottingPreferenceStore() {
		if (plottingPreferenceStore==null) plottingPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
	    return plottingPreferenceStore;
	}
}
