package org.dawnsci.plotting.api.tool;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class ToolPageFactory {

	/**
	 * Get a tool by id
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static IToolPage getToolPage(final String id) throws Exception {
		
	    final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.plotting.api.toolPage");
	    for (final IConfigurationElement e : configs) {
	    	if (id.equals(e.getAttribute("id"))) {
	    		return (IToolPage)e.createExecutableExtension("class");
	    	}
	    }
        return null;
	}
	
	/**
	 * Get a tool by id
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static List<String> getToolPageIds() throws Exception {
		
		final List<String> ret = new ArrayList<String>(31);
	    final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.plotting.api.toolPage");
	    for (final IConfigurationElement e : configs) {
            ret.add(e.getAttribute("id"));
	    }
        return ret;
	}

}
