/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Diamond Light Source - Custom modifications for Diamond's needs
 *******************************************************************************/
package org.dawnsci.fileviewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Recursive functions to copy/delete file or directory structure
 *
 */
public class FileUtils {

	/**
	 * Copies a file or entire directory structure.
	 * 
	 * @param oldFile
	 *            the location of the old file or directory
	 * @param newFile
	 *            the location of the new file or directory
	 * @param simulateOnly
	 * @param progressDialog
	 * @return true iff the operation succeeds without errors
	 */
	public static boolean copyFileStructure(File oldFile, File newFile, boolean simulateOnly, ProgressDialog progressDialog) {
		if (oldFile == null || newFile == null)
			return false;

		// ensure that newFile is not a child of oldFile or a dupe
		File searchFile = newFile;
		do {
			if (oldFile.equals(searchFile))
				return false;
			searchFile = searchFile.getParentFile();
		} while (searchFile != null);

		if (oldFile.isDirectory()) {
			/*
			 * Copy a directory
			 */
			if (progressDialog != null) {
				progressDialog.setDetailFile(oldFile, ProgressDialog.COPY);
			}
			if (simulateOnly) {
				// System.out.println(getResourceString("simulate.DirectoriesCreated.text",
				// new Object[] { newFile.getPath() }));
			} else {
				if (!newFile.mkdirs())
					return false;
			}
			File[] subFiles = oldFile.listFiles();
			if (subFiles != null) {
				if (progressDialog != null) {
					progressDialog.addWorkUnits(subFiles.length);
				}
				for (int i = 0; i < subFiles.length; i++) {
					File oldSubFile = subFiles[i];
					File newSubFile = new File(newFile, oldSubFile.getName());
					if (!copyFileStructure(oldSubFile, newSubFile, simulateOnly, progressDialog))
						return false;
					if (progressDialog != null) {
						progressDialog.addProgress(1);
						if (progressDialog.isCancelled())
							return false;
					}
				}
			}
		} else {
			/*
			 * Copy a file
			 */
			if (simulateOnly) {
				// System.out.println(getResourceString("simulate.CopyFromTo.text",
				// new Object[] { oldFile.getPath(), newFile.getPath() }));
			} else {
				FileReader in = null;
				FileWriter out = null;
				try {
					in = new FileReader(oldFile);
					out = new FileWriter(newFile);

					int count;
					while ((count = in.read()) != -1)
						out.write(count);
				} catch (FileNotFoundException e) {
					return false;
				} catch (IOException e) {
					return false;
				} finally {
					try {
						if (in != null)
							in.close();
						if (out != null)
							out.close();
					} catch (IOException e) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Deletes a file or entire directory structure.
	 * 
	 * @param oldFile
	 *            the location of the old file or directory
	 * @param simulateOnly
	 * @param progressDialog
	 * @return true iff the operation succeeds without errors
	 */
	public static boolean deleteFileStructure(File oldFile, boolean simulateOnly, ProgressDialog progressDialog) {
		if (oldFile == null)
			return false;
		if (oldFile.isDirectory()) {
			/*
			 * Delete a directory
			 */
			if (progressDialog != null) {
				progressDialog.setDetailFile(oldFile, ProgressDialog.DELETE);
			}
			File[] subFiles = oldFile.listFiles();
			if (subFiles != null) {
				if (progressDialog != null) {
					progressDialog.addWorkUnits(subFiles.length);
				}
				for (int i = 0; i < subFiles.length; i++) {
					File oldSubFile = subFiles[i];
					if (!deleteFileStructure(oldSubFile, simulateOnly, progressDialog))
						return false;
					if (progressDialog != null) {
						progressDialog.addProgress(1);
						if (progressDialog.isCancelled())
							return false;
					}
				}
			}
		}
		if (simulateOnly) {
			// System.out.println(getResourceString("simulate.Delete.text",
			// new Object[] { oldFile.getPath(), oldFile.getPath() }));
			return true;
		}
		return oldFile.delete();
	}
}
