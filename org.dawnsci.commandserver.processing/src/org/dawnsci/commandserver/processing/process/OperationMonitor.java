package org.dawnsci.commandserver.processing.process;

import java.net.URI;

import org.dawnsci.commandserver.processing.Activator;
import org.eclipse.january.IMonitor;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
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
	
	private static final Logger logger = LoggerFactory.getLogger(OperationMonitor.class);

	private OperationBean obean;
	private int           total;
	private int           count;
	private boolean       cancelled;
	private IPublisher<OperationBean> publisher; 
	
	public OperationMonitor(OperationBean obean, int total) {
		this.obean       = obean;
		this.total       = total;
		try {
			IEventService eventService = Activator.getService(IEventService.class);
			 publisher = eventService.createPublisher(new URI(obean.getPublisherURI()), "scisoft.operation.STATUS_TOPIC");
		} catch (Exception e) {
			logger.error("Could not create publisher:",e);
		}
	}
	
	@Override
	public void worked(int amount) {
		count+=amount;
		double done = (double)count / (double)total;
		obean.setPercentComplete(done);
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
		if (publisher != null) {
			obean.setStatus(Status.COMPLETE);
			try {
				publisher.broadcast(obean);
			} catch (EventException e) {
				logger.error("Could not broadcast bean:",e);
				logger.error(obean.toString());
			}
		}
	}

	public void setRunning() {
		if (publisher != null) {
			obean.setStatus(Status.RUNNING);
			try {
				publisher.broadcast(obean);
			} catch (EventException e) {
				logger.error("Could not broadcast bean:",e);
				logger.error(obean.toString());
			}
		}
	}

	@Override
	public void fileFlushed() {
		if (publisher != null) {
			obean.setPreviousStatus(Status.RUNNING);
			obean.setStatus(Status.RUNNING);
			obean.setMessage("Flushed " + count + " frames");
			try {
				publisher.broadcast(obean);
			} catch (EventException e) {
				logger.error("Could not broadcast bean:",e);
				logger.error(obean.toString());
			}
		}
		
	}
}
