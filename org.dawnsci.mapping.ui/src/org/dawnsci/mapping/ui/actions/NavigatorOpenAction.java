package org.dawnsci.mapping.ui.actions;

import java.nio.file.Files;
import java.nio.file.Path;

import org.dawb.common.util.io.IOpenFileAction;
import org.dawnsci.mapping.ui.api.IMapFileController;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigatorOpenAction implements IOpenFileAction {
	
	
	private static final Logger logger = LoggerFactory.getLogger(NavigatorOpenAction.class);
	
	@Override
	public void openFile(Path file) {

		if (file==null) return;
		
		if (!Files.isDirectory(file)) {
			BundleContext bundleContext =
	                FrameworkUtil.
	                getBundle(this.getClass()).
	                getBundleContext();
			
			IMapFileController manager = bundleContext.getService(bundleContext.getServiceReference(IMapFileController.class));
			if (manager != null) {
				manager.loadFiles(new String[] {file.toAbsolutePath().toString()}, null);
			} else {
				logger.error("Could not get file manager");
			}
		}
	}
}
