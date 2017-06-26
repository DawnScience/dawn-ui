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
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(FileTableContent.class);
	
	public FileTableContent(File file) {
		this.file = file;
		fileName = file.getName();
		fileSizeSI = Utils.getFileSizeString(file, true);
		fileSizeReg = Utils.getFileSizeString(file, false);
		fileType = Utils.getFileTypeString(file);
		fileDate = Utils.getFileDateString(file);
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
}
