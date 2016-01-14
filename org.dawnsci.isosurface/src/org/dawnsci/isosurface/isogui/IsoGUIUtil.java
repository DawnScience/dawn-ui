package org.dawnsci.isosurface.isogui;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

public class IsoGUIUtil {

	public static ImageDescriptor getImageDescriptor(String name) {
		try {
			final URL url = IsoGUIUtil.class.getResource(name);
			return ImageDescriptor.createFromURL(url);
		} catch (Exception ne) {
			throw new RuntimeException(ne);
		}
	}
	
}
