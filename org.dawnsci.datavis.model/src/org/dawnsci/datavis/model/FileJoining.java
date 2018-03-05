/*-
 * Copyright 2018 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


package org.dawnsci.datavis.model;


// Imports from java
import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedWriter;

// Imports from org.eclipse
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;

// Imports from org.slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Imports from uk.ac.diamond.scisoft
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;


// Currently this class just joins files with a NeXus tree, however, class
// should be extended to work with other filetypes! (2018-02-19)
public class FileJoining {
	
	
	// First, set up a logger
	private static final Logger logger = LoggerFactory.getLogger(FileJoining.class);
	
	
	public static String AutoFileJoiner(List<String> filePathList) {
		// First, let's find out something about the files
		String firstFilePath = filePathList.get(0);
		List<String> fileList = new ArrayList<String>();
		String directoryPath = firstFilePath.substring(0, firstFilePath.lastIndexOf(File.separator));
		
		// Now let's look at the file path list to work out whether to look deeper
		if (filePathList.size() == 1) {
			File onlyEntry = new File(firstFilePath);
			
			if (onlyEntry.isDirectory()) {
				directoryPath = firstFilePath;
				String[] directoryFileArray = onlyEntry.list();
				firstFilePath = directoryPath + File.separator + directoryFileArray[0];
				
				for (int iter = 0; iter < directoryFileArray.length; iter ++) {
					fileList.add(directoryFileArray[iter]);
				}
				
			} else {
				throw new IndexOutOfBoundsException();
			}
		// Or just take the list literally and get ready to link these files together
		} else {
			Iterator<String> filePathIterator = filePathList.iterator();
			
			while (filePathIterator.hasNext()) {
				String currentFilePath = filePathIterator.next();
				fileList.add(currentFilePath.substring(currentFilePath.lastIndexOf(File.separator) + 1));
			}
		}
		
		List<String> fileDatasets = DatasetsAsList(firstFilePath);
		
		String directoryString = "# DIR_NAME: " + directoryPath + "\n";
		String datasetsString = JoinFilesStringBuilder("# DATASET_NAME: ", fileDatasets, "\n");
		String fileNamesString = "# FILE_NAME";
		fileNamesString += JoinFilesStringBuilder("\n", fileList, "");
		
		// Now create a fileWriter
		BufferedWriter fileWriter = null;
		File tempFile = null;
		String outputName = JoinedFileName(fileList.get(0), fileList.get(fileList.size() - 1));
		
		// Now output the text into the .dawn file
		try {
			tempFile = File.createTempFile(outputName, ".dawn");
			tempFile.setWritable(true);
			tempFile.deleteOnExit();
			
			fileWriter = new BufferedWriter(new FileWriter(tempFile.getAbsoluteFile()));
			fileWriter.write(directoryString + datasetsString + fileNamesString + "\n");
			fileWriter.close();
		} catch (IOException fileError) {
			// Print out if we have any errors
			logger.error("There was a problem creating the .dawn linker file: " + fileError.toString() + " ");
		}
		
		// Return the absolute path to this linker file for the calling method to open
		return tempFile.getAbsolutePath();
	}
	
	
	private static String JoinedFileName(String firstFileName, String lastFileName) {
		int firstExtensionLocation = firstFileName.lastIndexOf(".");
		int lastExtensionLocation = lastFileName.lastIndexOf(".");
		String outputName = firstFileName.substring(0, firstExtensionLocation) + "--" + lastFileName.substring(0, lastExtensionLocation) + " ";
		return outputName;
	}
	
	private static List<String> DatasetsAsList(String filePath) {
		IDataHolder loadedFile;
		List<String> fileDatasets = new ArrayList<String>();
		
		try {
			loadedFile = LoaderFactory.getData(filePath);
			String[] datasetNameArray = loadedFile.getNames();
			
			for (int iter = 0; iter < datasetNameArray.length; iter ++) {
				String currentString = datasetNameArray[iter];
				fileDatasets.add(currentString);
			}
		} catch (Exception e) {
			logger.error("Error opening file " + filePath.substring(filePath.lastIndexOf(File.separator)) + "\n\n");
		}
		
		return fileDatasets;
	}
	
	
	private static String JoinFilesStringBuilder (String beginning, List<String> middle, String end) {
		// Set up our returnable
		String builtString = "";
		
		// Set up our iterator
		Iterator<String> stringIterator = middle.iterator();
		
		// Loop over our iterator
		while (stringIterator.hasNext()) {
			String currentString = stringIterator.next();
			builtString += beginning + currentString + end;
		}
		
		// Return the built string
		return builtString;
	}
}