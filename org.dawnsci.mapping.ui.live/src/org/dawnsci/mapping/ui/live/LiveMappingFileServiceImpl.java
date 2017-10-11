package org.dawnsci.mapping.ui.live;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.mapping.ui.ILiveMapFileListener;
import org.dawnsci.mapping.ui.ILiveMappingFileService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.processing.IOperationBean;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.core.IPropertyFilter.FilterAction;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveMappingFileServiceImpl implements ILiveMappingFileService {

private static final String PROCESSING_QUEUE_NAME = "scisoft.operation.STATUS_QUEUE";
	
	private Set<ILiveMapFileListener> listeners = new HashSet<>();
	private ISubscriber<EventListener> scanSubscriber;
	private ISubscriber<EventListener> procSubscriber;
	
	private boolean attached = false;
	private IScanListener scanListener;
	private IBeanListener<StatusBean> beanListener;
	
	private UpdateJob job;
	
	private static long MIN_REFRESH_TIME = 2000;
	
	private String[] initialFiles;
	
	private static final Logger logger = LoggerFactory.getLogger(LiveMappingFileServiceImpl.class);
	
	@Override
	public void addLiveFileListener(ILiveMapFileListener l) {
		listeners.add(l);
		if (!attached) attach();
	}


	@Override
	public void removeLiveFileListener(ILiveMapFileListener l) {
		listeners.remove(l);
		if (listeners.isEmpty()) {
			detach();
		}
		
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
	public void setInitialFiles(String[] files) {
		initialFiles = files.clone();
		
	}

	private void attach() {
		IEventService eService = LiveMappingServiceManager.getIEventService();
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri==null) return; // Nothing to start, standard DAWN.

		
		try {
			final URI uri = new URI(suri);
			scanSubscriber = eService.createSubscriber(uri, EventConstants.STATUS_TOPIC);
			
			// We don't care about the scan request, removing it means that
			// all the points models and detector models to not have to resolve in
			// order to get the event.
			scanSubscriber.addProperty("scanRequest", FilterAction.DELETE); 
			scanSubscriber.addProperty("position", FilterAction.DELETE); 	
			
			if (scanListener == null) scanListener = new ScanListener();
			
			scanSubscriber.addListener(scanListener);
			
			procSubscriber = eService.createSubscriber(uri, "scisoft.operation.STATUS_TOPIC");
			
			if (beanListener == null) beanListener = new BeanListener();
			
			procSubscriber.addListener(beanListener);
			
//			logger.info("Created subscriber");
			
			Job loadRunningScan = new Job("Load running scans") {
				
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					List<String> allRunningFiles = getAllRunningFiles();
					
					for (String fname : allRunningFiles){
						try {
							fireListeners(fname,null);
						} catch (Exception e) {
							logger.error("Could not load running file {}",fname,e);
						}
						
					}
					
					if (initialFiles != null) {
						for (String fname : initialFiles) {
							if (!allRunningFiles.contains(fname)) {
								for (ILiveMapFileListener l : listeners) l.fileLoadRequest(fname,null,-1, null);
							}
						}
					}

					return org.eclipse.core.runtime.Status.OK_STATUS;
				}
			};
			
			loadRunningScan.schedule();
			
		} catch (URISyntaxException | EventException e) {
			logger.error("Could not subscribe to the event service", e);
		}
	}
	
	private class UpdateJob extends Job {

		private final AtomicReference<Runnable> task =new AtomicReference<>();
		
		public UpdateJob(String name) {
			super(name);
		}
		
		public void setRunnable(Runnable runnable) {
			this.task.set(runnable);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Runnable local = task.getAndSet(null);
			if (local == null) return org.eclipse.core.runtime.Status.OK_STATUS;
			local.run();


			try {
				Thread.sleep(MIN_REFRESH_TIME);
			} catch (InterruptedException e) {
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}


			return org.eclipse.core.runtime.Status.OK_STATUS;
		}
		
	}

	private void detach() {
		
		if (scanSubscriber != null && scanListener != null) scanSubscriber.removeListener(scanListener);
		if (procSubscriber != null && beanListener != null) procSubscriber.removeListener(beanListener);
		
		attached = false;
	}
	
	interface BeanFileExtractor {
		
		public String getQueueName();
		
		public String getFileNameFromBean(StatusBean bean);

	}
	
	private class ScanListener implements IScanListener {
		
		@Override
		public void scanEventPerformed(ScanEvent evt) {
			for (ILiveMapFileListener l : listeners) {
				l.refreshRequest();
			}
		}
		
		@Override
		public void scanStateChanged(ScanEvent event) {
			
			ScanBean beanNoScanReq = event.getBean();
			
			if (beanNoScanReq.getStatus().equals(Status.RUNNING)) {
				for (ILiveMapFileListener l : listeners) {
					l.refreshRequest();
				}
			}
			
			final String filePath = beanNoScanReq.getFilePath();
			// Scan started
			if (beanNoScanReq.scanStart()) {

//				LiveLoadedFile f = new LiveLoadedFile(filePath, host, port);
				
				fireListeners(filePath, null);

			}
			
			if (beanNoScanReq.scanEnd()) {

				for (ILiveMapFileListener l : listeners) l.localReload(filePath);
			}
		}
		
	}
	
	private class BeanListener implements IBeanListener<StatusBean> {
		
		@Override
		public void beanChangePerformed(BeanEvent<StatusBean> evt) {
			
			if (!(evt.getBean() instanceof IOperationBean)) return;
			
			StatusBean bean = evt.getBean();
			
			if (Status.RUNNING.equals(bean.getStatus()) && !Status.RUNNING.equals(bean.getPreviousStatus())) {
				String host = getDataServerHost();
				int port    = getDataServerPort();
				

//				LiveLoadedFile f = new LiveLoadedFile(((IOperationBean)evt.getBean()).getOutputFilePath(), host, port);
//				
				fireListeners(((IOperationBean)evt.getBean()).getOutputFilePath(),((IOperationBean)evt.getBean()).getFilePath());
				
				return;
				
			}
			
			if (Status.RUNNING.equals(bean.getStatus())) {
				for (ILiveMapFileListener l : listeners) {
					l.refreshRequest();
				}
			}
			
			
			if (!evt.getBean().getStatus().isFinal()) return;
			if (evt.getBean() instanceof IOperationBean) {

				for (ILiveMapFileListener l : listeners) l.localReload(((IOperationBean)evt.getBean()).getOutputFilePath());

			}
		}
		
		@Override
		public Class<StatusBean> getBeanClass() {
			return StatusBean.class;
		}
	}
	
	private void fireListeners(String f, String parent) {
		for (ILiveMapFileListener l : listeners) l.fileLoadRequest(f,getDataServerHost(),getDataServerPort(), parent);
	}
	
	public static String getDataServerHost() {
		String name = System.getProperty("org.eclipse.dawnsci.data.server.host");
		if (name==null) name = System.getProperty("GDA/gda.dataserver.host");
		if (name==null) name = System.getProperty("gda.dataserver.host");
		return name;
	}

	public static int getDataServerPort() {
		int port = Integer.getInteger("org.eclipse.dawnsci.data.server.port", -1);
		if (port<=0) port = Integer.getInteger("GDA/gda.dataserver.port", -1);
		if (port<=0) port = Integer.getInteger("gda.dataserver.port", -1);
		return port;
	}
	
	private List<String> getAllRunningFiles(){
		List<String> scan = getRunningFiles(new LiveMappingFileServiceImpl.BeanFileExtractor() {
			
			@Override
			public String getQueueName() {
				return EventConstants.STATUS_SET;
			}
			
			@Override
			public String getFileNameFromBean(StatusBean bean) {
				return bean instanceof ScanBean ? ((ScanBean)bean).getFilePath() : null;
			}

		});
		
		List<String> processing = getRunningFiles(new LiveMappingFileServiceImpl.BeanFileExtractor() {
			
			@Override
			public String getQueueName() {
				return PROCESSING_QUEUE_NAME;
			}
			
			@Override
			public String getFileNameFromBean(StatusBean bean) {
				return bean instanceof IOperationBean ? ((IOperationBean)bean).getOutputFilePath() : null;
			}

			
		});
		
		scan.addAll(processing);
		
		return scan;
	}
	
	private List<String> getRunningFiles(BeanFileExtractor extractor){
		
		List<String> output = new ArrayList<>();
		
		IEventService eService = LiveMappingServiceManager.getIEventService();
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri==null) return output; // Nothing to start, standard DAWN.


		try {
			final URI uri = new URI(suri);

			ISubmitter<StatusBean> queueConnection = eService.createSubmitter(uri, extractor.getQueueName());
			queueConnection.setStatusTopicName(EventConstants.STATUS_TOPIC);

			List<StatusBean> queue = queueConnection.getQueue(extractor.getQueueName(), null);

			for (StatusBean b : queue) {
				if (b.getStatus().isActive()) {

					String filePath = extractor.getFileNameFromBean(b);
					if (filePath != null) output.add(filePath);
				}
			}

		} catch (Exception e) {
			logger.error("Could not get running files", e);
		}

		return output;
	}

}
