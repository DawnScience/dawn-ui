package org.dawnsci.spectrum.ui.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class SpectrumFileManager {
	
	private IPlottingSystem system;
	private Map<String,SpectrumFile> spectrumFiles;
	private HashSet<ISpectrumFileListener> listeners;
	private String xdefault = "/entry1/counterTimer01/Energy";
	private String ydefault = "/entry1/counterTimer01/lnI0It";
	
	public SpectrumFileManager(IPlottingSystem system) {
		spectrumFiles = new LinkedHashMap<String,SpectrumFile>();
		listeners = new HashSet<ISpectrumFileListener>();
		this.system = system;
	}
	
	public void addFile(String path) {
		
		if (spectrumFiles.containsKey(path)) return;
		
		SpectrumFileLoaderJob job = new SpectrumFileLoaderJob("File loader job", path);
		job.schedule();
	}
	
	public Set<String> getFileNames() {
		return spectrumFiles.keySet();
	}
	
	public Collection<SpectrumFile> getFiles() {
		return spectrumFiles.values();
	}
	
	public void removeFile(String path) {
		SpectrumFile file = spectrumFiles.get(path);
		spectrumFiles.remove(path);
		removeFromPlot(file);
		fireFileListeners(new SpectrumFileOpenedEvent(this, file));
	}
	
	private void pushToPlot(SpectrumFile file) {
		if (system != null) system.updatePlot1D(file.getxDataset(), file.getyDatasets(), null);
	}
	
	private void removeFromPlot(SpectrumFile file) {
		for (String dataset : file.getyDatasetNames()) {
			ITrace trace = system.getTrace(file.getPath() + " : " + dataset);
			if (trace != null) system.removeTrace(trace);
		}
	}
	
	public void addFileListener(ISpectrumFileListener listener) {
		listeners.add(listener);
	}
	
	public void removeFileListener(ISpectrumFileListener listener) {
		listeners.remove(listener);
	}
	
	private void fireFileListeners(SpectrumFileOpenedEvent event) {
		for (ISpectrumFileListener listener : listeners) listener.fileLoaded(event);
	}
	
	private class SpectrumFileLoaderJob extends Job {

		private final String path;
		
		public SpectrumFileLoaderJob(String name, final String path) {
			super(name);
			this.path = path;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			SpectrumFile file = SpectrumLoaderFactory.loadSpectrumFile(path);
			
			if (file.contains(xdefault)) file.setxDatasetName(xdefault);
			file.addyDatasetName(ydefault);
			
			spectrumFiles.put(file.getPath(), file);
			
			pushToPlot(file);
			
			fireFileListeners(new SpectrumFileOpenedEvent(this, file));
			
			return Status.OK_STATUS;
		}

	}

}
