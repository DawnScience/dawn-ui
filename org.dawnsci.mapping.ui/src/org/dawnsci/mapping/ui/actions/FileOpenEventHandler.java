package org.dawnsci.mapping.ui.actions;

import org.dawnsci.mapping.ui.MappingPerspective;
import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.LiveDataBean;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.eclipse.dawnsci.plotting.api.PlottingEventConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class FileOpenEventHandler implements EventHandler {
	
	private static final String DAQ_MAPPING_ID = "uk.ac.diamond.daq.mapping.ui.experiment.MappingPerspective";

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
		
		try {
			String id = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective().getId();
			if (!id.equals(MappingPerspective.ID) && !id.equals(DAQ_MAPPING_ID)) {
				return;
			}
		} catch (Exception e) {
			return;
		}
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		IMapFileController fm = bundleContext.getService(bundleContext.getServiceReference(IMapFileController.class));
		

		if (fm == null) return;
		
		String[] paths = (String[])event.getProperty(PlottingEventConstants.MULTIPLE_FILE_PROPERTY);
		
		if (paths != null) {
			fm.loadFiles(paths, null);
			return;
		}
		
		
		String path = (String)event.getProperty(PlottingEventConstants.SINGLE_FILE_PROPERTY);

		if (path == null) return;

		if (event.getTopic().endsWith("CLOSE")) {
			fm.removeFile(path);
			return;
		}
		
		if (event.getTopic().endsWith("LOCALRELOAD")) {
			fm.localReloadFile(path, true);
			return;
		}
		
		
		if (event.containsProperty("map_bean")) {
			Object p = event.getProperty("map_bean");
			if (p instanceof MappedDataFileBean) fm.loadFile(path, (MappedDataFileBean)p, null);
		}
		
		if (event.containsProperty(PlottingEventConstants.LIVE_BEAN_PROPERTY)) {
			Object p = event.getProperty(PlottingEventConstants.LIVE_BEAN_PROPERTY);
			if (p instanceof LiveDataBean) fm.loadLiveFile(path, (LiveDataBean)p,null, true);
			return;
		}
		
		if (event.containsProperty("host") && event.containsProperty("port")) {
			LiveDataBean b = new LiveDataBean();
			b.setHost(event.getProperty("host").toString());
			b.setPort(Integer.parseInt(event.getProperty("port").toString()));
			fm.loadLiveFile(path, (LiveDataBean)b,null, true);
			return;
		}

		fm.loadFiles(new String[] {path}, null);
	}

}
