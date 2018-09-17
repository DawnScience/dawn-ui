package org.dawnsci.datavis.live;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.datavis.model.ILiveFileListener;
import org.dawnsci.datavis.model.ILiveFileService;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.processing.IOperationBean;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPropertyFilter.FilterAction;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.diamond.scisoft.analysis.processing.bean.OperationBean;

public class LiveFileServiceImpl implements ILiveFileService {

	private static final String PROCESSING_QUEUE_NAME = "scisoft.operation.STATUS_QUEUE";
	
	private Set<ILiveFileListener> listeners = new HashSet<>();
	private ISubscriber<EventListener> scanSubscriber;
	private ISubscriber<EventListener> procSubscriber;
	
	private boolean attached = false;
	private IScanListener scanListener;
	private IBeanListener<OperationBean> beanListener;
	
	private static long MIN_REFRESH_TIME = 2000;
	
	private static final Logger logger = LoggerFactory.getLogger(LiveFileServiceImpl.class);
	
	private ExecutorService executor =  Executors.newSingleThreadExecutor();
	private AtomicReference<Runnable> atomicRunnable = new AtomicReference<>();
	
	@Override
	public void addLiveFileListener(ILiveFileListener l) {
		listeners.add(l);
		if (!attached) attach();
	}

	@Override
	public void removeLiveFileListener(ILiveFileListener l) {
		listeners.remove(l);
		if (listeners.isEmpty()) {
			detach();
		}
		
	}
	
	@Override
	public void runUpdate(Runnable runnable, boolean queue) {
		
		//not queued updates are allowed to be dropped
		//Atomic runnable used as a length 1 queue
		if (!queue) {
			atomicRunnable.set(runnable);
			executor.submit(new Runnable() {
					
				@Override
				public void run() {
					Runnable run = atomicRunnable.getAndSet(null);
					if (run == null) return;
					run.run();
					
					try {
						//Wait so to not swamp the UI with updates
						Thread.sleep(MIN_REFRESH_TIME);
					} catch (InterruptedException e) {
						//do nothing;
					}
				}
			});
		} else {
			//queued are added to the executor queue
			//and not dropped (reloads etc)
			executor.submit(runnable);
		}
	}

	private void attach() {
		IEventService eService = ServiceManager.getIEventService();
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
					String host = getDataServerHost();
					int port    = getDataServerPort();
					
					for (String fname : allRunningFiles){
						try {
							LiveLoadedFile f = new LiveLoadedFile(fname, host, port);
							fireListeners(f);
						} catch (Exception e) {
							logger.error("Could not load running file {}",fname,e);
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
	
	private List<String> getAllRunningFiles(){
		List<String> scan = getRunningFiles(new LiveFileServiceImpl.BeanFileExtractor() {
			
			@Override
			public String getQueueName() {
				return EventConstants.STATUS_SET;
			}
			
			@Override
			public String getFileNameFromBean(StatusBean bean) {
				return bean instanceof ScanBean ? ((ScanBean)bean).getFilePath() : null;
			}

		});
		
		List<String> processing = getRunningFiles(new LiveFileServiceImpl.BeanFileExtractor() {
			
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
		
		IEventService eService = ServiceManager.getIEventService();
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri==null) return output; // Nothing to start, standard DAWN.


		try (ISubmitter<StatusBean> queueConnection = eService.createSubmitter(new URI(suri), extractor.getQueueName())) {
			queueConnection.setStatusTopicName(EventConstants.STATUS_TOPIC);

			List<StatusBean> queue = queueConnection.getQueue(extractor.getQueueName());

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
	
	private void detach() {
		if (scanSubscriber != null && scanListener != null) {
			scanSubscriber.removeListener(scanListener);
			try {
				scanSubscriber.disconnect();
			} catch (EventException e) {
				logger.error("Could not disconnect subscriber to scan topic", e);
			}
			scanSubscriber = null;
		}
		
		if (procSubscriber != null && beanListener != null) {
			procSubscriber.removeListener(beanListener);
			try {
				procSubscriber.disconnect();
			} catch (EventException e) {
				logger.error("Could not disconnect subscriber to processing topic", e);
			}
			procSubscriber = null;
		}
		
		attached = false;
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

	public static int getDataServerPort() {
		int port = Integer.getInteger("org.eclipse.dawnsci.data.server.port", -1);
		if (port<=0) port = Integer.getInteger("GDA/gda.dataserver.port", -1);
		if (port<=0) port = Integer.getInteger("gda.dataserver.port", -1);
		return port;
	}
	
	interface BeanFileExtractor {
		
		public String getQueueName();
		
		public String getFileNameFromBean(StatusBean bean);

	}
	
	private class ScanListener implements IScanListener {
		
		@Override
		public void scanEventPerformed(ScanEvent evt) {
			for (ILiveFileListener l : listeners) {
				l.refreshRequest();
			}
		}
		
		@Override
		public void scanStateChanged(ScanEvent event) {
			
			ScanBean beanNoScanReq = event.getBean();
			
			if (beanNoScanReq.getStatus().equals(Status.RUNNING)) {
				for (ILiveFileListener l : listeners) {
					l.refreshRequest();
				}
			}
			
			final String filePath = beanNoScanReq.getFilePath();
			// Scan started
			if (beanNoScanReq.scanStart()) {

				// Recent change to GDA means that its configuration may be read without
				// making a dependency on it.
				String host = getDataServerHost();
				int port    = getDataServerPort();
				

				LiveLoadedFile f = new LiveLoadedFile(filePath, host, port);
				
				fireListeners(f);

			}
			
			if (beanNoScanReq.scanEnd()) {

				for (ILiveFileListener l : listeners) l.localReload(filePath);
			}
		}
		
	}
	
	private class BeanListener implements IBeanListener<OperationBean> {
		
		@Override
		public void beanChangePerformed(BeanEvent<OperationBean> evt) {
			
			if (!(evt.getBean() instanceof IOperationBean)) return;
			
			StatusBean bean = evt.getBean();
			
			if (Status.RUNNING.equals(bean.getStatus()) && !Status.RUNNING.equals(bean.getPreviousStatus())) {
				String host = getDataServerHost();
				int port    = getDataServerPort();
				

				LiveLoadedFile f = new LiveLoadedFile(((IOperationBean)evt.getBean()).getOutputFilePath(), host, port);
				
				fireListeners(f);
				
				return;
				
			}
			
			if (Status.RUNNING.equals(bean.getStatus())) {
				for (ILiveFileListener l : listeners) {
					l.refreshRequest();
				}
			}
			
			
			if (!evt.getBean().getStatus().isFinal()) return;
			if (evt.getBean() instanceof IOperationBean) {

				for (ILiveFileListener l : listeners) l.localReload(((IOperationBean)evt.getBean()).getOutputFilePath());

			}
		}
		
		@Override
		public Class<OperationBean> getBeanClass() {
			return OperationBean.class;
		}
	}

}
