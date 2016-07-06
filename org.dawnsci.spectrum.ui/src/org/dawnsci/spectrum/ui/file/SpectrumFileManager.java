/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotclient.dataset.DataMailEvent;
import uk.ac.diamond.scisoft.analysis.plotclient.dataset.DatasetMailman;
import uk.ac.diamond.scisoft.analysis.plotclient.dataset.IDataMailListener;

/**
* Manages the loading of files and the setting of the default x and y dataset names
*/
public class SpectrumFileManager implements IDataMailListener {
	
	private IPlottingSystem<?> system;
	private Map<String,ISpectrumFile> spectrumFiles;
	private HashSet<ISpectrumFileListener> listeners;
	private final static Logger logger = LoggerFactory.getLogger(SpectrumFileManager.class);
	private IContain1DData cachedFile;


	/**
	 * There should be one of these per TraceProcessPage
	 * @param system
	 */
	public SpectrumFileManager(IPlottingSystem<?> system) {
		spectrumFiles = new LinkedHashMap<String,ISpectrumFile>();
		listeners     = new HashSet<ISpectrumFileListener>();
		this.system   = system;
		
		DatasetMailman.getLocalManager().addMailListener(this);
	}
	

	public void dispose() {
		spectrumFiles.clear();
		listeners.clear();
		DatasetMailman.getLocalManager().removeMailListener(this);
	}

	@Override
	public void mailReceived(DataMailEvent evt) {
		
        if (evt.getData()==null || evt.getData().isEmpty()) {
        	removeFile(evt.getFullName());
        } else {
        	// Make sure the names match.
        	for (String name : evt.getData().keySet()) {
        		evt.getData().get(name).setName(name);
 			}
        	
        	Map<String,IDataset> sorted = new TreeMap<String, IDataset>(evt.getData());
        	
        	// TODO What about sending the x-axis?
        	final SpectrumInMemory mem = new SpectrumInMemory(evt.getFullName(), evt.getFullName(), null, sorted.values(), system);
    		removeFile(evt.getFullName());
            addFile(mem);
        }
	}

	public void addFile(ISpectrumFile file) {
		if (spectrumFiles.containsKey(file.getLongName())) return;
		
		spectrumFiles.put(file.getLongName(), file);
		
		file.plotAll();
		fireFileLoadedListeners(new SpectrumFileEvent(this, file));
	}

	public void addFiles(List<String> paths) {
		for (String path : paths) {
			if (spectrumFiles.containsKey(path)) return;
		}
		
		SpectrumFileLoaderJob job = new SpectrumFileLoaderJob(paths, system, spectrumFiles);
		ProgressMonitorDialog spectrumLoaderProgress = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
		spectrumLoaderProgress.setCancelable(true);
		try {
			spectrumLoaderProgress.run(true, true, job);
		} catch (Exception e1) {
			MessageDialog.openError(
							Display.getCurrent().getActiveShell(),
							"Spectrum file loading Error",
							"An error occured during data loading: " + e1.getMessage());
			logger.error(e1.getMessage());
		}
		spectrumFiles = job.getSpectrumFiles();
		
		ISpectrumFile file = null;
		
		for (String path : paths) {
			if (spectrumFiles.containsKey(path)) {
				file = spectrumFiles.get(path);
				break;
			};
		}
		
		if (file == null) return;
		
		fireFileLoadedListeners(new SpectrumFileEvent(this, file));
	}

	public Set<String> getFileNames() {
		return spectrumFiles.keySet();
	}

	public Collection<ISpectrumFile> getFiles() {
		return spectrumFiles.values();
	}

	public ISpectrumFile removeFile(String path) {
		ISpectrumFile file = spectrumFiles.get(path);
		if (file == null)
			return null;
		spectrumFiles.remove(path);
		file.removeAllFromPlot();
		fireFileRemovedListeners(new SpectrumFileEvent(this, file));
		return file;
	}

	public void addFileListener(ISpectrumFileListener listener) {
		listeners.add(listener);
	}

	public void removeFileListener(ISpectrumFileListener listener) {
		listeners.remove(listener);
	}

	private void fireFileLoadedListeners(SpectrumFileEvent event) {
		for (ISpectrumFileListener listener : listeners)
			listener.fileLoaded(event);
	}

	private void fireFileRemovedListeners(SpectrumFileEvent event) {
		for (ISpectrumFileListener listener : listeners)
			listener.fileRemoved(event);
	}

	/**
	 * Saves spectrum files in order to csv file.
	 * 
	 * @param exportFile
	 */
	public void export(File file) throws Exception {

		if (file.exists())
			file.delete();
		file.createNewFile();
		final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		try {
			for (String path : spectrumFiles.keySet()) {
				final File f = new File(path);
				if (f.exists()) {
					final StringBuilder buf = new StringBuilder();
					buf.append(path);
					buf.append(",");
					
					final ISpectrumFile sfile = spectrumFiles.get(path);
					if (sfile.getxDatasetName()!=null) {
						buf.append(sfile.getxDatasetName());
						buf.append(",");
					}
					for (String name : sfile.getyDatasetNames()) {
						buf.append(name);
						buf.append(",");
					}
					
					writer.write(buf.toString());
					writer.newLine();
				}
			}
		} finally {
			writer.close();
		}
	}

	public boolean isEmpty() {
		return spectrumFiles.isEmpty();
	}

	public IContain1DData getCachedFile() {
		return cachedFile;
	}

	public void setCachedFile(IContain1DData cachedFile) {
		this.cachedFile = cachedFile;
	}

}
