package org.dawnsci.mapping.ui.live;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPropertyFilter.FilterAction;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveMappingFileServiceImpl implements ILiveMappingFileService {

	private static final String PROCESSING_SUBMIT_QUEUE_NAME = "scisoft.operation.SUBMISSION_QUEUE";
	private static final String PROCESSING_STATUS_SET_NAME = "scisoft.operation.STATUS_QUEUE";
	
	private Set<ILiveMapFileListener> listeners = new HashSet<>();
	private ISubscriber<EventListener> scanSubscriber;
	private ISubscriber<EventListener> procSubscriber;
	
	private boolean attached = false;
	private IScanListener scanListener;
	private IBeanListener<StatusBean> beanListener;
	
	private AtomicReference<Runnable> atomicRunnable = new AtomicReference<>();
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private static final long MIN_REFRESH_TIME = 2000;
	
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
	public void runUpdate(Runnable runnable, boolean queue) {

		// not queued updates are allowed to be dropped
		// Atomic runnable used as a length 1 queue
		if (!queue) {
			Runnable current = atomicRunnable.getAndSet(runnable);

			// if current is a runnable, the internal runnable hasn't been taken,
			// no need to submit in this case, stops the queue being flooded.
			if (current != null) {
				return;
			}

			executor.submit(new Runnable() {

				@Override
				public void run() {
					Runnable run = atomicRunnable.getAndSet(null);
					if (run == null)
						return;
					run.run();

					try {
						// Wait so to not swamp the UI with updates
						Thread.sleep(MIN_REFRESH_TIME);
					} catch (InterruptedException e) {
						// do nothing;
					}
				}
			});
		} else {
			// queued are added to the executor queue
			// and not dropped (reloads etc)
			executor.submit(runnable);
		}
	}
	
	@Override
	public void setInitialFiles(String[] files) {
		if (files == null) return;
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

					try {
						fireListeners(allRunningFiles.toArray(new String[allRunningFiles.size()]), null, true);
					} catch (Exception e) {
						logger.error("Could not load running files", e);
					}

					if (initialFiles != null) {
						List<String> notRunning = Arrays.stream(initialFiles).filter(f -> !allRunningFiles.contains(f)).collect(Collectors.toList());
						fireListeners(notRunning.toArray(new String[notRunning.size()]),null, false);
					}

					return org.eclipse.core.runtime.Status.OK_STATUS;
				}
			};
			
			loadRunningScan.schedule();
			
		} catch (URISyntaxException | EventException e) {
			logger.error("Could not subscribe to the event service", e);
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
				
				fireListeners(new String[] {filePath}, null,true);

			}
			
			if (beanNoScanReq.scanEnd() || beanNoScanReq.getStatus().isTerminated()) {

				for (ILiveMapFileListener l : listeners) {
					l.localReload(filePath, false);
				}
			}
		}
		
	}
	
	private class BeanListener implements IBeanListener<StatusBean> {
		
		@Override
		public void beanChangePerformed(BeanEvent<StatusBean> evt) {
			
			if (!(evt.getBean() instanceof IOperationBean)) return;
			
			StatusBean bean = evt.getBean();
			
			if (Status.RUNNING.equals(bean.getStatus()) && !Status.RUNNING.equals(bean.getPreviousStatus())) {
			
				fireListeners(new String[] {((IOperationBean)evt.getBean()).getOutputFilePath()},((IOperationBean)evt.getBean()).getFilePath(), true);
				
				return;
				
			}
			
			if (Status.RUNNING.equals(bean.getStatus())) {
				for (ILiveMapFileListener l : listeners) {
					l.refreshRequest();
				}
			}
			
			
			if (!evt.getBean().getStatus().isFinal()) return;
			if (evt.getBean() instanceof IOperationBean) {

				for (ILiveMapFileListener l : listeners) l.localReload(((IOperationBean)evt.getBean()).getOutputFilePath(), false);

			}
		}
		
		@Override
		public Class<StatusBean> getBeanClass() {
			return StatusBean.class;
		}
	}
	
	private void fireListeners(String[] fs, String parent, boolean live) {
		for (ILiveMapFileListener l : listeners) l.fileLoadRequest(fs,live ? getDataServerHost() : null, getDataServerPort(), parent);
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
		List<String> fileNames = new ArrayList<>();
		fileNames.addAll(getRunningFiles(EventConstants.SUBMISSION_QUEUE,
				bean -> ((ScanBean) bean).getFilePath()));
		fileNames.addAll(getRunningFiles(PROCESSING_SUBMIT_QUEUE_NAME, 
				bean -> bean instanceof IOperationBean ? ((IOperationBean) bean).getOutputFilePath() : null));  
		return fileNames;
	}
	
	private List<String> getRunningFiles(String submitQueueName, Function<StatusBean, String> fileNameMapper) {
		final List<StatusBean> beans = getRunningBeans(submitQueueName);
		return beans.stream()
				.filter(b -> b.getStatus().isActive())
				.map(fileNameMapper)
				.filter(Objects::nonNull)
				.collect(toList());
	}
	
	private List<StatusBean> getRunningBeans(String submitQueueName) {
		IEventService eService = LiveMappingServiceManager.getIEventService();
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri != null) { // will be null for standard DAWN
			try {
				final URI uri = new URI(suri);
				IConsumer<StatusBean> queueConnection = eService.createConsumerProxy(uri, submitQueueName);
				return queueConnection.getRunningAndCompleted();
			} catch (Exception e) {
				logger.error("Could not get running files for submission queue {}", submitQueueName, e);
			}
		}
		return Collections.emptyList();
	}



}
