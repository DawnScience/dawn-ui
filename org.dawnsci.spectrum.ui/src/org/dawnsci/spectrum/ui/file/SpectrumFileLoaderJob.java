package org.dawnsci.spectrum.ui.file;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dawnsci.spectrum.ui.Activator;
import org.dawnsci.spectrum.ui.preferences.SpectrumConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpectrumFileLoaderJob implements IRunnableWithProgress {

	private Logger logger = LoggerFactory.getLogger(SpectrumFileLoaderJob.class);

	private final List<String> paths;
	private Map<String,ISpectrumFile> spectrumFiles;

	private IPlottingSystem system;

	public SpectrumFileLoaderJob(List<String> paths, IPlottingSystem system, Map<String,ISpectrumFile> spectrumFiles) {
		super();
		this.paths = paths;
		this.system = system;
		this.spectrumFiles = spectrumFiles;
	}

	@Override
	public void run(IProgressMonitor monitor) {
		monitor.beginTask("Loading files", paths.size());

		for (String path : paths) {
			SpectrumFile file = SpectrumFile.create(path, system);

			if (file == null) {
				logger .error("Could not load file!");
			}
			monitor.setTaskName("Loading " + path);
			setXandYdatasets(file);

			spectrumFiles.put(file.getPath(), file);

			if (monitor.isCanceled())
				return;
			monitor.worked(1);
		}
		monitor.done();
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
			if (name != null && file.getPossibleAxisNames().contains(name)) {
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

		builder.deleteCharAt(builder.length() - 1);

		Pattern pattern = Pattern.compile(builder.toString());

		for (String dataset : datasetNames) {
			Matcher matcher = pattern.matcher(dataset);
			if (matcher.matches()) {
				for (int i = 1; i < matcher.groupCount() + 1; i++) {
					if (matcher.group(i) != null && found[i - 1] == null) {
						found[i - 1] = matcher.group(i);
					}
				}
			}
		}
		return found;
	}

	public Map<String, ISpectrumFile> getSpectrumFiles() {
		return spectrumFiles;
	}
}
