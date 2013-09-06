package org.dawnsci.slicing.api;

import org.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;


/**
 * This class can read contributed slice components and return an
 * implementation of ISliceComponent which can be used for slicing.
 * 
 * A preference page will be added for choosing component from a drop down.
 * 
 * @author fcp94556
 *
 */
public class SlicingFactory {
	
	
	/**
	 * 
	 * @param sliceGalleryId the id of a view which implements ISliceGallery
	 * @return
	 * @throws Exception
	 */
	public static ISliceSystem createSliceSystem(String sliceGalleryId) throws Exception {

		ISliceSystem system = createSliceSystem();
		system.setSliceGalleryId(sliceGalleryId);
		return system;
	}
	
	/**
	 * Current implementation just gives back the first slice component
	 * it can find.
	 * @return ISliceSystem
	 * 
	 */
	public static ISliceSystem createSliceSystem() throws Exception {
		
        IConfigurationElement[] systems = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.slicing.api.sliceComponent");
        return (ISliceSystem)(systems[0].createExecutableExtension("class"));
	}
}
