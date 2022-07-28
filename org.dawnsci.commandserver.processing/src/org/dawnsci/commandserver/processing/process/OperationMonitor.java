package org.dawnsci.commandserver.processing.process;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;

import org.dawnsci.commandserver.processing.Activator;
import org.dawnsci.commandserver.processing.process.ProcessingMessage.ProcessingStatus;
import org.dawnsci.commandserver.processing.process.ProcessingMessage.SwmrStatus;
import org.eclipse.january.IMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.diamond.scisoft.analysis.processing.IFlushMonitor;
import uk.ac.diamond.scisoft.analysis.processing.bean.OperationBean;
import uk.ac.gda.common.activemq.ISessionService;

/**
 * Simply logs the message returned from the operations.
 * 
 * @author Matthew Gerring
 *
 */
public class OperationMonitor implements IMonitor, IFlushMonitor {
	
	private static final String PROCESSING_TOPIC = "gda.messages.processing";
	
	private static final Logger logger = LoggerFactory.getLogger(OperationMonitor.class);

	private OperationBean 	      obean;
	//Total and count currently not used but likely to be in future
	private int           	      total;
	private int           	      count;
	private boolean       		  cancelled;
	private final ISessionService sessionService;
	private final ObjectMapper 	  mapper;
	
	public OperationMonitor(OperationBean obean, int total) {
		this.obean       = obean;
		this.total       = total;
		this.sessionService = Activator.getService(ISessionService.class);
		this.mapper = new ObjectMapper().findAndRegisterModules();
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
		if (sessionService == null) {
			logger.debug("Ignoring message request as there is no session");
			return;
		}
		
		ProcessingMessage m = new ProcessingMessage(obean.getOutputFilePath(),
				obean.getFilePath(), status, obean.isReadable() ? SwmrStatus.ACTIVE : SwmrStatus.DISABLED);
		sendProcessingMessageWithBestEffort(m);
	}

	private void sendProcessingMessageWithBestEffort(ProcessingMessage processingMessage) {
		try {
			// Create a session for sending a single message
			Session session = createSession();
			
			// Serialize message to JSON
			String asJson = mapper.writeValueAsString(processingMessage);
			
			// Broadcast message to topic
			Message message = session.createTextMessage(asJson);
			Topic topic = session.createTopic(PROCESSING_TOPIC);
			session.createProducer(topic).send(message);
		} catch (JMSException | JsonProcessingException | NullPointerException e) {
			// NullPointer case is because ISessionService throws a NullPointer if the
			// connection fails
			logger.error("Could not broadcast message", e);
		}	
	}
	
	private Session createSession() throws JMSException {
		// Laziness means that an error will only be thrown the first time this is tried
		String publisherUri = obean.getPublisherURI();
		if (obean.getPublisherURI() == null || obean.getPublisherURI().isEmpty())
			throw new JMSException("No publisher URI set");
		
		return sessionService.getSession(publisherUri, false, Session.AUTO_ACKNOWLEDGE);
	}
}
