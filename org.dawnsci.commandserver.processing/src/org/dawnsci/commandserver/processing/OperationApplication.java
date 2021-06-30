package org.dawnsci.commandserver.processing;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.commandserver.processing.process.OperationExecution;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.bean.OperationBean;

/**
 * Used to execute an operation pipeline from an OperationBean
 * 
 * Example command line to run the application from a DAWN install
 * which should work on the cluster:
 *    module load dawn/nightly ; $DAWN_RELEASE_DIRECTORY/dawn -noSplash -application org.dawnsci.commandserver.processing.processing -data ~/Operation_Consumer -path /<beanpath>/operationBean.json >> someLogFile.txt
 * 
 * @author Matthew Gerring
 *
 */
public class OperationApplication implements IApplication {

	private final static Logger logger = LoggerFactory.getLogger(OperationApplication.class);
	
	private OperationExecution runner;

	/**
	 * Must have the path to where the OperationBean is jsoned
	 * as an argument called 'path'
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		logger.debug("Starting Processing Application");
		
		// Parse out the configuration.
		final Map      args          = context.getArguments();
		final String[] configuration = (String[])args.get("application.args");
        
		Map<String, String> conf = new HashMap<String, String>(7);
		for (int i = 0; i < configuration.length; i++) {
			final String pkey = configuration[i];
			if (pkey.startsWith("-")) {
				conf.put(pkey.substring(1), configuration[i+1]);
			}
		}
		
		try {
			this.runner = new OperationExecution();
			runner.run(createOperationBean(conf));
		} catch (Exception e) {
			logger.error("Error occured in processing:", e);
			return 1;
		}
		
		return IApplication.EXIT_OK;
	}

	private OperationBean createOperationBean(Map<String, String> conf) throws Exception {
		final String path   = conf.get("path");
		
		String json = new String(Files.readAllBytes(new File(path).toPath()));
		
		IMarshallerService service = Activator.getService(IMarshallerService.class);
		
		if (service == null) {
			throw new Exception("Could not get services required to deserialize bean");
		}
		
		OperationBean bean = service.unmarshal(json, OperationBean.class);
		if (conf.containsKey("publisheruri")){
			bean.setPublisherURI(conf.get("publisheruri"));
		}
		
		return bean;
	}

	@Override
	public void stop() {
		if (runner!=null) runner.stop();
	}

}
