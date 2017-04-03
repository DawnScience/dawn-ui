package org.dawnsci.datavis.live;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.datavis.model.ILiveFileListener;
import org.dawnsci.datavis.model.ILiveFileService;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.processing.IOperationBean;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPropertyFilter.FilterAction;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.CommandConstants;

public class LiveFileServiceImpl implements ILiveFileService {

	private Set<ILiveFileListener> listeners = new HashSet<>();
	private ISubscriber<EventListener> subscriber;
	
	private static long MIN_REFRESH_TIME = 2000;
	
	private UpdateJob job;
	
	@Override
	public void addLiveFileListener(ILiveFileListener l) {
		listeners.add(l);
	}

	@Override
	public void removeLiveFileListener(ILiveFileListener l) {
		listeners.remove(l);
		
	}
	
	@Override
	public void runUpdate(Runnable runnable) {
		if (job == null) {
			job = new UpdateJob("Update Live Data");
			job.setPriority(Job.INTERACTIVE);
		}
		job.setRunnable(runnable);
		job.schedule();
	}

	@Override
	public void attach() {
		IEventService eService = ServiceManager.getIEventService();
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri==null) return; // Nothing to start, standard DAWN.

		
		try {
			final URI uri = new URI(suri);
			subscriber = eService.createSubscriber(uri, EventConstants.STATUS_TOPIC);
			
			// We don't care about the scan request, removing it means that
			// all the points models and detector models to not have to resolve in
			// order to get the event.
			subscriber.addProperty("scanRequest", FilterAction.DELETE); 
			subscriber.addProperty("position", FilterAction.DELETE); 		            
			subscriber.addListener(new IScanListener() {
				
				public void scanEventPerformed(ScanEvent evt) {
					for (ILiveFileListener l : listeners) {
						l.refreshRequest();
					}
				}
				
				@Override
				public void scanStateChanged(ScanEvent event) {
					
					if (Boolean.getBoolean("org.dawnsci.mapping.ui.processing.off")) return;
					
					ScanBean beanNoScanReq = event.getBean();
					final String filePath = beanNoScanReq.getFilePath();
					// Scan started
					if (beanNoScanReq.scanStart() == true) {
//						logger.info("Pushing data to live visualisation from SWMR file: {}", filePath);

						// Create the LiveDataBean
//						LiveDataBean liveDataBean = new LiveDataBean();
						
						// Recent change to GDA means that its configuration may be read without
						// making a dependency on it.
						String host = getDataServerHost();
						int port    = getDataServerPort();
						

						LiveLoadedFile f = new LiveLoadedFile(filePath, host, port);
						
						fireListeners(f);


						// Configure the liveDataBean with a host and port to reach a dataserver
//						liveDataBean.setHost(dataServerHost);
						// Default the port to -1 so it can be checked next. An Integer is unboxed here to int
//						liveDataBean.setPort(dataServerPort);
						// Check the liveDataBean is valid
//						if (liveDataBean.getHost() == null || liveDataBean.getPort() == -1) {
//							logger.error("Live visualisation failed. The properties: {} or {} have not been set",
//									"GDA/gda.dataserver.host", "GDA/gda.dataserver.port");
//							// We can't do anything live at this point so return
//							return;
//						}

						// Create map holding the info needed to display the map
//						Map<String, Object> eventMap = new HashMap<String, Object>();
//						eventMap.put("path", filePath);
//						eventMap.put("live_bean", liveDataBean);
//
//						// Send the event
//						eventAdmin.postEvent(new Event(DAWNSCI_MAPPING_FILE_OPEN, eventMap));
//					}
//					// Scan ended swap out remote SWMR file access for direct file access
//					if (beanNoScanReq.scanEnd() == true) {
//						logger.info("Switching from remote SWMR file to direct access: {}", filePath);
//						Map<String, Object> eventMap = new HashMap<String, Object>();
//						eventMap.put("path", filePath);
//						// Reload the old remote file
//						eventAdmin.postEvent(new Event(DAWNSCI_MAPPING_FILE_RELOAD, eventMap));
					}
					
					if (beanNoScanReq.scanEnd() == true) {
//						logger.info("Switching from remote SWMR file to direct access: {}", filePath);
//						Map<String, Object> eventMap = new HashMap<String, Object>();
//						eventMap.put("path", filePath);
						// Reload the old remote file
//						eventAdmin.postEvent(new Event(DAWNSCI_MAPPING_FILE_RELOAD, eventMap));
						for (ILiveFileListener l : listeners) l.localReload(filePath);
					}
				}
				
			});
			
			
			ISubscriber<EventListener> procSub = eService.createSubscriber(uri, "scisoft.operation.STATUS_TOPIC");
			
			procSub.addListener(new IBeanListener<StatusBean>() {

				@Override
				public void beanChangePerformed(BeanEvent<StatusBean> evt) {
					System.out.println("bean update " + evt.getBean().toString());
					if (evt.getBean() instanceof IOperationBean && evt.getBean().getStatus().isRunning()) {
						String host = getDataServerHost();
						int port    = getDataServerPort();
						

						LiveLoadedFile f = new LiveLoadedFile(((IOperationBean)evt.getBean()).getOutputFilePath(), host, port);
						
						fireListeners(f);

						
						
					}
					
					if (!evt.getBean().getStatus().isFinal()) return;
					if (evt.getBean() instanceof IOperationBean) {

						for (ILiveFileListener l : listeners) l.localReload(((IOperationBean)evt.getBean()).getOutputFilePath());

					}
				}
				
				public Class<StatusBean> getBeanClass() {
					return StatusBean.class;
				}
			});
			
//			logger.info("Created subscriber");
			
		} catch (URISyntaxException | EventException e) {
//			logger.error("Could not subscribe to the event service", e);
		}
		
		
		
	}
	
	private void fireListeners(LoadedFile f) {
		for (ILiveFileListener l : listeners) l.fileLoaded(f);
	}
	
	public static String getDataServerHost() {
		String name = System.getProperty("org.eclipse.dawnsci.data.server.host");
		if (name==null) name = System.getProperty("GDA/gda.dataserver.host");
		if (name==null) name = System.getProperty("gda.dataserver.host");
		return name;
	}

	// TODO put this in global place?
	public static int getDataServerPort() {
		int port = Integer.getInteger("org.eclipse.dawnsci.data.server.port", -1);
		if (port<=0) port = Integer.getInteger("GDA/gda.dataserver.port", -1);
		if (port<=0) port = Integer.getInteger("gda.dataserver.port", -1);
		return port;
	}
	
	private class UpdateJob extends Job {

		private final AtomicReference<Runnable> task =new AtomicReference<Runnable>();
		
		public UpdateJob(String name) {
			super(name);
		}
		
		public void setRunnable(Runnable runnable) {
			this.task.set(runnable);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Runnable local = task.getAndSet(null);
			if (local == null) return Status.OK_STATUS;
			local.run();


			try {
				Thread.sleep(MIN_REFRESH_TIME);
			} catch (InterruptedException e) {
				return Status.OK_STATUS;
			}


			return Status.OK_STATUS;
		}
		
	}

}
