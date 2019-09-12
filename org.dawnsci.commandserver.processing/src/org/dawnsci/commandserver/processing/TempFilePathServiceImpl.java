package org.dawnsci.commandserver.processing;

import org.eclipse.scanning.api.scan.IFilePathService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempFilePathServiceImpl implements IFilePathService{
	
	private static final Logger logger = LoggerFactory.getLogger(TempFilePathServiceImpl.class);

	@Override
	public String getNextPath(String template) throws Exception {
		//Not needed
		return null;
	}

	@Override
	public String getNextPath(String dir, String template) throws Exception {
		//Not needed
		return null;
	}

	@Override
	public int getScanNumber() throws Exception {
		//Not needed
		return 0;
	}

	@Override
	public String getVisit() throws Exception {
		//Not needed
		return null;
	}

	@Override
	public String createFolderForLinkedFiles(String filename) throws Exception {
		//Not needed
		return null;
	}

	@Override
	public String getMostRecentPath() throws IllegalStateException {
		//Not needed
		return null;
	}

	@Override
	public String getTempDir() {
		//Not needed
		return null;
	}

	@Override
	public String getVisitConfigDir() {
		//Not needed
		return null;
	}

	@Override
	public String getVisitDir() {
		//Not needed
		return null;
	}
	
	@Override
	public String getProcessedFilesDir() {
		//Not needed
		return null;
	}

	@Override
	public String getProcessingDir() {
		//Not needed
		return null;
	}

	@Override
	public String getPersistenceDir() {
		
		if (isRunningInGDA()) {
			logger.error("DAWN FILE SERVICE BEING USED IN GDA, THIS IS LIKELY TO CAUSE SERIOUS PROBLEMS!");
		} else {
			logger.debug("Using DAWN file path service");
		}
		
		return System.getProperty("java.io.tmpdir");
	}
	
	private boolean isRunningInGDA() {
		// gda.var should always be set to something, so it is a reliable test
		// of whether we are running in GDA.
		return System.getProperty("GDA/gda.var") != null;
	}

	@Override
	public String getProcessingTemplatesDir() {
		//Not needed
		return null;
	}

}
