package org.dawnsci.commandserver.processing.process;

import java.net.URI;

import org.dawnsci.commandserver.processing.Activator;
import org.dawnsci.commandserver.processing.process.ProcessingMessage.ProcessingStatus;
import org.dawnsci.commandserver.processing.process.ProcessingMessage.SwmrStatus;
import org.eclipse.january.IMonitor;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.IFlushMonitor;
import uk.ac.diamond.scisoft.analysis.processing.bean.OperationBean;

/**
 * Simply logs the message returned from the operations.
 * 
 * @author Matthew Gerring
 *
 */
public class OperationMonitor implements IMonitor, IFlushMonitor {
	
	private static final String PROCESSING_TOPIC = "gda.messages.processing";
	
	private static final Logger logger = LoggerFactory.getLogger(OperationMonitor.class);

	private OperationBean obean;
	//Total and count currently not used but likely to be in future
	private int           total;
	private int           count;
	private boolean       cancelled;
	private IPublisher<ProcessingMessage> publisher; 
	
	public OperationMonitor(OperationBean obean, int total) {
		this.obean       = obean;
		this.total       = total;
		
		if (obean.getPublisherURI() == null || obean.getPublisherURI().isEmpty()) {
			logger.debug("No publisher URI set");
		} else {
			try {
				IEventService eventService = Activator.getService(IEventService.class);
				 publisher = eventService.createPublisher(new URI(obean.getPublisherURI()), PROCESSING_TOPIC);
			} catch (Exception e) {
				logger.error("Could not create publisher:",e);
			}
		}
	}
	
	@Override
	public void worked(int amount) {
		count+=amount;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void subTask(String taskName) {
		//do nothing - this is used for the progress bar in the ui
		//so not useful here
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public void setComplete() {
		
		buildMessageAndBroadcast(ProcessingStatus.FINISHED);

	}

	public void setRunning() {
		buildMessageAndBroadcast(ProcessingStatus.STARTED);
	}

	@Override
	public void fileFlushed() {
		buildMessageAndBroadcast(ProcessingStatus.UPDATED);
	}
	
	private void buildMessageAndBroadcast(ProcessingStatus status) {
		if (publisher == null) {
			return;
		}
		
		ProcessingMessage m = new ProcessingMessage(obean.getOutputFilePath(),
				obean.getFilePath(), status, obean.isReadable() ? SwmrStatus.ACTIVE : SwmrStatus.DISABLED);

		try {
			publisher.broadcast(m);
		} catch (EventException e) {
			logger.error("Could not broadcast message:",e);
		}

	}
}
