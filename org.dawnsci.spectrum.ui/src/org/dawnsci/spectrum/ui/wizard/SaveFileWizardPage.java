package org.dawnsci.spectrum.ui.wizard;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.wizard.ResourceChoosePage;
import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.processing.AbstractProcess;
import org.dawnsci.spectrum.ui.processing.AbstractSaveProcess;
import org.dawnsci.spectrum.ui.processing.SaveProcess;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public class SaveFileWizardPage extends ResourceChoosePage implements ISpectrumWizardPage{
	
	AbstractSaveProcess process;
	
	public SaveFileWizardPage(AbstractSaveProcess process) {
		super("Save file wizard", "Save processed data to file", null);
		setDirectory(false);
		setOverwriteVisible(false);
		setNewFile(true);
		setPathEditable(true);
    	setFileLabel("Output file");
    	this.process = process;
    	setPath(System.getProperty("user.home") +File.separator+ process.getDefaultName());
	}
	
	
	public List<IContain1DData> process(List<IContain1DData> dataList) {
		process.setPath(getAbsoluteFilePath());
		return process.process(dataList);
	}

}
