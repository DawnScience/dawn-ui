package org.dawnsci.fileviewer.table;

import java.io.File;

import org.dawnsci.fileviewer.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTableContent {
	private final File file;
	private final String fileName;
	private final String fileSizeSI;
	private final String fileSizeReg;
	private final String fileType;
	private final String fileDate;
	private String fileScanCmd = "";
	
	private static final Logger logger = LoggerFactory.getLogger(FileTableContent.class);
	
	static class FileScanCmdThread extends Thread {
		private final File file;
		private String cmd = "";
		
		public FileScanCmdThread(File file) {
			this.file = file;
		}
		
		@Override
		public void run() {
			cmd = Utils.getFileScanCmdString(file);
		}
	
		public String getFileScanCmd() {
			return cmd;
		}
	}
	
	public FileTableContent(File file) {
		this.file = file;
		fileName = file.getName();
		fileSizeSI = Utils.getFileSizeString(file, true);
		fileSizeReg = Utils.getFileSizeString(file, false);
		fileType = Utils.getFileTypeString(file);
		fileDate = Utils.getFileDateString(file);
	
		FileScanCmdThread thread = new FileScanCmdThread(file);
		thread.start();
		try {
			thread.join(200);
			fileScanCmd = thread.getFileScanCmd();
		} catch (InterruptedException e) {
		}
		// Since I cannot cancel the HDF5 loader routine while it is running,
		// and since it blocks all other invocations of this loader,
		// I see no alternative other than killing this thread
		if (thread.isAlive()) {
			logger.warn("Killing thread for {}", file.getName());
			thread.stop();
		}
	}

	public File getFile() {
		return file;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileSizeSI() {
		return fileSizeSI;
	}

	public String getFileSizeReg() {
		return fileSizeReg;
	}

	public String getFileType() {
		return fileType;
	}

	public String getFileDate() {
		return fileDate;
	}

	public String getFileScanCmd() {
		return fileScanCmd;
	}
}
