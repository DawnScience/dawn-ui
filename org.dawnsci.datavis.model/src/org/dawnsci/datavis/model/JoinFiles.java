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
import java.util.Collection;
import java.io.BufferedWriter;

// Imports from org.eclipse
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;

// Imports from org.slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Currently this class just joins files with a NeXus tree, however, class
// should be extended to work with other filetypes! (2018-02-19)
public class JoinFiles {

	
	// First, set up a logger
	private static final Logger logger = LoggerFactory.getLogger(JoinFiles.class);
	
	
	public static String fileJoiner(List<LoadedFile> files) {

		// First, let's set up some variables that we'll be adding to
		LoadedFile firstFile = files.get(0);
		String neXusPath = "";
		String datasetsString = "";
		String fileNamesString = "# FILE_NAME";
		String directoryPath = "# DIR_NAME: " + firstFile.getParent() + "\n";
		
		if (firstFile.getTree() != null) {
			// Then let's get some information about the NeXus tree
			Tree tree = firstFile.getTree();
			GroupNode groupNode = tree.getGroupNode();
			
			// Check that the file isn't empty...
			if (groupNode != null) {
				// And find all the datanodes within the file
				datasetsString = nodeSearcher(groupNode, neXusPath, datasetsString);
			}
		} else {
			
			List<DataOptions> dop = firstFile.getDataOptions();
			for (DataOptions d : dop) {
				String name = d.getName();
				datasetsString += "# DATASET_NAME: " + name + "\n";
			}
			
		}
		
		
		
		// Then generate the string for all the files within the passed list
		for (Iterator<LoadedFile> loopIter = files.iterator(); loopIter.hasNext(); ) {
			LoadedFile file = loopIter.next();
			fileNamesString += "\n" + file.getName();
		}
		
		// Now create a fileWriter
		BufferedWriter fileWriter = null;
		// Then come up with the output filename
		String firstFileName = files.get(0).getName();
		String lastFileName = files.get(files.size()-1).getName();
		
		int extensionLocation = firstFileName.lastIndexOf(".");
		String outputName = firstFileName.substring(0, extensionLocation) + "--" + lastFileName.substring(0, extensionLocation) + " ";
		File tempFile = null;
		
		// Now output the text into the .dawn file
		try {
			tempFile = File.createTempFile(outputName, ".dawn");
			tempFile.setWritable(true);
			tempFile.deleteOnExit();
			
			fileWriter = new BufferedWriter(new FileWriter(tempFile.getAbsoluteFile()));
			fileWriter.write(directoryPath + datasetsString + fileNamesString);
			fileWriter.close();
		} catch (IOException fileError) {
			// Print out if we have any errors
			logger.error("There was a problem creating the .dawn linker file: " + fileError.toString());
		}
		
		// Return the absolute path to this linker file for the calling method to open
		return tempFile.getAbsolutePath();
	}
	
	
	private static String nodeSearcher(GroupNode groupNode, String neXusPath, String datasetsString) {
		// Find out what we've been passed
		Collection<String> subNodeNames = groupNode.getNames();
		
		// And loop through it
		for (Iterator<String> subNodeNameIterator = subNodeNames.iterator(); subNodeNameIterator.hasNext(); ) {
			// Creating an iterator and going through all the nodes 
			String subNodeName = subNodeNameIterator.next();
			Node subNode = groupNode.getNode(subNodeName);
			// Building this as needed for our output file
			String currentPath = neXusPath + "/" + subNodeName;
		
			// Recursing if we've been passed a group node
			if (subNode.isGroupNode()) {
				datasetsString = nodeSearcher((GroupNode) subNode, currentPath, datasetsString);
			}
			
			// Logging it if we've been passed a data node
			if (subNode.isDataNode()) {
				datasetsString += "# DATASET_NAME: " + currentPath + "\n";
			}
		}
		// And returning the results of our investigation
		return datasetsString;
	}
}
