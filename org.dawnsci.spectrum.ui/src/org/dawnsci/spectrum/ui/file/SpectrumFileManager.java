package org.dawnsci.spectrum.ui.file;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.spectrum.ui.Activator;
import org.dawnsci.spectrum.ui.preferences.SpectrumConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Manages the loading of files and the setting of the default x and y dataset names
*/
public class SpectrumFileManager {
	
	private IPlottingSystem system;
	private Map<String,ISpectrumFile> spectrumFiles;
	private HashSet<ISpectrumFileListener> listeners;
	private final static Logger logger = LoggerFactory.getLogger(SpectrumFileManager.class);
	private final ISchedulingRule mutex = new Mutex();
	
	public SpectrumFileManager(IPlottingSystem system) {
		spectrumFiles = new LinkedHashMap<String,ISpectrumFile>();
		listeners = new HashSet<ISpectrumFileListener>();
		this.system = system;
	}

	public void addFile(ISpectrumFile file) {
		if (spectrumFiles.containsKey(file.getLongName())) return;
		
		spectrumFiles.put(file.getLongName(), file);
		
		file.plotAll();
		fireFileListeners(new SpectrumFileOpenedEvent(this, file));
	}
	
	public void addFile(String path) {
		
		if (spectrumFiles.containsKey(path)) return;
		
		SpectrumFileLoaderJob job = new SpectrumFileLoaderJob("File loader job", path);
		job.setRule(mutex);
		job.schedule();
	}
	
	public Set<String> getFileNames() {
		return spectrumFiles.keySet();
	}
	
	public Collection<ISpectrumFile> getFiles() {
		return spectrumFiles.values();
	}
	
	public void removeFile(String path) {
		ISpectrumFile file = spectrumFiles.get(path);
		spectrumFiles.remove(path);
		file.removeAllFromPlot();
		fireFileListeners(new SpectrumFileOpenedEvent(this, file));
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
	
	private void setXandYdatasets(SpectrumFile file) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String xdatasetNamesCombined = store.getString(SpectrumConstants.X_DATASETS);
		String ydatasetNamesCombined = store.getString(SpectrumConstants.Y_DATASETS);
		
		xdatasetNamesCombined = xdatasetNamesCombined.replace("*", ".*");
		ydatasetNamesCombined = ydatasetNamesCombined.replace("*", ".*");
		
		String[] foundx = findDatasets(file.getDataNames(), xdatasetNamesCombined);
		String[] foundy = findDatasets(file.getDataNames(), ydatasetNamesCombined);
		
		for (String name : foundx) {
			if (name != null) {
				file.setxDatasetName(name);
				break;
			}
		}
		
		for (String name : foundy) {
			if (name != null) {
				file.addyDatasetName(name);
				break;
			}
		}
	}
	
	private String[] findDatasets(Collection<String> datasetNames, String namesCombined) {
		
		String[] datasets = namesCombined.split(";");
		
		String[] found = new String[datasets.length];
		
		StringBuilder builder = new StringBuilder();
		
		for (String string : datasets) {
			builder.append("(");
			builder.append(string);
			builder.append(")");
			builder.append("|");
		}
		
		builder.deleteCharAt(builder.length()-1);
		
		Pattern pattern = Pattern.compile(builder.toString());
		
		for (String dataset : datasetNames) {
			Matcher matcher = pattern.matcher(dataset);
			if (matcher.matches()) {
				for (int i = 1; i < matcher.groupCount()+1; i++) {
					if (matcher.group(i) != null && found[i-1] == null) {
						found[i-1] = matcher.group(i);
					}
				}
			}
		}
		
		return found;
	}
	
	private class SpectrumFileLoaderJob extends Job {

		private final String path;
		
		public SpectrumFileLoaderJob(String name, final String path) {
			super(name);
			this.path = path;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			SpectrumFile file = SpectrumFile.create(path,system);
			
			if (file == null) {
				logger.error("Could not load file!");
				return Status.CANCEL_STATUS;
			}
			
			setXandYdatasets(file);
			
			spectrumFiles.put(file.getPath(), file);
			
			//file.plotAll();
			
			fireFileListeners(new SpectrumFileOpenedEvent(this, file));
			
			return Status.OK_STATUS;
		}

	}
	
	public class Mutex implements ISchedulingRule {

		public boolean contains(ISchedulingRule rule) {
			return (rule == this);
		}

		public boolean isConflicting(ISchedulingRule rule) {
			return (rule == this);
		}

	}

}
