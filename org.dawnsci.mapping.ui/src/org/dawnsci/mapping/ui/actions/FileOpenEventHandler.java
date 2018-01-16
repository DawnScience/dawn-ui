package org.dawnsci.mapping.ui.actions;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.LiveDataBean;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class FileOpenEventHandler implements EventHandler {

	@Override
	public void handleEvent(final Event event) {
		
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				
				@Override
				public void run() {
					handleEvent(event);
					
				}
			});
			return;
		}
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		IMapFileController fm = bundleContext.getService(bundleContext.getServiceReference(IMapFileController.class));
		

		if (fm == null) return;
		
		String path = (String)event.getProperty("path");

		if (event.getTopic().endsWith("CLOSE")) {
			fm.removeFile(path);
			return;
		}
		
		if (event.getTopic().endsWith("LOCALRELOAD")) {
			fm.localReloadFile(path);
			return;
		}
		
		
		if (event.containsProperty("map_bean")) {
			Object p = event.getProperty("map_bean");
			if (p instanceof MappedDataFileBean) fm.loadFile(path, (MappedDataFileBean)p, null);
		}
		
		if (event.containsProperty("live_bean")) {
			Object p = event.getProperty("live_bean");
			if (p instanceof LiveDataBean) fm.loadLiveFile(path, (LiveDataBean)p,null);
			return;
		}
		
		if (event.containsProperty("host") && event.containsProperty("port")) {
			LiveDataBean b = new LiveDataBean();
			b.setHost(event.getProperty("host").toString());
			b.setPort(Integer.parseInt(event.getProperty("port").toString()));
			fm.loadLiveFile(path, (LiveDataBean)b,null);
			return;
		}

		fm.loadFiles(new String[] {path}, null);
	}

}
